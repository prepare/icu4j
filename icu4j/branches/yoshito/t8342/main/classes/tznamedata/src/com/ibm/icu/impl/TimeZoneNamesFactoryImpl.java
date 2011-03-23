/*
 *******************************************************************************
 * Copyright (C) 2011, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import com.ibm.icu.text.TimeZoneNames;
import com.ibm.icu.text.TimeZoneNames.Factory;
import com.ibm.icu.util.ULocale;

/**
 * @author yumaoka
 *
 */
public class TimeZoneNamesFactoryImpl extends Factory {

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneNames.Factory#getTimeZoneNames(com.ibm.icu.util.ULocale)
     */
    @Override
    public TimeZoneNames getTimeZoneNames(ULocale locale) {
        // TODO
        return new TimeZoneNamesImpl();
    }

}
