/*
 *******************************************************************************
 * Copyright (C) 2002-2003, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/tool/localeconverter/RangeTransition.java,v $ 
 * $Date: 2003/12/20 03:06:56 $ 
 * $Revision: 1.4 $
 *
 *****************************************************************************************
 */
 
package com.ibm.icu.dev.tool.localeconverter;


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
            
    