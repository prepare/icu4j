/*
 *******************************************************************************
 *
 *   Copyright (C) 1999-2002, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *
 *******************************************************************************
 */

package com.ibm.icu.lang;

import com.ibm.icu.impl.UCharacterIterator;
import com.ibm.icu.impl.UForwardCharacterIterator;

/**
 * <code>UScriptRun</code> is used to find runs of characters in
 * the same script, as defined in the <code>UScript</code> class.
 * It implements a simple iterator over an array of characters.
 * The iterator will assign <code>COMMON</code> and <code>INHERITED</code>
 * characters to the same script as the preceeding characters. If the
 * COMMON and INHERITED characters are first, they will be assigned to
 * the same script as the following characters.
 *
 * The iterator will try to match paired punctuation. If it sees an
 * opening punctuation character, it will remember the script that
 * was assigned to that character, and assign the same script to the
 * matching closing punctuation.
 *
 * No attempt is made to combine related scripts into a single run. In
 * particular, Hiragana, Katakana, and Han characters will appear in seperate
 * runs.

 * Here is an example of how to iterate over script runs:
 * <pre>
 * void printScriptRuns(char[] text)
 * {
 *     UScriptRun scriptRun = new UScriptRun(text);
 *
 *     while (scriptRun.next()) {
 *         int start  = scriptRun.getScriptStart();
 *         int limit  = scriptRun.getScriptLimit();
 *         int script = scriptRun.getScriptCode();
 *
 *         System.out.println("Script \"" + UScript.getName(script) + "\" from " +
 *                            start + " to " + limit + ".");
 *     }
 *  }
 * </pre>
 */
public final class UScriptRun
{
    /**
     * Puts a copyright in the .class file
     */
    private static final String copyrightNotice
        = "Copyright \u00a91999-2002 IBM Corp.  All rights reserved.";

    /**
     * Construct an empty <code>UScriptRun</code> object. The <code>next()</code>
     * method will return <code>false</code> the first time it is called.
     *
     * @internal
     */
    public UScriptRun()
    {
        char[] nullChars = null;
        
        reset(nullChars, 0, 0);
    }
    
    /**
     * Construct a <code>UScriptRun</code> object which iterates over the
     * characters in the given string.
     *
     * @param text the string of characters over which to iterate.
     *
     * @internal
     */
    public UScriptRun(String text)
    {
        reset (text);
    }
    
    /**
     * Construct a <code>UScriptRun</code> object which iterates over a subrange
     * of the characetrs in the given string.
     *
     * @param text the string of characters over which to iterate.
     * @param start the index of the first character over which to iterate
     * @param count the number of characters over which to iterate
     *
     * @internal
     */
    public UScriptRun(String text, int start, int count)
    {
        reset(text, start, count);
    }

    /**
     * Construct a <code>UScriptRun</code> object which iterates over the given
     * characetrs.
     *
     * @param chars the array of characters over which to iterate.
     *
     * @internal
     */
    public UScriptRun(char[] chars)
    {
        reset(chars);
    }

    /**
     * Construct a <code>UScriptRun</code> object which iterates over a subrange
     * of the given characetrs.
     *
     * @param chars the array of characters over which to iterate.
     * @param start the index of the first character over which to iterate
     * @param count the number of characters over which to iterate
     *
     * @internal
     */
    public UScriptRun(char[] chars, int start, int count)
    {
        reset(chars, start, count);
    }


    /**
     * Reset the iterator to the start of the text.
     *
     * @internal
     */
    public final void reset()
    {
        scriptStart = textStart;
        scriptLimit = textStart;
        scriptCode  = UScript.INVALID_CODE;
        parenSP     = -1;
        
        text.setToStart();
    }

    /**
     * Reset the iterator to iterate over the given range of the text. Throws
     * IllegalArgumentException if the range is outside of the bounds of the
     * character array.
     *
     * @param start the index of the new first character over which to iterate
     * @param count the new number of characters over which to iterate.
     * @exception IllegalArgumentException
     *
     * @internal
     */
    public final void reset(int start, int count)
    throws IllegalArgumentException
    {
        int len = 0;
        
        if (text != null) {
            len = text.getLength();
        }
        
        if (start < 0 || count < 0 || start > len - count) {
            throw new IllegalArgumentException();
        }
        
        textStart = start;
        textLimit = start + count;

        reset();
    }

