/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.shell

import com.bukowiecki.weevil.debugger.engine.WeevilErrorTreeNode
import com.bukowiecki.weevil.utils.WeevilDebuggerUtils
import com.intellij.debugger.engine.DebuggerManagerThreadImpl
import com.intellij.debugger.engine.JavaValue
import com.intellij.debugger.impl.PrioritizedTask
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.xdebugger.XExpression
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.XValue
import com.intellij.xdebugger.impl.ui.tree.XInspectDialog

/**
 * @author Marcin Bukowiecki
 */
class ShellController(private val shellTab: ShellTab) {

    fun evaluate(expression: XExpression, selectedText: String?, caller: Disposable) {
        DebuggerManagerThreadImpl.createTestInstance(caller, shellTab.project).invoke(PrioritizedTask.Priority.HIGH) {
            WeevilDebuggerUtils.getCurrentSession(shellTab.project)?.let { session ->
                session.debugProcess.evaluator?.evaluate(
                    expression,
                    object : XDebuggerEvaluator.XEvaluationCallback {

                        override fun errorOccurred(errorMessage: String) {
                            shellTab.addError(selectedText, errorMessage)
                        }

                        @Suppress("UnstableApiUsage")
                        override fun evaluated(result: XValue) {
                            (result as? JavaValue)?.let {
                                shellTab.addToTree(selectedText, it)
                            }
                        }
                    },
                    null
                )
            }
        }
    }

    fun inspect(caller: Disposable, selectedText: String) {
        val session = shellTab.session

        DebuggerManagerThreadImpl.createTestInstance(caller, shellTab.project)
            .invoke(PrioritizedTask.Priority.HIGH) {
                shellTab.session.debugProcess.evaluator?.evaluate(
                    selectedText,
                    object : XDebuggerEvaluator.XEvaluationCallback {

                        @Suppress("UnstableApiUsage")
                        override fun errorOccurred(errorMessage: String) {
                            ApplicationManager.getApplication().invokeLaterOnWriteThread {
                                val dialog = XInspectDialog(
                                    shellTab.project,
                                    shellTab.myEditorsProvider,
                                    shellTab.sourcePosition,
                                    selectedText,
                                    WeevilErrorTreeNode("result", errorMessage),
                                    session.valueMarkers,
                                    session,
                                    true
                                )
                                dialog.show()
                            }
                        }

                        @Suppress("UnstableApiUsage")
                        override fun evaluated(result: XValue) {
                            ApplicationManager.getApplication().invokeLaterOnWriteThread {
                                val dialog = XInspectDialog(
                                    shellTab.project,
                                    shellTab.myEditorsProvider,
                                    shellTab.sourcePosition,
                                    selectedText,
                                    result,
                                    session.valueMarkers,
                                    session,
                                    true
                                )
                                dialog.show()
                            }
                        }
                    },
                    shellTab.sourcePosition
                )
            }
    }
}