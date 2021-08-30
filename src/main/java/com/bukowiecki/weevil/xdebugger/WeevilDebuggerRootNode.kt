/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.xdebugger

import com.intellij.ui.SimpleColoredText
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTree
import com.intellij.xdebugger.impl.ui.tree.nodes.XDebuggerTreeNode
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueContainerNode

/**
 * @author Marcin Bukowiecki
 */
class WeevilDebuggerRootNode(tree: XDebuggerTree) :
    XValueContainerNode<WeevilValueContainer>(tree, null, false, WeevilValueContainer()) {

    override fun getText(): SimpleColoredText {
        return SimpleColoredText()
    }

    override fun removeChildNode(children: MutableList<Any?>?, node: XDebuggerTreeNode?): Int {
        return super.removeChildNode(children, node)
    }
}