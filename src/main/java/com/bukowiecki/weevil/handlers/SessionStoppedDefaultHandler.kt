/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.handlers

import com.bukowiecki.weevil.debugger.listeners.WeevilDebuggerContext
import com.bukowiecki.weevil.inlay.WeevilDebuggerInlayUtil
import com.bukowiecki.weevil.services.WeevilDebuggerService
import com.intellij.openapi.project.Project

/**
 * @author Marcin Bukowiecki
 */
class SessionStoppedDefaultHandler(private val project: Project) : SessionHandler {

    override fun canHandle(weevilDebuggerContext: WeevilDebuggerContext): Boolean {
        return true
    }

    override fun handle(weevilDebuggerContext: WeevilDebuggerContext) {
        WeevilDebuggerInlayUtil.removeBlockInlays(project, WeevilDebuggerService.getInstance(project).allBlockInlayTypes)
    }
}