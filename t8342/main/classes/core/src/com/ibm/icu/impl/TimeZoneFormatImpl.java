/*
 *******************************************************************************
 * Copyright (C) 2011, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.ibm.icu.text.LocaleDisplayNames;
import com.ibm.icu.text.TimeZoneFormat;
import com.ibm.icu.text.TimeZoneNames;
import com.ibm.icu.text.TimeZoneNames.NameType;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.TimeZone.SystemTimeZoneType;
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
        FALLBACK_FORMAT("fallbackFormat", "{1} ({0})");

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
    private transient String _region;
    private transient volatile ConcurrentHashMap<String, String> _genericLocationNamesMap;

    private transient TextTrieMap<ZoneNameInfo> _longSpecificTrie;
    private transient TextTrieMap<ZoneNameInfo> _longGenericTrie;
    private transient TextTrieMap<ZoneNameInfo> _shortSpecificTrie;
    private transient TextTrieMap<ZoneNameInfo> _shortGenericTrie;
    private transient TextTrieMap<ZoneNameInfo> _genericLocationTrie;

    public TimeZoneFormatImpl(ULocale locale) {
        super(locale);
        _locale = locale;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleFormatLongGeneric(com.ibm.icu.util.TimeZone, long)
     */
    @Override
    protected String handleFormatLongGeneric(TimeZone tz, long date) {
        String tzID = tz.getID();
        TimeZoneNames names = getTimeZoneNames();

        // Try to get a name from time zone first
        String name = names.getTimeZoneDisplayName(tzID, NameType.LONG_GENERIC, null);

        if (name != null) {
            return name;
        }

        // Try meta zone
        String mzID = names.getMetaZoneID(tzID, date);
        if (mzID != null) {
            name = names.getMetaZoneDisplayName(mzID, NameType.LONG_GENERIC, null);
            if (name != null) {
                name = processMetaZoneGenericName(tz, date, mzID, name);
            }
        }
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
        String tzID = tz.getID();
        TimeZoneNames names = getTimeZoneNames();

        // Try to get a name from time zone first
        String name = names.getTimeZoneDisplayName(tzID, NameType.SHORT_GENERIC, null);

        if (name != null) {
            return name;
        }

        // Try meta zone
        String mzID = names.getMetaZoneID(tzID, date);
        if (mzID != null) {
            name = names.getMetaZoneDisplayName(mzID, NameType.LONG_GENERIC, null);
            if (name != null) {
                name = processMetaZoneGenericName(tz, date, mzID, name);
            }
        }
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
    protected String handleFormatGenericLocation(String tzID) {
        String name = null;
        if (_genericLocationNamesMap != null) {
            name = _genericLocationNamesMap.get(tzID);
            if (name != null) {
                if (name.length() == 0) {
                    // empty string to indicate the name is not available
                    return null;
                }
                return name;
            }
        }
        String countryCode = ZoneMeta.getCanonicalCountry(tzID);
        if (countryCode != null) {
            String country = LocaleDisplayNames.getInstance(_locale).regionDisplayName(countryCode);
            if (ZoneMeta.getSingleCountry(tzID) != null) {
                // If the zone is only one zone in the country, do not add city
                name = formatPattern(Pattern.REGION_FORMAT, country);
            } else {
                // getExemplarLocationName should return non-empty String
                // if the time zone is associated with a location
                String city = getTimeZoneNames().getExemplarLocationName(tzID);
                name = formatPattern(Pattern.FALLBACK_REGION_FORMAT, city, country);
            }
        }

        if (_genericLocationNamesMap == null) {
            synchronized(this) {
                if (_genericLocationNamesMap == null) {
                    _genericLocationNamesMap = new ConcurrentHashMap<String, String>();
                }
            }
        }
        if (name == null) {
            _genericLocationNamesMap.putIfAbsent(tzID, "");
        } else {
            String tmp = _genericLocationNamesMap.putIfAbsent(tzID, name);
            if (tmp != null) {
                name = tmp;
            }
        }
        return name;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleParseLongGeneric(java.lang.String, int, com.ibm.icu.text.TimeZoneFormat.ParseResult)
     */
    @Override
    protected void handleParseLongGeneric(String text, int start, ParseResult result) {
        result.reset();
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleParseLongSpecific(java.lang.String, int, com.ibm.icu.text.TimeZoneFormat.ParseResult)
     */
    @Override
    protected void handleParseLongSpecific(String text, int start, ParseResult result) {
        result.reset();
        int[] matchLen = new int[1];
        Iterator<ZoneNameInfo> matches = getLongSpecificTrie().get(text, start, matchLen);
        if (matches != null) {
            ZoneNameInfo info = matches.next();

            // should have only one match
            assert(!matches.hasNext());

            String tzID = info.tzID();
            if (tzID == null) {
                String mzID = info.mzID();
                // either tzID or mzID must be available
                assert(mzID != null);

                tzID = getTimeZoneNames().getReferenceZoneID(mzID, getTargetRegion());
            }

            if (tzID != null) {
                result.setID(tzID).setType(info.type()).setParseLength(matchLen[0]);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleParseShortGeneric(java.lang.String, int, com.ibm.icu.text.TimeZoneFormat.ParseResult)
     */
    @Override
    protected void handleParseShortGeneric(String text, int start, ParseResult result) {
        result.reset();
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleParseShortSpecific(java.lang.String, int, com.ibm.icu.text.TimeZoneFormat.ParseResult)
     */
    @Override
    protected void handleParseShortSpecific(String text, int start, ParseResult result) {
        result.reset();
        int[] matchLen = new int[1];
        Iterator<ZoneNameInfo> matches = getShortSpecificTrie().get(text, start, matchLen);
        if (matches != null) {
            ZoneNameInfo info = matches.next();

            // should have only one match
            assert(!matches.hasNext());

            String tzID = info.tzID();
            if (tzID == null) {
                String mzID = info.mzID();
                // either tzID or mzID must be available
                assert(mzID != null);

                tzID = getTimeZoneNames().getReferenceZoneID(mzID, getTargetRegion());
            }

            if (tzID != null) {
                result.setID(tzID).setType(info.type()).setParseLength(matchLen[0]);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleParseGenericLocation(java.lang.String, int, com.ibm.icu.text.TimeZoneFormat.ParseResult)
     */
    @Override
    protected void handleParseGenericLocation(String text, int start, ParseResult result) {
        result.reset();
        int[] matchLen = new int[1];
        Iterator<ZoneNameInfo> matches = getGenericLocationTrie().get(text, start, matchLen);
        if (matches != null) {
            ZoneNameInfo info = matches.next();
            result.setID(info.tzID()).setParseLength(matchLen[0]);

            // should have only one match
            assert(!matches.hasNext());
        }
    }

    private synchronized TextTrieMap<ZoneNameInfo> getLongSpecificTrie() {
        if (_longSpecificTrie != null) {
            return _longSpecificTrie;
        }

        _longSpecificTrie = new TextTrieMap<ZoneNameInfo>(true);
        Set<String> ids = TimeZone.getAvailableIDs(SystemTimeZoneType.CANONICAL, null, null);
        Set<String> processedMzids = new HashSet<String>(100);
        TimeZoneNames names = getTimeZoneNames();
        ZoneNameInfo info;
        for (String id : ids) {
            String longS = names.getTimeZoneDisplayName(id, NameType.LONG_STANDARD, null);
            String longD = names.getTimeZoneDisplayName(id, NameType.LONG_DAYLIGHT, null);
            if (longS != null) {
                info = new ZoneNameInfo(id, null, TimeType.STANDARD);
                _longSpecificTrie.put(longS, info);
            }
            if (longD != null) {
                info = new ZoneNameInfo(id, null, TimeType.DAYLIGHT);
                _longSpecificTrie.put(longD, info);
            }
            // add names for meta zones
            Set<String> mzids = names.getAvailableMetaZoneIDs(id);
            for (String mzid : mzids) {
                if (processedMzids.contains(mzid)) {
                    continue;
                }
                longS = names.getMetaZoneDisplayName(mzid, NameType.LONG_STANDARD, null);
                longD = names.getMetaZoneDisplayName(mzid, NameType.LONG_DAYLIGHT, null);
                if (longS != null) {
                    info = new ZoneNameInfo(null, mzid, TimeType.STANDARD);
                    _longSpecificTrie.put(longS, info);
                }
                if (longD != null) {
                    info = new ZoneNameInfo(null, mzid, TimeType.DAYLIGHT);
                    _longSpecificTrie.put(longD, info);
                }
                processedMzids.add(mzid);
            }
        }
        return _longSpecificTrie;
    }

    private synchronized TextTrieMap<ZoneNameInfo> getLongGenericTrie() {
        if (_longGenericTrie != null) {
            return _longGenericTrie;
        }
        //TODO
        return _longGenericTrie;
    }

    private synchronized TextTrieMap<ZoneNameInfo> getShortSpecificTrie() {
        if (_shortSpecificTrie != null) {
            return _shortSpecificTrie;
        }

        _shortSpecificTrie = new TextTrieMap<ZoneNameInfo>(true);
        Set<String> ids = TimeZone.getAvailableIDs(SystemTimeZoneType.CANONICAL, null, null);
        Set<String> processedMzids = new HashSet<String>(100);
        TimeZoneNames names = getTimeZoneNames();
        ZoneNameInfo info;
        for (String id : ids) {
            String shortS = names.getTimeZoneDisplayName(id, NameType.SHORT_STANDARD, null);
            String shortD = names.getTimeZoneDisplayName(id, NameType.SHORT_DAYLIGHT, null);
            if (shortS != null) {
                info = new ZoneNameInfo(id, null, TimeType.STANDARD);
                _shortSpecificTrie.put(shortS, info);
            }
            if (shortD != null) {
                info = new ZoneNameInfo(id, null, TimeType.DAYLIGHT);
                _shortSpecificTrie.put(shortD, info);
            }
            // add names for meta zones
            Set<String> mzids = names.getAvailableMetaZoneIDs(id);
            for (String mzid : mzids) {
                if (processedMzids.contains(mzid)) {
                    continue;
                }
                shortS = names.getMetaZoneDisplayName(mzid, NameType.SHORT_STANDARD, null);
                shortD = names.getMetaZoneDisplayName(mzid, NameType.SHORT_DAYLIGHT, null);
                if (shortS != null) {
                    info = new ZoneNameInfo(null, mzid, TimeType.STANDARD);
                    _shortSpecificTrie.put(shortS, info);
                }
                if (shortD != null) {
                    info = new ZoneNameInfo(null, mzid, TimeType.DAYLIGHT);
                    _shortSpecificTrie.put(shortD, info);
                }
                processedMzids.add(mzid);
            }
        }
        return _shortSpecificTrie;
    }

    private synchronized TextTrieMap<ZoneNameInfo> getShortGenericTrie() {
        if (_shortGenericTrie != null) {
            return _shortGenericTrie;
        }
        //TODO
        return _shortGenericTrie;
    }

    private synchronized TextTrieMap<ZoneNameInfo> getGenericLocationTrie() {
        if (_genericLocationTrie != null) {
            return _genericLocationTrie;
        }

        _genericLocationTrie = new TextTrieMap<ZoneNameInfo>(true);
        Set<String> ids = TimeZone.getAvailableIDs(SystemTimeZoneType.CANONICAL_LOCATION, null, null);
        for (String id : ids) {
            String name = handleFormatGenericLocation(id);
            if (name != null) {
                ZoneNameInfo info = new ZoneNameInfo(id, null, TimeType.UNKNOWN);
                _genericLocationTrie.put(name, info);
            }
        }
        return _genericLocationTrie;
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

    private synchronized String getTargetRegion() {
        if (_region == null) {
            _region = _locale.getCountry();
            if (_region.length() == 0) {
                ULocale tmp = ULocale.addLikelySubtags(_locale);
                _region = tmp.getCountry();
                if (_region.length() == 0) {
                    _region = "001";
                }
            }
        }
        return _region;
    }

    private String processMetaZoneGenericName(TimeZone tz, long date, String mzID, String mzDisplayName) {
        String name = mzDisplayName;
        String tzID = tz.getID();

        // Check if we need to use a partial location format.
        // This check is done by comparing offset with the meta zone's
        // golden zone at the given date.
        String goldenID = getTimeZoneNames().getReferenceZoneID(mzID, getTargetRegion());
        if (goldenID != null && !goldenID.equals(tz.getID())) {
            TimeZone goldenZone = TimeZone.getTimeZone(goldenID);
            int[] offsets0 = new int[2];
            int[] offsets1 = new int[2];

            tz.getOffset(date, false, offsets0);
            goldenZone.getOffset(date, false, offsets1);

            if (offsets0[0] != offsets1[0] || offsets0[1] != offsets1[1]) {
                // Now we need to use a partial location format.
                String location = null;
                String countryCode = ZoneMeta.getSingleCountry(tzID);
                if (countryCode != null) {
                    location = LocaleDisplayNames.getInstance(_locale).regionDisplayName(countryCode);
                } else {
                    location = getTimeZoneNames().getExemplarLocationName(tzID);
                }
                if (location != null) {
                    name = formatPattern(Pattern.FALLBACK_FORMAT, location, mzDisplayName);
                } else {
                    // This should not happen, but just in case...
                    name = null;
                }
            }
        }
        return name;
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

    private static class ZoneNameInfo {
        String _tzID;
        String _mzID;
        TimeType _type;

        ZoneNameInfo(String tzID, String mzID, TimeType type) {
            _tzID = tzID;
            _mzID = mzID;
            _type = type;
        }

        String tzID() {
            return _tzID;
        }

        String mzID() {
            return _mzID;
        }

        TimeType type() {
            return _type;
        }
    }
}
