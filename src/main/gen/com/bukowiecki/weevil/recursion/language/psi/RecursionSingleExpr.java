// This is a generated file. Not intended for manual editing.
package com.bukowiecki.weevil.recursion.language.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface RecursionSingleExpr extends PsiElement {

  @Nullable
  RecursionAboveExpr getAboveExpr();

  @Nullable
  RecursionNumberExpr getNumberExpr();

  @Nullable
  RecursionRangeExpr getRangeExpr();

  @Nullable
  RecursionUnderExpr getUnderExpr();

}
