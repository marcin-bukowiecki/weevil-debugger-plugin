/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.actions

import com.bukowiecki.weevil.debugger.ui.WithHistoryDescriptor
import com.bukowiecki.weevil.ui.SearchDialog
import com.bukowiecki.weevil.ui.SearchWithHistoryDialog
import com.bukowiecki.weevil.utils.WeevilDebuggerUtils
import com.intellij.debugger.engine.JavaDebugProcess
import com.intellij.debugger.engine.JavaValue
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiManager
import com.intellij.xdebugger.impl.XDebugSessionImpl
import com.intellij.xdebugger.impl.frame.XValueMarkers
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl

/**
 * @author Marcin Bukowiecki
 */
class ShowHistoryAction : SearchBaseAction() {

    override fun createSearchDialog(
        javaValue: JavaValue,
        descriptor: WithHistoryDescriptor,
        javaDebugProcess: JavaDebugProcess,
        session: XDebugSessionImpl
    ): SearchDialog? {
        val sp = session.currentStackFrame?.sourcePosition ?: return null
        val file = sp.file
        val psiFile = PsiManager.getInstance(session.project).findFile(file) ?: return null

        return SearchWithHistoryDialog(
            javaValue,
            descriptor,
            javaDebugProcess,
            sp,
            psiFile.language,
            session.valueMarkers as XValueMarkers<*, *>
        )
    }

    override fun isEnabled(node: XValueNodeImpl, e: AnActionEvent): Boolean {
        if (super.isEnabled(node, e) && node.valueContainer is JavaValue) {
            val javaValue = node.valueContainer as JavaValue
            if (!WeevilDebuggerUtils.getJavaVersion(javaValue).isSearchSupported()) return false

            val value = javaValue.descriptor as? WithHistoryDescriptor ?: return false
            return value.hasHistory()
        } else {
            return false
        }
    }
}