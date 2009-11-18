/*
 *******************************************************************************
 * Copyright (C) 2007-2009, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 * Copyright (C) 2009 , Yahoo! Inc.                                            *
 *******************************************************************************
 */

package com.ibm.icu.text;

import java.text.Format;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.ibm.icu.impl.UCharacterProperty;

/**
  * <p><code>SelectFormat</code> supports the creation of  internationalized
  * messages by selecting phrases based on keywords. The pattern  specifies
  * how to map keywords to phrases and provides a default phrase. The
  * object provided to the format method is a string that's matched
  * against the keywords. If there is a match, the corresponding phrase
  * is selected; otherwise, the default phrase is used.</p>
  *
  * <h4>Using <code>SelectFormat</code> for Gender Agreement</h4>
  *
  * <p>The main use case for the select format is gender based  inflection.
  * When names or nouns are inserted into sentences, their gender can  affect pronouns,
  * verb forms, articles, and adjectives. Special care needs to be
  * taken for the case where the gender cannot be determined.
  * The impact varies between languages:</p>
  *
  * <ul>
  * <li>English has three genders, and unknown gender is handled as a  special
  * case. Names use the gender of the named person (if known), nouns  referring
  * to people use natural gender, and inanimate objects are usually  neutral.
  * The gender only affects pronouns: "he", "she", "it", "they".
  *
  * <li>German differs from English in that the gender of nouns is  rather
  * arbitrary, even for nouns referring to people ("Mädchen", girl, is  neutral).
  * The gender affects pronouns ("er", "sie", "es"), articles ("der",  "die",
  * "das"), and adjective forms ("guter Mann", "gute Frau", "gutes  Mädchen").
  *
  * <li>French has only two genders; as in German the gender of nouns
  * is rather arbitrary – for sun and moon, the genders
  * are the opposite of those in German. The gender affects
  * pronouns ("il", "elle"), articles ("le", "la"),
  * adjective forms ("bon", "bonne"), and sometimes
  * verb forms ("allé", "allée").
  *
  * <li>Polish distinguishes five genders (or noun classes),
  * human masculine, animate non-human masculine, inanimate masculine,
  * feminine, and neuter.
  * </ul>
  *
  * <p>Some other languages have noun classes that are not related to  gender,
  * but similar in grammatical use.
  * Some African languages have around 20 noun classes.</p>
  *
  * <p>To enable localizers to create sentence patterns that take their
  * language's gender dependencies into consideration, software has to  provide
  * information about the gender associated with a noun or name to
  * <code>MessageFormat</code>.
  * Two main cases can be distinguished:</p>
  *
  * <ul>
  * <li>For people, natural gender information should be maintained  for each person.
  * The keywords "male", "female", "mixed" (for groups of people)
  * and "unknown" are used.
  *
  * <li>For nouns, grammatical gender information should be maintained  for
  * each noun and per language, e.g., in resource bundles.
  * The keywords "masculine", "feminine", and "neuter" are commonly  used,
  * but some languages may require other keywords.
  * </ul>
  *
  * <p>The resulting keyword is provided to <code>MessageFormat</code>  as a
  * parameter separate from the name or noun it's associated with. For  example,
  * to generate a message such as "Jean went to Paris", three separate  arguments
  * would be provided: The name of the person as argument 0, the  gender of
  * the person as argument 1, and the name of the city as argument 2.
  * The sentence pattern for English, where the gender of the person has
  * no impact on this simple sentence, would not refer to argument 1  at all:</p>
  *
  * <pre>{0} went to {2}.</pre>
  *
  * <p>The sentence pattern for French, where the gender of the person affects
  * the form of the participle, uses a select format based on argument 1:</p>
  *
  * <pre>{0} est {1, select, female {allée} other {allé}} à {2}.</pre>
  *
  * <p>Patterns can be nested, so that it's possible to handle  interactions of
  * number and gender where necessary. For example, if the above  sentence should
  * allow for the names of several people to be inserted, the  following sentence
  * pattern can be used (with argument 0 the list of people's names,  
  * argument 1 the number of people, argument 2 their combined gender, and  
  * argument 3 the city name):</p>
  *
  * <pre>{0} {1, plural, 
  * one {est {2, select, female {allée} other  {allé}}}
  * other {sont {2, select, female {allées} other {allés}}}
  * }à {3}.</pre>
  *
  * <h4>Patterns and Their Interpretation</h4>
  *
  * <p>The <code>SelectFormat</code> pattern text defines the phrase  output
  * for each user-defined keyword.
  * The pattern is a sequence of <code><i>keyword</i>{<i>phrase</i>}</code>
  * clauses, separated by white space characters.
  * Each clause assigns the phrase <code><i>phrase</i></code>
  * to the user-defined <code><i>keyword</i></code>.</p>
  *
  * <p>Keywords must match the pattern [a-zA-Z][a-zA-Z0-9_-]*; keywords
  * that don't match this pattern result in the error code
  * <code>U_ILLEGAL_CHARACTER</code>.
  * You always have to define a phrase for the default keyword
  * <code>other</code>; this phrase is returned when the keyword  
  * provided to
  * the <code>format</code> method matches no other keyword.
  * If a pattern does not provide a phrase for <code>other</code>, the  method
  * it's provided to returns the error  <code>U_DEFAULT_KEYWORD_MISSING</code>.
  * If a pattern provides more than one phrase for the same keyword, the
  * error <code>U_DUPLICATE_KEYWORD</code> is returned.
  * <br/>
  * Spaces between <code><i>keyword</i></code> and
  * <code>{<i>phrase</i>}</code>  will be ignored; spaces within
  * <code>{<i>phrase</i>}</code> will be preserved.<p>
  *
  * <p>The phrase for a particular select case may contain other message
  * format patterns. <code>SelectFormat</code> preserves these so that  you
  * can use the strings produced by <code>SelectFormat</code> with other
  * formatters. If you are using <code>SelectFormat</code> inside a
  * <code>MessageFormat</code> pattern, <code>MessageFormat</code> will
  * automatically evaluate the resulting format pattern.
  * Thus, curly braces (<code>{</code>, <code>}</code>) are <i>only</i> allowed
  * in phrases to define a nested format pattern.</p>
  *
  * <p>Example:
  * <pre>
  * MessageFormat msgFmt = new MessageFormat("{0} est " +
  *     "{1, select, female {allée} other {allé}} à Paris.",
  *     new ULocale("fr"));
  * Object args[] = {"Kirti","female"};
  * System.out.println(msgFmt.format(args));
  * </pre>
  * <pre>
  * Produces the output:<br/>
  * <code>Input is Kirti,female and result is: Kirti est allée à  Paris.</code>
  * </pre>
  */

