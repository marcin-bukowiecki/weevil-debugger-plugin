/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.actions

import com.intellij.debugger.engine.JavaValue
import com.intellij.debugger.engine.events.DebuggerCommandImpl
import com.intellij.debugger.impl.DebuggerUtilsImpl
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl
import com.jetbrains.jdi.ObjectReferenceImpl
import com.sun.jdi.ObjectReference
import org.apache.commons.lang.mutable.MutableBoolean

/**
 * @author Marcin Bukowiecki
 */
class CleaMapAction : ClearCollectionBase() {

    private val log = Logger.getInstance(CleaMapAction::class.java)

    override fun isEnabled(node: XValueNodeImpl, e: AnActionEvent): Boolean {
        if (super.isEnabled(node, e) && node.valueContainer is JavaValue) {

            val javaValue = node.valueContainer as JavaValue
            val value = javaValue.descriptor.value as? ObjectReferenceImpl ?: return false
            val evaluationContext = javaValue.evaluationContext
            val debugProcess = evaluationContext.debugProcess

            val result = MutableBoolean(false)

            debugProcess.managerThread.invokeAndWait(
                object : DebuggerCommandImpl() {
                    override fun action() {
                        try {
                            val classLoader = evaluationContext.classLoader
                            val collectionType = debugProcess.findClass(evaluationContext, "java.util.Map", classLoader)
                            val isInstanceOf = DebuggerUtilsImpl.instanceOf(
                                (value as ObjectReference).referenceType(),
                                collectionType
                            )
                            result.setValue(isInstanceOf)
                        } catch (e: Exception) {
                            log.info("Could not evaluate to check if node is 'java.util.Map'")
                            result.setValue(false)
                        }
                    }
                }
            )

            return result.booleanValue()
        } else {
            return false
        }
    }
}