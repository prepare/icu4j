/**
*******************************************************************************
* Copyright (C) 1996-2002, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
* $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/text/Collator.java,v $ 
* $Date: 2002/12/03 20:45:20 $ 
* $Revision: 1.17 $
*
*******************************************************************************
*/
package com.ibm.icu.text;

import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

import com.ibm.icu.impl.ICULocaleData;
import com.ibm.icu.impl.ICULocaleService;
import com.ibm.icu.impl.ICULocaleService.ICUResourceBundleFactory;
import com.ibm.icu.impl.ICUService;
import com.ibm.icu.impl.ICUService.Factory;

/**
* <p>Collator performs locale-sensitive string comparison. A concrete
* subclass, RuleBasedCollator, allows customization of the collation
* ordering by the use of rule sets.</p>
* 
* <p>Following the <a href=http://www.unicode.org>Unicode
* Consortium</a>'s specifications for the 
* <a href="http://www.unicode.org/unicode/reports/tr10/"> Unicode Collation
* Algorithm (UCA)</a>, there are 5 different levels of strength used
* in comparisons:
*
* <ul>
* <li>PRIMARY strength: Typically, this is used to denote differences between 
*     base characters (for example, "a" &lt; "b"). 
*     It is the strongest difference. For example, dictionaries are divided 
*     into different sections by base character. 
* <li>SECONDARY strength: Accents in the characters are considered secondary 
*     differences (for example, "as" &lt; "&agrave;s" &lt; "at"). Other 
*     differences 
*     between letters can also be considered secondary differences, depending 
*     on the language. A secondary difference is ignored when there is a 
*     primary difference anywhere in the strings.
* <li>TERTIARY strength: Upper and lower case differences in characters are 
*     distinguished at tertiary strength (for example, "ao" &lt; "Ao" &lt; 
*     "a&ograve;"). In addition, a variant of a letter differs from the base 
*     form on the tertiary strength (such as "A" and "&#9398;"). Another 
*     example is the 
*     difference between large and small Kana. A tertiary difference is ignored 
*     when there is a primary or secondary difference anywhere in the strings. 
* <li>QUATERNARY strength: When punctuation is ignored 
*     <a href=http://www-124.ibm.com/icu/userguide/Collate_Concepts.html#Ignoring_Punctuation>
*     (see Ignoring Punctuations in the user guide)</a> at PRIMARY to TERTIARY 
*     strength, an additional strength level can 
*     be used to distinguish words with and without punctuation (for example, 
*     "ab" &lt; "a-b" &lt; "aB"). 
*     This difference is ignored when there is a PRIMARY, SECONDARY or TERTIARY 
*     difference. The QUATERNARY strength should only be used if ignoring 
*     punctuation is required. 
* <li>IDENTICAL strength:
*     When all other strengths are equal, the IDENTICAL strength is used as a 
*     tiebreaker. The Unicode code point values of the NFD form of each string 
*     are compared, just in case there is no difference. 
*     For example, Hebrew cantellation marks are only distinguished at this 
*     strength. This strength should be used sparingly, as only code point 
*     value differences between two strings is an extremely rare occurrence. 
*     Using this strength substantially decreases the performance for both 
*     comparison and collation key generation APIs. This strength also 
*     increases the size of the collation key.
* </ul>
*
* Unlike the JDK, ICU4J's Collator deals only with 2 decomposition modes, 
* the canonical decomposition mode and one that does not use any decomposition.
* The compatibility decomposition mode, java.text.Collator.FULL_DECOMPOSITION
* is not supported here. If the canonical
* decomposition mode is set, the Collator handles un-normalized text properly, 
* producing the same results as if the text were normalized in NFD. If 
* canonical decomposition is turned off, it is the user's responsibility to 
* ensure that all text is already in the appropriate form before performing
* a comparison or before getting a CollationKey.</p>
*
* <p>For more information about the collation service see the 
* <a href="http://oss.software.ibm.com/icu/userguide/Collate_Intro.html">users 
* guide</a>.</p>
*
* <p>Examples of use
* <pre>
* // Get the Collator for US English and set its strength to PRIMARY
* Collator usCollator = Collator.getInstance(Locale.US);
* usCollator.setStrength(Collator.PRIMARY);
* if (usCollator.compare("abc", "ABC") == 0) {
*     System.out.println("Strings are equivalent");
* }
* 
* The following example shows how to compare two strings using the
* Collator for the default locale.
*
* // Compare two strings in the default locale
* Collator myCollator = Collator.getInstance();
* myCollator.setDecomposition(NO_DECOMPOSITION);
* if (myCollator.compare("&agrave;&#92;u0325", "a&#92;u0325&#768;") != 0) {
*     System.out.println("&agrave;&#92;u0325 is not equals to a&#92;u0325&#768; without decomposition");
*     myCollator.setDecomposition(CANONICAL_DECOMPOSITION);
*     if (myCollator.compare("&agrave;&#92;u0325", "a&#92;u0325&#768;") != 0) {
*         System.out.println("Error: &agrave;&#92;u0325 should be equals to a&#92;u0325&#768; with decomposition");
*     }
*     else {
*         System.out.println("&agrave;&#92;u0325 is equals to a&#92;u0325&#768; with decomposition");
*     }
* }
* else {
*     System.out.println("Error: &agrave;&#92;u0325 should be not equals to a&#92;u0325&#768; without decomposition");
* }
* </pre>
* </p>
* @see RuleBasedCollator
* @see CollationKey
* @author Syn Wee Quek
* @draft ICU 2.2 
*/
public abstract class Collator implements Comparator, Cloneable
{     
    // public data members ---------------------------------------------------
        
