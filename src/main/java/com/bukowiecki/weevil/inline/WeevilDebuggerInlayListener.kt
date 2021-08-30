/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.inline

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.openapi.editor.event.EditorMouseMotionListener
import com.intellij.openapi.project.Project

/**
 * @author Marcin Bukowiecki
 */
class WeevilDebuggerInlayListener(private val myProject: Project) : EditorMouseMotionListener, EditorMouseListener {

    private var lastHoveredInlay: Inlay<*>? = null
    private var myListening = false

    @Suppress("unused")
    fun startListening() {
        if (!myListening) {
            myListening = true
            val multicaster = EditorFactory.getInstance().eventMulticaster
            multicaster.addEditorMouseMotionListener(this, myProject)
            multicaster.addEditorMouseListener(this, myProject)
        }
    }

    override fun mouseMoved(event: EditorMouseEvent) {
        val inlay = event.inlay
        if (lastHoveredInlay != null) {
            val renderer = lastHoveredInlay!!.renderer as WeevilInlineDebugRenderer
            if (lastHoveredInlay !== event.inlay) {
                renderer.onMouseExit(lastHoveredInlay, event)
            }
            lastHoveredInlay = null
        }
        if (inlay != null) {
            lastHoveredInlay = if (inlay.renderer is WeevilInlineDebugRenderer) {
                (inlay.renderer as WeevilInlineDebugRenderer).onMouseMove(inlay, event)
                inlay
            } else {
                null
            }
        }
    }

    override fun mouseClicked(event: EditorMouseEvent) {
        if (event.isConsumed) return
        val inlay = event.inlay
        if (inlay != null && inlay.renderer is WeevilInlineDebugRenderer) {
            (inlay.renderer as WeevilInlineDebugRenderer).onClick(inlay, event)
            event.consume()
        }
    }

    companion object {

        fun getInstance(project: Project): WeevilDebuggerInlayListener {
            return project.getService(WeevilDebuggerInlayListener::class.java)
        }
    }
}