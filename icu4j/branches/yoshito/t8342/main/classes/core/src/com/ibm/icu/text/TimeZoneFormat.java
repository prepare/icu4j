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
import java.lang.reflect.Array;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
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
        GENERIC_LOCATION ("LOCALIZED_GMT"),
        /**
         * @draft ICU 4.8
         */
        GENERIC_LONG ("GENERIC_LOCATION", "SPECIFIC_LONG", "LOCALIZED_GMT"),
        /**
         * @draft ICU 4.8
         */
        GENERIC_SHORT ("GENERIC_LOCATION", "SPECIFIC_SHORT", "LOCALIZED_GMT"),
        /**
         * @draft ICU 4.8
         */
        SPECIFIC_LONG ("LOCALIZED_GMT"),
        /**
         * @draft ICU 4.8
         */
        SPECIFIC_SHORT ("LOCALIZED_GMT"),
        /**
         * @draft ICU 4.8
         */
        RFC822 (),
        /**
         * @draft ICU 4.8
         */
        LOCALIZED_GMT (),
        /**
         * @draft ICU 4.8
         */
        SPECIFIC_SHORT_COMMONLY_USED ("LOCALIZED_GMT");

        private String[] _fallbackStyles;

        private Style(String... fallbackStyles) {
            _fallbackStyles = fallbackStyles;
        }

        private String[] fallbackStyles() {
            return _fallbackStyles;
        }
    }

    /**
     * 
     * @draft ICU 4.8
     */
    public enum GMTOffsetPatternType {
        /**
         * @draft ICU 4.8
         */
        POSITIVE_HM ("+HH:mm", "Hm", true),
        /**
         * @draft ICU 4.8
         */
        POSITIVE_HMS ("+HH:mm:ss", "Hms", true),
        /**
         * @draft ICU 4.8
         */
        NEGATIVE_HM ("-HH:mm", "Hm", false),
        /**
         * @draft ICU 4.8
         */
        NEGATIVE_HMS ("-HH:mm:ss", "Hms", false);

        private String _defaultPattern;
        private String _required;
        private boolean _isPositive;

        private GMTOffsetPatternType(String defaultPattern, String required, boolean isPositive) {
            _defaultPattern = defaultPattern;
            _required = required;
            _isPositive = isPositive;
        }

        private String defaultPattern() {
            return _defaultPattern;
        }

        private String required() {
            return _required;
        }

        private boolean isPositive() {
            return _isPositive;
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
    private boolean _parseAllStyles;

    private transient String[] _gmtPatternTokens;
    private transient Object[][] _gmtOffsetPatternItems;

    private static final String[] ALT_GMT_STRINGS = {"GMT", "UTC", "UT"};

    private static final String DEFAULT_GMT_PATTERN = "GMT{0}";
    private static final String DEFAULT_GMT_ZERO = "GMT";
    private static final String[] DEFAULT_GMT_DIGITS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private static final String RFC822_DIGITS = "0123456789";

    // Order of GMT offset pattern parsing, *_HMS must be evaluated first
    // because *_HM is most likely a substring of *_HMS 
    private static final GMTOffsetPatternType[] PARSE_GMT_OFFSET_TYPES = {
        GMTOffsetPatternType.POSITIVE_HMS, GMTOffsetPatternType.NEGATIVE_HMS,
        GMTOffsetPatternType.POSITIVE_HM, GMTOffsetPatternType.NEGATIVE_HM,
    };

    private static final int MAX_OFFSET_HOUR = 23;
    private static final int MAX_OFFSET_MINUTE = 59;
    private static final int MAX_OFFSET_SECOND = 59;

    private static final int MILLIS_PER_HOUR = 60 * 60 * 1000;
    private static final int MILLIS_PER_MINUTE = 60 * 1000;
    private static final int MILLIS_PER_SECOND = 1000;

    private static TimeZoneFormatCache _tzfCache = new TimeZoneFormatCache();

    @SuppressWarnings("unchecked")
    private static final List<Style>[] PARSE_ORDERS = (List<Style>[])Array.newInstance(ArrayList.class, Style.values().length);
    static {
        for (Style style : Style.values()) {
            if (style == Style.SPECIFIC_SHORT_COMMONLY_USED) {
                // Same with SPECIFIC_SHORT, point to the list used for SPECIFIC_SHORT later
                continue;
            }
            String[] fallbackStyles = style.fallbackStyles();
            List<Style> parseOrder = new ArrayList<Style>();
            // Always start with RFC822, LOCALIZED_GMT
            parseOrder.add(Style.RFC822);
            parseOrder.add(Style.LOCALIZED_GMT);

            // The style itself
            if (!parseOrder.contains(style)) {
                parseOrder.add(style);
            }

            // Fallback styles
            for (String styleStr : fallbackStyles) {
                Style tmp = Style.valueOf(styleStr);
                if (!parseOrder.contains(tmp)) {
                    parseOrder.add(tmp);
                }
            }
            PARSE_ORDERS[style.ordinal()] = parseOrder;
        }
        PARSE_ORDERS[Style.SPECIFIC_SHORT_COMMONLY_USED.ordinal()] = PARSE_ORDERS[Style.SPECIFIC_SHORT.ordinal()];
    }

    // The order of parsing styles when preferred style is not provided.
    // We want to evaluate 1) RFC822 first, 2) LOCALIZED_GMT, 3) SPECIFIC_LONG/SHORT,
    // then all the rest (no preferred order)
    private static final List<Style> PARSE_ORDER_ALL_STYLES = Arrays.asList(
        Style.RFC822,
        Style.LOCALIZED_GMT,
        Style.SPECIFIC_LONG, Style.SPECIFIC_SHORT,
        Style.GENERIC_LOCATION, Style.GENERIC_LONG, Style.GENERIC_SHORT
    );

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

    public boolean isParseAllStyles() {
        return _parseAllStyles;
    }

    public TimeZoneFormat setParseAllStyles(boolean parseAllStyles) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
        _parseAllStyles = parseAllStyles;
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

        if (offsetH > MAX_OFFSET_HOUR || offsetM > MAX_OFFSET_MINUTE || offsetS > MAX_OFFSET_SECOND) {
            throw new IllegalArgumentException("Offset out of range :" + offset);
        }

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
            } else if (item instanceof GMTOffsetField) {
                // Hour/minute/second field
                GMTOffsetField field = (GMTOffsetField)item;
                switch (field.getType()) {
                case 'H':
                    appendOffsetDigits(buf, offsetH, field.getWidth());
                    break;
                case 'm':
                    appendOffsetDigits(buf, offsetM, field.getWidth());
                    break;
                case 's':
                    appendOffsetDigits(buf, offsetS, field.getWidth());
                    break;
                }
            }
        }
        buf.append(_gmtPatternTokens[1]);
        return buf.toString();
    }

    /**
     * @param style
     * @param tz
     * @param date
     * @return
     */
    public String format(Style style, TimeZone tz, long date) {
        return format(style, tz, date, null);
    }

    /**
     * @param style
     * @param tz
     * @param date
     * @param actualStyle
     * @return
     */
    public String format(Style style, TimeZone tz, long date, Style[] actualStyle) {
        Style styleUsed = style;
        String result = formatStyle(style, tz, date);

        if (result == null) {
            String[] fallbackStyles = style.fallbackStyles();
            for (String fbStyle : fallbackStyles) {
                styleUsed = Style.valueOf(fbStyle);
                result = formatStyle(styleUsed, tz, date);
                if (result != null) {
                    break;
                }
            }
        }

        if (actualStyle != null && actualStyle.length > 0) {
            actualStyle[0] = styleUsed;
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
        Style[] actual = new Style[1];
        String zoneStr = format(style, tz, date, actual);
        toAppendTo.append(zoneStr);

        boolean setPos = false;

        if (pos.getFieldAttribute() == DateFormat.Field.TIME_ZONE) {
            setPos = true;
        } else {
            switch (actual[0]) {
            case SPECIFIC_LONG:
            case SPECIFIC_SHORT:
                setPos = (pos.getField() == DateFormat.TIMEZONE_FIELD);
                break;
            case GENERIC_LONG:
            case GENERIC_SHORT:
                setPos = (pos.getField() == DateFormat.TIMEZONE_GENERIC_FIELD);
                break;
            case RFC822:
            case LOCALIZED_GMT:
                setPos = (pos.getField() == DateFormat.TIMEZONE_RFC_FIELD);
                break;
            case SPECIFIC_SHORT_COMMONLY_USED:
            case GENERIC_LOCATION:
                setPos = (pos.getField() == DateFormat.TIMEZONE_SPECIAL_FIELD);
                break;
            }
        }

        if (setPos) {
            pos.setBeginIndex(0);
            pos.setEndIndex(zoneStr.length());
        }

        return toAppendTo;
    }


    /**
     * 
     * @param text
     * @param style
     * @param pos
     * @param type
     * @return
     */
    public TimeZone parse(String text, Style style, ParsePosition pos, TimeType[] type) {
        return parse(text, PARSE_ORDERS[style.ordinal()], _parseAllStyles, pos, type);
    }

    /**
     * 
     * @param text
     * @param pos
     * @return
     */
    public final TimeZone parse(String text, ParsePosition pos) {
        return parse(text, null, true, pos, null);
    }

    /**
     * @param text
     * @return
     * @throws ParseException
     */
    public final TimeZone parse(String text) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        TimeZone tz = parse(text, null, true, pos, null);
        if (pos.getIndex() == 0) {
            throw new ParseException("Unparseable time zone: \"" + text + "\"" , 0);
        }
        assert(tz != null);
        return tz;
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
                obj.getClass().getName() + ") as a time zone");
    }

    /* (non-Javadoc)
     * @see java.text.Format#formatToCharacterIterator(java.lang.Object)
     */
    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        StringBuffer toAppendTo = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        toAppendTo = format(obj, toAppendTo, pos);

        // supporting only DateFormat.Field.TIME_ZONE
        AttributedString as = new AttributedString(toAppendTo.toString());
        as.addAttribute(DateFormat.Field.TIME_ZONE, DateFormat.Field.TIME_ZONE);

        return as.getIterator();
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

    private static class GMTOffsetField {
        final char _type;
        final int _width;

        GMTOffsetField(char type, int width) {
            _type = type;
            _width = width;
        }

        char getType() {
            return _type;
        }

        int getWidth() {
            return _width;
        }

        static boolean isValid(char type, int width) {
            switch (type) {
            case 'H':
                return (width == 1 || width == 2);
            case 'm':
            case 's':
                return (width == 2);
            }
            return false;
        }
    }

    private static Object[] parseOffsetPattern(String pattern, String letters) {
        boolean isPrevQuote = false;
        boolean inQuote = false;
        StringBuilder text = new StringBuilder();
        char itemType = 0;  // 0 for string literal, otherwise time pattern character
        int itemLength = 1;
        boolean invalidPattern = false;

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
                        if (GMTOffsetField.isValid(itemType, itemLength)) {
                            items.add(new GMTOffsetField(itemType, itemLength));
                        } else {
                            invalidPattern = true;
                            break;
                        }
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
                                if (GMTOffsetField.isValid(itemType, itemLength)) {
                                    items.add(new GMTOffsetField(itemType, itemLength));
                                } else {
                                    invalidPattern = true;
                                    break;
                                }
                            }
                            itemType = ch;
                            itemLength = 1;
                        }
                        checkBits.set(patFieldIdx);
                    } else {
                        // a string literal
                        if (itemType != 0) {
                            if (GMTOffsetField.isValid(itemType, itemLength)) {
                                items.add(new GMTOffsetField(itemType, itemLength));
                            } else {
                                invalidPattern = true;
                                break;
                            }
                            itemType = 0;
                        }
                        text.append(ch);
                    }
                }
            }
        }
        // handle last item
        if (!invalidPattern) {
            if (itemType == 0) {
                if (text.length() > 0) {
                    items.add(text.toString());
                    text.setLength(0);
                }
            } else {
                if (GMTOffsetField.isValid(itemType, itemLength)) {
                    items.add(new GMTOffsetField(itemType, itemLength));
                } else {
                    invalidPattern = true;
                }
            }
        }

        if (invalidPattern || checkBits.cardinality() != letters.length()) {
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

    private String formatStyle(Style style, TimeZone tz, long date) {
        String result = null;
        switch (style) {
        case GENERIC_LOCATION:
            result = handleFormatGenericLocation(tz.getCanonicalID());
            break;
        case GENERIC_LONG:
            result = handleFormatLongGeneric(tz, date);
            break;
        case GENERIC_SHORT:
            result = handleFormatShortGeneric(tz, date);
            break;
        case SPECIFIC_LONG:
            result = handleFormatLongSpecific(tz, date);
            break;
        case SPECIFIC_SHORT:
            result = handleFormatShortSpecific(tz, date);
            break;
        case RFC822:
            result = formatRFC822(tz.getOffset(date));
            break;
        case LOCALIZED_GMT:
            result = formatLocalizedGMT(tz.getOffset(date));
            break;
        case SPECIFIC_SHORT_COMMONLY_USED:
            result = handleFormatShortSpecificCommonlyUsed(tz, date);
            break;
        }
        return result;
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

        if (hour > MAX_OFFSET_HOUR || min > MAX_OFFSET_MINUTE || sec > MAX_OFFSET_SECOND) {
            // Invalid value range
            return;
        }

        result._length = 1 + numDigits;
        result._offset = ((((hour * 60) + min) * 60) + sec) * 1000 * sign;
        result._type = TimeType.UNKNOWN;
    }

    private void parseLocalizedGMT(String text, int start, ParseResult result) {
        result.reset();

        int idx = start;
        boolean parsed = false;
        int[] offset = new int[1];

        do {
            // Prefix part
            int len = _gmtPatternTokens[0].length();
            if (len > 0 && !text.regionMatches(true, idx, _gmtPatternTokens[0], 0, len)) {
                // prefix match failed
                break;
            }
            idx += len;

            // Offset part
            int offsetLen = parseGMTOffset(text, idx, false, offset);
            idx += offsetLen;

            // Suffix part
            len = _gmtPatternTokens[1].length();
            if (len > 0 && !text.regionMatches(true, idx, _gmtPatternTokens[1], 0, len)) {
                // no suffix match
                break;
            }
            idx += len;
            parsed = true;

        } while (false);

        if (parsed) {
            result._length = idx - start;
            result._offset = offset[0];
            result._type = TimeType.UNKNOWN;
        } else {
            // Check if this is a GMT zero format
            if (text.regionMatches(true, start, _gmtZeroFormat, 0, _gmtZeroFormat.length())) {
                result._length = _gmtZeroFormat.length();
                result._offset = 0;
                result._type = TimeType.UNKNOWN;
            }
        }
    }

    private int parseGMTOffset(String text, int start, boolean minimumHourWidth, int[] offset) {
        int parsedLen = 0;
        int[] tmpParsedLen = new int[1];
        offset[0] = 0;
        boolean sawVarHourAndAbuttingField = false;

        for (GMTOffsetPatternType gmtPatType : PARSE_GMT_OFFSET_TYPES) {
            int offsetH = 0, offsetM = 0, offsetS = 0;
            int idx = start;
            Object[] items = _gmtOffsetPatternItems[gmtPatType.ordinal()];
            boolean failed = false;
            for (int i = 0; i < items.length; i++) {
                if (items[i] instanceof String) {
                    String patStr = (String)items[i];
                    int len = patStr.length();
                    if (!text.regionMatches(true, idx, patStr, 0, len)) {
                        failed = true;
                        break;
                    }
                    idx += len;
                } else {
                    assert(items[i] instanceof GMTOffsetField);
                    GMTOffsetField field = (GMTOffsetField)items[i];
                    char fieldType = field.getType();
                    if (fieldType == 'H') {
                        int minDigits = 1;
                        int maxDigits = minimumHourWidth ? 1 : 2;
                        if (!minimumHourWidth && !sawVarHourAndAbuttingField) {
                            if (i + 1 < items.length && (items[i] instanceof GMTOffsetField)) {
                                sawVarHourAndAbuttingField = true;
                            }
                        }
                        offsetH = parseOffsetDigits(text, idx, minDigits, maxDigits, 0, MAX_OFFSET_HOUR, tmpParsedLen);
                    } else if (fieldType == 'm') {
                        offsetM = parseOffsetDigits(text, idx, 2, 2, 0, MAX_OFFSET_MINUTE, tmpParsedLen);
                    } else if (fieldType == 's') {
                        offsetS = parseOffsetDigits(text, idx, 2, 2, 0, MAX_OFFSET_SECOND, tmpParsedLen);
                    }

                    if (tmpParsedLen[0] == 0) {
                        failed = true;
                        break;
                    }
                    idx += tmpParsedLen[0];
                }
            }
            if (!failed) {
                int sign = gmtPatType.isPositive() ? 1 : -1;
                offset[0] = ((((offsetH * 60) + offsetM) * 60) + offsetS) * 1000 * sign;
                parsedLen = idx - start;
                break;
            }
        }

        if (parsedLen == 0 && sawVarHourAndAbuttingField && !minimumHourWidth) {
            // When hour field is variable width and another non-literal pattern
            // field follows, the parse loop above might eat up the digit from
            // the abutting field. For example, with pattern "-Hmm" and input "-100",
            // the hour is parsed as -10 and fails to parse minute field.
            //
            // If this is the case, try parsing the text one more time with the arg
            // minimumHourWidth = true
            //
            // Note: This fallback is not applicable when quitAtHourField is true, because
            // the option is designed for supporting the case like "GMT+5". In this case,
            // we should get better result for parsing hour digits as much as possible.

            return parseGMTOffset(text, start, true, offset);
        }

        return parsedLen;
    }

    private void parseDefaultGMT(String text, int start, ParseResult result) {
        result.reset();

        int idx = start;
        int len;

        // check global default GMT alternatives
        int gmtLen = 0;
        for (String gmt : ALT_GMT_STRINGS) {
            len = gmt.length();
            if (text.regionMatches(true, idx, gmt, 0, len)) {
                gmtLen = len;
                break;
            }
        }
        if (gmtLen == 0) {
            return;
        }
        idx += gmtLen;

        // at least, parsed up to GMT string
        result._length = idx - start;
        result._offset = 0;
        result._type = TimeType.UNKNOWN;

        // offset needs a sign char and a digit at minimum  
        if (idx + 1 >= text.length()) {
            return;
        }

        // parse sign
        int sign = 1;
        char c = text.charAt(idx);
        if (c == '+') {
            sign = 1;
        } else if (c == '-') {
            sign = -1;
        } else {
            // no sign part
            return;
        }
        idx++;

        // offset
        int[] parsedLen = new int[1];
        int num = parseOffsetDigits(text, idx, 1, 6, 0, 
                235959 /* MAX_OFFSET_HOUR*10000 + MAX_OFFSET_MINUTE*100 + MAX_OFFSET_SECOND */, parsedLen);
        if (parsedLen[0] == 0) {
            return;
        }

        // TODO
        int offsetH = 0, offsetM = 0, offsetS = 0;

    }


    private int parseOffsetDigits(String text, int offset, int minDigits, int maxDigits,
            int minVal, int maxVal, int[] parsedLength) {

        parsedLength[0] = 0;

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
            int tmpVal = decVal * 10 + digit;
            if (tmpVal > maxVal) {
                break;
            }
            decVal = tmpVal;
            numDigits++;
            idx += Character.charCount(cp);
        }

        // Note: maxVal is checked in the while loop
        if (numDigits < minDigits || decVal < minVal) {
            decVal = -1;
            numDigits = 0;
        } else {
            parsedLength[0] = idx - offset;
        }


        return decVal;
    }

    private TimeZone parse(String text, List<Style> styles, boolean parseAllStyles, ParsePosition pos, TimeType[] type) {
        int idx = pos.getIndex();
        final int maxMatchLen = text.length() - pos.getIndex();   // possible maximum match length

        ParseResult match = new ParseResult();;
        ParseResult bestMatch = null;
        int plen;

        if (styles != null) {
            // Find the longest match from the standard style list
            for (Style style : styles) {
                assert(style != Style.SPECIFIC_SHORT_COMMONLY_USED);

                parseStyle(style, text, idx, match);
                plen = match.getParseLength();
                if (plen > 0) {
                    if (bestMatch == null || bestMatch.getParseLength() < plen) {
                        bestMatch = match.clone();
                    }
                    if (plen == maxMatchLen) {
                        break;
                    }
                }
            }
        }

        if (bestMatch == null && parseAllStyles) {
            // Find the longest match from other styles
            for (Style style : PARSE_ORDER_ALL_STYLES) {
                assert(style != Style.SPECIFIC_SHORT_COMMONLY_USED);
                if (styles != null && styles.contains(style)) {
                    // already tried
                    continue;
                }
                parseStyle(style, text, idx, match);
                plen = match.getParseLength();
                if (plen > 0) {
                    if (bestMatch == null || bestMatch.getParseLength() < plen) {
                        bestMatch = match.clone();
                    }
                    if (plen == maxMatchLen) {
                        break;
                    }
                }
            }
        }

        TimeZone tz = null;
        if (bestMatch != null) {
            if (bestMatch.getID() != null) {
                tz = TimeZone.getTimeZone(bestMatch.getID());
            } else {
                assert(bestMatch.getOffset() != null);
                tz = ZoneMeta.getCustomTimeZone(bestMatch.getOffset());
            }
            pos.setIndex(idx + bestMatch.getParseLength());

            if (type != null && type.length > 0) {
                type[0] = bestMatch.getType();
            }
        } else {
            pos.setErrorIndex(idx);
        }

        return tz;
    }

    private void parseStyle(Style style, String text, int start, ParseResult result) {
        switch (style) {
        case GENERIC_LOCATION:
            handleParseGenericLocation(text, start, result);
            break;
        case GENERIC_LONG:
            handleParseLongGeneric(text, start, result);
            break;
        case GENERIC_SHORT:
            handleParseShortGeneric(text, start, result);
            break;
        case SPECIFIC_LONG:
            handleParseLongSpecific(text, start, result);
            break;
        case SPECIFIC_SHORT:
        case SPECIFIC_SHORT_COMMONLY_USED:
            handleParseShortSpecific(text, start, result);
            break;
        case RFC822:
            parseRFC822(text, start, result);
            break;
        case LOCALIZED_GMT:
            parseLocalizedGMT(text, start, result);
            break;
        }
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
    protected static class ParseResult implements Cloneable {
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

        public ParseResult clone() {
            try {
                return (ParseResult)super.clone();
            } catch (CloneNotSupportedException e) {
                // will never happen
                throw new RuntimeException(e);
            }
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

