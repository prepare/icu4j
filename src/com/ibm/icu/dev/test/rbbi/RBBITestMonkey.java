/*
 * Created on Apr 23, 2004
 *
 */
package com.ibm.icu.dev.test.rbbi;


// Monkey testing of RuleBasedBreakIterator
import com.ibm.icu.dev.test.*;
import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.text.UTF16;
import com.ibm.icu.text.UnicodeSet;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Locale;


/**
 * @author andy
 *
 * TODO To change the template for this generated type comment go to 
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class RBBITestMonkey extends TestFmwk {
    
	public static void main(String[] args) {
        new RBBITestMonkey().run(args);
	}
    
//
//     classs RBBIMonkeyKind
//
//        Monkey Test for Break Iteration
//        Abstract interface class.   Concrete derived classes independently
//        implement the break rules for different iterator types.
//
//        The Monkey Test itself uses doesn't know which type of break iterator it is
//        testing, but works purely in terms of the interface defined here.
//
    abstract static class RBBIMonkeyKind {
    
        // Return a List of UnicodeSets, representing the character classes used
        //   for this type of iterator.
        abstract  List  charClasses();

        // Set the test text on which subsequent calls to next() will operate
        abstract  void   setText(StringBuffer text);

        // Find the next break postion, starting from the specified position.
        // Return -1 after reaching end of string.
        abstract   int   next(int i);
    }

 
    /**
     * Monkey test subclass for testing Character (Grapheme Cluster) boundaries.
     */
    static class RBBICharMonkey extends RBBIMonkeyKind {
        List                      fSets;

        UnicodeSet                fCRLFSet;
        UnicodeSet                fControlSet;
        UnicodeSet                fExtendSet;
        UnicodeSet                fHangulSet;
        UnicodeSet                fAnySet;

        StringBuffer              fText;


    RBBICharMonkey() {
    	fText       = null;
        fCRLFSet    = new UnicodeSet("[\\r\\n]");
        fControlSet = new UnicodeSet("[[\\p{Zl}\\p{Zp}\\p{Cc}\\p{Cf}]-[\\n]-[\\r]]");
        fExtendSet  = new UnicodeSet("[\\p{Grapheme_Extend}]");
        fHangulSet  = new UnicodeSet(
            "[\\p{Hangul_Syllable_Type=L}\\p{Hangul_Syllable_Type=L}\\p{Hangul_Syllable_Type=T}" +
             "\\p{Hangul_Syllable_Type=LV}\\p{Hangul_Syllable_Type=LVT}]");
        fAnySet     = new UnicodeSet("[\\u0000-\\U0010ffff]");

        fSets       = new ArrayList();
        fSets.add(fCRLFSet);
        fSets.add(fControlSet);
        fSets.add(fExtendSet);
        fSets.add(fHangulSet);
        fSets.add(fAnySet);
     };


    void setText(StringBuffer s) {
        fText = s;        
    }
    
    List charClasses() {
        return fSets;
    }
    
    int next(int i) {
        return nextGC(fText, i);
    }
    }


    /**
     * 
     * Word Monkey Test Class
     *
     * 
     * 
     */
    static class RBBIWordMonkey extends RBBIMonkeyKind {
        List                      fSets;
        StringBuffer              fText;

        UnicodeSet                fKatakanaSet;
        UnicodeSet                fALetterSet;
        UnicodeSet                fMidLetterSet;
        UnicodeSet                fMidNumLetSet;
        UnicodeSet                fMidNumSet;
        UnicodeSet                fNumericSet;
        UnicodeSet                fFormatSet;
        UnicodeSet                fExtendSet;
        UnicodeSet                fOtherSet;

    	
    	RBBIWordMonkey() {
            fSets          = new ArrayList();

    	    fKatakanaSet   = new UnicodeSet("[\\p{script=KATAKANA}\\u30fc\\uff70\\uff9e\\uff9f]");

    	    String ALetterStr = "[[\\p{Alphabetic}\\u05f3]-[\\p{Ideographic}]-[\\p{Script=Thai}]" +
    	                                    "-[\\p{Script=Lao}]-[\\p{Script=Hiragana}]-" +
    	                                    "[\\p{script=KATAKANA}\\u30fc\\uff70\\uff9e\\uff9f]]";

    	    fALetterSet    = new UnicodeSet(ALetterStr);
    	    fMidLetterSet  = new UnicodeSet("[\\u0027\\u00b7\\u05f4\\u2019\\u2027]");
    	    fMidNumLetSet  = new UnicodeSet("[\\u002e\\u003a]");
    	    fMidNumSet     = new UnicodeSet("[\\p{Line_Break=Infix_Numeric}]");
    	    fNumericSet    = new UnicodeSet("[\\p{Line_Break=Numeric}]");
    	    fFormatSet     = new UnicodeSet("[\\p{Format}-\\p{Grapheme_Extend}]");
    	    fExtendSet     = new UnicodeSet("[\\p{Grapheme_Extend}]");
    	    fOtherSet      = new UnicodeSet();

    	    fOtherSet.complement();
    	    fOtherSet.removeAll(fKatakanaSet);
    	    fOtherSet.removeAll(fALetterSet);
    	    fOtherSet.removeAll(fMidLetterSet);
    	    fOtherSet.removeAll(fMidNumLetSet);
    	    fOtherSet.removeAll(fMidNumSet);
    	    fOtherSet.removeAll(fNumericSet);

    	    fSets.add(fALetterSet);
    	    fSets.add(fMidLetterSet);
    	    fSets.add(fMidNumLetSet);
    	    fSets.add(fMidNumSet);
    	    fSets.add(fNumericSet);
    	    fSets.add(fFormatSet);
    	    fSets.add(fOtherSet);
    	}
    	
    	
        List  charClasses() {
         return fSets;  
        }
        
        void   setText(StringBuffer s) { 
            fText = s;        
        }   

        int   next(int prevPos) {  
            int    p0, p1, p2, p3;    	// Indices of the significant code points around the 
            							//   break position being tested.  The candidate break
            							//   location is before p2.
            int     breakPos = -1;
            
            int c0, c1, c2, c3;   // The code points at p0, p1, p2 & p3.
            
            // Prev break at end of string.  return DONE.
            if (prevPos >= fText.length()) {
            	return -1;
            }
            p0 = p1 = p2 = p3 = prevPos;
            c3 = UTF16.charAt(fText, prevPos);
            c0 = c1 = c2 = 0;
            
            
            // Format char after prev break?  Special case, see last Note for Word Boundaries TR.
            // break immdiately after the format char.
            if (breakPos >= 0 && fFormatSet.contains(c3) && breakPos < (fText.length() -1)) {
            	breakPos = UTF16.moveCodePointOffset(fText, breakPos, 1);
            	return breakPos;
}


            // Loop runs once per "significant" character position in the input text.
            for (;;) {
            	// Move all of the positions forward in the input string.
            	p0 = p1;  c0 = c1;
            	p1 = p2;  c1 = c2;
            	p2 = p3;  c2 = c3;
                
            	// Advancd p3 by    (GC Format*)   Rules 3, 4
            	p3 = nextGC(fText, p3);
            	if (p3 == -1 || p3 >= fText.length()) {
            		p3 = fText.length();
            		c3 = 0;
            	} else {
            		c3 = UTF16.charAt(fText, p3);
                    while (fFormatSet.contains(c3)) {
                        p3 = moveIndex32(fText, p3, 1);
                        c3 = 0;
                        if (p3 < fText.length()) {
                            c3 = UTF16.charAt(fText, p3);   
                        }
                    }
            	}

            	if (p1 == p2) {
            		// Still warming up the loop.  (won't work with zero length strings, but we don't care)
            		continue;
            	}
            	if (p2 == fText.length()) {
            		// Reached end of string.  Always a break position.
            		break;
            	}

            	// Rule (5).   ALetter x ALetter
            	if (fALetterSet.contains(c1) &&
            			fALetterSet.contains(c2))  {
            		continue;
            	}
            	
            	// Rule (6)  ALetter  x  (MidLetter | MidNumLet) ALetter
            	//
            	//    Also incorporates rule 7 by skipping pos ahead to position of the
            	//    terminating ALetter.
            	if ( fALetterSet.contains(c1) &&
            			(fMidLetterSet.contains(c2) || fMidNumLetSet.contains(c2)) &&
						fALetterSet.contains(c3)) {
            		continue;
            	}
            	
            	
            	// Rule (7)  ALetter (MidLetter | MidNumLet)  x  ALetter
            	if (fALetterSet.contains(c0) &&
            			(fMidLetterSet.contains(c1) || fMidNumLetSet.contains(c1) ) &&
						fALetterSet.contains(c2)) {
            		continue;
            	}
            	
            	//  Rule (8)    Numeric x Numeric
            	if (fNumericSet.contains(c1) &&
            			fNumericSet.contains(c2))  {
            		continue;
            	}
            	
            	// Rule (9)    ALetter x Numeric
            	if (fALetterSet.contains(c1) &&
            			fNumericSet.contains(c2))  {
            		continue;
            	}

            	// Rule (10)    Numeric x ALetter
            	if (fNumericSet.contains(c1) &&
            			fALetterSet.contains(c2))  {
            		continue;
            	}
            	
            	// Rule (11)   Numeric (MidNum | MidNumLet)  x  Numeric
            	if ( fNumericSet.contains(c0) &&
            			(fMidNumSet.contains(c1) || fMidNumLetSet.contains(c1)) && 
						fNumericSet.contains(c2)) {
            		continue;
            	}
            	
            	// Rule (12)  Numeric x (MidNum | MidNumLet) Numeric
            	if (fNumericSet.contains(c1) &&
            			(fMidNumSet.contains(c2) || fMidNumLetSet.contains(c2)) &&
						fNumericSet.contains(c3)) {
            		continue;
            	}
            	
            	// Rule (13)  Katakana x Katakana
            	if (fKatakanaSet.contains(c1) &&
            			fKatakanaSet.contains(c2))  {
            		continue;
            	}
            	
            	// Rule 14.  Break found here.
            	break;
            }
            
            
            //  Rule 4 fixup,  back up before any trailing
            //         format characters at the end of the word.
            breakPos = p2;
            int  t = nextGC(fText, p1);
            if (t > p1) {
            	breakPos = t;
            }
            return breakPos;
        }
        
    }

 
    static class RBBILineMonkey extends RBBIMonkeyKind {
        List  charClasses() {
         return null;   // TODO:   
        }
        
        void   setText(StringBuffer text) {  // TODO:
        }   

        int   next(int i) {      // TODO:    
            return 0;
        }
    
    }

    
    /**
     * Move an index into a string by n code points.
     *   Similar to UTF16.moveCodePointOffset, but without the exceptions, which were
     *   complicating usage.
     * @param s   a Text string
     * @param i   The starting code unit index into the text string
     * @param amt  The amount to adjust the string by.
     * @return    The adjusted code unit index, pinned to the string's length, or
     *            unchanged if input index was outside of the string.
     */
    static int moveIndex32(StringBuffer s, int i, int amt) {
    	if (i < 0 || i >= s.length()) {
    		return i; 
    	}
        int retVal = UTF16.moveCodePointOffset(s, i, amt);
        return retVal;
    }
    
    
    /**
     * return the index of the next code point in the input text.
     * @param i the preceding index
     * @return
     * @internal
     */
    static int  nextCP(StringBuffer s, int i) {
        if (i == -1) {
            // End of Input indication.  Continue to return end value.
            return -1;
        }
        int  retVal = i + 1;
        if (retVal > s.length()) {
            return -1;
        }
        int  c = UTF16.charAt(s, i);
        if (c >= UTF16.SUPPLEMENTARY_MIN_VALUE) {
            retVal++;
        }
        return retVal;
    }


 
    private static UnicodeSet GC_Control =
         new UnicodeSet("[[:Zl:][:Zp:][:Cc:][:Cf:]-[\\u000d\\u000a]-[:Grapheme_Extend:]]");
    
    private static UnicodeSet GC_Extend = 
        new UnicodeSet("[[:Grapheme_Extend:]]");
    
    private static UnicodeSet GC_L = 
        new UnicodeSet("[[:Hangul_Syllable_Type=L:]]");
    
    private static UnicodeSet GC_V = 
        new UnicodeSet("[[:Hangul_Syllable_Type=V:]]");
    
    private static UnicodeSet GC_T = 
        new UnicodeSet("[[:Hangul_Syllable_Type=T:]]");
    
    private static UnicodeSet GC_LV = 
        new UnicodeSet("[[:Hangul_Syllable_Type=LV:]]");
    
    private static UnicodeSet GC_LVT = 
        new UnicodeSet("[[:Hangul_Syllable_Type=LVT:]]");
    
    /**
     * Find the end of the extent of a grapheme cluster.
     * This is the reference implementation used by the monkey test for comparison
     * with the RBBI results.
     * @param s  The string containing the text to be analyzed  
     * @param i  The index of the start of the grapheme cluster.
     * @return   The index of the first code point following the grapheme cluster
     * @internal
     */
    private static int nextGC(StringBuffer s, int i) {
        if (i >= s.length() || i == -1 ) {
            return -1;
        }

    	int  c = UTF16.charAt(s, i);
        int  pos = i;
    	
    	if (c == 0x0d) {
    	    pos = nextCP(s, i);
            if (pos >= s.length()) {
                return pos;
            }
            c = UTF16.charAt(s, pos);
    		if (c == 0x0a) {
    		    pos = nextCP(s, pos);
            }
            return pos;
    	}
        
    	if (GC_Control.contains(c) || c == 0x0a) {
            pos = nextCP(s, pos);
    		return pos;   
    	}
    	
    	// Little state machine to consume Hangul Syllables
    	int  hangulState = 1;
    	state_loop: for (;;) {
    		switch (hangulState) {
    			case 1:
    				if (GC_L.contains(c)) {
                        hangulState = 2;
    					break;
    				}
    				if (GC_V.contains(c) || GC_LV.contains(c)) {
                        hangulState = 3;
    					break;
    				}
    				if (GC_T.contains(c) || GC_LVT.contains(c)) {
                        hangulState = 4;
    					break;
    				}
    				break state_loop;
    			case 2:
    				if (GC_L.contains(c)) {
    					// continue in state 2.
    					break;
    				}
    				if (GC_V.contains(c) || GC_LV.contains(c)) {
                        hangulState = 3;
    					break;
    				}
                    if (GC_LVT.contains(c)) {
                        hangulState = 4;
                        break;
                    }
    				if (GC_Extend.contains(c)) {
                        hangulState = 5;
    					break;
    				}
    				break state_loop;
    			case 3:
    				if (GC_V.contains(c)) {
    					// continue in state 3;
    					break;
    				}
    				if (GC_T.contains(c)) {
                        hangulState = 4;
    					break;
    				}
    				if (GC_Extend.contains(c)) {
                        hangulState = 5;
    					break;
    				}
    				break state_loop;
    			case 4:
    				if (GC_T.contains(c)) {
    					// continue in state 4
    					break;
    				}
    				if (GC_Extend.contains(c)) {
                        hangulState = 5;
    					break;
    				}
    				break state_loop;
    			case 5:
    				if (GC_Extend.contains(c)) {
    				    hangulState = 5;
    				    break; 
    				}
    				break state_loop;
    		}
    		// We have exited the switch statement, but are still in the loop.
    		// Still in a Hangul Syllable, advance to the next code point.
            pos = nextCP(s, pos); 
            if (pos >= s.length()) {
                break;
            }
    		c = UTF16.charAt(s, pos);    
    	}  // end of loop
    	
    	if (hangulState != 1) {
    		// We found a Hangul.  We're done.
    		return pos;
    	}
    	
    	// Ordinary characters.  Consume one codepoint unconditionally, then any following Extends.
    	for (;;) {
            pos = nextCP(s, pos); 
            if (pos >= s.length()) {
                break;
            }
            c = UTF16.charAt(s, pos);    
            if (GC_Extend.contains(c) == false) {
                break;
            }
    	}
    	
    	return pos;   
    }
    
    
    /**
     * random number generator.  Not using Java's built-in Randoms for two reasons:
     *    1.  Using this code allows obtaining the same sequences as those from the ICU4C monkey test.
     *    2.  We need to get and restore the seed from values occuring in the middle
     *        of a long sequence, to more easily reproduce failing cases.
     */
    private static int m_seed = 1;
    private static int  m_rand()
    {
        m_seed = m_seed * 1103515245 + 12345;
        return (int)(m_seed >>> 16) % 32768;
    }

    
