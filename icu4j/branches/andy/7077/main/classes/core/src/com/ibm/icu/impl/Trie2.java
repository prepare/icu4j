/*
 *******************************************************************************
 * Copyright (C) 2009, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;


/**
 * This is a common implementation of a Unicode trie.
 * It is a kind of compressed, serializable table of 16- or 32-bit values associated with
 * Unicode code points (0..0x10ffff). (A map from code points to integers.)
 *
 * This is the second common version of a Unicode trie (hence the name Trie2).
 */
public class Trie2 implements Iterable<Trie2.EnumRange> {

   /**
    * Selectors for the width of a UTrie2 data value.
    */   
    public enum ValueWidth {
        BITS_16,
        BITS_32
    }
    
    /**
     * When iterating over the contents of a Trie2, Elements of this type are produced.
     * The iterator will return one item for each contiguous range of codepoints  with the same value.  
     * 
     * When iterating, the same Trie2EnumRange object will be reused and returned for each range.
     * If you need to retain the complete iteration results, clone each returned Trie2EnumRange,
     * or save the range in some other way, before advancing to the next iteration step.
     */
    public static class EnumRange {
        public int   startCodePoint;
        public int   endCodePoint;     // Inclusive.
        public int   value;
    }
    
    
    /**
     * When iterating over the contents of a Trie, an instance of TrieValueMapper may
     * be used to remap the values from the Trie.  The remapped values will be used
     * both in determining the ranges of codepoints and as the value to be returned
     * for each range.
     * 
     * Example of use, with an anonymous subclass of TrieValueMapper.
     * 
     * 
     * TrieValueMapper m = new TrieValueMapper() {
     *    int map(int in) {return in & 0x1f;};
     * }
     * for (Iterator<Trie2EnumRange> iter = trie.iterator(m); i.hasNext(); ) {
     *     Trie2EnumRange r = i.next();
     *     ...  // Do something with the range r.
     * }
     *    
     */
    public interface ValueMapper {
        public int  map(int originalVal);
    }
    
    /**
     * Create a frozen trie from its serialized form.
     * Inverse of utrie2_serialize().
     *
     * @param valueBits selects the data entry size; results in an
     *                  TODO:<which> exception if it does not match the serialized form
     * @param data an input stream to the serialized form of a UTrie2.  
     * @param pActualLength receives the actual number of bytes at data taken up by the trie data;
     *                      can be NULL
     * @param pErrorCode an in/out ICU UErrorCode
     * @return the unserialized trie
     *
     * @see utrie2_open
     * @see utrie2_serialize
     */
    public static Trie2  createFromSerialized(ValueWidth valueBits, 
                                InputStream data) 
        throws IOException {
        return null;
    }
    
    
    /**
     * Create a frozen, empty "dummy" trie.
     * A dummy trie is an empty trie, used when a real data trie cannot
     * be loaded. Equivalent to calling utrie2_open() and utrie2_freeze(),
     * but without internally creating and compacting/serializing the
     * builder data structure.
     *
     * The trie always returns the initialValue,
     * or the errorValue for out-of-range code points and illegal UTF-8.
     *
     * @param valueBits selects the data entry size
     * @param initialValue the initial value that is set for all code points
     * @param errorValue the value for out-of-range code points and illegal UTF-8
     * @param pErrorCode an in/out ICU UErrorCode
     * @return the dummy trie
     *
     * @see utrie2_openFromSerialized
     * @see utrie2_open
     */
    public static Trie2 createDummy(ValueWidth valueBits,
                                    int initialValue, 
                                    int errorValue) {
        return null;
    }
    /**
     * Get a value for a code point as stored in the trie.
     *
     * @param trie the trie
     * @param codePoint the code point
     * @return the value
     */
    public int get(int codePoint) {
        return 0;
    }

    
    /**
     *  Return an iterator over the value ranges in this Trie2.
     *  Values from the Trie are not remapped or filtered, but are returned as they
     *  appear in the Trie.
     *  
     *  This 
     * @return
     */
    public Iterator<EnumRange> iterator() {
        return null;
    }
    
    /**
     * Return an iterator over the value ranges from this Trie2.
     * Values from the trie are passed through a caller-supplied remapping function,
     * and it is the remapped values that determine the ranges that
     * are iterated over.
     * 
     * 
     * @param value
     * @return
     */
    public Iterator<EnumRange> iterator(ValueMapper value) {
        return null;
    }
    
    /* Building a trie ---------------------------------------------------------- */

    
    
