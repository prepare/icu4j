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


     private void testTrieRanges(String testName, String serializedName, boolean withClone,
             int[][] setRanges, int [][] checkRanges) throws IOException {
         
         // We don't have the Trie2 builder yet.
         // Run tests against Tries that were built by ICU4C and serialized.
         String fileName16 = "Trie2Test." + serializedName + ".16.tri2";
         String fileName32 = "Trie2Test." + serializedName + ".32.tri2";
         String currentDir = new File(".").getAbsolutePath();
         System.out.println(currentDir);
         FileInputStream is = new FileInputStream(fileName16);
         Trie2  trie16 = Trie2.createFromSerialized(is);
         is.close();
         
         is = new FileInputStream(fileName32);
         Trie2  trie32 = Trie2.createFromSerialized(is);
         is.close();
     }
     
     // Was "TrieTest" in trie2test.c 
     public void TestRanges() throws IOException {
         testTrieRanges("set1",           "setRanges1",     false, setRanges1,     checkRanges1);         
         testTrieRanges("set2-overlap",   "setRanges2",     false, setRanges2,     checkRanges2);
         testTrieRanges("set3-initial-9", "setRanges3",     false, setRanges3,     checkRanges3);
         testTrieRanges("set-empty",      "setRangesEmpty", false, setRangesEmpty, checkRangesEmpty);
         testTrieRanges("set-single-value", "setRangesSingleValue", false, setRangesSingleValue, 
             checkRangesSingleValue);
         testTrieRanges("set2-overlap.withClone", "setRanges2", true, setRanges2,     checkRanges2);
     }

}
