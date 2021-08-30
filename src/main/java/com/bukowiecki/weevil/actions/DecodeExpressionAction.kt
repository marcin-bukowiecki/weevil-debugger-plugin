/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.actions

import com.intellij.xdebugger.impl.DebuggerSupport
import com.intellij.xdebugger.impl.actions.DebuggerActionHandler
import com.intellij.xdebugger.impl.actions.XDebuggerActionBase

/**
 * @author Marcin Bukowiecki
 */
@Deprecated("Not yet supported")
class DecodeExpressionAction : XDebuggerActionBase() {

    private val handler = DecodeExpressionHandler()

    override fun getHandler(debuggerSupport: DebuggerSupport): DebuggerActionHandler {
        return handler
    }
}