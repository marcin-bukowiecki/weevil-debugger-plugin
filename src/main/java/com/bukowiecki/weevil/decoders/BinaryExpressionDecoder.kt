package com.bukowiecki.weevil.decoders

import com.bukowiecki.weevil.utils.WeevilDebuggerDataKey
import com.intellij.psi.*
import com.intellij.refactoring.suggested.startOffset

/**
 * @author Marcin Bukowiecki
 */
class BinaryExpressionDecoder(private val element: PsiBinaryExpression) : ExpressionDecoder() {

    override fun decode() {
        val binaryExpressionDecoderVisitor = BinaryExpressionDecoderVisitor()
        element.accept(binaryExpressionDecoderVisitor)
        binaryExpressionDecoderVisitor.visitEnd()
    }
}

class DecoderLine(element: PsiElement, margin: Int, text: String)

class BinaryExpressionDecoderVisitor : JavaElementVisitor() {

    private val lines = mutableListOf<DecoderLine>()

    override fun visitBinaryExpression(expression: PsiBinaryExpression) {
        getMargin(expression.operationSign)?.let { margin ->
            expression.getUserData(WeevilDebuggerDataKey.weevilResultDataKey)?.let { WeevilValue ->
                WeevilValue.ref.get()?.let {
                    lines.add(DecoderLine(expression.operationSign, margin, "Value"))
                }
                lines.add(DecoderLine(expression.operationSign, margin, expression.operationSign.text))
            }
        }
        //WeevilDebuggerInlayUtil.createInlay(expression.lOperand, "a", 100)
        //WeevilDebuggerInlayUtil.createInlay(expression.rOperand ?: return, "b", 200)
    }

    override fun visitExpression(expression: PsiExpression) {
        val document = PsiDocumentManager.getInstance(expression.project).getDocument(expression.containingFile) ?: return
        val lineNumber = document.getLineNumber(expression.startOffset)
        val lineStartOffset = document.getLineStartOffset(lineNumber)
        val margin = expression.startOffset - lineStartOffset

        expression.getUserData(WeevilDebuggerDataKey.weevilResultDataKey)?.let { WeevilValue ->
            if (expression is PsiLiteralExpression) {
                //WeevilDebuggerInlayUtil.createBlockInlay(expression, expression.text, margin)
            } else {
                WeevilValue.ref.get()?.let {
                    //WeevilDebuggerInlayUtil.createBlockInlay(expression, "Foo", margin)
                }
            }
        }
    }

    fun visitEnd() {

    }

    private fun getMargin(element: PsiElement): Int? {
        val document = PsiDocumentManager.getInstance(element.project).getDocument(element.containingFile) ?: return null
        val lineNumber = document.getLineNumber(element.startOffset)
        val lineStartOffset = document.getLineStartOffset(lineNumber)
        return element.startOffset - lineStartOffset
    }
}