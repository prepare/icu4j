/*
 *******************************************************************************
 * Copyright (C) 2011, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import com.ibm.icu.impl.ICUConfig;
import com.ibm.icu.impl.SoftCache;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;

//TODO provide formal documentation of the class
/**
 * <p>
 * The methods in this class assume that time zone IDs are already canonicalized. For example, you may not get proper
 * result returned by a method with time zone ID "America/Indiana/Indianapolis", because it's not a canonical time zone
 * ID (the canonical time zone ID for the time zone is "America/Indianapolis". See
 * {@link TimeZone#getCanonicalID(String)} about ICU canonical time zone IDs.
 * 
 * <p>
 * In CLDR, most of time zone display names except location names are provided through meta zones. But a time zone may
 * have a specific name that is not shared with other time zones.
 * 
 * <p>
 * For example, time zone "Europe/London" has English long name for standard time "Greenwich Mean Time", which is also
 * shared with other time zones. However, the long name for daylight saving time is "British Summer Time", which is only
 * used for "Europe/London".
 * 
 * <p>
 * {@link #getTimeZoneDisplayName(String, NameType)} is designed for accessing a name only used by a single time zone.
 * But is not necessarily mean that a subclass implementation use the same model with CLDR. A subclass implementation
 * may provide time zone names only through {@link #getTimeZoneDisplayName(String, NameType)}, or only through
 * {@link #getMetaZoneDisplayName(String, NameType)}, or both.
 * 
 */
public abstract class TimeZoneNames implements Serializable {

    /**
     * Time zone display name types
     * 
     * @draft ICU 4.8
     */
    public enum NameType {
        /**
         * Long display name, such as "Eastern Time".
         * 
         * @draft ICU 4.8
         */
        LONG_GENERIC,
        /**
         * Long display name for standard time, such as "Eastern Standard Time".
         * 
         * @draft ICU 4.8
         */
        LONG_STANDARD,
        /**
         * Long display name for daylight saving time, such as "Eastern Daylight Time".
         * 
         * @draft ICU 4.8
         */
        LONG_DAYLIGHT,
        /**
         * Short display name, such as "ET".
         * 
         * @draft ICU 4.8
         */
        SHORT_GENERIC,
        /**
         * Short display name for standard time, such as "EST".
         * 
         * @draft ICU 4.8
         */
        SHORT_STANDARD,
        /**
         * Short display name for daylight saving time, such as "EDT".
         * 
         * @draft ICU 4.8
         */
        SHORT_DAYLIGHT;

        public static int getMaxOrdinal() {
            return SHORT_DAYLIGHT.ordinal();
        }
    }

    private static Cache TZNAMES_CACHE = new Cache();

    private static final Factory TZNAMES_FACTORY;
    private static final String FACTORY_NAME_PROP = "com.ibm.icu.text.TimeZoneNames.Factory.impl";
    private static final String DEFAULT_FACTORY_CLASS = "com.ibm.icu.impl.TimeZoneNamesFactoryImpl";
    private static final Pattern LOC_EXCLUSION_PATTERN = Pattern.compile("Etc/.*|SystemV/.*|.*/Riyadh8[7-9]");

    static {
        Factory factory = null;
        String classname = ICUConfig.get(FACTORY_NAME_PROP, DEFAULT_FACTORY_CLASS);
        while (true) {
            try {
                factory = (Factory) Class.forName(classname).newInstance();
                break;
            } catch (ClassNotFoundException cnfe) {
                // fall through
            } catch (IllegalAccessException iae) {
                // fall through
            } catch (InstantiationException ie) {
                // fall through
            }
            if (classname.equals(DEFAULT_FACTORY_CLASS)) {
                break;
            }
            classname = DEFAULT_FACTORY_CLASS;
        }

        if (factory == null) {
            factory = new DefaultTimeZoneNames.FactoryImpl();
        }
        TZNAMES_FACTORY = factory;
    }

    /**
     * Returns an instance of <code>TimeZoneDisplayNames</code> for the specified locale.
     * 
     * @param locale
     *            The locale.
     * @return An instance of <code>TimeZoneDisplayNames</code>
     * @draft ICU 4.8
     */
    public static TimeZoneNames getInstance(ULocale locale) {
        String canonicalID = ULocale.canonicalize(locale.toString());
        return TZNAMES_CACHE.getInstance(canonicalID, locale);
    }

