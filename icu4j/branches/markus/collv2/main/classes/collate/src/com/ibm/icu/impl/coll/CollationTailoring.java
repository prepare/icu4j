/*
*******************************************************************************
* Copyright (C) 2013-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* CollationTailoring.java, ported from collationtailoring.h/.cpp
*
* @since 2013mar12
* @author Markus W. Scherer
*/

package com.ibm.icu.impl.coll;

import java.util.Map;

import com.ibm.icu.impl.Norm2AllModes;
import com.ibm.icu.impl.Normalizer2Impl;
import com.ibm.icu.impl.Trie2_32;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.VersionInfo;

/**
 * Collation tailoring data & settings.
 * This is a container of values for a collation tailoring
 * built from rules or deserialized from binary data.
 *
 * It is logically immutable: Do not modify its values.
 * The fields are public for convenience.
 */
final class CollationTailoring {
    CollationTailoring(CollationSettings baseSettings) {
        if(baseSettings != null) {
            assert(baseSettings.reorderCodes.length == 0);
            assert(baseSettings.reorderTable == null);
            settings = new SharedObject.Reference<CollationSettings>(baseSettings);
        } else {
            settings = new SharedObject.Reference<CollationSettings>(new CollationSettings());
        }
    }

    void ensureOwnedData() {
        if(ownedData == null) {
            Normalizer2Impl nfcImpl = Norm2AllModes.getNFCInstance().impl;
            ownedData = new CollationData(nfcImpl);
        }
        data = ownedData;
    }

    static VersionInfo makeBaseVersion(VersionInfo ucaVersion) {
        return VersionInfo.getInstance(
                VersionInfo.UCOL_BUILDER_VERSION.getMajor(),
                (ucaVersion.getMajor() << 3) + ucaVersion.getMinor(),
                ucaVersion.getMilli() << 6,
                0);
    }
    void setVersion(VersionInfo baseVersion, VersionInfo rulesVersion) {
        version = VersionInfo.getInstance(
                VersionInfo.UCOL_BUILDER_VERSION.getMajor(),
                baseVersion.getMinor(),
                (baseVersion.getMilli() & 0xc0) + ((rulesVersion.getMajor() + (rulesVersion.getMajor() >> 6)) & 0x3f),
                (rulesVersion.getMinor() << 3) + (rulesVersion.getMinor() >> 5) + rulesVersion.getMilli() +
                        (rulesVersion.getMicro() << 4) + (rulesVersion.getMicro() >> 4));
    }
    int getUCAVersion() {
        return (version.getMinor() << 4) | (version.getMilli() >> 6);
    }

    // data for sorting etc.
    CollationData data;  // == base data or ownedData
    SharedObject.Reference<CollationSettings> settings;  // reference-counted
    String rules;
    // The locale is null (C++: bogus) when built from rules or constructed from a binary blob.
    // It can then be set by the service registration code which is thread-safe.
    ULocale actualLocale;
    // UCA version u.v.w & rules version r.s.t.q:
    // version[0]: builder version (runtime version is mixed in at runtime)
    // version[1]: bits 7..3=u, bits 2..0=v
    // version[2]: bits 7..6=w, bits 5..0=r
    // version[3]= (s<<5)+(s>>3)+t+(q<<4)+(q>>4)
    VersionInfo version = ZERO_VERSION;
    private static final VersionInfo ZERO_VERSION = VersionInfo.getInstance(0, 0, 0, 0);

    // owned objects
    CollationData ownedData;
    Trie2_32 trie;
    UnicodeSet unsafeBackwardSet;
    Map<Integer, Integer> maxExpansions;

    /*
     * Not Cloneable: A CollationTailoring cannot be copied.
     * It is immutable, and the data trie cannot be copied either.
     */
}
