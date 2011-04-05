/*
 *******************************************************************************
 * Copyright (C) 2011, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.io.Serializable;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.MissingResourceException;

import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.SoftCache;
import com.ibm.icu.impl.TimeZoneFormatImpl;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.Freezable;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;

/**
 * @author yumaoka
 *
 */
public abstract class TimeZoneFormat extends UFormat implements Freezable<TimeZoneFormat>, Serializable {

    public enum Style {
        GENERIC_LOCATION,
        GENERIC_LONG,
        GENERIC_SHORT,
        SPECIFIC_LONG,
        SPECIFIC_SHORT,
        SPECIFIC_SHORT_ALL,
        RFC822,
        LOCALIZED_GMT,
    }

    public enum GMTOffsetPatternType {
        POSITIVE_HM ("+HH:mm"),
        POSITIVE_HMS ("+HH:mm:ss"),
        NEGATIVE_HM ("-HH:mm"),
        NEGATIVE_HMS ("-HH:mm:ss");

        String _defaultPattern;
        private GMTOffsetPatternType(String defaultPattern) {
            _defaultPattern = defaultPattern;
        }

        private String defaultPattern() {
            return _defaultPattern;
        }
    }

    private TimeZoneNames _tznames;
    private String _gmtPattern;
    private String[] _gmtOffsetPatterns;
    private NumberingSystem _gmtOffsetNumberingSystem;
    private String _gmtZeroFormat;

    private static final String DEFAULT_GMT_PATTERN = "GMT{0}";
    private static final String DEFAULT_GMT_ZERO = "GMT";
    private static final String DEFAULT_NUMBERING_SYSTEM_NAME = "latn";

    private static final int MILLIS_PER_HOUR = 60 * 60 * 1000;
    private static final int MILLIS_PER_MINUTE = 60 * 1000;
    private static final int MILLIS_PER_SECOND = 1000;

    private static TimeZoneFormatCache _tzfCache = new TimeZoneFormatCache();

    /**
     * Sole constructor for subclassing
     */
    protected TimeZoneFormat(ULocale locale) {
        _tznames = TimeZoneNames.getInstance(locale);

        _gmtPattern = DEFAULT_GMT_PATTERN;
        String hourFormats = null;
        _gmtZeroFormat = DEFAULT_GMT_ZERO;

        try {
            ICUResourceBundle bundle = (ICUResourceBundle) ICUResourceBundle.getBundleInstance(
                    ICUResourceBundle.ICU_ZONE_BASE_NAME, locale);
            try {
                _gmtPattern = bundle.getStringWithFallback("zoneStrings/gmtFormat");
            } catch (MissingResourceException e) {
                // fall through
            }
            try {
                hourFormats = bundle.getStringWithFallback("zoneStrings/hourFormat");
            } catch (MissingResourceException e) {
                // fall through
            }
            try {
                _gmtZeroFormat = bundle.getStringWithFallback("zoneStrings/gmtZeroFormat");
            } catch (MissingResourceException e) {
                // fall through
            }
        } catch (MissingResourceException e) {
            // fall through
        }

        _gmtOffsetPatterns = new String[GMTOffsetPatternType.values().length];
        if (hourFormats != null) {
            String[] hourPatterns = hourFormats.split(";", 2);
            _gmtOffsetPatterns[GMTOffsetPatternType.POSITIVE_HM.ordinal()] = hourPatterns[0];
            _gmtOffsetPatterns[GMTOffsetPatternType.POSITIVE_HMS.ordinal()] = expandOffsetPattern(hourPatterns[0]);
            _gmtOffsetPatterns[GMTOffsetPatternType.NEGATIVE_HM.ordinal()] = hourPatterns[1];
            _gmtOffsetPatterns[GMTOffsetPatternType.NEGATIVE_HMS.ordinal()] = expandOffsetPattern(hourPatterns[1]);
        } else {
            for (GMTOffsetPatternType patType : GMTOffsetPatternType.values()) {
                _gmtOffsetPatterns[patType.ordinal()] = patType.defaultPattern();
            }
        }

        _gmtOffsetNumberingSystem = NumberingSystem.getInstance(locale);
        if (_gmtOffsetNumberingSystem.isAlgorithmic()) {
            // we do not support algorithmic numbering system for GMT offset for now
            _gmtOffsetNumberingSystem = NumberingSystem.getInstanceByName(DEFAULT_NUMBERING_SYSTEM_NAME);
        }
    }

