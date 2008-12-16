/*
 ******************************************************************************
 * Copyright (C) 1996-2008, International Business Machines Corporation and   *
 * others. All Rights Reserved.                                               *
 ******************************************************************************
 */

/**
 * Test for CharsetSelector
 * 
 * This is a port of ucnvseltst.c from ICU4C
 * 
 * Tests related to serialization are not ported in this version. In addition,
 * the TestConversionUTF8 method is not going to be ported, as UTF8 is seldom used
 * in Java.
 * 
 * @author Shaopeng Jia
 */

package com.ibm.icu.dev.test.charset;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Vector;

import com.ibm.icu.charset.CharsetICU;
import com.ibm.icu.charset.CharsetProviderICU;
import com.ibm.icu.charset.CharsetSelector;
import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.text.UTF16;
import com.ibm.icu.text.UnicodeSet;

public class TestSelection extends TestFmwk {
    @SuppressWarnings("deprecation")
    private String[] availableCharsetNames = (String[]) CharsetProviderICU.getAvailableNames();
    
    private int findIndex(String charsetName) {
        Charset set1 = Charset.forName(charsetName);
        for (int i = 0; i < availableCharsetNames.length; i++) {
            Charset set2 = Charset.forName(availableCharsetNames[i]);
            if (set1.compareTo(set2) == 0) {
                return i;
            }
        }
        return -1;
    }
    
    @SuppressWarnings("unchecked")
    private void fillBool(List result, boolean[] toFill) {
        for (int i = 0; i < toFill.length; i++) {
            toFill[i] = false;
        }
        for (int i = 0; i < result.size(); i++) {
            String name = (String) result.get(i);
            toFill[findIndex(name)] = true;
        }
    }
    
    @SuppressWarnings("unchecked")
    private void verifyResultUTF16(String s, Vector encodings, List result, UnicodeSet excludedEncodings, int mappingTypes) {
        boolean[] resultsFromSystem = new boolean[availableCharsetNames.length];
        boolean[] resultsManually = new boolean[availableCharsetNames.length];
        for (int i = 0; i < availableCharsetNames.length; i++) {
            resultsFromSystem[i] = resultsManually[i] = false;
        }
        
        int numOfEncodings = encodings.size();
        for (int i = 0; i < numOfEncodings; i++) {
            resultsManually[findIndex((String) encodings.get(i))] = true;
            UnicodeSet unicodePointSet = new UnicodeSet();
            Charset testCharset = CharsetICU.forNameICU((String) encodings.get(i));
            ((CharsetICU) testCharset).getUnicodeSet(unicodePointSet, mappingTypes);
            resultsManually[findIndex((String) encodings.get(i))] = true; // why is it called again? should be removed
            int ch;
            int index = 0; 
            while (index < s.length()) {
                ch = UTF16.charAt(s, index);
                if (excludedEncodings.contains(ch) == false && unicodePointSet.contains(ch) == false) {
                    resultsManually[findIndex((String) encodings.get(i))] = false;
                    break;
                } 
                index += UTF16.getCharCount(ch);
            }         
        }
        
        // fill the bool for the selector results
        fillBool(result, resultsFromSystem);
        for (int i = 0; i < availableCharsetNames.length; i++) {
            if (resultsManually[i] != resultsFromSystem[i]) {
                errln("failure in charset selector! Charset " + availableCharsetNames[i] + 
                      " had conflicting results manual: " + resultsManually[i] + ", system: " + 
                      resultsFromSystem[i] + "\n");
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        new TestSelection().run(args);
    }
    
    @SuppressWarnings("unchecked")
    public void TestConversionUTF16() {
        /* 
         * test cases are separated by a -1
         * each line is one test case including encodings to check for
         * I'd like to generate this array randomly but not sure if this is an allowed practice in ICU
         */
        int encodingsTestCases[] = {  90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, -1,
                1, 3, 7, 9, 11, 13, 12, 15, 19, 20, 22, 24, -1,
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1,
                0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56, -1,
                1, 5, 9, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, -1,
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 
                30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59,
                60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89,
                90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 
                130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159,
                160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189,
                190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, -1, 1, -1};
        String[] texts = {""
        };
        
        UnicodeSet[] excludedSets = new UnicodeSet[3];
        excludedSets[0] = new UnicodeSet();
        for (int i = 1; i < 3; i++) {
            excludedSets[i] = new UnicodeSet(i * 30, i * 30 + 500);
        }
        
        for (int excludedSetId = 0; excludedSetId < 3; excludedSetId++) {
            for (int testCaseIdx = 0, prev = 0, curCase = 0; testCaseIdx < encodingsTestCases.length; testCaseIdx++) {
                if (encodingsTestCases[testCaseIdx] != -1) continue;
                curCase++;
                if (QUICK && curCase > 2) { // TODO: find out the equivalent of QUICK in ICU4J
                    break;
                }
                
                int numOfEncodings = testCaseIdx - prev;
                Vector encodings = new Vector();
                for (int i = prev; i < testCaseIdx; i++) {
                    encodings.add(availableCharsetNames[encodingsTestCases[i]]);
                }
                CharsetSelector sel = new CharsetSelector(encodings, excludedSets[excludedSetId], CharsetICU.ROUNDTRIP_SET);
                
                List result;
                for (int i = 0; i < texts.length; i++) {
                    result = sel.selectForString(texts[i]);
                    verifyResultUTF16(texts[i], encodings, result, excludedSets[excludedSetId], CharsetICU.ROUNDTRIP_SET);
                } 
                prev = testCaseIdx + 1;
            }
        }
    }
}
