/**
*******************************************************************************
* Copyright (C) 2006, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
*******************************************************************************
*/ 

package com.ibm.icu.charset;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;

import com.ibm.icu.lang.UCharacter;
/**
 * <p>A subclass of java.nio.Charset for providing implementation of ICU's charset converters.
 * This API is used to convert codepage or character encoded data to and
 * from UTF-16. You can open a converter with {@link Charset#forName } and {@link #forNameICU }. With that
 * converter, you can get its properties, set options, convert your data.</p>
 *
 * <p>Since many software programs recogize different converter names for
 * different types of converters, there are other functions in this API to
 * iterate over the converter aliases. 
 * 
 * @draft ICU 3.6
 * @provisional This API might change or be removed in a future release.
 */
public abstract class CharsetICU extends Charset{
	
     String icuCanonicalName;
     String javaCanonicalName;
     int options;

     float  maxCharsPerByte;
    
     boolean useFallback;
    
     String name; /* +4: 60  internal name of the converter- invariant chars */

     int codepage;               /* +64: 4 codepage # (now IBM-$codepage) */

     byte platform;                /* +68: 1 platform of the converter (only IBM now) */
     byte conversionType;          /* +69: 1 conversion type */

     int minBytesPerChar;         /* +70: 1 Minimum # bytes per char in this codepage */
     int maxBytesPerChar;         /* +71: 1 Maximum # bytes output per UChar in this codepage */

     byte subChar[/*UCNV_MAX_SUBCHAR_LEN*/]; /* +72: 4  [note:  4 and 8 byte boundary] */
     byte subCharLen;              /* +76: 1 */
    
     byte hasToUnicodeFallback;   /* +77: 1 UBool needs to be changed to UBool to be consistent across platform */
     byte hasFromUnicodeFallback; /* +78: 1 */
     short unicodeMask;            /* +79: 1  bit 0: has supplementary  bit 1: has single surrogates */
     byte subChar1;               /* +80: 1  single-byte substitution character for IBM MBCS (0 if none) */
     byte reserved[/*19*/];           /* +81: 19 to round out the structure */
     
     
    /**
     * 
     * @param icuCanonicalName
     * @param canonicalName
     * @param aliases
     * @draft ICU 3.6
     * @provisional This API might change or be removed in a future release.
     */
    protected CharsetICU(String icuCanonicalName, String canonicalName, String[] aliases) {
		super(canonicalName,aliases);
        if(canonicalName.length() == 0){
            throw new IllegalCharsetNameException(canonicalName);
        }
        this.javaCanonicalName = canonicalName;
        this.icuCanonicalName  = icuCanonicalName;
    }
    
