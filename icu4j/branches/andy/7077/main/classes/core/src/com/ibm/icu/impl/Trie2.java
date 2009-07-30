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
public abstract class Trie2 implements Iterable<Trie2.EnumRange> {

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
     * be loaded. 
     *
     * The trie always returns the initialValue,
     * or the errorValue for out-of-range code points.
     *
     * @param valueBits selects the data entry size
     * @param initialValue the initial value that is set for all code points
     * @param errorValue the value for out-of-range code points and illegal UTF-8
     * @return the dummy trie
     *
     */
    public static Trie2 createDummy(ValueWidth valueBits,
                                    int initialValue, 
                                    int errorValue) {
        return null;
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
    public static int getVersion(InputStream is, boolean anyEndianOk) {
        return 0;
    }
    
    
    /**
     * Create a Trie2 (version 2) from a Trie (version 1).
     * Enumerates all values in the Trie and builds a Trie2 with the same values.
     * The resulting Trie2 will be frozen, of type Trie2_16 or Trie2_32, depending
     * on the width of the source Trie.
     *
     * @param trie1 the version 1 Trie to be enumerated
     * @param errorValue the value for out-of-range code points and illegal UTF-8
     * @return The frozen Trie2 with the same values as the source Trie.
     */
    public static Trie2 createFromTrie(Trie trie1, int errorValue) {
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
    
    
    public Trie2 clone() {
        return null;
    }
    
    /**
     * Serialize a trie onto an OutputStream.
     * A trie can be serialized multiple times.
     * The serialized data is compatible with ICU4C UTrie2 serialization.
     * Trie serialization is unrelated to Java object serialization.
     * 
     * Throw an UnsupportedOperationException if the Trie contains data that is
     * larger than the specified width.
     * 
     * Any type of Trie2 may be serialized - a m
     * @param os the stream to which the serialized Trie data will be written.
     *           Can be null, in which case the size of the function will return the
     *           size (in bytes) of the serialized data, without attempting to write the data.
     * @param width the data width of for the serialized Trie.  
     * @return the number of bytes written or needed for the trie
     *
     */
    public int utrie2_serialize(OutputStream os, ValueWidth width) throws IOException {
        return 0;
    }

    
        
    
    static class IterationResults {
        public int i;
        public int c;
        public int val;
        public IterationResults() {
            i = 0;
            c = 0;
            val = 0;
        }
    }
    
    public class CharSequenceIterator<XYZ> implements Iterator {
        public CharSequenceIterator(CharSequence text, int index) {
            set(text, index);
        }
            
        public void set(CharSequence text, int index) {
            fIndex = index;
            fText  = text;            
        }
        public boolean hasNext() {
            return fIndex<fText.length();
        }
        private CharSequence fText;
        private int fIndex;
        private Trie2.IterationResults fResults = new Trie2.IterationResults();
        
        public Trie2.IterationResults next() {
            int c = Character.codePointAt(fText, fIndex);
            int val = get(fResults.c);
            
            fResults.i = fIndex;
            fResults.c = c;
            fResults.val = val;
            fIndex++;
            if (c >= 0x10000) {
                fIndex++;
            }            
            return fResults;
        }

        public Trie2.IterationResults previous() {
            return fResults;
        }
            
            
        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            throw new UnsupportedOperationException("Trie2.CharSequenceIterator does not support remove().");
            
        }
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
     * Same as get() if c is a BMP code point except for lead surrogates,
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
     * Each contiguous range of code points with a given value will be
     * returned by the iterator.
     *
     * @param leadSurrogateValue A UTF-16 lead surrogate value, in the
     *                  range of 0xd800 - 0xdbff
     *                  of code points with the same (transformed) value
     */

    public Iterator<EnumRange> iterator(int leadSurrogateValue) {
        return null;
    }

    public Iterator<EnumRange> iterator(int leadSurrogateValue, ValueMapper valueMapper) {
        return null;
    }

}
