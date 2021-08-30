/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.search

import com.bukowiecki.weevil.search.impl.SearchInputComponent
import com.bukowiecki.weevil.search.provider.*
import com.bukowiecki.weevil.ui.SearchController
import com.intellij.debugger.engine.JavaDebuggerEvaluator
import com.intellij.debugger.engine.JavaValue
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.project.Project
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.XValue
import com.sun.jdi.*

/**
 * @author Marcin Bukowiecki
 */
abstract class SearchPath(val name: String, private val parent: SearchPath?) {

    val next = mutableListOf<SearchPath>()

    var matched: Boolean = false

    fun addNext(next: SearchPath): SearchPath {
        this.next.add(next)
        if (next.matched) {
            propagateToParent(true)
        }
        return next
    }

    private fun propagateToParent(matched: Boolean) {
        this.matched = matched
        parent?.propagateToParent(matched)
    }

    fun matches(paths: List<String>, index: Int, currentRow: Int): Boolean {
        //i.e. (["", "s"]) where s is a local variable with history
        if (paths.size == 2 && currentRow < next.size && next[currentRow] is HistoryEntrySearchPath) {
            return next[currentRow].matched
        }
        return if (index == paths.size - 1) {
            paths[index] == name && matched
        } else {
            val anyMatches = next.any { it.matches(paths, index + 1, currentRow) }
            paths[index] == name && matched && anyMatches
        }
    }

    fun toList(): List<SearchPath> {
        val result = mutableListOf<SearchPath>()
        result.add(this)
        var givenParent = parent
        while (givenParent != null) {
            result.add(givenParent)
            givenParent = givenParent.parent
        }
        return result.reversed()
    }
}

/**
 * @author Marcin Bukowiecki
 */
class SearchPathWithValue(val value: Value?, parent: SearchPath) : SearchPath("", parent) {

    init {
        matched = true
    }
}

/**
 * @author Marcin BukowieckiX
 */
class MapSearchPath(index: Int, parent: SearchPath) : SearchPath(index.toString(), parent)

/**
 * @author Marcin BukowieckiX
 */
class ArraySearchPath(index: Int, parent: SearchPath) : SearchPath(index.toString(), parent)

/**
 * @author Marcin Bukowiecki
 */
class CollectionSearchPath(index: Int, parent: SearchPath) : SearchPath(index.toString(), parent)

/**
 * @author Marcin Bukowiecki
 */
class HistoryEntrySearchPath(name: String, parent: SearchPath) : SearchPath(name, parent)

/**
 * @author Marcin Bukowiecki
 */
class FieldSearchPath(name: String, parent: SearchPath) : SearchPath(name, parent)

/**
 * @author Marcin BukowieckiX
 */
class InitialSearchPath : SearchPath("", null)

/**
 * @author Marcin BukowieckiX
 */
class ValueSearcher(myProject: Project) : BaseWeevilSearcher(myProject) {

    private val searchProviders = listOf(
        EqualsSearch(this),
        ListSearch(this),
        ArraySearch(this),
        MapSearch(this),
        CollectionSearch(this),
        ObjectSearch(this),
    )

    override fun search(searchController: SearchController,
                        inputComponent: SearchInputComponent,
                        evaluator: JavaDebuggerEvaluator) {

        evaluator.evaluate(
            inputComponent.provideCode(), object : XDebuggerEvaluator.XEvaluationCallback {

                override fun errorOccurred(errorMessage: String) {
                    doError(searchController, errorMessage)
                }

                override fun evaluated(result: XValue) {
                    val javaValue = result as? JavaValue ?: return

                    val searchContext = prepareContext(javaValue.descriptor.value, javaValue.evaluationContext)
                    doSearch(searchController, searchContext, InitialSearchPath())
                }
            },
            searchController.searchDialog.sourcePosition
        )
    }

    override fun getLanguage(): Language {
        return JavaLanguage.INSTANCE
    }

    override fun searchSingleValue(
        value: Value?,
        searchContext: SearchContext,
        searchPath: SearchPath,
        depth: Int
    ) {
        if (searchContext.maxDepth < depth) return

        checkCanceled(searchContext)
        val valueToSearch = searchContext.valueToSearch
        if (value == null && valueToSearch == null) {
            addNext(value, searchPath)
            return
        }

        if (value is BooleanValue && valueToSearch is BooleanValue) {
            if (value.value() == valueToSearch.value()) {
                addNext(value, searchPath)
            }
            return
        }

        if (value is StringReference && valueToSearch is StringReference) {
            if (value.value() == valueToSearch.value()) {
                addNext(value, searchPath)
            }
            return
        }

        if (value is ByteValue && valueToSearch is ByteValue) {
            if (value.value() == valueToSearch.value()) {
                addNext(value, searchPath)
            }
            return
        }

        if (value is ShortValue && valueToSearch is ShortValue) {
            if (value.value() == valueToSearch.value()) {
                addNext(value, searchPath)
            }
            return
        }

        if (value is CharValue && valueToSearch is CharValue) {
            if (value.value() == valueToSearch.value()) {
                addNext(value, searchPath)
            }
            return
        }

        if (value is IntegerValue && valueToSearch is IntegerValue) {
            if (value.value() == valueToSearch.value()) {
                addNext(value, searchPath)
            }
            return
        }

        if (value is LongValue && valueToSearch is LongValue) {
            if (value.value() == valueToSearch.value()) {
                addNext(value, searchPath)
            }
            return
        }

        if (value is FloatValue && valueToSearch is FloatValue) {
            if (value.value() == valueToSearch.value()) {
                addNext(value, searchPath)
            }
            return
        }

        if (value is DoubleValue && valueToSearch is DoubleValue) {
            if (value.value() == valueToSearch.value()) {
                addNext(value, searchPath)
            }
            return
        }

        getSearchProviders().forEach { it.search(value, searchContext, searchPath, depth) }
    }

    override fun getSearchProviders(): List<SearchProvider> = searchProviders
}