/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/text/ReplaceableString.java,v $ 
 * $Date: 2001/12/04 20:09:07 $ 
 * $Revision: 1.7 $
 *
 *****************************************************************************************
 */
package com.ibm.text;

import com.ibm.util.Utility;

/**
 * <code>ReplaceableString</code> is an adapter class that implements the
 * <code>Replaceable</code> API around an ordinary <code>StringBuffer</code>.
 *
 * <p><em>Note:</em> This class does not support attributes and is not
 * intended for general use.  Most clients will need to implement
 * {@link Replaceable} in their text representation class.
 *
 * <p>Copyright &copy; IBM Corporation 1999.  All rights reserved.
 *
 * @see Replaceable
 * @author Alan Liu
 * @version $RCSfile: ReplaceableString.java,v $ $Revision: 1.7 $ $Date: 2001/12/04 20:09:07 $
 */
public class ReplaceableString implements Replaceable {
    private StringBuffer buf;

    private static final String COPYRIGHT =
        "\u00A9 IBM Corporation 1999. All rights reserved.";

    /**
     * Construct a new object with the given initial contents.
     * @param str initial contents
     */
    public ReplaceableString(String str) {
        buf = new StringBuffer(str);
    }

    /**
     * Construct a new object using <code>buf</code> for internal
     * storage.  The contents of <code>buf</code> at the time of
     * construction are used as the initial contents.  <em>Note!
     * Modifications to <code>buf</code> will modify this object, and
     * vice versa.</em>
     * @param buf object to be used as internal storage
     */
    public ReplaceableString(StringBuffer buf) {
        this.buf = buf;
    }

    /**
     * Construct a new empty object.
     */
    public ReplaceableString() {
        buf = new StringBuffer();
    }

    /**
     * Return the contents of this object as a <code>String</code>.
     * @return string contents of this object
     */
    public String toString() {
        return buf.toString();
    }

    /**
     * Return a substring of the given string.
     */
    public String substring(int start, int limit) {
        return buf.substring(start, limit);
    }

    /**
     * Return the number of characters contained in this object.
     * <code>Replaceable</code> API.
     */ 
    public int length() {
        return buf.length();
    }

    /**
     * Return the character at the given position in this object.
     * <code>Replaceable</code> API.
     * @param offset offset into the contents, from 0 to
     * <code>length()</code> - 1
     */
    public char charAt(int offset) {
        return buf.charAt(offset);
    }

    /**
     * Copies characters from this object into the destination
     * character array.  The first character to be copied is at index
     * <code>srcStart</code>; the last character to be copied is at
     * index <code>srcLimit-1</code> (thus the total number of
     * characters to be copied is <code>srcLimit-srcStart</code>). The
     * characters are copied into the subarray of <code>dst</code>
     * starting at index <code>dstStart</code> and ending at index
     * <code>dstStart + (srcLimit-srcStart) - 1</code>.
     *
     * @param srcStart the beginning index to copy, inclusive; <code>0
     * <= start <= limit</code>.
     * @param srcLimit the ending index to copy, exclusive;
     * <code>start <= limit <= length()</code>.
     * @param dst the destination array.
     * @param dstStart the start offset in the destination array.
     */
    public void getChars(int srcStart, int srcLimit, char dst[], int dstStart) {
        Utility.getChars(buf, srcStart, srcLimit, dst, dstStart);
    }

    /**
     * Replace zero or more characters with new characters.
     * <code>Replaceable</code> API.
     * @param start the beginning index, inclusive; <code>0 <= start
     * <= limit</code>.
     * @param limit the ending index, exclusive; <code>start <= limit
     * <= length()</code>.
     * @param text new text to replace characters <code>start</code> to
     * <code>limit - 1</code>
     */
    public void replace(int start, int limit, String text) {
        buf.replace(start, limit, text);
    }

    /**
     * Replace a substring of this object with the given text.
     * @param start the beginning index, inclusive; <code>0 <= start
     * <= limit</code>.
     * @param limit the ending index, exclusive; <code>start <= limit
     * <= length()</code>.
     * @param chars the text to replace characters <code>start</code>
     * to <code>limit - 1</code>
     * @param charsStart the beginning index into <code>chars</code>,
     * inclusive; <code>0 <= start <= limit</code>.
     * @param charsLen the number of characters of <code>chars</code>.
     */
    public void replace(int start, int limit, char[] chars,
                        int charsStart, int charsLen) {
        buf.delete(start, limit);
        buf.insert(start, chars, charsStart, charsLen);
    }

    /**
     * Copy a substring of this object, retaining attribute (out-of-band)
     * information.  This method is used to duplicate or reorder substrings.
     * The destination index must not overlap the source range.
     * 
     * @param start the beginning index, inclusive; <code>0 <= start <=
     * limit</code>.
     * @param limit the ending index, exclusive; <code>start <= limit <=
     * length()</code>.
     * @param dest the destination index.  The characters from
     * <code>start..limit-1</code> will be copied to <code>dest</code>.
     * Implementations of this method may assume that <code>dest <= start ||
     * dest >= limit</code>.
     */
    public void copy(int start, int limit, int dest) {
        if (start == limit && start >= 0 && start <= buf.length()) {
            return;
        }
        char[] text = new char[limit - start];
        getChars(start, limit, text, 0);
        replace(dest, dest, text, 0, limit - start);
    }
}