    public static TimeZoneFormat getInstance(ULocale locale) {
        if (locale == null) {
            throw new NullPointerException("locale is null");
        }
        return _tzfCache.getInstance(locale, locale);
    }

    public TimeZoneNames getTimeZoneNames() {
        return _tznames;
    }

    public TimeZoneFormat setTimeZoneNames(TimeZoneNames tznames) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
       _tznames = tznames;
       return this;
    }

    public String getGMTPattern() {
        return _gmtPattern;
    }

    public TimeZoneFormat setGMTPattern(String pattern) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
        _gmtPattern = pattern;
        return this;
    }

    public String getGMTOffsetPattern(GMTOffsetPatternType type) {
        return _gmtOffsetPatterns[type.ordinal()];
    }

    public TimeZoneFormat setGMTOffsetPattern(GMTOffsetPatternType type, String pattern) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
        _gmtOffsetPatterns[type.ordinal()] = pattern;
        return this;
    }

    public NumberingSystem getGMTOffsetNumberingSystem() {
        return _gmtOffsetNumberingSystem;
    }

    public TimeZoneFormat setGMTOffsetNumberingSystem(NumberingSystem numbers) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
        if (numbers.isAlgorithmic()) {
            throw new IllegalArgumentException("Algorithmic numbering system is not supported for Localized GMT format");
        }
        _gmtOffsetNumberingSystem = numbers;
        return this;
    }

    public String getGMTZeroFormat() {
        return _gmtZeroFormat;
    }

    public TimeZoneFormat setGMTZeroFormat(String gmtZeroFormat) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
        _gmtZeroFormat = gmtZeroFormat;
        return this;
    }

    protected abstract String handleFormatLongGeneric(TimeZone tz, long date);

    protected abstract String handleFormatLongSpecific(TimeZone tz, long date);

    protected abstract String handleFormatShortGeneric(TimeZone tz, long date);

    protected abstract String handleFormatShortSpecific(TimeZone tz, long date, boolean all);

    protected abstract String handleFormatGenericLocation(TimeZone tz);

    public final String formatRFC822(int offset) {
        StringBuilder buf = new StringBuilder();
        char sign = '+';
        if (offset < 0) {
            sign = '-';
            offset = -offset;
        }
        buf.append(sign);

        int offsetH = offset / MILLIS_PER_HOUR;
        offset = offset % MILLIS_PER_HOUR;
        int offsetM = offset / MILLIS_PER_MINUTE;
        offset = offset % MILLIS_PER_MINUTE;
        int offsetS = offset / MILLIS_PER_SECOND;

        int num = 0, denom = 0;
        if (offsetS == 0) {
            offset = offsetH * 100 + offsetM; // HHmm
            num = offset % 10000;
            denom = 1000;
        } else {
            offset = offsetH * 10000 + offsetM * 100 + offsetS; //HHmmss
            num = offset % 1000000;
            denom = 100000;
        }
        while (denom >= 1) {
            char digit = (char)((num / denom) + '0');
            buf.append(digit);
            num = num % denom;
            denom /= 10;
        }
        return buf.toString();
    }

    public final String formatLocalizedGMT(int offset) {
        // Note: This code is optimized for performance, but as a result, it makes assumptions
        // about the content and structure of the underlying CLDR data.
        // Specifically, it assumes that the H or HH in the pattern occurs before the mm,
        // and that there are no quoted literals in the pattern that contain H or m.
        // As of CLDR 2.0, all of the data conforms to these rules, so we should probably be OK.
        StringBuilder buf = new StringBuilder();
        boolean positive = true;
        if (offset < 0) {
            offset = -offset;
            positive = false;
        }

        int offsetH = offset / MILLIS_PER_HOUR;
        offset = offset % MILLIS_PER_HOUR;
        int offsetM = offset / MILLIS_PER_MINUTE;
        offset = offset % MILLIS_PER_MINUTE;
        int offsetS = offset / MILLIS_PER_SECOND;

        String offsetPattern;
        if (positive) {
            offsetPattern = (offsetS == 0) ?
                    _gmtOffsetPatterns[GMTOffsetPatternType.POSITIVE_HM.ordinal()] :
                    _gmtOffsetPatterns[GMTOffsetPatternType.POSITIVE_HMS.ordinal()];
        } else {
            offsetPattern = (offsetS == 0) ?
                    _gmtOffsetPatterns[GMTOffsetPatternType.NEGATIVE_HM.ordinal()] :
                    _gmtOffsetPatterns[GMTOffsetPatternType.NEGATIVE_HMS.ordinal()];
        }

        String digitString = _gmtOffsetNumberingSystem.getDescription();
        int subPosition = _gmtPattern.indexOf("{0}");
        for (int i = 0; i < _gmtPattern.length(); i++) {
            if (i == subPosition) {
                for (int j = 0; j < offsetPattern.length(); j++) {
                    switch(offsetPattern.charAt(j)) {
                    case 'H':
                        if (j + 1 < offsetPattern.length() && offsetPattern.charAt(j + 1) == 'H') {
                            j++;
                            if (offsetH < 10) {
                                buf.append(digitString.charAt(0));
                            }
                        }
                        if (offsetH >= 10) {
                            buf.append(digitString.charAt(offsetH / 10));
                        }
                        buf.append(digitString.charAt(offsetH % 10));
                        break;
                    case 'm':
                        if (j + 1 < offsetPattern.length() && offsetPattern.charAt(j + 1) == 'm') {
                            j++;
                            if (offsetM < 10) {
                                buf.append(digitString.charAt(0));
                            }
                        }
                        if (offsetM >= 10) {
                            buf.append(digitString.charAt(offsetM / 10));
                        }
                        buf.append(digitString.charAt(offsetM % 10));
                        break;
                    case 's':
                        if (j + 1 < offsetPattern.length() && offsetPattern.charAt(j + 1) == 's') {
                            j++;
                            if (offsetS < 10) {
                                buf.append(digitString.charAt(0));
                            }
                        }
                        if (offsetS >= 10) {
                            buf.append(digitString.charAt(offsetS / 10));
                        }
                        buf.append(digitString.charAt(offsetS % 10));
                        break;
                    default:
                        buf.append(offsetPattern.charAt(j));
                        break;
                    }
                }
                i += 3;
            } else {
                buf.append(_gmtPattern.charAt(i));
            }
        }
        return buf.toString();
    }

    public String format(Style style, TimeZone tz, long date) {
        String result = null;
        switch (style) {
        case GENERIC_LOCATION:
            result = handleFormatGenericLocation(tz);
            if (result == null) {
                result = formatLocalizedGMT(tz.getOffset(date));
            }
            break;
        case GENERIC_LONG:
            result = handleFormatLongGeneric(tz, date);
            if (result == null) {
                result = handleFormatGenericLocation(tz);
                if (result == null) {
                    result = formatLocalizedGMT(tz.getOffset(date));
                }
            }
            break;
        case GENERIC_SHORT:
            result = handleFormatShortGeneric(tz, date);
            if (result == null) {
                result = handleFormatGenericLocation(tz);
                if (result == null) {
                    result = formatLocalizedGMT(tz.getOffset(date));
                }
            }
            break;
        case SPECIFIC_LONG:
            result = handleFormatLongSpecific(tz, date);
            if (result == null) {
                result = formatLocalizedGMT(tz.getOffset(date));
            }
            break;
        case SPECIFIC_SHORT:
            result = handleFormatShortSpecific(tz, date, true);
            if (result == null) {
                result = formatLocalizedGMT(tz.getOffset(date));
            }
            break;
        case SPECIFIC_SHORT_ALL:
            result = handleFormatShortSpecific(tz, date, false);
            if (result == null) {
                result = formatLocalizedGMT(tz.getOffset(date));
            }
            break;
        case RFC822:
            result = formatRFC822(tz.getOffset(date));
            break;
        case LOCALIZED_GMT:
            result = formatLocalizedGMT(tz.getOffset(date));
            break;
        }
        return result;
    }

    public final StringBuffer format(Style style, TimeZone tz, long date, StringBuffer toAppendTo, FieldPosition pos) {
        String zoneStr = format(style, tz, date);
        toAppendTo.append(zoneStr);
        // TODO FieldPosition
        return toAppendTo;
    }

    protected abstract ParseResult handleParseLongGeneric(String text, int start);

    protected abstract ParseResult handleParseLongSpecific(String text, int start);

    protected abstract ParseResult handleParseShortGeneric(String text, int start);

    protected abstract ParseResult handleParseShortSpecific(String text, int start);

    protected abstract ParseResult handleParseGenericLocation(String text, int start);

    private IntParseResult parseRFC822(String text, int start) {
        return null;
    }

    private IntParseResult parseLocalizedGMT(String text, int start) {
        return null;
    }

    private IntParseResult parseGMTZeroFormat(String text, int start) {
        return null;
    }

    public final TimeZone parse(String text) throws ParseException {
        // TODO
        return null;
    }

    public final TimeZone parse(String text, ParsePosition pos) {
        // TODO
        return null;
    }

    /* (non-Javadoc)
     * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
     */
    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        if (obj instanceof TimeZone) {
            return format(Style.GENERIC_LOCATION, (TimeZone)obj, System.currentTimeMillis(), toAppendTo, pos);
        } else if (obj instanceof Calendar) {
            return format(Style.GENERIC_LOCATION, ((Calendar)obj).getTimeZone(), ((Calendar)obj).getTimeInMillis(), toAppendTo, pos);
        }
        throw new IllegalArgumentException("Cannot format given Object (" +
                obj.getClass().getName() + ") as a TimeZone");
    }

    /* (non-Javadoc)
     * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
     */
    @Override
    public Object parseObject(String source, ParsePosition pos) {
        return parse(source, pos);
    }

    private static String expandOffsetPattern(String offsetHM) {
        int idx_mm = offsetHM.indexOf("mm");
        if (idx_mm < 0) {
            // we cannot do anything with this...
            return offsetHM + ":ss";
        }
        String sep = ":";
        int idx_H = offsetHM.substring(0, idx_mm).lastIndexOf("H");
        if (idx_H >= 0) {
            sep = offsetHM.substring(idx_H + 1, idx_mm);
        }
        return offsetHM.substring(0, idx_mm + 2) + sep + "ss" + offsetHM.substring(idx_mm + 2);
    }

    protected static class ParseResult {
        protected String id;
        protected int length;
    }

    private static class IntParseResult {
        private int offset;
        private int length;
    }

    private static class TimeZoneFormatCache extends SoftCache<ULocale, TimeZoneFormat, ULocale> {

        /* (non-Javadoc)
         * @see com.ibm.icu.impl.CacheBase#createInstance(java.lang.Object, java.lang.Object)
         */
        @Override
        protected TimeZoneFormat createInstance(ULocale key, ULocale data) {
            TimeZoneFormat fmt = new TimeZoneFormatImpl(data);
            fmt.freeze();
            return fmt;
        }
    }
}

