/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.settings

import com.bukowiecki.weevil.bundle.WeevilDebuggerBundle
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JPanel

/**
 * @author Marcin Bukowiecki
 */
class WeevilDebuggerSettingsPanel {

    val mainPanel: JPanel
    val maxSearchDepth = JBTextField()
    val searchTimeout = JBTextField()
    val evaluationTimeout = JBTextField()
    val historyLimit = JBTextField()
    val futureEvaluationLimit = JBTextField()
    val showClassLoaderOfCurrentClass = JBCheckBox()
    val enableRecursionBreakpoint = JBCheckBox()
    val showMethodReturnValues = JBCheckBox()
    val showSingleReferences = JBCheckBox()
    val closeAuto = JBCheckBox()
    val closeAutoPrompt = JBCheckBox()

    init {
        mainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(
                JBLabel(WeevilDebuggerBundle.message("weevil.debugger.settings.search.depth")), maxSearchDepth,
                1, false
            )
            .addLabeledComponent(
                JBLabel(WeevilDebuggerBundle.message("weevil.debugger.settings.search.timeout")), searchTimeout,
                1, false
            )
            .addLabeledComponent(
                JBLabel(WeevilDebuggerBundle.message("weevil.debugger.settings.evaluation.timeout")), evaluationTimeout,
                1, false
            )
            .addLabeledComponent(
                JBLabel(WeevilDebuggerBundle.message("weevil.debugger.settings.history.limit")), historyLimit,
                1, false)
            .addLabeledComponent(
                JBLabel(WeevilDebuggerBundle.message("weevil.debugger.settings.future.limit")), futureEvaluationLimit,
                1, false)
            .addLabeledComponent(
                JBLabel(WeevilDebuggerBundle.message("weevil.debugger.settings.codeSource.show")), showClassLoaderOfCurrentClass,
                1, false
            )
            .addLabeledComponent(
                JBLabel(WeevilDebuggerBundle.message("weevil.debugger.settings.recursion.allow")), enableRecursionBreakpoint,
                1, false
            )
            .addLabeledComponent(
                JBLabel(WeevilDebuggerBundle.message("weevil.debugger.settings.methodReturnValues.show")), showMethodReturnValues,
                1, false
            )
            .addLabeledComponent(
                JBLabel(WeevilDebuggerBundle.message("weevil.debugger.settings.singleReferences.show")), showSingleReferences,
                1, false
            )
            .addLabeledComponent(
                JBLabel(WeevilDebuggerBundle.message("weevil.debugger.settings.close.auto")), closeAuto,
                1, false
            )
            .addLabeledComponent(
                JBLabel(WeevilDebuggerBundle.message("weevil.debugger.settings.close.showPrompt")), closeAutoPrompt,
                1, false
            )
            .addComponentFillVertically(JPanel(), 0)
            .panel

        closeAuto.addActionListener {
            if (closeAuto.isSelected) {
                closeAutoPrompt.isEnabled = true
            } else {
                closeAutoPrompt.isEnabled = false
                closeAutoPrompt.isSelected = false
            }
        }
    }
}