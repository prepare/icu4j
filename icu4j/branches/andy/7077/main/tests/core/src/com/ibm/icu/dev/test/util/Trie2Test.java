/*
 *******************************************************************************
 * Copyright (C) 2009, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.impl.Trie2;
import com.ibm.icu.impl.Trie2_16;
import com.ibm.icu.impl.Trie2_32;
import com.ibm.icu.impl.Trie2Writable;

public class Trie2Test extends TestFmwk {
    /**
     * Constructor
     */
     public Trie2Test()
     {
     }
       
     // public methods -----------------------------------------------
     
     public static void main(String arg[]) 
     {
         Trie2Test test = new Trie2Test();
         try {
             test.run(arg);
         } catch (Exception e) {
             test.errln("Error testing trietest");
         }
     }
     
     public void iterateAPI() {
         
         // See what API usage looks like with a value mapping function.
         
         Trie2.ValueMapper m = new Trie2.ValueMapper() {
             public int map(int in) {return in & 0x1f;};             
         };
         
         Trie2 trie = new Trie2Writable(0, 0);
         for (Iterator<Trie2.EnumRange> iter = trie.iterator(m); iter.hasNext(); ) {
             Trie2.EnumRange r = iter.next();

         }
         
         // Plain iteration, no mapping
         for (Trie2.EnumRange r: trie) {
             
         }
         
     }
     
     
     String[] trieNames = {"setRanges1", "setRanges2", "setRanges3", "setRangesEmpty", "setRangesSingleValue"};
     /* set consecutive ranges, even with value 0 */
          
         
     private static int[][] setRanges1 ={
         { 0,        0x40,     0,      0 },
         { 0x40,     0xe7,     0x1234, 0 },
         { 0xe7,     0x3400,   0,      0 },
         { 0x3400,   0x9fa6,   0x6162, 0 },
         { 0x9fa6,   0xda9e,   0x3132, 0 },
         { 0xdada,   0xeeee,   0x87ff, 0 },
         { 0xeeee,   0x11111,  1,      0 },
         { 0x11111,  0x44444,  0x6162, 0 },
         { 0x44444,  0x60003,  0,      0 },
         { 0xf0003,  0xf0004,  0xf,    0 },
         { 0xf0004,  0xf0006,  0x10,   0 },
         { 0xf0006,  0xf0007,  0x11,   0 },
         { 0xf0007,  0xf0040,  0x12,   0 },
         { 0xf0040,  0x110000, 0,      0 }
     };

     private static int[][]  checkRanges1 = {
         { 0,        0 },
         { 0x40,     0 },
         { 0xe7,     0x1234 },
         { 0x3400,   0 },
         { 0x9fa6,   0x6162 },
         { 0xda9e,   0x3132 },
         { 0xdada,   0 },
         { 0xeeee,   0x87ff },
         { 0x11111,  1 },
         { 0x44444,  0x6162 },
         { 0xf0003,  0 },
         { 0xf0004,  0xf },
         { 0xf0006,  0x10 },
         { 0xf0007,  0x11 },
         { 0xf0040,  0x12 },
         { 0x110000, 0 }
     };

     /* set some interesting overlapping ranges */
     private static  int [][] setRanges2={
         { 0x21,     0x7f,     0x5555, 1 },
         { 0x2f800,  0x2fedc,  0x7a,   1 },
         { 0x72,     0xdd,     3,      1 },
         { 0xdd,     0xde,     4,      0 },
         { 0x201,    0x240,    6,      1 },  /* 3 consecutive blocks with the same pattern but */
         { 0x241,    0x280,    6,      1 },  /* discontiguous value ranges, testing utrie2_enum() */
         { 0x281,    0x2c0,    6,      1 },
         { 0x2f987,  0x2fa98,  5,      1 },
         { 0x2f777,  0x2f883,  0,      1 },
         { 0x2f900,  0x2ffaa,  1,      0 },
         { 0x2ffaa,  0x2ffab,  2,      1 },
         { 0x2ffbb,  0x2ffc0,  7,      1 }
     };

     private static int[] [] checkRanges2={
         { 0,        0 },
         { 0x21,     0 },
         { 0x72,     0x5555 },
         { 0xdd,     3 },
         { 0xde,     4 },
         { 0x201,    0 },
         { 0x240,    6 },
         { 0x241,    0 },
         { 0x280,    6 },
         { 0x281,    0 },
         { 0x2c0,    6 },
         { 0x2f883,  0 },
         { 0x2f987,  0x7a },
         { 0x2fa98,  5 },
         { 0x2fedc,  0x7a },
         { 0x2ffaa,  1 },
         { 0x2ffab,  2 },
         { 0x2ffbb,  0 },
         { 0x2ffc0,  7 },
         { 0x110000, 0 }
     };

     private static int[] [] checkRanges2_d800={
         { 0x10000,  0 },
         { 0x10400,  0 }
     };

     private static int[][] checkRanges2_d87e={
         { 0x2f800,  6 },
         { 0x2f883,  0 },
         { 0x2f987,  0x7a },
         { 0x2fa98,  5 },
         { 0x2fc00,  0x7a }
     };

     private static int[][] checkRanges2_d87f={
         { 0x2fc00,  0 },
         { 0x2fedc,  0x7a },
         { 0x2ffaa,  1 },
         { 0x2ffab,  2 },
         { 0x2ffbb,  0 },
         { 0x2ffc0,  7 },
         { 0x30000,  0 }
     };

     private static int[][]  checkRanges2_dbff={
         { 0x10fc00, 0 },
         { 0x110000, 0 }
     };

     /* use a non-zero initial value */
     private static int[][] setRanges3={
         { 0x31,     0xa4,     1, 0 },
         { 0x3400,   0x6789,   2, 0 },
         { 0x8000,   0x89ab,   9, 1 },
         { 0x9000,   0xa000,   4, 1 },
         { 0xabcd,   0xbcde,   3, 1 },
         { 0x55555,  0x110000, 6, 1 },  /* highStart<U+ffff with non-initialValue */
         { 0xcccc,   0x55555,  6, 1 }
     };

     private static int[][] checkRanges3={
         { 0,        9 },  /* non-zero initialValue */
         { 0x31,     9 },
         { 0xa4,     1 },
         { 0x3400,   9 },
         { 0x6789,   2 },
         { 0x9000,   9 },
         { 0xa000,   4 },
         { 0xabcd,   9 },
         { 0xbcde,   3 },
         { 0xcccc,   9 },
         { 0x110000, 6 }
     };

     /* empty or single-value tries, testing highStart==0 */
     private static int[][] setRangesEmpty={
         //{ 0,        0,        0, FALSE },  /* need some values for it to compile (in C) */
     };

     private static int[][] checkRangesEmpty={
         { 0,        3 },
         { 0x110000, 3 }
     };

     private static int[][] setRangesSingleValue={
         { 0,        0x110000, 5, 1 },
     };

     private static int[][] checkRangesSingleValue={
         { 0,        3 },
         { 0x110000, 5 }
     };


     private void trieGettersTest(String testName,
             Trie2 trie, Trie2.ValueWidth valueBits,
             int[][] checkRanges) {
         int countCheckRanges = checkRanges.length;

         int initialValue, errorValue;
         int value, value2;
         int start, limit;
         int i, countSpecials;

         boolean isFrozen = trie instanceof Trie2_16 || trie instanceof Trie2_32;

         String typeName= isFrozen ? "frozen trie" : "newTrie";

         countSpecials=0;  /*getSpecialValues(checkRanges, countCheckRanges, &initialValue, &errorValue);*/
         errorValue = 0x0bad;
         initialValue = 0;
         if (checkRanges[countSpecials][0] == 0) {
             initialValue = checkRanges[countSpecials][1];
             countSpecials++;
         }

         start=0;
         for(i=countSpecials; i<countCheckRanges; ++i) {
             limit=checkRanges[i][0];
             value=checkRanges[i][1];

             while(start<limit) {
                 value2=trie.get(start);
                 if(value!=value2) {
                     errln(testName + ".get(" + Integer.toHexString(start) +") == " + 
                             Integer.toHexString(value2) + " instead of " + Integer.toHexString(value));
                 }
                 ++start;
             }
         }


         if(!testName.startsWith("dummy") && !testName.startsWith("trie1")) {
             /* test values for lead surrogate code units */
             for(start=0xd7ff; start<0xdc01; ++start) {
                 switch(start) {
                 case 0xd7ff:
                 case 0xdc00:
                     value=errorValue;
                     break;
                 case 0xd800:
                     value=90;
                     break;
                 case 0xd999:
                     value=94;
                     break;
                 case 0xdbff:
                     value=99;
                     break;
                 default:
                     value=initialValue;
                     break;
                 }
                 value2 = trie.getFromU16SingleLead(start);
                 if(value2!=value) {
                     errln("trie2.getFromU16SingleLead() failed.  char, exected, actual = " +
                             start + ", " + value + ", " + value2);
                 }
             }
         }

         /* test errorValue */
         value=trie.get(-1);
         value2=trie.get(0x110000);
         if(value!=errorValue || value2!=errorValue) {
             errln("trie2.get() error value test.  Expected, actual1, actual2 = " +
                     errorValue + ", " + value + ", " + value2);
         }
     }
                     
     // Was testTrieRanges in ICU4C.  Renamed to not conflict with ICU4J test framework.
     private void checkTrieRanges(String testName, String serializedName, boolean withClone,
             int[][] setRanges, int [][] checkRanges) throws IOException {
         
         // We don't have the Trie2 builder yet.
         // Run tests against Tries that were built by ICU4C and serialized.
         String fileName16 = "Trie2Test." + serializedName + ".16.tri2";
         String fileName32 = "Trie2Test." + serializedName + ".32.tri2";
         String currentDir = new File(".").getAbsolutePath();
         System.out.println(currentDir);
         
         // TODO:  find out the right way to access the test data.
         String testDir = "src/com/ibm/icu/dev/test/util/";
         FileInputStream is = new FileInputStream(testDir + fileName16);
         Trie2  trie16 = Trie2.createFromSerialized(is);
         is.close();
         
         trieGettersTest(testName, trie16, Trie2.ValueWidth.BITS_16, checkRanges);
         
         is = new FileInputStream(testDir + fileName32);
         Trie2  trie32 = Trie2.createFromSerialized(is);
         is.close();
     }
     
     // Was "TrieTest" in trie2test.c 
     public void TestRanges() throws IOException {
         checkTrieRanges("set1",           "setRanges1",     false, setRanges1,     checkRanges1);         
         checkTrieRanges("set2-overlap",   "setRanges2",     false, setRanges2,     checkRanges2);
         checkTrieRanges("set3-initial-9", "setRanges3",     false, setRanges3,     checkRanges3);
         checkTrieRanges("set-empty",      "setRangesEmpty", false, setRangesEmpty, checkRangesEmpty);
         checkTrieRanges("set-single-value", "setRangesSingleValue", false, setRangesSingleValue, 
             checkRangesSingleValue);
         checkTrieRanges("set2-overlap.withClone", "setRanges2", true, setRanges2,     checkRanges2);
     }

}
