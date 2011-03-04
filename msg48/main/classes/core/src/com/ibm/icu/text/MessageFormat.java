/*
**********************************************************************
* Copyright (c) 2004-2011, International Business Machines
* Corporation and others.  All Rights Reserved.
**********************************************************************
* Author: Alan Liu
* Created: April 6, 2004
* Since: ICU 3.0
**********************************************************************
*/
package com.ibm.icu.text;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.text.ChoiceFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.ibm.icu.impl.Utility;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.MessagePattern.ArgType;
import com.ibm.icu.text.MessagePattern.Part;
import com.ibm.icu.util.ULocale;

/**
 * {@icuenhanced java.text.MessageFormat}.{@icu _usage_}
 *
 * <p>MessageFormat produces concatenated messages in a language-neutral
 * way. Use this whenever concatenating strings that are displayed to
 * end users.
 *
 * <p>A MessageFormat contains an array of <em>subformats</em> arranged
 * within a <em>template string</em>.  Together, the subformats and
 * template string determine how the MessageFormat will operate during
 * formatting and parsing.
 *
 * <p>Typically, both the subformats and the template string are
 * specified at once in a <em>pattern</em>.  By using different
 * patterns for different locales, messages may be localized.
 *
 * <p>When formatting, MessageFormat takes a collection of arguments
 * and produces a user-readable string.  The arguments may be passed
 * as an array or as a Map.  Each argument is matched up with its
 * corresponding subformat, which then formats it into a string.  The
 * resulting strings are then assembled within the string template of
 * the MessageFormat to produce the final output string.
 *
 * <p><strong>Note:</strong>
 * <code>MessageFormat</code> differs from the other <code>Format</code>
 * classes in that you create a <code>MessageFormat</code> object with one
 * of its constructors (not with a <code>getInstance</code> style factory
 * method). The factory methods aren't necessary because <code>MessageFormat</code>
 * itself doesn't implement locale-specific behavior. Any locale-specific
 * behavior is defined by the pattern that you provide and the
 * subformats used for inserted arguments.
 *
 * <p><strong>Note:</strong>
 * In ICU 3.8 MessageFormat supports named arguments.  If a named argument
 * is used, all arguments must be named.  Names start with a character in
 * <code>:ID_START:</code> and continue with characters in <code>:ID_CONTINUE:</code>,
 * in particular they do not start with a digit.  If named arguments
 * are used, {@link #usesNamedArguments()} will return true.
 *
 * <p>The other new methods supporting named arguments are
 * {@link #setFormatsByArgumentName(Map)},
 * {@link #setFormatByArgumentName(String, Format)},
 * {@link #format(Map, StringBuffer, FieldPosition)},
 * {@link #format(String, Map)}, {@link #parseToMap(String, ParsePosition)},
 * and {@link #parseToMap(String)}.  These methods are all compatible
 * with patterns that do not used named arguments-- in these cases
 * the keys in the input or output <code>Map</code>s use
 * <code>String</code>s that name the argument indices, e.g. "0",
 * "1", "2"... etc.
 *
 * <p>When named arguments are used, certain methods on MessageFormat that take or
 * return arrays will throw an exception, since it is not possible to
 * identify positions in an array using a name.  These methods are
 * {@link #setFormatsByArgumentIndex(Format[])},
 * {@link #setFormatByArgumentIndex(int, Format)},
 * {@link #getFormatsByArgumentIndex()},
 * {@link #getFormats()},
 * {@link #format(Object[], StringBuffer, FieldPosition)},
 * {@link #format(String, Object[])},
 * {@link #parse(String, ParsePosition)}, and
 * {@link #parse(String)}.
 * These APIs all have corresponding new versions as listed above.
 *
 * <p>The API {@link #format(Object, StringBuffer, FieldPosition)} has
 * been modified so that the <code>Object</code> argument can be
 * either an <code>Object</code> array or a <code>Map</code>.  If this
 * format uses named arguments, this argument must not be an
 * <code>Object</code> array otherwise an exception will be thrown.
 * If the argument is a <code>Map</code> it can be used with Strings that
 * represent indices as described above.
 *
 * <h4><a name="patterns">Patterns and Their Interpretation</a></h4>
 *
 * <code>MessageFormat</code> uses patterns of the following form:
 * <blockquote><pre>
 * <i>MessageFormatPattern:</i>
 *         <i>String</i>
 *         <i>MessageFormatPattern</i> <i>FormatElement</i> <i>String</i>
 *
 * <i>FormatElement:</i>
 *         { <i>ArgumentIndexOrName</i> }
 *         { <i>ArgumentIndexOrName</i> , <i>FormatType</i> }
 *         { <i>ArgumentIndexOrName</i> , <i>FormatType</i> , <i>FormatStyle</i> }
 *
 * <i>ArgumentIndexOrName: one of </i>
 *         ['0'-'9']+
 *         [:ID_START:][:ID_CONTINUE:]*
 *
 * <i>FormatType: one of </i>
 *         number date time choice spellout ordinal duration plural select
 *
 * <i>FormatStyle:</i>
 *         short
 *         medium
 *         long
 *         full
 *         integer
 *         currency
 *         percent
 *         <i>SubformatPattern</i>
 *         <i>RulesetName</i>
 *
 * <i>String:</i>
 *         <i>StringPart<sub>opt</sub></i>
 *         <i>String</i> <i>StringPart</i>
 *
 * <i>StringPart:</i>
 *         ''
 *         ' <i>QuotedString</i> '
 *         <i>UnquotedString</i>
 *
 * <i>SubformatPattern:</i>
 *         <i>SubformatPatternPart<sub>opt</sub></i>
 *         <i>SubformatPattern</i> <i>SubformatPatternPart</i>
 *
 * <i>SubFormatPatternPart:</i>
 *         ' <i>QuotedPattern</i> '
 *         <i>UnquotedPattern</i>
 * </pre></blockquote>
 *
 * <i>RulesetName:</i>
 *         <i>UnquotedString</i>
 *
 * <p>Within a <i>String</i>, <code>"''"</code> represents a single
 * quote. A <i>QuotedString</i> can contain arbitrary characters
 * except single quotes; the surrounding single quotes are removed.
 * An <i>UnquotedString</i> can contain arbitrary characters
 * except single quotes and left curly brackets. Thus, a string that
 * should result in the formatted message "'{0}'" can be written as
 * <code>"'''{'0}''"</code> or <code>"'''{0}'''"</code>.
 *
 * <p>Within a <i>SubformatPattern</i>, different rules apply.
 * A <i>QuotedPattern</i> can contain arbitrary characters
 * except single quotes; but the surrounding single quotes are
 * <strong>not</strong> removed, so they may be interpreted by the
 * subformat. For example, <code>"{1,number,$'#',##}"</code> will
 * produce a number format with the pound-sign quoted, with a result
 * such as: "$#31,45".
 * An <i>UnquotedPattern</i> can contain arbitrary characters
 * except single quotes, but curly braces within it must be balanced.
 * For example, <code>"ab {0} de"</code> and <code>"ab '}' de"</code>
 * are valid subformat patterns, but <code>"ab {0'}' de"</code> and
 * <code>"ab } de"</code> are not.
 *
 * <p><dl><dt><b>Warning:</b><dd>The rules for using quotes within message
 * format patterns unfortunately have shown to be somewhat confusing.
 * In particular, it isn't always obvious to localizers whether single
 * quotes need to be doubled or not. Make sure to inform localizers about
 * the rules, and tell them (for example, by using comments in resource
 * bundle source files) which strings will be processed by MessageFormat.
 * Note that localizers may need to use single quotes in translated
 * strings where the original version doesn't have them.
 *
 * <br>Note also that the simplest way to avoid the problem is to
 * use the real apostrophe (single quote) character \u2019 (') for
 * human-readable text, and to use the ASCII apostrophe (\u0027 ' )
 * only in program syntax, like quoting in MessageFormat.
 * See the annotations for U+0027 Apostrophe in The Unicode Standard.</p>
 * </dl>
 *
 * <p>The <i>ArgumentIndex</i> value is a non-negative integer written
 * using the digits '0' through '9', and represents an index into the
 * <code>arguments</code> array passed to the <code>format</code> methods
 * or the result array returned by the <code>parse</code> methods.
 *
 * <p>The <i>FormatType</i> and <i>FormatStyle</i> values are used to create
 * a <code>Format</code> instance for the format element. The following
 * table shows how the values map to Format instances. Combinations not
 * shown in the table are illegal. A <i>SubformatPattern</i> must
 * be a valid pattern string for the Format subclass used.
 *
 * <p><table border=1>
 *    <tr>
 *       <th>Format Type
 *       <th>Format Style
 *       <th>Subformat Created
 *    <tr>
 *       <td colspan=2><i>(none)</i>
 *       <td><code>null</code>
 *    <tr>
 *       <td rowspan=5><code>number</code>
 *       <td><i>(none)</i>
 *       <td><code>NumberFormat.getInstance(getLocale())</code>
 *    <tr>
 *       <td><code>integer</code>
 *       <td><code>NumberFormat.getIntegerInstance(getLocale())</code>
 *    <tr>
 *       <td><code>currency</code>
 *       <td><code>NumberFormat.getCurrencyInstance(getLocale())</code>
 *    <tr>
 *       <td><code>percent</code>
 *       <td><code>NumberFormat.getPercentInstance(getLocale())</code>
 *    <tr>
 *       <td><i>SubformatPattern</i>
 *       <td><code>new DecimalFormat(subformatPattern, new DecimalFormatSymbols(getLocale()))</code>
 *    <tr>
 *       <td rowspan=6><code>date</code>
 *       <td><i>(none)</i>
 *       <td><code>DateFormat.getDateInstance(DateFormat.DEFAULT, getLocale())</code>
 *    <tr>
 *       <td><code>short</code>
 *       <td><code>DateFormat.getDateInstance(DateFormat.SHORT, getLocale())</code>
 *    <tr>
 *       <td><code>medium</code>
 *       <td><code>DateFormat.getDateInstance(DateFormat.DEFAULT, getLocale())</code>
 *    <tr>
 *       <td><code>long</code>
 *       <td><code>DateFormat.getDateInstance(DateFormat.LONG, getLocale())</code>
 *    <tr>
 *       <td><code>full</code>
 *       <td><code>DateFormat.getDateInstance(DateFormat.FULL, getLocale())</code>
 *    <tr>
 *       <td><i>SubformatPattern</i>
 *       <td><code>new SimpleDateFormat(subformatPattern, getLocale())
 *    <tr>
 *       <td rowspan=6><code>time</code>
 *       <td><i>(none)</i>
 *       <td><code>DateFormat.getTimeInstance(DateFormat.DEFAULT, getLocale())</code>
 *    <tr>
 *       <td><code>short</code>
 *       <td><code>DateFormat.getTimeInstance(DateFormat.SHORT, getLocale())</code>
 *    <tr>
 *       <td><code>medium</code>
 *       <td><code>DateFormat.getTimeInstance(DateFormat.DEFAULT, getLocale())</code>
 *    <tr>
 *       <td><code>long</code>
 *       <td><code>DateFormat.getTimeInstance(DateFormat.LONG, getLocale())</code>
 *    <tr>
 *       <td><code>full</code>
 *       <td><code>DateFormat.getTimeInstance(DateFormat.FULL, getLocale())</code>
 *    <tr>
 *       <td><i>SubformatPattern</i>
 *       <td><code>new SimpleDateFormat(subformatPattern, getLocale())
 *    <tr>
 *       <td><code>choice</code>
 *       <td><i>SubformatPattern</i>
 *       <td><code>new ChoiceFormat(subformatPattern)</code>
 *    <tr>
 *       <td><code>spellout</code>
 *       <td><i>RulesetName (optional)</i>
 *       <td><code>new RuleBasedNumberFormat(getLocale(), RuleBasedNumberFormat.SPELLOUT)
 *           <br/>&nbsp;&nbsp;&nbsp;&nbsp;.setDefaultRuleset(ruleset);</code>
 *    <tr>
 *       <td><code>ordinal</code>
 *       <td><i>RulesetName (optional)</i>
 *       <td><code>new RuleBasedNumberFormat(getLocale(), RuleBasedNumberFormat.ORDINAL)
 *           <br/>&nbsp;&nbsp;&nbsp;&nbsp;.setDefaultRuleset(ruleset);</code>
 *    <tr>
 *       <td><code>duration</code>
 *       <td><i>RulesetName (optional)</i>
 *       <td><code>new RuleBasedNumberFormat(getLocale(), RuleBasedNumberFormat.DURATION)
 *           <br/>&nbsp;&nbsp;&nbsp;&nbsp;.setDefaultRuleset(ruleset);</code>
 *    <tr>
 *       <td><code>plural</code>
 *       <td><i>SubformatPattern</i>
 *       <td><code>new PluralFormat(subformatPattern)</code>
 *    <tr>
 *       <td><code>select</code>
 *       <td><i>SubformatPattern</i>
 *       <td><code>new SelectFormat(subformatPattern)</code>
 * </table>
 * <p>
 *
 * <h4>Usage Information</h4>
 *
 * <p>Here are some examples of usage:
 * <blockquote>
 * <pre>
 * Object[] arguments = {
 *     new Integer(7),
 *     new Date(System.currentTimeMillis()),
 *     "a disturbance in the Force"
 * };
 *
 * String result = MessageFormat.format(
 *     "At {1,time} on {1,date}, there was {2} on planet {0,number,integer}.",
 *     arguments);
 *
 * <em>output</em>: At 12:30 PM on Jul 3, 2053, there was a disturbance
 *           in the Force on planet 7.
 *
 * </pre>
 * </blockquote>
 * Typically, the message format will come from resources, and the
 * arguments will be dynamically set at runtime.
 *
 * <p>Example 2:
 * <blockquote>
 * <pre>
 * Object[] testArgs = {new Long(3), "MyDisk"};
 *
 * MessageFormat form = new MessageFormat(
 *     "The disk \"{1}\" contains {0} file(s).");
 *
 * System.out.println(form.format(testArgs));
 *
 * // output, with different testArgs
 * <em>output</em>: The disk "MyDisk" contains 0 file(s).
 * <em>output</em>: The disk "MyDisk" contains 1 file(s).
 * <em>output</em>: The disk "MyDisk" contains 1,273 file(s).
 * </pre>
 * </blockquote>
 *
 * <p>
 * <strong>Creating internationalized messages that include plural forms, you
 * can use a PluralFormat:</strong>
 * <pre>
 * MessageFormat msgFmt = new MessageFormat("{0, plural, " +
 *     "one{{0, number, C''''est #,##0.0#  fichier}} " +
 *     "other {Ce sont # fichiers}} dans la liste.",
 *     new ULocale("fr"));
 * Object args[] = {new Long(0)};
 * System.out.println(msgFmt.format(args));
 * args = {new Long(3)};
 * System.out.println(msgFmt.format(args));
 * 
 * Produces the output:<br />
 * <code>C'est 0,0 fichier dans la liste.</code><br />
 * <code>Ce sont 3 fichiers dans la liste."</code>
 * </pre>
 * Please check {@link PluralFormat} and {@link PluralRules} for details.
 * </p>
 *
 * <h4><a name="synchronization">Synchronization</a></h4>
 *
 * <p>Message formats are not synchronized.
 * It is recommended to create separate format instances for each thread.
 * If multiple threads access a format concurrently, it must be synchronized
 * externally.
 *
 * @see          java.util.Locale
 * @see          Format
 * @see          NumberFormat
 * @see          DecimalFormat
 * @see          ChoiceFormat
 * @see          PluralFormat
 * @see          SelectFormat
 * @author       Mark Davis
 * @stable ICU 3.0
 */
