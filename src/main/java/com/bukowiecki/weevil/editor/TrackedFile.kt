/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.editor

import com.intellij.debugger.jdi.ThreadReferenceProxyImpl
import com.intellij.openapi.vfs.VirtualFile

/**
 * @author Marcin Bukowiecki
 */
class TrackedFile(val openedFromFrames: Boolean,
                  val url: String,
                  val file: VirtualFile,
                  val threadId: Long,
                  val openedByThread: ThreadReferenceProxyImpl) {

    override fun toString(): String {
        return url
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TrackedFile

        if (url != other.url) return false
        if (threadId != other.threadId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + threadId.hashCode()
        return result
    }
}