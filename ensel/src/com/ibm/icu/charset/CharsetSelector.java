/*
******************************************************************************
* Copyright (C) 1996-2008, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

/* 
 * This is a port of the C++ class UConverterSelector. 
 *
 * Methods related to serialization are not ported in this version. In addition,
 * the selectForUTF8 method is not going to be ported, as UTF8 is seldom used
 * in Java.
 */

package com.ibm.icu.charset;

/**
 * Charset Selector
 * 
 * A charset selector is built with a list of charset names and given an input CharSequence
 * returns the list of names the corresponding charsets which can convert the CharSequence.
 *
 * @draft ICU 4.2
 */
public final class CharsetSelector {

 /**
  * Construct a CharsetSelector from a list of charset names.
  * @param charsetList a list of charset names in the form
  * of strings. If charsetList is empty, a selector for all available charset 
  * is constructed.
  * @param excludedCodePoints a set of code points to be excluded from consideration.
  * Excluded code points appearing in the input CharSequence do not change the selection
  * result. It could be empty when no code point should be excluded.
  * @param mappingTypes an int which determines whether to consider only roundtrip mappings or
  * also fallbacks, e.g. CharsetICU.ROUNDTRIP_SET. See CharsetICU.java for the constants that 
  * are currently supported.
  * @throws IllegalArgumentException if the parameters is invalid. 
  * @throws IllegalCharsetNameException If the given charset name
  * is illegal.
  * @throws UnsupportedCharsetException If no support for the
  * named charset is available in this instance of the Java
  * virtual machine.
  * @draft ICU 4.2
  */
  public CharsetSelector(List charsetList, UnicodeSet excludedCodePoints, int mappingTypes);

 /**
  * Select charsets that can map all characters in a CharSequence,
  * ignoring the excluded code points.
  *
  * @param unicodeText a CharSequence. It could be empty.
  * @return a list that contains charset names in the form 
  * of strings. The returned encoding names and their order will be the same as
  * supplied when building the selector.
  *
  * @draft ICU 4.2
  */    
  public List selectForString(CharSequence unicodeText);
}
