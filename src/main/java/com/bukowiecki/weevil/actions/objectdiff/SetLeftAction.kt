/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.actions.objectdiff

import com.bukowiecki.weevil.objectdiff.ObjectDiffService
import com.intellij.debugger.engine.JavaValue
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.xdebugger.impl.ui.tree.actions.XDebuggerTreeActionBase
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl
import com.sun.jdi.ObjectReference

/**
 * @author Marcin Bukowiecki
 */
class SetLeftAction : XDebuggerTreeActionBase() {

    override fun perform(node: XValueNodeImpl, nodeName: String, e: AnActionEvent) {
        val objectDiffService = ObjectDiffService.getInstance()
        val javaValue = node.valueContainer as? JavaValue ?: return
        val value = javaValue.descriptor.value as? ObjectReference ?: return
        objectDiffService.setObjectToCompare(value)
    }
}
