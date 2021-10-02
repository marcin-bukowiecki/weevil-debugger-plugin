/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.objectdiff

import com.bukowiecki.weevil.objectdiff.ui.ObjectDiffValue
import com.intellij.debugger.engine.JavaValue
import com.intellij.debugger.ui.tree.ValueDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.sun.jdi.Field
import com.sun.jdi.ObjectReference
import com.sun.jdi.Value
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

/**
 * @author Marcin Bukowiecki
 */
class ObjectDiffService {

    /**
     * Reference to an object which is the "reference" object during comparison
     */
    private val myObjectToCompare = AtomicReference<WeakReference<ObjectReference>>()

    /**
     * List of objects to compare with the "reference" object
     */
    @Volatile
    private var myObjectsToCompareWith = listOf<WeakReference<ObjectReference>>()

    /**
     * Cached values to get the "reference" object during tree nod expand
     */
    private val cachedValues = ConcurrentHashMap<String /* tree path of node */, WeakReference<ValueDescriptor>>()

    fun findCorrespondingValue(value: ObjectDiffValue, childName: String): ValueDescriptor? {
        val key = LinkedList<String>()
        key.addFirst(childName)
        var current: JavaValue? = value
        while (current != null) {
            key.addFirst(current.name)
            current = current.parent
        }
        return cachedValues[key.joinToString(".")]?.get()
    }

    fun putCachedValue(key: String, correspondingValue: ValueDescriptor) {
        cachedValues[key] = WeakReference(correspondingValue)
    }

    fun clearCachedValues() {
        cachedValues.clear()
    }

    fun prepareContext(objectToCompare: ObjectReference,
                       objectToCompareWith: List<ObjectReference>): ObjectDiffContext {

        val givenType = objectToCompare.referenceType()
        val allFields = givenType.allFields()
        val allFieldNames = allFields.map { it.name() }
        val allFieldValues = allFields.map { objectToCompare.getValue(it) }
        val objectDiffContexts = mutableListOf<ObjectToCompareWithContext>()

        for (objectReference in objectToCompareWith) {
            val objectFields = mutableListOf<ObjectDiffField>()
            objectReference.referenceType().allFields().forEachIndexed { index, field ->
                objectFields.add(ObjectDiffField(allFieldNames[index], objectReference.getValue(field), allFieldValues[index]))
            }
            objectDiffContexts.add(ObjectToCompareWithContext(objectFields.toList()))
        }

        myObjectsToCompareWith = listOf()

        return ObjectDiffContext(
            objectToCompare,
            allFields.groupBy { it.name() },
            objectDiffContexts
        )
    }

    fun setObjectToCompare(objectReference: ObjectReference?) {
        if (objectReference == null) {
            myObjectToCompare.set(null)
            return
        }
        myObjectToCompare.set(WeakReference(objectReference))
    }

    fun addObjectToCompareWith(objectToAdd: ObjectReference) {
        myObjectsToCompareWith = myObjectsToCompareWith + WeakReference(objectToAdd)
    }

    fun getObjectToCompare(): ObjectReference? {
        val weakReference = myObjectToCompare.get() ?: return null
        return weakReference.get()
    }

    fun getObjectsToCompareWith(): List<ObjectReference> {
        return myObjectsToCompareWith.mapNotNull { it.get() }
    }

    fun clearObjectsToCompareWith() {
        myObjectsToCompareWith = emptyList()
    }

    companion object {

        fun getInstance(): ObjectDiffService {
            return ApplicationManager.getApplication().getService(ObjectDiffService::class.java)
        }
    }
}

/**
 * @author Marcin Bukowiecki
 */
data class ObjectDiffContext(val objectToCompare: ObjectReference,
                             val fields: Map<String, List<Field>>,
                             val withContext: List<ObjectToCompareWithContext>)

/**
 * @author Marcin Bukowiecki
 */
data class ObjectToCompareWithContext(val fields: List<ObjectDiffField>)

/**
 * @author Marcin Bukowiecki
 */
data class ObjectDiffField(val name: String, val value: Value?, val toCompareTo: Value?)
