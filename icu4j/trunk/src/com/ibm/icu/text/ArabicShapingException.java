/*
*******************************************************************************
*   Copyright (C) 2001-2006, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*/

package com.ibm.icu.text;

/**
 * Thrown by ArabicShaping when there is a shaping error.
 * @stable ICU 2.0
 */
public final class ArabicShapingException extends Exception {
    // generated by serialver from JDK 1.4.1_01
    static final long serialVersionUID = 5261531805497260490L;
    
    /**
     * Constuct the exception with the given message
     * @param msg the error message for this exception
     * 
     * @internal revisit for ICU 3.6
     * @deprecated This API is ICU internal only.
     */
    public ArabicShapingException(String message) {
        super(message);
    }
}
