/*
 *******************************************************************************
 * Copyright (C) 2007, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
/**
 * @author aheninger
 *         Port from ICU4C  6/10/2007
 *
 */
package com.ibm.icu.charset;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;

import com.ibm.icu.charset.CharsetMBCS.LoadArguments;
import com.ibm.icu.impl.InvalidFormatException;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.UTF16;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.impl.UCharacterProperty;

public class Charset2022 extends CharsetICU {
    
    // TODO: change converterArray back to an array of UConverterSharedData, which is lighter weight.
    int                    variant;           // one of enum {ISO_2022_JP, ISO_2022_KR, or ISO_2022_CN}
    CharsetMBCS[]          converterArray = new CharsetMBCS[UCNV_2022_MAX_CONVERTERS];
    int                    version2022;

    
    
    public Charset2022(String icuCanonicalName, String javaCanonicalName, String[] aliases) {
        super(icuCanonicalName, javaCanonicalName, aliases);
        
        int version = 0;
        int versionIndex = icuCanonicalName.indexOf("version=");
        if (versionIndex>0) {
            try {
                version = Integer.decode(icuCanonicalName.substring(versionIndex+8, versionIndex+9)).intValue();
            } catch (NumberFormatException e) {
                throw new UnsupportedCharsetException(icuCanonicalName);
            }
        }
            
        String locale = ULocale.getDefault().getLanguage();
        if (icuCanonicalName.equals("JIS7")) {
            ISO2022InitJP(3);
        } else if (icuCanonicalName.equals("JIS8")) {
            ISO2022InitJP(4);
        } else if (icuCanonicalName.indexOf("locale=ja")>0) {
            ISO2022InitJP(version);
        } else if (icuCanonicalName.indexOf("locale=zh")>0) {
            ISO2022InitCN(version);
        } else if (icuCanonicalName.indexOf("locale=ko")>0) {
            ISO2022InitKR(version);
        } else if (locale.equals("ja")) {
            ISO2022InitJP(version);
        } else if (locale.equals("zh")) {
            ISO2022InitCN(version);
        } else if (locale.equals("ko")) {
            ISO2022InitKR(version);
        } else {
            throw new UnsupportedCharsetException(icuCanonicalName);
        }
         
        /*
        CharsetMBCS.LoadArguments args = new CharsetMBCS.LoadArguments(1, icuCanonicalName);
        sharedData = CharsetMBCS.loadConverter(args);
           
        maxBytesPerChar = sharedData.staticData.maxBytesPerChar;
        minBytesPerChar = sharedData.staticData.minBytesPerChar;
        maxCharsPerByte = 1;
        fromUSubstitution = sharedData.staticData.subChar;
        subChar = sharedData.staticData.subChar;
        subCharLen = sharedData.staticData.subCharLen;
        subChar1 = sharedData.staticData.subChar1;
        fromUSubstitution = new byte[sharedData.staticData.subCharLen];
        System.arraycopy(sharedData.staticData.subChar, 0, fromUSubstitution, 0, sharedData.staticData.subCharLen);
        
        // Todo: pass options
        initializeConverter(0);
        */

    }

    
    static final String SHIFT_IN_STR  = "\u000F";
    static final String SHIFT_OUT_STR = "\u000E";

    static final char  CR    = '\r';
    static final char  LF    = '\n';
    static final char  H_TAB = '\u0009';
    static final char  V_TAB = '\u000B';
    static final char  SPACE = '\u0020';
    static final int   ESC_2022 = 0x1B; /*ESC*/
    static final int   UCNV_SI = 0x0F;
    static final int   UCNV_SO = 0x0E;

    
    /*
     * ISO 2022 control codes must not be converted from Unicode
     * because they would mess up the byte stream.
     * The bit mask 0x0800c000 has bits set at bit positions 0xe, 0xf, 0x1b
     * corresponding to SO, SI, and ESC.
     */
    private static boolean  IS_2022_CONTROL(int c) {
       return (((c)<(char)0x20) && (1<<((int)c)&0x0800c000)!=0);
    }

    /* for ISO-2022-JP and -CN implementations */
    //  enum StateEnum {
            /* shared values */
            private static final short INVALID_STATE=-1;
            private static final short ASCII = 0;

            private static final short SS2_STATE=0x10;
            private static final short SS3_STATE=0x11;

            /* JP */
            private static final short ISO8859_1  = 1 ;
            private static final short ISO8859_7  = 2 ;
            private static final short JISX201    = 3;
            private static final short JISX208    = 4;
            private static final short JISX212    = 5;
            private static final short GB2312     = 6;
            private static final short KSC5601    = 7;
            private static final short HWKANA_7BIT= 8;    /* Halfwidth Katakana 7 bit */

            /* CN */
            /* the first few enum constants must keep their values because they correspond to myConverterArray[] */
            private static final short GB2312_1   = 1;
            private static final short ISO_IR_165 = 2;
            private static final short CNS_11643  = 3;

            /*
             * these are used in StateEnum and ISO2022State variables,
             * but CNS_11643 must be used to index into myConverterArray[]
             */
            private static final short CNS_11643_0=0x20;
            private static final short CNS_11643_1=0x21;
            private static final short CNS_11643_2=0x22;
            private static final short CNS_11643_3=0x23;
            private static final short CNS_11643_4=0x24;
            private static final short CNS_11643_5=0x25;
            private static final short CNS_11643_6=0x26;
            private static final short CNS_11643_7=0x27;
    //  } StateEnum;
            
   /* is the StateEnum charset value for a DBCS charset? */
   private static boolean IS_JP_DBCS(int cs) {
       return  (JISX208<=cs && cs<=KSC5601);
   }

   private static short CSM(short cs) { 
       return (short)(1<<(cs));
   }

   /*
    * Each of these charset masks (with index x) contains a bit for a charset in exact correspondence
    * to whether that charset is used in the corresponding version x of ISO_2022,locale=ja,version=x
    *
    * Note: The converter uses some leniency:
    * - The escape sequence ESC ( I for half-width 7-bit Katakana is recognized in
    *   all versions, not just JIS7 and JIS8.
    * - ICU does not distinguish between different versions of JIS X 0208.
    */
   private static short[] jpCharsetMasks  = new short[]  {
            (short)(CSM(ASCII)|CSM(JISX201)|CSM(JISX208)|CSM(HWKANA_7BIT)),
            (short)(CSM(ASCII)|CSM(JISX201)|CSM(JISX208)|CSM(HWKANA_7BIT)|CSM(JISX212)),
            (short)(CSM(ASCII)|CSM(JISX201)|CSM(JISX208)|CSM(HWKANA_7BIT)|CSM(JISX212)|CSM(GB2312)|CSM(KSC5601)|
                               CSM(ISO8859_1)|CSM(ISO8859_7)),
            (short)(CSM(ASCII)|CSM(JISX201)|CSM(JISX208)|CSM(HWKANA_7BIT)|CSM(JISX212)|CSM(GB2312)|CSM(KSC5601)|
                               CSM(ISO8859_1)|CSM(ISO8859_7)),
            (short)(CSM(ASCII)|CSM(JISX201)|CSM(JISX208)|CSM(HWKANA_7BIT)|CSM(JISX212)|CSM(GB2312)|CSM(KSC5601)|
                               CSM(ISO8859_1)|CSM(ISO8859_7)),
   };
   

