/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.services

import com.bukowiecki.weevil.debugger.controller.PsiValueHolder
import com.bukowiecki.weevil.listeners.WeevilDebuggerListener
import com.bukowiecki.weevil.psi.CodeEvent
import com.bukowiecki.weevil.psi.visitors.PsiExtractor
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.messages.MessageBusConnection
import com.sun.jdi.BooleanValue

/**
 * @author Marcin Bukowiecki
 */
class WeevilResultService(private val project: Project): Disposable {

    @Volatile
    private var booleanFalseResult: MutableMap<WeevilResultKey, Set<MatchEntry>> = mutableMapOf()

    @Volatile
    private var booleanTrueResult: MutableMap<WeevilResultKey, Set<MatchEntry>> = mutableMapOf()

    val serviceLock = Object()

    @Volatile
    var xTreeElementToMark: PsiElement? = null

    private val myConnection: MessageBusConnection = project.messageBus.connect()

    init {
        this.myConnection.subscribe(WeevilDebuggerService.getInstance(project).topic, object : WeevilDebuggerListener {

            override fun sessionStopped() {
                clean()
            }

            override fun processStopped() {
                clean()
            }

            private fun clean() {
                xTreeElementToMark?.let {
                    val containingFile = ApplicationManager
                        .getApplication()
                        .runReadAction<PsiFile, Throwable> { it.containingFile }

                    xTreeElementToMark = null
                    DaemonCodeAnalyzer.getInstance(project).restart(containingFile)
                }
                clear().forEach {
                    DaemonCodeAnalyzer.getInstance(project).restart(it)
                }
            }
        })
    }

    fun addBooleanResult(codeEvent: CodeEvent) {
        PsiExtractor().visitCodeEvent(codeEvent).psi?.accept(WeevilBooleanResultVisitor(project))
    }

    fun addBooleanResult(psiElement: PsiElement, valueHolder: PsiValueHolder) {
        (valueHolder.ref.get() as? BooleanValue)?.let {
            ApplicationManager.getApplication().runReadAction {
                val containingFile = psiElement.containingFile
                val virtualFile = containingFile.virtualFile ?: return@runReadAction
                val weevilResultKey = WeevilResultKey(psiElement.containingFile, virtualFile)
                if (it.value()) {
                    booleanTrueResult[weevilResultKey] = booleanTrueResult[weevilResultKey]?.let { set ->
                        set + setOf(MatchEntry(psiElement.startOffset, psiElement.endOffset))
                    } ?: setOf(MatchEntry(psiElement.startOffset, psiElement.endOffset))
                } else {
                    booleanFalseResult[weevilResultKey] = booleanFalseResult[weevilResultKey]?.let { set ->
                        set + setOf(MatchEntry(psiElement.startOffset, psiElement.endOffset))
                    } ?: setOf(MatchEntry(psiElement.startOffset, psiElement.endOffset))
                }
            }
        }
    }

    fun isTrue(psiElement: PsiElement): Boolean {
        val containingFile = psiElement.containingFile
        val virtualFile = containingFile.virtualFile ?: return false
        val entry = MatchEntry(psiElement.startOffset, psiElement.endOffset)
        return booleanTrueResult[WeevilResultKey(psiElement.containingFile, virtualFile)]?.contains(entry) ?: return false
    }

    fun isFalse(psiElement: PsiElement): Boolean {
        val containingFile = psiElement.containingFile
        val virtualFile = containingFile.virtualFile ?: return false
        val entry = MatchEntry(psiElement.startOffset, psiElement.endOffset)
        return booleanFalseResult[WeevilResultKey(psiElement.containingFile, virtualFile)]?.contains(entry) ?: return false
    }

    fun clear(): List<PsiFile> {
        synchronized(this) {
            val result = mutableListOf<PsiFile>()
            result.addAll(booleanFalseResult.keys.map { it.psiFile })
            booleanFalseResult = mutableMapOf()
            result.addAll(booleanTrueResult.keys.map { it.psiFile })
            booleanTrueResult = mutableMapOf()
            return result
        }
    }

    override fun dispose() {
        myConnection.dispose()
        Disposer.dispose(this)
    }

    companion object {

        fun getInstance(project: Project): WeevilResultService {
            return project.getService(WeevilResultService::class.java)
        }
    }
}

/**
 * @author Marcin Bukowiecki
 */
data class MatchEntry(val start: Int, val end: Int)