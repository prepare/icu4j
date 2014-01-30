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
    public static SimplePatternFormatter compile(String pattern) {
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
    
    /**
     * Formats the given values.
     */
    public String format(String... values) {
        return format(new StringBuilder(), null, values).toString();
    }

    /**
     * Formats the given values.
     * 
     * @param appendTo the result appended here.
     * @param offsets position of first value in result stored in offfsets[0];
     *   second in offsets[1]; third in offsets[2] etc.
     * @param values the values
     * @return appendTo
     */
    public StringBuilder format(
            StringBuilder appendTo, int[] offsets, String... values) {
        if (values.length < placeholderCount) {
            throw new IllegalArgumentException("Too few values.");
        }
        int offsetLen = offsets == null ? 0 : offsets.length;
        for (int i = 0; i < offsetLen; i++) {
            offsets[i] = -1;
        }
        if (placeholderIdsOrderedByOffset.length == 0) {
            appendTo.append(patternWithoutPlaceholders);
            return appendTo;
        }
        appendTo.append(
                patternWithoutPlaceholders,
                0,
                placeholderIdsOrderedByOffset[0]);
        setPlaceholderOffset(
                placeholderIdsOrderedByOffset[1],
                appendTo.length(),
                offsets,
                offsetLen);
        appendTo.append(values[placeholderIdsOrderedByOffset[1]]);
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
}
