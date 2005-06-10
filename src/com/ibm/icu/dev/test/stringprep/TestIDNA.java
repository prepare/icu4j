/*
 *******************************************************************************
 * Copyright (C) 2003-2005, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
*/
package com.ibm.icu.dev.test.stringprep;

import java.io.InputStream;
import java.util.Random;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.text.IDNA;
import com.ibm.icu.text.StringPrepParseException;
import com.ibm.icu.text.StringPrep;
import com.ibm.icu.text.UCharacterIterator;
import com.ibm.icu.text.UTF16;
import com.ibm.icu.impl.ICUData;
import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.Utility;

/**
 * @author ram
 */
public class TestIDNA extends TestFmwk {
    public static void main(String[] args) throws Exception {
        new TestIDNA().run(args);
    }
    private StringPrepParseException unassignedException = new StringPrepParseException("",StringPrepParseException.UNASSIGNED_ERROR);
    public void TestToUnicode() throws Exception{
        for(int i=0; i<TestData.asciiIn.length; i++){
            // test StringBuffer toUnicode
            doTestToUnicode(TestData.asciiIn[i],new String(TestData.unicodeIn[i]),IDNA.DEFAULT, null);
            doTestToUnicode(TestData.asciiIn[i],new String(TestData.unicodeIn[i]),IDNA.ALLOW_UNASSIGNED, null);
            doTestToUnicode(TestData.asciiIn[i],new String(TestData.unicodeIn[i]),IDNA.USE_STD3_RULES, null); 
            doTestToUnicode(TestData.asciiIn[i],new String(TestData.unicodeIn[i]),IDNA.USE_STD3_RULES|IDNA.ALLOW_UNASSIGNED, null); 
    
        }
    }
    
    public void TestToASCII() throws Exception{
        for(int i=0; i<TestData.asciiIn.length; i++){
            // test StringBuffer toUnicode
            doTestToASCII(new String(TestData.unicodeIn[i]),TestData.asciiIn[i],IDNA.DEFAULT, null);
            doTestToASCII(new String(TestData.unicodeIn[i]),TestData.asciiIn[i],IDNA.ALLOW_UNASSIGNED, null);
            doTestToUnicode(TestData.asciiIn[i],new String(TestData.unicodeIn[i]),IDNA.USE_STD3_RULES, null); 
            doTestToUnicode(TestData.asciiIn[i],new String(TestData.unicodeIn[i]),IDNA.USE_STD3_RULES|IDNA.ALLOW_UNASSIGNED, null); 
    
        }
    }
    
    public void TestIDNToASCII() throws Exception{
        for(int i=0; i<TestData.domainNames.length; i++){
            doTestIDNToASCII(TestData.domainNames[i],TestData.domainNames[i],IDNA.DEFAULT, null);
            doTestIDNToASCII(TestData.domainNames[i],TestData.domainNames[i],IDNA.ALLOW_UNASSIGNED, null);
            doTestIDNToASCII(TestData.domainNames[i],TestData.domainNames[i],IDNA.USE_STD3_RULES, null);
            doTestIDNToASCII(TestData.domainNames[i],TestData.domainNames[i],IDNA.ALLOW_UNASSIGNED|IDNA.USE_STD3_RULES, null);
        }
        
        for(int i=0; i<TestData.domainNames1Uni.length; i++){
            doTestIDNToASCII(TestData.domainNames1Uni[i],TestData.domainNamesToASCIIOut[i],IDNA.DEFAULT, null);
            doTestIDNToASCII(TestData.domainNames1Uni[i],TestData.domainNamesToASCIIOut[i],IDNA.ALLOW_UNASSIGNED, null);
        }
    }
    public void TestIDNToUnicode() throws Exception{
        for(int i=0; i<TestData.domainNames.length; i++){
            doTestIDNToUnicode(TestData.domainNames[i],TestData.domainNames[i],IDNA.DEFAULT, null);
            doTestIDNToUnicode(TestData.domainNames[i],TestData.domainNames[i],IDNA.ALLOW_UNASSIGNED, null);
            doTestIDNToUnicode(TestData.domainNames[i],TestData.domainNames[i],IDNA.USE_STD3_RULES, null);
            doTestIDNToUnicode(TestData.domainNames[i],TestData.domainNames[i],IDNA.ALLOW_UNASSIGNED|IDNA.USE_STD3_RULES, null);
        }
        for(int i=0; i<TestData.domainNamesToASCIIOut.length; i++){
            doTestIDNToUnicode(TestData.domainNamesToASCIIOut[i],TestData.domainNamesToUnicodeOut[i],IDNA.DEFAULT, null);
            doTestIDNToUnicode(TestData.domainNamesToASCIIOut[i],TestData.domainNamesToUnicodeOut[i],IDNA.ALLOW_UNASSIGNED, null);
        }
    }
    
