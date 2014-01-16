/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.impl.stt.handlers;

/**
 * Handler adapted to processing structured text with the following format:
 * 
 * <pre>
 * system(user)
 * </pre>
 */
public class SystemAndUser extends Single {

    public SystemAndUser() {
        super("(");
    }
}