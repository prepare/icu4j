/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/text/CanonicalIterator.java,v $ 
 * $Date: 2002/09/17 19:11:52 $ 
 * $Revision: 1.12 $
 *
 *****************************************************************************************
 */
package com.ibm.icu.text;

import com.ibm.icu.lang.*;
import java.util.*;
import com.ibm.icu.impl.NormalizerImpl;
import com.ibm.icu.impl.USerializedSet;
/**
 * This class allows one to iterate through all the strings that are canonically equivalent to a given
 * string. For example, here are some sample results:
 * Results for: {A WITH RING ABOVE}{d}{DOT ABOVE}{CEDILLA}
 * <pre>
 1: {A}{RING ABOVE}{d}{DOT ABOVE}{CEDILLA}
 2: {A}{RING ABOVE}{d}{CEDILLA}{DOT ABOVE}
 3: {A}{RING ABOVE}{d WITH DOT ABOVE}{CEDILLA}
 4: {A}{RING ABOVE}{d WITH CEDILLA}{DOT ABOVE}
 5: {A WITH RING ABOVE}{d}{DOT ABOVE}{CEDILLA}
 6: {A WITH RING ABOVE}{d}{CEDILLA}{DOT ABOVE}
 7: {A WITH RING ABOVE}{d WITH DOT ABOVE}{CEDILLA}
 8: {A WITH RING ABOVE}{d WITH CEDILLA}{DOT ABOVE}
 9: {ANGSTROM SIGN}{d}{DOT ABOVE}{CEDILLA}
10: {ANGSTROM SIGN}{d}{CEDILLA}{DOT ABOVE}
11: {ANGSTROM SIGN}{d WITH DOT ABOVE}{CEDILLA}
12: {ANGSTROM SIGN}{d WITH CEDILLA}{DOT ABOVE}
 *</pre>
 *<br>Note: the code is intended for use with small strings, and is not suitable for larger ones,
 * since it has not been optimized for that situation.
 * @author M. Davis
 * @draft ICU 2.4
 */

public final class CanonicalIterator {
    /**
     * Construct a CanonicalIterator object
     * @param source string to get results for
	 * @draft ICU 2.4
     */
    public CanonicalIterator(String source) {
        setSource(source);
    }
    
    /**
     * Gets the NFD form of the current source we are iterating over.
     * @return gets the source: NOTE: it is the NFD form of the source originally passed in
	 * @draft ICU 2.4
     */
    public String getSource() {
      return source;
    }
    
    /**
     * Resets the iterator so that one can start again from the beginning.
	 * @draft ICU 2.4
     */
    public void reset() {
        done = false;
        for (int i = 0; i < current.length; ++i) {
            current[i] = 0;
        }
    }
    
    /**
     * Get the next canonically equivalent string.
	 * <br><b>Warning: The strings are not guaranteed to be in any particular order.</b>
     * @return the next string that is canonically equivalent. The value null is returned when
     * the iteration is done.
	 * @draft ICU 2.4
     */
    public String next() {
        if (done) return null;
        
        // construct return value
        
        buffer.setLength(0); // delete old contents
        for (int i = 0; i < pieces.length; ++i) {
            buffer.append(pieces[i][current[i]]);
        }
        String result = buffer.toString();
        
        // find next value for next time
        
        for (int i = current.length - 1; ; --i) {
            if (i < 0) {
                done = true;
                break;
            }
            current[i]++;
            if (current[i] < pieces[i].length) break; // got sequence
            current[i] = 0;
        }
        return result;
    }
    
    /**
     * Set a new source for this iterator. Allows object reuse.
     * @param set the source string to iterate against. This allows the same iterator to be used
     * while changing the source string, saving object creation.
	 * @draft ICU 2.4
     */
    public void setSource(String newSource) {
        source = Normalizer.normalize(newSource, Normalizer.NFD);
        done = false;
        
        // catch degenerate case
    	if (newSource.length() == 0) {
    		pieces = new String[1][];
    		current = new int[1];
    		pieces[0] = new String[]{""};
    		return;
    	}
        
        // find the segments
        List list = new ArrayList();
        int cp;
        int start = 0;
        
	    // i should be the end of the first code point
	    
	    int i = UTF16.findOffsetFromCodePoint(source, 1);
        
        for (; i < source.length(); i += UTF16.getCharCount(i)) {
            cp = UTF16.charAt(source, i);
            if (NormalizerImpl.isCanonSafeStart(cp)) {
                list.add(source.substring(start, i)); // add up to i
                start = i;
            }
        }
        list.add(source.substring(start, i)); // add last one
        
        // allocate the arrays, and find the strings that are CE to each segment
        pieces = new String[list.size()][];
        current = new int[list.size()];
        for (i = 0; i < pieces.length; ++i) {
            if (PROGRESS) System.out.println("SEGMENT");
            pieces[i] = getEquivalents((String) list.get(i));
        }
    }
    