    // enum Cnv2022Type
    private static final int  ASCII1 = 0;
    private static final int  LATIN1 = 1;
    private static final int  SBCS   = 2;
    private static final int  DBCS   = 3;
    private static final int  MBCS   = 4;
    private static final int  HWKANA = 5;
    
    class ISO2022State {
        byte[] cs;          /* charset number for SI (G0)/SO (G1)/SS2 (G2)/SS3 (G3) */
        byte   g;           /* 0..3 for G0..G3 (SI/SO/SS2/SS3) */
        byte   prevG;       /* g before single shift (SS2 or SS3) */
        
        ISO2022State() {
            cs = new byte[4];
        }
        
        void reset() {
            Arrays.fill(this.cs, (byte)(0));
            this.g = 0;
            this.prevG = 0;
        }
        
        void copyFrom(ISO2022State other) {
            for (int i=0; i<cs.length; i++) {cs[i] = other.cs[i];}
            g = other.g;
            prevG = other.prevG;
        }
    }

    private static final int UCNV_OPTIONS_VERSION_MASK = 0x000f;
    private static final int UCNV_2022_MAX_CONVERTERS  = 10;  // Number of internal sub-converter types,
                                                               //  not a limit to the number of user level 2022 converters.


    // enum UCNV_TableStates_2022
    private static final byte   INVALID_2022              = -1; /*Doesn't correspond to a valid iso 2022 escape sequence*/
    private static final byte   VALID_NON_TERMINAL_2022   =  0; /*so far corresponds to a valid iso 2022 escape sequence*/
    private static final byte   VALID_TERMINAL_2022       =  1; /*corresponds to a valid iso 2022 escape sequence*/
    private static final byte   VALID_MAYBE_TERMINAL_2022 =  2; /*so far matches one iso 2022 escape sequence, but by adding more characters might match another escape sequence*/

    /*
    * The way these state transition arrays work is:
    * ex : ESC$B is the sequence for JISX208
    *      a) First Iteration: char is ESC
    *          i) Get the value of ESC from normalize_esq_chars_2022[] with int value of ESC as index
    *             int x = normalize_esq_chars_2022[27] which is equal to 1
    *         ii) Search for this value in escSeqStateTable_Key_2022[]
    *             value of x is stored at escSeqStateTable_Key_2022[0]
    *        iii) Save this index as offset
    *         iv) Get state of this sequence from escSeqStateTable_Value_2022[]
    *             escSeqStateTable_Value_2022[offset], which is VALID_NON_TERMINAL_2022
    *     b) Switch on this state and continue to next char
    *          i) Get the value of $ from normalize_esq_chars_2022[] with int value of $ as index
    *             which is normalize_esq_chars_2022[36] == 4
    *         ii) x is currently 1(from above)
    *               x<<=5 -- x is now 32
    *               x+=normalize_esq_chars_2022[36]
    *               now x is 36
    *        iii) Search for this value in escSeqStateTable_Key_2022[]
    *             value of x is stored at escSeqStateTable_Key_2022[2], so offset is 2
    *         iv) Get state of this sequence from escSeqStateTable_Value_2022[]
    *             escSeqStateTable_Value_2022[offset], which is VALID_NON_TERMINAL_2022
    *     c) Switch on this state and continue to next char
    *        i)  Get the value of B from normalize_esq_chars_2022[] with int value of B as index
    *        ii) x is currently 36 (from above)
    *            x<<=5 -- x is now 1152
    *            x+=normalize_esq_chars_2022[66]
    *            now x is 1161
    *       iii) Search for this value in escSeqStateTable_Key_2022[]
    *            value of x is stored at escSeqStateTable_Key_2022[21], so offset is 21
    *        iv) Get state of this sequence from escSeqStateTable_Value_2022[21]
    *            escSeqStateTable_Value_2022[offset], which is VALID_TERMINAL_2022
    *         v) Get the converter name form escSeqStateTable_Result_2022[21] which is JISX208
    */
    
    
    /*Below are the 3 arrays depicting a state transition table*/
    // This array maps each byte value that can appear in an escape sequence to a new
    //   value between 1 and 31.  This allows a single escape sequence of up to 6 bytes
    //   to be represented as a single int by packing the bits, 5 bits per original byte.
    // This int representation of a sequence is referred to as the "key".
    static final byte[]  normalize_esq_chars_2022 = new byte[]  {
    /*       0      1       2       3       4      5       6        7       8       9           */

             0     ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0
            ,0     ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0
            ,0     ,0      ,0      ,0      ,0      ,0      ,0      ,1      ,0      ,0
            ,0     ,0      ,0      ,0      ,0      ,0      ,4      ,7      ,29      ,0
            ,2     ,24     ,26     ,27     ,0      ,3      ,23     ,6      ,0      ,0
            ,0     ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0
            ,0     ,0      ,0      ,0      ,5      ,8      ,9      ,10     ,11     ,12
            ,13    ,14     ,15     ,16     ,17     ,18     ,19     ,20     ,25     ,28
            ,0     ,0      ,21     ,0      ,0      ,0      ,0      ,0      ,0      ,0
            ,22    ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0
            ,0     ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0
            ,0     ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0
            ,0     ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0
            ,0     ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0
            ,0     ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0
            ,0     ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0
            ,0     ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0
            ,0     ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0
            ,0     ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0
            ,0     ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0
            ,0     ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0
            ,0     ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0
            ,0     ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0
            ,0     ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0
            ,0     ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0      ,0
            ,0     ,0      ,0      ,0      ,0      ,0
    };
    
 
    
    /*
     * When the generic ISO-2022 converter is completely removed, not just disabled
     * per #ifdef, then the following state table and the associated tables that are
     * dimensioned with MAX_STATES_2022 should be trimmed.
     *
     * Especially, VALID_MAYBE_TERMINAL_2022 will not be used any more, and all of
     * the associated escape sequences starting with ESC ( B should be removed.
     * This includes the ones with key values 1097 and all of the ones above 1000000.
     *
     * For the latter, the tables can simply be truncated.
     * For the former, since the tables must be kept parallel, it is probably best
     * to simply duplicate an adjacent table cell, parallel in all tables.
     *
     * It may make sense to restructure the tables, especially by using small search
     * tables for the variants instead of indexing them parallel to the table here.
     */
        
    static final int MAX_STATES_2022 = 74;
    
