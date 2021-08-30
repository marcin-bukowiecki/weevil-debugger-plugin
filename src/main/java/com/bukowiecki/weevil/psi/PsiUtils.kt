/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.psi

import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiImportStatement
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.util.classSetOf
import org.jetbrains.uast.util.isInstanceOf

/**
 * @author Marcin Bukowiecki
 */
@Suppress("UNCHECKED_CAST")
object PsiUtils {

    fun isWildcardImport(imp: PsiImportStatement): Boolean {
        return imp.text.endsWith(".*;")
    }

    fun getUniqueName(place: PsiMethod, name: String): String {
        return "$$$${name}$$$"
    }

    fun <T : PsiElement> findParent(element: PsiElement?, type: Class<T>): T? {
        if (element == null) return null

        return if (element.isInstanceOf(classSetOf(type))) {
            element as T
        } else {
            ReadAction.compute<T?, Throwable> { findParent(element.parent, type) }
        }
    }

    fun <T : PsiElement> findParent(element: PsiElement?, type: Class<T>, until: PsiElement): T? {
        if (element == null || element == until) return null

        return if (element.isInstanceOf(classSetOf(type))) {
            element as T
        } else {
            ReadAction.compute<T?, Throwable> { findParent(element.parent, type, until) }
        }
    }
}