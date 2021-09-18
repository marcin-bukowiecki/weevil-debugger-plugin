/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.debugger.ui

import com.bukowiecki.weevil.psi.CodeEvent
import com.bukowiecki.weevil.psi.ExprCaptor
import com.bukowiecki.weevil.psi.MethodReturnExprCaptor
import com.bukowiecki.weevil.settings.WeevilDebuggerSettings
import com.intellij.debugger.DebuggerContext
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl
import com.intellij.debugger.ui.tree.render.DescriptorLabelListener
import com.intellij.debugger.ui.tree.render.Renderer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiExpression
import com.intellij.psi.impl.JavaPsiFacadeImpl
import com.sun.jdi.Value

fun ValueDescriptorImpl.isSupported(): Boolean {
    return if (this is WeevilBaseValueDescriptorImpl) {
        this.isSupported()
    } else {
        true
    }
}

/**
 * @author Marcin Bukowiecki
 */
abstract class WeevilBaseValueDescriptorImpl(project: Project, latestValue: Value?) :
    ValueDescriptorImpl(project, latestValue) {

    open fun accept(visitor: WeevilDescriptorVisitor) {

    }

    open fun isSupported() = true
}

/**
 * @author Marcin Bukowiecki
 */
interface NamedValueDescriptor {
    val simpleName: String
}

/**
 * @author Marcin Bukowiecki
 */
open class WeevilNamedValueDescriptorImpl(
    project: Project,
    override val simpleName: String,
    value: Value?
) : WeevilBaseValueDescriptorImpl(project, value), NamedValueDescriptor {

    override fun getDescriptorEvaluation(context: DebuggerContext?): PsiExpression {
        return JavaPsiFacadeImpl.getElementFactory(project).createExpressionFromText(name, null)
    }

    override fun calcValue(evaluationContext: EvaluationContextImpl?): Value? {
        return value
    }

    override fun getName(): String {
        return simpleName
    }

    override fun getLastLabelRenderer(): Renderer {
        return WeevilXValuePresentationProviderImpl()
    }
}

/**
 * @author Marcin Bukowiecki
 */
open class ExceptionDescriptorImpl(
    project: Project,
    simpleName: String,
    value: Value?,
    val codeEvent: CodeEvent,
    val history: List<Value?>
) : WeevilNamedValueDescriptorImpl(project, simpleName, value) {

    override fun accept(visitor: WeevilDescriptorVisitor) {
        visitor.visit(this)
    }

    override fun calcRepresentation(context: EvaluationContextImpl?, labelListener: DescriptorLabelListener?): String {
        return super.calcRepresentation(context, labelListener)
    }
}

/**
 * @author Marcin Bukowiecki
 */
open class MethodReturnValueDescriptorImpl(
    project: Project,
    simpleName: String,
    value: Value?,
    val codeEvent: MethodReturnExprCaptor,
    val history: List<Value?>
) : WeevilNamedValueDescriptorImpl(project, simpleName, value) {

    override fun accept(visitor: WeevilDescriptorVisitor) {
        visitor.visit(this)
    }

    override fun isSupported(): Boolean {
        return WeevilDebuggerSettings.getInstance(project).showMethodReturnValues
    }
}

/**
 * @author Marcin Bukowiecki
 */
open class ExpressionValueDescriptorImpl(
    project: Project,
    simpleName: String,
    value: Value?,
    val codeEvent: ExprCaptor,
    val history: List<Value?>
) : WeevilNamedValueDescriptorImpl(project, simpleName, value) {

    override fun accept(visitor: WeevilDescriptorVisitor) {
        visitor.visit(this)
    }

    override fun isSupported(): Boolean {
        return WeevilDebuggerSettings.getInstance(project).showSingleReferences
    }
}

/**
 * @author Marcin Bukowiecki
 */
open class LogicalBinaryExpressionValueDescriptorImpl(
    project: Project,
    simpleName: String,
    value: Value?,
    val codeEvent: ExprCaptor,
    val history: List<Value?>
) : WeevilNamedValueDescriptorImpl(project, simpleName, value) {

    override fun accept(visitor: WeevilDescriptorVisitor) {
        visitor.visit(this)
    }

    override fun isSupported(): Boolean {
        return true
    }
}
