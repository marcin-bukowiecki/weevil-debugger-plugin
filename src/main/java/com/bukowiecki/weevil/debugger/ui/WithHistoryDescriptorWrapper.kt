/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.debugger.ui

import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl
import com.sun.jdi.Value

/**
 * @author Marcin Bukowiecki
 */
class WithHistoryDescriptorWrapper(private val descriptor: ValueDescriptorImpl) : WithHistoryDescriptor {

    override val history: List<Value?>
        get() = listOf(descriptor.value)

    override fun hasHistory(): Boolean = false

    override fun getName(): String {
        return descriptor.name
    }
}