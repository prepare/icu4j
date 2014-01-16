/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.impl.stt.handlers;

/**
 * Handler adapted to processing URLs.
 */
public class Url extends TypeHandler {
    public Url() {
        super(":?#/@.[]");
    }
}