/**
 *  Run a RBBI monkey test.  Common routine, for all break iterator types.
 *    Parameters:
 *       bi      - the break iterator to use
 *       mk      - MonkeyKind, abstraction for obtaining expected results
 *       name    - Name of test (char, word, etc.) for use in error messages
 *       seed    - Seed for starting random number generator (parameter from user)
 *       numIterations
 */
void RunMonkey(BreakIterator  bi, RBBIMonkeyKind mk, String name, int  seed, int numIterations) {
    int              TESTSTRINGLEN = 500;
    StringBuffer     testText         = new StringBuffer();
    int              numCharClasses;
    List             chClasses;
    int[]            expected         = new int[TESTSTRINGLEN*2 + 1];
    int              expectedCount    = 0;
    boolean[]        expectedBreaks   = new boolean[TESTSTRINGLEN*2 + 1];
    boolean[]        forwardBreaks    = new boolean[TESTSTRINGLEN*2 + 1];
    boolean[]        reverseBreaks    = new boolean[TESTSTRINGLEN*2 + 1];
    boolean[]        isBoundaryBreaks = new boolean[TESTSTRINGLEN*2 + 1];
    int              i;
    int              loopCount        = 0;
    boolean          printTestData    = false;
    boolean          printBreaksFromBI = false;

    m_seed = seed;

    numCharClasses = mk.charClasses().size();
    chClasses      = mk.charClasses();

    // Verify that the character classes all have at least one member.
    for (i=0; i<numCharClasses; i++) {
        UnicodeSet s = (UnicodeSet)chClasses.get(i);
        if (s == null || s.size() == 0) {
            errln("Character Class " + i + " is null or of zero size.");
            return;
        }
    }

    //--------------------------------------------------------------------------------------------
    //
    //  Debugging settings.  Comment out everything in the following block for normal operation
    //
    //--------------------------------------------------------------------------------------------
    // numIterations = 20;  
    //RuleBasedBreakIterator_New.fTrace = true;
    //m_seed = -1324359431;
    // TESTSTRINGLEN = 50;
    // printTestData = true;
    // printBreaksFromBI = true;
    // ((RuleBasedBreakIterator_New)bi).dump();
    
    //--------------------------------------------------------------------------------------------
    //
    //  End of Debugging settings.  
    //
    //--------------------------------------------------------------------------------------------
    
    int  dotsOnLine = 0;
    while (loopCount < numIterations || numIterations == -1) {
        if (numIterations == -1 && loopCount % 10 == 0) {
            // If test is running in an infinite loop, display a periodic tic so
            //   we can tell that it is making progress.
            System.out.print(".");
            if (dotsOnLine++ >= 80){
                System.out.println();
                dotsOnLine = 0;
            }
        }
        // Save current random number seed, so that we can recreate the random numbers
        //   for this loop iteration in event of an error.
        seed = m_seed;

        testText.setLength(0);
        // Populate a test string with data.
        if (printTestData) {
            System.out.println("Test Data string ..."); 
        }
        for (i=0; i<TESTSTRINGLEN; i++) {
            int        aClassNum = m_rand() % numCharClasses;
            UnicodeSet classSet  = (UnicodeSet)chClasses.get(aClassNum);
            int        charIdx   = m_rand() % classSet.size();
            int        c         = classSet.charAt(charIdx);
            if (c < 0) {   // TODO:  deal with sets containing strings.
                errln("c < 0");
            }
            UTF16.appendCodePoint(testText, c);
            if (printTestData) {
            	System.out.print(Integer.toHexString(c) + " ");
            }
        }
        if (printTestData) {
        	System.out.println(); 
        }

        Arrays.fill(expected, 0);
        Arrays.fill(expectedBreaks, false);
        Arrays.fill(forwardBreaks, false);
        Arrays.fill(reverseBreaks, false);
        Arrays.fill(isBoundaryBreaks, false);
 
        // Calculate the expected results for this test string.
        mk.setText(testText);
        expectedCount = 0;
        expectedBreaks[0] = true;
        expected[expectedCount ++] = 0;
        int breakPos = 0;
        for (;;) {
            breakPos = mk.next(breakPos);
            if (breakPos == -1) {
                break;
            }
            if (breakPos > testText.length()) {
                errln("breakPos > testText.length()");
            }
            expectedBreaks[breakPos] = true;
            expected[expectedCount ++] = breakPos;
        }

        // Find the break positions using forward iteration
        if (printBreaksFromBI) {
        	System.out.println("Breaks from BI...");  
        }
        bi.setText(testText.toString());
        for (i=bi.first(); i != BreakIterator.DONE; i=bi.next()) {
            if (i < 0 || i > testText.length()) {
                errln(name + " break monkey test: Out of range value returned by breakIterator::next()");
                break;
            }
            if (printBreaksFromBI) {
                System.out.print(Integer.toHexString(i) + " ");
            }
            forwardBreaks[i] = true;
        }
        if (printBreaksFromBI) {
        	System.out.println();
        }

        // Find the break positions using reverse iteration
        for (i=bi.last(); i != BreakIterator.DONE; i=bi.previous()) {
            if (i < 0 || i > testText.length()) {
                errln(name + " break monkey test: Out of range value returned by breakIterator.next()" + name);
                break;
            }
            reverseBreaks[i] = true;
        }

        // Find the break positions using isBoundary() tests.
        for (i=0; i<=testText.length(); i++) {
            isBoundaryBreaks[i] = bi.isBoundary(i);
        }


        // Compare the expected and actual results.
        for (i=0; i<=testText.length(); i++) {
            String errorType = null;
            if  (forwardBreaks[i] != expectedBreaks[i]) {
                errorType = "next()";
            } else if (reverseBreaks[i] != forwardBreaks[i]) {
                errorType = "previous()";
            } else if (isBoundaryBreaks[i] != expectedBreaks[i]) {
                errorType = "isBoundary()";
            }


            if (errorType != null) {
                // Format a range of the test text that includes the failure as
                //  a data item that can be included in the rbbi test data file.

                // Start of the range is the last point where expected and actual results
                //   both agreed that there was a break position.
                int startContext = i;
                int count = 0;
                for (;;) {
                    if (startContext==0) { break; }
                    startContext --;
                    if (expectedBreaks[startContext]) {
                        if (count == 2) break;
                        count ++;
                    }
                }

                // End of range is two expected breaks past the start position.
                int endContext = i + 1;
                int ci;
                for (ci=0; ci<2; ci++) {  // Number of items to include in error text.
                    for (;;) {
                        if (endContext >= testText.length()) {break;}
                        if (expectedBreaks[endContext-1]) { 
                            if (count == 0) break;
                            count --;
                        }
                        endContext ++;
                    }
                }

                // Format looks like   "<data><>\uabcd\uabcd<>\U0001abcd...</data>"
                StringBuffer errorText = new StringBuffer();
                errorText.append("<data>");

                String hexChars = "0123456789abcdef";
                int      c;    // Char from test data
                int      bn;
                for (ci = startContext;  ci <= endContext && ci != -1;  ci = nextCP(testText, ci)) {
                    if (ci == i) {
                        // This is the location of the error.
                        errorText.append("<?>");
                    } else if (expectedBreaks[ci]) {
                        // This a non-error expected break position.
                        errorText.append("<>");
                    }
                    if (ci < testText.length()) {
                    	c = UTF16.charAt(testText, ci);
                    	if (c < 0x10000) {
                    		errorText.append("\\u");
                    		for (bn=12; bn>=0; bn-=4) {
                    			errorText.append(hexChars.charAt((((int)c)>>bn)&0xf));
                    		}
                    	} else {
                    		errorText.append("\\U");
                    		for (bn=28; bn>=0; bn-=4) {
                    			errorText.append(hexChars.charAt((((int)c)>>bn)&0xf));
                    		}
                    	}
                    }
                }
                if (ci == testText.length() && ci != -1) {
                	errorText.append("<>");
                }
                errorText.append("</data>\n");

                // Output the error
                errln(name + " break monkey test error.  " + 
                     (expectedBreaks[i]? "Break expected but not found." : "Break found but not expected.") +
                      "\nOperation = " + errorType + "; random seed = " + seed + ";  buf Idx = " + i + "\n" +
                      errorText);
                break;
            }
        }

        loopCount++;
    }
}

