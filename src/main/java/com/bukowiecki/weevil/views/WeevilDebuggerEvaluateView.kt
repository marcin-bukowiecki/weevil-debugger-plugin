/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.views

import com.bukowiecki.weevil.bundle.WeevilDebuggerBundle.message
import com.bukowiecki.weevil.codesource.CodeSourceBlockInlayType
import com.bukowiecki.weevil.codesource.CodeSourceLocationHandler
import com.bukowiecki.weevil.debugger.controller.SessionController
import com.bukowiecki.weevil.debugger.listeners.WeevilDebuggerContext
import com.bukowiecki.weevil.inlay.WeevilDebuggerInlayUtil
import com.bukowiecki.weevil.listeners.WeevilDebuggerListener
import com.bukowiecki.weevil.services.WeevilDebuggerService
import com.bukowiecki.weevil.settings.WeevilDebuggerConfigurable
import com.bukowiecki.weevil.settings.WeevilDebuggerSettings
import com.intellij.CommonBundle
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.Disposer
import com.intellij.ui.CaptionPanel
import com.intellij.ui.ComboboxSpeedSearch
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.border.CustomLineBorder
import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.table.ComponentsListFocusTraversalPolicy
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.frame.XExecutionStack
import com.intellij.xdebugger.impl.frame.XDebugView
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JButton
import javax.swing.JPanel

/**
 * @author Marcin Bukowiecki
 */
class WeevilDebuggerEvaluateView(private val project: Project, private val controller: SessionController) : XDebugView() {

    private val myPublisher = project.messageBus.syncPublisher(WeevilDebuggerService.getInstance(project).topic)
    private val myConnection = project.messageBus.connect()
    private val myForm = EvaluateViewForm()

    val myThreadComboBox = ComboBox<XExecutionStack>()
    val evaluateButton: JButton = myForm.evaluateButton
    val myMainPanel: JPanel = myForm.mainPanel

    init {
        val settings = WeevilDebuggerSettings.getInstance(project)

        myForm.evaluateButton.text = message("weevil.debugger.evaluateFuture")
        myForm.codeSourceCheckBox.text = message("weevil.debugger.settings.codeSource.show")
        myForm.codeSourceCheckBox.isSelected = settings.showSourceCodeLocation

        myForm.showMethodReturnValuesCheckBox.text = message("weevil.debugger.settings.methodReturnValues.show")
        myForm.showMethodReturnValuesCheckBox.isSelected = settings.showMethodReturnValues

        myForm.showSingleExpressionValuesCheckBox.text = message("weevil.debugger.settings.singleReferences.show")
        myForm.showSingleExpressionValuesCheckBox.isSelected = settings.showSingleReferences

        myForm.threadComboBoxPanel.border = CustomLineBorder(CaptionPanel.CNT_ACTIVE_BORDER_COLOR, 0, 0, 1, 0)
        myForm.threadComboBoxPanel.add(myThreadComboBox,  BorderLayout.CENTER)
        myForm.threadComboBoxPanel.revalidate()

        myForm.mainPanel.isFocusCycleRoot = true
        myForm.mainPanel.focusTraversalPolicy = MyFocusPolicy(myThreadComboBox)

        myThreadComboBox.isEnabled = false //future feature
        myThreadComboBox.isSwingPopup = false
        myThreadComboBox.renderer =
            SimpleListCellRenderer.create { label: JBLabel, value: XExecutionStack?, index: Int ->
                if (value != null) {
                    label.text = value.displayName
                    label.icon = value.icon
                } else if (index >= 0) {
                    label.text = CommonBundle.getLoadingTreeNodeText()
                }
            }
        object : ComboboxSpeedSearch(myThreadComboBox) {
            override fun getElementText(element: Any): String {
                return (element as XExecutionStack).displayName
            }
        }

        myForm.codeSourceCheckBox.addActionListener {
            settings.showSourceCodeLocation = myForm.codeSourceCheckBox.isSelected
            myPublisher.settingsChanged()
        }

        myForm.showMethodReturnValuesCheckBox.addActionListener {
            settings.showMethodReturnValues = myForm.showMethodReturnValuesCheckBox.isSelected
            myPublisher.settingsChanged()
        }

        myForm.showSingleExpressionValuesCheckBox.addActionListener {
            settings.showSingleReferences = myForm.showSingleExpressionValuesCheckBox.isSelected
            myPublisher.settingsChanged()
        }

        myForm.evaluateButton.addActionListener {
            myPublisher.evaluateFuture()
            controller.evaluate()
        }

        myConnection.subscribe(WeevilDebuggerService.getInstance(project).topic, object : WeevilDebuggerListener {

            override fun settingsChanged() {
                evaluateCodeSourceLocation()
                myForm.showMethodReturnValuesCheckBox.isSelected = settings.showMethodReturnValues
                myForm.showSingleExpressionValuesCheckBox.isSelected = settings.showSingleReferences
                myForm.codeSourceCheckBox.isSelected = settings.showSourceCodeLocation
            }

            override fun sessionStopped() {
                myThreadComboBox.removeAllItems()
                myThreadComboBox.revalidate()
            }

            override fun evaluateFuture() {
                myThreadComboBox.removeAllItems()
                myForm.optionPanel.components.forEach { it.isEnabled = false }
            }

            override fun futureEvaluated() {
                evaluateCodeSourceLocation()
                myForm.optionPanel.components.forEach { it.isEnabled = true }
            }
        })

        this.myForm.otherOptionsPanel.add(ActionLink(message("weevil.debugger.settings.open")) {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, WeevilDebuggerConfigurable::class.java)
        })
    }

    private fun evaluateCodeSourceLocation() {
        val debugProcess = XDebuggerManager.getInstance(project).currentSession?.debugProcess ?: return
        val weevilDebuggerContext = WeevilDebuggerContext(project, debugProcess)
        WeevilDebuggerInlayUtil.removeBlockInlays(project, CodeSourceBlockInlayType)
        val settings = WeevilDebuggerSettings.getInstance(project)
        if (settings.showSourceCodeLocation) {
            CodeSourceLocationHandler(project).handle(weevilDebuggerContext)
        }
    }

    override fun dispose() {
        myConnection.dispose()
        myThreadComboBox.removeAll()
        Disposer.dispose(this)
    }

    override fun clear() {

    }

    override fun processSessionEvent(event: SessionEvent, session: XDebugSession) {

    }

    private class MyFocusPolicy(private val myThreadComboBox: ComboBox<XExecutionStack>) :
        ComponentsListFocusTraversalPolicy() {
        override fun getOrderedComponents(): List<Component> {
            return listOf(
                myThreadComboBox
            )
        }
    }
}