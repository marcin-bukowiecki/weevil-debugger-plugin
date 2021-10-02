/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.objectdiff.utils

import com.sun.jdi.ObjectReference

/**
 * @author Marcin Bukowiecki
 */
object ObjectDiffUtils {

    fun getIndexesOfDifferentChars(given: Boolean, toCompareWith: Boolean): DifferentCharsResult {
        return if (toCompareWith != given) {
            DifferentCharsResult(true, setOf(0))
        } else {
            DifferentCharsResult(false, emptySet())
        }
    }


    fun getIndexesOfDifferentChars(given: Char, toCompareWith: Char): DifferentCharsResult {
        return if (toCompareWith != given) {
            DifferentCharsResult(true, setOf(0))
        } else {
            DifferentCharsResult(false, emptySet())
        }
    }

    fun getIndexesOfDifferentChars(given: Number, toCompareWith: Number): DifferentCharsResult {
        return getIndexesOfDifferentChars(given.toString(), toCompareWith.toString())
    }

    fun getIndexesOfDifferentChars(given: String, toCompareWith: String): DifferentCharsResult {
        val result = mutableSetOf<Int>()

        for (index in given.indices) {
            if (index > toCompareWith.length - 1) {
                result.add(index)
            } else {
                if (given[index] != toCompareWith[index]) {
                    result.add(index)
                }
            }
        }

        val isDifferent = toCompareWith.length != given.length || result.isNotEmpty()

        return DifferentCharsResult(isDifferent, result)
    }

    fun checkTypes(objectToCompare: ObjectReference, objectsToCompareWith: List<ObjectReference>): Boolean {
        val expectedType = objectToCompare.type()
        return objectsToCompareWith.all { it.type() == expectedType }
    }
}

/**
 * @author Marcin Bukowiecki
 */
data class DifferentCharsResult(val isDifferent: Boolean, val differentIndexes: Set<Int>)
