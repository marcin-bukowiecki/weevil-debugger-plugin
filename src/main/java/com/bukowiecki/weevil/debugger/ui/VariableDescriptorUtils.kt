/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.debugger.ui

import com.bukowiecki.weevil.debugger.WeevilEvaluateContext
import com.bukowiecki.weevil.psi.CodeEvent
import com.bukowiecki.weevil.psi.ExceptionEventHolder
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.engine.evaluation.expression.FieldEvaluator
import com.intellij.debugger.engine.evaluation.expression.IdentityEvaluator
import com.intellij.debugger.engine.evaluation.expression.MethodEvaluator
import com.intellij.debugger.engine.evaluation.expression.UnBoxingEvaluator
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiPrimitiveType
import com.sun.jdi.*
import java.lang.reflect.InvocationTargetException

/**
 * @author Marcin Bukowiecki
 */
object VariableDescriptorUtils {

    @Suppress("unused")
    fun getLastFromList(ref: Value, context: EvaluationContextImpl): Value {
        val sizeMethodEvaluator = MethodEvaluator(IdentityEvaluator(ref), null, "size", null, arrayOfNulls(0))
        val sizeResult = sizeMethodEvaluator.evaluate(context) as IntegerValue
        return MethodEvaluator(
            IdentityEvaluator(ref), null, "get", null,
            arrayOf(IdentityEvaluator(context.debugProcess.virtualMachineProxy.mirrorOf(sizeResult.value() - 1)))
        ).evaluate(context) as Value
    }

    fun iterateCollection(ref: Value, context: EvaluationContextImpl, iterCallback: (Int, Value?) -> Unit) {
        val it =
            MethodEvaluator(IdentityEvaluator(ref), null, "iterator", null, arrayOfNulls(0)).evaluate(context) as Value
        val hasNextEval = MethodEvaluator(IdentityEvaluator(it), null, "hasNext", null, arrayOfNulls(0))
        val nextEval = MethodEvaluator(IdentityEvaluator(it), null, "next", null, arrayOfNulls(0))
        var i = 0
        while ((hasNextEval.evaluate(context) as BooleanValue).booleanValue()) {
            iterCallback.invoke(i, nextEval.evaluate(context) as Value?)
            i++
        }
    }

    fun getRecord(value: Value?, weevilContext: WeevilEvaluateContext, context: EvaluationContextImpl): EvalRecord? {
        if (value == null) return null

        val eventId = UnBoxingEvaluator(IdentityEvaluator(getFromArray(value, 0, context) as ObjectReference))
            .evaluate(context) as IntegerValue
        val eventValue = getFromArray(value, 1, context)
        val event = if (eventId.value() == Int.MAX_VALUE && eventValue != null && eventValue is ObjectReference) {
            val exceptionEvent = createExceptionEventHolder(weevilContext.place, eventValue, context)
            weevilContext.compilationContext.eventRegister.add(exceptionEvent)
            exceptionEvent
        } else {
            weevilContext.findEventById(eventId.value()) ?: return null
        }
        val blockName = (getFromArray(value, 2, context) as StringReference).value()
        val iteration = UnBoxingEvaluator(IdentityEvaluator(getFromArray(value, 3, context) as ObjectReference))
            .evaluate(context) as IntegerValue

        val evalRecordCreator = EvalRecordCreator(eventValue, blockName, iteration, context)
        event.accept(evalRecordCreator)

        return evalRecordCreator.evalRecord
    }

    fun getFromArray(ref: Value, index: Int, context: EvaluationContextImpl): Value? {
        val identityEvaluator = IdentityEvaluator(ref)
        return MethodEvaluator(
            identityEvaluator,
            null,
            "get",
            null,
            arrayOf(IdentityEvaluator(context.debugProcess.virtualMachineProxy.mirrorOf(index)))
        ).evaluate(context) as? Value
    }

    fun checkForPrimitive(value: Value?, event: CodeEvent, context: EvaluationContextImpl): Value? {
        if (value == null) return null

        return ApplicationManager.getApplication().runReadAction<Value> {
            if (value !is PrimitiveValue && event.type != null && event.type is PsiPrimitiveType) {
                UnBoxingEvaluator(IdentityEvaluator(value)).evaluate(context) as Value
            } else {
                value
            }
        }
    }

    private fun createExceptionEventHolder(
        place: PsiMethod,
        exception: ObjectReference,
        context: EvaluationContextImpl
    ): ExceptionEventHolder {

        val stackTraceReference = MethodEvaluator(
            IdentityEvaluator(exception),
            null,
            "getStackTrace",
            null,
            arrayOfNulls(0)
        ).evaluate(context) as ArrayReference

        val detailMessage =
            FieldEvaluator(IdentityEvaluator(exception), FieldEvaluator.TargetClassFilter.ALL, "detailMessage")
                .evaluate(context) as? StringReference

        val first = stackTraceReference.getValue(0)
        val declaringClass =
            FieldEvaluator(IdentityEvaluator(first), FieldEvaluator.TargetClassFilter.ALL, "declaringClass")
                .evaluate(context) as StringReference

        if (detailMessage?.value() == InvocationTargetException::class.java.canonicalName &&
            declaringClass.value() == "idea.debugger.rt.GeneratedEvaluationClass"
        ) {

            val cause = FieldEvaluator(IdentityEvaluator(exception), FieldEvaluator.TargetClassFilter.ALL, "cause")
                .evaluate(context) as Value
            val target = FieldEvaluator(IdentityEvaluator(cause), FieldEvaluator.TargetClassFilter.ALL, "target")
                .evaluate(context) as ObjectReference

            return ExceptionEventHolder(
                -1,
                ReadAction.compute<String, Throwable> { target.type().name() },
                place.containingFile,
                target
            )
        } else {
            return ExceptionEventHolder(
                -1,
                ReadAction.compute<String, Throwable> { exception.type().name() },
                place.containingFile,
                exception
            )
        }
    }
}

/**
 * @author Marcin Bukowiecki
 */
open class EvalRecord(
    val event: CodeEvent,
    val value: Value?,
    @Suppress("unused") val blockName: String,
    @Suppress("unused") val iteration: Int
) {

    override fun toString(): String {
        return event.toString()
    }
}

/**
 * @author Marcin Bukowiecki
 */
class ExceptionEvalRecord(event: CodeEvent, value: Value?, blockName: String, iteration: Int) :
    EvalRecord(event, value, blockName, iteration)