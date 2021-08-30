/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.psi.visitors

import com.bukowiecki.weevil.debugger.CompilationContext
import com.bukowiecki.weevil.utils.WeevilDebuggerDataKey
import com.intellij.psi.*
import com.siyeh.ig.psiutils.ExpressionUtils

/**
 * @author Marcin Bukowiecki
 */
class CodeEventMarker(private val compilationContext: CompilationContext) : JavaElementVisitor() {

    private val locals = mutableListOf<PsiLocalVariable>()

    override fun visitLambdaExpression(expression: PsiLambdaExpression) {
        expression.body?.accept(this)
    }

    override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
        compilationContext.eventRegister.add(compilationContext.codeEventFactory.createMethodReturnExprCaptor(expression))
        expression.argumentList.accept(this)
    }

    override fun visitExpressionList(list: PsiExpressionList) {
        for (expression in list.expressions) {
            expression.accept(this)
        }
    }

    override fun visitReferenceExpression(expression: PsiReferenceExpression) {
        if (expression.resolve() is PsiClass) return

        compilationContext.eventRegister.add(compilationContext.codeEventFactory.createExprCaptor(expression))
        expression.children.forEach { it.accept(this) }
    }

    override fun visitBinaryExpression(expression: PsiBinaryExpression) {
        compilationContext.codeEventFactory.createBinaryExpr(expression).let { evt -> compilationContext.eventRegister.add(evt) }
        expression.lOperand.accept(this)
        expression.rOperand?.accept(this)
    }

    override fun visitForeachStatement(statement: PsiForeachStatement) {
        compilationContext.codeEventFactory.createForEach(
            "block${compilationContext.blockCounter}",
            statement
        ).let { compilationContext.eventRegister.add(it) }
        statement.iteratedValue?.accept(this)
        statement.body?.accept(this)
    }

    override fun visitThrowStatement(statement: PsiThrowStatement) {
        if (statement.exception == null) return

        compilationContext.codeEventFactory.createThrow(
            statement
        ).let { compilationContext.eventRegister.add(it) }
        statement.exception?.accept(this)
    }

    override fun visitBlockStatement(statement: PsiBlockStatement) {
        statement.codeBlock.accept(this)
    }

    override fun visitIfStatement(statement: PsiIfStatement) {
        val condition = statement.condition ?: return
        compilationContext.eventRegister.add(compilationContext.codeEventFactory.createIfCondition(statement))
        condition.accept(this)
        statement.thenBranch?.accept(this)
        statement.elseBranch?.accept(this)
    }

    override fun visitParenthesizedExpression(expression: PsiParenthesizedExpression) {
        expression.expression?.accept(this)
    }

    override fun visitCodeBlock(block: PsiCodeBlock) {
        compilationContext.codeEventFactory
            .createBlockVisit("block${compilationContext.blockCounter++}", block)
            .let { compilationContext.eventRegister.add(it) }
        block.children.forEach { it.accept(this) }
    }

    override fun visitDeclarationStatement(statement: PsiDeclarationStatement) {
        statement.let {
            for (declaredElement in it.declaredElements) {
                if (declaredElement is PsiLocalVariable) {
                    locals.add(declaredElement)
                    compilationContext.eventRegister.add(compilationContext.codeEventFactory.createSetLocal(declaredElement))
                    declaredElement.initializer?.accept(this)
                }
            }
        }
    }

    override fun visitAssignmentExpression(expression: PsiAssignmentExpression) {
        expression.rExpression?.accept(this)
        expression.lExpression.let {
            locals.forEach { local ->
                if (ExpressionUtils.isReferenceTo(it, local)) {
                    compilationContext.eventRegister.add(compilationContext.codeEventFactory.createSetLocal(local))
                    expression.putCopyableUserData(WeevilDebuggerDataKey.weevilLocalRefDataKey, local)
                }
            }
        }
    }

    override fun visitLiteralExpression(expression: PsiLiteralExpression) {
        //compilationContext.eventRegister.add(compilationContext.codeEventFactory.createExprCaptor(expression))
    }

    override fun visitExpressionStatement(statement: PsiExpressionStatement) {
        statement.expression.accept(this)
    }

    override fun visitReturnStatement(statement: PsiReturnStatement) {
        statement.returnValue?.accept(this)
        compilationContext.eventRegister.add(compilationContext.codeEventFactory.createReturn(statement))
    }

    override fun visitTryStatement(statement: PsiTryStatement) {
        statement.children.forEach { it.accept(this) }
    }

    fun mark(statements: List<PsiElement>) {
        statements.forEach { it.accept(this) }
    }
}