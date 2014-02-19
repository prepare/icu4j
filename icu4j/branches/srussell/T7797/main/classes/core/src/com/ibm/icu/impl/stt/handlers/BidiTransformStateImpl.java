/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl.stt.handlers;

import com.ibm.icu.text.BidiTransformState;

/**
 * state object to house transformation information across transform calls. The type of data is
 * left to the implementation in the handler. 
 *
 */
public class BidiTransformStateImpl implements BidiTransformState
{
    /**
     * custom state object for transformations that require state information across
     * transforms
     */
    Object state = null;
    
    /**
     * reset state processing to its initial state
     */
    public void clear()
    {
        state = null;
    }
    
    
    /**
     * Gets the state established by the last text processing call. This is
     * <code>null</code> if the last text processing call had nothing to pass 
     * to the next call.
     * @return the state
     */
    public Object getState() 
    {
        return state;
    }

    
    /**
     * Sets the state for the next text processing call.
     * @param state the state to set
     */
    public void setState(Object state) 
    {
        this.state = state;
    }
    
}
