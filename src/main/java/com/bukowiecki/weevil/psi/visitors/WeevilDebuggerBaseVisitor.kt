/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.psi.visitors

import com.intellij.psi.JavaElementVisitor
import com.intellij.psi.PsiElement

/**
 * @author Marcin Bukowiecki
 */
open class WeevilDebuggerBaseVisitor(val expressionsToCompile: List<PsiElement>) : JavaElementVisitor() {

    open fun visit() {
        expressionsToCompile.forEach { it.accept(this) }
    }

    override fun visitElement(element: PsiElement) {
        element.children.forEach { it.accept(this) }
    }
}