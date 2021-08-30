/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.views

import com.bukowiecki.weevil.bundle.WeevilDebuggerBundle
import com.bukowiecki.weevil.debugger.controller.SessionController
import com.bukowiecki.weevil.inlay.WeevilDebuggerInlayUtil
import com.bukowiecki.weevil.listeners.WeevilDebuggerListener
import com.bukowiecki.weevil.services.WeevilDebuggerService
import com.bukowiecki.weevil.settings.WeevilDebuggerSettings
import com.bukowiecki.weevil.xdebugger.WeevilDebuggerRootNode
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.util.ui.components.BorderLayoutPanel
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.intellij.xdebugger.impl.actions.XDebuggerActions
import com.intellij.xdebugger.impl.frame.XDebugView
import com.intellij.xdebugger.impl.frame.XValueMarkers
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTree
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTreePanel
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueContainerNode

/**
 * @author Marcin Bukowiecki
 */
class WeevilDebuggerFutureValuesView(
    val project: Project,
    editorsProvider: XDebuggerEditorsProvider,
    markers: XValueMarkers<*, *>,
    private val sessionController: SessionController
) : XDebugView() {

    private var myLock = Object()

    private var currentPsiFile: PsiFile? = null

    private val myConnection = project.messageBus.connect()

    private val myTreePanel: XDebuggerTreePanel = XDebuggerTreePanel(
        project, editorsProvider, this, null, XDebuggerActions.VARIABLES_TREE_POPUP_GROUP, markers
    )

    private val myComponent: BorderLayoutPanel = BorderLayoutPanel()

    init {
        val settings = WeevilDebuggerSettings.getInstance(project)
        myComponent.add(myTreePanel.mainPanel)
        myConnection.subscribe(WeevilDebuggerService.getInstance(project).topic, object : WeevilDebuggerListener {

            override fun threadChanged() {

            }

            override fun settingsChanged() {
                if (settings.showMethodReturnValues) {
                    showMethodReturnValues()
                } else {
                    hideMethodReturnValues()
                }
            }

            override fun sessionStopped() {
                myTreePanel.tree.dispose()
            }

            override fun futureEvaluated() {
                myTreePanel.tree.isEnabled = true
                myTreePanel.tree.isEditable = true
            }

            override fun evaluateFuture() {
                myTreePanel.tree.isEnabled = false
                myTreePanel.tree.isEditable = false
            }
        })

        myTreePanel.tree.addTreeSelectionListener(WeevilTreeSelectionListener(this))
    }

    fun getPanel(): BorderLayoutPanel = myComponent

    override fun dispose() {
        myConnection.dispose()
        myTreePanel.tree.dispose()
    }

    override fun clear() {
        val tree = getTree()
        tree.sourcePosition = null
        val rootNode: XValueContainerNode<*> = WeevilDebuggerRootNode(tree)
        rootNode.setInfoMessage(WeevilDebuggerBundle.message("weevil.debugger.no.data"), null)
        tree.setRoot(rootNode, true)
    }

    override fun processSessionEvent(event: SessionEvent, session: XDebugSession) {

    }

    fun setCurrentFile(psiFile: PsiFile) {
        synchronized(myLock) {
            this.currentPsiFile = psiFile
        }
    }

    fun getCurrentFile() = this.currentPsiFile

    fun getTree(): XDebuggerTree {
        return myTreePanel.tree
    }

    private fun showMethodReturnValues() {
        WeevilDebuggerInlayUtil.removeLineInlays(project)
        sessionController.restoreState()
    }

    private fun hideMethodReturnValues() {
        WeevilDebuggerInlayUtil.removeLineInlays(project)
        sessionController.restoreState()
    }
}