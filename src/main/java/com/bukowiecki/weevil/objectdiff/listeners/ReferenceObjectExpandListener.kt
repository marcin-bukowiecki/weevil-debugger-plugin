/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.objectdiff.listeners

import com.bukowiecki.weevil.objectdiff.controller.ObjectDiffController
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTreeListener
import com.intellij.xdebugger.impl.ui.tree.nodes.XDebuggerTreeNode
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueContainerNode

/**
 * We need to load other trees to show difference
 *
 * @author Marcin Bukowiecki
 */
class ReferenceObjectExpandListener(private val controller: ObjectDiffController) : XDebuggerTreeListener {

    override fun childrenLoaded(
        node: XDebuggerTreeNode,
        children: MutableList<out XValueContainerNode<*>>,
        last: Boolean
    ) {
        super.childrenLoaded(node, children, last)
        controller.expandReferenceObjectNode(node, children)
    }
}
