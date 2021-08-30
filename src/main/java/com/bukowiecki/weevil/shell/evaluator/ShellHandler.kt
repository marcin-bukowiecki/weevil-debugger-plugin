/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.shell.evaluator

import com.bukowiecki.weevil.shell.ShellTab
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiDocumentManager
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl

/**
 * @author Marcin Bukowiecki
 */
interface ShellHandler : Disposable {

    fun handle(editor: Editor, shellTab: ShellTab)

    override fun dispose() {
        Disposer.dispose(this)
    }
}

/**
 * @author Marcin Bukowiecki
 */
class InspectShellHandler : ShellHandler {

    private var myDisposer = Disposer.newDisposable()

    override fun handle(editor: Editor, shellTab: ShellTab) {
        val selectedText = editor.selectionModel.selectedText
        if (selectedText.isNullOrEmpty()) return

        shellTab.controller.inspect(myDisposer, selectedText)
    }
}

/**
 * @author Marcin Bukowiecki
 */
class ClearShellHandler : ShellHandler {

    override fun handle(editor: Editor, shellTab: ShellTab) {
        ApplicationManager.getApplication().runWriteAction {
            editor.document.setText("")
            PsiDocumentManager.getInstance(editor.project ?: return@runWriteAction).commitDocument(editor.document)
        }
    }
}

/**
 * @author Marcin Bukowiecki
 */
class ClearHistoryShellHandler : ShellHandler {

    override fun handle(editor: Editor, shellTab: ShellTab) {
        shellTab.clearTree()
    }
}

/**
 * @author Marcin Bukowiecki
 */
class ExecuteShellHandler : ShellHandler {

    override fun handle(editor: Editor, shellTab: ShellTab) {
        val selectedText = editor.selectionModel.selectedText
        val expr = if (selectedText.isNullOrEmpty()) {
            shellTab.getExpression()
        } else {
            XExpressionImpl.fromText(selectedText)
        }

        if (expr.expression.isEmpty()) return

        shellTab.controller.evaluate(expr, selectedText, this)
    }
}