/*
 *******************************************************************************
 * Copyright (C) 2012, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.util.HashMap;
import java.util.Map;

import com.ibm.icu.impl.ICUCache;
import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.SimpleCache;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.UResourceBundle;

/**
 * A cache containing data by locale for {@link CompactDecimalFormat}
 *
 * @author Travis Keep
 */
class CompactDecimalDataCache {

    static final String OTHER = "other";

    /**
     * We can specify prefixes or suffixes for values with up to 15 digits,
     * less than 10^15.
     */
    static final int MAX_DIGITS = 15;

    private final ICUCache<ULocale, DataBundle> cache =
            new SimpleCache<ULocale, DataBundle>();

    /**
     * Data contains the compact decimal data for a particular locale. Data consists
     * of one array and two hashmaps. The index of the divisors array as well
     * as the arrays stored in the values of the two hashmaps correspond
     * to log10 of the number being formatted, so when formatting 12,345, the 4th
     * index of the arrays should be used. Divisors contain the number to divide
     * by before doing formatting. In the case of english, <code>divisors[4]</code>
     * is 1000.  So to format 12,345, divide by 1000 to get 12. Then use
     * PluralRules with the current locale to figure out which of the 6 plural variants
     * 12 matches: "zero", "one", "two", "few", "many", or "other." Prefixes and
     * suffixes are maps whose key is the plural variant and whose values are
     * arrays of strings with indexes corresponding to log10 of the original number.
     * these arrays contain the prefix or suffix to use.
     *
     * Each array in data is 15 in length, and every index is filled.
     *
     * @author Travis Keep
     *
     */
    static class Data {
        long[] divisors;
        Map<String, String[]> prefixes;
        Map<String, String[]> suffixes;

        Data(long[] divisors, Map<String, String[]> prefixes, Map<String, String[]> suffixes) {
            this.divisors = divisors;
            this.prefixes = prefixes;
            this.suffixes = suffixes;
        }
    }

    /**
     * DataBundle contains compact decimal data for all the styles in a particular
     * locale. Currently available styles are short and long.
     *
     * @author Travis Keep
     */
    static class DataBundle {
        Data shortData;
        Data longData;

        DataBundle(Data shortData, Data longData) {
            this.shortData = shortData;
            this.longData = longData;
        }
    }


    /**
     * Fetch data for a particular locale. Clients must not modify any part
     * of the returned data. Portions of returned data may be shared so modifying
     * it will have unpredictable results.
     */
    DataBundle get(ULocale locale) {
        DataBundle result = cache.get(locale);
        if (result == null) {
            result = load(locale);
            cache.put(locale, result);
        }
        return result;
    }

