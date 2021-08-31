/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.listeners.dependencies

import com.bukowiecki.weevil.services.WeevilDebuggerService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import org.jetbrains.kotlin.idea.KotlinLanguage

/**
 * @author Marcin Bukowiecki
 */
class KotlinDependencyLoader : StartupActivity {

    @Suppress("UnstableApiUsage")
    override fun runActivity(project: Project) {
        val weevilDebuggerService = WeevilDebuggerService.getInstance(project)
        weevilDebuggerService.addShellSupportedLanguage(KotlinLanguage.INSTANCE)
    }
}