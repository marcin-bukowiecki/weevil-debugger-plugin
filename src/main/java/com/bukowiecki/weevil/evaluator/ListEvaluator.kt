package com.bukowiecki.weevil.evaluator

import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.engine.evaluation.expression.IdentityEvaluator
import com.intellij.debugger.engine.evaluation.expression.MethodEvaluator
import com.sun.jdi.BooleanValue
import com.sun.jdi.ObjectReference
import com.sun.jdi.Value

/**
 * @author Marcin Bukowiecki
 */
class ListEvaluator(ref: ObjectReference) : WeevilDebuggerEvaluator(ref) {

    @Suppress("UNCHECKED_CAST")
    override fun evaluate(context: EvaluationContextImpl): Any {
        val list: MutableList<Value> = mutableListOf()
        var me = MethodEvaluator( IdentityEvaluator(ref), null, "iterator", null, arrayOfNulls(0))
        val itRef = me.evaluate(context) as ObjectReference

        me = MethodEvaluator(IdentityEvaluator(itRef), null, "hasNext", null, arrayOfNulls(0))
        var hasNext = me.evaluate(context) as BooleanValue
        while (hasNext.value()) {
            me = MethodEvaluator(IdentityEvaluator(itRef), null, "next", null, arrayOfNulls(0))
            val element = me.evaluate(context) as ObjectReference
            list.add(element)

            me = MethodEvaluator(IdentityEvaluator(itRef), null, "hasNext", null, arrayOfNulls(0))
            hasNext = me.evaluate(context) as BooleanValue
        }

        return list
    }
}