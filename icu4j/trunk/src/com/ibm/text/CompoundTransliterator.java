/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/text/Attic/CompoundTransliterator.java,v $ 
 * $Date: 2001/09/21 21:24:04 $ 
 * $Revision: 1.14 $
 *
 *****************************************************************************************
 */
package com.ibm.text;
import com.ibm.util.Utility;
import java.util.Enumeration;
import java.util.Vector;

/**
 * A transliterator that is composed of two or more other
 * transliterator objects linked together.  For example, if one
 * transliterator transliterates from script A to script B, and
 * another transliterates from script B to script C, the two may be
 * combined to form a new transliterator from A to C.
 *
 * <p>Composed transliterators may not behave as expected.  For
 * example, inverses may not combine to form the identity
 * transliterator.  See the class documentation for {@link
 * Transliterator} for details.
 *
 * <p>If a non-<tt>null</tt> <tt>UnicodeFilter</tt> is applied to a
 * <tt>CompoundTransliterator</tt>, it has the effect of being
 * logically <b>and</b>ed with the filter of each transliterator in
 * the chain.
 *
 * <p>Copyright &copy; IBM Corporation 1999.  All rights reserved.
 *
 * @author Alan Liu
 * @version $RCSfile: CompoundTransliterator.java,v $ $Revision: 1.14 $ $Date: 2001/09/21 21:24:04 $
 */
public class CompoundTransliterator extends Transliterator {

    private static final boolean DEBUG = false;

    private Transliterator[] trans;

    /**
     * Array of original filters associated with transliterators.
     */
    private UnicodeFilter[] filters = null;

    /**
     * For compound RBTs (those with an ::id block before and/or after
     * the main rule block) we record the index of the RBT here.
     * Otherwise, this should have a value of -1.  We need this
     * information to implement toRules().
     */
    private int compoundRBTIndex;    

    private static final String COPYRIGHT =
        "\u00A9 IBM Corporation 1999. All rights reserved.";

    /**
     * Constructs a new compound transliterator given an array of
     * transliterators.  The array of transliterators may be of any
     * length, including zero or one, however, useful compound
     * transliterators have at least two components.
     * @param transliterators array of <code>Transliterator</code>
     * objects
     * @param filter the filter.  Any character for which
     * <tt>filter.contains()</tt> returns <tt>false</tt> will not be
     * altered by this transliterator.  If <tt>filter</tt> is
     * <tt>null</tt> then no filtering is applied.
     */
    public CompoundTransliterator(Transliterator[] transliterators,
                                  UnicodeFilter filter) {
        super(joinIDs(transliterators), null); // don't set filter here!
        trans = new Transliterator[transliterators.length];
        System.arraycopy(transliterators, 0, trans, 0, trans.length);
        computeMaximumContextLength();
        if (filter != null) {
            setFilter(filter);
        }
    }

    /**
     * Constructs a new compound transliterator given an array of
     * transliterators.  The array of transliterators may be of any
     * length, including zero or one, however, useful compound
     * transliterators have at least two components.
     * @param transliterators array of <code>Transliterator</code>
     * objects
     */
    public CompoundTransliterator(Transliterator[] transliterators) {
        this(transliterators, null);
    }
    
    /**
     * Splits an ID of the form "ID;ID;..." into a compound using each
     * of the IDs. 
     * @param ID of above form
     * @param forward if false, does the list in reverse order, and
     * takes the inverse of each ID.
     */
    public CompoundTransliterator(String ID, int direction,
                                  UnicodeFilter filter) {
        // changed MED
        // Later, add "rule1[filter];rule2...
        super(ID, null); // don't set filter here!
        String[] list = split(ID, ';');
        trans = new Transliterator[list.length];
        for (int i = 0; i < list.length; ++i) {
            trans[i] = getInstance(list[direction==FORWARD ? i : (list.length-1-i)],
                                   direction);
        }
        
        // If the direction is REVERSE then we need to fix the ID.
        if (direction == REVERSE) {
            StringBuffer newID = new StringBuffer();
            for (int i=0; i<list.length; ++i) {
                if (i > 0) {
                    newID.append(';');
                }
                newID.append(trans[i].getID());
            }
            setID(newID.toString());
        }

        computeMaximumContextLength();
        if (filter != null) {
            setFilter(filter);
        }
    }
    
