/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.settings

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

/**
 * @author Marcin Bukowiecki
 */
@State(
    name = "com.bukowiecki.weevil.settings.WeevilDebuggerSettings",
    storages = [Storage("WeevilDebuggerSettings.xml")]
)
class WeevilDebuggerSettings : PersistentStateComponent<WeevilDebuggerSettings>, Disposable {

    var autoCloseFiles: Boolean = true

    var showPromptToCloseFiles: Boolean = true

    var maxSearchDepth: Int = 5

    var searchTimeout: Long = 5

    var evaluationTimeout: Long = 5

    var historyLimit: Int = 10

    var futureEvaluationLimit: Int = 10

    var showSourceCodeLocation = false

    var enableRecursionBreakpoint = false

    var showMethodReturnValues = false

    var showSingleReferences = false

    @Transient
    var version = 0

    fun incrVersion() {
        version++
    }

    override fun getState(): WeevilDebuggerSettings {
        return this
    }

    override fun loadState(state: WeevilDebuggerSettings) {
        this.maxSearchDepth = state.maxSearchDepth
        this.searchTimeout = state.searchTimeout
        this.evaluationTimeout = state.evaluationTimeout
        this.historyLimit = state.historyLimit
        this.futureEvaluationLimit = state.futureEvaluationLimit
        this.showSourceCodeLocation = state.showSourceCodeLocation
        this.enableRecursionBreakpoint = state.enableRecursionBreakpoint
        this.showMethodReturnValues = state.showMethodReturnValues
        this.showSingleReferences = state.showSingleReferences
        this.autoCloseFiles = state.autoCloseFiles
        this.showPromptToCloseFiles = state.showPromptToCloseFiles
    }

    override fun dispose() {

    }

    companion object {

        @JvmStatic
        fun getInstance(project: Project): WeevilDebuggerSettings {
            return project.getService(WeevilDebuggerSettings::class.java)
        }
    }
}