/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.text.stt.handlers;

/**
 * Handler adapted to processing XPath expressions.
 */
public class XPath extends Delims {

	public XPath() {
		super(" /[]<>=!:@.|()+-*");
	}

	/**
	 * @return 2 as the number of special cases handled by this handler.
	 */
	public int getSpecialsCount() {
		return 2;
	}

	/**
	 * @return apostrophe and quotation mark as delimiters.
	 */
	protected String getDelimiters() {
		return "''\"\"";
	}
}