/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.text.stt.handlers;

import com.ibm.icu.text.stt.CharTypes;
import com.ibm.icu.text.stt.Expert;
import com.ibm.icu.text.stt.Offsets;

/**
 * A base handler for structured text composed of text segments separated by
 * separators where the text segments may include delimited parts within which
 * separators are treated like regular characters and the delimiters may be
 * escaped.
 * <p>
 * This is similar to {@link Delims} except that delimiters can be escaped using
 * the backslash character.
 * <ul>
 * <li>Two consecutive backslashes in a delimited part are treated like one
 * regular character.</li>
 * <li>An ending delimiter preceded by an odd number of backslashes is treated
 * like a regular character within the delimited part.</li>
 * </ul>
 * </p>
 * 
 * @author Matitiahu Allouche, updated by Lina Kemmel
 */
public abstract class DelimsEsc extends Delims {

	public DelimsEsc() {
		// placeholder
	}

	public DelimsEsc(String separator) {
		super(separator);
	}

	/**
	 * Handles the text between start and end delimiters as a token. This method
	 * inserts a directional mark if needed at position
	 * <code>separLocation</code> which corresponds to a start delimiter, and
	 * skips until after the matching end delimiter, ignoring possibly escaped
	 * end delimiters.
	 */
	public int processSpecial(Expert expert, String text, CharTypes charTypes,
			Offsets offsets, int caseNumber, int separLocation) {
		TypeHandler.processSeparator(text, charTypes, offsets, separLocation);
		int location = separLocation + 1;
		char delim = getDelimiters().charAt((caseNumber * 2) - 1);
		while (true) {
			location = text.indexOf(delim, location);
			if (location < 0)
				return text.length();
			int cnt = 0;
			for (int i = location - 1; text.charAt(i) == '\\'; i--) {
				cnt++;
			}
			location++;
			if ((cnt & 1) == 0)
				return location;
		}
	}
}