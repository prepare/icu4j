//##header
/*
 **********************************************************************
 * Copyright (c) 2006, International Business Machines
 * Corporation and others.  All Rights Reserved.
 **********************************************************************
 * Created on 2006-4-21
 */
package com.ibm.icu.dev.test;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;

import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.ICUResourceBundleIterator;
import com.ibm.icu.util.UResourceBundle;
import com.ibm.icu.util.UResourceTypeMismatchException;

/**
 * Represents a collection of test data described in a UResourceBoundle file. 
 * 
 * The root of the UResourceBoundle file is a table resource, and it has one 
 * Info and one TestData sub-resources. The Info describes the data module
 * itself. The TestData, which is a table resource, has a collection of test 
 * data.
 * 
 * The test data is a named table resource which has Info, Settings, Headers,
 * and Cases sub-resources. 
 * 
 * <pre>
 * DataModule:table(nofallback){ 
 *   Info:table {} 
 *   TestData:table {
 *     entry_name:table{
 *       Info:table{}
 *       Settings:array{}
 *       Headers:array{}
 *       Cases:array{}
 *     }
 *   } 
 * }
 * </pre>
 * 
 * The test data is expected to be fed to test code by following sequence 
 *
 *   for each setting in Setting{
 *       prepare the setting
 *     for each test data in Cases{
 *       perform the test
 *     }
 *   }
 * 
 * For detail of the specification, please refer to the code. The code is 
 * initially ported from "icu4c/source/tools/ctestfw/unicode/tstdtmod.h"
 * and should be maintained parallelly.
 * 
 * @author Raymond Yang
 */
class ResourceModule implements TestDataModule {
    private static final String INFO = "Info";
//    private static final String DESCRIPTION = "Description";
//    private static final String LONG_DESCRIPTION = "LongDescription";
    private static final String TEST_DATA = "TestData";
    private static final String SETTINGS = "Settings";
    private static final String HEADER = "Headers";
    private static final String DATA = "Cases";

    
    ICUResourceBundle res;
    ICUResourceBundle info;
    ICUResourceBundle defaultHeader;
    ICUResourceBundle testData;
    
    ResourceModule(String baseName, String localeName) throws DataModuleFormatError{

        res = (ICUResourceBundle) UResourceBundle.getBundleInstance(baseName, localeName);
        info = getFromTable(res, INFO, ICUResourceBundle.TABLE);
        testData = getFromTable(res, TEST_DATA, ICUResourceBundle.TABLE);

        try {
            // unfortunately, actually, data can be either ARRAY or STRING
            defaultHeader = getFromTable(info, HEADER, new int[]{ICUResourceBundle.ARRAY, ICUResourceBundle.STRING});
        } catch (MissingResourceException e){
            defaultHeader = null;
        }
    }

    public String getName() {
        return res.getKey();
    }

    public DataMap getInfo() {
        return new UTableResource(info);
    }

    public TestData getTestData(String testName) throws DataModuleFormatError {
        return new UResourceTestData(defaultHeader, testData.get(testName));
    }

    public Iterator getTestDataIterator() {
        return new IteratorAdapter(testData){
            protected Object prepareNext(ICUResourceBundle nextRes) throws DataModuleFormatError {
                return new UResourceTestData(defaultHeader, nextRes);
            }
        };
    }

    /**
     * To make ICUResourceBundleIterator works like Iterator
     * and return various data-driven test object for next() call
     * 
     * @author Raymond Yang
     */
    private abstract static class IteratorAdapter implements Iterator{
        private ICUResourceBundle res;
        private ICUResourceBundleIterator itr;
        private Object preparedNextElement = null;
        // fix a strange behavior for ICUResourceBundleIterator for 
        // ICUResourceBundle.STRING. It support hasNext(), but does 
        // not support next() now. 
        // 
        // Use the iterated resource itself as the result from next() call
        private boolean isStrRes = false;
        private boolean isStrResPrepared = false; // for STRING resouce, we only prepare once

        IteratorAdapter(ICUResourceBundle theRes) {
            assert_not (theRes == null);
            res = theRes;
            itr = res.getIterator();
            isStrRes = res.getType() == ICUResourceBundle.STRING;
        }
        
        public void remove() {
            // do nothing
        }

