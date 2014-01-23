/*
**********************************************************************
* Copyright (c) 2004-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
**********************************************************************
* Author: Alan Liu
* Created: April 20, 2004
* Since: ICU 3.0
**********************************************************************
*/
package com.ibm.icu.text;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.text.AttributedCharacterIterator;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Set;

import com.ibm.icu.impl.DontCareFieldPosition;
import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.SimpleCache;
import com.ibm.icu.util.Currency;
import com.ibm.icu.util.CurrencyAmount;
import com.ibm.icu.util.Measure;
import com.ibm.icu.util.MeasureUnit;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.ULocale.Category;
import com.ibm.icu.util.UResourceBundle;

// If you update the examples in the doc, don't forget to update MesaureUnitTest.TestExamplesInDocs too.
/**
 * A formatter for Measure objects.
 *
 * <p>To format a Measure object, first create a formatter
 * object using a MeasureFormat factory method.  Then use that
 * object's format or formatMeasures methods.
 * 
 * Here is sample code:
 * <pre>
 *      MeasureFormat fmtFr = MeasureFormat.getInstance(
 *              ULocale.FRENCH, FormatWidth.SHORT);
 *      Measure measure = new Measure(23, MeasureUnit.CELSIUS);
 *      
 *      // Output: 23 °C
 *      System.out.println(fmtFr.format(measure));
 *
 *      Measure measureF = new Measure(70, MeasureUnit.FAHRENHEIT);
 *
 *      // Output: 70 °F
 *      System.out.println(fmtFr.format(measureF));
 *     
 *      MeasureFormat fmtFrFull = MeasureFormat.getInstance(
 *              ULocale.FRENCH, FormatWidth.WIDE);
 *      // Output: 70 pieds et 5,3 pouces
 *      System.out.println(fmtFrFull.formatMeasures(
 *              new Measure(70, MeasureUnit.FOOT),
 *              new Measure(5.3, MeasureUnit.INCH)));
 *              
 *      // Output: 1 pied et 1 pouce
 *      System.out.println(fmtFrFull.formatMeasures(
 *              new Measure(1, MeasureUnit.FOOT),
 *              new Measure(1, MeasureUnit.INCH)));
 *  
 *      MeasureFormat fmtFrNarrow = MeasureFormat.getInstance(
                ULocale.FRENCH, FormatWidth.NARROW);
 *      // Output: 1′ 1″
 *      System.out.println(fmtFrNarrow.formatMeasures(
 *              new Measure(1, MeasureUnit.FOOT),
 *              new Measure(1, MeasureUnit.INCH)));
 *      
 *      
 *      MeasureFormat fmtEn = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.WIDE);
 *      
 *      // Output: 1 inch, 2 feet
 *      fmtEn.formatMeasures(
 *              new Measure(1, MeasureUnit.INCH),
 *              new Measure(2, MeasureUnit.FOOT));
 * </pre>
 * <p>
 * This class does not do conversions from one unit to another. It simply formats
 * whatever units it is given
 * <p>
 * This class is immutable and thread-safe so long as its deprecated subclass,
 * TimeUnitFormat, is never used. TimeUnitFormat is not thread-safe, and is
 * mutable. Although this class has existing subclasses, this class does not support new
 * sub-classes.   
 *
 * @see com.ibm.icu.text.UFormat
 * @author Alan Liu
 * @stable ICU 3.0
 */
public class MeasureFormat extends UFormat {
    

    // Generated by serialver from JDK 1.4.1_01
    static final long serialVersionUID = -7182021401701778240L;
    
    private final transient ImmutableNumberFormat numberFormat;
    
    private final transient FormatWidth formatWidth;
    
    // PluralRules is documented as being immutable which implies thread-safety.
    private final transient PluralRules rules;
    
    // Measure unit -> format width -> plural form -> pattern ("{0} meters")
    private final transient Map<MeasureUnit, EnumMap<FormatWidth, Map<String, PatternData>>> unitToStyleToCountToFormat;
    
    private final transient NumericFormatters numericFormatters;
    
    private final transient ImmutableNumberFormat currencyFormat;

