package com.bukowiecki.weevil.evaluator

import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.engine.evaluation.expression.IdentityEvaluator
import com.intellij.debugger.engine.evaluation.expression.MethodEvaluator
import com.intellij.debugger.engine.evaluation.expression.UnBoxingEvaluator
import com.sun.jdi.*
import com.sun.jna.IntegerType

/**
 * @author Marcin Bukowiecki
 */
@Suppress("unused")
fun unboxFirstLevel(map: Map<Int, Any?>, evalContext: EvaluationContextImpl, objToString: Boolean = true): Map<Int, Any?> {
    val result = mutableMapOf<Int, Any?>()

    map.entries.forEach {
        val value = it.value
        if (value is ArrayReference) {
            val componentType = (value.type() as ArrayType).componentType()
            val values = value.values
            when (componentType) {
                is BooleanType -> result[it.key] = values.map { v -> unboxBoolean(v as BooleanValue) }.toTypedArray()
                is ByteType -> result[it.key] = values.map { v -> unboxByte(v as ByteValue) }.toTypedArray()
                is CharType -> result[it.key] = values.map { v -> unboxChar(v as CharValue) }.toTypedArray()
                is DoubleType -> result[it.key] = values.map { v -> unboxDouble(v as DoubleValue) }.toTypedArray()
                is FloatType -> result[it.key] = values.map { v -> unboxFloat(v as FloatValue) }.toTypedArray()
                is LongType -> result[it.key] = values.map { v -> unboxLong(v as LongValue) }.toTypedArray()
                is IntegerType -> result[it.key] = values.map { v -> unboxInt(v as IntegerValue) }.toTypedArray()
                is ShortType -> result[it.key] = values.map { v -> unboxShort(v as ShortValue) }.toTypedArray()
                else -> result[it.key] = values.map {
                    v ->
                    if (objToString) {
                        val me = MethodEvaluator(IdentityEvaluator(v), null, "toString", null, arrayOfNulls(0))
                        (me.evaluate(evalContext) as StringReference).value()
                    } else {
                        it
                    }
                }.toTypedArray()
            }
        } else if (value is ObjectReference) {
            if (value is StringReference) {
                result[it.key] = value.value()
            } else {
                val type = value.type()
                if (UnBoxingEvaluator.isTypeUnboxable(type.name())) {
                    when (val unBoxed = UnBoxingEvaluator(IdentityEvaluator(value)).evaluate(evalContext)) {
                        is BooleanValue -> result[it.key] = unBoxed.value()
                        is ByteValue -> result[it.key] = unBoxed.value()
                        is CharValue -> result[it.key] = unBoxed.value()
                        is IntegerValue -> result[it.key] = unBoxed.value()
                        is LongValue -> result[it.key] = unBoxed.value()
                        is FloatValue -> result[it.key] = unBoxed.value()
                        is DoubleValue -> result[it.key] = unBoxed.value()
                        is ShortValue -> result[it.key] = unBoxed.value()
                        else -> throw UnsupportedOperationException(unBoxed.toString())
                    }
                } else {
                    if (objToString) {
                        val me = MethodEvaluator(IdentityEvaluator(value), null, "toString", null, arrayOfNulls(0))
                        result[it.key] = (me.evaluate(evalContext) as StringReference).value()
                    } else {
                        result[it.key] = value
                    }
                }
            }
        }
    }

    return result
}

fun unboxBoolean(value: BooleanValue): Boolean {
    return value.booleanValue()
}

fun unboxInt(value: IntegerValue): Int {
    return value.intValue()
}

fun unboxShort(value: ShortValue): Short {
    return value.shortValue()
}

fun unboxByte(value: ByteValue): Byte {
    return value.byteValue()
}

fun unboxLong(value: LongValue): Long {
    return value.longValue()
}

fun unboxFloat(value: FloatValue): Float {
    return value.floatValue()
}

fun unboxDouble(value: DoubleValue): Double {
    return value.doubleValue()
}

fun unboxChar(value: CharValue): Char {
    return value.charValue()
}