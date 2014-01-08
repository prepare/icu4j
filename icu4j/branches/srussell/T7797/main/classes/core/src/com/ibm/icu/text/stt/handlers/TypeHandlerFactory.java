/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.text.stt.handlers;

import com.ibm.icu.text.stt.Environment;
import com.ibm.icu.text.stt.ExpertFactory;
import com.ibm.icu.text.stt.Processor;
import com.ibm.icu.text.stt.handlers.TypeHandler;

/**
 * Provides access to supplied structured text handlers.
 * <p>
 * A structured text handler is a subclass of {@link TypeHandler} adapted for a
 * given type of structured text.
 * <p>
 * The constants in this class are identifiers for structured text handlers
 * which are defined and supported "out of the box" by the SText* classes. Text
 * handler identifiers can be used when invoking
 * {@link Processor#processTyped(String, String)}, or when invoking
 * <code>getExpert</code> methods in {@link ExpertFactory}.
 * <p>
 * The {@link #getHandler} method in this class can be used to get a structured
 * text handler reference for one of the pre-defined handlers. Text handler
 * references can be used when invoking
 * {@link ExpertFactory#getStatefulExpert(TypeHandler, Environment)}.
 */
final public class TypeHandlerFactory {

	/**
	 * Structured text handler identifier for property file statements. It
	 * expects the following format:
	 * 
	 * <pre>
	 * name = value
	 * </pre>
	 */
	public static final String PROPERTY = "property";

	/**
	 * Structured text handler identifier for compound names. It expects text to
	 * be made of one or more parts separated by underscores:
	 * 
	 * <pre>
	 * part1_part2_part3
	 * </pre>
	 */
	public static final String UNDERSCORE = "underscore";

	/**
	 * Structured text handler identifier for comma-delimited lists, such as:
	 * 
	 * <pre>
	 *  part1,part2,part3
	 * </pre>
	 */
	public static final String COMMA_DELIMITED = "comma";

	/**
	 * Structured text handler identifier for strings with the following format:
	 * 
	 * <pre>
	 * system(user)
	 * </pre>
	 */
	public static final String SYSTEM_USER = "system";

	/**
	 * Structured text handler identifier for directory and file paths.
	 */
	public static final String FILE = "file";

	/**
	 * Structured text handler identifier for e-mail addresses.
	 */
	public static final String EMAIL = "email";

	/**
	 * Structured text handler identifier for URLs.
	 */
	public static final String URL = "url";

	/**
	 * Structured text handler identifier for regular expressions, possibly
	 * spanning multiple lines.
	 */
	public static final String REGEXP = "regex";

	/**
	 * Structured text handler identifier for XPath expressions.
	 */
	public static final String XPATH = "xpath";

	/**
	 * Structured text handler identifier for Java code, possibly spanning
	 * multiple lines.
	 */
	public static final String JAVA = "java";

	/**
	 * Structured text handler identifier for SQL statements, possibly spanning
	 * multiple lines.
	 */
	public static final String SQL = "sql";

	/**
	 * Structured text handler identifier for arithmetic expressions, possibly
	 * with a RTL base direction.
	 */
	public static final String RTL_ARITHMETIC = "math";

	private static final String[] types = { PROPERTY, UNDERSCORE,
			COMMA_DELIMITED, SYSTEM_USER, FILE, EMAIL, URL, REGEXP, XPATH,
			JAVA, SQL, RTL_ARITHMETIC };

	private static TypeHandler[] handlers = new TypeHandler[types.length];

	/**
	 * Prevents instantiation
	 */
	private TypeHandlerFactory() {
		// placeholder
	}

	/**
	 * Retrieve all supplied types of structured text handler.
	 * 
	 * @return an array of strings, each string identifying a type of structured
	 *         text handler.
	 */
	static public String[] getKnownTypes() {
		return types;
	}

	/**
	 * Obtains a structured text handler of a given type.
	 * 
	 * @param id
	 *            the string identifying a structured text handler.
	 * 
	 * @return a handler of the required type, or <code>null</code> if the type
	 *         is unknown.
	 */
	static public TypeHandler getHandler(String id) {
		int typeIndex;
		for (typeIndex = 0; typeIndex < types.length; typeIndex++)
			if (types[typeIndex].equals(id))
				break;
		if (typeIndex >= types.length)
			return null;
		if (handlers[typeIndex] != null)
			return handlers[typeIndex];
		switch (typeIndex) {
		case 0:
			return handlers[0] = new Property();
		case 1:
			return handlers[1] = new Underscore();
		case 2:
			return handlers[2] = new Comma();
		case 3:
			return handlers[3] = new SystemAndUser();
		case 4:
			return handlers[4] = new File();
		case 5:
			return handlers[5] = new Email();
		case 6:
			return handlers[6] = new Url();
		case 7:
			return handlers[7] = new Regex();
		case 8:
			return handlers[8] = new XPath();
		case 9:
			return handlers[9] = new Java();
		case 10:
			return handlers[10] = new Sql();
		case 11:
			return handlers[11] = new Math();
		}
		return null;
	}

}
