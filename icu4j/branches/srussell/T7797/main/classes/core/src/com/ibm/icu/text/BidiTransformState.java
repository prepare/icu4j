/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

/**
 * state information container for transformations that require previous transformation information
 * across transformation calls. 
 * <br/><br/>
 * Handler implementation determines details. 
 */
public interface BidiTransformState 
{
    /**
     * reset BiDi transformation state to an initial state
     */
    public void clear();
}
