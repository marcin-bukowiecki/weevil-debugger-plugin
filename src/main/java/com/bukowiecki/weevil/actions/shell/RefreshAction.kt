/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.actions.shell

import com.bukowiecki.weevil.bundle.WeevilDebuggerBundle
import com.bukowiecki.weevil.shell.ShellTab
import com.bukowiecki.weevil.shell.evaluator.ShellHandler
import com.bukowiecki.weevil.utils.WeevilDebuggerUtils
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiDocumentManager

/**
 * @author Marcin Bukowiecki
 */
class RefreshAction : BaseShellAction(AllIcons.Actions.Refresh) {

    override fun getTooltipText(): String {
        return WeevilDebuggerBundle.message("weevil.debugger.refresh")
    }

    override fun getHandler(): ShellHandler {
        return RefreshHandler()
    }
}

class RefreshHandler : ShellHandler {

    override fun handle(editor: Editor, shellTab: ShellTab) {
        val breakpoint = WeevilDebuggerUtils.getCurrentBreakpoint(shellTab.session) ?: return
        val language = PsiDocumentManager.getInstance(shellTab.project).getPsiFile(editor.document)?.language ?: return
        shellTab.reload(language, breakpoint.sourcePosition ?: return)
    }
}
