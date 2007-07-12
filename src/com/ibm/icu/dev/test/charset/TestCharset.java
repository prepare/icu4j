/**
*******************************************************************************
* Copyright (C) 2006-2007, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
*******************************************************************************
*/

package com.ibm.icu.dev.test.charset;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.charset.spi.CharsetProvider;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.SortedMap;

import com.ibm.icu.charset.CharsetEncoderICU;
import com.ibm.icu.charset.CharsetICU;
import com.ibm.icu.charset.CharsetProviderICU;
import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.text.UTF16;

public class TestCharset extends TestFmwk {
    private String m_encoding = "UTF-16";
    CharsetDecoder m_decoder = null;
    CharsetEncoder m_encoder = null;
    Charset m_charset =null;
    static final String unistr = "abcd\ud800\udc00\u1234\u00a5\u3000\r\n";
    static final byte[] byteStr ={   
            (byte) 0x00,(byte) 'a',
            (byte) 0x00,(byte) 'b',
            (byte) 0x00,(byte) 'c',
            (byte) 0x00,(byte) 'd',
            (byte) 0xd8,(byte) 0x00,
            (byte) 0xdc,(byte) 0x00,
            (byte) 0x12,(byte) 0x34,
            (byte) 0x00,(byte) 0xa5,
            (byte) 0x30,(byte) 0x00,
            (byte) 0x00,(byte) 0x0d,
            (byte) 0x00,(byte) 0x0a };
    static final byte[] expectedByteStr ={
        (byte) 0xfe,(byte) 0xff,
        (byte) 0x00,(byte) 'a',
        (byte) 0x00,(byte) 'b',
        (byte) 0x00,(byte) 'c',
        (byte) 0x00,(byte) 'd',
        (byte) 0xd8,(byte) 0x00,
        (byte) 0xdc,(byte) 0x00,
        (byte) 0x12,(byte) 0x34,
        (byte) 0x00,(byte) 0xa5,
        (byte) 0x30,(byte) 0x00,
        (byte) 0x00,(byte) 0x0d,
        (byte) 0x00,(byte) 0x0a };
    
    protected void init(){
        try{
            CharsetProviderICU provider = new CharsetProviderICU();
            //Charset charset = CharsetICU.forName(encoding);
            m_charset = provider.charsetForName(m_encoding);
            m_decoder = (CharsetDecoder) m_charset.newDecoder();
            m_encoder = (CharsetEncoder) m_charset.newEncoder();   
        }catch(MissingResourceException ex){
            warnln("Could not load charset data");
        }
    }
    
    public static void main(String[] args) throws Exception {
        new TestCharset().run(args);
    }
    public void TestUTF16Converter(){
        CharsetProvider icu = new CharsetProviderICU();
        Charset cs1 = icu.charsetForName("UTF-16BE");
        CharsetEncoder e1 = cs1.newEncoder();
        CharsetDecoder d1 = cs1.newDecoder();
        
        Charset cs2 = icu.charsetForName("UTF-16LE");
        CharsetEncoder e2 = cs2.newEncoder();
        CharsetDecoder d2 = cs2.newDecoder();
        
        for(int i=0x0000; i<0x10FFFF; i+=0xFF){
            CharBuffer us = CharBuffer.allocate(0xFF*2);
            ByteBuffer bs1 = ByteBuffer.allocate(0xFF*8);
            ByteBuffer bs2 = ByteBuffer.allocate(0xFF*8);
            for(int j=0;j<0xFF; j++){
                int c = i+j;
              
                if((c>=0xd800&&c<=0xdFFF)||c>0x10FFFF){
                    continue;
                }

                if(c>0xFFFF){
                    char lead = UTF16.getLeadSurrogate(c);
                    char trail = UTF16.getTrailSurrogate(c);
                    if(!UTF16.isLeadSurrogate(lead)){
                        errln("lead is not lead!"+lead+" for cp: \\U"+Integer.toHexString(c));
                        continue;
                    }
                    if(!UTF16.isTrailSurrogate(trail)){
                        errln("trail is not trail!"+trail);
                        continue;
                    }
                    us.put(lead);
                    us.put(trail);
                    bs1.put((byte)(lead>>8));
                    bs1.put((byte)(lead&0xFF));
                    bs1.put((byte)(trail>>8));
                    bs1.put((byte)(trail&0xFF));
                    
                    bs2.put((byte)(lead&0xFF));
                    bs2.put((byte)(lead>>8));
                    bs2.put((byte)(trail&0xFF));
                    bs2.put((byte)(trail>>8));
                }else{

                    if(c<0xFF){
                        bs1.put((byte)0x00);
                        bs1.put((byte)(c));
                        bs2.put((byte)(c));
                        bs2.put((byte)0x00);
                    }else{
                        bs1.put((byte)(c>>8));
                        bs1.put((byte)(c&0xFF));
                        
                        bs2.put((byte)(c&0xFF));
                        bs2.put((byte)(c>>8));
                    }
                    us.put((char)c);
                }
            }
            
            
            us.limit(us.position());
            us.position(0);
            if(us.length()==0){
                continue;
            }
            

            bs1.limit(bs1.position());
            bs1.position(0);
            ByteBuffer newBS = ByteBuffer.allocate(bs1.capacity());
            //newBS.put((byte)0xFE);
            //newBS.put((byte)0xFF);
            newBS.put(bs1);    
            bs1.position(0);
            smBufDecode(d1, "UTF-16", bs1, us);
            smBufEncode(e1, "UTF-16", us, newBS);
            
            bs2.limit(bs2.position());
            bs2.position(0);
            newBS.clear();
            //newBS.put((byte)0xFF);
            //newBS.put((byte)0xFE);
            newBS.put(bs2);     
            bs2.position(0);
            smBufDecode(d2, "UTF16-LE", bs2, us);
            smBufEncode(e2, "UTF-16LE", us, newBS);
            
        }
        
    }
    public void TestUTF32Converter(){
        CharsetProvider icu = new CharsetProviderICU();
        Charset cs1 = icu.charsetForName("UTF-32BE");
        CharsetEncoder e1 = cs1.newEncoder();
        CharsetDecoder d1 = cs1.newDecoder();
        
        Charset cs2 = icu.charsetForName("UTF-32LE");
        CharsetEncoder e2 = cs2.newEncoder();
        CharsetDecoder d2 = cs2.newDecoder();
        
        for(int i=0x000; i<0x10FFFF; i+=0xFF){
            CharBuffer us = CharBuffer.allocate(0xFF*2);
            ByteBuffer bs1 = ByteBuffer.allocate(0xFF*8);
            ByteBuffer bs2 = ByteBuffer.allocate(0xFF*8);
            for(int j=0;j<0xFF; j++){
                int c = i+j;
              
                if((c>=0xd800&&c<=0xdFFF)||c>0x10FFFF){
                    continue;
                }

                if(c>0xFFFF){
                    char lead = UTF16.getLeadSurrogate(c);
                    char trail = UTF16.getTrailSurrogate(c);

                    us.put(lead);
                    us.put(trail);
                }else{
                    us.put((char)c);
                }
                bs1.put((byte) (c >>> 24));
                bs1.put((byte) (c >>> 16)); 
                bs1.put((byte) (c >>> 8)); 
                bs1.put((byte) (c & 0xFF));       
                                
                bs2.put((byte) (c & 0xFF));  
                bs2.put((byte) (c >>> 8));
                bs2.put((byte) (c >>> 16)); 
                bs2.put((byte) (c >>> 24));
            }
            bs1.limit(bs1.position());
            bs1.position(0);
            bs2.limit(bs2.position());
            bs2.position(0);
            us.limit(us.position());
            us.position(0);
            if(us.length()==0){
                continue;
            }
             

            ByteBuffer newBS = ByteBuffer.allocate(bs1.capacity());
            
            newBS.put((byte)0x00);
            newBS.put((byte)0x00);
            newBS.put((byte)0xFE);
            newBS.put((byte)0xFF);
            
            newBS.put(bs1);
            bs1.position(0);
            smBufDecode(d1, "UTF-32", bs1, us);
            smBufEncode(e1, "UTF-32", us, newBS);
            
            
            newBS.clear();
            
            newBS.put((byte)0xFF);
            newBS.put((byte)0xFE);
            newBS.put((byte)0x00);
            newBS.put((byte)0x00);
            
            newBS.put(bs2);    
            bs2.position(0);
            smBufDecode(d2, "UTF-32LE", bs2, us);
            smBufEncode(e2, "UTF-32LE", us, newBS);

        }
        
    }
    public void TestASCIIConverter() {
        runASCIIBasedConverterTest("ASCII", 0x80);
    }    
    public void Test88591Converter() {
        runASCIIBasedConverterTest("iso-8859-1", 0x100);
    }
    public void runASCIIBasedConverterTest(String converter, int limit){
        CharsetProvider icu = new CharsetProviderICU();
        Charset icuChar = icu.charsetForName(converter);
        CharsetEncoder encoder = icuChar.newEncoder();
        CharsetDecoder decoder = icuChar.newDecoder();

        /* test with and without array-backed buffers */ 
        
        byte[] bytes = new byte[0x10000];
        char[] chars = new char[0x10000];
        for (int j = 0; j <= 0xffff; j++) {
            bytes[j] = (byte) j;
            chars[j] = (char) j;
        }

        boolean fail = false;
        boolean arrays = false;
        boolean decoding = false;
        int i;
        
        // 0 thru limit - 1
        ByteBuffer bs = ByteBuffer.wrap(bytes, 0, limit);
        CharBuffer us = CharBuffer.wrap(chars, 0, limit);
        smBufDecode(decoder, converter, bs, us, true);
        smBufDecode(decoder, converter, bs, us, false);
        smBufEncode(encoder, converter, us, bs, true);
        smBufEncode(encoder, converter, us, bs, false);
        for (i = 0; i < limit; i++) {
            bs = ByteBuffer.wrap(bytes, i, 1).slice();
            us = CharBuffer.wrap(chars, i, 1).slice();
            try {
                decoding = true;
                arrays = true;
                smBufDecode(decoder, converter, bs, us, true, false, true);
                
                decoding = true;
                arrays = false;
                smBufDecode(decoder, converter, bs, us, true, false, false);
                
                decoding = false;
                arrays = true;
                smBufEncode(encoder, converter, us, bs, true, false, true);
                
                decoding = false;
                arrays = false;
                smBufEncode(encoder, converter, us, bs, true, false, false);
                
            } catch (Exception ex) {
                errln("Failed to fail to " + (decoding ? "decode" : "encode") + " 0x"
                        + Integer.toHexString(i) + (arrays ? " with arrays" : " without arrays") + " in " + converter);
                return;
            }
        }
        
        // decode limit thru 255
        for (i = limit; i <= 0xff; i++) {
            bs = ByteBuffer.wrap(bytes, i, 1).slice();
            us = CharBuffer.wrap(chars, i, 1).slice();
            try {
                smBufDecode(decoder, converter, bs, us, true, false, true);
                fail = true;
                arrays = true;
                break;
            } catch (Exception ex) {
            }
            try {
                smBufDecode(decoder, converter, bs, us, true, false, false);
                fail = true;
                arrays = false;
                break;
            } catch (Exception ex) {
            }
        }
        if (fail) {
            errln("Failed to fail to decode 0x" + Integer.toHexString(i)
                    + (arrays ? " with arrays" : " without arrays") + " in " + converter);
            return;
        }
        
        // encode limit thru 0xffff, skipping through much of the 1ff to feff range to save
        // time (it would take too much time to test every possible case)
        for (i = limit; i <= 0xffff; i = ((i>=0x1ff && i<0xfeff) ? i+0xfd : i+1)) {
            bs = ByteBuffer.wrap(bytes, i, 1).slice();
            us = CharBuffer.wrap(chars, i, 1).slice();
            try {
                smBufEncode(encoder, converter, us, bs, true, false, true);
                fail = true;
                arrays = true;
                break;
            } catch (Exception ex) {
            }
            try {
                smBufEncode(encoder, converter, us, bs, true, false, false);
                fail = true;
                arrays = false;
                break;
            } catch (Exception ex) {
            }
        }
        if (fail) {
            errln("Failed to fail to encode 0x" + Integer.toHexString(i)
                    + (arrays ? " with arrays" : " without arrays") + " in " + converter);
            return;
        }
        
        // test overflow / underflow edge cases
        outer: for (int n = 1; n <= 3; n++) {
            for (int m = 0; m < n; m++) {
                // expecting underflow
                try {
                    bs = ByteBuffer.wrap(bytes, 'a', m).slice();
                    us = CharBuffer.wrap(chars, 'a', m).slice();
                    smBufDecode(decoder, converter, bs, us, true, false, true);
                    smBufDecode(decoder, converter, bs, us, true, false, false);
                    smBufEncode(encoder, converter, us, bs, true, false, true);
                    smBufEncode(encoder, converter, us, bs, true, false, false);
                    bs = ByteBuffer.wrap(bytes, 'a', m).slice();
                    us = CharBuffer.wrap(chars, 'a', n).slice();
                    smBufDecode(decoder, converter, bs, us, true, false, true, m);
                    smBufDecode(decoder, converter, bs, us, true, false, false, m);
                    bs = ByteBuffer.wrap(bytes, 'a', n).slice();
                    us = CharBuffer.wrap(chars, 'a', m).slice();
                    smBufEncode(encoder, converter, us, bs, true, false, true, m);
                    smBufEncode(encoder, converter, us, bs, true, false, false, m);
                    bs = ByteBuffer.wrap(bytes, 'a', n).slice();
                    us = CharBuffer.wrap(chars, 'a', n).slice();
                    smBufDecode(decoder, converter, bs, us, true, false, true);
                    smBufDecode(decoder, converter, bs, us, true, false, false);
                    smBufEncode(encoder, converter, us, bs, true, false, true);
                    smBufEncode(encoder, converter, us, bs, true, false, false);
                } catch (Exception ex) {
                    fail = true;
                    break outer;
                }
                
                // expecting overflow
                try {
                    bs = ByteBuffer.wrap(bytes, 'a', n).slice();
                    us = CharBuffer.wrap(chars, 'a', m).slice();
                    smBufDecode(decoder, converter, bs, us, true, false, true);
                    fail = true;
                    break;
                } catch (Exception ex) {
                    if (!(ex instanceof BufferOverflowException)) {
                        fail = true;
                        break outer;
                    }
                }
                try {
                    bs = ByteBuffer.wrap(bytes, 'a', n).slice();
                    us = CharBuffer.wrap(chars, 'a', m).slice();
                    smBufDecode(decoder, converter, bs, us, true, false, false);
                    fail = true;
                } catch (Exception ex) {
                    if (!(ex instanceof BufferOverflowException)) {
                        fail = true;
                        break outer;
                    }
                }
                try {
                    bs = ByteBuffer.wrap(bytes, 'a', m).slice();
                    us = CharBuffer.wrap(chars, 'a', n).slice();
                    smBufEncode(encoder, converter, us, bs, true, false, true);
                    fail = true;
                } catch (Exception ex) {
                    if (!(ex instanceof BufferOverflowException)) {
                        fail = true;
                        break outer;
                    }
                }
                try {
                    bs = ByteBuffer.wrap(bytes, 'a', m).slice();
                    us = CharBuffer.wrap(chars, 'a', n).slice();
                    smBufEncode(encoder, converter, us, bs, true, false, false);
                    fail = true;
                } catch (Exception ex) {
                    if (!(ex instanceof BufferOverflowException)) {
                        fail = true;
                        break outer;
                    }
                }
            }
        }
        if (fail) {
            errln("Incorrect result in " + converter + " for underflow / overflow edge cases");
            return;
        }
        
        // test surrogate combinations in encoding
        String lead = "" + (char)0xd888;
        String trail = "" + (char)0xdc88;
        String norm = "a";
        String end = "";
        bs = ByteBuffer.wrap(new byte[] { 0 });
        String[] input = new String[] { //
                lead + lead,   // malf(1)
                lead + trail,  // unmap(2)
                lead + norm,   // malf(1)
                lead + end,    // malf(1)
                trail + lead,  // unmap(1)
                trail + trail, // unmap(1)
                trail + norm,  // unmap(1)
                trail + end,   // unmap(1)
        };
        CoderResult[] result = new CoderResult[] {
                CoderResult.malformedForLength(1),
                CoderResult.unmappableForLength(2),
                CoderResult.malformedForLength(1),
                CoderResult.malformedForLength(1),
                CoderResult.unmappableForLength(1),
                CoderResult.unmappableForLength(1),
                CoderResult.unmappableForLength(1),
                CoderResult.unmappableForLength(1),
        };
        for (int index = 0; index < input.length; index++) {
            CoderResult cr = encoder.encode(CharBuffer.wrap(input[index]), bs, true);
            bs.rewind();
            encoder.reset();

            // if cr != results[x]
            if (!((cr.isUnderflow() && result[index].isUnderflow())
                    || (cr.isOverflow() && result[index].isOverflow())
                    || (cr.isMalformed() && result[index].isMalformed())
                    || (cr.isUnmappable() && result[index].isUnmappable()))
                    || (cr.isError() && cr.length() != result[index].length())) {
                errln("Incorrect result in " + converter + " for \"" + input[index] + "\"");
            }
        }
    }
    

