/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.search.provider

import com.bukowiecki.weevil.evaluator.MapEvaluator
import com.bukowiecki.weevil.search.*
import com.bukowiecki.weevil.utils.WeevilDebuggerUtils
import com.sun.jdi.ObjectReference
import com.sun.jdi.Value

/**
 * @author Marcin Bukowiecki
 */
class MapSearch(private val valueSearcher: BaseWeevilSearcher) : SearchProvider {

    @Suppress("UNCHECKED_CAST")
    override fun search(
        value: Value?,
        searchContext: SearchContext,
        searchPath: SearchPath,
        depth: Int
    ) {
        if (!canSearch(searchContext)) return
        if (!WeevilDebuggerUtils.isMap(value as? ObjectReference ?: return, searchContext.evaluationContextImpl)) return

        val eventCollectorMapEval = MapEvaluator(value).evaluate(searchContext.evaluationContextImpl) as Map<Value, Value>

        for ((i, entry) in eventCollectorMapEval.entries.withIndex()) {
            if (valueSearcher.checkCanceled(searchContext)) {
                return
            }

            val k = entry.key
            val v = entry.value

            valueSearcher.searchSingleValue(
                k,
                searchContext,
                MapSearchPath(i, searchPath).let { searchPath.addNext(it); it },
                depth + 1
            )
            valueSearcher.searchSingleValue(
                v,
                searchContext,
                MapSearchPath(i, searchPath).let { searchPath.addNext(it); it },
                depth + 1
            )
        }
    }
}