/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.objectdiff.ui

import com.bukowiecki.weevil.bundle.WeevilDebuggerBundle
import com.bukowiecki.weevil.objectdiff.ObjectDiffContext
import com.bukowiecki.weevil.objectdiff.ObjectDiffService
import com.bukowiecki.weevil.objectdiff.controller.ObjectDiffController
import com.bukowiecki.weevil.objectdiff.listeners.ReferenceObjectExpandListener
import com.bukowiecki.weevil.objectdiff.nodes.CompareWithRootNode
import com.bukowiecki.weevil.objectdiff.nodes.ReferenceObjectRootNode
import com.bukowiecki.weevil.objectdiff.nodes.XDebuggerTreeWrapper
import com.bukowiecki.weevil.objectdiff.utils.ObjectDiffUtils
import com.intellij.debugger.engine.JavaDebugProcess
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.ui.JBUI
import com.intellij.xdebugger.frame.XValueChildrenList
import com.intellij.xdebugger.impl.actions.XDebuggerActions
import com.intellij.xdebugger.impl.frame.XValueMarkers
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTreePanel
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeWillExpandListener

/**
 * @author Marcin Bukowiecki
 */
class ObjectDiffDialog(private val myProject: Project,
                       private val myJavaDebugProcess: JavaDebugProcess,
                       private val myMarkers: XValueMarkers<*, *>,
                       evaluationContext: EvaluationContextImpl) : DialogWrapper(myProject, true), Disposable {

    private val myPreferredTreePanelDimension = Dimension(300, 400)
    private val myInitialSize = Dimension(700, 500)
    private val myReferenceObjectTree: XDebuggerTreeWrapper

    val mainPanel = JBUI.Panels.simplePanel()
    val controller = ObjectDiffController(this)
    val compareWithTrees: List<XDebuggerTreeWrapper>

    init {
        val diffService = ObjectDiffService.getInstance()
        diffService.clearCachedValues()
        isModal = false
        super.init()
        title = this.provideTitle()

        val objectToCompare = diffService.getObjectToCompare()
        val objectsToCompareWith = diffService.getObjectsToCompareWith()

        if (objectToCompare == null || objectsToCompareWith.isEmpty()) {
            val infoLabel = JLabel(
                WeevilDebuggerBundle.message("weevil.debugger.objectDiff.error1"),
                AllIcons.Actions.IntentionBulb,
                SwingConstants.LEADING
            )
            mainPanel.addToTop(infoLabel)
            myReferenceObjectTree = XDebuggerTreeWrapper(createTreePanel().tree)
            compareWithTrees = emptyList()
        } else if (!ObjectDiffUtils.checkTypes(objectToCompare, objectsToCompareWith)) {
            val infoLabel = JLabel(
                WeevilDebuggerBundle.message("weevil.debugger.objectDiff.error2", objectToCompare.type().name()),
                AllIcons.Actions.IntentionBulb,
                SwingConstants.LEADING
            )
            mainPanel.addToTop(infoLabel)
            myReferenceObjectTree = XDebuggerTreeWrapper(createTreePanel().tree)
            compareWithTrees = emptyList()
        } else {
            val panel = JPanel(GridLayoutManager(1, 1 + objectsToCompareWith.size))
            panel.size = myInitialSize
            mainPanel.addToCenter(panel)

            val ctx = ObjectDiffService.getInstance().prepareContext(objectToCompare, objectsToCompareWith)
            val treePanel = createTreePanel()
            treePanel.mainPanel.size = myPreferredTreePanelDimension
            val tree = treePanel.tree
            tree.addTreeListener(ReferenceObjectExpandListener(controller))
            val children = XValueChildrenList()
            val rootNode = ReferenceObjectRootNode(tree)

            for (f in ctx.fields.values) {
                val name = f[0].name()
                val value = objectToCompare.getValue(f[0])
                val valueDescriptor = ObjectDiffValueDescriptorImpl(myProject, name, value)
                val createdValue = ObjectDiffValue(
                    null,
                    name,
                    valueDescriptor,
                    evaluationContext,
                    myJavaDebugProcess.nodeManager,
                    false
                )
                children.add(name, createdValue)
            }

            rootNode.addChildren(children, true)

            tree.setRoot(rootNode, false)
            tree.isVisible = true

            val gridConstraints = GridConstraints()
            gridConstraints.row = 0
            gridConstraints.column = 0
            panel.add(treePanel.mainPanel, gridConstraints)

            myReferenceObjectTree = XDebuggerTreeWrapper(tree)
            compareWithTrees = setupOtherObjectsTree(ctx, evaluationContext, panel)
        }
    }

    override fun getInitialSize(): Dimension {
        return myInitialSize
    }

    override fun createCenterPanel(): JComponent = mainPanel

    private fun setupOtherObjectsTree(ctx: ObjectDiffContext,
                                      evaluationContext: EvaluationContextImpl,
                                      panel: JPanel): List<XDebuggerTreeWrapper> {

        val result = mutableListOf<XDebuggerTreeWrapper>()
        var i = 1
        for (objectToCompareWithContext in ctx.withContext) {
            val treePanel = createTreePanel()
            treePanel.mainPanel.size = myPreferredTreePanelDimension
            val tree = treePanel.tree
            tree.addTreeWillExpandListener(object : TreeWillExpandListener {

                override fun treeWillExpand(event: TreeExpansionEvent?) {
                    controller.checkIfExpandIsAllowed(event)
                }

                override fun treeWillCollapse(event: TreeExpansionEvent?) {
                    controller.checkIfCollapseIsAllowed(event)
                }
            })

            val children = XValueChildrenList()
            val rootNode = CompareWithRootNode(tree)

            for (f in objectToCompareWithContext.fields) {
                val name = f.name
                val value = f.value
                val createdValue = ObjectDiffCompareToValue(
                    null,
                    name,
                    CompareToObjectDiffValueDescriptorImpl(
                        myProject,
                        name,
                        value,
                        ctx.objectToCompare.getValue(ctx.fields[name]!![0])
                    ),
                    evaluationContext,
                    myJavaDebugProcess.nodeManager,
                    false
                )
                children.add(name, createdValue)
            }

            rootNode.addChildren(children, true)

            tree.setRoot(rootNode, false)
            tree.isVisible = true

            val gridConstraints = GridConstraints()
            gridConstraints.row = 0
            gridConstraints.column = i
            panel.add(treePanel.mainPanel, gridConstraints)
            result.add(XDebuggerTreeWrapper(treePanel.tree))
            i++
        }

        return result
    }

    private fun provideTitle(): String {
        return WeevilDebuggerBundle.message("weevil.debugger.objectDiff.title")
    }

    override fun dispose() {
        val diffService = ObjectDiffService.getInstance()
        diffService.clearCachedValues()
        diffService.setObjectToCompare(null)
        diffService.clearObjectsToCompareWith()
        myReferenceObjectTree.getXTree().dispose()
        compareWithTrees.forEach { it.getXTree().dispose() }
        super.dispose()
    }

    private fun createTreePanel(): XDebuggerTreePanel {
        return XDebuggerTreePanel(
            myProject,
            myJavaDebugProcess.editorsProvider,
            this,
            null,
            XDebuggerActions.VARIABLES_TREE_POPUP_GROUP,
            myMarkers
        )
    }
}
