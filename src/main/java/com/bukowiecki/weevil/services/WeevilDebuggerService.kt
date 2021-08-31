/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.services

import com.bukowiecki.weevil.codesource.CodeSourceBlockInlayType
import com.bukowiecki.weevil.inlay.BlockInlayType
import com.bukowiecki.weevil.listeners.WeevilDebuggerListener
import com.bukowiecki.weevil.search.codefragment.BaseCodeFragmentSearcher
import com.bukowiecki.weevil.search.codefragment.JavaCodeFragmentSearcher
import com.intellij.debugger.ui.breakpoints.JavaBreakpointType
import com.intellij.debugger.ui.breakpoints.JavaFieldBreakpointType
import com.intellij.debugger.ui.breakpoints.JavaLineBreakpointType
import com.intellij.debugger.ui.breakpoints.JavaMethodBreakpointType
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.messages.Topic
import com.intellij.xdebugger.impl.breakpoints.XBreakpointUtil
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.uast.util.ClassSet
import org.jetbrains.uast.util.classSetOf

/**
 * @author Marcin Bukowiecki
 */
class WeevilDebuggerService(project: Project): Disposable {

    private val myConnection: MessageBusConnection = project.messageBus.connect()

    val topic = Topic(
        "WeevilDebugger.Events",
        WeevilDebuggerListener::class.java
    )

    @Volatile
    var supportedLanguages = setOf<Language>(
        JavaLanguage.INSTANCE
    )

    @Volatile
    var shellSupportedLanguages = setOf<Language>(
        JavaLanguage.INSTANCE
    )

    @Volatile
    var searchers = mapOf<String, () -> BaseCodeFragmentSearcher>(
        JavaLanguage.INSTANCE.id to {
            JavaCodeFragmentSearcher(project)
        }
    )

    val allBlockInlayTypes = listOf<BlockInlayType>(
        CodeSourceBlockInlayType
    )

    init {
        this.myConnection.subscribe(topic, object : WeevilDebuggerListener {

            override fun settingsChanged() {

            }
        })
    }

    fun addSupportedLanguage(language: Language) {
        this.supportedLanguages = this.supportedLanguages + language
    }

    fun isLanguageSupported(language: Language) = supportedLanguages.contains(language)

    @Suppress("unused")
    fun javaLineBreakpointType(): JavaLineBreakpointType? {
        return XBreakpointUtil.findType("java-line") as? JavaLineBreakpointType
    }

    @Suppress("unused")
    fun javaMethodBreakpointType(): JavaMethodBreakpointType? {
        return XBreakpointUtil.findType("java-method") as? JavaMethodBreakpointType
    }

    @Suppress("unused")
    fun javaFieldBreakpointType(): JavaFieldBreakpointType? {
        return XBreakpointUtil.findType("java-field") as? JavaFieldBreakpointType
    }

    fun javaBreakpointClassesForSetup(): ClassSet<JavaBreakpointType<*>> {
        return classSetOf(
            JavaLineBreakpointType::class.java,
            JavaMethodBreakpointType::class.java,
            JavaFieldBreakpointType::class.java
        )
    }

    companion object {

        fun getInstance(project: Project): WeevilDebuggerService {
            return project.getService(WeevilDebuggerService::class.java)
        }
    }

    override fun dispose() {
        myConnection.dispose()
        Disposer.dispose(this)
    }

    fun addSearcher(language: Language, searcher: () -> BaseCodeFragmentSearcher) {
        searchers = searchers + mapOf(language.id to searcher)
    }

    fun findSearcher(language: Language): (() -> BaseCodeFragmentSearcher)? {
        return searchers[language.id]
    }

    fun addShellSupportedLanguage(language: KotlinLanguage) {
        this.shellSupportedLanguages = this.shellSupportedLanguages + language
    }

    fun isLanguageSupportedForShell(language: Language) = shellSupportedLanguages.contains(language)
}

