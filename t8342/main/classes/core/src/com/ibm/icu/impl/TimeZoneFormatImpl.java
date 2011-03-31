/*
 *******************************************************************************
 * Copyright (C) 2011, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.text.MessageFormat;
import java.util.Date;
import java.util.MissingResourceException;

import com.ibm.icu.text.LocaleDisplayNames;
import com.ibm.icu.text.TimeZoneFormat;
import com.ibm.icu.text.TimeZoneNames.NameType;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;

/**
 * @author yumaoka
 *
 */
public class TimeZoneFormatImpl extends TimeZoneFormat {

    private ULocale _locale;

    private enum Pattern {
        // The format pattern such as "{0} Time", where {0} is the country. 
        REGION_FORMAT("regionFormat", "({0})"),

        // The format pattern such as "{1} Time ({0})", where {1} is the country and {0} is a city.
        FALLBACK_REGION_FORMAT("fallbackRegionFormat", "{1} ({0})"),

        // The format pattern such as "{1} ({0})", where {1} is the metazone, and {0} is the country or city.
        FALLBACK_FORMAT("fallbackFormat", "({0})");

        String _key;
        String _defaultVal;

        Pattern(String key, String defaultVal) {
            _key = key;
            _defaultVal = defaultVal;
        }

        String key() {
            return _key;
        }

        String defaultValue() {
            return _defaultVal;
        }
    }

    private transient MessageFormat[] _patternFormatters = new MessageFormat[Pattern.values().length];
    private transient boolean _frozen;

    public TimeZoneFormatImpl(ULocale locale) {
        // TODO
        _locale = locale;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleFormatLongGeneric(com.ibm.icu.util.TimeZone, long)
     */
    @Override
    protected String handleFormatLongGeneric(TimeZone tz, long date) {
        // TODO location fallback and partial location name
        String name = getTimeZoneNames().getDisplayName(tz.getID(), NameType.LONG_GENERIC, date, null);
        return name;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleFormatLongSpecific(com.ibm.icu.util.TimeZone, long)
     */
    @Override
    protected String handleFormatLongSpecific(TimeZone tz, long date) {
        boolean isDaylight = tz.inDaylightTime(new Date(date));
        String name = isDaylight?
                getTimeZoneNames().getDisplayName(tz.getID(), NameType.LONG_DAYLIGHT, date, null) :
                getTimeZoneNames().getDisplayName(tz.getID(), NameType.LONG_STANDARD, date, null);
        return name;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleFormatShortGeneric(com.ibm.icu.util.TimeZone, long)
     */
    @Override
    protected String handleFormatShortGeneric(TimeZone tz, long date) {
        // TODO location fallback and partial location name
        String name = getTimeZoneNames().getDisplayName(tz.getID(), NameType.SHORT_GENERIC, date, null);
        return name;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleFormatShortSpecific(com.ibm.icu.util.TimeZone, long, boolean)
     */
    @Override
    protected String handleFormatShortSpecific(TimeZone tz, long date, boolean all) {
        boolean isDaylight = tz.inDaylightTime(new Date(date));
        boolean[] isCommonlyUsed = new boolean[1];
        String name = isDaylight?
                getTimeZoneNames().getDisplayName(tz.getID(), NameType.SHORT_DAYLIGHT, date, isCommonlyUsed) :
                getTimeZoneNames().getDisplayName(tz.getID(), NameType.SHORT_STANDARD, date, isCommonlyUsed);

        if (!all && !isCommonlyUsed[0]) {
            name = null;
        }
        return name;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleFormatGenericLocation(com.ibm.icu.util.TimeZone)
     */
    @Override
    protected String handleFormatGenericLocation(TimeZone tz) {
        String tzID = tz.getID();
        String countryCode = ZoneMeta.getCanonicalCountry(tzID);
        if (countryCode != null) {
            String country = LocaleDisplayNames.getInstance(_locale).regionDisplayName(countryCode);
            if (ZoneMeta.getSingleCountry(tzID) != null) {
                // If the zone is only one zone in the country, do not add city
                return formatPattern(Pattern.REGION_FORMAT, country);
            } else {
                // getExemplarLocationName should return non-empty String
                // if the time zone is associated with a location
                String city = getTimeZoneNames().getExemplarLocationName(tzID);
                return formatPattern(Pattern.FALLBACK_REGION_FORMAT, city, country);
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleParseLongGeneric(java.lang.String, int)
     */
    @Override
    protected ParseResult handleParseLongGeneric(String text, int start) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleParseLongSpecific(java.lang.String, int)
     */
    @Override
    protected ParseResult handleParseLongSpecific(String text, int start) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleParseShortGeneric(java.lang.String, int)
     */
    @Override
    protected ParseResult handleParseShortGeneric(String text, int start) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleParseShortSpecific(java.lang.String, int)
     */
    @Override
    protected ParseResult handleParseShortSpecific(String text, int start) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleParseGenericLocation(java.lang.String, int)
     */
    @Override
    protected ParseResult handleParseGenericLocation(String text, int start) {
        // TODO Auto-generated method stub
        return null;
    }

    private synchronized String formatPattern(TimeZoneFormatImpl.Pattern pat, String... args) {
        int idx = pat.ordinal();
        if (_patternFormatters[idx] == null) {
            String patText;
            try {
                ICUResourceBundle bundle = (ICUResourceBundle) ICUResourceBundle.getBundleInstance(
                    ICUResourceBundle.ICU_ZONE_BASE_NAME, _locale);
                patText = bundle.getStringWithFallback("zoneStrings/" + pat.key());
            } catch (MissingResourceException e) {
                patText = pat.defaultValue();
            }

            _patternFormatters[idx] = new MessageFormat(patText);
        }
        return _patternFormatters[idx].format(args);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.Freezable#isFrozen()
     */
    public boolean isFrozen() {
        return _frozen;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.Freezable#freeze()
     */
    public TimeZoneFormat freeze() {
        _frozen = true;
        return this;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.util.Freezable#cloneAsThawed()
     */
    public TimeZoneFormat cloneAsThawed() {
        TimeZoneFormatImpl copy = (TimeZoneFormatImpl)super.clone();
        copy._frozen = false;
        return copy;
    }

}