    public CompoundTransliterator(String ID, int direction) {
        this(ID, direction, null);
    }
    
    public CompoundTransliterator(String ID) {
        this(ID, FORWARD, null);
    }

    /**
     * Package private constructor for compound RBTs.  Construct a
     * compound transliterator using the given idBlock, with the
     * splitTrans inserted at the idSplitPoint.
     */
    CompoundTransliterator(String ID,
                           String idBlock,
                           int idSplitPoint,
                           Transliterator splitTrans) {
        super(ID, null);
        init(idBlock, FORWARD, idSplitPoint, splitTrans, false);
    }
    
    /**
     * Package private constructor for Transliterator from a vector of
     * transliterators.  The vector order is FORWARD, so if dir is
     * REVERSE then the vector order will be reversed.  The caller is
     * responsible for fixing up the ID.
     */
    CompoundTransliterator(int dir,
                           Vector list) {
        super("", null);
        trans = null;
        compoundRBTIndex = -1;
        init(list, dir, false);
        // assume caller will fixup ID
    }

    /**
     * Finish constructing a transliterator: only to be called by
     * constructors.  Before calling init(), set trans and filter to NULL.
     * @param id the id containing ';'-separated entries
     * @param direction either FORWARD or REVERSE
     * @param idSplitPoint the index into id at which the
     * splitTrans should be inserted, if there is one, or
     * -1 if there is none.
     * @param splitTrans a transliterator to be inserted
     * before the entry at offset idSplitPoint in the id string.  May be
     * NULL to insert no entry.
     * @param fixReverseID if TRUE, then reconstruct the ID of reverse
     * entries by calling getID() of component entries.  Some constructors
     * do not require this because they apply a facade ID anyway.
     */
    private void init(String id,
                      int direction,
                      int idSplitPoint,
                      Transliterator splitTrans,
                      boolean fixReverseID) {
        // assert(trans == 0);

        Vector list = new Vector();
        int[] splitTransIndex = new int[1];
        StringBuffer regenID = new StringBuffer();
        Transliterator.parseCompoundID(id, regenID, direction,
                                       idSplitPoint, splitTrans,
                                       list, splitTransIndex);
        compoundRBTIndex = splitTransIndex[0];

        init(list, direction, fixReverseID);
    }

    /**
     * Finish constructing a transliterator: only to be called by
     * constructors.  Before calling init(), set trans and filter to NULL.
     * @param list a vector of transliterator objects to be adopted.  It
     * should NOT be empty.  The list should be in declared order.  That
     * is, it should be in the FORWARD order; if direction is REVERSE then
     * the list order will be reversed.
     * @param direction either FORWARD or REVERSE
     * @param fixReverseID if TRUE, then reconstruct the ID of reverse
     * entries by calling getID() of component entries.  Some constructors
     * do not require this because they apply a facade ID anyway.
     */
    private void init(Vector list,
                      int direction,
                      boolean fixReverseID) {
        // assert(trans == 0);

        // Allocate array
        int count = list.size();
        trans = new Transliterator[count];

        // Move the transliterators from the vector into an array.
        // Reverse the order if necessary.
        int i;
        for (i=0; i<count; ++i) {
            int j = (direction == FORWARD) ? i : count - 1 - i;
            trans[i] = (Transliterator) list.elementAt(j);
        }

        // Fix compoundRBTIndex for REVERSE transliterators
        if (compoundRBTIndex >= 0 && direction == REVERSE) {
            compoundRBTIndex = count - 1 - compoundRBTIndex;
        }

        // If the direction is UTRANS_REVERSE then we may need to fix the
        // ID.
        if (direction == REVERSE && fixReverseID) {
            StringBuffer newID = new StringBuffer();
            for (i=0; i<count; ++i) {
                if (i > 0) {
                    newID.append(ID_DELIM);
                }
                newID.append(trans[i].getID());
            }
            setID(newID.toString());
        }

        computeMaximumContextLength();
    }

