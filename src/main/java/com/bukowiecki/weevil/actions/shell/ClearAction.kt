/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.actions.shell

import com.bukowiecki.weevil.bundle.WeevilDebuggerBundle
import com.bukowiecki.weevil.shell.evaluator.ClearShellHandler
import com.bukowiecki.weevil.shell.evaluator.ShellHandler
import com.intellij.icons.AllIcons

/**
 * @author Marcin Bukowiecki
 */
class ClearAction : BaseShellAction(AllIcons.Diff.Remove) {

    override fun getTooltipText(): String {
        return WeevilDebuggerBundle.getMessage("weevil.debugger.clear")
    }

    override fun getHandler(): ShellHandler {
        return ClearShellHandler()
    }
}