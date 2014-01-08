/*
 *******************************************************************************
 *   Copyright (C) 2001-2014, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 *******************************************************************************
 */

package com.ibm.icu.text.stt;

import com.ibm.icu.text.stt.handlers.TypeHandler;
import com.ibm.icu.text.stt.handlers.TypeHandlerFactory;

/**
 * Provides the most commonly used methods to handle structured text; see the
 * explanations in this class for an introduction to structured text processing.
 * 
 * The SText* classes allow processing bidirectional text with a special
 * structure to ensure its proper presentation.
 * <p>
 * There are various types of structured text. Each type should be handled by a
 * specific <i>type handler</i>. A number of standard type handlers are supplied
 * in class {@link Handlers}.
 * 
 * <h2>Introduction to Structured Text</h2>
 * <p>
 * Bidirectional text offers interesting challenges to presentation systems. For
 * plain text, the Unicode Bidirectional Algorithm (<a
 * href="http://www.unicode.org/reports/tr9/">UBA</a>) generally specifies
 * satisfactorily how to reorder bidirectional text for display.
 * </p>
 * <p>
 * However, all bidirectional text is not necessarily plain text. There are also
 * instances of text structured to follow a given syntax, which should be
 * reflected in the display order. The general algorithm, which has no awareness
 * of these special cases, often gives incorrect results when displaying such
 * structured text.
 * </p>
 * <p>
 * The general idea in handling structured text in this package is to add
 * directional formatting characters at proper locations in the text to
 * supplement the standard algorithm, so that the final result is correctly
 * displayed using the UBA.
 * </p>
 * <p>
 * A class which handles structured text is thus essentially a transformation
 * engine which receives text without directional formatting characters as input
 * and produces as output the same text with added directional formatting
 * characters, hopefully in the minimum quantity which is sufficient to ensure
 * correct display, considering the type of structured text involved.
 * </p>
 * <p>
 * In this package, text without directional formatting characters is called
 * <b><i>lean</i></b> text while the text with added directional formatting
 * characters is called <b><i>full</i></b> text.
 * </p>
 * <p>
 * The class <b>Processor</b> is the main tool for processing structured text.
 * It facilitates handling several types of structured text, each type being
 * handled by a specific {@link TypeHandler type handler}:
 * </p>
 * <ul>
 * <li>property (name=value)</li>
 * <li>compound name (xxx_yy_zzzz)</li>
 * <li>comma delimited list</li>
 * <li>system(user)</li>
 * <li>directory and file path</li>
 * <li>e-mail address</li>
 * <li>URL</li>
 * <li>regular expression</li>
 * <li>Xpath</li>
 * <li>Java code</li>
 * <li>SQL statements</li>
 * <li>RTL arithmetic expressions</li>
 * </ul>
 * <p>
 * For each of these types, an identifier is defined in
 * {@link TypeHandlerFactory}. These identifiers can be used as argument in some
 * methods of <b>Processor</b> to specify the type of handler to apply.
 * </p>
 * <p>
 * A subset of the SText* classes is intended for users who need to process
 * structured text in the most straightforward manner, when the following
 * conditions are satisfied:
 * <ul>
 * <li>There exists an appropriate handler for the type of the structured text.</li>
 * <li>There is no need to specify non-default conditions related to the
 * {@link Environment environment}.</li>
 * <li>The only operations needed are to transform <i>lean</i> text into
 * <i>full</i> text or vice versa.</li>
 * <li>There is no interdependence between the processing of a given string and
 * the processing of preceding or succeeding strings.</li>
 * </ul>
 * <p>
 * For those users, the following classes are sufficient:
 * </p>
 * <ul>
 * <li>Processor</li>
 * <li>{@link TypeHandlerFactory}</li>
 * </ul>
 * <p>
 * Users whose needs go beyond the conditions above, can use the following
 * interface and classes:
 * </p>
 * <ul>
 * <li>{@link Expert}</li>
 * <li>{@link ExpertFactory}</li>
 * <li>{@link Environment}</li>
 * </ul>
 * <p>
 * Developers who want to develop new handlers to support types of structured
 * text not currently supported can use the following classes:
 * </p>
 * <ul>
 * <li>{@link TypeHandler}</li>
 * <li>{@link CharTypes}</li>
 * <li>{@link Offsets}</li>
 * </ul>
 * <p>
 * The source code of class {@link Handlers} can serve as example of how to
 * develop processors for currently unsupported types of structured text.
 * </p>
 * <p>
 * However, users wishing to process the currently supported types of structured
 * text typically don't need to examine this source code.
 * </p>
 * 
 * <h2>Abbreviations used in the documentation of the SText* classes</h2>
 * 
 * <dl>
 * <dt><b>UBA</b>
 * <dd>Unicode Bidirectional Algorithm
 * 
 * <dt><b>Bidi</b>
 * <dd>Bidirectional
 * 
 * <dt><b>GUI</b>
 * <dd>Graphical User Interface
 * 
 * <dt><b>UI</b>
 * <dd>User Interface
 * 
 * <dt><b>LTR</b>
 * <dd>Left to Right
 * 
 * <dt><b>RTL</b>
 * <dd>Right to Left
 * 
 * <dt><b>LRM</b>
 * <dd>Left-to-Right Mark
 * 
 * <dt><b>RLM</b>
 * <dd>Right-to-Left Mark
 * 
 * <dt><b>LRE</b>
 * <dd>Left-to-Right Embedding
 * 
 * <dt><b>RLE</b>
 * <dd>Right-to-Left Embedding
 * 
 * <dt><b>PDF</b>
 * <dd>Pop Directional Formatting
 * </dl>
 * 
 * <p>
 * &nbsp;
 * </p>
 * 
 * <h2>Known Limitations</h2>
 * 
 * <p>
 * The proposed solution is making extensive usage of LRM, RLM, LRE, RLE and PDF
 * directional controls which are invisible but affect the way bidi text is
 * displayed. The following related key points merit special attention:
 * </p>
 * 
 * <ul>
 * <li>Implementations of the UBA on various platforms (e.g., Windows and Linux)
 * are very similar but nevertheless have known differences. Those differences
 * are minor and will not have a visible effect in most cases. However there
 * might be cases in which the same bidi text on two platforms will look
 * different. These differences will surface in Java applications when they use
 * the platform visual components for their UI (e.g., AWT, SWT).</li>
 * 
 * <li>It is assumed that the presentation engine supports LRE, RLE and PDF
 * directional formatting characters.</li>
 * 
 * <li>Because some presentation engines are not strictly conformant to the UBA,
 * the implementation of structured text in this package adds LRM or RLM
 * characters in association with LRE, RLE or PDF in cases where this would not
 * be needed if the presentation engine was fully conformant to the UBA. Such
 * added marks will not have harmful effects on conformant presentation engines
 * and will help less conformant engines to achieve the desired presentation.</li>
 * </ul>
 * 
 * <h2>The Processor class</h2>
 * 
 * This class provides methods to process bidirectional text with a specific
 * structure. The methods in this class are the most straightforward way to add
 * directional formatting characters to the source text to ensure correct
 * presentation, or to remove those characters to restore the original text.
 * 
 * @author Matitiahu Allouche, updated by Lina Kemmel
 */
