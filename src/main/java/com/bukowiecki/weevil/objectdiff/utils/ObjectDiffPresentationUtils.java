/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.weevil.objectdiff.utils;

import com.bukowiecki.weevil.annotator.Colors;
import com.bukowiecki.weevil.bundle.WeevilDebuggerBundle;
import com.bukowiecki.weevil.utils.WeevilDebuggerUtils;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.frame.presentation.XValuePresentation;
import com.intellij.xdebugger.impl.ui.DebuggerUIUtil;
import com.sun.jdi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

/**
 * @author Marcin Bukowiecki
 */
public final class ObjectDiffPresentationUtils {

    public static void renderValue(@Nullable Value thisValue,
                                   @NotNull String thisValueText,
                                   ColoredTextContainer coloredTextContainer,
                                   @Nullable Value otherValue,
                                   @NotNull XValuePresentation.XValueTextRenderer renderer) {

        if (!WeevilDebuggerUtils.INSTANCE.typesSame(thisValue, otherValue)) {
            renderer.renderValue(thisValueText);
            renderer.renderError(WeevilDebuggerBundle.INSTANCE.message("weevil.debugger.objectDiff.differentTypes"));
            return;
        }

        if (thisValue == null && otherValue == null) {
            renderer.renderValue(thisValueText);
            return;
        }

        if (thisValue == null) {
            renderer.renderValue(thisValueText);
            renderer.renderError(WeevilDebuggerBundle.INSTANCE.message("weevil.debugger.objectDiff.differentValue"));
            return;
        }

        if (otherValue == null) {
            renderer.renderValue(thisValueText);
            renderer.renderError(WeevilDebuggerBundle.INSTANCE.message("weevil.debugger.objectDiff.differentValue"));
            return;
        }

        DifferentCharsResult diffResult = new DifferentCharsResult(false, Collections.emptySet());
        if (thisValue instanceof PrimitiveValue) {
            if (thisValue instanceof CharValue) {
                diffResult = ObjectDiffUtils.INSTANCE.getIndexesOfDifferentChars(
                        ((CharValue) thisValue).charValue(),
                        ((CharValue) otherValue).charValue()
                );
            }
            else if (thisValue instanceof BooleanValue) {
                diffResult = ObjectDiffUtils.INSTANCE.getIndexesOfDifferentChars(
                        ((BooleanValue) thisValue).booleanValue(),
                        ((BooleanValue) otherValue).booleanValue()
                );
            }
            else if (thisValue instanceof FloatValue) {
                diffResult = ObjectDiffUtils.INSTANCE.getIndexesOfDifferentChars(
                        ((FloatValue) thisValue).floatValue(),
                        ((FloatValue) otherValue).floatValue()
                );
            }
            else if (thisValue instanceof DoubleValue) {
                diffResult = ObjectDiffUtils.INSTANCE.getIndexesOfDifferentChars(
                        ((DoubleValue) thisValue).doubleValue(),
                        ((DoubleValue) otherValue).doubleValue()
                );
            }
            else if (thisValue instanceof ByteValue) {
                diffResult = ObjectDiffUtils.INSTANCE.getIndexesOfDifferentChars(
                        ((ByteValue) thisValue).byteValue(),
                        ((ByteValue) otherValue).byteValue()
                );
            }
            else if (thisValue instanceof ShortValue) {
                diffResult = ObjectDiffUtils.INSTANCE.getIndexesOfDifferentChars(
                        ((ShortValue) thisValue).shortValue(),
                        ((ShortValue) otherValue).shortValue()
                );
            }
            else if (thisValue instanceof IntegerValue) {
                diffResult = ObjectDiffUtils.INSTANCE.getIndexesOfDifferentChars(
                        ((IntegerValue) thisValue).intValue(),
                        ((IntegerValue) otherValue).intValue()
                );
            }
            else if (thisValue instanceof LongValue) {
                diffResult = ObjectDiffUtils.INSTANCE.getIndexesOfDifferentChars(
                        ((LongValue) thisValue).longValue(),
                        ((LongValue) otherValue).longValue()
                );
            }
            else {
                renderer.renderValue(thisValueText);
            }
        }
        else if (thisValue instanceof ArrayReference) {
            if (((ArrayReference) thisValue).length() != ((ArrayReference) otherValue).length()) {
                renderer.renderValue(thisValueText);
                renderer.renderError(WeevilDebuggerBundle.INSTANCE.message("weevil.debugger.objectDiff.arrayDifferentLength"));
            } else {
                renderer.renderValue(thisValueText);
            }
            return;
        }
        else if (thisValue instanceof ObjectReference){
            renderer.renderValue(thisValueText);
            return;
        }

        if (!diffResult.isDifferent()) {
            renderer.renderValue(thisValueText);
        } else {
            final TextAttributes regularAttributes = SimpleTextAttributes.REGULAR_ATTRIBUTES.toTextAttributes();
            final TextAttributes diffTextAttributes = regularAttributes.clone();
            diffTextAttributes.setForegroundColor(Colors.INSTANCE.getDiffBackgroundColor());
            ObjectDiffPresentationUtils.renderValue(
                    thisValueText,
                    coloredTextContainer,
                    SimpleTextAttributes.REGULAR_ATTRIBUTES,
                    SimpleTextAttributes.fromTextAttributes(diffTextAttributes),
                    -1,
                    null,
                    diffResult.getDifferentIndexes()
            );
            renderer.renderError(WeevilDebuggerBundle.INSTANCE.message("weevil.debugger.objectDiff.differentValue"));
        }
    }

