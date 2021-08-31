/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.debugger.controller

import com.bukowiecki.weevil.bundle.WeevilDebuggerBundle
import com.bukowiecki.weevil.debugger.EvaluateEngine
import com.bukowiecki.weevil.debugger.WeevilEvaluateContext
import com.bukowiecki.weevil.debugger.WeevilEvaluateContextProvider
import com.bukowiecki.weevil.debugger.engine.WeevilJavaExecutionStack
import com.bukowiecki.weevil.debugger.engine.WeevilJavaValue
import com.bukowiecki.weevil.debugger.ui.WeevilDescriptorCreator
import com.bukowiecki.weevil.debugger.ui.isSupported
import com.bukowiecki.weevil.inlay.WeevilDebuggerInlayUtil
import com.bukowiecki.weevil.listeners.WeevilDebuggerListener
import com.bukowiecki.weevil.psi.PsiUtils
import com.bukowiecki.weevil.psi.visitors.PsiExtractor
import com.bukowiecki.weevil.services.WeevilDebuggerService
import com.bukowiecki.weevil.services.WeevilResultService
import com.bukowiecki.weevil.ui.WeevilDebuggerSessionTab
import com.bukowiecki.weevil.utils.WeevilDebuggerDataKey
import com.bukowiecki.weevil.utils.WeevilDebuggerUtils
import com.bukowiecki.weevil.xdebugger.WeevilDebuggerRootNode
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.debugger.engine.JavaDebugProcess
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.jdi.ThreadReferenceProxyImpl
import com.intellij.lang.jvm.JvmModifier
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiImportStatement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiType
import com.intellij.xdebugger.frame.XValueChildrenList
import com.intellij.xdebugger.impl.XDebugSessionImpl
import com.sun.jdi.Value
import java.lang.ref.WeakReference

val predefinedImports = listOf(
    "java.util.Map",
    "java.util.HashMap",
    "java.util.Arrays",
    "java.util.ArrayList",
    "java.util.List",
    "java.util.function.Function",
    "java.util.function.Consumer",
    "java.util.concurrent.ConcurrentHashMap",
    "java.util.concurrent.atomic.AtomicInteger"
).joinToString(separator = ",")

/**
 * @author Marcin Bukowiecki
 */
