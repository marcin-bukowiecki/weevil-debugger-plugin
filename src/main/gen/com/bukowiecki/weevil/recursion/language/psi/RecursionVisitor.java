// This is a generated file. Not intended for manual editing.
package com.bukowiecki.weevil.recursion.language.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class RecursionVisitor extends PsiElementVisitor {

  public void visitAboveExpr(@NotNull RecursionAboveExpr o) {
    visitPsiElement(o);
  }

  public void visitExpression(@NotNull RecursionExpression o) {
    visitPsiElement(o);
  }

  public void visitLeftNumber(@NotNull RecursionLeftNumber o) {
    visitPsiElement(o);
  }

  public void visitNumberExpr(@NotNull RecursionNumberExpr o) {
    visitPsiElement(o);
  }

  public void visitRangeExpr(@NotNull RecursionRangeExpr o) {
    visitPsiElement(o);
  }

  public void visitRightNumber(@NotNull RecursionRightNumber o) {
    visitPsiElement(o);
  }

  public void visitSingleExpr(@NotNull RecursionSingleExpr o) {
    visitPsiElement(o);
  }

  public void visitUnderExpr(@NotNull RecursionUnderExpr o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
