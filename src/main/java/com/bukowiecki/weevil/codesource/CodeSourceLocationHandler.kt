/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.codesource

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.project.Project
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.XValue
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl
import com.bukowiecki.weevil.debugger.listeners.WeevilDebuggerContext
import com.bukowiecki.weevil.handlers.SessionHandler
import com.bukowiecki.weevil.settings.WeevilDebuggerSettings

/**
 * @author Marcin Bukowiecki
 */
class CodeSourceLocationHandler(private val project: Project) : SessionHandler {

    private val strategyHandler =
        mapOf<String, CodeSourceLocationStrategy>(JavaLanguage.INSTANCE.id to JavaCodeSourceLocationStrategy())

    override fun canHandle(weevilDebuggerContext: WeevilDebuggerContext): Boolean {
        return WeevilDebuggerSettings.getInstance(project).showSourceCodeLocation
    }

    override fun handle(weevilDebuggerContext: WeevilDebuggerContext) {
        if (!canHandle(weevilDebuggerContext)) return

        val debugProcess = weevilDebuggerContext.debugProcess
        val language = weevilDebuggerContext.getLanguage() ?: return
        val codeSourceLocationStrategy = strategyHandler[language.id] ?: return
        val pair = codeSourceLocationStrategy.getExpression(debugProcess) ?: return
        if (pair.second.isEmpty()) return
        val expr = pair.second

        debugProcess.evaluator?.evaluate(
            XExpressionImpl(expr, language, ""),
            object : XDebuggerEvaluator.XEvaluationCallback {

                override fun errorOccurred(errorMessage: String) {
                    codeSourceLocationStrategy.handleError(expr, errorMessage)
                }

                override fun evaluated(result: XValue) {
                    codeSourceLocationStrategy.handleValue(pair.first, result)
                }

            },
            null
        )
    }
}