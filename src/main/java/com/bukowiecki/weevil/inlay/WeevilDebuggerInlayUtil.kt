/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.inlay

import com.bukowiecki.weevil.debugger.engine.WeevilJavaValue
import com.bukowiecki.weevil.debugger.ui.ExpressionValueDescriptorImpl
import com.bukowiecki.weevil.debugger.ui.MethodReturnValueDescriptorImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiElement
import com.intellij.xdebugger.impl.inline.InlineDebugRenderer
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl
import org.apache.commons.lang3.reflect.FieldUtils

/**
 * @author Marcin Bukowiecki
 */
object WeevilDebuggerInlayUtil {

    private val log = Logger.getInstance(WeevilDebuggerInlayUtil::class.java)

    private const val valueNodeName: String = "myValueNode"

    fun createBlockInlay(
        offset: Int,
        psiElement: PsiElement,
        text: String,
        margin: Int,
        blockInlayType: BlockInlayType
    ) {

        ApplicationManager.getApplication().invokeLater {
            val virtualFile = psiElement.containingFile.virtualFile
            val selectedEditor =
                FileEditorManager.getInstance(psiElement.project).getSelectedEditor(virtualFile)

            if (selectedEditor is TextEditor) {
                val editor = selectedEditor.editor
                editor
                    .inlayModel
                    .addAfterLineEndElement(
                        offset,
                        true,
                        WeevilDebuggerEditorCustomElementRenderer(
                            text,
                            margin,
                            blockInlayType
                        )
                    )
            }
        }
    }

    fun removeBlockInlays(project: Project, inlayTypes: List<BlockInlayType>) {
        inlayTypes.forEach { removeBlockInlays(project, it) }
    }

    fun removeBlockInlays(project: Project, inlayType: BlockInlayType) {
        ApplicationManager.getApplication().invokeLater({
            val allEditors = FileEditorManager.getInstance(project).allEditors
            allEditors.filterIsInstance<TextEditor>().map { it }.forEach {
                val editor = it.editor
                val document = editor.document

                editor.inlayModel.getAfterLineEndElementsInRange(
                    0,
                    document.textLength,
                    WeevilDebuggerEditorCustomElementRenderer::class.java
                ).filter { inlay ->
                    inlay.renderer.inlayType.javaClass == inlayType.javaClass
                }.forEach { inlay -> Disposer.dispose(inlay) }
            }
        }, project.disposed)
    }

    fun removeLineInlays(project: Project) {
        ApplicationManager.getApplication().invokeLater({
            val editors = FileEditorManager.getInstance(project).allEditors
            for (editor in editors) {
                if (editor is TextEditor) {
                    val e = editor.editor
                    e.inlayModel.getAfterLineEndElementsInRange(
                        0, e.document.textLength,
                        InlineDebugRenderer::class.java
                    ).forEach { inlay ->
                        if (inlay != null) {
                            val renderer = inlay.renderer
                            try {
                                val field = FieldUtils.getField(InlineDebugRenderer::class.java, valueNodeName, true)
                                field.isAccessible = true
                                (field.get(renderer) as? XValueNodeImpl)?.let { valueNode ->
                                    (valueNode.valueContainer as? WeevilJavaValue)?.let { weevilJavaValue ->
                                        if (weevilJavaValue.descriptor is MethodReturnValueDescriptorImpl ||
                                                weevilJavaValue.descriptor is ExpressionValueDescriptorImpl) {
                                            Disposer.dispose(inlay)
                                        }
                                    }
                                }
                            } catch (e: Throwable) {
                                log.info("Exception while accessing field: $valueNodeName")
                            }
                        }
                    }
                }
            }
        }, project.disposed)
    }
}