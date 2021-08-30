/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.editor

import com.bukowiecki.weevil.editor.ui.ToCloseDialog
import com.bukowiecki.weevil.settings.WeevilDebuggerSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * @author Marcin Bukowiecki
 */
class SessionStoppedHandler(private val myProject: Project,
                            private val myTrackedFiles: Set<TrackedFile>) {

    private val log = Logger.getInstance(SessionStoppedHandler::class.java)

    @Suppress("UnstableApiUsage")
    fun handle() {
        if (myTrackedFiles.isNotEmpty()) {
            val settings = WeevilDebuggerSettings.getInstance(myProject)
            if (settings.autoCloseFiles) {
                if (settings.showPromptToCloseFiles) {
                    ApplicationManager.getApplication().invokeLaterOnWriteThread {
                        ToCloseDialog(
                            myProject,
                            getActualTrackedFiles()
                        ).show()
                    }
                } else {
                    val fileEditorManager = FileEditorManager.getInstance(myProject)
                    ApplicationManager.getApplication().invokeLaterOnWriteThread {
                        val filesToClose = getActualTrackedFiles()

                        for (toClose in filesToClose) {
                            log.info("Closing unused editor: " + toClose.url)
                            fileEditorManager.closeFile(toClose)
                        }
                    }
                }
            }
        }
    }

    private fun getActualTrackedFiles(): List<VirtualFile> {
        val fileEditorManager = FileEditorManager.getInstance(myProject)
        return myTrackedFiles
            .map { it.file }
            .distinctBy { it.url }
            .filter { fileEditorManager.isFileOpen(it) }
    }
}