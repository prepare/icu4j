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

import java.io.ObjectStreamException;
import java.text.FieldPosition;
import java.text.ParsePosition;

import com.ibm.icu.util.CurrencyAmount;
import com.ibm.icu.util.Measure;
import com.ibm.icu.util.ULocale;

/**
 * Temporary internal concrete subclass of MeasureFormat implementing
 * parsing and formatting of CurrencyAmount objects.  This class is
 * likely to be redesigned and rewritten in the near future.
 *
 * <p>This class currently delegates to DecimalFormat for parsing and
 * formatting.
 *
 * @see com.ibm.icu.text.UFormat
 * @see com.ibm.icu.text.DecimalFormat
 * @author Alan Liu
 */
class CurrencyFormat extends MeasureFormat {
    // Generated by serialver from JDK 1.4.1_01
    static final long serialVersionUID = -931679363692504634L;
    
    private NumberFormat fmt;
    private transient final MeasureFormat mf;

    public CurrencyFormat(ULocale locale) {
        mf = MeasureFormat.getInstance(locale, FormatWidth.WIDE);
        fmt = NumberFormat.getCurrencyInstance(locale.toLocale());
    }

    /**
     * Override Format.format().
     * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
     */
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        if (!(obj instanceof CurrencyAmount)) {
            throw new IllegalArgumentException("Invalid type: " + obj.getClass().getName());
        }
        CurrencyAmount currency = (CurrencyAmount) obj;
            
        // Since we extend MeasureFormat, we have to maintain thread-safety.
        synchronized (fmt) {
            fmt.setCurrency(currency.getCurrency());
            return fmt.format(currency.getNumber(), toAppendTo, pos);
        }
    }

    /**
     * Override Format.parseObject().
     * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
     */
    @Override
    public CurrencyAmount parseObject(String source, ParsePosition pos) {
        synchronized (fmt) {
            return fmt.parseCurrency(source, pos);
        }
    }
    
    // boilerplate code to make CurrencyFormat otherwise follow the contract of
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
    
    
    @Override
    public int hashCode() {
        return getLocale().hashCode() + 154321962;
    }
    
    @Override
    protected boolean equalsSameClass(MeasureFormat other) {
        return getLocale().equals(other.getLocale());
    }
    
    // Serialization
    
    private Object writeReplace() throws ObjectStreamException {
        return mf.toCurrencyProxy();
    }
    
    // Preserve backward serialize backward compatibility.
    private Object readResolve() throws ObjectStreamException {
        return new CurrencyFormat(fmt.getLocale(ULocale.ACTUAL_LOCALE));
    }
}
