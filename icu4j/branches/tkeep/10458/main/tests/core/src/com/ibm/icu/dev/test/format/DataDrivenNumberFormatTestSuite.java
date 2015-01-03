/*
 *******************************************************************************
 * Copyright (C) 2015, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.format;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.dev.test.TestUtil;

/**
 * A collection of methods to run the data driven number format test suite.
 * Right now supports only those tests that test formatting. May add methods to test parsing
 * later.
 */
public class DataDrivenNumberFormatTestSuite {
    
    /**
     * A data driven test runner.
     */
    public interface Runner {
        /**
         * Runs a single data driven test represented by tuple. On success, returns null.
         * On failure, returns the error.
         */
        String run(NumberFormatTestTuple tuple);
    }
    
    private final TestFmwk fmwk;
    private final Runner formatRunner;
    private final boolean runAllTests;
    private String fileLine = null;
    private int fileLineNumber = 0;
    private String fileTestName = "";
    private NumberFormatTestTuple tuple = new NumberFormatTestTuple();
      
    /**
     * Runs all the format tests in the data driven test suite that should pass.
     * @param fmwk the test framework.
     * @param fileName The name of the test file.
     * @param formatRunner What runs each test.
     */
    static void runFormatSuite(
            com.ibm.icu.dev.test.TestFmwk fmwk, String fileName, Runner formatRunner) {
        new DataDrivenNumberFormatTestSuite(
                fmwk, false, formatRunner).run(fileName);
    }
    
    /**
     * Runs every format test in data driven test suite including those that are known to
     * fail.
     * 
     * @param fmwk the test framework.
     * @param fileName The name of the test file.
     * @param formatRunner What runs each test.
     */
    static void runFormatSuiteIncludingKnownFailures(
            com.ibm.icu.dev.test.TestFmwk fmwk, String fileName, Runner formatRunner) {
        new DataDrivenNumberFormatTestSuite(
                fmwk, true, formatRunner).run(fileName);
    }
    
    private DataDrivenNumberFormatTestSuite(
            TestFmwk fmwk, boolean runAllTests, Runner formatRunner) {
        this.fmwk = fmwk;
        this.runAllTests = runAllTests;
        this.formatRunner = formatRunner;
    }
       