public class MessageFormat extends UFormat {

    // Generated by serialver from JDK 1.4.1_01
    static final long serialVersionUID = 7136212545847378651L;

    /**
     * Constructs a MessageFormat for the default locale and the
     * specified pattern.
     * The constructor first sets the locale, then parses the pattern and
     * creates a list of subformats for the format elements contained in it.
     * Patterns and their interpretation are specified in the
     * <a href="#patterns">class description</a>.
     *
     * @param pattern the pattern for this message format
     * @exception IllegalArgumentException if the pattern is invalid
     * @stable ICU 3.0
     */
    public MessageFormat(String pattern) {
        this.ulocale = ULocale.getDefault();
        applyPattern(pattern);
    }

    /**
     * Constructs a MessageFormat for the specified locale and
     * pattern.
     * The constructor first sets the locale, then parses the pattern and
     * creates a list of subformats for the format elements contained in it.
     * Patterns and their interpretation are specified in the
     * <a href="#patterns">class description</a>.
     *
     * @param pattern the pattern for this message format
     * @param locale the locale for this message format
     * @exception IllegalArgumentException if the pattern is invalid
     * @stable ICU 3.0
     */
    public MessageFormat(String pattern, Locale locale) {
        this(pattern, ULocale.forLocale(locale));
    }

    /**
     * Constructs a MessageFormat for the specified locale and
     * pattern.
     * The constructor first sets the locale, then parses the pattern and
     * creates a list of subformats for the format elements contained in it.
     * Patterns and their interpretation are specified in the
     * <a href="#patterns">class description</a>.
     *
     * @param pattern the pattern for this message format
     * @param locale the locale for this message format
     * @exception IllegalArgumentException if the pattern is invalid
     * @stable ICU 3.2
     */
    public MessageFormat(String pattern, ULocale locale) {
        this.ulocale = locale;
        applyPattern(pattern);
    }

    /**
     * Sets the locale to be used when creating or comparing subformats.
     * This affects subsequent calls to the {@link #applyPattern applyPattern}
     * and {@link #toPattern toPattern} methods as well as to the
     * <code>format</code> and
     * {@link #formatToCharacterIterator formatToCharacterIterator} methods.
     *
     * @param locale the locale to be used when creating or comparing subformats
     * @stable ICU 3.0
     */
    public void setLocale(Locale locale) {
        setLocale(ULocale.forLocale(locale));
    }

    /**
     * Sets the locale to be used when creating or comparing subformats.
     * This affects subsequent calls to the {@link #applyPattern applyPattern}
     * and {@link #toPattern toPattern} methods as well as to the
     * <code>format</code> and
     * {@link #formatToCharacterIterator formatToCharacterIterator} methods.
     *
     * @param locale the locale to be used when creating or comparing subformats
     * @stable ICU 3.2
     */
    public void setLocale(ULocale locale) {
        /* Save the pattern, and then reapply so that */
        /* we pick up any changes in locale specific */
        /* elements */
        String existingPattern = toPattern();                       /*ibm.3550*/
        this.ulocale = locale;
        // Invalidate all stock formatters. They are no longer valid since
        // the locale has changed.
        stockNumberFormatter = stockDateFormatter = null;
        pluralProvider = null;
        applyPattern(existingPattern);                              /*ibm.3550*/
    }

    /**
     * Returns the locale that's used when creating or comparing subformats.
     *
     * @return the locale used when creating or comparing subformats
     * @stable ICU 3.0
     */
    public Locale getLocale() {
        return ulocale.toLocale();
    }

    /**
     * {@icu} Returns the locale that's used when creating or comparing subformats.
     *
     * @return the locale used when creating or comparing subformats
     * @stable ICU 3.2
     */
    public ULocale getULocale() {
        return ulocale;
    }

    /**
     * Sets the pattern used by this message format.
     * The method parses the pattern and creates a list of subformats
     * for the format elements contained in it.
     * Patterns and their interpretation are specified in the
     * <a href="#patterns">class description</a>.
     * <p>
     * The pattern must contain only named or only numeric arguments,
     * mixing them is not allowed.
     *
     * @param pttrn the pattern for this message format
     * @throws IllegalArgumentException if the pattern is invalid
     * @stable ICU 3.0
     */
    @SuppressWarnings("fallthrough")
    public void applyPattern(String pttrn) {
        try {
            if (msgPattern == null) {
                msgPattern = new MessagePattern(pttrn);
            } else {
                msgPattern.parse(pttrn);
            }
            // Cache the formats that are explicitly mentioned in the message pattern.
            cacheExplicitFormats();
        } catch(RuntimeException e) {
            resetPattern();
            throw e;
        }
        
        
        StringBuilder[] segments = new StringBuilder[4];
        for (int i = 0; i < segments.length; ++i) {
            segments[i] = new StringBuilder();
        }
        int part = 0;
        int formatNumber = 0;
        boolean inQuote = false;
        int braceStack = 0;
        maxOffset = -1;
        for (int i = 0; i < pttrn.length(); ++i) {
            char ch = pttrn.charAt(i);
            if (part == 0) {
                if (ch == '\'') {
                    if (i + 1 < pttrn.length()
                        && pttrn.charAt(i+1) == '\'') {
                        segments[part].append(ch);  // handle doubles
                        ++i;
                    } else {
                        inQuote = !inQuote;
                    }
                } else if (ch == '{' && !inQuote) {
                    part = 1;
                } else {
                    segments[part].append(ch);
                }
            } else  if (inQuote) {  // just copy quotes in parts
                segments[part].append(ch);
                if (ch == '\'') {
                    inQuote = false;
                }
            } else {
                switch (ch) {
                case ',':
                    if (part < 3)
                        part += 1;
                    else
                        segments[part].append(ch);
                    break;
                case '{':
                    ++braceStack;
                    segments[part].append(ch);
                    break;
                case '}':
                    if (braceStack == 0) {
                        part = 0;
                        makeFormat(i, formatNumber, segments);
                        formatNumber++;
                    } else {
                        --braceStack;
                        segments[part].append(ch);
                    }
                    break;
                case '\'':
                    inQuote = true;
                    // fall through, so we keep quotes in other parts
                default:
                    segments[part].append(ch);
                    break;
                }
            }
        }
        if (braceStack == 0 && part != 0) {
            maxOffset = -1;
            throw new IllegalArgumentException("Unmatched braces in the pattern.");
        }
        this.pattern = segments[0].toString();
    }

    /**
     * Returns a pattern representing the current state of the message format.
     * The string is constructed from internal information and therefore
     * does not necessarily equal the previously applied pattern.
     *
     * @return a pattern representing the current state of the message format
     * @stable ICU 3.0
     */
    public String toPattern() {
        // Return the original, applied pattern string, or else "".
        // Note: This does not take into account
        // - changes from setFormat() and similar methods, or
        // - normalization of apostrophes and arguments, for example,
        //   whether some date/time/number formatter was created via a pattern
        //   but is equivalent to the "medium" default format.
        if (haveCustomFormats) {
            throw new IllegalStateException(
                    "toPattern() is not supported after custom Format objects "+
                    "have been set via setFormat() or similar APIs");
        }
        if (msgPattern == null) {
            return "";
        }
        String originalPattern = msgPattern.getString();
        return originalPattern == null ? "" : originalPattern;
    }

