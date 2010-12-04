/*
*******************************************************************************
*   Copyright (C) 2010, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*   created on: 2010nov23
*   created by: Markus W. Scherer
*   ported from ICU4C bytetrie.h/.cpp
*/
package com.ibm.icu.impl;

/**
 * Light-weight, non-const reader class for a ByteTrie.
 * Traverses a byte-serialized data structure with minimal state,
 * for mapping byte sequences to non-negative integer values.
 *
 * @author Markus W. Scherer
 */
public final class ByteTrie {
    public ByteTrie(byte[] trieBytes, int offset) {
        bytes=trieBytes;
        pos=markedPos=root=offset;
        remainingMatchLength=markedRemainingMatchLength=-1;
        value=markedValue=0;
        haveValue=markedHaveValue=false;
    }

    /**
     * Resets this trie (and its marked state) to its initial state.
     */
    public ByteTrie reset() {
        pos=markedPos=root;
        remainingMatchLength=markedRemainingMatchLength=-1;
        haveValue=markedHaveValue=false;
        return this;
    }

    /**
     * Marks the state of this trie.
     * @see #resetToMark
     */
    public ByteTrie mark() {
        markedPos=pos;
        markedRemainingMatchLength=remainingMatchLength;
        markedValue=value;
        markedHaveValue=haveValue;
        return this;
    }

    /**
     * Resets this trie to the state at the time mark() was last called.
     * If mark() has not been called since the last reset()
     * then this is equivalent to reset() itself.
     * @see #mark
     * @see #reset
     */
    public ByteTrie resetToMark() {
        pos=markedPos;
        remainingMatchLength=markedRemainingMatchLength;
        value=markedValue;
        haveValue=markedHaveValue;
        return this;
    }

    /**
     * Tests whether some input byte can continue a matching byte sequence.
     * In other words, this is true when next(b) for some byte would return true.
     * @return true if some byte can continue a matching byte sequence.
     */
    public boolean hasNext() /*const*/ {
        int node;
        return pos>=0 &&  // more input, and
            (remainingMatchLength>=0 ||  // more linear-match bytes or
                // the next node is not a final-value node
                (node=bytes[pos]&0xff)<kMinValueLead || (node&kValueIsFinal)==0);
    }

    /**
     * Traverses the trie from the current state for this input byte.
     * @return true if the byte continues a matching byte sequence.
     */
    public boolean next(int inByte) {
        if(pos<0) {
            return false;
        }
        haveValue=false;
        int length=remainingMatchLength;  // Actual remaining match length minus 1.
        if(length>=0) {
            // Remaining part of a linear-match node.
            if(inByte==(bytes[pos]&0xff)) {
                remainingMatchLength=length-1;
                ++pos;
                return true;
            } else {
                // No match.
                stop();
                return false;
            }
        }
        int node=bytes[pos]&0xff;
        if(node>=kMinValueLead) {
            if((node&kValueIsFinal)!=0) {
                // No further matching bytes.
                stop();
                return false;
            } else {
                // Skip intermediate value.
                pos+=bytesPerLead[node>>1];
                // The next node must not also be a value node.
                node=bytes[pos];
                assert(0<=node && node<kMinValueLead);
            }
        }
        ++pos;
        if(node<kMinLinearMatch) {
            // Branch according to the current byte.
            while(node<kMinListBranch) {
                // Branching on a byte value,
                // with a jump delta for less-than, a compact int for equals,
                // and continuing for greater-than.
                // The less-than and greater-than branches must lead to branch nodes again.
                int trieByte=bytes[pos++]&0xff;
                if(inByte<trieByte) {
                    int delta=readFixedInt(node);
                    pos+=delta;
                } else {
                    pos+=node+1;  // Skip fixed-width integer.
                    node=bytes[pos]&0xff;
                    assert(node>=kMinValueLead);
                    if(inByte==trieByte) {
                        if((node&kValueIsFinal)!=0) {
                            // Leave the final value for hasValue() to read.
                        } else {
                            // Use the non-final value as the jump delta.
                            ++pos;
                            readCompactInt(node);
                            pos+=value;
                        }
                        return true;
                    } else {  // inByte>trieByte
                        pos+=bytesPerLead[node>>1];
                    }
                }
                node=bytes[pos++];
                assert(0<=node && node<kMinLinearMatch);
            }
            // Branch node with a list of key-value pairs where
            // values are compact integers: either final values or jump deltas.
            // If the last key byte matches, just continue after it rather
            // than jumping.
            length=node-(kMinListBranch-1);  // Actual list length minus 1.
            for(;;) {
                int trieByte=bytes[pos++]&0xff;
                assert(length==0 || (bytes[pos]&0xff)>=kMinValueLead);
                if(inByte==trieByte) {
                    if(length>0) {
                        node=bytes[pos]&0xff;
                        if((node&kValueIsFinal)!=0) {
                            // Leave the final value for hasValue() to read.
                        } else {
                            // Use the non-final value as the jump delta.
                            ++pos;
                            readCompactInt(node);
                            pos+=value;
                        }
                    }
                    return true;
                }
                if(inByte<trieByte || length--==0) {
                    stop();
                    return false;
                }
                pos+=bytesPerLead[(bytes[pos]&0xff)>>1];
            }
        } else {
            // Match the first of length+1 bytes.
            length=node-kMinLinearMatch;  // Actual match length minus 1.
            if(inByte==(bytes[pos]&0xff)) {
                remainingMatchLength=length-1;
                ++pos;
                return true;
            } else {
                // No match.
                stop();
                return false;
            }
        }
    }

