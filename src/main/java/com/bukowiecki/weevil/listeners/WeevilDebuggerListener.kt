/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.listeners

/**
 * @author Marcin Bukowiecki
 */
interface WeevilDebuggerListener {

    fun evaluateFuture() {

    }

    fun futureEvaluated() {

    }

    fun settingsChanged() {

    }

    fun threadChanged() {

    }

    fun sessionPaused() {

    }

    fun sessionStopped() {

    }

    fun processStopped() {

    }

    fun stackFrameChanged() {

    }
}