    /**
     * Strongest collator strength value. Typically used to denote differences 
     * between base characters. See class documentation for more explanation.
     * @see #setStrength
     * @see #getStrength
     * @draft ICU 2.2
     */
    public final static int PRIMARY = 0;

    /**
     * Second level collator strength value. 
     * Accents in the characters are considered secondary differences.
     * Other differences between letters can also be considered secondary 
     * differences, depending on the language. 
     * See class documentation for more explanation.
     * @see #setStrength
     * @see #getStrength
     * @draft ICU 2.2
     */
    public final static int SECONDARY = 1;

    /**
     * Third level collator strength value. 
     * Upper and lower case differences in characters are distinguished at this
     * strength level. In addition, a variant of a letter differs from the base 
     * form on the tertiary level.
     * See class documentation for more explanation.
     * @see #setStrength
     * @see #getStrength
     * @draft ICU 2.2
     */
    public final static int TERTIARY = 2;                            

    /**
     * Fourth level collator strength value. 
     * When punctuation is ignored 
     * <a href="http://www-124.ibm.com/icu/userguide/Collate_Concepts.html#Ignoring_Punctuation">
     * (see Ignoring Punctuations in the user guide)</a> at PRIMARY to TERTIARY 
     * strength, an additional strength level can 
     * be used to distinguish words with and without punctuation.
     * See class documentation for more explanation.
     * @see #setStrength
     * @see #getStrength
     * @draft ICU 2.2
     */
    public final static int QUATERNARY = 3;

    /**
     * <p>
     * Smallest Collator strength value. When all other strengths are equal, 
     * the IDENTICAL strength is used as a tiebreaker. The Unicode code point 
     * values of the NFD form of each string are compared, just in case there 
     * is no difference. 
     * See class documentation for more explanation.
     * </p>
     * <p>
     * Note this value is different from JDK's
     * </p>
     * @draft ICU 2.2
     */
    public final static int IDENTICAL = 15;

