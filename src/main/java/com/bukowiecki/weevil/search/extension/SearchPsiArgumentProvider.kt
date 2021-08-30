/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.search.extension

import com.bukowiecki.weevil.utils.WeevilDebuggerDataKey
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.augment.PsiAugmentProvider
import com.intellij.psi.impl.JavaPsiFacadeImpl

/**
 * @author Marcin Bukowiecki
 */
class SearchPsiArgumentProvider : PsiAugmentProvider() {

    @Suppress("UNCHECKED_CAST")
    override fun <Psi : PsiElement?> getAugments(
        element: PsiElement,
        type: Class<Psi>,
        nameHint: String?
    ): MutableList<Psi> {
        if (nameHint == "\$it") {
            element.containingFile.getUserData(WeevilDebuggerDataKey.weevilCodeFragmentInputComponent)?.let { codeFragment ->
                if (!codeFragment.isEditorDisposed() && type == PsiField::class.java) {
                    val stmt = JavaPsiFacadeImpl
                        .getElementFactory(element.project)
                        .createFieldFromText("static Object \$it;", null)
                    return listOf(stmt as Psi).toMutableList()
                }
            }
        }
        return super.getAugments(element, type, nameHint)
    }
}