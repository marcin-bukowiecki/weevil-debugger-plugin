/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */


package com.bukowiecki.weevil.recursion.language

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

/**
 * @author Marcin Bukowiecki
 */
class RecursionFileType private constructor() : LanguageFileType(RecursionLanguage.INSTANCE) {

    override fun getName(): String {
        return "Weevil Debugger Recursion File"
    }

    override fun getDescription(): String {
        return "Weevil Debugger Recursion language file"
    }

    override fun getDefaultExtension(): String {
        return "sdrec"
    }

    override fun getIcon(): Icon {
        return RecursionIcons.FILE
    }

    companion object {
        val INSTANCE = RecursionFileType()
    }
}