    // Key values for each valid escape sequence.  The index at which a key is found within this table is then
    //   used to index escSeqStateTable_Value_2022[], nextStateToUnicodeJP[]
    static final int[] escSeqStateTable_Key_2022 = new int[/*MAX_STATES_2022*/] {
    /*   0           1           2           3           4           5           6           7           8           9           */

         1          ,34         ,36         ,39         ,55         ,57         ,60         ,61         ,1093       ,1096
        ,1097       ,1098       ,1099       ,1100       ,1101       ,1102       ,1103       ,1104       ,1105       ,1106
        ,1109       ,1154       ,1157       ,1160       ,1161       ,1176       ,1178       ,1179       ,1254       ,1257
        ,1768       ,1773       ,1957       ,35105      ,36933      ,36936      ,36937      ,36938      ,36939      ,36940
        ,36942      ,36943      ,36944      ,36945      ,36946      ,36947      ,36948      ,37640      ,37642      ,37644
        ,37646      ,37711      ,37744      ,37745      ,37746      ,37747      ,37748      ,40133      ,40136      ,40138
        ,40139      ,40140      ,40141      ,1123363    ,35947624   ,35947625   ,35947626   ,35947627   ,35947629   ,35947630
        ,35947631   ,35947635   ,35947636   ,35947638
    };
    
    static final byte[]  escSeqStateTable_Value_2022 = new byte[/*MAX_STATES_2022*/] {
        /*          0                           1                         2                             3                           4                           5                               6                        7                          8                           9       */
             VALID_NON_TERMINAL_2022    ,VALID_NON_TERMINAL_2022    ,VALID_NON_TERMINAL_2022    ,VALID_NON_TERMINAL_2022     ,VALID_NON_TERMINAL_2022   ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_NON_TERMINAL_2022    ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022
            ,VALID_MAYBE_TERMINAL_2022  ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022
            ,VALID_TERMINAL_2022        ,VALID_NON_TERMINAL_2022    ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_NON_TERMINAL_2022    ,VALID_NON_TERMINAL_2022    ,VALID_NON_TERMINAL_2022    ,VALID_NON_TERMINAL_2022    ,VALID_TERMINAL_2022
            ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_NON_TERMINAL_2022    ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022
            ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022
            ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022
            ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_NON_TERMINAL_2022    ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022
            ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022        ,VALID_TERMINAL_2022
        };


    /* Enum Variant2022 for refactoring changeState_2022 code*/
     static final int ISO_2022_JP=1;
     static final int ISO_2022_KR=2;
     static final int ISO_2022_CN=3;
    
     
    
     private void   ISO2022InitJP(int version) {
         version2022 = version;
         variant     = ISO_2022_JP;
         
         maxBytesPerChar = 6;  // 4 byte escape sequence + DBCS         
         minBytesPerChar = 1;
         maxCharsPerByte = 1;

         // open the required converters and cache them 
         if((jpCharsetMasks[version]&CSM(ISO8859_7))!=0) {
             converterArray[ISO8859_7] = (CharsetMBCS)CharsetICU.forNameICU("ISO8859_7");
         }
         converterArray[JISX201] = (CharsetMBCS)CharsetICU.forNameICU("jisx-201");
         converterArray[JISX208] = (CharsetMBCS)CharsetICU.forNameICU("jisx-208");
         if((jpCharsetMasks[version]&CSM(JISX212))!=0) {
             converterArray[JISX212] = (CharsetMBCS)CharsetICU.forNameICU("jisx-212");
         }
         if((jpCharsetMasks[version]&CSM(GB2312))!=0) {
             converterArray[GB2312] = (CharsetMBCS)CharsetICU.forNameICU("ibm-5478");   // gb_2312_80-1
         }
         if((jpCharsetMasks[version]&CSM(KSC5601))!=0) {
             converterArray[KSC5601] = (CharsetMBCS)CharsetICU.forNameICU("ksc_5601");
         }
     }


            
            
     private void   ISO2022InitKR(int version) {
         throw new UnsupportedCharsetException(icuCanonicalName);
            /*
                if (version==1){
                    myConverterData.currentConverter=
                        ucnv_open("icu-internal-25546",errorCode);

                    if (U_FAILURE(*errorCode)) {
                        _ISO2022Close(cnv);
                        return;
                    }

                    uprv_strcpy(myConverterData.name,"ISO_2022,locale=ko,version=1");
                    uprv_memcpy(cnv.subChars, myConverterData.currentConverter.subChars, 4);
                    cnv.subCharLen = myConverterData.currentConverter.subCharLen;
                }else{
                    myConverterData.currentConverter=ucnv_open("ibm-949",errorCode);

                    if (U_FAILURE(*errorCode)) {
                        _ISO2022Close(cnv);
                        return;
                    }

                    myConverterData.version = 0;
                    uprv_strcpy(myConverterData.name,"ISO_2022,locale=ko,version=0");
                }

                // initialize the state variables 
                setInitialStateToUnicodeKR(cnv, myConverterData);
                setInitialStateFromUnicodeKR(cnv, myConverterData);

                // set the function pointers to appropriate funtions
                cnv.sharedData=(UConverterSharedData*)&_ISO2022KRData;
                uprv_strcpy(myConverterData.locale,"ko");
                */
            }
     
     
     private void   ISO2022InitCN(int version) {
         throw new UnsupportedCharsetException(icuCanonicalName);

              /*

                // open the required converters and cache them 
                myConverterData.myConverterArray[GB2312_1]         = ucnv_loadSharedData("ibm-5478", NULL, errorCode);
                if(version==1) {
                    myConverterData.myConverterArray[ISO_IR_165]   = ucnv_loadSharedData("iso-ir-165", NULL, errorCode);
                }
                myConverterData.myConverterArray[CNS_11643]        = ucnv_loadSharedData("cns-11643-1992", NULL, errorCode);


                // set the function pointers to appropriate funtions 
                cnv.sharedData=(UConverterSharedData*)&_ISO2022CNData;
                uprv_strcpy(myConverterData.locale,"cn");

                if (version==1){
                    uprv_strcpy(myConverterData.name,"ISO_2022,locale=zh,version=1");
                }else{
                    myConverterData.version = 0;
                    uprv_strcpy(myConverterData.name,"ISO_2022,locale=zh,version=0");
                }
              */
            }
     
    
    public CharsetDecoder newDecoder() {
        switch (variant) {
        case ISO_2022_JP:
            return new CharsetDecoder2022JP(this);
        case ISO_2022_CN:
            throw new UnsupportedCharsetException("ISO_2022_CN");  // TODO: needs implementation
        case ISO_2022_KR:
            throw new UnsupportedCharsetException("ISO_2022_KR");  // TODO: needs implementation
        default:
            throw new UnsupportedCharsetException("Unknown");
        }
    }

    
    public CharsetEncoder newEncoder() {
        switch (variant) {
        case ISO_2022_JP:
            return new CharsetEncoder2022JP(this);
        case ISO_2022_CN:
            throw new UnsupportedCharsetException("ISO_2022_CN");  // TODO: needs implementation
        case ISO_2022_KR:
            throw new UnsupportedCharsetException("ISO_2022_KR");  // TODO: needs implementation
        default:
            throw new UnsupportedCharsetException("Unknown");
        }
    }