    /**
     * Ascertains if a charset is a sub set of this charset
     * Implements the abstract method of super class.
     * @param cs charset to test
     * @return true if the given charset is a subset of this charset
     * @stable ICU 3.6
     */
    public boolean contains(Charset cs){
        if (null == cs) {
            return false;
        } else if (this.equals(cs)) {
            return true;
        }
        return false;
    }
    private static final HashMap algorithmicCharsets = new HashMap();
    static{
        /*algorithmicCharsets.put("BOCU-1",                "com.ibm.icu.charset.CharsetBOCU1" );
        algorithmicCharsets.put("HZ",                    "com.ibm.icu.charset.CharsetHZ" );
        algorithmicCharsets.put("imapmailboxname",       "com.ibm.icu.charset.CharsetIMAP" );
        algorithmicCharsets.put("ISCII",                 "com.ibm.icu.charset.CharsetISCII" );
        algorithmicCharsets.put("iso2022",               "com.ibm.icu.charset.CharsetISO2022" );
        algorithmicCharsets.put("lmbcs1",                "com.ibm.icu.charset.CharsetLMBCS1" );
        algorithmicCharsets.put("lmbcs11",               "com.ibm.icu.charset.CharsetLMBCS11" );
        algorithmicCharsets.put("lmbcs16",               "com.ibm.icu.charset.CharsetLMBCS16" );
        algorithmicCharsets.put("lmbcs17",               "com.ibm.icu.charset.CharsetLMBCS17" );
        algorithmicCharsets.put("lmbcs18",               "com.ibm.icu.charset.CharsetLMBCS18" );
        algorithmicCharsets.put("lmbcs19",               "com.ibm.icu.charset.CharsetLMBCS19" );
        algorithmicCharsets.put("lmbcs2",                "com.ibm.icu.charset.CharsetLMBCS2" );
        algorithmicCharsets.put("lmbcs3",                "com.ibm.icu.charset.CharsetLMBCS3" );
        algorithmicCharsets.put("lmbcs4",                "com.ibm.icu.charset.CharsetLMBCS4" );
        algorithmicCharsets.put("lmbcs5",                "com.ibm.icu.charset.CharsetLMBCS5" );
        algorithmicCharsets.put("lmbcs6",                "com.ibm.icu.charset.CharsetLMBCS6" );
        algorithmicCharsets.put("lmbcs8",                "com.ibm.icu.charset.CharsetLMBCS8" )
        algorithmicCharsets.put("scsu",                  "com.ibm.icu.charset.CharsetSCSU" ); 
        algorithmicCharsets.put("UTF-7",                 "com.ibm.icu.charset.CharsetUTF7" );
        */
        algorithmicCharsets.put("US-ASCII",              "com.ibm.icu.charset.CharsetASCII" );
        algorithmicCharsets.put("ISO-8859-1",            "com.ibm.icu.charset.Charset88591" );
        algorithmicCharsets.put("UTF-16",                "com.ibm.icu.charset.CharsetUTF16" );
        algorithmicCharsets.put("UTF-16BE",              "com.ibm.icu.charset.CharsetUTF16BE" );
        algorithmicCharsets.put("UTF-16LE",              "com.ibm.icu.charset.CharsetUTF16LE" );
        algorithmicCharsets.put("UTF16_OppositeEndian",  "com.ibm.icu.charset.CharsetUTF16LE" );
        algorithmicCharsets.put("UTF16_PlatformEndian",  "com.ibm.icu.charset.CharsetUTF16" );
        algorithmicCharsets.put("UTF-32",                "com.ibm.icu.charset.CharsetUTF32" );
        algorithmicCharsets.put("UTF-32BE",              "com.ibm.icu.charset.CharsetUTF32BE" );
        algorithmicCharsets.put("UTF-32LE",              "com.ibm.icu.charset.CharsetUTF32LE" );
        algorithmicCharsets.put("UTF32_OppositeEndian",  "com.ibm.icu.charset.CharsetUTF32LE" );
        algorithmicCharsets.put("UTF32_PlatformEndian",  "com.ibm.icu.charset.CharsetUTF32" );
        algorithmicCharsets.put("UTF-8",                 "com.ibm.icu.charset.CharsetUTF8" );
        algorithmicCharsets.put("CESU-8",                "com.ibm.icu.charset.CharsetCESU8" );
    }

    /*public*/ static final Charset getCharset(String icuCanonicalName, String javaCanonicalName, String[] aliases){
       String className = (String) algorithmicCharsets.get(icuCanonicalName);
       if(className==null){
           //all the cnv files are loaded as MBCS
           className = "com.ibm.icu.charset.CharsetMBCS";
       }
       try{
           CharsetICU conv = null;
           Class cs = Class.forName(className);
           Class[] paramTypes = new Class[]{ String.class, String.class,  String[].class};
           final Constructor c = cs.getConstructor(paramTypes);
           Object[] params = new Object[]{ icuCanonicalName, javaCanonicalName, aliases};
           
           // Run constructor
           try {
               Object obj = c.newInstance(params);
               if(obj!=null && obj instanceof CharsetICU){
                   conv = (CharsetICU)obj;
                   return conv;
               }
           }catch (InvocationTargetException e) {
               throw new UnsupportedCharsetException( icuCanonicalName+": "+"Could not load " + className+ ". Exception:" + e.getTargetException());    
           }
       }catch(ClassNotFoundException ex){
       }catch(NoSuchMethodException ex){
       }catch (IllegalAccessException ex){ 
       }catch (InstantiationException ex){ 
       }
       throw new UnsupportedCharsetException( icuCanonicalName+": "+"Could not load " + className);    
    }
    
    static final boolean isSurrogate(int c){
        return (((c)&0xfffff800)==0xd800);
    }
    
    /**
     * Always use fallbacks from codepage to Unicode?
     * @draft ICU 3.6
     * @provisional This API might change or be removed in a future release.
     */
    final boolean isToUUseFallback() {
        return true;
    }    
    
    /**
     * Use fallbacks from Unicode to codepage when useFallback or for private-use code points
     * @param c A codepoint
     * @draft ICU 3.6
     * @provisional This API might change or be removed in a future release.
     */
    final boolean isFromUUseFallback(int c) {
        return (useFallback) || isPrivateUse(c);
    }
    
    /**
     * Returns the default charset name 
     * @draft ICU 3.6
     * @provisional This API might change or be removed in a future release.
     */
     static final String getDefaultCharsetName(){
        String defaultEncoding = new InputStreamReader(new ByteArrayInputStream(new byte[0])).getEncoding();
        return defaultEncoding;
    }
    
    static final boolean isPrivateUse(int c) {
        return (UCharacter.getType(c) == UCharacter.PRIVATE_USE);
    }

