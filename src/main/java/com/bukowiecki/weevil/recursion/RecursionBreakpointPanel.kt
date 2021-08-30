/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.recursion

import com.bukowiecki.weevil.recursion.utils.WeevilDebuggerRecursionUtils
import com.bukowiecki.weevil.services.RecursionBreakpointProperties
import com.intellij.openapi.project.Project
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.intellij.xdebugger.breakpoints.ui.XBreakpointCustomPropertiesPanel
import org.jetbrains.java.debugger.breakpoints.properties.JavaLineBreakpointProperties
import javax.swing.JComponent
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * @author Marcin Bukowiecki
 */
class RecursionBreakpointPanel(private val project: Project) :
    XBreakpointCustomPropertiesPanel<XLineBreakpoint<JavaLineBreakpointProperties>>() {

    private val form = com.bukowiecki.weevil.recursion.ui.RecursionBreakpointPanelForm()

    init {
        form.iterationTextField.document.addDocumentListener(object : DocumentListener {

            override fun insertUpdate(e: DocumentEvent) {
                handleExpressionUpdate()
            }

            override fun removeUpdate(e: DocumentEvent) {
                handleExpressionUpdate()
            }

            override fun changedUpdate(e: DocumentEvent) {
                handleExpressionUpdate()
            }
        })
    }

    fun handleExpressionUpdate() {
        val iterationTextField = form.iterationTextField

        WeevilDebuggerRecursionUtils.parseText(project, iterationTextField.text)?.let { recursionExpression ->
            val isOk = WeevilDebuggerRecursionUtils.validateExpression(recursionExpression)
            if (isOk) {
                iterationTextField.putClientProperty("JComponent.outline", null)
            } else {
                iterationTextField.putClientProperty("JComponent.outline", "error")
            }
        }

        iterationTextField.repaint()
    }

    override fun getComponent(): JComponent {
        return form.mainPanel
    }

    override fun saveTo(breakpoint: XLineBreakpoint<JavaLineBreakpointProperties>) {
        val service = RecursionBreakpointProperties.getInstance(project)
        service.findState(breakpoint)?.let {
            it.iterationEnabled = form.iterationsCheckBox.isSelected
            it.iterationExpression = form.iterationTextField.text
        } ?: kotlin.run {
            service.saveState(
                breakpoint,
                form.iterationsCheckBox.isSelected,
                form.iterationTextField.text,
            )
        }
    }

    override fun loadFrom(breakpoint: XLineBreakpoint<JavaLineBreakpointProperties>) {
        RecursionBreakpointProperties.getInstance(project).findState(breakpoint)?.let {
            form.iterationTextField.text = it.iterationExpression ?: ""
            form.iterationsCheckBox.isSelected = it.iterationEnabled
        } ?: kotlin.run {
            form.iterationTextField.text = "1"
            form.iterationsCheckBox.isSelected = true
        }
    }
}