/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.shell

import com.bukowiecki.weevil.actions.shell.*
import com.bukowiecki.weevil.annotator.activeColor
import com.bukowiecki.weevil.annotator.falseColor
import com.bukowiecki.weevil.bundle.WeevilDebuggerBundle
import com.bukowiecki.weevil.debugger.engine.WeevilErrorTreeNode
import com.bukowiecki.weevil.listeners.WeevilDebuggerListener
import com.bukowiecki.weevil.services.WeevilDebuggerService
import com.bukowiecki.weevil.shell.util.ShellTabUtil
import com.bukowiecki.weevil.utils.WeevilDebuggerUtils
import com.bukowiecki.weevil.xdebugger.WeevilDebuggerRootNode
import com.intellij.debugger.engine.DebuggerManagerThreadImpl
import com.intellij.debugger.engine.JavaValue
import com.intellij.debugger.engine.JavaValuePresentation
import com.intellij.debugger.impl.DebuggerUtilsEx
import com.intellij.debugger.impl.PrioritizedTask
import com.intellij.debugger.ui.tree.render.DescriptorLabelListener
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.impl.JavaPsiFacadeImpl
import com.intellij.ui.components.JBLabel
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.ui.JBUI
import com.intellij.xdebugger.XDebuggerBundle
import com.intellij.xdebugger.XExpression
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.frame.XValueChildrenList
import com.intellij.xdebugger.impl.XDebugSessionImpl
import com.intellij.xdebugger.impl.actions.XDebuggerActions
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTree
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTreePanel
import com.intellij.xdebugger.impl.ui.tree.nodes.XValuePresentationUtil
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * @author Marcin Bukowiecki
 */
