// This is a generated file. Not intended for manual editing.
package com.bukowiecki.weevil.recursion.language.psi.impl;

import java.util.List;

import com.bukowiecki.weevil.recursion.language.psi.RecursionAboveExpr;
import com.bukowiecki.weevil.recursion.language.psi.RecursionLeftNumber;
import com.bukowiecki.weevil.recursion.language.psi.RecursionVisitor;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.bukowiecki.weevil.recursion.language.psi.RecursionTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.bukowiecki.weevil.recursion.language.psi.*;

public class RecursionAboveExprImpl extends ASTWrapperPsiElement implements RecursionAboveExpr {

  public RecursionAboveExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull RecursionVisitor visitor) {
    visitor.visitAboveExpr(this);
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
