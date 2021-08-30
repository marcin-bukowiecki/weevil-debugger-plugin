/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.condition

import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl

/**
 * @author Marcin Bukowiecki
 */
class DefaultConditionHandler : SetupConditionAtBreakpointHandler() {

    override fun handle(name: String, breakpoint: XBreakpoint<*>, callback: () -> Unit) {
        val expression = XExpressionImpl.fromText("$name.")
        breakpoint.conditionExpression = expression
        callback.invoke()
    }
}