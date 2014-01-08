/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.text.stt.handlers;

/**
 * Handler adapted to processing property file statements. It expects the
 * following string format:
 * 
 * <pre>
 * name = value
 * </pre>
 */
public class Property extends Single {

	public Property() {
		super("=");
	}
}