    /**
     * Returns an immutable set of all available meta zone IDs.
     * @return An immutable set of all available meta zone IDs.
     * @draft ICU 4.8
     */
    public abstract Set<String> getAvailableMetaZoneIDs();

    /**
     * Returns an immutable set of all available meta zone IDs used by the given time zone.
     * 
     * @param tzID
     *            The canoniacl time zone ID.
     * @return An immutable set of all available meta zone IDs used by the given time zone.
     * @draft ICU 4.8
     */
    public abstract Set<String> getAvailableMetaZoneIDs(String tzID);

    /**
     * Returns the meta zone ID for the given canonical time zone ID at the given date.
     * 
     * @param tzID
     *            The canonical time zone ID.
     * @param date
     *            The date.
     * @return The meta zone ID for the given time zone ID at the given date. If the time zone does not have a
     *         corresponding meta zone at the given date or the implementation does not support meta zones, null is
     *         returned.
     * @draft ICU 4.8
     */
    public abstract String getMetaZoneID(String tzID, long date);

    /**
     * Returns the reference zone ID for the given meta zone ID for the region.
     * 
     * @param mzID
     *            The meta zone ID.
     * @param region
     *            The region.
     * @return The reference zone ID ("golden zone" in the LDML specification) for the given time zone ID for the
     *         region. If the meta zone is unknown or the implementation does not support meta zones, null is returned.
     */
    public abstract String getReferenceZoneID(String mzID, String region);

    /**
     * Returns the display name of the meta zone.
     * 
     * @param mzID
     *            The meta zone ID.
     * @param type
     *            The display name type. See {@link TimeZoneNames.NameType}.
     * @param isCommonlyUsed
     *            The optional output boolean value indicating if the display name is commonly used.
     * @return The display name of the meta zone. When this object does not have a localized display name for the given
     *         meta zone with the specified type or the implementation does not provide any display names associated
     *         with meta zones, null is returned.
     * @draft ICU 4.8
     */
    public abstract String getMetaZoneDisplayName(String mzID, NameType type, boolean[] isCommonlyUsed);

    /**
     * Returns the display name of the time zone at the given date.
     * 
     * <p>
     * <b>Note:</b> This method calls the subclass's {@link #getTimeZoneDisplayName(String, NameType)} first. When the
     * result is null, this method calls {@link #getMetaZoneID(String, long)} to get the meta zone ID mapped from the
     * time zone, then calls {@link #getMetaZoneDisplayName(String, NameType)}.
     * 
     * @param tzID
     *            The canonical time zone ID.
     * @param type
     *            The display name type. See {@link TimeZoneNames.NameType}.
     * @param date
     *            The date
     * @param isCommonlyUsed
     *            The optional output boolean value indicating if the display name is commonly used.
     * @return The display name for the time zone at the given date. When this object does not have a localized display
     *         name for the time zone with the specified type and date, null is returned.
     * @draft ICU 4.8
     */
    public final String getDisplayName(String tzID, NameType type, long date, boolean[] isCommonlyUsed) {
        String name = getTimeZoneDisplayName(tzID, type, isCommonlyUsed);
        if (name == null) {
            String mzID = getMetaZoneID(tzID, date);
            name = getMetaZoneDisplayName(mzID, type, isCommonlyUsed);
        }
        return name;
    }

    /**
     * Returns the display name of the time zone. Unlike {@link #getDisplayName(String, NameType, long, boolean[])},
     * this method does not get a name from a mata zone used by the time zone.
     * 
     * @param tzID
     *            The canonical time zone ID.
     * @param type
     *            The display name type. See {@link TimeZoneNames.NameType}.
     * @param isCommonlyUsed
     *            The optional output boolean value indicating if the display name is commonly used.
     * @return The display name for the time zone. When this object does not have a localized display name for the given
     *         time zone with the specified type, null is returned.
     * @internal
     */
    public abstract String getTimeZoneDisplayName(String tzID, NameType type, boolean[] isCommonlyUsed);