    /* ************** to unicode *******************/
    /* ***************************************************************************
     * Recognized escape sequences are
     * <ESC>(B  ASCII
     * <ESC>.A  ISO-8859-1
     * <ESC>.F  ISO-8859-7
     * <ESC>(J  JISX-201
     * <ESC>(I  JISX-201
     * <ESC>$B  JISX-208
     * <ESC>$@  JISX-208
     * <ESC>$(D JISX-212
     * <ESC>$A  GB2312
     * <ESC>$(C KSC5601
     */
    static final short nextStateToUnicodeJP[] = new short[/*MAX_STATES_2022*/] {
    //      0                1               2               3               4               5               6               7               8               9    
        INVALID_STATE   ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,SS2_STATE      ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE
        ,ASCII          ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,JISX201        ,HWKANA_7BIT    ,JISX201        ,INVALID_STATE
        ,INVALID_STATE  ,INVALID_STATE  ,JISX208        ,GB2312         ,JISX208        ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE
        ,ISO8859_1      ,ISO8859_7      ,JISX208        ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,KSC5601        ,JISX212        ,INVALID_STATE
        ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE
        ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE
        ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE
        ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE
    };

    /* ************** to unicode *******************/
    static final short nextStateToUnicodeCN[] = new short[/*MAX_STATES_2022*/] {
    //      0                1               2               3               4               5               6               7               8               9   
         INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,SS2_STATE      ,SS3_STATE      ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE
        ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE
        ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE
        ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE
        ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,GB2312_1       ,INVALID_STATE  ,ISO_IR_165
        ,CNS_11643_1    ,CNS_11643_2    ,CNS_11643_3    ,CNS_11643_4    ,CNS_11643_5    ,CNS_11643_6    ,CNS_11643_7    ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE
        ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE
        ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE  ,INVALID_STATE
    };


    //  getKey_2022()
    //    Parameters:
    //       c:      an input byte, candidate for being part of an escape sequence,
    //               in which case it will be added to the key that is being built up.
    //       partialKey
    //               index into escSeqStateTable_Key_2022[] of the partially built-up
    //               key to which the new input character c will be added.  Pass -1
    //               on the first call, and the value returned by this function on the
    //               prior iteration for each subsequent call.
    //    Returns 
    //       index of the key (may be partial) in escSeqStateTable_Key_2022[]
    //       or -1 if the input byte 'c' does not result in a valid escape sequence.
    //
    static int
    getKey_2022(byte c, int partialKey) {
        int togo;
        int low = 0;
        int hi = MAX_STATES_2022;
        int oldmid=0;

        togo = normalize_esq_chars_2022[((int)c) & 0x000000ff];
        if(togo == 0) {
            /* not a valid character anywhere in an escape sequence */
            return -1;
        }
        int key = (partialKey<0? 0 : escSeqStateTable_Key_2022[partialKey]);
        key = (key << 5) + togo;

        while (hi != low)  /*binary search*/{
            int mid = (hi+low) >> 1; /*Finds median*/
            if (mid == oldmid)
                break;
            if (escSeqStateTable_Key_2022[mid] > togo){
                hi = mid;
            }
            else if (escSeqStateTable_Key_2022[mid] < togo){
                low = mid;
            }
            else /*we found it*/{
                return mid;
            }
            oldmid = mid;
        }
        
        return -1;
    }

    //
    //  changeState_2022()
    //     runs through a state machine to determine the escape sequence - codepage correspondance
    //     Return a CoderResult indicating the problem if the source underflowed or an illegal 
    //          sequence was encountered.  Return null if valid sequence was consumed.
    //
    static CoderResult changeState_2022( 
            ByteBuffer  source,
            ISO2022State state,         // Current state, will be updated according to sequence found.
            int         variant,        // enum (ISO_2022_JP, ISO_2022_KR, ISO_2022_CN)
            int         version
    )  {
        int    value;   // enum UCNV_TableStates_2022
        int    offset = -1;
        byte   c;

        int initialPosition = source.position();
        value = VALID_NON_TERMINAL_2022;

        while (value==VALID_NON_TERMINAL_2022) {
            if (source.hasRemaining() == false) {
                source.position(initialPosition);
                return CoderResult.UNDERFLOW;
            }
            c = source.get();
            offset = getKey_2022(c, offset);
            if (offset<0) {
                value = INVALID_2022;
                break;
            }
            value = escSeqStateTable_Value_2022[offset];
        }


        if (value== VALID_MAYBE_TERMINAL_2022) {
            /* not ISO_2022 itself, finish here */
            value = VALID_TERMINAL_2022;
        }

        if (value == INVALID_2022 ) {
            return CoderResult.malformedForLength(source.position()-initialPosition);
        }

        /* value == VALID_TERMINAL_2022 */ {
            switch(variant) {
            // TODO:  the code inside this switch should be factored out into a function with separate impls for
            //        each of the three classes of decoders.
            case ISO_2022_JP:
            {
                short tempState = nextStateToUnicodeJP[offset];
                switch (tempState) {
                case INVALID_STATE:
                    return CoderResult.malformedForLength(source.position()-initialPosition);
                case SS2_STATE:
                    if(state.cs[2] != 0) {
                        if(state.g < 2) {
                            state.prevG = state.g;
                        }
                        state.g = 2;
                    } else {
                        /* illegal to have SS2 before a matching designator */
                        return CoderResult.malformedForLength(source.position()-initialPosition);
                    }
                    break;
                    /* case SS3_STATE: not used in ISO-2022-JP-x */
                case ISO8859_1:
                case ISO8859_7:
                    if((jpCharsetMasks[version] & CSM(tempState)) == 0) {
                        return CoderResult.malformedForLength(source.position()-initialPosition);
                    }
                    /* G2 charset for SS2 */
                    state.cs[2] = (byte)tempState;
                    break;
                default:
                    if((jpCharsetMasks[version] & CSM(tempState)) == 0) {
                        return CoderResult.malformedForLength(source.position()-initialPosition);
                    }
                /* G0 charset */
                state.cs[0] = (byte)tempState;
                break;
                }
            }
            break;
            case ISO_2022_CN:
            {
                short  tempState = nextStateToUnicodeCN[offset];
                switch(tempState) {
                case INVALID_STATE:
                    return CoderResult.malformedForLength(source.position()-initialPosition);
                case SS2_STATE:
                    if(state.cs[2] == 0) {
                        /* illegal to have SS2 before a matching designator */
                        return CoderResult.malformedForLength(source.position()-initialPosition);
                    }
                    if (state.g < 2 ) {
                        state.prevG = state.g;
                    }
                    state.g = 2;
                    break;
                case SS3_STATE:
                    if(state.cs[3] != 0) {
                        if(state.g < 2) {
                            state.prevG = state.g;
                        }
                        state.g = 3;
                    } else {
                        /* illegal to have SS3 before a matching designator */
                        return CoderResult.malformedForLength(source.position()-initialPosition);
                    }
                    break;
                case ISO_IR_165:
                    if(version==0) {
                        return CoderResult.malformedForLength(source.position()-initialPosition);
                    }
                    /*fall through*/
                case GB2312_1:
                    /*fall through*/
                case CNS_11643_1:
                    state.cs[1]=(byte)tempState;
                    break;
                case CNS_11643_2:
                    state.cs[2]=(byte)tempState;
                    break;
                default:
                    /* other CNS 11643 planes */
                    if(version==0) {
                        return CoderResult.malformedForLength(source.position()-initialPosition);
                    } 
                state.cs[3]=(byte)tempState;
                break;
                }
            }
            break;
            case ISO_2022_KR:
                if(offset==0x30){
                    /* nothing to be done, just accept this one escape sequence */
                } else {
                    return CoderResult.malformedForLength(source.position()-initialPosition);
                }
                break;

            default:
                return CoderResult.malformedForLength(source.position()-initialPosition);
            }
        }
        // Completely handled a valid escape sequence.
        //   return null for CoderResult, indicating success.
        return null;
    }
    
    

