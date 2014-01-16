/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.text;

import com.ibm.icu.impl.stt.Environment;
import com.ibm.icu.impl.stt.Expert;
import com.ibm.icu.impl.stt.ExpertFactory;
import com.ibm.icu.impl.stt.handlers.Comma;
import com.ibm.icu.impl.stt.handlers.Email;
import com.ibm.icu.impl.stt.handlers.File;
import com.ibm.icu.impl.stt.handlers.Java;
import com.ibm.icu.impl.stt.handlers.Math;
import com.ibm.icu.impl.stt.handlers.Property;
import com.ibm.icu.impl.stt.handlers.Regex;
import com.ibm.icu.impl.stt.handlers.Sql;
import com.ibm.icu.impl.stt.handlers.SystemAndUser;
import com.ibm.icu.impl.stt.handlers.TypeHandler;
import com.ibm.icu.impl.stt.handlers.Underscore;
import com.ibm.icu.impl.stt.handlers.Url;
import com.ibm.icu.impl.stt.handlers.XPath;
import com.ibm.icu.util.ULocale;

/**
 * Provides the public API for applying BiDi controls into structured text.
 *<p>
 * There are various types of structured text specified by {@link StructuredTypes}.
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
 * This class is the main tool for processing structured text.
 * It facilitates handling several types of structured text, each type being
 * handled by a specific {@link StructuredTypes}:
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
 * <li>XPath</li>
 * <li>Java code</li>
 * <li>SQL statements</li>
 * <li>RTL arithmetic expressions</li>
 * </ul>
 * <p>
 * For each of these types there is an enumerated type ({@link StructuredTypes}) used 
 * as argument in some methods of <b>BidiStructuredProcessor</b> to specify the type of processing required.
 * </p>
 * <h2>Abbreviations used</h2>
 * 
 * <dl>
 * <dt><b>UBA</b>
 * <dd>Unicode Bidirectional Algorithm
 * 
 * <dt><b>BiDi</b>
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
 */
public class BidiStructuredProcessor 
{
    /**
     * The default set of separators used to segment a string: dot, colon, slash, backslash.
     */
    private static final String defaultSeparators = ".:/\\";

    /**
     * Enumeration of supported structured text types.
     */
    public enum StructuredTypes 
    {
        /**
         * Structured text handler identifier for property file statements. It
         * expects the following format:
         * 
         * <pre>
         * name = value
         * </pre>
         */
        PROPERTY("property",Property.class.getName()),
        /**
         * Structured text handler identifier for compound names. It expects text to
         * be made of one or more parts separated by underscores:
         * 
         * <pre>
         * part1_part2_part3
         * </pre>
         */
        UNDERSCORE("underscore",Underscore.class.getName()),
        /**
         * Structured text handler identifier for comma-delimited lists, such as:
         * 
         * <pre>
         *  part1,part2,part3
         * </pre>
         */
        COMMA_DELIMITED("comma",Comma.class.getName()),
        /**
         * Structured text handler identifier for strings with the following format:
         * 
         * <pre>
         * system(user)
         * </pre>
         */
        SYSTEM_USER("system",SystemAndUser.class.getName()),
        /**
         * Structured text handler identifier for directory and file paths.
         */
        FILE("file",File.class.getName()),
        /**
         * Structured text handler identifier for e-mail addresses.
         */
        EMAIL("email",Email.class.getName()),
        /**
         * Structured text handler identifier for URLs.
         */
        URL("url",Url.class.getName()),
        /**
         * Structured text handler identifier for regular expressions, possibly
         * spanning multiple lines.
         */
        REGEXP("regex",Regex.class.getName()),
        /**
         * Structured text handler identifier for XPath expressions.
         */
        XPATH("xpath",XPath.class.getName()),
        /**
         * Structured text handler identifier for Java code, possibly spanning
         * multiple lines.
         */
        JAVA("java",Java.class.getName()),
        /**
         * Structured text handler identifier for SQL statements, possibly spanning
         * multiple lines.
         */
        SQL("sql",Sql.class.getName()),
        /**
         * Structured text handler identifier for arithmetic expressions, possibly
         * with a RTL base direction.
         */
        RTL_ARITHMETIC("math",Math.class.getName());

