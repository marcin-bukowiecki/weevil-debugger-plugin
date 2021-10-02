/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.objectdiff.ui

import com.bukowiecki.weevil.debugger.engine.WeevilJavaValue
import com.bukowiecki.weevil.objectdiff.ObjectDiffService
import com.intellij.debugger.engine.JavaValue
import com.intellij.debugger.engine.SuspendContextImpl
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.engine.events.SuspendContextCommandImpl
import com.intellij.debugger.impl.PrioritizedTask
import com.intellij.debugger.ui.impl.DebuggerTreeRenderer
import com.intellij.debugger.ui.impl.watch.MessageDescriptor
import com.intellij.debugger.ui.impl.watch.NodeManagerImpl
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl
import com.intellij.debugger.ui.tree.DebuggerTreeNode
import com.intellij.debugger.ui.tree.NodeDescriptorFactory
import com.intellij.debugger.ui.tree.NodeManager
import com.intellij.debugger.ui.tree.ValueDescriptor
import com.intellij.debugger.ui.tree.render.ArrayRenderer
import com.intellij.debugger.ui.tree.render.ChildrenBuilder
import com.intellij.debugger.ui.tree.render.NodeRenderer
import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.SimpleTextAttributes
import com.intellij.xdebugger.frame.*
import com.intellij.xdebugger.frame.presentation.XValuePresentation
import javax.swing.Icon


/**
 * @author Marcin Bukowiecki
 */
