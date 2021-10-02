/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.debugger.ui

import com.intellij.debugger.engine.JavaValuePresentation
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl

/**
 * @author Marcin Bukowiecki
 */
open class WeevilXValuePresentation(valueDescriptorImpl: ValueDescriptorImpl) : JavaValuePresentation(valueDescriptorImpl)
