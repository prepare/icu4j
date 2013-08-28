/*
 *******************************************************************************
 * Copyright (C) 2013, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.util;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;

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
     * class variable that holds the configured leniency defaults. The configured defaults will be obtained once and referenced as needed. 
     */
    private static volatile Set<Bit> defaults = EnumSet.noneOf(Leniency.Bit.class);
    private static volatile boolean defaultsObtained = false; 
    private static Object defaultsLock = new Object();
    
    /**
     * the currently leniency settings for this instance
     */
    private Set<Bit> bitMap = null;
    private static Object bitMapLock = new Object();

    /**
     * returns True if leniency has been set to true or still in it's default state. False if set to 
     * false explictly. 
     */
    public boolean isLenient() {
        if(getBitMap() == null || getBitMap().contains(Bit.LENIENT))
            return true;
            
        return false;
    }
    
    /**
     * True if the lenient bit specified by the mask is turned on. Otherwise, false.
     */ 
    public boolean isLenient(Bit mask) {
        normalizeBitMap();
        if( getBitMap().contains(mask) )
            return true;
            
        return false;
    }


    /**
     * this method ensures that if we're in a default state (leniency == true) that we force defaults processing because we need to actually know the bits at this point
     */
    private void normalizeBitMap() {
        if(getBitMap() == null) {
            synchronized(bitMapLock) {
                if(bitMap == null) {
                    bitMap = getDefaults();
                } 
            }            
        }
    }

    /**
     * sets this instances leniency bit map. If being set to true, the configured defaults are used. If being 
     * set to false the bit map is cleared.
     */
    public void setLenient(boolean flag) {
        if(flag == true) 
            setLenientFlags(getDefaults());
        else {
            setLenientFlags(EnumSet.noneOf(Leniency.Bit.class));
        }
    }

    /**
     * set the leniency bit map to the supplied bit map  
     */
    public void setLenientFlags(Set<Bit> newLeniencySet) {
        if(newLeniencySet.size() > 0)
            newLeniencySet.add(Bit.LENIENT);
        setBitMap(newLeniencySet);        
    }

    /**
     * returns the configured defaults for leniency as configured in ICUConfig.properties 
     */
    private Set<Bit> getDefaults() {
        boolean bitOn;
        Set<Bit> results = EnumSet.noneOf(Leniency.Bit.class);
        
        if(defaultsObtained) 
            return defaults;
        
        bitOn = ICUConfig.get(LENIENCY_FIELD_VALIDATION_PROPERTY, "true").equals("true");
        if(bitOn) results.add(Bit.FIELD_VALIDATION);
        
        bitOn = ICUConfig.get(LENIENCY_ALLOW_WHITESPACE__PROPERTY, "true").equals("true");
        if(bitOn) results.add(Bit.ALLOW_WHITESPACE);
        
        bitOn = ICUConfig.get(LENIENCY_ALLOW_NUMERIC_PROPERTY, "true").equals("true");
        if(bitOn) results.add(Bit.ALLOW_NUMERIC);
        
        synchronized(defaultsLock) {        
            if(defaultsObtained)
                return defaults;
            
            defaultsObtained = true;
            defaults = results;            
        }
        
        return results;
    }

    /**
     * @return the bitMap
     */
    protected Set<Bit> getBitMap() {
        Set<Bit> results;
        synchronized(bitMapLock) {
            results = bitMap;
        }
        return results;
    }

    /**
     * @param bitMap the bitMap to set
     */
    protected void setBitMap(Set<Bit> newBitMap) {
        synchronized(bitMapLock) {
            bitMap = newBitMap;
        }
    }
    
}
