/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.objectdiff.utils

import org.junit.Assert
import org.junit.Test

/**
 * @author Marcin Bukowiecki
 */
class ObjectDiffUtilsTest {

    @Test
    fun test_getIndexesOfDifferentChars_1() {
        val indexesOfDifferentChars = ObjectDiffUtils.getIndexesOfDifferentChars("foo1", "foo")
        Assert.assertEquals(setOf(3), indexesOfDifferentChars.differentIndexes)
        Assert.assertTrue(indexesOfDifferentChars.isDifferent)
    }

    @Test
    fun test_getIndexesOfDifferentChars_2() {
        val indexesOfDifferentChars = ObjectDiffUtils.getIndexesOfDifferentChars("foo", "foo1")
        Assert.assertEquals(emptySet<Int>(), indexesOfDifferentChars.differentIndexes)
        Assert.assertTrue(indexesOfDifferentChars.isDifferent)
    }

    @Test
    fun test_getIndexesOfDifferentChars_3() {
        val indexesOfDifferentChars = ObjectDiffUtils.getIndexesOfDifferentChars("foo", "foo")
        Assert.assertEquals(emptySet<Int>(), indexesOfDifferentChars.differentIndexes)
        Assert.assertFalse(indexesOfDifferentChars.isDifferent)
    }

    @Test
    fun test_getIndexesOfDifferentChars_4() {
        val indexesOfDifferentChars = ObjectDiffUtils.getIndexesOfDifferentChars("", "")
        Assert.assertEquals(emptySet<Int>(), indexesOfDifferentChars.differentIndexes)
        Assert.assertFalse(indexesOfDifferentChars.isDifferent)
    }

    @Test
    fun test_getIndexesOfDifferentChars_5() {
        val indexesOfDifferentChars = ObjectDiffUtils.getIndexesOfDifferentChars("foo", "bar")
        Assert.assertEquals(setOf(0,1,2), indexesOfDifferentChars.differentIndexes)
        Assert.assertTrue(indexesOfDifferentChars.isDifferent)
    }
}
