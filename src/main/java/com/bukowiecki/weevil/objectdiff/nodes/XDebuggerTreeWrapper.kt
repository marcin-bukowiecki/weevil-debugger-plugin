/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.objectdiff.nodes

import com.bukowiecki.weevil.xdebugger.WeevilDebuggerRootNode
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTree
import com.intellij.xdebugger.impl.ui.tree.nodes.XDebuggerTreeNode
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl
import java.util.*
import javax.swing.tree.TreePath

/**
 * @author Marcin Bukowiecki
 */
class XDebuggerTreeWrapper(private val tree: XDebuggerTree) {

    /**
     * Expand node requested from other tree
     */
    fun expandPath(path: TreePath) {
        val paths = LinkedList<XValueNodeImpl>()
        var currentPath = path
        while (true) {
            val lastPathComponent = currentPath.lastPathComponent
            if (lastPathComponent is WeevilDebuggerRootNode) {
                break
            }
            if (lastPathComponent !is XValueNodeImpl) {
                break
            }
            paths.addFirst(lastPathComponent)
            currentPath = currentPath.parentPath
        }

        val iterator = paths.iterator()
        if (!iterator.hasNext()) {
            return
        }

        var currentValueNode = iterator.next()
        var currentTreeNode = tree.root
        while (true) {
            val nodeToExpand = getNodeToExpand(currentTreeNode, currentValueNode)
            if (nodeToExpand == null) {
                break
            } else {
                currentTreeNode = nodeToExpand
                if (!iterator.hasNext()) {
                    break
                }
                currentValueNode = iterator.next()
            }
        }

        if (currentTreeNode != null) {
            tree.expandPath(currentTreeNode.path)
        }
    }

    fun repaint() {
        tree.repaint()
    }

    private fun getNodeToExpand(currentTreeNode: XDebuggerTreeNode, currentValueNode: XValueNodeImpl): XDebuggerTreeNode? {
        var nodeToExpand: XDebuggerTreeNode? = null
        for (child in currentTreeNode.children) {
            if (child is XValueNodeImpl && currentValueNode.name == child.name) {
                nodeToExpand = child
                break
            }
        }
        return nodeToExpand
    }

    fun getXTree(): XDebuggerTree {
        return tree
    }
}
