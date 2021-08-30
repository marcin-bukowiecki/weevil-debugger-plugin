/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.services

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile

/**
 * @author Marcin Bukowiecki
 */
class WeevilResultKey(val psiFile: PsiFile, val virtualFile: VirtualFile) {

    override fun hashCode(): Int {
        return virtualFile.canonicalFile.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is WeevilResultKey && virtualFile.canonicalPath == other.virtualFile.canonicalPath
    }
}