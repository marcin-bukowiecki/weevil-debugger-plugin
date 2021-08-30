/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.search.codefragment

import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.idea.KotlinLanguage

/**
 * @author Marcin Bukowiecki
 */
class KotlinCodeFragmentSearcher(project: Project) : JavaCodeFragmentSearcher(project) {

    override fun getLanguage(): Language {
        return KotlinLanguage.INSTANCE
    }

    override fun createNewCodeFragment(toInject: String): String {
        return """        
        object : java.util.function.Function<Any, Boolean> {
            
            override fun apply(thisObject: Any): Boolean {
                $toInject
            }
        };
        """
    }
}