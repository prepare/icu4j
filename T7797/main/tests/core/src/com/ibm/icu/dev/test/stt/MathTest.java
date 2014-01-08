/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.dev.test.stt;

import com.ibm.icu.text.stt.Environment;
import com.ibm.icu.text.stt.Expert;
import com.ibm.icu.text.stt.ExpertFactory;
import com.ibm.icu.text.stt.handlers.TypeHandlerFactory;
import com.ibm.icu.util.ULocale;

/**
 * Tests RTL arithmetic
 */
public class MathTest extends TestBase {

	int cntError;

	private Environment envLTR = new Environment(new ULocale("ar"), false,
			Environment.ORIENT_LTR);
	private Environment envRTL = new Environment(new ULocale("ar"), false,
			Environment.ORIENT_RTL);

	private Expert expertLTR = ExpertFactory.getExpert(
			TypeHandlerFactory.RTL_ARITHMETIC, envLTR);
	private Expert expertRTL = ExpertFactory.getExpert(
			TypeHandlerFactory.RTL_ARITHMETIC, envRTL);

	private void verifyOneLine(String msg, String data, String resLTR,
			String resRTL) {
		String lean = toUT16(data);
		String fullLTR = expertLTR.leanToFullText(lean);
		cntError += assertEquals(msg + " LTR - ", resLTR, toPseudo(fullLTR));

		String fullRTL = expertRTL.leanToFullText(lean);
		cntError += assertEquals(msg + " RTL - ", resRTL, toPseudo(fullRTL));
	}

	public static int main(String[] args) {
		MathTest test = new MathTest();

		test.verifyOneLine("Math #0", "", "", "");
		test.verifyOneLine("Math #1", "1+ABC", "1+ABC", ">@1+ABC@^");
		test.verifyOneLine("Math #2", "2+ABC-DEF", "2+ABC@-DEF",
				">@2+ABC@-DEF@^");
		test.verifyOneLine("Math #3", "A+3*BC/DEF", "A@+3*BC@/DEF",
				">@A@+3*BC@/DEF@^");
		test.verifyOneLine("Math #4", "4+ABC/DEF", "4+ABC@/DEF",
				">@4+ABC@/DEF@^");

		test.verifyOneLine("Math #5", "5#BC", "<&5#BC&^", "5#BC");
		test.verifyOneLine("Math #6", "6#BC-DE", "<&6#BC-DE&^", "6#BC-DE");
		test.verifyOneLine("Math #7", "7#BC+DE", "<&7#BC+DE&^", "7#BC+DE");
		test.verifyOneLine("Math #8", "8#BC*DE", "<&8#BC*DE&^", "8#BC*DE");
		test.verifyOneLine("Math #9", "9#BC/DE", "<&9#BC/DE&^", "9#BC/DE");
		test.verifyOneLine("Math #10", "10ab+cd-ef", "10ab+cd-ef",
				">@10ab+cd-ef@^");

		return test.cntError;
	}
}
