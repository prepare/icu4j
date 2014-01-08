/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.text.stt.handlers;

import com.ibm.icu.text.Bidi;
import com.ibm.icu.text.stt.CharTypes;
import com.ibm.icu.text.stt.Expert;

/**
 * Handler adapted to processing arithmetic expressions with a possible
 * right-to-left base direction.
 */
public class Math extends TypeHandler {
	public Math() {
		super("+-/*()=");
	}

	public int getDirection(Expert expert, String text) {
		return getDirection(expert, text, new CharTypes(expert, text));
	}

	/**
	 * @return {@link Expert#DIR_RTL DIR_RTL} if the following conditions are
	 *         satisfied:
	 *         <ul>
	 *         <li>The current locale (as expressed by the environment language)
	 *         is Arabic.</li>
	 *         <li>The first strong character is an Arabic letter.</li>
	 *         <li>If there is no strong character in the text, there is at
	 *         least one Arabic-Indic digit in the text.</li>
	 *         </ul>
	 *         Otherwise, returns {@link Expert#DIR_LTR DIR_LTR}.
	 */
	public int getDirection(Expert expert, String text, CharTypes charTypes) {
		String language = expert.getEnvironment().getLocale().getLanguage();
		if (!language.equals("ar"))
			return Bidi.DIRECTION_LEFT_TO_RIGHT;
		boolean flagAN = false;
		for (int i = 0; i < text.length(); i++) {
			byte charType = charTypes.getBidiTypeAt(i);
			if (charType == CharTypes.AL)
				return Bidi.DIRECTION_RIGHT_TO_LEFT;
			if (charType == CharTypes.L || charType == CharTypes.R)
				return Bidi.DIRECTION_LEFT_TO_RIGHT;
			if (charType == CharTypes.AN)
				flagAN = true;
		}
		if (flagAN)
			return Bidi.DIRECTION_RIGHT_TO_LEFT;
		return Bidi.DIRECTION_LEFT_TO_RIGHT;
	}
}