    class CharsetDecoder2022JP extends CharsetDecoderICU {

        public CharsetDecoder2022JP(CharsetICU cs) {
            super(cs);
        }
        
        private ISO2022State           toU2022State = new ISO2022State();          

        protected void implReset() {
            toU2022State.reset();
        }
        
        
        protected CoderResult decodeLoop(ByteBuffer source, CharBuffer target, IntBuffer offsets,
                boolean flush) {
            CoderResult cr = null;
            
            for (;;)  {
                cr = convertSegmentToU(source, target);
                if (cr != null) {
                    break;
                }
                cr = changeState_2022(source, toU2022State, ISO_2022_JP, version2022);
                if (cr != null) {
                    break;
                }
            }
   
             /* set offsets since the start */
            if (offsets != null) {
                // TODO:  offsets computation.  Really Needed?
            }
            return cr;
        }

         
        //
        // convertSegmentToU     Convert a segment of bytes to Unicode using the currently selected
        //                       sub-codepage according to the 2022 state.
        //                       Stop when one of these conditions occurs
        //                          - An escape is encountered in the input bytes.
        //                          - The output buffer can not hold the next character to be produced.
        //                          - The input buffer underflows.
        //                          - Conversion fails
        //                       Return a CoderResult indicating the reason for stopping.
        //                          In the event of an escape, return null.  This is a situation that
        //                          CoderResult is not setup to represent.
        //                       Advance the input byte buffer position only over completely processed
        //                           bytes - ones whose converted output has been fully written to the
        //                           target output buffer.
        //                       Put only complete characters into the output buffer.  If an input character
        //                           will produce multiple output characters, write either all of them
        //                           or none of them.
        private CoderResult convertSegmentToU(ByteBuffer source, CharBuffer dest) {
            for (;;) {
                if (source.hasRemaining() == false) {
                    return CoderResult.UNDERFLOW;
                }
                
                // Remember the buffer positions at the start of each character.
                //   In the case of overflow or underflow, the buffers are restored to these positions.
                int   startingSourcePosition = source.position();
                int   startingDestPosition   = dest.position();
                
                int   inputByte = (source.get() & UConverterConstants.UNSIGNED_BYTE_MASK);
                int   targetUniChar = UConverterConstants.missingCharMarker;
                
                switch (inputByte) {
                case UConverterConstants.SI:
                    if (version2022==3) {
                        toU2022State.g =0;
                        continue;
                    }
                    /* only JIS7 uses SI/SO, not ISO-2022-JP-x */
                    break;                        
                
                case UConverterConstants.SO:
                    if (version2022==3) {
                        /* JIS7: switch to G1 half-width Katakana */
                        toU2022State.cs[1] = HWKANA_7BIT;
                        toU2022State.g=1;
                        continue;
                     } 
                     /* only JIS7 uses SI/SO, not ISO-2022-JP-x */
                     break;

                case  ESC_2022:
                    // Escape sequence start.
                    // Stops the conversion within this function; dealing with it
                    //    is handled elsewhere.
                    source.position(startingSourcePosition);
                    return null;
                    
                case CR:
                case LF:
                    /* automatically reset to single-byte mode */
                    if(toU2022State.cs[0] != ASCII && toU2022State.cs[0] != JISX201) {
                        toU2022State.cs[0] = ASCII;
                    }
                    toU2022State.cs[2] = 0;
                    toU2022State.g = 0;
                    break;
                    
                default:
                    break;   
                }
                
             int cs = toU2022State.cs[toU2022State.g];
             boolean  needToRestoreState_g = false;
             byte  savedState_g = 0;
             if (inputByte >= 0xa1 && inputByte <= 0xdf && version2022==4 && !Charset2022.IS_JP_DBCS(cs)) {
                 /* 8-bit halfwidth katakana in any single-byte mode for JIS8 */
                 targetUniChar = inputByte + (0x0000ff61 - 0xa1);

                 /* return from a single-shift state to the previous one */
                 if(toU2022State.g >= 2) {
                     needToRestoreState_g = true;
                     savedState_g = toU2022State.g;
                     toU2022State.g = toU2022State.prevG;
                 }
             } else switch(cs) {
             case ASCII:
                 if(inputByte <= 0x7f) {
                     targetUniChar = inputByte;
                 }
                 break;
             case ISO8859_1:
                 if(inputByte <= 0x7f) {
                     targetUniChar = inputByte + 0x80;
                 }
                 /* return from a single-shift state to the previous one */
                 toU2022State.g=toU2022State.prevG;
                 break;
             case ISO8859_7:
                 if(inputByte <= 0x7f) {
                     /* convert mySourceChar+0x80 to use a normal 8-bit table */
                     targetUniChar =
                         CharsetMBCS.MBCS_SINGLE_SIMPLE_GET_NEXT_BMP(
                             converterArray[cs].sharedData.mbcs,
                             inputByte + 0x80);
                 }
                 /* return from a single-shift state to the previous one */
                 toU2022State.g=toU2022State.prevG;
                 break;
             case JISX201:
                 if(inputByte <= 0x7f) {
                     targetUniChar =
                         CharsetMBCS.MBCS_SINGLE_SIMPLE_GET_NEXT_BMP(
                             converterArray[cs].sharedData.mbcs,
                             inputByte);
                 }
                 break;
             case HWKANA_7BIT:
                 if((inputByte >= 0x21) && (inputByte <= 0x5f)) {
                     /* 7-bit halfwidth Katakana */
                     targetUniChar = inputByte + (0x0000ff61 - 0x21);
                 }
                 break;
             default:
                 /* G0 DBCS */
                 if (source.hasRemaining() == false) {
                     // The input contains only the first byte of a double byte character.
                     //   Back up the input position so that we will the leading byte
                     //   again after fetching more input.
                     source.position(source.position()-1);
                     return CoderResult.UNDERFLOW;
                 }
                                      
                 // Move the source position back to the first byte of the DBCS character so that
                 // the mbcs conversion sees the whole thing.
                 source.position(source.position()-1);
                 targetUniChar = CharsetMBCS.MBCSSimpleGetNextUChar(converterArray[cs].sharedData, source, false);
             }
             
             // We have converted a complete Unicode Character, now put it to the output buffer.
             try {
                 if (targetUniChar <= 0xffff) {
                     dest.put((char)targetUniChar);
                 } else {
                     dest.put(UTF16.getLeadSurrogate(targetUniChar));
                     dest.put(UTF16.getTrailSurrogate(targetUniChar));
                 }
             }
             catch (IndexOutOfBoundsException e) {
                 // The output buffer overflowed.
                 if (needToRestoreState_g) {
                     toU2022State.g = savedState_g;
                 }
                 dest.position(startingDestPosition);
                 source.position(startingSourcePosition);
                 return CoderResult.OVERFLOW;
             }
         }
        }
        