    private static final SimpleCache<ULocale,Map<MeasureUnit, EnumMap<FormatWidth, Map<String, PatternData>>>> localeToUnitToStyleToCountToFormat
            = new SimpleCache<ULocale,Map<MeasureUnit, EnumMap<FormatWidth, Map<String, PatternData>>>>();
  
    private static final SimpleCache<ULocale, NumericFormatters> localeToNumericDurationFormatters
            = new SimpleCache<ULocale,NumericFormatters>();
    
    private static final Map<MeasureUnit, Integer> hmsTo012 =
            new HashMap<MeasureUnit, Integer>();
    
    static {
        hmsTo012.put(MeasureUnit.HOUR, 0);
        hmsTo012.put(MeasureUnit.MINUTE, 1);
        hmsTo012.put(MeasureUnit.SECOND, 2);
    }
    
    // For serialization: sub-class types.
    private static final int MEASURE_FORMAT = 0;
    private static final int TIME_UNIT_FORMAT = 1;
    private static final int CURRENCY_FORMAT = 2;
    
    /**
     * Formatting width enum.
     * 
     * @draft ICU 53
     * @provisional
     */
    // Be sure to update MeasureUnitTest.TestSerialFormatWidthEnum
    // when adding an enum value.
    public enum FormatWidth {
        
        /**
         * Spell out everything.
         * 
         * @draft ICU 53
         * @provisional
         */
        WIDE("units", ListFormatter.Style.DURATION, NumberFormat.PLURALCURRENCYSTYLE), 
        
        /**
         * Abbreviate when possible.
         * 
         * @draft ICU 53
         * @provisional
         */
        SHORT("unitsShort", ListFormatter.Style.DURATION_SHORT, NumberFormat.ISOCURRENCYSTYLE), 
        
        /**
         * Brief. Use only a symbol for the unit when possible.
         * 
         * @draft ICU 53
         * @provisional
         */
        NARROW("unitsNarrow", ListFormatter.Style.DURATION_NARROW, NumberFormat.CURRENCYSTYLE),
        
        /**
         * Identical to NARROW except when formatMeasures is called with
         * an hour and minute; minute and second; or hour, minute, and second Measures.
         * In these cases formatMeasures formats as 5:37:23 instead of 5h, 37m, 23s.
         * 
         * @draft ICU 53
         * @provisional
         */
        NUMERIC("unitsNarrow", ListFormatter.Style.DURATION_NARROW, NumberFormat.CURRENCYSTYLE);
        
        // Be sure to update the toFormatWidth and fromFormatWidth() functions
        // when adding an enum value.
    
        final String resourceKey;
        private final ListFormatter.Style listFormatterStyle;
        private final int currencyStyle;
    
        private FormatWidth(String resourceKey, ListFormatter.Style style, int currencyStyle) {
            this.resourceKey = resourceKey;
            this.listFormatterStyle = style;
            this.currencyStyle = currencyStyle;
        }
        
        ListFormatter.Style getListFormatterStyle() {
            return listFormatterStyle;
        }
        
        int getCurrencyStyle() {
            return currencyStyle;
        }
    }
    
    /**
     * Create a format from the locale, formatWidth, and format.
     *
     * @param locale the locale.
     * @param formatWidth hints how long formatted strings should be.
     * @return The new MeasureFormat object.
     * @draft ICU 53
     * @provisional
     */
    public static MeasureFormat getInstance(ULocale locale, FormatWidth formatWidth) {
        return getInstance(locale, formatWidth, NumberFormat.getInstance(locale));
    }
    