    /**
     * Loads the "patternsShort" and "patternsLong" data for a particular locale.
     * We assume that "patternsShort" data can be found for any locale. If we can't
     * find it we throw an exception. However, we allow "patternsLong" data to be
     * missing for a locale. In this case, we assume that the "patternsLong" data
     * is identical to the "paternsShort" data.
     * @param ulocale the locale for which we are loading the data.
     * @return The returned data, never null.
     */
    private static DataBundle load(ULocale ulocale) {
        NumberingSystem ns = NumberingSystem.getInstance(ulocale);
        ICUResourceBundle r = (ICUResourceBundle)UResourceBundle.
                getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, ulocale);
        String numberingSystemName = ns.getName();
        Data shortData = new DataLoader(ulocale, "patternsShort")
            .loadWithStyle(r, numberingSystemName, false);
        Data longData = new DataLoader(ulocale, "patternsLong")
            .loadWithStyle(r, numberingSystemName, true);
        if (longData == null) {
            longData = shortData;
        }
        return new DataBundle(shortData, longData);
    }

    /**
     * This class is responsible for loading data for a particular locale and
     * style.
     *
     * @author Travis Keep
     */
    static class DataLoader {
        private final ULocale locale;
        private final String style;

        /**
         * @param locale The locale
         * @param style Either "patternsShort" or "patternsLong"
         */
        public DataLoader(ULocale locale, String style) {
            this.locale = locale;
            this.style = style;
        }

        /**
         * Loads the data
         * @param r the main resource bundle.
         * @param numberingSystemName The namespace name.
         * @param allowNullResult If true, returns null if no data can be found
         * for particular locale and style. If false, throws a runtime exception
         * if data cannot be found.
         * @return The loaded data or possibly null if allowNullResult is true.
         */
        public Data loadWithStyle(
                ICUResourceBundle r, String numberingSystemName, boolean allowNullResult) {
            String resourcePath =
                "NumberElements/" + numberingSystemName + "/" + style + "/decimalFormat";
            if (allowNullResult) {
                r = r.findWithFallback(resourcePath);
            } else {
                r = r.getWithFallback(resourcePath);
            }
            int size = r.getSize();
            Data result = new Data(
                    new long[MAX_DIGITS],
                    new HashMap<String, String[]>(),
                    new HashMap<String, String[]>());
            for (int i = 0; i < size; i++) {
                populateData(r.get(i), result);
            }
            fillInMissing(result);
            return result;
        }

        /**
         * Populates Data object with data for a particular divisor from resource bundle.
         * @param divisorData represents the rules for numbers of a particular size.
         * This may look like:
         * <pre>
         *   10000{
         *       few{"00K"}
         *       many{"00K"}
         *       one{"00 xnb"}
         *       other{"00 xnb"}
         *   }
         * </pre>
         * @param result rule stored here.
         *
         */
        private void populateData(UResourceBundle divisorData, Data result) {
            // This value will always be some even pwoer of 10. e.g 10000.
            long magnitude = Long.parseLong(divisorData.getKey());
            int thisIndex = (int) Math.log10(magnitude);

            // Silently ignore divisors that are too big.
            if (thisIndex >= MAX_DIGITS) {
                return;
            }

            int size = divisorData.getSize();

            // keep track of how many zeros are used in the plural variants.
            // For "00K" this would be 2. This number must be the same for all
            // plural variants. If they differ, we throw a runtime exception as
            // such an anomaly is unrecoverable. We expect at least one zero.
            int numZeros = 0;

            // Keep track if this block defines "other" variant. If a block
            // fails to define the "other" variant, we must immediately throw
            // an exception as it is assumed that "other" variants are always
            // defined.
            boolean otherVariantDefined = false;

            // Loop over all the plural variants. e.g one, other.
            for (int i = 0; i < size; i++) {
                UResourceBundle pluralVariantData = divisorData.get(i);
                String pluralVariant = pluralVariantData.getKey();
                String template = pluralVariantData.getString();
                if (pluralVariant.equals(OTHER)) {
                    otherVariantDefined = true;
                }
                int nz = populatePrefixSuffix(pluralVariant, thisIndex, template, result);
                if (nz != numZeros) {
                    if (numZeros != 0) {
                        throw new IllegalArgumentException(
                            "Plural variant '" + pluralVariant + "' template '" +
                            template + "' for 10^" + thisIndex +
                            " has wrong number of zeros in " + localeAndStyle());
                    }
                    numZeros = nz;
                }
            }

            if (!otherVariantDefined) {
                throw new IllegalArgumentException(
                        "No 'other' plural variant defined for 10^" + thisIndex +
                        "in " +localeAndStyle());
            }

            // We craft our divisor such that when we divide by it, we get a
            // number with the same number of digits as zeros found in the
            // plural variant templates. If our magnitude is 10000 and we have
            // two 0's in our plural variants, then we want a divisor of 1000.
            // Note that if we have 43560 which is of same magnitude as 10000.
            // When we divide by 1000 we a quotient which rounds to 44 (2 digits)
            long divisor = magnitude;
            for (int i = 1; i < numZeros; i++) {
                divisor /= 10;
            }
            result.divisors[thisIndex] = divisor;
        }


        /**
         * Populates prefix and suffix information for a particular plural variant
         * and index (log10 value).
         * @param pluralVariant e.g "one", "other"
         * @param idx the index (log10 value of the number) 0 <= idx < MAX_DIGITS
         * @param template e.g "00K"
         * @param result Extracted prefix and suffix stored here.
         */
        private int populatePrefixSuffix(
                String pluralVariant, int idx, String template, Data result) {
            int firstIdx = template.indexOf("0");
            int lastIdx = template.lastIndexOf("0");
            if (firstIdx == -1) {
                throw new IllegalArgumentException(
                    "Expect at least one zero in template '" + template +
                    "' for variant '" +pluralVariant + "' for 10^" + idx +
                    " in " + localeAndStyle());
            }
            savePrefixOrSuffix(
                    template.substring(0, firstIdx), pluralVariant, idx, result.prefixes);
            savePrefixOrSuffix(
                    template.substring(lastIdx + 1), pluralVariant, idx, result.suffixes);

            // Calculate number of zeros before decimal point.
            int i = firstIdx + 1;
            while (i <= lastIdx && template.charAt(i) == '0') {
                i++;
            }
            return i - firstIdx;
        }


        /**
         * Returns locale and style. Used to form useful messages in thrown
         * exceptions.
         */
        private String localeAndStyle() {
            return "locale '" + locale + "' style '" + style + "'";
        }
    }

    /**
     * After reading information from resource bundle into a Data object, there
     * is guarantee that it is complete.
     *
     * This method fixes any incomplete data it finds within <code>result</code>.
     * It looks at each log10 value applying the two rules.
     *   <p>
     *   If no prefix is defined for the "other" variant, use the divisor, prefixes and
     *   suffixes for all defined variants from the previous log10. For log10 = 0,
     *   use all empty prefixes and suffixes and a divisor of 1.
     *   </p><p>
     *   Otherwise, examine each plural variant defined for the given log10 value.
     *   If it has no prefix and suffix for a particular variant, use the one from the
     *   "other" variant.
     *   </p>
     *
     * @param result this instance is fixed in-place.
     */
    private static void fillInMissing(Data result) {
        // Initially we assume that previous divisor is 1 with no prefix or suffix.
        long lastDivisor = 1L;
        for (int i = 0; i < result.divisors.length; i++) {
            if (result.prefixes.get(OTHER)[i] == null) {
                result.divisors[i] = lastDivisor;
                copyFromPreviousIndex(i, result.prefixes);
                copyFromPreviousIndex(i, result.suffixes);
            } else {
                lastDivisor = result.divisors[i];
                propagateOtherToMissing(i, result.prefixes);
                propagateOtherToMissing(i, result.suffixes);
            }
        }
    }

    private static void propagateOtherToMissing(
            int idx, Map<String, String[]> prefixesOrSuffixes) {
        String otherVariantValue = prefixesOrSuffixes.get(OTHER)[idx];
        for (String[] byBase : prefixesOrSuffixes.values()) {
            if (byBase[idx] == null) {
                byBase[idx] = otherVariantValue;
            }
        }
    }

    private static void copyFromPreviousIndex(int idx, Map<String, String[]> prefixesOrSuffixes) {
        for (String[] byBase : prefixesOrSuffixes.values()) {
            if (idx == 0) {
                byBase[idx] = "";
            } else {
                byBase[idx] = byBase[idx - 1];
            }
        }
    }

    private static void savePrefixOrSuffix(
            String value, String pluralVariant, int idx,
            Map<String, String[]> prefixesOrSuffixes) {
        String[] byBase = prefixesOrSuffixes.get(pluralVariant);
        if (byBase == null) {
            byBase = new String[MAX_DIGITS];
            prefixesOrSuffixes.put(pluralVariant, byBase);
        }
        byBase[idx] = value;

    }

    /**
     * Fetches a prefix or suffix given a plural variant and log10 value. If it
     * can't find the given variant, it falls back to "other".
     * @param prefixOrSuffix the prefix or suffix map
     * @param variant the plural variant
     * @param base log10 value. 0 <= base < MAX_DIGITS.
     * @return the prefix or suffix.
     */
    static String getPrefixOrSuffix(
            Map<String, String[]> prefixOrSuffix, String variant, int base) {
        String[] byBase = prefixOrSuffix.get(variant);
        if (byBase == null) {
            byBase = prefixOrSuffix.get(CompactDecimalDataCache.OTHER);
        }
        return byBase[base];
    }
}
