/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.annotator

import com.bukowiecki.weevil.services.WeevilResultService
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.psi.PsiElement

/**
 * @author Marcin Bukowiecki
 */
class WeevilTrueFalseDebuggerAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val weevilResultService = WeevilResultService.getInstance(element.project)
        val color = when {
            weevilResultService.isTrue(element) -> {
                Colors.trueColor
            }
            weevilResultService.isFalse(element) -> {
                Colors.falseColor
            }
            else -> {
                return
            }
        }

        color.let {
            holder
                .newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element)
                .enforcedTextAttributes(
                    TextAttributes(
                        null, it, null,
                        EffectType.SEARCH_MATCH, 0
                    )
                )
                .create()
        }
    }
}