    /**
     * @return true if the trie contains the byte sequence so far.
     *         In this case, an immediately following call to getValue()
     *         returns the byte sequence's value.
     *         hasValue() is only defined if called from the initial state
     *         or immediately after next() returns true.
     */
    public boolean hasValue() {
        int node;
        if(haveValue) {
            return true;
        } else if(pos>=0 && remainingMatchLength<0 && (node=bytes[pos]&0xff)>=kMinValueLead) {
            // Deliver value for the matching bytes.
            ++pos;
            if(readCompactInt(node)) {
                stop();
            }
            haveValue=false;
            return true;
        }
        return false;
    }

    /**
     * Traverses the trie from the current state for this byte sequence,
     * calls next(b) for each byte b in the sequence,
     * and calls hasValue() at the end.
     */
    // public boolean hasValue(const char *s, int length);

    /**
     * Returns a byte sequence's value if called immediately after hasValue()
     * returned true. Otherwise undefined.
     */
    public int getValue() /*const*/ { return value; }

    /**
     * Determines whether all byte sequences reachable from the current state
     * map to the same value.
     * Sets hasValue() to the return value of this function, and if there is
     * a unique value, then a following getValue() will return that unique value.
     *
     * Aside from hasValue()/getValue(),
     * after this function returns the trie will be in the same state as before.
     *
     * @return true if all byte sequences reachable from the current state
     *         map to the same value.
     */
    public boolean hasUniqueValue() {
        if(pos<0) {
            return false;
        }
        // Save state variables that will be modified, for restoring
        // before we return.
        // We will use pos to move through the trie,
        // markedValue/markedHaveValue for the unique value,
        // and value/haveValue for the latest value we find.
        int originalPos=pos;
        int originalMarkedValue=markedValue;
        boolean originalMarkedHaveValue=markedHaveValue;
        markedValue=value;
        markedHaveValue=haveValue;

        if(remainingMatchLength>=0) {
            // Skip the rest of a pending linear-match node.
            pos+=remainingMatchLength+1;
        }
        haveValue=findUniqueValue();
        // If haveValue is true, then value is already set to the final value
        // of the last-visited branch.
        // Restore original state, except for value/haveValue.
        pos=originalPos;
        markedValue=originalMarkedValue;
        markedHaveValue=originalMarkedHaveValue;
        return haveValue;
    }

    // TODO: For startsWith() functionality, add
    //   boolean getRemainder(ByteSink *remainingBytes, &value);
    // Returns true if exactly one byte sequence can be reached from the current iterator state.
    // The remainingBytes sink will receive the remaining bytes of that one sequence.
    // It might receive some bytes even when the function returns false.

    private void stop() {
        pos=-1;
    }

