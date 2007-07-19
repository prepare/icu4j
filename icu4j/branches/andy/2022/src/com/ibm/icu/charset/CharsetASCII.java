/**
 *******************************************************************************
 * Copyright (C) 2006, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 *******************************************************************************
 */
package com.ibm.icu.charset;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.UTF16;

class CharsetASCII extends CharsetICU {
    protected byte[] fromUSubstitution = new byte[] { (byte) 0x1a };

    public CharsetASCII(String icuCanonicalName, String javaCanonicalName, String[] aliases) {
        super(icuCanonicalName, javaCanonicalName, aliases);
        maxBytesPerChar = 1;
        minBytesPerChar = 1;
        maxCharsPerByte = 1;
    }

    class CharsetDecoderASCII extends CharsetDecoderICU {

        public CharsetDecoderASCII(CharsetICU cs) {
            super(cs);
        }

        protected CoderResult decodeLoop(ByteBuffer source, CharBuffer target, IntBuffer offsets,
                boolean flush) {
            if (!source.hasRemaining()) {
                /* no input, nothing to do */
                return CoderResult.UNDERFLOW;
            }
            if (!target.hasRemaining()) {
                /* no output available, can't do anything */
                return CoderResult.OVERFLOW;
            }

            CoderResult cr;
            int oldSource = source.position();
            int oldTarget = target.position();

            if (source.hasArray() && target.hasArray()) {
                /* optimized loop */

                /*
                 * extract arrays from the buffers and obtain various constant values that will be
                 * necessary in the core loop
                 */
                byte[] sourceArray = source.array();
                int sourceOffset = source.arrayOffset();
                int sourceIndex = oldSource + sourceOffset;
                int sourceLength = source.limit() - oldSource;
                
                char[] targetArray = target.array();
                int targetOffset = target.arrayOffset();
                int targetIndex = oldTarget + targetOffset;
                int targetLength = target.limit() - oldTarget;

                int limit = ((sourceLength < targetLength) ? sourceLength : targetLength)
                        + sourceIndex;
                int offset = targetIndex - sourceIndex;

                /*
                 * perform the core loop... if it returns null, it must be due to an overflow or
                 * underflow
                 */
                if ((cr = decodeLoopCoreOptimized(source, target, sourceArray, targetArray,
                        sourceIndex, offset, limit)) == null) {
                    if (sourceLength <= targetLength) {
                        source.position(oldSource + sourceLength);
                        target.position(oldTarget + sourceLength);
                        cr = CoderResult.UNDERFLOW;
                    } else {
                        source.position(oldSource + targetLength);
                        target.position(oldTarget + targetLength);
                        cr = CoderResult.OVERFLOW;
                    }
                }
            } else {
                /* unoptimized loop */

                try {
                    /*
                     * perform the core loop... if it throws an exception, it must be due to an
                     * overflow or underflow
                     */
                    cr = decodeLoopCoreUnoptimized(source, target);

                } catch (BufferUnderflowException ex) {
                    /* all of the source has been read */
                    cr = CoderResult.UNDERFLOW;
                } catch (BufferOverflowException ex) {
                    /* the target is full */
                    source.position(source.position() - 1); /* rewind by 1 */
                    cr = CoderResult.OVERFLOW;
                }
            }

            /* set offsets since the start */
            if (offsets != null) {
                int count = target.position() - oldTarget;
                int sourceIndex = -1;
                while (--count >= 0)
                    offsets.put(++sourceIndex);
            }

            return cr;
        }

