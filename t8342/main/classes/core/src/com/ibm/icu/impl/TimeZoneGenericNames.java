/*
 *******************************************************************************
 * Copyright (C) 2011, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.ibm.icu.impl.TextTrieMap.ResultHandler;
import com.ibm.icu.text.LocaleDisplayNames;
import com.ibm.icu.text.TimeZoneNames;
import com.ibm.icu.text.TimeZoneNames.MatchInfo;
import com.ibm.icu.text.TimeZoneNames.NameType;
import com.ibm.icu.util.BasicTimeZone;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.TimeZone.SystemTimeZoneType;
import com.ibm.icu.util.TimeZoneTransition;
import com.ibm.icu.util.ULocale;

/**
 * @author yumaoka
 *
 */
public class TimeZoneGenericNames implements Serializable {

    public enum GenericNameType {
        LOCATION,
        LONG,
        SHORT
    }

    private ULocale _locale;
    private TimeZoneNames _tznames;

    private transient String _region;
    private transient WeakReference<LocaleDisplayNames> _localeDisplayNamesRef;
    private transient MessageFormat[] _patternFormatters;
    private transient volatile ConcurrentHashMap<String, String> _genericLocationNamesMap;
    private transient volatile TextTrieMap<NameInfo> _namesTrie;

    private static Cache GENERIC_NAMES_CACHE = new Cache();

    // Window size used for DST check for a zone in a metazone (about a half year)
    private static final long DST_CHECK_RANGE = 184L*(24*60*60*1000);

    private static final NameType[] TZNAMES_GENERIC_TYPES = {
        NameType.LONG_GENERIC, NameType.LONG_STANDARD,
        NameType.SHORT_GENERIC, NameType.SHORT_STANDARD_COMMONLY_USED};

    public TimeZoneGenericNames(ULocale locale, TimeZoneNames tznames) {
        _locale = locale;
        _tznames = tznames;
    }

    private TimeZoneGenericNames(ULocale locale) {
        this(locale, TimeZoneNames.getInstance(locale));
    }

    public static TimeZoneGenericNames getInstance(ULocale locale) {
        String key = locale.getBaseName();
        return GENERIC_NAMES_CACHE.getInstance(key, locale);
    }

    public String getDisplayName(TimeZone tz, GenericNameType type, long date) {
        String name = null;
        switch (type) {
        case LOCATION:
            name = getGenericLocationName(tz.getCanonicalID());
            break;
        case LONG:
        case SHORT:
            name = formatGenericNonLocationName(tz, type, date);
            break;
        }
        return name;
    }

