/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.search

import com.bukowiecki.weevil.bundle.WeevilDebuggerBundle
import com.bukowiecki.weevil.search.provider.SearchProvider
import com.bukowiecki.weevil.settings.WeevilDebuggerSettings
import com.bukowiecki.weevil.ui.SearchController
import com.bukowiecki.weevil.xdebugger.WeevilDebuggerRootNode
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.lang.Language
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.sun.jdi.Value
import java.time.Instant

/**
 * @author Marcin Bukowiecki
 */
abstract class BaseWeevilSearcher(private val myProject: Project) : WeevilSearcher {

    private val log = Logger.getInstance(BaseWeevilSearcher::class.java)

    private var timedOut = false

    private val matchedPaths = mutableListOf<List<SearchPath>>()

    abstract fun getLanguage(): Language

    fun doError(searchController: SearchController, errorMessage: String) {
        searchController.enableSearchButton()
        val searchDialog = searchController.searchDialog
        val tree = searchDialog.tree
        val rootNode = WeevilDebuggerRootNode(tree)
        rootNode.setErrorMessage(errorMessage)
        tree.setRoot(rootNode, true)
    }

    fun doSearch(searchController: SearchController, searchContext: SearchContext, initialSearchPath: InitialSearchPath) {
        val searchDialog = searchController.searchDialog
        searchController.dispatch {
            try {
                searchHistory(
                    searchDialog.descriptor.getName(),
                    searchDialog.descriptor.history,
                    searchContext,
                    initialSearchPath,
                    0
                )

                if (timedOut) {
                    log.info("Search timeout. Setting label")
                    searchDialog.historyLabel.text = WeevilDebuggerBundle.message("weevil.debugger.search.timeout")
                } else {
                    searchDialog.historyLabel.text = WeevilDebuggerBundle.message(
                        "weevil.debugger.search.info",
                        WeevilDebuggerSettings.getInstance(myProject).maxSearchDepth
                    )
                }

                if (initialSearchPath.matched) {
                    val searchXDebuggerTreeListener = SearchXDebuggerTreeListener(initialSearchPath, matchedPaths)
                    searchDialog.tree.addTreeListener(searchXDebuggerTreeListener)
                    searchDialog.searchXDebuggerTreeListener = searchXDebuggerTreeListener
                }
            } finally {
                searchController.enableSearchButton()
            }
        }
    }

    open fun searchHistory(
        name: String,
        history: List<Value?>,
        searchContext: SearchContext,
        searchPath: SearchPath,
        depth: Int
    ) {
        history.forEachIndexed { _, value ->
            if (checkCanceled(searchContext)) {
                return
            }
            searchSingleValue(
                value,
                searchContext,
                searchPath.addNext(HistoryEntrySearchPath(name, searchPath)),
                depth
            )
        }
    }

    abstract fun searchSingleValue(
        value: Value?,
        searchContext: SearchContext,
        searchPath: SearchPath,
        depth: Int
    )

    abstract fun getSearchProviders(): List<SearchProvider>

    fun addNext(value: Value?, parentPath: SearchPath) {
        val searchPathWithValue = SearchPathWithValue(value, parentPath)
        parentPath.addNext(searchPathWithValue)
        matchedPaths.add(searchPathWithValue.toList())
    }

    fun checkCanceled(searchContext: SearchContext): Boolean {
        val searchTimeout = WeevilDebuggerSettings.getInstance(myProject).searchTimeout
        val now = Instant.now()
        val end = searchContext.startup.plusSeconds(searchTimeout)
        if (end.isBefore(now)) {
            log.info("Timeout at: $now, end: $end")
            ProgressManager.getInstance().progressIndicator?.cancel()
            this.timedOut = true
        }
        return this.timedOut
    }

    fun prepareContext(valueToSearch: Value?, evaluationContextImpl: EvaluationContextImpl): SearchContext {
        return SearchContext(
            Instant.now(),
            valueToSearch,
            WeevilDebuggerSettings.getInstance(myProject).maxSearchDepth,
            evaluationContextImpl
        )
    }
}