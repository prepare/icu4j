/*
 *******************************************************************************
 * Copyright (C) 2002-2004, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/tool/localeconverter/ICU2LocaleWriter.java,v $ 
 * $Date: 2002/01/31 01:21:32 $ 
 * $Revision: 1.1 $
 *
 *****************************************************************************************
 */
 
package com.ibm.tools.localeconverter;

import java.io.*;
import java.util.*;

public class ICU2LocaleWriter extends LocaleWriter {
    public ICU2LocaleWriter(PrintStream out) {
        super(out);
            //{{INIT_CONTROLS
        //}}
}
    public ICU2LocaleWriter(PrintStream out, PrintStream err) {
        super(out, err);
    }
    protected void open(Locale locale) {
        print(locale.toString());
        println(" {");
        indent();
    }
    protected void write(String tag, String value) {
        print(tag);
        print(" { ");
        printString(value);
        println(" }");
    }
    protected void write(String tag, String[] value) {
        if (tag != null) {
            print(tag);
            println(" { ");
        } else {
            println("{");
        }
        indent();
            for (int i = 0; i < value.length; i++) {
                printString(value[i]);
                println(",");
            }
        outdent();
        println("}");
    }
    protected void write(String tag, Object o) {
        if ("CollationElements".equals(tag)) {
            writeTagged(tag,(Object[][])o);
        } else if (!(o instanceof CollationItem[])) {
            super.write(tag, o);
        } else {
            CollationItem[] items = (CollationItem[])o;
            print("CollationElements");
            println(" { ");
            for (int i = 0; i < items.length; i++) {
                if(items[i]!=null){
                    printString(items[i].toString());
                    if (items[i].comment != null) {
                        tabTo(30);
                        print("//");
                        println(items[i].comment);
                    }
                }
            }
        }
    }
    protected void writeTagged(String tag, Object[][] value) {
        print(tag);
        println(" { ");
        indent();
            for (int i = 0; i < value.length; i++) {
                write((String)value[i][0], value[i][1]);
            }
        outdent();
        println("}");
    }
    protected void write2D(String tag, String[][] value) {
        print(tag);
        println(" { ");
        indent();
            for (int i = 0; i < value.length; i++) {
                write(null, value[i]);
            }
        outdent();
        println("}");
    }
    protected void writeTagged(String tag, String[][] value) {
        print(tag);
        println(" { ");
        indent();
            for (int i = 0; i < value.length; i++) {
                write(value[i][0], value[i][1]);
            }
        outdent();
        println("}");
    }
    protected void close() {
        outdent();
        println("}");
    }

    protected String getStringJoiningCharacter() {
        return "";
    }
    
    protected boolean isEscapeChar(final char c) {
        return true;
    }
    protected String getEscapeChar() {
        return "%u";
    }
    //{{DECLARE_CONTROLS
    //}}
}