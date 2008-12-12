/*
 ******************************************************************************
 * Copyright (C) 1996-2008, International Business Machines Corporation and   *
 * others. All Rights Reserved.                                               *
 ******************************************************************************
 */

/*
 * @author Shaopeng Jia
 */

package com.ibm.icu.impl;

public interface TrieCompactor {
    public void compactToTrie(int start, int end, int rowIndex);
}
