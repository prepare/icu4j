/*
*******************************************************************************
* Copyright (C) 2012-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* CollationRoot.java, ported from collationroot.h/.cpp
*
* @since 2012dec17
* @author Markus W. Scherer
*/

package com.ibm.icu.impl.coll;

/**
 * Collation root provider.
 */
final class CollationRoot {
    static final CollationTailoring getRoot();
    static final CollationData getData();
    static final CollationSettings getSettings();

    private static void load();
}

    private static final CollationTailoring *rootSingleton = null;
    static UInitOnce initOnce = U_INITONCE_INITIALIZER;

    static boolean U_CALLCONV uprv_collation_root_cleanup() {
        rootSingleton.removeRef();
        rootSingleton = null;
        initOnce.reset();
        return true;
    }

    void
    CollationRoot.load() {
        if(U_FAILURE) { return; }
        LocalPointer<CollationTailoring> t(new CollationTailoring(null));
        if(t.isNull() || t.isBogus()) {
            errorCode = U_MEMORY_ALLOCATION_ERROR;
            return;
        }
        t.memory = udata_openChoice(U_ICUDATA_NAME U_TREE_SEPARATOR_STRING "coll",
                                    "icu", "ucadata",
                                    CollationDataReader.isAcceptable, t.version, &errorCode);
        if(U_FAILURE) { return; }
        const uint8_t *inBytes = static_cast<const uint8_t *>(udata_getMemory(t.memory));
        CollationDataReader.read(null, inBytes, udata_getLength(t.memory), *t);
        if(U_FAILURE) { return; }
        ucln_i18n_registerCleanup(UCLN_I18N_COLLATION_ROOT, uprv_collation_root_cleanup);
        t.addRef();  // The rootSingleton takes ownership.
        rootSingleton = t.orphan();
    }

    const CollationTailoring *
    CollationRoot.getRoot() {
        umtx_initOnce(initOnce, CollationRoot.load);
        if(U_FAILURE) { return null; }
        return rootSingleton;
    }

    CollationData 
    CollationRoot.getData() {
        const CollationTailoring *root = getRoot;
        if(U_FAILURE) { return null; }
        return root.data;
    }

    CollationSettings
    CollationRoot.getSettings() {
        const CollationTailoring *root = getRoot;
        if(U_FAILURE) { return null; }
        return root.settings;
    }
