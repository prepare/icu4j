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

import com.ibm.icu.impl.UCharacterProperty;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.util.Freezable;

/**
 * Parses and represents ICU MessageFormat patterns.
 * @author Markus Scherer
 */
public final class MessagePattern implements Cloneable, Freezable<MessagePattern> {
    public MessagePattern() {}

    public MessagePattern(String msg) {
        parse(msg);
    }

    public MessagePattern parse(String msg) {
        if(isFrozen()) {
            throw new UnsupportedOperationException(
                "Attempt to parse(\""+prefix(msg)+"\") on frozen MessagePattern instance.");
        }
        if(msg.length()>=Part.INDEX_MASK) {
            throw new IndexOutOfBoundsException("Message string \""+prefix(msg)+"\" too long.");
        }
        this.msg=msg;
        hasArgNames=hasArgNumbers=false;
        needsAutoQuoting=false;
        if(partsList==null) {
            partsList=new ArrayList<Integer>(msg.length()/4+2);
        } else {
            partsList.clear();
        }
        addPart(0, Part.Type.MSG_START, 0);
        parseMessage(0, 0, ArgType.NONE);
        // Unbox all parts only once.
        // TODO: Should we just use partsList and unbox each item?
        int length=partsList.size();
        if(parts==null || parts.length<length) {
            parts=new int[2*length+10];
        }
        for(int i=0; i<length; ++i) {
            parts[i]=partsList.get(i);
        }
        // TODO: Release memory? Remember int partsLength=partsList.size() and set partsList=null?
        return this;
    }

    public String getString() {
        return msg;
    }

    public boolean hasNamedArguments() {
        return hasArgNames;
    }

    public boolean hasNumberedArguments() {
        return hasArgNumbers;
    }

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

    public int countParts() {
        return partsList.size();
    }

    public Part getPart(int i, Part part) {
        // Check for overflow: It might be parts.length>countParts().
        if(i>=countParts()) {
            throw new IndexOutOfBoundsException();
        }
        part.part=parts[i];
        return part;
    }

    public Part.Type getPartType(int i) {
        if(i>=countParts()) {
            throw new IndexOutOfBoundsException();
        }
        return Part.getType(parts[i]);
    }

    /**
     * Finds the index of the MSG_LIMIT part corresponding to the MSG_START at msgStart.
     * @return the first i>msgStart where getPart(i).getType()==MSG_LIMIT at the same nesting level,
     *         or msgStart itself if getPart(msgStart).getType()!=MSG_START
     */
    public int findMsgLimit(int msgStart) {
        int msgStartPartInt=parts[msgStart];
        if(Part.getType(msgStartPartInt)!=Part.Type.MSG_START) {
            return msgStart;
        }
        int msgLimitPartInt=(msgStartPartInt&~Part.INDEX_MASK)+(1<<Part.TYPE_SHIFT);
        int i=msgStart+1;
        while((parts[i]&~Part.INDEX_MASK)!=msgLimitPartInt) { ++i; }
        return i;
    }

    public static final class Part {
        public int getIndex() {
            return part&INDEX_MASK;
        }

        public Type getType() {
            return getType(part);
        }

        public int getValue() {
            return getValue(part);
        }

        /**
         * @return ArgType for an ARG_START or ARG_LIMIT part; otherwise NONE
         */
        public ArgType getArgType() {
            Type type=getType();
            if(type==Type.ARG_START || type==Type.ARG_LIMIT) {
                return argTypes[getValue()];
            } else {
                return ArgType.NONE;
            }
        }

        public enum Type {
            MSG_START,
            MSG_LIMIT,
            SKIP_SYNTAX(true),
            INSERT_CHAR,
            REPLACE_NUMBER(true),
            ARG_START,
            ARG_LIMIT,
            ARG_NUMBER,
            ARG_NAME(true),
            ARG_TYPE(true),
            ARG_STYLE_START,
            ARG_SELECTOR(true),
            ARG_INT,
            ARG_DOUBLE(true);

            public boolean refersToSubstring() {
                return rts;
            }

            private Type() {
                rts=false;
            }

            private Type(boolean rts) {
                this.rts=rts;
            }

            private final boolean rts;
        }

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

    public enum ArgType {
        NONE, SIMPLE, CHOICE, PLURAL, SELECT
    }