    /**
     * Returns the part index of the next ARG_START after partIndex, or -1 if there is none more.
     * @param partIndex Part index of the previous ARG_START (initially 0).
     * @return
     */
    private int nextTopLevelArgStart(int partIndex, Part part) {
        if (partIndex != 0) {
            partIndex = msgPattern.getPartLimit(partIndex);
        }
        for (;;) {
            MessagePattern.Part.Type type = msgPattern.getPart(++partIndex, part).getType();
            if (type == MessagePattern.Part.Type.ARG_START) {
                return partIndex;
            }
            if (type == MessagePattern.Part.Type.MSG_LIMIT) {
                return -1;
            }
        }
    }

    private boolean argNameMatches(int partIndex, Part part, String argName, int argNumber) {
        MessagePattern.Part.Type type = msgPattern.getPart(partIndex, part).getType();
        return type == MessagePattern.Part.Type.ARG_NAME ?
            msgPattern.partSubstringMatches(part, argName) :
            part.getValue() == argNumber;  // ARG_NUMBER
    }

    private String getArgName(int partIndex, Part part) {
        MessagePattern.Part.Type type = msgPattern.getPart(partIndex, part).getType();
        if (type == MessagePattern.Part.Type.ARG_NAME) {
            return msgPattern.getSubstring(part);
        } else {
            return Integer.toString(part.getValue());
        }
    }

    /**
     * Sets the formats to use for the values passed into
     * <code>format</code> methods or returned from <code>parse</code>
     * methods. The indices of elements in <code>newFormats</code>
     * correspond to the argument indices used in the previously set
     * pattern string.
     * The order of formats in <code>newFormats</code> thus corresponds to
     * the order of elements in the <code>arguments</code> array passed
     * to the <code>format</code> methods or the result array returned
     * by the <code>parse</code> methods.
     * <p>
     * If an argument index is used for more than one format element
     * in the pattern string, then the corresponding new format is used
     * for all such format elements. If an argument index is not used
     * for any format element in the pattern string, then the
     * corresponding new format is ignored. If fewer formats are provided
     * than needed, then only the formats for argument indices less
     * than <code>newFormats.length</code> are replaced.
     *
     * This method is only supported if the format does not use
     * named arguments, otherwise an IllegalArgumentException is thrown.
     *
     * @param newFormats the new formats to use
     * @throws NullPointerException if <code>newFormats</code> is null
     * @throws IllegalArgumentException if this formatter uses named arguments
     * @stable ICU 3.0
     */
    public void setFormatsByArgumentIndex(Format[] newFormats) {
        if (msgPattern.hasNamedArguments()) {
            throw new IllegalArgumentException(
                    "This method is not available in MessageFormat objects " +
                    "that use alphanumeric argument names.");
        }
        for (int i = 0; i <= maxOffset; i++) {
            int j = Integer.parseInt(argumentNames[i]);
            if (j < newFormats.length) {
                formats[i] = newFormats[j];
            }
        }
        Part part = new Part();
        for (int partIndex = 0; (partIndex = nextTopLevelArgStart(partIndex, part)) >= 0;) {
            int j = msgPattern.getPart(partIndex + 1, part).getValue();
            if (j < newFormats.length) {
                setCustomArgStartFormat(partIndex, newFormats[j]);
            }
        }
    }

    /**
     * {@icu} Sets the formats to use for the values passed into
     * <code>format</code> methods or returned from <code>parse</code>
     * methods. The keys in <code>newFormats</code> are the argument
     * names in the previously set pattern string, and the values
     * are the formats.
     * <p>
     * Only argument names from the pattern string are considered.
     * Extra keys in <code>newFormats</code> that do not correspond
     * to an argument name are ignored.  Similarly, if there is no
     * format in newFormats for an argument name, the formatter
     * for that argument remains unchanged.
     * <p>
     * This may be called on formats that do not use named arguments.
     * In this case the map will be queried for key Strings that
     * represent argument indices, e.g. "0", "1", "2" etc.
     *
     * @param newFormats a map from String to Format providing new
     *        formats for named arguments.
     * @stable ICU 3.8
     */
    public void setFormatsByArgumentName(Map<String, Format> newFormats) {
        for (int i = 0; i <= maxOffset; i++) {
            if (newFormats.containsKey(argumentNames[i])) {
                Format f = newFormats.get(argumentNames[i]);
                formats[i] = f;
            }
        }
        Part part = new Part();
        for (int partIndex = 0; (partIndex = nextTopLevelArgStart(partIndex, part)) >= 0;) {
            String key = getArgName(partIndex + 1, part);
            if (newFormats.containsKey(key)) {
                setCustomArgStartFormat(partIndex, newFormats.get(key));
            }
        }
    }

    /**
     * Sets the formats to use for the format elements in the
     * previously set pattern string.
     * The order of formats in <code>newFormats</code> corresponds to
     * the order of format elements in the pattern string.
     * <p>
     * If more formats are provided than needed by the pattern string,
     * the remaining ones are ignored. If fewer formats are provided
     * than needed, then only the first <code>newFormats.length</code>
     * formats are replaced.
     * <p>
     * Since the order of format elements in a pattern string often
     * changes during localization, it is generally better to use the
     * {@link #setFormatsByArgumentIndex setFormatsByArgumentIndex}
     * method, which assumes an order of formats corresponding to the
     * order of elements in the <code>arguments</code> array passed to
     * the <code>format</code> methods or the result array returned by
     * the <code>parse</code> methods.
     *
     * @param newFormats the new formats to use
     * @exception NullPointerException if <code>newFormats</code> is null
     * @stable ICU 3.0
     */
    public void setFormats(Format[] newFormats) {
        int runsToCopy = newFormats.length;
        if (runsToCopy > maxOffset + 1) {
            runsToCopy = maxOffset + 1;
        }
        for (int i = 0; i < runsToCopy; i++) {
            formats[i] = newFormats[i];
        }
        int formatNumber = 0;
        Part part = new Part();
        for (int partIndex = 0;
                formatNumber < newFormats.length &&
                (partIndex = nextTopLevelArgStart(partIndex, part)) >= 0;) {
            setCustomArgStartFormat(partIndex, newFormats[formatNumber]);
            ++formatNumber;
        }
    }

    /**
     * Sets the format to use for the format elements within the
     * previously set pattern string that use the given argument
     * index.
     * The argument index is part of the format element definition and
     * represents an index into the <code>arguments</code> array passed
     * to the <code>format</code> methods or the result array returned
     * by the <code>parse</code> methods.
     * <p>
     * If the argument index is used for more than one format element
     * in the pattern string, then the new format is used for all such
     * format elements. If the argument index is not used for any format
     * element in the pattern string, then the new format is ignored.
     *
     * This method is only supported when exclusively numbers are used for
     * argument names. Otherwise an IllegalArgumentException is thrown.
     *
     * @param argumentIndex the argument index for which to use the new format
     * @param newFormat the new format to use
     * @throws IllegalArgumentException if this format uses named arguments
     * @stable ICU 3.0
     */
    public void setFormatByArgumentIndex(int argumentIndex, Format newFormat) {
        if (msgPattern.hasNamedArguments()) {
            throw new IllegalArgumentException(
                    "This method is not available in MessageFormat objects " +
                    "that use alphanumeric argument names.");
        }
        for (int j = 0; j <= maxOffset; j++) {
            if (Integer.parseInt(argumentNames[j]) == argumentIndex) {
                formats[j] = newFormat;
            }
        }
        Part part = new Part();
        for (int partIndex = 0; (partIndex = nextTopLevelArgStart(partIndex, part)) >= 0;) {
            if (msgPattern.getPart(partIndex + 1, part).getValue() == argumentIndex) {
                setCustomArgStartFormat(partIndex, newFormat);
            }
        }
    }

    /**
     * {@icu} Sets the format to use for the format elements within the
     * previously set pattern string that use the given argument
     * name.
     * <p>
     * If the argument name is used for more than one format element
     * in the pattern string, then the new format is used for all such
     * format elements. If the argument name is not used for any format
     * element in the pattern string, then the new format is ignored.
     * <p>
     * This API may be used on formats that do not use named arguments.
     * In this case <code>argumentName</code> should be a String that names
     * an argument index, e.g. "0", "1", "2"... etc.  If it does not name
     * a valid index, the format will be ignored.  No error is thrown.
     *
     * @param argumentName the name of the argument to change
     * @param newFormat the new format to use
     * @stable ICU 3.8
     */
    public void setFormatByArgumentName(String argumentName, Format newFormat) {
        for (int i = 0; i <= maxOffset; ++i) {
            if (argumentName.equals(argumentNames[i])) {
                formats[i] = newFormat;
            }
        }
        int argNumber = MessagePattern.validateArgumentName(argumentName);
        if (argNumber < -1) {
            return;
        }
        Part part = new Part();
        for (int partIndex = 0; (partIndex = nextTopLevelArgStart(partIndex, part)) >= 0;) {
            if (argNameMatches(partIndex + 1, part, argumentName, argNumber)) {
                setCustomArgStartFormat(partIndex, newFormat);
            }
        }
    }

    /**
     * Sets the format to use for the format element with the given
     * format element index within the previously set pattern string.
     * The format element index is the zero-based number of the format
     * element counting from the start of the pattern string.
     * <p>
     * Since the order of format elements in a pattern string often
     * changes during localization, it is generally better to use the
     * {@link #setFormatByArgumentIndex setFormatByArgumentIndex}
     * method, which accesses format elements based on the argument
     * index they specify.
     *
     * @param formatElementIndex the index of a format element within the pattern
     * @param newFormat the format to use for the specified format element
     * @exception ArrayIndexOutOfBoundsException if formatElementIndex is equal to or
     *            larger than the number of format elements in the pattern string
     * @stable ICU 3.0
     */
    public void setFormat(int formatElementIndex, Format newFormat) {
        formats[formatElementIndex] = newFormat;
        int formatNumber = 0;
        Part part = new Part();
        for (int partIndex = 0; (partIndex = nextTopLevelArgStart(partIndex, part)) >= 0;) {
            if (formatNumber == formatElementIndex) {
                setCustomArgStartFormat(partIndex, newFormat);
                return;
            }
            ++formatNumber;
        }
        throw new ArrayIndexOutOfBoundsException(formatElementIndex);
    }

    /**
     * Returns the formats used for the values passed into
     * <code>format</code> methods or returned from <code>parse</code>
     * methods. The indices of elements in the returned array
     * correspond to the argument indices used in the previously set
     * pattern string.
     * The order of formats in the returned array thus corresponds to
     * the order of elements in the <code>arguments</code> array passed
     * to the <code>format</code> methods or the result array returned
     * by the <code>parse</code> methods.
     * <p>
     * If an argument index is used for more than one format element
     * in the pattern string, then the format used for the last such
     * format element is returned in the array. If an argument index
     * is not used for any format element in the pattern string, then
     * null is returned in the array.
     *
     * This method is only supported when exclusively numbers are used for
     * argument names. Otherwise an IllegalArgumentException is thrown.
     *
     * @return the formats used for the arguments within the pattern
     * @throws IllegalArgumentException if this format uses named arguments
     * @stable ICU 3.0
     */
    public Format[] getFormatsByArgumentIndex() {
        if (msgPattern.hasNamedArguments()) {
            throw new IllegalArgumentException(
                    "This method is not available in MessageFormat objects " +
                    "that use alphanumeric argument names.");
        }
        ArrayList<Format> list = new ArrayList<Format>();
        Part part = new Part();
        for (int partIndex = 0; (partIndex = nextTopLevelArgStart(partIndex, part)) >= 0;) {
            int argNumber = msgPattern.getPart(partIndex + 1, part).getValue();
            while (argNumber >= list.size()) {
                list.add(null);
            }
            list.set(argNumber, cachedFormatters == null ? null : cachedFormatters.get(partIndex));
        }
        return list.toArray(new Format[list.size()]);
    }
    // TODO: provide method public Map getFormatsByArgumentName().
    // Where Map is: String argumentName --> Format format.

