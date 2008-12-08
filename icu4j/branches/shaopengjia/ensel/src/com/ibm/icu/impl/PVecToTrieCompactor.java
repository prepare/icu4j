/*
******************************************************************************
* Copyright (C) 1996-2008, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

package com.ibm.icu.impl;

public class PVecToTrieCompactor implements TrieCompactor {
	public IntTrieBuilder builder;
	public int capacity;
	public int initialValue;
	public boolean latinLinear;
	public void compactToTrie(int start, int end, int rowIndex) 
	throws IndexOutOfBoundsException {
		if (start < PropsVectors.PVEC_FIRST_SPECIAL_CP) {
			builder.setRange(start, end + 1, rowIndex, true);
		} else {
			switch (start) {
			case PropsVectors.PVEC_INITIAL_VALUE_CP:
				initialValue = rowIndex;
				break;
			case PropsVectors.PVEC_START_REAL_VALUES_CP:
				if (rowIndex > 0xffff) {
					// too many rows for a 16-bit trie
					throw new IndexOutOfBoundsException();
				} else {
					builder = new IntTrieBuilder(null, capacity, initialValue,
							initialValue, latinLinear);
				}
				break;
			default:
				break;
			}
		}
	}
	

}
