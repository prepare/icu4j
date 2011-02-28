/*
*******************************************************************************
*   Copyright (C) 2010-2011, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*   created on: 2010aug21
*   created by: Markus W. Scherer
*/

package com.ibm.icu.text;

import java.util.ArrayList;

import com.ibm.icu.impl.PatternProps;
import com.ibm.icu.util.Freezable;

/**
 * Parses and represents ICU MessageFormat patterns.
 * Also handles patterns for ChoiceFormat, PluralFormat and SelectFormat.
 * <p>
 * The parser handles all syntax relevant for identifying message arguments.
 * This includes "complex" arguments whose style strings contain
 * nested MessageFormat pattern substrings.
 * For "simple" arguments (with no nested MessageFormat pattern substrings),
 * the argument style is not parsed any further.
 * <p>
 * Once a pattern has been parsed successfully, iterate through the parsed data
 * with countParts(), getPart() and related methods.
 * <p>
 * The data logically represents a parse tree, but is stored and accessed
 * as a list of "parts" for fast and simple parsing. Arguments and nested messages
 * are best handled via recursion.
 * <p>
 * The parser handles named and numbered message arguments and does not check for consistent style.
 * <p>
 * This class is not intended for public subclassing.
 *
 * @author Markus Scherer
 */
public final class MessagePattern implements Cloneable, Freezable<MessagePattern> {
    /**
     * Constructs an empty MessagePattern.
     */
    public MessagePattern() {}

    /**
     * Constructs a MessagePattern and parses the MessageFormat pattern string.
     * @param pattern a MessageFormat pattern string
     */
    public MessagePattern(String pattern) {
        parse(pattern);
    }

    /**
     * Parses a MessageFormat pattern string.
     * @param pattern a MessageFormat pattern string
     * @return this
     */
    public MessagePattern parse(String pattern) {
        preParse(pattern);
        parseMessage(0, 0, ArgType.NONE);
        postParse();
        return this;
    }

    /**
     * Parses a ChoiceFormat pattern string.
     * @param pattern a ChoiceFormat pattern string
     * @return this
     */
    public MessagePattern parseChoiceStyle(String pattern) {
        preParse(pattern);
        parseChoiceStyle(0, 0);
        postParse();
        return this;
    }

    /**
     * Parses a PluralFormat pattern string.
     * @param pattern a PluralFormat pattern string
     * @return this
     */
    public MessagePattern parsePluralStyle(String pattern) {
        preParse(pattern);
        parsePluralOrSelectStyle(ArgType.PLURAL, 0, 0);
        postParse();
        return this;
    }

    /**
     * Parses a SelectFormat pattern string.
     * @param pattern a SelectFormat pattern string
     * @return this
     */
    public MessagePattern parseSelectStyle(String pattern) {
        preParse(pattern);
        parsePluralOrSelectStyle(ArgType.SELECT, 0, 0);
        postParse();
        return this;
    }

    /**
     * Clears this MessagePattern, returning it to the state after the default constructor.
     */
    public void clear() {
        // Mostly the same as preParse().
        if(isFrozen()) {
            throw new UnsupportedOperationException(
                "Attempt to clear() a frozen MessagePattern instance.");
        }
        msg=null;
        hasArgNames=hasArgNumbers=false;
        needsAutoQuoting=false;
        if(partsList!=null) {
            partsList.clear();
        }
        if(numericValues!=null) {
            numericValues.clear();
        }
    }

