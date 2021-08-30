package com.bukowiecki.weevil.condition

import com.intellij.debugger.engine.JavaValue
import com.intellij.xdebugger.breakpoints.XBreakpoint

/**
 * @author Marcin Bukowiecki
 */
abstract class SetupConditionAtBreakpointHandler {

    abstract fun handle(name: String, breakpoint: XBreakpoint<*>, callback: () -> Unit)

    companion object {

        fun getHandler(value: JavaValue): SetupConditionAtBreakpointHandler {
            val type = value.descriptor.type ?: return NullConditionHandler()
            val signature = type.signature()

            return if (signature.equals("I") || signature.equals("B") || signature.equals("S")) {
                IntegerConditionHandler(value)
            } else if (signature.equals("C")) {
                CharConditionHandler(value)
            } else if (signature.equals("J")) {
                LongConditionHandler(value)
            } else if (signature.equals("F")) {
                FloatConditionHandler(value)
            } else if (signature.equals("D")) {
                DoubleConditionHandler(value)
            } else if (signature.equals("Z")) {
                BooleanConditionHandler()
            } else if (signature.equals("Ljava/lang/String;")) {
                StringConditionHandler(value)
            } else {
                DefaultConditionHandler()
            }
        }
    }
}