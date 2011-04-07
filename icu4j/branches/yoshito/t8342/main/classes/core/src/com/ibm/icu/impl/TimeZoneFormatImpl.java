/*
 *******************************************************************************
 * Copyright (C) 2011, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.lang.ref.WeakReference;
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
 * The ICU's default implementation of <code>TimeZoneFormat</code>
 */
public class TimeZoneFormatImpl extends TimeZoneFormat {

    private static final long serialVersionUID = 2686884826329821680L;

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

    private transient WeakReference<LocaleDisplayNames> _localeDisplayNamesRef;
    private transient volatile ConcurrentHashMap<String, String> _genericLocationNamesMap;

    private transient TextTrieMap<ZoneNameInfo> _longSpecificTrie;
    private transient TextTrieMap<ZoneNameInfo> _longGenericTrie;
    private transient TextTrieMap<ZoneNameInfo> _shortSpecificTrie;
    private transient TextTrieMap<ZoneNameInfo> _shortGenericTrie;
    private transient TextTrieMap<ZoneNameInfo> _genericLocationTrie;

    private static final int DEF_MZIDS_HASH_SIZE = 128;

    public TimeZoneFormatImpl(ULocale locale) {
        super(locale);
        _locale = locale;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleFormatLongSpecific(com.ibm.icu.util.TimeZone, long)
     */
    @Override
    protected String handleFormatLongSpecific(TimeZone tz, long date) {
        return formatSpecific(tz, date, NameType.LONG_STANDARD, NameType.LONG_DAYLIGHT);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleFormatShortSpecific(com.ibm.icu.util.TimeZone, long)
     */
    @Override
    protected String handleFormatShortSpecific(TimeZone tz, long date) {
        return formatSpecific(tz, date, NameType.SHORT_STANDARD, NameType.SHORT_DAYLIGHT);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleFormatShortSpecificCommonlyUsed(com.ibm.icu.util.TimeZone, long)
     */
    @Override
    protected String handleFormatShortSpecificCommonlyUsed(TimeZone tz, long date) {
        return formatSpecific(tz, date, NameType.SHORT_STANDARD_COMMONLY_USED, NameType.SHORT_DAYLIGHT_COMMONLY_USED);
    }

    private String formatSpecific(TimeZone tz, long date, NameType stdType, NameType dstType) {
        boolean isDaylight = tz.inDaylightTime(new Date(date));
        String name = isDaylight?
                getTimeZoneNames().getDisplayName(tz.getID(), dstType, date) :
                getTimeZoneNames().getDisplayName(tz.getID(), stdType, date);
        return name;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleFormatLongGeneric(com.ibm.icu.util.TimeZone, long)
     */
    @Override
    protected String handleFormatLongGeneric(TimeZone tz, long date) {
        return formatGeneric(tz, date, NameType.LONG_GENERIC);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleFormatShortGeneric(com.ibm.icu.util.TimeZone, long)
     */
    @Override
    protected String handleFormatShortGeneric(TimeZone tz, long date) {
        return formatGeneric(tz, date, NameType.SHORT_GENERIC);
    }

    private String formatGeneric(TimeZone tz, long date, NameType nameType) {
        String tzID = tz.getID();
        TimeZoneNames names = getTimeZoneNames();

        // Try to get a name from time zone first
        String name = names.getTimeZoneDisplayName(tzID, nameType);

        if (name != null) {
            return name;
        }

        // Try meta zone
        String mzID = names.getMetaZoneID(tzID, date);
        if (mzID != null) {
            name = names.getMetaZoneDisplayName(mzID, nameType);
            if (name != null) {
                name = processMetaZoneGenericName(tz, date, mzID, name);
            }
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
            String country = getLocaleDisplayNames().regionDisplayName(countryCode);
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
     * @see com.ibm.icu.text.TimeZoneFormat#handleParseLongSpecific(java.lang.String, int, com.ibm.icu.text.TimeZoneFormat.ParseResult)
     */
    @Override
    protected void handleParseLongSpecific(String text, int start, ParseResult result) {
        parseCommon(getLongSpecificTrie(), text, start, result);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleParseShortSpecific(java.lang.String, int, com.ibm.icu.text.TimeZoneFormat.ParseResult)
     */
    @Override
    protected void handleParseShortSpecific(String text, int start, ParseResult result) {
        parseCommon(getShortSpecificTrie(), text, start, result);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleParseLongGeneric(java.lang.String, int, com.ibm.icu.text.TimeZoneFormat.ParseResult)
     */
    @Override
    protected void handleParseLongGeneric(String text, int start, ParseResult result) {
        parseCommon(getLongGenericTrie(),text, start, result);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneFormat#handleParseShortGeneric(java.lang.String, int, com.ibm.icu.text.TimeZoneFormat.ParseResult)
     */
    @Override
    protected void handleParseShortGeneric(String text, int start, ParseResult result) {
        parseCommon(getShortGenericTrie(),text, start, result);
    }

    private void parseCommon(TextTrieMap<ZoneNameInfo> trie, String text, int start, ParseResult result) {
        result.reset();
        int[] matchLen = new int[1];
        Iterator<ZoneNameInfo> matches = trie.get(text, start, matchLen);
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
        collectSpecificNames(_longSpecificTrie, true);

        return _longSpecificTrie;
    }

    private synchronized TextTrieMap<ZoneNameInfo> getShortSpecificTrie() {
        if (_shortSpecificTrie != null) {
            return _shortSpecificTrie;
        }

        _shortSpecificTrie = new TextTrieMap<ZoneNameInfo>(true);
        collectSpecificNames(_shortSpecificTrie, false);

        return _shortSpecificTrie;
    }

    private void collectSpecificNames(TextTrieMap<ZoneNameInfo> trie, boolean isLong) {
        NameType stdType = isLong ? NameType.LONG_STANDARD : NameType.SHORT_STANDARD;
        NameType dstType = isLong ? NameType.LONG_STANDARD : NameType.SHORT_DAYLIGHT;

        Set<String> ids = TimeZone.getAvailableIDs(SystemTimeZoneType.CANONICAL, null, null);
        Set<String> processedMzids = new HashSet<String>(DEF_MZIDS_HASH_SIZE);
        TimeZoneNames names = getTimeZoneNames();
        ZoneNameInfo info;
        for (String id : ids) {
            String stdName = names.getTimeZoneDisplayName(id, stdType);
            String dstName = names.getTimeZoneDisplayName(id, dstType);
            if (stdName != null) {
                info = new ZoneNameInfo(id, null, TimeType.STANDARD);
                trie.put(stdName, info);
            }
            if (dstName != null) {
                info = new ZoneNameInfo(id, null, TimeType.DAYLIGHT);
                trie.put(dstName, info);
            }
            // add names for meta zones
            Set<String> mzids = names.getAvailableMetaZoneIDs(id);
            for (String mzid : mzids) {
                if (processedMzids.contains(mzid)) {
                    continue;
                }
                stdName = names.getMetaZoneDisplayName(mzid, stdType);
                dstName = names.getMetaZoneDisplayName(mzid, dstType);
                if (stdName != null) {
                    info = new ZoneNameInfo(null, mzid, TimeType.STANDARD);
                    trie.put(stdName, info);
                }
                if (dstName != null) {
                    info = new ZoneNameInfo(null, mzid, TimeType.DAYLIGHT);
                    trie.put(dstName, info);
                }
                processedMzids.add(mzid);
            }
        }
    }

    private synchronized TextTrieMap<ZoneNameInfo> getLongGenericTrie() {
        if (_longGenericTrie != null) {
            return _longGenericTrie;
        }
        _longGenericTrie = new TextTrieMap<ZoneNameInfo>(true);
        collectGenericNames(_longGenericTrie, true);

        return _longGenericTrie;
    }

    private synchronized TextTrieMap<ZoneNameInfo> getShortGenericTrie() {
        if (_shortGenericTrie != null) {
            return _shortGenericTrie;
        }
        _shortGenericTrie = new TextTrieMap<ZoneNameInfo>(true);
        collectGenericNames(_shortGenericTrie, false);

        return _shortGenericTrie;
    }

    private void collectGenericNames(TextTrieMap<ZoneNameInfo> trie, boolean isLong) {
        Set<String> ids = TimeZone.getAvailableIDs(SystemTimeZoneType.CANONICAL, null, null);
        Set<String> processedMzids = new HashSet<String>(DEF_MZIDS_HASH_SIZE);
        TimeZoneNames names = getTimeZoneNames();
        NameType type = isLong ? NameType.LONG_GENERIC : NameType.SHORT_GENERIC;
        ZoneNameInfo info;
        for (String id : ids) {
            // time zone's permanent generic name
            String name = names.getTimeZoneDisplayName(id, type);
            if (name != null) {
                info = new ZoneNameInfo(id, null, TimeType.UNKNOWN);
                trie.put(name, info);
            } else {
                // if time zone's permanent generic name is not available,
                // collect meta zone names
                Set<String> mzIDs = names.getAvailableMetaZoneIDs(id);
                for (String mzID : mzIDs) {
                    String mzName = names.getMetaZoneDisplayName(mzID, type);
                    if (mzName == null) {
                        continue;
                    }
                    if (!processedMzids.contains(mzID)) {
                        // meta zone's plain name is not yet added
                        info = new ZoneNameInfo(null, mzID, TimeType.UNKNOWN);
                        trie.put(mzName, info);
                    }
                    // if this time zone is not the golden zone of the meta zone,
                    // partial location name (such as "PT (Los Angeles)" is also required.
                    String goldenID = names.getReferenceZoneID(mzID, getTargetRegion());
                    if (!id.equals(goldenID)) {
                        String partialLocationName = formatPartialLocation(id, mzName);
                        if (partialLocationName != null) {
                            info = new ZoneNameInfo(id, null, TimeType.UNKNOWN);
                            trie.put(partialLocationName, info);
                        }
                    }
                }
            }
        }
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
                name = formatPartialLocation(tzID, mzDisplayName);
            }
        }
        return name;
    }

    private String formatPartialLocation(String tzID, String mzDisplayName) {
        String name;
        String location = null;
        String countryCode = ZoneMeta.getSingleCountry(tzID);
        if (countryCode != null) {
            location = getLocaleDisplayNames().regionDisplayName(countryCode);
        } else {
            location = getTimeZoneNames().getExemplarLocationName(tzID);
        }
        if (location != null) {
            name = formatPattern(Pattern.FALLBACK_FORMAT, location, mzDisplayName);
        } else {
            // This should not happen, but just in case...
            name = null;
        }
        return name;
    }

    private synchronized LocaleDisplayNames getLocaleDisplayNames() {
        LocaleDisplayNames locNames = null;
        if (_localeDisplayNamesRef != null) {
            locNames = _localeDisplayNamesRef.get();
        }
        if (locNames == null) {
            locNames = LocaleDisplayNames.getInstance(_locale);
            _localeDisplayNamesRef = new WeakReference<LocaleDisplayNames>(locNames);
        }
        return locNames;
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
