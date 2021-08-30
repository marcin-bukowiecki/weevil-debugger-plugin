package com.bukowiecki.weevil.psi.visitors.passes

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiBlockStatement
import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIfStatement
import com.intellij.psi.impl.JavaPsiFacadeImpl
import com.bukowiecki.weevil.debugger.CompilationContext
import com.bukowiecki.weevil.psi.visitors.WeevilDebuggerBaseVisitor

/**
 * @author Marcin Bukowiecki
 */
class BlockAdderPass(
    expressionsToCompile: List<PsiElement>,
    private val project: Project,
    private val compilationContext: CompilationContext
) : WeevilDebuggerBaseVisitor(expressionsToCompile) {

    override fun visitIfStatement(statement: PsiIfStatement) {
        statement.children.forEach { it.accept(this) }

        if (statement.thenBranch != null && statement.thenBranch !is PsiBlockStatement) {
            createBlock(statement.thenBranch!!) {
                statement.setThenBranch(it)
                (statement.thenBranch as PsiBlockStatement).codeBlock
            }
        }
        if (statement.elseBranch != null && statement.elseBranch !is PsiBlockStatement) {
            createBlock(statement.elseBranch!!) {
                statement.setElseBranch(it)
                (statement.elseBranch as PsiBlockStatement).codeBlock
            }
        }
    }

    private fun createBlock(element: PsiElement, toApply: (PsiBlockStatement) -> PsiCodeBlock) {
        val createdBlockStmt = JavaPsiFacadeImpl
            .getElementFactory(project)
            .createStatementFromText("{}", null) as PsiBlockStatement
        createdBlockStmt.codeBlock.add(element)
        val codeBlock = toApply.invoke(createdBlockStmt)
        compilationContext
            .codeEventFactory
            .createBlockVisit("block${compilationContext.blockCounter++}", codeBlock).let { compilationContext.eventRegister.add(it) }
    }
}