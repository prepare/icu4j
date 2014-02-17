/**
 *******************************************************************************
 * Copyright (C) 2001-2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package com.ibm.icu.dev.test.collator;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.impl.Utility;
import com.ibm.icu.text.CollationElementIterator;
import com.ibm.icu.text.CollationKey;
import com.ibm.icu.text.RawCollationKey;
import com.ibm.icu.text.RuleBasedCollator;

public class CollationTest extends TestFmwk {
    public static void main(String[] args) throws Exception{
        new CollationTest().run(args);
    }

    public CollationTest() {
    }

    // package private methods ----------------------------------------------
    
    static void doTest(TestFmwk test, RuleBasedCollator col, String source, 
                       String target, int result)
    {
        doTestVariant(test, col, source, target, result);
        if (result == -1) {
            doTestVariant(test, col, target, source, 1);
        } 
        else if (result == 1) {
            doTestVariant(test, col, target, source, -1);
        }
        else {
            doTestVariant(test, col, target, source, 0);
        }

        CollationElementIterator iter = col.getCollationElementIterator(source);
        backAndForth(test, iter);
        iter.setText(target);
        backAndForth(test, iter);
    }
    
    /**
     * Return an integer array containing all of the collation orders
     * returned by calls to next on the specified iterator
     */
    static int[] getOrders(CollationElementIterator iter) 
    {
        int maxSize = 100;
        int size = 0;
        int[] orders = new int[maxSize];
        
        int order;
        while ((order = iter.next()) != CollationElementIterator.NULLORDER) {
            if (size == maxSize) {
                maxSize *= 2;
                int[] temp = new int[maxSize];
                System.arraycopy(orders, 0, temp,  0, size);
                orders = temp;
            }
            orders[size++] = order;
        }
        
        if (maxSize > size) {
            int[] temp = new int[size];
            System.arraycopy(orders, 0, temp,  0, size);
            orders = temp;
        }
        return orders;
    }
    
    static void backAndForth(TestFmwk test, CollationElementIterator iter) 
    {
        // Run through the iterator forwards and stick it into an array
        iter.reset();
        int[] orders = getOrders(iter);
    
        // Now go through it backwards and make sure we get the same values
        int index = orders.length;
        int o;
    
        // reset the iterator
        iter.reset();
    
        while ((o = iter.previous()) != CollationElementIterator.NULLORDER) {
            if (o != orders[--index]) {
                if (o == 0) {
                    index ++;
                } else {
                    while (index > 0 && orders[index] == 0) {
                        index --;
                    } 
                    if (o != orders[index]) {
                        test.errln("Mismatch at index " + index + ": 0x" 
                            + Integer.toHexString(orders[index]) + " vs 0x" + Integer.toHexString(o));
                        break;
                    }
                }
            }
        }
    
        while (index != 0 && orders[index - 1] == 0) {
          index --;
        }
    
        if (index != 0) {
            String msg = "Didn't get back to beginning - index is ";
            test.errln(msg + index);
    
            iter.reset();
            test.err("next: ");
            while ((o = iter.next()) != CollationElementIterator.NULLORDER) {
                String hexString = "0x" + Integer.toHexString(o) + " ";
                test.err(hexString);
            }
            test.errln("");
            test.err("prev: ");
            while ((o = iter.previous()) != CollationElementIterator.NULLORDER) {
                String hexString = "0x" + Integer.toHexString(o) + " ";
                 test.err(hexString);
            }
            test.errln("");
        }
    }
    
    static final String appendCompareResult(int result, String target){
        if (result == -1) {
            target += "LESS";
        } else if (result == 0) {
            target += "EQUAL";
        } else if (result == 1) {
            target += "GREATER";
        } else {
            String huh = "?";
            target += huh + result;
        }
        return target;
    }

    static final String prettify(CollationKey key) {
        byte[] bytes = key.toByteArray();
        return prettify(bytes, bytes.length);
    }

    static final String prettify(RawCollationKey key) {
        return prettify(key.bytes, key.size);
    }

    static final String prettify(byte[] skBytes, int length) {
        StringBuilder target = new StringBuilder(length * 3 + 2).append('[');
    
        for (int i = 0; i < length; i++) {
            String numStr = Integer.toHexString(skBytes[i] & 0xff);
            if (numStr.length() < 2) {
                target.append('0');
            }
            target.append(numStr).append(' ');
        }
        target.append(']');
        return target.toString();
    }

    private static void doTestVariant(TestFmwk test, 
                                      RuleBasedCollator myCollation,
                                      String source, String target, int result)
    {
        boolean printInfo = false;
        int compareResult  = myCollation.compare(source, target);
        if (compareResult != result) {
            
            // !!! if not mod build, error, else nothing.
            // warnln if not build, error, else always print warning.
            // do we need a 'quiet warning?' (err or log).  Hmmm,
            // would it work to have the 'verbose' flag let you 
            // suppress warnings?  Are there ever some warnings you
            // want to suppress, and others you don't?
            if(!test.isModularBuild()){
                test.errln("Comparing \"" + Utility.hex(source) + "\" with \""
                           + Utility.hex(target) + "\" expected " + result
                           + " but got " + compareResult);
            }else{
                printInfo = true;
            }
        }
        CollationKey ssk = myCollation.getCollationKey(source);
        CollationKey tsk = myCollation.getCollationKey(target);
        compareResult = ssk.compareTo(tsk);
        if (compareResult != result) {
            
            if(!test.isModularBuild()){
                test.errln("Comparing CollationKeys of \"" + Utility.hex(source) 
                           + "\" with \"" + Utility.hex(target) 
                           + "\" expected " + result + " but got " 
                           + compareResult);
           }else{
               printInfo = true;
           }
        }
        RawCollationKey srsk = new RawCollationKey();
        myCollation.getRawCollationKey(source, srsk);
        RawCollationKey trsk = new RawCollationKey();
        myCollation.getRawCollationKey(target, trsk);
        compareResult = ssk.compareTo(tsk);
        if (compareResult != result) {
            
            if(!test.isModularBuild()){
                test.errln("Comparing RawCollationKeys of \"" 
                           + Utility.hex(source) 
                           + "\" with \"" + Utility.hex(target) 
                           + "\" expected " + result + " but got " 
                           + compareResult);
           }else{
               printInfo = true;
           }
        }
        // hmmm, but here we issue a warning
        // only difference is, one warning or two, and detailed info or not?
        // hmmm, does seem preferable to omit detail if we know it is due to missing resource data.
        // well, if we label the errors as warnings, we can let people know the details, but
        // also know they may be due to missing resource data.  basically this code is asserting
        // that the errors are due to missing resource data, which may or may not be true.
        if (printInfo) {
            test.warnln("Could not load locale data skipping.");
        }
    }
}
