/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.listeners

import com.bukowiecki.weevil.debugger.listeners.WeevilDebugSessionListener
import com.bukowiecki.weevil.services.RecursionBreakpointProperties
import com.bukowiecki.weevil.services.WeevilDebuggerService
import com.bukowiecki.weevil.ui.WeevilDebuggerSessionTab
import com.bukowiecki.weevil.utils.WeevilDebuggerDataKey
import com.bukowiecki.weevil.views.WeevilDebuggerEvaluateView
import com.bukowiecki.weevil.views.WeevilDebuggerFutureValuesView
import com.bukowiecki.weevil.xdebugger.breakpoints.RecursionLineBreakpointType
import com.intellij.debugger.engine.JavaDebugProcess
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.Disposer
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.XDebuggerManagerListener
import com.intellij.xdebugger.breakpoints.XBreakpointListener
import com.intellij.xdebugger.impl.XDebugSessionImpl
import com.intellij.xdebugger.impl.breakpoints.XLineBreakpointImpl
import com.intellij.xdebugger.impl.frame.XValueMarkers

/**
 * @author Marcin Bukowiecki
 */
class ProjectOpened : StartupActivity {

    @Suppress("UnstableApiUsage")
    override fun runActivity(project: Project) {
        XDebuggerManager.getInstance(project).getDebugProcesses(JavaDebugProcess::class.java).forEach {
            (it.session as? XDebugSessionImpl)?.let { session ->
                addDebugSessionListener(project, it, session)
            }
        }

        val connection = project.messageBus.connect()
        Disposer.register(project, connection)

        connection.subscribe(
            XDebuggerManager.TOPIC, object : XDebuggerManagerListener {

                override fun processStarted(debugProcess: XDebugProcess) {
                    if (debugProcess is JavaDebugProcess && debugProcess.session is XDebugSessionImpl) {
                        val session = debugProcess.session as XDebugSessionImpl

                        ApplicationManager.getApplication().invokeLater {
                            addDebugSessionListener(project, debugProcess, session)
                        }
                    }
                }

                override fun processStopped(debugProcess: XDebugProcess) {
                    val topic = WeevilDebuggerService.getInstance(project).topic
                    project.messageBus.syncPublisher(topic).processStopped()
                }
            }
        )

        project.messageBus.simpleConnect().subscribe(
            XBreakpointListener.TOPIC, object : XBreakpointListener<XLineBreakpointImpl<*>> {

                override fun breakpointRemoved(breakpoint: XLineBreakpointImpl<*>) {
                    if (breakpoint.type is RecursionLineBreakpointType) {
                        RecursionBreakpointProperties.getInstance(project).removeState(breakpoint)
                    }
                }
            }
        )
    }

    private fun addDebugSessionListener(
        project: Project,
        debugProcess: JavaDebugProcess,
        session: XDebugSessionImpl
    ) {
        val sessionTab = WeevilDebuggerSessionTab(project, debugProcess, session)
        session.sessionData.putUserData(WeevilDebuggerDataKey.weevilDebuggerDataKeys, sessionTab)

        val weevilDebuggerEvaluateView = WeevilDebuggerEvaluateView(
            project,
            sessionTab.controller
        )
        sessionTab.registerEvaluateView(weevilDebuggerEvaluateView)

        val weevilDebuggerFutureValuesView = WeevilDebuggerFutureValuesView(
            project,
            debugProcess.editorsProvider,
            session.valueMarkers as XValueMarkers<*, *>,
            sessionTab.controller
        )
        sessionTab.registerFutureValuesView(weevilDebuggerFutureValuesView)

        session.addSessionListener(WeevilDebugSessionListener(sessionTab))
    }
}