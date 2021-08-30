// This is a generated file. Not intended for manual editing.
package com.bukowiecki.weevil.recursion.parser;

import com.bukowiecki.weevil.recursion.language.psi.RecursionTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.tree.IElementType;

import static com.intellij.lang.parser.GeneratedParserUtilBase.*;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class RecursionParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return root(b, l + 1);
  }

  /* ********************************************************** */
  // leftNumber PLUS
  public static boolean aboveExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "aboveExpr")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, RecursionTypes.NUMBER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = leftNumber(b, l + 1);
    r = r && GeneratedParserUtilBase.consumeToken(b, RecursionTypes.PLUS);
    exit_section_(b, m, RecursionTypes.ABOVE_EXPR, r);
    return r;
  }

  /* ********************************************************** */
  // singleExpr (COMMA singleExpr)*
  public static boolean expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, RecursionTypes.NUMBER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = singleExpr(b, l + 1);
    r = r && expression_1(b, l + 1);
    exit_section_(b, m, RecursionTypes.EXPRESSION, r);
    return r;
  }

  // (COMMA singleExpr)*
  private static boolean expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!expression_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expression_1", c)) break;
    }
    return true;
  }

  // COMMA singleExpr
  private static boolean expression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, RecursionTypes.COMMA);
    r = r && singleExpr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // NUMBER
  public static boolean leftNumber(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "leftNumber")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, RecursionTypes.NUMBER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, RecursionTypes.NUMBER);
    exit_section_(b, m, RecursionTypes.LEFT_NUMBER, r);
    return r;
  }

  /* ********************************************************** */
  // NUMBER
  public static boolean numberExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "numberExpr")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, RecursionTypes.NUMBER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, RecursionTypes.NUMBER);
    exit_section_(b, m, RecursionTypes.NUMBER_EXPR, r);
    return r;
  }

  /* ********************************************************** */
  // leftNumber MINUS rightNumber
  public static boolean rangeExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rangeExpr")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, RecursionTypes.NUMBER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = leftNumber(b, l + 1);
    r = r && GeneratedParserUtilBase.consumeToken(b, RecursionTypes.MINUS);
    r = r && rightNumber(b, l + 1);
    exit_section_(b, m, RecursionTypes.RANGE_EXPR, r);
    return r;
  }

  /* ********************************************************** */
  // NUMBER
  public static boolean rightNumber(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rightNumber")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, RecursionTypes.NUMBER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, RecursionTypes.NUMBER);
    exit_section_(b, m, RecursionTypes.RIGHT_NUMBER, r);
    return r;
  }

  /* ********************************************************** */
  // expression?
  static boolean root(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root")) return false;
    expression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // rangeExpr | underExpr | aboveExpr | numberExpr
  public static boolean singleExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "singleExpr")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, RecursionTypes.NUMBER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = rangeExpr(b, l + 1);
    if (!r) r = underExpr(b, l + 1);
    if (!r) r = aboveExpr(b, l + 1);
    if (!r) r = numberExpr(b, l + 1);
    exit_section_(b, m, RecursionTypes.SINGLE_EXPR, r);
    return r;
  }

  /* ********************************************************** */
  // leftNumber MINUS
  public static boolean underExpr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "underExpr")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, RecursionTypes.NUMBER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = leftNumber(b, l + 1);
    r = r && GeneratedParserUtilBase.consumeToken(b, RecursionTypes.MINUS);
    exit_section_(b, m, RecursionTypes.UNDER_EXPR, r);
    return r;
  }

}
