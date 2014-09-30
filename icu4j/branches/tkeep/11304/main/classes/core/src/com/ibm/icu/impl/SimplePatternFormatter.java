/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Compiled version of a pattern such as "{1} was born in {0}".
 * <p>
 * Using SimplePatternFormatter objects is both faster and safer than adhoc replacement 
 * such as <code>pattern.replace("{0}", "Colorado").replace("{1} "Fred");</code>.
 * They are faster because they are precompiled; they are safer because they
 * account for curly braces escaped by apostrophe (').
 * 
 * Placeholders are of the form \{[0-9]+\}. If a curly brace is preceded
 * by a single quote, it becomes a curly brace instead of the start of a
 * placeholder. Two single quotes resolve to one single quote. 
 * <p>
 * SimplePatternFormatter objects are immutable and can be safely cached like strings.
 * <p>
 * Example:
 * <pre>
 * SimplePatternFormatter fmt = SimplePatternFormatter.compile("{1} '{born} in {0}");
 * 
 * // Output: "paul {born} in england"
 * System.out.println(fmt.format("england", "paul"));
 * </pre>
 */
public class SimplePatternFormatter {
    private final String patternWithoutPlaceholders;
    private final int placeholderCount;
    
    // [0] first offset; [1] first placeholderId; [2] second offset;
    // [3] second placeholderId etc.
    private final int[] placeholderIdsOrderedByOffset;

    private SimplePatternFormatter(String pattern, PlaceholdersBuilder builder) {
        this.patternWithoutPlaceholders = pattern;
        this.placeholderIdsOrderedByOffset =
                builder.getPlaceholderIdsOrderedByOffset();
        this.placeholderCount = builder.getPlaceholderCount();
    }

    /**
     * Compiles a string.
     * @param pattern The string.
     * @return the new SimplePatternFormatter object.
     */
    public static SimplePatternFormatter compile(CharSequence pattern) {
        PlaceholdersBuilder placeholdersBuilder = new PlaceholdersBuilder();
        PlaceholderIdBuilder idBuilder =  new PlaceholderIdBuilder();
        StringBuilder newPattern = new StringBuilder();
        State state = State.INIT;
        for (int i = 0; i < pattern.length(); i++) {
            char ch = pattern.charAt(i);
            switch (state) {
            case INIT:
                if (ch == 0x27) {
                    state = State.APOSTROPHE;
                } else if (ch == '{') {
                    state = State.PLACEHOLDER;
                    idBuilder.reset();
                } else {
                    newPattern.append(ch);
                }
                break;
            case APOSTROPHE:
                if (ch == 0x27) {
                    newPattern.append("'");
                } else if (ch == '{') {
                    newPattern.append("{");
                } else {
                    newPattern.append("'");
                    newPattern.append(ch);
                }
                state = State.INIT;
                break;
            case PLACEHOLDER:
                if (ch >= '0' && ch <= '9') {
                    idBuilder.add(ch);
                } else if (ch == '}' && idBuilder.isValid()) {
                    placeholdersBuilder.add(idBuilder.getId(), newPattern.length());
                    state = State.INIT;
                } else {
                    newPattern.append('{');
                    idBuilder.appendTo(newPattern);
                    newPattern.append(ch);
                    state = State.INIT;
                }
                break;
            default:
                throw new IllegalStateException();
            }
        }
        switch (state) {
        case INIT:
            break;
        case APOSTROPHE:
            newPattern.append("'");
            break;
        case PLACEHOLDER:
            newPattern.append('{');
            idBuilder.appendTo(newPattern);
            break;
        default:
            throw new IllegalStateException();
        }
        return new SimplePatternFormatter(newPattern.toString(), placeholdersBuilder);
        
    }
      
    /**
     * Returns the max placeholder ID + 1.
     */
    public int getPlaceholderCount() {
        return placeholderCount;
    }
    
    private int getPlaceholderAtStart() {
        if (placeholderIdsOrderedByOffset.length == 0 || placeholderIdsOrderedByOffset[0] != 0) {
            return -1;
        }
        return placeholderIdsOrderedByOffset[1];
    }
    
    /**
     * Formats the given values.
     */
    public String format(CharSequence... values) {
        return format(new StringBuilder(), null, values).toString();
    }

    /**
     * Formats the given values.
     * 
     * @param appendTo the result appended here. 
     * @param offsets position of first value in appendTo stored in offsets[0];
     *   second in offsets[1]; third in offsets[2] etc. An offset of -1 means that the
     *   corresponding value is not in appendTo. offsets.length and values.length may
     *   differ. If offsets.length < values.length then only the first offsets are written out;
     *   If offsets.length > values.length then the extra offsets get -1.
     *   If caller is not interested in offsets, caller may pass null here.
     * @param values the values. Must be non-null and cannot be appendTo.
     * @return appendTo
     */
    public StringBuilder format(
            StringBuilder appendTo, int[] offsets, CharSequence... values) {
        if (values.length < placeholderCount) {
            throw new IllegalArgumentException("Too few values.");
        }
        for (int i = 0; i < values.length; i++) {
            if (values[i] == appendTo) {
                throw new IllegalArgumentException("Can't use appendTo buffer as a placeholder value");
            }
            if (values[i] == null) {
                throw new IllegalArgumentException("Can't use null as a placeholder value.");
            }
        }
        int offsetLen = offsets == null ? 0 : offsets.length;
        for (int i = 0; i < offsetLen; i++) {
            offsets[i] = -1;
        }
        return formatFrom(appendTo, true, offsets, offsetLen, values);
    }
    
    /**
     * Formats the given values in place.
     * 
     * @param inPlaceResult previous value replaced with formatted value. 
     * @param offsets position of first value in appendTo stored in offsets[0];
     *   second in offsets[1]; third in offsets[2] etc. An offset of -1 means that the
     *   corresponding value is not in appendTo. offsets.length and values.length may
     *   differ. If offsets.length < values.length then only the first offsets are written out;
     *   If offsets.length > values.length then the extra offsets get -1.
     *   If caller is not interested in offsets, caller may pass null here.
     * @param values the values. Cannot be inPlaceResult. A null value, means the previous value of
     *    inPlaceResult
     * @return inPlaceResult
     */
    public StringBuilder formatInPlace(
            StringBuilder inPlaceResult, int[] offsets, CharSequence... values) {
        boolean needToCopyValues = false;
        boolean canOptimize = false;
        int placeholderAtStart = getPlaceholderAtStart();
        for (int i = 0; i < values.length; i++) {
            if (inPlaceResult == values[i]) {
                throw new IllegalArgumentException("Can't use inPlaceResult buffer as a placeholder value");
            }
            if (values[i] == null) {
                if (i != placeholderAtStart) {
                    needToCopyValues = true;
                } else {
                    canOptimize = true;
                }
            }
        }
        CharSequence[] valuesCopy = values;
        if (needToCopyValues) {
            valuesCopy = new CharSequence[values.length];
            for (int i = 0; i < valuesCopy.length; i++) {
                if (canOptimize && i == placeholderAtStart) {
                    // When optimizing, we can ignore the NULL value for placeholderAtStart placeholder.
                    continue;
                }
                valuesCopy[i] = values[i] == null ? inPlaceResult.toString() : values[i];
            }
        }
        int offsetLen = offsets == null ? 0 : offsets.length;
        for (int i = 0; i < offsetLen; i++) {
            offsets[i] = -1;
        }
        if (canOptimize) {
            // Start placeholder is at position 0.
            setPlaceholderOffset(
                    placeholderAtStart,
                    0,
                    offsets,
                    offsetLen);
            // Format everything after first placeholder
            return formatFrom(inPlaceResult, false, offsets, offsetLen, valuesCopy);
        }
        // Clear appendTo
        inPlaceResult.setLength(0);
        return formatFrom(inPlaceResult, true, offsets, offsetLen, valuesCopy);
    }
    
    /**
     * Formats this object using values {0}, {1} etc. Note that this is
     * not the same as the original pattern string used to build this object.
     */
    @Override
    public String toString() {
        String[] values = new String[this.getPlaceholderCount()];
        for (int i = 0; i < values.length; i++) {
            values[i] = String.format("{%d}", i);
        }
        return format(new StringBuilder(), null, values).toString();
    }
    
    public StringBuilder formatFrom(
            StringBuilder appendTo,
            boolean includeFirstPlaceholder,
            int[] offsets,
            int offsetLen,
            CharSequence... values) {
        if (placeholderIdsOrderedByOffset.length == 0) {
            appendTo.append(patternWithoutPlaceholders);
            return appendTo;
        }
        appendTo.append(
                patternWithoutPlaceholders,
                0,
                placeholderIdsOrderedByOffset[0]);
        if (includeFirstPlaceholder) {
            setPlaceholderOffset(
                    placeholderIdsOrderedByOffset[1],
                    appendTo.length(),
                    offsets,
                    offsetLen);
            appendTo.append(values[placeholderIdsOrderedByOffset[1]]);
        }
        for (int i = 2; i < placeholderIdsOrderedByOffset.length; i += 2) {
            appendTo.append(
                    patternWithoutPlaceholders,
                    placeholderIdsOrderedByOffset[i - 2],
                    placeholderIdsOrderedByOffset[i]);
            setPlaceholderOffset(
                    placeholderIdsOrderedByOffset[i + 1],
                    appendTo.length(),
                    offsets,
                    offsetLen);
            appendTo.append(values[placeholderIdsOrderedByOffset[i + 1]]);
        }
        appendTo.append(
                patternWithoutPlaceholders,
                placeholderIdsOrderedByOffset[placeholderIdsOrderedByOffset.length - 2],
                patternWithoutPlaceholders.length());
        return appendTo;
    }
    
    private static void setPlaceholderOffset(
            int placeholderId, int offset, int[] offsets, int offsetLen) {
        if (placeholderId < offsetLen) {
            offsets[placeholderId] = offset;
        }
    }

    private static enum State {
        INIT,
        APOSTROPHE,
        PLACEHOLDER,
    }
    
    private static class PlaceholderIdBuilder {
        private int id = 0;
        private int idLen = 0;
        
        public void reset() {
            id = 0;
            idLen = 0;
        }

        public int getId() {
           return id;
        }

        public void appendTo(StringBuilder appendTo) {
            if (idLen > 0) {
                appendTo.append(id);
            }
        }

        public boolean isValid() {
           return idLen > 0;
        }

        public void add(char ch) {
            id = id * 10 + ch - '0';
            idLen++;
        }     
    }
    
    private static class PlaceholdersBuilder {
        private List<Integer> placeholderIdsOrderedByOffset = new ArrayList<Integer>();
        private int placeholderCount = 0;
        
        public void add(int placeholderId, int offset) {
            placeholderIdsOrderedByOffset.add(offset);
            placeholderIdsOrderedByOffset.add(placeholderId);
            if (placeholderId >= placeholderCount) {
                placeholderCount = placeholderId + 1;
            }
        }
        
        public int getPlaceholderCount() {
            return placeholderCount;
        }
        
        public int[] getPlaceholderIdsOrderedByOffset() {
            int[] result = new int[placeholderIdsOrderedByOffset.size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = placeholderIdsOrderedByOffset.get(i).intValue();
            }
            return result;
        }
    }

    /**
     * Returns this pattern with none of the placeholders.
     */
    public String getPatternWithNoPlaceholders() {
        return patternWithoutPlaceholders;
    }

   
}