    /**
     * Return the IDs of the given list of transliterators, concatenated
     * with ';' delimiting them.  Equivalent to the perlish expression
     * join(';', map($_.getID(), transliterators).
     */
    private static String joinIDs(Transliterator[] transliterators) {
        StringBuffer id = new StringBuffer();
        for (int i=0; i<transliterators.length; ++i) {
            if (i > 0) {
                id.append(';');
            }
            id.append(transliterators[i].getID());
        }
        return id.toString();
    }

    /**
     * Splits a string, as in JavaScript
     */
    private static String[] split(String s, char divider) {
        // changed MED

	    // see how many there are
	    int count = 1;
	    for (int i = 0; i < s.length(); ++i) {
	        if (s.charAt(i) == divider) ++count;
	    }
	    
	    // make an array with them
	    String[] result = new String[count];
	    int last = 0;
	    int current = 0;
	    int i;
	    for (i = 0; i < s.length(); ++i) {
	        if (s.charAt(i) == divider) {
	            result[current++] = s.substring(last,i);
	            last = i+1;
	        }
	    }
	    result[current++] = s.substring(last,i);
	    return result;
	}
    

    /**
     * Returns the number of transliterators in this chain.
     * @return number of transliterators in this chain.
     */
    public int getCount() {
        return trans.length;
    }

    /**
     * Returns the transliterator at the given index in this chain.
     * @param index index into chain, from 0 to <code>getCount() - 1</code>
     * @return transliterator at the given index
     */
    public Transliterator getTransliterator(int index) {
        return trans[index];
    }

    /**
     * Override Transliterator.  Modify the transliterators that make up
     * this compound transliterator so their filters are the logical AND
     * of this transliterator's filter and their own.  Original filters
     * are kept in the filters array.
     */
    public void setFilter(UnicodeFilter f) {
        /**
         * If there is a filter F for the compound transliterator as a
         * whole, then we need to modify every non-null filter f in
         * the chain to be f' = F & f.
         *
         * If anyone else is using the transliterators in the chain
         * outside of this context, they will get unexpected results.
         */
        if (f == null) {
            // Restore original filters
            if (filters != null) {
                for (int i=0; i<filters.length; ++i) {
                    trans[i].setFilter(filters[i]);
                }
            }
        } else {
            if (filters == null) {
                filters = new UnicodeFilter[trans.length];
                for (int i=0; i<filters.length; ++i) {
                    filters[i] = trans[i].getFilter();
                }
            }
            for (int i=0; i<filters.length; ++i) {
                trans[i].setFilter(UnicodeFilterLogic.and(f, filters[i]));
            }
        }
        super.setFilter(f);
    }

    public String toRules(boolean escapeUnprintable) {
        // We do NOT call toRules() on our component transliterators, in
        // general.  If we have several rule-based transliterators, this
        // yields a concatenation of the rules -- not what we want.  We do
        // handle compound RBT transliterators specially -- those for which
        // compoundRBTIndex >= 0.  For the transliterator at compoundRBTIndex,
        // we do call toRules() recursively.
        StringBuffer rulesSource = new StringBuffer();
        for (int i=0; i<trans.length; ++i) {
            String rule;
            if (i == compoundRBTIndex) {
                rule = trans[i].toRules(escapeUnprintable);
            } else {
                rule = trans[i].baseToRules(escapeUnprintable);
            }
            if (rulesSource.length() != 0 &&
                rulesSource.charAt(rulesSource.length() - 1) != '\n') {
                rulesSource.append('\n');
            }
            rulesSource.append(rule);
            if (rulesSource.length() != 0 &&
                rulesSource.charAt(rulesSource.length() - 1) != ID_DELIM) {
                rulesSource.append(ID_DELIM);
            }
        }
        return rulesSource.toString();
    }

