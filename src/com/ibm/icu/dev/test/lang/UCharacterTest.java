/**
*******************************************************************************
* Copyright (C) 1996-2003, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
* $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/test/lang/UCharacterTest.java,v $
* $Date: 2003/06/03 18:49:30 $
* $Revision: 1.56 $
*
*******************************************************************************
*/

package com.ibm.icu.dev.test.lang;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.dev.test.TestUtil;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UCharacterCategory;
import com.ibm.icu.lang.UCharacterDirection;
import com.ibm.icu.lang.UProperty;
import com.ibm.icu.lang.UScript;
import com.ibm.icu.text.UTF16;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.util.RangeValueIterator;
import com.ibm.icu.util.ValueIterator;
import com.ibm.icu.util.VersionInfo;
import com.ibm.icu.impl.UCharacterName;
import com.ibm.icu.impl.Utility;
import com.ibm.icu.impl.USerializedSet;
import com.ibm.icu.impl.NormalizerImpl;
import java.io.BufferedReader;
import java.util.Arrays;

/**
* Testing class for UCharacter
* Mostly following the test cases for ICU
* @author Syn Wee Quek
* @since nov 04 2000
*/
public final class UCharacterTest extends TestFmwk
{
    // private variables =============================================

    /**
    * ICU4J data version number
    */
    private final VersionInfo VERSION_ = VersionInfo.getInstance("4.0.0.0");

    // constructor ===================================================

    /**
    * Constructor
    */
    public UCharacterTest()
    {
    }

    // public methods ================================================

    public static void main(String[] arg)
    {
        try
        {
            UCharacterTest test = new UCharacterTest();
            test.run(arg);
        }
        catch (Exception e)
        {
        e.printStackTrace();
        }
    }

    /**
    * Testing the letter and number determination in UCharacter
    */
    public void TestLetterNumber()
    {
        for (int i = 0x0041; i < 0x005B; i ++)
        if (!UCharacter.isLetter(i))
            errln("FAIL \\u" + hex(i) + " expected to be a letter");

        for (int i = 0x0660; i < 0x066A; i ++)
        if (UCharacter.isLetter(i))
            errln("FAIL \\u" + hex(i) + " expected not to be a letter");

        for (int i = 0x0660; i < 0x066A; i ++)
        if (!UCharacter.isDigit(i))
            errln("FAIL \\u" + hex(i) + " expected to be a digit");

        for (int i = 0x0041; i < 0x005B; i ++)
            if (!UCharacter.isLetterOrDigit(i))
                errln("FAIL \\u" + hex(i) + " expected not to be a digit");

        for (int i = 0x0660; i < 0x066A; i ++)
            if (!UCharacter.isLetterOrDigit(i))
                errln("FAIL \\u" + hex(i) +
                    "expected to be either a letter or a digit");
        
        /*
         * The following checks work only starting from Unicode 4.0.
         * Check the version number here.
         */
        VersionInfo version =	UCharacter.getUnicodeVersion();
        if(version.getMajor()<4) {
            return;
        }



        /*
         * Sanity check:
         * Verify that exactly the digit characters have decimal digit values.
         * This assumption is used in the implementation of u_digit()
         * (which checks nt=de)
         * compared with the parallel java.lang.Character.digit()
         * (which checks Nd).
         *
         * This was not true in Unicode 3.2 and earlier.
         * The following characters had decimal digit values but were No not Nd.
         * (from DerivedNumericType-3.2.0.txt)
00B2..00B3    ; decimal # No   [2] SUPERSCRIPT TWO..SUPERSCRIPT THREE
00B9          ; decimal # No       SUPERSCRIPT ONE
2070          ; decimal # No       SUPERSCRIPT ZERO
2074..2079    ; decimal # No   [6] SUPERSCRIPT FOUR..SUPERSCRIPT NINE
2080..2089    ; decimal # No  [10] SUBSCRIPT ZERO..SUBSCRIPT NINE
         */
        String digitsPattern = "[:Nd:]";
        String decimalValuesPattern = "[:Numeric_Type=Decimal:]";

        UnicodeSet digits, decimalValues;

        digits= new UnicodeSet(digitsPattern);
        decimalValues=new UnicodeSet(decimalValuesPattern);

    
        compareUSets(digits, decimalValues, "[:Nd:]", "[:Numeric_Type=Decimal:]", true);


    }

    /**
    * Tests for space determination in UCharacter
    */
    public void TestSpaces()
    {
        int spaces[] = {0x0020, 0x0000a0, 0x002000, 0x002001, 0x002005};
        int nonspaces[] = {0x61, 0x0062, 0x0063, 0x0064, 0x0074};
        int whitespaces[] = {0x2008, 0x002009, 0x00200a, 0x00001c, 0x00000c};
        int nonwhitespaces[] = {0x61, 0x0062, 0x003c, 0x0028, 0x003f};

        int size = spaces.length;
        for (int i = 0; i < size; i ++)
        {
            if (!UCharacter.isSpaceChar(spaces[i]))
            {
                errln("FAIL \\u" + hex(spaces[i]) +
                    " expected to be a space character");
                break;
            }

            if (UCharacter.isSpaceChar(nonspaces[i]))
            {
                errln("FAIL \\u" + hex(nonspaces[i]) +
                " expected not to be space character");
                break;
            }

            if (!UCharacter.isWhitespace(whitespaces[i]))
            {
                errln("FAIL \\u" + hex(whitespaces[i]) +
                        " expected to be a white space character");
                break;
            }
            if (UCharacter.isWhitespace(nonwhitespaces[i]))
            {
                errln("FAIL \\u" + hex(nonwhitespaces[i]) +
                            " expected not to be a space character");
                break;
            }
            logln("Ok    \\u" + hex(spaces[i]) + " and \\u" +
                  hex(nonspaces[i]) + " and \\u" + hex(whitespaces[i]) +
                  " and \\u" + hex(nonwhitespaces[i]));
        }
    }

    /**
    * Tests for defined and undefined characters
    */
    public void TestDefined()
    {
        int undefined[] = {0xfff1, 0xfff7, 0xfa6b};
        int defined[] = {0x523E, 0x004f88, 0x00fffd};

        int size = undefined.length;
        for (int i = 0; i < size; i ++)
        {
            if (UCharacter.isDefined(undefined[i]))
            {
                errln("FAIL \\u" + hex(undefined[i]) +
                            " expected not to be defined");
                break;
            }
            if (!UCharacter.isDefined(defined[i]))
            {
                errln("FAIL \\u" + hex(defined[i]) + " expected defined");
                break;
            }
        }
    }

    /**
    * Tests for base characters and their cellwidth
    */
    public void TestBase()
    {
        int base[] = {0x0061, 0x000031, 0x0003d2};
        int nonbase[] = {0x002B, 0x000020, 0x00203B};
        int size = base.length;
        for (int i = 0; i < size; i ++)
        {
            if (UCharacter.isBaseForm(nonbase[i]))
            {
                errln("FAIL \\u" + hex(nonbase[i]) +
                            " expected not to be a base character");
                break;
            }
            if (!UCharacter.isBaseForm(base[i]))
            {
                errln("FAIL \\u" + hex(base[i]) +
                      " expected to be a base character");
                break;
            }
        }
    }