        private boolean hasNextForStrRes(){
            assert_is (isStrRes);
            assert_not (!isStrResPrepared && preparedNextElement != null);
            if (isStrResPrepared && preparedNextElement != null) return true;
            if (isStrResPrepared && preparedNextElement == null) return false; // only prepare once
            assert_is (!isStrResPrepared && preparedNextElement == null);
            
            try {
                preparedNextElement = prepareNext(res);
                assert_not (preparedNextElement == null, "prepareNext() should not return null");
                isStrResPrepared = true; // toggle the tag
                return true;
            } catch (DataModuleFormatError e) {
//#ifdef FOUNDATION
//##                throw new RuntimeException(e.getMessage());
//#else
                throw new RuntimeException(e.getMessage(),e);
//#endif
            }            
        }
        public boolean hasNext() {
            if (isStrRes) return hasNextForStrRes();
            
            if (preparedNextElement != null) return true;
            ICUResourceBundle t = null;
            if (itr.hasNext()) {
                // Notice, other RuntimeException may be throwed
                t = itr.next();
            } else {
                return false;
            }

            try {
                preparedNextElement = prepareNext(t);
                assert_not (preparedNextElement == null, "prepareNext() should not return null");
                return true;
            } catch (DataModuleFormatError e) {
                // Sadly, we throw RuntimeException also
//#ifdef FOUNDATION
//##                throw new RuntimeException(e.getMessage());
//#else
                throw new RuntimeException(e.getMessage(),e);
//#endif
            }
        }

        public Object next(){
            if (hasNext()) {
                Object t = preparedNextElement;
                preparedNextElement = null;
                return t;
            } else {
                throw new NoSuchElementException();
            }
        }
        /**
         * To prepare data-driven test object for next() call, should not return null
         */
        abstract protected Object prepareNext(ICUResourceBundle nextRes) throws DataModuleFormatError;
    }
    
    
    /**
     * Avoid use Java 1.4 language new assert keyword 
     */
    static void assert_is(boolean eq, String msg){
        if (!eq) throw new Error("test code itself has error: " + msg);
    }
    static void assert_is(boolean eq){
        if (!eq) throw new Error("test code itself has error.");
    }
    static void assert_not(boolean eq, String msg){
        assert_is(!eq, msg);
    }
    static void assert_not(boolean eq){
        assert_is(!eq);
    }
            
    /**
     * Internal helper function to get resource with following add-on 
     * 
     * 1. Assert the returned resource is never null.
     * 2. Check the type of resource. 
     * 
     * The UResourceTypeMismatchException for various get() method is a 
     * RuntimeException which can be silently bypassed. This behavior is a 
     * trouble. One purpose of the class is to enforce format checking for 
     * resource file. We don't want to the exceptions are silently bypassed 
     * and spreaded to our customer's code. 
     * 
     * Notice, the MissingResourceException for get() method is also a
     * RuntimeException. The caller functions should avoid sepread the execption
     * silently also. The behavior is modified because some resource are 
     * optional and can be missed.
     */
    static ICUResourceBundle getFromTable(ICUResourceBundle res, String key, int expResType) throws DataModuleFormatError{
        return getFromTable(res, key, new int[]{expResType});
    }
    
    static ICUResourceBundle getFromTable(ICUResourceBundle res, String key, int[] expResTypes) throws DataModuleFormatError{
        assert_is (res != null && key != null && res.getType() == ICUResourceBundle.TABLE);
        ICUResourceBundle t = res.get(key); 
      
        assert_not (t ==null);
        int type = t.getType();
        Arrays.sort(expResTypes);
        if (Arrays.binarySearch(expResTypes, type) >= 0) {
            return t;
        } else {
//#ifdef FOUNDATION
//##            throw new DataModuleFormatError("Actual type " + t.getType() + " != expected types " + expResTypes + ".");
//#else
            throw new DataModuleFormatError(new UResourceTypeMismatchException("Actual type " + t.getType() + " != expected types " + expResTypes + "."));
//#endif
        }
    }
    
    /**
     * Unfortunately, ICUResourceBundle is unable to treat one string as string array.
     * This function return a String[] from ICUResourceBundle, regardless it is an array or a string 
     */
    static String[] getStringArrayHelper(ICUResourceBundle res, String key) throws DataModuleFormatError{
        ICUResourceBundle t = getFromTable(res, key, new int[]{ICUResourceBundle.ARRAY, ICUResourceBundle.STRING});
        return getStringArrayHelper(t);
    }