        private String name;
        private String className;
        private TypeHandler handler = null;

        StructuredTypes(String name, String className) 
        {
          this.name = name;
          this.className = className;
        }

        public String getName() 
        {
          return this.name;
        }

        protected String getClassName()
        {
            return this.className;
        }
        
        public static StructuredTypes fromString(String text) 
        {
          if (text != null) 
          {
            for (StructuredTypes b : StructuredTypes.values()) 
            {
              if (text.equalsIgnoreCase(b.name)) 
              {
                return b;
              }
            }
          }
          return null;
        }
        
        public TypeHandler getInstance()
        {
            if(handler != null)
            {
                return handler;
            }
            
            Class<?> neededClass = null;
            try 
            {
                neededClass = Class.forName(this.getClassName());
            } 
            catch (ClassNotFoundException e) 
            {
                // should only happen in development when the class is specified incorrectly up above
                e.printStackTrace();
            } 
            try 
            {
                handler = (TypeHandler) neededClass.newInstance();
            } 
            // these exceptions should only happen in development when the class is specified incorrectly up above
            catch (IllegalAccessException e) 
            {
                e.printStackTrace();
            } 
            catch (InstantiationException e) 
            {
                e.printStackTrace();
            }
            return handler;
        }
      }

    
    /* original constants
    public static final int ORIENT_LTR = 0;
    public static final int ORIENT_RTL = 1;
    public static final int ORIENT_CONTEXTUAL = 1 << 1;
    public static final int ORIENT_CONTEXTUAL_LTR = ORIENT_CONTEXTUAL | ORIENT_LTR;
    public static final int ORIENT_CONTEXTUAL_RTL = ORIENT_CONTEXTUAL | ORIENT_RTL;
    public static final int ORIENT_UNKNOWN = 1 << 2;
    public static final int ORIENT_IGNORE = 1 << 3;
     */
    /**
     * Enumeration of accepted orientations.
     *
     */
    public enum Orientation
    {
        /**
         * Specifies that a GUI component should display text Left-To-Right.
         */
        LTR(true, false, false),
        
        /**
         * Specifies that a GUI component should display text Right-To-Left.
         */
        RTL(false, true, false),
        
        /**
         * Specifies that a GUI component should display text depending on the context.
         */
        CONTEXTUAL(false, false, true),
        
        /**
         * Specifies that a GUI component should display text depending on the
         * context with default orientation being Left-To-Right.
         */
        CONTEXTUAL_LTR(true, false, true),
        
        /**
         * Specifies that a GUI component should display text depending on the
         * context with default orientation being Right-To-Left (value is 3).
         */
        CONTEXTUAL_RTL(false, true, true),
        
        /**
         * Used when the orientation of a GUI component is not known.
         */
        UNKNOWN(false, false, false),
        
        /**
         * Used to specify that no directional formatting characters should be added
         * as prefix or suffix.
         */
        IGNORE(false, false, false);
        
        ; // end list of enums
        
        private boolean ltr;
        private boolean rtl;
        private boolean contextual;
        
        Orientation(boolean ltr, boolean rtl, boolean contextual)
        {
            this.ltr = ltr;
            this.rtl = rtl;
            this.contextual = contextual;
        }
        
        public boolean isLtr()
        {
            return ltr;
        }
        
        public boolean isRtl()
        {
            return rtl;
        }
        
        public boolean isContextual()
        {
            return contextual;
        }
    }

    /**
     * the stateful expert associated when instantiating a BidiStructuredProcessor
     */
    private Expert statefulExpert = null;
    