    // Reads a compact 32-bit integer and post-increments pos.
    // pos is already after the leadByte.
    // Returns true if the integer is a final value.
    private boolean readCompactInt(int leadByte) {
        boolean isFinal= (leadByte&kValueIsFinal)!=0;
        leadByte>>=1;
        int numBytes=bytesPerLead[leadByte]-1;  // -1: lead byte was already consumed.
        switch(numBytes) {
        case 0:
            value=leadByte-kMinOneByteLead;
            break;
        case 1:
            value=((leadByte-kMinTwoByteLead)<<8)|(bytes[pos]&0xff);
            break;
        case 2:
            value=((leadByte-kMinThreeByteLead)<<16)|((bytes[pos]&0xff)<<8)|(bytes[pos+1]&0xff);
            break;
        case 3:
            value=((bytes[pos]&0xff)<<16)|((bytes[pos+1]&0xff)<<8)|(bytes[pos+2]&0xff);
            break;
        case 4:
            value=(bytes[pos]<<24)|((bytes[pos+1]&0xff)<<16)|((bytes[pos+2]&0xff)<<8)|(bytes[pos+3]&0xff);
            break;
        }
        pos+=numBytes;
        return isFinal;
    }
    // pos is on the leadByte.
    // private boolean readCompactInt() {
    //     int leadByte=bytes[pos++]&0xff;
    //     return readCompactInt(leadByte);
    // }

    // Reads a fixed-width integer and post-increments pos.
    private int readFixedInt(int bytesPerValue) {
        int fixedInt;
        switch(bytesPerValue) {  // Actually number of bytes minus 1.
        case 0:
            fixedInt=(bytes[pos]&0xff);
            break;
        case 1:
            fixedInt=((bytes[pos]&0xff)<<8)|(bytes[pos+1]&0xff);
            break;
        case 2:
            fixedInt=((bytes[pos]&0xff)<<16)|((bytes[pos+1]&0xff)<<8)|(bytes[pos+2]&0xff);
            break;
        case 3:
            fixedInt=(bytes[pos]<<24)|((bytes[pos+1]&0xff)<<16)|((bytes[pos+2]&0xff)<<8)|(bytes[pos+3]&0xff);
            break;
        default:
            ///CLOVER:OFF
            // unreachable
            fixedInt=-1;
            break;
            ///CLOVER:ON
        }
        pos+=bytesPerValue+1;
        return fixedInt;
    }

    // Helper functions for hasUniqueValue().
    // Compare the latest value with the previous one, or save the latest one.
    private boolean isUniqueValue() {
        if(markedHaveValue) {
            if(value!=markedValue) {
                return false;
            }
        } else {
            markedValue=value;
            markedHaveValue=true;
        }
        return true;
    }
    // Recurse into a branch edge and return to the current position.
    private boolean findUniqueValueAt(int delta) {
        int currentPos=pos;
        pos+=delta;
        if(!findUniqueValue()) {
            return false;
        }
        pos=currentPos;
        return true;
    }
    // Handle a branch node entry (final value or jump delta).
    private boolean findUniqueValueFromBranchEntry() {
        int node=bytes[pos++]&0xff;
        assert(node>=kMinValueLead);
        if(readCompactInt(node)) {
            // Final value directly in the branch entry.
            if(!isUniqueValue()) {
                return false;
            }
        } else {
            // Use the non-final value as the jump delta.
            if(!findUniqueValueAt(value)) {
                return false;
            }
        }
        return true;
    }
    // Recursively find a unique value (or whether there is not a unique one)
    // starting from a position on a node lead unit.
    private boolean findUniqueValue() {
        for(;;) {
            int node=bytes[pos++]&0xff;
            if(node>=kMinValueLead) {
                boolean isFinal=readCompactInt(node);
                if(!isUniqueValue()) {
                    return false;
                }
                if(isFinal) {
                    return true;
                }
                node=bytes[pos++]&0xff;
                assert(node<kMinValueLead);
            }
            if(node<kMinLinearMatch) {
                while(node<kMinListBranch) {
                    // three-way-branch node
                    ++pos;  // ignore the comparison byte
                    // less-than branch
                    int delta=readFixedInt(node);
                    if(!findUniqueValueAt(delta)) {
                        return false;
                    }
                    // equals branch
                    if(!findUniqueValueFromBranchEntry()) {
                        return false;
                    }
                    // greater-than branch
                    node=bytes[pos++]&0xff;
                    assert(node<kMinLinearMatch);
                }
                // list-branch node
                int length=node-(kMinListBranch-1);  // Actual list length minus 1.
                do {
                    ++pos;  // ignore a comparison byte
                    // handle its value
                    if(!findUniqueValueFromBranchEntry()) {
                        return false;
                    }
                } while(--length>0);
                ++pos;  // ignore the last comparison byte
            } else {
                // linear-match node
                pos+=node-kMinLinearMatch+1;  // Ignore the match bytes.
            }
        }
    }