    /**
     * Returns a charset object for the named charset.
     * This method gurantee that ICU charset is returned when
     * available.  If the ICU charset provider does not support
     * the specified charset, then try other charset providers
     * including the standard Java charset provider.
     * 
     * @param charsetName The name of the requested charset,
     * may be either a canonical name or an alias
     * @return A charset object for the named charset
     * @throws IllegalCharsetNameException If the given charset name
     * is illegal
     * @throws UnsupportedCharsetException If no support for the
     * named charset is available in this instance of th Java
     * virtual machine
     * @draft ICU 3.6
     * @provisional This API might change or be removed in a future release.
     */
    public static Charset forNameICU(String charsetName) throws IllegalCharsetNameException, UnsupportedCharsetException {
        CharsetProviderICU icuProvider = new CharsetProviderICU();
        CharsetICU cs = (CharsetICU) icuProvider.charsetForName(charsetName);
        if (cs != null) {
            return cs;
        }
        return Charset.forName(charsetName);
    }
    
    /**
     * This follows ucnv.c method ucnv_detectUnicodeSignature() to detect the
     * start of the stream for example U+FEFF (the Unicode BOM/signature
     * character) that can be ignored.
     * 
     * Detects Unicode signature byte sequences at the start of the byte stream
     * and returns number of bytes of the BOM of the indicated Unicode charset.
     * 0 is returned when no Unicode signature is recognized.
     * 
     */
    static String detectUnicodeSignature(ByteBuffer source) {
        int signatureLength = 0; // number of bytes of the signature
        final int SIG_MAX_LEN = 5;
        String sigUniCharset = null; // states what unicode charset is the BOM
        int i = 0;

        /*
         * initial 0xa5 bytes: make sure that if we read <SIG_MAX_LEN bytes we
         * don't misdetect something
         */
        byte start[] = { (byte) 0xa5, (byte) 0xa5, (byte) 0xa5, (byte) 0xa5,
                (byte) 0xa5 };

        while (i < source.remaining() && i < SIG_MAX_LEN) {
            start[i] = source.get(i);
            i++;
        }

        if (start[0] == (byte) 0xFE && start[1] == (byte) 0xFF) {
            signatureLength = 2;
            sigUniCharset = "UTF-16BE";
            source.position(signatureLength);
            return sigUniCharset;
        } else if (start[0] == (byte) 0xFF && start[1] == (byte) 0xFE) {
            if (start[2] == (byte) 0x00 && start[3] == (byte) 0x00) {
                signatureLength = 4;
                sigUniCharset = "UTF-32LE";
                source.position(signatureLength);
                return sigUniCharset;
            } else {
                signatureLength = 2;
                sigUniCharset = "UTF-16LE";
                source.position(signatureLength);
                return sigUniCharset;
            }
        } else if (start[0] == (byte) 0xEF && start[1] == (byte) 0xBB
                && start[2] == (byte) 0xBF) {
            signatureLength = 3;
            sigUniCharset = "UTF-8";
            source.position(signatureLength);
            return sigUniCharset;
        } else if (start[0] == (byte) 0x00 && start[1] == (byte) 0x00
                && start[2] == (byte) 0xFE && start[3] == (byte) 0xFF) {
            signatureLength = 4;
            sigUniCharset = "UTF-32BE";
            source.position(signatureLength);
            return sigUniCharset;
        } else if (start[0] == (byte) 0x0E && start[1] == (byte) 0xFE
                && start[2] == (byte) 0xFF) {
            signatureLength = 3;
            sigUniCharset = "SCSU";
            source.position(signatureLength);
            return sigUniCharset;
        } else if (start[0] == (byte) 0xFB && start[1] == (byte) 0xEE
                && start[2] == (byte) 0x28) {
            signatureLength = 3;
            sigUniCharset = "BOCU-1";
            source.position(signatureLength);
            return sigUniCharset;
        } else if (start[0] == (byte) 0x2B && start[1] == (byte) 0x2F
                && start[2] == (byte) 0x76) {

            if (start[3] == (byte) 0x38 && start[4] == (byte) 0x2D) {
                signatureLength = 5;
                sigUniCharset = "UTF-7";
                source.position(signatureLength);
                return sigUniCharset;
            } else if (start[3] == (byte) 0x38 || start[3] == (byte) 0x39
                    || start[3] == (byte) 0x2B || start[3] == (byte) 0x2F) {
                signatureLength = 4;
                sigUniCharset = "UTF-7";
                source.position(signatureLength);
                return sigUniCharset;
            }
        } else if (start[0] == (byte) 0xDD && start[2] == (byte) 0x73
                && start[2] == (byte) 0x66 && start[3] == (byte) 0x73) {
            signatureLength = 4;
            sigUniCharset = "UTF-EBCDIC";
            source.position(signatureLength);
            return sigUniCharset;
        }

        /* no known Unicode signature byte sequence recognized */
        return null;
    }

}

