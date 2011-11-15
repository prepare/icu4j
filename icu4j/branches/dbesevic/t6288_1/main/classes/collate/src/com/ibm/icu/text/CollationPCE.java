/*
 *******************************************************************************
 * Copyright (C) 2011, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.util.ArrayList;

/**
 * @author dbesevic
 *
 */
public class CollationPCE 
{
    protected static final int    BUFFER_LENGTH = 100;

    protected static final int    DEFAULT_BUFFER_SIZE = 16;
    protected static final int    BUFFER_GROW = 8;
    
    private PCEBuffer          pceBuffer;
    private RCEBuffer          rceBuffer;
    private int                strength;
    private boolean            toShift;
    private boolean            isShifted;
    private int                variableTop;

    public CollationPCE(CollationElementIterator elems)
    {
        init(elems.getCollator());  
        pceBuffer = new PCEBuffer();
        rceBuffer = new RCEBuffer();
    }

    public void init(Collator coll)
    {
        strength    = coll.getStrength();
//        TODO: implement this
//        toShift     = ucol_getAttribute(coll, UCOL_ALTERNATE_HANDLING, &status) == UCOL_SHIFTED;
        toShift     = false;
        isShifted   = false;
        variableTop = coll.getVariableTop() << 16;
    }
    
    public class RCEI 
    {
        int ce;
        int low;
        int high;    
    }

    public class RCEBuffer 
    {
        ArrayList<RCEI> buffer = null;
        int bufferSize;
        int bufferIndex;

        public RCEBuffer()
        {
            buffer = new ArrayList<RCEI>(DEFAULT_BUFFER_SIZE);
            bufferIndex = 0;
            bufferSize = DEFAULT_BUFFER_SIZE;
        }

        public boolean empty()
        {
            return bufferIndex <= 0;
        }
        
        public void put(int ce, int ixLow, int ixHigh)
        {
            if (bufferIndex >= bufferSize) {
                buffer.ensureCapacity(bufferSize + BUFFER_GROW);
                bufferSize += BUFFER_GROW;
            }
            
            RCEI tempBuf = new RCEI();

            tempBuf.ce   = ce;
            tempBuf.low  = ixLow;
            tempBuf.high = ixHigh;
            
            buffer.add(bufferIndex, tempBuf);

            bufferIndex += 1;
        }

        public RCEI get()
        {
            if (bufferIndex > 0) {
                return buffer.get(--bufferIndex);
            }

            return null;
        }
        
        public void reset()
        {
            bufferIndex = 0;
            buffer.clear();
        }
    }

    public class PCEI 
    {
        long ce;
        int low;
        int high;    
    }

    public class PCEBuffer 
    {
        ArrayList<PCEI> buffer = null;
        int bufferSize;
        int bufferIndex;

        public PCEBuffer()
        {
            buffer = new ArrayList<PCEI>(DEFAULT_BUFFER_SIZE);
            bufferIndex = 0;
            bufferSize = DEFAULT_BUFFER_SIZE;
        }

        public boolean empty()
        {
            return bufferIndex <= 0;
        }
        
        public void reset()
        {
            bufferIndex = 0;
        }
        
        public void put(long ce, int ixLow, int ixHigh)
        {
            if (bufferIndex >= bufferSize) {
                buffer.ensureCapacity(bufferSize + BUFFER_GROW);
                bufferSize += BUFFER_GROW;
            }
            
            PCEI tempBuf = new PCEI();

            tempBuf.ce   = ce;
            tempBuf.low  = ixLow;
            tempBuf.high = ixHigh;
            
            buffer.add(bufferIndex, tempBuf);

            bufferIndex += 1;
        }

        public PCEI get()
        {
            if (bufferIndex > 0) {
                return buffer.get(--bufferIndex);
            }

            return null;
        }
    }

    /**
     * @return the pceBuffer
     */
    public PCEBuffer getPceBuffer() {
        return pceBuffer;
    }

    /**
     * @return the strength
     */
    public int getStrength() {
        return strength;
    }

    /**
     * @return the toShift
     */
    public boolean toShift() {
        return toShift;
    }

    /**
     * @return the isShifted
     */
    public boolean isShifted() {
        return isShifted;
    }

    /**
     * @return the variableTop
     */
    public int getVariableTop() {
        return variableTop;
    }

    /**
     * @param isShifted the isShifted to set
     */
    public void setShifted(boolean isShifted) {
        this.isShifted = isShifted;
    }

    /**
     * @return the rceBuffer
     */
    public RCEBuffer getRceBuffer() {
        return rceBuffer;
    }
}
