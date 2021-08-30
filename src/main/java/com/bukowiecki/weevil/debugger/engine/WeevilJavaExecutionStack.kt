/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.debugger.engine

import com.intellij.debugger.engine.DebugProcessImpl
import com.intellij.debugger.engine.JavaExecutionStack
import com.intellij.debugger.jdi.ThreadReferenceProxyImpl

/**
 * @author Marcin Bukowiecki
 */
class WeevilJavaExecutionStack(
    threadProxy: ThreadReferenceProxyImpl,
    debugProcess: DebugProcessImpl,
    current: Boolean,
    val threadId: Long
) : JavaExecutionStack(threadProxy, debugProcess, current)