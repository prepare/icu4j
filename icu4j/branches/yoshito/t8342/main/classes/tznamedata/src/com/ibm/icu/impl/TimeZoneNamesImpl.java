/*
 *******************************************************************************
 * Copyright (C) 2011, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

import com.ibm.icu.impl.TextTrieMap.ResultHandler;
import com.ibm.icu.text.TimeZoneNames;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.TimeZone.SystemTimeZoneType;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.UResourceBundle;

/**
 * The standard ICU implementation of TimeZoneNames
 */
public class TimeZoneNamesImpl extends TimeZoneNames {

    private static final long serialVersionUID = -2179814848495897472L;

    private static final String ZONE_STRINGS_BUNDLE = "zoneStrings";
    private static final String MZ_PREFIX = "meta:";

    private static Set<String> METAZONE_IDS;
    private static final TZ2MZsCache TZ_TO_MZS_CACHE = new TZ2MZsCache();
    private static final MZ2TZsCache MZ_TO_TZS_CACHE = new MZ2TZsCache();

    private transient ICUResourceBundle _zoneStrings;
    private transient MZNamesCache _mzCache;
    private transient TZNamesCache _tzCache;

    private transient volatile TextTrieMap<NameInfo> _namesTrie;

    public TimeZoneNamesImpl(ULocale locale) {
        initialize(locale);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneNames#getAvailableMetaZoneIDs()
     */
    @Override
    public synchronized Set<String> getAvailableMetaZoneIDs() {
        if (METAZONE_IDS == null) {
            try {
                UResourceBundle bundle = UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, "metaZones");
                UResourceBundle mapTimezones = bundle.get("mapTimezones");
                Set<String> keys = mapTimezones.keySet();
                METAZONE_IDS = Collections.unmodifiableSet(keys);
            } catch (MissingResourceException e) {
                METAZONE_IDS = Collections.emptySet();
            }
        }
        return METAZONE_IDS;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneNames#getAvailableMetaZoneIDs(java.lang.String)
     */
    @Override
    public Set<String> getAvailableMetaZoneIDs(String tzID) {
        List<MZMapEntry> maps = TZ_TO_MZS_CACHE.getInstance(tzID, tzID);
        if (maps.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> mzIDs = new HashSet<String>(maps.size());
        for (MZMapEntry map : maps) {
            mzIDs.add(map.mzID());
        }
        // make it unmodifiable because of the API contract. We may cache the results in futre.
        return Collections.unmodifiableSet(mzIDs);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneNames#getMetaZoneID(java.lang.String, long)
     */
    @Override
    public String getMetaZoneID(String tzID, long date) {
        String mzID = null;
        List<MZMapEntry> maps = TZ_TO_MZS_CACHE.getInstance(tzID, tzID);
        for (MZMapEntry map : maps) {
            if (date >= map.from() && date < map.to()) {
                mzID = map.mzID();
                break;
            }
        }
        return mzID;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneNames#getReferenceZoneID(java.lang.String, java.lang.String)
     */
    @Override
    public String getReferenceZoneID(String mzID, String region) {
        String refID = null;
        Map<String, String> regionTzMap = MZ_TO_TZS_CACHE.getInstance(mzID, mzID);
        if (!regionTzMap.isEmpty()) {
            refID = regionTzMap.get(region);
            if (refID == null) {
                refID = regionTzMap.get("001");
            }
        }
        return refID;
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneNames#getMetaZoneDisplayName(java.lang.String, com.ibm.icu.text.TimeZoneNames.NameType)
     */
    @Override
    public String getMetaZoneDisplayName(String mzID, NameType type) {
        String name = null;
        ZNames names = null;
        if (_zoneStrings != null && mzID != null && mzID.length() > 0) {
            names = _mzCache.getInstance(mzID, mzID);
            name = names.getName(type);
        }
        return name;
    }

    /*
     * (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneNames#getTimeZoneDisplayName(java.lang.String, com.ibm.icu.text.TimeZoneNames.NameType)
     */
    @Override
    public String getTimeZoneDisplayName(String tzID, NameType type) {
        String name = null;
        TZNames names = null;
        if (_zoneStrings != null && tzID != null && tzID.length() > 0) {
            names = _tzCache.getInstance(tzID, tzID);
            name = names.getName(type);
        }
        return name;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneNames#getExemplarLocationName(java.lang.String)
     */
    @Override
    public String getExemplarLocationName(String tzID) {
        String locName = null;
        if (_zoneStrings != null && tzID != null && tzID.length() != 0) {
            TZNames names = _tzCache.getInstance(tzID, tzID);
            locName = names.getLocationName();
        }
        if (locName == null) {
            locName = super.getExemplarLocationName(tzID);
        }
        return locName;
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneNames#find(java.lang.String, int, java.util.Set)
     */
    @Override
    public Collection<MatchInfo> find(String text, int start, NameType[] nameTypes) {
        if (_namesTrie == null) {
            synchronized (this) {
                if (_namesTrie == null) {
                    // Create the names trie. This could be very heavy process.
                    _namesTrie = new TextTrieMap<NameInfo>(true);

                    // time zones
                    Set<String> tzIDs = TimeZone.getAvailableIDs(SystemTimeZoneType.CANONICAL, null, null);
                    for (String tzID : tzIDs) {
                        for (NameType nameType : NameType.values()) {
                            String name = getTimeZoneDisplayName(tzID, nameType);
                            if (name != null) {
                                NameInfo info = new NameInfo();
                                info.tzID = tzID;
                                info.type = nameType;
                                _namesTrie.put(name, info);
                            }
                        }
                    }

                    // meta zones
                    Set<String> mzIDs = getAvailableMetaZoneIDs();
                    for (String mzID : mzIDs) {
                        for (NameType nameType : NameType.values()) {
                            String name = getMetaZoneDisplayName(mzID, nameType);
                            if (name != null) {
                                NameInfo info = new NameInfo();
                                info.mzID = mzID;
                                info.type = nameType;
                                _namesTrie.put(name, info);
                            }
                        }
                    }
                }
            }
        }
        NameSearchHandler handler = new NameSearchHandler(nameTypes);
        _namesTrie.find(text, start, handler);
        return handler.getMatches();
    }

    /**
     * Initialize the transient fields, called from the constructor and
     * readObject.
     * 
     * @param locale The locale
     */
    private void initialize(ULocale locale) {
        if (locale == null) {
            return;
        }
        try {
            ICUResourceBundle bundle = (ICUResourceBundle)ICUResourceBundle.getBundleInstance(
                    ICUResourceBundle.ICU_ZONE_BASE_NAME, locale);
            _zoneStrings = (ICUResourceBundle)bundle.get(ZONE_STRINGS_BUNDLE);
        } catch (MissingResourceException mre) {
            _zoneStrings = null;
        }

        _mzCache = new MZNamesCache();
        _tzCache = new TZNamesCache();
    }

    /*
     * The custom serialization method.
     * This implementation only preserve locale used for the names.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        ULocale locale = _zoneStrings == null ? null : _zoneStrings.getULocale();
        out.writeObject(locale);
    }

    /*
     * The custom deserialization method.
     * This implementation only read locale used by the object.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ULocale locale = (ULocale)in.readObject();
        initialize(locale);
    }

    private class MZNamesCache extends SoftCache<String, ZNames, String> {
        /* (non-Javadoc)
         * @see com.ibm.icu.impl.CacheBase#createInstance(java.lang.Object, java.lang.Object)
         */
        @Override
        protected ZNames createInstance(String key, String data) {
            return ZNames.getInstance(_zoneStrings, MZ_PREFIX + data);
        }
    }

    private class TZNamesCache extends SoftCache<String, TZNames, String> {
        /* (non-Javadoc)
         * @see com.ibm.icu.impl.CacheBase#createInstance(java.lang.Object, java.lang.Object)
         */
        @Override
        protected TZNames createInstance(String key, String data) {
            return TZNames.getInstance(_zoneStrings, data.replace('/', ':'));
        }
    }

    /**
     * An instance of NameInfo is stored in the zone names trie.
     */
    private static class NameInfo {
        String tzID;
        String mzID;
        NameType type;
    }

    /**
     * NameSearchHandler is used for collecting name matches.
     */
    private static class NameSearchHandler implements ResultHandler<NameInfo> {
        private NameType[] _nameTypes;
        private Collection<MatchInfo> _matches;

        NameSearchHandler(NameType[] nameTypes) {
            _nameTypes = nameTypes;
        }

        /* (non-Javadoc)
         * @see com.ibm.icu.impl.TextTrieMap.ResultHandler#handlePrefixMatch(int, java.util.Iterator)
         */
        public boolean handlePrefixMatch(int matchLength, Iterator<NameInfo> values) {
            while (values.hasNext()) {
                NameInfo ninfo = values.next();
                if (_nameTypes != null) {
                    boolean bInclude = false;
                    for (NameType t : _nameTypes) {
                        if (t == ninfo.type) {
                            // This name type was included in the requested list
                            bInclude = true;
                            break;
                        }
                    }
                    if (!bInclude) {
                        continue;
                    }
                }
                MatchInfo minfo;
                if (ninfo.tzID != null) {
                    minfo = MatchInfo.createTimeZoneMatch(ninfo.tzID, ninfo.type, matchLength);
                } else {
                    assert(ninfo.mzID != null);
                    minfo = MatchInfo.createMetaZoneMatch(ninfo.mzID, ninfo.type, matchLength);
                }
                if (_matches == null) {
                    _matches = new LinkedList<MatchInfo>();
                }
                _matches.add(minfo);
            }
            return true;
        }

        /**
         * Returns the match results
         * @return the match results
         */
        public Collection<MatchInfo> getMatches() {
            if (_matches == null) {
                return Collections.emptyList();
            }
            return _matches;
        }
    }

    /**
     * This class stores name data for a meta zone
     */
    private static class ZNames {
        private String[] _names;
        private boolean _shortCommonlyUsed;

        public static final ZNames EMPTY = new ZNames(null, false);

        private static final String[] KEYS = {"lg", "ls", "ld", "sg", "ss", "sd"};

        protected ZNames(String[] names, boolean shortCommonlyUsed) {
            _names = names;
            _shortCommonlyUsed = shortCommonlyUsed;
        }

        public static ZNames getInstance(ICUResourceBundle zoneStrings, String key) {
            boolean[] cu = new boolean[1];
            String[] names = loadData(zoneStrings, key, cu);
            if (names == null) {
                return EMPTY;
            }
            return new ZNames(names, cu[0]);
        }

        public String getName(NameType type) {
            if (_names == null) {
                return null;
            }
            String name = null;
            switch (type) {
            case LONG_GENERIC:
                name = _names[0];
                break;
            case LONG_STANDARD:
                name = _names[1];
                break;
            case LONG_DAYLIGHT:
                name = _names[2];
                break;
            case SHORT_GENERIC:
                if (_shortCommonlyUsed) {
                    name = _names[3];
                }
                break;
            case SHORT_STANDARD:
                name = _names[4];
                break;
            case SHORT_DAYLIGHT:
                name = _names[5];
                break;
            case SHORT_STANDARD_COMMONLY_USED:
                if (_shortCommonlyUsed) {
                    name = _names[4];
                }
                break;
            case SHORT_DAYLIGHT_COMMONLY_USED:
                if (_shortCommonlyUsed) {
                    name = _names[5];
                }
                break;
            }

            return name;
        }

        protected static String[] loadData(ICUResourceBundle zoneStrings, String key, boolean[] shortCommonlyUsed) {
            shortCommonlyUsed[0] = false;
            ICUResourceBundle table = null;
            try {
                table = zoneStrings.getWithFallback(key);
            } catch (MissingResourceException e) {
                return null;
            }

            boolean isEmpty = true;
            String[] names = new String[KEYS.length];
            for (int i = 0; i < names.length; i++) {
                try {
                    names[i] = table.getStringWithFallback(KEYS[i]);
                    isEmpty = false;
                } catch (MissingResourceException e) {
                    names[i] = null;
                }
            }

            if (isEmpty) {
                return null;
            }

            try {
                ICUResourceBundle cuRes = table.getWithFallback("cu");
                int cu = cuRes.getInt();
                shortCommonlyUsed[0] = (cu != 0);
            } catch (MissingResourceException e) {
                // cu is optional
            }

            return names;
        }
    }

    /**
     * This class stores name data for a single time zone
     */
    private static class TZNames extends ZNames {
        private String _locationName;

        public static final TZNames EMPTY = new TZNames(null, false, null);

        public static TZNames getInstance(ICUResourceBundle zoneStrings, String key) {
            ICUResourceBundle table = null;
            try {
                table = zoneStrings.getWithFallback(key);
            } catch (MissingResourceException e) {
                return EMPTY;
            }

            String locationName = null;
            try {
                locationName = table.getStringWithFallback("ec");
            } catch (MissingResourceException e) {
                // location name is optional
            }

            boolean[] cu = new boolean[1];
            String[] names = loadData(zoneStrings, key, cu);

            if (locationName == null && names == null) {
                return EMPTY;
            }
            return new TZNames(names, cu[0], locationName);
        }

        public String getLocationName() {
            return _locationName;
        }

        private TZNames(String[] names, boolean shortCommonlyUsed, String locationName) {
            super(names, shortCommonlyUsed);
            _locationName = locationName;
        }
    }


    //
    // Canonical time zone ID -> meta zone ID
    //

    private static class MZMapEntry {
        private String _mzID;
        private long _from;
        private long _to;

        MZMapEntry(String mzID, long from, long to) {
            _mzID = mzID;
            _from = from;
            _to = to;
        }

        String mzID() {
            return _mzID;
        }

        long from() {
            return _from;
        }

        long to() {
            return _to;
        }
    }

    private static class TZ2MZsCache extends SoftCache<String, List<MZMapEntry>, String> {
        /* (non-Javadoc)
         * @see com.ibm.icu.impl.CacheBase#createInstance(java.lang.Object, java.lang.Object)
         */
        @Override
        protected List<MZMapEntry> createInstance(String key, String data) {
            List<MZMapEntry> mzMaps = null;
            try {
                UResourceBundle bundle = UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, "metaZones");
                UResourceBundle metazoneInfoBundle = bundle.get("metazoneInfo");

                String tzkey = data.replace('/', ':');
                UResourceBundle zoneBundle = metazoneInfoBundle.get(tzkey);

                mzMaps = new ArrayList<MZMapEntry>(zoneBundle.getSize());
                for (int idx = 0; idx < zoneBundle.getSize(); idx++) {
                    UResourceBundle mz = zoneBundle.get(idx);
                    String mzid = mz.getString(0);
                    String fromStr = "1970-01-01 00:00";
                    String toStr = "9999-12-31 23:59";
                    if (mz.getSize() == 3) {
                        fromStr = mz.getString(1);
                        toStr = mz.getString(2);
                    }
                    long from, to;
                    from = parseDate(fromStr);
                    to = parseDate(toStr);
                    mzMaps.add(new MZMapEntry(mzid, from, to));
                }

            } catch (MissingResourceException mre) {
                // fall through
            }
            if (mzMaps == null) {
                mzMaps = Collections.emptyList();
            }
            return mzMaps;
        }

        private long parseDate (String text) {
            int year = 0, month = 0, day = 0, hour = 0, min = 0;
            int idx;
            int n;

            // "yyyy" (0 - 3)
            for (idx = 0; idx <= 3; idx++) {
                n = text.charAt(idx) - '0';
                if (n >= 0 && n < 10) {
                    year = 10*year + n;
                } else {
                    throw new IllegalArgumentException("Bad year");
                }
            }
            // "MM" (5 - 6)
            for (idx = 5; idx <= 6; idx++) {
                n = text.charAt(idx) - '0';
                if (n >= 0 && n < 10) {
                    month = 10*month + n;
                } else {
                    throw new IllegalArgumentException("Bad month");
                }
            }
            // "dd" (8 - 9)
            for (idx = 8; idx <= 9; idx++) {
                n = text.charAt(idx) - '0';
                if (n >= 0 && n < 10) {
                    day = 10*day + n;
                } else {
                    throw new IllegalArgumentException("Bad day");
                }
            }
            // "HH" (11 - 12)
            for (idx = 11; idx <= 12; idx++) {
                n = text.charAt(idx) - '0';
                if (n >= 0 && n < 10) {
                    hour = 10*hour + n;
                } else {
                    throw new IllegalArgumentException("Bad hour");
                }
            }
            // "mm" (14 - 15)
            for (idx = 14; idx <= 15; idx++) {
                n = text.charAt(idx) - '0';
                if (n >= 0 && n < 10) {
                    min = 10*min + n;
                } else {
                    throw new IllegalArgumentException("Bad minute");
                }
            }

            long date = Grego.fieldsToDay(year, month - 1, day) * Grego.MILLIS_PER_DAY
                        + hour * Grego.MILLIS_PER_HOUR + min * Grego.MILLIS_PER_MINUTE;
            return date;
         }
    }

    //
    // Meta zone ID -> time zone ID
    //

    private static class MZ2TZsCache extends SoftCache<String, Map<String, String>, String> {

        /* (non-Javadoc)
         * @see com.ibm.icu.impl.CacheBase#createInstance(java.lang.Object, java.lang.Object)
         */
        @Override
        protected Map<String, String> createInstance(String key, String data) {
            Map<String, String> map = null;
            try {
                UResourceBundle bundle = UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, "metaZones");
                UResourceBundle mapTimezones = bundle.get("mapTimezones");
                UResourceBundle regionMap = mapTimezones.get(key);

                Set<String> regions = regionMap.keySet();
                map = new HashMap<String, String>(regions.size());

                for (String region : regions) {
                    String tzID = regionMap.getString(region);
                    map.put(region, tzID);
                }
            } catch (MissingResourceException e) {
                // fall through
            }
            if (map == null) {
                map = Collections.emptyMap();
            }
            return map;
        }
    }
}