    /**
     * @param other another object to compare with.
     * @return true if this object is equivalent to the other one.
     */
    @Override
    public boolean equals(Object other) {
        if(this==other) {
            return true;
        }
        if(other==null || getClass()!=other.getClass()) {
            return false;
        }
        MessagePattern o=(MessagePattern)other;
        if(msg==null) {
            return o.msg==null;
        }
        if(!msg.equals(o.msg)) {
            return false;
        }
        int count=countParts();
        if(count!=o.countParts()) {
            return false;
        }
        for(int i=0; i<count; ++i) {
            if(parts[i]!=o.parts[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the parsed pattern string (null if none was parsed).
     */
    public String getString() {
        return msg;
    }

    /**
     * Does the parsed pattern have named arguments like {first_name}?
     * @return true if the parsed pattern has at least one named argument.
     */
    public boolean hasNamedArguments() {
        return hasArgNames;
    }

    /**
     * Does the parsed pattern have numbered arguments like {2}?
     * @return true if the parsed pattern has at least one numbered argument.
     */
    public boolean hasNumberedArguments() {
        return hasArgNumbers;
    }

    /**
     * Validates and parses an argument name or argument number string.
     * An argument name must be a "pattern identifier", that is, it must contain
     * no Unicode Pattern_Syntax or Pattern_White_Space characters.
     * If it only contains ASCII digits, then it must be a small integer with no leading zero.
     * @param name Input string.
     * @return &gt;=0 if the name is a valid number,
     *         -1 if it is a "pattern identifier" but not all ASCII digits,
     *         -2 if it is neither.
     */
    public static int validateArgumentName(String name) {
        if(!PatternProps.isIdentifier(name)) {
            return -2;
        }
        return parseArgNumber(name, 0, name.length());
    }

    /**
     * Returns a version of the parsed pattern string where each ASCII apostrophe
     * is doubled (escaped) if it is not already, and if it is not interpreted as quoting syntax.
     * <p>
     * For example, this turns "I don't '{know}' {gender,select,female{h''er}other{h'im}}."
     * into "I don''t '{know}' {gender,select,female{h''er}other{h''im}}."
     * @return the deep-auto-quoted version of the parsed pattern string.
     * @see MessageFormat#autoQuoteApostrophe(String)
     */
    public String autoQuoteApostropheDeep() {
        if(!needsAutoQuoting) {
            return msg;
        }
        StringBuilder modified=null;
        Part part=new Part();
        // Iterate backward so that the insertion indexes do not change.
        int count=countParts();
        for(int i=count; i>0;) {
            if(getPart(--i, part).getType()==Part.Type.INSERT_CHAR) {
                if(modified==null) {
                    modified=new StringBuilder(msg.length()+10).append(msg);
                }
                modified.insert(part.getIndex(), (char)part.getValue());
            }
        }
        if(modified==null) {
            return msg;
        } else {
            return modified.toString();
        }
    }

    /**
     * Returns the number of "parts" created by parsing the pattern string.
     * Returns 0 if no pattern has been parsed or clear() was called.
     * @return the number of pattern parts.
     */
    public int countParts() {
        return partsList==null ? 0 : partsList.size();
    }

    private void checkPartIndex(int i, int count) {
        // Check for overflow: It might be parts.length>countParts().
        if(i>=count) {
            throw new IndexOutOfBoundsException();
        }
    }

    private void checkPartIndex(int i) {
        checkPartIndex(i, countParts());
    }

    /**
     * Sets the "part" parameter to the data for the i-th pattern "part".
     * @param i The index of the Part data.
     * @param part The Part object to be modified.
     * @return part
     */
    public Part getPart(int i, Part part) {
        checkPartIndex(i);
        part.part=(int)parts[i];
        return part;
    }

    /**
     * Returns the Part.Type of the i-th pattern "part".
     * Equivalent to getPart(i, part).getType() but without the Part object.
     * @param i The index of the Part data.
     * @return The Part.Type of the i-th Part.
     */
    public Part.Type getPartType(int i) {
        checkPartIndex(i);
        return Part.getType((int)parts[i]);
    }

    /**
     * Returns the pattern index of the specified pattern "part".
     * @param partIndex The index of the Part data.
     * @return The pattern index of this Part.
     */
    public int getPatternIndex(int partIndex) {
        checkPartIndex(partIndex);
        return ((int)parts[partIndex])&Part.INDEX_MASK;
    }

    /**
     * Returns the getString() substring of the pattern string indicated by the Part,
     * or null if the Part does not refer to a substring.
     * @param part a part of this MessagePattern.
     * @return the substring associated with part.
     * @see Part.Type#refersToSubstring()
     */
    public String getSubstring(Part part) {
        if(part.getType().refersToSubstring()) {
            int index=part.getIndex();
            int length=part.getValue();
            return msg.substring(index, index+length);
        } else {
            return null;
        }
    }

    /**
     * Compares the part's substring with the input string s.
     * @param part a part of this MessagePattern.
     * @param s a string.
     * @return true if getSubstring(part).equals(s).
     */
    public boolean partSubstringMatches(Part part, String s) {
        return partSubstringMatches(part.part, s);
    }

    private boolean partSubstringMatches(int part, String s) {
        return
            Part.getType(part).refersToSubstring() &&
            s.regionMatches(0, msg, part&Part.INDEX_MASK, Part.getValue(part));
    }

    /**
     * Returns the numeric value associated with an ARG_INT or ARG_DOUBLE.
     * @param part a part of this MessagePattern.
     * @return the part's numeric value, or NO_NUMERIC_VALUE if this is not a numeric part.
     */
    public double getNumericValue(Part part) {
        return getNumericValue(part.part);
    }

    private double getNumericValue(int part) {
        Part.Type type=Part.getType(part);
        if(type==Part.Type.ARG_INT) {
            return Part.getValue(part);
        } else if(type==Part.Type.ARG_DOUBLE) {
            return numericValues.get(Part.getValue(part));
        } else {
            return NO_NUMERIC_VALUE;
        }
    }

    /**
     * Special value that is returned by getNumericValue(Part) when no
     * numeric value is defined for a part.
     * @see #getNumericValue
     */
    public static final double NO_NUMERIC_VALUE=-123456789;

    /**
     * Returns the "offset:" value of a PluralFormat argument, or 0 if none is specified.
     * @param pluralStart the index of the first PluralFormat argument style part.
     * @return the "offset:" value.
     */
    public double getPluralOffset(int pluralStart) {
        checkPartIndex(pluralStart);
        int part=(int)parts[pluralStart];
        if(Part.getType(part).hasNumericValue()) {
            return getNumericValue(part);
        } else {
            return 0;
        }
    }

    /**
     * Returns the index of the ARG|MSG_LIMIT part corresponding to the ARG|MSG_START at start.
     * @param start The index of some Part data; this Part should be of Type ARG_START or MSG_START.
     * @return The first i>start where getPart(i).getType()==ARG|MSG_LIMIT at the same nesting level,
     *         or start itself if getPartType(msgStart)!=ARG|MSG_START.
     */
    public int getPartLimit(int start) {
        long msgStartPartLong=parts[start];
        int limit=(int)(msgStartPartLong>>32);
        if(limit<start) {
            return start;
        }
        return limit;
    }

    /**
     * Finds the ChoiceFormat sub-message for the given number.
     * @param partIndex the index of the first ChoiceFormat argument style part.
     * @param number a number to be mapped to one of the ChoiceFormat argument's intervals
     * @return the sub-message start part index.
     */
    public int findChoiceSubMessage(int partIndex, double number) {
        int count=countParts();
        checkPartIndex(partIndex, count);
        int msgStart;
        // Iterate over (ARG_INT|DOUBLE, ARG_SELECTOR, message) tuples
        // until ARG_LIMIT or end of choice-only pattern.
        // Ignore the first number and selector and start the loop on the first message.
        partIndex+=2;
        for(;;) {
            // Skip but remember the current sub-message.
            msgStart=partIndex;
            partIndex=getPartLimit(partIndex);
            if(++partIndex>=count) {
                // Reached the end of the choice-only pattern.
                // Return with the last sub-message.
                break;
            }
            int part=(int)parts[partIndex++];
            Part.Type type=Part.getType(part);
            if(type==Part.Type.ARG_LIMIT) {
                // Reached the end of the ChoiceFormat style.
                // Return with the last sub-message.
                break;
            }
            // part is an ARG_INT or ARG_DOUBLE
            assert type.hasNumericValue();
            double boundary=getNumericValue(part);
            // Fetch the ARG_SELECTOR character.
            part=(int)parts[partIndex++];
            char boundaryChar=msg.charAt(part&Part.INDEX_MASK);
            if(boundaryChar=='#' ? number<boundary : number<=boundary) {
                // The number is in the interval between the previous boundary and the current one.
                // Return with the sub-message between them.
                break;
            }
        }
        return msgStart;
    }

    /**
     * Finds the PluralFormat sub-message for the given number, or the "other" sub-message.
     * @param partIndex the index of the first PluralFormat argument style part.
     * @param selector the PluralSelector for mapping the number (minus offset) to a keyword.
     * @param number a number to be matched to one of the PluralFormat argument's explicit values,
     *        or mapped via the PluralSelector.
     * @return the sub-message start part index.
     */
    public int findPluralSubMessage(int partIndex, PluralSelector selector, double number) {
        int count=countParts();
        checkPartIndex(partIndex, count);
        int part=(int)parts[partIndex];
        double offset;
        if(Part.getType(part).hasNumericValue()) {
            offset=getNumericValue(part);
            ++partIndex;
        } else {
            offset=0;
        }
        // The keyword is null until we need to match against non-explicit, not-"other" value.
        // Then we get the keyword from the selector.
        // (In other words, we never call the selector if we match against an explicit value,
        // or if the only non-explicit keyword is "other".)
        String keyword=null;
        // When we find a match, we set msgStart>0 and also set this boolean to true
        // to avoid matching the keyword again (duplicates are allowed)
        // while we continue to look for an explicit-value match.
        boolean haveKeywordMatch=false;
        // msgStart is 0 until we find any appropriate sub-message.
        // We remember the first "other" sub-message if we have not seen any
        // appropriate sub-message before.
        // We remember the first matching-keyword sub-message if we have not seen
        // one of those before.
        // (The parser allows [does not check for] duplicate keywords.
        // We just have to make sure to take the first one.)
        // We avoid matching the keyword twice by also setting haveKeywordMatch=true
        // at the first keyword match.
        // We keep going until we find an explicit-value match or reach the end of the plural style.
        int msgStart=0;
        // Iterate over (ARG_SELECTOR [ARG_INT|ARG_DOUBLE] message) tuples
        // until ARG_LIMIT or end of plural-only pattern.
        do {
            part=(int)parts[partIndex++];
            Part.Type type=Part.getType(part);
            if(type==Part.Type.ARG_LIMIT) {
                break;
            }
            assert type==Part.Type.ARG_SELECTOR;
            // part is an ARG_SELECTOR followed by an optional explicit value, and then a message
            int nextPart=(int)parts[partIndex];
            if(Part.getType(nextPart).hasNumericValue()) {
                // explicit value like "=2"
                ++partIndex;
                if(number==getNumericValue(nextPart)) {
                    // matches explicit value
                    return partIndex;
                }
            } else if(!haveKeywordMatch) {
                // plural keyword like "few" or "other"
                // Compare "other" first and call the selector if this is not "other".
                if(partSubstringMatches(part, "other")) {
                    if(msgStart==0) {
                        msgStart=partIndex;
                        if(keyword!=null && keyword.equals("other")) {
                            // This is the first "other" sub-message,
                            // and the selected keyword is also "other".
                            // Do not match "other" again.
                            haveKeywordMatch=true;
                        }
                    }
                } else {
                    if(keyword==null) {
                        keyword=selector.select(number-offset);
                        if(msgStart!=0 && keyword.equals("other")) {
                            // We have already seen an "other" sub-message.
                            // Do not match "other" again.
                            haveKeywordMatch=true;
                            continue;
                        }
                    }
                    if(partSubstringMatches(part, keyword)) {
                        // keyword matches
                        msgStart=partIndex;
                        // Do not match this keyword again.
                        haveKeywordMatch=true;
                    }
                }
            }
            partIndex=getPartLimit(partIndex);
        } while(++partIndex<count);
        return msgStart;
    }

    /**
     * Finds the SelectFormat sub-message for the given keyword, or the "other" sub-message.
     * @param partIndex the index of the first SelectFormat argument style part.
     * @param keyword a keyword to be matched to one of the SelectFormat argument's keywords.
     * @return the sub-message start part index.
     */
    public int findSelectSubMessage(int partIndex, String keyword) {
        int count=countParts();
        checkPartIndex(partIndex, count);
        int msgStart=0;
        // Iterate over (ARG_SELECTOR, message) pairs until ARG_LIMIT or end of select-only pattern.
        do {
            int part=(int)parts[partIndex++];
            Part.Type type=Part.getType(part);
            if(type==Part.Type.ARG_LIMIT) {
                break;
            }
            assert type==Part.Type.ARG_SELECTOR;
            // part is an ARG_SELECTOR followed by a message
            if(partSubstringMatches(part, keyword)) {
                // keyword matches
                return partIndex;
            } else if(msgStart==0 && partSubstringMatches(part, "other")) {
                msgStart=partIndex;
            }
            partIndex=getPartLimit(partIndex);
        } while(++partIndex<count);
        return msgStart;
    }

    /**
     * A message pattern "part", representing a pattern parsing event.
     * There is a part for the start and end of a message or argument,
     * for quoting and escaping of and with ASCII apostrophes,
     * and for syntax elements of "complex" arguments.
     */
    public static final class Part {
        /**
         * Returns the pattern string index associated with this Part.
         * Typically the index where the part begins, except for "limit" parts
         * where the limit (exclusive-end) index is returned.
         * @return the part index in the pattern string.
         */
        public int getIndex() {
            return part&INDEX_MASK;
        }

        /**
         * Returns type of this part.
         * @return the part type.
         */
        public Type getType() {
            return getType(part);
        }

        /**
         * Returns a value associated with this part.
         * See the documentation of each part type for details.
         * @return the part value.
         */
        public int getValue() {
            return getValue(part);
        }

        /**
         * Returns the argument type if this part is of type ARG_START or ARG_LIMIT,
         * otherwise ArgType.NONE.
         * @return the argument type for this part.
         */
        public ArgType getArgType() {
            Type type=getType();
            if(type==Type.ARG_START || type==Type.ARG_LIMIT) {
                return argTypes[getValue()];
            } else {
                return ArgType.NONE;
            }
        }

        /**
         * Part type constants.
         */
        public enum Type {
            /**
             * Start of a message pattern (main or nested).
             * The value indicates the nesting level, starting with 0 for the main message.
             * <p>
             * There is always a later MSG_LIMIT part.
             */
            MSG_START,
            /**
             * End of a message pattern (main or nested).
             * The value indicates the nesting level, starting with 0 for the main message.
             */
            MSG_LIMIT,
            /**
             * Indicates a substring of the pattern string which is to be skipped when formatting.
             * For example, an apostrophe that begins or ends quoted text
             * would be indicated with such a part.
             * The value provides the length of the substring to be skipped.
             */
            SKIP_SYNTAX(true),
            /**
             * Indicates that a syntax character needs to be inserted for auto-quoting.
             * The value is the character code of the insertion character. (U+0027=APOSTROPHE)
             */
            INSERT_CHAR,
            /**
             * Indicates a syntactic (non-escaped) # symbol in a plural variant.
             * When formatting, replace this with the (value-offset) for the plural argument value.
             * The value provides the length of the substring to be replaced.
             */
            REPLACE_NUMBER(true),
            /**
             * Start of an argument.
             * The value is the ordinal value of the ArgType. Use getArgType().
             */
            ARG_START,
            /**
             * End of an argument.
             * The value is the ordinal value of the ArgType. Use getArgType().
             * <p>
             * This part is followed by either an ARG_NUMBER or ARG_NAME,
             * followed by optional argument sub-parts (see ArgType constants)
             * and finally an ARG_LIMIT part.
             */
            ARG_LIMIT,
            /**
             * The argument number, provided by the value.
             */
            ARG_NUMBER,
            /**
             * The argument name.
             * The value provides the length of the argument name's substring.
             */
            ARG_NAME(true),
            /**
             * The argument type.
             * The value provides the length of the argument type's substring.
             */
            ARG_TYPE(true),
            /**
             * The start of the argument style.
             * The value is undefined and currently always 0.
             */
            ARG_STYLE_START,
            /**
             * A selector substring in a "complex" argument style.
             * The value provides the length of the selector's substring.
             */
            ARG_SELECTOR(true),
            /**
             * An integer value, for example the offset or an explicit selector value
             * in a PluralFormat style.
             * The part value is the integer value.
             */
            ARG_INT,
            /**
             * A numeric value, for example the offset or an explicit selector value
             * in a PluralFormat style.
             * The part value is an index into an internal array of numeric values;
             * use getNumericValue().
             */
            ARG_DOUBLE;

            /**
             * Indicates whether this part refers to a pattern substring.
             * If so, then that substring can be retrieved via {@link MessagePattern#getSubstring(Part)}.
             * @return true if this part refers to a pattern substring.
             */
            public boolean refersToSubstring() {
                return rts;
            }

            /**
             * Indicates whether this part has a numeric value.
             * If so, then that numeric value can be retrieved via {@link MessagePattern#getNumericValue(Part)}.
             * @return true if this part has a numeric value.
             */
            public boolean hasNumericValue() {
                return this==ARG_INT || this==ARG_DOUBLE;
            }

            private Type() {
                rts=false;
            }

            private Type(boolean rts) {
                this.rts=rts;
            }

            private final boolean rts;
        }

        /**
         * @return a string representation of this part.
         */
        @Override
        public String toString() {
            Type type=getType();
            String valueString=(type==Type.ARG_START || type==Type.ARG_LIMIT) ?
                getArgType().name() : Integer.toString(getValue());
            return type.name()+"("+valueString+")@"+getIndex();
        }

        private static Type getType(int part) {
            return types[(part>>TYPE_SHIFT)&TYPE_MASK];
        }

        private static int getValue(int part) {
            return part>>VALUE_SHIFT;
        }

        private int part;  // non-negative

        // Bit fields in the part integer:
        // Bits 31..24: Signed value, meaning depends on type.
        private static final int VALUE_SHIFT=24;
        private static final int MAX_VALUE=0x7f;
        // Bits 23..20: Type
        private static final int TYPE_SHIFT=20;
        private static final int TYPE_MASK=0xf;  // after shifting
        // Bits 19..0: Index into the message string.
        private static final int INDEX_MASK=0xfffff;

        private static final Type[] types=Type.values();
    }

    /**
     * Argument type constants.
     * Returned by Part.getArgType() for ARG_START and ARG_LIMIT parts.
     *
     * Messages nested inside an argument are each delimited by MSG_START and MSG_LIMIT,
     * with a nesting level one greater than the surrounding message.
     */
    public enum ArgType {
        /**
         * The argument has no specified type.
         */
        NONE,
        /**
         * The argument has a "simple" type which is provided by the ARG_TYPE part.
         * An ARG_STYLE part might follow that.
         */
        SIMPLE,
        /**
         * The argument is a ChoiceFormat with one or more (ARG_SELECTOR, message) pairs.
         */
        CHOICE,
        /**
         * The argument is a PluralFormat with an optional ARG_INT or ARG_DOUBLE offset
         * (e.g., offset:1)
         * and one or more (ARG_SELECTOR [explicit-value] message) tuples.
         * If the selector has an explicit value (e.g., =2), then
         * that value is provided by the ARG_INT or ARG_DOUBLE part preceding the message.
         * Otherwise the message immediately follows the ARG_SELECTOR.
         */
        PLURAL,
        /**
         * The argument is a SelectFormat with one or more (ARG_SELECTOR, message) pairs.
         */
        SELECT
    }

    /**
     * Interface for classes like PluralRules which select PluralFormat keywords for numbers.
     */
    public interface PluralSelector {
        /**
         * Given a number, returns the appropriate PluralFormat keyword.
         *
         * @param number The number to be plural-formatted.
         * @return The selected PluralFormat keyword.
         */
        public String select(double number);
    }

    /**
     * Creates and returns a copy of this object.
     * @return a copy of this object (or itself if frozen).
     */
    @Override
    public Object clone() {
        if(isFrozen()) {
            return this;
        } else {
            return cloneAsThawed();
        }
    }

    /**
     * Creates and returns an unfrozen copy of this object.
     * @return a copy of this object.
     */
    @SuppressWarnings("unchecked")
    public MessagePattern cloneAsThawed() {
        MessagePattern newMsg;
        try {
            newMsg=(MessagePattern)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        newMsg.msg=msg;
        newMsg.partsList=(ArrayList<Long>)partsList.clone();
        if(!partsList.isEmpty()) {
            newMsg.parts=new long[partsList.size()];
            System.arraycopy(parts, 0, newMsg.parts, 0, newMsg.parts.length);
        }
        newMsg.hasArgNames=hasArgNames;
        newMsg.hasArgNumbers=hasArgNumbers;
        newMsg.needsAutoQuoting=needsAutoQuoting;
        return newMsg;
    }

    /**
     * Freezes this object, making it immutable and thread-safe.
     * @return this 
     */
    public MessagePattern freeze() {
        frozen=true;
        return this;
    }

    /**
     * Determines whether this object is frozen (immutable) or not.
     * @return true if this object is frozen.
     */
    public boolean isFrozen() {
        return frozen;
    }

    private void preParse(String pattern) {
        if(isFrozen()) {
            throw new UnsupportedOperationException(
                "Attempt to parse(\""+prefix(pattern)+"\") on frozen MessagePattern instance.");
        }
        if(pattern.length()>Part.INDEX_MASK) {
            throw new IndexOutOfBoundsException("Message string \""+prefix(pattern)+"\" too long.");
        }
        msg=pattern;
        hasArgNames=hasArgNumbers=false;
        needsAutoQuoting=false;
        if(partsList==null) {
            partsList=new ArrayList<Long>(pattern.length()/4+2);
        } else {
            partsList.clear();
        }
        if(numericValues!=null) {
            numericValues.clear();
        }
    }

    private void postParse() {
        // Unbox all parts only once.
        // TODO: Should we just use partsList and unbox each item?
        int length=partsList.size();
        if(parts==null) {
            // Normally, allocate tightly. 
            parts=new long[length];
        } else if(parts.length<length) {
            // On the rare occasion when this object is reused, reallocate with more room.
            parts=new long[2*length+10];
        }
        for(int i=0; i<length; ++i) {
            parts[i]=partsList.get(i);
        }
        // TODO: Release memory? Remember int partsLength=partsList.size() and set partsList=null?
    }

    private int parseMessage(int index, int nestingLevel, ArgType parentType) {
        if(nestingLevel>Part.MAX_VALUE) {
            throw new IndexOutOfBoundsException();
        }
        int msgStart=partsList.size();
        addPart(nestingLevel, Part.Type.MSG_START, index);
mainLoop:
        while(index<msg.length()) {
            char c=msg.charAt(index++);
            if(c=='\'') {
                if(index<msg.length()) {
                    c=msg.charAt(index);
                    if(c=='\'') {
                        // double apostrophe, skip the second one
                        addPart(1, Part.Type.SKIP_SYNTAX, index++);
                        continue;
                    } else if(
                        c=='{' || c=='}' ||
                        (parentType==ArgType.CHOICE && c=='|') ||
                        (parentType==ArgType.PLURAL && c=='#')
                    ) {
                        // skip the quote-starting apostrophe
                        addPart(1, Part.Type.SKIP_SYNTAX, index-1);
                        // find the end of the quoted literal text
                        for(;;) {
                            index=msg.indexOf('\'', index+1);
                            if(index>=0) {
                                if((index+1)<msg.length() && msg.charAt(index+1)=='\'') {
                                    // double apostrophe inside quoted literal text
                                    // still encodes a single apostrophe, skip the second one
                                    addPart(1, Part.Type.SKIP_SYNTAX, ++index);
                                } else {
                                    // skip the quote-ending apostrophe
                                    addPart(1, Part.Type.SKIP_SYNTAX, index++);
                                    continue mainLoop;
                                }
                            } else {
                                // quoted text reaches to the end of the of the message
                                index=msg.length();
                                break;
                            }
                        }
                    }  // else interpret the apostrophe as literal text
                }
                // Add a Part for auto-quoting.
                addPart('\'', Part.Type.INSERT_CHAR, index);  // value=char to be inserted
                needsAutoQuoting=true;
            } else if(parentType==ArgType.PLURAL && c=='#') {
                // The unquoted # in a plural message fragment will be replaced
                // with the (number-offset).
                addPart(1, Part.Type.REPLACE_NUMBER, index-1);
            } else if(c=='{') {
                index=parseArg(index, nestingLevel);
            } else if((nestingLevel>0 && c=='}') || (parentType==ArgType.CHOICE && c=='|')) {
                // Finish the message before the terminator.
                addLimitPart(msgStart, nestingLevel, Part.Type.MSG_LIMIT, index-1);
                if(parentType==ArgType.CHOICE) {
                    // Let the choice style parser see the '}' or '|'.
                    return index-1;
                } else {
                    // continue parsing after the '}'
                    return index;
                }
            }  // c is part of literal text
        }
        if(nestingLevel>0 && !inTopLevelChoiceMessage(nestingLevel, parentType)) {
            throw new IllegalArgumentException(
                "Unmatched '{' braces in message \""+prefix()+"\"");
        }
        addLimitPart(msgStart, nestingLevel, Part.Type.MSG_LIMIT, index);
        return index;
    }

    private int parseArg(int index, int nestingLevel) {
        int argStart=partsList.size();
        ArgType argType=ArgType.NONE;
        addPart(argType.ordinal(), Part.Type.ARG_START, index-1);
        int nameIndex=index=skipWhiteSpace(index);
        if(index==msg.length()) {
            throw new IllegalArgumentException(
                "Unmatched '{' braces in message \""+prefix()+"\"");
        }
        // parse argument name or number
        index=skipIdentifier(index);
        int number=parseArgNumber(nameIndex, index);
        if(number>=0) {
            if(number>Part.MAX_VALUE) {
                throw new IndexOutOfBoundsException(
                    "Argument number too large: "+prefix(nameIndex));
            }
            hasArgNumbers=true;
            addPart(number, Part.Type.ARG_NUMBER, nameIndex);
        } else if(number==-1) {
            int length=index-nameIndex;
            if(length>Part.MAX_VALUE) {
                throw new IndexOutOfBoundsException(
                    "Argument name too long: "+prefix(nameIndex));
            }
            hasArgNames=true;
            addPart(length, Part.Type.ARG_NAME, nameIndex);
        } else {  // number<-1
            throw new IllegalArgumentException("Bad argument syntax: "+prefix(nameIndex));
        }
        index=skipWhiteSpace(index);
        if(index==msg.length()) {
            throw new IllegalArgumentException(
                "Unmatched '{' braces in message \""+prefix()+"\"");
        }
        char c=msg.charAt(index++);
        if(c=='}') {
            // all done
        } else if(c!=',') {
            throw new IllegalArgumentException("Bad argument syntax: "+prefix(nameIndex));
        } else /* ',' */ {
            // parse argument type: case-sensitive a-zA-Z
            int typeIndex=index=skipWhiteSpace(index);
            while(index<msg.length() && isArgTypeChar(msg.charAt(index))) {
                ++index;
            }
            int length=index-typeIndex;
            index=skipWhiteSpace(index);
            if(index==msg.length()) {
                throw new IllegalArgumentException(
                    "Unmatched '{' braces in message \""+prefix()+"\"");
            }
            if(length==0 || ((c=msg.charAt(index))!=',' && c!='}')) {
                throw new IllegalArgumentException("Bad argument syntax: "+prefix(nameIndex));
            }
            if(length>Part.MAX_VALUE) {
                throw new IndexOutOfBoundsException(
                    "Argument type name too long: "+prefix(nameIndex));
            }
            argType=ArgType.SIMPLE;
            if(length==6) {
                // case-insensitive comparisons for complex-type names
                if(isChoice(typeIndex)) {
                    argType=ArgType.CHOICE;
                } else if(isPlural(typeIndex)) {
                    argType=ArgType.PLURAL;
                } else if(isSelect(typeIndex)) {
                    argType=ArgType.SELECT;
                }
            }
            // change the ARG_START type from NONE to argType
            long argStartPart=partsList.get(argStart);
            partsList.set(argStart, argStartPart+(argType.ordinal()<<Part.VALUE_SHIFT));
            if(argType==ArgType.SIMPLE) {
                addPart(length, Part.Type.ARG_TYPE, typeIndex);
            }
            // look for an argument style (pattern)
            ++index;
            if(c=='}') {
                if(argType!=ArgType.SIMPLE) {
                    throw new IllegalArgumentException(
                        "No style field for complex argument: "+prefix(nameIndex));
                }
            } else /* ',' */ {
                if(argType==ArgType.SIMPLE) {
                    index=parseSimpleStyle(index);
                } else if(argType==ArgType.CHOICE) {
                    index=parseChoiceStyle(index, nestingLevel);
                } else {
                    index=parsePluralOrSelectStyle(argType, index, nestingLevel);
                }
            }
        }
        addLimitPart(argStart, argType.ordinal(), Part.Type.ARG_LIMIT, index);
        return index;
    }

    private int parseSimpleStyle(int index) {
        int start=index;
        addPart(0, Part.Type.ARG_STYLE_START, index);
        int nestedBraces=0;
        while(index<msg.length()) {
            char c=msg.charAt(index++);
            if(c=='\'') {
                // Treat apostrophe as quoting but include it in the style part.
                // Find the end of the quoted literal text.
                index=msg.indexOf('\'', index);
                if(index<0) {
                    throw new IllegalArgumentException(
                        "Quoted literal argument style text reaches to the end of the message: \""+
                        prefix(start)+"\"");
                }
                // skip the quote-ending apostrophe
                ++index;
            } else if(c=='{') {
                ++nestedBraces;
            } else if(c=='}') {
                if(nestedBraces>0) {
                    --nestedBraces;
                } else {
                    return index;
                }
            }  // c is part of literal text
        }
        throw new IllegalArgumentException(
            "Unmatched '{' braces in message \""+prefix()+"\"");
    }

    private int parseChoiceStyle(int index, int nestingLevel) {
        int start=index;
        index=skipWhiteSpace(index);
        if(index==msg.length() || msg.charAt(index)=='}') {
            throw new IllegalArgumentException(
                "Missing choice argument pattern in \""+prefix()+"\"");
        }
        for(;;) {
            // The choice argument style contains |-separated (number, separator, message) triples.
            // Parse the number.
            int numberIndex=index;
            index=skipDouble(index);
            int length=index-numberIndex;
            if(length==0) {
                throw new IllegalArgumentException("Bad choice pattern syntax: "+prefix(start));
            }
            parseDouble(numberIndex, index, true);  // adds ARG_INT or ARG_DOUBLE
            // Parse the separator.
            index=skipWhiteSpace(index);
            if(index==msg.length()) {
                throw new IllegalArgumentException("Bad choice pattern syntax: "+prefix(start));
            }
            char c=msg.charAt(index++);
            if(!(c=='#' || c=='<' || c=='\u2264')) {  // U+2264 is <=
                throw new IllegalArgumentException(
                    "Expected choice separator (#<\u2264) instead of '"+c+
                    "' in choice pattern "+prefix(start));
            }
            addPart(1, Part.Type.ARG_SELECTOR, index-1);
            // Parse the message fragment.
            index=parseMessage(index, nestingLevel+1, ArgType.CHOICE);
            // parseMessage(..., CHOICE) returns the index of the terminator, or msg.length().
            if(index==msg.length()) {
                return index;
            }
            if(msg.charAt(index++)=='}') {
                if(!inMessageFormatPattern(nestingLevel)) {
                    throw new IllegalArgumentException(
                        "Bad choice pattern syntax: "+prefix(start));
                }
                return index;
            }  // else the terminator is '|'
            index=skipWhiteSpace(index);
        }
    }

    private int parsePluralOrSelectStyle(ArgType argType, int index, int nestingLevel) {
        int start=index;
        boolean isEmpty=true;
        boolean hasOther=false;
        for(;;) {
            // First, collect the selector looking for a small set of terminators.
            // It would be a little faster to consider the syntax of each possible
            // token right here, but that makes the code too complicated.
            index=skipWhiteSpace(index);
            boolean eos=index==msg.length();
            if(eos || msg.charAt(index)=='}') {
                if(eos==inMessageFormatPattern(nestingLevel)) {
                    throw new IllegalArgumentException(
                        "Bad "+
                        (argType==ArgType.PLURAL ? "plural" : "select")+
                        " pattern syntax: "+prefix(start));
                }
                if(!hasOther) {
                    throw new IllegalArgumentException(
                        "Missing 'other' keyword in "+
                        (argType==ArgType.PLURAL ? "plural" : "select")+
                        " pattern in \""+prefix()+"\"");
                }
                return eos ? index: index+1;
            }
            int selectorIndex=index;
            if(argType==ArgType.PLURAL && msg.charAt(selectorIndex)=='=') {
                // explicit-value plural selector: =double
                index=skipDouble(index+1);
                int length=index-selectorIndex;
                if(length==1) {
                    throw new IllegalArgumentException(
                        "Bad "+
                        (argType==ArgType.PLURAL ? "plural" : "select")+
                        " pattern syntax: "+prefix(start));
                }
                if(length>Part.MAX_VALUE) {
                    throw new IndexOutOfBoundsException(
                        "Argument selector too long: "+msg.substring(selectorIndex, index));
                }
                addPart(length, Part.Type.ARG_SELECTOR, selectorIndex);
                parseDouble(selectorIndex+1, index, false);  // adds ARG_INT or ARG_DOUBLE
            } else {
                index=skipIdentifier(index);
                int length=index-selectorIndex;
                if(length==0) {
                    throw new IllegalArgumentException(
                        "Bad "+
                        (argType==ArgType.PLURAL ? "plural" : "select")+
                        " pattern syntax: "+prefix(start));
                }
                if( argType==ArgType.PLURAL && length==6 && index<msg.length() &&
                    msg.regionMatches(selectorIndex, "offset:", 0, 7)
                ) {
                    // plural offset, not a selector
                    if(!isEmpty) {
                        throw new IllegalArgumentException(
                            "Plural argument 'offset:' (if present) must precede key-message pairs: "+
                            prefix(start));
                    }
                    // allow whitespace between offset: and its value
                    int valueIndex=skipWhiteSpace(index+1);  // The ':' is at index.
                    index=skipDouble(valueIndex);
                    if(index==valueIndex) {
                        throw new IllegalArgumentException(
                            "Missing value for plural 'offset:' at "+prefix(start));
                    }
                    parseDouble(valueIndex, index, false);  // adds ARG_INT or ARG_DOUBLE
                    isEmpty=false;
                    continue;  // no message fragment after the offset
                } else {
                    // normal selector word
                    if(length>Part.MAX_VALUE) {
                        throw new IndexOutOfBoundsException(
                            "Argument selector too long: "+msg.substring(selectorIndex, index));
                    }
                    addPart(length, Part.Type.ARG_SELECTOR, selectorIndex);
                    if(msg.regionMatches(selectorIndex, "other", 0, length)) {
                        hasOther=true;
                    }
                }
            }

            // parse the message fragment following the selector
            index=skipWhiteSpace(index);
            if(index==msg.length() || msg.charAt(index++)!='{') {
                throw new IllegalArgumentException(
                    "No message fragment after "+
                    (argType==ArgType.PLURAL ? "plural" : "select")+
                    " selector: "+prefix(selectorIndex));
            }
            index=parseMessage(index, nestingLevel+1, argType);
            isEmpty=false;
        }
    }

    /**
     * Validates and parses an argument name or argument number string.
     * This internal method assumes that the input substring is a "pattern identifier".
     * @param s
     * @param start
     * @param limit
     * @return &gt;=0 if the name is a valid number,
     *         -1 if it is a "pattern identifier" but not all ASCII digits,
     *         -2 if it is neither.
     * @see #validateArgumentName(String)
     */
    private static int parseArgNumber(CharSequence s, int start, int limit) {
        // If the identifier contains only ASCII digits, then it is an argument _number_
        // and must not have leading zeros (except "0" itself).
        // Otherwise it is an argument _name_.
        if(start>=limit) {
            return -2;
        }
        int number;
        // Defer numeric errors until we know there are only digits.
        boolean badNumber;
        char c=s.charAt(start++);
        if(c=='0') {
            if(start==limit) {
                return 0;
            } else {
                number=0;
                badNumber=true;  // leading zero
            }
        } else if('1'<=c && c<='9') {
            number=c-'0';
            badNumber=false;
        } else {
            return -1;
        }
        while(start<limit) {
            c=s.charAt(start++);
            if('0'<=c && c<='9') {
                if(number>=Integer.MAX_VALUE/10) {
                    badNumber=true;  // overflow
                }
                number=number*10+(c-'0');
            } else {
                return -1;
            }
        }
        // There are only ASCII digits.
        if(badNumber) {
            return -2;
        } else {
            return number;
        }
    }

    private int parseArgNumber(int start, int limit) {
        return parseArgNumber(msg, start, limit);
    }

    /**
     * Parses a number from the specified message substring.
     * @param start start index into the message string
     * @param limit limit index into the message string, must be start<limit
     * @param allowInfinity true if U+221E is allowed (for ChoiceFormat)
     */
    private void parseDouble(int start, int limit, boolean allowInfinity) {
        assert start<limit;
        // fake loop for easy exit and single throw statement
        for(;;) {
            // fast path for small integers and infinity
            int value=0;
            int isNegative=0;  // not boolean so that we can easily add it to value
            int index=start;
            char c=msg.charAt(index++);
            if(c=='-') {
                isNegative=1;
                if(index==limit) {
                    break;  // no number
                }
                c=msg.charAt(index++);
            } else if(c=='+') {
                if(index==limit) {
                    break;  // no number
                }
                c=msg.charAt(index++);
            }
            if(c==0x221e) {  // infinity
                if(allowInfinity && index==limit) {
                    addArgDoublePart(
                        isNegative!=0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY,
                        start);
                    return;
                } else {
                    break;
                }
            }
            // try to parse the number as a small integer but fall back to a double
            while('0'<=c && c<='9') {
                value=value*10+(c-'0');
                if(value>(Part.MAX_VALUE+isNegative)) {
                    break;  // not a small-enough integer
                }
                if(index==limit) {
                    addPart(isNegative!=0 ? -value : value, Part.Type.ARG_INT, start);
                    return;
                }
                c=msg.charAt(index++);
            }
            // Let Double.parseDouble() throw a NumberFormatException.
            double numericValue=Double.parseDouble(msg.substring(start, limit));
            addArgDoublePart(numericValue, start);
            return;
        }
        throw new NumberFormatException(
            "Bad syntax for numeric value: "+msg.substring(start, limit));
    }

    private int skipWhiteSpace(int index) {
        return PatternProps.skipWhiteSpace(msg, index);
    }

    private int skipIdentifier(int index) {
        return PatternProps.skipIdentifier(msg, index);
    }

    /**
     * Skips a sequence of characters that could occur in a double value.
     * Does not fully parse or validate the value.
     */
    private int skipDouble(int index) {
        while(index<msg.length()) {
            char c=msg.charAt(index);
            // U+221E: Allow the infinity symbol, for ChoiceFormat patterns.
            if((c<'0' && "+-.".indexOf(c)<0) || (c>'9' && c!='e' && c!='E' && c!=0x221e)) {
                break;
            }
            ++index;
        }
        return index;
    }

    private static boolean isArgTypeChar(int c) {
        return ('a'<=c && c<='z') || ('A'<=c && c<='Z');
    }

    private boolean isChoice(int index) {
        char c;
        return
            ((c=msg.charAt(index++))=='c' || c=='C') &&
            ((c=msg.charAt(index++))=='h' || c=='H') &&
            ((c=msg.charAt(index++))=='o' || c=='O') &&
            ((c=msg.charAt(index++))=='i' || c=='I') &&
            ((c=msg.charAt(index++))=='c' || c=='C') &&
            ((c=msg.charAt(index))=='e' || c=='E');
    }

    private boolean isPlural(int index) {
        char c;
        return
            ((c=msg.charAt(index++))=='p' || c=='P') &&
            ((c=msg.charAt(index++))=='l' || c=='L') &&
            ((c=msg.charAt(index++))=='u' || c=='U') &&
            ((c=msg.charAt(index++))=='r' || c=='R') &&
            ((c=msg.charAt(index++))=='a' || c=='A') &&
            ((c=msg.charAt(index))=='l' || c=='L');
    }

    private boolean isSelect(int index) {
        char c;
        return
            ((c=msg.charAt(index++))=='s' || c=='S') &&
            ((c=msg.charAt(index++))=='e' || c=='E') &&
            ((c=msg.charAt(index++))=='l' || c=='L') &&
            ((c=msg.charAt(index++))=='e' || c=='E') &&
            ((c=msg.charAt(index++))=='c' || c=='C') &&
            ((c=msg.charAt(index))=='t' || c=='T');
    }

    /**
     * @return true if we are inside a MessageFormat (sub-)pattern,
     *         as opposed to inside a top-level choice/plural/select pattern.
     */
    private boolean inMessageFormatPattern(int nestingLevel) {
        return nestingLevel>0 || Part.getType((int)(long)partsList.get(0))==Part.Type.MSG_START;
    }

    /**
     * @return true if we are in a MessageFormat sub-pattern
     *         of a top-level ChoiceFormat pattern.
     */
    private boolean inTopLevelChoiceMessage(int nestingLevel, ArgType parentType) {
        return
            nestingLevel==1 &&
            parentType==ArgType.CHOICE &&
            Part.getType((int)(long)partsList.get(0))!=Part.Type.MSG_START;
    }

    private int makePartInt(int value, Part.Type type, int index) {
        return (value<<Part.VALUE_SHIFT)|(type.ordinal()<<Part.TYPE_SHIFT)|index;
    }

    private void addPart(int value, Part.Type type, int index) {
        partsList.add(makePartInt(value, type, index)&0xffffffffL);
    }

    private void addLimitPart(int start, int value, Part.Type type, int index) {
        setPartLimit(start, partsList.size());
        addPart(value, type, index);
    }

    private void setPartLimit(int start, int limit) {
        long partLong=partsList.get(start);
        partsList.set(start, (partLong&0xffffffffL)|((long)limit<<32));
    }

    private void addArgDoublePart(double numericValue, int start) {
        int numericIndex;
        if(numericValues==null) {
            numericValues=new ArrayList<Double>();
            numericIndex=0;
        } else {
            numericIndex=numericValues.size();
            if(numericIndex>Part.MAX_VALUE) {
                throw new IndexOutOfBoundsException("Too many numeric values");
            }
        }
        numericValues.add(numericValue);
        addPart(numericIndex, Part.Type.ARG_DOUBLE, start);
    }

    private static final int MAX_PREFIX_LENGTH=24;

    /**
     * Returns a prefix of s.substring(start). Used for Exception messages.
     * @param s
     * @param start start index in s
     * @return s.substring(start) or a prefix of that
     */
    private static String prefix(String s, int start) {
        int substringLength=s.length()-start;
        if(substringLength<=MAX_PREFIX_LENGTH) {
            return start==0 ? s : s.substring(start);
        } else {
            StringBuilder prefix=new StringBuilder(MAX_PREFIX_LENGTH);
            prefix.append(s, start, start+MAX_PREFIX_LENGTH-4);
            if(Character.isHighSurrogate(prefix.charAt(MAX_PREFIX_LENGTH-5))) {
                // remove lead surrogate from the end of the prefix
                prefix.setLength(MAX_PREFIX_LENGTH-5);
            }
            return prefix.append(" ...").toString();
        }
    }

    private static String prefix(String s) {
        return prefix(s, 0);
    }

    private String prefix(int start) {
        return prefix(msg, start);
    }

    private String prefix() {
        return prefix(msg, 0);
    }

    private String msg;
    private ArrayList<Long> partsList;  // used while parsing
    private ArrayList<Double> numericValues;
    private long[] parts;  // built at end of parsing
    private boolean hasArgNames;
    private boolean hasArgNumbers;
    private boolean needsAutoQuoting;
    private boolean frozen;

    private static final ArgType[] argTypes=ArgType.values();
}