    /**
     * Simple implementation of permutation. 
	 * <br><b>Warning: The strings are not guaranteed to be in any particular order.</b>
     * @param source the string to find permutations for
     * @param the set to add the results to
     * @internal
     */
    public static void permute(String source, boolean skipZeros, Set output) {
    	// TODO: optimize
        //if (PROGRESS) System.out.println("Permute: " + source);
        
        // optimization:
        // if zero or one character, just return a set with it
        // we check for length < 2 to keep from counting code points all the time
        if (source.length() <= 2 && UTF16.countCodePoint(source) <= 1) {
            output.add(source);
            return;
        }
        
        // otherwise iterate through the string, and recursively permute all the other characters
        Set subpermute = new HashSet();
        int cp;
        for (int i = 0; i < source.length(); i += UTF16.getCharCount(cp)) {
            cp = UTF16.charAt(source, i);
            
        	// optimization:
        	// if the character is canonical combining class zero,
        	// don't permute it
        	if (skipZeros && i != 0 && UCharacter.getCombiningClass(cp) == 0) {
        		//System.out.println("Skipping " + Utility.hex(UTF16.valueOf(source, i)));
	        	continue;
        	}            
            
            // see what the permutations of the characters before and after this one are
            subpermute.clear();
            permute(source.substring(0,i) 
            	+ source.substring(i + UTF16.getCharCount(cp)), skipZeros, subpermute);
            
            // prefix this character to all of them
            String chStr = UTF16.valueOf(source, i);
            Iterator it = subpermute.iterator();
            while (it.hasNext()) {
                String piece = chStr + (String) it.next();
                //if (PROGRESS) System.out.println("  Piece: " + piece);
                output.add(piece);
            }
        }
        return;
    }
    
    // FOR TESTING
    
    /**
     *@return the set of "safe starts", characters that are class zero AND are never non-initial in a decomposition.
     *@internal
     *
    public static UnicodeSet getSafeStart() {
        return (UnicodeSet) SAFE_START.clone();
    }
    */
    /**
     *@return the set of characters whose decompositions start with the given character
     *@internal
     *
    public static UnicodeSet getStarts(int cp) {
        UnicodeSet result = AT_START.get(cp);
        if (result == null) result = EMPTY;
        return (UnicodeSet) result.clone();
    }
    */
    
    // ===================== PRIVATES ==============================
    
    // debug
    private static boolean PROGRESS = false; // debug progress
    private static Transliterator NAME = PROGRESS ? Transliterator.getInstance("name") : null;
    private static boolean SKIP_ZEROS = true;
 
    // fields
    private String source;
    private boolean done;
    private String[][] pieces;
    private int[] current;
    // Note: C will need two more fields, since arrays there don't have lengths
    // int pieces_length;
    // int[] pieces_lengths;
    
