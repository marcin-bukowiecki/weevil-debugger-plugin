/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.debugger

import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.impl.source.codeStyle.CodeStyleManagerImpl
import com.bukowiecki.weevil.WeevilDebuggerTestCase
import org.junit.Assert
import org.junit.Test

/**
 * @author Marcin Bukowiecki
 */
class CodeGeneratorTest : WeevilDebuggerTestCase() {

    @Test
    fun helloWorld_test() {
        val file = getJavaSourceFile("codeGenerator/helloWorld1.java")
        val given = getPsiMethod(file, "given")
        val want = getPsiMethod(file, "want")
        val generatedCode = CodeGenerator.generateCode(
            project, getBody(given), CompilationContext(
                1,
                applyFunctionName = "applyFunction",
                threadCollectorName = "threadCollector",
                returnExceptionName = "returnException"
            )
        )
        val wrapped = JavaPsiFacade.getElementFactory(project).createCodeBlockFromText("{${want.body!!.text}}", null)
        val wantFormatted = CodeStyleManagerImpl.getInstance(project).reformat(wrapped)
        Assert.assertEquals(wantFormatted.text, generatedCode)
    }
}