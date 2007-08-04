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

import com.ibm.icu.charset.CharsetMBCS.LoadArguments;
import com.ibm.icu.impl.InvalidFormatException;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.UTF16;
import com.ibm.icu.util.ULocale;

public class Charset2022 extends CharsetICU {
    
    public Charset2022(String icuCanonicalName, String javaCanonicalName, String[] aliases) {
        super(icuCanonicalName, javaCanonicalName, aliases);
        
        String locale = ULocale.getDefault().getLanguage();
        
        this._ISO2022Open(icuCanonicalName, locale, options);

        maxBytesPerChar = 3;  // TODO: these are stubs
        minBytesPerChar = 1;
        maxCharsPerByte = 1;
        
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
    
    /*
     * ISO 2022 control codes must not be converted from Unicode
     * because they would mess up the byte stream.
     * The bit mask 0x0800c000 has bits set at bit positions 0xe, 0xf, 0x1b
     * corresponding to SO, SI, and ESC.
     */
    private static boolean  IS_2022_CONTROL(char c) {
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
    };

    private static final int UCNV_OPTIONS_VERSION_MASK = 0x000f;
    private static final int UCNV_2022_MAX_CONVERTERS  = 10;  // Number of internal sub-converter types,
                                                               //  not a limit to the number of user level 2022 converters.


    //
    //  UConverterDataISO2022
    //
    //      Corresponds to the C struct of the same name.
    //
    //      Contains both unchanging data relating to the 2022 charset and
    //      state pertaining to a conversion.  This works for C, where a converter represents both, but
    //      less well for the Java encoder/decoder architecture.
    //
    //      This structure is left as it is in C for ease of porting.
    //      The initial values are set up here.
    //      A copy from this common master is made into each encoder and decoder.
    //    
    class UConverterDataISO2022 {
        // TODO: change this back to a lighter weight data-only item.
        // UConverterSharedData[] myConverterArray;    // Sub-converters.
        CharsetMBCS[]           myConverterArray;    // Sub-converters.
        CharsetMBCS             currentConverter;
        int                    currentType;        // enum Cnv2022Type
        int                    version;
        String                 name;
        String                 locale;
        
        UConverterDataISO2022() {
            myConverterArray = new CharsetMBCS[UCNV_2022_MAX_CONVERTERS];
        }
    };
    UConverterDataISO2022  myConverterData;   // This variable name comes from C++

    static final int ESC_2022 = 0x1B; /*ESC*/

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
    
     
     
    //  ======================
    
    private void   _ISO2022Open(String name, String locale, int options) {

        // TODO:  need to look at the name we are being asked for.
        //        May be specific for JP or whatever, may include a version.
        
        String myLocale;
        myConverterData = new UConverterDataISO2022();
        int version;
        
        myConverterData.currentType = ASCII1;
        myLocale = locale;
        
        version = options & UCNV_OPTIONS_VERSION_MASK;
        myConverterData.version = version;
        
        if (/* myLocale.equals("jap") || myLocale.startsWith("jap_")*/ true) {
            int len=0;
            // open the required converters and cache them 
            if((jpCharsetMasks[version]&CSM(ISO8859_7))!=0) {
                myConverterData.myConverterArray[ISO8859_7] = (CharsetMBCS)CharsetICU.forNameICU("ISO8859_7");
            }
            myConverterData.myConverterArray[JISX201] = (CharsetMBCS)CharsetICU.forNameICU("jisx-201");
            myConverterData.myConverterArray[JISX208] = (CharsetMBCS)CharsetICU.forNameICU("jisx-208");
            if((jpCharsetMasks[version]&CSM(JISX212))!=0) {
                myConverterData.myConverterArray[JISX212] = (CharsetMBCS)CharsetICU.forNameICU("jisx-212");
            }
            if((jpCharsetMasks[version]&CSM(GB2312))!=0) {
                myConverterData.myConverterArray[GB2312] = (CharsetMBCS)CharsetICU.forNameICU("ibm-5478");   // gb_2312_80-1
            }
            if((jpCharsetMasks[version]&CSM(KSC5601))!=0) {
                myConverterData.myConverterArray[KSC5601] = (CharsetMBCS)CharsetICU.forNameICU("ksc_5601");
            }

            // set the function pointers to appropriate functions 
            /*
            cnv.sharedData=(UConverterSharedData*)(&_ISO2022JPData);
            uprv_strcpy(myConverterData.locale,"ja");

            uprv_strcpy(myConverterData.name,"ISO_2022,locale=ja,version=");
            len = uprv_strlen(myConverterData.name);
            myConverterData.name[len]=(char)(myConverterData.version+(int)'0');
            myConverterData.name[len+1]='\0';
            */
        }
        else if(myLocale.equals("kor") || myLocale.startsWith("kor_"))
        {
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
            else if (myLocale.equals("zh") || myLocale.equals("zh_") || myLocale.equals("cn") || myLocale.startsWith("cn_"))
            {
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
            else {
                // TODO:  is this the best exception to use here?
                // TODO:  close of open converters?
                throw new IllegalArgumentException();    
            }

            // TODO: something for this line...
            // cnv.maxBytesPerUChar=cnv.sharedData.staticData.maxBytesPerChar;

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
    //
    static void changeState_2022( ByteBuffer  source,
                           ISO2022State state,
                           int         variant,        // enum (ISO_2022_JP, ISO_2022_KR, ISO_2022_CN)
                           int         version
    ) throws InvalidFormatException {
        int    value;   // enum UCNV_TableStates_2022
        int    offset = -1;
        byte   c;

        value = VALID_NON_TERMINAL_2022;
        int initialPosition = source.position();

        while (source.hasRemaining() && value==VALID_NON_TERMINAL_2022) {
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

        if (value == VALID_NON_TERMINAL_2022) {
            // indicate that the escape sequence is incomplete.
            // The only way we get here is if the input buffer underflowed, otherwise
            //   the above loop would have run until we either got a complete sequence or
            //   it went invalid.
            source.position(initialPosition);
        } 

        if (value == INVALID_2022 ) {
            throw new InvalidFormatException("U_ILLEGAL_ESCAPE_SEQUENCE");
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
                throw new InvalidFormatException("U_UNSUPPORTED_ESCAPE_SEQUENCE");
            case SS2_STATE:
                if(state.cs[2] != 0) {
                    if(state.g < 2) {
                        state.prevG = state.g;
                    }
                    state.g = 2;
                } else {
                    /* illegal to have SS2 before a matching designator */
                    throw new InvalidFormatException("U_ILLEGAL_ESCAPE_SEQUENCE");
                }
                break;
                /* case SS3_STATE: not used in ISO-2022-JP-x */
            case ISO8859_1:
            case ISO8859_7:
                if((jpCharsetMasks[version] & CSM(tempState)) == 0) {
                    throw new InvalidFormatException("U_UNSUPPORTED_ESCAPE_SEQUENCE");
                }
                /* G2 charset for SS2 */
                state.cs[2] = (byte)tempState;
                break;
            default:
                if((jpCharsetMasks[version] & CSM(tempState)) == 0) {
                    throw new InvalidFormatException("U_UNSUPPORTED_ESCAPE_SEQUENCE");
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
                throw new InvalidFormatException("U_UNSUPPORTED_ESCAPE_SEQUENCE");
            case SS2_STATE:
                if(state.cs[2] == 0) {
                    /* illegal to have SS2 before a matching designator */
                    throw new InvalidFormatException("U_ILLEGAL_ESCAPE_SEQUENCE");
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
                    throw new InvalidFormatException("U_ILLEGAL_ESCAPE_SEQUENCE");
                }
                break;
            case ISO_IR_165:
                if(version==0) {
                    throw new InvalidFormatException("U_UNSUPPORTED_ESCAPE_SEQUENCE");
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
                    throw new InvalidFormatException("U_UNSUPPORTED_ESCAPE_SEQUENCE");
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
                throw new InvalidFormatException("U_UNSUPPORTED_ESCAPE_SEQUENCE");
            }
            break;

        default:
            throw new InvalidFormatException("U_ILLEGAL_ESCAPE_SEQUENCE");
        }
    }
    }
    
    

    // TODO:  we probably want three pairs of subclasses, one each for JP, KR, CN
    class CharsetDecoder2022JP extends CharsetDecoderICU {

        public CharsetDecoder2022JP(CharsetICU cs) {
            super(cs);
        };

        Charset2022.UConverterDataISO2022    myData2022;   // TODO:  needs initialization
        
        ISO2022State           toU2022State = new ISO2022State();
        
        
        
        // getEndOfBuffer_2022()
        //
        //   Checks the bytes of the buffer against valid 2022 escape sequences
        //   if the match we return a pointer to the initial start of the sequence otherwise
        //   we return sourceLimit.
        //
        // The current position of in the ByteBuffer is left unaltered.
        // 
        // for 2022 looks ahead in the stream
        // to determine the longest possible convertible
        // data stream
        // 
         int getEndOfBuffer_2022(ByteBuffer source,  boolean flush) {

             int initialPosition = source.position();
             int returnIndex = 0;

             try {
                 while ( source.get() != ESC_2022) {}
                 returnIndex = source.position() - 1;
             }
             catch (BufferUnderflowException e) {
                 returnIndex = source.limit();
             }
             source.position(initialPosition);
             return returnIndex;
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
          * TODO:  move up, so this can be declared to be static.
          */
        int
        MBCSFromUChar32_ISO2022(UConverterSharedData sharedData,
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
        //  TODO:  hoist up a level (or move to CharsetMBCS)  and make static.
        //
        int MBCS_SingleFromUChar32(UConverterSharedData sharedData,
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


        void  setInitialStateToUnicodeKR(Charset2022 converter, Charset2022.UConverterDataISO2022 myConverterData) {
            if(myConverterData.version == 1) {
                toUnicodeStatus = 0;     // offset,    field of CharsetDecoderICU 
                mode            = 0;     // state,     field of CharsetICU 
                toULength       = 0;     // byteIndex, field of CharsetICU 
            }
        }
        
          

        protected CoderResult decodeLoop(ByteBuffer source, CharBuffer target, IntBuffer offsets,
                boolean flush) {
            if (!source.hasRemaining()) {
                /* no input, nothing to do */
                return CoderResult.UNDERFLOW;
            }
            if (!target.hasRemaining()) {
                /* no output available, can't do anything */
                return CoderResult.OVERFLOW;
            }

            boolean targetOverflow = false;
            Boolean hardSegmentEnd = new Boolean(false);

            int segmentStart = 0;
            try {
            do  {
                segmentStart = source.position();
                targetOverflow = convertSegmentToU(source, target);
                changeState_2022(source, toU2022State, ISO_2022_JP, myData2022.version);
             } while (segmentStart < source.position() && targetOverflow == false);
            } catch (InvalidFormatException e) {
                // TODO:  handle this
            }
  
 
            /* set offsets since the start */
            if (offsets != null) {
                // TODO:  offsets computation.  Really Needed?
            }
            
            CoderResult cr = targetOverflow ? CoderResult.OVERFLOW :CoderResult.UNDERFLOW;
            return cr;
        }

         
        //
        // convertSegmentToU     Convert a segment of bytes to Unicode using the currently selected
        //                       sub-codepage.
        //                       Stop when one of these conditions occurs
        //                          - An escape is encountered in the input bytes.
        //                          - The output buffer can not hold the next character to be produced.
        //                          - The input buffer underflows.
        //                       Return FALSE if the output buffer overflows, forcing the conversion operation
        //                           to stop prematurely.
        //                       Advance the input byte buffer position only over completely processed
        //                           bytes - ones whose converted output has been fully written to the
        //                           target output buffer.
        //                       Put only complete characters into the output bufer.  If an input character
        //                           will produce multiple output characters, write either all of them
        //                           or none of them.
        boolean convertSegmentToU(ByteBuffer source, CharBuffer dest) {
            while (source.hasRemaining()) {
                int   inputByte = (source.get() & UConverterConstants.UNSIGNED_BYTE_MASK);
                int   targetUniChar = UConverterConstants.missingCharMarker;
                
                switch (inputByte) {
                case UConverterConstants.SI:
                    if (myData2022.version==3) {
                        toU2022State.g =0;
                        continue;
                    }
                    /* only JIS7 uses SI/SO, not ISO-2022-JP-x */
                    break;                        
                
                case UConverterConstants.SO:
                    if (myData2022.version==3) {
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
                    source.position(source.position()-1);
                    return true;
                    
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
             if (inputByte >= 0xa1 && inputByte <= 0xdf && myData2022.version==4 && !Charset2022.IS_JP_DBCS(cs)) {
                 /* 8-bit halfwidth katakana in any single-byte mode for JIS8 */
                 targetUniChar = inputByte + (0x0000ff61 - 0xa1);

                 /* return from a single-shift state to the previous one */
                 if(toU2022State.g >= 2) {
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
                             myData2022.myConverterArray[cs].sharedData.mbcs,
                             inputByte + 0x80);
                 }
                 /* return from a single-shift state to the previous one */
                 toU2022State.g=toU2022State.prevG;
                 break;
             case JISX201:
                 if(inputByte <= 0x7f) {
                     targetUniChar =
                         CharsetMBCS.MBCS_SINGLE_SIMPLE_GET_NEXT_BMP(
                             myData2022.myConverterArray[cs].sharedData.mbcs,
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
                 if(mySource < mySourceLimit) {
                     char trailByte;
getTrailByte:
                     tempBuf[0] = (char) (inputByte);
                     tempBuf[1] = trailByte = *mySource++;
                     mySourceChar = (inputByte << 8) | (uint8_t)(trailByte);
                     targetUniChar = ucnv_MBCSSimpleGetNextUChar(myData.myConverterArray[cs], tempBuf, 2, FALSE);
                 } else {
                     args.converter.toUBytes[0] = (uint8_t)mySourceChar;
                     args.converter.toULength = 1;
                     goto endloop;
                 }
             }
             break;
         }
             
             }
            return false;
        }
        
        
 
        protected CoderResult decodeMalformedOrUnmappable(int ch) {
            /*
             * put the guilty character into toUBytesArray and return a message saying that the
             * character was malformed and of length 1.
             */
            toUBytesArray[0] = (byte) ch;
            return CoderResult.malformedForLength(toULength = 1);
        }
    }
    



    protected byte[] fromUSubstitution = new byte[] { (byte) 0x1a };
    
    class CharsetEncoder2022 extends CharsetEncoderICU {

        public CharsetEncoder2022(CharsetICU cs) {
            super(cs, fromUSubstitution);
            implReset();
        }
        
        void setInitialStateFromUnicodeKR(Charset2022 converter, Charset2022.UConverterDataISO2022 myConverterData) {
            /* in ISO-2022-KR the designator sequence appears only once
             * in a file so we append it only once
             */
             if( errorBufferLength==0){

                 errorBufferLength = 4;
                 errorBuffer[0] = 0x1b;
                 errorBuffer[1] = 0x24;
                 errorBuffer[2] = 0x29;
                 errorBuffer[3] = 0x43;
             }
             if(myConverterData.version == 1) {
                 fromUChar32=0;
                 fromUnicodeStatus=1;   /* prevLength */
             }
         }



        private final static int NEED_TO_WRITE_BOM = 1;

        protected void implReset() {
            super.implReset();
            fromUnicodeStatus = NEED_TO_WRITE_BOM;
        }

        protected CoderResult encodeLoop(CharBuffer source, ByteBuffer target, IntBuffer offsets,
                boolean flush) {
            if (!source.hasRemaining()) {
                /* no input, nothing to do */
                return CoderResult.UNDERFLOW;
            }
            if (!target.hasRemaining()) {
                /* no output available, can't do anything */
                return CoderResult.OVERFLOW;
            }

            CoderResult cr;
            int oldSource = source.position();
            int oldTarget = target.position();

            if (fromUChar32 != 0) {
                /*
                 * if we have a leading character in fromUChar32 that needs to be dealt with, we
                 * need to check for a matching trail character and taking the appropriate action as
                 * dictated by encodeTrail.
                 */
                cr = encodeTrail(source, (char) fromUChar32, flush);
            } else {
                if (source.hasArray() && target.hasArray()) {
                    /* optimized loop */

                    /*
                     * extract arrays from the buffers and obtain various constant values that will
                     * be necessary in the core loop
                     */
                    char[] sourceArray = source.array();
                    byte[] targetArray = target.array();
                    int offset = oldTarget - oldSource;
                    int sourceLength = source.limit() - oldSource;
                    int targetLength = target.limit() - oldTarget;
                    int limit = ((sourceLength < targetLength) ? sourceLength : targetLength)
                            + oldSource;

                    /*
                     * perform the core loop... if it returns null, it must be due to an overflow or
                     * underflow
                     */
                    if ((cr = encodeLoopCoreOptimized(source, target, sourceArray, targetArray,
                            oldSource, offset, limit, flush)) == null) {
                        if (sourceLength <= targetLength) {
                            source.position(oldSource + sourceLength);
                            target.position(oldTarget + sourceLength);
                            cr = CoderResult.UNDERFLOW;
                        } else {
                            source.position(oldSource + targetLength + 1);
                            target.position(oldTarget + targetLength);
                            cr = CoderResult.OVERFLOW;
                        }
                    }
                } else {
                    /* unoptimized loop */

                    try {
                        /*
                         * perform the core loop... if it throws an exception, it must be due to an
                         * overflow or underflow
                         */
                        cr = encodeLoopCoreUnoptimized(source, target, flush);

                    } catch (BufferUnderflowException ex) {
                        cr = CoderResult.UNDERFLOW;
                    } catch (BufferOverflowException ex) {
                        cr = CoderResult.OVERFLOW;
                    }
                }
            }

            /* set offsets since the start */
            if (offsets != null) {
                int count = target.position() - oldTarget;
                int sourceIndex = -1;
                while (--count >= 0)
                    offsets.put(++sourceIndex);
            }

            return cr;
        }

        protected CoderResult encodeLoopCoreOptimized(CharBuffer source, ByteBuffer target,
                char[] sourceArray, byte[] targetArray, int oldSource, int offset, int limit,
                boolean flush) {
            int i, ch = 0;

            /*
             * perform ascii conversion from the source array to the target array, making sure each
             * char in the source is within the correct range
             */
            for (i = oldSource; i < limit && (((ch = (int) sourceArray[i]) & 0xff80) == 0); i++)
                targetArray[i + offset] = (byte) ch;

            /*
             * if some byte was not in the correct range, we need to deal with this byte by calling
             * encodeMalformedOrUnmappable and move the source and target positions to reflect the
             * early termination of the loop
             */
            if ((ch & 0xff80) != 0) {
                source.position(i + 1);
                target.position(i + offset);
                return encodeMalformedOrUnmappable(source, ch, flush);
            } else
                return null;
        }

        protected CoderResult encodeLoopCoreUnoptimized(CharBuffer source, ByteBuffer target,
                boolean flush) throws BufferUnderflowException, BufferOverflowException {
            int ch;

            /*
             * perform ascii conversion from the source buffer to the target buffer, making sure
             * each char in the source is within the correct range
             */
            while (((ch = (int) source.get()) & 0xff80) == 0)
                target.put((byte) ch);

            /*
             * if we reach here, it's because a character was not in the correct range, and we need
             * to deak with this by calling encodeMalformedOrUnmappable.
             */
            return encodeMalformedOrUnmappable(source, ch, flush);
        }

        protected CoderResult encodeMalformedOrUnmappable(CharBuffer source, int ch, boolean flush) {
            /*
             * if the character is a lead surrogate, we need to call encodeTrail to attempt to match
             * it up with a trail surrogate. if not, the character is unmappable.
             */
            return (UTF16.isLeadSurrogate((char) ch)) ? encodeTrail(source, (char) ch, flush)
                    : CoderResult.unmappableForLength(1);
        }

        protected CoderResult encodeTrail(CharBuffer source, char lead, boolean flush) {
            /*
             * if the next character is a trail surrogate, we have an unmappable codepoint of length
             * 2. if the next character is not a trail surrogate, we have a single malformed
             * character. if there is no next character, we either have a malformed character or an
             * underflow, depending on whether flush is enabled.
             */
            if (source.hasRemaining()) {
                char trail = source.get();
                if (UTF16.isTrailSurrogate(trail)) {
                    fromUChar32 = UCharacter.getCodePoint(lead, trail);
                    return CoderResult.unmappableForLength(2); /* two chars */
                } else {
                    fromUChar32 = lead;
                    source.position(source.position() - 1); /* rewind by 1 */
                    return CoderResult.malformedForLength(1);
                }
            } else {
                fromUChar32 = lead;
                if (flush)
                    return CoderResult.malformedForLength(1);
                else
                    return CoderResult.UNDERFLOW;
            }
        }

    }

    public CharsetDecoder newDecoder() {
        return new CharsetDecoder2022JP(this);
    }

    public CharsetEncoder newEncoder() {
        return new CharsetEncoder2022(this);
    }


}