public final class Processor {

	/**
	 * The default set of separators used to segment a string: dot, colon,
	 * slash, backslash.
	 */
	private static final String defaultSeparators = ".:/\\";

	// left to right mark
	// private static final char LRM = '\u200e';

	// left to right embedding
	// private static final char LRE = '\u202a';

	// right to left embedding
	// private static final char RLE = '\u202b';

	// pop directional format
	// private static final char PDF = '\u202c';

	/**
	 * Prevents instantiation.
	 */
	private Processor() {
		// empty
	}

	/**
	 * Processes the given (<i>lean</i>) text and returns a string with
	 * appropriate directional formatting characters (<i>full</i> text). This is
	 * equivalent to calling {@link #process(String str, String separators)}
	 * with the default set of separators.
	 * <p>
	 * The processing adds directional formatting characters so that
	 * presentation using the Unicode Bidirectional Algorithm will provide the
	 * expected result. The text is segmented according to the provided
	 * separators. Each segment has the Unicode Bidi Algorithm applied to it,
	 * but as a whole, the string is oriented left to right.
	 * </p>
	 * <p>
	 * For example, a file path such as <tt>d:\myfolder\FOLDER\MYFILE.java</tt>
	 * (where capital letters indicate RTL text) should render as
	 * <tt>d:\myfolder\REDLOF\ELIFYM.java</tt>.
	 * </p>
	 * 
	 * @param str
	 *            the <i>lean</i> text to process.
	 * 
	 * @return the processed string (<i>full</i> text).
	 * 
	 * @see #deprocess(String)
	 */
	public static String process(String str) {
		return process(str, defaultSeparators);
	}

