/*
 *******************************************************************************
 * Copyright (C) 2002-2004, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/tool/localeconverter/SpaceTransition.java,v $ 
 * $Date: 2002/01/31 01:22:25 $ 
 * $Revision: 1.1 $
 *
 *****************************************************************************************
 */
package com.ibm.tools.localeconverter;

import java.io.*;
import java.util.*;

public class SpaceTransition extends ComplexTransition {
    public static final SpaceTransition GLOBAL = new SpaceTransition(SUCCESS);
    public static final String SPACE_CHARS = " \t";
    
    public SpaceTransition(int success) {
        super(success);
            //{{INIT_CONTROLS
        //}}
    }
    public boolean accepts(int c) {
        return SPACE_CHARS.indexOf((char)c) >= 0;
    }
    protected Lex.Transition[][] getStates() {
        return states;
    }
    private static final Lex.Transition[][] states = {
        { //state 0: 
            new Lex.StringTransition(SPACE_CHARS, Lex.IGNORE_CONSUME, -1),
            new Lex.ParseExceptionTransition("illegal space character")
        },
        { //state 1:
            new Lex.EOFTransition(SUCCESS),
            new Lex.StringTransition(SPACE_CHARS, Lex.IGNORE_CONSUME, -1),
            new Lex.DefaultTransition(Lex.IGNORE_PUTBACK, SUCCESS)
        },
    };
    //{{DECLARE_CONTROLS
    //}}
}
