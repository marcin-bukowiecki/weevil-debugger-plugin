/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.debugger.ui

import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl
import com.intellij.debugger.ui.tree.render.Renderer
import com.intellij.debugger.ui.tree.render.XValuePresentationProvider
import com.intellij.xdebugger.frame.presentation.XValuePresentation
import org.jdom.Element

/**
 * @author Marcin Bukowiecki
 */
open class WeevilXValuePresentationProviderImpl : XValuePresentationProvider, Renderer {

    override fun getPresentation(descriptor: ValueDescriptorImpl): XValuePresentation {
        return WeevilXValuePresentation(descriptor)
    }

    override fun clone(): Renderer {
        return WeevilXValuePresentationProviderImpl()
    }

    override fun readExternal(element: Element?) {

    }

    override fun writeExternal(element: Element?) {

    }

    override fun getUniqueId(): String {
        return "Weevil XValue rendered"
    }
}



