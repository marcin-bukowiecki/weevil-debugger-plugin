/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.search.impl

import com.bukowiecki.weevil.search.ValueSearcher
import com.bukowiecki.weevil.search.WeevilSearcher
import com.bukowiecki.weevil.ui.SearchExpressionEditor
import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.JBSplitter
import com.intellij.xdebugger.XExpression
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.evaluation.EvaluationMode
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl
import com.intellij.xdebugger.impl.ui.XDebuggerEditorBase
import java.awt.BorderLayout
import javax.swing.JPanel

/**
 * @author Marcin Bukowiecki
 */
class SearchExpressionInputComponent(
    override val codeFragmentPanel: JPanel,
    private val myProject: Project,
    editorsProvider: XDebuggerEditorsProvider,
    sourcePosition: XSourcePosition,
    override val language: Language,
) : SearchInputComponent {

    private val myCodeEditor: XDebuggerEditorBase

    init {
        myCodeEditor = SearchExpressionEditor(
            myProject,
            editorsProvider,
            "WeevilDebugger.SearchDialog.Expression",
            sourcePosition
        )
    }

    override fun addComponent(myContentPanel: JPanel, myHistoryPanel: JPanel) {
        val splitter = JBSplitter(true, 0.1f, 0.1f, 0.1f)
        splitter.splitterProportionKey = getDimensionServiceKey() + ".splitter"
        myContentPanel.add(splitter, BorderLayout.CENTER)
        splitter.firstComponent = codeFragmentPanel
        splitter.secondComponent = myHistoryPanel
    }

    override fun getEvaluationMode(): EvaluationMode = EvaluationMode.EXPRESSION

    override fun getEmptyCode(): XExpression = XExpressionImpl.EMPTY_EXPRESSION

    override fun getSearcher(): WeevilSearcher = ValueSearcher(myProject)

    override fun dispose() {
        Disposer.dispose(this)
    }

    override fun getEditor() = myCodeEditor
}