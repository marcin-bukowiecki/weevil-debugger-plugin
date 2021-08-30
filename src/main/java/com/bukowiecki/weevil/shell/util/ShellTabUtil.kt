/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.shell.util

import com.bukowiecki.weevil.shell.ShellCommentChecker
import com.intellij.debugger.impl.DebuggerUtilsEx
import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.xdebugger.XSourcePosition

/**
 * @author Marcin Bukowiecki
 */
object ShellTabUtil {

    fun removeExistingComment(expressionToCheck: String, psiFile: PsiFile) {
        psiFile.accept(ShellCommentChecker(expressionToCheck))
    }

    fun getCurrentLanguage(sourcePosition: XSourcePosition, project: Project): Language? {
        return DebuggerUtilsEx.getPsiFile(sourcePosition, project)?.language
    }
}