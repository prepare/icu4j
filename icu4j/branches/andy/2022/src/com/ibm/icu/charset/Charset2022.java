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
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.UTF16;
import com.ibm.icu.util.ULocale;

public class Charset2022 extends CharsetICU {
    
    static final String SHIFT_IN_STR  = "\u000F";
    static final String SHIFT_OUT_STR = "\u000E";

    static char  CR    = '\r';
    static char  LF    = '\n';
    static char  H_TAB = '\u0009';
    static char  V_TAB = '\u000B';
    static char  SPACE = '\u0020';
    
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
    //  typedef enum  {
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
   private static boolean IS_JP_DBCS(short cs) {
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
    //  One of these set up at the time a Charset is opened.
    //    TODO:  from, to stuff probably wants to be moved to Encoder, Decoder.
    //           It's here from the C heritage.
    class UConverterDataISO2022 {
        UConverterSharedData[] myConverterArray;    // Sub-converters.
        CharsetICU             currentConverter;
        int                    currentType;        // enum Cnv2022Type
        ISO2022State           toU2022State;
        ISO2022State           fromU2022State;
        int                    key;                // TODO:  double check signed issues.  Was unsigned in C
        int                    version;
        String                 name;
        String                 locale;
        
        UConverterDataISO2022() {
            myConverterArray = new UConverterSharedData[UCNV_2022_MAX_CONVERTERS];
            toU2022State     = new ISO2022State();
            fromU2022State   = new ISO2022State();
        }
    };

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

   
    
     /*const UConverterSharedData _ISO2022Data;*/
     static UConverterSharedData _ISO2022JPData;
     static UConverterSharedData _ISO2022KRData;
     static UConverterSharedData _ISO2022CNData;
     
     
     
     
    //  ======================
    public Charset2022(String icuCanonicalName, String javaCanonicalName, String[] aliases) {
        super(icuCanonicalName, javaCanonicalName, aliases);
        
        String locale = ULocale.getDefault().getLanguage();
        
       this._ISO2022Open(icuCanonicalName, locale, options);

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
    
    void 
    _ISO2022Open(String name, String locale, int options) {
    /*


        cnv->extraInfo = uprv_malloc (sizeof (UConverterDataISO2022));
        if(cnv->extraInfo != NULL) {
            UConverterDataISO2022 *myConverterData=(UConverterDataISO2022 *) cnv->extraInfo;
            uint32_t version;

            uprv_memset(myConverterData, 0, sizeof(UConverterDataISO2022));
            myConverterData->currentType = ASCII1;
            cnv->fromUnicodeStatus =FALSE;
            if(locale){
                uprv_strncpy(myLocale, locale, sizeof(myLocale));
            }
            version = options & UCNV_OPTIONS_VERSION_MASK;
            myConverterData->version = version;
            if(myLocale[0]=='j' && (myLocale[1]=='a'|| myLocale[1]=='p') && 
                (myLocale[2]=='_' || myLocale[2]=='\0'))
            {
                size_t len=0;
                // open the required converters and cache them 
                if(jpCharsetMasks[version]&CSM(ISO8859_7)) {
                    myConverterData->myConverterArray[ISO8859_7]= ucnv_loadSharedData("ISO8859_7", NULL, errorCode);
                }
                myConverterData->myConverterArray[JISX201]      = ucnv_loadSharedData("JISX0201", NULL, errorCode);
                myConverterData->myConverterArray[JISX208]      = ucnv_loadSharedData("jisx-208", NULL, errorCode);
                if(jpCharsetMasks[version]&CSM(JISX212)) {
                    myConverterData->myConverterArray[JISX212]  = ucnv_loadSharedData("jisx-212", NULL, errorCode);
                }
                if(jpCharsetMasks[version]&CSM(GB2312)) {
                    myConverterData->myConverterArray[GB2312]   = ucnv_loadSharedData("ibm-5478", NULL, errorCode);   // gb_2312_80-1 
                }
                if(jpCharsetMasks[version]&CSM(KSC5601)) {
                    myConverterData->myConverterArray[KSC5601]  = ucnv_loadSharedData("ksc_5601", NULL, errorCode);
                }

                // set the function pointers to appropriate funtions 
                cnv->sharedData=(UConverterSharedData*)(&_ISO2022JPData);
                uprv_strcpy(myConverterData->locale,"ja");

                uprv_strcpy(myConverterData->name,"ISO_2022,locale=ja,version=");
                len = uprv_strlen(myConverterData->name);
                myConverterData->name[len]=(char)(myConverterData->version+(int)'0');
                myConverterData->name[len+1]='\0';
            }
            else if(myLocale[0]=='k' && (myLocale[1]=='o'|| myLocale[1]=='r') && 
                (myLocale[2]=='_' || myLocale[2]=='\0'))
            {
                if (version==1){
                    myConverterData->currentConverter=
                        ucnv_open("icu-internal-25546",errorCode);

                    if (U_FAILURE(*errorCode)) {
                        _ISO2022Close(cnv);
                        return;
                    }

                    uprv_strcpy(myConverterData->name,"ISO_2022,locale=ko,version=1");
                    uprv_memcpy(cnv->subChars, myConverterData->currentConverter->subChars, 4);
                    cnv->subCharLen = myConverterData->currentConverter->subCharLen;
                }else{
                    myConverterData->currentConverter=ucnv_open("ibm-949",errorCode);

                    if (U_FAILURE(*errorCode)) {
                        _ISO2022Close(cnv);
                        return;
                    }

                    myConverterData->version = 0;
                    uprv_strcpy(myConverterData->name,"ISO_2022,locale=ko,version=0");
                }

                // initialize the state variables 
                setInitialStateToUnicodeKR(cnv, myConverterData);
                setInitialStateFromUnicodeKR(cnv, myConverterData);

                // set the function pointers to appropriate funtions
                cnv->sharedData=(UConverterSharedData*)&_ISO2022KRData;
                uprv_strcpy(myConverterData->locale,"ko");
            }
            else if(((myLocale[0]=='z' && myLocale[1]=='h') || (myLocale[0]=='c'&& myLocale[1]=='n'))&& 
                (myLocale[2]=='_' || myLocale[2]=='\0'))
            {

                // open the required converters and cache them 
                myConverterData->myConverterArray[GB2312_1]         = ucnv_loadSharedData("ibm-5478", NULL, errorCode);
                if(version==1) {
                    myConverterData->myConverterArray[ISO_IR_165]   = ucnv_loadSharedData("iso-ir-165", NULL, errorCode);
                }
                myConverterData->myConverterArray[CNS_11643]        = ucnv_loadSharedData("cns-11643-1992", NULL, errorCode);


                // set the function pointers to appropriate funtions 
                cnv->sharedData=(UConverterSharedData*)&_ISO2022CNData;
                uprv_strcpy(myConverterData->locale,"cn");

                if (version==1){
                    uprv_strcpy(myConverterData->name,"ISO_2022,locale=zh,version=1");
                }else{
                    myConverterData->version = 0;
                    uprv_strcpy(myConverterData->name,"ISO_2022,locale=zh,version=0");
                }
            }
            else{
                *errorCode = U_UNSUPPORTED_ERROR;
                return;
            }

            cnv->maxBytesPerUChar=cnv->sharedData->staticData->maxBytesPerChar;

            if(U_FAILURE(*errorCode)) {
                _ISO2022Close(cnv);
            }
        } else {
            *errorCode = U_MEMORY_ALLOCATION_ERROR;
        }
        */
    }


    class CharsetDecoder2022 extends CharsetDecoderICU {

        public CharsetDecoder2022(CharsetICU cs) {
            super(cs);
        }

        void  setInitialStateToUnicodeKR(Charset2022 converter, UConverterDataISO2022 myConverterData) {
            if(myConverterData.version == 1) {
                toUnicodeStatus = 0;     // offset,    field of CharsetICU 
                mode            = 0;     // state,     field of CharsetICU 
                toULength       = 0;     // byteIndex, field of CharsetICU 
            }
        }
        
      

  
        

        protected CoderResult decodeLoop(ByteBuffer source, CharBuffer target, IntBuffer offsets,
                boolean flush) {
            if (!source.hasRemaining() && toUnicodeStatus == 0) {
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

            if (source.hasArray() && target.hasArray()) {
                /* optimized loop */

                /*
                 * extract arrays from the buffers and obtain various constant values that will be
                 * necessary in the core loop
                 */
                byte[] sourceArray = source.array();
                char[] targetArray = target.array();
                int offset = oldTarget - oldSource;
                int sourceLength = source.limit() - oldSource;
                int targetLength = target.limit() - oldTarget;
                int limit = ((sourceLength < targetLength) ? sourceLength : targetLength)
                        + oldSource;

                /*
                 * perform the core loop... if it returns null, it must be due to an overflow or
                 * underflow
                 */
                if ((cr = decodeLoopCoreOptimized(source, target, sourceArray, targetArray,
                        oldSource, offset, limit)) == null) {
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
                    cr = decodeLoopCoreUnoptimized(source, target);

                } catch (BufferUnderflowException ex) {
                    /* all of the source has been read */
                    cr = CoderResult.UNDERFLOW;
                } catch (BufferOverflowException ex) {
                    /* the target is full */
                    cr = CoderResult.OVERFLOW;
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

        protected CoderResult decodeLoopCoreOptimized(ByteBuffer source, CharBuffer target,
                byte[] sourceArray, char[] targetArray, int oldSource, int offset, int limit) {
            int i, ch = 0;

            /*
             * perform ascii conversion from the source array to the target array, making sure each
             * byte in the source is within the correct range
             */
            for (i = oldSource; i < limit && (((ch = (sourceArray[i] & 0xff)) & 0x80) == 0); i++)
                targetArray[i + offset] = (char) ch;

            /*
             * if some byte was not in the correct range, we need to deal with this byte by calling
             * decodeMalformedOrUnmappable and move the source and target positions to reflect the
             * early termination of the loop
             */
            if ((ch & 0x80) != 0) {
                source.position(i + 1);
                target.position(i + offset);
                return decodeMalformedOrUnmappable(ch);
            } else
                return null;
        }

        protected CoderResult decodeLoopCoreUnoptimized(ByteBuffer source, CharBuffer target)
                throws BufferUnderflowException, BufferOverflowException {
            int ch = 0;

            /*
             * perform ascii conversion from the source buffer to the target buffer, making sure
             * each byte in the source is within the correct range
             */
            while (((ch = (source.get() & 0xff)) & 0x80) == 0)
                target.put((char) ch);

            /*
             * if we reach here, it's because a character was not in the correct range, and we need
             * to deak with this by calling decodeMalformedOrUnmappable
             */
            return decodeMalformedOrUnmappable(ch);
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
        
        void setInitialStateFromUnicodeKR(Charset2022 converter, UConverterDataISO2022 myConverterData) {
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
        return new CharsetDecoder2022(this);
    }

    public CharsetEncoder newEncoder() {
        return new CharsetEncoder2022(this);
    }


}
