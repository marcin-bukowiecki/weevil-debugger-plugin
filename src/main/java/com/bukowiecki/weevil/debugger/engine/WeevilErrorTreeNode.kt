/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.debugger.engine

import com.intellij.debugger.JavaDebuggerBundle
import com.intellij.xdebugger.frame.XNamedValue
import com.intellij.xdebugger.frame.XValueNode
import com.intellij.xdebugger.frame.XValuePlace
import com.intellij.xdebugger.frame.presentation.XErrorValuePresentation

/**
 * @author Marcin Bukowiecki
 */
class WeevilErrorTreeNode(name: String, private val message: String) : XNamedValue(name) {

    @Suppress("unused")
    constructor(name: String): this(name, JavaDebuggerBundle.message("error.context.has.changed"))

    override fun computePresentation(node: XValueNode, place: XValuePlace) {
        node.setPresentation(null, XErrorValuePresentation(message), false)
    }
}