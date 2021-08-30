/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.source.codeStyle.CodeStyleManagerImpl
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase

/**
 * @author Marcin Bukowiecki
 */
abstract class WeevilDebuggerTestCase : LightPlatformCodeInsightFixture4TestCase() {

    override fun getTestDataPath(): String {
        return "src/test/resources/testData"
    }

    fun getJavaSourceFile(name: String): PsiJavaFile {
        if (!name.endsWith(".java")) throw IllegalStateException("Expected java source file got: $name")
        return myFixture.configureByFiles(name).let {
            if (it.isEmpty()) {
                throw IllegalStateException("Could not find source file: $name")
            } else {
                it[0] as PsiJavaFile
            }
        }
    }

    fun getPsiMethod(file: PsiJavaFile, name: String): PsiMethod {
        val filter = file.classes.map { c -> c.methods }.flatMap { m -> m.toList() }.filter { it.name == name }
        if (filter.size > 1) {
            throw IllegalArgumentException("Duplicated method: $name")
        }
        if (filter.isEmpty()) {
            throw IllegalArgumentException("Method: $name not found")
        }
        return filter[0]
    }

    fun getBody(psiMethod: PsiMethod): List<PsiElement> {
        return psiMethod.body?.children?.toList() ?: emptyList()
    }

    fun codeFormatter(): CodeStyleManager {
        return CodeStyleManagerImpl.getInstance(project)
    }
}