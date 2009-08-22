/*
 *******************************************************************************
 * Copyright (C) 2009, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;


/**
 * This is the interface and common implementation of a Unicode trie.
 * It is a kind of compressed table that maps from Unicode code points (0..0x10ffff)
 * to 16- or 32-bit integer values.  It works best when there are ranges of
 * characters with the same value, which is generally the case with Unicode
 * character properties.
 *
 * This is the second common version of a Unicode trie (hence the name Trie2).
 * 
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
     * Internal only constructor.  Wraps a Trie2 around a set of unserialized data
     *    for a read-only Trie.  Invoked from UTrie2_16 and UTrie2_32.
     * @param trieData  the trie data.
     * @internal
     */
    Trie2(UTrie2 trieData) {
        trie = trieData;
    }
    
    /**
     * Create a Trie2 from its serialized form.  Inverse of utrie2_serialize().
     * 
     * The actual type of the returned Trie2 will be either Trie2_16 or Trie2_32, depending
     * on the width of the data.  Logically this doesn't matter, because the entire API
     * for accessing the Trie data is available via the Trie2 base class.  But there
     * may some slight speed improvement available when repeatedly accessing the Trie
     * by casting to the actual type in advance.
     * 
     * The serialized Trie on the stream may be in either little or big endian byte order.
     *
     * @param is an input stream to the serialized form of a UTrie2.  
     * @return the unserialized trie
     * @throws IllegalArgumentException if the stream does not contain a serialized trie
     *                                  or if the value width of the trie does not match valueBits.
     * @throws IOException if a read error occurs on the InputStream.
     * 
     * Note: dropped the valueWidth parameter (16 or 32 bits) at Mark's suggestion.  If it matters,
     *       check the returned type. 
     */
    public static Trie2  createFromSerialized(InputStream is) throws IOException {
         //    From ICU4C utrie2_impl.h
         //    * Trie data structure in serialized form:
         //     *
         //     * UTrie2Header header;
         //     * uint16_t index[header.index2Length];
         //     * uint16_t data[header.shiftedDataLength<<2];  -- or uint32_t data[...]
         //     * @internal
         //     */
         //    typedef struct UTrie2Header {
         //        /** "Tri2" in big-endian US-ASCII (0x54726932) */
         //        uint32_t signature;
    
         //       /**
         //         * options bit field:
         //         * 15.. 4   reserved (0)
         //         *  3.. 0   UTrie2ValueBits valueBits
         //         */
         //        uint16_t options;
         // 
         //        /** UTRIE2_INDEX_1_OFFSET..UTRIE2_MAX_INDEX_LENGTH */
         //        uint16_t indexLength;
         // 
         //        /** (UTRIE2_DATA_START_OFFSET..UTRIE2_MAX_DATA_LENGTH)>>UTRIE2_INDEX_SHIFT */
         //        uint16_t shiftedDataLength;
         // 
         //        /** Null index and data blocks, not shifted. */
         //        uint16_t index2NullOffset, dataNullOffset;
         // 
         //        /**
         //         * First code point of the single-value range ending with U+10ffff,
         //         * rounded up and then shifted right by UTRIE2_SHIFT_1.
         //         */
         //        uint16_t shiftedHighStart;
         //    } UTrie2Header;
        
        DataInputStream dis = new DataInputStream(is);        
        UTrie2  trie = new UTrie2();
        boolean needByteSwap = false;
        
        /* check the signature */
        trie.header.signature = dis.readInt();
        switch (trie.header.signature) {
        case 0x54726932:
            needByteSwap = false;
            break;
        case 0x32697254:
            needByteSwap = true;
            trie.header.signature = Integer.reverseBytes(trie.header.signature);
            break;
        default:
            throw new IllegalArgumentException("Stream does not contain a serialized UTrie2");
        }
                
        trie.header.options = swapShort(needByteSwap, dis.readUnsignedShort());
        trie.header.indexLength = swapShort(needByteSwap, dis.readUnsignedShort());
        trie.header.shiftedDataLength = swapShort(needByteSwap, dis.readUnsignedShort());
        trie.header.index2NullOffset = swapShort(needByteSwap, dis.readUnsignedShort());
        trie.header.shiftedHighStart = swapShort(needByteSwap, dis.readUnsignedShort());
        
        // Trie data width - 0: 16 bits
        //                   1: 32 bits
        if ((trie.header.options & UTRIE2_OPTIONS_VALUE_BITS_MASK) > 1) {
            throw new IllegalArgumentException("UTrie2 serialized format error.");
        }
        trie.dataWidth = (trie.header.options & UTRIE2_OPTIONS_VALUE_BITS_MASK) == 0 ? 
                ValueWidth.BITS_16: ValueWidth.BITS_32;
        
        /* get the length values and offsets */
        trie.indexLength      = trie.header.indexLength;
        trie.dataLength       = trie.header.shiftedDataLength << UTRIE2_INDEX_SHIFT;
        trie.index2NullOffset = trie.header.index2NullOffset;
        trie.dataNullOffset   = trie.header.dataNullOffset;

        trie.highStart        = trie.header.shiftedHighStart << UTRIE2_SHIFT_1;
        trie.highValueIndex   = trie.dataLength - UTRIE2_DATA_GRANULARITY;
        if (trie.dataWidth == ValueWidth.BITS_16) {
            trie.highValueIndex += trie.indexLength;
        }

        // Allocate the trie index array.  If the data width is 16 bits, the array also
        //   includes the space for the data.
        
        int indexArraySize = trie.indexLength;
        if (trie.dataWidth == ValueWidth.BITS_16) {
            indexArraySize += trie.dataLength;
        }
        trie.index = new char[indexArraySize];
        
        /* Read in the index */
        int i;
        for (i=0; i<trie.indexLength; i++) {
            trie.index[i] = swapChar(needByteSwap, dis.readChar());
        }
        
        /* Read in the data.  16 bit data goes in the same array as the index.
         * 32 bit data goes in its own separate data array.
         */
        if (trie.dataWidth == ValueWidth.BITS_16) {
            for (i=0; i<trie.dataLength; i++) {
                trie.index[trie.indexLength + i] = swapChar(needByteSwap, dis.readChar());
            }
        } else {
            trie.data32 = new int[trie.dataLength];
            for (i=0; i<trie.dataLength; i++) {
                trie.data32[i] = swapInt(needByteSwap, dis.readInt());
            }
        }
        
        /* Create the Trie object of the appropriate type to be returned to the user.
         */
        Trie2 result = null;
        if (trie.dataWidth == ValueWidth.BITS_16) {
            result = new Trie2_16(trie);
        } else {
            result = new Trie2_32(trie);
        }
        return result;
    }
    
    private static int swapShort(boolean needSwap, int value) {
        return needSwap? ((int)Short.reverseBytes((short)value)) & 0x0000ffff : value;
    }
    private static char swapChar(boolean needSwap, char value) {
        return needSwap? (char)Short.reverseBytes((short)value) : value;
    }
    private static int swapInt(boolean needSwap, int value) {
        return needSwap? Integer.reverseBytes(value) : value;
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
     * @throws IOException on errors in reading from the input stream.
     */
    public static int getVersion(InputStream is, boolean anyEndianOk) throws IOException {
        if (! is.markSupported()) {
            throw new IllegalArgumentException("Input stream must support mark().");
            }
        is.mark(4);
        byte sig[] = new byte[4];
        is.read(sig);
        is.reset();
        
        if (sig[0]=='T' && sig[1]=='r' && sig[2]=='i' && sig[3]=='e') {
            return 1;
        }
        if (sig[0]=='T' && sig[1]=='r' && sig[2]=='i' && sig[3]=='2') {
            return 2;
        }
        if (anyEndianOk) {
            if (sig[0]=='e' && sig[1]=='i' && sig[2]=='r' && sig[3]=='T') {
                return 1;
            }
            if (sig[0]=='2' && sig[1]=='i' && sig[2]=='r' && sig[3]=='T') {
                return 2;
            }
        }
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
    abstract public int get(int codePoint);

    
    /**
     * Get a 16-bit trie value from a UTF-16 single/lead code unit (<=U+ffff).
     * Same as get() if c is a BMP code point except for lead surrogates,
     * but faster.
     * 
     * @param trie the trie
     * @param c the code unit (0x0000 .. 0x0000ffff)
     * @return the value
     */
    abstract int getFromU16SingleLead(int c);
   
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
     *  Create an iterator over the value ranges in this Trie2.
     *  Values from the Trie are not remapped or filtered, but are returned as they
     *  appear in the Trie.
     *  
     * @return an Iterator
     */
    public Iterator<EnumRange> iterator() {
        ValueMapper vm = new ValueMapper() {
            public int map(int in) { 
                return in;
            }
        };
        return iterator(vm);
    }
    
    /**
     * Create an iterator over the value ranges from this Trie2.
     * Values from the trie are passed through a caller-supplied remapping function,
     * and it is the remapped values that determine the ranges that
     * will be produced by the iterator.
     * 
     * 
     * @param mapper provides a function to remap values obtained from the Trie.
     * @return an Interator
     */
    public Iterator<EnumRange> iterator(ValueMapper mapper) {
        return null;
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
     * ValueMapper m = new ValueMapper() {
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
     * Serialize a trie onto an OutputStream.
     * A trie can be serialized multiple times.
     * The serialized data is compatible with ICU4C UTrie2 serialization.
     * Trie serialization is unrelated to Java object serialization.
     * 
     * A read-only Trie, of type Trie2_16 or Trie2_32 can only be serialized
     * with its actual data width.  A writeable Trie may be serialized with
     * either 16 or 32 bit data width, although serializing to 16 bit width
     * will fail if the actual data is wider.
     * 
     * @param os the stream to which the serialized Trie data will be written.
     * @param width the data width of for the serialized Trie.  
     * @return the number of bytes written.
     * @throw an UnsupportedOperationException if the Trie contains data that is
     *        larger than the specified width, or, for a read only Trie, if the
     *        actual width is different from the specified width.
     */
    public int serialize(OutputStream os, ValueWidth width) throws IOException {
        return 0;
    }

    
        
    /**
     * Struct-like class for holding the results returned by a UTrie2 CharSequence iterator.
     * The iteration walks over a CharSequence, and for each Unicode code point therein
     * returns the character and its associated Trie value.
     */
    static class IterationResults {
        /** string index of the current code point. */
        public int index;
        
        /** The code point at index.  */
        public int c;
        
        /** The Trie value for the current code point */
        public int val;  
    }
    

    /** Create an iterator that will produce the values from the Trie for
     *  the sequence of code points in an input text.
     *  
     * @param text A text string to be iterated over.
     * @param index The starting iteration position within the input text.
     * @return
     */
    public CharSequenceIterator iterator(CharSequence text, int index) {
        return new CharSequenceIterator(text, index);
    }
    
    
    /**
     * An iterator that operates over an input text, and for each Unicode code point
     * in the input returns the associated value from the Trie.
     * 
     * The iterator can move forwards or backwards, and can be reset to an 
     */
    public class CharSequenceIterator implements Iterator<IterationResults> {
        CharSequenceIterator(CharSequence t, int index) { 
            text = t;
            textLength = text.length();
            set(index);
        }
            
        CharSequence text;
        private int textLength;
        int index;
        Trie2.IterationResults fResults = new Trie2.IterationResults();
        
        
        public void set(int i) {
            if (i < 0 || i > textLength) {
                throw new IndexOutOfBoundsException();
            }
            index = i;
        }
        
        
        public final boolean hasNext() {
            return index<textLength;
        }
        
        
        // Note: next() is overridden in Trie2_16 and Trie_32 for potential efficiency gains.
        //       The functionality is identical.  This implementation is used by writable tries.
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

        
        // Note: previous() is overridden in Trie2_16 and Trie_32 for potential efficiency gains.
        //       The functionality is identical.  This implementation is used by writable tries.
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
    static class UTrie2Header {
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
    
    
    /**
     * UTrie2 structure definition.
     * This class closely parallels struct UTrie2 from the C implementation.
     * All data describing a frozen trie, of either data size, is contained or
     * referenced from here.
     *
     * Either the data table is 16 bits wide and accessed via the index
     * pointer, with each index item increased by indexLength;
     * in this case, data32==NULL, and data16 is used for direct ASCII access.
     *
     * Or the data table is 32 bits wide and accessed via the data32 pointer.
     * 
     * 
     * @internal
     */
    static class UTrie2 {
        /* protected: used by macros and functions for reading values */
        UTrie2Header  header = new UTrie2Header();
        ValueWidth    dataWidth;
        
        char index[];     // Index array.  Includes data for 16 bit Tries.
        int  data32[];    // NULL if 16b data is used via index 

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
    
    
    
    class TrieIterator implements Iterator<EnumRange> {
        TrieIterator() {
            
        }
        
        public EnumRange next() {
            return null;
        }
        
        public boolean hasNext() {
            return false;
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
        //
        //   Iteration State Variables
        //
        int    data32[] = null;
        char   idx[] = null;
        
        int    value;
        int    prevValue;
        int    initialValue;
        
        int    c;             // UChar32
        int    prev;          // UChar32
        int    highStart;     // UChar32
        
        int    j, i2Block, prevI2Block, index2NullOffset, block, prevBlock, nullBlock;

        
        /**
         * Enumerate all ranges of code points with the same relevant values.
         * The values are transformed from the raw trie entries by the enumValue function.
         *
         * Currently requires start<limit and both start and limit must be multiples
         * of UTRIE2_DATA_BLOCK_LENGTH.
         *
         * Optimizations:
         * - Skip a whole block if we know that it is filled with a single value,
         *   and it is the same as we visited just before.
         * - Handle the null block specially because we know a priori that it is filled
         *   with a single value.
         */
         void enumEitherTrie(int start, int limit) {
 
                /* frozen trie */
                idx=trie.index;
                data32=trie.data32;

                index2NullOffset=trie.index2NullOffset;
                nullBlock=trie.dataNullOffset;
            //} else {
            //    /* unfrozen, mutable trie */
            //    idx=NULL;
            //    data32=trie->newTrie->data;
            //
            //    index2NullOffset=trie->newTrie->index2NullOffset;
            //    nullBlock=trie->newTrie->dataNullOffset;
            // }

            highStart=trie.highStart;

            /* get the enumeration value that corresponds to an initial-value trie data entry */
            initialValue=enumValue(context, trie->initialValue);

            /* set variables for previous range */
            prevI2Block=-1;
            prevBlock=-1;
            prev=start;
            prevValue=0;

            /* enumerate index-2 blocks */
            for(c=start; c<limit && c<highStart;) {
                /* Code point limit for iterating inside this i2Block. */
                UChar32 tempLimit=c+UTRIE2_CP_PER_INDEX_1_ENTRY;
                if(limit<tempLimit) {
                    tempLimit=limit;
                }
                if(c<=0xffff) {
                    if(!U_IS_SURROGATE(c)) {
                        i2Block=c>>UTRIE2_SHIFT_2;
                    } else if(U_IS_SURROGATE_LEAD(c)) {
                        /*
                         * Enumerate values for lead surrogate code points, not code units:
                         * This special block has half the normal length.
                         */
                        i2Block=UTRIE2_LSCP_INDEX_2_OFFSET;
                        tempLimit=MIN(0xdc00, limit);
                    } else {
                        /*
                         * Switch back to the normal part of the index-2 table.
                         * Enumerate the second half of the surrogates block.
                         */
                        i2Block=0xd800>>UTRIE2_SHIFT_2;
                        tempLimit=MIN(0xe000, limit);
                    }
                } else {
                    /* supplementary code points */
                    if(idx!=NULL) {
                        i2Block=idx[(UTRIE2_INDEX_1_OFFSET-UTRIE2_OMITTED_BMP_INDEX_1_LENGTH)+
                                      (c>>UTRIE2_SHIFT_1)];
                    } else {
                        i2Block=trie->newTrie->index1[c>>UTRIE2_SHIFT_1];
                    }
                    if(i2Block==prevI2Block && (c-prev)>=UTRIE2_CP_PER_INDEX_1_ENTRY) {
                        /*
                         * The index-2 block is the same as the previous one, and filled with prevValue.
                         * Only possible for supplementary code points because the linear-BMP index-2
                         * table creates unique i2Block values.
                         */
                        c+=UTRIE2_CP_PER_INDEX_1_ENTRY;
                        continue;
                    }
                }
                prevI2Block=i2Block;
                if(i2Block==index2NullOffset) {
                    /* this is the null index-2 block */
                    if(prevValue!=initialValue) {
                        if(prev<c && !enumRange(context, prev, c-1, prevValue)) {
                            return;
                        }
                        prevBlock=nullBlock;
                        prev=c;
                        prevValue=initialValue;
                    }
                    c+=UTRIE2_CP_PER_INDEX_1_ENTRY;
                } else {
                    /* enumerate data blocks for one index-2 block */
                    int32_t i2, i2Limit;
                    i2=(c>>UTRIE2_SHIFT_2)&UTRIE2_INDEX_2_MASK;
                    if((c>>UTRIE2_SHIFT_1)==(tempLimit>>UTRIE2_SHIFT_1)) {
                        i2Limit=(tempLimit>>UTRIE2_SHIFT_2)&UTRIE2_INDEX_2_MASK;
                    } else {
                        i2Limit=UTRIE2_INDEX_2_BLOCK_LENGTH;
                    }
                    for(; i2<i2Limit; ++i2) {
                        if(idx!=NULL) {
                            block=(int32_t)idx[i2Block+i2]<<UTRIE2_INDEX_SHIFT;
                        } else {
                            block=trie->newTrie->index2[i2Block+i2];
                        }
                        if(block==prevBlock && (c-prev)>=UTRIE2_DATA_BLOCK_LENGTH) {
                            /* the block is the same as the previous one, and filled with prevValue */
                            c+=UTRIE2_DATA_BLOCK_LENGTH;
                            continue;
                        }
                        prevBlock=block;
                        if(block==nullBlock) {
                            /* this is the null data block */
                            if(prevValue!=initialValue) {
                                if(prev<c && !enumRange(context, prev, c-1, prevValue)) {
                                    return;
                                }
                                prev=c;
                                prevValue=initialValue;
                            }
                            c+=UTRIE2_DATA_BLOCK_LENGTH;
                        } else {
                            for(j=0; j<UTRIE2_DATA_BLOCK_LENGTH; ++j) {
                                value=enumValue(context, data32!=NULL ? data32[block+j] : idx[block+j]);
                                if(value!=prevValue) {
                                    if(prev<c && !enumRange(context, prev, c-1, prevValue)) {
                                        return;
                                    }
                                    prev=c;
                                    prevValue=value;
                                }
                                ++c;
                            }
                        }
                    }
                }
            }

            if(c>limit) {
                c=limit;  /* could be higher if in the index2NullOffset */
            } else if(c<limit) {
                /* c==highStart<limit */
                uint32_t highValue;
                if(idx!=NULL) {
                    highValue=
                        data32!=NULL ?
                            data32[trie->highValueIndex] :
                            idx[trie->highValueIndex];
                } else {
                    highValue=trie->newTrie->data[trie->newTrie->dataLength-UTRIE2_DATA_GRANULARITY];
                }
                value=enumValue(context, highValue);
                if(value!=prevValue) {
                    if(prev<c && !enumRange(context, prev, c-1, prevValue)) {
                        return;
                    }
                    prev=c;
                    prevValue=value;
                }
                c=limit;
            }

            /* deliver last range */
            enumRange(context, prev, c-1, prevValue);
        }

    }

}