    /**
     * Reset the iterator to iterate over <code>count</code> characters
     * in <code>chars</code> starting at <code>start</code>. This allows
     * clients to reuse an iterator.
     *
     * @param chars the new array of characters over which to iterate.
     * @param start the index of the first character over which to iterate.
     * @param count the nuber of characters over which to iterate.
     *
     * @internal
     */
    public final void reset(char[] chars, int start, int count)
    {
        if (chars == null) {
            chars = emptyCharArray;
        }
        
        text = UCharacterIterator.getInstance(chars, start, start + count);

        reset(start, count);
    }
    
    /**
     * Reset the iterator to iterate over the characters
     * in <code>chars</code>. This allows clients to reuse an iterator.
     *
     * @param chars the new array of characters over which to iterate.
     *
     * @internal
     */
    public final void reset(char[] chars)
    {
        int length = 0;
        
        if (chars != null) {
            length = chars.length;
        }
        
        reset(chars, 0, length);
    }
    
    /**
     * Reset the iterator to iterate over <code>count</code> characters
     * in <code>text</code> starting at <code>start</code>. This allows
     * clients to reuse an iterator.
     *
     * @param text the new string of characters over which to iterate.
     * @param start the index of the first character over which to iterate.
     * @param count the nuber of characters over which to iterate.
     *
     * @internal
     */
    public final void reset(String text, int start, int count)
    {
        char[] chars = null;
        
        if (text != null) {
            chars = text.toCharArray();
        }
        
        reset(chars, start, count);
    }
    
    /**
     * Reset the iterator to iterate over the characters
     * in <code>text</code>. This allows clients to reuse an iterator.
     *
     * @param text the new string of characters over which to iterate.
     *
     * @internal
     */
    public final void reset(String text)
    {
        int length   = 0;
        
        if (text != null) {
            length = text.length();
        }
        
        reset(text, 0, length);
    }
        


    /**
     * Get the starting index of the current script run.
     *
     * @return the index of the first character in the current script run.
     *
     * @internal
     */
    public final int getScriptStart()
    {
        return scriptStart;
    }

    /**
     * Get the index of the first character after the current script run.
     *
     * @return the index of the first character after the current script run.
     *
     * @internal
     */
    public final int getScriptLimit()
    {
        return scriptLimit;
    }

    /**
     * Get the script code for the script of the current script run.
     *
     * @return the script code for the script of the current script run.
     * @see com.ibm.icu.lang.UScript
     *
     * @internal
     */
    public final int getScriptCode()
    {
        return scriptCode;
    }

    /**
     * Find the next script run. Returns <code>false</code> if there
     * isn't another run, returns <code>true</code> if there is.
     *
     * @return <code>false</code> if there isn't another run, <code>true</code> if there is.
     *
     * @internal
     */
    public final boolean next()
	{
		int startSP  = parenSP;  // used to find the first new open character

		// if we've fallen off the end of the text, we're done
		if (scriptLimit >= textLimit) {
			return false;
		}
    
		scriptCode  = UScript.COMMON;
        scriptStart = scriptLimit;
        
        int ch;
        
        while ((ch = text.nextCodePoint()) != UForwardCharacterIterator.DONE) {
			int sc = UScript.getScript(ch);
			int pairIndex = getPairIndex(ch);

			// Paired character handling:
			//
			// if it's an open character, push it onto the stack.
			// if it's a close character, find the matching open on the
			// stack, and use that script code. Any non-matching open
			// characters above it on the stack will be poped.
			if (pairIndex >= 0) {
				if ((pairIndex & 1) == 0) {
				    parenStack[++parenSP] = new ParenStackEntry(pairIndex, scriptCode);
				} else if (parenSP >= 0) {
					int pi = pairIndex & ~1;

					while (parenSP >= 0 && parenStack[parenSP].pairIndex != pi) {
						parenSP -= 1;
					}

					if (parenSP < startSP) {
						startSP = parenSP;
					}

					if (parenSP >= 0) {
						sc = parenStack[parenSP].scriptCode;
					}
				}
			}

			if (sameScript(scriptCode, sc)) {
				if (scriptCode <= UScript.INHERITED && sc > UScript.INHERITED) {
					scriptCode = sc;

					// now that we have a final script code, fix any open
					// characters we pushed before we knew the script code.
					while (startSP < parenSP) {
						parenStack[++startSP].scriptCode = scriptCode;
					}
				}

				// if this character is a close paired character,
				// pop it from the stack
				if (pairIndex >= 0 && (pairIndex & 1) != 0 && parenSP >= 0) {
					parenSP -= 1;
					startSP -= 1;
				}
			} else {
			    // We've just seen the first character of
			    // the next run. Back over it so we'll see
			    // it again the next time.
			    text.previousCodePoint();
			    break;
			}
		}

        scriptLimit = textStart + text.getIndex();
		return true;
	}

