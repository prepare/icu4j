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
import com.ibm.icu.text.stt.Environment;
import com.ibm.icu.text.stt.Offsets;
import com.ibm.icu.text.stt.ExpertImpl;

/**
 * Generic handler to be used as superclass (base class) for specific structured
 * text handlers.
 * <p>
 * Here are some guidelines about how to write structured text handlers.
 * <ul>
 * <li>Handler instances may be accessed simultaneously by several threads. They
 * should have no instance variables.</li>
 * <li>This class provides common logic in code which can be invoked by any
 * {@link TypeHandler structured text handler}. This common logic uses handler
 * methods to query the characteristics of the specific handler:
 * <ul>
 * <li>the separators which separate the structured text into tokens. See
 * {@link #getSeparators}.</li>
 * <li>the direction which governs the display of tokens one after the other.
 * See {@link #getDirection}.</li>
 * <li>the number of special cases which need to be handled by code specific to
 * that handler. See {@link #getSpecialsCount}.</li>
 * </ul>
 * </li>
 * <li>Before starting deeper analysis of the submitted text, the common logic
 * gives to the handler a chance to shorten the process by invoking its
 * {@link #skipProcessing} method.</li>
 * <li>The common logic then analyzes the text to segment it into tokens
 * according to the appearance of separators (as retrieved using
 * {@link #getSeparators}).</li>
 * <li>If the handler indicated a positive number of special cases as return
 * value from its {@link #getSpecialsCount} method, the common logic will
 * repeatedly invoke the handler's {@link #indexOfSpecial} method to let it
 * signal the presence of special strings which may further delimit the source
 * text.</li>
 * <li>When such a special case is signaled by the handler, the common logic
 * will call the handler's {@link #processSpecial} method to give it the
 * opportunity to handle it as needed. Typical actions that the handler may
 * perform are to add directional marks unconditionally (by calling
 * {@link #insertMark} or conditionally (by calling {@link #processSeparator}).</li>
 * </ul>
 * 
 * @author Matitiahu Allouche, updated by Lina Kemmel
 */
public class TypeHandler {

	final private String separators;

	/**
	 * Creates a new instance of the TypeHandler class.
	 */
	public TypeHandler() {
		separators = "";
	}

	/**
	 * Creates a new instance of the TypeHandler class.
	 * 
	 * @param separators
	 *            string consisting of characters that split the text into
	 *            fragments.
	 */
	public TypeHandler(String separators) {
		this.separators = separators;
	}

	/**
	 * Locates occurrences of special strings within a structured text and
	 * returns their indexes one after the other in successive calls.
	 * <p>
	 * This method is called repeatedly if the number of special cases returned
	 * by {@link #getSpecialsCount} is greater than zero.
	 * </p>
	 * <p>
	 * A handler handling special cases must override this method.
	 * </p>
	 * 
	 * @param expert
	 *            Expert instance through which this handler is invoked. The
	 *            handler can use Expert methods to query items stored in the
	 *            expert instance, like the current {@link Environment
	 *            environment}.
	 * @param text
	 *            the structured text string before addition of any directional
	 *            formatting characters.
	 * @param charTypes
	 *            an object whose methods can be useful to the handler.
	 * @param offsets
	 *            an object whose methods can be useful to the handler.
	 * @param caseNumber
	 *            number of the special case to locate. This number varies from
	 *            1 to the number of special cases returned by
	 *            {@link #getSpecialsCount} for this handler. The meaning of
	 *            this number is internal to the class implementing
	 *            <code>indexOfSpecial</code>.
	 * @param fromIndex
	 *            the index within <code>text</code> to start the search from.
	 * 
	 * @return the position where the start of the special case corresponding to
	 *         <code>caseNumber</code> was located. The method must return the
	 *         first occurrence of whatever identifies the start of the special
	 *         case starting from <code>fromIndex</code>. The method does not
	 *         have to check if this occurrence appears within the scope of
	 *         another special case (e.g. a comment starting delimiter within
	 *         the scope of a literal or vice-versa). <br>
	 *         If no occurrence is found, the method must return -1.
	 * 
	 * @throws IllegalStateException
	 *             If not overridden, this method throws an
	 *             <code>IllegalStateException</code>. This is appropriate
	 *             behavior (and does not need to be overridden) for handlers
	 *             whose number of special cases is zero, which means that
	 *             <code>indexOfSpecial</code> should never be called for them.
	 */
	public int indexOfSpecial(Expert expert, String text, CharTypes charTypes,
			Offsets offsets, int caseNumber, int fromIndex) {
		// This method must be overridden by all subclasses with special cases.
		throw new IllegalStateException(
				"A handler with specialsCount > 0 must have an indexOfSpecial() method.");
	}

