/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.codesource

import com.intellij.lang.jvm.JvmModifier
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiClass
import com.intellij.xdebugger.XDebugProcess
import com.bukowiecki.weevil.utils.WeevilDebuggerUtils

/**
 * @author Marcin Bukowiecki
 */
class JavaCodeSourceLocationStrategy : CodeSourceLocationStrategy {

    private val log = Logger.getInstance(JavaCodeSourceLocationStrategy::class.java)

    private val instanceExprText = "this.getClass().getProtectionDomain().getCodeSource().getLocation().toString()"

    @Suppress("UnstableApiUsage")
    override fun getExpression(debugProcess: XDebugProcess): Pair<PsiClass, String>? {
        return debugProcess.session.currentStackFrame?.sourcePosition?.let { sourcePosition ->
            val method = WeevilDebuggerUtils.getMethod(debugProcess.session.project, sourcePosition) ?: return@let null
            return ApplicationManager.getApplication().runReadAction<Pair<PsiClass, String>?> {
                val containingClass = method.containingClass ?: return@runReadAction null
                return@runReadAction if (method.hasModifier(JvmModifier.STATIC)) {
                    Pair(containingClass, containingClass.name + ".class.getProtectionDomain().getCodeSource().getLocation().toString()")
                } else {
                    Pair(containingClass, instanceExprText)
                }
            }
        }
    }

    override fun handleError(expr: String, errorMessage: String) {
        log.info("Error while evaluating expression: $expr, $errorMessage")
    }
}