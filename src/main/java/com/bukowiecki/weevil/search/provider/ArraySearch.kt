/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.search.provider

import com.bukowiecki.weevil.search.*
import com.sun.jdi.ArrayReference
import com.sun.jdi.Value

/**
 * @author Marcin Bukowiecki
 */
class ArraySearch(private val valueSearcher: BaseWeevilSearcher) : SearchProvider {

    override fun search(
        value: Value?,
        searchContext: SearchContext,
        searchPath: SearchPath,
        depth: Int
    ) {
        if (!canSearch(searchContext)) return
        val arrayRef = value as? ArrayReference ?: return

        val length = arrayRef.length()
        var i = 0
        while (i < length) {
            if (valueSearcher.checkCanceled(searchContext)) {
                return
            }
            val currentValue = arrayRef.getValue(i)
            valueSearcher.searchSingleValue(
                currentValue,
                searchContext,
                ArraySearchPath(i, searchPath).let { searchPath.addNext(it); it },
                depth + 1
            )
            i++
        }
    }
}