    /**
     * protected constructor for use by:<br/>
     * <br/>{@link #getInstance(StructuredTypes)}<br/>
     * {@link #getInstance(StructuredTypes, Environment)} or<br/> 
     * {@link #getInstance(StructuredTypes, ULocale, Orientation, boolean)}
     * 
     * @param newStatefulExpert
     *          Expert - the expert implementation that supports this instance
     */
    protected BidiStructuredProcessor(Expert newStatefulExpert) 
    {
        super();
        
        this.setStatefulExpert(newStatefulExpert);
    }

    /**
     * Transforms a string that has a particular semantic meaning to render it
     * correctly on BiDi locales with specific usage details for target Locale, 
     * text orientation & if the UI is RTL centric. 
     * <br/><br/>
     * For more details, see {@link #transform(String)}
     * 
     * @param str
     *          the <i>lean</i> text to process.
     * @param textType
     *          an identifier for the structured text handler appropriate for
     *          the type of the text submitted. It must be one of the
     *          identifiers defined in {@link BidiStructuredProcessor}.
     * @param locale
     *          ULocale - Locale information
     * @param orientation
     *          Orientation - text orientation
     * @param mirrored
     *          boolean - true if target UI is RTL
     * 
     * @return the processed string (<i>full</i> text).
     */
    public static String transform(String str, StructuredTypes textType, ULocale locale, Orientation orientation, boolean mirrored)
    {
        Environment env = new Environment(locale, mirrored, orientation);
        Expert expert = ExpertFactory.getExpert(textType, env);
        
        return transform(str, expert);
    }
    
    /**
     * Transforms the given (<i>lean</i>) text and returns a string with
     * appropriate directional formatting characters (<i>full</i> text). This is
     * equivalent to calling {@link #transform(String str, String separators)}
     * with the default set of separators.
     * <p>
     * The processing adds directional formatting characters so that
     * presentation using the Unicode Bidirectional Algorithm will provide the
     * expected result. The text is segmented according to the provided
     * separators. Each segment has the Unicode BiDi Algorithm applied to it,
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
     * @return the transformed string (<i>full</i> text).
     */
    public static String transform(String str) 
    {
        return transform(str, defaultSeparators);
    }

    /**
     * Transforms a string that has a particular semantic meaning to render it
     * correctly on BiDi locales. For more details, see {@link #transform(String)}
     * .
     * 
     * @param str
     *            the <i>lean</i> text to process.
     * @param separators
     *            characters by which the string will be segmented.
     * 
     * @return the transformed string (<i>full</i> text).
     */
    public static String transform(String str, String separators) 
    {
        if ((str == null) || (str.length() <= 1))
        {
            return str;
        }

        // do not process a string that has already been processed.
        if (alreadyProcessed(str))
            return str;

        // do not process a string if all the following conditions are true:
        // a) it has no RTL characters
        // b) it starts with a LTR character
        // c) it ends with a LTR character or a digit
        boolean isStringBidi = false;
        int strLength = str.length();
        char c;
        for (int i = 0; i < strLength; i++) 
        {
            c = str.charAt(i);
            if (((c >= 0x05d0) && (c <= 0x07b1)) || ((c >= 0xfb1d) && (c <= 0xfefc))) 
            {
                isStringBidi = true;
                break;
            }
        }
        
        while (!isStringBidi) 
        {
            if (!Character.isLetter(str.charAt(0)))
            {
                break;
            }
            c = str.charAt(strLength - 1);
            if (!Character.isDigit(c) && !Character.isLetter(c))
            {
                break;
            }
            return str;
        }

        if (separators == null)
            separators = defaultSeparators;

        // make sure that LRE/PDF are added around the string
        Environment env = new Environment(null, false, BidiStructuredProcessor.Orientation.UNKNOWN);
        TypeHandler handler = new TypeHandler(separators);
        
        return transform(str, ExpertFactory.getStatefulExpert(handler, env));
    }