open class ObjectDiffValue(
    parent: JavaValue?,
    name: String,
    valueDescriptor: ValueDescriptorImpl,
    evaluationContext: EvaluationContextImpl,
    nodeManager: NodeManagerImpl,
    contextSet: Boolean
) : WeevilJavaValue(parent, name, valueDescriptor, evaluationContext, nodeManager, contextSet) {

    override fun computeChildren(node: XCompositeNode) {
        computeChildren(-1, node)
    }

    private fun computeChildren(remainingElements: Int, node: XCompositeNode) {
        val self = this
        scheduleCommand(
            evaluationContext,
            node,
            object : SuspendContextCommandImpl(evaluationContext.suspendContext) {
                override fun getPriority(): PrioritizedTask.Priority {
                    return PrioritizedTask.Priority.NORMAL
                }

                override fun contextAction(suspendContext: SuspendContextImpl) {
                    descriptor.getChildrenRenderer(evaluationContext.debugProcess)
                        .thenAccept { r: NodeRenderer ->
                            r.buildChildren(descriptor.value, object : ChildrenBuilder {
                                override fun getDescriptorManager(): NodeDescriptorFactory {
                                    return self.nodeManager
                                }

                                override fun getNodeManager(): NodeManager {
                                    return self.nodeManager
                                }

                                override fun getParentDescriptor(): ValueDescriptor {
                                    return self.descriptor
                                }

                                override fun initChildrenArrayRenderer(renderer: ArrayRenderer, arrayLength: Int) {
                                    renderer.START_INDEX = 0
                                    if (remainingElements >= 0) {
                                        renderer.START_INDEX = 0.coerceAtLeast(arrayLength - remainingElements)
                                    }
                                }

                                override fun addChildren(nodes: List<DebuggerTreeNode>, last: Boolean) {
                                    var childrenList = XValueChildrenList.EMPTY
                                    if (nodes.isNotEmpty()) {
                                        childrenList = XValueChildrenList(nodes.size)
                                        for (treeNode in nodes) {
                                            val descriptor = treeNode.descriptor
                                            if (descriptor is ValueDescriptorImpl) {
                                                // Value is calculated already in NodeManagerImpl
                                                childrenList.add(
                                                    ObjectDiffValue(
                                                        self,
                                                        descriptor.name,
                                                        descriptor,
                                                        evaluationContext,
                                                        self.nodeManager,
                                                        false
                                                    )
                                                )
                                            } else if (descriptor is MessageDescriptor) {
                                                childrenList.add(
                                                    DummyMessageValueNode(
                                                        descriptor.getLabel(),
                                                        DebuggerTreeRenderer.getDescriptorIcon(descriptor)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    node.addChildren(childrenList, last)
                                }

                                override fun setChildren(nodes: List<DebuggerTreeNode>) {
                                    addChildren(nodes, true)
                                }

                                override fun setMessage(
                                    message: String,
                                    icon: Icon?,
                                    attributes: SimpleTextAttributes,
                                    link: XDebuggerTreeNodeHyperlink?
                                ) {
                                    node.setMessage(message, icon, attributes, link)
                                }

                                override fun addChildren(children: XValueChildrenList, last: Boolean) {
                                    node.addChildren(children, last)
                                }

                                override fun tooManyChildren(remaining: Int) {
                                    node.tooManyChildren(remaining) { computeChildren(remaining, node) }
                                }

                                override fun setAlreadySorted(alreadySorted: Boolean) {
                                    node.setAlreadySorted(alreadySorted)
                                }

                                override fun setErrorMessage(errorMessage: String) {
                                    node.setErrorMessage(errorMessage)
                                }

                                override fun setErrorMessage(errorMessage: String, link: XDebuggerTreeNodeHyperlink?) {
                                    node.setErrorMessage(errorMessage, link)
                                }

                                override fun isObsolete(): Boolean {
                                    return node.isObsolete
                                }
                            }, evaluationContext)
                        }
                }
            })
    }
}

/**
 * @author Marcin Bukowiecki
 */
class ObjectDiffCompareToValue(
    parent: JavaValue?,
    name: String,
    valueDescriptor: ObjectDiffValueDescriptorImpl,
    evaluationContext: EvaluationContextImpl,
    nodeManager: NodeManagerImpl,
    contextSet: Boolean
) : ObjectDiffValue(parent, name, valueDescriptor, evaluationContext, nodeManager, contextSet) {

    override fun computeChildren(node: XCompositeNode) {
        computeChildren(-1, node)
    }

    private fun computeChildren(remainingElements: Int, node: XCompositeNode) {
        val self = this
        scheduleCommand(evaluationContext, node, object : SuspendContextCommandImpl(evaluationContext.suspendContext) {
            override fun getPriority(): PrioritizedTask.Priority {
                return PrioritizedTask.Priority.NORMAL
            }

            override fun contextAction(suspendContext: SuspendContextImpl) {
                val objectDiffService = ObjectDiffService.getInstance()

                descriptor.getChildrenRenderer(evaluationContext.debugProcess)
                    .thenAccept { r: NodeRenderer ->
                        r.buildChildren(descriptor.value, object : ChildrenBuilder {
                            override fun getDescriptorManager(): NodeDescriptorFactory {
                                return self.nodeManager
                            }

                            override fun getNodeManager(): NodeManager {
                                return self.nodeManager
                            }

                            override fun getParentDescriptor(): ValueDescriptor {
                                return self.descriptor
                            }

                            override fun initChildrenArrayRenderer(renderer: ArrayRenderer, arrayLength: Int) {
                                renderer.START_INDEX = 0
                                if (remainingElements >= 0) {
                                    renderer.START_INDEX = 0.coerceAtLeast(arrayLength - remainingElements)
                                }
                            }

                            override fun addChildren(nodes: List<DebuggerTreeNode>, last: Boolean) {
                                var childrenList = XValueChildrenList.EMPTY
                                if (nodes.isNotEmpty()) {
                                    childrenList = XValueChildrenList(nodes.size)
                                    for (treeNode in nodes) {
                                        val descriptor = treeNode.descriptor
                                        if (descriptor is ValueDescriptorImpl) {
                                            descriptor.setContext(evaluationContext)
                                            val value = descriptor.value

                                            val correspondingValue =
                                                objectDiffService.findCorrespondingValue(self, descriptor.name)
                                            val valueToShow = if (correspondingValue == null) {
                                                log.info("corresponding value not found for: ${descriptor.name}")
                                                null
                                            } else {
                                                correspondingValue.value
                                            }

                                            val newDescriptor = CompareToObjectDiffValueDescriptorImpl(
                                                evaluationContext.project,
                                                descriptor.name,
                                                value,
                                                valueToShow,
                                            )

                                            childrenList.add(
                                                ObjectDiffCompareToValue(
                                                    self,
                                                    descriptor.name,
                                                    newDescriptor,
                                                    evaluationContext,
                                                    self.nodeManager,
                                                    false
                                                )
                                            )
                                        } else if (descriptor is MessageDescriptor) {
                                            childrenList.add(
                                                DummyMessageValueNode(
                                                    descriptor.getLabel(),
                                                    DebuggerTreeRenderer.getDescriptorIcon(descriptor)
                                                )
                                            )
                                        }
                                    }
                                }
                                node.addChildren(childrenList, last)
                            }

                            override fun setChildren(nodes: List<DebuggerTreeNode>) {
                                addChildren(nodes, true)
                            }

                            override fun setMessage(
                                message: String,
                                icon: Icon?,
                                attributes: SimpleTextAttributes,
                                link: XDebuggerTreeNodeHyperlink?
                            ) {
                                node.setMessage(message, icon, attributes, link)
                            }

                            override fun addChildren(children: XValueChildrenList, last: Boolean) {
                                node.addChildren(children, last)
                            }

                            override fun tooManyChildren(remaining: Int) {
                                node.tooManyChildren(remaining) { computeChildren(remaining, node) }
                            }

                            override fun setAlreadySorted(alreadySorted: Boolean) {
                                node.setAlreadySorted(alreadySorted)
                            }

                            override fun setErrorMessage(errorMessage: String) {
                                node.setErrorMessage(errorMessage)
                            }

                            override fun setErrorMessage(errorMessage: String, link: XDebuggerTreeNodeHyperlink?) {
                                node.setErrorMessage(errorMessage, link)
                            }

                            override fun isObsolete(): Boolean {
                                return node.isObsolete
                            }
                        }, evaluationContext)
                    }
            }
        })
    }

    companion object {

        private val log = Logger.getInstance(ObjectDiffCompareToValue::class.java)
    }
}

/**
 * @author Marcin Bukowiecki
 */
internal class DummyMessageValueNode(private val myMessage: String, private val myIcon: Icon?) : XNamedValue("") {
    override fun computePresentation(node: XValueNode, place: XValuePlace) {
        node.setPresentation(myIcon, object : XValuePresentation() {
            override fun getSeparator(): String {
                return ""
            }

            override fun renderValue(renderer: XValueTextRenderer) {
                renderer.renderValue(myMessage)
            }
        }, false)
    }

    override fun toString(): String {
        return myMessage
    }
}
