/*
 * Created on Apr 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.ibm.icu.text;

import java.util.Arrays;

/**
 * CharsetRecognizer implemenation for Asian  - double or multi-byte - charsets.
 *                   Match is determined mostly by the input data adhering to the
 *                   encoding scheme for the charset, and, optionally,
 *                   frequency-of-occurence of characters.
 * <p/>
 *                   Instances of this class are singletons, one per encoding
 *                   being recognized.  They are created in the main
 *                   CharsetDetector class and kept in the global list of available
 *                   encodings to be checked.  The specific encoding being recognized
 *                   is determined by subclass.
 *                   
 */
abstract class CharsetRecog_mbcs extends CharsetRecognizer {

    
     
    /**
     * Get the IANA name of this charset.
     * @return the charset name.
     */
    abstract String      getName() ;
    
    
    /**
     * Test the match of this charset with the input text data
     *      which is obtained via the CharsetDetector object.
     * 
     * @param det  The CharsetDetector, which contains the input text
     *             to be checked for being in this charset.
     * @return     Two values packed into one int  (Damn java, anyhow)
     *             <br/>
     *             bits 0-7:  the match confidence, ranging from 0-100
     *             <br/>
     *             bits 8-15: The match reason, an enum-like value.
     */
    int         match(CharsetDetector det, int [] commonChars) {
        int   singleByteCharCount = 0;
        int   doubleByteCharCount = 0;
        int   commonCharCount     = 0;
        int   badCharCount        = 0;
        int   totalCharCount      = 0;
        int   confidence          = 0;
        iteratedChar   iter       = new iteratedChar();
        
        detectBlock: {
            for (iter.reset(); nextChar(iter, det);) {
                totalCharCount++;
                if (iter.error) {
                    badCharCount++; 
                } else {
                    
                    if (iter.charValue <= 0xff) {
                        singleByteCharCount++;
                    } else {
                        doubleByteCharCount++;
                        if (commonChars != null) {
                            if (Arrays.binarySearch(commonChars, iter.charValue) >= 0){
                                commonCharCount++;
                            }
                        }
                    }
                }
                if (badCharCount >= 2 && badCharCount*5 >= doubleByteCharCount) {
                    // Bail out early if the byte data is not matching the encoding scheme.
                    break detectBlock;
                }
            }
            
            if (doubleByteCharCount == 0 && badCharCount== 0) {
                // No multi-byte chars.
                //   ASCII file?  It's probably not our encoding,
                //   but is not incompatible with our encoding, so don't give it a zero.
                confidence = 10;
                break detectBlock;
            }
            
            //
            //  No match if there are too many characters that don't fit the encoding scheme.
            //    (should we have zero tolerance for these?)
            //
            if (doubleByteCharCount < 20*badCharCount) {
                confidence = 0;
                break detectBlock;
            }
            
            if (commonChars == null) {
                // We have no statistics on frequently occuring characters.
                //  Assess confidence purely on having a reasonable number of
                //  multi-byte characters (the more the better
                confidence = 30 + doubleByteCharCount - 20*badCharCount;
                if (confidence > 100) {
                    confidence = 100;
                }
            }else {
                //
                // Frequency of occurence statistics exist.
                //
                double maxVal = Math.log((float)doubleByteCharCount / 4);
                double scaleFactor = 90.0 / maxVal;
                confidence = (int)(Math.log(commonCharCount+1) * scaleFactor + 10);
                confidence = Math.min(confidence, 100);
            }
        }   // end of detectBlock:
        
        return confidence;
    }
    
     // "Character"  iterated character class.
     //    Recognizers for specific mbcs encodings make their "characters" available
     //    by providing a nextChar() function that fills in an instance of iteratedChar
     //    with the next char from the input.
     //    The returned characters are not converted to Unicode, but remain as the raw
     //    bytes (concatenated into an int) from the codepage data.
     //
     //  For Asian charsets, use the raw input rather than the input that has been
     //   stripped of markup.  Detection only considers multi-byte chars, effectively
     //   stripping markup anyway, and double byte chars do occur in markup too.
     //
     static class iteratedChar {
         int             charValue = 0;             // 1-4 bytes from the raw input data
         int             index     = 0;
         int             nextIndex = 0;
         boolean         error     = false;
         boolean         done      = false;
         
         void reset() {
             charValue = 0;
             index     = -1;
             nextIndex = 0;
             error     = false;
             done      = false;
         }
         