    /**
     * Returns the generic location name for the given canonical time zone ID.
     * 
     * @param canonicalTzID the canonical time zone ID
     * @return the generic location name for the given canonical time zone ID.
     */
    public String getGenericLocationName(String canonicalTzID) {
        String name = null;
        if (_genericLocationNamesMap != null) {
            name = _genericLocationNamesMap.get(canonicalTzID);
            if (name != null) {
                if (name.length() == 0) {
                    // empty string to indicate the name is not available
                    return null;
                }
                return name;
            }
        }
        String countryCode = ZoneMeta.getCanonicalCountry(canonicalTzID);
        if (countryCode != null) {
            String country = getLocaleDisplayNames().regionDisplayName(countryCode);
            if (ZoneMeta.getSingleCountry(canonicalTzID) != null) {
                // If the zone is only one zone in the country, do not add city
                name = formatPattern(Pattern.REGION_FORMAT, country);
            } else {
                // getExemplarLocationName should return non-empty String
                // if the time zone is associated with a location
                String city = _tznames.getExemplarLocationName(canonicalTzID);
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
            _genericLocationNamesMap.putIfAbsent(canonicalTzID, "");
        } else {
            String tmp = _genericLocationNamesMap.putIfAbsent(canonicalTzID, name);
            if (tmp != null) {
                name = tmp;
            }
        }
        return name;
    }

    /**
     * Private method to get a generic string, with fallback logics involved,
     * that is,
     * 
     * 1. If a generic non-location string is available for the zone, return it.
     * 2. If a generic non-location string is associated with a meta zone and 
     *    the zone never use daylight time around the given date, use the standard
     *    string (if available).
     * 3. If a generic non-location string is associated with a meta zone and
     *    the offset at the given time is different from the preferred zone for the
     *    current locale, then return the generic partial location string (if available)
     * 4. If a generic non-location string is not available, use generic location
     *    string.
     * 
     * @param tz the requested time zone
     * @param date the date
     * @param type the generic name type, either LONG or SHORT
     * @return the name used for a generic name type, which could be the
     * generic name, or the standard name (if the zone does not observes DST
     * around the date), or the partial location name.
     */
    private String formatGenericNonLocationName(TimeZone tz, GenericNameType type, long date) {
        assert(type == GenericNameType.LONG || type == GenericNameType.SHORT);
        String tzID = tz.getCanonicalID();

        // Try to get a name from time zone first
        NameType nameType = (type == GenericNameType.LONG) ? NameType.LONG_GENERIC : NameType.SHORT_GENERIC;
        String name = _tznames.getTimeZoneDisplayName(tzID, nameType);

        if (name != null) {
            return name;
        }

        // Try meta zone
        String mzID = _tznames.getMetaZoneID(tzID, date);
        if (mzID != null) {
            boolean useStandard = false;
            int[] offsets = {0, 0};
            tz.getOffset(date, false, offsets);

            if (offsets[1] == 0) {
                useStandard = true;
                // Check if the zone actually uses daylight saving time around the time
                if (tz instanceof BasicTimeZone) {
                    BasicTimeZone btz = (BasicTimeZone)tz;
                    TimeZoneTransition before = btz.getPreviousTransition(date, true);
                    if (before != null
                            && (date - before.getTime() < DST_CHECK_RANGE)
                            && before.getFrom().getDSTSavings() != 0) {
                        useStandard = false;
                    } else {
                        TimeZoneTransition after = btz.getNextTransition(date, false);
                        if (after != null
                                && (after.getTime() - date < DST_CHECK_RANGE)
                                && after.getTo().getDSTSavings() != 0) {
                            useStandard = false;
                        }
                    }
                } else {
                    // If not BasicTimeZone... only if the instance is not an ICU's implementation.
                    // We may get a wrong answer in edge case, but it should practically work OK.
                    int[] tmpOffsets = new int[2];
                    tz.getOffset(date - DST_CHECK_RANGE, false, tmpOffsets);
                    if (tmpOffsets[1] != 0) {
                        useStandard = false;
                    } else {
                        tz.getOffset(date + DST_CHECK_RANGE, false, tmpOffsets);
                        if (tmpOffsets[1] != 0){
                            useStandard = false;
                        }
                    }
                }
            }
            if (useStandard) {
                NameType stdNameType = (nameType == NameType.LONG_GENERIC) ?
                        NameType.LONG_STANDARD : NameType.SHORT_STANDARD_COMMONLY_USED;
                String stdName = _tznames.getDisplayName(tzID, stdNameType, date);
                if (stdName != null) {
                    name = stdName;

                    // TODO: revisit this issue later
                    // In CLDR, a same display name is used for both generic and standard
                    // for some meta zones in some locales.  This looks like a data bugs.
                    // For now, we check if the standard name is different from its generic
                    // name below.
                    String mzGenericName = _tznames.getMetaZoneDisplayName(mzID, nameType);
                    if (stdName.equalsIgnoreCase(mzGenericName)) {
                        name = null;
                    }
                }
            }

            if (name == null) {
                // Get a name from meta zone
                String mzName = _tznames.getMetaZoneDisplayName(mzID, nameType);
                if (mzName != null) {
                    // Check if we need to use a partial location format.
                    // This check is done by comparing offset with the meta zone's
                    // golden zone at the given date.
                    String goldenID = _tznames.getReferenceZoneID(mzID, getTargetRegion());
                    if (goldenID != null && !goldenID.equals(tz.getCanonicalID())) {
                        TimeZone goldenZone = TimeZone.getTimeZone(goldenID);
                        int[] offsets1 = {0, 0};

                        // Check offset in the golden zone with wall time.
                        // With getOffset(date, false, offsets1),
                        // you may get incorrect results because of time overlap at DST->STD
                        // transition.
                        goldenZone.getOffset(date + offsets[0] + offsets[1], true, offsets1);

                        if (offsets[0] != offsets1[0] || offsets[1] != offsets1[1]) {
                            // Now we need to use a partial location format.
                            name = formatPartialLocation(tzID, mzName);
                        }
                    } else {
                        name = mzName;
                    }
                }
            }
        }
        return name;
    }
    /**
     * Private enum definitions for message pattern formats
     */
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

    /**
     * Private simple pattern formatter used for formatting generic location names
     * and partial location names. We intentionally use JDK MessageFormat
     * for performance reason.
     * 
     * @param pat the message pattern enum
     * @param args the format argument(s)
     * @return the formatted string
     */
    private synchronized String formatPattern(Pattern pat, String... args) {
        if (_patternFormatters == null) {
            _patternFormatters = new MessageFormat[Pattern.values().length];
        }

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

    /**
     * Private method returning LocaleDisplayNames instance for the locale of this
     * instance. Because LocaleDisplayNames is only used for generic
     * location formant and partial location format, the LocaleDisplayNames
     * is instantiated lazily.
     * 
     * @return the instance of LocaleDisplayNames for the locale of this object.
     */
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

    /**
     * Private method returning the target region. The target regions is determined by
     * the locale of this instance. When a generic name is coming from
     * a meta zone, this region is used for checking if the time zone
     * is a reference zone of the meta zone.
     * 
     * @return the target region
     */
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

    /**
     * Private method for formatting partial location names. This format
     * is used when a generic name of a meta zone is available, but the given
     * time zone is not a reference zone (golden zone) of the meta zone.
     * 
     * @param tzID the time zone ID
     * @param mzDisplayName the meta zone generic display name
     * @return the partial location format string
     */
    private String formatPartialLocation(String tzID, String mzDisplayName) {
        String name;
        String location = null;
        String countryCode = ZoneMeta.getSingleCountry(tzID);
        if (countryCode != null) {
            location = getLocaleDisplayNames().regionDisplayName(countryCode);
        } else {
            location = _tznames.getExemplarLocationName(tzID);
            if (location == null) {
                // This could happen when the time zone is not associated with a country,
                // and its ID is not hierarchical, for example, CST6CDT.
                // We use the canonical ID itself as the location for this case.
                location = tzID;
            }
        }
        name = formatPattern(Pattern.FALLBACK_FORMAT, location, mzDisplayName);
        return name;
    }

    private static class NameInfo {
        String tzID;
        GenericNameType type;
    }

    public static class GenericMatchInfo {
        NameInfo info;
        int matchLength;

        public GenericNameType type() {
            return info.type;
        }

        public String tzID() {
            return info.tzID;
        }

        public int matchLength() {
            return matchLength;
        }
    }

    private static class GenericNameSearchHandler implements ResultHandler<NameInfo> {
        private GenericNameType[] _types;
        private Collection<GenericMatchInfo> _matches;

        GenericNameSearchHandler(GenericNameType[] types) {
            _types = types;
        }

        /* (non-Javadoc)
         * @see com.ibm.icu.impl.TextTrieMap.ResultHandler#handlePrefixMatch(int, java.util.Iterator)
         */
        public boolean handlePrefixMatch(int matchLength, Iterator<NameInfo> values) {
            while (values.hasNext()) {
                NameInfo info = values.next();
                if (_types != null) {
                    boolean bInclude = false;
                    for (GenericNameType t : _types) {
                        if (t == info.type) {
                            bInclude = true;
                            break;
                        }
                    }
                    if (!bInclude) {
                        continue;
                    }
                }
                GenericMatchInfo matchInfo = new GenericMatchInfo();
                matchInfo.info = info;
                matchInfo.matchLength = matchLength;
                if (_matches == null) {
                    _matches = new LinkedList<GenericMatchInfo>();
                }
                _matches.add(matchInfo);
            }
            return true;
        }

        /**
         * Returns the match results
         * @return the match results
         */
        public Collection<GenericMatchInfo> getMatches() {
            if (_matches == null) {
                return Collections.emptyList();
            }
            return _matches;
        }
    }

    private Collection<GenericMatchInfo> find(String text, int start, GenericNameType[] types) {
        if (_namesTrie == null) {
            synchronized (this) {
                if (_namesTrie == null) {
                    // Create the names trie. This could be very heavy process.
                    _namesTrie = new TextTrieMap<NameInfo>(true);
                    final NameType[] genNonLocTypes = {NameType.LONG_GENERIC, NameType.SHORT_GENERIC};

                    Set<String> tzIDs = TimeZone.getAvailableIDs(SystemTimeZoneType.CANONICAL, null, null);
                    for (String tzID : tzIDs) {
                        // Generic location name
                        String genericLocation = getGenericLocationName(tzID);
                        if (genericLocation != null) {
                            NameInfo info = new NameInfo();
                            info.tzID = tzID;
                            info.type = GenericNameType.LOCATION;
                            _namesTrie.put(genericLocation, info);
                        }

                        // Generic partial location format
                        Set<String> mzIDs = _tznames.getAvailableMetaZoneIDs(tzID);
                        for (String mzID : mzIDs) {
                            // if this time zone is not the golden zone of the meta zone,
                            // partial location name (such as "PT (Los Angeles)") might be
                            // available.
                            String goldenID = _tznames.getReferenceZoneID(mzID, getTargetRegion());
                            if (!tzID.equals(goldenID)) {
                                for (NameType genNonLocType : genNonLocTypes) {
                                    String mzGenName = _tznames.getMetaZoneDisplayName(mzID, genNonLocType);
                                    if (mzGenName != null) {
                                        String partialLocationName = formatPartialLocation(tzID, mzGenName);
                                        NameInfo info = new NameInfo();
                                        info.tzID = tzID;
                                        info.type = genNonLocType == NameType.LONG_GENERIC ?
                                                GenericNameType.LONG : GenericNameType.SHORT;
                                        _namesTrie.put(partialLocationName, info);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        GenericNameSearchHandler handler = new GenericNameSearchHandler(types);
        _namesTrie.find(text, start, handler);
        return handler.getMatches();
    }

    public GenericMatchInfo findMatch(String text, int start, GenericNameType preferredType) {
        MatchInfo TZNamesMatch = null;
        boolean tzTypeMatch = false;
        Collection<MatchInfo> tzmatches = _tznames.find(text, start, TZNAMES_GENERIC_TYPES);
        for (MatchInfo m : tzmatches) {
            boolean bTypeMatch = (preferredType == null)
                        || (preferredType == GenericNameType.LONG && (m.nameType() == NameType.LONG_GENERIC || m.nameType() == NameType.LONG_STANDARD))
                        || (preferredType == GenericNameType.SHORT && (m.nameType() == NameType.SHORT_GENERIC || m.nameType() == NameType.SHORT_STANDARD_COMMONLY_USED));
            if (!tzTypeMatch && bTypeMatch) {
                tzTypeMatch = true;
            }
            if (TZNamesMatch == null) {
                TZNamesMatch = m;
            } else if (bTypeMatch && m.matchLength() > TZNamesMatch.matchLength()) {
                TZNamesMatch = m;
            }
        }

        GenericMatchInfo GNamesMatch = null;
        boolean gTypeMatch = false;
        Collection<GenericMatchInfo> gmatches = find(text, start, GenericNameType.values());
        for (GenericMatchInfo gm : gmatches) {
            boolean bTypeMatch = (preferredType == null || preferredType == gm.type());
            if (!gTypeMatch && bTypeMatch) {
                gTypeMatch = true;
            }
            if (GNamesMatch == null) {
                GNamesMatch = gm;
            } else if (bTypeMatch && gm.matchLength() > GNamesMatch.matchLength()) {
                GNamesMatch = gm;
            }
        }

        if (TZNamesMatch != null) {
            if (GNamesMatch != null) {
                // found matches from both
                if (!gTypeMatch) {
                    if (tzTypeMatch || TZNamesMatch.matchLength() > GNamesMatch.matchLength()) {
                        GNamesMatch = null;
                    }
                } else if (tzTypeMatch && TZNamesMatch.matchLength() > GNamesMatch.matchLength()) {
                    GNamesMatch = null;
                }
            }
            if (GNamesMatch == null) {
                // transform
                String tzID = TZNamesMatch.tzID();
                if (tzID == null) {
                    tzID = _tznames.getReferenceZoneID(TZNamesMatch.mzID(), getTargetRegion());
                }
                GenericNameType gtype = (TZNamesMatch.nameType() == NameType.LONG_GENERIC || TZNamesMatch.nameType() == NameType.LONG_STANDARD) ?
                        GenericNameType.LONG : GenericNameType.SHORT;

                GNamesMatch = new GenericMatchInfo();
                GNamesMatch.matchLength = TZNamesMatch.matchLength();
                NameInfo ninfo = new NameInfo();
                ninfo.type = gtype;
                ninfo.tzID = tzID;
                GNamesMatch.info = ninfo;
            }
        }

        return GNamesMatch;
    }

    public GenericMatchInfo findMatch(String text, int start) {
        return findMatch(text, start, null);
    }

    private static class Cache extends SoftCache<String, TimeZoneGenericNames, ULocale> {

        /* (non-Javadoc)
         * @see com.ibm.icu.impl.CacheBase#createInstance(java.lang.Object, java.lang.Object)
         */
        @Override
        protected TimeZoneGenericNames createInstance(String key, ULocale data) {
            return new TimeZoneGenericNames(data);
        }
        
    }
}