    static String[] getStringArrayHelper(ICUResourceBundle res) throws DataModuleFormatError{
        try{
            int type = res.getType();
            switch (type) {
            case ICUResourceBundle.ARRAY:
                return res.getStringArray();
            case ICUResourceBundle.STRING:
                return new String[]{res.getString()};
            default:
                throw new UResourceTypeMismatchException("Only accept ARRAY and STRING types.");
            }
        } catch (UResourceTypeMismatchException e){
//#ifdef FOUNDATION
//##            throw new DataModuleFormatError(e.getMessage());
//#else
            throw new DataModuleFormatError(e);
//#endif
        }
    }
    
    public static void main(String[] args){
        try {
            TestDataModule m = new ResourceModule("com/ibm/icu/dev/data/testdata/","DataDrivenCollationTest");
        System.out.println("hello: " + m.getName());
        m.getInfo();
        m.getTestDataIterator();
        } catch (DataModuleFormatError e) {
            // TODO Auto-generated catch block
            System.out.println("???");
            e.printStackTrace();
        }
    }

    private static class UResourceTestData implements TestData{
        private ICUResourceBundle res;
        private ICUResourceBundle info;
        private ICUResourceBundle settings; 
        private ICUResourceBundle header;
        private ICUResourceBundle data;

        UResourceTestData(ICUResourceBundle defaultHeader, ICUResourceBundle theRes) throws DataModuleFormatError{
            
            assert_is (theRes != null && theRes.getType() == ICUResourceBundle.TABLE);
            res = theRes;
            // unfortunately, actually, data can be either ARRAY or STRING
            data = getFromTable(res, DATA, new int[]{ICUResourceBundle.ARRAY, ICUResourceBundle.STRING});
       

            
            try {
                // unfortunately, actually, data can be either ARRAY or STRING
                header = getFromTable(res, HEADER, new int[]{ICUResourceBundle.ARRAY, ICUResourceBundle.STRING});
            } catch (MissingResourceException e){
                if (defaultHeader == null) {
                    throw new DataModuleFormatError("Unable to find a header for test data '" + res.getKey() + "' and no default header exist.");
                } else {
                    header = defaultHeader;
                }
            }
         try{
                settings = getFromTable(res, SETTINGS, ICUResourceBundle.ARRAY);
                info = getFromTable(res, INFO, ICUResourceBundle.TABLE);
            } catch (MissingResourceException e){
                // do nothing, left them null;
                settings = data;
            }
        }
        
        public String getName() {
            return res.getKey();
        }

        public DataMap getInfo() {
            return info == null ? null : new UTableResource(info);
        }

        public Iterator getSettingsIterator() {
            assert_is (settings.getType() == ICUResourceBundle.ARRAY);
            return new IteratorAdapter(settings){
                protected Object prepareNext(ICUResourceBundle nextRes) throws DataModuleFormatError {
                    return new UTableResource(nextRes);
                }
            };
        }

        public Iterator getDataIterator() {
            // unfortunately,
            assert_is (data.getType() == ICUResourceBundle.ARRAY 
                 || data.getType() == ICUResourceBundle.STRING);
            return new IteratorAdapter(data){
                protected Object prepareNext(ICUResourceBundle nextRes) throws DataModuleFormatError {
                    return new UArrayResource(header, nextRes);
                }
            };
        }
    }
        
    private static class UTableResource implements DataMap{
        private ICUResourceBundle res;

        UTableResource(ICUResourceBundle theRes){
            res = theRes;
        }
        public String getString(String key) {
            String t;
            try{
                t = res.getString(key);
            } catch (MissingResourceException e){
                t = null;
            }
            return t;
        }
         public Object getObject(String key) {
            
            return res.get(key);
        }
    }
    
    private static class UArrayResource implements DataMap{
        private Map theMap; 
        UArrayResource(ICUResourceBundle theHeader, ICUResourceBundle theData) throws DataModuleFormatError{
            assert_is (theHeader != null && theData != null);
            String[] header;
         
            header = getStringArrayHelper(theHeader);
            if (theData.getSize() != header.length) 
                throw new DataModuleFormatError("The count of Header and Data is mismatch.");
            theMap = new HashMap();
            for (int i = 0; i < header.length; i++) {
                if(theData.getType()==ICUResourceBundle.ARRAY){
                    theMap.put(header[i], theData.get(i));
                }else if(theData.getType()==ICUResourceBundle.STRING){
                    theMap.put(header[i], theData.getString());
                }else{
                    throw new DataModuleFormatError("Did not get the expected data!");                   
                }
            }
            
        }
        
        public String getString(String key) {
            return (String)theMap.get(key);
        }
        public Object getObject(String key) {
            return theMap.get(key);
        }
    }
}