        // This point is unreachable.  No break from the above loop, only returns.
    }
        
        

    /*
     * This is another simple conversion function for internal use by other
     * conversion implementations.
     * It does not use the converter state nor call callbacks.
     * It does not handle the EBCDIC swaplfnl option (set in UConverter).
     * It handles conversion extensions but not GB 18030.
     *
     * It converts one single Unicode code point into codepage bytes, encoded
     * as one 32-bit value. The function returns the number of bytes in *pValue:
     * 1..4 the number of bytes in *pValue
     * 0    unassigned (*pValue undefined)
     * -1   illegal (currently not used, *pValue undefined)
     *
     * *pValue will contain the resulting bytes with the last byte in bits 7..0,
     * the second to last byte in bits 15..8, etc.
     * Currently, the function assumes but does not check that 0<=c<=0x10ffff.
     * 
     */
  static int MBCSFromUChar32_ISO2022(UConverterSharedData sharedData,
                              int                  c,
                              int[]                value,
                              boolean              useFallback,
                              int                  outputType   // Output Type from MBCS, e.g. CharsetMBCS.MBCS_OUTPUT_2
                              )
    {
        ByteBuffer   cx;
        char[]  table;
        int     stage2Entry;
        int     myValue;
        int     p;
        int     length;
        /* BMP-only codepages are stored without stage 1 entries for supplementary code points */
        if(c<0x10000 || (sharedData.mbcs.unicodeMask & UConverterConstants.HAS_SUPPLEMENTARY)!=0) {
            table=sharedData.mbcs.fromUnicodeTable;
            stage2Entry = CharsetMBCS.MBCS_STAGE_2_FROM_U(table, c);
            /* get the bytes and the length for the output */
            if(outputType==CharsetMBCS.MBCS_OUTPUT_2){
                myValue=CharsetMBCS.MBCS_VALUE_2_FROM_STAGE_2(sharedData.mbcs.fromUnicodeBytes, stage2Entry, c);
                if(myValue<=0xff) {
                    length=1;
                } else {
                    length=2;
                }
            } else /* outputType==MBCS_OUTPUT_3 */ {
                byte[] bytes = sharedData.mbcs.fromUnicodeBytes;
                p=CharsetMBCS.MBCS_POINTER_3_FROM_STAGE_2(bytes, stage2Entry, c);
                myValue = ((bytes[p]   & UConverterConstants.UNSIGNED_BYTE_MASK) <<16) | 
                          ((bytes[p+1] & UConverterConstants.UNSIGNED_BYTE_MASK) << 8) | 
                           (bytes[p+2] & UConverterConstants.UNSIGNED_BYTE_MASK);
                if(myValue<=0xff) {
                    length=1;
                } else if(myValue<=0xffff) {
                    length=2;
                } else {
                    length=3;
                }
            }
            /* is this code point assigned, or do we use fallbacks? */
            if( (stage2Entry&(1<<(16+(c&0xf))))!=0 ||
                (CharsetEncoderICU.fromUUseFallback(useFallback, c) && myValue!=0)) {
                /*
                 * We allow a 0 byte output if the "assigned" bit is set for this entry.
                 * There is no way with this data structure for fallback output
                 * to be a zero byte.
                 */
                /* assigned */
                value[0] = myValue;
                return length;
            }
        }

        cx=sharedData.mbcs.extIndexes;
        if(cx!=null) {
            // TODO:  need to port this function
            // *length=ucnv_extSimpleMatchFromU(cx, c, value, useFallback);
            System.err.println("Need port of ucnv_extSimpleMatchFromU()\n");
            return -1;
        }

        /* unassigned */
        return 0;
    }
   
   
  //
  // MBCS_SingleFromUChar32
  //
  //   corresponds to the inline func MBCS_SINGLE_FROM_UCHAR32() in ICU4C, file ucnv2022.c
  //
  //  Comment from ICU4C:
  //     This function replicates code in _MBCSSingleFromUChar32() function in ucnvmbcs.c
  //     any future change in _MBCSSingleFromUChar32() function should be reflected in
  //     this macro
  //  Not quite true for ICU4J, since the corresponding function from class CharsetMBCS has
  //  not been ported.
  //
  static int MBCS_SingleFromUChar32(UConverterSharedData sharedData,
                                    int                  c,
                                    boolean              useFallback) {
      char[]  table;
      int     value;
      /* BMP-only codepages are stored without stage 1 entries for supplementary code points */
      if(c>=0x10000 && (sharedData.mbcs.unicodeMask & UConverterConstants.HAS_SUPPLEMENTARY)==0) {
          return -1;
      }
      /* convert the Unicode code point in c into codepage bytes (same as in _MBCSFromUnicodeWithOffsets) */
      table=sharedData.mbcs.fromUnicodeTable;
      /* get the byte for the output */
      value=CharsetMBCS.MBCS_SINGLE_RESULT_FROM_U(table, sharedData.mbcs.fromUnicodeBytes, c);
      /* is this code point assigned, or do we use fallbacks? */
      if(useFallback ? value>=0x800 : value>=0xc00) {
          value &=0xff;
      } else {
          value= -1;
      }
      return value;
  }


  /***************************************************************************************************
  * Rules for ISO-2022-jp encoding
  * (i)   Escape sequences must be fully contained within a line they should not
  *       span new lines or CRs
  * (ii)  If the last character on a line is represented by two bytes then an ASCII or
  *       JIS-Roman character escape sequence should follow before the line terminates
  * (iii) If the first character on the line is represented by two bytes then a two
  *       byte character escape sequence should precede it
  * (iv)  If no escape sequence is encountered then the characters are ASCII
  * (v)   Latin(ISO-8859-1) and Greek(ISO-8859-7) characters must be designated to G2,
  *       and invoked with SS2 (ESC N).
  * (vi)  If there is any G0 designation in text, there must be a switch to
  *       ASCII or to JIS X 0201-Roman before a space character (but not
  *       necessarily before "ESC 4/14 2/0" or "ESC N ' '") or control
  *       characters such as tab or CRLF.
  * (vi)  Supported encodings:
  *          ASCII, JISX201, JISX208, JISX212, GB2312, KSC5601, ISO-8859-1,ISO-8859-7
  *
  *  source : RFC-1554
  *
  *          JISX201, JISX208,JISX212 : new .cnv data files created
  *          KSC5601 : alias to ibm-949 mapping table
  *          GB2312 : alias to ibm-1386 mapping table
  *          ISO-8859-1 : Algorithmic implemented as LATIN1 case
  *          ISO-8859-7 : alisas to ibm-9409 mapping table
  */
  
 /* preference order of JP charsets */
  static final int[] jpCharsetPref = new int[] {
      ASCII,
      JISX201,
      ISO8859_1,
      ISO8859_7,
      JISX208,
      JISX212,
      GB2312,
      KSC5601,
      HWKANA_7BIT
  };
  
