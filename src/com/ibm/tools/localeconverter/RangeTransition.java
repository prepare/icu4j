/*
 *******************************************************************************
 * Copyright (C) 2002-2004, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/tools/localeconverter/Attic/RangeTransition.java,v $ 
 * $Date: 2002/01/31 01:22:25 $ 
 * $Revision: 1.1 $
 *
 *****************************************************************************************
 */
 
package com.ibm.tools.localeconverter;

import java.io.*;
import java.util.*;

public class RangeTransition extends ComplexTransition {
    public static final RangeTransition GLOBAL = new RangeTransition(SUCCESS);
    public static final String RANGE_CHARS = "...";
    public RangeTransition(int success){
        super(success);
    }
    
    public boolean accepts(int c){
        return RANGE_CHARS.indexOf((char)c) >=0;
    }
    
    protected Lex.Transition[][]getStates(){
        return states;
    }
    private static final Lex.Transition[][] states= {

        { //state 0: 
            new Lex.StringTransition(RANGE_CHARS, Lex.IGNORE_CONSUME, -1),
            new Lex.ParseExceptionTransition("illegal space character")
        },
        { //state 1:
            new Lex.EOFTransition(SUCCESS),
            new Lex.StringTransition(RANGE_CHARS, Lex.IGNORE_CONSUME, -1),
            new Lex.DefaultTransition(Lex.IGNORE_PUTBACK, SUCCESS)
        },
    };
}
            
    