/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.psi

import com.bukowiecki.weevil.utils.WeevilDebuggerDataKey
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.psi.*
import com.intellij.refactoring.suggested.startOffset
import com.sun.jdi.ObjectReference
import org.apache.commons.lang3.mutable.MutableInt
import java.awt.Color
import java.awt.Font

/**
 * @author Marcin Bukowiecki
 */
class CodeEventFactory {

    private val eventCounter = MutableInt()

    private var booleanExprCounter = 0

    fun createReturn(stmt: PsiReturnStatement): ReturnEvent {
        val event = ReturnEvent(
            eventCounter.andIncrement,
            stmt
        )
        stmt.putCopyableUserData(WeevilDebuggerDataKey.weevilDataKey, event)
        return event
    }

    fun createSetLocal(local: PsiLocalVariable): SetLocalEvent {
        val event = SetLocalEvent(
            eventCounter.andIncrement,
            local.name,
            local
        )
        local.putCopyableUserData(WeevilDebuggerDataKey.weevilDataKey, event)
        return event
    }

    fun createIfCondition(ifStmt: PsiIfStatement): IfConditionEvent {
        val event = IfConditionEvent(
            eventCounter.andIncrement,
            "$$\$bool$$$",
            ifStmt
        )
        ifStmt.putCopyableUserData(WeevilDebuggerDataKey.weevilDataKey, event)
        return event
    }

    @Suppress("unused")
    fun createBlockInit(blockName: String, stmt: PsiForeachStatement): BlockInitEvent {
        val event = BlockInitEvent(
            eventCounter.andIncrement,
            blockName,
            stmt
        )
        stmt.putCopyableUserData(WeevilDebuggerDataKey.weevilDataKey, event)
        return event
    }

    fun createBlockVisit(blockName: String, ref: PsiElement): BlockVisitEvent {
        val event = BlockVisitEvent(
            eventCounter.andIncrement,
            blockName,
            ref
        )
        ref.putCopyableUserData(WeevilDebuggerDataKey.weevilDataKey, event)
        return event
    }

    fun createForEach(blockName: String, statement: PsiForeachStatement): ForEachEvent {
        val event = ForEachEvent(eventCounter.andIncrement, blockName, statement.iterationParameter.name, statement)
        statement.putCopyableUserData(WeevilDebuggerDataKey.weevilDataKey, event)
        return event
    }

    fun createThrow(statement: PsiThrowStatement): ThrowExprCaptor {
        val event = ThrowExprCaptor(eventCounter.andIncrement, statement)
        statement.putCopyableUserData(WeevilDebuggerDataKey.weevilDataKey, event)
        return event
    }

    fun createBinaryExpr(expr: PsiExpression): BinaryExprEvent {
        return BinaryExprEvent(
            eventCounter.andIncrement,
            "binaryExpr$booleanExprCounter", expr
        ).let {
            expr.putCopyableUserData(WeevilDebuggerDataKey.weevilDataKey, it)
            it
        }
    }

    fun createExprCaptor(expr: PsiExpression): ExprCaptor {
        val event = ExprCaptor(
            eventCounter.andIncrement,
            expr
        )
        expr.putCopyableUserData(WeevilDebuggerDataKey.weevilDataKey, event)
        return event
    }

    fun createMethodReturnExprCaptor(expr: PsiExpression): ExprCaptor {
        val event = MethodReturnExprCaptor(
            eventCounter.andIncrement,
            expr
        )
        expr.putCopyableUserData(WeevilDebuggerDataKey.weevilDataKey, event)
        return event
    }
}

fun getLineNumber(element: PsiElement): Int {
    return PsiDocumentManager
        .getInstance(element.project)
        .getDocument(element.containingFile)?.getLineNumber(element.startOffset) ?: -1
}

/**
 * @author Marcin Bukowiecki
 */
interface CodeEvent {
    val id: Int
    val line: Int
    val type: PsiType?
    val containingFile: PsiFile?
    val valueName: String

    fun accept(visitor: BaseEventVisitor) {
        visitor.visit(this)
    }

    fun isError(): Boolean = false
}

/**
 * @author Marcin Bukowiecki
 */
interface WithLocalVariable : CodeEvent {
    val name: String

    fun getPsiVariable(): PsiVariable?
}

/**
 * @author Marcin Bukowiecki
 */
abstract class PsiCodeEvent(override val id: Int, val element: PsiElement) : CodeEvent {

    val text: String = ApplicationManager.getApplication().runReadAction<String, Throwable> { element.text }

    override var line: Int = getLineNumber(element)

    override var containingFile: PsiFile? = element.containingFile

    override fun toString(): String {
        return text
    }
}

/**
 * @author Marcin Bukowiecki
 */
class ForEachEvent(
    override val id: Int,
    val blockName: String,
    override val name: String,
    val statement: PsiForeachStatement
) : PsiCodeEvent(id, statement), WithLocalVariable {

    override var type: PsiType? = statement.iterationParameter.type

    override var valueName: String = name

    override fun accept(visitor: BaseEventVisitor) {
        visitor.visit(this)
    }

    override fun getPsiVariable(): PsiVariable {
        return statement.iterationParameter
    }
}

