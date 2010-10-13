/*
 *******************************************************************************
 * Copyright (C) 2010, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.translit;

import java.util.ArrayList;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.text.Transliterator;



public class ThreadTest extends TestFmwk {
    public static void main(String[] args) throws Exception {
        new ThreadTest().run(args);
    }
    
    private ArrayList<X> threads = new ArrayList<X>();
    
    public void TestThreads()  {
        for (int i = 0; i < 1; i++) {
            X thread = new X();
            threads.add(thread);
            thread.start();
          }
        for (X thread: threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
    
    private static final String [] WORDS = {"edgar", "allen", "poe"};
   
    
    private static class X extends Thread {          
        public void run() {
            int charCount = 0;
            System.out.println("charCount = " + charCount);
            Transliterator tx = Transliterator.getInstance("Latin-Thai");
        
            for (int loop = 0; loop < 100000; loop++) {
                for (String s : WORDS) {
                    charCount += tx.transliterate(s).length();
                }                
            }
            System.out.println("charCount = " + charCount);
        }
    }

}
