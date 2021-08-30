/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.shell

import com.intellij.psi.JavaElementVisitor
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceExpression

/**
 * @author Marcin Bukowiecki
 */
@Deprecated("future releases")
class ShellCommentChecker(private val expressionToCheck: String) : JavaElementVisitor() {

    override fun visitComment(comment: PsiComment) {
        super.visitComment(comment)
        val text = comment.text
        if (text.startsWith("// Evaluation result: ")) {
            val split = text.split("// Evaluation result: ")
            if (split.size > 1 && split[1].startsWith("$expressionToCheck = ")) {
                comment.delete()
            }
        }
    }

    override fun visitReferenceExpression(expression: PsiReferenceExpression) {
        expression.children.forEach { it.accept(this) }
    }

    override fun visitElement(element: PsiElement) {
        element.children.forEach { it.accept(this) }
    }
}