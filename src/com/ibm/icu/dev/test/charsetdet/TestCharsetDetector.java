/**
 *******************************************************************************
 * Copyright (C) 2005, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.charsetdet;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.impl.UTF32;
import com.ibm.icu.text.*;
import com.ibm.icu.util.VersionInfo;

import javax.xml.parsers.*;
import org.w3c.dom.*;


/**
 * @author andy
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestCharsetDetector extends TestFmwk
{
    
    /**
     * Constructor
     */
    public TestCharsetDetector()
    {
    }

    public static void main(String[] args) {
        try
        {
            TestCharsetDetector test = new TestCharsetDetector();
            test.run(args);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void CheckAssert(boolean exp) {
        if (exp == false) {
            String msg;
            try {
                throw new Exception();
            }
            catch (Exception e) {
                StackTraceElement failPoint = e.getStackTrace()[1];
                msg = "Test failure in file " + failPoint.getFileName() +
                             " at line " + failPoint.getLineNumber();
            }
            errln(msg);
        }
        
    }
    
    private String stringFromReader(Reader reader)
    {
        StringBuffer sb = new StringBuffer();
        char[] buffer   = new char[1024];
        int bytesRead   = 0;
        
        try {
            while ((bytesRead = reader.read(buffer, 0, 1024)) >= 0) {
                sb.append(buffer, 0, bytesRead);
            }
            
            return sb.toString();
        } catch (Exception e) {
            errln("stringFromReader() failed: " + e.toString());
            return null;
        }
    }
    
    private void checkMatch(CharsetDetector det, String testString, String encoding, String language, String id) throws Exception
    {
        CharsetMatch m = det.detect();
        String decoded;
        
        if (! m.getName().equals(encoding)) {
            errln(id + ": encoding detection failure - expected " + encoding + ", got " + m.getName());
            return;
        }
        
        if (! (language == null || m.getLanguage().equals(language))) {
            errln(id + ", " + encoding + ": language detection failure - expected " + language + ", got " + m.getLanguage());
        }
        
        if (encoding.startsWith("UTF-32")) {
            return;
        }
        
        decoded = m.getString();
        
        if (! testString.equals(decoded)) {
            errln(id + ", " + encoding + ": getString() didn't return the original string!");
        }
        
        decoded = stringFromReader(m.getReader());
        
        if (! testString.equals(decoded)) {
            errln(id + ", " + encoding + ": getReader() didn't yield the original string!");
        }
    }
    
    private void checkEncoding(String testString, String encoding, String id)
    {
        String enc = null, from = null, lang = null;
        String[] split = encoding.split("/");
        
        enc = split[0];
        
        if (split.length > 1) {
            lang = split[1];
        }

        if (enc.equals("ISO-2022-CN")) {
            
            // Don't test ISO-2022-CN on older runtimes.
            if (! have_ISO_2022_CN) {
                return;
            }
            
            // ISO-2022-CN only works for converting *to* Unicode,
            // we need to use x-ISO-2022-CN-GB to convert *from* unicode...
            from = "x-ISO-2022-CN-GB";
        } else {
            from = enc;
        }
        
        try {
            CharsetDetector det = new CharsetDetector();
            byte[] bytes;
            
            if (from.startsWith("UTF-32")) {
                UTF32 utf32 = UTF32.getInstance(from);
                
                bytes = utf32.toBytes(testString);
            } else {
                bytes = testString.getBytes(from);
            }
        
            det.setText(bytes);
            checkMatch(det, testString, enc, lang, id);
            
            det.setText(new ByteArrayInputStream(bytes));
            checkMatch(det, testString, enc, lang, id);
         } catch (Exception e) {
            errln(id + ": " + e.toString());
        }
        
    }
    
    public void TestConstruction() {
        int i;
        CharsetDetector  det = new CharsetDetector();
        
        String [] charsetNames = CharsetDetector.getAllDetectableCharsets();
        CheckAssert(charsetNames.length != 0);
        for (i=0; i<charsetNames.length; i++) {
            CheckAssert(charsetNames[i].equals("") == false); 
            // System.out.println("\"" + charsetNames[i] + "\"");
        }
     }

    public void TestInputFilter() throws Exception
    {
        String s = "<a> <lot> <of> <English> <inside> <the> <markup> Un tr\u00E8s petit peu de Fran\u00E7ais. <to> <confuse> <the> <detector>";
        byte[] bytes = s.getBytes("ISO-8859-1");
        CharsetDetector det = new CharsetDetector();
        CharsetMatch m;
        
        det.enableInputFilter(true);
        if (!det.inputFilterEnabled()){
            errln("input filter should be enabled");
        }
        
        det.setText(bytes);
        m = det.detect();
        
        if (! m.getLanguage().equals("fr")) {
            errln("input filter did not strip markup!");
        }
        
        det.enableInputFilter(false);
        det.setText(bytes);
        m = det.detect();
        
        if (! m.getLanguage().equals("en")) {
            errln("unfiltered input did not detect as English!");
        }
    }
    
    public void TestUTF8() throws Exception {
        
        String  s = "This is a string with some non-ascii characters that will " +
                    "be converted to UTF-8, then shoved through the detection process.  " +
                    "\u0391\u0392\u0393\u0394\u0395" +
                    "Sure would be nice if our source could contain Unicode directly!";
        byte [] bytes = s.getBytes("UTF-8");
        CharsetDetector det = new CharsetDetector();
        String retrievedS;
        Reader reader;
        
        retrievedS = det.getString(bytes, "UTF-8");
        CheckAssert(s.equals(retrievedS));
        
        reader = det.getReader(new ByteArrayInputStream(bytes), "UTF-8");
        CheckAssert(s.equals(stringFromReader(reader)));
        det.setDeclaredEncoding("UTF-8");	// Jitterbug 4451, for coverage
    }
    
    public void TestUTF16() throws Exception
    {
        String source = 
                "u0623\u0648\u0631\u0648\u0628\u0627, \u0628\u0631\u0645\u062c\u064a\u0627\u062a " +
                "\u0627\u0644\u062d\u0627\u0633\u0648\u0628 \u002b\u0020\u0627\u0646\u062a\u0631\u0646\u064a\u062a";
        
        byte[] beBytes = source.getBytes("UnicodeBig");
        byte[] leBytes = source.getBytes("UnicodeLittle");
        CharsetDetector det = new CharsetDetector();
        CharsetMatch m;
        
        det.setText(beBytes);
        m = det.detect();
        
        if (! m.getName().equals("UTF-16BE")) {
            errln("Encoding detection failure: expected UTF-16BE, got " + m.getName());
        }
        
        det.setText(leBytes);
        m = det.detect();
        
        if (! m.getName().equals("UTF-16LE")) {
            errln("Encoding detection failure: expected UTF-16LE, got " + m.getName());
        }

        // Jitterbug 4451, for coverage
        int confidence = m.getConfidence(); 
        if(confidence != 100){
            errln("Did not get the expected confidence level " + confidence);
        }
        int matchType = m.getMatchType();
        if(matchType != 0){
            errln("Did not get the expected matchType level " + matchType);
        }
}
    
    public void TestDetection()
    {
        //
        //  Open and read the test data file.
        //
        InputStreamReader isr = null;
        
        try {
            InputStream is = TestCharsetDetector.class.getResourceAsStream("CharsetDetectionTests.xml");
            if (is == null) {
                errln("Could not open test data file CharsetDetectionTests.xml");
                return;
            }
            
            isr = new InputStreamReader(is, "UTF-8"); 

            // Set up an xml parser.
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            
            factory.setIgnoringComments(true);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            // Parse the xml content from the test case file.
            Document doc = builder.parse(is, null);
            Element root = doc.getDocumentElement();
            
            NodeList testCases = root.getElementsByTagName("test-case");
            
            // Process each test case
            for (int n = 0; n < testCases.getLength(); n += 1) {
                Node testCase = testCases.item(n);
                NamedNodeMap attrs = testCase.getAttributes();
                NodeList testData  = testCase.getChildNodes();
                StringBuffer testText = new StringBuffer();
                String id = attrs.getNamedItem("id").getNodeValue();
                String encodings = attrs.getNamedItem("encodings").getNodeValue();
                
                // Collect the test case text.
                for (int t = 0; t < testData.getLength(); t += 1) {
                    Node textNode = testData.item(t);
                    
                    testText.append(textNode.getNodeValue());                    
                }
                
                // Process test text with each encoding / language pair.
                String testString = testText.toString();
                String[] encodingList = encodings.split(" ");
                
                for (int e = 0; e < encodingList.length; e += 1) {
                    checkEncoding(testString, encodingList[e], id);
                }
            }
            
        } catch (Exception e) {
            errln("exception while processing test cases: " + e.toString());
        }
    }
    
    // Before Java 1.5, we cannot convert from Unicode to ISO-2022-CN, so checkEncoding() can't test it...
    private boolean have_ISO_2022_CN = VersionInfo.javaVersion().compareTo(VersionInfo.getInstance(1, 5)) >= 0;
}