    /**
     * Returns the formats used for the format elements in the
     * previously set pattern string.
     * The order of formats in the returned array corresponds to
     * the order of format elements in the pattern string.
     * <p>
     * Since the order of format elements in a pattern string often
     * changes during localization, it's generally better to use the
     * {@link #getFormatsByArgumentIndex()}
     * method, which assumes an order of formats corresponding to the
     * order of elements in the <code>arguments</code> array passed to
     * the <code>format</code> methods or the result array returned by
     * the <code>parse</code> methods.
     *
     * This method is only supported when exclusively numbers are used for
     * argument names. Otherwise an IllegalArgumentException is thrown.
     *
     * @return the formats used for the format elements in the pattern
     * @throws IllegalArgumentException if this format uses named arguments
     * @stable ICU 3.0
     */
    public Format[] getFormats() {
        ArrayList<Format> list = new ArrayList<Format>();
        Part part = new Part();
        for (int partIndex = 0; (partIndex = nextTopLevelArgStart(partIndex, part)) >= 0;) {
            list.add(cachedFormatters == null ? null : cachedFormatters.get(partIndex));
        }
        return list.toArray(new Format[list.size()]);
    }

    /**
     * {@icu} Returns the format argument names. For more details, see
     * {@link #setFormatByArgumentName(String, Format)}.
     * @return List of names
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public Set<String> getFormatArgumentNames() {
        Set<String> result = new HashSet<String>();
        Part part = new Part();
        for (int partIndex = 0; (partIndex = nextTopLevelArgStart(partIndex, part)) >= 0;) {
            result.add(getArgName(partIndex + 1, part));
        }
        return result;
    }

    /**
     * {@icu} Returns the formats according to their argument names. For more details, see
     * {@link #setFormatByArgumentName(String, Format)}.
     * @return format associated with the name, or null if there isn't one.
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public Format getFormatByArgumentName(String argumentName) {
        if (cachedFormatters == null) {
            return null;
        }
        int argNumber = MessagePattern.validateArgumentName(argumentName);
        if (argNumber < -1) {
            return null;
        }
        Part part = new Part();
        for (int partIndex = 0; (partIndex = nextTopLevelArgStart(partIndex, part)) >= 0;) {
            if (argNameMatches(partIndex + 1, part, argumentName, argNumber)) {
                return cachedFormatters.get(partIndex);
            }
        }
        return null;
    }

    /**
     * Formats an array of objects and appends the <code>MessageFormat</code>'s
     * pattern, with format elements replaced by the formatted objects, to the
     * provided <code>StringBuffer</code>.
     * <p>
     * The text substituted for the individual format elements is derived from
     * the current subformat of the format element and the
     * <code>arguments</code> element at the format element's argument index
     * as indicated by the first matching line of the following table. An
     * argument is <i>unavailable</i> if <code>arguments</code> is
     * <code>null</code> or has fewer than argumentIndex+1 elements.  When
     * an argument is unavailable no substitution is performed.
     * <p>
     * <table border=1>
     *    <tr>
     *       <th>Subformat
     *       <th>Argument
     *       <th>Formatted Text
     *    <tr>
     *       <td><i>any</i>
     *       <td><i>unavailable</i>
     *       <td><code>"{" + argumentIndex + "}"</code>
     *    <tr>
     *       <td><i>any</i>
     *       <td><code>null</code>
     *       <td><code>"null"</code>
     *    <tr>
     *       <td><code>instanceof ChoiceFormat</code>
     *       <td><i>any</i>
     *       <td><code>subformat.format(argument).indexOf('{') >= 0 ?<br>
     *           (new MessageFormat(subformat.format(argument), getLocale())).format(argument) :
     *           subformat.format(argument)</code>
     *    <tr>
     *       <td><code>!= null</code>
     *       <td><i>any</i>
     *       <td><code>subformat.format(argument)</code>
     *    <tr>
     *       <td><code>null</code>
     *       <td><code>instanceof Number</code>
     *       <td><code>NumberFormat.getInstance(getLocale()).format(argument)</code>
     *    <tr>
     *       <td><code>null</code>
     *       <td><code>instanceof Date</code>
     *       <td><code>DateFormat.getDateTimeInstance(DateFormat.SHORT,
     *           DateFormat.SHORT, getLocale()).format(argument)</code>
     *    <tr>
     *       <td><code>null</code>
     *       <td><code>instanceof String</code>
     *       <td><code>argument</code>
     *    <tr>
     *       <td><code>null</code>
     *       <td><i>any</i>
     *       <td><code>argument.toString()</code>
     * </table>
     * <p>
     * If <code>pos</code> is non-null, and refers to
     * <code>Field.ARGUMENT</code>, the location of the first formatted
     * string will be returned.
     *
     * This method is only supported when the format does not use named
     * arguments, otherwise an IllegalArgumentException is thrown.
     *
     * @param arguments an array of objects to be formatted and substituted.
     * @param result where text is appended.
     * @param pos On input: an alignment field, if desired.
     *            On output: the offsets of the alignment field.
     * @throws IllegalArgumentException if an argument in the
     *            <code>arguments</code> array is not of the type
     *            expected by the format element(s) that use it.
     * @throws IllegalArgumentException if this format uses named arguments
     * @stable ICU 3.0
     */
    public final StringBuffer format(Object[] arguments, StringBuffer result,
                                     FieldPosition pos)
    {
        format(arguments, null, new AppendableWrapper(result), pos);
        return result;
    }

    /**
     * Formats a map of objects and appends the <code>MessageFormat</code>'s
     * pattern, with format elements replaced by the formatted objects, to the
     * provided <code>StringBuffer</code>.
     * <p>
     * The text substituted for the individual format elements is derived from
     * the current subformat of the format element and the
     * <code>arguments</code> value corresopnding to the format element's
     * argument name.
     * <p>
     * This API may be called on formats that do not use named arguments.
     * In this case the the keys in <code>arguments</code> must be numeric
     * strings (e.g. "0", "1", "2"...).
     * <p>
     * An argument is <i>unavailable</i> if <code>arguments</code> is
     * <code>null</code> or does not have a value corresponding to an argument
     * name in the pattern.  When an argument is unavailable no substitution
     * is performed.
     *
     * @param arguments a map of objects to be formatted and substituted.
     * @param result where text is appended.
     * @param pos On input: an alignment field, if desired.
     *            On output: the offsets of the alignment field.
     * @throws IllegalArgumentException if an argument in the
     *         <code>arguments</code> array is not of the type
     *         expected by the format element(s) that use it.
     * @return the passed-in StringBuffer
     * @stable ICU 3.8
     */
    public final StringBuffer format(Map<String, Object> arguments, StringBuffer result,
                                     FieldPosition pos) {
        format(null, arguments, new AppendableWrapper(result), pos);
        return result;
    }

    /**
     * Creates a MessageFormat with the given pattern and uses it
     * to format the given arguments. This is equivalent to
     * <blockquote>
     *     <code>(new {@link #MessageFormat(String) MessageFormat}(pattern)).{@link
     *     #format(java.lang.Object[], java.lang.StringBuffer, java.text.FieldPosition)
     *     format}(arguments, new StringBuffer(), null).toString()</code>
     * </blockquote>
     *
     * @throws IllegalArgumentException if the pattern is invalid,
     *            or if an argument in the <code>arguments</code> array
     *            is not of the type expected by the format element(s)
     *            that use it.
     * @throws IllegalArgumentException if this format uses named arguments
     * @stable ICU 3.0
     */
    public static String format(String pattern, Object... arguments) {
        MessageFormat temp = new MessageFormat(pattern);
        return temp.format(arguments);
    }

    /**
     * Creates a MessageFormat with the given pattern and uses it to
     * format the given arguments.  The pattern must identifyarguments
     * by name instead of by number.
     * <p>
     * @throws IllegalArgumentException if the pattern is invalid,
     *         or if an argument in the <code>arguments</code> map
     *         is not of the type expected by the format element(s)
     *         that use it.
     * @see #format(Map, StringBuffer, FieldPosition)
     * @see #format(String, Object[])
     * @stable ICU 3.8
     */
    public static String format(String pattern, Map<String, Object> arguments) {
        MessageFormat temp = new MessageFormat(pattern);
        return temp.format(arguments);
    }

    /**
     * {@icu} Returns true if this MessageFormat uses named arguments,
     * and false otherwise.  See class description.
     *
     * @return true if named arguments are used.
     * @stable ICU 3.8
     */
    public boolean usesNamedArguments() {
        return msgPattern.hasNamedArguments();
    }

    // Overrides
    /**
     * Formats a map or array of objects and appends the <code>MessageFormat</code>'s
     * pattern, with format elements replaced by the formatted objects, to the
     * provided <code>StringBuffer</code>.
     * This is equivalent to either of
     * <blockquote>
     *     <code>{@link #format(java.lang.Object[], java.lang.StringBuffer,
     *     java.text.FieldPosition) format}((Object[]) arguments, result, pos)</code>
     *     <code>{@link #format(java.util.Map, java.lang.StringBuffer,
     *     java.text.FieldPosition) format}((Map) arguments, result, pos)</code>
     * </blockquote>
     * A map must be provided if this format uses named arguments, otherwise
     * an IllegalArgumentException will be thrown.
     * @param arguments a map or array of objects to be formatted
     * @param result where text is appended
     * @param pos On input: an alignment field, if desired
     *            On output: the offsets of the alignment field
     * @throws IllegalArgumentException if an argument in
     *         <code>arguments</code> is not of the type
     *         expected by the format element(s) that use it
     * @throws IllegalArgumentException if <code>arguments<code> is
     *         an array of Object and this format uses named arguments
     * @stable ICU 3.0
     */
    public final StringBuffer format(Object arguments, StringBuffer result,
                                     FieldPosition pos)
    {
        format(arguments, new AppendableWrapper(result), pos);
        return result;
    }

    /**
     * Formats an array of objects and inserts them into the
     * <code>MessageFormat</code>'s pattern, producing an
     * <code>AttributedCharacterIterator</code>.
     * You can use the returned <code>AttributedCharacterIterator</code>
     * to build the resulting String, as well as to determine information
     * about the resulting String.
     * <p>
     * The text of the returned <code>AttributedCharacterIterator</code> is
     * the same that would be returned by
     * <blockquote>
     *     <code>{@link #format(java.lang.Object[], java.lang.StringBuffer,
     *     java.text.FieldPosition) format}(arguments, new StringBuffer(), null).toString()</code>
     * </blockquote>
     * <p>
     * In addition, the <code>AttributedCharacterIterator</code> contains at
     * least attributes indicating where text was generated from an
     * argument in the <code>arguments</code> array. The keys of these attributes are of
     * type <code>MessageFormat.Field</code>, their values are
     * <code>Integer</code> objects indicating the index in the <code>arguments</code>
     * array of the argument from which the text was generated.
     * <p>
     * The attributes/value from the underlying <code>Format</code>
     * instances that <code>MessageFormat</code> uses will also be
     * placed in the resulting <code>AttributedCharacterIterator</code>.
     * This allows you to not only find where an argument is placed in the
     * resulting String, but also which fields it contains in turn.
     *
     * @param arguments an array of objects to be formatted and substituted.
     * @return AttributedCharacterIterator describing the formatted value.
     * @exception NullPointerException if <code>arguments</code> is null.
     * @exception IllegalArgumentException if an argument in the
     *            <code>arguments</code> array is not of the type
     *            expected by the format element(s) that use it.
     * @stable ICU 3.8
     */
    public AttributedCharacterIterator formatToCharacterIterator(Object arguments) {
        if (arguments == null) {
            throw new NullPointerException(
                   "formatToCharacterIterator must be passed non-null object");
        }
        StringBuilder result = new StringBuilder();
        AppendableWrapper wrapper = new AppendableWrapper(result);
        wrapper.useAttributes();
        format(arguments, wrapper, null);
        AttributedString as = new AttributedString(result.toString());
        for (AttributeAndPosition a : wrapper.attributes) {
            as.addAttribute(a.key, a.value, a.start, a.limit);
        }
        return as.getIterator();
    }

