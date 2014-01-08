/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.text.stt.handlers;

/**
 * Handler adapted to processing comma-delimited lists, such as:
 * 
 * <pre>
 *    part1,part2,part3
 * </pre>
 */
public class Comma extends TypeHandler {
	public Comma() {
		super(",");
	}
}