class ShellTab(val project: Project,
               val session: XDebugSessionImpl,
               var sourcePosition: XSourcePosition): Disposable {

    private val myEditor: ShellCodeEditor
    private val myTreePanel: XDebuggerTreePanel
    private val myHeaderMainPanel = JPanel(BorderLayout())
    private val myConnection: MessageBusConnection = project.messageBus.connect()
    private val myToolbarGroup: DefaultActionGroup
    private val myToolbar: ActionToolbar
    private val myActiveLabel: JBLabel
    private val myFilePathLabel: JBLabel
    private val myResultPanel: JPanel
    private val myLock = Object()
    private val myForm = ShellEvaluateForm()

    val controller = ShellController(this)

    @Volatile
    var myHistory = listOf<ShellEvalHistoryEntry>()

    val myMainPanel = JPanel(BorderLayout())
    val myEditorsProvider = session.debugProcess.editorsProvider

    init {
        this.myHeaderMainPanel.border = BorderFactory.createEmptyBorder(0, 100, 0, 25)

        this.myEditor = ShellCodeEditor(this,
            this.myForm.evaluateMainPanel,
            project,
            myEditorsProvider,
            sourcePosition,
            ShellTabUtil.getCurrentLanguage(sourcePosition, project) ?: JavaLanguage.INSTANCE
        )
        Disposer.register(this, this.myEditor)

        this.myToolbarGroup = DefaultActionGroup()
        this.myToolbarGroup.add(ExecuteShellAction().withForceShow().withEditor(myEditor))
        this.myToolbarGroup.add(InspectShellExpression().withForceShow().withEditor(myEditor))
        this.myToolbarGroup.add(RefreshAction().withForceShow().withEditor(myEditor))
        this.myToolbarGroup.add(ClearAction().withForceShow().withEditor(myEditor))
        this.myToolbarGroup.add(ClearHistoryAction().withForceShow().withEditor(myEditor))

        this.myToolbar = ActionManager
            .getInstance()
            .createActionToolbar("WeevilDebuggerShell", myToolbarGroup, true)

        this.myHeaderMainPanel.add(myToolbar.component, BorderLayout.WEST)
        this.myToolbar.setTargetComponent(myHeaderMainPanel)

        this.myTreePanel = XDebuggerTreePanel(
            project, myEditorsProvider, this, sourcePosition, XDebuggerActions.EVALUATE_DIALOG_TREE_POPUP_GROUP,
            session.valueMarkers
        )
        getTree().setRoot(WeevilDebuggerRootNode(getTree()), false)

        this.myResultPanel = JBUI.Panels.simplePanel()
            .addToTop(JLabel(XDebuggerBundle.message("xdebugger.evaluate.label.result")))
            .addToCenter(myTreePanel.mainPanel)
        this.myResultPanel.border = BorderFactory.createEmptyBorder(0, 5, 0, 5)

        this.myMainPanel.add(this.myHeaderMainPanel, BorderLayout.NORTH)

        val contentPanel = JBUI.Panels.simplePanel()
        this.myMainPanel.add(contentPanel, BorderLayout.CENTER)
        contentPanel.add(this.myForm.evaluateMainPanel, BorderLayout.CENTER)
        contentPanel.add(this.myResultPanel, BorderLayout.SOUTH)

        this.myForm.evaluateTextPanel.add(myEditor.component)

        this.myEditor.addComponent(contentPanel, myResultPanel, vertical = false)

        this.myActiveLabel = JBLabel(WeevilDebuggerBundle.message("weevil.debugger.shell.status.active"))
        this.myActiveLabel.foreground = activeColor
        this.myFilePathLabel = JBLabel("${sourcePosition.file.url} at ${sourcePosition.line + 1}")

        this.myHeaderMainPanel.add(myActiveLabel, BorderLayout.CENTER)
        this.myHeaderMainPanel.add(myFilePathLabel, BorderLayout.EAST)

        installDebuggerListener()
    }

    fun getExpression(): XExpression {
        return myEditor.expression
    }

    fun addError(selectedText: String?, message: String) {
        val name = if (selectedText.isNullOrEmpty()) {
            "result"
        } else {
            selectedText
        }
        val tree = getTree()
        val root = getTree().root as WeevilDebuggerRootNode
        root.addChildren(XValueChildrenList.singleton(name, WeevilErrorTreeNode(name, message)), false)
        tree.revalidate()
        tree.isVisible = true
    }

    fun reload(language: Language, sourcePosition: XSourcePosition) {
        ApplicationManager.getApplication().runReadAction {
            this.myEditor.setSourcePosition(sourcePosition)
            val currentExpression = this.myEditor.expression
            val newExpression = XExpressionImpl(
                currentExpression.expression,
                language,
                currentExpression.customInfo,
                currentExpression.mode
            )
            this.myEditor.expression = newExpression
            this.sourcePosition = sourcePosition
            this.myTreePanel.tree.sourcePosition = sourcePosition
            markActive()
        }
    }

    override fun dispose() {
        myConnection.dispose()
        myHistory = listOf()
        Disposer.dispose(this)
    }

    @Suppress("unused")
    fun isDeprecated(): Boolean {
        WeevilDebuggerUtils.getCurrentSession(project)?.let { session ->
            val givenSourcePosition = WeevilDebuggerUtils.getCurrentBreakpoint(session)?.sourcePosition ?: return true
            return givenSourcePosition.file.url != sourcePosition.file.url || givenSourcePosition.line != sourcePosition.line
        }
        return true
    }

    @Suppress("unused")
    fun appendResultComment(editor: Editor, expression: String, javaValue: JavaValue) {
        val selectionEnd = editor.selectionModel.selectionEnd
        val document = editor.document
        val lineNumber = document.getLineNumber(selectionEnd)

        PsiDocumentManager.getInstance(project).getPsiFile(document)?.let { psiFile ->
            WriteCommandAction.runWriteCommandAction(project) {
                ShellTabUtil.removeExistingComment(expression, psiFile)
            }

            DebuggerManagerThreadImpl.createTestInstance(this, project).invoke(PrioritizedTask.Priority.HIGH) {

                javaValue.descriptor.updateRepresentation(javaValue.evaluationContext, DescriptorLabelListener.DUMMY_LISTENER)

                val javaValuePresentation = JavaValuePresentation(javaValue.descriptor)
                val computeValueText = XValuePresentationUtil.computeValueText(javaValuePresentation)
                if (computeValueText.isEmpty()) return@invoke

                val comment = "// Evaluation result: $expression = $computeValueText"
                val createCommentFromText = WeevilDebuggerUtils.readAction {
                    JavaPsiFacadeImpl.getElementFactory(project).createCommentFromText(comment, null)
                }

                val startOffset = document.getLineStartOffset(lineNumber)
                val endOffset = document.getLineEndOffset(lineNumber)

                var i = 1
                while (true) {
                    val element = psiFile.findElementAt(endOffset-i)
                    if (element != null) {
                        WriteCommandAction.runWriteCommandAction(project) {
                            element.parent.addAfter(createCommentFromText, element)
                            PsiDocumentManager.getInstance(project).commitDocument(document)
                        }
                        break
                    }
                    if (startOffset == endOffset - 1) {
                        break
                    }
                    i++
                }
            }
        }
    }

    fun addToTree(expression: String?, result: JavaValue) {
        synchronized(myLock) {
            val name = if (expression.isNullOrEmpty()) {
                "result"
            } else {
                expression.trim()
            }

            val shellEvalHistoryEntry = ShellEvalHistoryEntry(name, result)
            myHistory = listOf(shellEvalHistoryEntry) + myHistory

            val root = getTree().root as WeevilDebuggerRootNode
            root.addChildren(XValueChildrenList.singleton(name, result), false)
            getTree().revalidate()
            getTree().isVisible = true
        }
    }

    fun clearTree() {
        val tree = getTree()
        tree.setRoot(WeevilDebuggerRootNode(getTree()), false)
    }

    private fun installDebuggerListener() {
        this.myConnection.subscribe(WeevilDebuggerService.getInstance(project).topic, object : WeevilDebuggerListener {

            override fun sessionStopped() {
                session.ui.findContent(contentId)?.let {
                    session.ui.contentManager.removeContent(it, true)
                }
            }

            @Suppress("UnstableApiUsage")
            override fun processStopped() {
                ApplicationManager.getApplication().invokeLaterOnWriteThread {
                    session.ui.findContent(contentId)?.let {
                        session.ui.contentManager.removeContent(it, true)
                    }
                }
            }

            override fun sessionPaused() {
                ApplicationManager.getApplication().invokeLater {
                    WeevilDebuggerUtils.getCurrentSession(project)?.let { session ->
                        val breakpoint = WeevilDebuggerUtils.getCurrentBreakpoint(session)
                        val sourcePosition = if (breakpoint == null) {
                            val topFrame = session.currentExecutionStack.topFrame ?: kotlin.run {
                                markDeprecated()
                                return@let
                            }

                            topFrame.sourcePosition
                        } else {
                            breakpoint.sourcePosition
                        } ?: kotlin.run {
                            markDeprecated()
                            return@let
                        }

                        DebuggerUtilsEx.getPsiFile(sourcePosition, project)?.let { psiFile ->
                            reload(psiFile.language, sourcePosition)
                        } ?: kotlin.run {
                            markDeprecated()
                            return@let
                        }
                    }
                }
            }
        })
    }

    private fun markDeprecated() {
        myActiveLabel.text = WeevilDebuggerBundle.message("weevil.debugger.shell.status.deprecated")
        myActiveLabel.foreground = falseColor
    }

    private fun markActive() {
        myActiveLabel.text = WeevilDebuggerBundle.message("weevil.debugger.shell.status.active")
        myActiveLabel.foreground = activeColor
        myFilePathLabel.text = "${sourcePosition.file.url} at ${sourcePosition.line + 1}"
    }

    private fun getTree(): XDebuggerTree {
        return myTreePanel.tree
    }

    companion object {

        const val contentId = "WeevilDebugger.Shell.Tab"
    }
}