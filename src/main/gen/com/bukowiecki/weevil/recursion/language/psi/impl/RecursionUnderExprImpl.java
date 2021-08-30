// This is a generated file. Not intended for manual editing.
package com.bukowiecki.weevil.recursion.language.psi.impl;

import com.bukowiecki.weevil.recursion.language.psi.RecursionLeftNumber;
import com.bukowiecki.weevil.recursion.language.psi.RecursionUnderExpr;
import com.bukowiecki.weevil.recursion.language.psi.RecursionVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public class RecursionUnderExprImpl extends ASTWrapperPsiElement implements RecursionUnderExpr {

  public RecursionUnderExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull RecursionVisitor visitor) {
    visitor.visitUnderExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof RecursionVisitor) accept((RecursionVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public RecursionLeftNumber getLeftNumber() {
    return findNotNullChildByClass(RecursionLeftNumber.class);
  }

}
