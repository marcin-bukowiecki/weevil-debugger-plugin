/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.ui

import com.bukowiecki.weevil.search.impl.SearchInputComponent
import com.intellij.debugger.engine.DebugProcessImpl
import com.intellij.debugger.engine.events.DebuggerCommandImpl
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

/**
 * @author Marcin Bukowiecki
 */
class SearchController(val searchDialog: SearchDialog,
                       val debugProcess: DebugProcessImpl,
                       val project: Project) {

    private val log = Logger.getInstance(SearchController::class.java)

    fun search(searchInputComponent: SearchInputComponent) {
        val code = searchInputComponent.provideCode()
        if (code.expression.isEmpty()) {
            log.info("Searching code is empty")
            return
        }

        log.info("Starting search for: $code")

        searchDialog.clearTree()
        searchDialog.setupTree()

        val evaluator = searchDialog.getEvaluator() ?: return

        disableSearchButton()

        searchInputComponent
            .getSearcher()?.search(this, searchInputComponent, evaluator) ?:
            log.info("Could not find searcher for language: " + searchInputComponent.language)
    }

    fun dispatch(toDispatch: () -> Unit) {
        debugProcess.managerThread.invoke(
            object : DebuggerCommandImpl() {

                override fun action() {
                    toDispatch.invoke()
                }
            }
        )
    }

    fun enableSearchButton() {
        searchDialog.enableSearchButton()
    }

    private fun disableSearchButton() {
        searchDialog.disableSearchButton()
    }
}