        protected CoderResult decodeLoopCoreOptimized(ByteBuffer source, CharBuffer target,
                byte[] sourceArray, char[] targetArray, int oldSource, int offset, int limit) {
            int i, ch = 0;

            /*
             * perform ascii conversion from the source array to the target array, making sure each
             * byte in the source is within the correct range
             */
            for (i = oldSource; i < limit && (((ch = (sourceArray[i] & 0xff)) & 0x80) == 0); i++)
                targetArray[i + offset] = (char) ch;

            /*
             * if some byte was not in the correct range, we need to deal with this byte by calling
             * decodeMalformedOrUnmappable and move the source and target positions to reflect the
             * early termination of the loop
             */
            if ((ch & 0x80) != 0) {
                source.position(i + 1);
                target.position(i + offset);
                return decodeMalformedOrUnmappable(ch);
            } else
                return null;
        }

        protected CoderResult decodeLoopCoreUnoptimized(ByteBuffer source, CharBuffer target)
                throws BufferUnderflowException, BufferOverflowException {
            int ch = 0;

            /*
             * perform ascii conversion from the source buffer to the target buffer, making sure
             * each byte in the source is within the correct range
             */
            while (((ch = (source.get() & 0xff)) & 0x80) == 0)
                target.put((char) ch);

            /*
             * if we reach here, it's because a character was not in the correct range, and we need
             * to deak with this by calling decodeMalformedOrUnmappable
             */
            return decodeMalformedOrUnmappable(ch);
        }

        protected CoderResult decodeMalformedOrUnmappable(int ch) {
            /*
             * put the guilty character into toUBytesArray and return a message saying that the
             * character was malformed and of length 1.
             */
            toUBytesArray[0] = (byte) ch;
            return CoderResult.malformedForLength(toULength = 1);
        }
    }

    class CharsetEncoderASCII extends CharsetEncoderICU {

        public CharsetEncoderASCII(CharsetICU cs) {
            super(cs, fromUSubstitution);
            implReset();
        }

        private final static int NEED_TO_WRITE_BOM = 1;

        protected void implReset() {
            super.implReset();
            fromUnicodeStatus = NEED_TO_WRITE_BOM;
        }

        protected CoderResult encodeLoop(CharBuffer source, ByteBuffer target, IntBuffer offsets,
                boolean flush) {
            if (!source.hasRemaining()) {
                /* no input, nothing to do */
                return CoderResult.UNDERFLOW;
            }
            if (!target.hasRemaining()) {
                /* no output available, can't do anything */
                return CoderResult.OVERFLOW;
            }

            CoderResult cr;
            int oldSource = source.position();
            int oldTarget = target.position();

            if (fromUChar32 != 0) {
                /*
                 * if we have a leading character in fromUChar32 that needs to be dealt with, we
                 * need to check for a matching trail character and taking the appropriate action as
                 * dictated by encodeTrail.
                 */
                cr = encodeTrail(source, (char) fromUChar32, flush);
            } else {
                if (source.hasArray() && target.hasArray()) {
                    /* optimized loop */

                    /*
                     * extract arrays from the buffers and obtain various constant values that will
                     * be necessary in the core loop
                     */
                    char[] sourceArray = source.array();
                    int sourceOffset = source.arrayOffset();
                    int sourceIndex = oldSource + sourceOffset;
                    int sourceLength = source.limit() - oldSource;

                    byte[] targetArray = target.array();
                    int targetOffset = target.arrayOffset();
                    int targetIndex = oldTarget + targetOffset;
                    int targetLength = target.limit() - oldTarget;

                    int limit = ((sourceLength < targetLength) ? sourceLength : targetLength)
                            + sourceIndex;
                    int offset = targetIndex - sourceIndex;

                    /*
                     * perform the core loop... if it returns null, it must be due to an overflow or
                     * underflow
                     */
                    if ((cr = encodeLoopCoreOptimized(source, target, sourceArray, targetArray,
                            sourceIndex, offset, limit, flush)) == null) {
                        if (sourceLength <= targetLength) {
                            source.position(oldSource + sourceLength);
                            target.position(oldTarget + sourceLength);
                            cr = CoderResult.UNDERFLOW;
                        } else {
                            source.position(oldSource + targetLength);
                            target.position(oldTarget + targetLength);
                            cr = CoderResult.OVERFLOW;
                        }
                    }
                } else {
                    /* unoptimized loop */

                    try {
                        /*
                         * perform the core loop... if it throws an exception, it must be due to an
                         * overflow or underflow
                         */
                        cr = encodeLoopCoreUnoptimized(source, target, flush);

                    } catch (BufferUnderflowException ex) {
                        cr = CoderResult.UNDERFLOW;
                    } catch (BufferOverflowException ex) {
                        source.position(source.position() - 1); /* rewind by 1 */
                        cr = CoderResult.OVERFLOW;
                    }
                }
            }

            /* set offsets since the start */
            if (offsets != null) {
                int count = target.position() - oldTarget;
                int sourceIndex = -1;
                while (--count >= 0)
                    offsets.put(++sourceIndex);
            }

            return cr;
        }