         int nextByte(CharsetDetector det) {
             if (nextIndex >= det.fRawLength) {
                 done = true;
                 return -1;
             }
             int byteValue = (int)det.fRawInput[nextIndex++] & 0x00ff;
             return byteValue;
         }       
     }
     
     /**
      * Get the next character (however many bytes it is) from the input data
      *    Subclasses for specific charset encodings must implement this function
      *    to get characters according to the rules of their encoding scheme.
      * 
      *  This function is not a method of class iteratedChar only because
      *   that would require a lot of extra derived classes, which is awkward.
      * @param it  The iteratedChar "struct" into which the returned char is placed.
      * @param det The charset detector, which is needed to get at the input byte data
      *            being iterated over.
      * @return    True if a character was returned, false at end of input.
      */
     abstract boolean nextChar(iteratedChar it, CharsetDetector det);
     


     
     
     /**
      *   Shift-JIS charset recognizer.   
      *
      */
     static class CharsetRecog_sjis extends CharsetRecog_mbcs {
         
         boolean nextChar(iteratedChar it, CharsetDetector det) {
             it.index = it.nextIndex;
             it.error = false;
             int firstByte;
             firstByte = it.charValue = it.nextByte(det);
             if (firstByte < 0) {
                 return false;
             }
             
             if (firstByte <= 0x7f || (firstByte>0xa0 && firstByte<=0xdf)) {
                 return true;
             }
             
             int secondByte = it.nextByte(det);
             if (secondByte < 0)  {
                 return false;          
             }
             it.charValue = firstByte << 8 + secondByte;
             if (! ((secondByte>=0x40 && secondByte<=0x7f) || (secondByte>=0x80 && secondByte<=0xff))) {
                 // Illegal second byte value.
                 it.error = true;
             }
             return true;
         }
         
         int match(CharsetDetector det) {
             return match(det, null);
         }
         
         String getName() {
             return "SHIFT_JIS";
         }
         
     }
     
     
     /**
      *   EUC charset recognizers.  One abstract class that provides the common function
      *             for getting the next character according to the EUC encoding scheme,
      *             and nested derived classes for EUC_KR, EUC_JP, EUC_CN.   
      *
      */
     abstract static class CharsetRecog_euc extends CharsetRecog_mbcs {
         
         /*
          *  (non-Javadoc)
          *  Get the next character value for EUC based encodings.
          *  Character "value" is simply the raw bytes that make up the character
          *     packed into an int.
          */
         boolean nextChar(iteratedChar it, CharsetDetector det) {
             it.index = it.nextIndex;
             it.error = false;
             int firstByte  = 0;
             int secondByte = 0;
             int thirdByte  = 0;
             int fourthByte = 0;
             
             buildChar: {
                 firstByte = it.charValue = it.nextByte(det);                 
                 if (firstByte < 0) {
                     // Ran off the end of the input data
                     it.done = true;
                     break buildChar;
                 }
                 if (firstByte <= 0x8d) {
                     // single byte char
                     break buildChar;
                 }
                 
                 secondByte = it.nextByte(det);
                 it.charValue = (it.charValue << 8) | secondByte;
                 
                 if (firstByte >= 0xA1 && firstByte <= 0xfe) {
                     // Two byte Char
                     if (secondByte < 0xa1) {
                         it.error = true;
                     }
                     break buildChar;
                 }
                 if (firstByte == 0x8e) {
                     // Code Set 2.
                     //   In EUC-JP, total char size is 2 bytes, only one byte of actual char value.
                     //   In EUC-TW, total char size is 4 bytes, three bytes contribute to char value.
                     // We don't know which we've got.
                     // Treat it like EUC-JP.  If the data really was EUC-TW, the following two
                     //   bytes will look like a well formed 2 byte char.  
                     if (secondByte < 0xa1) {
                         it.error = true;
                     }
                     break buildChar;                     
                 }
                 
                 if (firstByte == 0x8f) {
                     // Code set 3.
                     // Three byte total char size, two bytes of actual char value.
                     thirdByte    = it.nextByte(det);
                     it.charValue = (it.charValue << 8) | thirdByte;
                     if (thirdByte < 0xa1) {
                         it.error = true;
                     }
                 }
              }
             
             return (it.done == false);
         }
         
