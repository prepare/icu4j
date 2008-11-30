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
		
		// must be found because all ranges together always cover 
		// all of Unicode
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
	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
	 * @throws IndexOutOfBoundsException
	 */
	public void setValue(int start, int end, int column, int value, int mask) 
	throws IllegalArgumentException, IllegalStateException, 
	       IndexOutOfBoundsException {
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
		splitFirstRow = (start != v[firstRow] && 
				value != (v[firstRow + column] & mask));
		splitLastRow = (limit != v[lastRow+1] && 
				value != (v[lastRow + column] & mask));
		
		// split first/last rows if necessary
		if (splitFirstRow || splitLastRow) {
			int rowsToExpand = 0;
			if (splitFirstRow) {
				++rowsToExpand;
			}
			if (splitLastRow) {
				++rowsToExpand;
			}
			int newMaxRows = 0;
			if ((rows + rowsToExpand) > maxRows) {
				if (maxRows < PVEC_MEDIUM_ROWS) {
					newMaxRows = PVEC_MEDIUM_ROWS;
				} else if (maxRows < PVEC_MAX_ROWS) {
					newMaxRows = PVEC_MAX_ROWS;
				} else {
					throw new IndexOutOfBoundsException(
							"PVEC_MAX_ROWS exceeded! " +
							"Increase it to a higher value " +
							"in the implementation");
				}
				int[] temp = new int[maxRows * columns];
				System.arraycopy(v, 0, temp, 0, maxRows * columns);
				v = new int[newMaxRows * columns];
				System.arraycopy(temp, 0, v, 0, maxRows * columns);
				maxRows = newMaxRows;
			}
			
			// count the number of row cells to move after the last row, 
			// and move them 
			int count = (rows * columns) - (lastRow + columns);
			if (count > 0) {
				System.arraycopy(v, lastRow+columns, 
						v, lastRow+(1+rowsToExpand)*columns, count);
			}
			rows+=rowsToExpand;
			
			// split the first row, and move the firstRow pointer 
			// to the second part
			if (splitFirstRow) {
				// copy all affected rows up one and move the lastRow pointer
				count = lastRow - firstRow + columns;
				System.arraycopy(v, firstRow, v, firstRow + columns, count);
				lastRow+=column;
				
				// split the range and move the firstRow pointer
				v[firstRow+1] = v[firstRow+columns] = start;
				firstRow+=columns;
			}
			
			// split the last row
			if (splitLastRow) {
				// copy the last row data
				System.arraycopy(v, lastRow, v, lastRow + columns, columns);
				
				// split the range and move the firstRow pointer
				v[lastRow+1] = v[lastRow+columns] = limit;
			}
		}
		
		// set the "row last seen" to the last row for the range
		prevRow = lastRow / columns;
		
		// set the input value in all remaining rows
		firstRow+=column;
		lastRow+=column;
		mask=~mask;
		for (;;) {
			v[firstRow] = (v[firstRow] & mask) | value;
			if (firstRow == lastRow) {
				break;
			}
			firstRow+=columns;
		}
	}
	
	/*
	 * Always returns 0 if called after upvec_compact().
	 */
	public int getValue(int c, int column) {
		if (isCompacted || c < 0 || c > PVEC_MAX_CP || column < 0 ||
				column >= (columns - 2)) {
			return 0;
		}
		int index = findRow(c);
		return v[index + 2 + column];
	}

	/*
	 * @throws IllegalStateException
	 * @throws IllegalArgumentException
	 */
	public int[] getRow(int rowIndex) 
	throws IllegalStateException, IllegalArgumentException {
		if (isCompacted) {
			throw new IllegalStateException("Illegal Invocation of the method after" +
					"upvec_compact()");
		}
		if (rowIndex < 0 || rowIndex > rows) {
			throw new IllegalArgumentException("rowIndex out of bound!");
		}
		int[] rowToReturn = new int[columns - 2];
		System.arraycopy(v, rowIndex * columns + 2, rowToReturn, 0, columns - 2);
		return rowToReturn;
	}
	
	/*
	 * @throws IllegalStateException
	 * @throws IllegalArgumentException
	 */
	public int getRowStart(int rowIndex) 
	throws IllegalStateException, IllegalArgumentException {
		if (isCompacted) {
			throw new IllegalStateException("Illegal Invocation of the method after" +
					"upvec_compact()");
		}
		if (rowIndex < 0 || rowIndex > rows) {
			throw new IllegalArgumentException("rowIndex out of bound!");
		}
		return v[rowIndex * columns];
	}
	
	/*
	 * @throws IllegalStateException
	 * @throws IllegalArgumentException
	 */
	public int getRowEnd(int rowIndex) 
	throws IllegalStateException, IllegalArgumentException {
		if (isCompacted) {
			throw new IllegalStateException("Illegal Invocation of the method after" +
					"upvec_compact()");
		}
		if (rowIndex < 0 || rowIndex > rows) {
			throw new IllegalArgumentException("rowIndex out of bound!");
		}
		return v[rowIndex * columns] - 1;
	}
	
	public int compareRows(int indexOfRow1, int indexOfRow2) {
		int count = columns; // includes start/limit columns
		
		// start comparing after start/limit but wrap around to them
		int index = 2;
		do {
			if (v[indexOfRow1 + index] != v[indexOfRow2 + index]) {
				return v[indexOfRow1 + index] < v[indexOfRow2 + index] ? -1:1;
			}
			if (++index==columns) {
				index = 0;
			}
		} while(--count>0);
		
		return 0;
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