class SessionController(
    private val myDebugProcess: JavaDebugProcess,
    private val mySession: XDebugSessionImpl,
    private val myProject: Project,
    private val myViewRef: WeakReference<WeevilDebuggerSessionTab>
) : Disposable {

    private val log = Logger.getInstance(SessionController::class.java)

    private val myWeevilDebuggerService = WeevilDebuggerService.getInstance(myProject)
    private val myMessageBus = myProject.messageBus
    private val myConnection = myProject.messageBus.connect()
    private val myLock = Object()

    @Volatile
    private var myEvalState: EvalState? = null

    init {
        myConnection.subscribe(myWeevilDebuggerService.topic, object : WeevilDebuggerListener {

            override fun sessionPaused() {
                restoreState()
            }

            override fun evaluateFuture() {
                disableEvaluateButton()
                WeevilDebuggerInlayUtil.removeBlockInlays(myProject, myWeevilDebuggerService.allBlockInlayTypes)
            }

            override fun futureEvaluated() {
                enableEvaluateButton()
            }
        })
    }

    @Suppress("UnstableApiUsage")
    fun evaluate() {
        try {
            myDebugProcess.session.currentStackFrame?.sourcePosition?.let { sourcePosition ->
                val element = WeevilDebuggerUtils.getPlace(myProject, sourcePosition) ?: return@let
                if (!doPreCheck(element)) {
                    return
                }

                WeevilDebuggerUtils.getMethod(myProject, sourcePosition)?.let { psiMethod ->
                    if (psiMethod.hasModifier(JvmModifier.NATIVE)) {
                        showError("Can't evaluate native method")
                        myMessageBus.syncPublisher(myWeevilDebuggerService.topic).futureEvaluated()
                        return
                    }

                    WeevilEvaluateContextProvider.setupContext(psiMethod, element)?.let { weevilContext ->
                        val importedData = getImportedData(weevilContext)

                        EvaluateEngine(myDebugProcess, mySession).evaluate(
                            weevilContext,
                            buildImports(importedData),
                            object : EvaluateCallback {

                                override fun onError(errorsMessage: String) {
                                    myMessageBus.syncPublisher(myWeevilDebuggerService.topic).futureEvaluated()
                                    myEvalState = null
                                    log.info("Error while evaluating future: $errorsMessage")
                                    showError(errorsMessage)
                                }

                                override fun onSuccess(
                                    weevilContext: WeevilEvaluateContext,
                                    evalContext: EvaluationContextImpl
                                ) {
                                    myMessageBus.syncPublisher(myWeevilDebuggerService.topic).futureEvaluated()
                                    myEvalState = EvalState(weevilContext, evalContext)
                                    populateThreadList()
                                    showTree()
                                }
                            }
                        )
                    }
                } ?: kotlin.run {
                    showError("Can evaluate future only in method bodies")
                    myMessageBus.syncPublisher(myWeevilDebuggerService.topic).futureEvaluated()
                }
            }
        } catch (e: Exception) {
            log.info("Exception while evaluating future", e)
            enableEvaluateButton()
        }
    }

    fun showError(message: String) {
        val tabView = myViewRef.get() ?: return
        val tree = tabView.getTree()
        val rootNode = WeevilDebuggerRootNode(tree)
        rootNode.setErrorMessage(message)
        tree.setRoot(rootNode, true)
        tree.isVisible = true
        repaintTree()
        myDebugProcess.session.rebuildViews()
    }

    fun showTree() {
        val currentState = myEvalState ?: return

        val weevilContext = currentState.weevilContext
        val evalContext = currentState.evalContext
        val selectedExecutionStack: WeevilJavaExecutionStack? = currentState.selectedExecutionStack.get()
        val eventsResult = if (selectedExecutionStack != null) {
            weevilContext.findThreadDataByThreadId(selectedExecutionStack.threadId)
        } else {
            weevilContext.findMainThreadData()
        } ?: return

        val tabView = myViewRef.get() ?: return

        ApplicationManager.getApplication().runReadAction {
            tabView.futureValuesView.setCurrentFile(currentState.weevilContext.place.containingFile)
        }

        val children = XValueChildrenList()
        val eventMap = eventsResult.events

        var errorToAdd = {  }

        for (entry in eventMap.entries) {
            val event = weevilContext.findEventById(entry.key) ?: continue

            PsiExtractor().visitCodeEvent(event).psi?.putCopyableUserData(
                WeevilDebuggerDataKey.weevilResultDataKey,
                PsiValueHolder(WeakReference(entry.value.lastOrNull()?.value))
            )

            val descriptor = WeevilDescriptorCreator(
                myProject,
                entry.value
            ).visitCodeEvent(event).descriptor ?: continue

            if (!descriptor.isSupported()) continue

            val javaValue = WeevilJavaValue(
                null,
                event.valueName,
                descriptor,
                evalContext,
                myDebugProcess.nodeManager,
                false
            )

            if (event.type == PsiType.BOOLEAN) {
                WeevilResultService.getInstance(myProject).addBooleanResult(event)
            }

            if (event.isError()) {
                errorToAdd = {
                    children.add(event.valueName, javaValue)
                }
                continue
            }

            children.add(event.valueName, javaValue)
        }

        errorToAdd.invoke()

        ApplicationManager.getApplication().runReadAction {
            DaemonCodeAnalyzer.getInstance(myProject).restart(weevilContext.place.containingFile)
        }

        synchronized(myLock) {
            val tree = tabView.getTree()
            val rootNode = WeevilDebuggerRootNode(tree)
            rootNode.addChildren(children, true)
            tree.setRoot(rootNode, false)
            tree.revalidate()
            tree.isVisible = true
        }

        myDebugProcess.session.rebuildViews()
    }

    fun populateThreadList() {
        val currentState = myEvalState ?: return

        val weevilContext = currentState.weevilContext
        val threadsResult = weevilContext.threadsResult
        val evalContext = currentState.evalContext

        val currentDebugProcess = JavaDebugProcess.getCurrentDebugProcess(myProject) ?: return
        val virtualMachineProxy = currentDebugProcess.virtualMachineProxy
        val tabView = myViewRef.get() ?: return

        val currentThreadId = currentState.evalContext.suspendContext.thread?.let {
            WeevilDebuggerUtils.getThreadId(it.threadReference, evalContext)
        } ?: -1

        threadsResult.forEach { threadResult ->
            val threadReferenceProxyImpl = ThreadReferenceProxyImpl(virtualMachineProxy, threadResult.threadReference)
            val name = threadResult.threadReference.name()
            val current = name == "main"

            val weevilJavaExecutionStack = WeevilJavaExecutionStack(
                threadReferenceProxyImpl,
                currentDebugProcess,
                current,
                threadResult.threadId
            )

            tabView.evaluateView.myThreadComboBox.addItem(weevilJavaExecutionStack)

            if (threadResult.threadId == currentThreadId) {
                currentState.selectedExecutionStack = WeakReference(weevilJavaExecutionStack)
            }
        }
    }

    fun enableEvaluateButton() {
        val tabView = myViewRef.get() ?: return
        tabView.evaluateView.evaluateButton.text = WeevilDebuggerBundle.message("weevil.debugger.evaluateFuture")
        tabView.evaluateView.evaluateButton.isEnabled = true
    }

    fun restoreState() {
        showTree()
    }

    private fun disableEvaluateButton() {
        val tabView = myViewRef.get() ?: return
        tabView.evaluateView.evaluateButton.text = WeevilDebuggerBundle.message("weevil.debugger.evaluating")
        tabView.evaluateView.evaluateButton.isEnabled = false
    }

    private fun repaintTree() {
        val tabView = myViewRef.get() ?: return
        tabView.futureValuesView.getTree().repaint()
        tabView.futureValuesView.getPanel().revalidate()
        tabView.futureValuesView.getPanel().repaint()
    }

    override fun dispose() {
        myConnection.dispose()
        Disposer.dispose(this)
    }

    private fun getImportedData(weevilContext: WeevilEvaluateContext): String {
        return (weevilContext.place.containingFile as PsiJavaFile).importList?.allImportStatements?.map { imp ->
                (imp as? PsiImportStatement)?.let { importStmt ->
                    if (PsiUtils.isWildcardImport(importStmt)) {
                        importStmt.qualifiedName + ".*"
                    } else {
                        importStmt.qualifiedName
                    }
                } ?: ""
            }?.filter { it.isNotEmpty() }?.toList()?.joinToString(separator = ",") ?: ""
    }

    private fun doPreCheck(element: PsiElement): Boolean {
        if (!myWeevilDebuggerService.isLanguageSupported(element.language)) {
            showError("Language: ${element.language.displayName} is not supported.")
            myMessageBus.syncPublisher(myWeevilDebuggerService.topic).futureEvaluated()
            return false
        }

        if (!WeevilDebuggerUtils.getJavaVersion(myDebugProcess).isEvaluateFutureSupported()) {
            showError("At least Java 1.8 is required.")
            myMessageBus.syncPublisher(myWeevilDebuggerService.topic).futureEvaluated()
            return false
        }

        if (!myWeevilDebuggerService.isLanguageSupported(element.language)) {
            showError("Language: ${element.language.displayName} is not supported.")
            myMessageBus.syncPublisher(myWeevilDebuggerService.topic).futureEvaluated()
            return false
        }

        return true
    }
}

/**
 * @author Marcin Bukowiecki
 */
data class EvalState(
    val weevilContext: WeevilEvaluateContext,
    val evalContext: EvaluationContextImpl,

    var selectedExecutionStack: WeakReference<WeevilJavaExecutionStack> = WeakReference(null)
)

fun buildImports(otherImports: String): String {
    if (otherImports.isEmpty()) return predefinedImports
    return "$predefinedImports,$otherImports"
}

/**
 * @author Marcin Bukowiecki
 */
interface EvaluateCallback {

    fun onError(errorsMessage: String)

    fun onSuccess(weevilContext: WeevilEvaluateContext, evalContext: EvaluationContextImpl)
}

/**
 * @author Marcin Bukowiecki
 */
class PsiValueHolder(val ref: WeakReference<Value?>)