    /**
     * <p>Decomposition mode value. With NO_DECOMPOSITION set, Strings
     * will not be decomposed for collation. This is the default
     * decomposition setting unless otherwise specified by the locale
     * used to create the Collator.</p>
     *
     * <p><strong>Note</strong> this value is different from the JDK's.</p>
     * @see #CANONICAL_DECOMPOSITION
     * @see #getDecomposition
     * @see #setDecomposition
     * @draft ICU 2.2 
     */
    public final static int NO_DECOMPOSITION = 16;

    /**
     * <p>Decomposition mode value. With CANONICAL_DECOMPOSITION set,
     * characters that are canonical variants according to the Unicode standard
     * will be decomposed for collation.</p>
     *
     * <p>CANONICAL_DECOMPOSITION corresponds to Normalization Form D as
     * described in <a href="http://www.unicode.org/unicode/reports/tr15/">
     * Unicode Technical Report #15</a>.
     * </p>
     * @see #NO_DECOMPOSITION
     * @see #getDecomposition
     * @see #setDecomposition
     * @draft ICU 2.2 
     */
    public final static int CANONICAL_DECOMPOSITION = 17;
    
    // public methods --------------------------------------------------------
    
    // public setters --------------------------------------------------------
    
    /**
     * <p>Sets this Collator's strength property. The strength property 
     * determines the minimum level of difference considered significant 
     * during comparison.</p>
     * 
     * <p>The default strength for the Collator is TERTIARY, unless specified 
     * otherwise by the locale used to create the Collator.</p>
     *
     * <p>See the Collator class description for an example of use.</p>
     * @param new Strength the new strength value.
     * @see #getStrength
     * @see #PRIMARY
     * @see #SECONDARY
     * @see #TERTIARY
     * @see #QUATERNARY
     * @see #IDENTICAL
     * @exception IllegalArgumentException if the new strength value is not one 
     *                of PRIMARY, SECONDARY, TERTIARY, QUATERNARY or IDENTICAL.
     * @draft ICU 2.2
     */
    public void setStrength(int newStrength) 
    {
        if ((newStrength != PRIMARY) &&
            (newStrength != SECONDARY) &&
            (newStrength != TERTIARY) &&
            (newStrength != QUATERNARY) &&
            (newStrength != IDENTICAL)) {
            throw new IllegalArgumentException("Incorrect comparison level.");
        }
        m_strength_ = newStrength;
    }
    
    /**
     * <p>Set the decomposition mode of this Collator.  Setting this
     * decomposition property with CANONICAL_DECOMPOSITION allows the
     * Collator to handle un-normalized text properly, producing the
     * same results as if the text were normalized. If
     * NO_DECOMPOSITION is set, it is the user's responsibility to
     * insure that all text is already in the appropriate form before
     * a comparison or before getting a CollationKey. Adjusting
     * decomposition mode allows the user to select between faster and
     * more complete collation behavior.</p>
     * 
     * <p>Since a great many of the world's languages do not require
     * text normalization, most locales set NO_DECOMPOSITION as the
     * default decomposition mode.</p>
     * 
     * The default decompositon mode for the Collator is
     * NO_DECOMPOSITON, unless specified otherwise by the locale used
     * to create the Collator.</p>
     * 
     * <p>See getDecomposition for a description of decomposition
     * mode.</p>
     * 
     * @param decomposition the new decomposition mode
     * @see #getDecomposition
     * @see #NO_DECOMPOSITION
     * @see #CANONICAL_DECOMPOSITION
     * @exception IllegalArgumentException If the given value is not a valid 
     *            decomposition mode.
     * @draft ICU 2.2 
     */
    public void setDecomposition(int decomposition) 
    {
        if ((decomposition != NO_DECOMPOSITION) &&
            (decomposition != CANONICAL_DECOMPOSITION)) {
            throw new IllegalArgumentException("Wrong decomposition mode.");
        }
        m_decomposition_ = decomposition;
    }
    
    // public getters --------------------------------------------------------
    
