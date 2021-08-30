/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.editor.ui

import com.bukowiecki.weevil.bundle.WeevilDebuggerBundle
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import javax.swing.JComponent
import javax.swing.JLabel

/**
 * @author Marcin Bukowiecki
 */
class ToCloseDialog(private val myProject: Project,
                    private val myFilesToClose: List<VirtualFile>) : DialogWrapper(myProject, true) {

    private val defaultWidth = 200

    private val log = Logger.getInstance(ToCloseDialog::class.java)

    private lateinit var myCurrentModel: ToCloseTableModel

    init {
        super.init()
        title = WeevilDebuggerBundle.message("weevil.debugger.close.title")
        setOKButtonText(WeevilDebuggerBundle.message("weevil.debugger.close.selected"))
    }

    override fun createCenterPanel(): JComponent {
        val mainLabel = JLabel(WeevilDebuggerBundle.message("weevil.debugger.close.header"))
        val mainPanel = JBUI.Panels.simplePanel()
            .withMinimumWidth(calculateWidth())
            .withMinimumHeight(calculateHeight())
            .addToTop(mainLabel)

        myCurrentModel = ToCloseTableModel(myFilesToClose)
        val table = JBTable(myCurrentModel)
        table.columnModel.getColumn(0).maxWidth = 50
        val scrollPane = ScrollPaneFactory.createScrollPane(table)

        mainPanel.add(scrollPane)

        return mainPanel
    }

    @Suppress("UnstableApiUsage")
    override fun doOKAction() {
        val fileEditorManager = FileEditorManager.getInstance(myProject)
        ApplicationManager.getApplication().invokeLaterOnWriteThread {
            for (toClose in myFilesToClose) {
                if (myCurrentModel.isChecked(toClose)) {
                    log.info("Closing unused editor: " + toClose.url)
                    fileEditorManager.closeFile(toClose)
                }
            }
        }
        super.doOKAction()
    }

    private fun calculateWidth(): Int {
        val maxLength = myFilesToClose.map { it.presentableName }.maxOfOrNull { it.length } ?: return defaultWidth
        return 100 + (maxLength * 10)
    }

    private fun calculateHeight(): Int {
        return 120 + (myFilesToClose.size * 12)
    }
}