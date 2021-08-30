/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.search

import com.bukowiecki.weevil.search.impl.SearchInputComponent
import com.bukowiecki.weevil.ui.SearchController
import com.intellij.debugger.engine.JavaDebuggerEvaluator

/**
 * @author Marcin Bukowiecki
 */
interface WeevilSearcher {

    fun search(
        searchController: SearchController,
        inputComponent: SearchInputComponent,
        evaluator: JavaDebuggerEvaluator
    )
}