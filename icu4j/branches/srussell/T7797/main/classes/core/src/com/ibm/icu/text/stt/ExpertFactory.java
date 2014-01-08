/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.text.stt;

import java.util.HashMap;
import java.util.Map;

import com.ibm.icu.text.stt.ExpertImpl;
import com.ibm.icu.text.stt.Processor;
import com.ibm.icu.text.stt.handlers.TypeHandler;
import com.ibm.icu.text.stt.handlers.TypeHandlerFactory;

/**
 * Obtains Expert instances. An {@link Expert} instance (called in short an
 * "expert") provides the advanced methods to process a certain type of
 * structured text, and is thus related to a specific {@link TypeHandler
 * structured text type handler}. There are two kinds of experts:
 * <ul>
 * <li>stateful, obtained by calling {@link #getStatefulExpert}.</li>
 * <li>not stateful, obtained by calling {@link #getExpert}.</li>
 * </ul>
 * <p>
 * Only the stateful kind can remember the state established by a call to a text
 * processing method and transmit it as initial state in the next call to a text
 * processing method.
 * <p>
 * In other words, the methods {@link Expert#getState()},
 * {@link Expert#setState} and {@link Expert#clearState()} of {@link Expert} are
 * inoperative for experts which are not stateful.
 * <p>
 * Using a stateful expert is more resource intensive, thus not stateful experts
 * should be used when feasible.
 * 
 * @author Matitiahu Allouche, updated by Lina Kemmel
 * 
 */
final public class ExpertFactory {

	/**
	 * The default set of separators used to segment a string: dot, colon,
	 * slash, backslash.
	 */
	private static final String defaultSeparators = Processor
			.getDefaultSeparators();

	static private Map<String, Expert> sharedDefaultExperts = new HashMap<String, Expert>();

	// String type -> map of { environment -> expert }
	static private Map<String, Map<Environment, Expert>> sharedExperts = new HashMap<String, Map<Environment, Expert>>();

	static private Expert defaultExpert;

	private ExpertFactory() {
		// prevents instantiation
	}

	/**
	 * Obtains a Expert instance for processing structured text with a default
	 * type handler segmenting the text according to default separators. This
	 * expert instance does not handle states.
	 * 
	 * @return the Expert instance.
	 * @see Processor#getDefaultSeparators()
	 */
	static public Expert getExpert() {
		if (defaultExpert == null) {
			TypeHandler handler = new TypeHandler(defaultSeparators);
			defaultExpert = new ExpertImpl(handler, Environment.DEFAULT, false);
		}
		return defaultExpert;
	}

	/**
	 * Obtains a Expert instance for processing structured text with the
	 * specified type handler. This expert instance does not handle states.
	 * 
	 * @param type
	 *            the identifier for the required type handler. This identifier
	 *            must be one of those listed in {@link TypeHandlerFactory} .
	 * @return the Expert instance.
	 * @throws IllegalArgumentException
	 *             if <code>type</code> is not a known type identifier.
	 */
	static public Expert getExpert(String type) {
		Expert expert;
		synchronized (sharedDefaultExperts) {
			expert = sharedDefaultExperts.get(type);
			if (expert == null) {
				TypeHandler handler = TypeHandlerFactory.getHandler(type);
				if (handler == null)
					throw new IllegalArgumentException("Invalid type argument");
				expert = new ExpertImpl(handler, Environment.DEFAULT, false);
				sharedDefaultExperts.put(type, expert);
			}
		}
		return expert;
	}

	/**
	 * Obtains a Expert instance for processing structured text with the
	 * specified type handler and the specified environment. This expert
	 * instance does not handle states.
	 * 
	 * @param type
	 *            the identifier for the required type handler. This identifier
	 *            must be one of those listed in {@link TypeHandlerFactory} .
	 * @param environment
	 *            the current environment, which may affect the behavior of the
	 *            expert. This parameter may be specified as <code>null</code>,
	 *            in which case the {@link Environment#DEFAULT} environment
	 *            should be assumed.
	 * @return the Expert instance.
	 * @throws IllegalArgumentException
	 *             if <code>type</code> is not a known type identifier.
	 */
	static public Expert getExpert(String type, Environment environment) {
		Expert expert;
		if (environment == null)
			environment = Environment.DEFAULT;
		synchronized (sharedExperts) {
			Map<Environment, Expert> experts = sharedExperts.get(type);
			if (experts == null) {
				experts = new HashMap<Environment, Expert>();
				sharedExperts.put(type, experts);
			}
			expert = (Expert) experts.get(environment);
			if (expert == null) {
				TypeHandler handler = TypeHandlerFactory.getHandler(type);
				if (handler == null)
					throw new IllegalArgumentException("Invalid type argument");
				expert = new ExpertImpl(handler, environment, false);
				experts.put(environment, expert);
			}
		}
		return expert;
	}

	/**
	 * Obtains a Expert instance for processing structured text with the
	 * specified type handler. This expert instance can handle states.
	 * 
	 * @param type
	 *            the identifier for the required type handler. This identifier
	 *            must be one of those listed in {@link TypeHandlerFactory} .
	 * @return the Expert instance.
	 * @throws IllegalArgumentException
	 *             if <code>type</code> is not a known type identifier.
	 */
	static public Expert getStatefulExpert(String type) {
		return getStatefulExpert(type, Environment.DEFAULT);
	}

	/**
	 * Obtains a Expert instance for processing structured text with the
	 * specified type handler and the specified environment. This expert
	 * instance can handle states.
	 * 
	 * @param type
	 *            the identifier for the required type handler. This identifier
	 *            must be one of those listed in {@link TypeHandlerFactory} .
	 * @param environment
	 *            the current environment, which may affect the behavior of the
	 *            expert. This parameter may be specified as <code>null</code>,
	 *            in which case the {@link Environment#DEFAULT} environment
	 *            should be assumed.
	 * @return the Expert instance.
	 * @throws IllegalArgumentException
	 *             if <code>type</code> is not a known type identifier.
	 */
	static public Expert getStatefulExpert(String type, Environment environment) {
		TypeHandler handler = TypeHandlerFactory.getHandler(type);
		if (handler == null)
			throw new IllegalArgumentException("Invalid type argument");
		return getStatefulExpert(handler, environment);
	}

	/**
	 * Obtains a Expert instance for processing structured text with the
	 * specified type handler and the specified environment. This expert
	 * instance can handle states.
	 * 
	 * @param handler
	 *            the type handler instance. It may have been obtained using
	 *            {@link TypeHandlerFactory#getHandler(String)} or by
	 *            instantiating a type handler.
	 * @param environment
	 *            the current environment, which may affect the behavior of the
	 *            expert. This parameter may be specified as <code>null</code>,
	 *            in which case the {@link Environment#DEFAULT} environment
	 *            should be assumed.
	 * @return the Expert instance.
	 * @throws IllegalArgumentException
	 *             if <code>type</code> is not a known type identifier.
	 */
	static public Expert getStatefulExpert(TypeHandler handler,
			Environment environment) {
		if (environment == null)
			environment = Environment.DEFAULT;
		return new ExpertImpl(handler, environment, true);
	}

}
