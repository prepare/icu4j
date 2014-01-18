/*
*******************************************************************************
* Copyright (C) 2013, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* collationtailoring.h
*
* @since 2013mar12
* @author Markus W. Scherer
*/

package com.ibm.icu.impl.coll;

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
            assert(baseSettings.reorderCodesLength == 0);
            assert(baseSettings.reorderTable == null);
            settings = baseSettings;
        } else {
            settings = new CollationSettings();
        }
        settings.addRef();
        version[0] = version[1] = version[2] = version[3] = 0;
        maxExpansionsInitOnce.reset();
    }

    @Override
    protected void finalize() {
        super.finalize();
        if(settings != null) {
            settings.removeRef();
            settings = null;
        }
        bundle.close();
    }

    void ensureOwnedData() {
        if(ownedData == null) {
            Normalizer2Impl nfcImpl = Normalizer2Factory.getNFCImpl();
            ownedData = new CollationData(nfcImpl);
        }
        data = ownedData;
    }

    static void makeBaseVersion(const VersionInfo ucaVersion, VersionInfo version) {
        version[0] = UCOL_BUILDER_VERSION;
        version[1] = (ucaVersion[0] << 3) + ucaVersion[1];
        version[2] = ucaVersion[2] << 6;
        version[3] = 0;
    }
    void setVersion(const VersionInfo baseVersion, const VersionInfo rulesVersion) {
        version[0] = UCOL_BUILDER_VERSION;
        version[1] = baseVersion[1];
        version[2] = (baseVersion[2] & 0xc0) + ((rulesVersion[0] + (rulesVersion[0] >> 6)) & 0x3f);
        version[3] = (rulesVersion[1] << 3) + (rulesVersion[1] >> 5) + rulesVersion[2] +
                (rulesVersion[3] << 4) + (rulesVersion[3] >> 4);
    }
    int getUCAVersion() {
        return ((int32_t)version[1] << 4) | (version[2] >> 6);
    }

    // data for sorting etc.
    CollationData data;  // == base data or ownedData
    CollationSettings settings;  // reference-counted
    String rules;
    // The locale is bogus when built from rules or constructed from a binary blob.
    // It can then be set by the service registration code which is thread-safe.
    ULocale actualLocale = ULocale.ROOT;
    // UCA version u.v.w & rules version r.s.t.q:
    // version[0]: builder version (runtime version is mixed in at runtime)
    // version[1]: bits 7..3=u, bits 2..0=v
    // version[2]: bits 7..6=w, bits 5..0=r
    // version[3]= (s<<5)+(s>>3)+t+(q<<4)+(q>>4)
    VersionInfo version;

    // owned objects
    CollationData ownedData;
    Object builder;
    UDataMemory memory;
    UResourceBundle bundle;
    Trie2_32 trie;
    UnicodeSet unsafeBackwardSet;
    UHashtable maxExpansions;
    UInitOnce maxExpansionsInitOnce;

    /*
     * Not Cloneable: A CollationTailoring cannot be copied.
     * It is immutable, and the data trie cannot be copied either.
     */
}
