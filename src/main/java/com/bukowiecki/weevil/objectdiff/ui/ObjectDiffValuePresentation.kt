/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.objectdiff.ui

import com.bukowiecki.weevil.annotator.Colors
import com.bukowiecki.weevil.bundle.WeevilDebuggerBundle
import com.bukowiecki.weevil.debugger.ui.WeevilXValuePresentation
import com.bukowiecki.weevil.objectdiff.utils.ObjectDiffPresentationUtils
import com.bukowiecki.weevil.objectdiff.utils.ObjectDiffUtils
import com.bukowiecki.weevil.utils.WeevilDebuggerUtils
import com.intellij.debugger.ui.tree.render.ToStringRenderer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.ColoredTextContainer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.ReflectionUtil
import com.intellij.xdebugger.frame.XValueNode
import com.intellij.xdebugger.impl.ui.DebuggerUIUtil
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueTextRendererImpl
import com.sun.jdi.StringReference
import com.sun.jdi.Value

/**
 * @author Marcin Bukowiecki
 */
class ObjectDiffValuePresentation(
    valueDescriptorImpl: ObjectDiffValueDescriptorImpl,
) : WeevilXValuePresentation(valueDescriptorImpl) {

    private val valueToCompareWith = (myValueDescriptor as CompareToObjectDiffValueDescriptorImpl).valueToCompareWith

    override fun renderValue(renderer: XValueTextRenderer, node: XValueNodeImpl?) {
        val thisValue = myValueDescriptor.value
        assert(
            WeevilDebuggerUtils.typesSame(thisValue, valueToCompareWith)
        ) { "This: ${thisValue.type()}, to compare with: ${valueToCompareWith?.type()}" }

        val compact = node != null
        val valueText = myValueDescriptor.valueText
        val exception = myValueDescriptor.evaluateException
        if (exception != null) {
            val errorMessage = exception.message
            if (valueText.endsWith(errorMessage!!)) {
                renderer.renderValue(valueText.substring(0, valueText.length - errorMessage.length))
            }
            renderer.renderError(errorMessage)
        } else {
            if (compact) {
                val text = myValueDescriptor.compactValueText
                if (text != null) {
                    if (renderer is XValueTextRendererImpl) {
                        renderer.renderValue(text)
                    } else {
                        renderValue(thisValue, renderer, text)
                    }
                    return
                }
            }
            if (myValueDescriptor.isString) {
                if (renderer is XValueTextRendererImpl) {
                    renderStringValue(thisValue, renderer, valueText)
                } else {
                    renderer.renderStringValue(valueText, "\"", XValueNode.MAX_VALUE_LENGTH)
                }
                return
            }
            var value = truncateToMaxLength(valueText)
            val lastRenderer = myValueDescriptor.lastLabelRenderer
            if (lastRenderer is ToStringRenderer) {
                if (!lastRenderer.isShowValue(myValueDescriptor, myValueDescriptor.storedEvaluationContext)) {
                    return  // to avoid empty line for not calculated toStrings
                }
                value = StringUtil.wrapWithDoubleQuote(value)
            }
            if (renderer is XValueTextRendererImpl) {
                renderValue(thisValue, renderer, value)
            } else {
                renderer.renderValue(value)
            }
        }
    }

    private fun renderValue(
        thisValue: Value,
        renderer: XValueTextRenderer,
        valueText: String
    ) {
        val declaredField = ReflectionUtil.getDeclaredField(XValueTextRendererImpl::class.java, "myText")
        val myText = declaredField!!.get(renderer) as ColoredTextContainer
        ObjectDiffPresentationUtils.renderValue(thisValue, valueText, myText, valueToCompareWith, renderer)
    }

    private fun renderStringValue(
        thisValue: Value,
        renderer: XValueTextRenderer,
        valueText: String
    ) {
        val stringReference = valueToCompareWith as StringReference
        val stringReferenceValue = stringReference.value()
        val indexesOfDifferentChars =
            ObjectDiffUtils.getIndexesOfDifferentChars(stringReferenceValue, (thisValue as StringReference).value())

        val declaredField = ReflectionUtil.getDeclaredField(XValueTextRendererImpl::class.java, "myText")
        val myText = declaredField!!.get(renderer) as ColoredTextContainer
        val textAttributes = DebuggerUIUtil.getColorScheme().getAttributes(DefaultLanguageHighlighterColors.STRING)
        val diffTextAttributes = textAttributes.clone()
        diffTextAttributes.foregroundColor = Colors.diffBackgroundColor
        val attributes = SimpleTextAttributes.fromTextAttributes(textAttributes)
        myText.append("\"", attributes)
        ObjectDiffPresentationUtils.renderValue(
            valueText,
            myText,
            attributes,
            SimpleTextAttributes.fromTextAttributes(diffTextAttributes),
            XValueNode.MAX_VALUE_LENGTH,
            "\"",
            indexesOfDifferentChars
        )
        myText.append("\"", attributes)

        if (indexesOfDifferentChars.isNotEmpty()) {
            renderer.renderError(WeevilDebuggerBundle.message("weevil.debugger.objectDiff.differentValue"))
        }
    }

    private fun truncateToMaxLength(value: String): String {
        return value.substring(0, value.length.coerceAtMost(XValueNode.MAX_VALUE_LENGTH))
    }
}
