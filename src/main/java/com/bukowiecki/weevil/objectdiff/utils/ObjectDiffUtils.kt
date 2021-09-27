/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.objectdiff.utils

/**
 * @author Marcin Bukowiecki
 */
object ObjectDiffUtils {

    fun getIndexesOfDifferentChars(toCompareWith: Boolean, given: Boolean): Set<Int> {
        return if (toCompareWith != given) {
            setOf(0)
        } else {
            emptySet()
        }
    }


    fun getIndexesOfDifferentChars(toCompareWith: Char, given: Char): Set<Int> {
        return if (toCompareWith != given) {
            setOf(0)
        } else {
            emptySet()
        }
    }

    fun getIndexesOfDifferentChars(toCompareWith: Number, given: Number): Set<Int> {
        return getIndexesOfDifferentChars(toCompareWith.toString(), given.toString())
    }

    fun getIndexesOfDifferentChars(toCompareWith: String, given: String): Set<Int> {
        val result = mutableSetOf<Int>()
        toCompareWith.forEachIndexed { index, c ->
            if (index > given.length) {
                result.add(index)
            } else {
                if (given[index] != c) {
                    result.add(index)
                }
            }
        }
        return result
    }
}
