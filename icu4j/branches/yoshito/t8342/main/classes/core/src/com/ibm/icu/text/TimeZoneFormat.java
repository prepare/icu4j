/*
 *******************************************************************************
 * Copyright (C) 2011, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.io.Serializable;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;

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

    public enum GMTOffsetFormatType {
        POSITIVE_HM,
        POSITIVE_HMS,
        NEGATIVE_HM,
        NEGATIVE_HMS,
    }

    private TimeZoneNames _tznames;
    private String[] _gmtOffsetPatterns;
    private NumberingSystem _gmtOffsetNumberingSystem;
    private String _gmtZeroFormat;

    public static TimeZoneFormat getInstance(ULocale locale) {
        //TODO
        return null;
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

    public String getGMTOffsetFormatPattern(GMTOffsetFormatType type) {
        return _gmtOffsetPatterns[type.ordinal()];
    }

    public TimeZoneFormat setGMTOffsetFormatPattern(GMTOffsetFormatType type, String pattern) {
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
        // TODO
        return null;
    }

    public final String formatLocalizedGMT(int offset) {
        // TODO
        return null;
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

    protected static class ParseResult {
        protected String id;
        protected int length;
    }

    private static class IntParseResult {
        private int offset;
        private int length;
    }
}

