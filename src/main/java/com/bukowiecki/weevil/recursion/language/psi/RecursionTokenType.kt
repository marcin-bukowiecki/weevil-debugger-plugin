package com.bukowiecki.weevil.recursion.language.psi

import com.intellij.psi.tree.IElementType
import com.bukowiecki.weevil.recursion.language.RecursionLanguage

/**
 * @author Marcin Bukowiecki
 */
class RecursionTokenType(debugName: String) : IElementType(debugName, RecursionLanguage.INSTANCE)