/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.xdebugger.breakpoints

import com.intellij.debugger.engine.DebugProcessImpl
import com.intellij.debugger.engine.JavaBreakpointHandler
import com.intellij.debugger.engine.JavaBreakpointHandlerFactory

/**
 * @author Marcin Bukowiecki
 */
class WeevilDebuggerBreakpointHandlerFactory : JavaBreakpointHandlerFactory {

    override fun createHandler(process: DebugProcessImpl): JavaBreakpointHandler {
        return JavaRecursiveLineBreakpointHandler(process)
    }
}

class JavaRecursiveLineBreakpointHandler(process: DebugProcessImpl) :
    JavaBreakpointHandler(RecursionLineBreakpointType::class.java, process)