    /**
     * Create a format from the locale, formatWidth, and format.
     *
     * @param locale the locale.
     * @param formatWidth hints how long formatted strings should be.
     * @param format This is defensively copied.
     * @return The new MeasureFormat object.
     * @draft ICU 53
     * @provisional
     */
    public static MeasureFormat getInstance(ULocale locale, FormatWidth formatWidth, NumberFormat format) {
        PluralRules rules = PluralRules.forLocale(locale);
        Map<MeasureUnit, EnumMap<FormatWidth, Map<String, PatternData>>> unitToStyleToCountToFormat;
        NumericFormatters formatters = null;
        unitToStyleToCountToFormat = localeToUnitToStyleToCountToFormat.get(locale);
        if (unitToStyleToCountToFormat == null) {
            unitToStyleToCountToFormat = loadLocaleData(locale, rules);
            localeToUnitToStyleToCountToFormat.put(locale, unitToStyleToCountToFormat);
        }
        if (formatWidth == FormatWidth.NUMERIC) {
            formatters = localeToNumericDurationFormatters.get(locale);
            if (formatters == null) {
                formatters = loadNumericFormatters(locale);
                localeToNumericDurationFormatters.put(locale, formatters);
            }
        }
        
        return new MeasureFormat(
                locale,
                formatWidth,
                new ImmutableNumberFormat(format),
                rules,
                unitToStyleToCountToFormat,
                formatters,
                new ImmutableNumberFormat(
                        NumberFormat.getInstance(locale, formatWidth.getCurrencyStyle())));
    }
    
