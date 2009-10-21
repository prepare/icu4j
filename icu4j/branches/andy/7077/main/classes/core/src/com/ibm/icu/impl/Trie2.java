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
import java.util.NoSuchElementException;


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
     * Create a Trie2 from its serialized form.  Inverse of utrie2_serialize().
     * The serialized format is identical between ICU4C and ICU4J, so this function
     * will work with serialized Tries from either.
     * 
     * The actual type of the returned Trie2 will be either Trie2_16 or Trie2_32, depending
     * on the width of the data.  Logically this doesn't matter, because the entire API
     * for accessing the Trie data is available via the Trie2 base class.  But there
     * may some slight speed improvement available when repeatedly accessing the Trie
     * by casting to the actual type in advance.
     * 
     * To obtain the width of the Trie, check the actual class type of the returned Trie.
     * 
     * The serialized Trie on the stream may be in either little or big endian byte order.
     * This allows using serialized Tries from ICU4C without needing to consider the
     * byte order of the system that created them.
     *
     * @param is an input stream to the serialized form of a UTrie2.  
     * @return An unserialized trie, ready for use.
     * @throws IllegalArgumentException if the stream does not contain a serialized trie.
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
        boolean needByteSwap = false;
        
        UTrie2Header  header = new UTrie2Header();
        
        /* check the signature */
        header.signature = dis.readInt();
        switch (header.signature) {
        case 0x54726932:
            needByteSwap = false;
            break;
        case 0x32697254:
            needByteSwap = true;
            header.signature = Integer.reverseBytes(header.signature);
            break;
        default:
            throw new IllegalArgumentException("Stream does not contain a serialized UTrie2");
        }
                
        header.options = swapShort(needByteSwap, dis.readUnsignedShort());
        header.indexLength = swapShort(needByteSwap, dis.readUnsignedShort());
        header.shiftedDataLength = swapShort(needByteSwap, dis.readUnsignedShort());
        header.index2NullOffset = swapShort(needByteSwap, dis.readUnsignedShort());
        header.dataNullOffset   = swapShort(needByteSwap, dis.readUnsignedShort());
        header.shiftedHighStart = swapShort(needByteSwap, dis.readUnsignedShort());
        
        // Trie data width - 0: 16 bits
        //                   1: 32 bits
        if ((header.options & UTRIE2_OPTIONS_VALUE_BITS_MASK) > 1) {
            throw new IllegalArgumentException("UTrie2 serialized format error.");
        }
        ValueWidth  width;
        Trie2 This;
        if ((header.options & UTRIE2_OPTIONS_VALUE_BITS_MASK) == 0) {
            width = ValueWidth.BITS_16;
            This  = new Trie2_16();
        } else {
            width = ValueWidth.BITS_32;
            This  = new Trie2_32();
        }
        This.header = header;
        
        /* get the length values and offsets */
        This.indexLength      = header.indexLength;
        This.dataLength       = header.shiftedDataLength << UTRIE2_INDEX_SHIFT;
        This.index2NullOffset = header.index2NullOffset;
        This.dataNullOffset   = header.dataNullOffset;

        This.highStart        = header.shiftedHighStart << UTRIE2_SHIFT_1;
        This.highValueIndex   = This.dataLength - UTRIE2_DATA_GRANULARITY;
        if (width == ValueWidth.BITS_16) {
            This.highValueIndex += This.indexLength;
        }

        // Allocate the trie index array.  If the data width is 16 bits, the array also
        //   includes the space for the data.
        
        int indexArraySize = This.indexLength;
        if (width == ValueWidth.BITS_16) {
            indexArraySize += This.dataLength;
        }
        This.index = new char[indexArraySize];
        
        /* Read in the index */
        int i;
        for (i=0; i<This.indexLength; i++) {
            This.index[i] = swapChar(needByteSwap, dis.readChar());
        }
        
        /* Read in the data.  16 bit data goes in the same array as the index.
         * 32 bit data goes in its own separate data array.
         */
        if (width == ValueWidth.BITS_16) {
            This.data16 = This.indexLength;
            for (i=0; i<This.dataLength; i++) {
                This.index[This.data16 + i] = swapChar(needByteSwap, dis.readChar());
            }
        } else {
            This.data32 = new int[This.dataLength];
            for (i=0; i<This.dataLength; i++) {
                This.data32[i] = swapInt(needByteSwap, dis.readInt());
            }
        }
        
        /* get the data */
        switch(width) {
        case BITS_16:
            This.data32 = null;
            This.initialValue = This.index[This.dataNullOffset];
            This.errorValue   = This.index[This.data16+UTRIE2_BAD_UTF8_DATA_OFFSET];
            break;
        case BITS_32:
            This.data16=0;
            This.initialValue=This.data32[This.dataNullOffset];
            This.errorValue=This.data32[UTRIE2_BAD_UTF8_DATA_OFFSET];
            break;
        default:
            throw new IllegalArgumentException("UTrie2 serialized format error.");
        }

        return This;
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
     * @param is   an InputStream containing the serialized form
     *             of a UTrie, version 1 or 2.  The stream must support mark() and reset().
     *             TODO:  is requiring mark and reset ok?
     *             The position of the input stream will be left unchanged.
     * @param anyEndianOk If FALSE, only big-endian (Java native) serialized forms are recognized.
     *                    If TRUE, little-endian serialized forms are recognized as well.
     *             TODO:  dump this option, always allow either endian?  Or allow only big endian?
     * @return     the Trie version of the serialized form, or 0 if it is not
     *             recognized as a serialized UTrie
     * @throws     IOException on errors in reading from the input stream.
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
     * Get the value for a code point as stored in the trie.
     *
     * @param trie the trie
     * @param codePoint the code point
     * @return the value
     */
    abstract public int get(int codePoint);

    
    /**
     * Get the trie value for a UTF-16 code unit.
     *
     * A Trie2 stores two distinct values for input in the lead surrogate
     * range, one for lead surrogates, which is the value that will be
     * returned by this function, and a second value that is returned
     * by Trie2.get().
     * 
     * For code units outside of the lead surrogate range, this function
     * returns the same result as Trie2.get().
     * 
     * This function, together with the alternate value for lead surrogates,
     * makes possible very efficient processing of UTF-16 strings without
     * first converting surrogate pairs to their corresponding 32 bit code point
     * values.
     * 
     * @param trie the trie
     * @param c the code point or lead surrogate value.
     * @return the value
     */
    abstract public int getFromU16SingleLead(char c);
   
    /**
     * When iterating over the contents of a Trie2, Elements of this type are produced.
     * The iterator will return one item for each contiguous range of codepoints  having the same value.  
     * 
     * When iterating, the same Trie2EnumRange object will be reused and returned for each range.
     * If you need to retain complete iteration results, clone each returned Trie2EnumRange,
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
     *  are stored in the Trie.
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
        return new TrieIterator(mapper);
    }
    
    /**
     * When iterating over the contents of a Trie, an instance of TrieValueMapper may
     * be used to remap the values from the Trie.  The remapped values will be used
     * both in determining the ranges of codepoints and as the value to be returned
     * for each range.
     * 
     * Example of use, with an anonymous subclass of TrieValueMapper:
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
     * The iterator can move forwards or backwards, and can be reset to an arbitrary index.
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
    
    //--------------------------------------------------------------------------------
    //
    // Below this point are internal implementation items.  No further public API.
    //
    //--------------------------------------------------------------------------------
    
    
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
    
    UTrie2Header  header;
    ValueWidth    dataWidth;
    char index[];     // Index array.  Includes data for 16 bit Tries.
    int  data16;      // Offset to data portion of the index array, if 16 bit data.
                      //    zero if 32 bit data.
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
   // static class UTrie2 {
        /* protected: used by macros and functions for reading values */
     //   UTrie2Header  header = new UTrie2Header();
        // ValueWidth    dataWidth;
        
        //char index[];     // Index array.  Includes data for 16 bit Tries.
        //int  data16;      // Offset to data portion of the index array, if 16 bit data.
                          //    zero if 32 bit data.
        //int  data32[];    // NULL if 16b data is used via index 

        //int  indexLength, dataLength;
        //int  index2NullOffset;  /* 0xffff if there is no dedicated index-2 null block */
        //int  dataNullOffset;
       // int  initialValue;
        /** Value returned for out-of-range code points and illegal UTF-8. */
        //int  errorValue;

        /* Start of the last range which ends at U+10ffff, and its value. */
        //int  highStart;
        //int  highValueIndex;
        
        //UNewTrie2   newTrie;
   // };
    
    
    /*
     * Build-time trie structure.
     *
     * Just using a boolean flag for "repeat use" could lead to data array overflow
     * because we would not be able to detect when a data block becomes unused.
     * It also leads to orphan data blocks that are kept through serialization.
     *
     * Need to use reference counting for data blocks,
     * and allocDataBlock() needs to look for a free block before increasing dataLength.
     *
     * This scheme seems like overkill for index-2 blocks since the whole index array is
     * preallocated anyway (unlike the growable data array).
     * Just allocating multiple index-2 blocks as needed.
     */
    static class UNewTrie2 {
        int[]      index1 = new int[UNEWTRIE2_INDEX_1_LENGTH];
        int[]      index2 = new int[UNEWTRIE2_MAX_INDEX_2_LENGTH];
        int[]      data;

        int        initialValue, errorValue;
        int        index2Length, dataCapacity, dataLength;
        int        firstFreeBlock;
        int        index2NullOffset, dataNullOffset;
        int        highStart;     // UChar32
        boolean    isCompacted;

        /**
         * Multi-purpose per-data-block table.
         *
         * Before compacting:
         *
         * Per-data-block reference counters/free-block list.
         *  0: unused
         * >0: reference counter (number of index-2 entries pointing here)
         * <0: next free data block in free-block list
         *
         * While compacting:
         *
         * Map of adjusted indexes, used in compactData() and compactIndex2().
         * Maps from original indexes to new ones.
         */
        int[]      map = new int[UNEWTRIE2_MAX_DATA_LENGTH>>UTRIE2_SHIFT_2];
    };

    
    /**
     * Trie constants, defining shift widths, index array lengths, etc.
     *
     * These are needed for the runtime macros but users can treat these as
     * implementation details and skip to the actual public API further below.
     */
    
    static final int UTRIE2_OPTIONS_VALUE_BITS_MASK=0x000f;
    
    
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
    
    /* Building a trie ---------------------------------------------------------- */

    /*
     * These definitions are mostly needed by utrie2_builder.c, but also by
     * utrie2_get32() and utrie2_enum().
     */

    /*
     * At build time, leave a gap in the index-2 table,
     * at least as long as the maximum lengths of the 2-byte UTF-8 index-2 table
     * and the supplementary index-1 table.
     * Round up to UTRIE2_INDEX_2_BLOCK_LENGTH for proper compacting.
     */
    static final int UNEWTRIE2_INDEX_GAP_OFFSET = UTRIE2_INDEX_2_BMP_LENGTH;
    static final int UNEWTRIE2_INDEX_GAP_LENGTH =
        ((UTRIE2_UTF8_2B_INDEX_2_LENGTH + UTRIE2_MAX_INDEX_1_LENGTH) + UTRIE2_INDEX_2_MASK) &
        ~UTRIE2_INDEX_2_MASK;

    /**
     * Maximum length of the build-time index-2 array.
     * Maximum number of Unicode code points (0x110000) shifted right by UTRIE2_SHIFT_2,
     * plus the part of the index-2 table for lead surrogate code points,
     * plus the build-time index gap,
     * plus the null index-2 block.
     */
    static final int UNEWTRIE2_MAX_INDEX_2_LENGTH=
        (0x110000>>UTRIE2_SHIFT_2)+
        UTRIE2_LSCP_INDEX_2_LENGTH+
        UNEWTRIE2_INDEX_GAP_LENGTH+
        UTRIE2_INDEX_2_BLOCK_LENGTH;

    static final int UNEWTRIE2_INDEX_1_LENGTH = 0x110000>>UTRIE2_SHIFT_1;

    /**
     * Maximum length of the build-time data array.
     * One entry per 0x110000 code points, plus the illegal-UTF-8 block and the null block,
     * plus values for the 0x400 surrogate code units.
     */
    static final int  UNEWTRIE2_MAX_DATA_LENGTH = (0x110000+0x40+0x40+0x400);

 
   
    /** 
     * Implementation class for an iterator over a Trie2.
     * 
     *     The structure of the implementation is largely unchanged from the C code
     *     which uses a callback model rather than an iterator model.  Efficiency could
     *     probably be improved by reworking things a bit.
     *     
     * @internal
     */
    class TrieIterator implements Iterator<EnumRange> {
        TrieIterator(ValueMapper vm) {
            mapper = vm;
        }
        
        /**
         *  The main next() function for Trie iterators
         *  
         */
        public EnumRange next() {
            if (lastReturnedChar >= 0x10ffff) {
                throw new NoSuchElementException();
            }
            int   c = lastReturnedChar + 1;
            int   endOfRange = 0;
            int   val = get(c);
            int   mappedVal = mapper.map(val);
            
            // Loop once for each range in the Trie with the same raw (unmapped) value.
            // Loop continues so long as the mapped values are the same.
            for (;;) {
                endOfRange = rangeEnd(c);
                if (endOfRange == 0x10ffff) {
                    break;
                }
                val = get(c);
                if (mapper.map(val) != mappedVal) {
                    break;
                }
                c = endOfRange+1;
            }
            returnValue.startCodePoint = lastReturnedChar + 1;
            returnValue.endCodePoint   = endOfRange;
            returnValue.value          = mappedVal;
            lastReturnedChar           = endOfRange;            
            return returnValue;
        }
        
        /**
         * 
         */
        public boolean hasNext() {
            return lastReturnedChar < 0x10ffff;
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
        
        /**
         * Find the last character in a contiguous range of characters with the
         * same Trie value as the input character.
         * 
         * @param c  The character to begin with.
         * @return   The last contiguous character with the same value.
         */
        private int rangeEnd(int startingC) {
            if (startingC >= highStart) {
                return 0x10ffff;
            }
            
            // TODO: add optimizations
            int c;
            int val = get(startingC);
            for (c = startingC+1; c <= highStart; c++) {
                if (get(c) != val) {
                    break;
                }
            }
            if (c < highStart) {
                return c-1;
            } else {
                return 0x10ffff;
            }
        }
                
        //
        //   Iteration State Variables
        //
        
        
        private ValueMapper    mapper;
        private EnumRange      returnValue = new EnumRange();
        private int            lastReturnedChar;
        

    }

}
