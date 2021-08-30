/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.views

import com.bukowiecki.weevil.debugger.engine.WeevilJavaValue
import com.bukowiecki.weevil.debugger.ui.*
import com.bukowiecki.weevil.services.WeevilResultService
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener

/**
 * @author Marcin Bukowiecki
 */
class WeevilTreeSelectionListener(private val weevilDebuggerFutureValuesView: WeevilDebuggerFutureValuesView) : TreeSelectionListener {

    override fun valueChanged(event: TreeSelectionEvent) {
        val currentPsiFile = weevilDebuggerFutureValuesView.getCurrentFile() ?: return
        if (!handleChange(currentPsiFile, event)) {
            restart(currentPsiFile)
        }
    }

    private fun handleChange(currentPsiFile: PsiFile, event: TreeSelectionEvent): Boolean {
        val newLeadSelectionPath = event.newLeadSelectionPath ?: return false

        if (newLeadSelectionPath.pathCount == 2) {
            (newLeadSelectionPath.lastPathComponent as? XValueNodeImpl)?.let { valueNode ->
                (valueNode.valueContainer as? WeevilJavaValue)?.let { weevilJavaValue ->
                    (weevilJavaValue.descriptor as? WeevilBaseValueDescriptorImpl)?.let { weevilPsiValueDescriptorImpl ->
                        val psiElementRef = Ref<PsiElement?>()

                        val visitor = object : WeevilDescriptorVisitor() {

                            override fun visit(descriptor: PredictedLocalVariableDescriptorImpl) {
                                psiElementRef.set(descriptor.codeEvent.getPsiVariable())
                            }

                            override fun visit(descriptor: MethodReturnValueDescriptorImpl) {
                                psiElementRef.set(descriptor.codeEvent.ref)
                            }

                            override fun visit(descriptor: ExpressionValueDescriptorImpl) {
                                psiElementRef.set(descriptor.codeEvent.ref)
                            }

                            override fun visit(descriptor: BooleanDescriptorImpl) {
                                psiElementRef.set(descriptor.codeEvent.ref)
                            }

                            override fun visit(descriptor: IfConditionDescriptorImpl) {
                                psiElementRef.set(descriptor.codeEvent.stmt)
                            }
                        }

                        weevilPsiValueDescriptorImpl.accept(visitor)

                        psiElementRef.get()?.let { psiElement ->
                            val service = WeevilResultService.getInstance(weevilDebuggerFutureValuesView.project)
                            synchronized(service.serviceLock) {
                                service.xTreeElementToMark = psiElement
                            }
                            DaemonCodeAnalyzer.getInstance(weevilJavaValue.project).restart(currentPsiFile)
                            return true
                        }
                    }
                }
            }
        }

        return false
    }

    private fun restart(currentPsiFile: PsiFile) {
        val project = weevilDebuggerFutureValuesView.project
        val service = WeevilResultService.getInstance(project)

        synchronized(service.serviceLock) {
            service.xTreeElementToMark = null
        }

        DaemonCodeAnalyzer.getInstance(project).restart(currentPsiFile)
    }
}