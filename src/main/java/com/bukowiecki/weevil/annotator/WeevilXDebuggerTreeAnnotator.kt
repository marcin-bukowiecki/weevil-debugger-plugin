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
import java.awt.Color

val markColor = Color(83, 85, 50)

/**
 * @author Marcin Bukowiecki
 */
class WeevilXDebuggerTreeAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val instance = WeevilResultService.getInstance(element.project)
        instance.xTreeElementToMark?.let {
            if (element == it) {
                holder
                    .newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(element)
                    .enforcedTextAttributes(
                        TextAttributes(
                            null, markColor, null,
                            EffectType.SEARCH_MATCH, 0
                        )
                    )
                    .create()
            }
        }
    }
}