    /**
    * Tests for digit characters
    */
    public void TestDigits()
    {
        int digits[] = {0x0030, 0x000662, 0x000F23, 0x000ED5, 0x002160};

        //special characters not in the properties table
        int digits2[] = {0x3007, 0x004e00, 0x004e8c, 0x004e09, 0x0056d8,
                         0x004e94, 0x00516d, 0x4e03, 0x00516b, 0x004e5d};
        int nondigits[] = {0x0010, 0x000041, 0x000122, 0x0068FE};

        int digitvalues[] = {0, 2, 3, 5, 1};
        int digitvalues2[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        int size  = digits.length;
        for (int i = 0; i < size; i ++) {
            if (UCharacter.isDigit(digits[i]) &&
                UCharacter.digit(digits[i]) != digitvalues[i])
            {
                errln("FAIL \\u" + hex(digits[i]) +
                        " expected digit with value " + digitvalues[i]);
                break;
            }
        } 
        size = nondigits.length;
        for (int i = 0; i < size; i ++)
            if (UCharacter.isDigit(nondigits[i]))
            {
                errln("FAIL \\u" + hex(nondigits[i]) + " expected nondigit");
                break;
            }

        size = digits2.length;
        for (int i = 0; i < 10; i ++) {
            if (UCharacter.isDigit(digits2[i]) &&
                UCharacter.digit(digits2[i]) != digitvalues2[i])
            {
                errln("FAIL \\u" + hex(digits2[i]) +
                    " expected digit with value " + digitvalues2[i]);
                break;
            }
        }
    }

    /**
    *  Tests for numeric characters
    */
    public void TestNumeric()
    {
        for (int i = '0'; i < '9'; i ++) {
            int n1 = UCharacter.getNumericValue(i);
            double n2 = UCharacter.getUnicodeNumericValue(i);
            if (n1 != n2 ||  n1 != (i - '0')) {
                errln("Numeric value of " + (char)i + " expected to be " +
                      (i - '0'));
            }
        }
        for (int i = 'A'; i < 'F'; i ++) {
            int n1 = UCharacter.getNumericValue(i);
            double n2 = UCharacter.getUnicodeNumericValue(i);
            if (n2 != UCharacter.NO_NUMERIC_VALUE ||  n1 != (i - 'A' + 10)) {
                errln("Numeric value of " + (char)i + " expected to be " +
                      (i - 'A' + 10));
            }
        }
        for (int i = 0xFF21; i < 0xFF26; i ++) {
            // testing full wideth latin characters A-F
            int n1 = UCharacter.getNumericValue(i);
            double n2 = UCharacter.getUnicodeNumericValue(i);
            if (n2 != UCharacter.NO_NUMERIC_VALUE ||  n1 != (i - 0xFF21 + 10)) {
                errln("Numeric value of " + (char)i + " expected to be " +
                      (i - 0xFF21 + 10));
            }
        }
        // testing han numbers
        int han[] = {0x96f6, 0, 0x58f9, 1, 0x8cb3, 2, 0x53c3, 3,
                     0x8086, 4, 0x4f0d, 5, 0x9678, 6, 0x67d2, 7,
                     0x634c, 8, 0x7396, 9, 0x5341, 10, 0x62fe, 10,
                     0x767e, 100, 0x4f70, 100, 0x5343, 1000, 0x4edf, 1000,
                     0x824c, 10000, 0x5104, 100000000};
        for (int i = 0; i < han.length; i += 2) {
            if (UCharacter.getHanNumericValue(han[i]) != han[i + 1]) {
                errln("Numeric value of \\u" +
                      Integer.toHexString(han[i]) + " expected to be " +
                      han[i + 1]);
            }
        }
    }

    /**
    * Tests for version
    */
    public void TestVersion()
    {
        if (!UCharacter.getUnicodeVersion().equals(VERSION_))
            errln("FAIL expected: " + VERSION_ + "got: " + UCharacter.getUnicodeVersion());
    }

    /**
    * Tests for control characters
    */
    public void TestISOControl()
    {
        int control[] = {0x001b, 0x000097, 0x000082};
        int noncontrol[] = {0x61, 0x000031, 0x0000e2};

        int size = control.length;
        for (int i = 0; i < size; i ++)
        {
            if (!UCharacter.isISOControl(control[i]))
            {
                errln("FAIL 0x" + Integer.toHexString(control[i]) +
                        " expected to be a control character");
                break;
            }
            if (UCharacter.isISOControl(noncontrol[i]))
            {
                errln("FAIL 0x" + Integer.toHexString(noncontrol[i]) +
                        " expected to be not a control character");
                break;
            }

            logln("Ok    0x" + Integer.toHexString(control[i]) + " and 0x" +
                    Integer.toHexString(noncontrol[i]));
        }
    }

    /**
     * Test Supplementary
     */
    public void TestSupplementary()
    {
        for (int i = 0; i < 0x10000; i ++) {
            if (UCharacter.isSupplementary(i)) {
                errln("Codepoint \\u" + Integer.toHexString(i) +
                      " is not supplementary");
            }
        }
        for (int i = 0x10000; i < 0x10FFFF; i ++) {
            if (!UCharacter.isSupplementary(i)) {
                errln("Codepoint \\u" + Integer.toHexString(i) +
                      " is supplementary");
            }
        }
    }

    /**
     * Test mirroring
     */
    public void TestMirror()
    {
        if (!(UCharacter.isMirrored(0x28) && UCharacter.isMirrored(0xbb) &&
              UCharacter.isMirrored(0x2045) && UCharacter.isMirrored(0x232a)
              && !UCharacter.isMirrored(0x27) &&
              !UCharacter.isMirrored(0x61) && !UCharacter.isMirrored(0x284)
              && !UCharacter.isMirrored(0x3400))) {
            errln("isMirrored() does not work correctly");
        }

        if (!(UCharacter.getMirror(0x3c) == 0x3e &&
              UCharacter.getMirror(0x5d) == 0x5b &&
              UCharacter.getMirror(0x208d) == 0x208e &&
              UCharacter.getMirror(0x3017) == 0x3016 &&
              UCharacter.getMirror(0x2e) == 0x2e &&
              UCharacter.getMirror(0x6f3) == 0x6f3 &&
              UCharacter.getMirror(0x301c) == 0x301c &&
              UCharacter.getMirror(0xa4ab) == 0xa4ab)) {
            errln("getMirror() does not work correctly");
        }
    }

    /**
    * Tests for printable characters
    */
    public void TestPrint()
    {
        int printable[] = {0x0042, 0x00005f, 0x002014};
        int nonprintable[] = {0x200c, 0x00009f, 0x00001b};

        int size = printable.length;
        for (int i = 0; i < size; i ++)
        {
            if (!UCharacter.isPrintable(printable[i]))
            {
                errln("FAIL \\u" + hex(printable[i]) +
                    " expected to be a printable character");
                break;
            }
            if (UCharacter.isPrintable(nonprintable[i]))
            {
                errln("FAIL \\u" + hex(nonprintable[i]) +
                        " expected not to be a printable character");
                break;
            }
            logln("Ok    \\u" + hex(printable[i]) + " and \\u" +
                    hex(nonprintable[i]));
        }

        // test all ISO 8 controls
        for (int ch = 0; ch <= 0x9f; ++ ch) {
            if (ch == 0x20) {
                // skip ASCII graphic characters and continue with DEL
                ch = 0x7f;
            }
            if (UCharacter.isPrintable(ch)) {
                errln("Fail \\u" + hex(ch) +
                    " is a ISO 8 control character hence not printable\n");
            }
        }

        /* test all Latin-1 graphic characters */
        for (int ch = 0x20; ch <= 0xff; ++ ch) {
            if (ch == 0x7f) {
                ch = 0xa0;
            }
            if (!UCharacter.isPrintable(ch) 
            	&& ch != 0x00AD/* Unicode 4.0 changed the defintion of soft hyphen to be a Cf*/) {
                errln("Fail \\u" + hex(ch) +
                      " is a Latin-1 graphic character\n");
            }
        }
    }

    /**
    * Testing for identifier characters
    */
    public void TestIdentifier()
    {
        int unicodeidstart[] = {0x0250, 0x0000e2, 0x000061};
        int nonunicodeidstart[] = {0x2000, 0x00000a, 0x002019};
        int unicodeidpart[] = {0x005f, 0x000032, 0x000045};
        int nonunicodeidpart[] = {0x2030, 0x0000a3, 0x000020};
        int idignore[] = {0x0006, 0x0010, 0x206b};
        int nonidignore[] = {0x0075, 0x0000a3, 0x000061};

        int size = unicodeidstart.length;
        for (int i = 0; i < size; i ++)
        {
            if (!UCharacter.isUnicodeIdentifierStart(unicodeidstart[i]))
            {
                errln("FAIL \\u" + hex(unicodeidstart[i]) +
                    " expected to be a unicode identifier start character");
                break;
            }
            if (UCharacter.isUnicodeIdentifierStart(nonunicodeidstart[i]))
            {
                errln("FAIL \\u" + hex(nonunicodeidstart[i]) +
                        " expected not to be a unicode identifier start " +
                        "character");
                break;
            }
            if (!UCharacter.isUnicodeIdentifierPart(unicodeidpart[i]))
            {
                errln("FAIL \\u" + hex(unicodeidpart[i]) +
                    " expected to be a unicode identifier part character");
                break;
            }
            if (UCharacter.isUnicodeIdentifierPart(nonunicodeidpart[i]))
            {
                errln("FAIL \\u" + hex(nonunicodeidpart[i]) +
                        " expected not to be a unicode identifier part " +
                        "character");
                break;
            }
            if (!UCharacter.isIdentifierIgnorable(idignore[i]))
            {
                errln("FAIL \\u" + hex(idignore[i]) +
                        " expected to be a ignorable unicode character");
                break;
            }
            if (UCharacter.isIdentifierIgnorable(nonidignore[i]))
            {
                errln("FAIL \\u" + hex(nonidignore[i]) +
                    " expected not to be a ignorable unicode character");
                break;
            }
            logln("Ok    \\u" + hex(unicodeidstart[i]) + " and \\u" +
                    hex(nonunicodeidstart[i]) + " and \\u" +
                    hex(unicodeidpart[i]) + " and \\u" +
                    hex(nonunicodeidpart[i]) + " and \\u" +
                    hex(idignore[i]) + " and \\u" + hex(nonidignore[i]));
        }
    }

    /**
    * Tests for the character types, direction.<br>
    * This method reads in UnicodeData.txt file for testing purposes. A
    * default path is provided relative to the src path, however the user
    * could set a system property to change the directory path.<br>
    * e.g. java -DUnicodeData="data_directory_path"
    * com.ibm.icu.dev.test.lang.UCharacterTest
    */
    public void TestUnicodeData()
    {
        // this is the 2 char category types used in the UnicodeData file
        final String TYPE =
            "LuLlLtLmLoMnMeMcNdNlNoZsZlZpCcCfCoCsPdPsPePcPoSmScSkSoPiPf";

        // directory types used in the UnicodeData file
        // padded by spaces to make each type size 4
        final String DIR =
            "L   R   EN  ES  ET  AN  CS  B   S   WS  ON  LRE LRO AL  RLE RLO PDF NSM BN  ";

        final int LASTUNICODECHAR = 0xFFFD;
        int ch = 0,
            index = 0,
            type = 0,
            dir = 0;

        try
        {
            BufferedReader input = TestUtil.getDataReader(
                                                "unicode/UnicodeData.txt");
            int numErrors = 0;

            while (ch != LASTUNICODECHAR)
            {
                String s = input.readLine();
                // geting the unicode character, its type and its direction
                ch = Integer.parseInt(s.substring(0, 4), 16);
                index = s.indexOf(';', 5);
                String t = s.substring(index + 1, index + 3);
                index += 4;
                int oldindex = index;
                index = s.indexOf(';', index);
                int cc = Integer.parseInt(s.substring(oldindex, index));
                oldindex = index + 1;
                index = s.indexOf(';', oldindex);
                String d = s.substring(oldindex, index);

                for (int i = 0; i < 6; i ++) {
                    index = s.indexOf(';', index + 1);
                    // skipping to the 11th field
                }
                // iso comment
                oldindex = index + 1;
                index = s.indexOf(';', oldindex);
                String isocomment = s.substring(oldindex, index);
                // uppercase
                oldindex = index + 1;
                index = s.indexOf(';', oldindex);
                String upper = s.substring(oldindex, index);
                // lowercase
                oldindex = index + 1;
                index = s.indexOf(';', oldindex);
                String lower = s.substring(oldindex, index);
                // titlecase last element
                oldindex = index + 1;
                String title = s.substring(oldindex);

                // testing the category
                // we override the general category of some control
                // characters
                type = TYPE.indexOf(t);
                if (type < 0)
                    type = 0;
                else
                    type = (type >> 1) + 1;
                if (UCharacter.getType(ch) != type)
                {
                    errln("FAIL \\u" + hex(ch) + " expected type " +
                            type);
                    break;
                }

                if (UCharacter.getIntPropertyValue(ch,
                           UProperty.GENERAL_CATEGORY_MASK) != (1 << type)) {
                    errln("error: getIntPropertyValue(\\u" +
                          Integer.toHexString(ch) +
                          ", UProperty.GENERAL_CATEGORY_MASK) != " +
                          "getMask(getType(ch))");
                }

                // testing combining class
                if (UCharacter.getCombiningClass(ch) != cc)
                {
                    errln("FAIL \\u" + hex(ch) + " expected combining " +
                            "class " + cc);
                    break;
                }

                // testing the direction
                if (d.length() == 1)
                    d = d + "   ";

                dir = DIR.indexOf(d) >> 2;
                if (UCharacter.getDirection(ch) != dir)
                {
                    errln("FAIL \\u" + hex(ch) +
                        " expected wrong direction " + dir);
                    break;
                }

                // testing iso comment
                try{
                    String comment = UCharacter.getISOComment(ch);
                    if (comment == null) {
                        comment = "";
                    }
                    if (!comment.equals(isocomment)) {
                        errln("FAIL \\u" + hex(ch) +
                            " expected iso comment " + isocomment);
                        break;
                    }
                }catch(Exception e){
                    if(e.getMessage().indexOf("unames.icu") >= 0){
                        numErrors++;
                    }else{
                        throw e;
                    }
                }

                int tempchar = ch;
                if (upper.length() > 0) {
                    tempchar = Integer.parseInt(upper, 16);
                }
                if (UCharacter.toUpperCase(ch) != tempchar) {
                    errln("FAIL \\u" + Utility.hex(ch, 4)
                            + " expected uppercase \\u"
                            + Utility.hex(tempchar, 4));
                    break;
                }
                tempchar = ch;
                if (lower.length() > 0) {
                    tempchar = Integer.parseInt(lower, 16);
                }
                if (UCharacter.toLowerCase(ch) != tempchar) {
                    errln("FAIL \\u" + Utility.hex(ch, 4)
                            + " expected lowercase \\u"
                            + Utility.hex(tempchar, 4));
                    break;
                }
                tempchar = ch;
                if (title.length() > 0) {
                    tempchar = Integer.parseInt(title, 16);
                }
                if (UCharacter.toTitleCase(ch) != tempchar) {
                    errln("FAIL \\u" + Utility.hex(ch, 4)
                            + " expected titlecase \\u"
                            + Utility.hex(tempchar, 4));
                    break;
                }
            }
            input.close();
            if(numErrors > 0){
                warnln("Could not find unames.icu");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        if (UCharacter.UnicodeBlock.of(0x0041)
                                        != UCharacter.UnicodeBlock.BASIC_LATIN
            || UCharacter.getIntPropertyValue(0x41, UProperty.BLOCK)
                              != UCharacter.UnicodeBlock.BASIC_LATIN.getID()) {
            errln("UCharacter.UnicodeBlock.of(\\u0041) property failed! "
                    + "Expected : "
                    + UCharacter.UnicodeBlock.BASIC_LATIN.getID() + " got "
                    + UCharacter.UnicodeBlock.of(0x0041));
        }

        // sanity check on repeated properties
        for (ch = 0xfffe; ch <= 0x10ffff;) {
            type = UCharacter.getType(ch);
            if (UCharacter.getIntPropertyValue(ch,
                                               UProperty.GENERAL_CATEGORY_MASK)
                != (1 << type)) {
                errln("error: UCharacter.getIntPropertyValue(\\u"
                      + Integer.toHexString(ch)
                      + ", UProperty.GENERAL_CATEGORY_MASK) != "
                      + "getMask(getType())");
            }
            if (type != UCharacterCategory.UNASSIGNED) {
                errln("error: UCharacter.getType(\\u" + Utility.hex(ch, 4)
                        + " != UCharacterCategory.UNASSIGNED (returns "
                        + UCharacterCategory.toString(UCharacter.getType(ch))
                        + ")");
            }
            if ((ch & 0xffff) == 0xfffe) {
                ++ ch;
            }
            else {
                ch += 0xffff;
            }
        }

        // test that PUA is not "unassigned"
        for(ch = 0xe000; ch <= 0x10fffd;) {
            type = UCharacter.getType(ch);
            if (UCharacter.getIntPropertyValue(ch,
                                               UProperty.GENERAL_CATEGORY_MASK)
                != (1 << type)) {
                errln("error: UCharacter.getIntPropertyValue(\\u"
                      + Integer.toHexString(ch)
                      + ", UProperty.GENERAL_CATEGORY_MASK) != "
                      + "getMask(getType())");
            }

            if (type == UCharacterCategory.UNASSIGNED) {
                errln("error: UCharacter.getType(\\u"
                        + Utility.hex(ch, 4)
                        + ") == UCharacterCategory.UNASSIGNED");
            }
            else if (type != UCharacterCategory.PRIVATE_USE) {
                logln("PUA override: UCharacter.getType(\\u"
                      + Utility.hex(ch, 4) + ")=" + type);
            }
            if (ch == 0xf8ff) {
                ch = 0xf0000;
            }
            else if (ch == 0xffffd) {
                ch = 0x100000;
            }
            else {
                ++ ch;
            }
        }
    }


    /**
    * Test for the character names
    */
    public void TestNames()
    {
        try{
            int length = UCharacterName.getInstance().getMaxCharNameLength();
            if (length < 83) { // Unicode 3.2 max char name length
               errln("getMaxCharNameLength()=" + length + " is too short");
            }
            // ### TODO same tests for max ISO comment length as for max name length

            int c[] = {0x0061, 0x000284, 0x003401, 0x007fed, 0x00ac00, 0x00d7a3,
                       0x00d800, 0x00dc00, 0xff08, 0x00ffe5, 0x00ffff,
                       0x0023456};
            String name[] = {"LATIN SMALL LETTER A",
                         "LATIN SMALL LETTER DOTLESS J WITH STROKE AND HOOK",
                         "CJK UNIFIED IDEOGRAPH-3401",
                         "CJK UNIFIED IDEOGRAPH-7FED", "HANGUL SYLLABLE GA",
                         "HANGUL SYLLABLE HIH", "", "",
                         "FULLWIDTH LEFT PARENTHESIS",
                         "FULLWIDTH YEN SIGN", "", "CJK UNIFIED IDEOGRAPH-23456"};
            String oldname[] = {"", "LATIN SMALL LETTER DOTLESS J BAR HOOK", "",
                            "",
                            "", "", "", "", "FULLWIDTH OPENING PARENTHESIS", "",
                            "", ""};
            String extendedname[] = {"LATIN SMALL LETTER A",
                                 "LATIN SMALL LETTER DOTLESS J WITH STROKE AND HOOK",
                                 "CJK UNIFIED IDEOGRAPH-3401",
                                 "CJK UNIFIED IDEOGRAPH-7FED",
                                 "HANGUL SYLLABLE GA",
                                 "HANGUL SYLLABLE HIH",
                                 "<lead surrogate-D800>",
                                 "<trail surrogate-DC00>",
                                 "FULLWIDTH LEFT PARENTHESIS",
                                 "FULLWIDTH YEN SIGN",
                                 "<noncharacter-FFFF>",
                                 "CJK UNIFIED IDEOGRAPH-23456"};

            int size = c.length;
            String str;
            int uc;

            for (int i = 0; i < size; i ++)
            {
                // modern Unicode character name
                str = UCharacter.getName(c[i]);
                if ((str == null && name[i].length() > 0) ||
                    (str != null && !str.equals(name[i])))
                {
                    errln("FAIL \\u" + hex(c[i]) + " expected name " +
                            name[i]);
                    break;
                }

                // 1.0 Unicode character name
                str = UCharacter.getName1_0(c[i]);
                if ((str == null && oldname[i].length() > 0) ||
                    (str != null && !str.equals(oldname[i])))
                {
                    errln("FAIL \\u" + hex(c[i]) + " expected 1.0 name " +
                            oldname[i]);
                    break;
                }

                // extended character name
                str = UCharacter.getExtendedName(c[i]);
                if (str == null || !str.equals(extendedname[i]))
                {
                    errln("FAIL \\u" + hex(c[i]) + " expected extended name " +
                            extendedname[i]);
                    break;
                }

                // retrieving unicode character from modern name
                uc = UCharacter.getCharFromName(name[i]);
                if (uc != c[i] && name[i].length() != 0)
                {
                    errln("FAIL " + name[i] + " expected character \\u" +
                          hex(c[i]));
                    break;
                }

                //retrieving unicode character from 1.0 name
                uc = UCharacter.getCharFromName1_0(oldname[i]);
                if (uc != c[i] && oldname[i].length() != 0)
                {
                    errln("FAIL " + oldname[i] + " expected 1.0 character \\u" +
                          hex(c[i]));
                    break;
                }

                //retrieving unicode character from 1.0 name
                uc = UCharacter.getCharFromExtendedName(extendedname[i]);
                if (uc != c[i] && i != 0 && (i == 1 || i == 6))
                {
                    errln("FAIL " + extendedname[i] +
                          " expected extended character \\u" + hex(c[i]));
                    break;
                }
            }

            // test getName works with mixed-case names (new in 2.0)
            if (0x61 != UCharacter.getCharFromName("LATin smALl letTER A")) {
                errln("FAIL: 'LATin smALl letTER A' should result in character "
                      + "U+0061");
            }

            if (getInclusion() >= 5) {
                // extra testing different from icu
                for (int i = UCharacter.MIN_VALUE; i < UCharacter.MAX_VALUE; i ++)
                {
                    str = UCharacter.getName(i);
                    if (str != null && UCharacter.getCharFromName(str) != i)
                    {
                        errln("FAIL \\u" + hex(i) + " " + str  +
                                            " retrieval of name and vice versa" );
                        break;
                    }
                }
            }

            // Test getCharNameCharacters
            if (getInclusion() >= 10) {
                boolean map[] = new boolean[256];

                UnicodeSet set = new UnicodeSet(1, 0); // empty set
                UnicodeSet dumb = new UnicodeSet(1, 0); // empty set

                // uprv_getCharNameCharacters() will likely return more lowercase
                // letters than actual character names contain because
                // it includes all the characters in lowercased names of
                // general categories, for the full possible set of extended names.
                UCharacterName.getInstance().getCharNameCharacters(set);

                // build set the dumb (but sure-fire) way
                Arrays.fill(map, false);

                int maxLength = 0;
                for (int cp = 0; cp < 0x110000; ++ cp) {
                    String n = UCharacter.getExtendedName(cp);
                    int len = n.length();
                    if (len > maxLength) {
                        maxLength = len;
                    }

                    for (int i = 0; i < len; ++ i) {
                        char ch = n.charAt(i);
                        if (!map[ch & 0xff]) {
                            dumb.add(ch);
                            map[ch & 0xff] = true;
                        }
                    }
                }

                length = UCharacterName.getInstance().getMaxCharNameLength();
                if (length != maxLength) {
                    errln("getMaxCharNameLength()=" + length
                          + " differs from the maximum length " + maxLength
                          + " of all extended names");
                }

                // compare the sets.  Where is my uset_equals?!!
                boolean ok = true;
                for (int i = 0; i < 256; ++ i) {
                    if (set.contains(i) != dumb.contains(i)) {
                        if (0x61 <= i && i <= 0x7a // a-z
                            && set.contains(i) && !dumb.contains(i)) {
                            // ignore lowercase a-z that are in set but not in dumb
                            ok = true;
                        }
                        else {
                            ok = false;
                            break;
                        }
                    }
                }

                String pattern1 = set.toPattern(true);
                String pattern2 = dumb.toPattern(true);

                if (!ok) {
                    errln("FAIL: getCharNameCharacters() returned " + pattern1
                          + " expected " + pattern2
                          + " (too many lowercase a-z are ok)");
                } else {
                    logln("Ok: getCharNameCharacters() returned " + pattern1);
                }
            }
        }catch(IllegalArgumentException e){
            if(e.getMessage().indexOf("unames.icu") >= 0){
                warnln("Could not find unames.icu");
            }else{
                throw e;
            }
        }
    }

    /**
    * Testing name iteration
    */
    public void TestNameIteration()throws Exception
    {
        try {
            ValueIterator iterator = UCharacter.getExtendedNameIterator();
            ValueIterator.Element element = new ValueIterator.Element();
            ValueIterator.Element old     = new ValueIterator.Element();
            // testing subrange
            iterator.setRange(-10, -5);
            if (iterator.next(element)) {
                errln("Fail, expected iterator to return false when range is set outside the meaningful range");
            }
            iterator.setRange(0x110000, 0x111111);
            if (iterator.next(element)) {
                errln("Fail, expected iterator to return false when range is set outside the meaningful range");
            }
            try {
                iterator.setRange(50, 10);
                errln("Fail, expected exception when encountered invalid range");
            } catch (Exception e) {
            }

            iterator.setRange(-10, 10);
            if (!iterator.next(element) || element.integer != 0) {
                errln("Fail, expected iterator to return 0 when range start limit is set outside the meaningful range");
            }

            iterator.setRange(0x10FFFE, 0x200000);
            int last = 0;
            while (iterator.next(element)) {
                last = element.integer;
            }
            if (last != 0x10FFFF) {
                errln("Fail, expected iterator to return 0x10FFFF when range end limit is set outside the meaningful range");
            }

            iterator = UCharacter.getNameIterator();
            iterator.setRange(0xF, 0x45);
            while (iterator.next(element)) {
                if (element.integer <= old.integer) {
                    errln("FAIL next returned a less codepoint \\u" +
                        Integer.toHexString(element.integer) + " than \\u" +
                        Integer.toHexString(old.integer));
                    break;
                }
                if (!UCharacter.getName(element.integer).equals(element.value))
                {
                    errln("FAIL next codepoint \\u" +
                        Integer.toHexString(element.integer) +
                        " does not have the expected name " +
                        UCharacter.getName(element.integer) +
                        " instead have the name " + (String)element.value);
                    break;
                }
                old.integer = element.integer;
            }

            iterator.reset();
            iterator.next(element);
            if (element.integer != 0x20) {
                errln("FAIL reset in iterator");
            }

            iterator.setRange(0, 0x110000);
            old.integer = 0;
            while (iterator.next(element)) {
                if (element.integer != 0 && element.integer <= old.integer) {
                    errln("FAIL next returned a less codepoint \\u" +
                        Integer.toHexString(element.integer) + " than \\u" +
                        Integer.toHexString(old.integer));
                    break;
                }
                if (!UCharacter.getName(element.integer).equals(element.value))
                {
                    errln("FAIL next codepoint \\u" +
                            Integer.toHexString(element.integer) +
                            " does not have the expected name " +
                            UCharacter.getName(element.integer) +
                            " instead have the name " + (String)element.value);
                    break;
                }
                for (int i = old.integer + 1; i < element.integer; i ++) {
                    if (UCharacter.getName(i) != null) {
                        errln("FAIL between codepoints are not null \\u" +
                                Integer.toHexString(old.integer) + " and " +
                                Integer.toHexString(element.integer) + " has " +
                                Integer.toHexString(i) + " with a name " +
                                UCharacter.getName(i));
                        break;
                    }
                }
                old.integer = element.integer;
            }

            iterator = UCharacter.getExtendedNameIterator();
            old.integer = 0;
            while (iterator.next(element)) {
                if (element.integer != 0 && element.integer != old.integer) {
                    errln("FAIL next returned a codepoint \\u" +
                            Integer.toHexString(element.integer) +
                            " different from \\u" +
                            Integer.toHexString(old.integer));
                    break;
                }
                if (!UCharacter.getExtendedName(element.integer).equals(
                                                              element.value)) {
                    errln("FAIL next codepoint \\u" +
                        Integer.toHexString(element.integer) +
                        " name should be "
                        + UCharacter.getExtendedName(element.integer) +
                        " instead of " + (String)element.value);
                    break;
                }
                old.integer++;
            }
            iterator = UCharacter.getName1_0Iterator();
            old.integer = 0;
            while (iterator.next(element)) {
                logln(Integer.toHexString(element.integer) + " " +
                                                        (String)element.value);
                if (element.integer != 0 && element.integer <= old.integer) {
                    errln("FAIL next returned a less codepoint \\u" +
                        Integer.toHexString(element.integer) + " than \\u" +
                        Integer.toHexString(old.integer));
                    break;
                }
                if (!element.value.equals(UCharacter.getName1_0(
                                                            element.integer))) {
                    errln("FAIL next codepoint \\u" +
                            Integer.toHexString(element.integer) +
                            " name cannot be null");
                    break;
                }
                for (int i = old.integer + 1; i < element.integer; i ++) {
                    if (UCharacter.getName1_0(i) != null) {
                        errln("FAIL between codepoints are not null \\u" +
                            Integer.toHexString(old.integer) + " and " +
                            Integer.toHexString(element.integer) + " has " +
                            Integer.toHexString(i) + " with a name " +
                            UCharacter.getName1_0(i));
                        break;
                    }
                }
                old.integer = element.integer;
            }
        } catch(Exception e){
            // !!! wouldn't preflighting be simpler?  This looks like
            // it is effectively be doing that.  It seems that for every
            // true error the code will call errln, which will throw the error, which
            // this will catch, which this will then rethrow the error.  Just seems
            // cumbersome.
            if(e.getMessage().indexOf("unames.icu") >= 0){
                warnln("Could not find unames.icu");
            } else {
                errln(e.getMessage());
            }
        }
    }

    /**
    * Testing the for illegal characters
    */
    public void TestIsLegal()
    {
        int illegal[] = {0xFFFE, 0x00FFFF, 0x005FFFE, 0x005FFFF, 0x0010FFFE,
                         0x0010FFFF, 0x110000, 0x00FDD0, 0x00FDDF, 0x00FDE0,
                         0x00FDEF, 0xD800, 0xDC00, -1};
        int legal[] = {0x61, 0x00FFFD, 0x0010000, 0x005FFFD, 0x0060000,
                       0x0010FFFD, 0xFDCF, 0x00FDF0};
        for (int count = 0; count < illegal.length; count ++) {
            if (UCharacter.isLegal(illegal[count])) {
                errln("FAIL \\u" + hex(illegal[count]) +
                        " is not a legal character");
            }
        }

        for (int count = 0; count < legal.length; count ++) {
            if (!UCharacter.isLegal(legal[count])) {
                errln("FAIL \\u" + hex(legal[count]) +
                                                   " is a legal character");
            }
        }

        String illegalStr = "This is an illegal string ";
        String legalStr = "This is a legal string ";

        for (int count = 0; count < illegal.length; count ++) {
            StringBuffer str = new StringBuffer(illegalStr);
            if (illegal[count] < 0x10000) {
                str.append((char)illegal[count]);
            }
            else {
                char lead = UTF16.getLeadSurrogate(illegal[count]);
                char trail = UTF16.getTrailSurrogate(illegal[count]);
                str.append(lead);
                str.append(trail);
            }
            if (UCharacter.isLegal(str.toString())) {
                errln("FAIL " + hex(str.toString()) +
                      " is not a legal string");
            }
        }

        for (int count = 0; count < legal.length; count ++) {
            StringBuffer str = new StringBuffer(legalStr);
            if (legal[count] < 0x10000) {
                str.append((char)legal[count]);
            }
            else {
                char lead = UTF16.getLeadSurrogate(legal[count]);
                char trail = UTF16.getTrailSurrogate(legal[count]);
                str.append(lead);
                str.append(trail);
            }
            if (!UCharacter.isLegal(str.toString())) {
                errln("FAIL " + hex(str.toString()) + " is a legal string");
            }
        }
    }

    /**
     * Test getCodePoint
     */
    public void TestCodePoint()
    {
        int ch = 0x10000;
        for (char i = 0xD800; i < 0xDC00; i ++) {
            for (char j = 0xDC00; j <= 0xDFFF; j ++) {
                if (UCharacter.getCodePoint(i, j) != ch) {
                    errln("Error getting codepoint for surrogate " +
                          "characters \\u"
                          + Integer.toHexString(i) + " \\u" +
                          Integer.toHexString(j));
                }
                ch ++;
            }
        }
        try
        {
            UCharacter.getCodePoint((char)0xD7ff, (char)0xDC00);
            errln("Invalid surrogate characters should not form a " +
                  "supplementary");
        } catch(Exception e) {
        }
        for (char i = 0; i < 0xFFFF; i++) {
            if (i == 0xFFFE ||
                (i >= 0xD800 && i <= 0xDFFF) ||
                (i >= 0xFDD0 && i <= 0xFDEF)) {
                // not a character
                try {
                    UCharacter.getCodePoint(i);
                    errln("Not a character is not a valid codepoint");
                } catch (Exception e) {
                }
            }
            else {
                if (UCharacter.getCodePoint(i) != i) {
                    errln("A valid codepoint should return itself");
                }
            }
        }
    }

    /**
    * This method is alittle different from the type test in icu4c.
    * But combined with testUnicodeData, they basically do the same thing.
    */
    public void TestIteration()
    {
        int limit     = 0;
        int prevtype  = -1;
        int test[][]={{0x41, UCharacterCategory.UPPERCASE_LETTER},
                        {0x308, UCharacterCategory.NON_SPACING_MARK},
                        {0xfffe, UCharacterCategory.GENERAL_OTHER_TYPES},
                        {0xe0041, UCharacterCategory.FORMAT},
                        {0xeffff, UCharacterCategory.UNASSIGNED}};

        // default Bidi classes for unassigned code points
        int defaultBidi[][]={{ 0x0590, UCharacterDirection.LEFT_TO_RIGHT },
            { 0x0600, UCharacterDirection.RIGHT_TO_LEFT },
            { 0x07C0, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },
            { 0x0900, UCharacterDirection.RIGHT_TO_LEFT },
            { 0xFB1D, UCharacterDirection.LEFT_TO_RIGHT },
            { 0xFB50, UCharacterDirection.RIGHT_TO_LEFT },
            { 0xFE00, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },
            { 0xFE70, UCharacterDirection.LEFT_TO_RIGHT },
            { 0xFF00, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },
            { 0x10800, UCharacterDirection.LEFT_TO_RIGHT },
        	{ 0x11000, UCharacterDirection.RIGHT_TO_LEFT },
            { 0x110000, UCharacterDirection.LEFT_TO_RIGHT }};

        RangeValueIterator iterator = UCharacter.getTypeIterator();
        RangeValueIterator.Element result = new RangeValueIterator.Element();
        while (iterator.next(result)) {
            if (result.start != limit) {
                errln("UCharacterIteration failed: Ranges not continuous " +
                        "0x" + Integer.toHexString(result.start));
            }

            limit = result.limit;
            if (result.value == prevtype) {
                errln("Type of the next set of enumeration should be different");
            }
            prevtype = result.value;

            for (int i = result.start; i < limit; i ++) {
                int temptype = UCharacter.getType(i);
                if (temptype != result.value) {
                    errln("UCharacterIteration failed: Codepoint \\u" +
                            Integer.toHexString(i) + " should be of type " +
                            temptype + " not " + result.value);
                }
            }

            for (int i = 0; i < test.length; ++ i) {
                if (result.start <= test[i][0] && test[i][0] < result.limit) {
                    if (result.value != test[i][1]) {
                        errln("error: getTypes() has range ["
                              + Integer.toHexString(result.start) + ", "
                              + Integer.toHexString(result.limit)
                              + "] with type " + result.value
                              + " instead of ["
                              + Integer.toHexString(test[i][0]) + ", "
                              + Integer.toHexString(test[i][1]));
                    }
                }
            }

            // LineBreak.txt specifies:
            //   #  - Assigned characters that are not listed explicitly are given the value
            //   #    "AL".
            //   #  - Unassigned characters are given the value "XX".
            //
            // PUA characters are listed explicitly with "XX".
            // Verify that no assigned character has "XX".
            if (result.value != UCharacterCategory.UNASSIGNED
                && result.value != UCharacterCategory.PRIVATE_USE) {
                int c = result.start;
                while (c < result.limit) {
                    if (0 == UCharacter.getIntPropertyValue(c,
                                                UProperty.LINE_BREAK)) {
                        logln("error UProperty.LINE_BREAK(assigned \\u"
                              + Utility.hex(c, 4) + ")=XX");
                    }
                    ++ c;
                }
            }

            /*
             * Verify default Bidi classes.
             * See table 3-7 "Bidirectional Character Types" in UAX #9.
             * http://www.unicode.org/reports/tr9/
             */
            if (result.value == UCharacterCategory.UNASSIGNED
                || result.value == UCharacterCategory.PRIVATE_USE) {
                int c = result.start;
                for (int i = 0; i < defaultBidi.length && c < result.limit;
                     ++ i) {
                    if (c < defaultBidi[i][0]) {
                        while (c < result.limit && c < defaultBidi[i][0]) {
                            if (UCharacter.getDirection(c) != defaultBidi[i][1]
                                || UCharacter.getIntPropertyValue(c,
                                                          UProperty.BIDI_CLASS)
                                   != defaultBidi[i][1]) {
                                errln("error: getDirection(unassigned/PUA "
                                      + Integer.toHexString(c)
                                      + ") should be "
                                      + defaultBidi[i][1]);
                            }
                            ++ c;
                        }
                    }
                }
            }
        }

        iterator.reset();
        if (iterator.next(result) == false || result.start != 0) {
            System.out.println("result " + result.start);
            errln("UCharacterIteration reset() failed");
        }
    }

    /**
     * Testing getAge
     */
    public void TestGetAge()
    {
        int ages[] = {0x41,    1, 1, 0, 0,
                      0xffff,  1, 1, 0, 0,
                      0x20ab,  2, 0, 0, 0,
                      0x2fffe, 2, 0, 0, 0,
                      0x20ac,  2, 1, 0, 0,
                      0xfb1d,  3, 0, 0, 0,
                      0x3f4,   3, 1, 0, 0,
                      0x10300, 3, 1, 0, 0,
                      0x220,   3, 2, 0, 0,
                      0xff60,  3, 2, 0, 0};
        for (int i = 0; i < ages.length; i += 5) {
            VersionInfo age = UCharacter.getAge(ages[i]);
            if (age != VersionInfo.getInstance(ages[i + 1], ages[i + 2],
                                               ages[i + 3], ages[i + 4])) {
                errln("error: getAge(\\u" + Integer.toHexString(ages[i]) +
                      ") == " + age.toString() + " instead of " +
                      ages[i + 1] + "." + ages[i + 2] + "." + ages[i + 3] +
                      "." + ages[i + 4]);
            }
        }
    }

    /**
     * Test binary non core properties
     */
    public void TestAdditionalProperties()
    {
        // test data for hasBinaryProperty()
        int props[][] = { // code point, property
	        { 0x0627, UProperty.ALPHABETIC, 1 },
	        { 0x1034a, UProperty.ALPHABETIC, 1 },
	        { 0x2028, UProperty.ALPHABETIC, 0 },
	
	        { 0x0066, UProperty.ASCII_HEX_DIGIT, 1 },
	        { 0x0067, UProperty.ASCII_HEX_DIGIT, 0 },
	
	        { 0x202c, UProperty.BIDI_CONTROL, 1 },
	        { 0x202f, UProperty.BIDI_CONTROL, 0 },
	
	        { 0x003c, UProperty.BIDI_MIRRORED, 1 },
	        { 0x003d, UProperty.BIDI_MIRRORED, 0 },
	
	        { 0x058a, UProperty.DASH, 1 },
	        { 0x007e, UProperty.DASH, 0 },
	
	        { 0x0c4d, UProperty.DIACRITIC, 1 },
	        { 0x3000, UProperty.DIACRITIC, 0 },
	
	        { 0x0e46, UProperty.EXTENDER, 1 },
	        { 0x0020, UProperty.EXTENDER, 0 },
	
	        { 0xfb1d, UProperty.FULL_COMPOSITION_EXCLUSION, 1 },
	        { 0x1d15f, UProperty.FULL_COMPOSITION_EXCLUSION, 1 },
	        { 0xfb1e, UProperty.FULL_COMPOSITION_EXCLUSION, 0 },
	
	        { 0x0044, UProperty.HEX_DIGIT, 1 },
	        { 0xff46, UProperty.HEX_DIGIT, 1 },
	        { 0x0047, UProperty.HEX_DIGIT, 0 },
	
	        { 0x30fb, UProperty.HYPHEN, 1 },
	        { 0xfe58, UProperty.HYPHEN, 0 },
	
	        { 0x2172, UProperty.ID_CONTINUE, 1 },
	        { 0x0307, UProperty.ID_CONTINUE, 1 },
	        { 0x005c, UProperty.ID_CONTINUE, 0 },
	
	        { 0x2172, UProperty.ID_START, 1 },
	        { 0x007a, UProperty.ID_START, 1 },
	        { 0x0039, UProperty.ID_START, 0 },
	
	        { 0x4db5, UProperty.IDEOGRAPHIC, 1 },
	        { 0x2f999, UProperty.IDEOGRAPHIC, 1 },
	        { 0x2f99, UProperty.IDEOGRAPHIC, 0 },
	
	        { 0x200c, UProperty.JOIN_CONTROL, 1 },
	        { 0x2029, UProperty.JOIN_CONTROL, 0 },
	
	        { 0x1d7bc, UProperty.LOWERCASE, 1 },
	        { 0x0345, UProperty.LOWERCASE, 1 },
	        { 0x0030, UProperty.LOWERCASE, 0 },
	
	        { 0x1d7a9, UProperty.MATH, 1 },
	        { 0x2135, UProperty.MATH, 1 },
	        { 0x0062, UProperty.MATH, 0 },
	
	        { 0xfde1, UProperty.NONCHARACTER_CODE_POINT, 1 },
	        { 0x10ffff, UProperty.NONCHARACTER_CODE_POINT, 1 },
	        { 0x10fffd, UProperty.NONCHARACTER_CODE_POINT, 0 },
	
	        { 0x0022, UProperty.QUOTATION_MARK, 1 },
	        { 0xff62, UProperty.QUOTATION_MARK, 1 },
	        { 0xd840, UProperty.QUOTATION_MARK, 0 },
	
	        { 0x061f, UProperty.TERMINAL_PUNCTUATION, 1 },
	        { 0xe003f, UProperty.TERMINAL_PUNCTUATION, 0 },
	
	        { 0x1d44a, UProperty.UPPERCASE, 1 },
	        { 0x2162, UProperty.UPPERCASE, 1 },
	        { 0x0345, UProperty.UPPERCASE, 0 },
	
	        { 0x0020, UProperty.WHITE_SPACE, 1 },
	        { 0x202f, UProperty.WHITE_SPACE, 1 },
	        { 0x3001, UProperty.WHITE_SPACE, 0 },
	
	        { 0x0711, UProperty.XID_CONTINUE, 1 },
	        { 0x1d1aa, UProperty.XID_CONTINUE, 1 },
	        { 0x007c, UProperty.XID_CONTINUE, 0 },
	
	        { 0x16ee, UProperty.XID_START, 1 },
	        { 0x23456, UProperty.XID_START, 1 },
	        { 0x1d1aa, UProperty.XID_START, 0 },
	
	        /*
	         * Version break:
	         * The following properties are only supported starting with the
	         * Unicode version indicated in the second field.
	         */
	        { -1, 0x32, 0 },
	
	        { 0x180c, UProperty.DEFAULT_IGNORABLE_CODE_POINT, 1 },
	        { 0xfe02, UProperty.DEFAULT_IGNORABLE_CODE_POINT, 1 },
	        { 0x1801, UProperty.DEFAULT_IGNORABLE_CODE_POINT, 0 },
	
	        { 0x0341, UProperty.DEPRECATED, 1 },
	        { 0xe0041, UProperty.DEPRECATED, 0 },
	
	        { 0x00a0, UProperty.GRAPHEME_BASE, 1 },
	        { 0x0a4d, UProperty.GRAPHEME_BASE, 0 },
	        { 0xff9f, UProperty.GRAPHEME_BASE, 1 },      /* changed from Unicode 3.2 to 4 */
	
	        { 0x0300, UProperty.GRAPHEME_EXTEND, 1 },
	        { 0xff9f, UProperty.GRAPHEME_EXTEND, 0 },   /* changed from Unicode 3.2 to 4 */
	        { 0x0603, UProperty.GRAPHEME_EXTEND, 0 },
	
	        { 0x0a4d, UProperty.GRAPHEME_LINK, 1 },
	        { 0xff9f, UProperty.GRAPHEME_LINK, 0 },
	
	        { 0x2ff7, UProperty.IDS_BINARY_OPERATOR, 1 },
	        { 0x2ff3, UProperty.IDS_BINARY_OPERATOR, 0 },
	
	        { 0x2ff3, UProperty.IDS_TRINARY_OPERATOR, 1 },
	        { 0x2f03, UProperty.IDS_TRINARY_OPERATOR, 0 },
	
	        { 0x0ec1, UProperty.LOGICAL_ORDER_EXCEPTION, 1 },
	        { 0xdcba, UProperty.LOGICAL_ORDER_EXCEPTION, 0 },
	
	        { 0x2e9b, UProperty.RADICAL, 1 },
	        { 0x4e00, UProperty.RADICAL, 0 },
	
	        { 0x012f, UProperty.SOFT_DOTTED, 1 },
	        { 0x0049, UProperty.SOFT_DOTTED, 0 },
	
	        { 0xfa11, UProperty.UNIFIED_IDEOGRAPH, 1 },
	        { 0xfa12, UProperty.UNIFIED_IDEOGRAPH, 0 },
	
	        /* enum/integer type properties */
	        /* test default Bidi classes for unassigned code points */
            { 0x0590, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT },
		    { 0x05a2, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT },
		    { 0x05ed, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT },
		    { 0x07f2, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT },
		    { 0x08ba, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT },
            { 0xfb37, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT },
            { 0xfb42, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT },
            { 0x10806, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT },
            { 0x10909, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT },
            { 0x10fe4, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT },
   
            { 0x0606, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },
            { 0x061c, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },
            { 0x063f, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },
            { 0x070e, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },
            { 0x0775, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },
            { 0xfbc2, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },
            { 0xfd90, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },
            { 0xfefe, UProperty.BIDI_CLASS, UCharacterDirection.RIGHT_TO_LEFT_ARABIC },
	
	        { 0x02AF, UProperty.BLOCK, UCharacter.UnicodeBlock.IPA_EXTENSIONS.getID() },
	        { 0x0C4E, UProperty.BLOCK, UCharacter.UnicodeBlock.TELUGU.getID()},
	        { 0x155A, UProperty.BLOCK, UCharacter.UnicodeBlock.UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS.getID() },
	        { 0x1717, UProperty.BLOCK, UCharacter.UnicodeBlock.TAGALOG.getID() },
	        { 0x1AFF, UProperty.BLOCK, UCharacter.UnicodeBlock.NO_BLOCK.getID()},
	        { 0x3040, UProperty.BLOCK, UCharacter.UnicodeBlock.HIRAGANA.getID()},
	        { 0x1D0FF, UProperty.BLOCK, UCharacter.UnicodeBlock.BYZANTINE_MUSICAL_SYMBOLS.getID()},
	        { 0x10D0FF, UProperty.BLOCK, UCharacter.UnicodeBlock.SUPPLEMENTARY_PRIVATE_USE_AREA_B.getID() },
	        { 0xEFFFF, UProperty.BLOCK, UCharacter.UnicodeBlock.NO_BLOCK.getID() },
	
	        /* UProperty.CANONICAL_COMBINING_CLASS tested for assigned characters in TestUnicodeData() */
	        { 0xd7d7, UProperty.CANONICAL_COMBINING_CLASS, 0 },
	
	        { 0x00A0, UProperty.DECOMPOSITION_TYPE, UCharacter.DecompositionType.NOBREAK },
	        { 0x00A8, UProperty.DECOMPOSITION_TYPE, UCharacter.DecompositionType.COMPAT },
	        { 0x00bf, UProperty.DECOMPOSITION_TYPE, UCharacter.DecompositionType.NONE },
	        { 0x00c0, UProperty.DECOMPOSITION_TYPE, UCharacter.DecompositionType.CANONICAL },
	        { 0x1E9B, UProperty.DECOMPOSITION_TYPE, UCharacter.DecompositionType.CANONICAL },
	        { 0xBCDE, UProperty.DECOMPOSITION_TYPE, UCharacter.DecompositionType.CANONICAL },
	        { 0xFB5D, UProperty.DECOMPOSITION_TYPE, UCharacter.DecompositionType.MEDIAL },
	        { 0x1D736, UProperty.DECOMPOSITION_TYPE, UCharacter.DecompositionType.FONT },
	        { 0xe0033, UProperty.DECOMPOSITION_TYPE, UCharacter.DecompositionType.NONE },
	
	        { 0x0009, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.NEUTRAL },
	        { 0x0020, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.NARROW },
	        { 0x00B1, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.AMBIGUOUS },
	        { 0x20A9, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.HALFWIDTH },
	        { 0x2FFB, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.WIDE },
	        { 0x3000, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.FULLWIDTH },
	        { 0x35bb, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.WIDE },
	        { 0x58bd, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.WIDE },
	        { 0xD7A3, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.WIDE },
	        { 0xEEEE, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.AMBIGUOUS },
	        { 0x1D198, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.NEUTRAL },
	        { 0x20000, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.WIDE },
	        { 0x2F8C7, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.WIDE },
	        { 0x3a5bd, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.NEUTRAL },
	        { 0xFEEEE, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.AMBIGUOUS },
	        { 0x10EEEE, UProperty.EAST_ASIAN_WIDTH, UCharacter.EastAsianWidth.AMBIGUOUS },
	
	        /* UProperty.GENERAL_CATEGORY tested for assigned characters in TestUnicodeData() */
	        { 0xd7d7, UProperty.GENERAL_CATEGORY, 0 },
	
	        { 0x0444, UProperty.JOINING_GROUP, UCharacter.JoiningGroup.NO_JOINING_GROUP },
	        { 0x0639, UProperty.JOINING_GROUP, UCharacter.JoiningGroup.AIN },
	        { 0x072A, UProperty.JOINING_GROUP, UCharacter.JoiningGroup.DALATH_RISH },
	        { 0x0647, UProperty.JOINING_GROUP, UCharacter.JoiningGroup.HEH },
	        { 0x06C1, UProperty.JOINING_GROUP, UCharacter.JoiningGroup.HEH_GOAL },
	        { 0x06C3, UProperty.JOINING_GROUP, UCharacter.JoiningGroup.HAMZA_ON_HEH_GOAL },
	
	        { 0x200C, UProperty.JOINING_TYPE, UCharacter.JoiningType.NON_JOINING },
	        { 0x200D, UProperty.JOINING_TYPE, UCharacter.JoiningType.JOIN_CAUSING },
	        { 0x0639, UProperty.JOINING_TYPE, UCharacter.JoiningType.DUAL_JOINING },
	        { 0x0640, UProperty.JOINING_TYPE, UCharacter.JoiningType.JOIN_CAUSING },
	        { 0x06C3, UProperty.JOINING_TYPE, UCharacter.JoiningType.RIGHT_JOINING },
	        { 0x0300, UProperty.JOINING_TYPE, UCharacter.JoiningType.TRANSPARENT },
	        { 0x070F, UProperty.JOINING_TYPE, UCharacter.JoiningType.TRANSPARENT },
	        { 0xe0033, UProperty.JOINING_TYPE, UCharacter.JoiningType.TRANSPARENT },
	
	        /* TestUnicodeData() verifies that no assigned character has "XX" (unknown) */
	        { 0xe7e7, UProperty.LINE_BREAK, UCharacter.LineBreak.UNKNOWN },
	        { 0x10fffd, UProperty.LINE_BREAK, UCharacter.LineBreak.UNKNOWN },
	        { 0x0028, UProperty.LINE_BREAK, UCharacter.LineBreak.OPEN_PUNCTUATION },
	        { 0x232A, UProperty.LINE_BREAK, UCharacter.LineBreak.CLOSE_PUNCTUATION },
	        { 0x3401, UProperty.LINE_BREAK, UCharacter.LineBreak.IDEOGRAPHIC },
	        { 0x4e02, UProperty.LINE_BREAK, UCharacter.LineBreak.IDEOGRAPHIC },
	        { 0xac03, UProperty.LINE_BREAK, UCharacter.LineBreak.IDEOGRAPHIC },
	        { 0x20004, UProperty.LINE_BREAK, UCharacter.LineBreak.IDEOGRAPHIC },
	        { 0xf905, UProperty.LINE_BREAK, UCharacter.LineBreak.IDEOGRAPHIC },
	        { 0xdb7e, UProperty.LINE_BREAK, UCharacter.LineBreak.SURROGATE },
	        { 0xdbfd, UProperty.LINE_BREAK, UCharacter.LineBreak.SURROGATE },
	        { 0xdffc, UProperty.LINE_BREAK, UCharacter.LineBreak.SURROGATE },
	        { 0x2762, UProperty.LINE_BREAK, UCharacter.LineBreak.EXCLAMATION },
	        { 0x002F, UProperty.LINE_BREAK, UCharacter.LineBreak.BREAK_SYMBOLS },
	        { 0x1D49C, UProperty.LINE_BREAK, UCharacter.LineBreak.ALPHABETIC },
	        { 0x1731, UProperty.LINE_BREAK, UCharacter.LineBreak.ALPHABETIC },
	
	        /* UProperty.NUMERIC_TYPE tested in TestNumericProperties() */
	
	        /* UProperty.SCRIPT tested in TestUScriptCodeAPI() */
	
	        { 0x1100, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LEADING_JAMO },
	        { 0x1111, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LEADING_JAMO },
	        { 0x1159, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LEADING_JAMO },
	        { 0x115f, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LEADING_JAMO },
	
	        { 0x1160, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.VOWEL_JAMO },
	        { 0x1161, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.VOWEL_JAMO },
	        { 0x1172, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.VOWEL_JAMO },
	        { 0x11a2, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.VOWEL_JAMO },
	
	        { 0x11a8, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.TRAILING_JAMO },
	        { 0x11b8, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.TRAILING_JAMO },
	        { 0x11c8, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.TRAILING_JAMO },
	        { 0x11f9, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.TRAILING_JAMO },
	
	        { 0x115a, UProperty.HANGUL_SYLLABLE_TYPE, 0 },
	        { 0x115e, UProperty.HANGUL_SYLLABLE_TYPE, 0 },
	        { 0x11a3, UProperty.HANGUL_SYLLABLE_TYPE, 0 },
	        { 0x11a7, UProperty.HANGUL_SYLLABLE_TYPE, 0 },
	        { 0x11fa, UProperty.HANGUL_SYLLABLE_TYPE, 0 },
	        { 0x11ff, UProperty.HANGUL_SYLLABLE_TYPE, 0 },
	
	        { 0xac00, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LV_SYLLABLE },
	        { 0xac1c, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LV_SYLLABLE },
	        { 0xc5ec, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LV_SYLLABLE },
	        { 0xd788, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LV_SYLLABLE },
	
	        { 0xac01, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LVT_SYLLABLE },
	        { 0xac1b, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LVT_SYLLABLE },
	        { 0xac1d, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LVT_SYLLABLE },
	        { 0xc5ee, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LVT_SYLLABLE },
	        { 0xd7a3, UProperty.HANGUL_SYLLABLE_TYPE, UCharacter.HangulSyllableType.LVT_SYLLABLE },
	
	        { 0xd7a4, UProperty.HANGUL_SYLLABLE_TYPE, 0 },
	
	        /* undefined UProperty values */
	        { 0x61, 0x4a7, 0 },
	        { 0x234bc, 0x15ed, 0 }
	    };


        if (UCharacter.getIntPropertyMinValue(UProperty.DASH) != 0
            || UCharacter.getIntPropertyMinValue(UProperty.BIDI_CLASS) != 0
            || UCharacter.getIntPropertyMinValue(UProperty.BLOCK)!= 0  /* j2478 */
            || UCharacter.getIntPropertyMinValue(UProperty.SCRIPT)!= 0 /* JB#2410 */
            || UCharacter.getIntPropertyMinValue(0x2345) != 0) {
            errln("error: UCharacter.getIntPropertyMinValue() wrong");
        }

        if (UCharacter.getIntPropertyMaxValue(UProperty.DASH) != 1
            || UCharacter.getIntPropertyMaxValue(UProperty.ID_CONTINUE) != 1
            || UCharacter.getIntPropertyMaxValue(UProperty.BINARY_LIMIT-1) != 1
            || UCharacter.getIntPropertyMaxValue(UProperty.BIDI_CLASS)
                                != UCharacterDirection.CHAR_DIRECTION_COUNT - 1
            || UCharacter.getIntPropertyMaxValue(UProperty.BLOCK)
                                        != UCharacter.UnicodeBlock.COUNT - 1
            || UCharacter.getIntPropertyMaxValue(UProperty.LINE_BREAK)
                                        != UCharacter.LineBreak.COUNT - 1 
            || UCharacter.getIntPropertyMaxValue(UProperty.SCRIPT)
                                        != UScript.CODE_LIMIT - 1
            || UCharacter.getIntPropertyMaxValue(0x2345) != -1 //JB#2410 
        	|| UCharacter.getIntPropertyMaxValue(UProperty.DECOMPOSITION_TYPE) != (UCharacter.DecompositionType.COUNT - 1) 
        	|| UCharacter.getIntPropertyMaxValue(UProperty.JOINING_GROUP) !=  (UCharacter.JoiningGroup.COUNT -1) 
        	|| UCharacter.getIntPropertyMaxValue(UProperty.JOINING_TYPE) !=  (UCharacter.JoiningType.COUNT -1) 
        	|| UCharacter.getIntPropertyMaxValue(UProperty.EAST_ASIAN_WIDTH) !=  (UCharacter.EastAsianWidth.COUNT -1)
           	
           ) {
            errln("error: UCharacter.getIntPropertyMaxValue() wrong");
        }

        VersionInfo version = UCharacter.getUnicodeVersion();

        // test hasBinaryProperty()
        for (int i = 0; i < props.length; ++ i) {
            if (props[i][0] < 0) {
                if (version.compareTo(VersionInfo.getInstance(props[i][1] >> 4,
                                                          props[i][1] & 0xF,
                                                          0, 0)) < 0) {
                    break;
                }
                continue;
            }
            boolean expect = true;
            if (props[i][2] == 0) {
                expect = false;
            }
            if (props[i][1] < UProperty.INT_START) {
                if (UCharacter.hasBinaryProperty(props[i][0], props[i][1])
                    != expect) {
                    errln("error: UCharacter.hasBinaryProperty(\\u" +
                          Integer.toHexString(props[i][0]) + ", " +
                          Integer.toHexString(props[i][1])
                          + ") has an error expected " + props[i][2]);
                }
            }
            
			int retVal = UCharacter.getIntPropertyValue(props[i][0], props[i][1]);
            if (retVal != props[i][2]) {
                errln("error: UCharacter.getIntPropertyValue(\\u" +
                      Utility.hex(props[i][0], 4) +
                      ", " + props[i][1] + " is wrong, should be "
                      + props[i][2] + " not " + retVal);
            }

            // test separate functions, too
            switch (props[i][1]) {
            case UProperty.ALPHABETIC:
                if (UCharacter.isUAlphabetic(props[i][0]) != expect) {
                    errln("error: UCharacter.isUAlphabetic(\\u" +
                          Integer.toHexString(props[i][0]) +
                          ") is wrong expected " + props[i][2]);
                }
                break;
            case UProperty.LOWERCASE:
                if (UCharacter.isULowercase(props[i][0]) != expect) {
                    errln("error: UCharacter.isULowercase(\\u" +
                          Integer.toHexString(props[i][0]) +
                          ") is wrong expected " +props[i][2]);
                }
                break;
            case UProperty.UPPERCASE:
                if (UCharacter.isUUppercase(props[i][0]) != expect) {
                    errln("error: UCharacter.isUUppercase(\\u" +
                          Integer.toHexString(props[i][0]) +
                          ") is wrong expected " + props[i][2]);
                }
                break;
            case UProperty.WHITE_SPACE:
                if (UCharacter.isUWhiteSpace(props[i][0]) != expect) {
                    errln("error: UCharacter.isUWhiteSpace(\\u" +
                          Integer.toHexString(props[i][0]) +
                          ") is wrong expected " + props[i][2]);
                }
                break;
            default:
                break;
            }
        }
    }

    public void TestNumericProperties()
    {
        // see UnicodeData.txt, DerivedNumericValues.txt
        int testvar[][] = {
            { 0x0F33, UCharacter.NumericType.NUMERIC },
            { 0x0C66, UCharacter.NumericType.DECIMAL },
            { 0x2159, UCharacter.NumericType.NUMERIC },
            { 0x00BD, UCharacter.NumericType.NUMERIC },
            { 0x0031, UCharacter.NumericType.DECIMAL },
            { 0x10320, UCharacter.NumericType.NUMERIC },
            { 0x0F2B, UCharacter.NumericType.NUMERIC },
            { 0x00B2, UCharacter.NumericType.DIGIT }, /* Unicode 4.0 change */
            { 0x1813, UCharacter.NumericType.DECIMAL },
            { 0x2173, UCharacter.NumericType.NUMERIC },
            { 0x278E, UCharacter.NumericType.DIGIT },
            { 0x1D7F2, UCharacter.NumericType.DECIMAL },
            { 0x247A, UCharacter.NumericType.DIGIT },
            { 0x1372, UCharacter.NumericType.NUMERIC },
            { 0x216B, UCharacter.NumericType.NUMERIC },
            { 0x16EE, UCharacter.NumericType.NUMERIC },
            { 0x249A, UCharacter.NumericType.NUMERIC },
            { 0x303A, UCharacter.NumericType.NUMERIC },
            { 0x32B2, UCharacter.NumericType.NUMERIC },
            { 0x1375, UCharacter.NumericType.NUMERIC },
            { 0x10323, UCharacter.NumericType.NUMERIC },
            { 0x0BF1, UCharacter.NumericType.NUMERIC },
            { 0x217E, UCharacter.NumericType.NUMERIC },
            { 0x2180, UCharacter.NumericType.NUMERIC },
            { 0x2181, UCharacter.NumericType.NUMERIC },
            { 0x137C, UCharacter.NumericType.NUMERIC },
            { 0x61, UCharacter.NumericType.NONE },
            { 0x3000, UCharacter.NumericType.NONE },
            { 0xfffe, UCharacter.NumericType.NONE },
            { 0x10301, UCharacter.NumericType.NONE },
            { 0xe0033, UCharacter.NumericType.NONE },
            { 0x10ffff, UCharacter.NumericType.NONE },
            /* Unicode 4.0 Changes */
            { 0x96f6,  UCharacter.NumericType.NUMERIC },
            { 0x4e00,  UCharacter.NumericType.NUMERIC },
            { 0x58f1,  UCharacter.NumericType.NUMERIC },
            { 0x5f10,  UCharacter.NumericType.NUMERIC },
            { 0x5f0e,  UCharacter.NumericType.NUMERIC },
            { 0x8086,  UCharacter.NumericType.NUMERIC },
            { 0x7396,  UCharacter.NumericType.NUMERIC },
            { 0x5345,  UCharacter.NumericType.NUMERIC },
            { 0x964c,  UCharacter.NumericType.NUMERIC },
            { 0x4edf,  UCharacter.NumericType.NUMERIC },
            { 0x4e07,  UCharacter.NumericType.NUMERIC },
            { 0x4ebf,  UCharacter.NumericType.NUMERIC },
            { 0x5146,  UCharacter.NumericType.NUMERIC }
        };

        double expected[] = {-1/(double)2,
                             0,
                             1/(double)6,
                             1/(double)2,
                             1,
                             1,
                             3/(double)2,
                             2,
                             3,
                             4,
                             5,
                             6,
                             7,
                             10,
                             12,
                             17,
                             19,
                             30,
                             37,
                             40,
                             50,
                             100,
                             500,
                             1000,
                             5000,
                             10000,
                             UCharacter.NO_NUMERIC_VALUE,
                             UCharacter.NO_NUMERIC_VALUE,
                             UCharacter.NO_NUMERIC_VALUE,
                             UCharacter.NO_NUMERIC_VALUE,
                             UCharacter.NO_NUMERIC_VALUE,
                             UCharacter.NO_NUMERIC_VALUE,
                             0 ,
							 1 , 
							 1 , 
							 2 ,
							 3 ,
							 4 ,
							 9 ,
							 30 ,
							 100 ,
							 1000 ,
							 10000 , 
							 100000000 , 
							 1000000000000.00
        };


        for (int i = 0; i < testvar.length; ++ i) {
            int c = testvar[i][0];
            int type = UCharacter.getIntPropertyValue(c,
                                                      UProperty.NUMERIC_TYPE);
            double nv = UCharacter.getUnicodeNumericValue(c);

            if (type != testvar[i][1]) {
                errln("UProperty.NUMERIC_TYPE(\\u" + Utility.hex(c, 4)
                       + ") = " + type + " should be " + testvar[i][1]);
            }
            if (0.000001 <= Math.abs(nv - expected[i])) {
                errln("UCharacter.getNumericValue(\\u" + Utility.hex(c, 4)
                        + ") = " + nv + " should be " + expected[i]);
            }
        }
    }

    /**
     * Test the property values API.  See JB#2410.
     */
    public void TestPropertyValues() {
        int i, p, min, max;

        /* Min should be 0 for everything. */
        /* Until JB#2478 is fixed, the one exception is UCHAR_BLOCK. */
        for (p=UProperty.INT_START; p<UProperty.INT_LIMIT; ++p) {
            min = UCharacter.getIntPropertyMinValue(p);
            if (min != 0) {
                if (p == UProperty.BLOCK) {
                    /* This is okay...for now.  See JB#2487.
                       TODO Update this for JB#2487. */
                } else {
                    String name;
                    name = UCharacter.getPropertyName(p, UProperty.NameChoice.LONG);
                    errln("FAIL: UCharacter.getIntPropertyMinValue(" + name + ") = " +
                          min + ", exp. 0");
                }
            }
        }

        if (UCharacter.getIntPropertyMinValue(UProperty.GENERAL_CATEGORY_MASK)
            != 0
            || UCharacter.getIntPropertyMaxValue(
                                               UProperty.GENERAL_CATEGORY_MASK)
               != -1) {
            errln("error: UCharacter.getIntPropertyMin/MaxValue("
                  + "UProperty.GENERAL_CATEGORY_MASK) is wrong");
        }

        /* Max should be -1 for invalid properties. */
        max = UCharacter.getIntPropertyMaxValue(-1);
        if (max != -1) {
            errln("FAIL: UCharacter.getIntPropertyMaxValue(-1) = " +
                  max + ", exp. -1");
        }

        /* Script should return 0 for an invalid code point. If the API
           throws an exception then that's fine too. */
        for (i=0; i<2; ++i) {
            try {
                int script = 0;
                String desc = null;
                switch (i) {
                case 0:
                    script = UScript.getScript(-1);
                    desc = "UScript.getScript(-1)";
                    break;
                case 1:
                    script = UCharacter.getIntPropertyValue(-1, UProperty.SCRIPT);
                    desc = "UCharacter.getIntPropertyValue(-1, UProperty.SCRIPT)";
                    break;
                }
                if (script != 0) {
                    errln("FAIL: " + desc + " = " + script + ", exp. 0");
                }
            } catch (IllegalArgumentException e) {}
        }
    }
	/* add characters from a serialized set to a normal one */ 
	private static void _setAddSerialized(UnicodeSet set, USerializedSet sset) { 
	 //  int start, end; 
	   int i, count; 
	
	   count=sset.countSerializedRanges(); 
	   int[] range = new int[2];
	   for(i=0; i<count; ++i) { 
	       sset.getSerializedRange(i,range); 
	       set.add(range[0],range[1]); 
	   } 
	} 

	private boolean showADiffB(UnicodeSet a, UnicodeSet b,
							            String a_name, String b_name,
							            boolean expect,
							            boolean diffIsError){
		int i, start, end, length;
		boolean equal;
		equal=true;
		i=0;
		for(;;) {
			start  = a.getRangeStart(i);
		    length = (i < a.getRangeCount()) ? 0 : a.getRangeCount();
		    end    = a.getRangeEnd(i);

		    if(length!=0) {
		        return equal; /* done with code points, got a string or -1 */
		    }
		
		    if(expect!=b.contains(start, end)) {
		        equal=false;
		        while(start<=end) {
		            if(expect!=b.contains(start)) {
		                if(diffIsError) {
		                    if(expect) {
		                        errln("error: "+ a_name +" contains "+ hex(start)+" but "+ b_name +" does not");
		                    } else {
		                        errln("error: "+a_name +" and "+ b_name+" both contain "+hex(start) +" but should not intersect");
		                    }
		                } else {
		                    if(expect) {
		                        logln("info: "+a_name +" contains "+hex(start)+ "but " + b_name +" does not");
		                    } else {
		                        logln("info: "+a_name +" and "+b_name+" both contain "+hex(start)+" but should not intersect");
		                    }
		                }
		            }
		            ++start;
		        }
		    }
		
		    ++i;
		}
	}
	private boolean showAMinusB(UnicodeSet a, UnicodeSet b,
							            String a_name, String b_name,
							            boolean diffIsError) {

	    return showADiffB(a, b, a_name, b_name, true, diffIsError);
	}
	
	private boolean showAIntersectB(UnicodeSet a, UnicodeSet b,
							                String a_name, String b_name,
							                boolean diffIsError) {
	    return showADiffB(a, b, a_name, b_name, false, diffIsError);
	}
	
	private boolean compareUSets(UnicodeSet a, UnicodeSet b,
							             String a_name, String b_name,
							             boolean diffIsError) {
	    return
	        showAMinusB(a, b, a_name, b_name, diffIsError) &&
	        showAMinusB(b, a, b_name, a_name, diffIsError);
	}

   /* various tests for consistency of UCD data and API behavior */ 
   public void TestConsistency() { 
       char[] buffer16 = new char[300]; 
       char[] buffer   = new char[300]; 
       UnicodeSet set1, set2, set3, set4; 
    
       USerializedSet sset; 
       int start, end; 
       int i, length; 
    
       String hyphenPattern = "[:Hyphen:]"; 
       String dashPattern = "[:Dash:]"; 
       String lowerPattern = "[:Lowercase:]"; 
       String formatPattern = "[:Cf:]"; 
       String alphaPattern  =  "[:Alphabetic:]"; 
     
       /* 
        * It used to be that UCD.html and its precursors said 
        * "Those dashes used to mark connections between pieces of words, 
        *  plus the Katakana middle dot." 
        * 
        * Unicode 4 changed 00AD Soft Hyphen to Cf and removed it from Dash 
        * but not from Hyphen. 
        * UTC 94 (2003mar) decided to leave it that way and to changed UCD.html. 
        * Therefore, do not show errors when testing the Hyphen property. 
        */ 
       logln("Starting with Unicode 4, inconsistencies with [:Hyphen:] are\n" 
                   + "known to the UTC and not considered errors.\n"); 
    
       set1=new UnicodeSet(hyphenPattern); 
       set2=new UnicodeSet(dashPattern); 
 
           /* remove the Katakana middle dot(s) from set1 */ 
           set1.remove(0x30fb); 
           set2.remove (0xff65); /* halfwidth variant */ 
           showAMinusB(set1, set2, "[:Hyphen:]", "[:Dash:]", false); 

    
       /* check that Cf is neither Hyphen nor Dash nor Alphabetic */ 
       set3=new UnicodeSet(formatPattern); 
       set4=new UnicodeSet(alphaPattern); 
 
       showAIntersectB(set3, set1, "[:Cf:]", "[:Hyphen:]", false); 
       showAIntersectB(set3, set2, "[:Cf:]", "[:Dash:]", true); 
       showAIntersectB(set3, set4, "[:Cf:]", "[:Alphabetic:]", true); 
       /* 
        * Check that each lowercase character has "small" in its name 
        * and not "capital". 
        * There are some such characters, some of which seem odd. 
        * Use the verbose flag to see these notices. 
        */ 
       set1=new UnicodeSet(lowerPattern); 

       for(i=0;; ++i) { 
//       		try{
//           		length=set1.getItem(set1, i, &start, &end, NULL, 0, &errorCode); 
//       		}catch(Exception e){
//       			break;
//       		} 
 		   start = set1.getRangeStart(i);
 		   end = set1.getRangeEnd(i);
 		   length = i<set1.getRangeCount() ? set1.getRangeCount() : 0;
           if(length!=0) { 
               break; /* done with code points, got a string or -1 */ 
           } 

           while(start<=end) { 
               String name=UCharacter.getName(start); 

               if( (name.indexOf("SMALL")< 0 || name.indexOf("CAPITAL")<-1) && 
                   name.indexOf("SMALL CAPITAL")==-1 
               ) { 
                   logln("info: [:Lowercase:] contains U+"+hex(start) + " whose name does not suggest lowercase: " + name); 
               } 
               ++start; 
           } 
       } 

    
       /* 
        * Test for an example that unorm_getCanonStartSet() delivers 
        * all characters that compose from the input one, 
        * even in multiple steps. 
        * For example, the set for "I" (0049) should contain both 
        * I-diaeresis (00CF) and I-diaeresis-acute (1E2E). 
        * In general, the set for the middle such character should be a subset 
        * of the set for the first. 
        */ 
       set1=new UnicodeSet(); 
       set2=new UnicodeSet(); 
       sset = new USerializedSet();
       NormalizerImpl.getCanonStartSet(0x49,sset); 
       _setAddSerialized(set1, sset); 
    
       /* enumerate all characters that are plausible to be latin letters */ 
       for(start=0xa0; start<0x2000; ++start) { 
           if(NormalizerImpl.getDecomposition(start, false, buffer16,0,buffer16.length) > 1 && buffer[0]==0x0049) { 
               set2.add(start); 
           } 
       } 
    
       compareUSets(set1, set2, 
                    "[canon start set of 0049]", "[all c with canon decomp with 0049]", 
                    false); 

   } 

}
