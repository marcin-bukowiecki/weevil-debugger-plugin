/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.shell.xdebugger

import com.intellij.debugger.engine.JavaValue
import com.intellij.debugger.ui.impl.DebuggerTreeRenderer
import com.intellij.icons.AllIcons
import com.intellij.util.ThreeState
import com.intellij.xdebugger.XExpression
import com.intellij.xdebugger.evaluation.XInstanceEvaluator
import com.intellij.xdebugger.frame.*
import com.intellij.xdebugger.frame.presentation.XStringValuePresentation
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.resolvedPromise
import java.lang.ref.WeakReference
import javax.swing.Icon

/**
 * @author Marcin Bukowiecki
 */
class ShellXValueWrapper(private val xValueRef: WeakReference<JavaValue>,
                         private val evaluationExpression: String) : XValue() {

    override fun computePresentation(node: XValueNode, place: XValuePlace) {
        val xValue = xValueRef.get()

        if (xValue == null) {
            node.setPresentation(getIcon(), XStringValuePresentation("Unreachable"), false)
        } else {
            xValue.computePresentation(node, place)
        }
    }

    override fun computeChildren(node: XCompositeNode) {
        val xValue = xValueRef.get() ?: return

        xValue.computeChildren(node)
    }

    override fun getEvaluationExpression(): String {
        return evaluationExpression
    }

    override fun calculateEvaluationExpression(): Promise<XExpression> {
        return resolvedPromise(XExpressionImpl.fromText(evaluationExpression))
    }

    override fun getInstanceEvaluator(): XInstanceEvaluator? {
        return xValueRef.get()?.instanceEvaluator
    }

    override fun getModifier(): XValueModifier? {
        return xValueRef.get()?.modifier
    }

    override fun computeSourcePosition(navigatable: XNavigatable) {
        xValueRef.get()?.computeSourcePosition(navigatable)
    }

    override fun computeInlineDebuggerData(callback: XInlineDebuggerDataCallback): ThreeState {
        return xValueRef.get()?.computeInlineDebuggerData(callback) ?: ThreeState.UNSURE
    }

    override fun canNavigateToSource(): Boolean {
        return xValueRef.get()?.canNavigateToSource() ?: false
    }

    override fun canNavigateToTypeSource(): Boolean {
        return xValueRef.get()?.canNavigateToTypeSource() ?: false
    }

    override fun computeTypeSourcePosition(navigatable: XNavigatable) {
        xValueRef.get()?.computeTypeSourcePosition(navigatable)
    }

    override fun getReferrersProvider(): XReferrersProvider? {
        return xValueRef.get()?.referrersProvider
    }

    private fun getIcon(): Icon {
        val xValue = xValueRef.get() ?: return AllIcons.Debugger.Value
        return DebuggerTreeRenderer.getValueIcon(
            xValue.descriptor,
            if (xValue.parent != null) xValue.parent.descriptor else null
        )
    }
}