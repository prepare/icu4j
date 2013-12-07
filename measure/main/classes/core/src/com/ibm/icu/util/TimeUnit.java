/*
 **************************************************************************
 * Copyright (C) 2008-2013, Google, International Business Machines
 * Corporation and others. All Rights Reserved.
 **************************************************************************
 */
package com.ibm.icu.util;

import java.io.ObjectStreamException;


/**
 * Measurement unit for time units.
 * @see TimeUnitAmount
 * @see TimeUnit
 * @author markdavis
 * @stable ICU 4.0
 */
public class TimeUnit extends MeasureUnit {
    
    /** 
     * Constant value for supported time unit.
     * @stable ICU 4.0
     */
    public static TimeUnit
    SECOND = (TimeUnit) MeasureUnit.SECOND,
    MINUTE = (TimeUnit) MeasureUnit.MINUTE,
    HOUR = (TimeUnit) MeasureUnit.HOUR,
    DAY = (TimeUnit) MeasureUnit.DAY,
    WEEK = (TimeUnit) MeasureUnit.WEEK,
    MONTH = (TimeUnit) MeasureUnit.MONTH,
    YEAR = (TimeUnit) MeasureUnit.YEAR;
    
    TimeUnit(String type, String code) {
        super(type, code);
    }

    /**
     * @return the available values
     * @stable ICU 4.0
     */
    public static TimeUnit[] values() {
        return new TimeUnit[]{SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR};
    }
    
    private Object writeReplace() throws ObjectStreamException {
        return new MeasureUnitProxy(type, code);
    }
    
    // We have agreed to break serialization here so the serialization version UID has changed
    // and we are not supplying a readResolve method for backward compatibility.
}
