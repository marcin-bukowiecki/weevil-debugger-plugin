package com.bukowiecki.weevil.handlers

import com.bukowiecki.weevil.debugger.listeners.WeevilDebuggerContext

/**
 * @author Marcin Bukowiecki
 */
interface SessionHandler {

    fun canHandle(weevilDebuggerContext: WeevilDebuggerContext): Boolean

    fun handle(weevilDebuggerContext: WeevilDebuggerContext)
}