/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.search.codefragment

import com.bukowiecki.weevil.search.BaseWeevilSearcher
import com.bukowiecki.weevil.search.SearchContext
import com.bukowiecki.weevil.search.SearchPath
import com.bukowiecki.weevil.search.provider.*
import com.intellij.debugger.engine.evaluation.expression.IdentityEvaluator
import com.intellij.debugger.engine.evaluation.expression.MethodEvaluator
import com.intellij.debugger.engine.evaluation.expression.UnBoxingEvaluator
import com.intellij.openapi.project.Project
import com.sun.jdi.BooleanValue
import com.sun.jdi.ObjectReference
import com.sun.jdi.Value

/**
 * @author Marcin Bukowiecki
 */
abstract class BaseCodeFragmentSearcher(project: Project) : BaseWeevilSearcher(project) {

    protected lateinit var functionRef: ObjectReference

    private val searchProviders = initProviders()

    override fun getSearchProviders(): List<SearchProvider> {
        return searchProviders
    }

    abstract fun createNewCodeFragment(toInject: String): String

    override fun searchSingleValue(
        value: Value?,
        searchContext: SearchContext,
        searchPath: SearchPath,
        depth: Int
    ) {
        if (searchContext.maxDepth < depth) return

        checkCanceled(searchContext)

        val matchingResult = MethodEvaluator(
            IdentityEvaluator(functionRef),
            null,
            "apply",
            null,
            arrayOf(IdentityEvaluator(value ?: return))
        ).evaluate(searchContext.evaluationContextImpl)

        (matchingResult as? ObjectReference)?.let { objectReference ->
            UnBoxingEvaluator(IdentityEvaluator(objectReference)).evaluate(searchContext.evaluationContextImpl)
                ?.let { unboxed ->
                    (unboxed as? BooleanValue)?.let { booleanValue ->
                        if (booleanValue.value()) {
                            addNext(value, searchPath)
                        }
                        getSearchProviders().forEach { it.search(value, searchContext, searchPath, depth) }
                    }
                }
        }
    }

    private fun initProviders(): List<SearchProvider> {
        return listOf(
            EqualsSearch(this),
            ListSearch(this),
            ArraySearch(this),
            MapSearch(this),
            CollectionSearch(this),
            ObjectSearch(this),
        )
    }
}