    /**
     * Transforms a string that has a particular semantic meaning to render it
     * correctly on BiDi locales. For more details, see {@link #transform(String)}
     * 
     * @param str
     *            the <i>lean</i> text to process.
     * @param textType
     *            an identifier for the structured text handler appropriate for
     *            the type of the text submitted. It must be one of the
     *            identifiers defined in {@link BidiStructuredProcessor}.
     * 
     * @return the processed string (<i>full</i> text).
     */
    public static String transform(String str, StructuredTypes textType) 
    {
        Environment env = new Environment(null, false, BidiStructuredProcessor.Orientation.UNKNOWN);
        
        return transform(str, ExpertFactory.getExpert(textType, env));
    }

    
    /**
     * actual transform logic called from convenience methods
     * 
     * @param str
     *          String - the String to transform
     * @param env
     *          Environment - the environment details to control the transform
     * @param textType
     *          StructuredTypes - the supported format of the String
     * @return 
     *          String - the transformed String
     */
    
    private static String transform(String str, Expert expert)
    {
        if ((str == null) || (str.length() <= 1))
            return str;

        // do not process a string that has already been processed.
        if (alreadyProcessed(str))
            return str;

        // make sure that LRE/PDF are added around the string
        if (!expert.getEnvironment().isProcessingNeeded())
            return str;
        
        //Expert expert = ExpertFactory.getExpert(textType, env);
        
        return expert.leanToFullText(str);
    }

    /**
     * Check to see if the String has already been processed
     * @param str
     *          String - the String to transform
     * @return
     *          boolean - true if the String has been previously transformed
     */
    private static boolean alreadyProcessed(String str)
    {
        char c = str.charAt(0);
        if (((c == Expert.LRE) || (c == Expert.RLE)) && str.charAt(str.length() - 1) == Expert.PDF)
            return true;
        
        return false;
    }
    
    /**
     * Returns a string containing all the default separator characters to be
     * used to segment a given string.
     * 
     * @return string containing all separators.
     */
    public static String getDefaultSeparators() 
    {
        return defaultSeparators;
    }

    /**
     * Returns an instance of a BidiStructuredProcessor suitable for stateful processing. 
     * @param textType
     *          StructuredTypes - the desired type for the stateful processing
     * @param locale
     *          ULocale - the Locale associated with future transforms
     * @param orientation
     *          Orientation - the text orientation of future transforms
     * @param mirrored
     *          boolean - true if the desired target is generally RTL
     * @return
     *          BidiStructuredProcessor - an instance of of stateful BidiStructuredProcessor
     */
    public static BidiStructuredProcessor getInstance(StructuredTypes textType, ULocale locale, Orientation orientation, boolean mirrored)
    {
        Environment env = new Environment(locale, mirrored, orientation);
        return getInstance(textType, env);
    }
    
    /**
     * Returns an instance of a BidiStructuredProcessor suitable for stateful processing when only the text type is known.
     * 
     * @param textType
     *          StructuredTypes - the desired type for the stateful processing
     * @return
     *          BidiStructuredProcessor - an instance of of stateful BidiStructuredProcessor
     */
    public static BidiStructuredProcessor getInstance(StructuredTypes textType)
    {
        Environment env = new Environment(null, false, BidiStructuredProcessor.Orientation.UNKNOWN);
        return getInstance(textType, env);
    }
    
    private static BidiStructuredProcessor getInstance(StructuredTypes textType, Environment env)
    {
        return new BidiStructuredProcessor(ExpertFactory.getStatefulExpert(textType.getInstance(), env));
    }

    
    public String transformWithState(String str)
    {
        return getStatefulExpert().leanToFullText(str);
    }
    
    /**
     * @return the statefulExpert
     */
    protected Expert getStatefulExpert() 
    {
        if(statefulExpert == null)  // should never happen
            throw new IllegalStateException("no expert associated with this BidiStructuredProcessor"); 
            
        return statefulExpert;
    }

    /**
     * @param statefulExpert the statefulExpert to set
     */
    protected void setStatefulExpert(Expert statefulExpert) 
    {
        this.statefulExpert = statefulExpert;
    }
}

