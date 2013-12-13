/*
**********************************************************************
* Copyright (c) 2004-2013, International Business Machines
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
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Set;

import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.SimpleCache;
import com.ibm.icu.util.Currency;
import com.ibm.icu.util.Measure;
import com.ibm.icu.util.MeasureUnit;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.ULocale.Category;
import com.ibm.icu.util.UResourceBundle;

/**
 * A formatter for Measure objects.
 *
 * <p>To format a Measure object, first create a formatter
 * object using a MeasureFormat factory method.  Then use that
 * object's format, formatMeasure, or formatMeasures methods.
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
 *      // Output: 70 pieds, 5,3 pouces
 *      System.out.println(fmtFrFull.formatMeasures(
 *              new Measure(70, MeasureUnit.FOOT),
 *              new Measure(5.3, MeasureUnit.INCH)));
 *              
 *      // Output: 1 pied, 1 pouce
 *      System.out.println(fmtFrFull.formatMeasures(
 *              new Measure(1, MeasureUnit.FOOT),
 *              new Measure(1, MeasureUnit.INCH)));
 *      }
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
 * This class is immutable so long as no mutable subclass, namely TimeUnitFormat,
 * is used. Although this class has existing subclasses, this class does not
 * support new sub-classes.   
 *
 * @see com.ibm.icu.text.UFormat
 * @author Alan Liu
 * @stable ICU 3.0
 */
public class MeasureFormat extends UFormat {
    

    // Generated by serialver from JDK 1.4.1_01
    static final long serialVersionUID = -7182021401701778240L;
    
    private final transient ULocale locale;
    
    // NumberFormat is known to lack thread-safety, all access to this
    // field must be synchronized.
    private final transient NumberFormat numberFormat;
    
    private final transient FormatWidth length;
    
    // PluralRules is documented as being immutable which implies thread-safety.
    private final transient PluralRules rules;
    
    // Measure unit -> format width -> plural form -> pattern ("{0} meters")
    private final transient Map<MeasureUnit, EnumMap<FormatWidth, Map<String, PatternData>>> unitToStyleToCountToFormat;

    static final SimpleCache<ULocale,Map<MeasureUnit, EnumMap<FormatWidth, Map<String, PatternData>>>> localeToUnitToStyleToCountToFormat
            = new SimpleCache<ULocale,Map<MeasureUnit, EnumMap<FormatWidth, Map<String, PatternData>>>>();
    
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
    public static enum FormatWidth {
        
        /**
         * Spell out everything.
         * 
         * @draft ICU 53
         * @provisional
         */
        WIDE("units"), 
        
        /**
         * Abbreviate when possible.
         * 
         * @draft ICU 53
         * @provisional
         */
        SHORT("unitsShort"), 
        
        /**
         * Brief. Use only a symbol for the unit when possible.
         * 
         * @draft ICU 53
         * @provisional
         */
        NARROW("unitsNarrow"),
        
        /**
         * Very brief. Omit unit entirely when possible.
         * For example with durations, show 5:37 instead of 5m, 37s.
         */
        NUMERIC("unitsNarrow");
    
        final String resourceKey;
    
        private FormatWidth(String resourceKey) {
            this.resourceKey = resourceKey;
        }
    }
    
    /**
     * Create a format from the locale, length, and format.
     *
     * @param locale the locale.
     * @param width hints how long formatted strings should be.
     * @return The new MeasureFormat object.
     * @draft ICU 53
     * @provisional
     */
    public static MeasureFormat getInstance(ULocale locale, FormatWidth width) {
        return getInstance(locale, width, NumberFormat.getInstance(locale));
    }
    
