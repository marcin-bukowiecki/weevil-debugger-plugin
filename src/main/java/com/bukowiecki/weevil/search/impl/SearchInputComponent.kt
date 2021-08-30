/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.search.impl

import com.bukowiecki.weevil.search.WeevilSearcher
import com.intellij.lang.Language
import com.intellij.openapi.Disposable
import com.intellij.ui.JBSplitter
import com.intellij.xdebugger.XExpression
import com.intellij.xdebugger.evaluation.EvaluationMode
import com.intellij.xdebugger.impl.ui.XDebuggerEditorBase
import java.awt.BorderLayout
import javax.swing.JPanel

/**
 * @author Marcin Bukowiecki
 */
interface SearchInputComponent : CodeProvider, Disposable {

    val codeFragmentPanel: JPanel
    val language: Language

    fun getEditor(): XDebuggerEditorBase

    fun addComponent(myContentPanel: JPanel, myHistoryPanel: JPanel) {
        val splitter = JBSplitter(true, 0.3f, 0.2f, 0.7f)
        splitter.splitterProportionKey = getDimensionServiceKey() + ".splitter"
        myContentPanel.add(splitter, BorderLayout.CENTER)
        splitter.firstComponent = codeFragmentPanel
        splitter.secondComponent = myHistoryPanel
    }

    fun getDimensionServiceKey(): String {
        return "#weevildebugger.search"
    }

    fun getEvaluationMode(): EvaluationMode

    fun getEmptyCode(): XExpression

    override fun provideCode(): XExpression {
        val editor = getEditor()
        editor.saveTextInHistory()
        return editor.expression
    }

    fun getSearcher(): WeevilSearcher?
}