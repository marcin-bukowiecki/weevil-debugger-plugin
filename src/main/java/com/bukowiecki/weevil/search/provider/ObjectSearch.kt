/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.search.provider

import com.bukowiecki.weevil.search.BaseWeevilSearcher
import com.bukowiecki.weevil.search.FieldSearchPath
import com.bukowiecki.weevil.search.SearchContext
import com.bukowiecki.weevil.search.SearchPath
import com.bukowiecki.weevil.utils.WeevilDebuggerUtils
import com.intellij.openapi.diagnostic.Logger
import com.sun.jdi.ObjectReference
import com.sun.jdi.Value

/**
 * @author Marcin Bukowiecki
 */
class ObjectSearch(private val valueSearcher: BaseWeevilSearcher) : SearchProvider {

    private val log = Logger.getInstance(ObjectSearch::class.java)

    override fun search(
        value: Value?,
        searchContext: SearchContext,
        searchPath: SearchPath,
        depth: Int
    ) {
        if (!canSearch(searchContext)) return
        val ref = value as? ObjectReference ?: return

        val isCollection = WeevilDebuggerUtils.isCollection(ref, searchContext.evaluationContextImpl)
        if (isCollection) return
        val isMap = WeevilDebuggerUtils.isMap(ref, searchContext.evaluationContextImpl)
        if (isMap) return

        val referenceType = ref.referenceType()
        val allFields = referenceType.allFields()
        allFields.forEach { f ->
            if (valueSearcher.checkCanceled(searchContext)) {
                return@forEach
            }
            val currentValue = ref.getValue(f)
            log.info("Checking field: " + f.name())

            if (currentValue != null) {
                valueSearcher.searchSingleValue(
                    currentValue,
                    searchContext,
                    FieldSearchPath(f.name(), searchPath).let { searchPath.addNext(it); it },
                    depth + 1
                )
            }
        }
    }
}