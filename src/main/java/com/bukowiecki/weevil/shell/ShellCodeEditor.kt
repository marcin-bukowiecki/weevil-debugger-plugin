/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.shell

import com.bukowiecki.weevil.utils.WeevilDebuggerDataKey
import com.bukowiecki.weevil.xdebugger.impl.WeevilCodeEditor
import com.intellij.lang.Language
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.evaluation.EvaluationMode
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import javax.swing.JPanel

/**
 * @author Marcin Bukowiecki
 */
class ShellCodeEditor(
    private val shellTab: ShellTab,
    evaluationPanel: JPanel,
    project: Project,
    editorsProvider: XDebuggerEditorsProvider,
    sourcePosition: XSourcePosition,
    language: Language
) : WeevilCodeEditor(
    evaluationPanel,
    project,
    editorsProvider,
    sourcePosition,
    "WeevilDebugger.Shell",
    EvaluationMode.CODE_FRAGMENT,
    true,
    language
), Disposable {

    override fun fillUserData(editor: Editor) {
        editor.putUserData(WeevilDebuggerDataKey.weevilDebuggerShellDataKeys, shellTab)
    }
}
