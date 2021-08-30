/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.actions

import com.bukowiecki.weevil.condition.SetupConditionAtBreakpointHandler
import com.bukowiecki.weevil.utils.WeevilDebuggerUtils
import com.intellij.debugger.engine.JavaDebugProcess
import com.intellij.debugger.engine.JavaValue
import com.intellij.debugger.ui.breakpoints.JavaLineBreakpointTypeBase
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorImpl
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.breakpoints.*
import com.intellij.xdebugger.impl.XDebugSessionImpl
import com.intellij.xdebugger.impl.XDebuggerSupport
import com.intellij.xdebugger.impl.breakpoints.XBreakpointBase
import com.intellij.xdebugger.impl.breakpoints.XBreakpointUtil
import com.intellij.xdebugger.impl.ui.tree.actions.XDebuggerTreeActionBase
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl

/**
 * @author Marcin Bukowiecki
 */
class AtCursorAction : XDebuggerTreeActionBase() {

    override fun isEnabled(node: XValueNodeImpl, e: AnActionEvent): Boolean {
        if (node.valueContainer !is JavaValue) return false

        val xdebugProcess = (node.valueContainer as JavaValue).evaluationContext.debugProcess.xdebugProcess
        if (xdebugProcess !is JavaDebugProcess) return false

        val session = xdebugProcess.session as? XDebugSessionImpl ?: return false
        val file = WeevilDebuggerUtils.getContainingFile(session) ?: return false
        val editor = findEditor(session, file) ?: return false
        val line = editor.caretModel.logicalPosition.line
        val breakpoint = WeevilDebuggerUtils.getCurrentBreakpoint(session) ?: return true
        val sourcePosition = breakpoint.sourcePosition ?: return true

        if (sourcePosition.line == line && sourcePosition.file.url == file.url) return false

        return true
    }

    override fun perform(node: XValueNodeImpl, nodeName: String, e: AnActionEvent) {
        val javaValue = node.valueContainer as JavaValue
        val xdebugProcess = (node.valueContainer as JavaValue).evaluationContext.debugProcess.xdebugProcess
        if (xdebugProcess !is JavaDebugProcess) return

        val session = xdebugProcess.session as? XDebugSessionImpl ?: return
        val project = session.project
        val file = WeevilDebuggerUtils.getContainingFile(session) ?: return
        val editor = findEditor(session, file) ?: return
        val breakpointManager = XDebuggerManager.getInstance(project).breakpointManager
        val caretModel = editor.caretModel

        val psiFile = PsiManager.getInstance(project).findFile(file) ?: return
        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(psiFile) ?: return
        val lineNumber = document.getLineNumber(caretModel.visualLineStart)

        val debuggerSupport = XDebuggerSupport.getDebuggerSupport(XDebuggerSupport::class.java)
        val allTypes = XBreakpointUtil.breakpointTypes()

        for (breakpointType in allTypes) {
            if (breakpointType is JavaLineBreakpointTypeBase<*> && breakpointType.canPutAt(file, lineNumber, project)) {
                val breakpointProperties = breakpointType.createBreakpointProperties(file, lineNumber)
                val breakpoint = WriteAction.computeAndWait(ThrowableComputable<XLineBreakpoint<*>, RuntimeException> {
                    breakpointManager.addLineBreakpoint(
                        breakpointType as XLineBreakpointType<XBreakpointProperties<*>>,
                        file.url,
                        lineNumber,
                        breakpointProperties as XBreakpointProperties<*>,
                        false
                    )
                }) as? XBreakpointBase<*, *, *> ?: return

                SetupConditionAtBreakpointHandler.getHandler(javaValue).handle(nodeName, breakpoint, callback = {
                    debuggerSupport.editBreakpointAction.editBreakpoint(
                        session.project,
                        editor,
                        breakpoint,
                        breakpoint.createGutterIconRenderer()
                    )
                })
                return
            }
        }
    }
}

/**
 * @author Marcin Bukowiecki
 */
class AtBreakpoint : XDebuggerTreeActionBase() {

    override fun isEnabled(node: XValueNodeImpl, e: AnActionEvent): Boolean {
        if (node.valueContainer !is JavaValue) return false

        val xdebugProcess = (node.valueContainer as JavaValue).evaluationContext.debugProcess.xdebugProcess
        if (xdebugProcess !is JavaDebugProcess) return false

        val session = xdebugProcess.session as? XDebugSessionImpl ?: return false
        return WeevilDebuggerUtils.getCurrentBreakpoint(session)?.sourcePosition != null
    }

    override fun perform(node: XValueNodeImpl, nodeName: String, e: AnActionEvent) {
        val javaValue = node.valueContainer as JavaValue
        val debugProcess = javaValue.evaluationContext.debugProcess.xdebugProcess as JavaDebugProcess
        val session = debugProcess.session as? XDebugSessionImpl ?: return
        val topFramePosition = session.topFramePosition
        val line = topFramePosition?.line ?: return
        val file = WeevilDebuggerUtils.getContainingFile(session) ?: return
        val breakpoints = WeevilDebuggerUtils.getBreakpoints(session)

        val debuggerSupport = XDebuggerSupport.getDebuggerSupport(XDebuggerSupport::class.java)

        for (breakpoint in breakpoints) {
            if (breakpoint !is XBreakpointBase<*, *, *>) continue
            val sourcePosition = breakpoint.sourcePosition ?: continue

            if (sourcePosition.line == line && sourcePosition.file.url == file.url) {
                findEditor(session, file)?.let { editor ->
                    SetupConditionAtBreakpointHandler.getHandler(javaValue).handle(nodeName, breakpoint, callback = {
                        debuggerSupport.editBreakpointAction.editBreakpoint(
                            session.project,
                            editor,
                            breakpoint,
                            breakpoint.createGutterIconRenderer()
                        )
                    })
                }
                return
            }
        }
    }
}

private fun findEditor(session: XDebugSessionImpl, file: VirtualFile): Editor? {
    val editors = FileEditorManager.getInstance(session.project).allEditors
    for (editor in editors) {
        if (editor is PsiAwareTextEditorImpl && editor.file.path == file.path) {
            return editor.editor
        }
    }
    return null
}