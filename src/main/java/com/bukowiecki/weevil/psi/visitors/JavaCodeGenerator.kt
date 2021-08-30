/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.psi.visitors

import com.bukowiecki.weevil.debugger.CompilationContext
import com.bukowiecki.weevil.psi.*
import com.bukowiecki.weevil.utils.WeevilDebuggerDataKey
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.JavaPsiFacadeImpl
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl
import java.util.*

/**
 * @author Marcin Bukowiecki
 */
class JavaCodeGenerator(expressionsToCompile: List<PsiElement>,
                        private val project: Project,
                        compilationContext: CompilationContext) : WeevilDebuggerBaseVisitor(expressionsToCompile) {

    private val applyFunction = compilationContext.applyFunctionName
    private val returnExceptionName = compilationContext.returnExceptionName
    private val currentBlockName = LinkedList<String>()
    private var boolCounter = 1

    val result = mutableListOf<PsiElement>()

    init {
        currentBlockName.addLast("entry")
    }

    override fun visit() {
        expressionsToCompile.forEach { result.add(it) }
        expressionsToCompile.forEach {
            it.accept(this)
        }
    }

    override fun visitMethod(method: PsiMethod) {
        method.body?.children?.forEach {
            it.accept(this)
        }
    }

    override fun visitLambdaExpression(expression: PsiLambdaExpression) {
        expression.children.forEach { it.accept(this) }
    }

    override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
        expression.argumentList.accept(this)
        expression.getEvent<ExprCaptor>()?.let { event ->
            if (event.type != null && event.type != PsiPrimitiveType.VOID) {
                val forceCast = forceCast(expression.parent)
                createExprEvent(expression, forceCast)
            }
        }
    }

    override fun visitExpressionStatement(statement: PsiExpressionStatement) {
        statement.expression.accept(this)
    }

    override fun visitReferenceExpression(expression: PsiReferenceExpression) {
        if (expression.resolve() is PsiClass) return
        expression.children.forEach { it.accept(this) }
        val forceCast = forceCast(expression.parent)
        createExprEvent(expression, forceCast)
    }

    private fun forceCast(parent: PsiElement): Boolean {
        return parent is PsiExpressionList ||
                parent is PsiBinaryExpression ||
                parent is PsiLocalVariable ||
                parent is PsiIfStatement ||
                parent is PsiForeachStatement ||
                parent is PsiReferenceExpression
    }

    override fun visitBinaryExpression(expression: PsiBinaryExpression) {
        expression.lOperand.accept(this)
        expression.rOperand?.accept(this)

        expression.getEvent<BinaryExprEvent>()?.let { event ->
            val id = event.id
            val expectedType = event.type?.canonicalText ?: ""
            val applyF = JavaPsiFacadeImpl
                .getElementFactory(project)
                .createExpressionFromText(createRecord(id, expression.text, currentBlockName.last, stmt = false, expectedType = expectedType), null)
            expression.replace(applyF)
        }
    }

    override fun visitExpressionList(list: PsiExpressionList) {
        for (expression in list.expressions) {
            expression.accept(this)
        }
    }

    override fun visitLiteralExpression(expression: PsiLiteralExpression) {
        if (expression is PsiLiteralExpressionImpl && expression.literalElementType == JavaTokenType.STRING_LITERAL) {
            return
        }
        createExprEvent(expression)
    }

    private fun createExprEvent(expression: PsiElement, forceCast: Boolean = true) {
        expression.getEvent<ExprCaptor>()?.let { event ->
            val id = event.id
            val expectedType = if (forceCast) event.type?.canonicalText ?: "" else ""

            val applyF = JavaPsiFacadeImpl
                .getElementFactory(project)
                .createExpressionFromText(createRecord(id, expression.text, currentBlockName.last, stmt = false, expectedType = expectedType), null)
            expression.replace(applyF)
        }
    }

    override fun visitForeachStatement(statement: PsiForeachStatement) {
        val event = statement.getEvent<ForEachEvent>()

        if (event != null) {
            val id = event.id
            val name = event.name
            val blockName = event.blockName
            currentBlockName.addLast(blockName)

            val elementFactory = JavaPsiFacadeImpl.getElementFactory(project)
            val applyF = elementFactory.createStatementFromText(createRecord(id, name, blockName), null)

            statement.iteratedValue?.accept(this)
            statement.body?.accept(this)

            (statement.body as? PsiBlockStatement)?.let {
                it.codeBlock.addAfter(applyF, it.codeBlock.firstChild)
            }
            currentBlockName.removeLast()
        } else {
            statement.iteratedValue?.accept(this)
            statement.body?.accept(this)
        }
    }

    override fun visitCodeBlock(block: PsiCodeBlock) {
        val event = block.getEvent<BlockVisitEvent>()
        if (event != null) {
            val id = event.id
            val blockName = event.blockName
            currentBlockName.addLast(blockName)
            block.children.forEach { it.accept(this) }
            val applyF = JavaPsiFacadeImpl
                .getElementFactory(project)
                .createStatementFromText(createRecord(id, "\"$blockName\"", blockName, stmt = true), null)
            block.addAfter(applyF, block.firstChild)
            currentBlockName.removeLast()
        } else {
            block.children.forEach { it.accept(this) }
        }
    }

    override fun visitBlockStatement(statement: PsiBlockStatement) {
        statement.codeBlock.accept(this)
    }

    override fun visitIfStatement(statement: PsiIfStatement) {
        val event = statement.getEvent<IfConditionEvent>()

        if (event != null) {
            val id = event.id
            val booleanName = event.booleanName + boolCounter++
            val blockName = currentBlockName.last

            statement.condition?.accept(this)

            currentBlockName.addLast("if")
            statement.thenBranch?.accept(this)
            statement.elseBranch?.accept(this)
            currentBlockName.removeLast()

            var applyF = JavaPsiFacadeImpl
                    .getElementFactory(project)
                    .createStatementFromText("boolean $booleanName = ${statement.condition?.text};", null)
            statement.parent.addBefore(applyF, statement)

            applyF = JavaPsiFacadeImpl
                .getElementFactory(project)
                .createStatementFromText(createRecord(id, booleanName, blockName), null)
            statement.parent.addBefore(applyF, statement)

            statement.condition?.replace(JavaPsiFacadeImpl
                .getElementFactory(project)
                .createExpressionFromText(booleanName, null))
        } else {
            statement.condition?.accept(this)
            statement.thenBranch?.accept(this)
            statement.elseBranch?.accept(this)
        }
    }

    override fun visitParenthesizedExpression(expression: PsiParenthesizedExpression) {
        expression.expression?.accept(this)
    }

    override fun visitDeclarationStatement(statement: PsiDeclarationStatement) {
        val declaredElements = statement.declaredElements
        if (declaredElements.size == 1 && declaredElements[0] is PsiLocalVariable) {
            val declaredElement = declaredElements[0] as PsiLocalVariable
            declaredElement.initializer!!.accept(this)
            declaredElement.getEvent<SetLocalEvent>()?.let { event ->
                val id = event.id
                val name = event.name
                val blockName = currentBlockName.last
                val type = declaredElement.type
                val prefix = when {
                    declaredElement.typeElement.isInferredType -> {
                        "var $name = "
                    }
                    else -> {
                        "${type.canonicalText} $name = "
                    }
                }

                val applyF = JavaPsiFacadeImpl
                    .getElementFactory(project)
                    .createStatementFromText(prefix + createRecord(id, declaredElement.initializer!!.text, blockName, expectedType = type.canonicalText), null)
                declaredElement.replace(applyF)
            }
        } else {
            for (declaredElement in declaredElements) {
                if (declaredElement is PsiLocalVariable && declaredElement.hasInitializer()) {
                    declaredElement.initializer?.accept(this)
                    declaredElement.getEvent<SetLocalEvent>()?.let { event ->
                        val id = event.id
                        val name = event.name
                        val blockName = currentBlockName.last

                        val applyF = JavaPsiFacadeImpl
                            .getElementFactory(project)
                            .createStatementFromText(createRecord(id, name, blockName), null)
                        statement.add(applyF)
                    }
                } else {
                    declaredElement.children.forEach { it.accept(this) }
                }
            }
        }
    }

    override fun visitAssignmentExpression(expression: PsiAssignmentExpression) {
        expression.rExpression?.accept(this)
        expression.lExpression.accept(this)
        expression.getCopyableUserData(WeevilDebuggerDataKey.weevilLocalRefDataKey)?.let {
            it.getEvent<SetLocalEvent>()?.let { event ->
                val id = event.id
                val name = event.name
                val blockName = currentBlockName.last
                val type = event.type.canonicalText

                val exprText = "$name = ${createRecord(id, expression.rExpression?.text ?: return, blockName, stmt = false, expectedType = type)}"
                val applyF = JavaPsiFacadeImpl
                    .getElementFactory(project)
                    .createExpressionFromText(exprText,null)
                expression.replace(applyF)
            }
        }
    }

    override fun visitThrowStatement(statement: PsiThrowStatement) {
        val exception = statement.exception ?: return

        exception.accept(this)
        statement.getEvent<ThrowExprCaptor>()?.let { event ->
            val id = event.id
            val expectedType = event.type?.canonicalText ?: "Throwable"

            val applyF = JavaPsiFacadeImpl
                .getElementFactory(project)
                .createExpressionFromText(createRecord(id, exception.text, currentBlockName.last, stmt = false, expectedType = expectedType), null)
            exception.replace(applyF)
        }
    }

    override fun visitTryStatement(statement: PsiTryStatement) {
        super.visitTryStatement(statement)
        statement.catchBlockParameters.forEach { param ->
            (param.declarationScope as? PsiCatchSection)?.let { catchSection ->
                val catchBlock = catchSection.catchBlock ?: return@let
                val rethrowStmt = JavaPsiFacadeImpl
                    .getElementFactory(project)
                    .createStatementFromText("if (${param.name} == $returnExceptionName) throw ${param.name};", null)

                if (catchBlock.isEmpty) {
                    catchBlock.add(rethrowStmt)
                } else {
                    catchBlock.addBefore(rethrowStmt, catchBlock.firstBodyElement)
                }
            }
        }
    }

    override fun visitReturnStatement(statement: PsiReturnStatement) {
        statement.returnValue?.let {
            it.accept(this)
            statement.getEvent<ReturnEvent>()?.let { event ->
                val id = event.id
                val blockName = currentBlockName.last

                var applyF: PsiElement = JavaPsiFacadeImpl
                    .getElementFactory(project)
                    .createStatementFromText(createRecord(id, statement.returnValue!!.text, blockName), null)
                applyF = statement.replace(applyF)

                val ending = JavaPsiFacadeImpl
                    .getElementFactory(project)
                    .createStatementFromText("throw $returnExceptionName;", null)
                applyF.parent.addAfter(ending, applyF)
            }
        }
    }

    private fun createRecord(eventId: Int,
                             variableName: String,
                             blockName: String,
                             iteration: String = "0",
                             stmt: Boolean = true,
                             expectedType: String = ""): String {

        assert(eventId != -1)

        var dontCast = false
        var expr = "$applyFunction.apply(Arrays.asList($eventId, $variableName, \"$blockName\", $iteration, Thread.currentThread().getId()))"

        checkType(expectedType, expr).let {
            if (it != null) {
                expr = it
                dontCast = true
            }
        }

        val toCast = if (dontCast || expectedType.isEmpty() || variableName == "null") {
            ""
        } else {
            "($expectedType)"
        }

        return if (stmt) {
            "$toCast$expr;\n"
        } else {
            "$toCast$expr"
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : CodeEvent> PsiElement.getEvent(): T? {
    val data = this.getCopyableUserData(WeevilDebuggerDataKey.weevilDataKey)
    return (data as T?)
}

fun checkType(expectedType: String, expr: String): String? {
    return when (expectedType) {
        "long" -> {
            "java.lang.Long.parseLong(${expr}.toString())"
        }
        "int" -> {
            "java.lang.Integer.parseInt(${expr}.toString())"
        }
        "char" -> {
            "${expr}.toString().charAt(0)"
        }
        "byte" -> {
            "java.lang.Byte.parseByte(${expr}.toString())"
        }
        "short" -> {
            "java.lang.Short.parseShort(${expr}.toString())"
        }
        "float" -> {
            "java.lang.Float.parseFloat(${expr}.toString())"
        }
        "double" -> {
            "java.lang.Double.parseDouble(${expr}.toString())"
        }
        else -> {
            null
        }
    }
}