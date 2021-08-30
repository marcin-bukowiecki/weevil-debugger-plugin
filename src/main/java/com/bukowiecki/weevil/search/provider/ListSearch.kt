/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.search.provider

import com.bukowiecki.weevil.debugger.ui.VariableDescriptorUtils
import com.bukowiecki.weevil.search.*
import com.bukowiecki.weevil.utils.WeevilDebuggerUtils
import com.sun.jdi.ObjectReference
import com.sun.jdi.Value

/**
 * @author Marcin Bukowiecki
 */
class ListSearch(private val valueSearcher: BaseWeevilSearcher) : SearchProvider {

    override fun search(
        value: Value?,
        searchContext: SearchContext,
        searchPath: SearchPath,
        depth: Int
    ) {
        if (!canSearch(searchContext)) return
        val ref = value as? ObjectReference ?: return

        val isList = WeevilDebuggerUtils.isList(ref, searchContext.evaluationContextImpl)
        if (!isList) return

        VariableDescriptorUtils.iterateCollection(ref, searchContext.evaluationContextImpl) { index, collectionValue ->
            if (valueSearcher.checkCanceled(searchContext)) {
                return@iterateCollection
            }
            valueSearcher.searchSingleValue(
                collectionValue,
                searchContext,
                searchPath.addNext(CollectionSearchPath(index, searchPath)),
                depth
            )
        }
    }
}