    // transient fields
    private transient StringBuffer buffer = new StringBuffer();
    
    
    // we have a segment, in NFD. Find all the strings that are canonically equivalent to it.
    private String[] getEquivalents(String segment) {
        Set result = new HashSet();
        Set basic = getEquivalents2(segment);
        Set permutations = new HashSet();
        
        // now get all the permutations
        // add only the ones that are canonically equivalent
        // TODO: optimize by not permuting any class zero.
        Iterator it = basic.iterator();
        while (it.hasNext()) {
            String item = (String) it.next();
        	permutations.clear();
            permute(item, SKIP_ZEROS, permutations);
            Iterator it2 = permutations.iterator();
            while (it2.hasNext()) {
                String possible = (String) it2.next();
                
/*               
				String attempt = Normalizer.normalize(possible, Normalizer.DECOMP, 0);
				if (attempt.equals(segment)) {
*/
                if (Normalizer.compare(possible, segment,0)==0) {
             	               	
            		if (PROGRESS) System.out.println("Adding Permutation: " + NAME.transliterate(possible));
                	result.add(possible);

                } else {
            		if (PROGRESS) System.out.println("-Skipping Permutation: " + NAME.transliterate(possible));
                }
            }
        }
        
        // convert into a String[] to clean up storage
        String[] finalResult = new String[result.size()];
        result.toArray(finalResult);
        return finalResult;
    }
    
     
    private Set getEquivalents2(String segment) {
        
        Set result = new HashSet();
        
        if (PROGRESS) System.out.println("Adding: " + NAME.transliterate(segment));
        
        result.add(segment);
        StringBuffer workingBuffer = new StringBuffer();
        
        // cycle through all the characters
        int cp=0,end=0;
	    int[] range = new int[2];
        for (int i = 0; i < segment.length(); i += UTF16.getCharCount(cp)) {
            
	        // see if any character is at the start of some decomposition
	        cp = UTF16.charAt(segment, i);;
	        USerializedSet starts = new USerializedSet();
           
            if (!NormalizerImpl.getCanonStartSet(cp, starts)) {
	          continue;
	        }
	        int j=0;
            // if so, see which decompositions match 
	        for(j = 0, cp = end+1; cp <= end ||starts.getSerializedRange(j++, range); ++cp) {
                if(cp>end){
                    cp=range[0];
                    end=range[1];
                }
                
	            Set remainder = extract(cp, segment, i,workingBuffer);
	            if (remainder == null) continue;
	
	            // there were some matches, so add all the possibilities to the set.
	            String prefix= segment.substring(0,i);
	            prefix += UTF16.valueOf(cp);
	            int el = -1;
	            Iterator iter = remainder.iterator();
	            while (iter.hasNext()) {
	                String item = (String) iter.next();
	                String toAdd = new String(prefix);
	                toAdd += item;
	                result.add(toAdd);		
	                //if (PROGRESS) printf("Adding: %s\n", UToS(Tr(*toAdd)));
	            }

            }
	    }
	    return result;
        /*
        Set result = new HashSet();
        if (PROGRESS) System.out.println("Adding: " + NAME.transliterate(segment));
        result.add(segment);
        StringBuffer workingBuffer = new StringBuffer();
        
        // cycle through all the characters
        int cp;
        
        for (int i = 0; i < segment.length(); i += UTF16.getCharCount(cp)) {
            // see if any character is at the start of some decomposition
            cp = UTF16.charAt(segment, i);
            NormalizerImpl.getCanonStartSet(c,fillSet)
            UnicodeSet starts = AT_START.get(cp);
            if (starts == null) continue;
            UnicodeSetIterator usi = new UnicodeSetIterator(starts);
            // if so, see which decompositions match
            while (usi.next()) {
                int cp2 = usi.codepoint;
                // we know that there are no strings in it
                // so we don't have to check CharacterIterator.IS_STRING
                Set remainder = extract(cp2, segment, i, workingBuffer);
                if (remainder == null) continue;
                
                // there were some matches, so add all the possibilities to the set.
                String prefix = segment.substring(0, i) + UTF16.valueOf(cp2);
                Iterator it = remainder.iterator();
                while (it.hasNext()) {
                    String item = (String) it.next();
                    if (PROGRESS) System.out.println("Adding: " + NAME.transliterate(prefix + item));
                    result.add(prefix + item);
                }
            }
        }
        return result;
        */
    }
    
    /**
     * See if the decomposition of cp2 is at segment starting at segmentPos 
     * (with canonical rearrangment!)
     * If so, take the remainder, and return the equivalents 
     */
    private Set extract(int comp, String segment, int segmentPos, StringBuffer buffer) {
        if (PROGRESS) System.out.println(" extract: " + NAME.transliterate(UTF16.valueOf(comp))
            + ", " + NAME.transliterate(segment.substring(segmentPos)));
            
        //String decomp = Normalizer.normalize(UTF16.valueOf(comp), Normalizer.DECOMP, 0);
        String decomp = Normalizer.normalize(comp, Normalizer.NFD);
        
        // See if it matches the start of segment (at segmentPos)
        boolean ok = false;
        int cp;
        int decompPos = 0;
        int decompCp = UTF16.charAt(decomp,0);
        decompPos += UTF16.getCharCount(decompCp); // adjust position to skip first char
        //int decompClass = getClass(decompCp);
        buffer.setLength(0); // initialize working buffer, shared among callees
        
        for (int i = segmentPos; i < segment.length(); i += UTF16.getCharCount(cp)) {
            cp = UTF16.charAt(segment, i);
            if (cp == decompCp) { // if equal, eat another cp from decomp
                if (PROGRESS) System.out.println("  matches: " + NAME.transliterate(UTF16.valueOf(cp)));
                if (decompPos == decomp.length()) { // done, have all decomp characters!
                    buffer.append(segment.substring(i + UTF16.getCharCount(cp))); // add remaining segment chars
                    ok = true;
                    break;
                }
                decompCp = UTF16.charAt(decomp, decompPos);
                decompPos += UTF16.getCharCount(decompCp);
                //decompClass = getClass(decompCp);
            } else {
                if (PROGRESS) System.out.println("  buffer: " + NAME.transliterate(UTF16.valueOf(cp)));
                // brute force approach
                UTF16.append(buffer, cp);
                /* TODO: optimize
                // since we know that the classes are monotonically increasing, after zero
                // e.g. 0 5 7 9 0 3
                // we can do an optimization
                // there are only a few cases that work: zero, less, same, greater
                // if both classes are the same, we fail
                // if the decomp class < the segment class, we fail
        
                segClass = getClass(cp);
                if (decompClass <= segClass) return null;
                */
            }
        }
        if (!ok) return null; // we failed, characters left over
        if (PROGRESS) System.out.println("Matches");
        if (buffer.length() == 0) return SET_WITH_NULL_STRING; // succeed, but no remainder
        String remainder = buffer.toString();
        
        // brute force approach
        // to check to make sure result is canonically equivalent
        /*
        String trial = Normalizer.normalize(UTF16.valueOf(comp) + remainder, Normalizer.DECOMP, 0);
        if (!segment.regionMatches(segmentPos, trial, 0, segment.length() - segmentPos)) return null;
        */
        
        if (0!=Normalizer.compare(UTF16.valueOf(comp) + remainder, segment.substring(segmentPos), 0)) return null;
        
        // get the remaining combinations
        return getEquivalents2(remainder);
    }
    