    /**
     * Parses the string.
     *
     * <p>Caveats: The parse may fail in a number of circumstances.
     * For example:
     * <ul>
     * <li>If one of the arguments does not occur in the pattern.
     * <li>If the format of an argument loses information, such as
     *     with a choice format where a large number formats to "many".
     * <li>Does not yet handle recursion (where
     *     the substituted strings contain {n} references.)
     * <li>Will not always find a match (or the correct match)
     *     if some part of the parse is ambiguous.
     *     For example, if the pattern "{1},{2}" is used with the
     *     string arguments {"a,b", "c"}, it will format as "a,b,c".
     *     When the result is parsed, it will return {"a", "b,c"}.
     * <li>If a single argument is parsed more than once in the string,
     *     then the later parse wins.
     * </ul>
     * When the parse fails, use ParsePosition.getErrorIndex() to find out
     * where in the string did the parsing failed. The returned error
     * index is the starting offset of the sub-patterns that the string
     * is comparing with. For example, if the parsing string "AAA {0} BBB"
     * is comparing against the pattern "AAD {0} BBB", the error index is
     * 0. When an error occurs, the call to this method will return null.
     * If the source is null, return an empty array.
     * <p>
     * This method is only supported with numbered arguments.  If
     * the format pattern used named argument an
     * IllegalArgumentException is thrown.
     *
     * @throws IllegalArgumentException if this format uses named arguments
     * @stable ICU 3.0
     */
    public Object[] parse(String source, ParsePosition pos) {
        if (msgPattern.hasNamedArguments()) {
            throw new IllegalArgumentException(
                    "This method is not available in MessageFormat objects " +
                    "that use named argument.");
        }
        Map<String, Object> objectMap = parseToMap(source, pos);
        int maximumArgumentNumber = -1;
        for (int i = 0; i <= maxOffset; i++) {
            int argumentNumber = Integer.parseInt(argumentNames[i]);
            if (argumentNumber > maximumArgumentNumber) {
                maximumArgumentNumber = argumentNumber;
            }
        }

        if (objectMap == null) {
            return null;
        }

        Object[] resultArray = new Object[maximumArgumentNumber + 1];
        for (String key : objectMap.keySet()) {
            resultArray[Integer.parseInt(key)] = objectMap.get(key);
        }

        return resultArray;
    }

    /**
     * {@icu} Parses the string, returning the results in a Map.
     * This is similar to the version that returns an array
     * of Object.  This supports both named and numbered
     * arguments-- if numbered, the keys in the map are the
     * corresponding Strings (e.g. "0", "1", "2"...).
     *
     * @param source the text to parse
     * @param pos the position at which to start parsing.  on return,
     *        contains the result of the parse.
     * @return a Map containing key/value pairs for each parsed argument.
     * @stable ICU 3.8
     */
    public Map<String, Object> parseToMap(String source, ParsePosition pos) {
        if (source == null) {
            Map<String, Object> empty = new HashMap<String, Object>();
            return empty;
        }

        Map<String, Object> resultMap = new HashMap<String, Object>();

        int patternOffset = 0;
        int sourceOffset = pos.getIndex();
        ParsePosition tempStatus = new ParsePosition(0);
        for (int i = 0; i <= maxOffset; ++i) {
            // match up to format
            int len = offsets[i] - patternOffset;
            if (len == 0 || pattern.regionMatches(patternOffset,
                                                  source, sourceOffset, len)) {
                sourceOffset += len;
                patternOffset += len;
            } else {
                pos.setErrorIndex(sourceOffset);
                return null; // leave index as is to signal error
            }

            // now use format
            if (formats[i] == null) {   // string format
                // if at end, use longest possible match
                // otherwise uses first match to intervening string
                // does NOT recursively try all possibilities
                int tempLength = (i != maxOffset) ? offsets[i+1] : pattern.length();

                int next;
                if (patternOffset >= tempLength) {
                    next = source.length();
                }else{
                    next = source.indexOf( pattern.substring(patternOffset,tempLength), sourceOffset);
                }

                if (next < 0) {
                    pos.setErrorIndex(sourceOffset);
                    return null; // leave index as is to signal error
                } else {
                    String strValue = source.substring(sourceOffset, next);
                    if (!strValue.equals("{" + argumentNames[i] + "}"))
                        resultMap.put(argumentNames[i], source.substring(sourceOffset, next));
//                        resultArray[Integer.parseInt(argumentNames[i])] =
//                            source.substring(sourceOffset, next);
                    sourceOffset = next;
                }
            } else {
                tempStatus.setIndex(sourceOffset);
                resultMap.put(argumentNames[i], formats[i].parseObject(source, tempStatus));
//                resultArray[Integer.parseInt(argumentNames[i])] =
//                    formats[i].parseObject(source, tempStatus);
                if (tempStatus.getIndex() == sourceOffset) {
                    pos.setErrorIndex(sourceOffset);
                    return null; // leave index as is to signal error
                }
                sourceOffset = tempStatus.getIndex(); // update
            }
        }
        int len = pattern.length() - patternOffset;
        if (len == 0 || pattern.regionMatches(patternOffset,
                                              source, sourceOffset, len)) {
            pos.setIndex(sourceOffset + len);
        } else {
            pos.setErrorIndex(sourceOffset);
            return null; // leave index as is to signal error
        }
        return resultMap;
    }

    /**
     * Parses text from the beginning of the given string to produce an object
     * array.
     * The method may not use the entire text of the given string.
     * <p>
     * See the {@link #parse(String, ParsePosition)} method for more information
     * on message parsing.
     *
     * @param source A <code>String</code> whose beginning should be parsed.
     * @return An <code>Object</code> array parsed from the string.
     * @exception ParseException if the beginning of the specified string cannot be parsed.
     * @exception IllegalArgumentException if this format uses named arguments
     * @stable ICU 3.0
     */
    public Object[] parse(String source) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Object[] result = parse(source, pos);
        if (pos.getIndex() == 0) // unchanged, returned object is null
            throw new ParseException("MessageFormat parse error!",
                                     pos.getErrorIndex());

