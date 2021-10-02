/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.actions.objectdiff

import com.bukowiecki.weevil.objectdiff.ui.ObjectDiffDialog
import com.bukowiecki.weevil.objectdiff.ObjectDiffService
import com.intellij.debugger.engine.JavaDebugProcess
import com.intellij.debugger.engine.JavaValue
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.AppUIUtil
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.impl.XDebugSessionImpl
import com.intellij.xdebugger.impl.frame.XValueMarkers
import com.intellij.xdebugger.impl.ui.tree.actions.XDebuggerTreeActionBase
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl

/**
 * @author Marcin Bukowiecki
 */
class OpenDiffAction : XDebuggerTreeActionBase() {

    override fun perform(node: XValueNodeImpl, nodeName: String, e: AnActionEvent) {
        val project = e.project ?: return
        val currentSession = XDebuggerManager.getInstance(project).currentSession as? XDebugSessionImpl ?: return
        val debugProcess = currentSession.debugProcess as? JavaDebugProcess ?: return
        val xValueMarkers = currentSession.valueMarkers as XValueMarkers<*, *>
        val javaValue = node.valueContainer as? JavaValue ?: return
        val evaluationContext = javaValue.evaluationContext

        AppUIUtil.invokeOnEdt {
            val objectDiffDialog = ObjectDiffDialog(
                project,
                debugProcess,
                xValueMarkers,
                evaluationContext
            )
            objectDiffDialog.show()
        }
    }
}
