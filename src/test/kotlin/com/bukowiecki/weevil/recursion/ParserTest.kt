/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.recursion

import com.bukowiecki.weevil.WeevilDebuggerTestCase
import com.bukowiecki.weevil.recursion.language.RecursionLanguage
import com.bukowiecki.weevil.recursion.language.psi.RecursionTypes
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.impl.PsiFileFactoryImpl
import org.junit.Assert
import org.junit.Test

/**
 * @author Marcin Bukowiecki
 */
class ParserTest : WeevilDebuggerTestCase() {

    @Test
    fun testSingleExpr() {
        val psiFileFactory = PsiFileFactoryImpl.getInstance(project) as PsiFileFactoryImpl
        val recursionExpression = psiFileFactory
            .createElementFromText("123", RecursionLanguage.INSTANCE, RecursionTypes.EXPRESSION, null)
        Assert.assertEquals("123", recursionExpression!!.text)
        Assert.assertTrue(recursionExpression.children.all { it !is PsiErrorElement })
    }

    @Test
    fun testSingleRangeExpr() {
        val psiFileFactory = PsiFileFactoryImpl.getInstance(project) as PsiFileFactoryImpl
        val recursionExpression = psiFileFactory
            .createElementFromText("1-2", RecursionLanguage.INSTANCE, RecursionTypes.EXPRESSION, null)
        Assert.assertEquals("1-2", recursionExpression!!.text)
        Assert.assertTrue(recursionExpression.children.all { it !is PsiErrorElement })
        Assert.assertEquals(recursionExpression.children[0].javaClass, com.bukowiecki.weevil.recursion.language.psi.impl.RecursionSingleExprImpl::class.java)
        Assert.assertEquals(recursionExpression.children[0].children[0].javaClass, com.bukowiecki.weevil.recursion.language.psi.impl.RecursionRangeExprImpl::class.java)
    }

    @Test
    fun testMultiRangeExpr() {
        val psiFileFactory = PsiFileFactoryImpl.getInstance(project) as PsiFileFactoryImpl
        val recursionExpression = psiFileFactory
            .createElementFromText("1-2,7-9", RecursionLanguage.INSTANCE, RecursionTypes.EXPRESSION, null)
        Assert.assertEquals("1-2,7-9", recursionExpression!!.text)
        Assert.assertTrue(recursionExpression.children.all { it !is PsiErrorElement })
    }

    @Test
    fun testMixedExpr() {
        val psiFileFactory = PsiFileFactoryImpl.getInstance(project) as PsiFileFactoryImpl
        val recursionExpression = psiFileFactory
            .createElementFromText("1-4,7+", RecursionLanguage.INSTANCE, RecursionTypes.EXPRESSION, null)
        Assert.assertEquals("1-4,7+", recursionExpression!!.text)
        Assert.assertTrue(recursionExpression.children.all { it !is PsiErrorElement })
    }

    @Test
    fun testSingleExpressions() {
        val psiFileFactory = PsiFileFactoryImpl.getInstance(project) as PsiFileFactoryImpl
        val recursionExpression = psiFileFactory
            .createElementFromText("4,6,9", RecursionLanguage.INSTANCE, RecursionTypes.EXPRESSION, null)
        Assert.assertEquals("4,6,9", recursionExpression!!.text)
        Assert.assertTrue(recursionExpression.children.all { it !is PsiErrorElement })
    }

    @Test
    fun testError() {
        val psiFileFactory = PsiFileFactoryImpl.getInstance(project) as PsiFileFactoryImpl
        val recursionExpression = psiFileFactory
            .createElementFromText("1-4,foo", RecursionLanguage.INSTANCE, RecursionTypes.EXPRESSION, null)
        Assert.assertEquals("1-4,foo", recursionExpression!!.text)
        Assert.assertTrue(recursionExpression.children.any { it is PsiErrorElement })
    }
}