/*
*******************************************************************************
* Copyright (C) 2013, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* collationdatareader.h
*
* @since 2013feb07
* @author Markus W. Scherer
*/

package com.ibm.icu.impl.coll;

#include "unicode/udata.h"

struct UDataMemory;

U_NAMESPACE_BEGIN

struct CollationTailoring;

/**
 * Collation binary data reader.
 */
final class CollationDataReader /* all static */ {
    // The following constants are also copied into source/common/ucol_swp.cpp.
    // Keep them in sync!
    enum {
        /**
         * Number of int indexes.
         *
         * Can be 2 if there are only options.
         * Can be 7 or 8 if there are only options and a script reordering.
         * The loader treats any index>=indexes[IX_INDEXES_LENGTH] as 0.
         */
        IX_INDEXES_LENGTH,  // 0
        /**
         * Bits 31..24: numericPrimary, for numeric collation
         *      23..16: fast Latin format version (0 = no fast Latin table)
         *      15.. 0: options bit set
         */
        IX_OPTIONS,
        IX_RESERVED2,
        IX_RESERVED3,

        /** Array offset to Jamo CE32s in ce32s[], or <0 if none. */
        IX_JAMO_CE32S_START,  // 4

        // Byte offsets from the start of the data, after the generic header.
        // The indexes[] are at byte offset 0, other data follows.
        // Each data item is aligned properly.
        // The data items should be in descending order of unit size,
        // to minimize the need for padding.
        // Each item's byte length is given by the difference between its offset and
        // the next index/offset value.
        /** Byte offset to int reorderCodes[]. */
        IX_REORDER_CODES_OFFSET,
        /**
         * Byte offset to uint8_t reorderTable[].
         * Empty table if <256 bytes (padding only).
         * Otherwise 256 bytes or more (with padding).
         */
        IX_REORDER_TABLE_OFFSET,
        /** Byte offset to the collation trie. Its length is a multiple of 8 bytes. */
        IX_TRIE_OFFSET,

        IX_RESERVED8_OFFSET,  // 8
        /** Byte offset to long ces[]. */
        IX_CES_OFFSET,
        IX_RESERVED10_OFFSET,
        /** Byte offset to int ce32s[]. */
        IX_CE32S_OFFSET,

        /** Byte offset to uint32_t rootElements[]. */
        IX_ROOT_ELEMENTS_OFFSET,  // 12
        /** Byte offset to UChar *contexts[]. */
        IX_CONTEXTS_OFFSET,
        /** Byte offset to char [] with serialized unsafeBackwardSet. */
        IX_UNSAFE_BWD_OFFSET,
        /** Byte offset to char fastLatinTable[]. */
        IX_FAST_LATIN_TABLE_OFFSET,

        /** Byte offset to char scripts[]. */
        IX_SCRIPTS_OFFSET,  // 16
        /**
         * Byte offset to boolean compressibleBytes[].
         * Empty table if <256 bytes (padding only).
         * Otherwise 256 bytes or more (with padding).
         */
        IX_COMPRESSIBLE_BYTES_OFFSET,
        IX_RESERVED18_OFFSET,
        IX_TOTAL_SIZE
    };

    static void read(const CollationTailoring *base, const uint8_t *inBytes, int inLength,
                     CollationTailoring &tailoring);

    static boolean U_CALLCONV
    isAcceptable(void *context, const char *type, const char *name, const UDataInfo *pInfo);

private:
    CollationDataReader();  // no constructor
};

/*
 * Format of collation data (ucadata.icu, binary data in coll/ *.res files):
 * See ICU4C source/common/collationdatareader.h.
 */

U_NAMESPACE_END

#endif  // !UCONFIG_NO_COLLATION
#endif  // __COLLATIONDATAREADER_H__
/*
*******************************************************************************
* Copyright (C) 2013-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* collationdatareader.cpp
*
* @since 2013feb07
* @author Markus W. Scherer
*/

#include "unicode/utypes.h"

#if !UCONFIG_NO_COLLATION

#include "unicode/ucol.h"
#include "unicode/udata.h"
#include "unicode/uscript.h"
#include "cmemory.h"
#include "collation.h"
#include "collationdata.h"
#include "collationdatareader.h"
#include "collationfastlatin.h"
#include "collationkeys.h"
#include "collationrootelements.h"
#include "collationsettings.h"
#include "collationtailoring.h"
#include "normalizer2impl.h"
#include "uassert.h"
#include "ucmndata.h"
#include "utrie2.h"

