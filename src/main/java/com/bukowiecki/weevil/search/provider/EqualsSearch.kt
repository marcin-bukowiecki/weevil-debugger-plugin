/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.search.provider

import com.bukowiecki.weevil.search.BaseWeevilSearcher
import com.bukowiecki.weevil.search.SearchContext
import com.bukowiecki.weevil.search.SearchPath
import com.intellij.debugger.engine.evaluation.expression.IdentityEvaluator
import com.intellij.debugger.engine.evaluation.expression.MethodEvaluator
import com.sun.jdi.BooleanValue
import com.sun.jdi.ObjectReference
import com.sun.jdi.Value

/**
 * @author Marcin Bukowiecki
 */
class EqualsSearch(private val valueSearcher: BaseWeevilSearcher) : SearchProvider {

    override fun canSearch(searchContext: SearchContext): Boolean {
        return searchContext.valueToSearch != null
    }

    override fun search(
        value: Value?,
        searchContext: SearchContext,
        searchPath: SearchPath,
        depth: Int
    ) {
        if (!canSearch(searchContext)) return
        val ref = value as? ObjectReference ?: return

        val result = MethodEvaluator(
            IdentityEvaluator(ref),
            null,
            "equals",
            null,
            arrayOf(IdentityEvaluator(searchContext.valueToSearch))
        ).evaluate(searchContext.evaluationContextImpl)

        if (result is BooleanValue && result.value()) {
            valueSearcher.addNext(ref, searchPath)
        }
    }
}