// This is a generated file. Not intended for manual editing.
package com.bukowiecki.weevil.recursion.language.psi;

import com.bukowiecki.weevil.recursion.language.psi.impl.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.bukowiecki.weevil.recursion.language.psi.impl.*;

public interface RecursionTypes {

  IElementType ABOVE_EXPR = new RecursionElementType("ABOVE_EXPR");
  IElementType EXPRESSION = new RecursionElementType("EXPRESSION");
  IElementType LEFT_NUMBER = new RecursionElementType("LEFT_NUMBER");
  IElementType NUMBER_EXPR = new RecursionElementType("NUMBER_EXPR");
  IElementType RANGE_EXPR = new RecursionElementType("RANGE_EXPR");
  IElementType RIGHT_NUMBER = new RecursionElementType("RIGHT_NUMBER");
  IElementType SINGLE_EXPR = new RecursionElementType("SINGLE_EXPR");
  IElementType UNDER_EXPR = new RecursionElementType("UNDER_EXPR");

  IElementType COMMA = new RecursionTokenType("COMMA");
  IElementType MINUS = new RecursionTokenType("MINUS");
  IElementType NUMBER = new RecursionTokenType("NUMBER");
  IElementType PLUS = new RecursionTokenType("PLUS");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ABOVE_EXPR) {
        return new RecursionAboveExprImpl(node);
      }
      else if (type == EXPRESSION) {
        return new RecursionExpressionImpl(node);
      }
      else if (type == LEFT_NUMBER) {
        return new RecursionLeftNumberImpl(node);
      }
      else if (type == NUMBER_EXPR) {
        return new RecursionNumberExprImpl(node);
      }
      else if (type == RANGE_EXPR) {
        return new RecursionRangeExprImpl(node);
      }
      else if (type == RIGHT_NUMBER) {
        return new RecursionRightNumberImpl(node);
      }
      else if (type == SINGLE_EXPR) {
        return new RecursionSingleExprImpl(node);
      }
      else if (type == UNDER_EXPR) {
        return new RecursionUnderExprImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