    /**
     * Gets the Collator for the current default locale.
     * The default locale is determined by java.util.Locale.getDefault().
     * @return the Collator for the default locale (for example, en_US) if it
     *         is created successfully. Otherwise if there is no Collator
     *         associated with the current locale, the default UCA collator 
     *         will be returned.
     * @see java.util.Locale#getDefault()
     * @see #getInstance(Locale)
     * @draft ICU 2.2
     */
    public static final Collator getInstance() 
    {
        return getInstance(Locale.getDefault());
    }

    // begin registry stuff

    /**
     * Gets the Collator for the desired locale.
     * @param locale the desired locale.
     * @return Collator for the desired locale if it is created successfully.
     *         Otherwise if there is no Collator
     *         associated with the current locale, a default UCA collator will 
     *         be returned.
     * @see java.util.Locale
     * @see java.util.ResourceBundle
     * @see #getInstance()
     * @draft ICU 2.2
     */
    public static final Collator getInstance(Locale locale)
    {
        if (service == null) {
            return new RuleBasedCollator(locale);
        } else {
            try {
                return (Collator)((Collator)service.get(locale)).clone();
            }
            catch (CloneNotSupportedException e) {
                throw new InternalError(e.getMessage());
            }
        }
    }

    private static ICULocaleService service;
    private static ICULocaleService getService() {
        if (service == null) {
            ICULocaleService newService = new ICULocaleService("Collator");

            class CollatorFactory extends ICUResourceBundleFactory {
                protected Object handleCreate(Locale loc, int kind, ICUService service) {
                    return new RuleBasedCollator(loc);
                }
            }
            newService.registerFactory(new CollatorFactory());

            synchronized (Collator.class) {
                if (service == null) {
                    service = newService;
                }
            }
        }
        return service;
    }

    /* @prototype */
    /* public */ static final Object register(Collator collator, Locale locale) {
        return getService().registerObject(collator, locale);
    }

    /* @prototype */
    /* public */ static final boolean unregister(Object registryKey) {
        if (service != null) {
            return service.unregisterFactory((Factory)registryKey);
        }
        return false;
    }
    
    /**
     * Get the set of Locales for which Collators are installed. 
     * @return the list of available locales which collators are installed.
     *         The set of Locales returned will be the ones registered in 
     *         ICULocaleService, if ICULocaleService is used. Otherwise it will
     *         be the set of Locales that are installed with ICU4J.
     * @draft ICU 2.4
     */
    public static final Locale[] getAvailableLocales() {
        if (service != null) {
            return service.getAvailableLocales();
        } else {
            return ICULocaleData.getAvailableLocales();
        }
    }

    /* @prototype */
    /* public */ static final Map getDisplayNames(Locale locale) {
        return getService().getDisplayNames(locale);
    }

    // end registry stuff

    /**
     * <p>Returns this Collator's strength property. The strength property 
     * determines the minimum level of difference considered significant.
     * </p>
     * <p>
     * See the Collator class description for more details.
     * </p>
     * @return this Collator's current strength property.
     * @see #setStrength
     * @see #PRIMARY
     * @see #SECONDARY
     * @see #TERTIARY
     * @see #QUATERNARY
     * @see #IDENTICAL
     * @draft ICU 2.2
     */
    public int getStrength()
    {
        return m_strength_;
    }
    
    /**
     * <p>
     * Get the decomposition mode of this Collator. Decomposition mode
     * determines how Unicode composed characters are handled. 
     * </p>
     * <p>
     * See the Collator class description for more details.
     * </p>
     * @return the decomposition mode
     * @see #setDecomposition
     * @see #NO_DECOMPOSITION
     * @see #CANONICAL_DECOMPOSITION
     * @draft ICU 2.2
     */
    public int getDecomposition()
    {
        return m_decomposition_;
    }
    
