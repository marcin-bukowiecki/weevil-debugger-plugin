/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.actions

import com.bukowiecki.weevil.services.WeevilDebuggerService
import com.bukowiecki.weevil.utils.WeevilDebuggerUtils
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.impl.XDebugSessionImpl

/**
 * @author Marcin Bukowiecki
 */
interface WeevilEditorAction {

    fun isEnabled(e: AnActionEvent): Boolean {
        val project = e.project ?: return false
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return false

        if (!WeevilDebuggerService.getInstance(project).isLanguageSupported(psiFile.language)) {
            return false
        }

        val currentSession = XDebuggerManager.getInstance(project).currentSession as? XDebugSessionImpl ?: return false
        val breakpoint = WeevilDebuggerUtils.getCurrentBreakpoint(currentSession)

        if (breakpoint != null) return true

        if (currentSession.topFramePosition != null) return true

        return true
    }
}