    public void TestAPISemantics(/*String encoding*/) 
                throws Exception {
        int rc;
        ByteBuffer byes = ByteBuffer.wrap(byteStr);
        CharBuffer uniVal = CharBuffer.wrap(unistr);
        ByteBuffer expected = ByteBuffer.wrap(expectedByteStr);
        
        rc = 0;
        if(m_decoder==null){
            warnln("Could not load decoder.");
            return;
        }
        m_decoder.reset();
        /* Convert the whole buffer to Unicode */
        try {
            CharBuffer chars = CharBuffer.allocate(unistr.length());
            CoderResult result = m_decoder.decode(byes, chars, false);

            if (result.isError()) {
                errln("ToChars encountered Error");
                rc = 1;
            }
            if (result.isOverflow()) {
                errln("ToChars encountered overflow exception");
                rc = 1;
            }
            if (!equals(chars, unistr)) {
                errln("ToChars does not match");
                printchars(chars);
                errln("Expected : ");
                printchars(unistr);
                rc = 2;
            }

        } catch (Exception e) {
            errln("ToChars - exception in buffer");
            rc = 5;
        }

        /* Convert single bytes to Unicode */
        try {
            CharBuffer chars = CharBuffer.allocate(unistr.length());
            ByteBuffer b = ByteBuffer.wrap(byteStr);
            m_decoder.reset();
            CoderResult result=null;
            for (int i = 1; i <= byteStr.length; i++) {
                b.limit(i);
                result = m_decoder.decode(b, chars, false);
                if(result.isOverflow()){
                    errln("ToChars single threw an overflow exception");
                }
                if (result.isError()) {
                    errln("ToChars single the result is an error "+result.toString());
                } 
            }
            if (unistr.length() != (chars.limit())) {
                errln("ToChars single len does not match");
                rc = 3;
            }
            if (!equals(chars, unistr)) {
                errln("ToChars single does not match");
                printchars(chars);
                rc = 4;
            }
        } catch (Exception e) {
            errln("ToChars - exception in single");
            //e.printStackTrace();
            rc = 6;
        }

        /* Convert the buffer one at a time to Unicode */
        try {
            CharBuffer chars = CharBuffer.allocate(unistr.length());
            m_decoder.reset();
            byes.rewind();
            for (int i = 1; i <= byteStr.length; i++) {
                byes.limit(i);
                CoderResult result = m_decoder.decode(byes, chars, false);
                if (result.isError()) {
                    errln("Error while decoding: "+result.toString());
                }
                if(result.isOverflow()){
                    errln("ToChars Simple threw an overflow exception");
                }
            }
            if (chars.limit() != unistr.length()) {
                errln("ToChars Simple buffer len does not match");
                rc = 7;
            }
            if (!equals(chars, unistr)) {
                errln("ToChars Simple buffer does not match");
                printchars(chars);
                err(" Expected : ");
                printchars(unistr);
                rc = 8;
            }
        } catch (Exception e) {
            errln("ToChars - exception in single buffer");
            //e.printStackTrace(System.err);
            rc = 9;
        }
        if (rc != 0) {
            errln("Test Simple ToChars for encoding : FAILED");
        }

        rc = 0;
        /* Convert the whole buffer from unicode */
        try {
            ByteBuffer bytes = ByteBuffer.allocate(expectedByteStr.length);
            m_encoder.reset();
            CoderResult result = m_encoder.encode(uniVal, bytes, false);
            if (result.isError()) {
                errln("FromChars reported error: " + result.toString());
                rc = 1;
            }
            if(result.isOverflow()){
                errln("FromChars threw an overflow exception");
            }
            bytes.position(0);
            if (!bytes.equals(expected)) {
                errln("FromChars does not match");
                printbytes(bytes);
                rc = 2;
            }
        } catch (Exception e) {
            errln("FromChars - exception in buffer");
            //e.printStackTrace(System.err);
            rc = 5;
        }

        /* Convert the buffer one char at a time to unicode */
        try {
            ByteBuffer bytes = ByteBuffer.allocate(expectedByteStr.length);
            CharBuffer c = CharBuffer.wrap(unistr);
            m_encoder.reset();
            CoderResult result= null;
            for (int i = 1; i <= unistr.length(); i++) {
                c.limit(i);
                result = m_encoder.encode(c, bytes, false);
                if(result.isOverflow()){
                    errln("FromChars single threw an overflow exception");
                }
                if(result.isError()){
                    errln("FromChars single threw an error: "+ result.toString());
                }
            }
            if (expectedByteStr.length != bytes.limit()) {
                errln("FromChars single len does not match");
                rc = 3;
            }

            bytes.position(0);
            if (!bytes.equals(expected)) {
                errln("FromChars single does not match");
                printbytes(bytes);
                rc = 4;
            }

        } catch (Exception e) {
            errln("FromChars - exception in single");
            //e.printStackTrace(System.err);
            rc = 6;
        }

        /* Convert one char at a time to unicode */
        try {
            ByteBuffer bytes = ByteBuffer.allocate(expectedByteStr.length);
            m_encoder.reset();
            char[] temp = unistr.toCharArray();
            CoderResult result=null;
            for (int i = 0; i <= temp.length; i++) {
                uniVal.limit(i);
                result = m_encoder.encode(uniVal, bytes, false);
                if(result.isOverflow()){
                    errln("FromChars simple threw an overflow exception");
                }
                if(result.isError()){
                    errln("FromChars simple threw an error: "+ result.toString());
                }
            }
            if (bytes.limit() != expectedByteStr.length) {
                errln("FromChars Simple len does not match");
                rc = 7;
            }
            if (!bytes.equals(byes)) {
                errln("FromChars Simple does not match");
                printbytes(bytes);
                rc = 8;
            }
        } catch (Exception e) {
            errln("FromChars - exception in single buffer");
            //e.printStackTrace(System.err);
            rc = 9;
        }
        if (rc != 0) {
            errln("Test Simple FromChars " + m_encoding + " --FAILED");
        }
    }

    void printchars(CharBuffer buf) {
        int i;
        char[] chars = new char[buf.limit()];
        //save the current position
        int pos = buf.position();
        buf.position(0);
        buf.get(chars);
        //reset to old position
        buf.position(pos);
        for (i = 0; i < chars.length; i++) {
            err(hex(chars[i]) + " ");
        }
        errln("");
    }
    void printchars(String str) {
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            err(hex(chars[i]) + " ");
        }
        errln("");
    }
    void printbytes(ByteBuffer buf) {
        int i;
        byte[] bytes = new byte[buf.limit()];
        //save the current position
        int pos = buf.position();
        buf.position(0);
        buf.get(bytes);
        //reset to old position
        buf.position(pos);
        for (i = 0; i < bytes.length; i++) {
            System.out.print(hex(bytes[i]) + " ");
        }
        errln("");
    }

    public boolean equals(CharBuffer buf, String str) {
        return equals(buf, str.toCharArray());
    }
    public boolean equals(CharBuffer buf, CharBuffer str) {
        if (buf.limit() != str.limit())
            return false;
        int limit = buf.limit();
        for (int i = 0; i < limit; i++)
            if (buf.get(i) != str.get(i))
                return false;
        return true;
    }
    public boolean equals(CharBuffer buf, CharBuffer str, int limit) {
        if (limit > buf.limit() || limit > str.limit())
            return false;
        for (int i = 0; i < limit; i++)
            if (buf.get(i) != str.get(i))
                return false;
        return true;
    }
    public boolean equals(CharBuffer buf, char[] compareTo) {
        char[] chars = new char[buf.limit()];
        //save the current position
        int pos = buf.position();
        buf.position(0);
        buf.get(chars);
        //reset to old position
        buf.position(pos);
        return equals(chars, compareTo);
    }

    public boolean equals(char[] chars, char[] compareTo) {
        if (chars.length != compareTo.length) {
            errln(
                "Length does not match chars: "
                    + chars.length
                    + " compareTo: "
                    + compareTo.length);
            return false;
        } else {
            boolean result = true;
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] != compareTo[i]) {    
                    logln(
                        "Got: "
                            + hex(chars[i])
                            + " Expected: "
                            + hex(compareTo[i])
                            + " At: "
                            + i);
                    result = false;
                }
            }
            return result;
        }
    }

    public boolean equals(ByteBuffer buf, byte[] compareTo) {
        byte[] chars = new byte[buf.limit()];
        //save the current position
        int pos = buf.position();
        buf.position(0);
        buf.get(chars);
        //reset to old position
        buf.position(pos);
        return equals(chars, compareTo);
    }
    public boolean equals(ByteBuffer buf, ByteBuffer compareTo) {
        if (buf.limit() != compareTo.limit())
            return false;
        int limit = buf.limit();
        for (int i = 0; i < limit; i++)
            if (buf.get(i) != compareTo.get(i))
                return false;
        return true;
    }
    public boolean equals(ByteBuffer buf, ByteBuffer compareTo, int limit) {
        if (limit > buf.limit() || limit > compareTo.limit())
            return false;
        for (int i = 0; i < limit; i++)
            if (buf.get(i) != compareTo.get(i))
                return false;
        return true;
    }
    public boolean equals(byte[] chars, byte[] compareTo) {
        if (false/*chars.length != compareTo.length*/) {
            errln(
                "Length does not match chars: "
                    + chars.length
                    + " compareTo: "
                    + compareTo.length);
            return false;
        } else {
            boolean result = true;
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] != compareTo[i]) {
                    logln(
                        "Got: "
                            + hex(chars[i])
                            + " Expected: "
                            + hex(compareTo[i])
                            + " At: "
                            + i);
                    result = false;
                }
            }
            return result;
        }
    }

//  TODO
  /*
    public void TestCallback(String encoding) throws Exception {
        
        byte[] gbSource =
            {
                (byte) 0x81,
                (byte) 0x36,
                (byte) 0xDE,
                (byte) 0x36,
                (byte) 0x81,
                (byte) 0x36,
                (byte) 0xDE,
                (byte) 0x37,
                (byte) 0x81,
                (byte) 0x36,
                (byte) 0xDE,
                (byte) 0x38,
                (byte) 0xe3,
                (byte) 0x32,
                (byte) 0x9a,
                (byte) 0x36 };

        char[] subChars = { 'P', 'I' };

        decoder.reset();

        decoder.replaceWith(new String(subChars));
        ByteBuffer mySource = ByteBuffer.wrap(gbSource);
        CharBuffer myTarget = CharBuffer.allocate(5);

        decoder.decode(mySource, myTarget, true);
        char[] expectedResult =
            { '\u22A6', '\u22A7', '\u22A8', '\u0050', '\u0049', };

        if (!equals(myTarget, new String(expectedResult))) {
            errln("Test callback GB18030 to Unicode : FAILED");
        }
        
    }
*/
    public void TestCanConvert(/*String encoding*/)throws Exception {
        char[] mySource = { 
            '\ud800', '\udc00',/*surrogate pair */
            '\u22A6','\u22A7','\u22A8','\u22A9','\u22AA',
            '\u22AB','\u22AC','\u22AD','\u22AE','\u22AF',
            '\u22B0','\u22B1','\u22B2','\u22B3','\u22B4',
            '\ud800','\udc00',/*surrogate pair */
            '\u22B5','\u22B6','\u22B7','\u22B8','\u22B9',
            '\u22BA','\u22BB','\u22BC','\u22BD','\u22BE' 
            };
        if(m_encoder==null){
            warnln("Could not load encoder.");
            return;
        }
        m_encoder.reset();
        if (!m_encoder.canEncode(new String(mySource))) {
            errln("Test canConvert() " + m_encoding + " failed. "+m_encoder);
        }

    }
    public void TestAvailableCharsets() {
        SortedMap map = Charset.availableCharsets();
        Set keySet = map.keySet();
        Iterator iter = keySet.iterator();
        while(iter.hasNext()){
            logln("Charset name: "+iter.next().toString());
        }
        Object[] charsets = CharsetProviderICU.getAvailableNames();
        int mapSize = map.size();
        if(mapSize < charsets.length){
            errln("Charset.availableCharsets() returned a number less than the number returned by icu. ICU: " + charsets.length
                    + " JDK: " + mapSize);
        }
        logln("Total Number of chasets = " + map.size());
	}
    
    public void TestWindows936(){
        CharsetProviderICU icu = new CharsetProviderICU();
        Charset cs = icu.charsetForName("windows-936-2000");
        String canonicalName = cs.name();
        if(!canonicalName.equals("GBK")){
            errln("Did not get the expected canonical name. Got: "+canonicalName); //get the canonical name
        }
    }
    
    public void TestICUAvailableCharsets() {
        CharsetProviderICU icu = new CharsetProviderICU();
        Object[] charsets = CharsetProviderICU.getAvailableNames();
        for(int i=0;i<charsets.length;i++){
            Charset cs = icu.charsetForName((String)charsets[i]);
            try{
                CharsetEncoder encoder = cs.newEncoder();
                if(encoder!=null){
                    logln("Creation of encoder succeeded. "+cs.toString());
                }
            }catch(Exception ex){
                errln("Could not instantiate encoder for "+charsets[i]+". Error: "+ex.toString());
            }
            try{
                CharsetDecoder decoder = cs.newDecoder();
                if(decoder!=null){
                    logln("Creation of decoder succeeded. "+cs.toString());
                }
            }catch(Exception ex){
                errln("Could not instantiate decoder for "+charsets[i]+". Error: "+ex.toString());
            }
        }
    }
    /* jitterbug 4312 */
    public void TestUnsupportedCharset(){
        CharsetProvider icu = new CharsetProviderICU();
        Charset icuChar = icu.charsetForName("impossible");
        if(icuChar != null){
            errln("ICU does not conform to the spec");
        }
    }


    public void TestEncoderCreation(){
        try{
            Charset cs = Charset.forName("GB_2312-80");
            CharsetEncoder enc = cs.newEncoder();
            if(enc!=null && (enc instanceof CharsetEncoderICU) ){
                logln("Successfully created the encoder: "+ enc);
            }else{
                errln("Error creating charset encoder.");
            }
        }catch(Exception e){
            warnln("Error creating charset encoder."+ e.toString());
           // e.printStackTrace();
        }
        try{
            Charset cs = Charset.forName("x-ibm-971_P100-1995");
            CharsetEncoder enc = cs.newEncoder();
            if(enc!=null && (enc instanceof CharsetEncoderICU) ){
                logln("Successfully created the encoder: "+ enc);
            }else{
                errln("Error creating charset encoder.");
            }
        }catch(Exception e){
            warnln("Error creating charset encoder."+ e.toString());
        }
    }
    public void TestSubBytes(){
        try{
            //create utf-8 decoder
            CharsetDecoder decoder = new CharsetProviderICU().charsetForName("utf-8").newDecoder();
    
            //create a valid byte array, which can be decoded to " buffer"
            byte[] unibytes = new byte[] { 0x0020, 0x0062, 0x0075, 0x0066, 0x0066, 0x0065, 0x0072 };
    
            ByteBuffer buffer = ByteBuffer.allocate(20);
    
            //add a evil byte to make the byte buffer be malformed input
            buffer.put((byte)0xd8);
    
            //put the valid byte array
            buffer.put(unibytes);
    
            //reset postion
            buffer.flip();  
            
            decoder.onMalformedInput(CodingErrorAction.REPLACE);
            CharBuffer out = decoder.decode(buffer);
            String expected = "\ufffd buffer";
            if(!expected.equals(new String(out.array()))){
                errln("Did not get the expected result for substitution chars. Got: "+
                       new String(out.array()) + "("+ hex(out.array())+")");
            }
            logln("Output: "+  new String(out.array()) + "("+ hex(out.array())+")");
        }catch (CharacterCodingException ex){
            errln("Unexpected exception: "+ex.toString());
        }
    }
    /*
    public void TestImplFlushFailure(){
   
       try{
           CharBuffer in = CharBuffer.wrap("\u3005\u3006\u3007\u30FC\u2015\u2010\uFF0F");
           CharsetEncoder encoder = new CharsetProviderICU().charsetForName("iso-2022-jp").newEncoder();
           ByteBuffer out = ByteBuffer.allocate(30);
           encoder.encode(in, out, true);
           encoder.flush(out);
           if(out.position()!= 20){
               errln("Did not get the expected position from flush");
           }
           
       }catch (Exception ex){
           errln("Could not create encoder for  iso-2022-jp exception: "+ex.toString());
       } 
    }
   */
    public void TestISO88591() {
       
        Charset cs = new CharsetProviderICU().charsetForName("iso-8859-1");
        if(cs!=null){
            CharsetEncoder encoder = cs.newEncoder();
            if(encoder!=null){
                encoder.canEncode("\uc2a3");
            }else{
                errln("Could not create encoder for iso-8859-1");
            }
        }else{
            errln("Could not create Charset for iso-8859-1");
        }
        
    }
    public  void TestUTF8Encode() {
        CharsetEncoder encoderICU = new CharsetProviderICU().charsetForName("utf-8").newEncoder();
        ByteBuffer out = ByteBuffer.allocate(30);
        CoderResult result = encoderICU.encode(CharBuffer.wrap("\ud800"), out, true);
       
        if (result.isMalformed()) {
            logln("\\ud800 is malformed for ICU4JNI utf-8 encoder");
        } else if (result.isUnderflow()) {
            errln("\\ud800 is OK for ICU4JNI utf-8 encoder");
        }

        CharsetEncoder encoderJDK = Charset.forName("utf-8").newEncoder();
        result = encoderJDK.encode(CharBuffer.wrap("\ud800"), ByteBuffer
                .allocate(10), true);
        if (result.isUnderflow()) {
            errln("\\ud800 is OK for JDK utf-8 encoder");
        } else if (result.isMalformed()) {
            logln("\\ud800 is malformed for JDK utf-8 encoder");
        }
    }

