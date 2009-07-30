/*
 *******************************************************************************
 * Copyright (C) 2009, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.util;

import java.util.Iterator;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.impl.Trie2;
import com.ibm.icu.impl.Trie2Builder;

/**
 * @author aheninger
 *
 */
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
         
         Trie2 trie = new Trie2Builder(0, 0);
         for (Iterator<Trie2.EnumRange> iter = trie.iterator(m); iter.hasNext(); ) {
             Trie2.EnumRange r = iter.next();

         }
         
         // Plain iteration, no mapping
         for (Trie2.EnumRange r: trie) {
             
         }
         
     }

}
