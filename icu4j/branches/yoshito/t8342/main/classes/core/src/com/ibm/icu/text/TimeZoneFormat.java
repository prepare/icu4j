/*
 *******************************************************************************
 * Copyright (C) 2011, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.io.Serializable;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.MissingResourceException;

import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.SoftCache;
import com.ibm.icu.impl.TimeZoneFormatImpl;
import com.ibm.icu.impl.ZoneMeta;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.Freezable;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;

/**
 * @author yumaoka
 *
 */
public abstract class TimeZoneFormat extends UFormat implements Freezable<TimeZoneFormat>, Serializable {

    /**
     * @draft ICU 4.8
     */
    public enum Style {
        /**
         * @draft ICU 4.8
         */
        GENERIC_LOCATION,
        /**
         * @draft ICU 4.8
         */
        GENERIC_LONG,
        /**
         * @draft ICU 4.8
         */
        GENERIC_SHORT,
        /**
         * @draft ICU 4.8
         */
        SPECIFIC_LONG,
        /**
         * @draft ICU 4.8
         */
        SPECIFIC_SHORT,
        /**
         * @draft ICU 4.8
         */
        RFC822,
        /**
         * @draft ICU 4.8
         */
        LOCALIZED_GMT,
        /**
         * @draft ICU 4.8
         */
        SPECIFIC_SHORT_COMMONLY_USED,
    }

    /**
     * 
     * @draft ICU 4.8
     */
    public enum GMTOffsetPatternType {
        /**
         * @draft ICU 4.8
         */
        POSITIVE_HM ("+HH:mm"),
        /**
         * @draft ICU 4.8
         */
        POSITIVE_HMS ("+HH:mm:ss"),
        /**
         * @draft ICU 4.8
         */
        NEGATIVE_HM ("-HH:mm"),
        /**
         * @draft ICU 4.8
         */
        NEGATIVE_HMS ("-HH:mm:ss");

        String _defaultPattern;
        private GMTOffsetPatternType(String defaultPattern) {
            _defaultPattern = defaultPattern;
        }

        private String defaultPattern() {
            return _defaultPattern;
        }
    }

    /**
     * 
     * @draft ICU 4.8
     */
    public enum TimeType {
        /**
         * @draft ICU 4.8
         */
        UNKNOWN,
        /**
         * @draft ICU 4.8
         */
        STANDARD,
        /**
         * @draft ICU 4.8
         */
        DAYLIGHT,
    }

    private TimeZoneNames _tznames;
    private String _gmtPattern;
    private String[] _gmtOffsetPatterns;
    private String _gmtOffsetDigits;
    private String _gmtZeroFormat;

    private static final String DEFAULT_GMT_PATTERN = "GMT{0}";
    private static final String DEFAULT_GMT_ZERO = "GMT";
    private static final String DEFAULT_GMT_DIGITS = "0123456789";
    private static final String RFC822_DIGITS = "0123456789";

    private static final int MILLIS_PER_HOUR = 60 * 60 * 1000;
    private static final int MILLIS_PER_MINUTE = 60 * 1000;
    private static final int MILLIS_PER_SECOND = 1000;

    private static TimeZoneFormatCache _tzfCache = new TimeZoneFormatCache();

    /**
     * 
     * @param locale
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

        _gmtOffsetDigits = DEFAULT_GMT_DIGITS;
        NumberingSystem ns = NumberingSystem.getInstance(locale);
        if (!ns.isAlgorithmic()) {
            // we do not support algorithmic numbering system for GMT offset for now
            _gmtOffsetDigits = ns.getDescription();
        }
    }

    /**
     * 
     * @param locale
     * @return
     */
    public static TimeZoneFormat getInstance(ULocale locale) {
        if (locale == null) {
            throw new NullPointerException("locale is null");
        }
        return _tzfCache.getInstance(locale, locale);
    }

    /**
     * 
     * @return
     */
    public TimeZoneNames getTimeZoneNames() {
        return _tznames;
    }