    /**
     * <p>
     * Compares the source text String to the target text String according to 
     * this Collator's rules, strength and decomposition mode.
     * Returns an integer less than, 
     * equal to or greater than zero depending on whether the source String is 
     * less than, equal to or greater than the target String. See the Collator
     * class description for an example of use.
     * </p>
     * @param source the source String.
     * @param target the target String.
     * @return Returns an integer value. Value is less than zero if source is 
     *         less than target, value is zero if source and target are equal, 
     *         value is greater than zero if source is greater than target.
     * @see CollationKey
     * @see #getCollationKey
     * @exception NullPointerException thrown if either arguments is null.
     *            IllegalArgumentException thrown if either source or target is
     *            not of the class String.
     * @draft ICU 2.2
     */
    public int compare(Object source, Object target)
    {
        if (!(source instanceof String) || !(target instanceof String)) {
            throw new IllegalArgumentException("Arguments have to be of type String");
        }
        return compare((String)source, (String)target);
    }
    
    // public other methods -------------------------------------------------

    /**
     * Convenience method for comparing the equality of two text Strings using
     * this Collator's rules, strength and decomposition mode.
     * @param source the source string to be compared.
     * @param target the target string to be compared.
     * @return true if the strings are equal according to the collation
     *         rules, otherwise false.
     * @see #compare
     * @exception NullPointerException thrown if either arguments is null.
     * @draft ICU 2.2
     */
    public boolean equals(String source, String target) 
    {
        return (compare(source, target) == 0);
    }

	  /**
	   * Get an UnicodeSet that contains all the characters and sequences 
	   * tailored in this collator.
	   * @return a pointer to a UnicodeSet object containing all the 
	   *         code points and sequences that may sort differently than
	   *         in the UCA. 
	   * @draft ICU 2.4
	   */
  	public UnicodeSet getTailoredSet()
  	{
  		return new UnicodeSet(0, 0x10FFFF);
  	}

    /**
     * Compares the equality of two Collators.
     * @param that the Collator to be compared with this.
     * @return true if this Collator is the same as that Collator;
     *         false otherwise.
     * @draft ICU 2.2
     */
    public abstract boolean equals(Object that);
    
    // public abstract methods -----------------------------------------------

    /**
     * Generates a unique hash code for this Collator.
     * @draft ICU 2.2
     * @return 32 bit unique hash code
     */
    public abstract int hashCode();

    /**
     * <p>
     * Compares the source text String to the target text String according to 
     * this Collator's rules, strength and decomposition mode.
     * Returns an integer less than, 
     * equal to or greater than zero depending on whether the source String is 
     * less than, equal to or greater than the target String. See the Collator
     * class description for an example of use.
     * </p>
     * @param source the source String.
     * @param target the target String.
     * @return Returns an integer value. Value is less than zero if source is 
     *         less than target, value is zero if source and target are equal, 
     *         value is greater than zero if source is greater than target.
     * @see CollationKey
     * @see #getCollationKey
     * @exception NullPointerException thrown if either arguments is null.
     * @draft ICU 2.2
     */
    public abstract int compare(String source, String target);

    /**
     * <p>
     * Transforms the String into a CollationKey suitable for efficient
     * repeated comparison.  The resulting key depends on the collator's
     * rules, strength and decomposition mode.
     * </p> 
     * <p>See the CollationKey class documentation for more information.</p>
     * @param source the string to be transformed into a CollationKey.
     * @return the CollationKey for the given String based on this Collator's 
     *         collation rules. If the source String is null, a null 
     *         CollationKey is returned.
     * @see CollationKey
     * @see #compare(String, String)
     * @draft ICU 2.2
     */
    public abstract CollationKey getCollationKey(String source);
    
    // protected constructor -------------------------------------------------
  
    /**
     * Empty default constructor to make javadocs happy
     * @draft ICU 2.4
     */
    protected Collator()
    {
    }
    
    // private data members --------------------------------------------------
    
    /**
     * Collation strength
     */
    private int m_strength_ = TERTIARY;

    /**
     * Decomposition mode
     */ 
    private int m_decomposition_ = CANONICAL_DECOMPOSITION;
}

