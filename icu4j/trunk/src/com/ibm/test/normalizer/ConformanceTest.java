/*
************************************************************************
* Copyright (c) 1997-2000, International Business Machines
* Corporation and others.  All Rights Reserved.
************************************************************************
*/

package com.ibm.test.normalizer;

import java.io.*;
import com.ibm.test.*;
import com.ibm.text.*;
import com.ibm.util.Utility;

public class ConformanceTest extends TestFmwk {

    static final String TEST_SUITE_FILE =
        "src/data/unicode/Draft-TestSuite.txt";

    public static void main(String[] args) throws Exception {
        new ConformanceTest().run(args);
    }

    /**
     * Test the conformance of Normalizer to
     * http://www.unicode.org/unicode/reports/tr15/conformance/Draft-TestSuite.txt.
     * This file must be located at the path specified as TEST_SUITE_FILE.
     */
    public void TestConformance() {
        BufferedReader input = null;
        String line = null;
        String[] fields = new String[5];
        StringBuffer buf = new StringBuffer();
        int passCount = 0;
        int failCount = 0;

        try {
            input = new BufferedReader(new FileReader(TEST_SUITE_FILE),64*1024);
            for (int count = 0;;++count) {
                line = input.readLine();
                if (line == null) break;
                if (line.length() == 0) continue;

                // Expect 5 columns of this format:
                // 1E0C;1E0C;0044 0323;1E0C;0044 0323; # <comments>

                // Parse out the comment.
                int i = line.indexOf('#');
                if (i == 0) continue;

                // Parse out the fields
                hexsplit(line, ';', fields, buf);
                if (checkConformance(fields, line)) {
                    ++passCount;
                } else {
                    ++failCount;
                }
            }
        } catch (IOException ex) {
            try {
                input.close();
            } catch (Exception ex2) {}
            ex.printStackTrace();
            throw new IllegalArgumentException("Couldn't read file "
              + ex.getClass().getName() + " " + ex.getMessage()
              + " line = " + line
              );
        }

        if (failCount != 0) {
            errln("Total: " + failCount + " lines failed, " +
                  passCount + " lines passed");
        } else {
            logln("Total: " + passCount + " lines passed");
        }
    }

    /**
     * Verify the conformance of the given line of the Unicode
     * normalization (UTR 15) test suite file.  For each line,
     * there are five columns, corresponding to field[0]..field[4].
     *
     * The following invariants must be true for all conformant implementations
     *  c2 == NFC(c1) == NFC(c2) == NFC(c3)
     *  c3 == NFD(c1) == NFD(c2) == NFD(c3)
     *  c4 == NFKC(c1) == NFKC(c2) == NFKC(c3) == NFKC(c4) == NFKC(c5)
     *  c5 == NFKD(c1) == NFKD(c2) == NFKD(c3) == NFKD(c4) == NFKD(c5)
     *
     * @param field the 5 columns
     * @param line the source line from the test suite file
     * @return true if the test passes
     */
    private boolean checkConformance(String[] field, String line) {
        boolean pass = true;
        for (int i=0; i<5; ++i) {
            if (i<3) {
                String nfc = Normalizer.normalize(field[i], Normalizer.COMPOSE, 0);
                String nfd = Normalizer.normalize(field[i], Normalizer.DECOMP, 0);
                pass &= assertEqual("C", field[i], nfc, field[1], "c2!=C(c" + (i+1));
                pass &= assertEqual("D", field[i], nfd, field[2], "c3!=D(c" + (i+1));
            }
            String nfkc = Normalizer.normalize(field[i],
                                               Normalizer.COMPOSE_COMPAT, 0);
            String nfkd = Normalizer.normalize(field[i],
                                               Normalizer.DECOMP_COMPAT, 0);
            pass &= assertEqual("KC", field[i], nfkc, field[3], "c4!=KC(c" + (i+1));
            pass &= assertEqual("KD", field[i], nfkd, field[4], "c5!=KD(c" + (i+1));
        }
        if (!pass) {
            errln("FAIL: " + line);
        }
        return pass;
    }

    /**
     * @param op name of normalization form, e.g., "KC"
     * @param s string being normalized
     * @param got value received
     * @param exp expected value
     * @param msg description of this test
     * @param return true if got == exp
     */
    private boolean assertEqual(String op, String s, String got,
                                String exp, String msg) {
        if (exp.equals(got)) return true;
        errln(Utility.escape("      " + msg + ") " + op + "(" + s + ")=" + got +
                             ", exp. " + exp));
        return false;
    }

    /**
     * Split a string into pieces based on the given delimiter
     * character.  Then, parse the resultant fields from hex into
     * characters.  That is, "0040 0400;0C00;0899" -> new String[] {
     * "\u0040\u0400", "\u0C00", "\u0899" }.  The output is assumed to
     * be of the proper length already, and exactly output.length
     * fields are parsed.  If there are too few an exception is
     * thrown.  If there are too many the extras are ignored.
     *
     * @param buf scratch buffer
     */
    private static void hexsplit(String s, char delimiter,
                                 String[] output, StringBuffer buf) {
        int i;
        int pos = 0;
        for (i=0; i<output.length; ++i) {
            int delim = s.indexOf(delimiter, pos);
            if (delim < 0) {
                throw new IllegalArgumentException("Missing field in " + s);
            }
            // Our field is from pos..delim-1.
            buf.setLength(0);
            while (pos < delim) {
                if (s.charAt(pos) == ' ') {
                    ++pos;
                } else if (pos+4 > delim) {
                    throw new IllegalArgumentException("Premature eol in " + s);
                } else {
                    int hex = Integer.parseInt(s.substring(pos, pos+4), 16);
                    if (hex < 0 || hex > 0xFFFF) {
                        throw new IllegalArgumentException("Out of range hex " +
                                                           hex + " in " + s);
                    }
                    buf.append((char) hex);
                    pos += 4;
                }
            }
            if (buf.length() < 1) {
                throw new IllegalArgumentException("Empty field " + i + " in " + s);
            }
            output[i] = buf.toString();
            ++pos; // Skip over delim
        }
    }

    // Specific tests for debugging.  These are generally failures taken from
    // the conformance file, but culled out to make debugging easier.

    public void TestCase1() {
        String s = "a\u0315\u0300\u05ae\u0300b";
        String cs = "\u00e0\u05ae\u0300\u0315b"; // expected C(s)
        String t = Normalizer.normalize(s, Normalizer.COMPOSE, 0);
        if (!cs.equals(t)) {
            errln(Utility.escape("FAIL: C(" + s + ")=" + t +
                                 ", exp. " + cs));
        }
    }

    public void TestCase2() {
        String s = "\u1fee";
        String cs = "\u0385"; // expected C(s)
        String kcs = " \u0308\u0301"; // expected KC(s)
        String t = Normalizer.normalize(s, Normalizer.COMPOSE, 0);
        if (!cs.equals(t)) {
            errln(Utility.escape("FAIL: C(" + s + ")=" + t +
                                 ", exp. " + cs));
        }
        t = Normalizer.normalize(s, Normalizer.COMPOSE_COMPAT, 0);
        if (!kcs.equals(t)) {
            errln(Utility.escape("FAIL: KC(" + s + ")=" + t +
                                 ", exp. " + kcs));
        }
    }
}
