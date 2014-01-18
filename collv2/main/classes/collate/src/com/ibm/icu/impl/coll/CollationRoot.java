/*
*******************************************************************************
* Copyright (C) 2012-2013, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* collationroot.h
*
* @since 2012dec17
* @author Markus W. Scherer
*/

package com.ibm.icu.impl.coll;

struct CollationData;
struct CollationSettings;
struct CollationTailoring;

/**
 * Collation root provider.
 */
final class CollationRoot {
    static final CollationTailoring getRoot();
    static final CollationData getData();
    static final CollationSettings getSettings();

    private static void load();
};

U_NAMESPACE_END

#endif  // !UCONFIG_NO_COLLATION
#endif  // __COLLATIONROOT_H__
/*
*******************************************************************************
* Copyright (C) 2012-2013, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* collationroot.cpp
*
* @since 2012dec17
* @author Markus W. Scherer
*/

#include "unicode/utypes.h"

#if !UCONFIG_NO_COLLATION

#include "unicode/coll.h"
#include "unicode/udata.h"
#include "collation.h"
#include "collationdata.h"
#include "collationdatareader.h"
#include "collationroot.h"
#include "collationsettings.h"
#include "collationtailoring.h"
#include "normalizer2impl.h"
#include "ucln_in.h"
#include "udatamem.h"
#include "umutex.h"

U_NAMESPACE_BEGIN

namespace {

private static final CollationTailoring *rootSingleton = null;
static UInitOnce initOnce = U_INITONCE_INITIALIZER;

}  // namespace

U_CDECL_BEGIN

static boolean U_CALLCONV uprv_collation_root_cleanup() {
    rootSingleton.removeRef();
    rootSingleton = null;
    initOnce.reset();
    return true;
}

U_CDECL_END

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

U_NAMESPACE_END

#endif  // !UCONFIG_NO_COLLATION
