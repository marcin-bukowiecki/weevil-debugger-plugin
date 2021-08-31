package com.bukowiecki.weevil.shell

import com.intellij.debugger.engine.JavaValue
import java.lang.ref.WeakReference

/**
 * @author Marcin Bukowiecki
 */
data class ShellEvalHistoryEntry(val expression: String, val xValueRef: WeakReference<JavaValue>)