    public static void renderValue(@NotNull String value,
                                   @NotNull ColoredTextContainer text,
                                   @NotNull SimpleTextAttributes attributes,
                                   @NotNull SimpleTextAttributes diffAttributes,
                                   int maxLength,
                                   @Nullable String additionalCharsToEscape,
                                   Set<Integer> differentCharIndexes) {

        SimpleTextAttributes escapeAttributes = null;
        int lastOffset = 0;
        int length = maxLength == -1 ? value.length() : Math.min(value.length(), maxLength);
        for (int i = 0; i < length; i++) {
            char ch = value.charAt(i);
            int additionalCharIndex = -1;
            if (ch == '\n' || ch == '\r' || ch == '\t' || ch == '\b' || ch == '\f'
                    || (additionalCharsToEscape != null && (additionalCharIndex = additionalCharsToEscape.indexOf(ch)) != -1)) {
                if (i > lastOffset) {
                    text.append(value.substring(lastOffset, i), attributes);
                }
                lastOffset = i + 1;

                if (escapeAttributes == null) {
                    TextAttributes fromHighlighter = DebuggerUIUtil.getColorScheme()
                            .getAttributes(DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);

                    if (fromHighlighter != null) {
                        escapeAttributes = SimpleTextAttributes.fromTextAttributes(fromHighlighter);
                    }
                    else {
                        escapeAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, JBColor.GRAY);
                    }
                }

                if (additionalCharIndex == -1) {
                    text.append("\\", escapeAttributes);
                }

                text.append(String.valueOf(getEscapingSymbol(ch)), escapeAttributes);
            }
        }

        if (lastOffset < length) {
            for (int i = lastOffset; i < length; i++) {
                boolean isDifferent = differentCharIndexes.contains(i);
                if (isDifferent) {
                    text.append(String.valueOf(value.charAt(i)), diffAttributes);
                } else {
                    text.append(String.valueOf(value.charAt(i)), attributes);
                }
            }
        }
    }

    private static char getEscapingSymbol(char ch) {
        switch (ch) {
            case '\n': return 'n';
            case '\r': return 'r';
            case '\t': return 't';
            case '\b': return 'b';
            case '\f': return 'f';
            default: return ch;
        }
    }
}
