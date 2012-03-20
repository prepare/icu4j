/*
 *******************************************************************************
 * Copyright (C) 2008-2012, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.localespi;

import java.util.Locale;
import java.util.TimeZone;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.text.TimeZoneNames;
import com.ibm.icu.text.TimeZoneNames.NameType;
import com.ibm.icu.util.ULocale;

public class TimeZoneNameTest extends TestFmwk {
    public static void main(String[] args) throws Exception {
        new TimeZoneNameTest().run(args);
    }

    public void TestTimeZoneNames() {
        Locale[] locales = Locale.getAvailableLocales();
        String[] tzids = TimeZone.getAvailableIDs();

        for (Locale loc : locales) {
            boolean warningOnly = false;
            if (TestUtil.isProblematicIBMLocale(loc)) {
                warningOnly = true;
            }

            for (String tzid : tzids) {
                // Java does not pick up time zone names for ID/Locale from an SPI
                // when long standard display name is not available.

                String icuStdLong = getIcuDisplayName(tzid, false, TimeZone.LONG, loc);
                if (icuStdLong != null) {
                    checkDisplayNamePair(TimeZone.SHORT, tzid, loc, warningOnly);
                    checkDisplayNamePair(TimeZone.LONG, tzid, loc, warningOnly);
                } else {
                    logln("Localized long standard name is not available for "
                            + tzid + " in locale " + loc + " in ICU");
                }
            }
        }
    }

    private void checkDisplayNamePair(int style, String tzid, Locale loc, boolean warnOnly) {
        /* Note: There are two problems here.
         * 
         * It looks Java 6 requires a TimeZoneNameProvider to return both standard name and daylight name
         * for a zone.  If the provider implementation only returns either of them, Java 6 also ignore
         * the other.  In ICU, there are zones which do not have daylight names, especially zones which
         * do not use daylight time.  This test case does not check a standard name if its daylight name
         * is not available because of the Java 6 implementation problem.
         * 
         * Another problem is that ICU always use a standard name for a zone which does not use daylight
         * saving time even daylight name is requested.
         */

        String icuStdName = getIcuDisplayName(tzid, false, style, loc);
        String icuDstName = getIcuDisplayName(tzid, true, style, loc);
        if (icuStdName != null && icuDstName != null && !icuStdName.equals(icuDstName)) {
            checkDisplayName(false, style, tzid, loc, icuStdName, warnOnly);
            checkDisplayName(true, style, tzid, loc, icuDstName, warnOnly);
        }
    }

    private String getIcuDisplayName(String tzid, boolean daylight, int style, Locale loc) {
        String icuName = null;
        boolean[] isSystemID = new boolean[1];
        String canonicalID = com.ibm.icu.util.TimeZone.getCanonicalID(tzid, isSystemID);
        if (isSystemID[0]) {
            long date = System.currentTimeMillis();
            TimeZoneNames tznames = TimeZoneNames.getInstance(ULocale.forLocale(loc));
            switch (style) {
            case TimeZone.LONG:
                icuName = daylight ?
                        tznames.getDisplayName(canonicalID, NameType.LONG_DAYLIGHT, date) :
                        tznames.getDisplayName(canonicalID, NameType.LONG_STANDARD, date);
                break;
            case TimeZone.SHORT:
                icuName = daylight ?
                        tznames.getDisplayName(canonicalID, NameType.SHORT_DAYLIGHT, date) :
                        tznames.getDisplayName(canonicalID, NameType.SHORT_STANDARD, date);
                break;
            }
        }
        return icuName;
    }

    private void checkDisplayName(boolean daylight, int style,  String tzid, Locale loc, String icuname, boolean warnOnly) {
        String styleStr = (style == TimeZone.SHORT) ? "SHORT" : "LONG";
        TimeZone tz = TimeZone.getTimeZone(tzid);
        String name = tz.getDisplayName(daylight, style, loc);

        if (TestUtil.isICUExtendedLocale(loc)) {
            // The name should be taken from ICU
            if (!name.equals(icuname)) {
                if (warnOnly) {
                    logln("WARNING: TimeZone name by ICU is " + icuname + ", but got " + name
                            + " for time zone " + tz.getID() + " in locale " + loc
                            + " (daylight=" + daylight + ", style=" + styleStr + ")");
                    
                } else {
                    errln("FAIL: TimeZone name by ICU is " + icuname + ", but got " + name
                            + " for time zone " + tz.getID() + " in locale " + loc
                            + " (daylight=" + daylight + ", style=" + styleStr + ")");
                }
            }
        } else {
            if (!name.equals(icuname)) {
                logln("INFO: TimeZone name by ICU is " + icuname + ", but got " + name
                        + " for time zone " + tz.getID() + " in locale " + loc
                        + " (daylight=" + daylight + ", style=" + styleStr + ")");
            }
            // Try explicit ICU locale (xx_yy_ICU)
            Locale icuLoc = TestUtil.toICUExtendedLocale(loc);
            name = tz.getDisplayName(daylight, style, icuLoc);
            if (!name.equals(icuname)) {
                if (warnOnly) {
                    logln("WARNING: TimeZone name by ICU is " + icuname + ", but got " + name
                            + " for time zone " + tz.getID() + " in locale " + icuLoc
                            + " (daylight=" + daylight + ", style=" + styleStr + ")");
                } else {
                    errln("FAIL: TimeZone name by ICU is " + icuname + ", but got " + name
                            + " for time zone " + tz.getID() + " in locale " + icuLoc
                            + " (daylight=" + daylight + ", style=" + styleStr + ")");
                }
            }
        }
    }
}
