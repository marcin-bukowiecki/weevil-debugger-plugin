/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.debugger.ui

import com.sun.jdi.Value

/**
 * @author Marcin Bukowiecki
 */
interface WithHistoryDescriptor {

    val history: List<Value?>

    fun latest(): Value? = history.last()

    fun hasHistory(): Boolean = history.size > 1

    fun getName(): String
}