  /*
   * The escape sequences must be in order of the enum constants like JISX201  = 3,
   * not in order of jpCharsetPref[]!
   */
  static final byte[][] escSeqChars  = new byte [][] {
      new byte[] {0x1B, 0x28, 0x42},         /* <ESC>(B  ASCII       */
      new byte[] {0x1B, 0x2E, 0x41},         /* <ESC>.A  ISO-8859-1  */
      new byte[] {0x1B, 0x2E, 0x46},         /* <ESC>.F  ISO-8859-7  */
      new byte[] {0x1B, 0x28, 0x4A},         /* <ESC>(J  JISX-201    */
      new byte[] {0x1B, 0x24, 0x42},         /* <ESC>$B  JISX-208    */
      new byte[] {0x1B, 0x24, 0x28, 0x44},   /* <ESC>$(D JISX-212    */
      new byte[] {0x1B, 0x24, 0x41},         /* <ESC>$A  GB2312      */
      new byte[] {0x1B, 0x24, 0x28, 0x43},   /* <ESC>$(C KSC5601     */
      new byte[] {0x1B, 0x28, 0x49}          /* <ESC>(I  HWKANA_7BIT */
  };
  

  /*
  * The iteration over various code pages works this way:
  * i)   Get the currentState from myConverterData->currentState
  * ii)  Check if the character is mapped to a valid character in the currentState
  *      Yes ->  a) set the initIterState to currentState
  *       b) remain in this state until an invalid character is found
  *      No  ->  a) go to the next code page and find the character
  * iii) Before changing the state increment the current state check if the current state
  *      is equal to the intitIteration state
  *      Yes ->  A character that cannot be represented in any of the supported encodings
  *       break and return a U_INVALID_CHARACTER error
  *      No  ->  Continue and find the character in next code page
  *
  *
  * TODO: Implement a priority technique where the users are allowed to set the priority of code pages
  */

    // Default Substitution string for unmappable characters.
    //    Substitution is handled completely by the Java framework classes.  This string is
    //    used only to initialize the Java stuff, which then handles everything else.
    //
    byte[] fromUSubstitutionDefault = new byte[] { (byte) 0x1a };
    
    public class CharsetEncoder2022JP extends CharsetEncoderICU {

        public CharsetEncoder2022JP(CharsetICU cs) {
            super(cs, fromUSubstitutionDefault);
            implReset();
        }
        
        
       ISO2022State fromU2022State = new ISO2022State();  // Current state (which charsets are active) in the output
                                                          //   stream.
       
       ISO2022State saved2022State = new ISO2022State();  // A back-up state, for use if the output byte buffer
                                                          //   overflows and we need to revert the current state
                                                          //   because of discarding a partially output escape sequence.
       
       CoderResult  encoderResult;         // result status, used by internal functions.
                                           //   null is used to signal success.
                                           //   Class scope, to sidestep out-parameter limitations.
       
       int [] choices = new int[10];       // The ordered list of charsets to try when converting a char from Unicode.
       int    choiceCount = 0;             //  and the number of charsets in the list
                                           //  The list gets dynamically re-arranged based on the current state
                                           //    of the conversion.
       
       int [] mbcsByteValues = new int[1]; // Receives the result of a Unicode -> MBCS conversion.
                                           //   (Conversion function needs an array as an out param.)

       
       protected void implReset() {
           super.implReset();
           fromU2022State.reset();
       }
       
       protected CoderResult implFlush(ByteBuffer target) {
           // ISO-2022-JP conversion needs to be in ASCII mode at the very end.
           int restorePosition = target.position();
           try {
               saved2022State.copyFrom(fromU2022State);
               if(fromU2022State.g != 0) {
                   target.put((byte)UCNV_SI);
                   fromU2022State.g = 0;
               }
               if(fromU2022State.cs[0] != ASCII) {
                   target.put(escSeqChars[ASCII]);
                   fromU2022State.cs[0] = ASCII;
               }
           } catch (BufferOverflowException e) {
               target.position(restorePosition);
               fromU2022State.copyFrom(saved2022State);
               return CoderResult.OVERFLOW;
           }
           return CoderResult.UNDERFLOW;         
       }

        protected CoderResult encodeLoop(CharBuffer source, ByteBuffer target, IntBuffer offsets,
                boolean flush) {
            
            int   sourceChar;    
            int   cs;               // Identifies an output character set
            int   g;
            int   len;              // number of bytes needed for the current Unicode character.
            int   targetValue = 0;  // The output bytes for one character, packed into an int.
            
            if (!source.hasRemaining()) {
                /* no input, nothing to do */
                return CoderResult.UNDERFLOW;
            }
            if (!target.hasRemaining()) {
                /* no output available, can't do anything */
                return CoderResult.OVERFLOW;
            }

            while (source.hasRemaining()) {
                int sourcePositionAtStartOfChar = source.position();
                sourceChar = source.get();
                if (UTF16.isSurrogate((char)sourceChar)) {
                    sourceChar = getSupplementary(source, sourceChar, flush);
                    if (encoderResult != null) {   // Back Door out parameter form getSupplementary
                        return encoderResult;
                    }
                }
                
                if(IS_2022_CONTROL(sourceChar)) {
                    return CoderResult.unmappableForLength(1);
                }
                
                if (choiceCount == 0) {
                    rebuildChoiceList();
                }

                
                //
                //  Find the first available charset from the choices[] list that
                //   can convert the Unicode character we've got.
                //
                cs = g = 0;
                len = 0;
                for(int i = 0; i < choiceCount && len == 0; ++i) {
                    cs = choices[i];
                    switch(cs) {
                    case ASCII:
                        if(sourceChar <= 0x7f) {
                            targetValue = sourceChar;
                            len = 1;
                        }
                        break;
                    case ISO8859_1:
                        if(0x80 <= sourceChar && sourceChar <= 0xff) {
                            targetValue = sourceChar - 0x80;
                            len = 1;
                            g = 2;
                        }
                        break;
                    case HWKANA_7BIT:
                        if (sourceChar>=0x0000ff61 && sourceChar<=0x0000ff9f) {
                            targetValue = (sourceChar - (0xff61 - 0x21));
                            len = 1;

                            if(version2022==3) {
                                /* JIS7: use G1 (SO) */
                                fromU2022State.cs[1] = (byte)cs; /* do not output an escape sequence */
                                g = 1;
                            } else if(version2022==4) {
                                /* JIS8: use 8-bit bytes with any single-byte charset, see escape sequence output below */
                                int cs0;

                                targetValue += 0x80;

                                cs0 = fromU2022State.cs[0];
                                if(IS_JP_DBCS(cs0)) {
                                    /* switch from a DBCS charset to JISX201 */
                                    cs = JISX201;
                                } else {
                                    /* stay in the current G0 charset */
                                    cs = cs0;
                                }
                            }
                        }
                        break;
                    case JISX201:
                        /* G0 SBCS */
                        targetValue = MBCS_SingleFromUChar32(converterArray[cs].sharedData,
                                                             sourceChar, useFallback);
                        if(targetValue >= 0) {
                            len = 1;
                        }
                        break;
                    case ISO8859_7:
                        /* G0 SBCS forced to 7-bit output */
                        targetValue = MBCS_SingleFromUChar32(converterArray[cs].sharedData,
                                sourceChar, useFallback);
                        if(0x80 <= targetValue && targetValue <= 0xff) {
                            targetValue -= 0x80;
                            len = 1;
                            g = 2;
                        }
                        break;
                    default:
                        /* G0 DBCS */
                        len = MBCSFromUChar32_ISO2022(
                                converterArray[cs].sharedData,
                                sourceChar, mbcsByteValues,
                                useFallback, CharsetMBCS.MBCS_OUTPUT_2);
                    targetValue = mbcsByteValues[0];
                    if(len != 2) {
                        len = 0;
                    }
                    break;
                    }
                }

                //
                //  At this point, either the Unicode character has been converted to byte(s), or
                //    we've tried all possible converters and failed.
                if (len==0) {
                    return CoderResult.unmappableForLength(source.position() - sourcePositionAtStartOfChar);
                }

                //  Write the output bytes, both state change (if any) and the character bytes.
                //  Do this inside a try.  If the buffer overflows, restore both source and target buffers
                //    and the converter state info, to their orginal position.  The next level up in the
                //    framework will then redo the entire character from the beginning.
                //
                int outputPositionAtStartOfChar = target.position();
                try {
                    //
                    //  Output any 2022 state changing shift or escape sequences that need to 
                    //    precede the character bytes themselves.
                    //
                    OutputStateChange(fromU2022State, saved2022State, target,  sourceChar, g, cs);


                    /* write the output bytes */
                    if(len == 1) {
                        target.put((byte)targetValue);
                    } else /* len == 2 */ {
                        target.put((byte)(targetValue>>8));
                        target.put((byte)targetValue);
                    }
                }
                catch (BufferOverflowException e) {
                    target.position(outputPositionAtStartOfChar);
                    source.position(sourcePositionAtStartOfChar);
                    if (saved2022State.g != -1) {
                        fromU2022State.copyFrom(saved2022State);
                    }
                   return CoderResult.OVERFLOW;
                }

            } /* end while (source.hasRemaining()) */

            // The conversion loop only comes out the bottom, to here, if the source input
            //   is exhausted.  All other cases for stopping (output buffer full, conversion failure)
            //   return from within the body of the loop.
            
            if (flush) {
                // ISO-2022-JP conversion needs to be in ASCII mode at the very end.
                implFlush(target);
            }
                        
            return CoderResult.UNDERFLOW;
        }

