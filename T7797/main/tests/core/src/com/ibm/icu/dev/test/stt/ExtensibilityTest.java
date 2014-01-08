/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.dev.test.stt;

import com.ibm.icu.text.stt.handlers.TypeHandler;
import com.ibm.icu.text.stt.handlers.TypeHandlerFactory;

/**
 * Tests contribution of BiDi handlers.
 */
public class ExtensibilityTest extends TestBase {

	public static int main(String[] args) {
		int cntError = 0;
		TypeHandler handler;

		handler = TypeHandlerFactory.getHandler("badtest");
		cntError += assertNull(handler);

		return cntError;
	}

}
