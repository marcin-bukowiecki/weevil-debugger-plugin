/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.actions

import com.intellij.debugger.engine.JavaValue
import com.intellij.debugger.engine.evaluation.expression.IdentityEvaluator
import com.intellij.debugger.engine.evaluation.expression.MethodEvaluator
import com.intellij.debugger.impl.PrioritizedTask
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.xdebugger.impl.ui.tree.actions.XDebuggerTreeActionBase
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl
import com.jetbrains.jdi.ObjectReferenceImpl

/**
 * @author Marcin Bukowiecki
 */
abstract class ClearCollectionBase : XDebuggerTreeActionBase() {

    override fun update(e: AnActionEvent) {
        super.update(e)
        val presentation = e.presentation
        presentation.isVisible = true
    }

    override fun perform(node: XValueNodeImpl, nodeName: String, e: AnActionEvent) {
        val javaValue = node.valueContainer as JavaValue
        val evaluationContext = javaValue.evaluationContext
        val debugProcess = evaluationContext.debugProcess

        debugProcess.managerThread.invoke(PrioritizedTask.Priority.NORMAL) {
            val value = javaValue.descriptor.value as? ObjectReferenceImpl ?: return@invoke
            MethodEvaluator(IdentityEvaluator(value), null, "clear", null, arrayOfNulls(0))
                .evaluate(evaluationContext)
        }
    }
}