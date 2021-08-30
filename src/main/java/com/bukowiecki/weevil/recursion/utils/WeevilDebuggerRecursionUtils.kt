/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.recursion.utils

import com.bukowiecki.weevil.recursion.language.RecursionLanguage
import com.bukowiecki.weevil.recursion.language.psi.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.impl.PsiFileFactoryImpl
import org.apache.commons.lang3.Range

/**
 * @author Marcin Bukowiecki
 */
object WeevilDebuggerRecursionUtils {

    private val log = Logger.getInstance(WeevilDebuggerRecursionUtils::class.java)

    fun parseText(project: Project, text: String): PsiElement? {
        val psiFileFactory = PsiFileFactoryImpl.getInstance(project) as PsiFileFactoryImpl
        return ReadAction.compute<PsiElement?, Throwable> { psiFileFactory
            .createElementFromText(text, RecursionLanguage.INSTANCE, RecursionTypes.EXPRESSION, null) }
    }

    fun validateExpression(recursionExpression: PsiElement): Boolean {
        var gotError = false
        val visitor = object : PsiRecursiveElementVisitor() {

            override fun visitErrorElement(element: PsiErrorElement) {
                gotError = true
            }
        }
        recursionExpression.accept(visitor)
        return !gotError
    }

    fun compile(recursionExpression: PsiElement): Set<Range<Int>> {
        val result = mutableSetOf<Range<Int>>()
        val visitor = object : com.bukowiecki.weevil.recursion.language.psi.RecursionVisitor() {

            override fun visitUnderExpr(element: RecursionUnderExpr) {
                val l = parseNumber(element.leftNumber.text)
                if (l != null) {
                    result.add(Range.between(l, Int.MAX_VALUE))
                }
            }

            override fun visitAboveExpr(element: RecursionAboveExpr) {
                val l = parseNumber(element.leftNumber.text)
                if (l != null) {
                    result.add(Range.between(l, Int.MAX_VALUE))
                }
            }

            override fun visitRangeExpr(element: RecursionRangeExpr) {
                val l = parseNumber(element.leftNumber.text)
                val r = parseNumber(element.rightNumber.text)
                if (l != null && r != null) {
                    result.add(Range.between(l, r))
                }
            }

            override fun visitNumberExpr(element: RecursionNumberExpr) {
                parseNumber(element.text)?.let {
                    result.add(Range.between(it, it))
                }
            }

            override fun visitSingleExpr(element: RecursionSingleExpr) {
                element.children.forEach { it.accept(this) }
            }

            override fun visitExpression(element: RecursionExpression) {
                element.children.forEach { it.accept(this) }
            }
        }

        ApplicationManager.getApplication().runReadAction {
            recursionExpression.accept(visitor)
        }

        return result
    }

    private fun parseNumber(text: String): Int? {
        return try {
            text.toInt()
        } catch(ex: NumberFormatException) {
            log.info("Exception while parsing number: $text")
            null
        }
    }
}