public void TestCharMonkey() {
    
    int        loopCount = 500;
    int        seed      = 1;
    
    if (params.inclusion >= 9) {
        loopCount = 10000;
    }
    
    RBBICharMonkey  m = new RBBICharMonkey();
    BreakIterator   bi = BreakIterator.getCharacterInstance(Locale.US);
    RunMonkey(bi, m, "char", seed, loopCount);
}

public void TestWordMonkey() {
    
    int        loopCount = 500;
    int        seed      = 1;
    
    if (params.inclusion >= 9) {
        loopCount = 10000;
    }
    
    logln("Word Break Monkey Test");
    RBBIWordMonkey  m = new RBBIWordMonkey();
    BreakIterator   bi = BreakIterator.getWordInstance(Locale.US);
    RunMonkey(bi, m, "word", seed, loopCount);
}

public void TestLineMonkey() {
    
    int        loopCount = 500;
    int        seed      = 1;
    String     breakType = "all";
    
    if (params.inclusion >= 9) {
        loopCount = 10000;
    }
    
    logln("Line Break Monkey Test");
    RBBILineMonkey  m = new RBBILineMonkey();
    BreakIterator   bi = BreakIterator.getLineInstance(Locale.US);
    if (params == null) {
        loopCount = 50;
    }
    // RunMonkey(bi, m, "line", seed, loopCount);
}

}

