/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.actions.shell

import com.bukowiecki.weevil.bundle.WeevilDebuggerBundle
import com.bukowiecki.weevil.shell.evaluator.InspectShellHandler
import com.bukowiecki.weevil.shell.evaluator.ShellHandler
import com.intellij.icons.AllIcons

/**
 * @author Marcin Bukowiecki
 */
class InspectShellExpression : BaseShellAction(AllIcons.Actions.ToggleSoftWrap) {

    override fun getHandler(): ShellHandler {
        return InspectShellHandler()
    }

    override fun getTooltipText(): String {
        return WeevilDebuggerBundle.getMessage("weevil.debugger.inspect")
    }
}