    // ByteTrie data structure
    //
    // The trie consists of a series of byte-serialized nodes for incremental
    // string/byte sequence matching. The root node is at the beginning of the trie data.
    //
    // Types of nodes are distinguished by their node lead byte ranges.
    // After each node, except a final-value node, another node follows to
    // encode match values or continue matching further bytes.
    //
    // Node types:
    //  - Value node: Stores a 32-bit integer in a compact, variable-length format.
    //    The value is for the string/byte sequence so far.
    //  - Linear-match node: Matches a number of bytes.
    //  - Branch node: Branches to other nodes according to the current input byte.
    //    - List-branch node: If the input byte is in the list, a "jump"
    //        leads to another node for further matching.
    //        Instead of a jump, a final value may be stored.
    //        For the last byte listed there is no "jump" or value directly in
    //        the branch node: Instead, matching continues with the next node.
    //    - Three-way-branch node: Compares the input byte with one included byte.
    //        If less-than, "jumps" to another node which is a branch node.
    //        If equals, "jumps" to another node (any type) or stores a final value.
    //        If greater-than, matching continues with the next node which is a branch node.

    // Node lead byte values.

    // 0..3: Three-way-branch node with less/equal/greater outbound edges.
    // The 2 lower bits indicate the length of the less-than "jump" (1..4 bytes).
    // Followed by the comparison byte, the equals value (compact int) and
    // continue reading the next node from there for the "greater" edge.

    // 04..0b: Branch node with a list of 2..9 comparison bytes.
    // Followed by the (key, value) pairs except that the last byte's value is omitted
    // (just continue reading the next node from there).
    // Values are compact ints: Final values or jump deltas.
    private static final int kMinListBranch=4;
    private static final int kMaxListBranchLength=9;

    // 0c..1f: Linear-match node, match 1..24 bytes and continue reading the next node.
    private static final int kMinLinearMatch=kMinListBranch+kMaxListBranchLength-1;  // 0xc
    private static final int kMaxLinearMatchLength=20;

    // 20..ff: Variable-length value node.
    // If odd, the value is final. (Otherwise, intermediate value or jump delta.)
    // Then shift-right by 1 bit.
    // The remaining lead byte value indicates the number of following bytes (0..4)
    // and contains the value's top bits.
    private static final int kMinValueLead=kMinLinearMatch+kMaxLinearMatchLength;  // 0x20
    // It is a final value if bit 0 is set.
    private static final int kValueIsFinal=1;

    // Compact int: After testing bit 0, shift right by 1 and then use the following thresholds.
    private static final int kMinOneByteLead=kMinValueLead/2;  // 0x10
    private static final int kMaxOneByteValue=0x40;  // At least 6 bits in the first byte.

    private static final int kMinTwoByteLead=kMinOneByteLead+kMaxOneByteValue+1;  // 0x51
    private static final int kMaxTwoByteValue=0x1aff;

    private static final int kMinThreeByteLead=kMinTwoByteLead+(kMaxTwoByteValue>>8)+1;  // 0x6c
    // private static final int kFourByteLead=0x7e;

    // A little more than Unicode code points.
    // private static final int kMaxThreeByteValue=((kFourByteLead-kMinThreeByteLead)<<16)-1;  // 0x11ffff;

    // private static final int kFiveByteLead=0x7f;

    // Map a shifted-right compact-int lead byte to its number of bytes.
    private static final byte bytesPerLead[/*kFiveByteLead+1*/]={
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3,
        3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 5
    };

    // Fixed value referencing the ByteTrie bytes.
    private byte[] bytes;
    private int root;

    // Iterator variables.

    // Pointer to next trie byte to read. NULL if no more matches.
    private int pos;
    // Remaining length of a linear-match node, minus 1. Negative if not in such a node.
    private int remainingMatchLength;
    // Value for a match, after hasValue() returned true.
    private int value;
    private boolean haveValue;

    // mark() and resetToMark() variables
    private int markedPos;
    private int markedRemainingMatchLength;
    private int markedValue;
    private boolean markedHaveValue;
    // Note: If it turns out that constructor and reset() are too slow because
    // of the extra mark() variables, then we could move them out into
    // a separate state object which is passed into mark() and resetToMark().
    // Usage of those functions would be a little more clunky,
    // especially in Java where the state object would have to be heap-allocated.
};
