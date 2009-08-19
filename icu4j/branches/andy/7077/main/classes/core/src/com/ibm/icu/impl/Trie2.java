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
     * Create a Trie2 from its serialized form.  Inverse of utrie2_serialize().
     * 
     * The actual type of the returned Trie2 will be either Trie2_16 or Trie2_32, depending
     * on the width of the data.  Logically this doesn't matter, because the entire API
     * for accessing the Trie data is available via the Trie2 base class.  But there
     * may some slight speed improvement available by casting to the actual type in advance
     * when repeatedly accessing the Trie.
     *
     * @param valueBits selects the data entry size; results in an
     *                  TODO:<which> exception if it does not match the serialized form
     * @param data an input stream to the serialized form of a UTrie2.  
     * @param pActualLength receives the actual number of bytes at data taken up by the trie data;
     *                      can be NULL
     * @param pErrorCode an in/out ICU UErrorCode
     * @return the unserialized trie
     * @throws IllegalArgumentException if the stream does not contain a serialized trie
     *                                  or if the value width of the trie does not match valueBits.
     * @throws IOException if a read error occurs on the InputStream.
     * 
     *
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
     * @param os the stream to which the serialized Trie data will be written.
     *           Can be null, in which case the size of the function will return the
     *           size (in bytes) of the serialized data, without attempting to write the data.
     * @param width the data width of for the serialized Trie.  
     * @return the number of bytes written.
     * @throw an UnsupportedOperationException if the Trie contains data that is
     *        larger than the specified width.
     */
    public int serialize(OutputStream os, ValueWidth width) throws IOException {
        return 0;
    }

    
        
    /**
     * Struct-like class for holding the results returned by a UTrie2 CharSequence iterator.
     * The iteration walks over a CharSequence, and for each Unicode code point therein
     * returns the associated Trie value.
     */
    static class IterationResults {
        /** string index of the current code point. */
        public int index;
        
        /** The code point at index.  */
        public int c;
        
        /** The Trie value for the current code point */
        public int val;  
    }
    

    /** Return an iterator that will produce the value from the Trie for
     *  the sequence of code points in an input text.
     *  
     *  This function is overridden in classes Trie2_16 and Trie2_32
     * @param text
     * @param index
     * @return
     */
    public CharSequenceIterator iterator(CharSequence text, int index) {
        return new CharSequenceIterator(text, index);
    }
    
    
    public class CharSequenceIterator implements Iterator<IterationResults> {
        CharSequenceIterator(CharSequence text, int index) { 
            set(text, index);
        }
            
        CharSequence text;
        private int textLength;
        int index;
        Trie2.IterationResults fResults = new Trie2.IterationResults();
        
        public void set(CharSequence t, int i) {
            if (i<0 || i > t.length()) {
                throw new IndexOutOfBoundsException();
            }
            index = i;
            text  = t;
            textLength = text.length();
        }
        
        public void set(int i) {
            if (i < 0 || i > textLength) {
                throw new IndexOutOfBoundsException();
            }
            index = i;
        }
        
        
        public final boolean hasNext() {
            return index<textLength;
        }
        
        
        public Trie2.IterationResults next() {
            int c = Character.codePointAt(text, index);
            int val = get(c);

            fResults.index = index;
            fResults.c = c;
            fResults.val = val;
            index++;
            if (c >= 0x10000) {
                index++;
            }            
            return fResults;
        }

        
        public Trie2.IterationResults previous() {
            int c = Character.codePointBefore(text, index);
            int val = get(c);
            index--;
            if (c >= 0x10000) {
                index--;
            }
            fResults.index = index;
            fResults.c = c;
            fResults.val = val;
            return fResults;
        }
            
        /** 
         * Iterator.remove() is not supported by Trie2.CharSequenceIterator.
         * @throws UnsupportedOperationException
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            throw new UnsupportedOperationException("Trie2.CharSequenceIterator does not support remove().");
            
        }
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
     * for [U+2F800..U+2FC00].
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
    
    
    //
    // Below this point are internal implementation items.  No further public API.
    //
    
    /**
     * Trie data structure in serialized form:
     *
     * UTrie2Header header;
     * uint16_t index[header.index2Length];
     * uint16_t data[header.shiftedDataLength<<2];  -- or uint32_t data[...]
     * 
     * For Java, this is read from the stream into one of these.
     * (The C version just places a struct over the raw serialized data.)
     * 
     * @internal
     */
    class UTrie2Header {
        /** "Tri2" in big-endian US-ASCII (0x54726932) */
        int signature;
        
        /**
         * options bit field (uint16_t):
         * 15.. 4   reserved (0)
         *  3.. 0   UTrie2ValueBits valueBits
         */
        int  options;

        /** UTRIE2_INDEX_1_OFFSET..UTRIE2_MAX_INDEX_LENGTH  (uint16_t) */
        int  indexLength;
        
        /** (UTRIE2_DATA_START_OFFSET..UTRIE2_MAX_DATA_LENGTH)>>UTRIE2_INDEX_SHIFT  (uint16_t) */
        int  shiftedDataLength;

        /** Null index and data blocks, not shifted.  (uint16_t) */
        int  index2NullOffset, dataNullOffset;

        /**
         * First code point of the single-value range ending with U+10ffff,
         * rounded up and then shifted right by UTRIE2_SHIFT_1.  (uint16_t)
         */
        int shiftedHighStart;
    }
    
    static final int UTRIE2_OPTIONS_VALUE_BITS_MASK=0x000f;
    
    
    /*
     * UTrie2 structure definition.
     *
     * Either the data table is 16 bits wide and accessed via the index
     * pointer, with each index item increased by indexLength;
     * in this case, data32==NULL, and data16 is used for direct ASCII access.
     *
     * Or the data table is 32 bits wide and accessed via the data32 pointer.
     */
    class UTrie2 {
        /* protected: used by macros and functions for reading values */
        char index1[];     // First level index (upper bits of character)
        char index2[];     // Second level index (middle bits of character
        char data16[];     /* for fast UTF-8 ASCII access, if 16b data */
        int  data32[];     /* NULL if 16b data is used via index */

        int  indexLength, dataLength;
        int  index2NullOffset;  /* 0xffff if there is no dedicated index-2 null block */
        int  dataNullOffset;
        int  initialValue;
        /** Value returned for out-of-range code points and illegal UTF-8. */
        int  errorValue;

        /* Start of the last range which ends at U+10ffff, and its value. */
        int  highStart;
        int  highValueIndex;
    };
    
    UTrie2   trie;

    /**
     * Trie constants, defining shift widths, index array lengths, etc.
     *
     * These are needed for the runtime macros but users can treat these as
     * implementation details and skip to the actual public API further below.
     */
    /** Shift size for getting the index-1 table offset. */
    static final int UTRIE2_SHIFT_1=6+5;

    /** Shift size for getting the index-2 table offset. */
    static final int UTRIE2_SHIFT_2=5;

    /**
     * Difference between the two shift sizes,
     * for getting an index-1 offset from an index-2 offset. 6=11-5
     */
    static final int UTRIE2_SHIFT_1_2=UTRIE2_SHIFT_1-UTRIE2_SHIFT_2;

    /**
     * Number of index-1 entries for the BMP. 32=0x20
     * This part of the index-1 table is omitted from the serialized form.
     */
    static final int UTRIE2_OMITTED_BMP_INDEX_1_LENGTH=0x10000>>UTRIE2_SHIFT_1;

    /** Number of code points per index-1 table entry. 2048=0x800 */
    static final int UTRIE2_CP_PER_INDEX_1_ENTRY=1<<UTRIE2_SHIFT_1;
    
    /** Number of entries in an index-2 block. 64=0x40 */
    static final int UTRIE2_INDEX_2_BLOCK_LENGTH=1<<UTRIE2_SHIFT_1_2;
    
    /** Mask for getting the lower bits for the in-index-2-block offset. */
    static final int UTRIE2_INDEX_2_MASK=UTRIE2_INDEX_2_BLOCK_LENGTH-1;
    
    /** Number of entries in a data block. 32=0x20 */
    static final int UTRIE2_DATA_BLOCK_LENGTH=1<<UTRIE2_SHIFT_2;
    
    /** Mask for getting the lower bits for the in-data-block offset. */
    static final int UTRIE2_DATA_MASK=UTRIE2_DATA_BLOCK_LENGTH-1;
    
    /**
     * Shift size for shifting left the index array values.
     * Increases possible data size with 16-bit index values at the cost
     * of compactability.
     * This requires data blocks to be aligned by UTRIE2_DATA_GRANULARITY.
     */
    static final int UTRIE2_INDEX_SHIFT=2;
    
    /** The alignment size of a data block. Also the granularity for compaction. */
    static final int UTRIE2_DATA_GRANULARITY=1<<UTRIE2_INDEX_SHIFT;
    
    /* Fixed layout of the first part of the index array. ------------------- */
    
    /**
     * The BMP part of the index-2 table is fixed and linear and starts at offset 0.
     * Length=2048=0x800=0x10000>>UTRIE2_SHIFT_2.
     */
    static final int UTRIE2_INDEX_2_OFFSET=0;
    
    /**
     * The part of the index-2 table for U+D800..U+DBFF stores values for
     * lead surrogate code _units_ not code _points_.
     * Values for lead surrogate code _points_ are indexed with this portion of the table.
     * Length=32=0x20=0x400>>UTRIE2_SHIFT_2. (There are 1024=0x400 lead surrogates.)
     */
    static final int UTRIE2_LSCP_INDEX_2_OFFSET=0x10000>>UTRIE2_SHIFT_2;
    static final int UTRIE2_LSCP_INDEX_2_LENGTH=0x400>>UTRIE2_SHIFT_2;
    
    /** Count the lengths of both BMP pieces. 2080=0x820 */
    static final int UTRIE2_INDEX_2_BMP_LENGTH=UTRIE2_LSCP_INDEX_2_OFFSET+UTRIE2_LSCP_INDEX_2_LENGTH;
    
    /**
     * The 2-byte UTF-8 version of the index-2 table follows at offset 2080=0x820.
     * Length 32=0x20 for lead bytes C0..DF, regardless of UTRIE2_SHIFT_2.
     */
    static final int UTRIE2_UTF8_2B_INDEX_2_OFFSET=UTRIE2_INDEX_2_BMP_LENGTH;
    static final int UTRIE2_UTF8_2B_INDEX_2_LENGTH=0x800>>6;  /* U+0800 is the first code point after 2-byte UTF-8 */
    
    /**
     * The index-1 table, only used for supplementary code points, at offset 2112=0x840.
     * Variable length, for code points up to highStart, where the last single-value range starts.
     * Maximum length 512=0x200=0x100000>>UTRIE2_SHIFT_1.
     * (For 0x100000 supplementary code points U+10000..U+10ffff.)
     *
     * The part of the index-2 table for supplementary code points starts
     * after this index-1 table.
     *
     * Both the index-1 table and the following part of the index-2 table
     * are omitted completely if there is only BMP data.
     */
    static final int UTRIE2_INDEX_1_OFFSET=UTRIE2_UTF8_2B_INDEX_2_OFFSET+UTRIE2_UTF8_2B_INDEX_2_LENGTH;
    static final int UTRIE2_MAX_INDEX_1_LENGTH=0x100000>>UTRIE2_SHIFT_1;
    
    /*
     * Fixed layout of the first part of the data array. -----------------------
     * Starts with 4 blocks (128=0x80 entries) for ASCII.
     */
    
    /**
     * The illegal-UTF-8 data block follows the ASCII block, at offset 128=0x80.
     * Used with linear access for single bytes 0..0xbf for simple error handling.
     * Length 64=0x40, not UTRIE2_DATA_BLOCK_LENGTH.
     */
    static final int UTRIE2_BAD_UTF8_DATA_OFFSET=0x80;
    
    /** The start of non-linear-ASCII data blocks, at offset 192=0xc0. */
    static final int UTRIE2_DATA_START_OFFSET=0xc0;

}
