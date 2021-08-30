/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.search

import com.intellij.xdebugger.impl.ui.tree.XDebuggerTreeListener
import com.intellij.xdebugger.impl.ui.tree.nodes.RestorableStateNode
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl

/**
 * @author Marcin Bukowiecki
 */
class SearchXDebuggerTreeListener(private val initialSearchPath: InitialSearchPath,
                                  private val matchedPaths: List<List<SearchPath>>) : XDebuggerTreeListener {

    override fun nodeLoaded(node: RestorableStateNode, name: String) {
        if (matchedPaths.isEmpty()) return

        val currentRow = node.parent.getIndex(node)
        val paths = node.path.path.map { it.toString() }.toList()
        val matches = initialSearchPath.matches(paths, 0, currentRow)
        if (matches && node is XValueNodeImpl) {
            val tree = node.tree
            tree.addSelectionPath(node.path)
            if (node.isLeaf) {
                tree.repaint()
                return
            }
            tree.expandPath(node.path)
            tree.repaint()
        }
    }
}