/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.editor.ui

import com.intellij.openapi.vfs.VirtualFile

/**
 * @author Marcin Bukowiecki
 */
class ToCloseFileRow(val myVirtualFile: VirtualFile) {

    var myChecked: Boolean = true

    fun getColumn(index: Int): Any {
        return when (index) {
            0 -> {
                myChecked
            }
            1 -> {
                myVirtualFile.presentableName
            }
            else -> {
                throw UnsupportedOperationException()
            }
        }
    }
}