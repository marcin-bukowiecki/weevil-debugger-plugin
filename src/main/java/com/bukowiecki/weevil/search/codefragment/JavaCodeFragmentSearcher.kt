/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.search.codefragment

import com.bukowiecki.weevil.search.InitialSearchPath
import com.bukowiecki.weevil.search.impl.SearchInputComponent
import com.bukowiecki.weevil.ui.SearchController
import com.intellij.debugger.engine.JavaDebuggerEvaluator
import com.intellij.debugger.engine.JavaValue
import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.xdebugger.evaluation.EvaluationMode
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.XValue
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl
import com.sun.jdi.ObjectReference

/**
 * @author Marcin Bukowiecki
 */
open class JavaCodeFragmentSearcher(project: Project) : BaseCodeFragmentSearcher(project) {

    private val log = Logger.getInstance(JavaCodeFragmentSearcher::class.java)

    override fun getLanguage(): Language {
        return JavaLanguage.INSTANCE
    }

    override fun search(
        searchController: SearchController,
        inputComponent: SearchInputComponent,
        evaluator: JavaDebuggerEvaluator
    ) {
        try {
            val expression = inputComponent.provideCode()
            val newExpression = createNewCodeFragment(expression.expression)
            val searchDialog = searchController.searchDialog
            val self = this

            val customInfo = if (expression.customInfo.isNullOrEmpty()) {
                "java.util.function.Function"
            } else {
                expression.customInfo + ",java.util.function.Function"
            }

            log.info("Got expression to search: $newExpression")

            evaluator.evaluate(XExpressionImpl(
                newExpression,
                getLanguage(),
                customInfo,
                EvaluationMode.CODE_FRAGMENT
            ),
                object : XDebuggerEvaluator.XEvaluationCallback {

                    override fun errorOccurred(errorMessage: String) {
                        log.error("Exception while searching: $errorMessage")
                        doError(searchController, errorMessage)
                    }

                    override fun evaluated(result: XValue) {
                        (result as? JavaValue)?.let { functionRef ->
                            self.functionRef = functionRef.descriptor.value as? ObjectReference ?: return

                            val searchContext = prepareContext(null, functionRef.evaluationContext)
                            doSearch(searchController, searchContext, InitialSearchPath())
                        }
                    }

                }, searchDialog.sourcePosition
            )
        } finally {
            searchController.enableSearchButton()
        }
    }

    override fun createNewCodeFragment(toInject: String): String {
        val dollar = '$'

        return """        
        Function<Object, Boolean> wrapped = new Function<Object, Boolean>() {
            @Override
            public Boolean apply(Object ${dollar}it) {
                $toInject
            }
        };
        """
    }
}