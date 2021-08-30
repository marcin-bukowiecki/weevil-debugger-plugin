/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.evaluator

import com.bukowiecki.weevil.debugger.WeevilEvaluateContext
import com.bukowiecki.weevil.debugger.ui.EvalRecord
import com.bukowiecki.weevil.debugger.ui.VariableDescriptorUtils
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.engine.evaluation.expression.IdentityEvaluator
import com.intellij.debugger.engine.evaluation.expression.UnBoxingEvaluator
import com.sun.jdi.*

/**
 * @author Marcin Bukowiecki
 */
class EventsResultEvaluator(private val weevilEvaluateContext: WeevilEvaluateContext,
                            ref: ObjectReference) : WeevilDebuggerEvaluator(ref) {

    @Suppress("UNCHECKED_CAST")
    override fun evaluate(context: EvaluationContextImpl): Any {
        val eventCollectorRef = VariableDescriptorUtils.getFromArray(ref, 0, context) as ObjectReference
        val threadCollectorRef = VariableDescriptorUtils.getFromArray(ref, 1, context) as ObjectReference

        val eventCollectorMapEval = MapEvaluator(eventCollectorRef).evaluate(context) as Map<ObjectReference, ObjectReference>
        val eventCollectorMapResult = mutableMapOf<Long, MutableMap<Int, List<EvalRecord>>>()
        for (entry in eventCollectorMapEval.entries) {
            val threadId = (UnBoxingEvaluator(IdentityEvaluator(entry.key)).evaluate(context) as LongValue).value()
            val eventsMapEval = MapEvaluator(entry.value).evaluate(context) as Map<ObjectReference, Value>
            val eventsMap = mutableMapOf<Int, List<EvalRecord>>()
            for (eventEntry in eventsMapEval) {
                val eventId = (UnBoxingEvaluator(IdentityEvaluator(eventEntry.key)).evaluate(context) as IntegerValue).value()
                val eventList = ListEvaluator(eventEntry.value as ObjectReference).evaluate(context) as List<ObjectReference>
                val eventListResult = mutableListOf<EvalRecord>()
                for (evt in eventList) {
                    VariableDescriptorUtils.getRecord(
                        evt,
                        weevilEvaluateContext,
                        context
                    )?.let { eventListResult.add(it) } ?: continue
                }
                eventsMap[eventId] = eventListResult
            }

            eventCollectorMapResult[threadId] = eventsMap
        }

        val threadMapEval = MapEvaluator(threadCollectorRef).evaluate(context) as Map<LongValue, ThreadReference>
        val threadMapResult = mutableListOf<EventsResult>()
        for (entry in threadMapEval.entries) {
            val name = entry.value.name()
            val uniqueID = entry.value.uniqueID()
            threadMapResult.add(EventsResult(uniqueID, name, eventCollectorMapResult[uniqueID]!!))
        }

        return threadMapResult
    }
}

/**
 * @author Marcin Bukowiecki
 */
data class EventsResult(val threadId: Long,
                        val threadName: String,
                        val events: Map<Int /* eventID */, List<EvalRecord> /* history */>)

/**
 * @author Marcin Bukowiecki
 */
data class ThreadResult(val threadId: Long, val threadReference: ThreadReference)

/**
 * @author Marcin Bukowiecki
 */
class ThreadResultEvaluator(ref: ObjectReference) : WeevilDebuggerEvaluator(ref) {

    @Suppress("UNCHECKED_CAST")
    override fun evaluate(context: EvaluationContextImpl): Any {
        val threadCollectorRef = VariableDescriptorUtils.getFromArray(ref, 1, context) as ObjectReference
        val threadMapEval = MapEvaluator(threadCollectorRef).evaluate(context) as Map<ObjectReference, ThreadReference>

        return threadMapEval.entries.map { entry ->
            val key = UnBoxingEvaluator(IdentityEvaluator(entry.key)).evaluate(context) as LongValue
            ThreadResult(key.value(), entry.value)
        }
    }
}


