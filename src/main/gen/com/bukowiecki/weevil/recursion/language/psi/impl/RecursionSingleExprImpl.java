// This is a generated file. Not intended for manual editing.
package com.bukowiecki.weevil.recursion.language.psi.impl;

import java.util.List;

import com.bukowiecki.weevil.recursion.language.psi.*;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.bukowiecki.weevil.recursion.language.psi.RecursionTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.bukowiecki.weevil.recursion.language.psi.*;

public class RecursionSingleExprImpl extends ASTWrapperPsiElement implements RecursionSingleExpr {

  public RecursionSingleExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull RecursionVisitor visitor) {
    visitor.visitSingleExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof RecursionVisitor) accept((RecursionVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public RecursionAboveExpr getAboveExpr() {
    return findChildByClass(RecursionAboveExpr.class);
  }

  @Override
  @Nullable
  public RecursionNumberExpr getNumberExpr() {
    return findChildByClass(RecursionNumberExpr.class);
  }

  @Override
  @Nullable
  public RecursionRangeExpr getRangeExpr() {
    return findChildByClass(RecursionRangeExpr.class);
  }

  @Override
  @Nullable
  public RecursionUnderExpr getUnderExpr() {
    return findChildByClass(RecursionUnderExpr.class);
  }

}
