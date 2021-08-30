/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.services

import com.bukowiecki.weevil.utils.WeevilDebuggerDataKey
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaElementVisitor
import com.intellij.psi.PsiBinaryExpression
import com.intellij.psi.PsiElement

/**
 * @author Marcin Bukowiecki
 */
class WeevilBooleanResultVisitor(private val project: Project) : JavaElementVisitor() {

    override fun visitBinaryExpression(expression: PsiBinaryExpression) {
        val weevilResultService = WeevilResultService.getInstance(project)
        expression.getCopyableUserData(WeevilDebuggerDataKey.weevilResultDataKey)?.let { result ->
            weevilResultService.addBooleanResult(expression.operationSign, result)
        }
    }

    override fun visitElement(element: PsiElement) {
        val weevilResultService = WeevilResultService.getInstance(project)
        element.getCopyableUserData(WeevilDebuggerDataKey.weevilResultDataKey)?.let { result ->
            weevilResultService.addBooleanResult(element, result)
        }
    }
}