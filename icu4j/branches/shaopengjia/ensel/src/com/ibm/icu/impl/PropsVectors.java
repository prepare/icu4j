/*
******************************************************************************
* Copyright (C) 1996-2008, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

/**
 * Store bits (Unicode character properties) in bit set vectors.
 * 
 * This is a port of the C++ class UPropsVectors from ICU4C
 * 
 * @author Shaopeng Jia
 * @internal
 */

package com.ibm.icu.impl;

/**
 * Unicode Properties Vectors associated with code point ranges.
 *
 * Rows of primitive integers in a contiguous array store
 * the range limits and the properties vectors.
 *
 * In each row, row[0] contains the start code point and
 * row[1] contains the limit code point,
 * which is the start of the next range.
 *
 * Initially, there is only one range [0..0x110000] with values 0.
 *
 * It would be possible to store only one range boundary per row,
 * but self-contained rows allow to later sort them by contents.
 */
public class PropsVectors {
	private int v[];
	private int columns; // number of columns, plus two for start
	                     // and limit values	
	private int maxRows;
	private int rows;
	private int prevRow; // search optimization: remember last row seen 
	boolean isCompacted;

	/*
	 * Special pseudo code points for storing the initialValue and the         
	 * errorValue which are used to initialize a Trie or similar.
	 */
	private final int PVEC_FIRST_SPECIAL_CP = 0x110000;
	private final int PVEC_INITIAL_VALUE_CP = 0x110000;
	private final int PVEC_ERROR_VALUE_CP = 0x110001;
	private final int PVEC_MAX_CP = 0x110001;
	
	private final int PVEC_INITIAL_ROWS = 1<<14;
	private final int PVEC_MEDIUM_ROWS = 1<<17;
	private final int PVEC_MAX_ROWS = PVEC_MAX_CP + 1;
	
	/*
	 * Special pseudo code point used in compact() signaling the end of
	 * delivering special values and the beginning of delivering real ones.
	 * Stable value, unlike PVEC_MAX_CP which might grow over time.
	 */
	private final int PVEC_START_REAL_VALUE_CP = 0x200000;
	
	private int findRow(int rangeStart) {
		int index = 0;
		
		// check the vicinity of the last-seen row
		if (prevRow < rows) {
			index = prevRow*columns;
			if (rangeStart >= v[index]) {
				if (rangeStart < v[index+1]) {
					// same row as last seen
					return index;
				} else if (
						(prevRow + 1) < rows &&
						rangeStart >= v[index+columns] &&
						rangeStart < v[index+columns+1]) {
					// next row after the last one
					++prevRow;
					return index + columns;
				}
			}
		}
		
		// do a binary search for the start of the range
		int start = 0;
		int mid = 0;
		int limit = rows;
		while (start < limit - 1) {
			mid = (start + limit) / 2;
			index = columns * mid;
			if (rangeStart < v[index]) {
				limit = mid;
			} else if (rangeStart < v[index+1]) {
				prevRow = mid;
				return index;
			} else {
				start = mid;
			}
		}
		
		// must be found because all ranges together always cover all of Unicode
		prevRow = start;
		index = start * columns;
		return index;
	}
	
	public PropsVectors(int numOfColumns) throws IllegalArgumentException {
		if (numOfColumns < 1) {
			throw new IllegalArgumentException("numOfColumns need to be no " +
					"less than 1; but it is " + numOfColumns);
		}
		columns = numOfColumns + 2;  // count range start and limit columns
		v = new int[PVEC_INITIAL_ROWS*columns];
		maxRows = PVEC_INITIAL_ROWS;
		rows = 2 + (PVEC_MAX_CP - PVEC_FIRST_SPECIAL_CP);
		prevRow = 0;
		isCompacted = false;
		v[0] = 0;
		v[1] = 0x110000;
		int index = columns;
		for (int cp = PVEC_FIRST_SPECIAL_CP; cp <= PVEC_MAX_CP; ++cp) {
			v[index] = cp;
			v[index+1] = cp + 1;
			index += columns;
		}	
	}
	
	
	/*
	 * In rows for code points [start..end], select the column,
	 * reset the mask bits and set the value bits (ANDed with the mask).
	 * 
	 * @throws IllegalArgumentException, IllegalStateException
	 */
	public void setValue(int start, int end, int column, int value, int mask) 
	throws IllegalArgumentException, IllegalStateException {
		if (start < 0 || start > end || end > PVEC_MAX_CP ||
				column < 0 || column >= (columns - 2)
				) {
			throw new IllegalArgumentException();
		}
		if (isCompacted) {
			throw new IllegalStateException("Shouldn't be called after" +
					"compact()!");
		}
		
		int firstRow, lastRow;
		int limit = end + 1;
		boolean splitFirstRow, splitLastRow;
		// skip range start and limit columns
		column+=2;
		value&=mask;
		
		// find the rows whose ranges overlap with the input range
		firstRow = findRow(start); // find the first row, always successful
		lastRow = firstRow; 
		
	    /*
	     * Start searching with an unrolled loop:
	     * start and limit are often in a single range, or in adjacent ranges.
	     */
		if (limit > v[lastRow+1]) {
			lastRow+=columns;
			if (limit > v[lastRow+1]) {
				if ((limit-v[lastRow+1]) < 10) {
					// we are close, continue looping
					do {
						lastRow+=columns;
					} while(limit > v[lastRow+1]);
				} else {
					lastRow = findRow(limit - 1);
				}
			}
		}
		
	    /*
	     * Rows need to be split if they partially overlap with the
	     * input range (only possible for the first and last rows)
	     * and if their value differs from the input value.
	     */
		splitFirstRow = (start != v[firstRow] && value != (v[firstRow + column] & mask));
		splitLastRow = (limit != v[lastRow+1] && value != (v[lastRow + column] & mask));
		
		// split first/last rows if necessary
		if (splitFirstRow || splitLastRow) {
			
		}
		
		
		
		
		
	}
	
	public int getValue() {
		
	}
	
	public int[] getRow() {
		
	}
	
	public void compact() {
	
	}
	
	public int[] getArray() {
		
	}
	
	public int[] cloneArray() {
		
	}
	
	public Trie compactToTrieWithRowIndexes() {
	
	}
	
}