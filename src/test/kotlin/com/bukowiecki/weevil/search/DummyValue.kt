/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.search

import com.sun.jdi.Type
import com.sun.jdi.Value
import com.sun.jdi.VirtualMachine
import java.lang.UnsupportedOperationException

/**
 * @author Marcin Bukowiecki
 */
class DummyValue : Value {

    override fun virtualMachine(): VirtualMachine {
        throw UnsupportedOperationException()
    }

    override fun type(): Type {
        throw UnsupportedOperationException()
    }
}