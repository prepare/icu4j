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
	
	public PropsVectors(int columns) {
		
	}
	
	public void setValue() {
		
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