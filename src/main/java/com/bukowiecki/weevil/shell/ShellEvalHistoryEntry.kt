package com.bukowiecki.weevil.shell

import com.intellij.xdebugger.frame.XValue

/**
 * @author Marcin Bukowiecki
 */
data class ShellEvalHistoryEntry(val expression: String, val xValue: XValue)