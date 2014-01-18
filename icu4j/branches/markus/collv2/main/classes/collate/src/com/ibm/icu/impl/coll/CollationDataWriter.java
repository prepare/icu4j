/*
*******************************************************************************
* Copyright (C) 2013, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* collationdatawriter.h
*
* @since 2013aug06
* @author Markus W. Scherer
*/

package com.ibm.icu.impl.coll;

struct CollationData;
struct CollationSettings;
struct CollationTailoring;

/**
 * Collation-related code for tools & demos.
 */
final class CollationDataWriter /* all static */ {
public:
    static int writeBase(CollationData data, const CollationSettings &settings,
                             const void *rootElements, int rootElementsLength,
                             int indexes[], uint8_t *dest, int capacity,
                             );

    static int writeTailoring(const CollationTailoring &t, const CollationSettings &settings,
                                  int indexes[], uint8_t *dest, int capacity,
                                  );

private:
    CollationDataWriter();  // no constructor

    static int write(boolean isBase, const VersionInfo dataVersion,
                         CollationData data, const CollationSettings &settings,
                         const void *rootElements, int rootElementsLength,
                         int indexes[], uint8_t *dest, int capacity,
                         );

    static void copyData(const int indexes[], int startIndex,
                         const void *src, uint8_t *dest);
};

U_NAMESPACE_END

#endif  // !UCONFIG_NO_COLLATION
#endif  // __COLLATIONDATAWRITER_H__
/*
*******************************************************************************
* Copyright (C) 2013, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* collationdatawriter.cpp
*
* @since 2013aug06
* @author Markus W. Scherer
*/

#include "unicode/utypes.h"

#if !UCONFIG_NO_COLLATION

#include "unicode/tblcoll.h"
#include "unicode/udata.h"
#include "unicode/uniset.h"
#include "cmemory.h"
#include "collationdata.h"
#include "collationdatabuilder.h"
#include "collationdatareader.h"
#include "collationdatawriter.h"
#include "collationfastlatin.h"
#include "collationsettings.h"
#include "collationtailoring.h"
#include "uassert.h"
#include "ucmndata.h"

U_NAMESPACE_BEGIN

uint8_t *
RuleBasedCollator.cloneRuleData(int &length) {
    if(U_FAILURE) { return null; }
    LocalMemory<uint8_t> buffer((uint8_t *)uprv_malloc(20000));
    if(buffer.isNull()) {
        errorCode = U_MEMORY_ALLOCATION_ERROR;
        return null;
    }
    length = cloneBinary(buffer.getAlias(), 20000);
    if(errorCode == U_BUFFER_OVERFLOW_ERROR) {
        if(buffer.allocateInsteadAndCopy(length, 0) == null) {
            errorCode = U_MEMORY_ALLOCATION_ERROR;
            return null;
        }
        errorCode = U_ZERO_ERROR;
        length = cloneBinary(buffer.getAlias(), length);
    }
    if(U_FAILURE) { return null; }
    return buffer.orphan();
}

int32_t
RuleBasedCollator.cloneBinary(uint8_t *dest, int capacity) {
    int indexes[CollationDataReader.IX_TOTAL_SIZE + 1];
    return CollationDataWriter.writeTailoring(
            *tailoring, *settings, indexes, dest, capacity,
            errorCode);
}

private static final UDataInfo dataInfo = {
    sizeof(UDataInfo),
    0,

    U_IS_BIG_ENDIAN,
    U_CHARSET_FAMILY,
    U_SIZEOF_UCHAR,
    0,

    { 0x55, 0x43, 0x6f, 0x6c },         // dataFormat="UCol"
    { 4, 0, 0, 0 },                     // formatVersion
    { 6, 3, 0, 0 }                      // dataVersion
};

int32_t
CollationDataWriter.writeBase(CollationData data, const CollationSettings &settings,
                               const void *rootElements, int rootElementsLength,
                               int indexes[], uint8_t *dest, int capacity,
                               ) {
    return write(true, null,
                 data, settings,
                 rootElements, rootElementsLength,
                 indexes, dest, capacity);
}

int32_t
CollationDataWriter.writeTailoring(const CollationTailoring &t, const CollationSettings &settings,
                                    int indexes[], uint8_t *dest, int capacity,
                                    ) {
    return write(false, t.version,
                 *t.data, settings,
                 null, 0,
                 indexes, dest, capacity);
}

