/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.utils

import com.bukowiecki.weevil.debugger.controller.PsiValueHolder
import com.bukowiecki.weevil.psi.CodeEvent
import com.bukowiecki.weevil.search.impl.SearchCodeFragmentInputComponent
import com.bukowiecki.weevil.shell.ShellTab
import com.bukowiecki.weevil.ui.WeevilDebuggerSessionTab
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiLocalVariable

/**
 * @author Marcin Bukowiecki
 */
object WeevilDebuggerDataKey {

    val weevilResultDataKey = Key.create<PsiValueHolder>("WeevilValue")
    val weevilDebuggerShellDataKeys = Key.create<ShellTab>("WeevilDebuggerShellTab")
    val weevilDebuggerDataKeys = Key.create<WeevilDebuggerSessionTab>("WeevilDebuggerTab")
    val weevilDataKey = Key.create<CodeEvent>("WeevilCodeEvent")
    val weevilLocalRefDataKey = Key.create<PsiLocalVariable>("WeevilLocalRefDataKey")
    val weevilCodeFragmentInputComponent = Key.create<SearchCodeFragmentInputComponent>("weevilIsCodeFragmentEditor")
}