	/**
	 * Processes a string that has a particular semantic meaning to render it
	 * correctly on bidi locales. For more details, see {@link #process(String)}
	 * .
	 * 
	 * @param str
	 *            the <i>lean</i> text to process.
	 * @param separators
	 *            characters by which the string will be segmented.
	 * 
	 * @return the processed string (<i>full</i> text).
	 * 
	 * @see #deprocess(String)
	 */
	public static String process(String str, String separators) {
		if ((str == null) || (str.length() <= 1))
			return str;

		// do not process a string that has already been processed.
		if (str.charAt(0) == Expert.LRE
				&& str.charAt(str.length() - 1) == Expert.PDF)
			return str;

		Environment env = new Environment(null, false,
				Environment.ORIENT_UNKNOWN);
		if (!env.isProcessingNeeded())
			return str;
		// do not process a string if all the following conditions are true:
		// a) it has no RTL characters
		// b) it starts with a LTR character
		// c) it ends with a LTR character or a digit
		boolean isStringBidi = false;
		int strLength = str.length();
		char c;
		for (int i = 0; i < strLength; i++) {
			c = str.charAt(i);
			if (((c >= 0x05d0) && (c <= 0x07b1))
					|| ((c >= 0xfb1d) && (c <= 0xfefc))) {
				isStringBidi = true;
				break;
			}
		}
		while (!isStringBidi) {
			if (!Character.isLetter(str.charAt(0)))
				break;
			c = str.charAt(strLength - 1);
			if (!Character.isDigit(c) && !Character.isLetter(c))
				break;
			return str;
		}

		if (separators == null)
			separators = defaultSeparators;

		// make sure that LRE/PDF are added around the string
		TypeHandler handler = new TypeHandler(separators);
		Expert expert = ExpertFactory.getStatefulExpert(handler, env);
		return expert.leanToFullText(str);
	}

	/**
	 * Processes a string that has a particular semantic meaning to render it
	 * correctly on bidi locales. For more details, see {@link #process(String)}
	 * .
	 * 
	 * @param str
	 *            the <i>lean</i> text to process.
	 * @param textType
	 *            an identifier for the structured text handler appropriate for
	 *            the type of the text submitted. It must be one of the
	 *            identifiers defined in {@link TypeHandlerFactory}.
	 * 
	 * @return the processed string (<i>full</i> text).
	 * 
	 * @see #deprocessTyped
	 */
	public static String processTyped(String str, String textType) {
		if ((str == null) || (str.length() <= 1))
			return str;

		// do not process a string that has already been processed.
		char c = str.charAt(0);
		if (((c == Expert.LRE) || (c == Expert.RLE))
				&& str.charAt(str.length() - 1) == Expert.PDF)
			return str;

		// make sure that LRE/PDF are added around the string
		Environment env = new Environment(null, false,
				Environment.ORIENT_UNKNOWN);
		if (!env.isProcessingNeeded())
			return str;
		Expert expert = ExpertFactory.getExpert(textType, env);
		return expert.leanToFullText(str);
	}

	/**
	 * Removes directional formatting characters in the given string.
	 * 
	 * @param str
	 *            string with directional characters to remove (<i>full</i>
	 *            text).
	 * 
	 * @return string without directional formatting characters (<i>lean</i>
	 *         text).
	 */
	public static String deprocess(String str) {
		if ((str == null) || (str.length() <= 1))
			return str;
		Environment env = new Environment(null, false,
				Environment.ORIENT_UNKNOWN);
		if (!env.isProcessingNeeded())
			return str;

		StringBuffer buf = new StringBuffer();
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			char c = str.charAt(i);
			switch (c) {
			case Expert.LRM:
				continue;
			case Expert.LRE:
				continue;
			case Expert.PDF:
				continue;
			default:
				buf.append(c);
			}
		}
		return buf.toString();
	}

	/**
	 * Removes directional formatting characters in the given string.
	 * 
	 * @param str
	 *            string with directional characters to remove (<i>full</i>
	 *            text).
	 * @param textType
	 *            an identifier for the structured text handler appropriate for
	 *            the type of the text submitted. It must be one of the
	 *            identifiers defined in {@link TypeHandlerFactory}.
	 * 
	 * @return string without directional formatting characters (<i>lean</i>
	 *         text).
	 * 
	 * @see #processTyped(String, String)
	 */
	public static String deprocessTyped(String str, String textType) {
		if ((str == null) || (str.length() <= 1))
			return str;

		// make sure that LRE/PDF are added around the string
		Environment env = new Environment(null, false,
				Environment.ORIENT_UNKNOWN);
		if (!env.isProcessingNeeded())
			return str;
		Expert expert = ExpertFactory.getExpert(textType, env);
		return expert.fullToLeanText(str);
	}

	/**
	 * Returns a string containing all the default separator characters to be
	 * used to segment a given string.
	 * 
	 * @return string containing all separators.
	 */
	public static String getDefaultSeparators() {
		return defaultSeparators;
	}

}