    /**
     * Returns the getString() substring indicated by the Part,
     * or null if the Part does not refer to a substring.
     * @param part Part of this message
     * @return substring associated with part
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

    public boolean partSubstringMatches(Part part, String s) {
        return
            part.getType().refersToSubstring() &&
            s.regionMatches(0, msg, part.getIndex(), part.getValue());
    }

    @Override
    public Object clone() {
        if(isFrozen()) {
            return this;
        } else {
            return cloneAsThawed();
        }
    }

    @SuppressWarnings("unchecked")
    public MessagePattern cloneAsThawed() {
        MessagePattern newMsg;
        try {
            newMsg=(MessagePattern)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        newMsg.msg=msg;
        newMsg.partsList=(ArrayList<Integer>)partsList.clone();
        if(!partsList.isEmpty()) {
            newMsg.parts=new int[partsList.size()];
            System.arraycopy(parts, 0, newMsg.parts, 0, newMsg.parts.length);
        }
        newMsg.hasArgNames=hasArgNames;
        newMsg.hasArgNumbers=hasArgNumbers;
        newMsg.needsAutoQuoting=needsAutoQuoting;
        return newMsg;
    }

    public MessagePattern freeze() {
        frozen=true;
        return this;
    }

    public boolean isFrozen() {
        return frozen;
    }

    private int parseMessage(int index, int nestingLevel, ArgType parentType) {
        if(nestingLevel>Part.MAX_VALUE) {
            throw new IndexOutOfBoundsException();
        }
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
                addPart(ArgType.NONE.ordinal(), Part.Type.ARG_START, index-1);
                index=parseArg(index, nestingLevel);
            } else if((nestingLevel>0 && c=='}') || (parentType==ArgType.CHOICE && c=='|')) {
                // Finish the message before the terminator.
                addPart(nestingLevel, Part.Type.MSG_LIMIT, index-1);
                if(parentType==ArgType.CHOICE) {
                    // Let the choice style parser see the '}' or '|'.
                    return index-1;
                } else {
                    // continue parsing after the '}'
                    return index;
                }
            }  // c is part of literal text
        }
        if(nestingLevel>0) {
            throw new IllegalArgumentException(
                "Unmatched '{' braces in message \""+prefix()+"\"");
        }
        addPart(nestingLevel, Part.Type.MSG_LIMIT, index);
        return index;
    }

    private int parseArg(int index, int nestingLevel) {
        ArgType argType=ArgType.NONE;
        int nameIndex=index=skipWhiteSpace(index);
        if(index==msg.length()) {
            throw new IllegalArgumentException(
                "Unmatched '{' braces in message \""+prefix()+"\"");
        }
        // parse argument name or number
        int c=msg.codePointAt(index);
        if('0'<=c && c<='9') {
            // argument number
            int number=c-'0';
            while(++index<msg.length() && '0'<=(c=msg.charAt(index)) && c<='9') {
                number=(number*10)+(c-'0');
                if(number>Part.MAX_VALUE) {
                    throw new IndexOutOfBoundsException(
                        "Argument number too large: "+prefix(nameIndex));
                }
            }
            hasArgNumbers=true;
            addPart(number, Part.Type.ARG_NUMBER, nameIndex);
        } else if(UCharacter.isUnicodeIdentifierStart(c)) {
            while((index+=Character.charCount(c))<msg.length() &&
                  UCharacter.isUnicodeIdentifierPart(c=msg.codePointAt(index))) {}
            int length=index-nameIndex;
            if(length>Part.MAX_VALUE) {
                throw new IndexOutOfBoundsException(
                    "Argument name too long: "+prefix(nameIndex));
            }
            hasArgNames=true;
            addPart(length, Part.Type.ARG_NAME, nameIndex);
        } else {
            throw new IllegalArgumentException("Bad argument syntax: "+prefix(nameIndex));
        }
        index=skipWhiteSpace(index);
        if(index==msg.length()) {
            throw new IllegalArgumentException(
                "Unmatched '{' braces in message \""+prefix()+"\"");
        }
        c=msg.charAt(index++);
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
            if(length==0 || ((c=msg.charAt(index))!=',') && c!='}') {
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
            // change the ARG_START (second-to-last-added part) type
            // from NONE to argType
            int argStartPos=partsList.size()-2;
            int argStartPart=partsList.get(argStartPos);
            partsList.set(argStartPos, argStartPart+(argType.ordinal()<<Part.VALUE_SHIFT));
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
        addPart(argType.ordinal(), Part.Type.ARG_LIMIT, index);
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
            index=findTokenLimit(index);
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
            addPart(nestingLevel+1, Part.Type.MSG_START, index);
            index=parseMessage(index, nestingLevel+1, ArgType.CHOICE);
            // parseMessage(..., CHOICE) returns the index of the terminator.
            if(msg.charAt(index++)=='}') {
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
            // TODO: verify/align plural & select format selector syntaxes
            // TODO: when porting extended plural syntax to public ICU,
            //       make "offset" case-sensitive and require it to be before all key-message pairs
            // First, collect the selector looking for a small set of terminators.
            // It would be a little faster to consider the syntax of each possible
            // token right here, but that makes the code too complicated.
            index=skipWhiteSpace(index);
            if(index==msg.length()) {
                throw new IllegalArgumentException(
                    "Bad "+
                    (argType==ArgType.PLURAL ? "plural" : "select")+
                    " pattern syntax: "+prefix(start));
            }
            if(msg.charAt(index)=='}') {
                if(!hasOther) {
                    throw new IllegalArgumentException(
                        "Missing 'other' keyword in "+
                        (argType==ArgType.PLURAL ? "plural" : "select")+
                        " pattern in \""+prefix()+"\"");
                }
                return index+1;
            }
            int selectorIndex=index;
            index=findTokenLimit(index);
            int length=index-selectorIndex;
            if(length==0) {
                throw new IllegalArgumentException(
                    "Bad "+
                    (argType==ArgType.PLURAL ? "plural" : "select")+
                    " pattern syntax: "+prefix(start));
            }
            if(argType==ArgType.PLURAL && msg.charAt(selectorIndex)=='=') {
                // explicit-value plural selector: =double
                if(length>Part.MAX_VALUE) {
                    throw new IndexOutOfBoundsException(
                        "Argument selector too long: "+msg.substring(selectorIndex, index));
                }
                addPart(length, Part.Type.ARG_SELECTOR, selectorIndex);
                parseDouble(selectorIndex+1, index, false);  // adds ARG_INT or ARG_DOUBLE
            } else if(argType==ArgType.PLURAL && length>=7 &&
                      msg.regionMatches(selectorIndex, "offset:", 0, 7)) {
                // plural offset, not a selector
                if(!isEmpty) {
                    throw new IllegalArgumentException(
                        "Plural argument 'offset:' (if present) must precede key-message pairs: "+
                        prefix(start));
                }
                int valueIndex=selectorIndex+7;
                if(index==valueIndex) {
                    // allow whitespace between offset: and its value
                    valueIndex=skipWhiteSpace(index);
                    index=findTokenLimit(valueIndex);
                    if(index==valueIndex) {
                        throw new IllegalArgumentException(
                            "Missing value for plural 'offset:' at "+prefix(start));
                    }
                }
                parseDouble(valueIndex, index, false);  // adds ARG_INT or ARG_DOUBLE
                isEmpty=false;
                continue;  // no message fragment after the offset
            } else if(isIdentifier(selectorIndex, index)) {
                // normal selector word
                if(length>Part.MAX_VALUE) {
                    throw new IndexOutOfBoundsException(
                        "Argument selector too long: "+msg.substring(selectorIndex, index));
                }
                addPart(length, Part.Type.ARG_SELECTOR, selectorIndex);
                if(msg.regionMatches(selectorIndex, "other", 0, length)) {
                    hasOther=true;
                }
            } else {
                throw new IllegalArgumentException(
                    "Expected "+
                    (argType==ArgType.PLURAL ? "plural" : "select")+
                    " selector word at "+prefix(selectorIndex));
            }

            // parse the message fragment following the selector
            index=skipWhiteSpace(index);
            if(index==msg.length() || msg.charAt(index++)!='{') {
                throw new IllegalArgumentException(
                    "No message fragment after "+
                    (argType==ArgType.PLURAL ? "plural" : "select")+
                    " selector: "+prefix(selectorIndex));
            }
            addPart(nestingLevel+1, Part.Type.MSG_START, index);
            index=parseMessage(index, nestingLevel+1, argType);
            isEmpty=false;
        }
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
            int length=limit-start;
            // fast path for small integers and infinity
            int value=0;
            int isNegative=0;  // not boolean so that we can easily add it to value
            int index=start;
            char c=msg.charAt(index++);
            if(c=='-') {
                isNegative=1;
                if(index==msg.length()) {
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
                    addPart(length, Part.Type.ARG_DOUBLE, start);
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
            Double.parseDouble(msg.substring(start, limit));
            // TODO: Store the double values? Mapped from Part.getValue()?
            if(length>Part.MAX_VALUE) {
                throw new IndexOutOfBoundsException(
                    "Number too long: "+msg.substring(start, limit));
            }
            addPart(length, Part.Type.ARG_DOUBLE, start);
            return;
        }
        throw new NumberFormatException(
            "Bad syntax for numeric value: "+msg.substring(start, limit));
    }

    private int findTokenLimit(int index) {
        char c;
        while(index<msg.length() &&
              !(" ,{}#<\u2264".indexOf(c=msg.charAt(index))>=0 ||  // U+2264 is <=
                UCharacterProperty.isRuleWhiteSpace(c))) {
            ++index;
        }
        return index;
    }

    private int skipWhiteSpace(int index) {
        while(index<msg.length() && UCharacterProperty.isRuleWhiteSpace(msg.charAt(index))) {
            ++index;
        }
        return index;
    }

    private boolean isIdentifier(int start, int limit) {
        assert start<limit;
        int c=msg.codePointAt(start);
        if(!UCharacter.isUnicodeIdentifierStart(c)) {
            return false;
        }
        while((start+=Character.charCount(c))<limit) {
            if(!UCharacter.isUnicodeIdentifierPart(c=msg.codePointAt(start))) {
                return false;
            }
        }
        return true;
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

    private int makePartInt(int value, Part.Type type, int index) {
        return (value<<Part.VALUE_SHIFT)|(type.ordinal()<<Part.TYPE_SHIFT)|index;
    }

    private void addPart(int value, Part.Type type, int index) {
        partsList.add(makePartInt(value, type, index));
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
    private ArrayList<Integer> partsList;  // used while parsing
    private int[] parts;  // built at end of parsing
    private boolean hasArgNames;
    private boolean hasArgNumbers;
    private boolean needsAutoQuoting;
    private boolean frozen;

    private static final ArgType[] argTypes=ArgType.values();
}
