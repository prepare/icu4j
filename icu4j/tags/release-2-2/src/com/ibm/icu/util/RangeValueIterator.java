/*
******************************************************************************
* Copyright (C) 1996-2002, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*
* $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/util/RangeValueIterator.java,v $
* $Date: 2002/04/15 17:26:56 $
* $Revision: 1.4 $
*
******************************************************************************
*/

package com.ibm.icu.util;

/**
 * <p>Interface for enabling iteration over sets of <int index, int value>, 
 * where index is the sorted integer index in ascending order and value, its 
 * associated integer value.</p>
 * <p>The result for each iteration is the consecutive range of 
 * <int index, int value> with the same value. Result is represented by 
 * <start, limit, value> where</p>
 * <ul>
 * <li> start is the starting integer of the result range
 * <li> limit is 1 after the maximum integer that follows start, such that
 *      all integers between start and (limit - 1), inclusive, have the same 
 *      associated integer value.
 * <li> value is the integer value that all integers from start to (limit - 1) 
 *      share in common.
 * </ul>
 * <p>
 * Hence value(start) = value(start + 1) = .... = value(start + n) = .... =
 * value(limit - 1). However value(start -1) != value(start) and 
 * value(limit) != value(start).
 * </p>
 * <p>Most implementations will be created by factory methods, such as the
 * character type iterator in UCharacter.getTypeIterator. See example below.
 * </p>
 * Example of use:<br>
 * <pre>
 * RangeValueIterator iterator = UCharacter.getTypeIterator();
 * RangeValueIterator.Element result = new RangeValueIterator.Element();
 * while (iterator.next(result)) {
 *     System.out.println("Codepoint \\u" + 
 *                        Integer.toHexString(result.start) + 
 *                        " to codepoint \\u" +
 *                        Integer.toHexString(result.limit - 1) + 
 *                        " has the character type " + result.value);
 * }
 * </pre>
 * @author synwee
 * @since release 2.1, Jan 17 2002
 * @draft 2.1
 */
public interface RangeValueIterator
{
    // public inner class ---------------------------------------------
    
    /**
    * Return result wrapper for com.ibm.icu.util.RangeValueIterator.
    * Stores the start and limit of the continous result range and the
    * common value all integers between [start, limit - 1] has.
    * @draft 2.1
    */
    public class Element
    {
        /**
        * Starting integer of the continuous result range that has the same 
        * value
        * @draft 2.1
        */
        public int start;
        /**
        * (End + 1) integer of continuous result range that has the same 
        * value
        * @draft 2.1
        */
        public int limit;
        /**
        * Gets the common value of the continous result range
        * @draft 2.1
        */ 
        public int value;
    }
    
    // public methods -------------------------------------------------
    
    /**
    * <p>Gets the next maximal result range with a common value and returns 
    * true if we are not at the end of the iteration, false otherwise.</p>
    * <p>If the return boolean is a false, the contents of elements will not
    * be updated.</p>
    * @param element for storing the result range and value
    * @return true if we are not at the end of the iteration, false otherwise.
    * @see Element
    * @draft 2.1
    */
    public boolean next(Element element);
    
    /**
    * Resets the iterator to the beginning of the iteration.
    * @draft 2.1
    */
    public void reset();
}