/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.debugger.ui

/**
 * @author Marcin Bukowiecki
 */
abstract class WeevilDescriptorVisitor {

    open fun visit(descriptor: WeevilBaseValueDescriptorImpl) {

    }

    open fun visit(descriptor: ExceptionDescriptorImpl) {

    }

    open fun visit(descriptor: MethodReturnValueDescriptorImpl) {

    }

    open fun visit(descriptor: ExpressionValueDescriptorImpl) {

    }

    open fun visit(descriptor: PredictedLocalVariableDescriptorImpl) {

    }

    open fun visit(descriptor: BooleanDescriptorImpl) {

    }

    open fun visit(descriptor: IfConditionDescriptorImpl) {

    }
}