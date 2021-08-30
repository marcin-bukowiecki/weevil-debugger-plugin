package com.bukowiecki.weevil.decoders

import com.bukowiecki.weevil.utils.WeevilDebuggerDataKey
import com.intellij.psi.PsiMethodCallExpression

/**
 * @author Marcin Bukowiecki
 */
class MethodCallDecoder(private val methodCallExpression: PsiMethodCallExpression) : ExpressionDecoder() {

    override fun decode() {
        for (expression in methodCallExpression.argumentList.expressions) {
            expression.getUserData(WeevilDebuggerDataKey.weevilResultDataKey)?.let {
                holder ->
                holder.ref.get()?.let {

                }
            }
        }
    }
}