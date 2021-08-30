/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.ui

import com.intellij.debugger.engine.JavaDebugProcess
import com.intellij.debugger.engine.JavaValue
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.impl.frame.XValueMarkers
import com.bukowiecki.weevil.bundle.WeevilDebuggerBundle
import com.bukowiecki.weevil.debugger.ui.WithHistoryDescriptor
import com.intellij.lang.Language

/**
 * @author Marcin Bukowiecki
 */
class SearchWithHistoryDialog(
    javaValue: JavaValue,
    descriptor: WithHistoryDescriptor,
    javaDebugProcess: JavaDebugProcess,
    sourcePosition: XSourcePosition,
    language: Language,
    markers: XValueMarkers<*, *>
) : SearchDialog(javaValue, descriptor, javaDebugProcess, sourcePosition, language, markers) {

    override fun provideTitle(): String {
        return WeevilDebuggerBundle.message("weevil.debugger.history")
    }

    override fun getNodeName(name: String, index: Int): String {
        return "$name ($index)"
    }
}