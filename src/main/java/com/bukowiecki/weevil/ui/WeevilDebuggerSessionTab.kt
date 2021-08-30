/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.ui

import com.bukowiecki.weevil.bundle.WeevilDebuggerBundle
import com.bukowiecki.weevil.debugger.controller.SessionController
import com.bukowiecki.weevil.views.WeevilDebuggerEvaluateView
import com.bukowiecki.weevil.views.WeevilDebuggerFutureValuesView
import com.intellij.debugger.engine.JavaDebugProcess
import com.intellij.execution.ui.layout.PlaceInGrid
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.content.Content
import com.intellij.xdebugger.impl.XDebugSessionImpl
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTree
import java.lang.ref.WeakReference

const val FutureValuesContentId = "FutureValuesContent"
const val EvaluateContentId = "EvaluateContent"

const val tabId = 1000

/**
 * @author Marcin Bukowiecki
 */
class WeevilDebuggerSessionTab(
    val project: Project,
    val debugProcess: JavaDebugProcess,
    val session: XDebugSessionImpl,
): Disposable {
    lateinit var evaluateView: WeevilDebuggerEvaluateView
    lateinit var futureValuesView: WeevilDebuggerFutureValuesView

    val controller = SessionController(debugProcess, session, project, WeakReference(this))

    init {
        val ui = session.ui
        ui.defaults.initTabDefaults(tabId, WeevilDebuggerBundle.message("weevil.debugger.tab.name"), null)
/*
        ui.defaults
            .initContentAttraction(FutureValuesContentId, LayoutViewOptions.STARTUP, LayoutAttractionPolicy.FocusOnce(false))
            .initContentAttraction(EvaluateContentId, LayoutViewOptions.STARTUP, LayoutAttractionPolicy.FocusOnce(false))*/
    }

    fun registerEvaluateView(view: WeevilDebuggerEvaluateView) {
        val ui = session.ui
        evaluateView = view
        val content: Content = ui.createContent(
            EvaluateContentId,
            view.myMainPanel,
            WeevilDebuggerBundle.message("weevil.debugger.evaluate"), null, null
        )
        content.isCloseable = false
        ui.addContent(content, tabId, PlaceInGrid.left, false)
        Disposer.register(session.runContentDescriptor, view)
    }

    fun registerFutureValuesView(view: WeevilDebuggerFutureValuesView) {
        val ui = session.ui
        futureValuesView = view
        val content: Content = ui.createContent(
            FutureValuesContentId,
            view.getPanel(),
            WeevilDebuggerBundle.message("weevil.debugger.futureValues"), null, null
        )
        content.isCloseable = false
        ui.addContent(content, tabId, PlaceInGrid.center, false)
        Disposer.register(session.runContentDescriptor, view)
    }

    fun getTree(): XDebuggerTree {
        return futureValuesView.getTree()
    }

    override fun dispose() {
        controller.dispose()
        evaluateView.dispose()
        futureValuesView.dispose()
        Disposer.dispose(this)
    }
}