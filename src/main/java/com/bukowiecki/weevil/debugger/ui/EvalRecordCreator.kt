/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.debugger.ui

import com.bukowiecki.weevil.psi.BaseEventVisitor
import com.bukowiecki.weevil.psi.CodeEvent
import com.bukowiecki.weevil.psi.ExceptionEventHolder
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.sun.jdi.IntegerValue
import com.sun.jdi.Value

/**
 * @author Marcin Bukowiecki
 */
class EvalRecordCreator(
    private val eventValue: Value?,
    private val blockName: String,
    private val iteration: IntegerValue,
    private val context: EvaluationContextImpl
) : BaseEventVisitor() {

    var evalRecord: EvalRecord? = null

    override fun visit(codeEvent: CodeEvent) {
        this.evalRecord = EvalRecord(
            codeEvent,
            VariableDescriptorUtils.checkForPrimitive(eventValue, codeEvent, context), blockName, iteration.value()
        )
    }

    override fun visit(codeEvent: ExceptionEventHolder) {
        this.evalRecord = ExceptionEvalRecord(codeEvent, eventValue, blockName, iteration.value())
    }
}