	/**
	 * Handles special cases specific to this handler. It is called when a
	 * special case occurrence is located by {@link #indexOfSpecial}.
	 * <p>
	 * If a special processing cannot be completed within a current call to
	 * <code>processSpecial</code> (for instance, a comment has been started in
	 * the current line but its end appears in a following line),
	 * <code>processSpecial</code> should specify a final state by calling
	 * {@link Expert#setState(Object)}. The meaning of this state is internal to
	 * the handler.
	 * <p>
	 * On a later call, <code>processSpecial</code> will be called with
	 * <code>-1</code> for parameter <code>separLocation</code>. It should then
	 * retrieve the last state by calling {@link Expert#getState()} and clear
	 * the state by calling {@link Expert#clearState()}. After that, it should
	 * perform whatever initializations are required depending on the last
	 * state.
	 * </p>
	 * <p>
	 * A handler handling special cases (with a number of special cases greater
	 * than zero) must override this method.
	 * </p>
	 * 
	 * @param expert
	 *            Expert instance through which this handler is invoked. The
	 *            handler can use Expert methods to query items stored in the
	 *            expert instance, like the current {@link Environment
	 *            environment}.
	 * @param text
	 *            the structured text string before addition of any directional
	 *            formatting characters.
	 * @param charTypes
	 *            an object whose methods can be useful to the handler.
	 * @param offsets
	 *            an object whose methods can be useful to the handler.
	 * @param caseNumber
	 *            number of the special case to handle.
	 * @param separLocation
	 *            the position returned by {@link #indexOfSpecial}. After calls
	 *            to {@link Expert#leanToFullText} and other methods of
	 *            {@link Expert} which set a non-null final state,
	 *            <code>processSpecial</code> is called when initializing the
	 *            processing with value of <code>separLocation</code> equal to
	 *            <code>-1</code>.
	 * 
	 * @return the position after the scope of the special case ends. For
	 *         instance, the position after the end of a comment, the position
	 *         after the end of a literal. <br>
	 *         A value greater or equal to the length of <code>text</code> means
	 *         that there is no further occurrence of this case in the current
	 *         structured text.
	 * 
	 * @throws IllegalStateException
	 *             If not overridden, this method throws an
	 *             <code>IllegalStateException</code>. This is appropriate
	 *             behavior (and does not need to be overridden) for handlers
	 *             whose number of special cases is zero, which means that
	 *             <code>processSpecial</code> should never be called for them.
	 */
	public int processSpecial(Expert expert, String text, CharTypes charTypes,
			Offsets offsets, int caseNumber, int separLocation) {
		// This method must be overridden by all subclasses with any special
		// case.
		throw new IllegalStateException(
				"A handler with specialsCount > 0 must have a processSpecial() method.");
	}

	/**
	 * Specifies that a mark character must be added before the character at the
	 * specified position of the <i>lean</i> text when generating the
	 * <i>full</i> text. This method can be called from within
	 * {@link #indexOfSpecial} or {@link #processSpecial} in extensions of
	 * <code>TypeHandler</code>. The mark character will be LRM for structured
	 * text with a LTR base direction, and RLM for structured text with RTL base
	 * direction. The mark character is not added physically by this method, but
	 * its position is noted and will be used when generating the <i>full</i>
	 * text.
	 * 
	 * @param text
	 *            is the structured text string received as parameter to
	 *            <code>indexOfSpecial</code> or <code>processSpecial</code>.
	 * @param charTypes
	 *            is a parameter received by <code>indexOfSpecial</code> or
	 *            <code>processSpecial</code>.
	 * @param offsets
	 *            is a parameter received by <code>indexOfSpecial</code> or
	 *            <code>processSpecial</code>.
	 * @param offset
	 *            position of the character in the <i>lean</i> text. It must be
	 *            a non-negative number smaller than the length of the
	 *            <i>lean</i> text. For the benefit of efficiency, it is better
	 *            to insert multiple marks in ascending order of the offsets.
	 */
	public static final void insertMark(String text, CharTypes charTypes,
			Offsets offsets, int offset) {
		offsets.insertOffset(charTypes, offset);
	}

	/**
	 * Adds a directional mark before a separator if needed for correct display,
	 * depending on the base direction of the text and on the class of the
	 * characters in the <i>lean</i> text preceding and following the separator
	 * itself. This method can be called from within {@link #indexOfSpecial} or
	 * {@link #processSpecial} in extensions of <code>TypeHandler</code>.
	 * <p>
	 * The logic implemented in this method considers the text before
	 * <code>separLocation</code> and the text following it. If, and only if, a
	 * directional mark is needed to insure that the two parts of text will be
	 * laid out according to the base direction, a mark will be added when
	 * generating the <i>full</i> text.
	 * </p>
	 * 
	 * @param text
	 *            is the structured text string received as parameter to
	 *            <code>indexOfSpecial</code> or <code>processSpecial</code>.
	 * @param charTypes
	 *            is a parameter received by <code>indexOfSpecial</code> or
	 *            <code>processSpecial</code>.
	 * @param offsets
	 *            is a parameter received by <code>indexOfSpecial</code> or
	 *            <code>processSpecial</code>.
	 * @param separLocation
	 *            offset of the separator in the <i>lean</i> text. It must be a
	 *            non-negative number smaller than the length of the <i>lean</i>
	 *            text.
	 */
	public static final void processSeparator(String text, CharTypes charTypes,
			Offsets offsets, int separLocation) {
		ExpertImpl.processSeparator(text, charTypes, offsets, separLocation);
	}