    /**
     * Create a new, empty, writable Trie2. At build time, 32-bit data values are used.
     * Note:  where to specifiy the data size?  ICU4C UTrie2 does not specify valuebits on create,
     *        but on freeze().  But ICU4J freeze comes from the Freezable interface.
     *        It seems to me that anyone creating a trie will know what size data they
     *        are intending to put into it, so having to know the size upfront shouldn't
     *        be a problem.  Also, knowing in advance allows checking the values as
     *        they are added.
     *
     * @param initialValue the initial value that is set for all code points
     * @param errorValue the value for out-of-range code points and illegal UTF-8
     * @return a pointer to the allocated and initialized new trie
     */
    public static Trie2 create(ValueWidth valueBits, int initialValue, int errorValue) {
        return null;
    }
    
    public Trie2 clone() {
        return null;
    }
    
    /** inherited from Freezable */
    public Trie2 cloneAsThawed() {
        return null;
    }

    /**
     * Set a value for a code point.
     * Throws UnsupportedOperationException if the Trie2 is frozen.
     *
     * @param c the code point
     * @param value the value
     */
    public Trie2 set(int c, int value) {
        return null;
    }
    
    /**
     * Set a value in a range of code points [start..end].
     * All code points c with start<=c<=end will get the value if
     * overwrite is TRUE or if the old value is the initial value.
     * Throws UnsupportedOperationException if the Trie2 is frozen.
     *
     * @param start the first code point to get the value
     * @param end the last code point to get the value (inclusive)
     * @param value the value
     * @param overwrite flag for whether old non-initial values are to be overwritten
     */
     public Trie2 setRange(int start, int end,
                           int value, boolean overwrite) {
    
        return null;
    }
     
    /**
     * Freeze a trie. Make it immutable (read-only), compact it, and
     * optimize it for fast access.
     * Functions to set values will fail after freezing.
     * 
     * TODO:  ICU4C UTrie2::freeze() takes a valueBits parameter, for 16 or 32 bit data size.
     *        Because ICU4J freeze() comes from Freezable, that option is does not fit as
     *        cleanly here..
     *        
     *
     * @see cloneAsThawed
     */
    public Trie2 freeze() {
        return null;
    }
     
     
    /**
     * Test if the Trie2 is frozen. 
     *
     * @param trie the trie
     * @return TRUE if the trie is frozen, that is, immutable, ready for serialization
     *         and for fast access.
     */
    public boolean isFrozen() {
        return false;
    }
    
    
    /**
     * Serialize a frozen trie onto an OutputStream.
     * If the trie is not frozen, then the function throws an UnsupportedOperationException.
     * A trie can be serialized multiple times.
     * The serialized data is compatible with ICU4C UTrie2 serialization.
     * Trie serialization is unrelated to Java object serialization.
     * 
     * TODO:  confusion with Java serialization. Is the name ok?
     *
     * @param os the stream to which the serialized Trie data will be written.
     *           Can be null, in which case the size of the function will return the
     *           size (in bytes) of the serialized data, without attempting to write the data.
     * @return the number of bytes written or needed for the trie
     *
     * @see createFromSerialized()
     */
    public int utrie2_serialize(OutputStream os) throws IOException {
        return 0;
    }

    
    /**
     * Get the UTrie version from an InputStream containing the serialized form
     * of either a Trie (version 1) or a Trie2 (version 2).
     *
     * @param is an InputStream containing the serialized form
     *             of a UTrie, version 1 or 2.  The stream must support mark() and reset().
     *             TODO:  is requiring mark and reset ok?
     *             The position of the input stream will be left unchanged.
     * @param anyEndianOk If FALSE, only big-endian (Java native) serialized forms are recognized.
     *                    If TRUE, little-endian serialized forms are recognized as well.
     *             TODO:  dump this option, always allow either endian?  Or allow only big endian?
     * @return the Trie version of the serialized form, or 0 if it is not
     *         recognized as a serialized UTrie
     */
    public int getVersion(InputStream is, boolean anyEndianOk) {
        return 0;
    }
    
    
    /**
     * Create a Trie2 (version 2) from a Trie (version 1).
     * Enumerates all values in the Trie and builds a Trie2 with the same values.
     * The resulting Trie2 will be frozen.
     *
     * @param trie1 the version 1 Trie to be enumerated
     * @param errorValue the value for out-of-range code points and illegal UTF-8
     * @return The frozen Trie2 with the same values as the source Trie.
     */
    public Trie2 createFromTrie(Trie trie1, int errorValue) {
        return null;
    }
    
    
    /**
     * 
     * @return a Trie that, for frozen Tries, may have slightly faster access times.
     *         The returned Trie will contain identical data.
     *         The returned Trie may be a different Java object.
     *         TODO:  The idea is that freezing a build-time Trie will create
     *           an optimized read-only Trie implementing the common Trie2 interface.
     *           The original object, now frozen, will delegate everything to the
     *           read-only optimized object.  This function will return the
     *           read-only object, bypassing the delegation.  If freeze() weren't
     *           documented as returning the original object, it could just return
     *           the optimized object.  Maybe that would be OK anyhow?
     */
    public Trie2 getOptimizedTrie() {
        return null;        
    }

    
    /**
     * Get the next code point, post-increment the index, and get the value 
     * for the code point from the trie.  This function will correctly handle 
     * UTF-16 input containing supplementary characters.
     * 
     * TODO:  An alternative might be to use a Java iterator.
     *
     * @param src    input text
     * @param index  input text index, in index[0].  In/Out parameter, 
     * @return       the Trie value for the code point at index.
     */
    int nextUTF16(CharSequence src, int[] index) {
        return 0;
    }
    
    
    /**
     * Get the previous code point, pre-decrementing index, and get the value 
     * for the code point from the trie.  This function will correctly handle 
     * UTF-16 input containing supplementary characters.
     * 
     * @param src    input text
     * @param index  input text index, in index[0].  In/Out parameter
     * @return       The Trie value for the code point preceding index.
     */
    int prevUTF16(CharSequence src, int[] index) {
        return 0;
    }
    
