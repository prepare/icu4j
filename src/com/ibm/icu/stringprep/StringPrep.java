/*
 *******************************************************************************
 * Copyright (C) 2003-2004, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/stringprep/Attic/StringPrep.java,v $
 * $Date: 2003/08/21 23:40:41 $
 * $Revision: 1.1 $ 
 *
 *****************************************************************************************
 */
package com.ibm.icu.stringprep;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.ibm.icu.impl.CharTrie;
import com.ibm.icu.impl.StringPrepDataReader;
import com.ibm.icu.impl.Trie;
import com.ibm.icu.text.Normalizer;
import com.ibm.icu.text.UCharacterIterator;
import com.ibm.icu.text.UTF16;
import com.ibm.icu.util.VersionInfo;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UCharacterDirection;

/**
 * @author ram
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class StringPrep {
    /** 
     * Option to prohibit processing of unassigned code points in the input
     * 
     * @see  usprep_prepare
     * @draft ICU 2.8
     */
    public static final int NONE = 0x0000;

    /** 
     * Option to allow processing of unassigned code points in the input
     * 
     * @see  usprep_prepare
     * @draft ICU 2.8
     */
    public static final int ALLOW_UNASSIGNED = 0x0001;
    
    private static final int UNASSIGNED        = 0x0000; 
    private static final int MAP               = 0x0001; 
    private static final int PROHIBITED        = 0x0002; 
    private static final int LABEL_SEPARATOR   = 0x0003;
    private static final int DELETE            = 0x0004;
    private static final int TYPE_LIMIT        = 0x0005;
    
    private static final int NORMALIZATION_ON  = 0x0001;
    private static final int CHECK_BIDI_ON     = 0x0002;
    
    private static final int TYPE_THRESHOLD       = 0xFFF0;
    private static final int MAX_INDEX_VALUE      = 0x3FBF;   /*16139*/ 
    private static final int MAX_INDEX_TOP_LENGTH = 0x0003;
    
    /* indexes[] value names */
    private static final int INDEX_TRIE_SIZE                  =  0; /* number of bytes in normalization trie */
    private static final int INDEX_MAPPING_DATA_SIZE          =  1; /* The array that contains the mapping   */
    private static final int NORM_CORRECTNS_LAST_UNI_VERSION  =  2; /* The index of Unicode version of last entry in NormalizationCorrections.txt */ 
    private static final int ONE_UCHAR_MAPPING_INDEX_START    =  3; /* The starting index of 1 UChar mapping index in the mapping data array */
    private static final int TWO_UCHARS_MAPPING_INDEX_START   =  4; /* The starting index of 2 UChars mapping index in the mapping data array */
    private static final int THREE_UCHARS_MAPPING_INDEX_START =  5;
    private static final int FOUR_UCHARS_MAPPING_INDEX_START  =  6;
    private static final int OPTIONS                          =  7; /* Bit set of options to turn on in the profile */
    private static final int INDEX_TOP                        = 16;                          /* changing this requires a new formatVersion */
   
   
    /**
     * Default buffer size of datafile
     */
    private static final int DATA_BUFFER_SIZE = 25000;
    
    /* Wrappers for Trie implementations */ 
    private static final class StringPrepTrieImpl implements Trie.DataManipulate{
        static CharTrie sprepTrie = null;
       /**
        * Called by com.ibm.icu.util.Trie to extract from a lead surrogate's 
        * data the index array offset of the indexes for that lead surrogate.
        * @param property data value for a surrogate from the trie, including 
        *        the folding offset
        * @return data offset or 0 if there is no data for the lead surrogate
        */
         public int getFoldingOffset(int value){
            return value;
        }
    }
    
    private static StringPrepTrieImpl sprepTrieImpl;
    private static int[] indexes;
    private static char[] mappingData;
    private static byte[] formatVersion;
    
    private char getCodePointValue(int ch){
        return StringPrepTrieImpl.sprepTrie.getCodePointValue(ch);
    }
    
    //protected 
    private boolean doNFKC = false;
    private boolean checkBiDi = false;
    
    private VersionInfo unicodeVersion;
    private VersionInfo normVersion;
    

    private static VersionInfo getVersionInfo(int comp){
        int micro = comp & 0xFF;
        int milli =(comp >> 8)  & 0xFF;
        int minor =(comp >> 16) & 0xFF;
        int major =(comp >> 24) & 0xFF;
        return VersionInfo.getInstance(major,minor,milli,micro);
    }
    private static VersionInfo getVersionInfo(byte[] version){
        if(version.length != 4){
            return null;
        }
        return VersionInfo.getInstance((int)version[0],(int) version[1],(int) version[2],(int) version[3]);
    }
    
    private StringPrep(InputStream inputStream) throws IOException{

        BufferedInputStream b = new BufferedInputStream(inputStream,DATA_BUFFER_SIZE);
  
        StringPrepDataReader reader = new StringPrepDataReader(b);
        
        // read the indexes            
        indexes = reader.readIndexes(INDEX_TOP);
   
        byte[] sprepBytes = new byte[indexes[INDEX_TRIE_SIZE]];
   
        sprepTrieImpl = new StringPrepTrieImpl();
        //indexes[INDEX_MAPPING_DATA_SIZE] store the size of mappingData in bytes           
        mappingData = new char[indexes[INDEX_MAPPING_DATA_SIZE]/2]; 
        // load the rest of the data data and initialize the data members
        reader.read(sprepBytes,mappingData);
                                   
        StringPrepTrieImpl.sprepTrie   = new CharTrie( new ByteArrayInputStream(sprepBytes),sprepTrieImpl  );
               
        // get the data format version                           
        formatVersion = reader.getDataFormatVersion();
 
        // get the options
        doNFKC            = ((indexes[OPTIONS] & NORMALIZATION_ON) > 0);
        checkBiDi         = ((indexes[OPTIONS] & CHECK_BIDI_ON) > 0);
        unicodeVersion    = getVersionInfo(reader.getUnicodeVersion());
        normVersion       = getVersionInfo(indexes[NORM_CORRECTNS_LAST_UNI_VERSION]);
        if(normVersion.compareTo(UCharacter.getUnicodeVersion())>0){
            throw new IOException("Normalization Correction version not supported");
        }
        b.close();
    }
    /**
     * Returns the StringPrep instance created after reading the input stream.
     * The object does not hold a reference to the input steam, so the stream can be
     * closed after the method returns.
     * 
     * @param inputStream The stream for reading the StringPrep profile binary
     * @return StringPrep object created from the input stream
     * @throws IOException
     * @draft ICU 2.8
     */
    public static final StringPrep getInstance(InputStream inputStream)
        throws IOException{
            
        StringPrep prep = null;
        // load the file and create the object
        prep = new StringPrep(inputStream);
       
        return prep;
    }
    
    private class Values{
        boolean isIndex;
        int value;
        int type;
    }

    private static final void getValues(char trieWord,Values values){
       
        if(trieWord == 0){
            /* 
             * Initial value stored in the mapping table 
             * just return USPREP_TYPE_LIMIT .. so that
             * the source codepoint is copied to the destination
             */
            values.type = TYPE_LIMIT;
        }else if(trieWord >= TYPE_THRESHOLD){
            values.type = (trieWord - TYPE_THRESHOLD);
        }else{
            /* get the type */
            values.type = MAP;
            /* ascertain if the value is index or delta */
            if((trieWord & 0x02)>0){
                values.isIndex = true;
                values.value = trieWord  >> 2; //mask off the lower 2 bits and shift

            }else{
                values.isIndex = false;
                values.value = ((int)(trieWord<<16))>>16;
                values.value =  (values.value >> 2);

            }
 
            if((trieWord>>2) == MAX_INDEX_VALUE){
                values.type = DELETE;
                values.isIndex = false;
                values.value = 0;
            }
        }
    }



    private StringBuffer map( UCharacterIterator iter, int options)
                            throws ParseException{
    
        Values val = new Values();
        char result = 0;
        int ch  = UCharacterIterator.DONE;
        StringBuffer dest = new StringBuffer();
        boolean allowUnassigned = (boolean) ((options & ALLOW_UNASSIGNED)>0);
        
        while((ch=iter.nextCodePoint())!= UCharacterIterator.DONE){
            
            result = getCodePointValue(ch);
            getValues(result,val);

            // check if the source codepoint is unassigned
            if(val.type == UNASSIGNED && allowUnassigned == false){
                 throw new ParseException("An unassigned code point was found in the input",
                                          ParseException.UNASSIGNED_ERROR,
                                          iter.getText(),iter.getIndex());    
            }else if((val.type == MAP)){
                int index, length;

                if(val.isIndex){
                    index = val.value;
                    if(index >= indexes[ONE_UCHAR_MAPPING_INDEX_START] &&
                             index < indexes[TWO_UCHARS_MAPPING_INDEX_START]){
                        length = 1;
                    }else if(index >= indexes[TWO_UCHARS_MAPPING_INDEX_START] &&
                             index < indexes[THREE_UCHARS_MAPPING_INDEX_START]){
                        length = 2;
                    }else if(index >= indexes[THREE_UCHARS_MAPPING_INDEX_START] &&
                             index < indexes[FOUR_UCHARS_MAPPING_INDEX_START]){
                        length = 3;
                    }else{
                        length = mappingData[index++];
                    }
                    /* copy mapping to destination */
                    dest.append(mappingData,index,length);                    
                    continue;
                    
                }else{
                    ch -= val.value;
                }
            }else if(val.type == DELETE){
                // just consume the codepoint and contine
                continue;
            }
            //copy the source into destination
            UTF16.append(dest,ch);
        }
        
        return dest;
    }


    private StringBuffer normalize(StringBuffer src){
        return new StringBuffer(Normalizer.normalize(src.toString(),Normalizer.NFKC,Normalizer.UNICODE_3_2));
    }
    
    protected boolean isLabelSeparator(int ch){
        int result = getCodePointValue(ch);
        if( (result & 0x07)  == LABEL_SEPARATOR){
            return true;
        }
        return false;
    }

     /*
       1) Map -- For each character in the input, check if it has a mapping
          and, if so, replace it with its mapping.  

       2) Normalize -- Possibly normalize the result of step 1 using Unicode
          normalization. 

       3) Prohibit -- Check for any characters that are not allowed in the
          output.  If any are found, return an error.  

       4) Check bidi -- Possibly check for right-to-left characters, and if
          any are found, make sure that the whole string satisfies the
          requirements for bidirectional strings.  If the string does not
          satisfy the requirements for bidirectional strings, return an
          error.  
          [Unicode3.2] defines several bidirectional categories; each character
           has one bidirectional category assigned to it.  For the purposes of
           the requirements below, an "RandALCat character" is a character that
           has Unicode bidirectional categories "R" or "AL"; an "LCat character"
           is a character that has Unicode bidirectional category "L".  Note


           that there are many characters which fall in neither of the above
           definitions; Latin digits (<U+0030> through <U+0039>) are examples of
           this because they have bidirectional category "EN".

           In any profile that specifies bidirectional character handling, all
           three of the following requirements MUST be met:

           1) The characters in section 5.8 MUST be prohibited.

           2) If a string contains any RandALCat character, the string MUST NOT
              contain any LCat character.

           3) If a string contains any RandALCat character, a RandALCat
              character MUST be the first character of the string, and a
              RandALCat character MUST be the last character of the string.
    */
    /**
     * Prepare the input buffer for use in applications with the given profile. This operation maps, normalizes(NFKC),
     * checks for prohited and BiDi characters in the order defined by RFC 3454
     * depending on the options specified in the profile.
     *
     * @param src           A UCharacterIterator object containing the source string
     * @param options       A bit set of options:
     *
     *  - StringPrep.NONE               Prohibit processing of unassigned code points in the input
     *
     *  - StringPrep.ALLOW_UNASSIGNED   Treat the unassigned code points are in the input 
     *                                  as normal Unicode code points.
     *
     * @return StringBuffer A StringBuffer containing the output
     * @throws ParseException
     * @draft ICU 2.8
     */
    public StringBuffer prepare(UCharacterIterator src, int options)
                        throws ParseException{
                                              
        // map 
        StringBuffer mapOut = map(src,options);
        StringBuffer normOut = mapOut;// initialize 
        
        if(doNFKC){
            // normalize 
            normOut = normalize(mapOut);
        }

        int ch;
        char result;
        UCharacterIterator iter = UCharacterIterator.getInstance(normOut);
        Values val = new Values(); 
        int direction=UCharacterDirection.CHAR_DIRECTION_COUNT,
            firstCharDir=UCharacterDirection.CHAR_DIRECTION_COUNT;    
        int rtlPos=-1, ltrPos=-1;
        boolean rightToLeft=false, leftToRight=false;
           
        while((ch=iter.nextCodePoint())!= UCharacterIterator.DONE){
            result = getCodePointValue(ch);
            getValues(result,val);

            if(val.type == PROHIBITED ){
                throw new ParseException("A prohibited code point was found in the input",
                                         ParseException.PROHIBITED_ERROR,iter.getText(),val.value);
            }

            direction = UCharacter.getDirection(ch);
            if(firstCharDir == UCharacterDirection.CHAR_DIRECTION_COUNT){
                firstCharDir = direction;
            }
            if(direction == UCharacterDirection.LEFT_TO_RIGHT){
                leftToRight = true;
                ltrPos = iter.getIndex()-1;
            }
            if(direction == UCharacterDirection.RIGHT_TO_LEFT || direction == UCharacterDirection.RIGHT_TO_LEFT_ARABIC){
                rightToLeft = true;
                rtlPos = iter.getIndex()-1;
            }
        }           
        if(checkBiDi == true){
            // satisfy 2
            if( leftToRight == true && rightToLeft == true){
                throw new ParseException("The input does not conform to the rules for BiDi code points.",
                                         ParseException.CHECK_BIDI_ERROR,iter.getText(),
                                         (rtlPos>ltrPos) ? rtlPos : ltrPos);
             }
    
            //satisfy 3
            if( rightToLeft == true && 
                !((firstCharDir == UCharacterDirection.RIGHT_TO_LEFT || firstCharDir == UCharacterDirection.RIGHT_TO_LEFT_ARABIC) &&
                (direction == UCharacterDirection.RIGHT_TO_LEFT || direction == UCharacterDirection.RIGHT_TO_LEFT_ARABIC))
              ){
                throw new ParseException("The input does not conform to the rules for BiDi code points.",
                                         ParseException.CHECK_BIDI_ERROR,iter.getText(),
                                         (rtlPos>ltrPos) ? rtlPos : ltrPos);
            }
        }
        return normOut;

      }
}
