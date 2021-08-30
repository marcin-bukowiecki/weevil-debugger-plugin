/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.xdebugger.breakpoints

import com.bukowiecki.weevil.recursion.utils.WeevilDebuggerRecursionUtils
import com.bukowiecki.weevil.services.RecursionBreakpointProperties
import com.bukowiecki.weevil.services.RecursionBreakpointState
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.ui.breakpoints.LineBreakpoint
import com.intellij.openapi.project.Project
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.sun.jdi.event.LocatableEvent
import org.apache.commons.lang3.Range
import org.jetbrains.java.debugger.breakpoints.properties.JavaLineBreakpointProperties

/**
 * @author Marcin Bukowiecki
 */
class RecursionLineBreakpoint(project: Project, xBreakpoint: XBreakpoint<*>) : LineBreakpoint<JavaLineBreakpointProperties>(project, xBreakpoint) {

    private var cachedState: RecursionBreakpointState? = null

    override fun evaluateCondition(context: EvaluationContextImpl, event: LocatableEvent): Boolean {
        getState()?.let { properties ->
            val recursionExpressionText = properties.iterationExpression

            if (!recursionExpressionText.isNullOrEmpty()) {
                WeevilDebuggerRecursionUtils.parseText(project, recursionExpressionText)?.let { recursionExpression ->
                    if (WeevilDebuggerRecursionUtils.validateExpression(recursionExpression)) {
                        val ranges = WeevilDebuggerRecursionUtils.compile(recursionExpression)
                        return matchesAnyRange(context, ranges) && super.evaluateCondition(context, event)
                    }
                }
            }
        }

        return super.evaluateCondition(context, event)
    }

    private fun getState(): RecursionBreakpointState? {
        if (cachedState == null) {
            val sourcePosition = xBreakpoint.sourcePosition ?: return null
            val file = sourcePosition.file
            val url = file.url
            val line = sourcePosition.line

            val propService = RecursionBreakpointProperties.getInstance(project)
            cachedState = propService.findState(url, line)
        }
        return cachedState
    }

    private fun matchesAnyRange(context: EvaluationContextImpl,
                                ranges: Set<Range<Int>>): Boolean {

        val thread = context.frameProxy?.threadProxy() ?: return false
        val frames = thread.frames()

        val iterator = frames.iterator()
        val frameSlices = mutableListOf<FramesSlice>()

        var currentAccumulator = mutableListOf<StackFrameEntry>()

        while (iterator.hasNext()) {
            val next = iterator.next().stackFrame.location()
            val stackFrame = StackFrameEntry(next.sourceName(), next.method().name(), next.lineNumber())

            if (currentAccumulator.firstOrNull() != null && currentAccumulator.first() == stackFrame) {
                val slice = FramesSlice(currentAccumulator.toList())
                if (frameSlices.lastOrNull() != null) {
                    if (frameSlices.last() != slice) {
                        break
                    }
                }

                frameSlices.add(slice)
                currentAccumulator = mutableListOf()
            }

            currentAccumulator.add(stackFrame)
        }

        if (ranges.any { it.contains(frameSlices.size+1) }) {
            return true
        }

        return false
    }
}

class FramesSlice(val frames: List<StackFrameEntry>) {

    override fun hashCode(): Int {
        return frames.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is FramesSlice) return false
        return other.frames == frames
    }

    override fun toString(): String {
        val result = StringBuilder()
        result.append("[")
        result.append(frames.joinToString(separator = ","))
        result.append("]")
        return result.toString()
    }
}

data class StackFrameEntry(val className: String, val methodName: String, val line: Int)