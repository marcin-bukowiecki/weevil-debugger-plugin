/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.debugger.ui

import com.bukowiecki.weevil.psi.*
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl
import com.intellij.lang.jvm.types.JvmPrimitiveTypeKind
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiPrimitiveType

/**
 * @author Marcin Bukowiecki
 */
class WeevilDescriptorCreator(
    private val project: Project,
    private val values: List<EvalRecord>
) : BaseEventVisitor() {

    var descriptor: ValueDescriptorImpl? = null

    fun visitCodeEvent(codeEvent: CodeEvent): WeevilDescriptorCreator {
        ApplicationManager
            .getApplication()
            .runReadAction {
                codeEvent.accept(this)
            }
        return this
    }

    override fun visit(codeEvent: ForEachEvent) {
        val history = values.map { it.value }
        this.descriptor = PredictedLocalVariableDescriptorImpl(
            codeEvent,
            project,
            history.last(),
            history
        )
    }

    override fun visit(codeEvent: SetLocalEvent) {
        val history = values.map { it.value }
        this.descriptor = PredictedLocalVariableDescriptorImpl(
            codeEvent,
            project,
            history.last(),
            history
        )
    }

    override fun visit(codeEvent: ExceptionEventHolder) {
        this.descriptor = ExceptionDescriptorImpl(
            project,
            codeEvent.valueName,
            codeEvent.exceptionRef,
            codeEvent,
            listOf(codeEvent.exceptionRef)
        )
    }

    override fun visit(codeEvent: MethodReturnExprCaptor) {
        val history = values.map { it.value }
        this.descriptor = MethodReturnValueDescriptorImpl(
            project,
            ApplicationManager.getApplication().runReadAction<String, Throwable> { codeEvent.text },
            history.last(),
            codeEvent,
            history
        )
    }

    @Suppress("UnstableApiUsage")
    override fun visit(codeEvent: ExprCaptor) {
        val type = codeEvent.type
        this.descriptor = if (type != null && type is PsiPrimitiveType && type.kind == JvmPrimitiveTypeKind.BOOLEAN) {
            val history = values.map { it.value }
            BooleanDescriptorImpl(
                codeEvent,
                project,
                history.last(),
                history
            )
        } else {
            val history = values.map { it.value }
            ExpressionValueDescriptorImpl(
                project,
                codeEvent.valueName,
                history.last(),
                codeEvent,
                history
            )
        }
    }

    override fun visit(codeEvent: ThrowExprCaptor) {
        visit(codeEvent as ExprCaptor)
    }
}