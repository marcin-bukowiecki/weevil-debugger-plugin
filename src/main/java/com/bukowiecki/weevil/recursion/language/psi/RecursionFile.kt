package com.bukowiecki.weevil.recursion.language.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.bukowiecki.weevil.recursion.language.RecursionFileType
import com.bukowiecki.weevil.recursion.language.RecursionLanguage

/**
 * @author Marcin Bukowiecki
 */
class RecursionFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, RecursionLanguage.INSTANCE) {

    override fun getFileType(): FileType {
        return RecursionFileType.INSTANCE
    }

    override fun toString(): String {
        return "Weevil Debugger Recursion File"
    }
}