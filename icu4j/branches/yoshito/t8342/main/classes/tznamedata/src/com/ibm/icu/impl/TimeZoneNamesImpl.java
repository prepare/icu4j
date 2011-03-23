/*
 *******************************************************************************
 * Copyright (C) 2011, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import com.ibm.icu.text.TimeZoneNames;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.UResourceBundle;

/**
 * @author yumaoka
 *
 */
public class TimeZoneNamesImpl extends TimeZoneNames {

    private UResourceBundle zoneStrings;

    private static final String MZ_PREFIX = "meta:";

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneNames#getLocale()
     */
    @Override
    public ULocale getLocale() {
        return zoneStrings.getULocale();
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneNames#getMetaZoneID(java.lang.String, long)
     */
    @Override
    public String getMetaZoneID(String tzID, long time) {
        String mzID = ZoneMeta.getMetazoneID(tzID, time);
        if (mzID == null) {
            String canonicalTZID = ZoneMeta.getCanonicalCLDRID(tzID);
            if (!tzID.equals(canonicalTZID)) {
                mzID = ZoneMeta.getMetazoneID(canonicalTZID, time);
            }
        }
        return mzID;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneNames#getMetaZoneDisplayName(java.lang.String, com.ibm.icu.text.TimeZoneNames.NameType)
     */
    @Override
    public String getMetaZoneDisplayName(String mzID, NameType type) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneNames#getDisplayName(java.lang.String, com.ibm.icu.text.TimeZoneNames.NameType, long)
     */
    @Override
    protected String getDisplayName(String tzID, NameType type) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneNames#getExemplarLocationName(java.lang.String)
     */
    @Override
    public String getExemplarLocationName(String tzID) {
        //TODO
        return super.getExemplarLocationName(tzID);
    }
}