    private void run(String fileName) {
        BufferedReader in = null;
        try {
            in = TestUtil.getDataReader("numberformattestspecification.txt", "UTF-8");
            // read first line and remove BOM if present
            readLine(in);
            if (fileLine != null && fileLine.charAt(0) == '\uFEFF') {
                fileLine = fileLine.substring(1);
            }
            
            int state = 0;
            List<String> columnValues;
            List<String> columnNames = null;
            while (true) {
                if (fileLine == null || fileLine.length() == 0) {
                    if (!readLine(in)) {
                        break;
                    }
                    if (fileLine.isEmpty() && state == 2) {
                        state = 0;
                    }
                    continue;
                }
                if (fileLine.startsWith("//")) {
                    fileLine = null;
                    continue;
                }
                // Initial setup of test.
                if (state == 0) {
                    if (fileLine.startsWith("test ")) {
                        fileTestName = fileLine;
                        tuple = new NumberFormatTestTuple();
                    } else if (fileLine.startsWith("set ")) {
                        setTupleField();
                    } else if(fileLine.startsWith("begin")) {
                        state = 1;
                    } else {
                        showError("Unrecognized verb.");
                        return;
                    }
                // column specification
                } else if (state == 1) {
                    columnNames = splitBy((char) 0x09);
                    state = 2;
                // run the tests
                } else {
                    columnValues = splitBy(columnNames.size(), (char) 0x09);
                    for (int i = 0; i < columnValues.size(); ++i) {
                        setField(columnNames.get(i), columnValues.get(i));
                    }
                    if (runAllTests || !breaksJ()) {
                        String errorMessage = isPass(tuple);
                        if (errorMessage != null) {
                            showError(errorMessage);
                        }
                    }
                }
                fileLine = null;
            }
        } catch (DataDrivenException e) {
            // swallow
        } catch (Exception e) {
           showError(e.getMessage());
           e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean breaksJ() {
       return (tuple.breaks.getValue("").toUpperCase().indexOf('J') != -1);
    }

    private static boolean isSpace(char c) {
        return (c == 0x09 || c == 0x20 || c == 0x3000);
    }
    
    private void setTupleField() {
        List<String> parts = splitBy(3, (char) 0x20);
        if (parts.size() < 3) {
            showError("Set expects 2 parameters");
            throw new DataDrivenException();
        }
        setField(parts.get(1), parts.get(2));
    }
    
    private void setField(String name, String value) {
        try {
            tuple.setField(name,  unescape(value));
        } catch (Exception e) {
            showError("No such field: " + name + ", or bad value: " + value);
            throw new DataDrivenException();
        }
    }
    
    private void showError(String message) {
        fmwk.errln(String.format("line %d: %s", fileLineNumber, escape(message)));
        fmwk.errln("    " + fileTestName);
        fmwk.errln("    " + fileLine);
    }
   
    private List<String> splitBy(char delimiter) {
        return splitBy(Integer.MAX_VALUE, delimiter);
    }
      
    private List<String> splitBy(int max, char delimiter) {
        ArrayList<String> result = new ArrayList<String>();    
        int colIdx = 0;
        int colStart = 0;
        int len = fileLine.length();
        for (int idx = 0; colIdx < max - 1 && idx < len; ++idx) {
            char ch = fileLine.charAt(idx);
            if (ch == delimiter) {
                result.add(
                        fileLine.substring(colStart, idx));
                colStart = idx + 1;
            }
        }
        result.add(fileLine.substring(colStart, len));
        return result;
    }  

    private boolean readLine(BufferedReader in) throws IOException {
        String line = in.readLine();
        if (line == null) {
            fileLine = null;
            return false;
        }
        ++fileLineNumber;
        // Strip trailing comments and spaces
        int idx = line.length();
        for (; idx > 0; idx--) {
            if (!isSpace(line.charAt(idx -1))) {
                break;
            }
        }
        fileLine = idx < line.length() ? line.substring(0, idx) : line;
        return true;
    }
    
    private static char toHexDigit(int digit) {
        if (digit < 10) {
            return (char) (digit | 0x30);
        }
        return (char) ((digit - 9) | 0x40);
    }
    
    // TODO(rocketman): See if there is already a function that converts non printable chars
    // to \\uxxxx
    static String escape(String s) {
        StringBuilder result = new StringBuilder();
        int len = s.length();
        for (int i = 0; i < len; ++i) {
            int ch = (s.charAt(i) & 0xffff);
            if (ch >= 0x20 && ch < 0x80) {
                result.append((char) ch);
            } else {
                result.append("\\u");
                result.append(toHexDigit((ch & 0xf000) >> 12));
                result.append(toHexDigit((ch & 0x0f00) >> 8));
                result.append(toHexDigit((ch & 0x00f0) >> 4));
                result.append(toHexDigit(ch & 0x000f));
            }
        }    
        return result.toString();
    }
    
    // TODO(rocketman): See if there is already a function that resolves \\uxxxx
    static String unescape(String s) {
        StringBuilder result = new StringBuilder();
        int state = 0;
        int len = s.length();
        int codex = 0;
        int digitCount = 0;
        for (int i = 0; i < len; ++i) {
            char ch = s.charAt(i);
            if (state == 0) {
                if (ch == '\\') {
                    state = 1;
                } else {
                    result.append(ch);
                }
            } else if (state == 1) {
                if (ch == 'u') {
                    state = 2;
                } else {
                    result.append('\\');
                    result.append(ch);   
                    state = 0;
                }
            } else if (state == 2) {
                if (ch >= '0' && ch <= '9') {
                    codex <<= 4;
                    codex |= (ch & 0x000f);
                    digitCount++;
                } else if ((ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f')) {
                    codex <<= 4;
                    codex |= (ch & 0x000f) + 9;
                    digitCount++;
                } else {
                    throw new IllegalArgumentException("Must have \\uxxxx where x is a hex digit.");
                }
                if (digitCount == 4) {
                    result.append((char) codex);
                    codex = 0;
                    digitCount = 0;
                    state = 0;
                }
            }
        }
        if (state == 1) {
            result.append('\\');
        } else if (state == 2) {
            throw new IllegalArgumentException("Must have \\uxxxx where x is a hex digit");
        }
        return result.toString();
    }
    
    private String isPass(NumberFormatTestTuple tuple) {
        StringBuilder result = new StringBuilder();
        if (tuple.format.isValue() && tuple.output.isValue()) {
            String errorMessage = formatRunner.run(tuple);
            if (errorMessage != null) {
                result.append(errorMessage);
            }
        } else {
            result.append("At least format and output must be set.");
        }
        if (result.length() > 0) {
            result.append(": ");
            result.append(tuple);
            return result.toString();
        }
        return null;
    }
    
    private class DataDrivenException extends RuntimeException {
    }

}
