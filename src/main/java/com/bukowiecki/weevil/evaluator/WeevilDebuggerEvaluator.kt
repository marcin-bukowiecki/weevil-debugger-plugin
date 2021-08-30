package com.bukowiecki.weevil.evaluator

import com.intellij.debugger.engine.SuspendContextImpl
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.engine.evaluation.expression.Evaluator
import com.sun.jdi.ObjectReference

/**
 * @author Marcin Bukowiecki
 */
abstract class WeevilDebuggerEvaluator(val ref: ObjectReference) : Evaluator {

    open fun evaluate(suspensionContext: SuspendContextImpl): Any {
        val evalContext = EvaluationContextImpl(suspensionContext, suspensionContext.frameProxy, ref)
        return evaluate(evalContext)
    }
}