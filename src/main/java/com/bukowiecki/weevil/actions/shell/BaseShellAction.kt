/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.actions.shell

import com.bukowiecki.weevil.shell.ShellCodeEditor
import com.bukowiecki.weevil.shell.ShellTab
import com.bukowiecki.weevil.shell.evaluator.ShellHandler
import com.bukowiecki.weevil.utils.WeevilDebuggerDataKey
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAwareAction
import javax.swing.Icon

/**
 * @author Marcin Bukowiecki
 */
abstract class BaseShellAction(icon: Icon,
                               private var forceShow: Boolean = false,
                               private var editor: ShellCodeEditor? = null) : DumbAwareAction(icon) {

    override fun update(e: AnActionEvent) {
        if (forceShow) {
            e.presentation.isEnabled = true
            e.presentation.isVisible = true
            e.presentation.text = getTooltipText()
            return
        }

        getEditor(e)?.let {
            val toShow = it.getUserData(WeevilDebuggerDataKey.weevilDebuggerShellDataKeys) != null
            e.presentation.isEnabled = toShow
            e.presentation.isVisible = toShow
        } ?: kotlin.run {
            e.presentation.isEnabled = false
            e.presentation.isVisible = false
        }
    }

    fun withForceShow(): BaseShellAction {
        this.forceShow = true
        return this
    }

    fun withEditor(editor: ShellCodeEditor): BaseShellAction {
        this.editor = editor
        return this
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = getEditor(e) ?: return
        val shellTab = editor.getShellTab() ?: return
        val handler = getHandler()
        handler.handle(editor, shellTab)
        handler.dispose()
    }

    abstract fun getTooltipText(): String

    abstract fun getHandler(): ShellHandler

    private fun getEditor(e: AnActionEvent): Editor? {
        if (this.editor != null) {
            return (this.editor as ShellCodeEditor).editor
        }
        return e.getData(CommonDataKeys.EDITOR)
    }

    private fun Editor.getShellTab(): ShellTab? {
        return this.getUserData(WeevilDebuggerDataKey.weevilDebuggerShellDataKeys)
    }
}