/**
 * @author Marcin Bukowiecki
 */
class SetLocalEvent(
    override val id: Int,
    override val name: String,
    val ref: PsiLocalVariable
) : PsiCodeEvent(id, ref), WithLocalVariable {

    override var type: PsiType = ref.type

    override val valueName: String
        get() = name

    override fun accept(visitor: BaseEventVisitor) {
        visitor.visit(this)
    }

    override fun getPsiVariable(): PsiVariable {
        return ref
    }
}

/**
 * @author Marcin Bukowiecki
 */
class IfConditionEvent(
    override val id: Int,
    val booleanName: String,
    val stmt: PsiIfStatement
) : PsiCodeEvent(id, stmt) {

    override var type: PsiType? = stmt.condition?.type

    override val valueName: String
        get() = text
}

/**
 * @author Marcin Bukowiecki
 */
class BlockInitEvent(
    override val id: Int,
    private val blockName: String,
    startElement: PsiElement
) : PsiCodeEvent(id, startElement) {

    override var type: PsiType? = null

    override val valueName: String
        get() = blockName

    override fun toString(): String {
        return "$blockName.init"
    }
}

/**
 * @author Marcin Bukowiecki
 */
class BlockVisitEvent(
    override val id: Int,
    val blockName: String,
    ref: PsiElement
) : PsiCodeEvent(id, ref) {

    override var type: PsiType? = null

    override val valueName: String
        get() = blockName

    override fun toString(): String {
        return "$blockName.visit"
    }
}

/**
 * @author Marcin Bukowiecki
 */
class BinaryExprEvent(
    override val id: Int,
    val name: String,
    ref: PsiExpression
) : ExprCaptor(id, ref) {

    override var type: PsiType? = ref.type

    override val valueName: String
        get() = text

    override fun accept(visitor: BaseEventVisitor) {
        visitor.visit(this)
    }
}

/**
 * @author Marcin Bukowiecki
 */
open class ExprCaptor(override val id: Int, val ref: PsiExpression) : PsiCodeEvent(id, ref) {

    override var type: PsiType? = ApplicationManager.getApplication().runReadAction<PsiType?, Throwable> { ref.type }

    override val valueName: String
        get() = text

    override fun accept(visitor: BaseEventVisitor) {
        visitor.visit(this)
    }
}

/**
 * @author Marcin Bukowiecki
 */
class MethodReturnExprCaptor(id: Int, ref: PsiExpression) : ExprCaptor(id, ref) {

    override fun accept(visitor: BaseEventVisitor) {
        visitor.visit(this)
    }
}

/**
 * @author Marcin Bukowiecki
 */
class ReturnEvent(override val id: Int, val ref: PsiReturnStatement) : PsiCodeEvent(id, ref) {

    override var type: PsiType? = ref.returnValue?.type

    @Suppress("unused")
    val textAttributes: TextAttributes = TextAttributes(
        Color.gray,
        Color.CYAN,
        Color.gray,
        EffectType.ROUNDED_BOX,
        Font.ITALIC
    )

    override val valueName: String
        get() = text
}

/**
 * @author Marcin Bukowiecki
 */
class ThrowExprCaptor(id: Int, throwStmt: PsiThrowStatement) : ExprCaptor(id, throwStmt.exception!!) {

    override fun accept(visitor: BaseEventVisitor) {
        visitor.visit(this)
    }
}

/**
 * @author Marcin Bukowiecki
 */
class ExceptionEventHolder(override var line: Int,
                           override val valueName: String,
                           override var containingFile: PsiFile,
                           val exceptionRef: ObjectReference) : CodeEvent {

    override val id: Int = Int.MAX_VALUE

    override var type: PsiType? = null

    override fun accept(visitor: BaseEventVisitor) {
        visitor.visit(this)
    }

    override fun isError(): Boolean {
        return true
    }
}

/**
 * @author Marcin Bukowiecki
 */
abstract class BaseEventVisitor {

    open fun visit(codeEvent: CodeEvent) {

    }

    open fun visit(codeEvent: ExceptionEventHolder) {
        visit(codeEvent as CodeEvent)
    }

    open fun visit(codeEvent: SetLocalEvent) {
        visit(codeEvent as CodeEvent)
    }

    open fun visit(codeEvent: ForEachEvent) {
        visit(codeEvent as CodeEvent)
    }

    open fun visit(codeEvent: BinaryExprEvent) {
        visit(codeEvent as CodeEvent)
    }

    open fun visit(codeEvent: MethodReturnExprCaptor) {
        visit(codeEvent as CodeEvent)
    }

    open fun visit(codeEvent: ExprCaptor) {
        visit(codeEvent as CodeEvent)
    }

    open fun visit(codeEvent: ThrowExprCaptor) {
        visit(codeEvent as CodeEvent)
    }
}