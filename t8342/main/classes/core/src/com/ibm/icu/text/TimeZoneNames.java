/*
 *******************************************************************************
 * Copyright (C) 2011, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.util.regex.Pattern;

import com.ibm.icu.impl.ICUConfig;
import com.ibm.icu.impl.ZoneMeta;
import com.ibm.icu.util.ULocale;

//TODO provide formal documentation of the class
/**
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
public abstract class TimeZoneNames {

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
        SHORT_DAYLIGHT,
    }

    private static final String FACTORY_NAME_PROP = "com.ibm.icu.text.TimeZoneNames.Factory.impl";
    private static final String DEFAULT_FACTORY_CLASS = "com.ibm.icu.impl.TimeZoneNamesFactoryImpl";
    private static final Pattern LOC_EXCLUSION_PATTERN = Pattern.compile("Etc/.*|SystemV/.*|.*/Riyadh8[7-9]");

    private static final Factory TZNAMES_FACTORY;
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
        return TZNAMES_FACTORY.getTimeZoneNames(locale);
    }

    /**
     * Returns the locale used to determine the time zone display names. This is not necessarily the same locale passed
     * to {@link #getInstance}.
     * 
     * @return The locale used for the time zone display names.
     * @draft ICU 4.8
     */
    public abstract ULocale getLocale();

    /**
     * Returns the meta zone ID for the given time zone ID at the given date.
     * 
     * @param tzID
     *            The time zone ID.
     * @param date
     *            The date.
     * @return The meta zone ID for the given time zone ID at the given date. If the time zone does not have a
     *         corresponding meta zone at the given date, null is returned.
     * @draft ICU 4.8
     */
    public abstract String getMetaZoneID(String tzID, long date);

    /**
     * Returns the display name of the meta zone.
     * 
     * @param mzID
     *            The meta zone ID.
     * @param type
     *            The display name type. See {@link TimeZoneNames.NameType}.
     * @return The display name of the meta zone. When this object does not have a localized display name for the given
     *         meta zone with the specified type, null is returned.
     * @draft ICU 4.8
     */
    public abstract String getMetaZoneDisplayName(String mzID, NameType type);

    /**
     * Returns the display name of the time zone at the given date.
     * 
     * <p>
     * <b>Note:</b> This method calls the subclass's {@link #getTimeZoneDisplayName(String, NameType)} first. When the
     * result is null, this method calls {@link #getMetaZoneID(String, long)} to get the meta zone ID mapped from the
     * time zone, then calls {@link #getMetaZoneDisplayName(String, NameType)}.
     * 
     * @param tzID
     *            The time zone ID.
     * @param type
     *            The display name type. See {@link TimeZoneNames.NameType}.
     * @param date
     *            The date
     * @return The display name for the time zone at the given date. When this object does not have a localized display
     *         name for the time zone with the specified type and date, null is returned.
     * @draft ICU 4.8
     */
    public final String getDisplayName(String tzID, NameType type, long date) {
        String name = getTimeZoneDisplayName(tzID, type);
        if (name == null) {
            String mzID = getMetaZoneID(tzID, date);
            name = getMetaZoneDisplayName(mzID, type);
        }
        return name;
    }

    /**
     * Returns the display name of the time zone.
     * 
     * @param tzID
     *            The time zone ID.
     * @param type
     *            The display name type. See {@link TimeZoneNames.NameType}.
     * @return The display name for the time zone. When this object does not have a localized display name for the given
     *         meta zone with the specified type, null is returned.
     * @internal
     */
    protected abstract String getTimeZoneDisplayName(String tzID, NameType type);

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
     *            The time zone ID
     * @return The exemplar location name for the given time zone, or null when a localized location name is not
     *         available and the fallback logic described above cannot extract location from the ID.
     */
    public String getExemplarLocationName(String tzID) {
        String canonicalID = ZoneMeta.getCanonicalCLDRID(tzID);
        if (canonicalID == null || LOC_EXCLUSION_PATTERN.matcher(canonicalID).matches()) {
            return null;
        }

        String location = null;
        int sep = canonicalID.lastIndexOf('/');
        if (sep > 0 && sep + 1 < canonicalID.length()) {
            location = canonicalID.substring(sep + 1).replace('_', ' ');
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
     * The default implementation of <code>TimeZoneNames</code> used by {@link TimeZoneNames#getInstance(ULocale)} when
     * the ICU4J tznamedata component is not available.
     */
    private static class DefaultTimeZoneNames extends TimeZoneNames {
        public static final DefaultTimeZoneNames INSTANCE = new DefaultTimeZoneNames();

        /*
         * (non-Javadoc)
         * 
         * @see com.ibm.icu.text.TimeZoneNames#getLocale()
         */
        @Override
        public ULocale getLocale() {
            return ULocale.ROOT;
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
         * @see com.ibm.icu.text.TimeZoneNames#getMetaZoneDisplayName (java.lang.String,
         * com.ibm.icu.text.TimeZoneNames.NameType)
         */
        @Override
        public String getMetaZoneDisplayName(String mzID, NameType type) {
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.ibm.icu.text.TimeZoneNames#getTimeZoneDisplayName (java.lang.String,
         * com.ibm.icu.text.TimeZoneNames.NameType, long)
         */
        @Override
        protected String getTimeZoneDisplayName(String tzID, NameType type) {
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
