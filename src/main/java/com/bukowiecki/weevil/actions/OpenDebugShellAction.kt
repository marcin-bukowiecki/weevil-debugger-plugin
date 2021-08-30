/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.actions

import com.bukowiecki.weevil.bundle.WeevilDebuggerBundle
import com.bukowiecki.weevil.shell.ShellTab
import com.bukowiecki.weevil.utils.WeevilDebuggerDataKey
import com.bukowiecki.weevil.utils.WeevilDebuggerUtils
import com.intellij.execution.ui.layout.PlaceInGrid
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.impl.XDebugSessionImpl

const val shellTabId = 1001

/**
 * @author Marcin Bukowiecki
 */
class OpenDebugShellAction : AnAction(), WeevilEditorAction {

    override fun update(e: AnActionEvent) {
        val enabled = isEnabled(e)
        e.presentation.isEnabled = enabled
        e.presentation.isVisible = enabled
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val currentSession = XDebuggerManager.getInstance(project).currentSession as? XDebugSessionImpl ?: return

        val breakpoint = WeevilDebuggerUtils.getCurrentBreakpoint(currentSession)
        val sourcePosition = if (breakpoint != null) {
            breakpoint.sourcePosition
        } else {
            currentSession.topFramePosition
        } ?: return

        val tab = ShellTab(project, currentSession, sourcePosition)
        currentSession.sessionData.putUserData(WeevilDebuggerDataKey.weevilDebuggerShellDataKeys, tab)

        ApplicationManager.getApplication().invokeLater {
            val myUi = currentSession.ui ?: return@invokeLater
            val content = myUi.createContent(
                ShellTab.contentId,
                tab.myMainPanel,
                WeevilDebuggerBundle.message("weevil.debugger.shell.title"),
                AllIcons.Debugger.Console,
                null
            )
            content.isCloseable = true
            myUi.addContent(content, shellTabId, PlaceInGrid.center, false)
            myUi.selectAndFocus(content, true, false)
        }
    }
}