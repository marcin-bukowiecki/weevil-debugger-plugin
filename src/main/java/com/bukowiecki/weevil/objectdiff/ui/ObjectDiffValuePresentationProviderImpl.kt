/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.objectdiff.ui

import com.bukowiecki.weevil.debugger.ui.WeevilXValuePresentationProviderImpl
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl
import com.intellij.xdebugger.frame.presentation.XValuePresentation

/**
 * @author Marcin Bukowiecki
 */
class ObjectDiffValuePresentationProviderImpl : WeevilXValuePresentationProviderImpl() {

    override fun getPresentation(descriptor: ValueDescriptorImpl): XValuePresentation {
        return ObjectDiffValuePresentation(descriptor as ObjectDiffValueDescriptorImpl)
    }
}
