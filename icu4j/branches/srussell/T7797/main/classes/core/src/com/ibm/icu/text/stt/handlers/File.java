/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.text.stt.handlers;

/**
 * Handler adapted to processing directory and file paths.
 */
public class File extends TypeHandler {

	public File() {
		super(":/\\.");
	}
}