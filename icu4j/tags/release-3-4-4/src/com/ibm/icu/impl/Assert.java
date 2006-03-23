/*
*******************************************************************************
*   Copyright (C) 2005, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*/
package com.ibm.icu.impl;

// 1.3 compatibility layer
public class Assert {
    public static void fail(Exception e) {
        fail(e.toString()); // can't wrap exceptions in jdk 1.3
    }
    public static void fail(String msg) {
        throw new InternalError("failure '" + msg + "'");
    }
    public static void assrt(boolean val) {
        if (!val) throw new InternalError("assert failed");
    }
    public static void assrt(String msg, boolean val) {
        if (!val) throw new InternalError("assert '" + msg + "' failed");
    }
}