    /**
     * Create a format from the locale, length, and format.
     *
     * @param locale the locale.
     * @param width hints how long formatted strings should be.
     * @param format This is defensively copied.
     * @return The new MeasureFormat object.
     * @draft ICU 53
     * @provisional
     */
    public static MeasureFormat getInstance(ULocale locale, FormatWidth width, NumberFormat format) {
        PluralRules rules = PluralRules.forLocale(locale);
        Map<MeasureUnit, EnumMap<FormatWidth, Map<String, PatternData>>> unitToStyleToCountToFormat; 
        unitToStyleToCountToFormat = localeToUnitToStyleToCountToFormat.get(locale);
        if (unitToStyleToCountToFormat == null) {
            unitToStyleToCountToFormat = loadLocaleData(locale, rules);
            localeToUnitToStyleToCountToFormat.put(locale, unitToStyleToCountToFormat);
        }
        return new MeasureFormat(locale, width, format, rules, unitToStyleToCountToFormat);
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
                new StringBuilder(), new FieldPosition(0), measures);
        return result.toString();
    }
    
    /**
     * Able to format Collection&lt;? extends Measure&gt;, Measure[], and Measure
     * by delegating to formatMeasure or formatMeasures.
     * If the pos argument identifies a field used by the format
     * then its indices are set to the beginning and end of the first such field
     * encountered.
     * 
     * @param obj must be a Collection<? extends Measure>, Measure[], or Measure object.
     * @param toAppendTo Formatted string appended here.
     * @pram pos Identifies a field in the formatted text.
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
     * Formats a single measure and adds to appendable.
     * If the fieldPosition argument identifies a field used by the format,
     * then its indices are set to the beginning and end of the first such
     * field encountered.
     * 
     * @param measure the measure to format
     * @param appendable the formatted string appended here.
     * @param fieldPosition Identifies a field in the formatted text.
     * @return appendable.
     * @see MeasureFormat#formatMeasures(Measure...)
     * @draft ICU 53
     * @provisional
     */
    public <T extends Appendable> T formatMeasure(
            Measure measure, T appendable, FieldPosition fieldPosition) {
        Number n = measure.getNumber();
        MeasureUnit unit = measure.getUnit();        
        UFieldPosition fpos = new UFieldPosition(fieldPosition.getFieldAttribute(), fieldPosition.getField());
        StringBuffer formattedNumber = numberFormat.format(n, new StringBuffer(), fpos);
        String keyword = rules.select(new PluralRules.FixedDecimal(n.doubleValue(), fpos.getCountVisibleFractionDigits(), fpos.getFractionDigits()));

        Map<FormatWidth, Map<String, PatternData>> styleToCountToFormat = unitToStyleToCountToFormat.get(unit);
        Map<String, PatternData> countToFormat = styleToCountToFormat.get(length);
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
    
    /**
     * Formats a sequence of measures and adds to appendable.
     * If the fieldPosition argument identifies a field used by the format,
     * then its indices are set to the beginning and end of the first such
     * field encountered.
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
        
        // Zero out our field position so that we can tell when we find our field.
        FieldPosition fpos = new FieldPosition(fieldPosition.getFieldAttribute(), fieldPosition.getField());
        FieldPosition dummyPos = new FieldPosition(0);
        
        int fieldPositionFoundIndex = -1;
        StringBuilder[] results = new StringBuilder[measures.length];
        for (int i = 0; i < measures.length; ++i) {
            if (fieldPositionFoundIndex == -1) {
                results[i] = formatMeasure(measures[i], new StringBuilder(), fpos);
                if (fpos.getBeginIndex() != 0 || fpos.getEndIndex() != 0) {
                    fieldPositionFoundIndex = i;    
                }
            } else {
                results[i] = formatMeasure(measures[i], new StringBuilder(), dummyPos);
            }
        }
        ListFormatter listFormatter = ListFormatter.getInstance(locale, 
                length == FormatWidth.WIDE ? ListFormatter.Style.DURATION : ListFormatter.Style.DURATION_SHORT);
        
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
            
        // This is safe because appendable is of type T.
        try {
            return (T) appendable.append(listFormatter.format((Object[]) results));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * For two MeasureFormat objects, a and b, to be equal, <code>a.getClass().equals(b.getClass())</code>
     * <code>a.equalsSameClass(b)</code> must be true.
     * 
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
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        return equalsSameClass((MeasureFormat) other);
    }
    
    /**
     * @draft ICU 53
     * @provisional
     */
    @Override
    public int hashCode() {
        return (numberFormat.hashCode() * 31 + locale.hashCode()) * 31 + length.hashCode();
    }
    
    /**
     * Returns true if this object is equal to other. The class of this and the class of other
     * are guaranteed to be equal.
     * 
     * @deprecated For ICU internal use only.
     * @internal
     */
    protected boolean equalsSameClass(MeasureFormat other) {
        return objEquals(numberFormat,other.numberFormat)
                && objEquals(locale, other.locale) && objEquals(length, other.length);        
    }
    
    
    /**
     * Get the format width this instance is using.
     * @draft ICU 53
     * @provisional
     */
    public MeasureFormat.FormatWidth getWidth() {
        return length;
    }
    
    /**
     * Get the locale of this instance.
     * @draft ICU 53
     * @provisional
     */
    public ULocale getLocale() {
        return locale;
    }
    
    /**
     * Get a copy of the number format.
     * @draft ICU 53
     * @provisional
     */
    public NumberFormat getNumberFormat() {
        synchronized (numberFormat) {
            return (NumberFormat) numberFormat.clone();
        }
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
    
    MeasureFormat withLocale(ULocale locale) {
        return MeasureFormat.getInstance(locale, getWidth());
    }

    MeasureFormat withNumberFormat(NumberFormat format) {
        return new MeasureFormat(
                this.locale,
                this.length,
                (NumberFormat) format.clone(),
                this.rules,
                this.unitToStyleToCountToFormat);
    }
    
    private MeasureFormat(
            ULocale locale,
            FormatWidth width,
            NumberFormat format,
            PluralRules rules,
            Map<MeasureUnit, EnumMap<FormatWidth, Map<String, PatternData>>> unitToStyleToCountToFormat) {
        this.locale = locale;
        this.length = width;
        this.numberFormat = format;
        this.rules = rules;
        this.unitToStyleToCountToFormat = unitToStyleToCountToFormat;
    }
    
    /**
     * For backward compatibility only.
     * @internal
     * @deprecated
     */
    protected MeasureFormat() {
        // Make compiler happy by setting final fields to null.
        this.length = null;
        this.locale = null;
        this.numberFormat = null;
        this.rules = null;
        this.unitToStyleToCountToFormat = null;
        
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
    
    private static boolean objEquals(Object lhs, Object rhs) {
        return lhs == null ? rhs == null : lhs.equals(rhs);
        
    }
    
    Object toTimeUnitProxy() {
        return new MeasureProxy(locale, length, numberFormat, TIME_UNIT_FORMAT);
    }
    
    Object toCurrencyProxy() {
        return new MeasureProxy(locale, length, numberFormat, CURRENCY_FORMAT);
    }
    
    private Object writeReplace() throws ObjectStreamException {
        return new MeasureProxy(
                locale, length, numberFormat, MEASURE_FORMAT);
    }
    
    static class MeasureProxy implements Externalizable {
        private static final long serialVersionUID = -6033308329886716770L;
        
        private ULocale locale;
        private FormatWidth length;
        private NumberFormat numberFormat;
        private int subClass;
        private HashMap<Object, Object> keyValues;

        public MeasureProxy(
                ULocale locale,
                FormatWidth length,
                NumberFormat numberFormat,
                int subClass) {
            this.locale = locale;
            this.length = length;
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
            out.writeObject(length);
            out.writeObject(numberFormat);
            out.writeByte(subClass);
            out.writeObject(keyValues);
        }

        @SuppressWarnings("unchecked")
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            in.readByte(); // version.
            locale = ULocale.forLanguageTag(in.readUTF());
            length = (FormatWidth) in.readObject();
            if (length == null) {
                throw new InvalidObjectException("Missing width.");
            }
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
            if (length == FormatWidth.WIDE) {
                style = TimeUnitFormat.FULL_NAME;
            } else if (length == FormatWidth.SHORT) {
                style = TimeUnitFormat.ABBREVIATED_NAME;
            } else {
                throw new InvalidObjectException("Bad width: " + length);
            }
            TimeUnitFormat result = new TimeUnitFormat(locale, style);
            result.setNumberFormat(numberFormat);
            return result;
        }

        private Object readResolve() throws ObjectStreamException {
            switch (subClass) {
            case MEASURE_FORMAT:
                return MeasureFormat.getInstance(locale, length, numberFormat);
            case TIME_UNIT_FORMAT:
                return createTimeUnitFormat();
            case CURRENCY_FORMAT:
                return new CurrencyFormat(locale);
            default:
                throw new InvalidObjectException("Unknown subclass: " + subClass);
            }
        }
    }
}
