 /*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

/**
 * Thrown by various format methods when appending output fails.
 * @draft ICU 53.
 * @provisional
 */
public class AppendFailureException extends RuntimeException {
    
    private static final long serialVersionUID = 8945207986164199285L;

    /**
     * Constructs a new AppendFailureException with the specified cause and a detail message of
     * (cause==null ? null : cause.toString())
     * 
     * @param cause the cause
     * @draft ICU 53
     * @provisional
     */
    public AppendFailureException(Throwable cause) {
        super(cause);
    }
}