    /**
     * Implements {@link Transliterator#handleTransliterate}.
     */
    protected void handleTransliterate(Replaceable text,
                                       Position index, boolean incremental) {
        /* Call each transliterator with the same start value and
         * initial cursor index, but with the limit index as modified
         * by preceding transliterators.  The cursor index must be
         * reset for each transliterator to give each a chance to
         * transliterate the text.  The initial cursor index is known
         * to still point to the same place after each transliterator
         * is called because each transliterator will not change the
         * text between start and the initial value of cursor.
         *
         * IMPORTANT: After the first transliterator, each subsequent
         * transliterator only gets to transliterate text committed by
         * preceding transliterators; that is, the cursor (output
         * value) of transliterator i becomes the limit (input value)
         * of transliterator i+1.  Finally, the overall limit is fixed
         * up before we return.
         *
         * Assumptions we make here:
         * (1) start <= cursor <= limit    ;cursor valid on entry
         * (2) cursor <= cursor' <= limit' ;cursor doesn't move back
         * (3) cursor <= limit'            ;text before cursor unchanged
         * - cursor' is the value of cursor after calling handleKT
         * - limit' is the value of limit after calling handleKT
         */

        /**
         * Example: 3 transliterators.  This example illustrates the
         * mechanics we need to implement.  S, C, and L are the start,
         * cursor, and limit.  gl is the globalLimit.
         *
         * 1. h-u, changes hex to Unicode
         *
         *    4  7  a  d  0      4  7  a
         *    abc/u0061/u    =>  abca/u    
         *    S  C       L       S   C L   gl=f->a
         *
         * 2. upup, changes "x" to "XX"
         *
         *    4  7  a       4  7  a
         *    abca/u    =>  abcAA/u    
         *    S  CL         S    C   
         *                       L    gl=a->b
         * 3. u-h, changes Unicode to hex
         *
         *    4  7  a        4  7  a  d  0  3
         *    abcAA/u    =>  abc/u0041/u0041/u    
         *    S  C L         S              C
         *                                  L   gl=b->15
         * 4. return
         *
         *    4  7  a  d  0  3
         *    abc/u0041/u0041/u    
         *    S C L
         */
        int cursor = index.start;
        int limit = index.limit;
        int globalLimit = limit;
        /* globalLimit is the overall limit.  We keep track of this
         * since we overwrite index.contextLimit with the previous
         * index.start.  After each transliteration, we update
         * globalLimit for insertions or deletions that have happened.
         */

        for (int i=0; i<trans.length; ++i) {
            index.start = cursor; // Reset cursor
            index.limit = limit;

            if (DEBUG) {
                System.out.print(Utility.escape(i + ": \"" +
                    substring(text, index.contextStart, index.start) + '|' +
                    substring(text, index.start, index.contextLimit) +
                    "\" -> \""));
            }

            trans[i].handleTransliterate(text, index, incremental);

            if (DEBUG) {
                System.out.println(Utility.escape(
                    substring(text, index.contextStart, index.start) + '|' +
                    substring(text, index.start, index.contextLimit) +
                    '"'));
            }

            // Adjust overall limit for insertions/deletions
            globalLimit += index.limit - limit;
            limit = index.start; // Move limit to end of committed text
        }
        // Cursor is good where it is -- where the last
        // transliterator left it.  Limit needs to be put back
        // where it was, modulo adjustments for deletions/insertions.
        index.limit = globalLimit;
    }

    /**
     * Compute and set the length of the longest context required by this transliterator.
     * This is <em>preceding</em> context.
     */
    private void computeMaximumContextLength() {
        int max = 0;
        for (int i=0; i<trans.length; ++i) {
            int len = trans[i].getMaximumContextLength();
            if (len > max) {
                max = len;
            }
        }
        setMaximumContextLength(max);
    }

    /**
     * DEBUG
     * Returns a substring of a Replaceable.
     */
    private static final String substring(Replaceable str, int start, int limit) {
        StringBuffer buf = new StringBuffer();
        while (start < limit) {
            buf.append(str.charAt(start++));
        }
        return buf.toString();
    }
}