        protected CoderResult encodeLoopCoreOptimized(CharBuffer source, ByteBuffer target,
                char[] sourceArray, byte[] targetArray, int oldSource, int offset, int limit,
                boolean flush) {
            int i, ch = 0;

            /*
             * perform ascii conversion from the source array to the target array, making sure each
             * char in the source is within the correct range
             */
            for (i = oldSource; i < limit && (((ch = (int) sourceArray[i]) & 0xff80) == 0); i++)
                targetArray[i + offset] = (byte) ch;

            /*
             * if some byte was not in the correct range, we need to deal with this byte by calling
             * encodeMalformedOrUnmappable and move the source and target positions to reflect the
             * early termination of the loop
             */
            if ((ch & 0xff80) != 0) {
                source.position(i + 1);
                target.position(i + offset);
                return encodeMalformedOrUnmappable(source, ch, flush);
            } else
                return null;
        }

        protected CoderResult encodeLoopCoreUnoptimized(CharBuffer source, ByteBuffer target,
                boolean flush) throws BufferUnderflowException, BufferOverflowException {
            int ch;

            /*
             * perform ascii conversion from the source buffer to the target buffer, making sure
             * each char in the source is within the correct range
             */
            while (((ch = (int) source.get()) & 0xff80) == 0)
                target.put((byte) ch);

            /*
             * if we reach here, it's because a character was not in the correct range, and we need
             * to deak with this by calling encodeMalformedOrUnmappable.
             */
            return encodeMalformedOrUnmappable(source, ch, flush);
        }

        protected CoderResult encodeMalformedOrUnmappable(CharBuffer source, int ch, boolean flush) {
            /*
             * if the character is a lead surrogate, we need to call encodeTrail to attempt to match
             * it up with a trail surrogate. if not, the character is unmappable.
             */
            return (UTF16.isLeadSurrogate((char) ch))
                    ? encodeTrail(source, (char) ch, flush)
                    : CoderResult.unmappableForLength(1);
        }

        protected CoderResult encodeTrail(CharBuffer source, char lead, boolean flush) {
            /*
             * if the next character is a trail surrogate, we have an unmappable codepoint of length
             * 2. if the next character is not a trail surrogate, we have a single malformed
             * character. if there is no next character, we either have a malformed character or an
             * underflow, depending on whether flush is enabled.
             */
            if (source.hasRemaining()) {
                char trail = source.get();
                if (UTF16.isTrailSurrogate(trail)) {
                    fromUChar32 = UCharacter.getCodePoint(lead, trail);
                    return CoderResult.unmappableForLength(2); /* two chars */
                } else {
                    fromUChar32 = lead;
                    source.position(source.position() - 1); /* rewind by 1 */
                    return CoderResult.malformedForLength(1);
                }
            } else {
                fromUChar32 = lead;
                if (flush)
                    return CoderResult.malformedForLength(1);
                else
                    return CoderResult.UNDERFLOW;
            }
        }

    }

    public CharsetDecoder newDecoder() {
        return new CharsetDecoderASCII(this);
    }

    public CharsetEncoder newEncoder() {
        return new CharsetEncoderASCII(this);
    }
}
