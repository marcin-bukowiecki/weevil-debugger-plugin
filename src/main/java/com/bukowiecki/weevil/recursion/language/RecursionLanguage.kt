package com.bukowiecki.weevil.recursion.language

import com.intellij.lang.Language

/**
 * @author Marcin Bukowiecki
 */
class RecursionLanguage : Language("Weevil-debugger-recursion") {

    companion object {
        val INSTANCE = RecursionLanguage()
    }
}