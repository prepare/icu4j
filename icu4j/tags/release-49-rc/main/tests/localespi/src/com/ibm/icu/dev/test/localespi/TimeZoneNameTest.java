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
import com.ibm.icu.lang.UCharacter;
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
                com.ibm.icu.util.TimeZone tzIcu = com.ibm.icu.util.TimeZone.getTimeZone(tzid);

                // Java does not pick up time zone names for ID/Locale from an SPI
                // when long standard display name is not available.

                String icuStdLong = getIcuDisplayName(tzIcu, false, TimeZone.LONG, loc);
                if (icuStdLong != null) {
                    TimeZone tz = TimeZone.getTimeZone(tzid);
                    checkDisplayNamePair(TimeZone.SHORT, tz, tzIcu, loc, warningOnly);
                    checkDisplayNamePair(TimeZone.LONG, tz, tzIcu, loc, warningOnly);
                } else {
                    logln("Localized long standard name is not available for "
                            + tzid + " in locale " + loc + " in ICU");
                }
            }
        }
    }

    private void checkDisplayNamePair(int style, TimeZone tz, com.ibm.icu.util.TimeZone icuTz, Locale loc, boolean warnOnly) {
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

        String icuStdName = getIcuDisplayName(icuTz, false, style, loc);
        String icuDstName = getIcuDisplayName(icuTz, true, style, loc);
        if (icuStdName != null && icuDstName != null && !icuStdName.equals(icuDstName)) {
            checkDisplayName(false, style, tz, loc, icuStdName, warnOnly);
            checkDisplayName(true, style, tz, loc, icuDstName, warnOnly);
        }
    }

    private String getIcuDisplayName(com.ibm.icu.util.TimeZone icuTz, boolean daylight, int style, Locale loc) {
        ULocale uloc = ULocale.forLocale(loc);
        boolean shortStyle = (style == TimeZone.SHORT);
        String icuname = icuTz.getDisplayName(daylight,
                (shortStyle ? com.ibm.icu.util.TimeZone.SHORT : com.ibm.icu.util.TimeZone.LONG),
                uloc);
        int numDigits = 0;
        for (int i = 0; i < icuname.length(); i++) {
            if (UCharacter.isDigit(icuname.charAt(i))) {
                numDigits++;
            }
        }
        if (numDigits >= 3) {
            // ICU does not have the localized name
            return null;
        }
        return icuname;
    }

    private void checkDisplayName(boolean daylight, int style, TimeZone tz, Locale loc, String icuname, boolean warnOnly) {
        String styleStr = (style == TimeZone.SHORT) ? "SHORT" : "LONG";

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