public class SelectFormat extends Format {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new <code>SelectFormat</code> 
     * @throws UnsupportedOperationException
     * @draft ICU 4.4
     */
    public SelectFormat() {
        throw new UnsupportedOperationException("Constructor SelectFormat() is not implemented yet.");
    }

    /**
     * Creates a new <code>SelectFormat</code> for a given pattern string.
     * @param  pattern the pattern for this <code>SelectFormat</code>.
     * @throws UnsupportedOperationException
     * @draft ICU 4.4
     */
    public SelectFormat(String pattern) {
        throw new UnsupportedOperationException("Constructor SelectFormat(String) is not implemented yet.");
    }

    /**
     * Sets the pattern used by this select format.
     * Patterns and their interpretation are specified in the class description.
     *
     * @param pattern the pattern for this select format.
     * @throws UnsupportedOperationException
     * @draft ICU 4.4
     */
    public void applyPattern(String pattern) {
        throw new UnsupportedOperationException("SelectFormat.applyPattern(String) is not implemented yet.");
    }

    /**
     * Returns the pattern for this <code>SelectFormat</code>
     *
     * @return the pattern string
     * @throws UnsupportedOperationException
     * @draft ICU 4.4
     */
    public String toPattern() {
        throw new UnsupportedOperationException("SelectFormat.toPattern() is not implemented yet.");
    }

    /**
     * Formats a select message for a given keyword.
     *
     * @param keyword a keyword for which the select message should be formatted.
     * @return the string containing the formatted select message.
     * @throws UnsupportedOperationException
     * @draft ICU 4.4
     */
    public final String format(String keyword) {
        throw new UnsupportedOperationException("SelectFormat.format(String) is not implemented yet.");
    }

    /**
     * Formats a select message for a given keyword and appends the formatted
     * message to the given <code>StringBuffer</code>.
     * @param keyword a keyword for which the select message should be formatted.
     * @param toAppendTo the formatted message will be appended to this
     *        <code>StringBuffer</code>.
     * @param pos will be ignored by this method.
     * @return the string buffer passed in as toAppendTo, with formatted text
     *         appended.
     * @throws UnsupportedOperationException
     * @draft ICU 4.4
     */
    public StringBuffer format(Object keyword, StringBuffer toAppendTo,
            FieldPosition pos) {
        throw new UnsupportedOperationException("SelectFormat.format( Object, StringBuffer,FieldPosition) is not implemented yet.");
    }

    /**
     * This method is not yet supported by <code>SelectFormat</code>.
     * @param source the string to be parsed.
     * @param pos defines the position where parsing is to begin,
     * and upon return, the position where parsing left off.  If the position
     * has not changed upon return, then parsing failed.
     * @return nothing because this method is not yet implemented.
     * @throws UnsupportedOperationException
     * @draft ICU 4.4
     */
    public Object parseObject(String source, ParsePosition pos) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * @draft ICU 4.4
     */
    public boolean equals(Object rhs) {
        throw new UnsupportedOperationException("SelectFormat.equals(Object) is not implemented yet.");
    }

    /**
     * Returns true if this equals the provided <code>SelectFormat<code>.
     * @param rhs the SelectFormat to compare against
     * @return true if this equals rhs
     * @throws UnsupportedOperationException
     * @draft ICU 4.4
     */
    public boolean equals(SelectFormat rhs) {
        throw new UnsupportedOperationException("SelectFormat.equals(SelectFormat) is not implemented yet.");
    }

    /**
     * {@inheritDoc}
     * @draft ICU 4.4
     */
    public int hashCode() {
        throw new UnsupportedOperationException("SelectFormat.hashCode() is not implemented yet.");
    }

    /**
     * Returns a string representation of the object
     * @return a text representation of the format object.
     * The result string includes the class name and
     * the pattern string returned by <code>toPattern()</code>.
     * @throws UnsupportedOperationException
     * @draft ICU 4.4
     */
    public String toString() {
        throw new UnsupportedOperationException("SelectFormat.toString() is not implemented yet.");
    }
}
