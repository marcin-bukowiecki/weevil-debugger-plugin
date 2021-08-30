/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.utils

import com.intellij.openapi.projectRoots.JavaSdkVersion

/**
 * @author Marcin Bukowiecki
 */
class JavaSdkVersionWrapper(private val sdk: JavaSdkVersion?) {

    fun isSearchSupported(): Boolean {
        return sdk != null && sdk.isAtLeast(JavaSdkVersion.JDK_1_8)
    }

    fun isEvaluateFutureSupported(): Boolean {
        return sdk != null && sdk.isAtLeast(JavaSdkVersion.JDK_1_8)
    }
}