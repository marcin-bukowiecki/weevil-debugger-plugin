/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.editor

import com.bukowiecki.weevil.listeners.WeevilDebuggerListener
import com.bukowiecki.weevil.services.WeevilDebuggerService
import com.bukowiecki.weevil.settings.WeevilDebuggerSettings
import com.intellij.debugger.engine.JavaDebugProcess
import com.intellij.debugger.engine.JavaStackFrame
import com.intellij.debugger.engine.evaluation.EvaluateException
import com.intellij.debugger.impl.DebuggerUtilsEx
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.messages.MessageBusConnection
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Marcin Bukowiecki
 */
class WeevilEditorTracker constructor(
    private val myProject: Project,
    private val myJavaDebugProcess: JavaDebugProcess
) : Disposable {

    private val log = Logger.getInstance(WeevilEditorTracker::class.java)

    private val myConnection: MessageBusConnection = myProject.messageBus.connect()
    private val myLock = Object()
    private val myRunId = AtomicInteger()

    //files opened on suspension like: breakpoint hits, step out, step into, step over or clicking on stack frame
    @Volatile
    private var myPotentialFileToOpen: TrackedFile? = null
    @Volatile
    private var myCachedFilesToTrack = setOf<String>()
    @Volatile
    private var myFilesToTrack = setOf<TrackedFile>()

    fun startListening() {
        if (!WeevilDebuggerSettings.getInstance(myProject).autoCloseFiles) {
            return
        }

        val self = this

        myConnection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object :
            FileEditorManagerListener {

            override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
                if (myCachedFilesToTrack.contains(file.url)) {
                    synchronized(myLock) {
                        myCachedFilesToTrack = myCachedFilesToTrack.filter { it != file.url }.toSet()
                        myFilesToTrack = myFilesToTrack.filter { myCachedFilesToTrack.contains(it.file.url) }.toSet()
                    }
                }
            }

            override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                synchronized(myLock) {
                    val given = file.url
                    myPotentialFileToOpen?.let {
                        val actual = it.url
                        if (given == actual) {
                            myFilesToTrack = myFilesToTrack + setOf(self.myPotentialFileToOpen!!)
                            myPotentialFileToOpen = null
                        }
                    }
                }
            }
        })

        val myWeevilDebuggerService = WeevilDebuggerService.getInstance(myProject)

        myConnection.subscribe(myWeevilDebuggerService.topic, object : WeevilDebuggerListener {

            override fun stackFrameChanged() {
                tryAddTrackedFile(true)
            }

            @Suppress("UnstableApiUsage")
            override fun sessionStopped() {
                val currentFilesToTrack = self.myFilesToTrack
                self.dispose()
                SessionStoppedHandler(myProject, currentFilesToTrack).handle()
            }

            override fun sessionPaused() {
                checkEditors()
                tryAddTrackedFile(false)
            }

            private fun tryAddTrackedFile(openedFromFrames: Boolean) {
                val javaStackFrame = myJavaDebugProcess.session.currentStackFrame as? JavaStackFrame ?: return
                val currentPosition = javaStackFrame.sourcePosition ?: kotlin.run {
                    log.info("Could not get source position")
                    return
                }

                val potentialFileToOpen = currentPosition.file
                FileEditorManager.getInstance(myProject).openFiles.forEach {
                    //already opened
                    if (potentialFileToOpen.url == it.url) {
                        return
                    }
                }

                synchronized(myLock) {
                    val threadReference = javaStackFrame.stackFrameProxy.threadProxy()
                    log.info("Started tracking file: " + potentialFileToOpen.name)
                    self.myCachedFilesToTrack = self.myCachedFilesToTrack + setOf(potentialFileToOpen.url)
                    self.myPotentialFileToOpen = TrackedFile(
                        openedFromFrames,
                        potentialFileToOpen.url,
                        potentialFileToOpen,
                        threadReference.uniqueID(),
                        threadReference
                    )
                }
            }
        })
    }

    @Suppress("UnstableApiUsage")
    fun checkEditors() {
        val runId = myRunId.incrementAndGet()
        val fileEditorManager = FileEditorManager.getInstance(myProject)
        val debugProcessImpl = myJavaDebugProcess.debuggerSession.process
        val currentFilesToTrack = this.myFilesToTrack
        val filesToClose = mutableSetOf<VirtualFile>()

        //list of used threads (so we don't need to iterate over all current running threads)
        val threadsToCheck = currentFilesToTrack
            .filter { !it.openedFromFrames }
            .map { it.openedByThread }
            .distinctBy { it.uniqueID() }

        val usedFiles = currentFilesToTrack
            .filter { !it.openedFromFrames }
            .map { it.file }
            .distinctBy { it.url }

        forLabel@
        for (usedFile in usedFiles) {
            for (threadRef in threadsToCheck) {
                try {
                    for (frame in threadRef.frames()) {
                        val sourcePosition = DebuggerUtilsEx
                            .toXSourcePosition(debugProcessImpl.positionManager.getSourcePosition(frame.location()))
                            ?: continue

                        //is used, so skip
                        if (sourcePosition.file.url == usedFile.url) {
                            continue@forLabel
                        }
                    }
                } catch (ex: EvaluateException) {
                    log.info("Got evaluate exception", ex)
                }
            }

            filesToClose.add(usedFile)
        }

        if (runId == myRunId.get()) {
            synchronized(myLock) {
                for (fileToClose in filesToClose) {
                    //close unreachable editor from stack frames
                    ApplicationManager.getApplication().invokeLaterOnWriteThread {
                        log.info("Closing unused editor: " + fileToClose.url)
                        fileEditorManager.closeFile(fileToClose)
                    }
                }
            }
        }
    }

    override fun dispose() {
        myPotentialFileToOpen = null
        myCachedFilesToTrack = emptySet()
        myFilesToTrack = emptySet()
        myConnection.dispose()
    }
}
