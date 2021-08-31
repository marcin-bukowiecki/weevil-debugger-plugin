/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.debugger

import com.bukowiecki.weevil.debugger.controller.EvaluateCallback
import com.bukowiecki.weevil.evaluator.EventsResult
import com.bukowiecki.weevil.evaluator.EventsResultEvaluator
import com.bukowiecki.weevil.evaluator.ThreadResult
import com.bukowiecki.weevil.evaluator.ThreadResultEvaluator
import com.bukowiecki.weevil.psi.CodeEvent
import com.bukowiecki.weevil.psi.CodeEventFactory
import com.bukowiecki.weevil.psi.PsiUtils
import com.bukowiecki.weevil.psi.visitors.CodeEventMarker
import com.bukowiecki.weevil.psi.visitors.JavaCodeGenerator
import com.bukowiecki.weevil.psi.visitors.passes.PassManager
import com.bukowiecki.weevil.settings.WeevilDebuggerSettings
import com.bukowiecki.weevil.utils.WeevilDebuggerUtils
import com.intellij.debugger.engine.JavaDebugProcess
import com.intellij.debugger.engine.JavaValue
import com.intellij.debugger.engine.SuspendContextImpl
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.source.codeStyle.CodeStyleManagerImpl
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.XValue
import com.intellij.xdebugger.impl.XDebugSessionImpl
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl
import com.jetbrains.jdi.ObjectReferenceImpl
import com.sun.jdi.ThreadReference

/**
 * @author Marcin Bukowiecki
 */
class EvaluateEngine(
    private val debugProcess: JavaDebugProcess,
    private val session: XDebugSessionImpl
) {

    @Suppress("UNCHECKED_CAST")
    fun evaluate(weevilContext: WeevilEvaluateContext, customInfo: String, callback: EvaluateCallback) {

        val expr = XExpressionImpl(weevilContext.expression, JavaLanguage.INSTANCE, customInfo)

        debugProcess.evaluator?.evaluate(expr, object : XDebuggerEvaluator.XEvaluationCallback {

            override fun errorOccurred(errorMessage: String) {
                callback.onError(errorMessage)
            }

            override fun evaluated(result: XValue) {
                (result as? JavaValue)?.let {
                    (result.descriptor.value as? ObjectReferenceImpl)?.let { ref ->

                        (session.suspendContext as? SuspendContextImpl)?.let {
                            val evalContext = EvaluationContextImpl(it, it.frameProxy, ref)

                            val eventsResult =
                                EventsResultEvaluator(weevilContext, ref).evaluate(evalContext) as List<EventsResult>
                            val threadsResult =
                                ThreadResultEvaluator(ref).evaluate(evalContext) as List<ThreadResult>

                            weevilContext.eventsResult = eventsResult
                            weevilContext.threadsResult = threadsResult

                            callback.onSuccess(
                                weevilContext,
                                evalContext,
                            )
                        }
                    }
                }
            }
        }, null)
    }
}

/**
 * @author Marcin Bukowiecki
 */
object WeevilEvaluateContextProvider {

    fun setupContext(place: PsiMethod, pivot: PsiElement): WeevilEvaluateContext? {
        val block = PsiUtils.findParent(pivot, PsiCodeBlock::class.java, place) ?: return null
        val documentManager = PsiDocumentManager.getInstance(place.project)
        val document = documentManager.getDocument(place.containingFile) ?: return null
        val settings = WeevilDebuggerSettings.getInstance(place.project)
        val futureLimit = settings.futureEvaluationLimit

        val applyFunctionName = PsiUtils.getUniqueName(place, "applyFunction")
        val eventCollectorName = PsiUtils.getUniqueName(place, "eventCollector")
        val threadCollectorName = PsiUtils.getUniqueName(place, "threadCollector")
        val returnExceptionName = PsiUtils.getUniqueName(place, "returnException")

        var lastBrace: PsiElement? = null
        var furtherExpressions: MutableList<PsiElement> = mutableListOf()
        var start = -1

        for (child in block.children) {
            if (child.startOffset >= pivot.endOffset) {
                if (furtherExpressions.isEmpty()) {
                    start = document.getLineNumber(child.startOffset)
                }
                if (start + futureLimit < document.getLineNumber(child.startOffset)) {
                    break
                }
                furtherExpressions.add(child)
                if (child.text == "}") {
                    lastBrace = child
                }
            }
        }

        if (furtherExpressions.last() == lastBrace) {
            furtherExpressions = furtherExpressions.dropLast(1).toMutableList()
        }

        val compilationContext = CompilationContext(
            timeoutAt = System.nanoTime() + (settings.evaluationTimeout * 1_000_000_000),
            threadCollectorName = threadCollectorName,
            applyFunctionName = applyFunctionName,
            returnExceptionName = returnExceptionName
        )

        val generatedCode = ApplicationManager.getApplication().runWriteAction<String, Throwable> {
            return@runWriteAction CodeGenerator.generateCode(
                place.project,
                furtherExpressions,
                compilationContext
            )
        }

        var finalCode = ""
        finalCode += "Map<Long, Map<Integer, List<List<Object>>>> $eventCollectorName = new ConcurrentHashMap<>();\n"
        finalCode += "Map<Long, Thread> $threadCollectorName = new ConcurrentHashMap<>();\n"
        finalCode += "RuntimeException $returnExceptionName = new RuntimeException(\"WeevilReturn\");\n"
        finalCode += "${getApplyFunction(applyFunctionName, eventCollectorName, threadCollectorName)}\n"
        finalCode += "\n"
        finalCode += "try {\n"
        finalCode += generatedCode
        finalCode += "} catch (Throwable t) { \n if (t != $returnExceptionName) { " +
                "$applyFunctionName.apply(Arrays.asList(${Integer.MAX_VALUE}, t, \"entry\", 0, Thread.currentThread().getId())); " +
                "} }\nArrays.asList($eventCollectorName, $threadCollectorName)"

        return WeevilEvaluateContext(finalCode, place, compilationContext)
    }
}

