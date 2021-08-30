/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.debugger.listeners

import com.bukowiecki.weevil.codesource.CodeSourceLocationHandler
import com.bukowiecki.weevil.editor.WeevilEditorTracker
import com.bukowiecki.weevil.handlers.CodeMarkerHandler
import com.bukowiecki.weevil.handlers.SessionStoppedDefaultHandler
import com.bukowiecki.weevil.services.WeevilDebuggerService
import com.bukowiecki.weevil.ui.WeevilDebuggerSessionTab
import com.bukowiecki.weevil.utils.WeevilDebuggerUtils
import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugSessionListener

/**
 * @author Marcin Bukowiecki
 */
class WeevilDebugSessionListener(private val sessionTab: WeevilDebuggerSessionTab) : XDebugSessionListener {

    private var messageBus = sessionTab.project.messageBus

    private val weevilService = WeevilDebuggerService.getInstance(sessionTab.project)

    private val pausedHandlers = listOf(
        CodeMarkerHandler(sessionTab.project),
        CodeSourceLocationHandler(sessionTab.project)
    )

    private val stopHandlers = listOf(
        CodeMarkerHandler(sessionTab.project),
        SessionStoppedDefaultHandler(sessionTab.project)
    )

    private val weevilDebuggerContext = WeevilDebuggerContext(sessionTab.project, sessionTab.debugProcess)

    private val weevilEditorTracker: WeevilEditorTracker = WeevilEditorTracker(sessionTab.project, sessionTab.debugProcess)

    init {
        weevilEditorTracker.startListening()
    }

    override fun settingsChanged() {

    }

    override fun stackFrameChanged() {
        messageBus.syncPublisher(weevilService.topic).stackFrameChanged()
    }

    override fun sessionStopped() {
        stopHandlers.forEach { it.handle(weevilDebuggerContext) }
        sessionTab.dispose()
        WeevilDebuggerUtils.getShellTab(sessionTab.session)?.dispose()
        messageBus.syncPublisher(weevilService.topic).sessionStopped()
    }

    override fun sessionResumed() {
        stopHandlers.forEach { it.handle(weevilDebuggerContext) }
    }

    override fun sessionPaused() {
        pausedHandlers.forEach { it.handle(weevilDebuggerContext) }
        messageBus.syncPublisher(weevilService.topic).sessionPaused()
    }
}

class WeevilDebuggerContext(val project: Project, val debugProcess: XDebugProcess) {

    fun getLanguage(): Language? {
        val supportedLanguages = WeevilDebuggerService.getInstance(project).supportedLanguages
        val langIds = supportedLanguages.map { it.id }

        return debugProcess.session.currentPosition?.let { xSourcePosition ->
            ApplicationManager.getApplication().runReadAction<Language?> {
                val file = xSourcePosition.file
                val psiFile = PsiManager.getInstance(project).findFile(file)
                if (psiFile?.language == null || !langIds.contains(psiFile.language.id)) {
                    null
                } else {
                    psiFile.language
                }
            }
        }
    }

    @Suppress("unused")
    fun getPlace(): PsiFile? {
        return debugProcess.session.currentPosition?.let { xSourcePosition ->
            return ApplicationManager.getApplication().runReadAction<PsiFile?> {
                val file = xSourcePosition.file
                PsiManager.getInstance(project).findFile(file)
            }
        }
    }
}