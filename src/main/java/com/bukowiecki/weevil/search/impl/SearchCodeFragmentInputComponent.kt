/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.search.impl

import com.bukowiecki.weevil.search.WeevilSearcher
import com.bukowiecki.weevil.services.WeevilDebuggerService
import com.bukowiecki.weevil.utils.WeevilDebuggerDataKey
import com.bukowiecki.weevil.xdebugger.impl.GetPsiHandler
import com.bukowiecki.weevil.xdebugger.impl.WeevilCodeEditor
import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiFile
import com.intellij.xdebugger.XExpression
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.evaluation.EvaluationMode
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl
import com.intellij.xdebugger.impl.ui.XDebuggerEditorBase
import javax.swing.JPanel

/**
 * @author Marcin Bukowiecki
 */
class SearchCodeFragmentInputComponent(
    override val codeFragmentPanel: JPanel,
    private val myProject: Project,
    editorsProvider: XDebuggerEditorsProvider,
    sourcePosition: XSourcePosition,
    override val language: Language
) : SearchInputComponent {

    private val myCodeEditor: XDebuggerEditorBase

    init {
        myCodeEditor = WeevilCodeEditor(
            codeFragmentPanel,
            myProject,
            editorsProvider,
            sourcePosition,
            "WeevilDebugger.SearchDialog.CodeFragment",
            EvaluationMode.CODE_FRAGMENT,
            true,
            language
        )
        val self = this
        myCodeEditor.getPsiFileHandler = object : GetPsiHandler {

            override fun handle(psiFile: PsiFile): PsiFile {
                psiFile.putUserData(WeevilDebuggerDataKey.weevilCodeFragmentInputComponent, self)
                return psiFile
            }
        }
    }

    override fun getEditor() = myCodeEditor

    override fun getEvaluationMode(): EvaluationMode = EvaluationMode.CODE_FRAGMENT

    override fun getEmptyCode(): XExpression = XExpressionImpl.EMPTY_CODE_FRAGMENT

    override fun getSearcher(): WeevilSearcher? = WeevilDebuggerService.getInstance(myProject)
        .findSearcher(language)?.invoke()

    fun isEditorDisposed(): Boolean {
        return myCodeEditor.editor?.isDisposed ?: false
    }

    override fun dispose() {
        Disposer.dispose(this)
    }
}