    // Note:  ICU4C contains UTrie2 macros for optimized handling of UTF-8 text.
    //    I don't think that this makes sense for Java.  No "ByteSequence"
    //
    
    
    /*
     * The following functions  are used for highly optimized UTF-16
     * text processing. 
     *
     * A Trie2 stores separate values for lead surrogate code _units_ vs. code _points_.
     * UTF-16 text processing can be optimized by detecting surrogate pairs and
     * assembling supplementary code points only when there is non-trivial data
     * available.
     *
     * At build-time, use utrie2_enumForLeadSurrogate() to see if there
     * is non-trivial (non-initialValue) data for any of the supplementary
     * code points associated with a lead surrogate.
     * If so, then set a special (application-specific) value for the
     * lead surrogate code _unit_, with utrie2_set32ForLeadSurrogateCodeUnit().
     *
     * At runtime, use UTRIE2_GET16_FROM_U16_SINGLE_LEAD() or
     * UTRIE2_GET32_FROM_U16_SINGLE_LEAD() per code unit. If there is non-trivial
     * data and the code unit is a lead surrogate, then check if a trail surrogate
     * follows. If so, assemble the supplementary code point with
     * U16_GET_SUPPLEMENTARY() and look up its value with UTRIE2_GET16_FROM_SUPP()
     * or UTRIE2_GET32_FROM_SUPP(); otherwise reset the lead
     * surrogate's value or do a code point lookup for it.
     *
     * If there is only trivial data for lead and trail surrogates, then processing
     * can often skip them. For example, in normalization or case mapping
     * all characters that do not have any mappings are simply copied as is.
     */
     
    
    /**
     * Get a 16-bit trie value from a UTF-16 single/lead code unit (<=U+ffff).
     * Same as UTRIE2_GET16() if c is a BMP code point except for lead surrogates,
     * but faster.
     * 
     * @param trie the trie
     * @param c the code unit (0x0000 .. 0x0000ffff)
     * @return the value
     */
    int getFromU16SingleLead(int c) {
        return 0;
    }
   

    /**
     * Enumerate the trie values for the 1024=0x400 code points
     * corresponding to a given lead surrogate.
     * For example, for the lead surrogate U+D87E it will enumerate the values
     * for [U+2F800..U+2FC00[.
     * Used by data builder code that sets special lead surrogate code unit values
     * for optimized UTF-16 string processing.
     *
     * Do not modify the trie during the enumeration.
     *
     * Except for the limited code point range, this functions just like iterator():
     * For each entry in the trie, the value to be delivered is passed through
     * the UTrie2EnumValue function.
     * The value is unchanged if that function pointer is NULL.
     *
     * For each contiguous range of code points with a given (transformed) value,
     * the UTrie2EnumRange function is called.
     *
     * @param trie a pointer to the trie
     * @param enumValue a pointer to a function that may transform the trie entry value,
     *                  or NULL if the values from the trie are to be used directly
     * @param enumRange a pointer to a function that is called for each contiguous range
     *                  of code points with the same (transformed) value
     * @param context an opaque pointer that is passed on to the callback functions
     */

    public Iterator<EnumRange> iterator(int leadSurrogateValue) {
        return null;
    }

    public Iterator<EnumRange> iterator(int leadSurrogateValue, ValueMapper valueMapper) {
        return null;
    }

}
