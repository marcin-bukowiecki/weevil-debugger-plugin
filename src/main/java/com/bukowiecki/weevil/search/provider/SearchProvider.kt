package com.bukowiecki.weevil.search.provider

import com.bukowiecki.weevil.search.SearchContext
import com.bukowiecki.weevil.search.SearchPath
import com.sun.jdi.Value

/**
 * @author Marcin Bukowiecki
 */
interface SearchProvider {

    fun canSearch(searchContext: SearchContext): Boolean = true

    fun search(value: Value?,
               searchContext: SearchContext,
               searchPath: SearchPath,
               depth: Int)
}