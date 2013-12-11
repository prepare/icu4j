/*
 **************************************************************************
 * Copyright (C) 2008-2013, Google, International Business Machines
 * Corporation and others. All Rights Reserved.
 **************************************************************************
 */
package com.ibm.icu.text;

import java.io.ObjectStreamException;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;

import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.text.MeasureFormat.MeasureProxy;
import com.ibm.icu.util.Measure;
import com.ibm.icu.util.TimeUnit;
import com.ibm.icu.util.TimeUnitAmount;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.ULocale.Category;
import com.ibm.icu.util.UResourceBundle;


/**
 * Format or parse a TimeUnitAmount, using plural rules for the units where available.
 *
 * <P>
 * Code Sample: 
 * <pre>
 *   // create a time unit instance.
 *   // only SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, and YEAR are supported
 *   TimeUnit timeUnit = TimeUnit.SECOND;
 *   // create time unit amount instance - a combination of Number and time unit
 *   TimeUnitAmount source = new TimeUnitAmount(2, timeUnit);
 *   // create time unit format instance
 *   TimeUnitFormat format = new TimeUnitFormat();
 *   // set the locale of time unit format
 *   format.setLocale(new ULocale("en"));
 *   // format a time unit amount
 *   String formatted = format.format(source);
 *   System.out.println(formatted);
 *   try {
 *       // parse a string into time unit amount
 *       TimeUnitAmount result = (TimeUnitAmount) format.parseObject(formatted);
 *       // result should equal to source 
 *   } catch (ParseException e) {
 *   }
 * </pre>
 *
 * <P>
 * @see TimeUnitAmount
 * @see TimeUnitFormat
 * @author markdavis
 * @stable ICU 4.0
 */
public class TimeUnitFormat extends MeasureFormat {

    /**
     * Constant for full name style format. 
     * For example, the full name for "hour" in English is "hour" or "hours".
     * @stable ICU 4.2
     */
    public static final int FULL_NAME = 0;
    /**
     * Constant for abbreviated name style format. 
     * For example, the abbreviated name for "hour" in English is "hr" or "hrs".
     * @stable ICU 4.2
     */
    public static final int ABBREVIATED_NAME = 1;
    
    private static final int TOTAL_STYLES = 2;

    private static final long serialVersionUID = -3707773153184971529L;
  
    // Although these fields are not used anymore, they are left here to keep serialization
    // backward compatible.
    private NumberFormat format;
    private ULocale locale;
    private int style;
    
    // This is the field that is used.
    private transient MeasureFormat mf;

    /**
     * Create empty format using full name style, for example, "hours". 
     * Use setLocale and/or setFormat to modify.
     * @stable ICU 4.0
     */
    public TimeUnitFormat() {
        mf = MeasureFormat.getInstance(ULocale.getDefault(), FormatWidth.WIDE);
    }

    /**
     * Create TimeUnitFormat given a ULocale, and using full name style.
     * @param locale   locale of this time unit formatter.
     * @stable ICU 4.0
     */
    public TimeUnitFormat(ULocale locale) {
        mf = MeasureFormat.getInstance(locale,  FormatWidth.WIDE);
    }

    /**
     * Create TimeUnitFormat given a Locale, and using full name style.
     * @param locale   locale of this time unit formatter.
     * @stable ICU 4.0
     */
    public TimeUnitFormat(Locale locale) {
        this(ULocale.forLocale(locale));
    }

    /**
     * Create TimeUnitFormat given a ULocale and a formatting style.
     * @param locale   locale of this time unit formatter.
     * @param style    format style, either FULL_NAME or ABBREVIATED_NAME style.
     * @throws IllegalArgumentException if the style is not FULL_NAME or
     *                                  ABBREVIATED_NAME style.
     * @stable ICU 4.2
     */
    public TimeUnitFormat(ULocale locale, int style) {
        if (style < FULL_NAME || style >= TOTAL_STYLES) {
            throw new IllegalArgumentException("style should be either FULL_NAME or ABBREVIATED_NAME style");
        }
        mf = MeasureFormat.getInstance(
                locale, style == FULL_NAME ? FormatWidth.WIDE : FormatWidth.SHORT);
    }
    
