/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.evaluator

import com.intellij.debugger.engine.SuspendContextImpl
import com.intellij.debugger.engine.evaluation.EvaluateException
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.engine.evaluation.expression.Evaluator
import com.intellij.debugger.engine.evaluation.expression.IdentityEvaluator
import com.intellij.debugger.engine.evaluation.expression.MethodEvaluator
import com.sun.jdi.BooleanValue
import com.sun.jdi.ObjectReference
import com.sun.jdi.Value

/**
 * @author Marcin Bukowiecki
 */
class MapEvaluator(private val ref: ObjectReference) : Evaluator {

    fun evaluate(suspensionContext: SuspendContextImpl): Any {
        val evalContext = EvaluationContextImpl(suspensionContext, suspensionContext.frameProxy, ref)
        return evaluate(evalContext)
    }

    override fun evaluate(context: EvaluationContextImpl): Any {
        val identityEvaluator = IdentityEvaluator(ref)
        var me = MethodEvaluator(identityEvaluator, null, "isEmpty", null, arrayOfNulls(0))

        val evalResult = try {
            me.evaluate(context)
        } catch (e: EvaluateException) {
            return EvalError("Exception while evaluating Map")
        }

        if (evalResult is BooleanValue) {
            val booleanValue = evalResult.value()
            if (booleanValue) return emptyMap<String, Any?>()

            me = MethodEvaluator(identityEvaluator, null, "keySet", null, arrayOfNulls(0))
            val keySetResult = me.evaluate(context)

            val keySetResultEvaluator = IdentityEvaluator(keySetResult as ObjectReference)

            me = MethodEvaluator(keySetResultEvaluator, null, "iterator", null, arrayOfNulls(0))
            val iteratorReference = me.evaluate(context)

            val iteratorReferenceEvaluator = IdentityEvaluator(iteratorReference as ObjectReference)
            me = MethodEvaluator(iteratorReferenceEvaluator, null, "hasNext", null, arrayOfNulls(0))
            var hasNext = me.evaluate(context) as BooleanValue

            val result = mutableMapOf<Value, Value>()

            while (hasNext.value()) {
                me = MethodEvaluator(iteratorReferenceEvaluator, null, "next", null, arrayOfNulls(0))
                val nextReference = me.evaluate(context)
                val key = nextReference as ObjectReference

                me = MethodEvaluator(IdentityEvaluator(ref), null, "get", null, arrayOf(IdentityEvaluator(key)))
                val valueRef = me.evaluate(context) as Value

                result[key] = valueRef

                me = MethodEvaluator(iteratorReferenceEvaluator, null, "hasNext", null, arrayOfNulls(0))
                hasNext = me.evaluate(context) as BooleanValue
            }

            return result
        } else {
            return EvalError("Exception while evaluating Map. Expected boolean for isEmpty method")
        }
    }
}