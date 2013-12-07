/*
 *******************************************************************************
 * Copyright (C) 2013, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.format;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.dev.test.serializable.SerializableTest;
import com.ibm.icu.impl.Utility;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.MeasureFormat;
import com.ibm.icu.text.MeasureFormat.FormatWidth;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.util.Currency;
import com.ibm.icu.util.Measure;
import com.ibm.icu.util.MeasureUnit;
import com.ibm.icu.util.TimeUnit;
import com.ibm.icu.util.TimeUnitAmount;
import com.ibm.icu.util.ULocale;

/**
 * @author markdavis
 */
public class MeasureUnitTest extends TestFmwk {
    
    private static final TimeUnitAmount[] _19m = {new TimeUnitAmount(19.0, TimeUnit.MINUTE)};
    private static final TimeUnitAmount[] _1h_23_5s = {
            new TimeUnitAmount(1.0, TimeUnit.HOUR),
            new TimeUnitAmount(23.5, TimeUnit.SECOND)};
    private static final TimeUnitAmount[] _1h_23_5m = {
            new TimeUnitAmount(1.0, TimeUnit.HOUR),
            new TimeUnitAmount(23.5, TimeUnit.MINUTE)};
    private static final TimeUnitAmount[] _1h_0m_23s = {
            new TimeUnitAmount(1.0, TimeUnit.HOUR),
            new TimeUnitAmount(0.0, TimeUnit.MINUTE),
            new TimeUnitAmount(23.0, TimeUnit.SECOND)};
    private static final TimeUnitAmount[] _2y_5M_3w_4d = {
            new TimeUnitAmount(2.0, TimeUnit.YEAR),
            new TimeUnitAmount(5.0, TimeUnit.MONTH),
            new TimeUnitAmount(3.0, TimeUnit.WEEK),
            new TimeUnitAmount(4.0, TimeUnit.DAY)};
    private static final TimeUnitAmount[] _1m_59_9996s = {
            new TimeUnitAmount(1.0, TimeUnit.MINUTE),
            new TimeUnitAmount(59.9996, TimeUnit.SECOND)};
    
    public void TestAAA() {
        System.out.println(TimeUnit.HOUR);
    }
    
    
    /**
     * @author markdavis
     *
     */
    public static void main(String[] args) {
        //generateConstants(); if (true) return;
        new MeasureUnitTest().run(args);
    }
    
