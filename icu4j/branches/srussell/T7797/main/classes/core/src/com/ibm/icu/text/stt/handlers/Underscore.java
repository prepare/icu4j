/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.text.stt.handlers;

/**
 * Handler adapted to processing compound names. This type covers names made of
 * one or more parts, separated by underscores:
 * 
 * <pre>
 * part1_part2_part3
 * </pre>
 */
public class Underscore extends TypeHandler {

	public Underscore() {
		super("_");
	}
}