    private void doTestToUnicode(String src, String expected, int options, Object expectedException) 
                throws Exception{
        StringBuffer inBuf = new StringBuffer(src);
        UCharacterIterator inIter = UCharacterIterator.getInstance(src);
        try{
            
            StringBuffer out = IDNA.convertToUnicode(src,options);
            if(expected!=null && out != null && !out.toString().equals(expected)){
                errln("convertToUnicode did not return expected result with options : "+ options + 
                      " Expected: " + prettify(expected)+" Got: "+prettify(out));
            }
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertToUnicode did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !ex.equals(expectedException)){
                errln("convertToUnicode did not get the expected exception for source: " + prettify(src) +" Got:  "+ ex.toString());
            }
        }
        try{
            
            StringBuffer out = IDNA.convertToUnicode(inBuf,options);
            if(expected!=null && out != null && !out.toString().equals(expected)){
               errln("convertToUnicode did not return expected result with options : "+ options + 
                     " Expected: " + prettify(expected)+" Got: "+out);
            }
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertToUnicode did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !ex.equals(expectedException)){
                errln("convertToUnicode did not get the expected exception for source: " + prettify(src) +" Got:  "+ ex.toString());
            }
        }
        
        try{
            StringBuffer out = IDNA.convertToUnicode(inIter,options);
            if(expected!=null && out != null && !out.toString().equals(expected)){
               errln("convertToUnicode did not return expected result with options : "+ options +
                     " Expected: " + prettify(expected)+" Got: "+prettify(out));
            }
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("Did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !ex.equals(expectedException)){
                errln("Did not get the expected exception for source: " + prettify(src) +" Got:  "+ ex.toString());
            }
        }
    }
    
    private void doTestIDNToUnicode(String src, String expected, int options, Object expectedException) 
                throws Exception{
        StringBuffer inBuf = new StringBuffer(src);
        UCharacterIterator inIter = UCharacterIterator.getInstance(src);
        try{
            
            StringBuffer out = IDNA.convertIDNToUnicode(src,options);
            if(expected!=null && out != null && !out.toString().equals(expected)){
                errln("convertToUnicode did not return expected result with options : "+ options + 
                      " Expected: " + prettify(expected)+" Got: "+prettify(out));
            }
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertToUnicode did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !expectedException.equals(ex)){
                errln("convertToUnicode did not get the expected exception for source: " +src +" Got:  "+ ex.toString());
            }
        }
        try{
            StringBuffer out = IDNA.convertIDNToUnicode(inBuf,options);
            if(expected!=null && out != null && !out.toString().equals(expected)){
               errln("convertToUnicode did not return expected result with options : "+ options + 
                     " Expected: " + prettify(expected)+" Got: "+out);
            }
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertToUnicode did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !expectedException.equals(ex)){
                errln("convertToUnicode did not get the expected exception for source: " +src +" Got:  "+ ex.toString());
            }
        }
        
        try{
            StringBuffer out = IDNA.convertIDNToUnicode(inIter,options);
            if(expected!=null && out != null && !out.toString().equals(expected)){
               errln("convertToUnicode did not return expected result with options : "+ options +
                     " Expected: " + prettify(expected)+" Got: "+prettify(out));
            }
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("Did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !expectedException.equals(ex)){
                errln("Did not get the expected exception for source: " +src +" Got:  "+ ex.toString());
            }
        }
    }
    private void doTestToASCII(String src, String expected, int options, Object expectedException) 
                throws Exception{
        StringBuffer inBuf = new StringBuffer(src);
        UCharacterIterator inIter = UCharacterIterator.getInstance(src);
        try{
            
            StringBuffer out = IDNA.convertToASCII(src,options);
            if(!unassignedException.equals(expectedException) && expected!=null && out != null && expected!=null && out != null && !out.toString().equals(expected.toLowerCase())){
                errln("convertToASCII did not return expected result with options : "+ options + 
                      " Expected: " + expected+" Got: "+out);
            }           
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertToASCII did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !expectedException.equals(ex)){
                errln("convertToASCII did not get the expected exception for source: " +src +"\n Got:  "+ ex.toString() +"\n Expected: " +ex.toString());
            }
        }
        
        try{            
            StringBuffer out = IDNA.convertToASCII(inBuf,options);
            if(!unassignedException.equals(expectedException) && expected!=null && out != null && expected!=null && out != null && !out.toString().equals(expected.toLowerCase())){
               errln("convertToASCII did not return expected result with options : "+ options + 
                     " Expected: " + expected+" Got: "+out);
            }
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertToASCII did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !expectedException.equals(ex)){
                errln("convertToASCII did not get the expected exception for source: " +src +" Got:  "+ ex.toString());
            }
        }
        
        try{
            StringBuffer out = IDNA.convertToASCII(inIter,options);
            if(!unassignedException.equals(expectedException) && expected!=null && out != null && expected!=null && out != null && !out.toString().equals(expected.toLowerCase())){
               errln("convertToASCII did not return expected result with options : "+ options +
                     " Expected: " + expected+" Got: "+ out);
            }
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertToASCII did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !expectedException.equals(ex)){
                errln("convertToASCII did not get the expected exception for source: " +src +" Got:  "+ ex.toString());
            }
        }
    }
    private void doTestIDNToASCII(String src, String expected, int options, Object expectedException) 
                throws Exception{
        StringBuffer inBuf = new StringBuffer(src);
        UCharacterIterator inIter = UCharacterIterator.getInstance(src);
        try{
            
            StringBuffer out = IDNA.convertIDNToASCII(src,options);
            if(expected!=null && out != null && !out.toString().equals(expected)){
                errln("convertToIDNASCII did not return expected result with options : "+ options + 
                      " Expected: " + expected+" Got: "+out);
            }
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertToIDNASCII did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !ex.equals(expectedException)){
                errln("convertToIDNASCII did not get the expected exception for source: " +src +" Got:  "+ ex.toString());
            }
        }
        try{
            StringBuffer out = IDNA.convertIDNToASCII(inBuf,options);
            if(expected!=null && out != null && !out.toString().equals(expected)){
               errln("convertToIDNASCII did not return expected result with options : "+ options + 
                     " Expected: " + expected+" Got: "+out);
            }           
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertToIDNASCII did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !ex.equals(expectedException)){
                errln("convertToIDNASCII did not get the expected exception for source: " +src +" Got:  "+ ex.toString());
            }
        }
        
        try{
            StringBuffer out = IDNA.convertIDNToASCII(inIter,options);
            if(expected!=null && out != null && !out.toString().equals(expected)){
               errln("convertIDNToASCII did not return expected result with options : "+ options +
                     " Expected: " + expected+" Got: "+ out);
            }
            
            if(expectedException!=null && !unassignedException.equals(expectedException)){
                errln("convertIDNToASCII did not get the expected exception. The operation succeeded!");
            }
        }catch(StringPrepParseException ex){
            if(expectedException == null || !ex.equals(expectedException)){
                errln("convertIDNToASCII did not get the expected exception for source: " +src +" Got:  "+ ex.toString());
            }
        }
    }
    public void TestConformance()throws Exception{
        for(int i=0; i<TestData.conformanceTestCases.length;i++){
            
            TestData.ConformanceTestCase testCase = TestData.conformanceTestCases[i];
            if(testCase.expected != null){
                //Test toASCII
                doTestToASCII(testCase.input,testCase.output,IDNA.DEFAULT,testCase.expected);
                doTestToASCII(testCase.input,testCase.output,IDNA.ALLOW_UNASSIGNED,testCase.expected);
            }
            //Test toUnicode
            //doTestToUnicode(testCase.input,testCase.output,IDNA.DEFAULT,testCase.expected);
        }
    }
    public void TestNamePrepConformance() throws Exception{
        InputStream stream = ICUData.getRequiredStream(ICUResourceBundle.ICU_BUNDLE+"/uidna.spp");
        StringPrep namePrep = new StringPrep(stream);
        for(int i=0; i<TestData.conformanceTestCases.length;i++){
            TestData.ConformanceTestCase testCase = TestData.conformanceTestCases[i];
            UCharacterIterator iter = UCharacterIterator.getInstance(testCase.input);
            try{
                StringBuffer output = namePrep.prepare(iter,StringPrep.DEFAULT);
                if(testCase.output !=null && output!=null && !testCase.output.equals(output.toString())){
                    errln("Did not get the expected output. Expected: " + prettify(testCase.output)+
                          " Got: "+ prettify(output) );
                }
                if(testCase.expected!=null && !unassignedException.equals(testCase.expected)){
                    errln("Did not get the expected exception. The operation succeeded!");
                }
            }catch(StringPrepParseException ex){
                if(testCase.expected == null || !ex.equals(testCase.expected)){
                    errln("Did not get the expected exception for source: " +testCase.input +" Got:  "+ ex.toString());
                }
            }
            
            try{
                iter.setToStart();
                StringBuffer output = namePrep.prepare(iter,StringPrep.ALLOW_UNASSIGNED);
                if(testCase.output !=null && output!=null && !testCase.output.equals(output.toString())){
                    errln("Did not get the expected output. Expected: " + prettify(testCase.output)+
                          " Got: "+ prettify(output) );
                }
                if(testCase.expected!=null && !unassignedException.equals(testCase.expected)){
                    errln("Did not get the expected exception. The operation succeeded!");
                }
            }catch(StringPrepParseException ex){
                if(testCase.expected == null || !ex.equals(testCase.expected)){
                    errln("Did not get the expected exception for source: " +testCase.input +" Got:  "+ ex.toString());
                }
            }
        }
        
    }
    public void TestErrorCases() throws Exception{
        for(int i=0; i < TestData.errorCases.length; i++){
            TestData.ErrorCase errCase = TestData.errorCases[i];
            if(errCase.testLabel==true){
                // Test ToASCII
                doTestToASCII(new String(errCase.unicode),errCase.ascii,IDNA.DEFAULT,errCase.expected);
                doTestToASCII(new String(errCase.unicode),errCase.ascii,IDNA.ALLOW_UNASSIGNED,errCase.expected);
                if(errCase.useSTD3ASCIIRules){
                    doTestToASCII(new String(errCase.unicode),errCase.ascii,IDNA.USE_STD3_RULES,errCase.expected);
                }
            }
            if(errCase.useSTD3ASCIIRules!=true){
                
                // Test IDNToASCII
                doTestIDNToASCII(new String(errCase.unicode),errCase.ascii,IDNA.DEFAULT,errCase.expected);
                doTestIDNToASCII(new String(errCase.unicode),errCase.ascii,IDNA.ALLOW_UNASSIGNED,errCase.expected);
                
            }else{
                doTestIDNToASCII(new String(errCase.unicode),errCase.ascii,IDNA.USE_STD3_RULES,errCase.expected);
            }
            
            //TestToUnicode
            if(errCase.testToUnicode==true){
                if(errCase.useSTD3ASCIIRules!=true){
                    // Test IDNToUnicode
                    doTestIDNToUnicode(errCase.ascii,new String(errCase.unicode),IDNA.DEFAULT,errCase.expected);
                    doTestIDNToUnicode(errCase.ascii,new String(errCase.unicode),IDNA.ALLOW_UNASSIGNED,errCase.expected);
                
                }else{
                    doTestIDNToUnicode(errCase.ascii,new String(errCase.unicode),IDNA.USE_STD3_RULES,errCase.expected);
                }
            }
        }
    }
    private void doTestCompare(String s1, String s2, boolean isEqual){
        try{
            int retVal = IDNA.compare(s1,s2,IDNA.DEFAULT);
            if(isEqual==true && retVal != 0){
                errln("Did not get the expected result for s1: "+ prettify(s1)+ 
                      " s2: "+prettify(s2));
            }
            retVal = IDNA.compare(new StringBuffer(s1), new StringBuffer(s2), IDNA.DEFAULT);
            if(isEqual==true && retVal != 0){
                errln("Did not get the expected result for s1: "+ prettify(s1)+ 
                     " s2: "+prettify(s2));
            }
            retVal = IDNA.compare(UCharacterIterator.getInstance(s1), UCharacterIterator.getInstance(s2), IDNA.DEFAULT);
            if(isEqual==true && retVal != 0){
                errln("Did not get the expected result for s1: "+ prettify(s1)+ 
                     " s2: "+prettify(s2));
            }
        }catch(Exception e){
            e.printStackTrace();
            errln("Unexpected exception thrown by IDNA.compare");
        }
        
        try{
            int retVal = IDNA.compare(s1,s2,IDNA.ALLOW_UNASSIGNED);
            if(isEqual==true && retVal != 0){
                errln("Did not get the expected result for s1: "+ prettify(s1)+ 
                      " s2: "+prettify(s2));
            }
            retVal = IDNA.compare(new StringBuffer(s1), new StringBuffer(s2), IDNA.ALLOW_UNASSIGNED);
            if(isEqual==true && retVal != 0){
                errln("Did not get the expected result for s1: "+ prettify(s1)+ 
                     " s2: "+prettify(s2));
            }
            retVal = IDNA.compare(UCharacterIterator.getInstance(s1), UCharacterIterator.getInstance(s2), IDNA.ALLOW_UNASSIGNED);
            if(isEqual==true && retVal != 0){
                errln("Did not get the expected result for s1: "+ prettify(s1)+ 
                     " s2: "+prettify(s2));
            }
        }catch(Exception e){
            errln("Unexpected exception thrown by IDNA.compare");
        }
    }
    public void TestCompare() throws Exception{
        String www = "www.";
        String com = ".com";
        StringBuffer source = new StringBuffer(www);
        StringBuffer uni0   = new StringBuffer(www);
        StringBuffer uni1   = new StringBuffer(www);
        StringBuffer ascii0 = new StringBuffer(www);
        StringBuffer ascii1 = new StringBuffer(www);

        uni0.append(TestData.unicodeIn[0]);
        uni0.append(com);

        uni1.append(TestData.unicodeIn[1]);
        uni1.append(com);

        ascii0.append(TestData.asciiIn[0]);
        ascii0.append(com);

        ascii1.append(TestData.asciiIn[1]);
        ascii1.append(com);

        for(int i=0;i< TestData.unicodeIn.length; i++){

            // for every entry in unicodeIn array
            // prepend www. and append .com
            source.setLength(4);
            source.append(TestData.unicodeIn[i]);
            source.append(com);
            
            // a) compare it with itself
            doTestCompare(source.toString(),source.toString(),true);
        
            // b) compare it with asciiIn equivalent
            doTestCompare(source.toString(),www+TestData.asciiIn[i]+com,true);
        
            // c) compare it with unicodeIn not equivalent
            if(i==0){
                doTestCompare(source.toString(), uni1.toString(), false);
            }else{
                doTestCompare(source.toString(),uni0.toString(), false);
            }
            // d) compare it with asciiIn not equivalent
            if(i==0){
                doTestCompare(source.toString(),ascii1.toString(), false);
            }else{
                doTestCompare(source.toString(),ascii0.toString(), false);
            }

        }
    }

    //  test and ascertain
    //  func(func(func(src))) == func(src)
    public void doTestChainingToASCII(String source)throws Exception{
        StringBuffer expected; 
        StringBuffer chained;
        
        // test convertIDNToASCII
        expected = IDNA.convertIDNToASCII(source,IDNA.DEFAULT);
        chained = expected;
        for(int i=0; i< 4; i++){
            chained = IDNA.convertIDNToASCII(chained,IDNA.DEFAULT);
        }
        if(!expected.toString().equals(chained.toString())){
            errln("Chaining test failed for convertIDNToASCII");
        }
        // test convertIDNToA
        expected = IDNA.convertToASCII(source,IDNA.DEFAULT);
        chained = expected;
        for(int i=0; i< 4; i++){
            chained = IDNA.convertToASCII(chained,IDNA.DEFAULT);
        }
        if(!expected.toString().equals(chained.toString())){
            errln("Chaining test failed for convertToASCII");
        }   
    }
    //  test and ascertain
    //  func(func(func(src))) == func(src)
    public void doTestChainingToUnicode(String source)throws Exception{
        StringBuffer expected; 
        StringBuffer chained;
        
        // test convertIDNToUnicode
        expected = IDNA.convertIDNToUnicode(source,IDNA.DEFAULT);
        chained = expected;
        for(int i=0; i< 4; i++){
            chained = IDNA.convertIDNToUnicode(chained,IDNA.DEFAULT);
        }
        if(!expected.toString().equals(chained.toString())){
            errln("Chaining test failed for convertIDNToUnicode");
        }
        // test convertIDNToA
        expected = IDNA.convertToUnicode(source,IDNA.DEFAULT);
        chained = expected;
        for(int i=0; i< 4; i++){
            chained = IDNA.convertToUnicode(chained,IDNA.DEFAULT);
        }
        if(!expected.toString().equals(chained.toString())){
            errln("Chaining test failed for convertToUnicode");
        }   
    }
    public void TestChaining() throws Exception{
        for(int i=0; i< TestData.asciiIn.length; i++){
            doTestChainingToUnicode(TestData.asciiIn[i]);
        }
        for(int i=0; i< TestData.unicodeIn.length; i++){
            doTestChainingToASCII(new String(TestData.unicodeIn[i]));
        }
    }
    

    /* IDNA RFC Says:
    A label is an individual part of a domain name.  Labels are usually
    shown separated by dots; for example, the domain name
    "www.example.com" is composed of three labels: "www", "example", and
    "com".  (The zero-length root label described in [STD13], which can
    be explicit as in "www.example.com." or implicit as in
    "www.example.com", is not considered a label in this specification.)
     so this test is wrong. The conformance test also tests whether an exception is thrown
     when the API is given a zero-length lable.
    *
    public void TestRootLabelSeparator() throws Exception{
        String www = "www.";
        String com = ".com."; //root label separator
        StringBuffer source = new StringBuffer(www);
        StringBuffer uni0   = new StringBuffer(www);
        StringBuffer uni1   = new StringBuffer(www);
        StringBuffer ascii0 = new StringBuffer(www);
        StringBuffer ascii1 = new StringBuffer(www);

        uni0.append(TestData.unicodeIn[0]);
        uni0.append(com);

        uni1.append(TestData.unicodeIn[1]);
        uni1.append(com);

        ascii0.append(TestData.asciiIn[0]);
        ascii0.append(com);

        ascii1.append(TestData.asciiIn[1]);
        ascii1.append(com);

        for(int i=0;i< TestData.unicodeIn.length; i++){

            // for every entry in unicodeIn array
            // prepend www. and append .com
            source.setLength(4);
            source.append(TestData.unicodeIn[i]);
            source.append(com);
            
            // a) compare it with itself
            doTestCompare(source.toString(),source.toString(),true);
        
            // b) compare it with asciiIn equivalent
            doTestCompare(source.toString(),www+TestData.asciiIn[i]+com,true);
        
            // c) compare it with unicodeIn not equivalent
            if(i==0){
                doTestCompare(source.toString(), uni1.toString(), false);
            }else{
                doTestCompare(source.toString(),uni0.toString(), false);
            }
            // d) compare it with asciiIn not equivalent
            if(i==0){
                doTestCompare(source.toString(),ascii1.toString(), false);
            }else{
                doTestCompare(source.toString(),ascii0.toString(), false);
            }

        }

    }
    */
    
    private static final int loopCount = 100;
    private static final int maxCharCount = 15;
   // private static final int maxCodePoint = 0x10ffff;
    private Random random = null;
    
    /**
     * Return a random integer i where 0 <= i < n.
     * A special function that gets random codepoints from planes 0,1,2 and 14
     */
    private int rand_uni()
    {
       int retVal = (int)(random.nextLong()& 0x3FFFF);
       if(retVal >= 0x30000){
           retVal+=0xB0000;
       }
       return retVal;
    }

    private int randi(int n){
        return (random.nextInt(0x7fff) % (n+1));
    }

    private StringBuffer getTestSource(StringBuffer fillIn) {
        // use uniform seed value from the framework
        if(random==null){
            random = createRandom();
        }
        int i = 0;
        int charCount = (randi(maxCharCount) + 1);
        while (i <charCount ) {
            int codepoint = rand_uni();
            if(codepoint == 0x0000){
                continue;
            }
            UTF16.append(fillIn, codepoint);
            i++;
        }
        return fillIn;
       
    }
    public void MonkeyTest() throws Exception{
         StringBuffer source = new StringBuffer();
         /* do the monkey test   */       
         for(int i=0; i<loopCount; i++){
             source.setLength(0);
             getTestSource(source);
             doTestCompareReferenceImpl(source);
         }
         
         // test string with embedded null  
         source.append( "\\u0000\\u2109\\u3E1B\\U000E65CA\\U0001CAC5" );
                           
         source = new StringBuffer(Utility.unescape(source.toString()));
         doTestCompareReferenceImpl(source);
         
         //StringBuffer src = new StringBuffer(Utility.unescape("\\uDEE8\\U000E228C\\U0002EE8E\\U000E6350\\U00024DD9\u4049\\U000E0DE4\\U000E448C\\U0001869B\\U000E3380\\U00016A8E\\U000172D5\\U0001C408\\U000E9FB5"));
         //doTestCompareReferenceImpl(src);
         
         //test deletion of code points
         source = new StringBuffer(Utility.unescape("\\u043f\\u00AD\\u034f\\u043e\\u0447\\u0435\\u043c\\u0443\\u0436\\u0435\\u043e\\u043d\\u0438\\u043d\\u0435\\u0433\\u043e\\u0432\\u043e\\u0440\\u044f\\u0442\\u043f\\u043e\\u0440\\u0443\\u0441\\u0441\\u043a\\u0438"));
         StringBuffer expected = new StringBuffer("xn--b1abfaaepdrnnbgefbadotcwatmq2g4l");
         doTestCompareReferenceImpl(source);
         doTestToASCII(source.toString(),expected.toString(), IDNA.DEFAULT, null);
    }
    private void doTestCompareReferenceImpl(StringBuffer src) throws Exception{
        
        StringBuffer label = src;  

        StringPrepParseException expected = null;
        StringBuffer ascii = null;
        int options = IDNA.DEFAULT;
        logln("Comparing idnaref_toASCII with uidna_toASCII for input: " + prettify(label));
        try{       
            ascii = IDNAReference.convertToASCII(label, options);
        }catch( StringPrepParseException e){
            expected = e;
            if(e.equals(unassignedException)){
                options = IDNA.ALLOW_UNASSIGNED;
                expected = null;
                try{
                    ascii = IDNAReference.convertToASCII(label, options);
                }catch( StringPrepParseException ex){
                    expected = ex;                  
                }
            }
        }
        
        doTestToASCII(label.toString(), 
                      (ascii == null) ? null : ascii.toString(),
                      options,
                      expected);

        logln("Comparing idnaref_toUnicode with uidna_toUnicode for input: " + prettify(label));
        StringBuffer uni =null;
        
        if(expected == null){
            options = IDNA.DEFAULT;
            try{
                 uni = IDNAReference.convertToUnicode(ascii, options);
            }catch( StringPrepParseException e ){
                expected = e;
                if(expected.equals(unassignedException)){
                    options = IDNA.ALLOW_UNASSIGNED;
                    expected = null;
                    try{
                        uni = IDNAReference.convertToUnicode(ascii, options);
                    }catch(StringPrepParseException ex){
                        expected = ex;
                    }
                }
            }
            doTestToUnicode(ascii.toString(),
                            (uni==null)? null : uni.toString(),
                            options,
                            expected);
        }

    }
    public void TestCompareRefImpl() throws Exception{
        
        for(int i = 0x40000 ; i< 0x10ffff; i++){
            StringBuffer src = new StringBuffer();
           
            if(isQuick()==true && i> 0x1FFFF){
                return;
            }
            if(i >= 0x30000 && i<=0xf0000){
               i+=0xB0000;
            }
            UTF16.append(src,i);
            doTestCompareReferenceImpl(src);

        }  
    }
    public void TestUnicode32Norm() {
        /*
         * test Unicode 3.2 normalization, before Public Review Issue #29
         * see cnormtst.c TestComposition()
         */
        final String strings[]={
            "\u1100\u0300\u1161\u0327",
            "\u0b47\u0300\u0b3e\u0327"
        };

        String ascii, unicode;
        StringPrepParseException ex2;
        int i;

        for(i=0; i<strings.length; ++i) {
            ex2=null;
            try {
                ascii=IDNA.convertToASCII(strings[i], 0).toString();
                unicode=IDNA.convertToUnicode(ascii, 0).toString();
            } catch(StringPrepParseException ex) {
                ex2=ex;
            }
            if(ex2==null || ex2.getMessage().indexOf("verification")<0) {
                String es= ex2==null ? "null" : ex2.toString();
                errln("string "+i+" yields "+es+" instead of VERIFICATION_ERROR");
            }
        }
    }
    public void TestJB4490(){
        String[] in = new String[]{
                "\u00F5\u00dE\u00dF\u00dD",
                "\uFB00\uFB01"
               };
        for ( int i=0; i< in.length; i++){   
            try{
                String ascii = IDNA.convertToASCII(in[i],IDNA.DEFAULT).toString();
                try{
                    String unicode = IDNA.convertToUnicode(ascii,IDNA.DEFAULT).toString();
                }catch(StringPrepParseException ex){
                    errln("Unexpected exception for convertToUnicode: " + ex.getMessage());
                }
            }catch(StringPrepParseException ex){
                errln("Unexpected exception for convertToASCII: " + ex.getMessage());
            }
        }
    }
    public void TestJB4475(){
        String[] in = new String[]{
                        "TEST",
                        "test"
                       };
        for ( int i=0; i< in.length; i++){
            
            try{
                String ascii = IDNA.convertToASCII(in[i],IDNA.DEFAULT).toString();
                if(!ascii.equals(in[i])){
                    errln("Did not get the expected string for convertToASCII. Expected: "+ in[i] +" Got: " + ascii);
                }
            }catch(StringPrepParseException ex){
                errln("Unexpected exception: " + ex.getMessage());
            }
        }
            
    }
    public void TestDebug(){     
        try{
            String src = "\u00ED4dn";
            String uni = IDNA.convertToUnicode(src,IDNA.DEFAULT).toString();
            if(!uni.equals(src)){
                errln("Did not get the expected result. Expected: "+ prettify(src) +" Got: " +uni);
            }
        }catch(StringPrepParseException ex){
            logln("Unexpected exception: " + ex.getMessage());
        }
        try{
            String ascii = IDNA.convertToASCII("\u00AD",IDNA.DEFAULT).toString();
            if(ascii!=null){
                errln("Did not get the expected exception");
            }
        }catch(StringPrepParseException ex){
            logln("Got the expected exception: " + ex.getMessage());
        }
    }
}