/**
 * @author Marcin Bukowiecki
 */
object CodeGenerator {

    fun generateCode(
        project: Project,
        furtherExpressions: List<PsiElement>,
        compilationContext: CompilationContext
    ): String {

        val codeEventMarker = CodeEventMarker(compilationContext)
        codeEventMarker.mark(furtherExpressions)

        val copied = furtherExpressions.map { it.copy() }

        PassManager.visit(copied, project, compilationContext)

        val dummyCodeBlock = JavaPsiFacade.getElementFactory(project).createCodeBlock()
        for (psiElement in copied) {
            val lastBodyElement = dummyCodeBlock.lastBodyElement
            if (lastBodyElement == null) {
                dummyCodeBlock.addAfter(psiElement, dummyCodeBlock.firstChild)
            } else {
                dummyCodeBlock.addAfter(psiElement, lastBodyElement)
            }
        }

        val visitor = JavaCodeGenerator(dummyCodeBlock.children.toList(), project, compilationContext)
        visitor.visit()

        val resultPsi =
            JavaPsiFacade.getElementFactory(project).createCodeBlockFromText(dummyCodeBlock.text, null)
        val resultFormatted = CodeStyleManagerImpl.getInstance(project).reformat(resultPsi)

        return resultFormatted.text
    }
}

/**
 * @author Marcin Bukowiecki
 */
data class CompilationContext(
    val timeoutAt: Long,
    val eventRegister: MutableList<CodeEvent> = mutableListOf(),
    val codeEventFactory: CodeEventFactory = CodeEventFactory(),
    val applyFunctionName: String,
    val returnExceptionName: String,
    val threadCollectorName: String,
    var blockCounter: Int = 1
)

/**
 * @author Marcin Bukowiecki
 */
class WeevilEvaluateContext(
    val expression: String,
    val place: PsiMethod,
    val compilationContext: CompilationContext
) {

    var eventsResult = listOf<EventsResult>()
    var threadsResult = listOf<ThreadResult>()

    fun findEventById(id: Int): CodeEvent? {
        val iterator = compilationContext.eventRegister.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.id == id) return next
        }
        return null
    }

    @Suppress("unused")
    fun getMainThread(): ThreadReference? {
        return threadsResult.map { it.threadReference }.firstOrNull { it.name().equals("main") }
    }

    @Suppress("unused")
    fun findThreadDataByReference(ref: ThreadReference, evaluationContext: EvaluationContextImpl): EventsResult? {
        val threadId = WeevilDebuggerUtils.getThreadId(ref, evaluationContext)
        return eventsResult.firstOrNull { it.threadId == threadId }
    }

    fun findThreadDataByThreadId(threadId: Long): EventsResult? {
        return eventsResult.firstOrNull { it.threadId == threadId }
    }

    fun findMainThreadData(): EventsResult? {
        return eventsResult.firstOrNull { it.threadName == "main" }
    }
}

fun getApplyFunction(variableName: String,
                     eventCollectorName: String,
                     threadCollectorName: String): String {

    return """
            Function<List<Object>, Object> $variableName = event -> {
                Thread thread = Thread.currentThread();
                Map<Integer, List<List<Object>>> threadEvents = $eventCollectorName.get(thread.getId());
                if (threadEvents == null) {
                    Map<Integer, List<List<Object>>> eventsMap = new HashMap<Integer, List<List<Object>>>();
                    List<List<Object>> eventList = new ArrayList<List<Object>>();
                    eventList.add(event);
                    eventsMap.put((Integer) event.get(0), eventList);
                    $eventCollectorName.put(thread.getId(), eventsMap);
                } else {
                    List<List<Object>> eventsMap = threadEvents.get((Integer) event.get(0));
                    if (eventsMap == null) {
                        List<List<Object>> eventList = new ArrayList<List<Object>>();
                        eventList.add(event);
                        $eventCollectorName.get(thread.getId()).put((Integer) event.get(0), eventList);
                    } else {
                        eventsMap.add(event);
                    }
                }
                $threadCollectorName.put(thread.getId(), thread);
                return event.get(1);
            };
    """.trimIndent()
}