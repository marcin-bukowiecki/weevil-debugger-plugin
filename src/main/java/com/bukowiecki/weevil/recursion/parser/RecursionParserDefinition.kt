/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.recursion.parser

import com.bukowiecki.weevil.recursion.language.RecursionLanguage
import com.bukowiecki.weevil.recursion.language.psi.RecursionFile
import com.bukowiecki.weevil.recursion.language.psi.RecursionTypes
import com.bukowiecki.weevil.recursion.lexer.RecursionLexerAdapter
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

/**
 * @author Marcin Bukowiecki
 */
class RecursionParserDefinition : ParserDefinition {

    override fun createLexer(project: Project?): Lexer {
        return RecursionLexerAdapter()
    }

    override fun createParser(project: Project?): PsiParser {
        return com.bukowiecki.weevil.recursion.parser.RecursionParser()
    }

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet {
        return TokenSet.EMPTY
    }

    override fun getStringLiteralElements(): TokenSet {
        return TokenSet.EMPTY
    }

    override fun createElement(node: ASTNode?): PsiElement {
        return RecursionTypes.Factory.createElement(node)
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return RecursionFile(viewProvider)
    }

    companion object {

        val FILE = IFileElementType(RecursionLanguage.INSTANCE)
    }
}