    /*
    // TODO: fix once we have a codepoint interface to get the canonical combining class
    // TODO: Need public access to canonical combining class in UCharacter!
    private static int getClass(int cp) {
        return Normalizer.getClass((char)cp);
    }
    */
    
   // ================= BUILDER =========================
    // TODO: Flatten this data so it doesn't have to be reconstructed each time!
    
    private static final UnicodeSet EMPTY = new UnicodeSet(); // constant, don't change
    private static final Set SET_WITH_NULL_STRING = new HashSet(); // constant, don't change
    static {
        SET_WITH_NULL_STRING.add("");
    }
    
  //  private static UnicodeSet SAFE_START = new UnicodeSet();
  //  private static CharMap AT_START = new CharMap();
    
        // TODO: WARNING, NORMALIZER doesn't have supplementaries yet !!;
        // Change FFFF to 10FFFF in C, and in Java when normalizer is upgraded.
  //  private static int LAST_UNICODE = 0x10FFFF;
    /*
    static {
        buildData();
    }
    */
    /*
    private static void buildData() {

        if (PROGRESS) System.out.println("Getting Safe Start");
        for (int cp = 0; cp <= LAST_UNICODE; ++cp) {
            if (PROGRESS & (cp & 0x7FF) == 0) System.out.print('.');
            int cc = UCharacter.getCombiningClass(cp);
            if (cc == 0) SAFE_START.add(cp);
            // will fix to be really safe below
        }
        if (PROGRESS) System.out.println();
        
        if (PROGRESS) System.out.println("Getting Containment");
        for (int cp = 0; cp <= LAST_UNICODE; ++cp) {
            if (PROGRESS & (cp & 0x7FF) == 0) System.out.print('.');
            
            if (Normalizer.isNormalized(cp, Normalizer.NFD)) continue;

            //String istr = UTF16.valueOf(cp);
            String decomp = Normalizer.normalize(cp, Normalizer.NFD);
            //if (decomp.equals(istr)) continue;
            
            // add each character in the decomposition to canBeIn 
            
            int component;
            for (int i = 0; i < decomp.length(); i += UTF16.getCharCount(component)) {
                component = UTF16.charAt(decomp, i);
                if (i == 0) {
                    AT_START.add(component, cp);
                } else if (UCharacter.getCombiningClass(component) == 0) {
                    SAFE_START.remove(component);
                }
            }
        }
        if (PROGRESS) System.out.println();
    }
        // the following is just for a map from characters to a set of characters
    
    private static class CharMap {
        Map storage = new HashMap();
        MutableInt probe = new MutableInt();
        boolean converted = false;
        
        public void add(int cp, int whatItIsIn) {
            UnicodeSet result = (UnicodeSet) storage.get(probe.set(cp));
            if (result == null) {
                result = new UnicodeSet();
                storage.put(probe, result);
            }
            result.add(whatItIsIn);
        }
        
        public UnicodeSet get(int cp) {
            return (UnicodeSet) storage.get(probe.set(cp));
        }
    }
            
    private static class MutableInt {
        public int contents;
        public int hashCode() { return contents; }
        public boolean equals(Object other) {
            return ((MutableInt)other).contents == contents;
        }
        // allows chaining
        public MutableInt set(int contents) {
            this.contents = contents;
            return this;
        }
    }
    */

}
    