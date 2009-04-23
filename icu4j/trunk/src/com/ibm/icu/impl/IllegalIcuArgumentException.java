//##header
/*
 *******************************************************************************
 * Copyright (C) 2009, Google, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

/**
 * @author markdavis
 *
 */
public class IllegalIcuArgumentException extends IllegalArgumentException {
    private static final long serialVersionUID = 3789261542830211225L;

    public IllegalIcuArgumentException(String errorMessage) {
        super(errorMessage);
    }
    
    public synchronized Throwable initCause(Throwable cause) {
//#if defined(FOUNDATION10) || defined(J2SE13)
//## //JDK1.3 / CDC Foundation 1.0 specific code prefixed by //##
//#else
// Code for all other environments
    return super.initCause(cause);
//#endif 
    }
}
