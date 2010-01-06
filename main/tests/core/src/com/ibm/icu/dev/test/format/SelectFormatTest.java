/*
 *******************************************************************************
 * Copyright (C) 2007-2009, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.format;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.impl.Utility;
import com.ibm.icu.text.SelectFormat;
import java.text.FieldPosition;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kirtig 
 * This class tests the API functionality of the SelectFormat
 */
public class SelectFormatTest extends TestFmwk {
  
static final String SIMPLE_PATTERN = "feminine {feminineVerbValue} other{otherVerbValue}";
static final int SELECT_PATTERN_DATA = 4 ;
static final int SELECT_SYNTAX_DATA = 10 ;
static final int EXP_FORMAT_RESULT_DATA = 12 ;
static final int NUM_OF_FORMAT_ARGS = 3 ;

  public static void main(String[] args) throws Exception {
    new SelectFormatTest().run(args);
  }
  
  public void TestConstructors() {
    log("Inside TestConstructors");
    System.out.println("\nInside TestConstructors");

    //Default constructor
    try{
        SelectFormat selFmt = new SelectFormat();
    }catch(UnsupportedOperationException e){
       System.out.println("Not implemented yet - TestConstructors while creating a default constructor");
    }catch(Exception e){
        errln("Exception encountered in TestConstructors while creating a default constructor");
    }

    //Constructor with argument - a pattern
    try{
        SelectFormat selFmt = new SelectFormat(SIMPLE_PATTERN);
    }catch(UnsupportedOperationException e){
       System.out.println("Not implemented yet - TestConstructors while creating a constructor with argument");
    }catch(Exception e){
        errln("Exception encountered in TestConstructors while creating a constructor with argument");
    }
  }

  public void TestEquals() {
    log("Inside TestEquals");
    System.out.println("\nInside TestEquals");
    SelectFormat selFmt1 = null;
    SelectFormat selFmt3 = null;

    //Check equality for Default constructed SelectFormats
    try{
        selFmt1 = new SelectFormat();
        SelectFormat selFmt2 = new SelectFormat();
        if( selFmt1.equals(selFmt2)){
            errln("Equals test failed while checking equality for default constructed SelectFormats ."); 
        }
    }catch(UnsupportedOperationException e){
       System.out.println("Not implemented yet - TestEquals ");
       //assertEquals("Equals test failed." ,selFmt1 , selFmt2); 
    }catch(Exception e){
        errln("Exception encountered in TestEquals");
    }

    //Check equality for pattern constructed SelectFormats
    try{
        selFmt3 = new SelectFormat(SIMPLE_PATTERN);
        SelectFormat selFmt4 = new SelectFormat(SIMPLE_PATTERN);
        if( selFmt3.equals(selFmt4)){
            errln("Equals test failed while checking equality for pattern constructed SelectFormats ."); 
        }
    }catch(UnsupportedOperationException e){
       System.out.println("Not implemented yet - TestEquals ");
       //assertEquals("Equals test failed." ,selFmt1 , selFmt2); 
    }catch(Exception e){
        errln("Exception encountered in TestEquals");
    }

    //Check equality for 2 objects  
    try{
        Object selFmt5 = new SelectFormat();
        Object selFmt6 = new SelectFormat(SIMPLE_PATTERN);
        if( selFmt1.equals(selFmt5)){
            errln("Equals test failed while checking equality for object."); 
        }
        if( selFmt3.equals(selFmt6)){
            errln("Equals test failed while checking equality for object."); 
        }
    }catch(UnsupportedOperationException e){
       System.out.println("Not implemented yet - TestEquals ");
       //assertEquals("Equals test failed." ,selFmt1 , selFmt3); 
    }catch(Exception e){
        errln("Exception encountered in TestEquals");
    }

  }//end of TestEquals

  public void TestApplyPatternToPattern() {
    log("Inside TestApplyPatternToPattern");
    System.out.println("\nInside TestApplyPatternToPattern");
    String pattern = "masculine{masculineVerbValue} other{otherVerbValue}";

    try{
        SelectFormat selFmt1 = new SelectFormat(SIMPLE_PATTERN);
        selFmt1.applyPattern(pattern);
        assertEquals("Failed in applyPattern,toPattern with unexpected output",( selFmt1.toPattern()), pattern);
    }catch(UnsupportedOperationException e){
       System.out.println("Not implemented yet - TestApplyPatternToPattern ");
    }catch(Exception e){
        errln("Exception encountered in TestApplyPatternToPattern");
    }
  }

  public void TestToString(){
    log("Inside TestToString");
    System.out.println("\nInside TestToString");
    try{
        SelectFormat selFmt1 = new SelectFormat(SIMPLE_PATTERN);
        String expected = "";
        selFmt1.toString();
        assertEquals("Failed in TestToString with unexpected output", (selFmt1.toString()), expected);
    }catch(UnsupportedOperationException e){
       System.out.println("Not implemented yet-ToString");
    }catch(Exception e){
        errln("Exception encountered in TestToString");
    }
  }

  public void TestHashCode(){
    log("Inside TestHashCode");
    System.out.println("\nInside TestHashCode");
    try{
        SelectFormat selFmt1 = new SelectFormat();
        int expected = 0;
        assertEquals("Failed in TestHashCode with unexpected output", (selFmt1.hashCode()), expected);
    }catch(UnsupportedOperationException e){
       System.out.println("Not implemented yet-HashCode");
    }catch(Exception e){
        errln("Exception encountered in TestHashCode");
    }
  }

  public void TestFormat(){
    log("Inside TestFormat");
    System.out.println("\nInside TestFormat");

    try{
        SelectFormat selFmt1 = new SelectFormat(SIMPLE_PATTERN);
        String expected = "feminineVerbValue";
        assertEquals("Failed in TestFormat with unexpected output", (selFmt1.format("feminine")), expected);
    }catch(UnsupportedOperationException e){
       System.out.println("Not implemented yet-TestFormat");
    }catch(Exception e){
        errln("Exception encountered in TestFormat");
    }

    try{
        SelectFormat selFmt1 = new SelectFormat(SIMPLE_PATTERN);
        String expected = "otherVerbValue";
        StringBuffer strBuf = new StringBuffer("AppendHere-");
        assertEquals("Failed in TestFormat with unexpected output 2", (selFmt1.format("other",strBuf, new FieldPosition(0) )), expected);
    }catch(UnsupportedOperationException e){
       System.out.println("Not implemented yet-TestFormat");
    }catch(Exception e){
        errln("Exception encountered in TestFormat");
    }

  }

}

