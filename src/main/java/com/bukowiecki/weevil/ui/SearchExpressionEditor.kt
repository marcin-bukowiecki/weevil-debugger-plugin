/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.intellij.xdebugger.impl.ui.XDebuggerExpressionComboBox

/**
 * @author Marcin Bukowiecki
 */
class SearchExpressionEditor(
    project: Project,
    editorsProvider: XDebuggerEditorsProvider,
    historyId: String,
    sourcePosition: XSourcePosition,
) : XDebuggerExpressionComboBox(
    project,
    editorsProvider,
    historyId,
    sourcePosition,
    true,
    false
), Disposable {

    override fun dispose() {
        Disposer.dispose(this)
    }
}