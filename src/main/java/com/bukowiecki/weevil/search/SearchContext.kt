/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.search

import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.sun.jdi.Value
import java.time.Instant

/**
 * @author Marcin Bukowiecki
 */
data class SearchContext(
    val startup: Instant,
    val valueToSearch: Value?,
    val maxDepth: Int,
    val evaluationContextImpl: EvaluationContextImpl
)
