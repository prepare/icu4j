/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.io.IOException;

final class DigitFormatter {
    private final int groupingSize;
    private final int groupingSize2;

    private final char groupingSeparator;

    private final int minimumGroupingSize;

    private final boolean groupingUsed;

    private final char[] nativeDigits;

    DigitFormatter(DecimalFormat decimalFormat) {
        groupingSize = decimalFormat.getGroupingSize();
        groupingUsed = decimalFormat.isGroupingUsed();
        minimumGroupingSize = groupingSize + 1;
        groupingSize2 = decimalFormat.getSecondaryGroupingSize();
        groupingSeparator = decimalFormat.getDecimalFormatSymbols().getGroupingSeparator();
        nativeDigits = decimalFormat.getDecimalFormatSymbols().getDigits();
    }

    public <T extends Appendable> void appendDigits(DigitList2 digits, T toAppendTo) throws IOException {
        int totalDigits = digits.getTotalDigits();
        if (groupingUsed && digits.getIntegerDigits() >= minimumGroupingSize) {
            // TODO Hindi
            int groupingDigits = digits.getIntegerDigits() % groupingSize;
            for (int i = 0; i < totalDigits; ++i) {
                if (groupingDigits == 0) {
                    toAppendTo.append(groupingSeparator);
                    groupingDigits += groupingSize;
                }
                --groupingDigits;
                toAppendTo.append(nativeDigits[digits.getDigit(i)]);
            }
        } else {
            for (int i = 0; i < totalDigits; ++i) {
                toAppendTo.append(nativeDigits[digits.getDigit(i)]);
            }
        }
    }

}