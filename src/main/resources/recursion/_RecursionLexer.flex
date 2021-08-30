package com.bukowiecki.weevil.recursion.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.bukowiecki.weevil.recursion.language.psi.RecursionTypes.*;

%%

%{
  public _RecursionLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _RecursionLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL=\R
WHITE_SPACE=\s+
NUMBER = 0 | [1-9][0-9]*

%state WAITING_VALUE

%%
<YYINITIAL> {
        {NUMBER}         { return NUMBER; }
              "-"        { yybegin(YYINITIAL); return MINUS; }
              "+"        { yybegin(YYINITIAL); return PLUS; }
              ","        { yybegin(YYINITIAL); return COMMA; }
}

<WAITING_VALUE> {
        {WHITE_SPACE}     { return WHITE_SPACE; }
}

[^] { return BAD_CHARACTER; }
