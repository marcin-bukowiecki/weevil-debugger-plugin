/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.actions

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.psi.*
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.impl.actions.handlers.XDebuggerActionHandler
import com.bukowiecki.weevil.decoders.BinaryExpressionDecoder
import com.bukowiecki.weevil.decoders.ExpressionDecoder
import com.bukowiecki.weevil.decoders.MethodCallDecoder

/**
 * @author Marcin Bukowiecki
 */
class DecodeExpressionHandler : XDebuggerActionHandler() {

    override fun perform(session: XDebugSession, dataContext: DataContext) {
        val editor = CommonDataKeys.EDITOR.getData(dataContext) ?: return
        val psiFile = CommonDataKeys.PSI_FILE.getData(dataContext) ?: return
        val startOffset = editor.selectionModel.selectionStart

        psiFile.findElementAt(startOffset)?.let {
            psiElement ->

            var parent = psiElement.parent
            while (parent != null) {
                if (parent.parent is PsiStatement || parent.parent is PsiLocalVariable || parent.parent is PsiExpressionList) {
                    break
                } else {
                    parent = parent.parent
                }
            }

            parent?.let {
                getDecoder(it)?.decode()
            }
        }
    }

    override fun isEnabled(session: XDebugSession, dataContext: DataContext?): Boolean {
        return session.debugProcess.evaluator != null
    }

    private fun getDecoder(element: PsiElement): ExpressionDecoder? {
        return when (element) {
            is PsiBinaryExpression -> {
                BinaryExpressionDecoder(element)
            }
            is PsiMethodCallExpression -> {
                MethodCallDecoder(element)
            }
            else -> {
                null
            }
        }
    }
}