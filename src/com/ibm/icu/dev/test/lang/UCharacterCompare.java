/**
*******************************************************************************
* Copyright (C) 1996-2000, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
* $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/test/lang/UCharacterCompare.java,v $ 
* $Date: 2000/12/26 20:01:08 $ 
* $Revision: 1.1 $
*
*******************************************************************************
*/

package com.ibm.icu.test.text;

import com.ibm.icu.text.UCharacter;
import com.ibm.icu.text.UCharacterCategoryEnum;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Enumeration;

/**
* A class to compare the difference in methods between java.lang.Character and
* UCharacter
* @author Syn Wee Quek
* @since oct 06 2000
* @see com.ibm.icu.text.UCharacter
*/

public final class UCharacterCompare
{ 
  // private variables ================================================
  
  private static Hashtable m_hashtable_ = new Hashtable();
  
  // public methods ======================================================
  
  /**
  * Main testing method
  */
  public static void main(String arg[])
  {
    try
    {
      FileWriter f;
      if (arg.length == 0)
        f = new FileWriter("compare.txt");
      else
        f = new FileWriter(arg[0]);
      PrintWriter p = new PrintWriter(f);
      p.print("char  character name                                                           ");
      p.println("method name               ucharacter character");
      for (char i = Character.MIN_VALUE; i < Character.MAX_VALUE; i ++)
      {
        if (UCharacter.isDefined(i) != Character.isDefined(i))
          trackDifference(p, i, "isDefined()", "" + UCharacter.isDefined(i), 
                          "" + Character.isDefined(i));
        else
        {
          if (UCharacter.digit(i, 10) != Character.digit(i, 10))
            trackDifference(p, i, "digit()", "" + UCharacter.digit(i, 10),
                            "" + Character.digit(i, 10));
          if (UCharacter.getNumericValue(i) != Character.getNumericValue(i))
            trackDifference(p, i, "getNumericValue()", 
                            "" + UCharacter.getNumericValue(i),
                            "" + Character.getNumericValue(i));
          if (!compareType(UCharacter.getType(i), Character.getType(i)))
            trackDifference(p, i, "getType()", "" + UCharacter.getType(i),
                            "" + Character.getType(i));
          if (UCharacter.isDigit(i) != Character.isDigit(i))
            trackDifference(p, i, "isDigit()",
                            "" + UCharacter.isDigit(i), 
                            "" + Character.isDigit(i));
          if (UCharacter.isISOControl(i) != Character.isISOControl(i))
            trackDifference(p, i, "isISOControl()", 
                            "" + UCharacter.isISOControl(i),
                            "" + Character.isISOControl(i));
          if (UCharacter.isLetter(i) != Character.isLetter(i))
            trackDifference(p, i, "isLetter()", "" + UCharacter.isLetter(i),
                            "" + Character.isLetter(i));
          if (UCharacter.isLetterOrDigit(i) != Character.isLetterOrDigit(i))
            trackDifference(p, i, "isLetterOrDigit()", 
                            "" + UCharacter.isLetterOrDigit(i),
                            "" + Character.isLetterOrDigit(i));
          if (UCharacter.isLowerCase(i) != Character.isLowerCase(i))
            trackDifference(p, i, "isLowerCase()", 
                            "" + UCharacter.isLowerCase(i),
                            "" + Character.isLowerCase(i));
          if (UCharacter.isWhitespace(i) != Character.isWhitespace(i))
            trackDifference(p, i, "isWhitespace()", 
                            "" + UCharacter.isWhitespace(i),
                            "" + Character.isWhitespace(i));
          if (UCharacter.isSpaceChar(i) != Character.isSpaceChar(i))
            trackDifference(p, i, "isSpaceChar()", 
                            "" + UCharacter.isSpaceChar(i),
                            "" + Character.isSpaceChar(i));
          if (UCharacter.isTitleCase(i) != Character.isTitleCase(i))
            trackDifference(p, i, "isTitleChar()", 
                            "" + UCharacter.isTitleCase(i),
                            "" + Character.isTitleCase(i));
          if (UCharacter.isUnicodeIdentifierPart(i) != 
              Character.isUnicodeIdentifierPart(i))
            trackDifference(p, i, "isUnicodeIdentifierPart()", 
                            "" + UCharacter.isUnicodeIdentifierPart(i),
                            "" + Character.isUnicodeIdentifierPart(i));
          if (UCharacter.isUnicodeIdentifierStart(i) != 
              Character.isUnicodeIdentifierStart(i))
            trackDifference(p, i, "isUnicodeIdentifierStart()",
                            "" + UCharacter.isUnicodeIdentifierStart(i),
                            "" + Character.isUnicodeIdentifierStart(i));
          if (UCharacter.isIdentifierIgnorable(i) != 
              Character.isIdentifierIgnorable(i))
            trackDifference(p, i, "isIdentifierIgnorable()",
                            "" + UCharacter.isIdentifierIgnorable(i),
                            "" + Character.isIdentifierIgnorable(i));
          if (UCharacter.isUpperCase(i) != Character.isUpperCase(i))
            trackDifference(p, i, "isUpperCase()", 
                            "" + UCharacter.isUpperCase(i),
                            "" + Character.isUpperCase(i));
          if (UCharacter.toLowerCase(i) != Character.toLowerCase(i))
            trackDifference(p, i, "toLowerCase()", 
                            Integer.toHexString(UCharacter.toLowerCase(i)),
                            Integer.toHexString(Character.toLowerCase(i)));
          if (!UCharacter.toString(i).equals(new Character(i).toString()))
            trackDifference(p, i, "toString()",
                            UCharacter.toString(i),
                            new Character(i).toString());
          if (UCharacter.toTitleCase(i) != Character.toTitleCase(i))
            trackDifference(p, i, "toTitleCase()",
                            Integer.toHexString(UCharacter.toTitleCase(i)),
                            Integer.toHexString(Character.toTitleCase(i)));
          if (UCharacter.toUpperCase(i) != Character.toUpperCase(i))
            trackDifference(p, i, "toUpperCase()",
                            Integer.toHexString(UCharacter.toUpperCase(i)),
                            Integer.toHexString(Character.toUpperCase(i)));
        }
      }
      summary(p);
      p.close();
    } 
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  // private methods ===================================================
  
  /**
  * Comparing types
  * @param uchartype UCharacter type
  * @param jchartype java.lang.Character type
  */
  private static boolean compareType(int uchartype, int jchartype)
  {
    if (uchartype == UCharacterCategoryEnum.UNASSIGNED && 
        jchartype == Character.UNASSIGNED)
      return true;
    if (uchartype == UCharacterCategoryEnum.UPPERCASE_LETTER && 
        jchartype == Character.UPPERCASE_LETTER)
      return true;                                                   
    if (uchartype == UCharacterCategoryEnum.LOWERCASE_LETTER && 
        jchartype == Character.LOWERCASE_LETTER)
      return true;                                                   
    if (uchartype == UCharacterCategoryEnum.TITLECASE_LETTER && 
        jchartype == Character.TITLECASE_LETTER)
      return true;
    if (uchartype == UCharacterCategoryEnum.MODIFIER_LETTER && 
        jchartype == Character.MODIFIER_LETTER)
      return true;
    if (uchartype == UCharacterCategoryEnum.OTHER_LETTER && 
        jchartype == Character.OTHER_LETTER)
      return true;
    if (uchartype == UCharacterCategoryEnum.NON_SPACING_MARK && 
        jchartype == Character.NON_SPACING_MARK)
      return true;
    if (uchartype == UCharacterCategoryEnum.ENCLOSING_MARK && 
        jchartype == Character.ENCLOSING_MARK)
      return true;
    if (uchartype == UCharacterCategoryEnum.COMBINING_SPACING_MARK && 
        jchartype == Character.COMBINING_SPACING_MARK)
      return true;
	  if (uchartype == UCharacterCategoryEnum.DECIMAL_DIGIT_NUMBER && 
        jchartype == Character.DECIMAL_DIGIT_NUMBER)
      return true;
	  if (uchartype == UCharacterCategoryEnum.LETTER_NUMBER && 
        jchartype == Character.LETTER_NUMBER)
      return true;
	  if (uchartype == UCharacterCategoryEnum.OTHER_NUMBER && 
        jchartype == Character.OTHER_NUMBER)
      return true;
	  if (uchartype == UCharacterCategoryEnum.SPACE_SEPARATOR && 
        jchartype == Character.SPACE_SEPARATOR)
      return true;
	  if (uchartype == UCharacterCategoryEnum.LINE_SEPARATOR && 
        jchartype == Character.LINE_SEPARATOR)
      return true;
	  if (uchartype == UCharacterCategoryEnum.PARAGRAPH_SEPARATOR && 
        jchartype == Character.PARAGRAPH_SEPARATOR)
      return true;
	  if (uchartype == UCharacterCategoryEnum.CONTROL && 
        jchartype == Character.CONTROL)
      return true;
    if (uchartype == UCharacterCategoryEnum.FORMAT && 
        jchartype == Character.FORMAT)
      return true;
	  if (uchartype == UCharacterCategoryEnum.PRIVATE_USE && 
        jchartype == Character.PRIVATE_USE)
      return true;
    if (uchartype == UCharacterCategoryEnum.SURROGATE && 
        jchartype == Character.SURROGATE)
      return true;
    if (uchartype == UCharacterCategoryEnum.DASH_PUNCTUATION && 
        jchartype == Character.DASH_PUNCTUATION)
      return true;
	  if (uchartype == UCharacterCategoryEnum.START_PUNCTUATION && 
        jchartype == Character.START_PUNCTUATION)
      return true;
	  if (uchartype == UCharacterCategoryEnum.END_PUNCTUATION && 
        jchartype == Character.END_PUNCTUATION)
      return true;
    if (uchartype == UCharacterCategoryEnum.CONNECTOR_PUNCTUATION && 
        jchartype == Character.CONNECTOR_PUNCTUATION)
      return true;
    if (uchartype == UCharacterCategoryEnum.OTHER_PUNCTUATION && 
        jchartype == Character.OTHER_PUNCTUATION)
      return true;
	  if (uchartype == UCharacterCategoryEnum.MATH_SYMBOL && 
        jchartype == Character.MATH_SYMBOL)
      return true;
	  if (uchartype == UCharacterCategoryEnum.CURRENCY_SYMBOL && 
        jchartype == Character.CURRENCY_SYMBOL)
      return true;
	  if (uchartype == UCharacterCategoryEnum.MODIFIER_SYMBOL && 
        jchartype == Character.MODIFIER_SYMBOL)
      return true;
	  if (uchartype == UCharacterCategoryEnum.OTHER_SYMBOL && 
        jchartype == Character.OTHER_SYMBOL)
      return true;
	  if (uchartype == UCharacterCategoryEnum.INITIAL_PUNCTUATION && 
        jchartype == Character.START_PUNCTUATION)
      return true;
    if (uchartype == UCharacterCategoryEnum.FINAL_PUNCTUATION && 
        jchartype == Character.END_PUNCTUATION)
      return true;
	  /*if (uchartype == UCharacterCategoryEnum.GENERAL_OTHER_TYPES && 
        jchartype == Character.GENERAL_OTHER_TYPES)
      return true;*/
    return false;
	}
	
	/**
	* Difference writing to file
	* @param f file outputstream
	* @param ch code point
	* @param method for testing
	* @param ucharval UCharacter value after running method
	* @param charval Character value after running method
	* @exception thrown when error occur in writing to file
	*/
	private static void trackDifference(PrintWriter f, int ch, String method,
	                                    String ucharval, String charval)
	  throws Exception
	{
	  if (m_hashtable_.containsKey(method))
	  {
	    Integer value = (Integer)m_hashtable_.get(method);
	    m_hashtable_.put(method, new Integer(value.intValue() + 1));
	  }
	  else
	    m_hashtable_.put(method, new Integer(1));
	  
	  String temp = Integer.toHexString(ch);
	  StringBuffer s = new StringBuffer(temp);
	  for (int i = 0; i < 6 - temp.length(); i ++)
	    s.append(' ');
	  temp = UCharacter.getName(ch);
	  if (temp == null)
	    temp = " ";
	  s.append(temp);
	  for (int i = 0; i < 73 - temp.length(); i ++)
	    s.append(' ');
	  
	  s.append(method);
	  for (int i = 0; i < 27 - method.length(); i ++)
	    s.append(' ');
	  s.append(ucharval);
	  for (int i = 0; i < 11 - ucharval.length(); i ++)
	    s.append(' ');
	  s.append(charval);
	  f.println(s.toString());
	}
	
	/**
	* Does up a summary of the differences
	* @param f file outputstream
	*/
	private static void summary(PrintWriter f)
	{
	  f.println("==================================================");
	  f.println("Summary of differences");
	  for (Enumeration e = m_hashtable_.keys() ; e.hasMoreElements() ;) 
	  {
	    StringBuffer method = new StringBuffer((String)e.nextElement());
	    int count = ((Integer)m_hashtable_.get(method.toString())).intValue();
	    for (int i = 30 - method.length(); i > 0; i --)
	      method.append(' ');
      f.println(method + "  " + count);
    }
	}
}