    /**
     * 
     * @param tznames
     * @return
     */
    public TimeZoneFormat setTimeZoneNames(TimeZoneNames tznames) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
       _tznames = tznames;
       return this;
    }

    /**
     * 
     * @return
     */
    public String getGMTPattern() {
        return _gmtPattern;
    }

    /**
     * 
     * @param pattern
     * @return
     */
    public TimeZoneFormat setGMTPattern(String pattern) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
        _gmtPattern = pattern;
        return this;
    }

    /**
     * 
     * @param type
     * @return
     */
    public String getGMTOffsetPattern(GMTOffsetPatternType type) {
        return _gmtOffsetPatterns[type.ordinal()];
    }

    /**
     * 
     * @param type
     * @param pattern
     * @return
     */
    public TimeZoneFormat setGMTOffsetPattern(GMTOffsetPatternType type, String pattern) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
        _gmtOffsetPatterns[type.ordinal()] = pattern;
        return this;
    }

    /**
     * 
     * @return
     */
    public String getGMTOffsetDigits() {
        return _gmtOffsetDigits;
    }

    /**
     * 
     * @param digits
     * @return
     */
    public TimeZoneFormat setGMTOffsetDigits(String digits) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
        if (digits.length() != 10) {
            throw new IllegalArgumentException("Length of digits must be 10");
        }
        _gmtOffsetDigits = digits;
        return this;
    }

    /**
     * 
     * @return
     */
    public String getGMTZeroFormat() {
        return _gmtZeroFormat;
    }

    /**
     * 
     * @param gmtZeroFormat
     * @return
     */
    public TimeZoneFormat setGMTZeroFormat(String gmtZeroFormat) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
        _gmtZeroFormat = gmtZeroFormat;
        return this;
    }


    /**
     * 
     * @param offset
     * @return
     */
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

        assert(offsetH >= 0 && offsetH < 100);
        assert(offsetM >= 0 && offsetM < 60);
        assert(offsetS >= 0 && offsetS < 60);

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

    /**
     * 
     * @param offset
     * @return
     */
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

        assert(offsetH >= 0 && offsetH < 100);
        assert(offsetM >= 0 && offsetM < 60);
        assert(offsetS >= 0 && offsetS < 60);

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

        int subPosition = _gmtPattern.indexOf("{0}");
        for (int i = 0; i < _gmtPattern.length(); i++) {
            if (i == subPosition) {
                for (int j = 0; j < offsetPattern.length(); j++) {
                    switch(offsetPattern.charAt(j)) {
                    case 'H':
                        if (j + 1 < offsetPattern.length() && offsetPattern.charAt(j + 1) == 'H') {
                            j++;
                            if (offsetH < 10) {
                                buf.append(_gmtOffsetDigits.charAt(0));
                            }
                        }
                        if (offsetH >= 10) {
                            buf.append(_gmtOffsetDigits.charAt(offsetH / 10));
                        }
                        buf.append(_gmtOffsetDigits.charAt(offsetH % 10));
                        break;
                    case 'm':
                        if (j + 1 < offsetPattern.length() && offsetPattern.charAt(j + 1) == 'm') {
                            j++;
                            if (offsetM < 10) {
                                buf.append(_gmtOffsetDigits.charAt(0));
                            }
                        }
                        if (offsetM >= 10) {
                            buf.append(_gmtOffsetDigits.charAt(offsetM / 10));
                        }
                        buf.append(_gmtOffsetDigits.charAt(offsetM % 10));
                        break;
                    case 's':
                        if (j + 1 < offsetPattern.length() && offsetPattern.charAt(j + 1) == 's') {
                            j++;
                            if (offsetS < 10) {
                                buf.append(_gmtOffsetDigits.charAt(0));
                            }
                        }
                        if (offsetS >= 10) {
                            buf.append(_gmtOffsetDigits.charAt(offsetS / 10));
                        }
                        buf.append(_gmtOffsetDigits.charAt(offsetS % 10));
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

    /**
     * 
     * @param style
     * @param tz
     * @param date
     * @return
     */
    public String format(Style style, TimeZone tz, long date) {
        String result = null;
        switch (style) {
        case GENERIC_LOCATION:
            result = handleFormatGenericLocation(tz.getID());
            if (result == null) {
                result = formatLocalizedGMT(tz.getOffset(date));
            }
            break;
        case GENERIC_LONG:
            result = handleFormatLongGeneric(tz, date);
            if (result == null) {
                result = handleFormatGenericLocation(tz.getID());
                if (result == null) {
                    result = formatLocalizedGMT(tz.getOffset(date));
                }
            }
            break;
        case GENERIC_SHORT:
            result = handleFormatShortGeneric(tz, date);
            if (result == null) {
                result = handleFormatGenericLocation(tz.getID());
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
            result = handleFormatShortSpecific(tz, date);
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
        case SPECIFIC_SHORT_COMMONLY_USED:
            result = handleFormatShortSpecificCommonlyUsed(tz, date);
            if (result == null) {
                result = formatLocalizedGMT(tz.getOffset(date));
            }
            break;
        }
        return result;
    }

    /**
     * 
     * @param style
     * @param tz
     * @param date
     * @param toAppendTo
     * @param pos
     * @return
     */
    public final StringBuffer format(Style style, TimeZone tz, long date, StringBuffer toAppendTo, FieldPosition pos) {
        String zoneStr = format(style, tz, date);
        toAppendTo.append(zoneStr);
        // TODO FieldPosition
        return toAppendTo;
    }


    private void parseRFC822(String text, int start, ParseResult result) {
        result.reset();
        if (start + 2 >= text.length()) {
            // minimum 2 characters
            return;
        }

        int len = 0;

        int sign;
        char signChar = text.charAt(start);
        if (signChar == '+') {
            sign = 1;
        } else if (signChar == '-') {
            sign = -1;
        } else {
            // Not an RFC822 offset string
            return;
        }
        len++;

        // Parse digits
        // Possible format (excluding sign char) are:
        // HHmmss
        // HmmSS
        // HHmm
        // Hmm
        // HH
        // H
        int idx = start + 1;
        int numDigits = 0;
        int[] digits = new int[6];
        while (numDigits < digits.length && idx < text.length()) {
            int digit = RFC822_DIGITS.indexOf(text.charAt(idx));
            if (digit < 0) {
                break;
            }
            digits[numDigits] = digit;
            numDigits++;
            idx++;
        }

        if (numDigits == 0) {
            // Not an RFC822 offset string
            return;
        }

        int hour = 0, min = 0, sec = 0;
        switch (numDigits) {
        case 1: //H
            hour = digits[0];
            break;
        case 2: //HH
            hour = digits[0] * 10 + digits[1];
            break;
        case 3: //Hmm
            hour = digits[0];
            min = digits[1] * 10 + digits[2];
            break;
        case 4: //HHmm
            hour = digits[0] * 10 + digits[1];
            min = digits[2] * 10 + digits[3];
            break;
        case 5: //Hmmss
            hour = digits[0];
            min = digits[1] * 10 + digits[2];
            sec = digits[3] * 10 + digits[4];
            break;
        case 6: //HHmmss
            hour = digits[0] * 10 + digits[1];
            min = digits[2] * 10 + digits[3];
            sec = digits[4] * 10 + digits[5];
            break;
        }

        if (hour >= 24 || min >= 60 || sec >= 60) {
            // Invalid value range
            return;
        }

        result._length = 1 + numDigits;
        result._offset = ((((hour * 60) + min) * 60) + sec) * 1000 * sign;
        result._type = TimeType.UNKNOWN;
    }

    private void parseLocalizedGMT(String text, int start, ParseResult result) {
        //TODO
    }

    private void parseGMTZeroFormat(String text, int start, ParseResult result) {
        result.reset();
        if (text.regionMatches(true, start, _gmtZeroFormat, 0, _gmtZeroFormat.length())) {
            result._length = _gmtZeroFormat.length();
            result._offset = 0;
            result._type = TimeType.STANDARD;
        }
    }

    /**
     * 
     * @param text
     * @param style
     * @param pos
     * @param type
     * @return
     */
    public final TimeZone parse(String text, Style style, ParsePosition pos, TimeType[] type) {
        ParseResult pres = new ParseResult();
        int idx = pos.getIndex();
        switch (style) {
        case GENERIC_LOCATION:
            handleParseGenericLocation(text, idx, pres);
            if (pres.getParseLength() == 0) {
                parseLocalizedGMT(text, pos.getIndex(), pres);
            }
            break;
        case GENERIC_LONG:
            handleParseLongGeneric(text, idx, pres);
            if (pres.getParseLength() == 0) {
                handleParseGenericLocation(text, idx, pres);
                if (pres.getParseLength() == 0) {
                    parseLocalizedGMT(text, idx, pres);
                }
            }
            break;
        case GENERIC_SHORT:
            handleParseShortGeneric(text, idx, pres);
            if (pres.getParseLength() == 0) {
                handleParseGenericLocation(text, idx, pres);
                if (pres.getParseLength() == 0) {
                    parseLocalizedGMT(text, idx, pres);
                }
            }
            break;
        case SPECIFIC_LONG:
            handleParseLongSpecific(text, idx, pres);
            if (pres.getParseLength() == 0) {
                parseLocalizedGMT(text, idx, pres);
            }
            break;
        case SPECIFIC_SHORT:
        case SPECIFIC_SHORT_COMMONLY_USED:
            handleParseShortSpecific(text, idx, pres);
            if (pres.getParseLength() == 0) {
                parseLocalizedGMT(text, idx, pres);
            }
            break;
        case RFC822:
            parseRFC822(text, idx, pres);
            break;
        case LOCALIZED_GMT:
            parseLocalizedGMT(text, idx, pres);
            break;
        }

        TimeZone tz = null;
        if (pres.getParseLength() > 0) {
            if (pres.getID() != null) {
                tz = TimeZone.getTimeZone(pres.getID());
            } else {
                assert(pres.getOffset() != null);
                tz = ZoneMeta.getCustomTimeZone(pres.getOffset());
            }
            pos.setIndex(idx + pres.getParseLength());

            if (type != null && type.length > 0) {
                type[0] = pres.getType();
            }
        }

        return tz;
    }

    /**
     * 
     * @param text
     * @param pos
     * @return
     */
    public final TimeZone parse(String text, ParsePosition pos) {
        //TODO
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

    /**
     * @draft ICU 4.8
     */
    protected static class ParseResult {
        private String _id;
        private Integer _offset;
        private int _length;
        private TimeType _type = TimeType.UNKNOWN;

        public ParseResult() {
        }

        public void reset() {
            _id = null;
            _offset = null;
            _length = 0;
            _type = TimeType.UNKNOWN;
        }

        public String getID() {
            return _id;
        }

        public ParseResult setID(String id) {
            _id = id;
            return this;
        }

        public Integer getOffset() {
            return _offset;
        }

        public ParseResult setOffset(Integer offset) {
            _offset = offset;
            return this;
        }

        public int getParseLength() {
            return _length;
        }

        public ParseResult setParseLength(int length) {
            _length = length;
            return this;
        }

        public TimeType getType() {
            return _type;
        }

        public ParseResult setType(TimeType type) {
            _type = type;
            return this;
        }
    }

    /**
     * 
     * @param tz
     * @param date
     * @return
     */
    protected abstract String handleFormatLongGeneric(TimeZone tz, long date);

    /**
     * 
     * @param tz
     * @param date
     * @return
     */
    protected abstract String handleFormatLongSpecific(TimeZone tz, long date);

    /**
     * 
     * @param tz
     * @param date
     * @return
     */
    protected abstract String handleFormatShortGeneric(TimeZone tz, long date);

    /**
     * 
     * @param tz
     * @param date
     * @param all
     * @return
     */
    protected abstract String handleFormatShortSpecific(TimeZone tz, long date);

    /**
     * 
     * @param tz
     * @param date
     * @param all
     * @return
     */
    protected abstract String handleFormatShortSpecificCommonlyUsed(TimeZone tz, long date);

    /**
     * 
     * @param tzID
     * @return
     */
    protected abstract String handleFormatGenericLocation(String tzID);

    /**
     * 
     * @param text
     * @param start
     * @param result
     */
    protected abstract void handleParseLongGeneric(String text, int start, ParseResult result);

    /**
     * 
     * @param text
     * @param start
     * @param result
     */
    protected abstract void handleParseLongSpecific(String text, int start, ParseResult result);

    /**
     * 
     * @param text
     * @param start
     * @param result
     */
    protected abstract void handleParseShortGeneric(String text, int start, ParseResult result);

    /**
     * 
     * @param text
     * @param start
     * @param result
     */
    protected abstract void handleParseShortSpecific(String text, int start, ParseResult result);

    /**
     * 
     * @param text
     * @param start
     * @param result
     */
    protected abstract void handleParseGenericLocation(String text, int start, ParseResult result);

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