    public void TestFormatPeriodEn() {
        Object[][] fullData = {
                {_1m_59_9996s, "1 minute, 59.9996 seconds"},
                {_19m, "19 minutes"},
                {_1h_23_5s, "1 hour, 23.5 seconds"},
                {_1h_23_5m, "1 hour, 23.5 minutes"},
                {_1h_0m_23s, "1 hour, 0 minutes, 23 seconds"},
                {_2y_5M_3w_4d, "2 years, 5 months, 3 weeks, 4 days"}};
        Object[][] abbrevData = {
                {_1m_59_9996s, "1 min, 59.9996 secs"},
                {_19m, "19 mins"},
                {_1h_23_5s, "1 hr, 23.5 secs"},
                {_1h_23_5m, "1 hr, 23.5 mins"},
                {_1h_0m_23s, "1 hr, 0 mins, 23 secs"},
                {_2y_5M_3w_4d, "2 yrs, 5 mths, 3 wks, 4 days"}};
        
        // TODO(Travis Keep): We need to support numeric formatting. Either here or in TimeUnitFormat.
        /*
        Object[][] numericData = {
                {_1m_59_9996s, "1:59.9996"},
                {_19m, "19 mins"},
                {_1h_23_5s, "1:00:23.5"},
                {_1h_0m_23s, "1:00:23"},
                {_1h_23_5m, "1:23.5"},
                {_5h_17m, "5:17"},
                {_19m_28s, "19:28"},
                {_2y_5M_3w_4d, "2 yrs, 5 mths, 3 wks, 4 days"},
                {_0h_0m_17s, "0:00:17"},
                {_6h_56_92m, "6:56.92"}};
       */
        
        NumberFormat nf = NumberFormat.getNumberInstance(ULocale.ENGLISH);
        nf.setMaximumFractionDigits(4);
        MeasureFormat mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.WIDE, nf);
        verifyFormatPeriod("en FULL", mf, fullData);
        mf = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.SHORT, nf);
        verifyFormatPeriod("en SHORT", mf, abbrevData);
       
       
    }
    
    private void verifyFormatPeriod(String desc, MeasureFormat mf, Object[][] testData) {
        StringBuilder builder = new StringBuilder();
        boolean failure = false;
        for (Object[] testCase : testData) {
            String actual = mf.format((Measure[]) testCase[0]);
            if (!testCase[1].equals(actual)) {
                builder.append(String.format("%s: Expected: '%s', got: '%s'\n", desc, testCase[1], actual));
                failure = true;
            }
        }
        if (failure) {
            errln(builder.toString());
        }
    }

    public void testAUnit() {
        String lastType = null;
        for (MeasureUnit expected : MeasureUnit.getAvailable()) {
            String type = expected.getType();
            String code = expected.getSubtype();
            if (!type.equals(lastType)) {
                logln(type);
                lastType = type;
            }
            MeasureUnit actual = MeasureUnit.internalGetInstance(type, code);
            assertSame("Identity check", expected, actual);
        }
    }
    
    public void testTimePeriods() {
        
    }

    public void testMultiples() {
        for (ULocale locale : new ULocale[]{
                ULocale.ENGLISH, 
                new ULocale("ru"), 
                //ULocale.JAPANESE
        }) {

            for (FormatWidth style : FormatWidth.values()) {
                MeasureFormat mf = MeasureFormat.getInstance(locale, style);
                String formatted = mf.format(
                        new Measure(2, MeasureUnit.MILE), 
                        new Measure(1, MeasureUnit.FOOT), 
                        new Measure(2.3, MeasureUnit.INCH));
                logln(locale + ",\t" + style + ": " + formatted);
            }

        }
    }

    public void testGram() {
        checkRoundtrip(ULocale.ENGLISH, MeasureUnit.GRAM, 1, 0, FormatWidth.SHORT);
        checkRoundtrip(ULocale.ENGLISH, MeasureUnit.G_FORCE, 1, 0, FormatWidth.SHORT);
    }

    public void testRoundtripFormat() {        
        for (ULocale locale : new ULocale[]{
                ULocale.ENGLISH, 
                new ULocale("ru"), 
                //ULocale.JAPANESE
        }) {
            for (MeasureUnit unit : MeasureUnit.getAvailable()) {
                for (double d : new double[]{2.1, 1}) {
                    for (int fractionalDigits : new int[]{0, 1}) {
                        for (FormatWidth style : FormatWidth.values()) {
                            checkRoundtrip(locale, unit, d, fractionalDigits, style);
                        }
                    }
                }
            }
        }
    }

    private void checkRoundtrip(ULocale locale, MeasureUnit unit, double d, int fractionalDigits, FormatWidth style) {
        if (unit instanceof Currency) {
            return; // known limitation
        }
        Measure amount = new Measure(d, unit);
        String header = locale
                + "\t" + unit
                + "\t" + d
                + "\t" + fractionalDigits;
        ParsePosition pex = new ParsePosition(0);
        NumberFormat nformat = NumberFormat.getInstance(locale);
        nformat.setMinimumFractionDigits(fractionalDigits);

        MeasureFormat format = MeasureFormat.getInstance(locale, style, nformat);
        
        FieldPosition pos = new FieldPosition(DecimalFormat.FRACTION_FIELD);
        StringBuffer b = format.<StringBuffer>format(amount, new StringBuffer(), pos);
        String message = header + "\t" + style
                + "\t«" + b.substring(0, pos.getBeginIndex())
                + "⟪" + b.substring(pos.getBeginIndex(), pos.getEndIndex())
                + "⟫" + b.substring(pos.getEndIndex()) + "»";
        pex.setIndex(0);
        Measure unitAmount = format.parseObject(b.toString(), pex);
        if (!assertNotNull(message, unitAmount)) {
            logln("Parse: «" 
                    + b.substring(0,pex.getErrorIndex())
                    + "||" + b.substring(pex.getErrorIndex()) + "»");
        } else if (style != FormatWidth.NARROW) { // narrow items may collide
            if (unit.equals(MeasureUnit.GRAM)) {
                logKnownIssue("cldrupdate", "waiting on collision fix for gram");
                return;
            }
            if (unit.equals(MeasureUnit.ARC_MINUTE) || unit.equals(MeasureUnit.ARC_SECOND) || unit.equals(MeasureUnit.METER)) {
                logKnownIssue("8474", "Waiting for CLDR data");
            } else {
                assertEquals(message + "\tParse Roundtrip of unit", unit, unitAmount.getUnit());
            }
            double actualNumber = unitAmount.getNumber().doubleValue();
            assertEquals(message + "\tParse Roundtrip of number", d, actualNumber);
        }
    }

    public void testExamples() {
        MeasureFormat fmtFr = MeasureFormat.getInstance(ULocale.FRENCH, FormatWidth.SHORT);
        Measure measure = new Measure(23, MeasureUnit.CELSIUS);
        assertEquals("", "23 °C", fmtFr.format(measure));

        Measure measureF = new Measure(70, MeasureUnit.FAHRENHEIT);
        assertEquals("", "70 °F", fmtFr.format(measureF));

        MeasureFormat fmtFrFull = MeasureFormat.getInstance(ULocale.FRENCH, FormatWidth.WIDE);
        if (!logKnownIssue("8474", "needs latest CLDR data")) {
            assertEquals("", "70 pieds, 5,3 pouces", fmtFrFull.format(new Measure(70, MeasureUnit.FOOT),
                    new Measure(5.3, MeasureUnit.INCH)));
            assertEquals("", "1 pied, 1 pouce", fmtFrFull.format(new Measure(1, MeasureUnit.FOOT),
                    new Measure(1, MeasureUnit.INCH)));
        }
        // Degenerate case
        MeasureFormat fmtEn = MeasureFormat.getInstance(ULocale.ENGLISH, FormatWidth.WIDE);
        assertEquals("", "1 inch, 2 feet", fmtEn.format(new Measure(1, MeasureUnit.INCH),
                new Measure(2, MeasureUnit.FOOT)));

        logln("Show all currently available units");
        String lastType = null;
        for (MeasureUnit unit : MeasureUnit.getAvailable()) {
            String type = unit.getType();
            if (!type.equals(lastType)) {
                logln(type);
                lastType = type;
            }
            logln("\t" + unit);
        }
        // TODO 
        // Add these examples (and others) to the class definition.
        // Clarify that these classes *do not* do conversion; they simply do the formatting of whatever units they
        // are provided.
    }

    static void generateConstants() {
        System.out.println("static final MeasureUnit");
        Map<String, MeasureUnit> seen = new HashMap<String, MeasureUnit>();
        boolean first = true;
        for (String type : new TreeSet<String>(MeasureUnit.getAvailableTypes())) {
            for (MeasureUnit unit : MeasureUnit.getAvailable(type)) {
                String code = unit.getSubtype();
                String name = code.toUpperCase(Locale.ENGLISH).replace("-", "_");

                if (type.equals("angle")) {
                    if (code.equals("minute") || code.equals("second")) {
                        name = "ARC_" + name;
                    }
                }
                if (first) {
                    first = false;
                } else {
                    System.out.print(",");
                }
                if (seen.containsKey(name)) {
                    System.out.println("\nCollision!!" + unit + ", " + seen.get(name));
                } else {
                    seen.put(name, unit);
                }
                System.out.println("\n\t/** Constant for unit of " + type +
                        ": " +
                        code +
                        " */");

                System.out.print("\t" + name + " = MeasureUnit.getInstance(\"" +
                        type +
                        "\", \"" +
                        code +
                        "\")");
            }
            System.out.println(";");
        }
    }
    
    public void TestSerial() {
        checkStreamingEquality(MeasureUnit.CELSIUS);
        checkStreamingEquality(MeasureFormat.getInstance(ULocale.FRANCE, FormatWidth.NARROW));
        checkStreamingEquality(Currency.getInstance("EUR"));
    }
    
    public <T extends Serializable> void checkStreamingEquality(T item) {
        try {
          ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
          ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOut);
          objectOutputStream.writeObject(item);
          objectOutputStream.close();
          byte[] contents = byteOut.toByteArray();
          logln("bytes: " + contents.length + "; " + item.getClass() + ": " + showBytes(contents));
          ByteArrayInputStream byteIn = new ByteArrayInputStream(contents);
          ObjectInputStream objectInputStream = new ObjectInputStream(byteIn);
          Object obj = objectInputStream.readObject();
          assertEquals("Streamed Object equals ", item, obj);
        } catch (IOException e) {
          assertNull("Test Serialization " + item.getClass(), e);
        } catch (ClassNotFoundException e) {
          assertNull("Test Serialization " + item.getClass(), e);
        }
      }

    /**
     * @param contents
     * @return
     */
    private String showBytes(byte[] contents) {
      StringBuilder b = new StringBuilder('[');
      for (int i = 0; i < contents.length; ++i) {
        int item = contents[i] & 0xFF;
        if (item >= 0x20 && item <= 0x7F) {
          b.append((char) item);
        } else {
          b.append('(').append(Utility.hex(item, 2)).append(')');
        }
      }
      return b.append(']').toString();
    }
    
    public static class MeasureUnitHandler implements SerializableTest.Handler
    {
        public Object[] getTestObjects()
        {
            MeasureUnit items[] = {
                    MeasureUnit.CELSIUS,
                    Currency.getInstance("EUR")               
            };
            return items;
        }

        public boolean hasSameBehavior(Object a, Object b)
        {
            MeasureUnit a1 = (MeasureUnit) a;
            MeasureUnit b1 = (MeasureUnit) b;
            return a1.getType().equals(b1.getType()) 
                    && a1.getSubtype().equals(b1.getSubtype());
        }
    }
    
    public static class GeneralMeasureFormatHandler  implements SerializableTest.Handler
    {
        public Object[] getTestObjects()
        {
            MeasureFormat items[] = {
                    MeasureFormat.getInstance(ULocale.FRANCE, FormatWidth.SHORT),
                    MeasureFormat.getInstance(ULocale.FRANCE, FormatWidth.WIDE, NumberFormat.getIntegerInstance(ULocale.CANADA_FRENCH
                            )),
            };
            return items;
        }

        public boolean hasSameBehavior(Object a, Object b)
        {
            MeasureFormat a1 = (MeasureFormat) a;
            MeasureFormat b1 = (MeasureFormat) b;
            return a1.getLocale().equals(b1.getLocale()) 
                    && a1.getWidth().equals(b1.getWidth())
                    // && a1.getNumberFormat().equals(b1.getNumberFormat())
                    ;
        }
    }
}