int32_t getIndex(const int *indexes, int length, int i) {
    return (i < length) ? indexes[i] : -1;
}

}  // namespace

void
CollationDataReader.read(const CollationTailoring *base, const uint8_t *inBytes, int inLength,
                          CollationTailoring &tailoring) {
    if(U_FAILURE) { return; }
    if(base != null) {
        if(inBytes == null || (0 <= inLength && inLength < 24)) {
            errorCode = U_ILLEGAL_ARGUMENT_ERROR;
            return;
        }
        const DataHeader *header = reinterpret_cast<const DataHeader *>(inBytes);
        if(!(header.dataHeader.magic1 == 0xda && header.dataHeader.magic2 == 0x27 &&
                isAcceptable(tailoring.version, null, null, &header.info))) {
            errorCode = U_INVALID_FORMAT_ERROR;
            return;
        }
        if(base.getUCAVersion() != tailoring.getUCAVersion()) {
            errorCode = U_COLLATOR_VERSION_MISMATCH;
            return;
        }
        int headerLength = header.dataHeader.headerSize;
        inBytes += headerLength;
        if(inLength >= 0) {
            inLength -= headerLength;
        }
    }

    if(inBytes == null || (0 <= inLength && inLength < 8)) {
        errorCode = U_ILLEGAL_ARGUMENT_ERROR;
        return;
    }
    const int *inIndexes = reinterpret_cast<const int *>(inBytes);
    int indexesLength = inIndexes[IX_INDEXES_LENGTH];
    if(indexesLength < 2 || (0 <= inLength && inLength < indexesLength * 4)) {
        errorCode = U_INVALID_FORMAT_ERROR;  // Not enough indexes.
        return;
    }

    // Assume that the tailoring data is in initial state,
    // with null pointers and 0 lengths.

    // Set pointers to non-empty data parts.
    // Do this in order of their byte offsets. (Should help porting to Java.)

    int index;  // one of the indexes[] slots
    int offset;  // byte offset for the index part
    int length;  // number of bytes in the index part

    if(indexesLength > IX_TOTAL_SIZE) {
        length = inIndexes[IX_TOTAL_SIZE];
    } else if(indexesLength > IX_REORDER_CODES_OFFSET) {
        length = inIndexes[indexesLength - 1];
    } else {
        length = 0;  // only indexes, and inLength was already checked for them
    }
    if(0 <= inLength && inLength < length) {
        errorCode = U_INVALID_FORMAT_ERROR;
        return;
    }

    CollationData baseData = base == null ? null : base.data;
    const int *reorderCodes = null;
    int reorderCodesLength = 0;
    index = IX_REORDER_CODES_OFFSET;
    offset = getIndex(inIndexes, indexesLength, index);
    length = getIndex(inIndexes, indexesLength, index + 1) - offset;
    if(length >= 4) {
        if(baseData == null) {
            // We assume for collation settings that
            // the base data does not have a reordering.
            errorCode = U_INVALID_FORMAT_ERROR;
            return;
        }
        reorderCodes = reinterpret_cast<const int *>(inBytes + offset);
        reorderCodesLength = length / 4;
    }

    // There should be a reorder table only if there are reorder codes.
    // However, when there are reorder codes the reorder table may be omitted to reduce
    // the data size.
    const uint8_t *reorderTable = null;
    index = IX_REORDER_TABLE_OFFSET;
    offset = getIndex(inIndexes, indexesLength, index);
    length = getIndex(inIndexes, indexesLength, index + 1) - offset;
    if(length >= 256) {
        if(reorderCodesLength == 0) {
            errorCode = U_INVALID_FORMAT_ERROR;  // Reordering table without reordering codes.
            return;
        }
        reorderTable = inBytes + offset;
    } else {
        // If we have reorder codes, then build the reorderTable at the end,
        // when the CollationData is otherwise complete.
    }

    if(baseData != null && baseData.numericPrimary != (inIndexes[IX_OPTIONS] & 0xff000000L)) {
        errorCode = U_INVALID_FORMAT_ERROR;
        return;
    }
    CollationData *data = null;  // Remains null if there are no mappings.

    index = IX_TRIE_OFFSET;
    offset = getIndex(inIndexes, indexesLength, index);
    length = getIndex(inIndexes, indexesLength, index + 1) - offset;
    if(length >= 8) {
        if(!tailoring.ensureOwnedData) { return; }
        data = tailoring.ownedData;
        data.base = baseData;
        data.numericPrimary = inIndexes[IX_OPTIONS] & 0xff000000L;
        data.trie = tailoring.trie = utrie2_openFromSerialized(
            UTRIE2_32_VALUE_BITS, inBytes + offset, length, null,
            &errorCode);
        if(U_FAILURE) { return; }
    } else if(baseData != null) {
        // Use the base data. Only the settings are tailored.
        tailoring.data = baseData;
    } else {
        errorCode = U_INVALID_FORMAT_ERROR;  // No mappings.
        return;
    }

    index = IX_CES_OFFSET;
    offset = getIndex(inIndexes, indexesLength, index);
    length = getIndex(inIndexes, indexesLength, index + 1) - offset;
    if(length >= 8) {
        if(data == null) {
            errorCode = U_INVALID_FORMAT_ERROR;  // Tailored ces without tailored trie.
            return;
        }
        data.ces = reinterpret_cast<const long *>(inBytes + offset);
        data.cesLength = length / 8;
    }

    index = IX_CE32S_OFFSET;
    offset = getIndex(inIndexes, indexesLength, index);
    length = getIndex(inIndexes, indexesLength, index + 1) - offset;
    if(length >= 4) {
        if(data == null) {
            errorCode = U_INVALID_FORMAT_ERROR;  // Tailored ce32s without tailored trie.
            return;
        }
        data.ce32s = reinterpret_cast<const uint32_t *>(inBytes + offset);
        data.ce32sLength = length / 4;
    }

    int jamoCE32sStart = getIndex(inIndexes, indexesLength, IX_JAMO_CE32S_START);
    if(jamoCE32sStart >= 0) {
        if(data == null || data.ce32s == null) {
            errorCode = U_INVALID_FORMAT_ERROR;  // Index into non-existent ce32s[].
            return;
        }
        data.jamoCE32s = data.ce32s + jamoCE32sStart;
    } else if(data == null) {
        // Nothing to do.
    } else if(baseData != null) {
        data.jamoCE32s = baseData.jamoCE32s;
    } else {
        errorCode = U_INVALID_FORMAT_ERROR;  // No Jamo CE32s for Hangul processing.
        return;
    }

    index = IX_ROOT_ELEMENTS_OFFSET;
    offset = getIndex(inIndexes, indexesLength, index);
    length = getIndex(inIndexes, indexesLength, index + 1) - offset;
    if(length >= 4) {
        length /= 4;
        if(data == null || length <= CollationRootElements.IX_SEC_TER_BOUNDARIES) {
            errorCode = U_INVALID_FORMAT_ERROR;
            return;
        }
        // TODO: read uint32_t rootElements into a new long[rootElementsLength]
        data.rootElements = reinterpret_cast<const uint32_t *>(inBytes + offset);
        data.rootElementsLength = length;
        uint32_t commonSecTer = data.rootElements[CollationRootElements.IX_COMMON_SEC_AND_TER_CE];
        if(commonSecTer != Collation.COMMON_SEC_AND_TER_CE) {
            errorCode = U_INVALID_FORMAT_ERROR;
            return;
        }
        long secTerBoundaries = data.rootElements[CollationRootElements.IX_SEC_TER_BOUNDARIES];
        if((secTerBoundaries >> 24) < CollationKeys.SEC_COMMON_HIGH) {
            // [fixed last secondary common byte] is too low,
            // and secondary weights would collide with compressed common secondaries.
            errorCode = U_INVALID_FORMAT_ERROR;
            return;
        }
    }

    index = IX_CONTEXTS_OFFSET;
    offset = getIndex(inIndexes, indexesLength, index);
    length = getIndex(inIndexes, indexesLength, index + 1) - offset;
    if(length >= 2) {
        if(data == null) {
            errorCode = U_INVALID_FORMAT_ERROR;  // Tailored contexts without tailored trie.
            return;
        }
        data.contexts = reinterpret_cast<const UChar *>(inBytes + offset);
        data.contextsLength = length / 2;
    }

    index = IX_UNSAFE_BWD_OFFSET;
    offset = getIndex(inIndexes, indexesLength, index);
    length = getIndex(inIndexes, indexesLength, index + 1) - offset;
    if(length >= 2) {
        if(data == null) {
            errorCode = U_INVALID_FORMAT_ERROR;
            return;
        }
        if(baseData == null) {
            // Create the unsafe-backward set for the root collator.
            // Include all non-zero combining marks and trail surrogates.
            // We do this at load time, rather than at build time,
            // to simplify Unicode version bootstrapping:
            // The root data builder only needs the new FractionalUCA.txt data,
            // but it need not be built with a version of ICU already updated to
            // the corresponding new Unicode Character Database.
            //
            // The following is an optimized version of
            // new UnicodeSet("[[:^lccc=0:][\\udc00-\\udfff]]").
            // It is faster and requires fewer code dependencies.
            tailoring.unsafeBackwardSet = new UnicodeSet(0xdc00, 0xdfff);  // trail surrogates
            if(tailoring.unsafeBackwardSet == null) {
                errorCode = U_MEMORY_ALLOCATION_ERROR;
                return;
            }
            data.nfcImpl.addLcccChars(*tailoring.unsafeBackwardSet);
        } else {
            // Clone the root collator's set contents.
            tailoring.unsafeBackwardSet = static_cast<UnicodeSet *>(
                baseData.unsafeBackwardSet.cloneAsThawed());
            if(tailoring.unsafeBackwardSet == null) {
                errorCode = U_MEMORY_ALLOCATION_ERROR;
                return;
            }
        }
        // Add the ranges from the data file to the unsafe-backward set.
        USerializedSet sset;
        const char *unsafeData = reinterpret_cast<const char *>(inBytes + offset);
        if(!uset_getSerializedSet(&sset, unsafeData, length / 2)) {
            errorCode = U_INVALID_FORMAT_ERROR;
            return;
        }
        int count = uset_getSerializedRangeCount(&sset);
        for(int i = 0; i < count; ++i) {
            int start, end;
            uset_getSerializedRange(&sset, i, &start, &end);
            tailoring.unsafeBackwardSet.add(start, end);
        }
        // Mark each lead surrogate as "unsafe"
        // if any of its 1024 associated supplementary code points is "unsafe".
        int c = 0x10000;
        for(UChar lead = 0xd800; lead < 0xdc00; ++lead, c += 0x400) {
            if(!tailoring.unsafeBackwardSet.containsNone(c, c + 0x3ff)) {
                tailoring.unsafeBackwardSet.add(lead);
            }
        }
        tailoring.unsafeBackwardSet.freeze();
        data.unsafeBackwardSet = tailoring.unsafeBackwardSet;
    } else if(data == null) {
        // Nothing to do.
    } else if(baseData != null) {
        // No tailoring-specific data: Alias the root collator's set.
        data.unsafeBackwardSet = baseData.unsafeBackwardSet;
    } else {
        errorCode = U_INVALID_FORMAT_ERROR;  // No unsafeBackwardSet.
        return;
    }

    // If the fast Latin format version is different,
    // or the version is set to 0 for "no fast Latin table",
    // then just always use the normal string comparison path.
    if(data != null) {
        data.fastLatinTable = null;
        data.fastLatinTableLength = 0;
        if(((inIndexes[IX_OPTIONS] >> 16) & 0xff) == CollationFastLatin.VERSION) {
            index = IX_FAST_LATIN_TABLE_OFFSET;
            offset = getIndex(inIndexes, indexesLength, index);
            length = getIndex(inIndexes, indexesLength, index + 1) - offset;
            if(length > 0) {
                data.fastLatinTable = reinterpret_cast<const char *>(inBytes + offset);
                data.fastLatinTableLength = length / 2;
                if((data.fastLatinTable[0] >> 8) != CollationFastLatin.VERSION) {
                    errorCode = U_INVALID_FORMAT_ERROR;  // header vs. table version mismatch
                    return;
                }
            } else if(baseData != null) {
                data.fastLatinTable = baseData.fastLatinTable;
                data.fastLatinTableLength = baseData.fastLatinTableLength;
            }
        }
    }

    index = IX_SCRIPTS_OFFSET;
    offset = getIndex(inIndexes, indexesLength, index);
    length = getIndex(inIndexes, indexesLength, index + 1) - offset;
    if(length >= 2) {
        if(data == null) {
            errorCode = U_INVALID_FORMAT_ERROR;
            return;
        }
        data.scripts = reinterpret_cast<const char *>(inBytes + offset);
        data.scriptsLength = length / 2;
    } else if(data == null) {
        // Nothing to do.
    } else if(baseData != null) {
        data.scripts = baseData.scripts;
        data.scriptsLength = baseData.scriptsLength;
    }

    index = IX_COMPRESSIBLE_BYTES_OFFSET;
    offset = getIndex(inIndexes, indexesLength, index);
    length = getIndex(inIndexes, indexesLength, index + 1) - offset;
    if(length >= 256) {
        if(data == null) {
            errorCode = U_INVALID_FORMAT_ERROR;
            return;
        }
        data.compressibleBytes = reinterpret_cast<const boolean *>(inBytes + offset);
    } else if(data == null) {
        // Nothing to do.
    } else if(baseData != null) {
        data.compressibleBytes = baseData.compressibleBytes;
    } else {
        errorCode = U_INVALID_FORMAT_ERROR;  // No compressibleBytes[].
        return;
    }

    const CollationSettings &ts = *tailoring.settings;
    int options = inIndexes[IX_OPTIONS] & 0xffff;
    char fastLatinPrimaries[CollationFastLatin.LATIN_LIMIT];
    int fastLatinOptions = CollationFastLatin.getOptions(
            tailoring.data, ts, fastLatinPrimaries, LENGTHOF(fastLatinPrimaries));
    if(options == ts.options && ts.variableTop != 0 &&
            reorderCodesLength == ts.reorderCodesLength &&
            uprv_memcmp(reorderCodes, ts.reorderCodes, reorderCodesLength * 4) == 0 &&
            fastLatinOptions == ts.fastLatinOptions &&
            (fastLatinOptions < 0 ||
                uprv_memcmp(fastLatinPrimaries, ts.fastLatinPrimaries,
                            sizeof(fastLatinPrimaries)) == 0)) {
        return;
    }

    CollationSettings *settings = SharedObject.copyOnWrite(tailoring.settings);
    if(settings == null) {
        errorCode = U_MEMORY_ALLOCATION_ERROR;
        return;
    }
    settings.options = options;
    // Set variableTop from options and scripts data.
    settings.variableTop = tailoring.data.getLastPrimaryForGroup(
            Collator.ReorderCodes.FIRST + settings.getMaxVariable());
    if(settings.variableTop == 0) {
        errorCode = U_INVALID_FORMAT_ERROR;
        return;
    }

    if(reorderCodesLength == 0 || reorderTable != null) {
        settings.aliasReordering(reorderCodes, reorderCodesLength, reorderTable);
    } else {
        uint8_t table[256];
        baseData.makeReorderTable(reorderCodes, reorderCodesLength, table);
        if(U_FAILURE) { return; }
        if(!settings.setReordering(reorderCodes, reorderCodesLength,table)) {
            errorCode = U_MEMORY_ALLOCATION_ERROR;
            return;
        }
    }

    settings.fastLatinOptions = CollationFastLatin.getOptions(
        tailoring.data, *settings,
        settings.fastLatinPrimaries, LENGTHOF(settings.fastLatinPrimaries));
}

boolean U_CALLCONV
CollationDataReader.isAcceptable(void *context,
                                  const char * /* type */, const char * /*name*/,
                                  const UDataInfo *pInfo) {
    if(
        pInfo.size >= 20 &&
        pInfo.isBigEndian == U_IS_BIG_ENDIAN &&
        pInfo.charsetFamily == U_CHARSET_FAMILY &&
        pInfo.dataFormat[0] == 0x55 &&  // dataFormat="UCol"
        pInfo.dataFormat[1] == 0x43 &&
        pInfo.dataFormat[2] == 0x6f &&
        pInfo.dataFormat[3] == 0x6c &&
        pInfo.formatVersion[0] == 4
    ) {
        VersionInfo *version = static_cast<VersionInfo *>(context);
        if(version != null) {
            uprv_memcpy(version, pInfo.dataVersion, 4);
        }
        return true;
    } else {
        return false;
    }
}

U_NAMESPACE_END

#endif  // !UCONFIG_NO_COLLATION