    /**
     * Able to format Collection&lt;? extends Measure&gt;, Measure[], and Measure
     * by delegating to formatMeasures.
     * If the pos argument identifies a NumberFormat field,
     * then its indices are set to the beginning and end of the first such field
     * encountered. MeasureFormat itself does not supply any fields.
     * 
     * Calling a
     * <code>formatMeasures</code> method is preferred over calling
     * this method as they give better performance.
     * 
     * @param obj must be a Collection<? extends Measure>, Measure[], or Measure object.
     * @param toAppendTo Formatted string appended here.
     * @param pos Identifies a field in the formatted text.
     * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
     * 
     * @draft ICU53
     * @provisional
     */
    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        if (obj instanceof Collection) {
            Collection<?> coll = (Collection<?>) obj;
            Measure[] measures = new Measure[coll.size()];
            int idx = 0;
            for (Object o : coll) {
                if (!(o instanceof Measure)) {
                    throw new IllegalArgumentException(obj.toString());
                }
                measures[idx++] = (Measure) o;
            }
            return formatMeasures(toAppendTo, pos, measures);
        } else if (obj instanceof Measure[]) {
            return formatMeasures(toAppendTo, pos, (Measure[]) obj);
        } else if (obj instanceof Measure){
            return this.formatMeasure((Measure) obj, toAppendTo, pos);
        } else {
            throw new IllegalArgumentException(obj.toString());            
        }
    }
    
    /**
     * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
     * @throws UnsupportedOperationException Not supported.
     * @draft ICU 53
     * @provisional
     */
    @Override
    public Measure parseObject(String source, ParsePosition pos) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Format a sequence of measures. Uses the ListFormatter unit lists.
     * So, for example, one could format “3 feet, 2 inches”.
     * Zero values are formatted (eg, “3 feet, 0 inches”). It is the caller’s
     * responsibility to have the appropriate values in appropriate order,
     * and using the appropriate Number values. Typically the units should be
     * in descending order, with all but the last Measure having integer values
     * (eg, not “3.2 feet, 2 inches”).
     * 
     * @param measures a sequence of one or more measures.
     * @return the formatted string.
     * @draft ICU 53
     * @provisional
     */
    public String formatMeasures(Measure... measures) {
        StringBuilder result = this.formatMeasures(
                new StringBuilder(), DontCareFieldPosition.INSTANCE, measures);
        return result.toString();
    }
    
    /**
     * Formats a sequence of measures and adds to appendable.
     * 
     * If the fieldPosition argument identifies a NumberFormat field,
     * then its indices are set to the beginning and end of the first such field
     * encountered. MeasureFormat itself does not supply any fields.
     * 
     * @param appendable the formatted string appended here.
     * @param fieldPosition Identifies a field in the formatted text.
     * @param measures the measures to format.
     * @return appendable.
     * @see MeasureFormat#formatMeasures(Measure...)
     * @draft ICU 53
     * @provisional
     */
    @SuppressWarnings("unchecked")
    public <T extends Appendable> T formatMeasures(
            T appendable, FieldPosition fieldPosition, Measure... measures) {
        // fast track for trivial cases
        if (measures.length == 0) {
            return appendable;
        }
        if (measures.length == 1) {
            return formatMeasure(measures[0], appendable, fieldPosition);
        }
        
        if (formatWidth == FormatWidth.NUMERIC) {
            // If we have just hour, minute, or second follow the numeric
            // track.
            Number[] hms = toHMS(measures);
            if (hms != null) {
                return formatNumeric(hms, appendable);
            }
        }
        
        ListFormatter listFormatter = ListFormatter.getInstance(
                getLocale(), formatWidth.getListFormatterStyle());
        String[] results = null;
        if (fieldPosition == DontCareFieldPosition.INSTANCE) {
            
            // Fast track: No field position.
            results = new String[measures.length];
            for (int i = 0; i < measures.length; i++) {
                results[i] = formatMeasure(measures[i]);
            }
        } else {
            
            // Slow track: Have to calculate field position.
            results = formatMeasuresSlowTrack(listFormatter, fieldPosition, measures);            
        }
                 
        // This is safe because appendable is of type T.
        try {
            return (T) appendable.append(listFormatter.format((Object[]) results));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }   
    
    /**
     * Two MeasureFormats, a and b, are equal if and only if they have the same formatWidth,
     * locale, and equal number formats.
     * @draft ICU 53
     * @provisional
     */
    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MeasureFormat)) {
            return false;
        }
        MeasureFormat rhs = (MeasureFormat) other;
        // A very slow but safe implementation.
        return getWidth() == rhs.getWidth() 
                && getLocale().equals(rhs.getLocale()) 
                && getNumberFormat().equals(rhs.getNumberFormat());
    }
    
    /**
     * @draft ICU 53
     * @provisional
     */
    @Override
    public final int hashCode() {
        // A very slow but safe implementation.
        return (getLocale().hashCode() * 31 
                + getNumberFormat().hashCode()) * 31 + getWidth().hashCode();
    }
    
    /**
     * Get the format width this instance is using.
     * @draft ICU 53
     * @provisional
     */
    public MeasureFormat.FormatWidth getWidth() {
        return formatWidth;
    }
    
    /**
     * Get the locale of this instance.
     * @draft ICU 53
     * @provisional
     */
    public ULocale getLocale() {
        return getLocale(ULocale.VALID_LOCALE);
    }
    
    /**
     * Get a copy of the number format.
     * @draft ICU 53
     * @provisional
     */
    public NumberFormat getNumberFormat() {
        return numberFormat.get();
    }

    /**
     * Return a formatter for CurrencyAmount objects in the given
     * locale.
     * @param locale desired locale
     * @return a formatter object
     * @stable ICU 3.0
     */
    public static MeasureFormat getCurrencyFormat(ULocale locale) {
        return new CurrencyFormat(locale);
    }

    /**
     * Return a formatter for CurrencyAmount objects in the default
     * <code>FORMAT</code> locale.
     * @return a formatter object
     * @see Category#FORMAT
     * @stable ICU 3.0
     */
    public static MeasureFormat getCurrencyFormat() {
        return getCurrencyFormat(ULocale.getDefault(Category.FORMAT));
    }
    
    // This method changes the NumberFormat object as well to match the new locale.
    MeasureFormat withLocale(ULocale locale) {
        return MeasureFormat.getInstance(locale, getWidth());
    }

    MeasureFormat withNumberFormat(NumberFormat format) {
        return new MeasureFormat(
                getLocale(),
                this.formatWidth,
                new ImmutableNumberFormat(format),
                this.rules,
                this.unitToStyleToCountToFormat,
                this.numericFormatters,
                this.currencyFormat);
    }
    
    private MeasureFormat(
            ULocale locale,
            FormatWidth formatWidth,
            ImmutableNumberFormat format,
            PluralRules rules,
            Map<MeasureUnit, EnumMap<FormatWidth, Map<String, PatternData>>> unitToStyleToCountToFormat,
            NumericFormatters formatters,
            ImmutableNumberFormat currencyFormat) {
        setLocale(locale, locale);
        this.formatWidth = formatWidth;
        this.numberFormat = format;
        this.rules = rules;
        this.unitToStyleToCountToFormat = unitToStyleToCountToFormat;
        this.numericFormatters = formatters;
        this.currencyFormat = currencyFormat;
    }
    
    MeasureFormat() {
        // Make compiler happy by setting final fields to null.
        this.formatWidth = null;
        this.numberFormat = null;
        this.rules = null;
        this.unitToStyleToCountToFormat = null;
        this.numericFormatters = null;
        this.currencyFormat = null;
    }
    
    static class NumericFormatters {
        private DateFormat hourMinute;
        private DateFormat minuteSecond;
        private DateFormat hourMinuteSecond;
        
        public NumericFormatters(
                DateFormat hourMinute,
                DateFormat minuteSecond,
                DateFormat hourMinuteSecond) {
            this.hourMinute = hourMinute;
            this.minuteSecond = minuteSecond;
            this.hourMinuteSecond = hourMinuteSecond;
        }
        
        public DateFormat getHourMinute() { return hourMinute; }
        public DateFormat getMinuteSecond() { return minuteSecond; }
        public DateFormat getHourMinuteSecond() { return hourMinuteSecond; }
    }
    
    private static NumericFormatters loadNumericFormatters(
            ULocale locale) {
        ICUResourceBundle r = (ICUResourceBundle)UResourceBundle.
                getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, locale);
        return new NumericFormatters(
                loadNumericDurationFormat(r, "hm"),
                loadNumericDurationFormat(r, "ms"),
                loadNumericDurationFormat(r, "hms"));
    }
    
    /**
     * Returns formatting data for all MeasureUnits except for currency ones.
     */
    private static Map<MeasureUnit, EnumMap<FormatWidth, Map<String, PatternData>>> loadLocaleData(
            ULocale locale, PluralRules rules) {
        Set<String> keywords = rules.getKeywords();
        Map<MeasureUnit, EnumMap<FormatWidth, Map<String, PatternData>>> unitToStyleToCountToFormat
                = new HashMap<MeasureUnit, EnumMap<FormatWidth, Map<String, PatternData>>>();
        ICUResourceBundle resource = (ICUResourceBundle)UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, locale);
        for (MeasureUnit unit : MeasureUnit.getAvailable()) {
            // Currency data cannot be found here. Skip.
            if (unit instanceof Currency) {
                continue;
            }
            EnumMap<FormatWidth, Map<String, PatternData>> styleToCountToFormat = unitToStyleToCountToFormat.get(unit);
            if (styleToCountToFormat == null) {
                unitToStyleToCountToFormat.put(unit, styleToCountToFormat = new EnumMap<FormatWidth, Map<String, PatternData>>(FormatWidth.class));
            }
            for (FormatWidth styleItem : FormatWidth.values()) {
                try {
                    ICUResourceBundle unitTypeRes = resource.getWithFallback(styleItem.resourceKey);
                    ICUResourceBundle unitsRes = unitTypeRes.getWithFallback(unit.getType());
                    ICUResourceBundle oneUnitRes = unitsRes.getWithFallback(unit.getSubtype());
                    Map<String, PatternData> countToFormat = styleToCountToFormat.get(styleItem);
                    if (countToFormat == null) {
                        styleToCountToFormat.put(styleItem, countToFormat = new HashMap<String, PatternData>());
                    }
                    // TODO(rocketman): Seems like we should be iterating over the bundles in
                    // oneUnitRes instead of all the plural key words since most languages have
                    // just 1 or 2 forms.
                    for (String keyword : keywords) {
                        UResourceBundle countBundle;
                        try {
                            countBundle = oneUnitRes.get(keyword);
                        } catch (MissingResourceException e) {
                            continue;
                        }
                        String pattern = countBundle.getString();
                        //                        System.out.println(styleItem.resourceKey + "/" 
                        //                                + unit.getType() + "/" 
                        //                                + unit.getCode() + "/" 
                        //                                + keyword + "=" + pattern);
                        PatternData format = new PatternData(pattern);
                        countToFormat.put(keyword, format);
                        //                        System.out.println(styleToCountToFormat);
                    }
                    // fill in 'other' for any missing values
                    PatternData other = countToFormat.get("other");
                    for (String keyword : keywords) {
                        if (!countToFormat.containsKey(keyword)) {
                            countToFormat.put(keyword, other);
                        }
                    }
                } catch (MissingResourceException e) {
                    continue;
                }
            }
            // now fill in the holes
            fillin:
                if (styleToCountToFormat.size() != FormatWidth.values().length) {
                    Map<String, PatternData> fallback = styleToCountToFormat.get(FormatWidth.SHORT);
                    if (fallback == null) {
                        fallback = styleToCountToFormat.get(FormatWidth.WIDE);
                    }
                    if (fallback == null) {
                        break fillin; // TODO use root
                    }
                    for (FormatWidth styleItem : FormatWidth.values()) {
                        Map<String, PatternData> countToFormat = styleToCountToFormat.get(styleItem);
                        if (countToFormat == null) {
                            styleToCountToFormat.put(styleItem, countToFormat = new HashMap<String, PatternData>());
                            for (Entry<String, PatternData> entry : fallback.entrySet()) {
                                countToFormat.put(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                }
        }
        return unitToStyleToCountToFormat;
    }
    
    private String formatMeasure(Measure measure) {
        return formatMeasure(
                measure, new StringBuilder(),
                DontCareFieldPosition.INSTANCE).toString();
    }
    
    private <T extends Appendable> T formatMeasure(
            Measure measure, T appendable, FieldPosition fieldPosition) {
        if (measure.getUnit() instanceof Currency) {
            try {
                appendable.append(
                        currencyFormat.format(
                                new CurrencyAmount(measure.getNumber(), (Currency) measure.getUnit()),
                                new StringBuffer(),
                                fieldPosition));
                return appendable;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Number n = measure.getNumber();
        MeasureUnit unit = measure.getUnit(); 
        UFieldPosition fpos = new UFieldPosition(fieldPosition.getFieldAttribute(), fieldPosition.getField());
        StringBuffer formattedNumber = numberFormat.format(n, new StringBuffer(), fpos);
        String keyword = rules.select(new PluralRules.FixedDecimal(n.doubleValue(), fpos.getCountVisibleFractionDigits(), fpos.getFractionDigits()));

        Map<FormatWidth, Map<String, PatternData>> styleToCountToFormat = unitToStyleToCountToFormat.get(unit);
        Map<String, PatternData> countToFormat = styleToCountToFormat.get(formatWidth);
        PatternData messagePatternData = countToFormat.get(keyword);
        try {
            appendable.append(messagePatternData.prefix);
            if (messagePatternData.suffix != null) { // there is a number (may not happen with, say, Arabic dual)
                // Fix field position
                if (fpos.getBeginIndex() != 0 || fpos.getEndIndex() != 0) {
                    fieldPosition.setBeginIndex(fpos.getBeginIndex() + messagePatternData.prefix.length());
                    fieldPosition.setEndIndex(fpos.getEndIndex() + messagePatternData.prefix.length());
                }
                appendable.append(formattedNumber);
                appendable.append(messagePatternData.suffix);
            }
            return appendable;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    // Wrapper around NumberFormat that provides immutability and thread-safety.
    private static final class ImmutableNumberFormat {
        private NumberFormat nf;
        
        public ImmutableNumberFormat(NumberFormat nf) {
            this.nf = (NumberFormat) nf.clone();
        }
        
        public synchronized NumberFormat get() {
            return (NumberFormat) nf.clone();
        }
        
        public synchronized StringBuffer format(
                Number n, StringBuffer buffer, FieldPosition pos) {
            return nf.format(n, buffer, pos);
        }
        
        public synchronized StringBuffer format(
                CurrencyAmount n, StringBuffer buffer, FieldPosition pos) {
            return nf.format(n, buffer, pos);
        }

        public synchronized String format(Number number) {
            return nf.format(number);
        }
    }
    
    static final class PatternData {
        final String prefix;
        final String suffix;
        public PatternData(String pattern) {
            int pos = pattern.indexOf("{0}");
            if (pos < 0) {
                prefix = pattern;
                suffix = null;
            } else {
                prefix = pattern.substring(0,pos);
                suffix = pattern.substring(pos+3);
            }
        }
        public String toString() {
            return prefix + "; " + suffix;
        }

    }
    
    Object toTimeUnitProxy() {
        return new MeasureProxy(getLocale(), formatWidth, numberFormat.get(), TIME_UNIT_FORMAT);
    }
    
    Object toCurrencyProxy() {
        return new MeasureProxy(getLocale(), formatWidth, numberFormat.get(), CURRENCY_FORMAT);
    }
    
    private String[] formatMeasuresSlowTrack(ListFormatter listFormatter, FieldPosition fieldPosition,
            Measure... measures) {
        String[] results = new String[measures.length];
        
        // Zero out our field position so that we can tell when we find our field.
        FieldPosition fpos = new FieldPosition(fieldPosition.getFieldAttribute(), fieldPosition.getField());
        
        int fieldPositionFoundIndex = -1;
        for (int i = 0; i < measures.length; ++i) {
            if (fieldPositionFoundIndex == -1) {
                results[i] = formatMeasure(measures[i], new StringBuilder(), fpos).toString();
                if (fpos.getBeginIndex() != 0 || fpos.getEndIndex() != 0) {
                    fieldPositionFoundIndex = i;    
                }
            } else {
                results[i] = formatMeasure(measures[i]);
            }
        }
        
        // Fix up FieldPosition indexes if our field is found.
        if (fieldPositionFoundIndex != -1) {
            String listPattern = listFormatter.getPatternForNumItems(measures.length);
            int positionInPattern = listPattern.indexOf("{" + fieldPositionFoundIndex + "}");
            if (positionInPattern == -1) {
                throw new IllegalStateException("Can't find position with ListFormatter.");
            }
            // Now we have to adjust our position in pattern
            // based on the previous values.
            for (int i = 0; i < fieldPositionFoundIndex; i++) {
                positionInPattern += (results[i].length() - ("{" + i + "}").length());
            }
            fieldPosition.setBeginIndex(fpos.getBeginIndex() + positionInPattern);
            fieldPosition.setEndIndex(fpos.getEndIndex() + positionInPattern);
        }
        return results;
    }
    
    // type is one of "hm", "ms" or "hms"
    private static DateFormat loadNumericDurationFormat(
            ICUResourceBundle r, String type) {
        r = r.getWithFallback(String.format("durationUnits/%s", type));
        // We replace 'h' with 'H' because 'h' does not make sense in the context of durations.
        DateFormat result = new SimpleDateFormat(r.getString().replace("h", "H"));
        result.setTimeZone(TimeZone.GMT_ZONE);
        return result;
    }
    
    private static Number[] toHMS(Measure[] measures) {
        Number[] result = new Number[3];
        int count = 0;
        for (Measure m : measures) {
            Integer idx = hmsTo012.get(m.getUnit());
            if (idx == null) {
                return null;
            }
            if (result[idx.intValue()] != null) {
                return null;
            }
            result[idx.intValue()] = m.getNumber();
            count++;
        }
        if (count < 2) {
            return null;
        }
        return result;
    }
    
    private <T extends Appendable> T formatNumeric(Number[] hms, T appendable) {
        int startIndex = -1;
        int endIndex = -1;
        for (int i = 0; i < hms.length; i++) {
            if (hms[i] != null) {
                endIndex = i;
                if (startIndex == -1) {
                    startIndex = endIndex;
                }
            } else {
                hms[i] = Integer.valueOf(0);
            }
        }
        long millis = (long) (((hms[0].doubleValue() * 60.0
                + hms[1].doubleValue()) * 60.0
                + hms[2].doubleValue()) * 1000.0);
        Date d = new Date(millis);
        if (startIndex == 0 && endIndex == 2) {
            return formatNumeric(
                    d, 
                    numericFormatters.getHourMinuteSecond(),
                    DateFormat.Field.SECOND,
                    hms[endIndex],
                    appendable);
        }
        if (startIndex == 1 && endIndex == 2) {
            return formatNumeric(
                    d, 
                    numericFormatters.getMinuteSecond(),
                    DateFormat.Field.SECOND,
                    hms[endIndex],
                    appendable);
        }
        if (startIndex == 0 && endIndex == 1) {
            return formatNumeric(
                    d, 
                    numericFormatters.getHourMinute(),
                    DateFormat.Field.MINUTE,
                    hms[endIndex],
                    appendable);
        }
        throw new IllegalStateException();
    }
    
    private <T extends Appendable> T formatNumeric(
            Date duration,
            DateFormat formatter,
            DateFormat.Field smallestField,
            Number smallestAmount,
            T appendable) {
        // Format the smallest amount ahead of time.
        String smallestAmountFormatted;
        smallestAmountFormatted = numberFormat.format(smallestAmount);
       
        // Format the duration using the provided DateFormat object. The smallest
        // field in this result will be missing the fractional part.
        AttributedCharacterIterator iterator = formatter.formatToCharacterIterator(duration);
       
        // iterate through formatted text copying to 'builder' one character at a time.
        // When we get to the smallest amount, skip over it and copy
        // 'smallestAmountFormatted' to the builder instead.
        for (iterator.first(); iterator.getIndex() < iterator.getEndIndex();) {
            try {
                if (iterator.getAttributes().containsKey(smallestField)) {
                    appendable.append(smallestAmountFormatted);
                    iterator.setIndex(iterator.getRunLimit(smallestField));
                } else {
                    appendable.append(iterator.current());
                    iterator.next();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return appendable;
    }
    
    private Object writeReplace() throws ObjectStreamException {
        return new MeasureProxy(
                getLocale(), formatWidth, numberFormat.get(), MEASURE_FORMAT);
    }
    
    static class MeasureProxy implements Externalizable {
        private static final long serialVersionUID = -6033308329886716770L;
        
        private ULocale locale;
        private FormatWidth formatWidth;
        private NumberFormat numberFormat;
        private int subClass;
        private HashMap<Object, Object> keyValues;

        public MeasureProxy(
                ULocale locale,
                FormatWidth width,
                NumberFormat numberFormat,
                int subClass) {
            this.locale = locale;
            this.formatWidth = width;
            this.numberFormat = numberFormat;
            this.subClass = subClass;
            this.keyValues = new HashMap<Object, Object>();
        }

        // Must have public constructor, to enable Externalizable
        public MeasureProxy() {
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeByte(0); // version
            out.writeUTF(locale.toLanguageTag());
            out.writeByte(formatWidth.ordinal());
            out.writeObject(numberFormat);
            out.writeByte(subClass);
            out.writeObject(keyValues);
        }

        @SuppressWarnings("unchecked")
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            in.readByte(); // version.
            locale = ULocale.forLanguageTag(in.readUTF());
            formatWidth = fromFormatWidthOrdinal(in.readByte() & 0xFF);
            numberFormat = (NumberFormat) in.readObject();
            if (numberFormat == null) {
                throw new InvalidObjectException("Missing number format.");
            }
            subClass = in.readByte() & 0xFF;
            
            // This cast is safe because the serialized form of hashtable can have
            // any object as the key and any object as the value.
            keyValues = (HashMap<Object, Object>) in.readObject();
            if (keyValues == null) {
                throw new InvalidObjectException("Missing optional values map.");
            }
        }
        
        private TimeUnitFormat createTimeUnitFormat() throws InvalidObjectException {
            int style;
            if (formatWidth == FormatWidth.WIDE) {
                style = TimeUnitFormat.FULL_NAME;
            } else if (formatWidth == FormatWidth.SHORT) {
                style = TimeUnitFormat.ABBREVIATED_NAME;
            } else {
                throw new InvalidObjectException("Bad width: " + formatWidth);
            }
            TimeUnitFormat result = new TimeUnitFormat(locale, style);
            result.setNumberFormat(numberFormat);
            return result;
        }

        private Object readResolve() throws ObjectStreamException {
            switch (subClass) {
            case MEASURE_FORMAT:
                return MeasureFormat.getInstance(locale, formatWidth, numberFormat);
            case TIME_UNIT_FORMAT:
                return createTimeUnitFormat();
            case CURRENCY_FORMAT:
                return new CurrencyFormat(locale);
            default:
                throw new InvalidObjectException("Unknown subclass: " + subClass);
            }
        }
    }
    
    private static FormatWidth fromFormatWidthOrdinal(int ordinal) {
        FormatWidth[] values = FormatWidth.values();
        if (ordinal < 0 || ordinal >= values.length) {
            return FormatWidth.WIDE;
        }
        return values[ordinal];
    }
}
