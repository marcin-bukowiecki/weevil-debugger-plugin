/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.actions

import com.bukowiecki.weevil.debugger.ui.WithHistoryDescriptor
import com.bukowiecki.weevil.debugger.ui.WithHistoryDescriptorWrapper
import com.bukowiecki.weevil.ui.SearchDialog
import com.intellij.debugger.engine.JavaDebugProcess
import com.intellij.debugger.engine.JavaValue
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.AppUIUtil
import com.intellij.xdebugger.impl.XDebugSessionImpl
import com.intellij.xdebugger.impl.ui.tree.actions.XDebuggerTreeActionBase
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl

/**
 * @author Marcin Bukowiecki
 */
abstract class SearchBaseAction : XDebuggerTreeActionBase() {

    override fun update(e: AnActionEvent) {
        super.update(e)
        val presentation = e.presentation
        presentation.isVisible = true
    }

    override fun perform(node: XValueNodeImpl, nodeName: String, e: AnActionEvent) {
        val javaValue = node.valueContainer as JavaValue
        val descriptor = if (javaValue.descriptor is WithHistoryDescriptor) {
            javaValue.descriptor as WithHistoryDescriptor
        } else {
            WithHistoryDescriptorWrapper(javaValue.descriptor)
        }
        val evaluationContext = javaValue.evaluationContext
        val debugProcess = evaluationContext.debugProcess.xdebugProcess as JavaDebugProcess
        val session = debugProcess.session as? XDebugSessionImpl ?: return

        AppUIUtil.invokeOnEdt {
            createSearchDialog(javaValue, descriptor, debugProcess, session)?.let { dialog ->
                dialog.setupTree()
                dialog.show()
            }
        }
    }

    abstract fun createSearchDialog(
        javaValue: JavaValue,
        descriptor: WithHistoryDescriptor,
        javaDebugProcess: JavaDebugProcess,
        session: XDebugSessionImpl
    ): SearchDialog?
}
