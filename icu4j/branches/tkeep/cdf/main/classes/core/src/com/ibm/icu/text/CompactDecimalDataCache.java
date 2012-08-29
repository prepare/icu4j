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

    // These constants map plural variants to indexes in plural variant arrays
    // which are found in the prefixes and suffixes arrays.
    static final int ZERO = 1;
    static final int ONE = 2;
    static final int TWO = 3;
    static final int FEW = 4;
    static final int MANY = 5;

    // This OTHER constant must always be 0 as OTHER is always required, and
    // plural variant arrays length may be 1 if all plural variants are the same.
    static final int OTHER = 0;

    /**
     * We can specify prefixes or suffixes for values < 10^15.
     */
    static final int MAX_DIGITS = 15;

    /**
     * Six plural variants in all.
     */
    static final int NUM_PLURAL_VARIANTS = 6;

    /**
     * Maps plural variant strings, e.g "one", "other" to their index.
     */
    static final Map<String, Integer> PLURAL_FORM_TO_INDEX =
            new HashMap<String, Integer>();

    /**
     * This plural variant array specifies empty string for all plural variants.
     * Note that its length is 1, signifying that all plural variants are the same.
     */
    private static final String[] EMPTY_TERM = new String[]{""};

    private final ICUCache<ULocale, DataBundle> cache =
            new SimpleCache<ULocale, DataBundle>();

    /**
     * Data contains the compact decimal data for a particular locale. Data consists
     * of three arrays. The index of each array corresponds to log10 of the number
     * being formatted, so when formatting 12,345, the 4th index of the arrays should
     * be used. Divisors contain the number to divide by before doing formatting.
     * In the case of english, <code>divisors[4]</code> is 1000.  So to format
     * 12,345, divide by 1000 to get 12. Then use PluralRules with the current
     * locale to figure out which of the 6 plural forms 12 matches: ZERO, ONE, TWO,
     * FEW, MANY, or OTHER. In Serbian, 12 matches MANY. Each element of prefixes and
     * suffixes is a plural variants array where each of those elements correspond
     * to ZERO, ONE, TWO, FEW, MANY or OTHER. Even though most languages do not
     * use all 6 plural forms, any unspecified plural form is set to be the same
     * as OTHER. For optimization, if all plural variants are the same, we may use
     * a single element plural variant array. If CompactDecimalFormat sees that
     * all the plural forms are the same, it can skip the call to PluralRules to
     * determine the correct plural rule to use. For any index, the plural
     * variant arrays for prefix and suffix will always be the same size (either 1
     * or NUM_PLURAL_VARIANTS).
     *
     * Each array in data is 15 in length, and every index is filled.
     *
     * @author Travis Keep
     *
     */
    static class Data {
        long[] divisors;
        String[][] prefixes;
        String[][] suffixes;

        Data(long[] divisors, String[][] prefixes, String[][] suffixes) {
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
        Data shortData = new DataLoader(ulocale, "patternsShort").loadWithStyle(false);
        Data longData = new DataLoader(ulocale, "patternsLong").loadWithStyle(true);
        if (longData == null) {
            longData = shortData;
        }
        return new DataBundle(shortData, longData);
    }

    /**
     * This class is responsible for loading data for a particular locale and
     * style.
     *
     * @author rocketman
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
         * @param allowNullResult If true, returns null if no data can be found
         * for particular locale and style. If false, throws a runtime exception
         * if data cannot be found.
         * @return The loaded data or possibly null if allowNullResult is true.
         */
        public Data loadWithStyle(boolean allowNullResult) {
            NumberingSystem ns = NumberingSystem.getInstance(locale);
            ICUResourceBundle r = (ICUResourceBundle)UResourceBundle.
                    getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, locale);
            String resourcePath =
                "NumberElements/" + ns.getName() + "/" + style + "/decimalFormat";
            if (allowNullResult) {
                r = r.findWithFallback(resourcePath);
            } else {
                r = r.getWithFallback(resourcePath);
            }
            int size = r.getSize();
            Data result = new Data(
                    new long[MAX_DIGITS],
                    new String[MAX_DIGITS][],
                    new String[MAX_DIGITS][]);
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
         * @param result rule stored in appropriate index in data arrays.
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

            // Create plural variant arrays for prefix and suffix.
            String[] newPrefix = new String[NUM_PLURAL_VARIANTS];
            String[] newSuffix = new String[NUM_PLURAL_VARIANTS];
            int size = divisorData.getSize();

            // keep track of how many zeros are used in the plural variants.
            // For "00K" this would be 2. This number must be the same for all
            // plural variants. If they differ, we throw a runtime exception as
            // such an anomaly is unrecoverable. We expect at least one zero.
            int numZeros = 0;
            // Loop over all the plural variants. e.g one, other
            for (int i = 0; i < size; i++) {
                UResourceBundle pluralVariantData = divisorData.get(i);
                String pluralVariant = pluralVariantData.getKey();
                String template = pluralVariantData.getString();
                int nz = populatePrefixSuffix(pluralVariant, template, newPrefix, newSuffix);
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
            if (numZeros == 0) {
                // Silently skip divisors with no variants.
                return;
            }

            // The OTHER variant must be defined.
            if (newPrefix[OTHER] == null || newSuffix[OTHER] == null) {
                throw new IllegalArgumentException(
                    "No 'other' form defined for 10^" + thisIndex + " in "
                    + localeAndStyle());
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

            // Fill in missing plural variants with OTHER variant while checking
            // to see if all variants are the same. Note that the prefixes and
            // suffixes arrays are always the same size (either both 1 or both
            // NUM_PLURAL_VARIANTS).
            boolean differentVariantValues = false;
            differentVariantValues |= fillInMissingVariantsWithOther(newPrefix);
            differentVariantValues |= fillInMissingVariantsWithOther(newSuffix);
            if (differentVariantValues) {
                result.prefixes[thisIndex] = newPrefix;
                result.suffixes[thisIndex] = newSuffix;
            } else {
                // If all variants are the same, optimize by using single
                // element arrays with just the OTHER variant.
                result.prefixes[thisIndex] = new String[] {newPrefix[OTHER]};
                result.suffixes[thisIndex] = new String[] {newSuffix[OTHER]};
            }
        }

        /**
         * Fills in any null values in the given pluralVariant array with the
         * value for the OTHER variant. This function expects that OTHER has
         * a non-null value already.
         * @return true if values for variants differ; false otherwise.
         */
        private static boolean fillInMissingVariantsWithOther(
                String[] pluralVariantArray) {
            String otherValue = pluralVariantArray[OTHER];
            boolean differentVariantValues = false;
            for (int i = 0; i < pluralVariantArray.length; i++) {
                if (i == OTHER) {
                    continue;
                }
                if (pluralVariantArray[i] == null) {
                    pluralVariantArray[i] = otherValue;
                } else if (!pluralVariantArray[i].equals(otherValue)) {
                    differentVariantValues = true;
                }
            }
            return differentVariantValues;
        }

        /**
         * Populates prefix and suffix information in the prefixes and suffixes
         * plural variant arrays.
         * @param pluralVariant e.g "one", "other"
         * @param template e.g "00K"
         * @param prefixes Found prefix stored in appropriate index of this array.
         * @param suffixes Found suffix stored in appropriate index of this array.
         * @return Number of zeros found in template variable before first decimal
         * point character.
         */
        private int populatePrefixSuffix(
            String pluralVariant, String template, String[] prefixes, String[] suffixes) {
            Integer pluralFormIdxObj = PLURAL_FORM_TO_INDEX.get(pluralVariant);

            // pluralVariant argument must be a valid plural variant string.
            if (pluralFormIdxObj == null) {
                throw new IllegalArgumentException(
                    "Plural variant '" + pluralVariant + "' not recognized in "
                    + localeAndStyle());
            }
            int pluralFormIdx = pluralFormIdxObj.intValue();
            int firstIdx = template.indexOf("0");
            int lastIdx = template.lastIndexOf("0");
            if (firstIdx == -1) {
                throw new IllegalArgumentException(
                    "Expect at least one zero in template '" + template +
                    "' for variant '" +pluralVariant + "' in " + localeAndStyle());
            }
            prefixes[pluralFormIdx] = template.substring(0, firstIdx);
            suffixes[pluralFormIdx] = template.substring(lastIdx + 1);

            // Calculate number of zeros before decimal point.
            int i = firstIdx + 1;
            while (i <= lastIdx && template.charAt(i) == '0') {
                i++;
            }
            return i - firstIdx;
        }

        /**
         * After reading information from resource bundle into a Data object, there
         * is no guarantee that every index of the arrays will be filled.
         *
         * This function walks through the arrays filling in indexes with missing
         * data from the previous index. If the first indexes are missing data,
         * they are assumed to have no prefixes or suffixes for any plural variant
         * and have a divisor of 1. We assume an index has missing data if the
         * corresponding element in the prefixes array is null.
         *
         * @param result this instance is fixed in-place.
         */
        private static void fillInMissing(Data result) {
            // Initially we assume that previous divisor is 1 with no prefix or suffix.
            long lastDivisor = 1L;
            String[] lastPrefixes = EMPTY_TERM;
            String[] lastSuffixes = EMPTY_TERM;
            for (int i = 0; i < result.divisors.length; i++) {
                if (result.prefixes[i] == null) {
                    result.divisors[i] = lastDivisor;
                    result.prefixes[i] = lastPrefixes;
                    result.suffixes[i] = lastSuffixes;
                } else {
                    lastDivisor = result.divisors[i];
                    lastPrefixes = result.prefixes[i];
                    lastSuffixes = result.suffixes[i];
                }
            }
        }

        /**
         * Returns locale and style. Used to form useful messages in thrown
         * exceptions.
         */
        private String localeAndStyle() {
            return "locale '" + locale + "' style '" + style + "'";
        }
    }

    static {
        PLURAL_FORM_TO_INDEX.put("other", OTHER);
        PLURAL_FORM_TO_INDEX.put("zero", ZERO);
        PLURAL_FORM_TO_INDEX.put("one", ONE);
        PLURAL_FORM_TO_INDEX.put("two", TWO);
        PLURAL_FORM_TO_INDEX.put("few", FEW);
        PLURAL_FORM_TO_INDEX.put("many", MANY);
    }
}
