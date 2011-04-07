/*
 *******************************************************************************
 * Copyright (C) 2011, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.MissingResourceException;

import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.SoftCache;
import com.ibm.icu.impl.TimeZoneFormatImpl;
import com.ibm.icu.impl.ZoneMeta;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.Freezable;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;

//TODO format documentation
/**
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
        POSITIVE_HM ("+HH:mm", "Hm"),
        /**
         * @draft ICU 4.8
         */
        POSITIVE_HMS ("+HH:mm:ss", "Hms"),
        /**
         * @draft ICU 4.8
         */
        NEGATIVE_HM ("-HH:mm", "Hm"),
        /**
         * @draft ICU 4.8
         */
        NEGATIVE_HMS ("-HH:mm:ss", "Hms");

        private String _defaultPattern;
        private String _required;
        private GMTOffsetPatternType(String defaultPattern, String required) {
            _defaultPattern = defaultPattern;
            _required = required;
        }

        private String defaultPattern() {
            return _defaultPattern;
        }

        private String required() {
            return _required;
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
    private String[] _gmtOffsetDigits;
    private String _gmtZeroFormat;

    private transient String[] _gmtPatternTokens;
    private transient Object[][] _gmtOffsetPatternItems;

    private static final String[] GMT_ZERO = {"gmt", "utc", "ut"}; // lower case for matching

    private static final String DEFAULT_GMT_PATTERN = "GMT{0}";
    private static final String DEFAULT_GMT_ZERO = "GMT";
    private static final String[] DEFAULT_GMT_DIGITS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
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

        String gmtPattern = null;
        String hourFormats = null;
        _gmtZeroFormat = DEFAULT_GMT_ZERO;

        try {
            ICUResourceBundle bundle = (ICUResourceBundle) ICUResourceBundle.getBundleInstance(
                    ICUResourceBundle.ICU_ZONE_BASE_NAME, locale);
            try {
                gmtPattern = bundle.getStringWithFallback("zoneStrings/gmtFormat");
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

        if (gmtPattern != null) {
            gmtPattern = DEFAULT_GMT_PATTERN;
        }
        initGMTPattern(gmtPattern);

        String[] gmtOffsetPatterns = new String[GMTOffsetPatternType.values().length];
        if (hourFormats != null) {
            String[] hourPatterns = hourFormats.split(";", 2);
            gmtOffsetPatterns[GMTOffsetPatternType.POSITIVE_HM.ordinal()] = hourPatterns[0];
            gmtOffsetPatterns[GMTOffsetPatternType.POSITIVE_HMS.ordinal()] = expandOffsetPattern(hourPatterns[0]);
            gmtOffsetPatterns[GMTOffsetPatternType.NEGATIVE_HM.ordinal()] = hourPatterns[1];
            gmtOffsetPatterns[GMTOffsetPatternType.NEGATIVE_HMS.ordinal()] = expandOffsetPattern(hourPatterns[1]);
        } else {
            for (GMTOffsetPatternType patType : GMTOffsetPatternType.values()) {
                gmtOffsetPatterns[patType.ordinal()] = patType.defaultPattern();
            }
        }
        initGMTOffsetPatterns(gmtOffsetPatterns);

        _gmtOffsetDigits = DEFAULT_GMT_DIGITS;
        NumberingSystem ns = NumberingSystem.getInstance(locale);
        if (!ns.isAlgorithmic()) {
            // we do not support algorithmic numbering system for GMT offset for now
            _gmtOffsetDigits = toCodePoints(ns.getDescription());
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
        initGMTPattern(pattern);
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
        if (pattern == null) {
            throw new NullPointerException("Null GMT offset pattern");
        }

        Object[] parsedItems = parseOffsetPattern(pattern, type.required());

        _gmtOffsetPatterns[type.ordinal()] = pattern;
        _gmtOffsetPatternItems[type.ordinal()] = parsedItems;

        return this;
    }

    /**
     * 
     * @return
     */
    public String getGMTOffsetDigits() {
        StringBuilder buf = new StringBuilder(_gmtOffsetDigits.length);
        for (String digit : _gmtOffsetDigits) {
            buf.append(digit);
        }
        return buf.toString();
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
        if (digits == null) {
            throw new NullPointerException("Null GMT offset digits");
        }
        String[] digitArray = toCodePoints(digits);
        if (digitArray.length != 10) {
            throw new IllegalArgumentException("Length of digits must be 10");
        }
        _gmtOffsetDigits = digitArray;
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
        if (gmtZeroFormat == null) {
            throw new NullPointerException("Null GMT zero format");
        }
        if (gmtZeroFormat.length() == 0) {
            throw new IllegalArgumentException("Empty GMT zero format");
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
        if (offset == 0) {
            return _gmtZeroFormat;
        }

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

        Object[] offsetPatternItems;
        if (positive) {
            offsetPatternItems = (offsetS == 0) ?
                    _gmtOffsetPatternItems[GMTOffsetPatternType.POSITIVE_HM.ordinal()] :
                    _gmtOffsetPatternItems[GMTOffsetPatternType.POSITIVE_HMS.ordinal()];
        } else {
            offsetPatternItems = (offsetS == 0) ?
                    _gmtOffsetPatternItems[GMTOffsetPatternType.NEGATIVE_HM.ordinal()] :
                    _gmtOffsetPatternItems[GMTOffsetPatternType.NEGATIVE_HMS.ordinal()];
        }

        // Building the GMT format string
        buf.append(_gmtPatternTokens[0]);

        for (Object item : offsetPatternItems) {
            if (item instanceof String) {
                // pattern literal
                buf.append((String)item);
            } else if (item instanceof PatternItem) {
                // Hour/minute/second field
                PatternItem pti = (PatternItem)item;
                switch (pti.getType()) {
                case 'H':
                    appendOffsetDigits(buf, offsetH, pti.getWidth());
                    break;
                case 'm':
                    appendOffsetDigits(buf, offsetM, pti.getWidth());
                    break;
                case 's':
                    appendOffsetDigits(buf, offsetS, pti.getWidth());
                    break;
                }
            }
        }
        buf.append(_gmtPatternTokens[1]);
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

        // Check if this is a GMT zero format
        if (text.regionMatches(true, start, _gmtZeroFormat, 0, _gmtZeroFormat.length())) {
            result._length = _gmtZeroFormat.length();
            result._offset = 0;
            result._type = TimeType.STANDARD;
        }
        // try "global" GMT zero formats ("GMT", "UTC" and "UT")
        for (String gmt : GMT_ZERO) {
            if (text.regionMatches(true, start, gmt, 0, gmt.length())) {
                result._length = gmt.length();
                result._offset = 0;
                result._type = TimeType.STANDARD;
            }
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

    private void initGMTPattern(String gmtPattern) {
        // This implementation not perfect, but sufficient practically.
        int idx = gmtPattern.indexOf("{0}");
        if (idx < 0) {
            throw new IllegalArgumentException("Bad localized GMT pattern: " + gmtPattern);
        }
        _gmtPattern = gmtPattern;
        _gmtPatternTokens = new String[2];
        _gmtPatternTokens[0] = unquote(gmtPattern.substring(0, idx));
        _gmtPatternTokens[1] = unquote(gmtPattern.substring(idx + 3));
    }

    private static String unquote(String s) {
        if (s.indexOf('\'') < 0) {
            return s;
        }
        boolean isPrevQuote = false;
        boolean inQuote = false;
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'') {
                if (isPrevQuote) {
                    buf.append(c);
                    isPrevQuote = false;
                } else {
                    isPrevQuote = true;
                }
                inQuote = !inQuote;
            } else {
                isPrevQuote = false;
                buf.append(c);
            }
        }
        return buf.toString();
    }

    private void initGMTOffsetPatterns(String[] gmtOffsetPatterns) {
        int size = GMTOffsetPatternType.values().length;
        if (gmtOffsetPatterns.length < size) {
            throw new IllegalArgumentException("Insufficient number of elements in gmtOffsetPatterns");
        }
        Object[][] gmtOffsetPatternItems = new Object[size][];
        for (GMTOffsetPatternType t : GMTOffsetPatternType.values()) {
            int idx = t.ordinal();
            // Note: parseOffsetPattern will validate the given pattern and throws
            // IllegalArgumentException when pattern is not valid
            Object[] parsedItems = parseOffsetPattern(gmtOffsetPatterns[idx], t.required());
            gmtOffsetPatternItems[idx] = parsedItems;
        }

        _gmtOffsetPatterns = new String[size];
        System.arraycopy(gmtOffsetPatterns, 0, _gmtOffsetPatterns, 0, size);
        _gmtOffsetPatternItems = gmtOffsetPatternItems;
    }

    private static class PatternItem {
        final char _type;
        final int _width;

        PatternItem(char type, int width) {
            _type = type;
            _width = width;
        }

        char getType() {
            return _type;
        }

        int getWidth() {
            return _width;
        }
    }

    private static Object[] parseOffsetPattern(String pattern, String letters) {
        boolean isPrevQuote = false;
        boolean inQuote = false;
        StringBuilder text = new StringBuilder();
        char itemType = 0;  // 0 for string literal, otherwise time pattern character
        int itemLength = 1;

        List<Object> items = new ArrayList<Object>();
        BitSet checkBits = new BitSet(letters.length());

        for (int i = 0; i < pattern.length(); i++) {
            char ch = pattern.charAt(i);
            if (ch == '\'') {
                if (isPrevQuote) {
                    text.append('\'');
                    isPrevQuote = false;
                } else {
                    isPrevQuote = true;
                    if (itemType != 0) {
                        items.add(new PatternItem(itemType, itemLength));
                        itemType = 0;
                    }
                }
                inQuote = !inQuote;
            } else {
                isPrevQuote = false;
                if (inQuote) {
                    text.append(ch);
                } else {
                    int patFieldIdx = letters.indexOf(ch);
                    if (patFieldIdx >= 0) {
                        // an offset time pattern character
                        if (ch == itemType) {
                            itemLength++;
                        } else {
                            if (itemType == 0) {
                                if (text.length() > 0) {
                                    items.add(text.toString());
                                    text.setLength(0);
                                }
                            } else {
                                items.add(new PatternItem(itemType, itemLength));
                            }
                            itemType = ch;
                            itemLength = 1;
                        }
                        checkBits.set(patFieldIdx);
                    } else {
                        // a string literal
                        if (itemType != 0) {
                            items.add(new PatternItem(itemType, itemLength));
                            itemType = 0;
                        }
                        text.append(ch);
                    }
                }
            }
        }
        // handle last item
        if (itemType == 0) {
            if (text.length() > 0) {
                items.add(text.toString());
                text.setLength(0);
            }
        } else {
            items.add(new PatternItem(itemType, itemLength));
        }

        if (checkBits.cardinality() != letters.length()) {
            throw new IllegalStateException("Bad localized GMT offset pattern: " + pattern);
        }

        return items.toArray(new Object[items.size()]);
    }

    /*
     * This code will be obsoleted once we add hour-minute-second pattern data in CLDR
     */
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

    private void appendOffsetDigits(StringBuilder buf, int n, int minDigits) {
        // This code assumes that the input number is 0 - 59
        assert(n >= 0 && n < 60);
        int numDigits = n >= 10 ? 2 : 1;
        for (int i = 0; i < minDigits - numDigits; i++) {
            buf.append(_gmtOffsetDigits[0]);
        }
        if (numDigits == 2) {
            buf.append(_gmtOffsetDigits[n / 10]);
        }
        buf.append(_gmtOffsetDigits[n % 10]);
    }

    private int parseOffsetDigits(String text, int offset, int minDigits, int maxDigits, int[] parsedLength) {
        int decVal = 0;
        int numDigits = 0;
        int idx = offset;
        while (idx < text.length() && numDigits < maxDigits) {
            int digit = -1;
            int cp = Character.codePointAt(text, idx);

            // First, try digits configured for this instance
            for (int i = 0; i < _gmtOffsetDigits.length; i++) {
                if (cp == _gmtOffsetDigits[i].codePointAt(0)) {
                    digit = i;
                    break;
                }
            }
            // If failed, check if this is a Unicode digit
            if (digit < 0) {
                digit = UCharacter.digit(cp);
                if (digit < 0) {
                    break;
                }
            }
            decVal = decVal * 10 + digit;
            numDigits++;
            idx += Character.charCount(cp);
        }

        if (numDigits < minDigits) {
            decVal = -1;
            numDigits = 0;
        }

        if (parsedLength != null && parsedLength.length > 0) {
            parsedLength[0] = idx - offset; 
        }

        return decVal;
    }

    private static String[] toCodePoints(String str) {
        int len = str.codePointCount(0, str.length());
        String[] codePoints = new String[len];

        for (int i = 0, offset = 0; i < len; i++) {
            int code = str.codePointAt(offset);
            int codeLen = Character.charCount(code);
            codePoints[i] = str.substring(offset, offset + codeLen);
            offset += codeLen;
        }
        return codePoints;
    }

    /*
     * Custom readObject for initializing the necessary transient fields
     */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();

        initGMTPattern(_gmtPattern);
        initGMTOffsetPatterns(_gmtOffsetPatterns);
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