	/**
	 * Indicates the separators to use for the current handler. This method is
	 * invoked before starting the processing.
	 * <p>
	 * If no separators are specified, this method returns an empty string.
	 * </p>
	 * 
	 * @param expert
	 *            Expert instance through which this handler is invoked. The
	 *            handler can use Expert methods to query items stored in the
	 *            expert instance, like the current {@link Environment
	 *            environment}.
	 * 
	 * @return a string grouping one-character separators which separate the
	 *         structured text into tokens.
	 */
	public String getSeparators(Expert expert) {
		return separators;
	}

	/**
	 * Indicates the base text direction appropriate for an instance of
	 * structured text. This method is invoked before starting the processing.
	 * <p>
	 * If not overridden, this method returns {@link Expert#DIR_LTR DIR_LTR}.
	 * </p>
	 * 
	 * @param expert
	 *            Expert instance through which this handler is invoked. The
	 *            handler can use Expert methods to query items stored in the
	 *            expert instance, like the current {@link Environment
	 *            environment}.
	 * @param text
	 *            the structured text string to process.
	 * 
	 * @return the base direction of the structured text. This direction may not
	 *         be the same depending on the environment and on whether the
	 *         structured text contains Arabic or Hebrew letters.<br>
	 *         The value returned is either {@link Expert#DIR_LTR DIR_LTR} or
	 *         {@link Expert#DIR_RTL DIR_RTL}.
	 */
	public int getDirection(Expert expert, String text) {
		return Bidi.DIRECTION_LEFT_TO_RIGHT;
	}

	/**
	 * Indicates the base text direction appropriate for an instance of
	 * structured text. This method is invoked before starting the processing.
	 * <p>
	 * If not overridden, this method returns {@link Expert#DIR_LTR DIR_LTR}.
	 * </p>
	 * 
	 * @param expert
	 *            Expert instance through which this handler is invoked. The
	 *            handler can use Expert methods to query items stored in the
	 *            expert instance, like the current {@link Environment
	 *            environment}.
	 * @param text
	 *            is the structured text string to process.
	 * @param charTypes
	 *            is a parameter received by <code>indexOfSpecial</code> or
	 *            <code>processSpecial</code>.
	 * 
	 * @return the base direction of the structured text. This direction may not
	 *         be the same depending on the environment and on whether the
	 *         structured text contains Arabic or Hebrew letters.<br>
	 *         The value returned is either {@link Expert#DIR_LTR DIR_LTR} or
	 *         {@link Expert#DIR_RTL DIR_RTL}.
	 */
	public int getDirection(Expert expert, String text, CharTypes charTypes) {
		return Bidi.DIRECTION_LEFT_TO_RIGHT;
	}

	/**
	 * Indicates the number of special cases handled by the current handler.
	 * This method is invoked before starting the processing. If the number
	 * returned is zero, {@link #indexOfSpecial} and {@link #processSpecial}
	 * will not be invoked.
	 * <p>
	 * If not overridden, this method returns <code>zero</code>.
	 * </p>
	 * 
	 * @param expert
	 *            Expert instance through which this handler is invoked. The
	 *            handler can use Expert methods to query items stored in the
	 *            expert instance, like the current {@link Environment
	 *            environment}.
	 * 
	 * @return the number of special cases for the associated handler. Special
	 *         cases exist for some types of structured text handlers. They are
	 *         implemented by overriding methods
	 *         {@link TypeHandler#indexOfSpecial} and
	 *         {@link TypeHandler#processSpecial}. Examples of special cases are
	 *         comments, literals, or anything which is not identified by a
	 *         one-character separator.
	 * 
	 */
	public int getSpecialsCount() {
		return 0;
	}

	/**
	 * Checks if there is a need for processing structured text. This method is
	 * invoked before starting the processing. If the handler returns
	 * <code>true</code>, no directional formatting characters are added to the
	 * <i>lean</i> text and the processing is shortened.
	 * <p>
	 * If not overridden, this method returns <code>false</code>.
	 * </p>
	 * 
	 * @param expert
	 *            Expert instance through which this handler is invoked. The
	 *            handler can use Expert methods to query items stored in the
	 *            expert instance, like the current {@link Environment
	 *            environment}.
	 * @param text
	 *            is the structured text string to process.
	 * @param charTypes
	 *            is a parameter received by <code>indexOfSpecial</code> or
	 *            <code>processSpecial</code>.
	 * 
	 * @return a flag indicating if there is no need to process the structured
	 *         text to add directional formatting characters.
	 * 
	 */
	public boolean skipProcessing(Expert expert, String text,
			CharTypes charTypes) {
		return false;
	}

}
