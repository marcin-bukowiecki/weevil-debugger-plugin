/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.settings

import com.bukowiecki.weevil.bundle.WeevilDebuggerBundle
import com.bukowiecki.weevil.services.WeevilDebuggerService
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

/**
 * @author Marcin Bukowiecki
 */
class WeevilDebuggerConfigurable(private val project: Project) : Configurable {

    private var settingsComponent: WeevilDebuggerSettingsPanel? = null

    private var messageBus = project.messageBus

    override fun createComponent(): JComponent? {
        settingsComponent = WeevilDebuggerSettingsPanel()
        return settingsComponent?.mainPanel
    }

    override fun isModified(): Boolean {
        val settings = WeevilDebuggerSettings.getInstance(project)
        var modified: Boolean = settingsComponent?.maxSearchDepth?.text != settings.maxSearchDepth.toString()
        modified = modified || settingsComponent?.searchTimeout?.text != settings.searchTimeout.toString()
        modified = modified || settingsComponent?.evaluationTimeout?.text != settings.evaluationTimeout.toString()
        modified = modified || settingsComponent?.historyLimit?.text != settings.historyLimit.toString()
        modified = modified || settingsComponent?.futureEvaluationLimit?.text != settings.futureEvaluationLimit.toString()
        modified = modified || settingsComponent?.showClassLoaderOfCurrentClass?.isSelected != settings.showSourceCodeLocation
        modified = modified || settingsComponent?.enableRecursionBreakpoint?.isSelected != settings.enableRecursionBreakpoint
        modified = modified || settingsComponent?.showMethodReturnValues?.isSelected != settings.showMethodReturnValues
        modified = modified || settingsComponent?.showSingleReferences?.isSelected != settings.showSingleReferences
        modified = modified || settingsComponent?.closeAuto?.isSelected != settings.autoCloseFiles
        modified = modified || settingsComponent?.closeAutoPrompt?.isSelected != settings.showPromptToCloseFiles

        return modified
    }

    override fun apply() {
        val rerun = isModified

        val settings = WeevilDebuggerSettings.getInstance(project)
        settings.maxSearchDepth = settingsComponent?.maxSearchDepth?.text?.toInt() ?: return
        settings.searchTimeout = settingsComponent?.searchTimeout?.text?.toLong() ?: return
        settings.evaluationTimeout = settingsComponent?.evaluationTimeout?.text?.toLong() ?: return
        settings.historyLimit = settingsComponent?.historyLimit?.text?.toInt() ?: return
        settings.futureEvaluationLimit = settingsComponent?.futureEvaluationLimit?.text?.toInt() ?: return
        settings.showSourceCodeLocation = settingsComponent?.showClassLoaderOfCurrentClass?.isSelected ?: false
        settings.enableRecursionBreakpoint = settingsComponent?.enableRecursionBreakpoint?.isSelected ?: true
        settings.showMethodReturnValues = settingsComponent?.showMethodReturnValues?.isSelected ?: false
        settings.showSingleReferences = settingsComponent?.showSingleReferences?.isSelected ?: false
        settings.autoCloseFiles = settingsComponent?.closeAuto?.isSelected ?: false
        settings.showPromptToCloseFiles = settingsComponent?.closeAutoPrompt?.isSelected ?: false

        settings.incrVersion()

        if (rerun) {
            messageBus.syncPublisher(WeevilDebuggerService.getInstance(project).topic).settingsChanged()
        }
    }

    override fun getDisplayName(): String {
        return WeevilDebuggerBundle.getMessage("weevil.debugger.configurable")
    }

    override fun reset() {
        val instance = WeevilDebuggerSettings.getInstance(project)

        settingsComponent?.maxSearchDepth?.text = instance.maxSearchDepth.toString()
        settingsComponent?.searchTimeout?.text = instance.searchTimeout.toString()
        settingsComponent?.evaluationTimeout?.text = instance.evaluationTimeout.toString()
        settingsComponent?.historyLimit?.text = instance.historyLimit.toString()
        settingsComponent?.futureEvaluationLimit?.text = instance.futureEvaluationLimit.toString()
        settingsComponent?.showClassLoaderOfCurrentClass?.isSelected = instance.showSourceCodeLocation
        settingsComponent?.enableRecursionBreakpoint?.isSelected = instance.enableRecursionBreakpoint
        settingsComponent?.showMethodReturnValues?.isSelected = instance.showMethodReturnValues
        settingsComponent?.showSingleReferences?.isSelected = instance.showSingleReferences
        settingsComponent?.closeAuto?.isSelected = instance.autoCloseFiles
        settingsComponent?.closeAutoPrompt?.isSelected = instance.showPromptToCloseFiles

        instance.incrVersion()
    }

    companion object {

        fun getInstance(project: Project): WeevilDebuggerConfigurable {
            return WeevilDebuggerConfigurable(project)
        }
    }
}