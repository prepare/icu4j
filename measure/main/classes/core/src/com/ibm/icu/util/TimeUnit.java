/*
 **************************************************************************
 * Copyright (C) 2008-2013, Google, International Business Machines
 * Corporation and others. All Rights Reserved.
 **************************************************************************
 */
package com.ibm.icu.util;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;


/**
 * Measurement unit for time units.
 * @see TimeUnitAmount
 * @see TimeUnit
 * @author markdavis
 * @stable ICU 4.0
 */
public class TimeUnit extends MeasureUnit {
    private static final long serialVersionUID = -2839973855554750484L;
    
    /**
     * Here for serialization backward compatibility only.
     */
    private final int index;
    
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
        index = 0;
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
    
    // For backward compatibility only
    private Object readResolve() throws ObjectStreamException {
        switch (index) {
        case 6:
            return SECOND;
        case 5:
            return MINUTE;
        case 4:
            return HOUR;
        case 3:
            return DAY;
        case 2:
            return WEEK;
        case 1:
            return MONTH;
        case 0:
            return YEAR;
        default:
            throw new InvalidObjectException("Bad index: " + index);
        }
    }
}
