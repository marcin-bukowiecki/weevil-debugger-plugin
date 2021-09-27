/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.objectdiff.controller

import com.bukowiecki.weevil.objectdiff.ObjectDiffService
import com.bukowiecki.weevil.objectdiff.ui.ObjectDiffDialog
import com.intellij.debugger.engine.JavaValue
import com.intellij.xdebugger.impl.ui.tree.nodes.XDebuggerTreeNode
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueContainerNode
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl
import org.apache.commons.lang3.StringUtils
import javax.swing.event.TreeExpansionEvent
import javax.swing.tree.ExpandVetoException

/**
 * @author Marcin Bukowiecki
 */
class ObjectDiffController(private val dialog: ObjectDiffDialog) {

    @Volatile
    private var referenceObjectRequest: Boolean = false

    fun expandReferenceObjectNode(node: XDebuggerTreeNode,
                                  children: List<XValueContainerNode<*>>) {

        //cache children of expanded node for "reference" object
        val objectDiffService = ObjectDiffService.getInstance()
        children.filterIsInstance<XValueNodeImpl>().forEach { child ->
            val valueContainer = child.valueContainer
            if (valueContainer is JavaValue) {
                val key = child.path.path
                    .map { it.toString() }
                    .filter { StringUtils.isNotEmpty(it) }
                    .joinToString(".")

                objectDiffService.putCachedValue(key, valueContainer.descriptor)
            }
        }

        referenceObjectRequest = true
        //expand trees of objects for comparison
        try {
            for (tree in dialog.compareWithTrees) {
                tree.expandPath(node.path)
                tree.repaint()
            }
        } finally {
            referenceObjectRequest = false
        }

        dialog.mainPanel.repaint()
    }

    fun checkIfExpandIsAllowed(event: TreeExpansionEvent?) {
        checkCollapseOrExpandIsAllowed(event)
    }

    fun checkIfCollapseIsAllowed(event: TreeExpansionEvent?) {
        checkCollapseOrExpandIsAllowed(event)
    }

    private fun checkCollapseOrExpandIsAllowed(event: TreeExpansionEvent?) {
        if (!referenceObjectRequest) {
            throw ExpandVetoException(event, "Collapse/Expand tree not allowed")
        }
    }
}