         /**
          * The charset recognize for EUC-JP.  A singleton instance of this class
          *    is created and kept by the public CharsetDetector class
          */
         static class CharsetRecog_euc_jp extends CharsetRecog_euc {
             static int [] commonChars = 
                 // TODO:  This set of data comes from the character frequency-
                 //        of-occurence analysis tool.  The data needs to be moved
                 //        into a resource and loaded from there.
                    {0xa4ce, 0xa4c7, 0xa4a4, 0xa1bc, 0xa1a2, 0xa4b7, 0xa4cb, 0xa4b9, 0xa1a3, 0xa4c6, 
                     0xa4c8, 0xa4cf, 0xa4de, 0xa4f2, 0xa4eb, 0xa4ca, 0xa4ac, 0xa5f3, 0xa4bf, 0xa5b9, 
                     0xa4ec, 0xa5a4, 0xa4a6, 0xa4ab, 0xa5c8, 0xa4b3, 0xa1a6, 0xa4e2, 0xa5eb, 0xa5af, 
                     0xa4ea, 0xa4e9, 0xa1a1, 0xa5c3, 0xa5e9, 0xa4c3, 0xa5ea, 0xa4ad, 0xa5d7, 0xa4b5, 
                     0xa4f3, 0xa4a2, 0xa5c9, 0xc6fc, 0xa1d6, 0xa1d7, 0xa5bf, 0xa4e8, 0xa5b8, 0xa4af, 
                     0xa5e1, 0xa4a8, 0xa4bb, 0xa4bd, 0xa4c0, 0xa5a2, 0xa5d5, 0xa4b1, 0xbfb7, 0xa4aa, 
                     0xa4c4, 0xa5b5, 0xbbc8, 0xa5d6, 0xa4c9, 0xcaf3, 0xa5b7, 0xcbdc, 0xc4ea, 0xa5a6, 
                     0xa4d0, 0xa5e5, 0xcdd1, 0xa4e1, 0xa4df, 0xa5d0, 0xa5a3, 0xb8a9, 0xa5b3, 0xa5de, 
                     0xa5ed, 0xa5a7, 0xa5b0, 0xa5e0, 0xa4ef, 0xb9d4, 0xa5aa, 0xa5c6, 0xbef0, 0xcab8, 
                     0xa1ca, 0xa1cb, 0xa5cb, 0xbaee, 0xa4c1, 0xa5ad, 0xa5c7, 0xa4e4, 0xa5ec, 0xc7bd};
             
             String getName() {
                 return "EUC_JP";
             }
             
             int match(CharsetDetector det) {
                 return match(det, commonChars);
             }
         }

     
         
         /**
          * The charset recognize for EUC-KR.  A singleton instance of this class
          *    is created and kept by the public CharsetDetector class
          */
         static class CharsetRecog_euc_kr extends CharsetRecog_euc {
             static int [] commonChars = 
                 // TODO:  This set of data comes from the character frequency-
                 //        of-occurence analysis tool.  The data needs to be moved
                 //        into a resource and loaded from there.
                    {0xc0cc, 0xb4d9, 0xb4c2, 0xc0c7, 0xbfa1, 0xc7cf, 0xb0a1, 0xb0ed, 0xc7d1, 0xc1f6, 
                     0xc0bb, 0xb7ce, 0xb1e2, 0xbcad, 0xc0ba, 0xbbe7, 0xc1a4, 0xc0da, 0xb5b5, 0xb8a6, 
                     0xbeee, 0xb4cf, 0xbcf6, 0xbdc3, 0xb1d7, 0xb4eb, 0xb8ae, 0xc0ce, 0xb3aa, 0xbec6, 
                     0xc0d6, 0xbab8, 0xb5e9, 0xb6f3, 0xc7d8, 0xb0cd, 0xc0cf, 0xbdba, 0xc0b8, 0xb1b9, 
                     0xc1a6, 0xb9fd, 0xbbf3, 0xb0d4, 0xb8e9, 0xb8b8, 0xb0fa, 0xc0fb, 0xbace, 0xc1d6, 
                     0xbfa9, 0xc0fc, 0xbfeb, 0xb9ae, 0xc6ae, 0xbbfd, 0xbcba, 0xc0a7, 0xbff8, 0xb5c7, 
                     0xbfe4, 0xbfec, 0xbdc5, 0xc7d2, 0xc7e5, 0xb0fc, 0xb1b8, 0xbaf1, 0xbedf, 0xc5cd, 
                     0xb8b6, 0xbdc0, 0xb7af, 0xb5bf, 0xb3bb, 0xc8ad, 0xc0bd, 0xb0b3, 0xc4a1, 0xb7c2, 
                     0xb9ab, 0xc0af, 0xbef8, 0xb5a5, 0xbcd2, 0xb9ce, 0xc1df, 0xbfc0, 0xc1f8, 0xb0e6, 
                     0xb1c7, 0xbad0, 0xbefa, 0xc0e5, 0xbec8, 0xc1b6, 0xb8bb, 0xb0f8, 0xb9cc, 0xb0c5};
             