int32_t
CollationDataWriter.write(boolean isBase, const VersionInfo dataVersion,
                           CollationData data, const CollationSettings &settings,
                           const void *rootElements, int rootElementsLength,
                           int indexes[], uint8_t *dest, int capacity,
                           ) {
    if(U_FAILURE) { return 0; }
    if(capacity < 0 || (capacity > 0 && dest == null)) {
        errorCode = U_ILLEGAL_ARGUMENT_ERROR;
        return 0;
    }

    // Figure out which data items to write before settling on
    // the indexes length and writing offsets.
    // For any data item, we need to write the start and limit offsets,
    // so the indexes length must be at least index-of-start-offset + 2.
    int indexesLength;
    boolean hasMappings;
    UnicodeSet unsafeBackwardSet;
    CollationData baseData = data.base;

    int fastLatinVersion;
    if(data.fastLatinTable != null) {
        fastLatinVersion = CollationFastLatin.VERSION << 16;
    } else {
        fastLatinVersion = 0;
    }
    int fastLatinTableLength = 0;

    if(isBase) {
        // For the root collator, we write an even number of indexes
        // so that we start with an 8-aligned offset.
        indexesLength = CollationDataReader.IX_TOTAL_SIZE + 1;
        assert(settings.reorderCodesLength == 0);
        hasMappings = true;
        unsafeBackwardSet = *data.unsafeBackwardSet;
        fastLatinTableLength = data.fastLatinTableLength;
    } else if(baseData == null) {
        hasMappings = false;
        if(settings.reorderCodesLength == 0) {
            // only options
            indexesLength = CollationDataReader.IX_OPTIONS + 1;  // no limit offset here
        } else {
            // only options, reorder codes, and the reorder table
            indexesLength = CollationDataReader.IX_REORDER_TABLE_OFFSET + 2;
        }
    } else {
        hasMappings = true;
        // Tailored mappings, and what else?
        // Check in ascending order of optional tailoring data items.
        indexesLength = CollationDataReader.IX_CE32S_OFFSET + 2;
        if(data.contextsLength != 0) {
            indexesLength = CollationDataReader.IX_CONTEXTS_OFFSET + 2;
        }
        unsafeBackwardSet.addAll(*data.unsafeBackwardSet).removeAll(*baseData.unsafeBackwardSet);
        if(!unsafeBackwardSet.isEmpty()) {
            indexesLength = CollationDataReader.IX_UNSAFE_BWD_OFFSET + 2;
        }
        if(data.fastLatinTable != baseData.fastLatinTable) {
            fastLatinTableLength = data.fastLatinTableLength;
            indexesLength = CollationDataReader.IX_FAST_LATIN_TABLE_OFFSET + 2;
        }
    }

    int headerSize;
    if(isBase) {
        headerSize = 0;  // udata_create() writes the header
    } else {
        DataHeader header;
        header.dataHeader.magic1 = 0xda;
        header.dataHeader.magic2 = 0x27;
        uprv_memcpy(&header.info, &dataInfo, sizeof(UDataInfo));
        uprv_memcpy(header.info.dataVersion, dataVersion, sizeof(VersionInfo));
        headerSize = (int32_t)sizeof(header);
        assert((headerSize & 3) == 0);  // multiple of 4 bytes
        if(hasMappings && data.cesLength != 0) {
            // Sum of the sizes of the data items which are
            // not automatically multiples of 8 bytes and which are placed before the CEs.
            int sum = headerSize + (indexesLength + settings.reorderCodesLength) * 4;
            if((sum & 7) != 0) {
                // We need to add padding somewhere so that the 64-bit CEs are 8-aligned.
                // We add to the header size here.
                // Alternatively, we could increment the indexesLength
                // or add a few bytes to the reorderTable.
                headerSize += 4;
            }
        }
        header.dataHeader.headerSize = (char)headerSize;
        if(headerSize <= capacity) {
            uprv_memcpy(dest, &header, sizeof(header));
            // Write 00 bytes so that the padding is not mistaken for a copyright string.
            uprv_memset(dest + sizeof(header), 0, headerSize - (int32_t)sizeof(header));
            dest += headerSize;
            capacity -= headerSize;
        } else {
            dest = null;
            capacity = 0;
        }
    }

    indexes[CollationDataReader.IX_INDEXES_LENGTH] = indexesLength;
    assert((settings.options & ~0xffff) == 0);
    indexes[CollationDataReader.IX_OPTIONS] =
            data.numericPrimary | fastLatinVersion | settings.options;
    indexes[CollationDataReader.IX_RESERVED2] = 0;
    indexes[CollationDataReader.IX_RESERVED3] = 0;

    // Byte offsets of data items all start from the start of the indexes.
    // We add the headerSize at the very end.
    int totalSize = indexesLength * 4;

    if(hasMappings && (isBase || data.jamoCE32s != baseData.jamoCE32s)) {
        indexes[CollationDataReader.IX_JAMO_CE32S_START] = data.jamoCE32s - data.ce32s;
    } else {
        indexes[CollationDataReader.IX_JAMO_CE32S_START] = -1;
    }

    indexes[CollationDataReader.IX_REORDER_CODES_OFFSET] = totalSize;
    totalSize += settings.reorderCodesLength * 4;

    indexes[CollationDataReader.IX_REORDER_TABLE_OFFSET] = totalSize;
    if(settings.reorderTable != null) {
        totalSize += 256;
    }

    indexes[CollationDataReader.IX_TRIE_OFFSET] = totalSize;
    if(hasMappings) {
        UErrorCode errorCode2 = U_ZERO_ERROR;
        int length;
        if(totalSize < capacity) {
            length = utrie2_serialize(data.trie, dest + totalSize,
                                      capacity - totalSize, &errorCode2);
        } else {
            length = utrie2_serialize(data.trie, null, 0, &errorCode2);
        }
        if(U_FAILURE(errorCode2) && errorCode2 != U_BUFFER_OVERFLOW_ERROR) {
            errorCode = errorCode2;
            return 0;
        }
        // The trie size should be a multiple of 8 bytes due to the way
        // compactIndex2(UNewTrie2 *trie) currently works.
        assert((length & 7) == 0);
        totalSize += length;
    }

    indexes[CollationDataReader.IX_RESERVED8_OFFSET] = totalSize;
    indexes[CollationDataReader.IX_CES_OFFSET] = totalSize;
    if(hasMappings && data.cesLength != 0) {
        assert(((headerSize + totalSize) & 7) == 0);
        totalSize += data.cesLength * 8;
    }

    indexes[CollationDataReader.IX_RESERVED10_OFFSET] = totalSize;
    indexes[CollationDataReader.IX_CE32S_OFFSET] = totalSize;
    if(hasMappings) {
        totalSize += data.ce32sLength * 4;
    }

    indexes[CollationDataReader.IX_ROOT_ELEMENTS_OFFSET] = totalSize;
    totalSize += rootElementsLength * 4;

    indexes[CollationDataReader.IX_CONTEXTS_OFFSET] = totalSize;
    if(hasMappings) {
        totalSize += data.contextsLength * 2;
    }

    indexes[CollationDataReader.IX_UNSAFE_BWD_OFFSET] = totalSize;
    if(hasMappings && !unsafeBackwardSet.isEmpty()) {
        UErrorCode errorCode2 = U_ZERO_ERROR;
        int length;
        if(totalSize < capacity) {
            char *p = reinterpret_cast<char *>(dest + totalSize);
            length = unsafeBackwardSet.serialize(
                    p, (capacity - totalSize) / 22);
        } else {
            length = unsafeBackwardSet.serialize(null, 02);
        }
        if(U_FAILURE(errorCode2) && errorCode2 != U_BUFFER_OVERFLOW_ERROR) {
            errorCode = errorCode2;
            return 0;
        }
        totalSize += length * 2;
    }

    indexes[CollationDataReader.IX_FAST_LATIN_TABLE_OFFSET] = totalSize;
    totalSize += fastLatinTableLength * 2;

    indexes[CollationDataReader.IX_SCRIPTS_OFFSET] = totalSize;
    if(isBase) {
        totalSize += data.scriptsLength * 2;
    }

    indexes[CollationDataReader.IX_COMPRESSIBLE_BYTES_OFFSET] = totalSize;
    if(isBase) {
        totalSize += 256;
    }

    indexes[CollationDataReader.IX_RESERVED18_OFFSET] = totalSize;
    indexes[CollationDataReader.IX_TOTAL_SIZE] = totalSize;

    if(totalSize > capacity) {
        errorCode = U_BUFFER_OVERFLOW_ERROR;
        return headerSize + totalSize;
    }

    uprv_memcpy(dest, indexes, indexesLength * 4);
    copyData(indexes, CollationDataReader.IX_REORDER_CODES_OFFSET, settings.reorderCodes, dest);
    copyData(indexes, CollationDataReader.IX_REORDER_TABLE_OFFSET, settings.reorderTable, dest);
    // The trie has already been serialized into the dest buffer.
    copyData(indexes, CollationDataReader.IX_CES_OFFSET, data.ces, dest);
    copyData(indexes, CollationDataReader.IX_CE32S_OFFSET, data.ce32s, dest);
    copyData(indexes, CollationDataReader.IX_ROOT_ELEMENTS_OFFSET, rootElements, dest);
    copyData(indexes, CollationDataReader.IX_CONTEXTS_OFFSET, data.contexts, dest);
    // The unsafeBackwardSet has already been serialized into the dest buffer.
    copyData(indexes, CollationDataReader.IX_FAST_LATIN_TABLE_OFFSET, data.fastLatinTable, dest);
    copyData(indexes, CollationDataReader.IX_SCRIPTS_OFFSET, data.scripts, dest);
    copyData(indexes, CollationDataReader.IX_COMPRESSIBLE_BYTES_OFFSET, data.compressibleBytes, dest);

    return headerSize + totalSize;
}

void
CollationDataWriter.copyData(const int indexes[], int startIndex,
                              const void *src, uint8_t *dest) {
    int start = indexes[startIndex];
    int limit = indexes[startIndex + 1];
    if(start < limit) {
        uprv_memcpy(dest + start, src, limit - start);
    }
}

U_NAMESPACE_END

#endif  // !UCONFIG_NO_COLLATION
