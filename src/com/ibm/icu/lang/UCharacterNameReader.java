/**
*******************************************************************************
* Copyright (C) 1996-2001, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
* $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/lang/Attic/UCharacterNameReader.java,v $ 
* $Date: 2002/02/08 01:12:11 $ 
* $Revision: 1.1 $
*
*******************************************************************************
*/
package com.ibm.text;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import com.ibm.util.Utility;

/**
* <p>Internal reader class for ICU data file uname.dat containing 
* Unicode codepoint name data.</p> 
* <p>This class simply reads unames.dat, authenticates that it is a valid
* ICU data file and split its contents up into blocks of data for use in
* <a href=UCharacterNamehtml>com.ibm.text.UCharacterName</a>.
* </p> 
* <p>For more information about the format of unames.dat refer to
* <a href=oss.software.ibm.com/icu4j/icu4jhtml/com/ibm/icu/text/readme.html>
* ReadMe</a>.<\p>
* <p>unames.dat which is in big-endian format is jared together with this 
* package.</p>
* @author Syn Wee Quek
* @since release 2.1, February 1st 2002
* @draft 2.1
*/

final class UCharacterNameReader
{      
    // protected constructor ---------------------------------------------
    
    /**
    * <p>Protected constructor.</p>
    * @param inputStream ICU uprop.dat file input stream
    * @exception IOException throw if data file fails authentication 
    * @draft 2.1
    */
    protected UCharacterNameReader(InputStream inputStream) 
                                                        throws IOException
    {
        Utility.readICUDataHeader(inputStream, DATA_FORMAT_ID_, 
                                  DATA_FORMAT_VERSION_, UNICODE_VERSION_);
        m_dataInputStream_ = new DataInputStream(inputStream);
    }
  
    // protected methods -------------------------------------------------
      
    /**
    * Read and break up the stream of data passed in as arguments
    * and fills up UCharacterName.
    * If unsuccessful false will be returned.
    * @param data instance of datablock
    * @exception thrown when there's a data error.
    */
    protected void read(UCharacterName data) throws IOException
    {
        // reading index
        m_tokenstringindex_ = m_dataInputStream_.readInt();
        m_groupindex_       = m_dataInputStream_.readInt();
        m_groupstringindex_ = m_dataInputStream_.readInt();
        m_algnamesindex_    = m_dataInputStream_.readInt();
        
        // reading tokens
        int count = m_dataInputStream_.readChar();
        char token[] = new char[count];
        for (char i = 0; i < count; i ++) {
            token[i] = m_dataInputStream_.readChar();
        }
        int size = m_groupindex_ - m_tokenstringindex_;
        byte tokenstr[] = new byte[size];
        m_dataInputStream_.readFully(tokenstr);
        data.setToken(token, tokenstr);
        
        // reading the group information records
        count = m_dataInputStream_.readChar();
        data.setGroupCountSize(count, GROUP_INFO_SIZE_);
        count *= GROUP_INFO_SIZE_;
        char group[] = new char[count];
        for (int i = 0; i < count; i ++) {
            group[i] = m_dataInputStream_.readChar();
        }
        
        size = m_algnamesindex_ - m_groupstringindex_;
        byte groupstring[] = new byte[size];
        m_dataInputStream_.readFully(groupstring);
        data.setGroup(group, groupstring);
        
        count = m_dataInputStream_.readInt();
        UCharacterName.AlgorithmName alg[] = 
                                 new UCharacterName.AlgorithmName[count];
     
        for (int i = 0; i < count; i ++)
        {
            UCharacterName.AlgorithmName an = readAlg();
            if (an == null) {
                throw new IOException("unames.dat read error: Algorithmic names creation error");
            }
            alg[i] = an;
        }
        data.setAlgorithm(alg);
    }
      
    /**
    * <p>Checking the file for the correct format.</p>
    * @param dataformatid
    * @param dataformatversio
    * @return true if the file format version is correct
    * @draft 2.1
    */
    protected boolean authenticate(byte dataformatid[],
                                   byte dataformatversion[])
    {
        return Arrays.equals(DATA_FORMAT_ID_, dataformatid) &&
               Arrays.equals(DATA_FORMAT_VERSION_, dataformatversion);
    }
    
    // private variables -------------------------------------------------
  
    /**
    * Data input stream for names 
    */
    private DataInputStream m_dataInputStream_;
    /**
    * Size of the group information block in number of char
    */
    private static final int GROUP_INFO_SIZE_ = 3;

    /**
    * Index of the offset information
    */
    private int m_tokenstringindex_;
    private int m_groupindex_;
    private int m_groupstringindex_;
    private int m_algnamesindex_;
      
    /**
    * Size of an algorithmic name information group
    * start code point size + end code point size + type size + variant size + 
    * size of data size
    */
    private static final int ALG_INFO_SIZE_ = 12;
      
    /**
    * File format version and id that this class understands.
    * No guarantees are made if a older version is used
    */
    private static final byte DATA_FORMAT_VERSION_[] = 
                                    {(byte)0x1, (byte)0x0, (byte)0x0, (byte)0x0};
    private static final byte DATA_FORMAT_ID_[] = {(byte)0x75, (byte)0x6E, 
                                                    (byte)0x61, (byte)0x6D};
    private static final byte UNICODE_VERSION_[] = {(byte)0x3, (byte)0x1, 
                                                    (byte)0x1, (byte)0x0};
                                                     
    /**
    * Corrupted error string
    */
    private static final String CORRUPTED_DATA_ERROR_ =
                                "Data corrupted in character name data file";
      
    // private methods ---------------------------------------------------
      
    /**
    * Reads an individual record of AlgorithmNames
    * @return an instance of AlgorithNames if read is successful otherwise null
    * @exception thrown when file read error occurs or data is corrupted
    */
    private UCharacterName.AlgorithmName readAlg() throws IOException
    {
        UCharacterName.AlgorithmName result = 
                                       new UCharacterName.AlgorithmName();
        int rangestart = m_dataInputStream_.readInt();
        int rangeend   = m_dataInputStream_.readInt();
        byte type      = m_dataInputStream_.readByte();
        byte variant   = m_dataInputStream_.readByte();
        if (!result.setInfo(rangestart, rangeend, type, variant)) {
            return null;
        }
                         
        int size = m_dataInputStream_.readChar();
        if (type == UCharacterName.AlgorithmName.TYPE_1_)
        {
            char factor[] = new char[variant];
            for (int j = 0; j < variant; j ++) {
                factor[j] = m_dataInputStream_.readChar();
            }
                  
            result.setFactor(factor);
            size -= (variant << 1);
        }
          
        StringBuffer prefix = new StringBuffer();
        char c = (char)(m_dataInputStream_.readByte() & 0x00FF);
        while (c != 0)
        {
            prefix.append(c);
            c = (char)(m_dataInputStream_.readByte() & 0x00FF);
        }
        
        result.setPrefix(prefix.toString());
        
        size -= (ALG_INFO_SIZE_ + prefix.length() + 1);
        
        if (size > 0)
        {
            byte string[] = new byte[size];
            m_dataInputStream_.readFully(string);
            result.setFactorString(string);
        }
        return result;
    }
}