    /**
     * Compare two script codes to see if they are in the same script. If one script is
     * a strong script, and the other is INHERITED or COMMON, it will compare equal.
     *
     * @param scriptOne one of the script codes.
     * @param scriptTwo the other script code.
     * @return <code>true</code> if the two scripts are the same.
     * @see com.ibm.icu.lang.UScript
     */
    private static boolean sameScript(int scriptOne, int scriptTwo)
	{
		return scriptOne <= UScript.INHERITED || scriptTwo <= UScript.INHERITED || scriptOne == scriptTwo;
	}

    /*
     * An internal class which holds entries on the paren stack.
     */
	private static final class ParenStackEntry
	{
		int pairIndex;
		int scriptCode;
		
		public ParenStackEntry(int thePairIndex, int theScriptCode)
		{
		    pairIndex  = thePairIndex;
		    scriptCode = theScriptCode;
		}
    };
    
    private char[] emptyCharArray = {};

    private UCharacterIterator text;

    private int  textStart;
    private int  textLimit;
    
    private int  scriptStart;
    private int  scriptLimit;
    private int  scriptCode;

    private static ParenStackEntry parenStack[] = new ParenStackEntry[128];
    private int  parenSP;

    /**
     * Find the highest bit that's set in a word. Uses a binary search through
     * the bits.
     *
     * @param n the word in which to find the highest bit that's set.
     * @return the bit number (counting from the low order bit) of the highest bit.
     */
    private static final byte highBit(int n)
    {
        if (n <= 0) {
            return -32;
        }

        byte bit = 0;

        if (n >= 1 << 16) {
            n >>= 16;
            bit += 16;
        }

        if (n >= 1 << 8) {
            n >>= 8;
            bit += 8;
        }

        if (n >= 1 << 4) {
            n >>= 4;
            bit += 4;
        }

        if (n >= 1 << 2) {
            n >>= 2;
            bit += 2;
        }

        if (n >= 1 << 1) {
            n >>= 1;
            bit += 1;
        }

        return bit;
    }

    /**
     * Search the pairedChars array for the given character.
     *
     * @param ch the character for which to search.
     * @return the index of the character in the table, or -1 if it's not there.
     */
    private static int getPairIndex(int ch)
	{
		int probe = pairedCharPower;
		int index = 0;

		if (ch >= pairedChars[pairedCharExtra]) {
			index = pairedCharExtra;
		}

		while (probe > (1 << 0)) {
			probe >>= 1;

			if (ch >= pairedChars[index + probe]) {
				index += probe;
			}
		}

		if (pairedChars[index] != ch) {
			index = -1;
		}

		return index;
	}

    private static int pairedChars[] = {
		0x0028, 0x0029, // ascii paired punctuation
		0x003c, 0x003e,
		0x005b, 0x005d,
		0x007b, 0x007d,
		0x00ab, 0x00bb, // guillemets
		0x2018, 0x2019, // general punctuation
		0x201c, 0x201d,
		0x2039, 0x203a,
		0x3008, 0x3009, // chinese paired punctuation
		0x300a, 0x300b,
		0x300c, 0x300d,
		0x300e, 0x300f,
		0x3010, 0x3011,
		0x3014, 0x3015,
		0x3016, 0x3017,
		0x3018, 0x3019,
		0x301a, 0x301b
	};

    private static int pairedCharPower = 1 << highBit(pairedChars.length);
    private static int pairedCharExtra = pairedChars.length - pairedCharPower;
}

