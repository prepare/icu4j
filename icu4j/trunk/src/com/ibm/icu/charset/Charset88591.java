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
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

class Charset88591 extends CharsetASCII {
    public Charset88591(String icuCanonicalName, String javaCanonicalName, String[] aliases) {
        super(icuCanonicalName, javaCanonicalName, aliases);
    }

    class CharsetDecoder88591 extends CharsetDecoderASCII {
        public CharsetDecoder88591(CharsetICU cs) {
            super(cs);
        }

        protected CoderResult decodeLoopCoreOptimized(ByteBuffer source, CharBuffer target,
                byte[] sourceArray, char[] targetArray, int oldSource, int offset, int limit) {

            /*
             * perform 88591 conversion from the source array to the target array. no range check is
             * necessary.
             */
            for (int i = oldSource; i < limit; i++)
                targetArray[i + offset] = (char) (sourceArray[i] & 0xff);

            return null;
        }

        protected CoderResult decodeLoopCoreUnoptimized(ByteBuffer source, CharBuffer target)
                throws BufferUnderflowException, BufferOverflowException {

            /*
             * perform 88591 conversion from the source buffer to the target buffer. no range check
             * is necessary (an exception will be generated to end the loop).
             */
            while (true)
                target.put((char) (source.get() & 0xff));
        }
    }

    class CharsetEncoder88591 extends CharsetEncoderASCII {
        public CharsetEncoder88591(CharsetICU cs) {
            super(cs);
        }

        protected CoderResult encodeLoopCoreOptimized(CharBuffer source, ByteBuffer target,
                char[] sourceArray, byte[] targetArray, int oldSource, int offset, int limit,
                boolean flush) {
            int i, ch = 0;

            /*
             * perform 88591 conversion from the source array to the target array, making sure each
             * char in the source is within the correct range
             */
            for (i = oldSource; i < limit && (((ch = (int) sourceArray[i]) & 0xff00) == 0); i++)
                targetArray[i + offset] = (byte) ch;

            /*
             * if some byte was not in the correct range, we need to deal with this byte by calling
             * encodeMalformedOrUnmappable and move the source and target positions to reflect the
             * early termination of the loop
             */
            if ((ch & 0xff00) != 0) {
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
             * perform 88591 conversion from the source buffer to the target buffer, making sure
             * each char in the source is within the correct range
             */
            while (((ch = (int) source.get()) & 0xff00) == 0)
                target.put((byte) ch);

            /*
             * if we reach here, it's because a character was not in the correct range, and we need
             * to deak with this by calling encodeMalformedOrUnmappable.
             */
            return encodeMalformedOrUnmappable(source, ch, flush);
        }

    }

    public CharsetDecoder newDecoder() {
        return new CharsetDecoder88591(this);
    }

    public CharsetEncoder newEncoder() {
        return new CharsetEncoder88591(this);
    }

}
