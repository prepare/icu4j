/*
 *******************************************************************************
 * Copyright (C) 2009, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
public abstract class Trie2 implements Iterable<Trie2.Range> {

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
        
        switch(width) {
        case BITS_16:
            This.data32 = null;
            This.initialValue = This.index[This.dataNullOffset];
            This.errorValue   = This.index[This.data16+UTRIE2_BAD_UTF8_DATA_OFFSET];
            break;
        case BITS_32:
            This.data16=0;
            This.initialValue = This.data32[This.dataNullOffset];
            This.errorValue   = This.data32[UTRIE2_BAD_UTF8_DATA_OFFSET];
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
        // TODO:  I don't think that we need this function, and propose that we drop
        //        it from the API.
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
     * At build-time, enumerate the contents of the Trie to see if there
     * is non-trivial (non-initialValue) data for any of the supplementary
     * code points associated with a lead surrogate.
     * If so, then set a special (application-specific) value for the
     * lead surrogate code _unit_, with Trie2Writable.setForLeadSurrogateCodeUnit().
     *
     * At runtime, use Trie2.getFromU16SingleLead(). If there is non-trivial
     * data and the code unit is a lead surrogate, then check if a trail surrogate
     * follows. If so, assemble the supplementary code point and look up its value 
     * with Trie2.get(); otherwise reset the lead
     * surrogate's value or do a code point lookup for it.
     *
     * If there is only trivial data for lead and trail surrogates, then processing
     * can often skip them. For example, in normalization or case mapping
     * all characters that do not have any mappings are simply copied as is.

     * 
     * @param trie the trie
     * @param c the code point or lead surrogate value.
     * @return the value
     */
    abstract public int getFromU16SingleLead(char c);
   

    /**
     * Equals function.  Two Tries are equal if their contents are equal.
     * The type need not be the same, so a Trie2Writable will be equal to 
     * (frozen) Trie2_16 or Trie2_32 so long as they are storing the same values.
     * 
     */
    public final boolean equals(Object other) {
        // TODO:  should the error and default values be considered for equality?
        if(!(other instanceof Trie2)) {
            return false;
        }
        Trie2 OtherTrie = (Trie2)other;
        Range  rangeFromOther;
        
        Iterator<Trie2.Range> otherIter = OtherTrie.iterator();
        for (Trie2.Range rangeFromThis: this) {
            if (otherIter.hasNext() == false) {
                return false;
            }
            rangeFromOther = otherIter.next();
            if (!rangeFromThis.equals(rangeFromOther)) {
                return false;
            }
        }
        if (otherIter.hasNext()) {
            return false;
        }
        return true;
    }
    
    
    public int hashCode() {
        if (fHash == 0) {
            int hash = initHash();
            for (Range r: this) {
                hash = hashInt(hash, r.hashCode());
            }
            fHash = hash;
        }
        return fHash;
    }
    
    /**
     * When iterating over the contents of a Trie2, Elements of this type are produced.
     * The iterator will return one item for each contiguous range of codepoints  having the same value.  
     * 
     * When iterating, the same Trie2EnumRange object will be reused and returned for each range.
     * If you need to retain complete iteration results, clone each returned Trie2EnumRange,
     * or save the range in some other way, before advancing to the next iteration step.
     */
    public static class Range {
        public int     startCodePoint;
        public int     endCodePoint;     // Inclusive.
        public int     value;
        public boolean leadSurrogate;
        
        public boolean equals(Object other) {
            if (other == null || !(other.getClass().equals(getClass()))) {
                return false;
            }
            Range tother = (Range)other;
            return this.startCodePoint == tother.startCodePoint &&
                   this.endCodePoint   == tother.endCodePoint   &&
                   this.value          == tother.value;
        }
        
        
        public int hashCode() {
            int h = initHash();
            h = hashUChar32(h, startCodePoint);
            h = hashUChar32(h, endCodePoint);
            h = hashInt(h, value);
            return h;
        }
    }
    
    
    /**
     *  Create an iterator over the value ranges in this Trie2.
     *  Values from the Trie are not remapped or filtered, but are returned as they
     *  are stored in the Trie.
     *  
     * @return an Iterator
     */
    public Iterator<Range> iterator() {
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
    public Iterator<Range> iterator(ValueMapper mapper) {
        return new TrieIterator(mapper);
    }

    
    /**
     * Create an iterator over the trie values for the 1024=0x400 code points
     * corresponding to a given lead surrogate.
     * For example, for the lead surrogate U+D87E it will enumerate the values
     * for [U+2F800..U+2FC00[.
     * Used by data builder code that sets special lead surrogate code unit values
     * for optimized UTF-16 string processing.
     *
     * Do not modify the trie during the iteration.
     *
     * Except for the limited code point range, this functions just like Trie2.iterator().
     *
     */
    public Iterator<Range> iteratorForLeadSurrogate(char lead, ValueMapper mapper) {
        return new TrieIterator(lead, mapper);
    }

    /**
     * Create an iterator over the trie values for the 1024=0x400 code points
     * corresponding to a given lead surrogate.
     * For example, for the lead surrogate U+D87E it will enumerate the values
     * for [U+2F800..U+2FC00[.
     * Used by data builder code that sets special lead surrogate code unit values
     * for optimized UTF-16 string processing.
     *
     * Do not modify the trie during the iteration.
     *
     * Except for the limited code point range, this functions just like Trie2.iterator().
     *
     */
    public Iterator<Range> iteratorForLeadSurrogate(char lead) {
        ValueMapper mapper = new ValueMapper() {
            public int map(int in) { 
                return in;
            }
        };
        return new TrieIterator(lead, mapper);
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
        // Note:  class Trie2Writable overrides this serialize function.
        //        This implementation will be directly called only for read-only Tries2s.
        
        // Verify that the requested data width matches with the width of the Trie.
        if (this instanceof Trie2_16 && width != ValueWidth.BITS_16) {
            throw new UnsupportedOperationException("Trie2 serialize requested width does not match the data.");
        }
        
        // Write the header.  It is already set and ready to use, having been
        //  created when the Trie2 was unserialized or when it was frozen.
        DataOutputStream dos = new DataOutputStream(os);
        int  bytesWritten = 0;
        
        dos.writeInt(header.signature);  
        dos.writeShort(header.options);
        dos.writeShort(header.indexLength);
        dos.writeShort(header.shiftedDataLength);
        dos.writeShort(header.index2NullOffset);
        dos.writeShort(header.dataNullOffset);
        dos.writeShort(header.shiftedHighStart);
        bytesWritten += 16;
        
        // Write the index
        int i;
        for (i=0; i< header.indexLength; i++) {
            dos.writeChar(index[i]);
        }
        bytesWritten += header.indexLength;
        
        // Write the data
        if (this instanceof Trie2_16) {
            for (i=0; i<dataLength; i++) {
                dos.writeChar(index[data16+i]);
            }
            bytesWritten += dataLength*2;
        } else {
            for (i=0; i<dataLength; i++) {
                dos.writeInt(data32[i]);
            }
            bytesWritten += dataLength*4;
        }
        
        return bytesWritten;        
    }
    
        
    /**
     * Struct-like class for holding the results returned by a UTrie2 CharSequence iterator.
     * The iteration walks over a CharSequence, and for each Unicode code point therein
     * returns the character and its associated Trie value.
     */
    public static class CharSequenceValues {
        // TODO:  a better name for this class? 
        
        /** string index of the current code point. */
        public int index;
        
        /** The code point at index.  */
        public int codePoint;
        
        /** The Trie value for the current code point */
        public int value;  
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
     * An iterator that operates over an input CharSequence, and for each Unicode code point
     * in the input returns the associated value from the Trie.
     * 
     * The iterator can move forwards or backwards, and can be reset to an arbitrary index.
     */
    public class CharSequenceIterator implements Iterator<CharSequenceValues> {
        CharSequenceIterator(CharSequence t, int index) { 
            text = t;
            textLength = text.length();
            set(index);
        }
            
        CharSequence text;
        private int textLength;
        int index;
        Trie2.CharSequenceValues fResults = new Trie2.CharSequenceValues();
        
        
        public void set(int i) {
            if (i < 0 || i > textLength) {
                throw new IndexOutOfBoundsException();
            }
            index = i;
        }
        
        
        public final boolean hasNext() {
            return index<textLength;
        }
        
        
        public final boolean hasPrevious() {
            return index>0;
        }
        
        // Note: next() is overridden in Trie2_16 and Trie_32 for potential efficiency gains.
        //       The functionality is identical.  This implementation is used by writable tries.
        public Trie2.CharSequenceValues next() {
            int c = Character.codePointAt(text, index);
            int val = get(c);

            fResults.index = index;
            fResults.codePoint = c;
            fResults.value = val;
            index++;
            if (c >= 0x10000) {
                index++;
            }            
            return fResults;
        }

        
        // Note: previous() is overridden in Trie2_16 and Trie_32 for potential efficiency gains.
        //       The functionality is identical.  This implementation is used by writable tries.
        public Trie2.CharSequenceValues previous() {
            int c = Character.codePointBefore(text, index);
            int val = get(c);
            index--;
            if (c >= 0x10000) {
                index--;
            }
            fResults.index = index;
            fResults.codePoint = c;
            fResults.value = val;
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
    
    //
    //  Data members of UTrie2.
    //
    UTrie2Header  header;
    char          index[];           // Index array.  Includes data for 16 bit Tries.
    int           data16;            // Offset to data portion of the index array, if 16 bit data.
                                     //    zero if 32 bit data.
    int           data32[];          // NULL if 16b data is used via index 

    int           indexLength;
    int           dataLength;
    int           index2NullOffset;  // 0xffff if there is no dedicated index-2 null block
    int           initialValue;

    /** Value returned for out-of-range code points and illegal UTF-8. */
    int           errorValue;

    /* Start of the last range which ends at U+10ffff, and its value. */
    int           highStart;
    int           highValueIndex;
    
    int           dataNullOffset;
    
    int           fHash;              // Zero if not yet computed.
                                      //  Shared by Trie2Writable, Trie2_16, Trie2_32.
                                      //  Thread safety:  if two racing threads compute
                                      //     the same hash on a frozen Trie, no damage is done.

        
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
     *   Iteration over a Trie2 first returns all of the ranges that are indexed by code points,
     *   then returns the special alternate values for the lead surrogates
     *     
     * @internal
     */
    class TrieIterator implements Iterator<Range> {
        // The normal constructor that configures the iterator to cover the complete
        //   contents of the Trie
        TrieIterator(ValueMapper vm) {
            mapper    = vm;
            nextStart = 0;
            limitCP   = 0x110000;
            doLeadSurrogates = true;
        }
        
        // An alternate constructor that configures the iterator to cover only the
        //   code points corresponding to a particular Lead Surrogate value.
        TrieIterator(char leadSurrogate, ValueMapper vm) {
            if (leadSurrogate < 0xd800 || leadSurrogate > 0xdbff) {
                throw new IllegalArgumentException("Bad lead surrogate value.");
            }
            mapper    = vm;
            nextStart = 0x10000 + (leadSurrogate - 0xd800) * 1024;
            limitCP   = nextStart + 1024;
            doLeadSurrogates = false;   // Do not iterate over lead the special lead surrogate
                                        //   values after completing iteration over code points.
        }
        
        /**
         *  The main next() function for Trie iterators
         *  
         */
        public Range next() {
            if (!doingCodePoints && nextStart >= 0xdc00) {
                throw new NoSuchElementException();
            }
            if (nextStart >= limitCP) {
                // Switch over from iterating normal code point values to
                //   doing the alternate lead-surrogate values.
                doingCodePoints = false;
                nextStart = 0xd800;
            }
            int   endOfRange = 0;
            int   val = 0;
            int   mappedVal = 0;
            
            if (doingCodePoints) {
                // Iteration over code point values.
                val = get(nextStart);
                mappedVal = mapper.map(val);
                endOfRange = rangeEnd(nextStart);
                // Loop once for each range in the Trie with the same raw (unmapped) value.
                // Loop continues so long as the mapped values are the same.
                for (;;) {
                    if (endOfRange >= limitCP-1) {
                        break;
                    }
                    val = get(endOfRange+1);
                    if (mapper.map(val) != mappedVal) {
                        break;
                    }
                    endOfRange = rangeEnd(endOfRange+1);
                }
            } else {
                // Iteration over the alternate lead surrogate values.
                val = getFromU16SingleLead((char)nextStart); 
                mappedVal = mapper.map(val);
                endOfRange = rangeEndLS((char)nextStart);
                // Loop once for each range in the Trie with the same raw (unmapped) value.
                // Loop continues so long as the mapped values are the same.
                for (;;) {
                    if (endOfRange >= 0xdbff) {
                        break;
                    }
                    val = getFromU16SingleLead((char)(endOfRange+1));
                    if (mapper.map(val) != mappedVal) {
                        break;
                    }
                    endOfRange = rangeEndLS((char)(endOfRange+1));
                }
            }
            returnValue.startCodePoint = nextStart;
            returnValue.endCodePoint   = endOfRange;
            returnValue.value          = mappedVal;
            returnValue.leadSurrogate  = !doingCodePoints;
            nextStart                  = endOfRange+1;            
            return returnValue;
        }
        
        /**
         * 
         */
        public boolean hasNext() {
            return doingCodePoints && doLeadSurrogates || nextStart < 0xdc00;
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
            // TODO: add optimizations
            int c;
            int val = get(startingC);
            int limit = Math.min(highStart, limitCP);
            
            for (c = startingC+1; c <= limit; c++) {
                if (get(c) != val) {
                    break;
                }
            }
            if (c >= highStart) {
                c = limitCP;
            }
            return c - 1;
        }
                
        /**
         * Find the last lead surrogate in a contiguous range  with the
         * same Trie value as the input character.
         * 
         * Use the alternate Lead Surrogate values from the Trie,
         * not the code-point values.
         * 
         * @param c  The character to begin with.
         * @return   The last contiguous character with the same value.
         */
        private int rangeEndLS(char startingLS) {
            if (startingLS >= 0xdbff) {
                return 0xdbff;
            }
            
            // TODO: add optimizations
            int c;
            int val = getFromU16SingleLead(startingLS);
            for (c = startingLS+1; c <= 0x0dbff; c++) {
                if (getFromU16SingleLead((char)c) != val) {
                    break;
                }
            }
            return c-1;
        }
        
        //
        //   Iteration State Variables
        //
        private ValueMapper    mapper;
        private Range          returnValue = new Range();
        // The starting code point for the next range to be returned.
        private int            nextStart;
        // The upper limit for the last normal range to be returned.  Normally 0x110000, but
        //   may be lower when iterating over the code points for a single lead surrogate.
        private int            limitCP;
        
        // True while iterating over the the trie values for code points.
        // False while iterating over the alternate values for lead surrogates.
        private boolean        doingCodePoints = true;
        
        // True if the iterator should iterate the special values for lead surrogates in
        //   addition to the normal values for code points.
        private boolean        doLeadSurrogates = true;
    }
    
    
    //
    //  Hashing implementation functions.  FNV hash.  Respected public domain algorithm.
    //
    private static int initHash() {
        return 0x811c9DC5;  // unsigned 2166136261
    }
    
    private static int hashByte(int h, int b) {
        h = h * 16777619;
        h = h ^ b;
        return h;
    }
    
    private static int hashUChar32(int h, int c) {
        h = Trie2.hashByte(h, c & 255);
        h = Trie2.hashByte(h, (c>>8) & 255);
        h = Trie2.hashByte(h, c>>16);
        return h;
    }
    
    private static int hashInt(int h, int i) {
        h = Trie2.hashByte(h, i & 255);
        h = Trie2.hashByte(h, (i>>8) & 255);
        h = Trie2.hashByte(h, (i>>16) & 255);
        h = Trie2.hashByte(h, (i>>24) & 255);
        return h;
    }

}
