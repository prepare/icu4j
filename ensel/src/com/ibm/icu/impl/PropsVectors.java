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

import java.util.Arrays;
import java.util.Comparator;

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
	private boolean isCompacted;
	
	private int findRow(int rangeStart) {
		int index = 0;
		
		// check the vicinity of the last-seen row (start
		// searching with an unrolled loop)
		
		index = prevRow*columns;
		if (rangeStart >= v[index]) {
			if (rangeStart < v[index+1]) {
				// same row as last seen
				return index;
			} else {
				index+=columns;
				if (rangeStart < v[index+1]) {
					++prevRow;
					return index;
				} else {
					index+=columns;
					if (rangeStart < v[index+1]) {
						prevRow+=2;
						return index;
					} else if ((rangeStart - v[index+1]) < 10) {
						// we are close, continue looping
						prevRow+=2;
						do {
							++prevRow;
							index+=columns;
						} while (rangeStart >= v[index+1]);
						return index;
					}
				}
			}
		} else if (rangeStart < v[1]) {
			// the very first row
			prevRow = 0;
			return 0;
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
	
	/*
	 * Special pseudo code points for storing the initialValue and the         
	 * errorValue which are used to initialize a Trie or similar.
	 */
	public final static int PVEC_FIRST_SPECIAL_CP = 0x110000;
	public final static int PVEC_INITIAL_VALUE_CP = 0x110000;
	public final static int PVEC_ERROR_VALUE_CP = 0x110001;
	public final static int PVEC_MAX_CP = 0x110001;
	
	public final static int PVEC_INITIAL_ROWS = 1<<14;
	public final static int PVEC_MEDIUM_ROWS = 1<<17;
	public final static int PVEC_MAX_ROWS = PVEC_MAX_CP + 1;
	
	/*
	 * Special pseudo code point used in compact() signaling the end of
	 * delivering special values and the beginning of delivering real ones.
	 * Stable value, unlike PVEC_MAX_CP which might grow over time.
	 */
	public final static int PVEC_START_REAL_VALUES_CP = 0x200000;
	
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
		// find the first and last row, always successful
		firstRow = findRow(start); 
		lastRow = findRow(end); 
		
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
	
	@SuppressWarnings("unchecked")
	public void compact(TrieCompactor compactor) {
		if (isCompacted) {
			return;
		}
		
		// Set the flag now: Sorting and compacting destroys the builder 
		// data structure.
		isCompacted = true;
		int valueColumns = columns - 2; // not counting start & limit
		
		// sort the properties vectors to find unique vector values
		if (rows > 1) {
			Integer[] indexArray = new Integer[rows];
			for (int i = 0; i < rows; ++i) {
				indexArray[i] = new Integer(columns * i);
			}
			
			Arrays.sort(indexArray, new Comparator() {
				public int compare(Object o1, Object o2) {
					int indexOfRow1 = ((Integer)o1).intValue();
					int indexOfRow2 = ((Integer)o2).intValue();
					int count = columns; // includes start/limit columns
					
					// start comparing after start/limit 
					// but wrap around to them
					int index = 2;
					do {
						if (v[indexOfRow1 + index] != v[indexOfRow2 + index]) {
							return v[indexOfRow1 + index] < 
							       v[indexOfRow2 + index] ? -1:1;
						}
						if (++index==columns) {
							index = 0;
						}
					} while(--count>0);
					
					return 0;
				}
			});
			
			int[] temp = new int[rows*columns];
			System.arraycopy(v, 0, temp, 0, rows * columns);
			for (int rowNumber = 0; rowNumber < rows; ++rowNumber) {
				System.arraycopy(temp, (indexArray[rowNumber]).intValue(), v, 
						rowNumber * columns, columns);
			}	
		}
		
	    /*
	     * Find and set the special values.
	     * This has to do almost the same work as the compaction below,
	     * to find the indexes where the special-value rows will move.
	     */
		int count = -valueColumns;
		int index = 0;
		int[] prev = new int[valueColumns];
		int[] current = new int[valueColumns];
		for (int i = 0; i < rows; ++i) {
			int start = v[index];
			
			// count a new values vector if it is different 
			// from the current one
			if (count < 0) {
				count+=valueColumns;
				System.arraycopy(v, 2, prev, 0, valueColumns);
			} else {
				System.arraycopy(v, index+2, current, 0, valueColumns);
				if (!Arrays.equals(current, prev)) {
					count+=valueColumns;
				}
				System.arraycopy(current, 0, prev, 0, valueColumns);
			}
			
			if (start >= PVEC_FIRST_SPECIAL_CP) {
				compactor.compactToTrie(start, start, count);
			}
			index+=columns;
		}
		
		// count is at the beginning of the last vector, 
		// add valueColumns to include that last vector
		count+=valueColumns;
		
	    // Call the handler once more to signal the start of 
		// delivering real values.
        compactor.compactToTrie(PVEC_START_REAL_VALUES_CP, 
        		PVEC_START_REAL_VALUES_CP, count);
        
        /*
         * Move vector contents up to a contiguous array with only unique
         * vector values, and call the handler function for each vector.
         *
         * This destroys the Properties Vector structure and replaces it
         * with an array of just vector values.
         */
	    count = -valueColumns;
		index = 0;
		for (int i = 0; i < rows; ++i) {
			int start = v[index];
			int limit = v[index + 1];
			
			// count a new values vector if it is different 
			// from the current one
			if (count < 0) {
				count+=valueColumns;
				System.arraycopy(v, 2, v, 0, valueColumns);
			} else {
				System.arraycopy(v, count, prev, 0, valueColumns);
				System.arraycopy(v, index+2, current, 0, valueColumns);
				if (!Arrays.equals(current, prev)) {
					count+=valueColumns;
					System.arraycopy(v, index+2, v, count, valueColumns);
				}
			}
			
			if (start < PVEC_FIRST_SPECIAL_CP) {
				compactor.compactToTrie(start, limit - 1, count);
			}
			index+=columns;
		}
	
		 // count is at the beginning of the last vector, 
	 	 // add one to include that last vector
		rows = count/valueColumns + 1;
	}
	
	/*
	 * @throws IllegalStateException
	 */
	public int[] getCompactedArray() throws IllegalStateException {
		if (!isCompacted) {
			throw new IllegalStateException("Illegal Invocation of the method before" +
					"upvec_compact()");
		}
		int numberOfElements = getCompactedRows() * getCompactedColumns();
		int[] result = new int[numberOfElements];
		System.arraycopy(v, 0, result, 0, numberOfElements);
		return result;
	}
	
	/*
	 * @throws IllegalStateException
	 */
	public int getCompactedRows() throws IllegalStateException {
		if (!isCompacted) {
			throw new IllegalStateException("Illegal Invocation of the method before" +
					"upvec_compact()");
		}
		return rows;
	}
	
	/*
	 * @throws IllegalStateException
	 */
	public int getCompactedColumns() throws IllegalStateException {
		if (!isCompacted) {
			throw new IllegalStateException("Illegal Invocation of the method before" +
					"upvec_compact()");
		}
		return columns - 2;
	}
	
	public IntTrie compactToTrieWithRowIndexes() {
		PVecToTrieCompactor compactor = new PVecToTrieCompactor();
		compact(compactor);
		return compactor.builder.serialize(new DefaultGetFoldedValue(compactor.builder), 
				new DefaultGetFoldingOffset());
	}
	
	// inner class implementation of Trie.DataManipulate
	private static class DefaultGetFoldingOffset implements Trie.DataManipulate {
		public int getFoldingOffset(int value) {
			return value;
		}
	}
	
	// inner class implementation of TrieBuilder.DataManipulate
	private static class DefaultGetFoldedValue implements TrieBuilder.DataManipulate {
		private IntTrieBuilder builder;
		public DefaultGetFoldedValue(IntTrieBuilder inBuilder) {
			builder = inBuilder;
		}
		public int getFoldedValue(int start, int offset) {
			int initialValue = builder.getValue(0); // TODO: need to double check this line
			int limit = start + 0x400;
			while (start < limit) {
				boolean[] inBlockZero = new boolean[1];
				int value = builder.getValue(start, inBlockZero);
				if (inBlockZero[0]) {
					start+=TrieBuilder.DATA_BLOCK_LENGTH;
				} else if (value != initialValue) {
					return offset;
				} else {
					++start;
				}
			}
			return 0;
		}
	}
}