    /**
     * Returns the exemplar location name for the given time zone. When this object does not have a localized location
     * name, the default implementation may still returns a programmatically generated name with the logic described
     * below.
     * <ol>
     * <li>Check if the ID contains "/". If not, return null.
     * <li>Check if the ID does not start with "Etc/" or "SystemV/". If it does, return null.
     * <li>Extract a substring after the last occurrence of "/".
     * <li>Replace "_" with " ".
     * </ol>
     * For example, "New York" is returned for the time zone ID "America/New_York" when this object does not have the
     * localized location name.
     * 
     * @param tzID
     *            The canonical time zone ID
     * @return The exemplar location name for the given time zone, or null when a localized location name is not
     *         available and the fallback logic described above cannot extract location from the ID.
     */
    public String getExemplarLocationName(String tzID) {
        if (tzID == null || tzID.length() == 0 || LOC_EXCLUSION_PATTERN.matcher(tzID).matches()) {
            return null;
        }

        String location = null;
        int sep = tzID.lastIndexOf('/');
        if (sep > 0 && sep + 1 < tzID.length()) {
            location = tzID.substring(sep + 1).replace('_', ' ');
        }

        return location;
    }

    /**
     * Sole constructor for invocation by subclass constructors.
     * 
     * @internal
     */
    protected TimeZoneNames() {
    }

    /**
     * The super class of <code>TimeZoneNames</code> service factory classes.
     * 
     * @internal
     */
    public static abstract class Factory {
        /**
         * The factory method of <code>TimeZoneNames</code>.
         * 
         * @param locale
         *            The display locale
         * @return An instance of <code>TimeZoneNames</code>.
         * @internal
         */
        public abstract TimeZoneNames getTimeZoneNames(ULocale locale);
    }

    /**
     * TimeZoneNames cache used by {@link TimeZoneNames#getInstance(ULocale)}
     */
    private static class Cache extends SoftCache<String, TimeZoneNames, ULocale> {

        /*
         * (non-Javadoc)
         * 
         * @see com.ibm.icu.impl.CacheBase#createInstance(java.lang.Object, java.lang.Object)
         */
        @Override
        protected TimeZoneNames createInstance(String key, ULocale data) {
            return TZNAMES_FACTORY.getTimeZoneNames(data);
        }

    }

    /**
     * The default implementation of <code>TimeZoneNames</code> used by {@link TimeZoneNames#getInstance(ULocale)} when
     * the ICU4J tznamedata component is not available.
     */
    private static class DefaultTimeZoneNames extends TimeZoneNames {

        public static final DefaultTimeZoneNames INSTANCE = new DefaultTimeZoneNames();

        /* (non-Javadoc)
         * @see com.ibm.icu.text.TimeZoneNames#getAvailableMetaZoneIDs()
         */
        @Override
        public Set<String> getAvailableMetaZoneIDs() {
            return Collections.emptySet();
        }

        /* (non-Javadoc)
         * @see com.ibm.icu.text.TimeZoneNames#getAvailableMetaZoneIDs(java.lang.String)
         */
        @Override
        public Set<String> getAvailableMetaZoneIDs(String tzID) {
            return Collections.emptySet();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.ibm.icu.text.TimeZoneNames#getMetaZoneID (java.lang.String, long)
         */
        @Override
        public String getMetaZoneID(String tzID, long date) {
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.ibm.icu.text.TimeZoneNames#getReferenceZoneID(java.lang.String, java.lang.String)
         */
        @Override
        public String getReferenceZoneID(String mzID, String region) {
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.ibm.icu.text.TimeZoneNames#getMetaZoneDisplayName (java.lang.String,
         * com.ibm.icu.text.TimeZoneNames.NameType)
         */
        @Override
        public String getMetaZoneDisplayName(String mzID, NameType type, boolean[] isCommonlyUsed) {
            if (isCommonlyUsed != null && isCommonlyUsed.length > 0) {
                isCommonlyUsed[0] = false;
            }
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.ibm.icu.text.TimeZoneNames#getTimeZoneDisplayName (java.lang.String,
         * com.ibm.icu.text.TimeZoneNames.NameType, long)
         */
        @Override
        public String getTimeZoneDisplayName(String tzID, NameType type, boolean[] isCommonlyUsed) {
            if (isCommonlyUsed != null && isCommonlyUsed.length > 0) {
                isCommonlyUsed[0] = false;
            }
            return null;
        }

        /**
         * The default <code>TimeZoneNames</code> factory called from {@link TimeZoneNames#getInstance(ULocale)} when
         * the ICU4J tznamedata component is not available.
         */
        public static class FactoryImpl extends Factory {

            /*
             * (non-Javadoc)
             * 
             * @see com.ibm.icu.text.TimeZoneNames.Factory#getTimeZoneNames (com.ibm.icu.util.ULocale)
             */
            @Override
            public TimeZoneNames getTimeZoneNames(ULocale locale) {
                return DefaultTimeZoneNames.INSTANCE;
            }
        }
    }
}
