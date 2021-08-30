/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.search

import org.junit.Test

/**
 * @author Marcin BukowieckiX
 */
class InitialSearchPathTest {

    @Test
    fun testWithHistory_1() {
        val initialSearchPath = InitialSearchPath()
        initialSearchPath.next.add(HistoryEntrySearchPath("s", initialSearchPath))
        initialSearchPath.next.add(HistoryEntrySearchPath("s", initialSearchPath))
        initialSearchPath.next.add(HistoryEntrySearchPath("s", initialSearchPath))

        initialSearchPath.next[1].next.add(SearchPathWithValue(DummyValue(), initialSearchPath.next[1]))
    }
}