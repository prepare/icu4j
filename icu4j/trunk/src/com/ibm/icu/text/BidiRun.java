/*
*******************************************************************************
*   Copyright (C) 2001-2007, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*/
/* Written by Simon Montagu, Matitiahu Allouche
 * (ported from C code written by Markus W. Scherer)
 */

package com.ibm.icu.text;

/**
 * A BidiRun represents a sequence of characters at the same embedding level.
 * The Bidi algorithm decomposes a piece of text into sequences of characters
 * at the same embedding level, each such sequence is called a <quote>run</quote>.
 *
 * <p>A BidiRun represents such a run by storing its essential properties,
 * but does not duplicate the characters which form the run.
 *
 * <p>The &quot;limit&quot; of the run is the position just after the
 * last character, i.e., one more than that position.
 *
 * <p>This class has no public constructor, and its members cannot be
 * modified by users.
 *
 * @see com.ibm.icu.text.Bidi
 * @draft ICU 3.8
 */
public class BidiRun {

    int start;              /* first logical position of the run */
    int limit;              /* last visual position of the run +1 */
    int insertRemove;       /* if >0, flags for inserting LRM/RLM before/after run,
                               if <0, count of bidi controls within run            */
    byte level;

    /**
     * Default constructor
     *
     * Note that members start and limit of a run instance have different
     * meanings depending whether the run is part of the runs array of a Bidi
     * object, or if it is a reference returned by getVisualRun() or
     * getLogicalRun().
     * For a member of the runs array of a Bidi object,
     *   - start is the first logical position of the run in the source text.
     *   - limit is one after the last visual position of the run.
     * For a reference returned by getLogicalRun() or getVisualRun(),
     *   - start is the first logical position of the run in the source text.
     *   - limit is one after the last logical position of the run.
     *
     * @draft ICU 3.8
     */
    BidiRun()
    {
        this(0, 0, (byte)0);
    }

    /**
     * Constructor
     * @draft ICU 3.8
     */
    BidiRun(int start, int limit, byte embeddingLevel)
    {
        this.start = start;
        this.limit = limit;
        this.level = embeddingLevel;
    }

    /**
     * Copy the content of a BidiRun instance
     * @draft ICU 3.8
     */
    void copyFrom(BidiRun run)
    {
        this.start = run.start;
        this.limit = run.limit;
        this.level = run.level;
        this.insertRemove = run.insertRemove;
    }

    /**
     * Get the first logical position of the run in the source text
     * @draft ICU 3.8
     */
    public int getStart()
    {
        return start;
    }

    /**
     * Set start of run
     * @draft ICU 3.8
     */
    void setStart(int start)
    {
        this.start = start;
    }

    /**
     * Get position of one character after the end of the run in the source text
     * @draft ICU 3.8
     */
    public int getLimit()
    {
        return limit;
    }

    /**
     * Set limit of run
     * @draft ICU 3.8
     */
    void setLimit(int limit)
    {
        this.limit = limit;
    }

    /**
     * Get length of run
     * @draft ICU 3.8
     */
    public int getLength()
    {
        return limit - start;
    }

    /**
     * Get level of run
     * @draft ICU 3.8
     */
    public byte getEmbeddingLevel()
    {
        return level;
    }

    /**
     * Set level of run
     * @draft ICU 3.8
     */
    void setEmbeddingLevel(byte embeddingLevel)
    {
        this.level = embeddingLevel;
    }

    /**
     * Check if run level is odd
     * @return true if the embedding level of this run is odd, i.e. it is a
     *  right-to-left run.
     * @draft ICU 3.8
     */
    public boolean isOddRun()
    {
        return (level & 1) == 1;
    }

    /**
     * Check if run level is even
     * @return true if the embedding level of this run is even, i.e. it is a
     *  left-to-right run.
     * @draft ICU 3.8
     */
    public boolean isEvenRun()
    {
        return (level & 1) == 0;
    }

    /**
     * Get direction of run
     * @draft ICU 3.8
     */
    public byte getDirection()
    {
        return (byte)(level & 1);
    }

    /**
     * String to display run
     * @draft ICU 3.8
     */
    public String toString()
    {
        return new String("BidiRun " + start + " - " + limit + " @ " + level);
    }
}
