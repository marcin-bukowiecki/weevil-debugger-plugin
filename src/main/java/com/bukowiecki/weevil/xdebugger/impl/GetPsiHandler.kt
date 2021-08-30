/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.xdebugger.impl

import com.intellij.psi.PsiFile

/**
 * @author Marcin Bukowiecki
 */
interface GetPsiHandler {

    fun handle(psiFile: PsiFile): PsiFile {
        return psiFile
    }
}