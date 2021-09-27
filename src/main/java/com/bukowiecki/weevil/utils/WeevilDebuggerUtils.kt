/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.utils

import com.bukowiecki.weevil.psi.PsiUtils
import com.bukowiecki.weevil.services.WeevilDebuggerService
import com.bukowiecki.weevil.shell.ShellTab
import com.intellij.debugger.engine.DebugProcessImpl
import com.intellij.debugger.engine.JavaDebugProcess
import com.intellij.debugger.engine.JavaExecutionStack
import com.intellij.debugger.engine.JavaValue
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.engine.evaluation.expression.IdentityEvaluator
import com.intellij.debugger.engine.evaluation.expression.MethodEvaluator
import com.intellij.debugger.engine.events.DebuggerCommandImpl
import com.intellij.debugger.impl.DebuggerUtilsImpl
import com.intellij.debugger.jdi.ThreadReferenceProxyImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdkVersion
import com.intellij.openapi.util.Ref
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.util.ReflectionUtil
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.impl.XDebugSessionImpl
import com.intellij.xdebugger.impl.XDebuggerManagerImpl
import com.intellij.xdebugger.impl.breakpoints.XBreakpointBase
import com.sun.jdi.LongValue
import com.sun.jdi.ObjectReference
import com.sun.jdi.ThreadReference
import com.sun.jdi.Value
import org.jetbrains.uast.util.hasClassOf

/**
 * @author Marcin Bukowiecki
 */
object WeevilDebuggerUtils {

    private const val collectionCanonicalPath = "java.util.Collection"

    private const val mapCanonicalPath = "java.util.Map"

    private const val listCanonicalPath = "java.util.List"

    fun typesSame(left: Value?, right: Value?): Boolean {
        if (right == null || left == null) return false
        return left.type().equals(right.type())
    }

    fun getActiveThread(javaExecutionStack: JavaExecutionStack): ThreadReferenceProxyImpl? {
        val declaredField = ReflectionUtil
            .getDeclaredField(JavaExecutionStack::class.java, "myThreadProxy") ?: return null
        declaredField.isAccessible = true
        return declaredField.get(javaExecutionStack) as? ThreadReferenceProxyImpl
    }

    fun getJavaVersion(process: JavaDebugProcess): JavaSdkVersionWrapper {
        return getJavaVersion(process.debuggerSession.process)
    }

    fun getJavaVersion(javaValue: JavaValue): JavaSdkVersionWrapper {
        return getJavaVersion(javaValue.evaluationContext.debugProcess)
    }

    private fun getJavaVersion(debugProcessImpl: DebugProcessImpl): JavaSdkVersionWrapper {
        val ref = Ref<JavaSdkVersionWrapper>()
        debugProcessImpl.managerThread.invokeAndWait(object : DebuggerCommandImpl() {

            override fun action() {
                ref.set(JavaSdkVersionWrapper(
                    JavaSdkVersion.fromVersionString(debugProcessImpl.virtualMachineProxy.version())
                ))
            }
        })
        return ref.get()
    }

    fun getThreadId(ref: ThreadReference, evaluationContext: EvaluationContextImpl): Long {
        val me = MethodEvaluator(IdentityEvaluator(ref), null, "getId", null, arrayOfNulls(0))
        return (me.evaluate(evaluationContext) as LongValue).value()
    }

    fun getCurrentSession(project: Project): XDebugSessionImpl? {
        return XDebuggerManagerImpl.getInstance(project).currentSession as? XDebugSessionImpl
    }

    fun getShellTab(session: XDebugSessionImpl): ShellTab? {
        return session.sessionData.getUserData(WeevilDebuggerDataKey.weevilDebuggerShellDataKeys)
    }

    fun getCurrentBreakpoint(session: XDebugSessionImpl): XBreakpointBase<*, *, *>? {
        val breakpoints = getBreakpoints(session)
        val topFramePosition = session.topFramePosition
        val line = topFramePosition?.line ?: return null
        val file = getContainingFile(session) ?: return null

        return ApplicationManager.getApplication().runReadAction<XBreakpointBase<*, *, *>?, Throwable> {
            for (b in breakpoints) {
                if (session.isBreakpointActive(b) &&
                    b is XBreakpointBase<*, *, *> &&
                    b.getSourcePosition()?.line == line && b.getSourcePosition()?.file?.url == file.url
                ) {

                    return@runReadAction b
                }
            }

            return@runReadAction null
        }
    }

    fun getContainingFile(session: XDebugSessionImpl): VirtualFile? {
        val topFramePosition = session.topFramePosition ?: return null
        return topFramePosition.file
    }

    fun getBreakpoints(session: XDebugSessionImpl): List<XBreakpoint<*>> {
        val breakpointClasses =
            WeevilDebuggerService.getInstance(session.project).javaBreakpointClassesForSetup()

        return ApplicationManager.getApplication().runReadAction<List<XBreakpoint<*>>, Throwable> {
            XDebuggerManager.getInstance(session.project).breakpointManager
                .allBreakpoints
                .filter { breakpointClasses.hasClassOf(it.type) }.toList()
        }
    }

    fun isCollection(ref: ObjectReference, evaluationContext: EvaluationContextImpl): Boolean {
        val collectionType = evaluationContext.debugProcess.findClass(
            evaluationContext,
            collectionCanonicalPath,
            evaluationContext.classLoader
        )
        return DebuggerUtilsImpl.instanceOf(
            ref.referenceType(),
            collectionType
        )
    }

    fun isList(ref: ObjectReference, evaluationContext: EvaluationContextImpl): Boolean {
        val collectionType = evaluationContext.debugProcess.findClass(
            evaluationContext,
            listCanonicalPath,
            evaluationContext.classLoader
        )
        return DebuggerUtilsImpl.instanceOf(
            ref.referenceType(),
            collectionType
        )
    }

    fun isMap(ref: ObjectReference, evaluationContext: EvaluationContextImpl): Boolean {
        val collectionType = evaluationContext.debugProcess.findClass(
            evaluationContext,
            mapCanonicalPath,
            evaluationContext.classLoader
        )
        return DebuggerUtilsImpl.instanceOf(
            ref.referenceType(),
            collectionType
        )
    }

    fun getMethod(project: Project, sourcePosition: XSourcePosition): PsiMethod? {
        val element = getPlace(project, sourcePosition) ?: return null
        return PsiUtils.findParent(element, PsiMethod::class.java)
    }

    fun getPlace(project: Project, sourcePosition: XSourcePosition): PsiElement? {
        return ApplicationManager.getApplication().runReadAction<PsiElement> {
            val sourceFile = sourcePosition.file
            val psiFile = PsiManager.getInstance(project).findFile(sourceFile) ?: return@runReadAction null
            psiFile.findElementAt(sourcePosition.offset)
        }
    }

    fun <T> readAction(toRun: () -> T): T {
        return ApplicationManager.getApplication().runReadAction<T, Throwable> { toRun.invoke() }
    }
}
