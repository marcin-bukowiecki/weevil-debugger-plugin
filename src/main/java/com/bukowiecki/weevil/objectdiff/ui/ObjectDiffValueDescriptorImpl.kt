/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.objectdiff.ui

import com.bukowiecki.weevil.debugger.ui.WeevilNamedValueDescriptorImpl
import com.intellij.debugger.ui.tree.render.Renderer
import com.intellij.openapi.project.Project
import com.sun.jdi.Value

/**
 * @author Marcin Bukowiecki
 */
open class ObjectDiffValueDescriptorImpl(
    project: Project,
    simpleName: String,
    value: Value?,
) : WeevilNamedValueDescriptorImpl(project, simpleName, value)

/**
 * @author Marcin Bukowiecki
 */
class CompareToObjectDiffValueDescriptorImpl(
    project: Project,
    simpleName: String,
    value: Value?,
    val valueToCompareWith: Value?
) : ObjectDiffValueDescriptorImpl(project, simpleName, value) {

    override fun getLastLabelRenderer(): Renderer {
        return ObjectDiffValuePresentationProviderImpl()
    }
}