    private TimeUnitFormat(ULocale locale, int style, NumberFormat numberFormat) {
        if (style < FULL_NAME || style >= TOTAL_STYLES) {
            throw new IllegalArgumentException("style should be either FULL_NAME or ABBREVIATED_NAME style");
        }
        mf = MeasureFormat.getInstance(
                locale, style == FULL_NAME ? FormatWidth.WIDE : FormatWidth.SHORT, numberFormat);
    }

    /**
     * Create TimeUnitFormat given a Locale and a formatting style.
     * @stable ICU 4.2
     */
    public TimeUnitFormat(Locale locale, int style) {
        this(ULocale.forLocale(locale),  style);
    }

    /**
     * Set the locale used for formatting or parsing.
     * @param locale   locale of this time unit formatter.
     * @return this, for chaining.
     * @stable ICU 4.0
     */
    public TimeUnitFormat setLocale(ULocale locale) {
        mf = mf.withLocale(locale);
        return this;
    }
    
    /**
     * Set the locale used for formatting or parsing.
     * @param locale   locale of this time unit formatter.
     * @return this, for chaining.
     * @stable ICU 4.0
     */
    public TimeUnitFormat setLocale(Locale locale) {
        return setLocale(ULocale.forLocale(locale));
    }
    
    /**
     * Set the format used for formatting or parsing. Passing null is equivalent to passing
     * {@link NumberFormat#getNumberInstance(ULocale)}.
     * @param format   the number formatter.
     * @return this, for chaining.
     * @stable ICU 4.0
     */
    public TimeUnitFormat setNumberFormat(NumberFormat format) {
        mf = mf.withNumberFormat(format);
        return this;
    }


    /**
     * Format a TimeUnitAmount.
     * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
     * @stable ICU 4.0
     */
    public StringBuffer format(Object obj, StringBuffer toAppendTo,
            FieldPosition pos) {
        return mf.format(obj, toAppendTo, pos);
    }
    
    /**
     * Parse a TimeUnitAmount.
     * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
     * @stable ICU 4.0
     */
    @Override
    public TimeUnitAmount parseObject(String source, ParsePosition pos) {
        int origIndex = pos.getIndex();
        Measure m = mf.parseObject(source, "duration", pos);
        if (m == null) {
            return null;
        }
        if (m.getUnit() instanceof TimeUnit) {
            return new TimeUnitAmount(m.getNumber(), (TimeUnit) m.getUnit());
        }
        pos.setIndex(origIndex);
        pos.setErrorIndex(origIndex);
        return null;
    }
    
    // boilerplate code to make TimeUnitFormat otherwise follow the contract of
    // MeasureFormat
    
    @Override
    public String formatMeasures(Measure... measures) {
        return mf.formatMeasures(measures);
    }
    
    @Override
    public <T extends Appendable> T formatMeasure(
            Measure measure, T appendable, FieldPosition fieldPosition) {
        return mf.formatMeasure(measure, appendable, fieldPosition);
    }
    
    @Override
    public <T extends Appendable> T formatMeasures(
            T appendable, FieldPosition fieldPosition, Measure... measures) {
        return mf.formatMeasures(appendable, fieldPosition, measures);
    }
    
    @Override
    public MeasureFormat.FormatWidth getWidth() {
        return mf.getWidth();
    }
    
    @Override
    public ULocale getLocale() {
        return mf.getLocale();
    }
    
    @Override
    public NumberFormat getNumberFormat() {
        return mf.getNumberFormat();
    }
    
    // End boilerplate.
    
    // equals / hashcode
    
    @Override
    public int hashCode() {
        return getLocale().hashCode() + 911247101;
    }
    
    @Override
    protected boolean equalsSameClass(MeasureFormat other) {
        return getLocale().equals(other.getLocale());
    }
    
    // End equals / hashcode
    
    // Serialization
    
    private Object writeReplace() throws ObjectStreamException {
        return mf.toTimeUnitProxy(new TimeUnitBundles());
    }
    
    // Preserve backward serialize backward compatibility.
    private Object readResolve() throws ObjectStreamException {
        return new TimeUnitFormat(locale, style, format);
    }
}
