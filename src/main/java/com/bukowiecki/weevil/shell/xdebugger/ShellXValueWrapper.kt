/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.shell.xdebugger

import com.intellij.debugger.engine.JavaValue
import com.intellij.debugger.ui.impl.DebuggerTreeRenderer
import com.intellij.util.ThreeState
import com.intellij.xdebugger.XExpression
import com.intellij.xdebugger.evaluation.XInstanceEvaluator
import com.intellij.xdebugger.frame.*
import org.jetbrains.concurrency.Promise
import javax.swing.Icon

/**
 * @author Marcin Bukowiecki
 */
@Suppress("unused")
class ShellXValueWrapper(private val wrapped: JavaValue) : XValue() {

    @Volatile
    private var wasComputed = false

    @Volatile
    private var refreshRequired = false

    override fun computePresentation(node: XValueNode, place: XValuePlace) {
        if (wasComputed) {
            node.setPresentation(getIcon(), JavaValue.createPresentation(wrapped.descriptor), wrapped.descriptor.isExpandable)
            refreshRequired = true
            return
        }
        wrapped.computePresentation(node, place)
        wasComputed = true
    }

    override fun computeChildren(node: XCompositeNode) {
        if (refreshRequired) {
            refreshRequired = false
        }
        wrapped.computeChildren(node)
    }

    override fun getEvaluationExpression(): String? {
        return wrapped.evaluationExpression
    }

    override fun calculateEvaluationExpression(): Promise<XExpression> {
        return wrapped.calculateEvaluationExpression()
    }

    override fun getInstanceEvaluator(): XInstanceEvaluator? {
        return wrapped.instanceEvaluator
    }

    override fun getModifier(): XValueModifier? {
        return wrapped.modifier
    }

    override fun computeSourcePosition(navigatable: XNavigatable) {
        wrapped.computeSourcePosition(navigatable)
    }

    override fun computeInlineDebuggerData(callback: XInlineDebuggerDataCallback): ThreeState {
        return wrapped.computeInlineDebuggerData(callback)
    }

    override fun canNavigateToSource(): Boolean {
        return wrapped.canNavigateToSource()
    }

    override fun canNavigateToTypeSource(): Boolean {
        return wrapped.canNavigateToTypeSource()
    }

    override fun computeTypeSourcePosition(navigatable: XNavigatable) {
        wrapped.computeTypeSourcePosition(navigatable)
    }

    override fun getReferrersProvider(): XReferrersProvider? {
        return wrapped.referrersProvider
    }

    private fun getIcon(): Icon {
        return DebuggerTreeRenderer.getValueIcon(
            wrapped.descriptor,
            if (wrapped.parent != null) wrapped.parent.descriptor else null
        )
    }
}