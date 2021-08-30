package com.bukowiecki.weevil.condition

import com.intellij.debugger.engine.JavaValue
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl

/**
 * @author Marcin Bukowiecki
 */
class DoubleConditionHandler(private val javaValue: JavaValue) : SetupConditionAtBreakpointHandler() {

    override fun handle(name: String, breakpoint: XBreakpoint<*>, callback: () -> Unit) {
        val expression = XExpressionImpl.fromText("Math.abs($name - ${javaValue.valueText}) < 0.00001d")
        breakpoint.conditionExpression = expression
        callback.invoke()
    }
}