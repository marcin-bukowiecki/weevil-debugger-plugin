/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.debugger.ui

import com.bukowiecki.weevil.debugger.engine.PredictedLocalVariableProxy
import com.bukowiecki.weevil.psi.WithLocalVariable
import com.intellij.debugger.DebuggerContext
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.engine.jdi.LocalVariableProxy
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl
import com.intellij.debugger.ui.tree.LocalVariableDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiExpression
import com.intellij.psi.impl.JavaPsiFacadeImpl
import com.sun.jdi.Value

/**
 * @author Marcin Bukowiecki
 */
class PredictedLocalVariableDescriptorImpl(
    val codeEvent: WithLocalVariable,
    project: Project,
    latestValue: Value?,
    override val history: List<Value?>
) : WeevilBaseValueDescriptorImpl(project, latestValue), LocalVariableDescriptor, WithHistoryDescriptor {

    override fun getDescriptorEvaluation(context: DebuggerContext): PsiExpression {
        return JavaPsiFacadeImpl.getElementFactory(project).createExpressionFromText(codeEvent.name, null)
    }

    override fun getLocalVariable(): LocalVariableProxy {
        return PredictedLocalVariableProxy()
    }

    override fun calcValue(evaluationContext: EvaluationContextImpl?): Value? {
        return value
    }

    override fun getName(): String {
        return codeEvent.name
    }

    override fun accept(visitor: WeevilDescriptorVisitor) {
        visitor.visit(this)
    }
}