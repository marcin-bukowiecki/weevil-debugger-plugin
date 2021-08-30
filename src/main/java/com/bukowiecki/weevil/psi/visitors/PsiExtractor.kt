/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.psi.visitors

import com.bukowiecki.weevil.psi.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement

/**
 * @author Marcin Bukowiecki
 */
class PsiExtractor : BaseEventVisitor() {

    var psi: PsiElement? = null

    fun visitCodeEvent(codeEvent: CodeEvent): PsiExtractor {
        ApplicationManager
            .getApplication()
            .runReadAction {
                codeEvent.accept(this)
            }
        return this
    }

    override fun visit(codeEvent: ForEachEvent) {
        this.psi = codeEvent.statement
    }

    override fun visit(codeEvent: SetLocalEvent) {
        this.psi = codeEvent.ref
    }

    override fun visit(codeEvent: ExprCaptor) {
        this.psi = codeEvent.ref
    }

    override fun visit(codeEvent: MethodReturnExprCaptor) {
        this.psi = codeEvent.ref
    }

    override fun visit(codeEvent: BinaryExprEvent) {
        this.psi = codeEvent.ref
    }

    override fun visit(codeEvent: ThrowExprCaptor) {
        this.psi = codeEvent.ref
    }
}