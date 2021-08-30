/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.xdebugger.breakpoints

import com.intellij.debugger.ui.breakpoints.Breakpoint
import com.intellij.debugger.ui.breakpoints.JavaLineBreakpointType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.intellij.xdebugger.breakpoints.ui.XBreakpointCustomPropertiesPanel
import com.bukowiecki.weevil.recursion.RecursionBreakpointPanel
import com.bukowiecki.weevil.settings.WeevilDebuggerSettings
import org.jetbrains.java.debugger.breakpoints.properties.JavaLineBreakpointProperties

/**
 * @author Marcin Bukowiecki
 */
class RecursionLineBreakpointType : JavaLineBreakpointType(
    "java-recursive-line",
    "Java Recursive Method Line Breakpoint"
) {

    override fun createCustomPropertiesPanel(project: Project): XBreakpointCustomPropertiesPanel<XLineBreakpoint<JavaLineBreakpointProperties>> {
        return RecursionBreakpointPanel(project)
    }

    override fun createJavaBreakpoint(
        project: Project,
        breakpoint: XBreakpoint<JavaLineBreakpointProperties>
    ): Breakpoint<JavaLineBreakpointProperties> {
        return RecursionLineBreakpoint(project, breakpoint)
    }

    override fun createProperties(): JavaLineBreakpointProperties {
        return JavaLineBreakpointProperties()
    }

    override fun createBreakpointProperties(file: VirtualFile, line: Int): JavaLineBreakpointProperties {
        return JavaLineBreakpointProperties()
    }

    override fun canPutAt(file: VirtualFile, line: Int, project: Project): Boolean {
        return WeevilDebuggerSettings.getInstance(project).enableRecursionBreakpoint && super.canPutAt(file, line, project)
    }
}