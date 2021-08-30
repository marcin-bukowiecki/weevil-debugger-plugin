/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.handlers

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.bukowiecki.weevil.debugger.listeners.WeevilDebuggerContext
import com.bukowiecki.weevil.services.WeevilResultService

/**
 * @author Marcin Bukowiecki
 */
class CodeMarkerHandler(private val project: Project) : SessionHandler {

    override fun canHandle(weevilDebuggerContext: WeevilDebuggerContext): Boolean {
        return true
    }

    override fun handle(weevilDebuggerContext: WeevilDebuggerContext) {
        WeevilResultService.getInstance(project).clear().forEach {
            ApplicationManager.getApplication().runReadAction {
                val psiFile = PsiManager.getInstance(project).findFile(it.virtualFile) ?: return@runReadAction
                DaemonCodeAnalyzer.getInstance(project).restart(psiFile)
            }
        }
    }
}