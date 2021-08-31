/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.xdebugger.impl

import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.CommonShortcuts
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiDocumentManager
import com.intellij.ui.EditorTextField
import com.intellij.ui.JBSplitter
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.xdebugger.XExpression
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.evaluation.EvaluationMode
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl
import com.intellij.xdebugger.impl.ui.XDebuggerEditorBase
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.border.CompoundBorder

/**
 * @author Marcin Bukowiecki
 */
open class WeevilCodeEditor(
    private val myPanel: JPanel,
    project: Project,
    editorsProvider: XDebuggerEditorsProvider,
    sourcePosition: XSourcePosition,
    historyId: String,
    evaluationMode: EvaluationMode,
    private val multiline: Boolean,
    language: Language
) : XDebuggerEditorBase(
    project,
    editorsProvider,
    evaluationMode,
    historyId,
    sourcePosition
), Disposable {

    private val myEditorTextField: EditorTextField

    private var evaluationLang = language
    private var myComponent: JComponent

    var getPsiFileHandler: GetPsiHandler = object : GetPsiHandler { }

    init {
        val expression = XExpressionImpl.fromText("", evaluationMode)
        this.myEditorTextField = createEditor(expression)
        this.myEditorTextField.setFontInheritedFromLAF(false)
        this.myEditorTextField.font = EditorUtil.getEditorFont()

        DumbAwareAction.create { goForward() }
            .registerCustomShortcutSet(CommonShortcuts.MOVE_UP, myEditorTextField)
        DumbAwareAction.create { goBackward() }
            .registerCustomShortcutSet(CommonShortcuts.MOVE_DOWN, myEditorTextField)

        myComponent = decorate(myEditorTextField, multiline, showEditor = false)
        this.expression = expression
    }

    final override fun decorate(component: JComponent?, multiline: Boolean, showEditor: Boolean): JComponent {
        var givenComponent = component
        val panel = JBUI.Panels.simplePanel()
        if (!multiline && showEditor) {
            givenComponent = addExpand(givenComponent, false)
        }
        panel.addToCenter(givenComponent!!)
        return panel
    }

    fun addComponent(contentPanel: JPanel, resultPanel: JPanel?, vertical: Boolean = true) {
        val splitter = JBSplitter(vertical, 0.7f, 0.2f, 0.7f)
        splitter.splitterProportionKey = getDimensionServiceKey() + ".splitter"
        contentPanel.add(splitter, BorderLayout.CENTER)
        splitter.firstComponent = myPanel
        splitter.secondComponent = resultPanel
    }

    private fun createEditor(expression: XExpression): EditorTextField {
        val editor = object : EditorTextField(
            createDocument(expression),
            project,
            editorsProvider.fileType,
            false,
            !multiline
        ) {
            override fun createEditor(): EditorEx {
                val editor = super.createEditor()
                editor.setHorizontalScrollbarVisible(multiline)
                editor.setVerticalScrollbarVisible(multiline)
                editor.settings.isUseSoftWraps = true
                editor.settings.lineCursorWidth = EditorUtil.getDefaultCaretWidth()
                editor.colorsScheme.editorFontName = font.fontName
                editor.colorsScheme.editorFontSize = font.size
                if (multiline) {
                    editor.contentComponent.border =
                        CompoundBorder(editor.contentComponent.border, JBUI.Borders.emptyLeft(2))
                    editor.contextMenuGroupId = "XDebugger.Evaluate.Code.Fragment.Editor.Popup"
                } else {
                    foldNewLines(editor)
                    setExpandable(editor)
                }
                fillUserData(editor)
                return editor
            }

            override fun getData(dataId: String): Any? {
                if (LangDataKeys.CONTEXT_LANGUAGES.`is`(dataId)) {
                    return arrayOf<Language>(JavaLanguage.INSTANCE)
                } else if (CommonDataKeys.PSI_FILE.`is`(dataId)) {
                    return PsiDocumentManager
                        .getInstance(project)
                        .getPsiFile(document)?.let { return getPsiFileHandler.handle(it.context?.containingFile ?: return it) }
                }
                return super.getData(dataId)
            }
        }

        editor.setFontInheritedFromLAF(false)
        editor.font = EditorUtil.getEditorFont()

        return editor
    }

    open fun fillUserData(editor: Editor) {

    }

    private fun getDimensionServiceKey(): String {
        return "#xdebugger.evaluate"
    }

    override fun getComponent(): JComponent {
        return myComponent
    }

    override fun getEditorComponent(): JComponent {
        return myEditorTextField
    }

    override fun doSetText(text: XExpression) {
        ApplicationManager.getApplication().runWriteAction {
            myEditorTextField.setNewDocumentAndFileType(getFileType(text), createDocument(text))
        }
    }

    override fun getExpression(): XExpression {
        return editorsProvider.createExpression(
            project,
            myEditorTextField.document,
            evaluationLang,
            EvaluationMode.CODE_FRAGMENT
        )
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        val editor: Editor? = myEditorTextField.editor
        return editor?.contentComponent
    }

    override fun setEnabled(enable: Boolean) {
        if (enable == myComponent.isEnabled) return
        UIUtil.setEnabled(myComponent, enable, true)
    }

    override fun getEditor(): Editor? {
        return myEditorTextField.editor
    }

    override fun selectAll() {
        myEditorTextField.selectAll()
    }

    override fun dispose() {
        Disposer.dispose(this)
    }
}