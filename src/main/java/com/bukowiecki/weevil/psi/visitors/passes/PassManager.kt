package com.bukowiecki.weevil.psi.visitors.passes

import com.bukowiecki.weevil.debugger.CompilationContext
import com.bukowiecki.weevil.psi.visitors.WeevilDebuggerBaseVisitor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

/**
 * @author Marcin Bukowiecki
 */
object PassManager {

    private val passes = listOf<(List<PsiElement>, Project, CompilationContext) -> WeevilDebuggerBaseVisitor?>(
        { elements, project, compilationContext ->
            BlockAdderPass(elements, project, compilationContext)
        },
        { elements, _, compilationContext ->
            TimeoutCheckPass(elements, compilationContext)
        }
    )

    fun visit(
        expressionsToCompile: List<PsiElement>,
        project: Project,
        compilationContext: CompilationContext
    ) {
        passes.forEach { it.invoke(expressionsToCompile, project, compilationContext)?.visit() }
    }
}