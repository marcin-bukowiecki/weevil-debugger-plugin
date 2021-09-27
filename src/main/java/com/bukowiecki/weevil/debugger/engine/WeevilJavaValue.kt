/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.debugger.engine

import com.intellij.debugger.engine.JavaValue
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.ui.impl.watch.NodeManagerImpl
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl

/**
 * @author Marcin Bukowiecki
 */
open class WeevilJavaValue(
    parent: JavaValue?,
    name: String,
    valueDescriptor: ValueDescriptorImpl,
    evaluationContext: EvaluationContextImpl,
    nodeManager: NodeManagerImpl,
    contextSet: Boolean
) : JavaValue(
    parent,
    name,
    valueDescriptor,
    evaluationContext,
    nodeManager,
    contextSet) {

    override val isPinned: Boolean = false

    override fun canBePinned(): Boolean {
        return false
    }
}
