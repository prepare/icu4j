/*
******************************************************************************
* Copyright (C) 1996-2002, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*
* $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/impl/IntTrie.java,v $
* $Date: 2002/02/08 01:08:38 $
* $Revision: 1.1 $
*
******************************************************************************
*/

package com.ibm.icu.util;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.IOException;
import com.ibm.text.UTF16;
import com.ibm.icu.internal.util.Trie;

/**
 * Trie implementation which stores data in int, 32 bits.
 * @author synwee
 * @see com.ibm.icu.util.Trie
 * @since release 2.1, Jan 01 2002
 */
public class IntTrie extends Trie
{
    // public constructors ---------------------------------------------

    /**
    * <p>Creates a new Trie with the settings for the trie data.</p>
    * <p>Unserialize the 32-bit-aligned input stream and use the data for the 
    * trie.</p>
    * @param inputStream file input stream to a ICU data file, containing 
    *                    the trie
    * @param dataManipulate, object which provides methods to parse the char 
    *                        data
    * @exception IOException thrown when data reading fails
    * @draft 2.1
    */
    public IntTrie(InputStream inputStream, DataManipulate datamanipulate)
                                                    throws IOException
    {
        super(inputStream, datamanipulate);
        if (!isIntTrie()) {
            throw new IllegalArgumentException(
                               "Data given does not belong to a int trie.");
        }
    }

    // public methods --------------------------------------------------

    // to be removed
    public String toString()
    {
        StringBuffer result = new StringBuffer(super.toString());
        result.append("\ndata length ");
        int length = m_data_.length;
        result.append(length);
        result.append("\ndata-------------------\n");

        for (int i = 0; i < length;) {
            result.append("0x");
            result.append(Integer.toHexString(m_data_[i]));
            result.append(", ");
            i ++;
            if ((i % 15) == 0) {
                result.append("\n");
            }
        }
        return result.toString();
    }

    /**
    * Gets the value associated with the codepoint.
    * If no value is associated with the codepoint, a default value will be
    * returned.
    * @param ch codepoint
    * @return offset to data
    * @draft 2.1
    */
    public final int getCodePointValue(int ch)
    {
        int offset = getCodePointOffset(ch);
        if (offset > 0) {
            return m_data_[offset];
        }
        return m_initialValue_;
    }

    /**
    * Gets the value to the data which this lead surrogate character points
    * to.
    * Returned data may contain folding offset information for the next
    * trailing surrogate character.
    * This method does not guarantee correct results for trail surrogates.
    * @param ch lead surrogate character
    * @return data value
    * @draft 2.1
    */
    public final int getLeadValue(char ch)
    {
        return m_data_[getLeadOffset(ch)];
    }

    /**
    * Get the value associated with the BMP code point.
    * Lead surrogate code points are treated as normal code points, with
    * unfolded values that may differ from getLeadValue() results.
    * @param ch the input BMP code point
    * @return trie data value associated with the BMP codepoint
    * @draft 2.1
    */
    public final int getBMPValue(char ch)
    {
        return m_data_[getBMPOffset(ch)];
    }

    /**
    * Get the value associated with a pair of surrogates.
    * @param lead a lead surrogate
    * @param trail a trail surrogate
    * @param trie data value associated with the surrogate characters
    * @draft 2.1
    */
    public final int getSurrogateValue(char lead, char trail)
    {
        if (!UTF16.isLeadSurrogate(lead) || !UTF16.isTrailSurrogate(trail)) {
            throw new IllegalArgumentException(
                "Argument characters do not form a supplementary character");
        }
        // get fold position for the next trail surrogate
        int offset = getSurrogateOffset(lead, trail);

        // get the real data from the folded lead/trail units
        if (offset > 0) {
            return m_data_[offset];
        }

        // return m_initialValue_ if there is an error
        return m_initialValue_;
    }

    /**
    * Get a value from a folding offset (from the value of a lead surrogate)
    * and a trail surrogate.
    * @param leadvalue the value of a lead surrogate that contains the
    *        folding offset
    * @param trail surrogate
    * @return trie data value associated with the trail character
    * @draft 2.1
    */
    public final int getTrailValue(int leadvalue, char trail)
    {
        if (m_dataManipulate_ == null) {
            throw new NullPointerException(
                             "The field DataManipulate in this Trie is null");
        }
        return m_data_[getRawOffset(
                               m_dataManipulate_.getFoldingOffset(leadvalue),
                               (char)(trail & SURROGATE_MASK_))];
    }

    // protected methods -----------------------------------------------

    /**
    * <p>Parses the input stream and stores its trie content into a index and
    * data array</p>
    * @param inputStream data input stream containing trie data
    * @exception IOException thrown when data reading fails
    */
    protected final void unserialize(InputStream inputStream) 
                                                    throws IOException
    {
        super.unserialize(inputStream);
        // one used for initial value
        m_data_               = new int[m_dataLength_];
        DataInputStream input = new DataInputStream(inputStream);
        for (int i = 0; i < m_dataLength_; i ++) {
            m_data_[i] = input.readInt();
        }
        m_initialValue_ = m_data_[0];
    }
    
    /**
    * Gets the offset to the data which the surrogate pair points to.
    * @param lead lead surrogate
    * @param trail trailing surrogate
    * @return offset to data
    * @draft 2.1
    */
    protected final int getSurrogateOffset(char lead, char trail)
    {
        if (m_dataManipulate_ == null) {
            throw new NullPointerException(
                             "The field DataManipulate in this Trie is null");
        }
        // get fold position for the next trail surrogate
        int offset = m_dataManipulate_.getFoldingOffset(getLeadValue(lead));

        // get the real data from the folded lead/trail units
        if (offset > 0) {
            return getRawOffset(offset, (char)(trail & SURROGATE_MASK_));
        }

        // return -1 if there is an error, in this case we return the default
        // value: m_initialValue_
        return -1;
    }
    
    /**
    * Gets the value at the argument index.
    * For use internally in com.ibm.icu.util.TrieEnumeration.
    * @param index value at index will be retrieved
    * @return 32 bit value
    * @see com.ibm.icu.util.TrieEnumeration
    * @draft 2.1
    */
    protected final int getValue(int index)
    {
      return m_data_[index];
    }
    
    /**
    * Gets the default initial value
    * @return 32 bit value 
    * @draft 2.1
    */
    protected final int getInitialValue()
    {
        return m_initialValue_;
    }

    // private data members --------------------------------------------

    /**
    * Default value
    */
    private int m_initialValue_;
    /**
    * Array of char data
    */
    private int m_data_[];
}