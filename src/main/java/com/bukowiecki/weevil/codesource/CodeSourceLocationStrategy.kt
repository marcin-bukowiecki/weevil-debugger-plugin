/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.codesource

import com.intellij.debugger.engine.JavaValue
import com.intellij.psi.PsiClass
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.frame.XValue
import com.jetbrains.jdi.StringReferenceImpl
import com.bukowiecki.weevil.inlay.WeevilDebuggerInlayUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.refactoring.suggested.endOffset

/**
 * @author Marcin Bukowiecki
 */
interface CodeSourceLocationStrategy {

    fun getExpression(debugProcess: XDebugProcess): Pair<PsiClass, String>?

    fun handleError(expr: String, errorMessage: String)

    fun handleValue(psiClass: PsiClass, result: XValue) {
        if (result is JavaValue && result.descriptor.value is StringReferenceImpl) {
            (result.descriptor.value as StringReferenceImpl).valueAsync().thenAccept { filePath ->
                val document = PsiDocumentManager.getInstance(psiClass.project).getDocument(psiClass.containingFile)
                val ln = document?.getLineNumber(psiClass.lBrace?.endOffset ?: return@thenAccept) ?: return@thenAccept
                val offset = document.getLineEndOffset(ln)

                WeevilDebuggerInlayUtil.createBlockInlay(
                    offset,
                    psiClass,
                    "Code loaded from: $filePath",
                    0,
                    CodeSourceBlockInlayType
                )
            }
        }
    }
}