/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package com.ibm.icu.text;

/**
 * @author markdavis
 */
public class DigitList2 implements Cloneable {
    private static final boolean CHECK_INVARIANTS = true;
    
    private boolean isNegative = false; 
    private int totalDigits = 0; // the total number of digits. For example, 0123.4560 has 8 digits
    private int integerDigits = 0; // the total number of integer digits. For example, 0123.4560 has 4 integer digits
    private int startDigits = 0; // the start position for the first non-zero digit
    
    private int start = 0; // the starting point in digitList for the real digits
    private int count = 0; // the actual number of digits in digitList, eg start..(start+count)
    private byte[] digitList = new byte[DigitBuilder.MAX_LONG_DIGITS]; // a list of digits

    /**
     * @return the isNegative
     */
    public boolean isNegative() {
        return isNegative;
    }
    /**
     * @param isNegative the isNegative to set
     */
    public void setNegative(boolean isNegative) {
        this.isNegative = isNegative;
    }
    /**
     * @return the integerDigits
     */
    public int getIntegerDigits() {
        return integerDigits;
    }
    /**
     * @param integerDigits the integerDigits to set
     */
    public void setIntegerDigits(int integerDigits) {
        this.integerDigits = integerDigits;
    }
    /**
     * @return the totalDigits
     */
    public int getTotalDigits() {
        return totalDigits;
    }
    /**
     * @param totalDigits the totalDigits to set
     */
    public void setTotalDigits(int totalDigits) {
        this.totalDigits = totalDigits;
    }
    /**
     * @return the startDigits
     */
    public int getStartDigits() {
        return startDigits;
    }
    /**
     * @param startDigits the startDigits to set
     */
    public void setStartDigits(int startDigits) {
        this.startDigits = startDigits;
    }
    
//    public void addDigit(byte value) {
//        digits[count++] = value;
//    }
//
//    public void addDigits(byte... values) {
//        for (byte value : values) {
//            digits[count++] = value;
//        }
//    }

    @Override
    public DigitList2 clone() {
        try {
            DigitList2 result = (DigitList2) super.clone();
            result.digitList = digitList.clone(); // primitives are ok, but need to clone array
            return result;
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    public DigitList2 clear() {
        this.isNegative = false;
        this.totalDigits = 0;
        this.startDigits = 0;
        this.integerDigits = 0;
        count = 0;
        return this;
    }
    /*
     * Examples:
     * 0 : digits=<>, integerDigits=1
     * 003.40 : digits=<34>, decimalAt=1, integerDigits=3, fractionDigits=2
     * 00340.00 : digits=<34>, decimalAt=3, integerDigits=5, fractionDigits=2
     * 00.0340 : digits=<34>, decimalAt=-1, integerDigits=2, fractionDigits=4
     * Invariants:
     *   integerDigits >= 1
     *   integerDigits >= decimalAt
     *   fractionDigits >= count-decimalAt
     *   integerDigits + fractionDigits >= count
     *   ...
     */
    public String toString() {
        if (CHECK_INVARIANTS) {
            checkInvariants();
        }
        StringBuilder stringRep = new StringBuilder();
        if (isNegative) {
            stringRep.append('-');
        }
        int endDigits = startDigits + count;
        for (int i = 0; i < totalDigits; ++i) {
            if (i == integerDigits) {
                stringRep.append('.');
            }
            int inDigits = i - startDigits;
            if (inDigits >= 0 && inDigits < endDigits) {
                stringRep.append((char)(digitList[start+inDigits] + '0'));
            } else {
                stringRep.append('0');
            }
        }
        return stringRep.toString();
    }

    public void checkInvariants() {
        if (!(integerDigits >= 0)) {
            throw new IllegalArgumentException("FAILED integerDigits >= 0");
        } else if (!(startDigits >= 0)) {
            throw new IllegalArgumentException("FAILED startDigits >= 0");
        } else if (!(totalDigits >= integerDigits)) {
            throw new IllegalArgumentException("FAILED totalDigits >= integerDigits");
        } else if (!(totalDigits >= startDigits + count)) {
            throw new IllegalArgumentException("FAILED totalDigits >= count + startDigits");
        } else if (count > 0) {         // no leading/trailing digits
            if (digitList[start] == 0 || digitList[start+count-1] == 0) {
                throw new IllegalArgumentException("FAILED leading or trailing zero in digits");
            }
        }
    }

    /**
     * Given a string representation of the form DDDDD, DDDDD.DDDDD, set this object's value to it.
     * Allows leading '-'.
     */
    public DigitList2 set(String rep) {
        DigitList2 target = this;
        target.clear();
        // Number of zeros between decimal point and first non-zero digit after
        // decimal point, for numbers < 1.
        // Skip over leading '-'
        int i = 0;
        if (rep.charAt(i) == '-') {
            target.isNegative = true;
            ++i;
        }
        boolean decimalSeen = false;
        int skippedZeros = 0;
        for (; i < rep.length(); ++i) {
            char c = rep.charAt(i);
            c -= '0';
            switch(c) {
            case (char)('.'-'0'):
                if (decimalSeen) {
                    throw new IllegalArgumentException("Multiple decimals");
                }
                decimalSeen = true;
                break;
            case 0:
                ++target.totalDigits;
                if (!decimalSeen) {
                    ++target.integerDigits;
                }
                if (count > 0) {
                    ++skippedZeros;
                }
                break;
            case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
                if (count == 0) {
                    target.startDigits = target.totalDigits;
                }
                ++target.totalDigits;
                if (!decimalSeen) {
                    ++target.integerDigits;
                }
                while (skippedZeros > 0) {
                    digitList[count++] = 0;
                    --skippedZeros;
                }
                skippedZeros = 0;
                digitList[count++] = (byte)c;
                break;
            default:
                throw new IllegalArgumentException("Illegal character");
            }
        }
        if (CHECK_INVARIANTS) {
            checkInvariants();
        }
        return this;
    }
    /**
     * @param digits2
     * @param left
     * @param right
     */
    public void setDigits(byte[] digits2, int left, int right) {
        start = left;
        digitList = digits2;
        count = right - left;
    }
    /**
     * @param i
     * @return
     */
    public int getDigit(int i) {
        return digitList[start + i];
    }
    /**
     * @param i
     * @param j
     * @param k
     */
    public void addDigits(int... digits2) {
        start = 0;
        count = digits2.length;
        for (int i = 0; i < digits2.length; ++i) {
            digitList[i] = (byte) digits2[i];
        }
    }
}
