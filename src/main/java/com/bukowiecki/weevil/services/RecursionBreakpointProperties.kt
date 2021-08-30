/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.intellij.xdebugger.impl.breakpoints.XLineBreakpointImpl
import org.jetbrains.java.debugger.breakpoints.properties.JavaLineBreakpointProperties

/**
 * @author Marcin Bukowiecki
 */
@State(
    name = "WeevilDebuggerData",
    storages = [Storage(StoragePathMacros.WORKSPACE_FILE)]
)
class RecursionBreakpointProperties(@Suppress("unused") private val project: Project) : PersistentStateComponent<StateData> {

    private val state = StateData()

    override fun getState(): StateData {
        return this.state
    }

    override fun loadState(state: StateData) {
        this.state.breakpoints = state.breakpoints
    }

    fun findState(url: String, line: Int): RecursionBreakpointState? {
        return state.breakpoints.find { it.url == url && it.line == line }
    }

    fun findState(xBreakpoint: XLineBreakpoint<*>): RecursionBreakpointState? {
        val sourcePosition = xBreakpoint.sourcePosition ?: return null
        val file = sourcePosition.file
        val url = file.url
        val line = sourcePosition.line

        return findState(url, line)
    }

    fun saveState(xBreakpoint: XLineBreakpoint<JavaLineBreakpointProperties>,
                  iterationEnabled: Boolean,
                  iterationExpression: String) {

        val sourcePosition = xBreakpoint.sourcePosition ?: return
        val file = sourcePosition.file
        val url = file.url
        val line = sourcePosition.line

        val recursionBreakpointState = RecursionBreakpointState()
        recursionBreakpointState.iterationEnabled = iterationEnabled
        recursionBreakpointState.iterationExpression = iterationExpression
        recursionBreakpointState.line = line
        recursionBreakpointState.url = url

        state.breakpoints.add(recursionBreakpointState)
    }

    fun removeState(xBreakpoint: XLineBreakpointImpl<*>) {
        val sourcePosition = xBreakpoint.sourcePosition ?: return
        val file = sourcePosition.file
        val url = file.url
        val line = sourcePosition.line

        val iterator = state.breakpoints.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.line == line && next.url == url) {
                iterator.remove()
                break
            }
        }
    }

    companion object {

        fun getInstance(project: Project): RecursionBreakpointProperties {
            return project.getService(RecursionBreakpointProperties::class.java)
        }
    }
}

/**
 * @author Marcin Bukowiecki
 */
class StateData {
    var breakpoints = mutableListOf<RecursionBreakpointState>()
}

/**
 * @author Marcin Bukowiecki
 */
class RecursionBreakpointState {
    var url: String? = null
    var line: Int? = null
    var iterationEnabled: Boolean = true
    var iterationExpression: String? = null
}
