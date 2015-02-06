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
import com.ibm.icu.impl.Utility;

/**
 * A collection of methods to run the data driven number format test suite.
 * Right now supports only those tests that test formatting. May add methods to test parsing
 * later.
 */
public class DataDrivenNumberFormatTestSuite {
    
    /**
     * Base class for code under test.
     */
    public static abstract class CodeUnderTest {
        /**
         *  Runs a single formatting test. On success, returns null.
         *  On failure, returns the error. This implementation just returns null.
         *  Subclasses should override.
         *  @param tuple contains the parameters of the format test.
         */
        public String format(NumberFormatTestTuple tuple) {
            return null;
        }
    }
    
    private final TestFmwk fmwk;
    private final CodeUnderTest codeUnderTest;
    private String fileLine = null;
    private int fileLineNumber = 0;
    private String fileTestName = "";
    private NumberFormatTestTuple tuple = new NumberFormatTestTuple();
      
    /**
     * Runs all the tests in the data driven test suite that should pass.
     * @param fmwk the test framework.
     * @param fileName The name of the test file.
     * @param code indicates the source of code under test. e.g 'J' or 'K'.
     *   'J' for ICU, and 'K' for JDK. Used to exclude tests that are known to fail.
     */
    static void runSuite(
            com.ibm.icu.dev.test.TestFmwk fmwk, String fileName, CodeUnderTest codeUnderTest, char code) {
        new DataDrivenNumberFormatTestSuite(
                fmwk, codeUnderTest).run(fileName, Character.toUpperCase(code));
    }
    
    /**
     * Runs every format test in data driven test suite including those that are known to
     * fail.
     * 
     * @param fmwk the test framework.
     * @param fileName The name of the test file.
     */
    static void runFormatSuiteIncludingKnownFailures(
            com.ibm.icu.dev.test.TestFmwk fmwk, String fileName, CodeUnderTest codeUnderTest) {
        new DataDrivenNumberFormatTestSuite(
                fmwk, codeUnderTest).run(fileName, (char) 0);
    }
    
    private DataDrivenNumberFormatTestSuite(
            TestFmwk fmwk, CodeUnderTest codeUnderTest) {
        this.fmwk = fmwk;
        this.codeUnderTest = codeUnderTest;
    }
       
    private void run(String fileName, char code) {
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
                    int columnNamesSize = columnNames.size();
                    columnValues = splitBy(columnNamesSize, (char) 0x09);
                    int columnValuesSize = columnValues.size();
                    for (int i = 0; i < columnValuesSize; ++i) {
                        setField(columnNames.get(i), columnValues.get(i));
                    }
                    for (int i = columnValuesSize; i < columnNamesSize; ++i) {
                        clearField(columnNames.get(i));
                    }
                    if (code == 0 || !breaks(code)) {
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

    private boolean breaks(char code) {
       return (tuple.breaks.getValue("").toUpperCase().indexOf(code) != -1);
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
            tuple.setField(name,  Utility.unescape(value));
        } catch (Exception e) {
            showError("No such field: " + name + ", or bad value: " + value);
            throw new DataDrivenException();
        }
    }
    
    private void clearField(String name) {
        try {
            tuple.clearField(name);
        } catch (Exception e) {
            showError("Field cannot be clared: " + name);
            throw new DataDrivenException();
        }
    }
    
    private void showError(String message) {
        fmwk.errln(String.format("line %d: %s", fileLineNumber, Utility.escape(message)));
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
    
    private String isPass(NumberFormatTestTuple tuple) {
        StringBuilder result = new StringBuilder();
        if (tuple.format.isValue() && tuple.output.isValue()) {
            String errorMessage = codeUnderTest.format(tuple);
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
