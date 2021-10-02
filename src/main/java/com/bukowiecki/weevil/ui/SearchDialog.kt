/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.ui

import com.bukowiecki.weevil.bundle.WeevilDebuggerBundle
import com.bukowiecki.weevil.debugger.engine.WeevilJavaValue
import com.bukowiecki.weevil.debugger.ui.WeevilNamedValueDescriptorImpl
import com.bukowiecki.weevil.debugger.ui.WithHistoryDescriptor
import com.bukowiecki.weevil.search.SearchXDebuggerTreeListener
import com.bukowiecki.weevil.search.impl.SearchCodeFragmentInputComponent
import com.bukowiecki.weevil.search.impl.SearchExpressionInputComponent
import com.bukowiecki.weevil.search.impl.SearchInputComponent
import com.bukowiecki.weevil.settings.WeevilDebuggerSettings
import com.bukowiecki.weevil.xdebugger.WeevilDebuggerRootNode
import com.intellij.debugger.engine.JavaDebugProcess
import com.intellij.debugger.engine.JavaDebuggerEvaluator
import com.intellij.debugger.engine.JavaValue
import com.intellij.icons.AllIcons
import com.intellij.lang.Language
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.Disposer
import com.intellij.util.ui.JBUI
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.frame.XValueChildrenList
import com.intellij.xdebugger.impl.actions.XDebuggerActions
import com.intellij.xdebugger.impl.frame.XValueMarkers
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTree
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTreePanel
import java.awt.BorderLayout
import java.awt.Dimension
import java.util.*
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.tree.TreePath

/**
 * @author Marcin Bukowiecki
 */
open class SearchDialog(
    val javaValue: JavaValue,
    val descriptor: WithHistoryDescriptor,
    private val myJavaDebugProcess: JavaDebugProcess,
    val sourcePosition: XSourcePosition,
    val language: Language,
    markers: XValueMarkers<*, *>
) : DialogWrapper(javaValue.project, true), Disposable {

    var searchXDebuggerTreeListener: SearchXDebuggerTreeListener? = null

    private lateinit var mySearchInputComponent: SearchInputComponent

    private val myProject: Project = javaValue.project
    private val myTreePanel: XDebuggerTreePanel = XDebuggerTreePanel(
        javaValue.project,
        myJavaDebugProcess.editorsProvider,
        getThisDisposable(),
        null,
        XDebuggerActions.VARIABLES_TREE_POPUP_GROUP,
        markers
    )
    private val myController =
        SearchController(getThisDisposable(), javaValue.evaluationContext.debugProcess, javaValue.project)
    private val myMainPanel: JPanel = JPanel(BorderLayout())
    private val myHistoryPanel: JPanel
    private val mySearchPanel: JPanel

    val historyLabel = JLabel(WeevilDebuggerBundle.message(
        "weevil.debugger.search.info",
        WeevilDebuggerSettings.getInstance(myProject).maxSearchDepth
    ))
    val tree: XDebuggerTree
        get() = myTreePanel.tree

    init {
        isModal = false

        val searchLabel = JLabel(WeevilDebuggerBundle.message("weevil.debugger.expression"))
        mySearchPanel = JBUI.Panels.simplePanel()
            .withMinimumWidth(450)
            .addToTop(searchLabel)
        myMainPanel.add(mySearchPanel, BorderLayout.NORTH)

        historyLabel.icon = AllIcons.Actions.IntentionBulb
        historyLabel.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

        myHistoryPanel = JBUI.Panels.simplePanel()
            .addToTop(historyLabel)
            .addToCenter(myTreePanel.mainPanel)
        myMainPanel.add(myHistoryPanel, BorderLayout.CENTER)

        super.init()

        title = this.provideTitle()

        setOKButtonText(WeevilDebuggerBundle.message("weevil.debugger.search"))
        handleCollapse()
    }

    private fun handleExpand() {
        myMainPanel.removeAll()
        mySearchPanel.removeAll()

        mySearchInputComponent = SearchCodeFragmentInputComponent(
            mySearchPanel,
            myProject,
            myJavaDebugProcess.editorsProvider,
            sourcePosition,
            language
        )
        val editor = mySearchInputComponent.getEditor()
        editor.addCollapseButton {
            handleCollapse()
        }

        mySearchPanel.add(JLabel(WeevilDebuggerBundle.message("weevil.debugger.codeFragment")), BorderLayout.NORTH)
        mySearchPanel.add(editor.component)

        myMainPanel.add(myHistoryPanel)
        mySearchInputComponent.addComponent(myMainPanel, myHistoryPanel)
    }

    private fun handleCollapse() {
        myMainPanel.removeAll()
        mySearchPanel.removeAll()

        mySearchInputComponent = SearchExpressionInputComponent(
            mySearchPanel,
            myProject,
            myJavaDebugProcess.editorsProvider,
            sourcePosition,
            language
        )

        val editor = mySearchInputComponent.getEditor()
        mySearchPanel.add(JLabel(WeevilDebuggerBundle.message("weevil.debugger.expression")), BorderLayout.NORTH)
        mySearchPanel.add(editor.component)

        myMainPanel.add(myHistoryPanel)
        mySearchInputComponent.addComponent(myMainPanel, myHistoryPanel)

        editor.setExpandHandler {
            handleExpand()
        }
    }

    open fun provideTitle(): String {
        return WeevilDebuggerBundle.message("weevil.debugger.search")
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(450, 250)
    }

    override fun createCenterPanel(): JComponent? {
        return myMainPanel
    }

    override fun dispose() {
        mySearchInputComponent.dispose()
        myTreePanel.tree.dispose()
        super.dispose()
        Disposer.dispose(this)
    }

    fun clearTree() {
        val tree = myTreePanel.tree
        tree.root.clearChildren()
        searchXDebuggerTreeListener?.let { tree.removeTreeListener(it) }
    }

    override fun doOKAction() {
        search()
    }

    fun search() {
        myController.search(mySearchInputComponent)
    }

    fun setupTree() {
        val tree = myTreePanel.tree
        val children = XValueChildrenList()
        val rootNode = WeevilDebuggerRootNode(tree)

        for ((i, value) in descriptor.history.withIndex()) {
            val name = descriptor.getName()
            val valueDescriptor = WeevilNamedValueDescriptorImpl(myProject, name, value)
            val createdValue = WeevilJavaValue(
                null,
                name,
                valueDescriptor,
                javaValue.evaluationContext,
                myJavaDebugProcess.nodeManager,
                false
            )
            children.add(getNodeName(name, i), createdValue)
        }

        rootNode.addChildren(children, true)
        tree.setRoot(rootNode, false)
        tree.isVisible = true
    }

    fun getEvaluator(): JavaDebuggerEvaluator? {
        return myJavaDebugProcess.evaluator as? JavaDebuggerEvaluator
    }

    open fun getNodeName(name: String, index: Int): String {
        return name
    }

    fun enableSearchButton() {
        okAction.isEnabled = true
        setOKButtonText(WeevilDebuggerBundle.message("weevil.debugger.search"))
    }

    fun disableSearchButton() {
        okAction.isEnabled = false
        setOKButtonText(WeevilDebuggerBundle.message("weevil.debugger.searching"))
    }

    private fun getThisDisposable(): SearchDialog = this

}