        return result;
    }

    /**
     * {@icu} Parses text from the beginning of the given string to produce a map from
     * argument to values. The method may not use the entire text of the given string.
     *
     * <p>See the {@link #parse(String, ParsePosition)} method for more information on
     * message parsing.
     *
     * @param source A <code>String</code> whose beginning should be parsed.
     * @return A <code>Map</code> parsed from the string.
     * @throws ParseException if the beginning of the specified string cannot
     *         be parsed.
     * @see #parseToMap(String, ParsePosition)
     * @stable ICU 3.8
     */
    public Map<String, Object> parseToMap(String source) throws ParseException {

        ParsePosition pos = new ParsePosition(0);
        Map<String, Object> result = parseToMap(source, pos);
        if (pos.getIndex() == 0) // unchanged, returned object is null
            throw new ParseException("MessageFormat parse error!",
                                     pos.getErrorIndex());

        return result;
    }

    /**
     * Parses text from a string to produce an object array or Map.
     * <p>
     * The method attempts to parse text starting at the index given by
     * <code>pos</code>.
     * If parsing succeeds, then the index of <code>pos</code> is updated
     * to the index after the last character used (parsing does not necessarily
     * use all characters up to the end of the string), and the parsed
     * object array is returned. The updated <code>pos</code> can be used to
     * indicate the starting point for the next call to this method.
     * If an error occurs, then the index of <code>pos</code> is not
     * changed, the error index of <code>pos</code> is set to the index of
     * the character where the error occurred, and null is returned.
     * <p>
     * See the {@link #parse(String, ParsePosition)} method for more information
     * on message parsing.
     *
     * @param source A <code>String</code>, part of which should be parsed.
     * @param pos A <code>ParsePosition</code> object with index and error
     *            index information as described above.
     * @return An <code>Object</code> parsed from the string, either an
     *         array of Object, or a Map, depending on whether named
     *         arguments are used.  This can be queried using <code>usesNamedArguments</code>.
     *         In case of error, returns null.
     * @throws NullPointerException if <code>pos</code> is null.
     * @stable ICU 3.0
     */
    public Object parseObject(String source, ParsePosition pos) {
        if (!msgPattern.hasNamedArguments()) {
            return parse(source, pos);
        } else {
            return parseToMap(source, pos);
        }
    }

    /**
     * Overrides clone.
     *
     * @return a clone of this instance.
     * @stable ICU 3.0
     */
    public Object clone() {
        MessageFormat other = (MessageFormat) super.clone();

        // clone arrays. Can't do with utility because of bug in Cloneable
        other.formats = formats.clone(); // shallow clone
        for (int i = 0; i < formats.length; ++i) {
            if (formats[i] != null)
                other.formats[i] = (Format) formats[i].clone();
        }
        // for primitives or immutables, shallow clone is enough
        other.offsets = offsets.clone();
        other.argumentNames = argumentNames.clone();

        return other;
    }

    /**
     * Overrides equals.
     * @stable ICU 3.0
     */
    public boolean equals(Object obj) {
        if (this == obj)                      // quick check
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        MessageFormat other = (MessageFormat) obj;
        return (maxOffset == other.maxOffset
                && pattern.equals(other.pattern)
                && Utility.objectEquals(msgPattern, other.msgPattern)
                && Utility.objectEquals(ulocale, other.ulocale) // does null check
                && Utility.arrayEquals(offsets, other.offsets)
                && Utility.arrayEquals(argumentNames, other.argumentNames)
                && Utility.arrayEquals(formats, other.formats));
    }

    /**
     * Overrides hashCode.
     * @stable ICU 3.0
     */
    public int hashCode() {
        return pattern.hashCode(); // enough for reasonable distribution
    }

    /**
     * Defines constants that are used as attribute keys in the
     * <code>AttributedCharacterIterator</code> returned
     * from <code>MessageFormat.formatToCharacterIterator</code>.
     *
     * @stable ICU 3.8
     */
    public static class Field extends Format.Field {

        private static final long serialVersionUID = 7510380454602616157L;

        /**
         * Create a <code>Field</code> with the specified name.
         *
         * @param name The name of the attribute
         *
         * @stable ICU 3.8
         */
        protected Field(String name) {
            super(name);
        }

        /**
         * Resolves instances being deserialized to the predefined constants.
         *
         * @return resolved MessageFormat.Field constant
         * @throws InvalidObjectException if the constant could not be resolved.
         *
         * @stable ICU 3.8
         */
        protected Object readResolve() throws InvalidObjectException {
            if (this.getClass() != MessageFormat.Field.class) {
                throw new InvalidObjectException(
                    "A subclass of MessageFormat.Field must implement readResolve.");
            }
            if (this.getName().equals(ARGUMENT.getName())) {
                return ARGUMENT;
            } else {
                throw new InvalidObjectException("Unknown attribute name.");
            }
        }

        /**
         * Constant identifying a portion of a message that was generated
         * from an argument passed into <code>formatToCharacterIterator</code>.
         * The value associated with the key will be an <code>Integer</code>
         * indicating the index in the <code>arguments</code> array of the
         * argument from which the text was generated.
         *
         * @stable ICU 3.8
         */
        public static final Field ARGUMENT = new Field("message argument field");
    }

    // ===========================privates============================

    /**
     * The locale to use for formatting numbers and dates.
     * This is no longer used, and here only for serialization compatibility.
     * @serial
     */
    // TODO: private Locale locale;

    /**
     * The locale to use for formatting numbers and dates.
     * @serial
     */
    private ULocale ulocale;

    /**
     * The string that the formatted values are to be plugged into.  In other words, this
     * is the pattern supplied on construction with all of the {} expressions taken out.
     * @serial
     */
    private String pattern = "";

    /** The initially expected number of subformats in the format */
    private static final int INITIAL_FORMATS = 10;

    /**
     * An array of formatters, which are used to format the arguments.
     * @serial
     */
    private Format[] formats = new Format[INITIAL_FORMATS];

    /**
     * True if a custom, user-provided Format object was set via setFormat() or similar API.
     */
    transient private boolean haveCustomFormats = false;

    /**
     * The positions where the results of formatting each argument are to be
     * inserted into the pattern.
     *
     * @serial
     */
    private int[] offsets = new int[INITIAL_FORMATS];

    /**
     * The argument numbers corresponding to each formatter.  (The formatters are stored
     * in the order they occur in the pattern, not in the order in which the arguments
     * are specified.)
     * @serial
     */
    // retained for backwards compatibility
    // TODO: private int[] argumentNumbers = new int[INITIAL_FORMATS];

    /**
     * The argument names corresponding to each formatter. (The formatters are
     * stored in the order they occur in the pattern, not in the order in which
     * the arguments are specified.)
     *
     * @serial
     */
    private String[] argumentNames = new String[INITIAL_FORMATS];

    /**
     * Is true iff all argument names are non-negative numbers.
     *
     * @serial
     */
    // TODO: private boolean argumentNamesAreNumeric = true;

    /**
     * One less than the number of entries in <code>offsets</code>.  Can also be thought of
     * as the index of the highest-numbered element in <code>offsets</code> that is being used.
     * All of these arrays should have the same number of elements being used as <code>offsets</code>
     * does, and so this variable suffices to tell us how many entries are in all of them.
     * @serial
     */
    private int maxOffset = -1;

    /**
     * The MessagePattern which contains the parsed structure of the pattern string.
     */
    transient private MessagePattern msgPattern;
    /**
     * Cached formatters so we can just use them whenever needed instead of creating
     * them from scratch every time.
     */
    transient private Map<Integer, Format> cachedFormatters;

    /**
     * Stock formatters. Those are used when a format is not explicitly mentioned in
     * the message. The format is inferred from the argument.
     */
    transient private Format stockDateFormatter;
    transient private Format stockNumberFormatter;

    transient private PluralSelectorProvider pluralProvider;

    private int format(int msgStart, Part part, double pluralNumber,
                       Object[] args, Map<String, Object> argsMap,
                       AppendableWrapper dest, FieldPosition fp) {
        String msgString=msgPattern.getString();
        int prevIndex=msgPattern.getPart(msgStart, part).getIndex();
        assert part.getType()==MessagePattern.Part.Type.MSG_START;
        for(int i=msgStart+1;; ++i) {
            Part.Type type=msgPattern.getPart(i, part).getType();
            int index=part.getIndex();
            dest.append(msgString, prevIndex, index);
            if(type==Part.Type.MSG_LIMIT) {
                return i;
            }
            if(type==Part.Type.SKIP_SYNTAX) {
                prevIndex=index+part.getValue();
                continue;
            }
            if(type==Part.Type.REPLACE_NUMBER) {
                prevIndex=index+part.getValue();
                if (stockNumberFormatter == null) {
                    stockNumberFormatter = NumberFormat.getInstance(ulocale);
                }
                dest.formatAndAppend(stockNumberFormatter, pluralNumber);
                continue;
            }
            if(type==Part.Type.INSERT_CHAR) {
                prevIndex=index;
                continue;
            }
            assert type==Part.Type.ARG_START : "Unexpected Part "+part+" in parsed message.";
            int argLimit=msgPattern.getPartLimit(i);
            ArgType argType=part.getArgType();
            msgPattern.getPart(++i, part);
            Object arg;
            String noArg=null;
            Object argId=null;
            if(args!=null) {
                int argNumber=part.getValue();  // ARG_NUMBER
                if (dest.attributes != null) {
                    // We only need argId if we add it into the attributes.
                    argId = new Integer(argNumber);
                }
                if(0<=argNumber && argNumber<args.length) {
                    arg=args[argNumber];
                } else {
                    arg=null;
                    noArg="{"+argNumber+"}";
                }
            } else {
                String key;
                if(part.getType()==MessagePattern.Part.Type.ARG_NAME) {
                    key=msgPattern.getSubstring(part);
                } else /* ARG_NUMBER */ {
                    key=Integer.toString(part.getValue());
                }
                argId = key;
                if(argsMap!=null && argsMap.containsKey(key)) {
                    arg=argsMap.get(key);
                } else {
                    arg=null;
                    noArg="{"+key+"}";
                }
            }
            ++i;
            int prevDestLength=dest.length;
            Format formatter = null;
            if (noArg != null) {
                dest.append(noArg);
            } else if (arg == null) {
                dest.append("null");
            } else if(cachedFormatters!=null && (formatter=cachedFormatters.get(i - 2))!=null) {
                // Handles all ArgType.SIMPLE, and formatters from setFormat() and its siblings.
                if (    formatter instanceof ChoiceFormat ||
                        formatter instanceof PluralFormat ||
                        formatter instanceof SelectFormat) {
                    // We only handle nested formats here if they were provided via setFormat() or its siblings.
                    // Otherwise they are not cached and instead handled below according to argType.
                    String subMsgString = formatter.format(arg);
                    if (subMsgString.indexOf('{') >= 0 || subMsgString.indexOf('\'') >= 0) {
                        MessageFormat subMsgFormat = new MessageFormat(subMsgString, ulocale);
                        subMsgFormat.format(0, part, 0, args, argsMap, dest, null);
                    } else if (dest.attributes == null) {
                        dest.append(subMsgString);
                    } else {
                        // This formats the argument twice, once above to get the subMsgString
                        // and then once more here.
                        // It only happens in formatToCharacterIterator()
                        // on a complex Format set via setFormat(),
                        // and only when the selected subMsgString does not need further formatting.
                        // This imitates ICU 4.6 behavior.
                        dest.formatAndAppend(formatter, arg);
                    }
                } else {
                    dest.formatAndAppend(formatter, arg);
                }
            } else if(
                    argType==ArgType.NONE ||
                    (cachedFormatters!=null && cachedFormatters.containsKey(i - 2))) {
                // ArgType.NONE, or
                // any argument which got reset to null via setFormat() or its siblings.
                if (arg instanceof Number) {
                    // format number if can
                    if (stockNumberFormatter == null) {
                        stockNumberFormatter = NumberFormat.getInstance(ulocale);
                    }
                    dest.formatAndAppend(stockNumberFormatter, arg);
                 } else if (arg instanceof Date) {
                    // format a Date if can
                    if (stockDateFormatter == null) {
                        stockDateFormatter = DateFormat.getDateTimeInstance(
                                DateFormat.SHORT, DateFormat.SHORT, ulocale);//fix
                    }
                    dest.formatAndAppend(stockDateFormatter, arg);
                } else {
                    dest.append(arg.toString());
                }
            } else if(argType==ArgType.CHOICE) {
                if (!(arg instanceof Number)) {
                    throw new IllegalArgumentException("'" + arg + "' is not a Number");
                }
                double number = ((Number)arg).doubleValue();
                int subMsgStart=findChoiceSubMessage(msgPattern, i, part, number);
                format(subMsgStart, part, 0, args, argsMap, dest, null);
            } else if(argType==ArgType.PLURAL) {
                if (!(arg instanceof Number)) {
                    throw new IllegalArgumentException("'" + arg + "' is not a Number");
                }
                double number = ((Number)arg).doubleValue();
                if (pluralProvider == null) {
                    pluralProvider = new PluralSelectorProvider(ulocale);
                }
                int subMsgStart=PluralFormat.findSubMessage(msgPattern, i, part, pluralProvider, number);
                double offset=msgPattern.getPluralOffset(subMsgStart);
                format(subMsgStart, part, number-offset, args, argsMap, dest, null);
            } else if(argType==ArgType.SELECT) {
                int subMsgStart=SelectFormat.findSubMessage(msgPattern, i, part, arg.toString());
                format(subMsgStart, part, 0, args, argsMap, dest, null);
            } else {
                // This should never happen.
                throw new IllegalStateException("unexpected argType "+argType);
            }
            fp = updateMetaData(dest, prevDestLength, fp, argId);
            prevIndex=msgPattern.getPatternIndex(argLimit);
            i=argLimit;
        }
    }

    private FieldPosition updateMetaData(AppendableWrapper dest, int prevLength,
                                         FieldPosition fp, Object argId) {
        if (dest.attributes != null && prevLength < dest.length) {
            dest.attributes.add(new AttributeAndPosition(argId, prevLength, dest.length));
        }
        if (fp != null && Field.ARGUMENT.equals(fp.getFieldAttribute())) {
            fp.setBeginIndex(prevLength);
            fp.setEndIndex(dest.length);
            return null;
        }
        return fp;
    }

    // This lives here because ICU4J does not have its own ChoiceFormat class.
    /**
     * Finds the ChoiceFormat sub-message for the given number.
     * @param pattern A MessagePattern.
     * @param partIndex the index of the first ChoiceFormat argument style part.
     * @param part A MessagePattern.Part to be reused. (Just to avoid allocation.)
     * @param number a number to be mapped to one of the ChoiceFormat argument's intervals
     * @return the sub-message start part index.
     */
    /*package*/ static int findChoiceSubMessage(
            MessagePattern pattern,
            int partIndex, MessagePattern.Part part,
            double number) {
        int count=pattern.countParts();
        int msgStart;
        // Iterate over (ARG_INT|DOUBLE, ARG_SELECTOR, message) tuples
        // until ARG_LIMIT or end of choice-only pattern.
        // Ignore the first number and selector and start the loop on the first message.
        partIndex+=2;
        for(;;) {
            // Skip but remember the current sub-message.
            msgStart=partIndex;
            partIndex=pattern.getPartLimit(partIndex);
            if(++partIndex>=count) {
                // Reached the end of the choice-only pattern.
                // Return with the last sub-message.
                break;
            }
            Part.Type type=pattern.getPart(partIndex++, part).getType();
            if(type==Part.Type.ARG_LIMIT) {
                // Reached the end of the ChoiceFormat style.
                // Return with the last sub-message.
                break;
            }
            // part is an ARG_INT or ARG_DOUBLE
            assert type.hasNumericValue();
            double boundary=pattern.getNumericValue(part);
            // Fetch the ARG_SELECTOR character.
            pattern.getPart(partIndex++, part);
            char boundaryChar=pattern.getString().charAt(part.getIndex());
            if(boundaryChar=='#' ? number<boundary : number<=boundary) {
                // The number is in the interval between the previous boundary and the current one.
                // Return with the sub-message between them.
                break;
            }
        }
        return msgStart;
    }

    /**
     * This provider helps defer instantiation of a PluralRules object
     * until we actually need to select a keyword.
     * For example, if the number matches an explicit-value selector like "=1"
     * we do not need any PluralRules.
     */
    private static final class PluralSelectorProvider implements PluralFormat.PluralSelector {
        public PluralSelectorProvider(ULocale loc) {
            locale=loc;
        }
        public String select(double number) {
            if(rules == null) {
                rules = PluralRules.forLocale(locale);
            }
            return rules.select(number);
        }
        private ULocale locale;
        private PluralRules rules;
    }

    @SuppressWarnings("unchecked")
    private void format(Object arguments, AppendableWrapper result, FieldPosition fp) {
        if ((arguments == null || arguments instanceof Map)) {
            format(null, (Map<String, Object>)arguments, result, fp);
        } else {
            format((Object[])arguments, null, result, fp);
        }
    }

    /**
     * Internal routine used by format.
     *
     * @throws IllegalArgumentException if an argument in the
     *         <code>arguments</code> map is not of the type
     *         expected by the format element(s) that use it.
     */
    private void format(Object[] arguments, Map<String, Object> argsMap,
                        AppendableWrapper dest, FieldPosition fp) {
        if (arguments != null && msgPattern.hasNamedArguments()) {
            throw new IllegalArgumentException(
                "This method is not available in MessageFormat objects " +
                "that use alphanumeric argument names.");
        }
        format(0, new Part(), 0, arguments, argsMap, dest, fp);
    }

    private void resetPattern() {
        pattern = "";
        if (msgPattern != null) {
            msgPattern.clear();
        }
        if (cachedFormatters != null) {
            cachedFormatters.clear();
        }
        haveCustomFormats = false;
        maxOffset = -1;
    }

    private static final String[] typeList =
        {"", "number", "date", "time", "choice", "spellout", "ordinal",
         "duration", "plural", "select" };
    private static final int
        TYPE_EMPTY = 0,
        TYPE_NUMBER = 1,
        TYPE_DATE = 2,
        TYPE_TIME = 3,
        TYPE_CHOICE = 4,
        TYPE_SPELLOUT = 5,
        TYPE_ORDINAL = 6,
        TYPE_DURATION = 7,
        TYPE_PLURAL = 8,
        TYPE_SELECT = 9;

    private static final String[] modifierList =
        {"", "currency", "percent", "integer"};

    private static final int
        MODIFIER_EMPTY = 0,
        MODIFIER_CURRENCY = 1,
        MODIFIER_PERCENT = 2,
        MODIFIER_INTEGER = 3;

    private static final String[] dateModifierList =
        {"", "short", "medium", "long", "full"};

    private static final int
        DATE_MODIFIER_EMPTY = 0,
        DATE_MODIFIER_SHORT = 1,
        DATE_MODIFIER_MEDIUM = 2,
        DATE_MODIFIER_LONG = 3,
        DATE_MODIFIER_FULL = 4;

    private void makeFormat(int position, int offsetNumber,
                            StringBuilder[] segments)
    {
        // get the argument number
        // int argumentNumber;
        // try {
        //     argumentNumber = Integer.parseInt(segments[1].toString()); // always unlocalized!
        // } catch (NumberFormatException e) {
        //    throw new IllegalArgumentException("can't parse argument number "
        //            + segments[1]);
        //}
        // if (argumentNumber < 0) {
        //    throw new IllegalArgumentException("negative argument number "
        //            + argumentNumber);
        //}

        // resize format information arrays if necessary
        if (offsetNumber >= formats.length) {
            int newLength = formats.length * 2;
            Format[] newFormats = new Format[newLength];
            int[] newOffsets = new int[newLength];
            String[] newArgumentNames = new String[newLength];
            System.arraycopy(formats, 0, newFormats, 0, maxOffset + 1);
            System.arraycopy(offsets, 0, newOffsets, 0, maxOffset + 1);
            System.arraycopy(argumentNames, 0, newArgumentNames, 0,
                    maxOffset + 1);
            formats = newFormats;
            offsets = newOffsets;
            argumentNames = newArgumentNames;
        }
        int oldMaxOffset = maxOffset;
        maxOffset = offsetNumber;
        offsets[offsetNumber] = segments[0].length();
        argumentNames[offsetNumber] = segments[1].toString();
        // All argument names numeric ?
        int argumentNumber;
        try {
            // always unlocalized!
             argumentNumber = Integer.parseInt(segments[1].toString());
         } catch (NumberFormatException e) {
             argumentNumber = -1;
         }

         if (argumentNumber < 0 && !isAlphaIdentifier(argumentNames[offsetNumber])) {
             throw new IllegalArgumentException(
                     "All argument identifiers have to be either non-negative " +
                     "numbers or strings following the pattern " +
                     "([:ID_Start:] [:ID_Continue:]*).\n" +
                     "For more details on these unicode sets, visit " +
                     "http://demo.icu-project.org/icu-bin/ubrowse");
         }

        // now get the format
        Format newFormat = null;
        int subformatType  = findKeyword(segments[2].toString(), typeList);
        switch (subformatType){
        case TYPE_EMPTY:
            break;
        case TYPE_NUMBER:
            switch (findKeyword(segments[3].toString(), modifierList)) {
            case MODIFIER_EMPTY:
                newFormat = NumberFormat.getInstance(ulocale);
                break;
            case MODIFIER_CURRENCY:
                newFormat = NumberFormat.getCurrencyInstance(ulocale);
                break;
            case MODIFIER_PERCENT:
                newFormat = NumberFormat.getPercentInstance(ulocale);
                break;
            case MODIFIER_INTEGER:
                newFormat = NumberFormat.getIntegerInstance(ulocale);
                break;
            default: // pattern
                newFormat = new DecimalFormat(segments[3].toString(),
                        new DecimalFormatSymbols(ulocale));
                break;
            }
            break;
        case TYPE_DATE:
            switch (findKeyword(segments[3].toString(), dateModifierList)) {
            case DATE_MODIFIER_EMPTY:
                newFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, ulocale);
                break;
            case DATE_MODIFIER_SHORT:
                newFormat = DateFormat.getDateInstance(DateFormat.SHORT, ulocale);
                break;
            case DATE_MODIFIER_MEDIUM:
                newFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, ulocale);
                break;
            case DATE_MODIFIER_LONG:
                newFormat = DateFormat.getDateInstance(DateFormat.LONG, ulocale);
                break;
            case DATE_MODIFIER_FULL:
                newFormat = DateFormat.getDateInstance(DateFormat.FULL, ulocale);
                break;
            default:
                newFormat = new SimpleDateFormat(segments[3].toString(), ulocale);
                break;
            }
            break;
        case TYPE_TIME:
            switch (findKeyword(segments[3].toString(), dateModifierList)) {
            case DATE_MODIFIER_EMPTY:
                newFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT, ulocale);
                break;
            case DATE_MODIFIER_SHORT:
                newFormat = DateFormat.getTimeInstance(DateFormat.SHORT, ulocale);
                break;
            case DATE_MODIFIER_MEDIUM:
                newFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT, ulocale);
                break;
            case DATE_MODIFIER_LONG:
                newFormat = DateFormat.getTimeInstance(DateFormat.LONG, ulocale);
                break;
            case DATE_MODIFIER_FULL:
                newFormat = DateFormat.getTimeInstance(DateFormat.FULL, ulocale);
                break;
            default:
                newFormat = new SimpleDateFormat(segments[3].toString(), ulocale);
                break;
            }
            break;
        case TYPE_CHOICE:
            try {
                newFormat = new ChoiceFormat(segments[3].toString());
            } catch (Exception e) {
                maxOffset = oldMaxOffset;
                throw new IllegalArgumentException("Choice Pattern incorrect", e);
            }
            break;
        case TYPE_SPELLOUT:
            {
                RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat(ulocale,
                        RuleBasedNumberFormat.SPELLOUT);
                String ruleset = segments[3].toString().trim();
                if (ruleset.length() != 0) {
                    try {
                        rbnf.setDefaultRuleSet(ruleset);
                    }
                    catch (Exception e) {
                        // warn invalid ruleset
                    }
                }
                newFormat = rbnf;
            }
            break;
        case TYPE_ORDINAL:
            {
                RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat(ulocale,
                        RuleBasedNumberFormat.ORDINAL);
                String ruleset = segments[3].toString().trim();
                if (ruleset.length() != 0) {
                    try {
                        rbnf.setDefaultRuleSet(ruleset);
                    }
                    catch (Exception e) {
                        // warn invalid ruleset
                    }
                }
                newFormat = rbnf;
            }
            break;
        case TYPE_DURATION:
            {
                RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat(ulocale,
                        RuleBasedNumberFormat.DURATION);
                String ruleset = segments[3].toString().trim();
                if (ruleset.length() != 0) {
                    try {
                        rbnf.setDefaultRuleSet(ruleset);
                    }
                    catch (Exception e) {
                        // warn invalid ruleset
                    }
                }
                newFormat = rbnf;
            }
            break;
        case TYPE_PLURAL:
            try {
                newFormat = new PluralFormat(ulocale, segments[3].toString());
            } catch (Exception e) {
                maxOffset = oldMaxOffset;
                throw new IllegalArgumentException("Plural Pattern incorrect", e);
                }
            break;
        case TYPE_SELECT:
            try {
                newFormat = new SelectFormat(segments[3].toString());
            } catch (Exception e) {
                maxOffset = oldMaxOffset;
                throw new IllegalArgumentException("Select Pattern incorrect", e);
            }
            break;
        default:
            maxOffset = oldMaxOffset;
            throw new IllegalArgumentException("unknown format type at ");
        }
        formats[offsetNumber] = newFormat;
        segments[1].setLength(0);   // throw away other segments
        segments[2].setLength(0);
        segments[3].setLength(0);
    }

    // Creates an appropriate Format object for the type and style passed.
    // Both arguments cannot be null.
    private Format createAppropriateFormat(String type, String style) {
        Format newFormat = null;
        int subformatType  = findKeyword(type, typeList);
        switch (subformatType){
        case TYPE_EMPTY:
            break;
        case TYPE_NUMBER:
            switch (findKeyword(style, modifierList)) {
            case MODIFIER_EMPTY:
                newFormat = NumberFormat.getInstance(ulocale);
                break;
            case MODIFIER_CURRENCY:
                newFormat = NumberFormat.getCurrencyInstance(ulocale);
                break;
            case MODIFIER_PERCENT:
                newFormat = NumberFormat.getPercentInstance(ulocale);
                break;
            case MODIFIER_INTEGER:
                newFormat = NumberFormat.getIntegerInstance(ulocale);
                break;
            default: // pattern
                newFormat = new DecimalFormat(style,
                        new DecimalFormatSymbols(ulocale));
                break;
            }
            break;
        case TYPE_DATE:
            switch (findKeyword(style, dateModifierList)) {
            case DATE_MODIFIER_EMPTY:
                newFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, ulocale);
                break;
            case DATE_MODIFIER_SHORT:
                newFormat = DateFormat.getDateInstance(DateFormat.SHORT, ulocale);
                break;
            case DATE_MODIFIER_MEDIUM:
                newFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, ulocale);
                break;
            case DATE_MODIFIER_LONG:
                newFormat = DateFormat.getDateInstance(DateFormat.LONG, ulocale);
                break;
            case DATE_MODIFIER_FULL:
                newFormat = DateFormat.getDateInstance(DateFormat.FULL, ulocale);
                break;
            default:
                newFormat = new SimpleDateFormat(style, ulocale);
                break;
            }
            break;
        case TYPE_TIME:
            switch (findKeyword(style, dateModifierList)) {
            case DATE_MODIFIER_EMPTY:
                newFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT, ulocale);
                break;
            case DATE_MODIFIER_SHORT:
                newFormat = DateFormat.getTimeInstance(DateFormat.SHORT, ulocale);
                break;
            case DATE_MODIFIER_MEDIUM:
                newFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT, ulocale);
                break;
            case DATE_MODIFIER_LONG:
                newFormat = DateFormat.getTimeInstance(DateFormat.LONG, ulocale);
                break;
            case DATE_MODIFIER_FULL:
                newFormat = DateFormat.getTimeInstance(DateFormat.FULL, ulocale);
                break;
            default:
                newFormat = new SimpleDateFormat(style, ulocale);
                break;
            }
            break;
        case TYPE_CHOICE:
            try {
                newFormat = new ChoiceFormat(style);
            } catch (Exception e) {
                throw new IllegalArgumentException("Choice Pattern incorrect", e);
            }
            break;
        case TYPE_SPELLOUT:
            {
                RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat(ulocale,
                        RuleBasedNumberFormat.SPELLOUT);
                String ruleset = style.trim();
                if (ruleset.length() != 0) {
                    try {
                        rbnf.setDefaultRuleSet(ruleset);
                    }
                    catch (Exception e) {
                        // warn invalid ruleset
                    }
                }
                newFormat = rbnf;
            }
            break;
        case TYPE_ORDINAL:
            {
                RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat(ulocale,
                        RuleBasedNumberFormat.ORDINAL);
                String ruleset = style.trim();
                if (ruleset.length() != 0) {
                    try {
                        rbnf.setDefaultRuleSet(ruleset);
                    }
                    catch (Exception e) {
                        // warn invalid ruleset
                    }
                }
                newFormat = rbnf;
            }
            break;
        case TYPE_DURATION:
            {
                RuleBasedNumberFormat rbnf = new RuleBasedNumberFormat(ulocale,
                        RuleBasedNumberFormat.DURATION);
                String ruleset = style.trim();
                if (ruleset.length() != 0) {
                    try {
                        rbnf.setDefaultRuleSet(ruleset);
                    }
                    catch (Exception e) {
                        // warn invalid ruleset
                    }
                }
                newFormat = rbnf;
            }
            break;
        case TYPE_PLURAL:
            try {
                newFormat = new PluralFormat(ulocale, style);
            } catch (Exception e) {
                throw new IllegalArgumentException("Plural Pattern incorrect", e);
            }
            break;
        case TYPE_SELECT:
            try {
                newFormat = new SelectFormat(style);
            } catch (Exception e) {
                throw new IllegalArgumentException("Select Pattern incorrect", e);
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown format type at ");
        }
        return newFormat;
    }

    private static final Locale rootLocale = new Locale("");  // Locale.ROOT only @since 1.6

    private static final int findKeyword(String s, String[] list) {
        s = s.trim().toLowerCase(rootLocale);
        for (int i = 0; i < list.length; ++i) {
            if (s.equals(list[i]))
                return i;
        }
        return -1;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        throw new NotSerializableException("com.ibm.icu.text.MessageFormat");
    }

    /**
     * After reading an object from the input stream, do a simple verification
     * to maintain class invariants.
     * @throws InvalidObjectException if the objects read from the stream is invalid.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new NotSerializableException("com.ibm.icu.text.MessageFormat");
    }
    
    private void cacheExplicitFormats() {
        if (cachedFormatters != null) {
            cachedFormatters.clear();
        }
        haveCustomFormats = false;
        Part part = new Part();
        int limit = msgPattern.countParts() - 1;
        for(int i=1; i < limit; ++i) {
            Part.Type type=msgPattern.getPart(i, part).getType();
            if(type!=Part.Type.ARG_START) {
                continue;
            }
            ArgType argType=part.getArgType();
            if(argType != ArgType.SIMPLE) {
                continue;
            }
            int index = i;
            i += 2;
            String explicitType = msgPattern.getSubstring(msgPattern.getPart(i++, part));
            String style = "";
            if (msgPattern.getPart(i, part).getType() == MessagePattern.Part.Type.ARG_STYLE_START) {
                style = msgPattern.getString().substring(part.getIndex(),
                        msgPattern.getPatternIndex(i+1)-1);
                ++i;
            }
            Format formatter = createAppropriateFormat(explicitType, style);
            setArgStartFormat(index, formatter);
        }
    }

    /**
     * Sets a formatter for a MessagePattern ARG_START part index.
     */
    private void setArgStartFormat(int argStart, Format formatter) {
        if (cachedFormatters == null) {
            cachedFormatters = new HashMap<Integer, Format>();
        }
        cachedFormatters.put(argStart, formatter);
    }

    /**
     * Sets a custom formatter for a MessagePattern ARG_START part index.
     * "Custom" formatters are provided by the user via setFormat() or similar APIs.
     */
    private void setCustomArgStartFormat(int argStart, Format formatter) {
        setArgStartFormat(argStart, formatter);
        haveCustomFormats = true;
    }

    private boolean isAlphaIdentifier(String argument) {
        if (argument.length() == 0) {
            return false;
        }
        for (int i = 0; i < argument.length(); ++i ) {
            if (i == 0 && !UCharacter.isUnicodeIdentifierStart(argument.charAt(i)) ||
                    i > 0 &&  !UCharacter.isUnicodeIdentifierPart(argument.charAt(i))){
                    return false;
                }
        }
        return true;
    }

    private static final char SINGLE_QUOTE = '\'';
    private static final char CURLY_BRACE_LEFT = '{';
    private static final char CURLY_BRACE_RIGHT = '}';

    private static final int STATE_INITIAL = 0;
    private static final int STATE_SINGLE_QUOTE = 1;
    private static final int STATE_IN_QUOTE = 2;
    private static final int STATE_MSG_ELEMENT = 3;

    /**
     * {@icu} Converts an 'apostrophe-friendly' pattern into a standard
     * pattern.  Standard patterns treat all apostrophes as
     * quotes, which is problematic in some languages, e.g.
     * French, where apostrophe is commonly used.  This utility
     * assumes that only an unpaired apostrophe immediately before
     * a brace is a true quote.  Other unpaired apostrophes are paired,
     * and the resulting standard pattern string is returned.
     *
     * <p><b>Note</b>: It is not guaranteed that the returned pattern
     * is indeed a valid pattern.  The only effect is to convert
     * between patterns having different quoting semantics.
     *
     * @param pattern the 'apostrophe-friendly' pattern to convert
     * @return the standard equivalent of the original pattern
     * @stable ICU 3.4
     */
    public static String autoQuoteApostrophe(String pattern) {
        StringBuilder buf = new StringBuilder(pattern.length() * 2);
        int state = STATE_INITIAL;
        int braceCount = 0;
        for (int i = 0, j = pattern.length(); i < j; ++i) {
            char c = pattern.charAt(i);
            switch (state) {
            case STATE_INITIAL:
                switch (c) {
                case SINGLE_QUOTE:
                    state = STATE_SINGLE_QUOTE;
                    break;
                case CURLY_BRACE_LEFT:
                    state = STATE_MSG_ELEMENT;
                    ++braceCount;
                    break;
                }
                break;
            case STATE_SINGLE_QUOTE:
                switch (c) {
                case SINGLE_QUOTE:
                    state = STATE_INITIAL;
                    break;
                case CURLY_BRACE_LEFT:
                case CURLY_BRACE_RIGHT:
                    state = STATE_IN_QUOTE;
                    break;
                default:
                    buf.append(SINGLE_QUOTE);
                    state = STATE_INITIAL;
                    break;
                }
                break;
            case STATE_IN_QUOTE:
                switch (c) {
                case SINGLE_QUOTE:
                    state = STATE_INITIAL;
                    break;
                }
                break;
            case STATE_MSG_ELEMENT:
                switch (c) {
                case CURLY_BRACE_LEFT:
                    ++braceCount;
                    break;
                case CURLY_BRACE_RIGHT:
                    if (--braceCount == 0) {
                        state = STATE_INITIAL;
                    }
                    break;
                }
                break;
            ///CLOVER:OFF
            default: // Never happens.
                break;
            ///CLOVER:ON
            }
            buf.append(c);
        }
        // End of scan
        if (state == STATE_SINGLE_QUOTE || state == STATE_IN_QUOTE) {
            buf.append(SINGLE_QUOTE);
        }
        return new String(buf);
    }

    /**
     * Convenience wrapper for Appendable, tracks the result string length.
     * Also, Appendable throws IOException, and we turn that into a RuntimeException
     * so that we need no throws clauses.
     */
    private static final class AppendableWrapper {
        public AppendableWrapper(StringBuilder sb) {
            app = sb;
            length = sb.length();
            attributes = null;
        }

        public AppendableWrapper(StringBuffer sb) {
            app = sb;
            length = sb.length();
            attributes = null;
        }

        public void useAttributes() {
            attributes = new ArrayList<AttributeAndPosition>();
        }

        public void append(CharSequence s) {
            try {
                app.append(s);
                length += s.length();
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void append(CharSequence s, int start, int limit) {
            try {
                app.append(s, start, limit);
                length += limit - start;
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void append(CharacterIterator iterator) {
            length += append(app, iterator);
        }

        public static int append(Appendable result, CharacterIterator iterator) {
            try {
                int start = iterator.getBeginIndex();
                int limit = iterator.getEndIndex();
                int length = limit - start;
                if (start < limit) {
                    result.append(iterator.first());
                    while (++start < limit) {
                        result.append(iterator.next());
                    }
                }
                return length;
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void formatAndAppend(Format formatter, Object arg) {
            if (attributes == null) {
                append(formatter.format(arg));
            } else {
                AttributedCharacterIterator formattedArg = formatter.formatToCharacterIterator(arg);
                int prevLength = length;
                append(formattedArg);
                // Copy all of the attributes from formattedArg to our attributes list.
                formattedArg.first();
                int start = formattedArg.getIndex();  // Should be 0 but might not be.
                int limit = formattedArg.getEndIndex();  // == start + length - prevLength
                int offset = prevLength - start;  // Adjust attribute indexes for the result string.
                while (start < limit) {
                    Map<Attribute, Object> map = formattedArg.getAttributes();
                    int runLimit = formattedArg.getRunLimit();
                    if (map.size() != 0) {
                        for (Map.Entry<Attribute, Object> entry : map.entrySet()) {
                           attributes.add(
                               new AttributeAndPosition(
                                   entry.getKey(), entry.getValue(),
                                   offset + start, offset + runLimit));
                        }
                    }
                    start = runLimit;
                    formattedArg.setIndex(start);
                }
            }
        }

        private Appendable app;
        private int length;
        private List<AttributeAndPosition> attributes;
    }

    private static final class AttributeAndPosition {
        /**
         * Defaults the field to Field.ARGUMENT.
         */
        public AttributeAndPosition(Object fieldValue, int startIndex, int limitIndex) {
            init(Field.ARGUMENT, fieldValue, startIndex, limitIndex);
        }

        public AttributeAndPosition(Attribute field, Object fieldValue, int startIndex, int limitIndex) {
            init(field, fieldValue, startIndex, limitIndex);
        }

        public void init(Attribute field, Object fieldValue, int startIndex, int limitIndex) {
            key = field;
            value = fieldValue;
            start = startIndex;
            limit = limitIndex;
        }

        private Attribute key;
        private Object value;
        private int start;
        private int limit;
    }
}