             String getName() {
                 return "EUC_KR";
             }
             
             int match(CharsetDetector det) {
                 return match(det, commonChars);
             }
         }
         
         
         /**
          * The charset recognize for EUC-CN.  A singleton instance of this class
          *    is created and kept by the public CharsetDetector class
          */
         static class CharsetRecog_euc_cn extends CharsetRecog_euc {
             static int [] commonChars = 
                 // TODO:  This set of data comes from the character frequency-
                 //        of-occurence analysis tool.  The data needs to be moved
                 //        into a resource and loaded from there.
             {0xa3ac, 0xb5c4, 0xa1a1, 0xa1a4, 0xa1a3, 0xcac7, 0xd2bb, 0xb4f3, 0xd4da, 0xd6d0, 
                     0xcafd, 0xd3d0, 0xa1f3, 0xb2bb, 0xa3ba, 0xbbfa, 0xc8cb, 0xa1a2, 0xd3c3, 0xd1a7, 
                     0xc8d5, 0xbedd, 0xb8f6, 0xd0c2, 0xcdf8, 0xd2aa, 0xb9fa, 0xc1cb, 0xc9cf, 0xa1b0, 
                     0xa1b1, 0xced2, 0xbcfe, 0xcec4, 0xd2d4, 0xc4dc, 0xc0b4, 0xd4c2, 0xcab1, 0xd0d0, 
                     0xbdcc, 0xbfc9, 0xb6d4, 0xbcdb, 0xb1be, 0xb3f6, 0xb8b4, 0xc9fa, 0xb1b8, 0xbcbc, 
                     0xcfc2, 0xbacd, 0xbecd, 0xb3c9, 0xd5e2, 0xb8df, 0xb7d6, 0xc5cc, 0xbfc6, 0xbbe1, 
                     0xceaa, 0xc8e7, 0xcfb5, 0xa1f1, 0xc4ea, 0xb1a8, 0xb6af, 0xc0ed, 0xd3fd, 0xb7a2, 
                     0xc8ab, 0xb7bd, 0xcee5, 0xc2db, 0xbba7, 0xd0d4, 0xb9c9, 0xc3c7, 0xb9fd, 0xcad0, 
                     0xb5e3, 0xbbd6, 0xcfd6, 0xcab5, 0xd2b2, 0xbfb4, 0xb6e0, 0xccec, 0xc7f8, 0xd0c5, 
                     0xcad6, 0xb9d8, 0xb5bd, 0xb7dd, 0xc6f7, 0xcaf5, 0xa3a1, 0xb7a8, 0xb9ab, 0xd2b5, 
                     0xcbf9, 0xcdbc, 0xc6e4, 0xd3da, 0xd0a1, 0xd1a1, 0xd3ce, 0xbfaa, 0xb4e6, 0xc4bf, 
                     0xd7f7, 0xb5e7, 0xcdb3, 0xc7e9, 0xd7ee, 0xc6c0, 0xcfdf, 0xb5d8, 0xb5c0, 0xbead, 
                     0xb4c5, 0xc6b7, 0xc4da, 0xd0c4, 0xb9a4, 0xd4aa, 0xc2bc, 0xc3c0, 0xbaf3, 0xcabd, 
                     0xbcd2, 0xcef1, 0xbdab, 0xa3ad, 0xa3bf, 0xb3a4, 0xb9fb, 0xd6ae, 0xc1bf, 0xbbd8, 
                     0xb8f1, 0xb6f8, 0xb6a8, 0xcde2, 0xbac3, 0xb3cc, 0xccd8, 0xd7d4, 0xcbb5};
             
             String getName() {
                 return "EUC_CN";
             }
             
             int match(CharsetDetector det) {
                 return match(det, commonChars);
             }
         }
     }
     
     
}
