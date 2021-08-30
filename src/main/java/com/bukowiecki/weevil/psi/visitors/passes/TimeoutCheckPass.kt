/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.psi.visitors.passes

import com.bukowiecki.weevil.debugger.CompilationContext
import com.bukowiecki.weevil.psi.visitors.WeevilDebuggerBaseVisitor
import com.intellij.psi.*
import com.intellij.psi.impl.JavaPsiFacadeImpl
import com.intellij.psi.util.elementType

/**
 * Adds timeout check at the start of each loop body
 *
 * @author Marcin Bukowiecki
 */
class TimeoutCheckPass(
    expressionsToCompile: List<PsiElement>,
    private val compilationContext: CompilationContext,
) : WeevilDebuggerBaseVisitor(expressionsToCompile) {

    override fun visitForStatement(statement: PsiForStatement) {
        this.visitElement(statement)
        addCheck(statement.body ?: return)
    }

    override fun visitForeachStatement(statement: PsiForeachStatement) {
        this.visitElement(statement)
        addCheck(statement.body ?: return)
    }

    override fun visitWhileStatement(statement: PsiWhileStatement) {
        this.visitElement(statement)
        addCheck(statement.body ?: return)
    }

    override fun visitDoWhileStatement(statement: PsiDoWhileStatement) {
        this.visitElement(statement)
        addCheck(statement.body ?: return)
    }

    private fun addCheck(statement: PsiStatement) {
        (statement as? PsiBlockStatement)?.let { blockStmt ->
            val firstChild = blockStmt.codeBlock.firstChild
            if (firstChild.elementType == JavaTokenType.LBRACE) {
                val factory = JavaPsiFacadeImpl.getElementFactory(statement.project)
                val stmt = factory.createStatementFromText(
                    "if (System.nanoTime() > ${compilationContext.timeoutAt}L) throw new RuntimeException(\"timeout\");",
                    null
                )
                firstChild.parent.addAfter(stmt, firstChild)
            }
        }
    }
}