/*
 *******************************************************************************
 * Copyright (C) 2013, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.util;

import java.io.Serializable;

import com.ibm.icu.impl.ICUConfig;

/**
 * Leniency provides a mechanism to support fine grain leniency control to improve upon a simple boolean state.
 * 
 * Configuration properties are read from ICUConfig.properties. The default leniency is read from ICUConfig.properties and
 * that value is used when calling setLenient(true). Individual leniency can be checked by calling isLenient(mask) where
 * mask is one of the public LENIENT_xxxxx class variables.   
 */
public class Leniency implements Serializable {

    private static final long serialVersionUID = -4793946280300419795L;

    /*
     * property keys for configuring leniency in ICUConfig.properties  
     */
    private static final String LENIENCY_FIELD_VALIDATION_PROPERTY      = "com.ibm.icu.util.leniency.FieldValidation";
    private static final String LENIENCY_ALLOW_WHITESPACE__PROPERTY     = "com.ibm.icu.util.leniency.AllowWhitespace";
    private static final String LENIENCY_ALLOW_NUMERIC_PROPERTY         = "com.ibm.icu.util.leniency.AllowNumeric";
    
    public enum Bit { 
        // general leniency indicator for instances where specific leniency is not {yet} defined
        LENIENT,
        // lenient field validation indicates that the code will do it's best not to fail. Examples would be to pin a value
        // to the fields min/max or adjust other fields in the object to enable a given field to be recalculated into a valid range. 
        FIELD_VALIDATION,
        // indicates tolerance of whitespace.
        ALLOW_WHITESPACE,
        // indicates tolerance of numeric data when String data may be assumed.
        ALLOW_NUMERIC 
    };

    /**
     * valid list of leniency bits
     */
    static private long validLeniency = 0; 
    static {
       Bit[] allBits = Bit.values();
       for (int i = 0; i < allBits.length; i++) {
           validLeniency |= allBits[i].ordinal();
       }
    }
    
    /**
     * class variable that holds the configured leniency defaults. The configured defaults will be obtained once and referenced as needed. 
     */
    private static volatile long defaults = -1;
    private static Object defaultsLock = new Object();
    
    /**
     * the currently leniency settings for this instance
     */
    private long bitMap = -1;

    /**
     * returns True if leniency has been set to true or still in it's default state. False if set to 
     * false explictly. 
     */
    public boolean isLenient() {
        if(getBitMap() == 0)
            return false;
            
        return true;
    }
    
    /**
     * True if the lenient bit specified by the supplied mask is turned on. Otherwise, false.
     */ 
    public boolean isLenient(Bit mask) {
        if( (getBitMap() & mask.ordinal()) == 0)
            return false;
            
        return true;
    }


    /**
     * sets this instances leniency bit map. If being set to true, the configured defaults are used. If being 
     * set to false the bit map is cleared.
     */
    public void setLenient(boolean flag) {
        if(flag == true)
            setLenientFlags(getDefaults() | Bit.LENIENT.ordinal());
        else
            setLenientFlags(0);
    }

    /**
     * set the leniency bit map to the supplied bit map  
     */
    public void setLenientFlags(long leniencyBits) {
        //TODO decide if this should be done with an OR of Leniency.LENIENT ?!?
        setBitMap(leniencyBits);        
    }

    /**
     * returns the configured defaults for leniency as configured in ICUConfig.properties 
     */
    private long getDefaults() {
        boolean bitOn;
        int results = 0;

        if(defaults != -1)
            return defaults;

        bitOn = ICUConfig.get(LENIENCY_FIELD_VALIDATION_PROPERTY, "true").equals("true");
        if(bitOn) results |= Bit.FIELD_VALIDATION.ordinal();
        
        bitOn = ICUConfig.get(LENIENCY_ALLOW_WHITESPACE__PROPERTY, "true").equals("true");
        if(bitOn) results |= Bit.ALLOW_WHITESPACE.ordinal();
        
        bitOn = ICUConfig.get(LENIENCY_ALLOW_NUMERIC_PROPERTY, "true").equals("true");
        if(bitOn) results |= Bit.ALLOW_NUMERIC.ordinal();
        
        synchronized(defaultsLock) {        
            if(defaults != -1)
                return defaults;
            
            defaults = results;
        }
        
        return results;
    }

    /**
     * @return the bitMap
     */
    protected long getBitMap() {
        return bitMap;
    }

    /**
     * @param bitMap the bitMap to set
     */
    protected void setBitMap(long newBitMap) {
        bitMap = newBitMap & validLeniency;
    }
    
}
