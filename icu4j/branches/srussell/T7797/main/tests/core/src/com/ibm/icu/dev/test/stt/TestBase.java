/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.dev.test.stt;

/**
 * Base functionality for the handler tests.
 */
public class TestBase {

    static final private char LRM = 0x200E;

    static final private char RLM = 0x200F;

    static final private char LRE = 0x202A;

    static final private char RLE = 0x202B;

    static final private char PDF = 0x202C;

    public static String toPseudo(String text) {
        char[] chars = text.toCharArray();
        int len = chars.length;

        for (int i = 0; i < len; i++) {
            char c = chars[i];
            if (c >= 'A' && c <= 'Z')
                chars[i] = (char) (c + 'a' - 'A');
            else if (c >= 0x05D0 && c < 0x05EA)
                chars[i] = (char) (c + 'A' - 0x05D0);
            else if (c == 0x05EA)
                chars[i] = '~';
            else if (c == 0x0644)
                chars[i] = '#';
            else if (c >= 0x0665 && c <= 0x0669)
                chars[i] = (char) (c + '5' - 0x0665);
            else if (c == LRM)
                chars[i] = '@';
            else if (c == RLM)
                chars[i] = '&';
            else if (c == LRE)
                chars[i] = '>';
            else if (c == RLE)
                chars[i] = '<';
            else if (c == PDF)
                chars[i] = '^';
            else if (c == '\n')
                chars[i] = '|';
            else if (c == '\r')
                chars[i] = '`';
        }
        return new String(chars);
    }

    public static String toUT16(String text) {
        char[] chars = text.toCharArray();
        int len = chars.length;

        for (int i = 0; i < len; i++) {
            char c = chars[i];
            if (c >= '5' && c <= '9')
                chars[i] = (char) (0x0665 + c - '5');
            else if (c >= 'A' && c <= 'Z')
                chars[i] = (char) (0x05D0 + c - 'A');
            else if (c == '~')
                chars[i] = (char) (0x05EA);
            else if (c == '#')
                chars[i] = (char) (0x0644);
            else if (c == '@')
                chars[i] = LRM;
            else if (c == '&')
                chars[i] = RLM;
            else if (c == '>')
                chars[i] = LRE;
            else if (c == '<')
                chars[i] = RLE;
            else if (c == '^')
                chars[i] = PDF;
            else if (c == '|')
                chars[i] = '\n';
            else if (c == '`')
                chars[i] = '\r';
        }
        return new String(chars);
    }

    static String array_display(int[] array) {
        if (array == null) {
            return "null";
        }
        StringBuffer sb = new StringBuffer(50);
        int len = array.length;
        for (int i = 0; i < len; i++) {
            sb.append(array[i]);
            sb.append(' ');
        }
        return sb.toString();
    }

    static int assertNull(Object obj) {
        if (obj == null)
            return 0;
        try {
            throw new AssertionError("Object should be null");
        } catch (AssertionError e) {
            e.printStackTrace();
            return 1;
        }
    }

    static int assertNotNull(Object obj) {
        if (obj != null)
            return 0;
        try {
            throw new AssertionError("Object should not be null");
        } catch (AssertionError e) {
            e.printStackTrace();
            return 1;
        }
    }

    static int assertTrue(String msg, boolean flag) {
        if (flag)
            return 0;
        try {
            throw new AssertionError(msg + ":  Condition should be true");
        } catch (AssertionError e) {
            e.printStackTrace();
            return 1;
        }
    }

    static int assertFalse(String msg, boolean flag) {
        if (!flag)
            return 0;
        try {
            throw new AssertionError(msg + ":  Condition should be false");
        } catch (AssertionError e) {
            e.printStackTrace();
            return 1;
        }
    }

    static int assertEquals(String msg, int i1, int i2) {
        if (i1 == i2)
            return 0;
        try {
            throw new AssertionError(msg + ":  \"" + i1 + "\" and  \"" + i2
                    + "\" should be equal.");
        } catch (AssertionError e) {
            e.printStackTrace();
            return 1;
        }
    }

    static int assertEquals(String msg, Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null)
            return 0;
        if (obj1 != null && obj2 != null && obj1.equals(obj2))
            return 0;
        try {
            throw new AssertionError(msg + ":  Object \"" + obj1
                    + "\" and object \"" + obj2 + "\" should be equal.");
        } catch (AssertionError e) {
            e.printStackTrace();
            return 1;
        }
    }

    static int assertEquals(String msg, String s1, String s2) {
        if (s1.equals(s2))
            return 0;
        try {
            throw new AssertionError(msg + ":  String \"" + s1
                    + "\" and string \"" + s2 + "\" should be equal.");
        } catch (AssertionError e) {
            e.printStackTrace();
            return 1;
        }

    }

}
