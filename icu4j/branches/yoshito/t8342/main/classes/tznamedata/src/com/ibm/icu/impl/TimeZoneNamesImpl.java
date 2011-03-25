/*
 *******************************************************************************
 * Copyright (C) 2011, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.util.MissingResourceException;

import com.ibm.icu.text.TimeZoneNames;
import com.ibm.icu.util.ULocale;

/**
 * @author yumaoka
 *
 */
public class TimeZoneNamesImpl extends TimeZoneNames {

    private ICUResourceBundle _zoneStrings;
    private MZNamesCache _mzCache = new MZNamesCache();
    private TZNamesCache _tzCache = new TZNamesCache();

    private static final String ZONE_STRINGS_BUNDLE = "zoneStrings";
    private static final String MZ_PREFIX = "meta:";

    public TimeZoneNamesImpl(ULocale locale) {
        try {
            ICUResourceBundle bundle = (ICUResourceBundle)ICUResourceBundle.getBundleInstance(
                    ICUResourceBundle.ICU_ZONE_BASE_NAME, locale);
            _zoneStrings = (ICUResourceBundle)bundle.get(ZONE_STRINGS_BUNDLE);
        } catch (MissingResourceException mre) {
            _zoneStrings = null;
        }
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneNames#getLocale()
     */
    @Override
    public ULocale getLocale() {
        if (_zoneStrings == null) {
            return ULocale.ROOT;
        }
        return _zoneStrings.getULocale();
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneNames#getMetaZoneID(java.lang.String, long)
     */
    @Override
    public String getMetaZoneID(String tzID, long time) {
        // TODO We probably should move metaZones.res to tznamedata package later
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
        if (_zoneStrings == null || mzID == null || mzID.length() == 0) {
            return null;
        }
        ZNames names = _mzCache.getInstance(mzID, mzID);
        return names.getName(type);
    }

    /* (non-Javadoc)
     * @see com.ibm.icu.text.TimeZoneNames#getTimeZoneDisplayName(java.lang.String, com.ibm.icu.text.TimeZoneNames.NameType, long)
     */
    @Override
    protected String getTimeZoneDisplayName(String tzID, NameType type) {
        if (_zoneStrings == null || tzID == null || tzID.length() == 0) {
            return null;
        }
        TZNames names = _tzCache.getInstance(tzID, tzID);
        return names.getName(type);
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
     * This class stores name data for a meta zone
     */
    private static class ZNames {
        private String[] _names;
        private boolean _shortCommonlyUsed;

        public static final ZNames EMPTY = new ZNames(null, false);

        // KEYS must be synchronized with TimeZoneNames.NameType
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
            int idx = type.getIndex();
            if (idx >= 0 && idx < _names.length) {
                return _names[idx];
            }
            return null;
        }

        public boolean isShortNamesCommonlyUsed() {
            return _shortCommonlyUsed;
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
            for (NameType type : NameType.values()) {
                int idx = type.getIndex();
                if (idx < 0 || idx >= names.length) {
                    continue;
                }
                try {
                    names[idx] = table.getStringWithFallback(KEYS[idx]);
                    isEmpty = false;
                } catch (MissingResourceException e) {
                    names[idx] = null;
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
}
