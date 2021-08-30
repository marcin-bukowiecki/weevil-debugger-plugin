/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.debugger.engine

import com.bukowiecki.weevil.debugger.ui.*
import com.bukowiecki.weevil.psi.CodeEvent
import com.intellij.debugger.SourcePosition
import com.intellij.debugger.engine.SourcePositionProvider
import com.intellij.debugger.impl.DebuggerContextImpl
import com.intellij.debugger.ui.tree.NodeDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

/**
 * @author Marcin Bukowiecki
 */
class WeevilSourcePositionProvider : SourcePositionProvider() {

    override fun computeSourcePosition(
        descriptor: NodeDescriptor,
        project: Project,
        context: DebuggerContextImpl,
        nearest: Boolean
    ): SourcePosition? {
        if (nearest) return null

        if (descriptor is WeevilBaseValueDescriptorImpl) {
            val sourcePositionExtractor = SourcePositionExtractor()
            descriptor.accept(sourcePositionExtractor)
            return sourcePositionExtractor.sourcePosition
        }

        return null
    }
}

/**
 * @author Marcin Bukowiecki
 */
class SourcePositionExtractor : WeevilDescriptorVisitor() {

    private val log = Logger.getInstance(WeevilSourcePositionProvider::class.java)

    var sourcePosition: SourcePosition? = null

    override fun visit(descriptor: MethodReturnValueDescriptorImpl) {
        createSourcePosition(descriptor.codeEvent)
    }

    override fun visit(descriptor: ExpressionValueDescriptorImpl) {
        createSourcePosition(descriptor.codeEvent)
    }

    override fun visit(descriptor: PredictedLocalVariableDescriptorImpl) {
        createSourcePosition(descriptor.codeEvent)
    }

    override fun visit(descriptor: BooleanDescriptorImpl) {
        createSourcePosition(descriptor.codeEvent)
    }

    private fun createSourcePosition(codeEvent: CodeEvent) {
        val containingFile = codeEvent.containingFile
        if (containingFile == null) {
            log.info("Could not compute source position")
            return
        }
        this.sourcePosition = SourcePosition.createFromLine(containingFile, codeEvent.line)
    }
}