        //
        //   getSupplementary()  Finish up fetching a supplementary character after a surrogate has
        //                       been encountered.  
        //
        //            source:       The source Char Buffer
        //            sourceChar:   The surrogate char that was encountered in the source
        //            flush:        Flag controlling behavior if an unpaired lead surrogate
        //                          appears a at the end of the source buffer.
        //
        //            Return:       The supplementary character.
        //            encoderResult (object scope) CoderResult value in the event of an error.
        //                          null if supplementary fetch was successful.
        //
        private int getSupplementary(CharBuffer source, int sourceChar, boolean flush) {
            if (UTF16.isTrailSurrogate((char)sourceChar)) {
                encoderResult = CoderResult.malformedForLength(1);
                return sourceChar;
            }
            if (source.hasRemaining() == false) {
                if (flush) {
                    encoderResult = CoderResult.malformedForLength(1);
                    return sourceChar;
                } else {
                    encoderResult = CoderResult.UNDERFLOW;
                    source.position(source.position()-1);
                    return sourceChar;
                }
            }
            char trailSurrogate = source.get();
            if (UTF16.isTrailSurrogate(trailSurrogate) == false) {
                source.position(source.position()-1);
                encoderResult = CoderResult.malformedForLength(1);
                return sourceChar;
            }
            int supplementaryChar = UCharacterProperty.getRawSupplementary((char)sourceChar, trailSurrogate);
            encoderResult = null;
            return supplementaryChar;
        }
        
        // CSM - character set mask (bit set)
        private int CSM(int cs) {return 1 << cs;}
        
        
        //
        //  rebuildChoiceList   Reconstruct the preference-ordered list of charsets to
        //                      try when converting a character from Unicode.
        //
        void rebuildChoiceList() {
            int csm;
            int cs;

            /*
             * The csm variable keeps track of which charsets are allowed
             * and not used yet while building the choices[].
             */
            csm = jpCharsetMasks[version2022];
            choiceCount = 0;

            /* JIS7/8: try single-byte half-width Katakana before JISX208 */
            if(version2022 == 3 || version2022 == 4) {
                choices[choiceCount++] = cs = HWKANA_7BIT;
                csm &= ~CSM(cs);
            }

            /* try the current G0 charset */
            choices[choiceCount++] = cs = fromU2022State.cs[0];
            csm &= ~CSM(cs);

            /* try the current G2 charset */
            if((cs = fromU2022State.cs[2]) != 0) {
                choices[choiceCount++] = cs;
                csm &= ~CSM(cs);
            }

            /* try all the other possible charsets */
            for(int i = 0; i < jpCharsetPref.length; ++i) {
                cs = jpCharsetPref[i];
                if((CSM(cs) & csm)!=0) {
                    choices[choiceCount++] = cs;
                    csm &= ~CSM(cs);
                }
            }
        }
        
        //
        //  OutputStateChange   Output any shift or escape sequences that need to precede
        //                      the output of the bytes for the next character.  Adjust the
        //                      current 2022 state to reflect this.  Save the previous
        //                      state for use in the event that a buffer overflow requires
        //                      backing out of the operation.  Buffer overflows throw
        //                      normal Java buffer overflow exceptions
        
        private void OutputStateChange(ISO2022State state, ISO2022State bkupState, ByteBuffer target,
                int sourceChar, int  g,  int  cs) {
                        
            // flag the backup state to indicate no changes made, no need to restore
            bkupState.g = -1;
            
            /* write SI if necessary (only for JIS7) */
            if(state.g == 1 && g == 0) {
                bkupState.copyFrom(state);
                target.put((byte)UCNV_SI);
                state.g = 0;
            }

            /* write the designation sequence if necessary */
            if(cs != state.cs[g]) {
                if (bkupState.g == -1) {
                    bkupState.copyFrom(state);
                }
                int escLen = escSeqChars[cs].length;
                for (int i=0; i<escLen; i++) {
                    target.put(escSeqChars[cs][i]);
                }
                state.cs[g] = (byte)cs;

                // invalidate the choices[]
                //  Note: choiceCount is up at object scope. 
                choiceCount = 0;
            }

            /* write the shift sequence if necessary */
            if(g != state.g) {
                if (bkupState.g == -1) {
                    bkupState.copyFrom(state);
                }
                switch(g) {
                /* case 0 handled before writing escapes */
                case 1:
                    target.put((byte)UCNV_SO);
                    state.g = 1;
                    break;
                default: /* case 2 */
                    target.put((byte)ESC_2022);
                target.put((byte)0x4e);
                break;
                /* no case 3: no SS3 in ISO-2022-JP-x */
                }
            }
            
            if(sourceChar == CR || sourceChar == LF) {
                /* reset the G2 state at the end of a line (conversion got us into ASCII or JISX201 already) */
                if (bkupState.g == -1) {
                    bkupState.copyFrom(state);
                }
                state.cs[2] = 0;
                choiceCount = 0;
            }          
        }
 
    }


}