/*    private void printCB(CharBuffer buf){
        buf.rewind();
        while(buf.hasRemaining()){
            System.out.println(hex(buf.get()));
        }
        buf.rewind();
    }
*/
    public void TestUTF8() throws CharacterCodingException{
           try{
               CharsetEncoder encoderICU = new CharsetProviderICU().charsetForName("utf-8").newEncoder();
               encoderICU.encode(CharBuffer.wrap("\ud800"));
               errln("\\ud800 is OK for ICU4JNI utf-8 encoder");
           }catch (Exception e) {
               logln("\\ud800 is malformed for JDK utf-8 encoder");
              //e.printStackTrace();
           }
           
           CharsetEncoder encoderJDK = Charset.forName("utf-8").newEncoder();
           try {
               encoderJDK.encode(CharBuffer.wrap("\ud800"));
               errln("\\ud800 is OK for JDK utf-8 encoder");
           } catch (Exception e) {
               logln("\\ud800 is malformed for JDK utf-8 encoder");
               //e.printStackTrace();
           }         
    }
    
    public void TestUTF16Bom(){

        Charset cs = (new CharsetProviderICU()).charsetForName("UTF-16");
        char[] in = new char[] { 0x1122, 0x2211, 0x3344, 0x4433,
                                0x5566, 0x6655, 0x7788, 0x8877, 0x9900 };
        CharBuffer inBuf = CharBuffer.allocate(in.length);
        inBuf.put(in);
        CharsetEncoder encoder = cs.newEncoder();
        ByteBuffer outBuf = ByteBuffer.allocate(in.length*2+2);
        inBuf.rewind();
        encoder.encode(inBuf, outBuf, true);
        outBuf.rewind();
        if(outBuf.get(0)!= (byte)0xFE && outBuf.get(1)!= (byte)0xFF){
            errln("The UTF16 encoder did not appended bom. Length returned: " + outBuf.remaining());
        }
        while(outBuf.hasRemaining()){
            logln("0x"+hex(outBuf.get()));
        }
        CharsetDecoder decoder = cs.newDecoder();
        outBuf.rewind();
        CharBuffer rt = CharBuffer.allocate(in.length);
        CoderResult cr = decoder.decode(outBuf, rt, true);
        if(cr.isError()){
            errln("Decoding with BOM failed. Error: "+ cr.toString());
        }
        equals(rt, in);
        {
            rt.clear();
            outBuf.rewind();
            Charset utf16 = Charset.forName("UTF-16");
            CharsetDecoder dc = utf16.newDecoder();
            cr = dc.decode(outBuf, rt, true);
            equals(rt, in);
        }
    }
     
    private void smBufDecode(CharsetDecoder decoder, String encoding, ByteBuffer source,
            CharBuffer target, boolean throwException, boolean flush)
            throws BufferOverflowException, Exception {
      smBufDecode(decoder, encoding, source, target, throwException, flush, true);
    }
    private void smBufDecode(CharsetDecoder decoder, String encoding, ByteBuffer source,
            CharBuffer target, boolean throwException, boolean flush, boolean backedByArray)
            throws BufferOverflowException, Exception {
      smBufDecode(decoder, encoding, source, target, throwException, flush, backedByArray, -1);
    }
    private void smBufDecode(CharsetDecoder decoder, String encoding, ByteBuffer source,
            CharBuffer target, boolean throwException, boolean flush, boolean backedByArray,
            int targetLimit) throws BufferOverflowException, Exception {
        ByteBuffer mySource;
        CharBuffer myTarget;
        if (backedByArray) {
            mySource = ByteBuffer.allocate(source.capacity());
            myTarget = CharBuffer.allocate(target.capacity());
        } else {
            // this does not guarantee by any means that mySource and myTarget are not backed by arrays
            mySource = ByteBuffer.allocateDirect(source.capacity());
            myTarget = ByteBuffer.allocateDirect(target.capacity() * 2).asCharBuffer();
        }
        mySource.position(source.position());
        for (int i=source.position(); i<source.limit(); i++)
            mySource.put(i, source.get(i));
        
        {            
            decoder.reset();
            myTarget.limit(target.limit());
            mySource.limit(source.limit());
            mySource.position(source.position());
            CoderResult result = CoderResult.UNDERFLOW;
            result = decoder.decode(mySource, myTarget, true);
            if (flush) {
                decoder.flush(myTarget);
            }
            if (result.isError()) {
                if (throwException) {
                    throw new Exception();
                }
                errln("Test complete buffers while decoding failed. "+result.toString());
                return;
            }
            if (result.isOverflow()) {
                if (throwException) {
                    throw new BufferOverflowException();
                }
                errln("Test complete buffers while decoding threw overflow exception");
                return;
            }
            myTarget.limit(myTarget.position());
            myTarget.position(0);
            target.position(0);
            if (result.isUnderflow() && !equals(myTarget, target, targetLimit)) {
                errln(
                    " Test complete buffers while decoding  "
                        + encoding
                        + " TO Unicode--failed");
            }
        }
        if(isQuick()){
            return;
        }
        {
            decoder.reset();
            myTarget.limit(target.limit());
            mySource.limit(source.limit());
            mySource.position(source.position());
            myTarget.clear();
            myTarget.position(0);
            
            int inputLen = mySource.remaining();

            CoderResult result = CoderResult.UNDERFLOW;
            for(int i=1; i<=inputLen; i++) {
                mySource.limit(i);
                if(i==inputLen){
                    result = decoder.decode(mySource, myTarget, true);
                }else{
                    result = decoder.decode(mySource, myTarget, false);
                }
                if (result.isError()) {
                    errln("Test small input buffers while decoding failed. "+result.toString());
                    break;
                }
                if (result.isOverflow()) {
                    if (throwException) {
                        throw new BufferOverflowException();
                    }
                    errln("Test small input buffers while decoding threw overflow exception");
                    break;
                }

            }
            if (result.isUnderflow() && !equals(myTarget, target, targetLimit)) {
                errln(
                    "Test small input buffers while decoding "
                        + encoding
                        + " TO Unicode--failed");
            }
        }
        {
            decoder.reset();
            myTarget.limit(target.limit());
            mySource.limit(source.limit());
            mySource.position(source.position());
            myTarget.clear();
            while (true) {
                int pos = myTarget.position();
                myTarget.limit(++pos);
                CoderResult result = decoder.decode(mySource, myTarget, false);
                if (result.isError()) {
                    errln("Test small output buffers while decoding "+ result.toString());
                }
                if (mySource.position()== mySource.limit()) {
                    result = decoder.decode(mySource, myTarget, true);
                    if (result.isError()) {
                        errln("Test small output buffers while decoding "+result.toString());
                    }
                    result = decoder.flush(myTarget);
                    if (result.isError()) {
                        errln("Test small output buffers while decoding "+ result.toString());
                    }
                    break;
                }
            }

            if (!equals(myTarget, target, targetLimit)) {
                errln(
                    "Test small output buffers "
                        + encoding
                        + " TO Unicode failed");
            }
        }
    }

    private void smBufEncode(CharsetEncoder encoder, String encoding, CharBuffer source,
            ByteBuffer target, boolean throwException, boolean flush) throws Exception,
            BufferOverflowException {
        smBufEncode(encoder, encoding, source, target, throwException, flush, true); 
    }
    private void smBufEncode(CharsetEncoder encoder, String encoding, CharBuffer source,
            ByteBuffer target, boolean throwException, boolean flush, boolean backedByArray)
            throws Exception, BufferOverflowException {
        smBufEncode(encoder, encoding, source, target, throwException, flush, true, -1); 
    }
    private void smBufEncode(CharsetEncoder encoder, String encoding, CharBuffer source,
            ByteBuffer target, boolean throwException, boolean flush, boolean backedByArray,
            int targetLimit) throws Exception, BufferOverflowException {
        logln("Running smBufEncode for "+ encoding + " with class " + encoder);
        
        CharBuffer mySource;
        ByteBuffer myTarget;
        if (backedByArray) {
            mySource = CharBuffer.allocate(source.capacity());
            myTarget = ByteBuffer.allocate(target.capacity());
        } else {
            mySource = ByteBuffer.allocateDirect(source.capacity() * 2).asCharBuffer();
            myTarget = ByteBuffer.allocateDirect(target.capacity());
        }
        mySource.position(source.position());
        for (int i=source.position(); i<source.limit(); i++)
            mySource.put(i, source.get(i));
        
        myTarget.clear();
        {
            logln("Running tests on small input buffers for "+ encoding);
            encoder.reset();
            myTarget.limit(target.limit());
            mySource.limit(source.limit());
            mySource.position(source.position());
            CoderResult result=null;
            
            result = encoder.encode(mySource, myTarget, true);
            if (flush) {
                result = encoder.flush(myTarget);
            }

            if (result.isError()) {
                if (throwException) {
                    throw new Exception();
                }
                errln("Test complete while encoding failed. "+result.toString());
            }
            if (result.isOverflow()) {
                if (throwException) {
                    throw new BufferOverflowException();
                }
                errln("Test complete while encoding threw overflow exception");
            }
            if (!equals(myTarget, target, targetLimit)) {
                // TODO: REMOVE output
                System.out.println(source.limit() + " " + mySource.limit() + " " + target.limit() + " " + myTarget.limit() + " " + targetLimit);
                System.out.println((char)target.get(0) + " " + myTarget.get(0) + " " + targetLimit);
                errln("Test complete buffers while encoding for "+ encoding+ " failed");

            }
            else{
                logln("Tests complete buffers for "+ encoding +" passed");
            }
        }
        if(isQuick()){
            return;
        }
        {
            logln("Running tests on small input buffers for "+ encoding);
            encoder.reset();
            myTarget.clear();
            myTarget.limit(target.limit());
            mySource.limit(source.limit());
            mySource.position(source.position());
            int inputLen = mySource.limit();
            CoderResult result=null;
            for(int i=1; i<=inputLen; i++) {
                mySource.limit(i);
                result = encoder.encode(mySource, myTarget, false);
                if (result.isError()) {
                    errln("Test small input buffers while encoding failed. "+result.toString());
                }
                if (result.isOverflow()) {
                    if (throwException) {
                        throw new BufferOverflowException();
                    }
                    errln("Test small input buffers while encoding threw overflow exception");
                }
            }
            if (!equals(myTarget, target, targetLimit)) {
                errln("Test small input buffers "+ encoding+ " From Unicode failed");
            }else{
                logln("Tests on small input buffers for "+ encoding +" passed");
            }
        }
        {
            logln("Running tests on small output buffers for "+ encoding);
            encoder.reset();
            myTarget.clear();
            myTarget.limit(target.limit());
            mySource.limit(source.limit());
            mySource.position(source.position());
            mySource.position(0);
            myTarget.position(0);
            
            logln("myTarget.limit: " + myTarget.limit() + " myTarget.capcity: " + myTarget.capacity());
            
            while (true) {
                int pos = myTarget.position();

                CoderResult result = encoder.encode(mySource, myTarget, false);
                logln("myTarget.Position: "+ pos + " myTarget.limit: " + myTarget.limit());
                logln("mySource.position: " + mySource.position() + " mySource.limit: " + mySource.limit());
                
                if (result.isError()) {
                    errln("Test small output buffers while encoding "+result.toString());
                }
                if (mySource.position() == mySource.limit()) {
                    result = encoder.encode(mySource, myTarget, true);
                    if (result.isError()) {
                        errln("Test small output buffers while encoding "+result.toString());
                    }
                    
                    myTarget.limit(myTarget.capacity());
                    result = encoder.flush(myTarget);
                    if (result.isError()) {
                        errln("Test small output buffers while encoding "+result.toString());
                    }
                    break;
                }
            }
            if (!equals(myTarget, target, targetLimit)) {
                errln("Test small output buffers "+ encoding+ " From Unicode failed.");
            }
            logln("Tests on small output buffers for "+ encoding +" passed");

        }
    }
    public void convertAllTest(ByteBuffer bSource, CharBuffer uSource) throws Exception {
        {
            try {
                m_decoder.reset();
                ByteBuffer mySource = bSource.duplicate();
                CharBuffer myTarget = m_decoder.decode(mySource);
                if (!equals(myTarget, uSource)) {
                    errln(
                        "--Test convertAll() "
                            + m_encoding
                            + " to Unicode  --FAILED");
                }
            } catch (Exception e) {
                //e.printStackTrace();
                errln(e.getMessage());
            }
        }
        {
            try {
                m_encoder.reset();
                CharBuffer mySource = CharBuffer.wrap(uSource);
                ByteBuffer myTarget = m_encoder.encode(mySource);
                if (!equals(myTarget, bSource)) {
                    errln(
                        "--Test convertAll() "
                            + m_encoding
                            + " to Unicode  --FAILED");
                }
            } catch (Exception e) {
                //e.printStackTrace();
                errln("encoder.encode() failed "+ e.getMessage()+" "+e.toString());
            }
        }

    }
    //TODO
    /*
    public void TestString(ByteBuffer bSource, CharBuffer uSource) throws Exception {
        try {
            {
                String source = uSource.toString();
                byte[] target = source.getBytes(m_encoding);
                if (!equals(target, bSource.array())) {
                    errln("encode using string API failed");
                }
            }
            {

                String target = new String(bSource.array(), m_encoding);
                if (!equals(uSource, target.toCharArray())) {
                    errln("decode using string API failed");
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            errln(e.getMessage());
        }
    }

    /*private void fromUnicodeTest() throws Exception {
        
        logln("Loaded Charset: " + charset.getClass().toString());
        logln("Loaded CharsetEncoder: " + encoder.getClass().toString());
        logln("Loaded CharsetDecoder: " + decoder.getClass().toString());
        
        ByteBuffer myTarget = ByteBuffer.allocate(gbSource.length);
        logln("Created ByteBuffer of length: " + uSource.length);
        CharBuffer mySource = CharBuffer.wrap(uSource);
        logln("Wrapped ByteBuffer with CharBuffer  ");
        encoder.reset();
        logln("Test Unicode to " + encoding );
        encoder.encode(mySource, myTarget, true);
        if (!equals(myTarget, gbSource)) {
            errln("--Test Unicode to " + encoding + ": FAILED");
        } 
        logln("Test Unicode to " + encoding +" passed");
    }

    public void TestToUnicode( ) throws Exception {
        
        logln("Loaded Charset: " + charset.getClass().toString());
        logln("Loaded CharsetEncoder: " + encoder.getClass().toString());
        logln("Loaded CharsetDecoder: " + decoder.getClass().toString());
        
        CharBuffer myTarget = CharBuffer.allocate(uSource.length);
        ByteBuffer mySource = ByteBuffer.wrap(getByteArray(gbSource));
        decoder.reset();
        CoderResult result = decoder.decode(mySource, myTarget, true);
        if (result.isError()) {
            errln("Test ToUnicode -- FAILED");
        }
        if (!equals(myTarget, uSource)) {
            errln("--Test " + encoding + " to Unicode :FAILED");
        }
    }

    public static byte[] getByteArray(char[] source) {
        byte[] target = new byte[source.length];
        int i = source.length;
        for (; --i >= 0;) {
            target[i] = (byte) source[i];
        }
        return target;
    }
    /*
    private void smBufCharset(Charset charset) {
        try {
            ByteBuffer bTarget = charset.encode(CharBuffer.wrap(uSource));
            CharBuffer uTarget =
                charset.decode(ByteBuffer.wrap(getByteArray(gbSource)));

            if (!equals(uTarget, uSource)) {
                errln("Test " + charset.toString() + " to Unicode :FAILED");
            }
            if (!equals(bTarget, gbSource)) {
                errln("Test " + charset.toString() + " from Unicode :FAILED");
            }
        } catch (Exception ex) {
            errln("Encountered exception in smBufCharset");
        }
    }
    
    public void TestMultithreaded() throws Exception {
        final Charset cs = Charset.forName(encoding);
        if (cs == charset) {
            errln("The objects are equal");
        }
        smBufCharset(cs);
        try {
            final Thread t1 = new Thread() {
                public void run() {
                    // commented out since the mehtods on
                    // Charset API are supposed to be thread
                    // safe ... to test it we dont sync
            
                    // synchronized(charset){
                   while (!interrupted()) {
                        try {
                            smBufCharset(cs);
                        } catch (UnsupportedCharsetException ueEx) {
                            errln(ueEx.toString());
                        }
                    }

                    // }
                }
            };
            final Thread t2 = new Thread() {
                public void run() {
                        // synchronized(charset){
                    while (!interrupted()) {
                        try {
                            smBufCharset(cs);
                        } catch (UnsupportedCharsetException ueEx) {
                            errln(ueEx.toString());
                        }
                    }

                    //}
                }
            };
            t1.start();
            t2.start();
            int i = 0;
            for (;;) {
                if (i > 1000000000) {
                    try {
                        t1.interrupt();
                    } catch (Exception e) {
                    }
                    try {
                        t2.interrupt();
                    } catch (Exception e) {
                    }
                    break;
                }
                i++;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void TestSynchronizedMultithreaded() throws Exception {
        // Methods on CharsetDecoder and CharsetEncoder classes
        // are inherently unsafe if accessed by multiple concurrent
        // thread so we synchronize them
        final Charset charset = Charset.forName(encoding);
        final CharsetDecoder decoder = charset.newDecoder();
        final CharsetEncoder encoder = charset.newEncoder();
        try {
            final Thread t1 = new Thread() {
                public void run() {
                    while (!interrupted()) {
                        try {
                            synchronized (encoder) {
                                smBufEncode(encoder, encoding);
                            }
                            synchronized (decoder) {
                                smBufDecode(decoder, encoding);
                            }
                        } catch (UnsupportedCharsetException ueEx) {
                            errln(ueEx.toString());
                        }
                    }

                }
            };
            final Thread t2 = new Thread() {
                public void run() {
                    while (!interrupted()) {
                        try {
                            synchronized (encoder) {
                                smBufEncode(encoder, encoding);
                            }
                            synchronized (decoder) {
                                smBufDecode(decoder, encoding);
                            }
                        } catch (UnsupportedCharsetException ueEx) {
                            errln(ueEx.toString());
                        }
                    }
                }
            };
            t1.start();
            t2.start();
            int i = 0;
            for (;;) {
                if (i > 1000000000) {
                    try {
                        t1.interrupt();
                    } catch (Exception e) {
                    }
                    try {
                        t2.interrupt();
                    } catch (Exception e) {
                    }
                    break;
                }
                i++;
            }
        } catch (Exception e) {
            throw e;
        }
    }
    */
    
    public void TestMBCS(){      
        {
            // Encoder: from Unicode conversion
            CharsetEncoder encoderICU = new CharsetProviderICU().charsetForName("ibm-971").newEncoder();
            ByteBuffer out = ByteBuffer.allocate(6);
            encoderICU.onUnmappableCharacter(CodingErrorAction.REPLACE);
            CoderResult result = encoderICU.encode(CharBuffer.wrap("\u0131\u0061\u00a1"), out, true);
            if(!result.isError()){
                byte[] expected = {(byte)0xA9, (byte)0xA5, (byte)0xAF, (byte)0xFE, (byte)0xA2, (byte)0xAE};
                if(!equals(expected, out.array())){
                    errln("Did not get the expected result for substitution bytes. Got: "+
                           hex(out.array()));
                }
                logln("Output: "+  hex(out.array()));
            }else{
                errln("Encode operation failed for encoder: "+encoderICU.toString());
            }
        }
        {
            // Decoder: to Unicode conversion
            CharsetDecoder decoderICU = new CharsetProviderICU().charsetForName("ibm-971").newDecoder();
            CharBuffer out = CharBuffer.allocate(3);
            decoderICU.onMalformedInput(CodingErrorAction.REPLACE);
            CoderResult result = decoderICU.decode(ByteBuffer.wrap(new byte[] { (byte)0xA2, (byte)0xAE, (byte)0x12, (byte)0x34, (byte)0xEF, (byte)0xDC }), out, true);
            if(!result.isError()){
                char[] expected = {'\u00a1', '\ufffd', '\u6676'};
                if(!equals(expected, out.array())){
                    errln("Did not get the expected result for substitution chars. Got: "+
                           hex(out.array()));
                }
                logln("Output: "+  hex(out.array()));
            }else{
                errln("Decode operation failed for encoder: "+decoderICU.toString());
            }
        }
    }
    
    public void TestJB4897(){
        CharsetProviderICU provider = new CharsetProviderICU();
        Charset charset = provider.charsetForName("x-abracadabra");  
        if(charset!=null && charset.canEncode()== true){
            errln("provider.charsetForName() does not validate the charset names" );
        }
    }

    public void TestJB5027() {
        CharsetProviderICU provider= new CharsetProviderICU();

        Charset fake = provider.charsetForName("doesNotExist");
        if(fake != null){
            errln("\"doesNotExist\" returned " + fake);
        }
        Charset xfake = provider.charsetForName("x-doesNotExist");
        if(xfake!=null){
            errln("\"x-doesNotExist\" returned " + xfake);
        }
    }
    //test to make sure that number of aliases and canonical names are in the charsets that are in
    public void TestAllNames() {
        
        CharsetProviderICU provider= new CharsetProviderICU();
        Object[] available = CharsetProviderICU.getAvailableNames();
        for(int i=0; i<available.length;i++){
            try{
                String canon  = CharsetProviderICU.getICUCanonicalName((String)available[i]);

                // ',' is not allowed by Java's charset name checker
                if(canon.indexOf(',')>=0){
                    continue;
                }
                Charset cs = provider.charsetForName((String)available[i]);
              
                Object[] javaAliases =  cs.aliases().toArray();
                //seach for ICU canonical name in javaAliases
                boolean inAliasList = false;
                for(int j=0; j<javaAliases.length; j++){
                    String java = (String) javaAliases[j];
                    if(java.equals(canon)){
                        logln("javaAlias: " + java + " canon: " + canon);
                        inAliasList = true;
                    }
                }
                if(inAliasList == false){
                    errln("Could not find ICU canonical name: "+canon+ " for java canonical name: "+ available[i]+ " "+ i);
                }
            }catch(UnsupportedCharsetException ex){
                errln("could no load charset "+ available[i]+" "+ex.getMessage());
                continue;
            }
        }
    }
    public void TestDecoderImplFlush() {
        CharsetProviderICU provider = new CharsetProviderICU();
        Charset ics = provider.charsetForName("UTF-16");
        Charset jcs = Charset.forName("UTF-16"); // Java's UTF-16 charset
        execDecoder(jcs);
        execDecoder(ics);
    }
    public void TestEncoderImplFlush() {
        CharsetProviderICU provider = new CharsetProviderICU();
        Charset ics = provider.charsetForName("UTF-16");
        Charset jcs = Charset.forName("UTF-16"); // Java's UTF-16 charset
        execEncoder(jcs);
        execEncoder(ics);
    }
    private void execDecoder(Charset cs){
        CharsetDecoder decoder = cs.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        CharBuffer out = CharBuffer.allocate(10);
        CoderResult result = decoder.decode(ByteBuffer.wrap(new byte[] { -1,
                -2, 32, 0, 98 }), out, false);
        result = decoder.decode(ByteBuffer.wrap(new byte[] { 98 }), out, true);

        logln(cs.getClass().toString()+ ":" +result.toString());
        try {
            result = decoder.flush(out);
            logln(cs.getClass().toString()+ ":" +result.toString());
        } catch (Exception e) {
            errln(e.getMessage()+" "+cs.getClass().toString());
        }
    }
    private void execEncoder(Charset cs){
        CharsetEncoder encoder = cs.newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPORT);
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        ByteBuffer out = ByteBuffer.allocate(10);
        CoderResult result = encoder.encode(CharBuffer.wrap(new char[] { '\uFFFF',
                '\u2345', 32, 98 }), out, false);
        logln(cs.getClass().toString()+ ":" +result.toString());
        result = encoder.encode(CharBuffer.wrap(new char[] { 98 }), out, true);

        logln(cs.getClass().toString()+ ":" +result.toString());
        try {
            result = encoder.flush(out);
            logln(cs.getClass().toString()+ ":" +result.toString());
        } catch (Exception e) {
            errln(e.getMessage()+" "+cs.getClass().toString());
        }
    }
    public void TestDecodeMalformed() {
        CharsetProviderICU provider = new CharsetProviderICU();
        Charset ics = provider.charsetForName("UTF-16BE");
        //Use SUN's charset
        Charset jcs = Charset.forName("UTF-16");
        CoderResult ir = execMalformed(ics);
        CoderResult jr = execMalformed(jcs);
        if(ir!=jr){
            errln("ICU's decoder did not return the same result as Sun. ICU: "+ir.toString()+" Sun: "+jr.toString());
        }
    }
    private CoderResult execMalformed(Charset cs){
        CharsetDecoder decoder = cs.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.IGNORE);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        ByteBuffer in = ByteBuffer.wrap(new byte[] { 0x00, 0x41, 0x00, 0x42, 0x01 });
        CharBuffer out = CharBuffer.allocate(3);
        return decoder.decode(in, out, true);
    }
    
    public void TestJavaUTF16Decoder(){
        CharsetProviderICU provider = new CharsetProviderICU();
        Charset ics = provider.charsetForName("UTF-16BE");
        //Use SUN's charset
        Charset jcs = Charset.forName("UTF-16");
        Exception ie = execConvertAll(ics);
        Exception je = execConvertAll(jcs);
        if(ie!=je){
            errln("ICU's decoder did not return the same result as Sun. ICU: "+ie.toString()+" Sun: "+je.toString());
        }
    }
    private Exception execConvertAll(Charset cs){
        ByteBuffer in = ByteBuffer.allocate(400);
        int i=0;
        while(in.position()!=in.capacity()){
            in.put((byte)0xD8);
            in.put((byte)i);
            in.put((byte)0xDC);
            in.put((byte)i);
            i++;
        }
        in.limit(in.position());
        in.position(0);
        CharsetDecoder decoder = cs.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.IGNORE);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        try{
            CharBuffer out = decoder.decode(in);
            if(out!=null){
                logln(cs.toString()+" encoing succeeded as expected!");
            }
        }catch ( Exception ex){
            errln("Did not get expected exception for encoding: "+cs.toString());
            return ex;
        }
        return null;
    }
    public void TestUTF32BOM(){

        Charset cs = (new CharsetProviderICU()).charsetForName("UTF-32");
        char[] in = new char[] { 0xd800, 0xdc00, 
                                 0xd801, 0xdc01,
                                 0xdbff, 0xdfff, 
                                 0xd900, 0xdd00, 
                                 0x0000, 0x0041,
                                 0x0000, 0x0042,
                                 0x0000, 0x0043};
        
        CharBuffer inBuf = CharBuffer.allocate(in.length);
        inBuf.put(in);
        CharsetEncoder encoder = cs.newEncoder();
        ByteBuffer outBuf = ByteBuffer.allocate(in.length*4+4);
        inBuf.rewind();
        encoder.encode(inBuf, outBuf, true);
        outBuf.rewind();
        if(outBuf.get(0)!= (byte)0x00 && outBuf.get(1)!= (byte)0x00 && 
                outBuf.get(2)!= (byte)0xFF && outBuf.get(3)!= (byte)0xFE){
            errln("The UTF16 encoder did not appended bom. Length returned: " + outBuf.remaining());
        }
        while(outBuf.hasRemaining()){
            logln("0x"+hex(outBuf.get()));
        }
        CharsetDecoder decoder = cs.newDecoder();
        outBuf.limit(outBuf.position());
        outBuf.rewind();
        CharBuffer rt = CharBuffer.allocate(in.length);
        CoderResult cr = decoder.decode(outBuf, rt, true);
        if(cr.isError()){
            errln("Decoding with BOM failed. Error: "+ cr.toString());
        }
        equals(rt, in);
        try{
            rt.clear();
            outBuf.rewind();
            Charset utf16 = Charset.forName("UTF-32");
            CharsetDecoder dc = utf16.newDecoder();
            cr = dc.decode(outBuf, rt, true);
            equals(rt, in);
        }catch(UnsupportedCharsetException ex){
            // swallow the expection.
        }
    }
    
    /*
     *  Michael Ow
     *  Modified 070424
     */
    /*The following two methods provides the option of exceptions when Decoding 
     * and Encoding if needed for testing purposes.
     */
    private void smBufDecode(CharsetDecoder decoder, String encoding, ByteBuffer source, CharBuffer target) {
        smBufDecode(decoder, encoding, source, target, true);
    }
    private void smBufDecode(CharsetDecoder decoder, String encoding, ByteBuffer source, CharBuffer target, boolean backedByArray) {
        try {
            smBufDecode(decoder, encoding, source, target, false, false, backedByArray);
        }    
        catch (Exception ex) {           
            System.out.println("!exception!");
        }
    }
    private void smBufEncode(CharsetEncoder encoder, String encoding, CharBuffer source, ByteBuffer target)  {
        smBufEncode(encoder, encoding, source, target, true);
    }
    private void smBufEncode(CharsetEncoder encoder, String encoding, CharBuffer source, ByteBuffer target, boolean backedByArray)  {
        try {
            smBufEncode(encoder, encoding, source, target, false, false); 
        }
        catch (Exception ex) {
            System.out.println("!exception!");
        }
    }
    //Test CharsetICUProvider
    public void TestNullCanonicalName() {
        String enc = null;
        String canonicalName = CharsetProviderICU.getICUCanonicalName(enc);
        
        if (canonicalName != null) {
            errln("getICUCanonicalName return a non-null string for given null string");
        }
    }
    public void TestGetAllNames() {
        String[] names = null;
        
        names = CharsetProviderICU.getAllNames();
        
        if (names == null) {
            errln("getAllNames returned a null string.");
        }
    }
    //Test CharsetICU
    public void TestCharsetContains() {
        boolean test;
        
        CharsetProvider provider = new CharsetProviderICU();     
        Charset cs1 = provider.charsetForName("UTF-32");
        Charset cs2 = null;
        
        test = cs1.contains(cs2);
        
        if (test != false) {
            errln("Charset.contains returned true for a null charset.");
        }
        
        cs2 = CharsetICU.forNameICU("UTF-32");
        
        test = cs1.contains(cs2);
        
        if (test != true) {
            errln("Charset.contains returned false for an identical charset.");
        }
        
        cs2 = provider.charsetForName("UTF-8");
        
        test = cs1.contains(cs2);
        
        if (test != false) {
            errln("Charset.contains returned true for a different charset.");
        }
    }
    public void TestCharsetICUNullCharsetName() {
        String charsetName = null;
        
        try {
            CharsetICU.forNameICU(charsetName);
            errln("CharsetICU.forName should have thown an exception after getting a null charsetName.");
        }
        catch(Exception ex) {          
        }
    }
    
    //Test CharsetASCII
    public void TestCharsetASCIIOverFlow() {
        int byteBufferLimit;
        int charBufferLimit;
        
        CharsetProvider provider = new CharsetProviderICU();
        Charset cs = provider.charsetForName("ASCII");        
        CharsetEncoder encoder = cs.newEncoder();
        CharsetDecoder decoder = cs.newDecoder();
        
        CharBuffer charBuffer = CharBuffer.allocate(0x90);
        ByteBuffer byteBuffer = ByteBuffer.allocate(0x90);
        
        CharBuffer charBufferTest = CharBuffer.allocate(0xb0);
        ByteBuffer byteBufferTest = ByteBuffer.allocate(0xb0);
        
        for(int j=0;j<=0x7f; j++){
           charBuffer.put((char)j);
           byteBuffer.put((byte)j);
        }
        
        byteBuffer.limit(byteBufferLimit = byteBuffer.position());
        byteBuffer.position(0);
        charBuffer.limit(charBufferLimit = charBuffer.position());
        charBuffer.position(0);
        
        //test for overflow
        byteBufferTest.limit(byteBufferLimit - 5);
        byteBufferTest.position(0);
        charBufferTest.limit(charBufferLimit - 5);
        charBufferTest.position(0);
        try {
            smBufDecode(decoder, "ASCII", byteBuffer, charBufferTest, true, false);
            errln("Overflow exception while decoding ASCII should have been thrown.");
        }
        catch(Exception ex) {
        }
        try {
            smBufEncode(encoder, "ASCII", charBuffer, byteBufferTest, true, false);
            errln("Overflow exception while encoding ASCII should have been thrown.");
        }
        catch (Exception ex) {
        }
    }
    //Test CharsetUTF7
    public void TestCharsetUTF7() {
        CharsetProvider provider = new CharsetProviderICU();
        Charset cs = provider.charsetForName("UTF-7");        
        CharsetEncoder encoder = cs.newEncoder();
        CharsetDecoder decoder = cs.newDecoder();
        
        CharBuffer us = CharBuffer.allocate(0x100);
        ByteBuffer bs = ByteBuffer.allocate(0x100);
        
        /* Unicode :  A<not equal to Alpha Lamda>. */
        /* UTF7: AImIDkQ. */
        us.put((char)0x41); us.put((char)0x2262); us.put((char)0x391); us.put((char)0x39B); us.put((char)0x2e);
        bs.put((byte)0x41); bs.put((byte)0x2b); bs.put((byte)0x49); bs.put((byte)0x6d); 
        bs.put((byte)0x49); bs.put((byte)0x44); bs.put((byte)0x6b); bs.put((byte)0x51); 
        bs.put((byte)0x4f); bs.put((byte)0x62); bs.put((byte)0x2e);
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);

        smBufDecode(decoder, "UTF-7", bs, us);
        smBufEncode(encoder, "UTF-7", us, bs);
        
        //The rest of the code in this method is to provide better code coverage
        CharBuffer ccus = CharBuffer.allocate(0x10);
        ByteBuffer ccbs = ByteBuffer.allocate(0x10);
        
        //start of charset decoder code coverage code
        //test for accurate illegal and control character checking
        ccbs.put((byte)0x0D); ccbs.put((byte)0x05);
        ccus.put((char)0x0000);
        
        ccbs.limit(ccbs.position());
        ccbs.position(0);
        ccus.limit(ccus.position());
        ccus.position(0);

        try {
            smBufDecode(decoder, "UTF-7-CC-DE-1", ccbs, ccus, true, false);
            errln("Exception while decoding UTF-7 code coverage test should have been thrown.");
        }
        catch (Exception ex) {
        }
        
        ccbs.clear();
        ccus.clear();
        
        //test for illegal base64 character
        ccbs.put((byte)0x2b); ccbs.put((byte)0xff);
        ccus.put((char)0x0000);
        
        ccbs.limit(ccbs.position());
        ccbs.position(0);
        ccus.limit(ccus.position());
        ccus.position(0);
        
        try {
            smBufDecode(decoder, "UTF-7-CC-DE-2", ccbs, ccus, true, false);
            errln("Exception while decoding UTF-7 code coverage test should have been thrown.");
        }
        catch (Exception ex) {
        }
        
        ccbs.clear();
        ccus.clear();
        
        //test for illegal order of the base64 character sequence
        ccbs.put((byte)0x2b); ccbs.put((byte)0x2d); ccbs.put((byte)0x2b); ccbs.put((byte)0x49); ccbs.put((byte)0x2d);
        ccus.put((char)0x0000); ccus.put((char)0x0000);
        
        ccbs.limit(ccbs.position());
        ccbs.position(0);
        ccus.limit(ccus.position());
        ccus.position(0);
        
        try {
            smBufDecode(decoder, "UTF-7-CC-DE-3", ccbs, ccus, true, false);
            errln("Exception while decoding UTF-7 code coverage test should have been thrown.");
        }
        catch (Exception ex) {
        }
        
        ccbs.clear();
        ccus.clear();
        
        //test for illegal order of the base64 character sequence 
        ccbs.put((byte)0x2b); ccbs.put((byte)0x0a); ccbs.put((byte)0x09);
        ccus.put((char)0x0000);
        
        ccbs.limit(ccbs.position());
        ccbs.position(0);
        ccus.limit(ccus.position());
        ccus.position(0);
        
        try {
            smBufDecode(decoder, "UTF-7-CC-DE-4", ccbs, ccus, true, false);
            errln("Exception while decoding UTF-7 code coverage test should have been thrown.");
        }
        catch (Exception ex) {
        }
        
        ccbs.clear();
        ccus.clear();
        
        //test for illegal order of the base64 character sequence
        ccbs.put((byte)0x2b); ccbs.put((byte)0x49); ccbs.put((byte)0x0a);
        ccus.put((char)0x0000);
        
        ccbs.limit(ccbs.position());
        ccbs.position(0);
        ccus.limit(ccus.position());
        ccus.position(0);
        
        try {
            smBufDecode(decoder, "UTF-7-CC-DE-5", ccbs, ccus, true, false);
            errln("Exception while decoding UTF-7 code coverage test should have been thrown.");
        }
        catch (Exception ex) {
        }
        
        ccbs.clear();
        ccus.clear();
        
        //test for illegal order of the base64 character sequence
        ccbs.put((byte)0x2b); ccbs.put((byte)0x00);
        ccus.put((char)0x0000);
        
        ccbs.limit(ccbs.position());
        ccbs.position(0);
        ccus.limit(ccus.position());
        ccus.position(0);
        
        try {
            smBufDecode(decoder, "UTF-7-CC-DE-6", ccbs, ccus, true, false);
            errln("Exception while decoding UTF-7 code coverage test should have been thrown.");
        }
        catch (Exception ex) {
        }
        
        ccbs.clear();
        ccus.clear();
        
        //test for overflow buffer error
        ccbs.put((byte)0x2b); ccbs.put((byte)0x49);
        
        ccbs.limit(ccbs.position());
        ccbs.position(0);
        ccus.limit(0);
        ccus.position(0);
        
        try {
            smBufDecode(decoder, "UTF-7-CC-DE-7", ccbs, ccus, true, false);
            errln("Exception while decoding UTF-7 code coverage test should have been thrown.");
        }
        catch (Exception ex) {
        }
        
        ccbs.clear();
        ccus.clear();
        
        //test for overflow buffer error
        ccbs.put((byte)0x0c); ccbs.put((byte)0x0c);
        
        ccbs.limit(ccbs.position());
        ccbs.position(0);
        ccus.limit(0);
        ccus.position(0);
        
        try {
            smBufDecode(decoder, "UTF-7-CC-DE-8", ccbs, ccus, true, false);
            errln("Exception while decoding UTF-7 code coverage test should have been thrown.");
        }
        catch (Exception ex) {
        }
        //end of charset decoder code coverage code
        
        //start of charset encoder code coverage code
        ccbs.clear();
        ccus.clear();
        //test for overflow buffer error
        ccus.put((char)0x002b);
        ccbs.put((byte)0x2b); 
        
        ccbs.limit(ccbs.position());
        ccbs.position(0);
        ccus.limit(ccus.position());
        ccus.position(0);
        
        try {
            smBufEncode(encoder, "UTF-7-CC-EN-1", ccus, ccbs, true, false);
            errln("Exception while encoding UTF-7 code coverage test should have been thrown.");
        }
        catch (Exception ex) {
        }
        
        ccbs.clear();
        ccus.clear();
        
        //test for overflow buffer error
        ccus.put((char)0x002b); ccus.put((char)0x2262);
        ccbs.put((byte)0x2b); ccbs.put((byte)0x2d); ccbs.put((byte)0x00); ccbs.put((byte)0x00);
        
        ccbs.limit(ccbs.position());
        ccbs.position(0);
        ccus.limit(ccus.position());
        ccus.position(0);
        
        try {
            smBufEncode(encoder, "UTF-7-CC-EN-2", ccus, ccbs, true, false);
            errln("Exception while encoding UTF-7 code coverage test should have been thrown.");
        }
        catch (Exception ex) {
        } 
        
        ccbs.clear();
        ccus.clear();
        
        //test for overflow buffer error
        ccus.put((char)0x2262); ccus.put((char)0x0049);
        ccbs.put((byte)0x00); ccbs.put((byte)0x00); ccbs.put((byte)0x00); ccbs.put((byte)0x00); ccbs.put((byte)0x00);
        ccbs.limit(ccbs.position());
        ccbs.position(0);
        ccus.limit(ccus.position());
        ccus.position(0);
        
        try {
            smBufEncode(encoder, "UTF-7-CC-EN-3", ccus, ccbs, true, false);
            errln("Exception while encoding UTF-7 code coverage test should have been thrown.");
        }
        catch (Exception ex) {
        }  
        
        ccbs.clear();
        ccus.clear();
        
        //test for overflow buffer error
        ccus.put((char)0x2262); ccus.put((char)0x0395);
        ccbs.put((byte)0x00); ccbs.put((byte)0x00); ccbs.put((byte)0x00); ccbs.put((byte)0x00);
        ccbs.limit(ccbs.position());
        ccbs.position(0);
        ccus.limit(ccus.position());
        ccus.position(0);
        
        try {
            smBufEncode(encoder, "UTF-7-CC-EN-4", ccus, ccbs, true, false);
            errln("Exception while encoding UTF-7 code coverage test should have been thrown.");
        }
        catch (Exception ex) {
        }  
        
        ccbs.clear();
        ccus.clear();
        
        //test for overflow buffer error
        ccus.put((char)0x2262); ccus.put((char)0x0395);
        ccbs.put((byte)0x00); ccbs.put((byte)0x00); ccbs.put((byte)0x00); ccbs.put((byte)0x00); ccbs.put((byte)0x00);
        ccbs.limit(ccbs.position());
        ccbs.position(0);
        ccus.limit(ccus.position());
        ccus.position(0);
        
        try {
            smBufEncode(encoder, "UTF-7-CC-EN-5", ccus, ccbs, true, false);
            errln("Exception while encoding UTF-7 code coverage test should have been thrown.");
        }
        catch (Exception ex) {
        }  
        
        ccbs.clear();
        ccus.clear();
        
        //test for overflow buffer error
        ccus.put((char)0x2262); ccus.put((char)0x0395); ccus.put((char)0x0391);
        ccbs.put((byte)0x00); ccbs.put((byte)0x00); ccbs.put((byte)0x00); ccbs.put((byte)0x00); ccbs.put((byte)0x00); ccbs.put((byte)0x00); ccbs.put((byte)0x00);
        ccbs.limit(ccbs.position());
        ccbs.position(0);
        ccus.limit(ccus.position());
        ccus.position(0);
        
        try {
            smBufEncode(encoder, "UTF-7-CC-EN-6", ccus, ccbs, true, false);
            errln("Exception while encoding UTF-7 code coverage test should have been thrown.");
        }
        catch (Exception ex) {
        }  
        
        ccbs.clear();
        ccus.clear();
        
        //test for overflow buffer error
        ccus.put((char)0x2262); ccus.put((char)0x0395); ccus.put((char)0x0391);
        ccbs.put((byte)0x00); ccbs.put((byte)0x00); ccbs.put((byte)0x00); ccbs.put((byte)0x00); 
        ccbs.put((byte)0x00); ccbs.put((byte)0x00); ccbs.put((byte)0x00); ccbs.put((byte)0x00);
        ccbs.limit(ccbs.position());
        ccbs.position(0);
        ccus.limit(ccus.position());
        ccus.position(0);
        
        try {
            smBufEncode(encoder, "UTF-7-CC-EN-7", ccus, ccbs, true, false);
            errln("Exception while encoding UTF-7 code coverage test should have been thrown.");
        }
        catch (Exception ex) {
        }  
        
        ccbs.clear();
        ccus.clear();
        
        //test for overflow buffer error
        ccus.put((char)0x0049); ccus.put((char)0x0048);
        ccbs.put((byte)0x00); 
        ccbs.limit(ccbs.position());
        ccbs.position(0);
        ccus.limit(ccus.position());
        ccus.position(0);
        
        try {
            smBufEncode(encoder, "UTF-7-CC-EN-8", ccus, ccbs, true, false);
            errln("Exception while encoding UTF-7 code coverage test should have been thrown.");
        }
        catch (Exception ex) {
        } 
        
        ccbs.clear();
        ccus.clear();
        
        //test for overflow buffer error
        ccus.put((char)0x2262);
        ccbs.put((byte)0x00);
        ccbs.limit(ccbs.position());
        ccbs.position(0);
        ccus.limit(ccus.position());
        ccus.position(0);
        
        try {
            smBufEncode(encoder, "UTF-7-CC-EN-9", ccus, ccbs, true, false);
            errln("Exception while encoding UTF-7 code coverage test should have been thrown.");
        }
        catch (Exception ex) {
        } 
        
        ccbs.clear();
        ccus.clear();
        
        //test for overflow buffer error
        ccus.put((char)0x2262); ccus.put((char)0x0049);
        ccbs.put((byte)0x00); ccbs.put((byte)0x00); ccbs.put((byte)0x00); ccbs.put((byte)0x00);
        ccbs.limit(ccbs.position());
        ccbs.position(0);
        ccus.limit(ccus.position());
        ccus.position(0);
        
        try {
            smBufEncode(encoder, "UTF-7-CC-EN-10", ccus, ccbs, true, false);
            errln("Exception while encoding UTF-7 code coverage test should have been thrown.");
        }
        catch (Exception ex) {
        }  
        
        ccbs.clear();
        ccus.clear();
        
        //test for overflow buffer error
        ccus.put((char)0x2262);
        ccbs.put((byte)0x2b); ccbs.put((byte)0x49); ccbs.put((byte)0x6d); ccbs.put((byte)0x49);
        
        ccbs.limit(ccbs.position());
        ccbs.position(0);
        ccus.limit(ccus.position());
        ccus.position(0);
        try {
            smBufEncode(encoder, "UTF-7-CC-EN-11", ccus, ccbs, false, true);
        } catch (Exception ex) {
        }
        //end of charset encoder code coverage code
    }
    //Test Charset ISCII
    public void TestCharsetISCII() {
        CharsetProvider provider = new CharsetProviderICU();
        Charset cs = provider.charsetForName("ISCII,version=0");        
        CharsetEncoder encoder = cs.newEncoder();
        CharsetDecoder decoder = cs.newDecoder();
        
        CharBuffer us = CharBuffer.allocate(0x100);
        ByteBuffer bs = ByteBuffer.allocate(0x100);
        ByteBuffer bsr = ByteBuffer.allocate(0x100);
        
        //test full range of Devanagari
        us.put((char)0x0901); us.put((char)0x0902); us.put((char)0x0903); us.put((char)0x0905); us.put((char)0x0906); us.put((char)0x0907);
        us.put((char)0x0908); us.put((char)0x0909); us.put((char)0x090A); us.put((char)0x090B); us.put((char)0x090E); us.put((char)0x090F);
        us.put((char)0x0910); us.put((char)0x090D); us.put((char)0x0912); us.put((char)0x0913); us.put((char)0x0914); us.put((char)0x0911);
        us.put((char)0x0915); us.put((char)0x0916); us.put((char)0x0917); us.put((char)0x0918); us.put((char)0x0919); us.put((char)0x091A);
        us.put((char)0x091B); us.put((char)0x091C); us.put((char)0x091D); us.put((char)0x091E); us.put((char)0x091F); us.put((char)0x0920);
        us.put((char)0x0921); us.put((char)0x0922); us.put((char)0x0923); us.put((char)0x0924); us.put((char)0x0925); us.put((char)0x0926); 
        us.put((char)0x0927); us.put((char)0x0928); us.put((char)0x0929); us.put((char)0x092A); us.put((char)0x092B); us.put((char)0x092C); 
        us.put((char)0x092D); us.put((char)0x092E); us.put((char)0x092F); us.put((char)0x095F); us.put((char)0x0930); us.put((char)0x0931); 
        us.put((char)0x0932); us.put((char)0x0933); us.put((char)0x0934); us.put((char)0x0935); us.put((char)0x0936); us.put((char)0x0937); 
        us.put((char)0x0938); us.put((char)0x0939); us.put((char)0x200D); us.put((char)0x093E); us.put((char)0x093F); us.put((char)0x0940); 
        us.put((char)0x0941); us.put((char)0x0942); us.put((char)0x0943); us.put((char)0x0946); us.put((char)0x0947); us.put((char)0x0948); 
        us.put((char)0x0945); us.put((char)0x094A); us.put((char)0x094B); us.put((char)0x094C); us.put((char)0x0949); us.put((char)0x094D); 
        us.put((char)0x093D); us.put((char)0x0966); us.put((char)0x0967); us.put((char)0x0968); us.put((char)0x0969); us.put((char)0x096A); 
        us.put((char)0x096B); us.put((char)0x096C); us.put((char)0x096D); us.put((char)0x096E); us.put((char)0x096F); 
        
        bs.put((byte)0xEF); bs.put((byte)0x42);
        bs.put((byte)0xA1); bs.put((byte)0xA2); bs.put((byte)0xA3); bs.put((byte)0xA4); bs.put((byte)0xA5); bs.put((byte)0xA6);
        bs.put((byte)0xA7); bs.put((byte)0xA8); bs.put((byte)0xA9); bs.put((byte)0xAA); bs.put((byte)0xAB); bs.put((byte)0xAC); 
        bs.put((byte)0xAD); bs.put((byte)0xAE); bs.put((byte)0xAF); bs.put((byte)0xB0); bs.put((byte)0xB1); bs.put((byte)0xB2); 
        bs.put((byte)0xB3); bs.put((byte)0xB4); bs.put((byte)0xB5); bs.put((byte)0xB6); bs.put((byte)0xB7); bs.put((byte)0xB8); 
        bs.put((byte)0xB9); bs.put((byte)0xBA); bs.put((byte)0xBB); bs.put((byte)0xBC); bs.put((byte)0xBD); bs.put((byte)0xBE); 
        bs.put((byte)0xBF); bs.put((byte)0xC0); bs.put((byte)0xC1); bs.put((byte)0xC2); bs.put((byte)0xC3); bs.put((byte)0xC4); 
        bs.put((byte)0xC5); bs.put((byte)0xC6); bs.put((byte)0xC7); bs.put((byte)0xC8); bs.put((byte)0xC9); bs.put((byte)0xCA); 
        bs.put((byte)0xCB); bs.put((byte)0xCC); bs.put((byte)0xCD); bs.put((byte)0xCE); bs.put((byte)0xCF); bs.put((byte)0xD0); 
        bs.put((byte)0xD1); bs.put((byte)0xD2); bs.put((byte)0xD3); bs.put((byte)0xD4); bs.put((byte)0xD5); bs.put((byte)0xD6); 
        bs.put((byte)0xD7); bs.put((byte)0xD8); bs.put((byte)0xD9); bs.put((byte)0xDA); bs.put((byte)0xDB); bs.put((byte)0xDC); 
        bs.put((byte)0xDD); bs.put((byte)0xDE); bs.put((byte)0xDF); bs.put((byte)0xE0); bs.put((byte)0xE1); bs.put((byte)0xE2); 
        bs.put((byte)0xE3); bs.put((byte)0xE4); bs.put((byte)0xE5); bs.put((byte)0xE6); bs.put((byte)0xE7); bs.put((byte)0xE8); 
        bs.put((byte)0xEA); bs.put((byte)0xE9); bs.put((byte)0xF1); bs.put((byte)0xF2); bs.put((byte)0xF3); bs.put((byte)0xF4); 
        bs.put((byte)0xF5); bs.put((byte)0xF6); bs.put((byte)0xF7); bs.put((byte)0xF8); bs.put((byte)0xF9); bs.put((byte)0xFA); 
        
        bsr.put((byte)0xA1); bsr.put((byte)0xA2); bsr.put((byte)0xA3); bsr.put((byte)0xA4); bsr.put((byte)0xA5); bsr.put((byte)0xA6);
        bsr.put((byte)0xA7); bsr.put((byte)0xA8); bsr.put((byte)0xA9); bsr.put((byte)0xAA); bsr.put((byte)0xAB); bsr.put((byte)0xAC); 
        bsr.put((byte)0xAD); bsr.put((byte)0xAE); bsr.put((byte)0xAF); bsr.put((byte)0xB0); bsr.put((byte)0xB1); bsr.put((byte)0xB2); 
        bsr.put((byte)0xB3); bsr.put((byte)0xB4); bsr.put((byte)0xB5); bsr.put((byte)0xB6); bsr.put((byte)0xB7); bsr.put((byte)0xB8); 
        bsr.put((byte)0xB9); bsr.put((byte)0xBA); bsr.put((byte)0xBB); bsr.put((byte)0xBC); bsr.put((byte)0xBD); bsr.put((byte)0xBE); 
        bsr.put((byte)0xBF); bsr.put((byte)0xC0); bsr.put((byte)0xC1); bsr.put((byte)0xC2); bsr.put((byte)0xC3); bsr.put((byte)0xC4); 
        bsr.put((byte)0xC5); bsr.put((byte)0xC6); bsr.put((byte)0xC7); bsr.put((byte)0xC8); bsr.put((byte)0xC9); bsr.put((byte)0xCA); 
        bsr.put((byte)0xCB); bsr.put((byte)0xCC); bsr.put((byte)0xCD); bsr.put((byte)0xCE); bsr.put((byte)0xCF); bsr.put((byte)0xD0); 
        bsr.put((byte)0xD1); bsr.put((byte)0xD2); bsr.put((byte)0xD3); bsr.put((byte)0xD4); bsr.put((byte)0xD5); bsr.put((byte)0xD6); 
        bsr.put((byte)0xD7); bsr.put((byte)0xD8); bsr.put((byte)0xD9); bsr.put((byte)0xDA); bsr.put((byte)0xDB); bsr.put((byte)0xDC); 
        bsr.put((byte)0xDD); bsr.put((byte)0xDE); bsr.put((byte)0xDF); bsr.put((byte)0xE0); bsr.put((byte)0xE1); bsr.put((byte)0xE2); 
        bsr.put((byte)0xE3); bsr.put((byte)0xE4); bsr.put((byte)0xE5); bsr.put((byte)0xE6); bsr.put((byte)0xE7); bsr.put((byte)0xE8); 
        bsr.put((byte)0xEA); bsr.put((byte)0xE9); bsr.put((byte)0xF1); bsr.put((byte)0xF2); bsr.put((byte)0xF3); bsr.put((byte)0xF4); 
        bsr.put((byte)0xF5); bsr.put((byte)0xF6); bsr.put((byte)0xF7); bsr.put((byte)0xF8); bsr.put((byte)0xF9); bsr.put((byte)0xFA); 
        
        //test Soft Halant
        us.put((char)0x0915); us.put((char)0x094d); us.put((char)0x200D);
        bs.put((byte)0xB3); bs.put((byte)0xE8); bs.put((byte)0xE9);
        bsr.put((byte)0xB3); bsr.put((byte)0xE8); bsr.put((byte)0xE9);
        
        //test explicit halant
        us.put((char)0x0915); us.put((char)0x094D); us.put((char)0x200C);
        bs.put((byte)0xB3); bs.put((byte)0xE8); bs.put((byte)0xE8);
        bsr.put((byte)0xB3); bsr.put((byte)0xE8); bsr.put((byte)0xE8);
        
        //test double danda
        us.put((char)0x0965); 
        bs.put((byte)0xEA); bs.put((byte)0xEA); 
        bsr.put((byte)0xEA); bsr.put((byte)0xEA); 
        
        //test ASCII
        us.put((char)0x1B); us.put((char)0x24); us.put((char)0x29); us.put((char)0x47); us.put((char)0x0E); us.put((char)0x23);
        us.put((char)0x21); us.put((char)0x23); us.put((char)0x22); us.put((char)0x23); us.put((char)0x23); us.put((char)0x23);
        us.put((char)0x24); us.put((char)0x23); us.put((char)0x25); us.put((char)0x23); us.put((char)0x26); us.put((char)0x23);
        us.put((char)0x27); us.put((char)0x23); us.put((char)0x28); us.put((char)0x23); us.put((char)0x29); us.put((char)0x23);
        us.put((char)0x2A); us.put((char)0x23); us.put((char)0x2B); us.put((char)0x0F); us.put((char)0x2F); us.put((char)0x2A);
        
        bs.put((byte)0x1B); bs.put((byte)0x24); bs.put((byte)0x29); bs.put((byte)0x47); bs.put((byte)0x0E); bs.put((byte)0x23);
        bs.put((byte)0x21); bs.put((byte)0x23); bs.put((byte)0x22); bs.put((byte)0x23); bs.put((byte)0x23); bs.put((byte)0x23);
        bs.put((byte)0x24); bs.put((byte)0x23); bs.put((byte)0x25); bs.put((byte)0x23); bs.put((byte)0x26); bs.put((byte)0x23);
        bs.put((byte)0x27); bs.put((byte)0x23); bs.put((byte)0x28); bs.put((byte)0x23); bs.put((byte)0x29); bs.put((byte)0x23);
        bs.put((byte)0x2A); bs.put((byte)0x23); bs.put((byte)0x2B); bs.put((byte)0x0F); bs.put((byte)0x2F); bs.put((byte)0x2A);
        
        bsr.put((byte)0x1B); bsr.put((byte)0x24); bsr.put((byte)0x29); bsr.put((byte)0x47); bsr.put((byte)0x0E); bsr.put((byte)0x23);
        bsr.put((byte)0x21); bsr.put((byte)0x23); bsr.put((byte)0x22); bsr.put((byte)0x23); bsr.put((byte)0x23); bsr.put((byte)0x23);
        bsr.put((byte)0x24); bsr.put((byte)0x23); bsr.put((byte)0x25); bsr.put((byte)0x23); bsr.put((byte)0x26); bsr.put((byte)0x23);
        bsr.put((byte)0x27); bsr.put((byte)0x23); bsr.put((byte)0x28); bsr.put((byte)0x23); bsr.put((byte)0x29); bsr.put((byte)0x23);
        bsr.put((byte)0x2A); bsr.put((byte)0x23); bsr.put((byte)0x2B); bsr.put((byte)0x0F); bsr.put((byte)0x2F); bsr.put((byte)0x2A);
        
        //test from Lotus
        //Some of the Lotus ISCII code points have been changed or commented out.
        us.put((char)0x0061); us.put((char)0x0915); us.put((char)0x000D); us.put((char)0x000A); us.put((char)0x0996); us.put((char)0x0043);
        us.put((char)0x0930); us.put((char)0x094D); us.put((char)0x200D); us.put((char)0x0901); us.put((char)0x000D); us.put((char)0x000A);
        us.put((char)0x0905); us.put((char)0x0985); us.put((char)0x0043); us.put((char)0x0915); us.put((char)0x0921); us.put((char)0x002B);
        us.put((char)0x095F); 
        bs.put((byte)0x61); bs.put((byte)0xB3);
        bs.put((byte)0x0D); bs.put((byte)0x0A); 
        bs.put((byte)0xEF); bs.put((byte)0x42); 
        bs.put((byte)0xEF); bs.put((byte)0x43); bs.put((byte)0xB4); bs.put((byte)0x43);
        bs.put((byte)0xEF); bs.put((byte)0x42); bs.put((byte)0xCF); bs.put((byte)0xE8); bs.put((byte)0xE9); bs.put((byte)0xA1); bs.put((byte)0x0D); bs.put((byte)0x0A); bs.put((byte)0xEF); bs.put((byte)0x42);
        bs.put((byte)0xA4); bs.put((byte)0xEF); bs.put((byte)0x43); bs.put((byte)0xA4); bs.put((byte)0x43); bs.put((byte)0xEF);
        bs.put((byte)0x42); bs.put((byte)0xB3); bs.put((byte)0xBF); bs.put((byte)0x2B);
        bs.put((byte)0xCE);
        bsr.put((byte)0x61); bsr.put((byte)0xEF); bsr.put((byte)0x42); bsr.put((byte)0xEF); bsr.put((byte)0x30); bsr.put((byte)0xB3);
        bsr.put((byte)0x0D); bsr.put((byte)0x0A); bsr.put((byte)0xEF); bsr.put((byte)0x43); bsr.put((byte)0xB4); bsr.put((byte)0x43);
        bsr.put((byte)0xEF); bsr.put((byte)0x42); bsr.put((byte)0xCF); bsr.put((byte)0xE8); bsr.put((byte)0xD9); bsr.put((byte)0xEF);
        bsr.put((byte)0x42); bsr.put((byte)0xA1); bsr.put((byte)0x0D); bsr.put((byte)0x0A); bsr.put((byte)0xEF); bsr.put((byte)0x42);
        bsr.put((byte)0xA4); bsr.put((byte)0xEF); bsr.put((byte)0x43); bsr.put((byte)0xA4); bsr.put((byte)0x43); bsr.put((byte)0xEF);
        bsr.put((byte)0x42); bsr.put((byte)0xB3); bsr.put((byte)0xBF); bsr.put((byte)0x2B); bsr.put((byte)0xEF); bsr.put((byte)0x42);
        bsr.put((byte)0xCE);
        //end of test from Lotus
        
        //tamil range
        us.put((char)0x0B86); us.put((char)0x0B87); us.put((char)0x0B88);
        bs.put((byte)0xEF); bs.put((byte)0x44); bs.put((byte)0xA5); bs.put((byte)0xA6); bs.put((byte)0xA7);
        bsr.put((byte)0xEF); bsr.put((byte)0x44); bsr.put((byte)0xA5); bsr.put((byte)0xA6); bsr.put((byte)0xA7);
        
        //telugu range
        us.put((char)0x0C05); us.put((char)0x0C02); us.put((char)0x0C03); us.put((char)0x0C31);
        bs.put((byte)0xEF); bs.put((byte)0x45); bs.put((byte)0xA4); bs.put((byte)0xA2); bs.put((byte)0xA3); bs.put((byte)0xD0);
        bsr.put((byte)0xEF); bsr.put((byte)0x45); bsr.put((byte)0xA4); bsr.put((byte)0xA2); bsr.put((byte)0xA3); bsr.put((byte)0xD0);
        
        //kannada range
        us.put((char)0x0C85); us.put((char)0x0C82); us.put((char)0x0C83);
        bs.put((byte)0xEF); bs.put((byte)0x48); bs.put((byte)0xA4); bs.put((byte)0xA2); bs.put((byte)0xA3);
        bsr.put((byte)0xEF); bsr.put((byte)0x48); bsr.put((byte)0xA4); bsr.put((byte)0xA2); bsr.put((byte)0xA3);  
        
        //test Abbr sign and Anudatta
        us.put((char)0x0970); us.put((char)0x0952); us.put((char)0x0960); us.put((char)0x0944); us.put((char)0x090C); us.put((char)0x0962);
        us.put((char)0x0961); us.put((char)0x0963); us.put((char)0x0950); us.put((char)0x093D); us.put((char)0x0958); us.put((char)0x0959);
        us.put((char)0x095A); us.put((char)0x095B); us.put((char)0x095C); us.put((char)0x095D); us.put((char)0x095E); us.put((char)0x0020);
        us.put((char)0x094D); us.put((char)0x0930); us.put((char)0x0000); us.put((char)0x00A0); 
        bs.put((byte)0xEF); bs.put((byte)0x42); bs.put((byte)0xF0); bs.put((byte)0xBF); bs.put((byte)0xF0); bs.put((byte)0xB8);
        bs.put((byte)0xAA); bs.put((byte)0xE9); bs.put((byte)0xDF); bs.put((byte)0xE9); bs.put((byte)0xA6); bs.put((byte)0xE9);
        bs.put((byte)0xDB); bs.put((byte)0xE9); bs.put((byte)0xA7); bs.put((byte)0xE9); bs.put((byte)0xDC); bs.put((byte)0xE9);
        bs.put((byte)0xA1); bs.put((byte)0xE9); bs.put((byte)0xEA); bs.put((byte)0xE9); bs.put((byte)0xB3); bs.put((byte)0xE9);
        bs.put((byte)0xB4); bs.put((byte)0xE9); bs.put((byte)0xB5); bs.put((byte)0xE9); bs.put((byte)0xBA); bs.put((byte)0xE9);
        bs.put((byte)0xBF); bs.put((byte)0xE9); bs.put((byte)0xC0); bs.put((byte)0xE9); bs.put((byte)0xC9); bs.put((byte)0xE9);
        bs.put((byte)0x20); bs.put((byte)0xE8); bs.put((byte)0xCF); bs.put((byte)0x00); bs.put((byte)0xA0); 
        //bs.put((byte)0xEF); bs.put((byte)0x30); 
        bsr.put((byte)0xEF); bsr.put((byte)0x42); bsr.put((byte)0xF0); bsr.put((byte)0xBF); bsr.put((byte)0xF0); bsr.put((byte)0xB8);
        bsr.put((byte)0xAA); bsr.put((byte)0xE9); bsr.put((byte)0xDF); bsr.put((byte)0xE9); bsr.put((byte)0xA6); bsr.put((byte)0xE9);
        bsr.put((byte)0xDB); bsr.put((byte)0xE9); bsr.put((byte)0xA7); bsr.put((byte)0xE9); bsr.put((byte)0xDC); bsr.put((byte)0xE9);
        bsr.put((byte)0xA1); bsr.put((byte)0xE9); bsr.put((byte)0xEA); bsr.put((byte)0xE9); bsr.put((byte)0xB3); bsr.put((byte)0xE9);
        bsr.put((byte)0xB4); bsr.put((byte)0xE9); bsr.put((byte)0xB5); bsr.put((byte)0xE9); bsr.put((byte)0xBA); bsr.put((byte)0xE9);
        bsr.put((byte)0xBF); bsr.put((byte)0xE9); bsr.put((byte)0xC0); bsr.put((byte)0xE9); bsr.put((byte)0xC9); bsr.put((byte)0xE9);
        bsr.put((byte)0xD9); bsr.put((byte)0xE8); bsr.put((byte)0xCF); bsr.put((byte)0x00); bsr.put((byte)0xA0);  
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        bsr.limit(bsr.position());
        bsr.position(0);
        
        //round trip test
        try {
            smBufDecode(decoder, "ISCII-part1", bsr, us, false, true);
            smBufEncode(encoder, "ISCII-part2", us, bs); 
            smBufDecode(decoder, "ISCII-part3", bs, us, false, true);
        } catch (Exception ex) {
            errln("ISCII round trip test failed.");
        }
        
        //Test new characters in the ISCII charset
        encoder = cs.newEncoder();
        decoder = cs.newDecoder();
        char u_pts[] = {
                /* DEV */ (char)0x0904,
                /* PNJ */ (char)0x0A01, (char)0x0A03, (char)0x0A33, (char)0x0A70
            };
        byte b_pts[] = {
                /* DEV */ (byte)0xa4, (byte)0xe0,
                /* PNJ */ (byte)0xef, (byte)0x4b, (byte)0xa1, (byte)0xa3, (byte)0xd2, (byte)0xf0, (byte)0xbf
            };
        us = CharBuffer.allocate(u_pts.length);
        bs = ByteBuffer.allocate(b_pts.length);
        us.put(u_pts);
        bs.put(b_pts);
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        
        try {
            smBufDecode(decoder, "ISCII-update", bs, us, true, true);         
            bs.position(0);
            us.position(0);
            smBufEncode(encoder, "ISCII-update", us, bs, true, true);
        } catch (Exception ex) {
            errln("Error occurred while encoding/decoding ISCII with the new characters.");
        }
        
        //The rest of the code in this method is to provide better code coverage
        CharBuffer ccus = CharBuffer.allocate(0x10);
        ByteBuffer ccbs = ByteBuffer.allocate(0x10);
        
        //start of charset decoder code coverage code
        //test overflow buffer
        ccbs.put((byte)0x49);
        
        ccbs.limit(ccbs.position());
        ccbs.position(0);
        ccus.limit(0);
        ccus.position(0);
        
        try {
            smBufDecode(decoder, "ISCII-CC-DE-1", ccbs, ccus, true, false);
            errln("Exception while decoding ISCII should have been thrown.");
        }
        catch (Exception ex) {
        }
        
        ccbs.clear();
        ccus.clear();
        
        //test atr overflow buffer
        ccbs.put((byte)0xEF); ccbs.put((byte)0x40); ccbs.put((byte)0xEF); ccbs.put((byte)0x20);
        ccus.put((char)0x00);
        
        ccbs.limit(ccbs.position());
        ccbs.position(0);
        ccus.limit(ccus.position());
        ccus.position(0);
        
        try {
            smBufDecode(decoder, "ISCII-CC-DE-2", ccbs, ccus, true, false);
            errln("Exception while decoding ISCII should have been thrown.");
        }
        catch (Exception ex) {
        }
        
        //end of charset decoder code coverage code
        
        ccbs.clear();
        ccus.clear();
      
        //start of charset encoder code coverage code
        //test ascii overflow buffer
        ccus.put((char)0x41);
        
        ccus.limit(ccus.position());
        ccus.position(0);
        ccbs.limit(0);
        ccbs.position(0);
           
        try {
            smBufEncode(encoder, "ISCII-CC-EN-1", ccus, ccbs, true, false);
            errln("Exception while encoding ISCII should have been thrown.");
        }
        catch (Exception ex) {
        }
        
        ccbs.clear();
        ccus.clear();
        
        //test ascii overflow buffer
        ccus.put((char)0x0A);
        ccbs.put((byte)0x00);
        
        ccus.limit(ccus.position());
        ccus.position(0);
        ccbs.limit(ccbs.position());
        ccbs.position(0);
           
        try {
            smBufEncode(encoder, "ISCII-CC-EN-2", ccus, ccbs, true, false);
            errln("Exception while encoding ISCII should have been thrown.");
        }
        catch (Exception ex) {
        }
        
        ccbs.clear();
        ccus.clear();
        
        //test surrogate malform
        ccus.put((char)0x06E3);
        ccbs.put((byte)0x00);
        
        ccus.limit(ccus.position());
        ccus.position(0);
        ccbs.limit(ccbs.position());
        ccbs.position(0);
           
        try {
            smBufEncode(encoder, "ISCII-CC-EN-3", ccus, ccbs, true, false);
            errln("Exception while encoding ISCII should have been thrown.");
        }
        catch (Exception ex) {
        }
        
        ccbs.clear();
        ccus.clear();
        
        //test surrogate malform
        ccus.put((char)0xD801); ccus.put((char)0xDD01);
        ccbs.put((byte)0x00);
        
        ccus.limit(ccus.position());
        ccus.position(0);
        ccbs.limit(ccbs.position());
        ccbs.position(0);
           
        try {
            smBufEncode(encoder, "ISCII-CC-EN-4", ccus, ccbs, true, false);
            errln("Exception while encoding ISCII should have been thrown.");
        }
        catch (Exception ex) {
        }
        
        ccbs.clear();
        ccus.clear();
        
        //test trail surrogate malform
        ccus.put((char)0xDD01); 
        ccbs.put((byte)0x00);
        
        ccus.limit(ccus.position());
        ccus.position(0);
        ccbs.limit(ccbs.position());
        ccbs.position(0);
           
        try {
            smBufEncode(encoder, "ISCII-CC-EN-5", ccus, ccbs, true, false);
            errln("Exception while encoding ISCII should have been thrown.");
        }
        catch (Exception ex) {
        }
        
        ccbs.clear();
        ccus.clear();
        
        //test lead surrogates malform
        ccus.put((char)0xD801); ccus.put((char)0xD802); 
        ccbs.put((byte)0x00);
        
        ccus.limit(ccus.position());
        ccus.position(0);
        ccbs.limit(ccbs.position());
        ccbs.position(0);
           
        try {
            smBufEncode(encoder, "ISCII-CC-EN-6", ccus, ccbs, true, false);
            errln("Exception while encoding ISCII should have been thrown.");
        }
        catch (Exception ex) {
        }
        
        ccus.clear();
        ccbs.clear();
        
        //test overflow buffer
        ccus.put((char)0x0901); 
        ccbs.put((byte)0x00);
        
        ccus.limit(ccus.position());
        ccus.position(0);
        ccbs.limit(ccbs.position());
        ccbs.position(0);
           
        cs = provider.charsetForName("ISCII,version=0");
        encoder = cs.newEncoder();
        
        try {
            smBufEncode(encoder, "ISCII-CC-EN-7", ccus, ccbs, true, false);
            errln("Exception while encoding ISCII should have been thrown.");
        }
        catch (Exception ex) {
        }
        //end of charset encoder code coverage code
    }
    
    //Test for the IMAP Charset
    public void TestCharsetIMAP() {
        CharsetProvider provider = new CharsetProviderICU();
        Charset cs = provider.charsetForName("IMAP-mailbox-name");        
        CharsetEncoder encoder = cs.newEncoder();
        CharsetDecoder decoder = cs.newDecoder();
        
        CharBuffer us = CharBuffer.allocate(0x20);
        ByteBuffer bs = ByteBuffer.allocate(0x20);
        
        us.put((char)0x00A3); us.put((char)0x2020); us.put((char)0x41);
        
        bs.put((byte)0x26); bs.put((byte)0x41); bs.put((byte)0x4B); bs.put((byte)0x4D); bs.put((byte)0x67); bs.put((byte)0x49);
        bs.put((byte)0x41); bs.put((byte)0x2D); bs.put((byte)0x41);
        
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);

        smBufDecode(decoder, "IMAP", bs, us);
        smBufEncode(encoder, "IMAP", us, bs);
        
        //the rest of the code in this method is for better code coverage
        us.clear();
        bs.clear();
        
        //start of charset encoder code coverage
        //test buffer overflow
        us.put((char)0x0026); us.put((char)0x17A9); 
        bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00);
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        
        try {
            smBufEncode(encoder, "IMAP-EN-1", us, bs, true, false);
            errln("Exception while encoding IMAP (1) should have been thrown.");
        } catch(Exception ex) {
        }
        
        us.clear();
        bs.clear();
        
        //test buffer overflow
        us.put((char)0x17A9); us.put((char)0x0941);
        bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00);
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        
        try {
            smBufEncode(encoder, "IMAP-EN-2", us, bs, true, false);
            errln("Exception while encoding IMAP (2) should have been thrown.");
        } catch(Exception ex) {
        }
        
        us.clear();
        bs.clear();
        
        //test buffer overflow
        us.put((char)0x17A9); us.put((char)0x0941);
        bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00);   
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        
        try {
            smBufEncode(encoder, "IMAP-EN-3", us, bs, true, false);
            errln("Exception while encoding IMAP (3) should have been thrown.");
        } catch(Exception ex) {
        }
        
        us.clear();
        bs.clear();
        
        //test buffer overflow
        us.put((char)0x17A9); us.put((char)0x0941); us.put((char)0x0955);
        bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00);      
        bs.put((byte)0x00);
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        
        try {
            smBufEncode(encoder, "IMAP-EN-4", us, bs, true, false);
            errln("Exception while encoding IMAP (4) should have been thrown.");
        } catch(Exception ex) {
        }
        
        us.clear();
        bs.clear();
        
        //test buffer overflow
        us.put((char)0x17A9); us.put((char)0x0941); us.put((char)0x0955);
        bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00);  
        bs.put((byte)0x00); bs.put((byte)0x00); 
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        
        try {
            smBufEncode(encoder, "IMAP-EN-5", us, bs, true, false);
            errln("Exception while encoding IMAP (5) should have been thrown.");
        } catch(Exception ex) {
        }
        
        us.clear();
        bs.clear();
        
        //test buffer overflow
        us.put((char)0x17A9); us.put((char)0x0941); us.put((char)0x0955); us.put((char)0x0970);
        bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00);  
        bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00);
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        
        try {
            smBufEncode(encoder, "IMAP-EN-6", us, bs, true, false);
            errln("Exception while encoding IMAP (6) should have been thrown.");
        } catch(Exception ex) {
        }
        
        us.clear();
        bs.clear();
        
        //test buffer overflow
        us.put((char)0x17A9); us.put((char)0x0941);
        bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00);  bs.put((byte)0x00); bs.put((byte)0x00);
        bs.put((byte)0x00); 
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        
        try {
            smBufEncode(encoder, "IMAP-EN-7", us, bs, true, true);
            errln("Exception while encoding IMAP (7) should have been thrown.");
        } catch(Exception ex) {
        }
        
        us.clear();
        bs.clear();
        
        //test flushing
        us.put((char)0x17A9); us.put((char)0x0941); 
        bs.put((byte)0x26); bs.put((byte)0x46); bs.put((byte)0x36); bs.put((byte)0x6b);  bs.put((byte)0x4a); bs.put((byte)0x51);
        bs.put((byte)0x51); bs.put((byte)0x2d);
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        
        try {
            smBufEncode(encoder, "IMAP-EN-8", us, bs, true, true);
        } catch(Exception ex) {
            errln("Exception while encoding IMAP (8) should not have been thrown.");
        }
        
        us = CharBuffer.allocate(0x08);
        bs = ByteBuffer.allocate(0x08);
        
        //test flushing buffer overflow
        us.put((char)0x0061);
        bs.put((byte)0x61); bs.put((byte)0x00);
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        
        try {
            smBufEncode(encoder, "IMAP-EN-9", us, bs, true, true);
        } catch(Exception ex) {
            errln("Exception while encoding IMAP (9) should not have been thrown.");
        }
        //end of charset encoder code coverage
        
        us = CharBuffer.allocate(0x10);
        bs = ByteBuffer.allocate(0x10);
        
        //start of charset decoder code coverage
        //test malform case 2
        us.put((char)0x0000); us.put((char)0x0000); 
        bs.put((byte)0x26); bs.put((byte)0x41); bs.put((byte)0x43); bs.put((byte)0x41);  
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        
        try {
            smBufDecode(decoder, "IMAP-DE-1", bs, us, true, false);
            errln("Exception while decoding IMAP (1) should have been thrown.");
        } catch(Exception ex) {
        }
        
        us.clear();
        bs.clear();
        
        //test malform case 5
        us.put((char)0x0000); us.put((char)0x0000); us.put((char)0x0000);
        bs.put((byte)0x26); bs.put((byte)0x41); bs.put((byte)0x41); bs.put((byte)0x41); 
        bs.put((byte)0x41); bs.put((byte)0x49); bs.put((byte)0x41);  
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        
        try {
            smBufDecode(decoder, "IMAP-DE-2", bs, us, true, false);
            errln("Exception while decoding IMAP (2) should have been thrown.");
        } catch(Exception ex) {
        }
        
        us.clear();
        bs.clear();
        
        //test malform case 7
        us.put((char)0x0000); us.put((char)0x0000); us.put((char)0x0000); us.put((char)0x0000);
        bs.put((byte)0x26); bs.put((byte)0x41); bs.put((byte)0x41); bs.put((byte)0x41); 
        bs.put((byte)0x41); bs.put((byte)0x41); bs.put((byte)0x41); bs.put((byte)0x42); 
        bs.put((byte)0x41);  
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        
        try {
            smBufDecode(decoder, "IMAP-DE-3", bs, us, true, false);
            errln("Exception while decoding IMAP (3) should have been thrown.");
        } catch(Exception ex) {
        }
        //end of charset decoder coder coverage  
    }
    
    //Test for charset UTF32LE to provide better code coverage
    public void TestCharsetUTF32LE() {
        CoderResult result = CoderResult.UNDERFLOW;
        CharsetProvider provider = new CharsetProviderICU();
        Charset cs = provider.charsetForName("UTF-32LE");        
        CharsetEncoder encoder = cs.newEncoder();
        //CharsetDecoder decoder = cs.newDecoder();
        
        CharBuffer us = CharBuffer.allocate(0x10);
        ByteBuffer bs = ByteBuffer.allocate(0x10);
        
        
        //test malform surrogate
        us.put((char)0xD901);
        bs.put((byte)0x00);
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        
        try {
            smBufEncode(encoder, "UTF32LE-EN-1", us, bs, true, false);
            errln("Exception while encoding UTF32LE (1) should have been thrown.");
        } catch (Exception ex) {
        }
        
        bs.clear();
        us.clear();
        
        //test malform surrogate
        us.put((char)0xD901); us.put((char)0xD902);
        bs.put((byte)0x00);
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        
        result = encoder.encode(us, bs, true);
        
        if (!result.isError() && !result.isOverflow()) {
            errln("Error while encoding UTF32LE (2) should have occurred.");
        }
        
        bs.clear();
        us.clear();
        
        //test overflow trail surrogate
        us.put((char)0xDD01); us.put((char)0xDD0E); us.put((char)0xDD0E);
        bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00);
        bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); 
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        
        result = encoder.encode(us, bs, true);
        
        if (!result.isError() && !result.isOverflow()) {
            errln("Error while encoding UTF32LE (3) should have occurred.");
        }
        
        bs.clear();
        us.clear();
        
        //test malform lead surrogate
        us.put((char)0xD90D); us.put((char)0xD90E);
        bs.put((byte)0x00); 
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        
        try {
            smBufEncode(encoder, "UTF32LE-EN-4", us, bs, true, false);
            errln("Exception while encoding UTF32LE (4) should have been thrown.");
        } catch (Exception ex) {
        }
        
        bs.clear();
        us.clear();
        
        //test overflow buffer
        us.put((char)0x0061);
        bs.put((byte)0x00); 
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        
        try {
            smBufEncode(encoder, "UTF32LE-EN-5", us, bs, true, false);
            errln("Exception while encoding UTF32LE (5) should have been thrown.");
        } catch (Exception ex) {
        }
        
        bs.clear();
        us.clear();
        
        //test malform trail surrogate
        us.put((char)0xDD01);
        bs.put((byte)0x00); 
        
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        
        try {
            smBufEncode(encoder, "UTF32LE-EN-6", us, bs, true, false);
            errln("Exception while encoding UTF32LE (6) should have been thrown.");
        } catch (Exception ex) {
        }
    }			
    
    //Test for charset UTF16LE to provide better code coverage
    public void TestCharsetUTF16LE() {
        CoderResult result = CoderResult.UNDERFLOW;
        CharsetProvider provider = new CharsetProviderICU();
        Charset cs = provider.charsetForName("UTF-16LE");        
        CharsetEncoder encoder = cs.newEncoder();
        //CharsetDecoder decoder = cs.newDecoder();
        
        // Test for malform and change fromUChar32 for next call
        char u_pts1[] = {
                (char)0xD805, 
                (char)0xDC01, (char)0xDC02, (char)0xDC03,
                (char)0xD901, (char)0xD902
                };
        byte b_pts1[] = {
                (byte)0x00, 
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00
                };
        
        CharBuffer us = CharBuffer.allocate(u_pts1.length);
        ByteBuffer bs = ByteBuffer.allocate(b_pts1.length);
        
        us.put(u_pts1);
        bs.put(b_pts1);
        
        us.limit(1);
        us.position(0);
        bs.limit(1);
        bs.position(0);
       
        result = encoder.encode(us, bs, true);
        
        if (!result.isMalformed()) {
            errln("Error while encoding UTF-16LE (1) should have occured.");
        }
        
        // Test for malform surrogate from previous buffer
        us.limit(4);
        us.position(1);
        bs.limit(7);
        bs.position(1);
        
        result = encoder.encode(us, bs, true);
        
        if (!result.isMalformed()) {
            errln("Error while encoding UTF-16LE (2) should have occured.");
        }       
        
        // Test for malform trail surrogate
        encoder.reset();
        
        us.limit(1);
        us.position(0);
        bs.limit(1);
        bs.position(0);
       
        result = encoder.encode(us, bs, true);    
        
        us.limit(6);
        us.position(4);
        bs.limit(4);
        bs.position(1);
        
        result = encoder.encode(us, bs, true);
        
        if (!result.isMalformed()) {
            errln("Error while encoding UTF-16LE (3) should have occured.");
        }          
    }
    
    //provide better code coverage for the generic charset UTF32
    public void TestCharsetUTF32() {
        CoderResult result = CoderResult.UNDERFLOW;
        CharsetProvider provider = new CharsetProviderICU();
        Charset cs = provider.charsetForName("UTF-32");        
        CharsetDecoder decoder = cs.newDecoder();
        CharsetEncoder encoder = cs.newEncoder();
        
        //start of decoding code coverage
        char us_array[] = {
                0x0000, 0x0000, 0x0000, 0x0000,
            };
        
        byte bs_array1[] = {
                (byte)0x00, (byte)0x00, (byte)0xFE, (byte)0xFF,
                (byte)0x00, (byte)0x00, (byte)0x04, (byte)0x43,
                (byte)0xFF, (byte)0xFE, (byte)0x00, (byte)0x00,
                (byte)0x43, (byte)0x04, (byte)0x00, (byte)0x00,
            };
        
        byte bs_array2[] = {
                (byte)0xFF, (byte)0xFE, (byte)0x00, (byte)0x00,
                (byte)0x43, (byte)0x04, (byte)0x00, (byte)0x00,
            };
        
        CharBuffer us = CharBuffer.allocate(us_array.length);
        ByteBuffer bs = ByteBuffer.allocate(bs_array1.length);
        
        us.put(us_array);
        bs.put(bs_array1);
        
        us.limit(us.position());
        us.position(0);
        bs.limit(bs.position());
        bs.position(0);
            
        try {
            smBufDecode(decoder, "UTF32-DE-1", bs, us, true, false);
            errln("Malform exception while decoding UTF32 charset (1) should have been thrown.");
        } catch (Exception ex) {
        }
        
        decoder = cs.newDecoder();
        
        bs = ByteBuffer.allocate(bs_array2.length);
        bs.put(bs_array2);
        
        us.limit(4);
        us.position(0);
        bs.limit(bs.position());
        bs.position(0);
            
        try {
            smBufDecode(decoder, "UTF32-DE-2", bs, us, true, false);
            errln("Malform exception while decoding UTF32 charset (2) should have been thrown.");
        } catch (Exception ex) {
        }
        
        //Test malform exception
        bs.clear();
        us.clear();
        
        bs.put((byte)0x00); bs.put((byte)0xFE); bs.put((byte)0xFF); bs.put((byte)0x00); bs.put((byte)0x00);
        us.put((char)0x0000);
        
        us.limit(us.position());
        us.position(0);
        bs.limit(bs.position());
        bs.position(0);
        
        try {
            smBufDecode(decoder, "UTF32-DE-3", bs, us, true, false);
            errln("Malform exception while decoding UTF32 charset (3) should have been thrown.");
        } catch (Exception ex) {
        }
        
        //Test BOM testing
        bs.clear();
        us.clear();
        
        bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0xFF); bs.put((byte)0xFE); 
        us.put((char)0x0000);
        
        us.limit(us.position());
        us.position(0);
        bs.limit(bs.position());
        bs.position(0);
        
        try {
            smBufDecode(decoder, "UTF32-DE-4", bs, us, true, false);
            errln("Malform exception while decoding UTF32 charset (4) should have been thrown.");
        } catch (Exception ex) {
        }
        //end of decoding code coverage
        
        //start of encoding code coverage
        us = CharBuffer.allocate(0x10);
        bs = ByteBuffer.allocate(0x10);
        
        //test wite BOM overflow error
        us.put((char)0xDC01);
        bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00);
        
        us.limit(us.position());
        us.position(0);
        bs.limit(bs.position());
        bs.position(0);
        
        result = encoder.encode(us, bs, true);
        if (!result.isOverflow()) {
            errln("Buffer overflow error while encoding UTF32 charset (1) should have occurred.");
        }
        
        us.clear();
        bs.clear();
        
        //test malform surrogate and store value in fromChar32
        us.put((char)0xD801); us.put((char)0xD802);
        bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00);
        
        us.limit(us.position());
        us.position(0);
        bs.limit(bs.position());
        bs.position(0);
        
        result = encoder.encode(us, bs, true);
        if (!result.isOverflow()) {
            errln("Overflow error while encoding UTF32 charset (2) should have occurred.");
        }    
        
        us.clear();
        bs.clear();
        
        //test malform surrogate
        us.put((char)0x0000); us.put((char)0xD902);
        bs.put((byte)0x00); 
        
        us.limit(us.position());
        us.position(0);
        bs.limit(bs.position());
        bs.position(0);
        
        result = encoder.encode(us, bs, true);
        if (!result.isOverflow()) {
            errln("Overflow error while encoding UTF32 charset (3) should have occurred.");
        } 
        
        us.clear();
        bs.clear();
        
        //test malform surrogate
        encoder.reset();
        us.put((char)0xD801);
        bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00);
   
        us.limit(us.position());
        us.position(0);
        bs.limit(bs.position());
        bs.position(0);
        
        result = encoder.encode(us, bs, true);
        if (!result.isMalformed()) {
            errln("Malform error while encoding UTF32 charset (4) should have occurred.");
        } 
        
        us.clear();
        bs.clear();
        
        //test overflow surrogate
        us.put((char)0x0000); us.put((char)0xDDE1); us.put((char)0xD915); us.put((char)0xDDF2);
        bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); 
   
        us.limit(us.position());
        us.position(0);
        bs.limit(bs.position());
        bs.position(0);
        
        result = encoder.encode(us, bs, true);
        if (!result.isOverflow()) {
            errln("Overflow error while encoding UTF32 charset (5) should have occurred.");
        } 
        
        us.clear();
        bs.clear();
        
        //test malform surrogate
        encoder.reset();
        us.put((char)0xDDE1);
        bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00); bs.put((byte)0x00);
   
        us.limit(us.position());
        us.position(0);
        bs.limit(bs.position());
        bs.position(0);
        
        result = encoder.encode(us, bs, true);
        if (!result.isMalformed()) {
            errln("Malform error while encoding UTF32 charset (6) should have occurred.");
        } 
        
        
        //end of encoding code coverage
    }
}
