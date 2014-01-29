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

import java.io.InputStream;

import com.ibm.icu.impl.ICUData;
import com.ibm.icu.impl.ICUResourceBundle;

/**
 * Collation root provider.
 */
final class CollationRoot {
    private static final CollationTailoring rootSingleton;

    static final CollationTailoring getRoot() {
        return rootSingleton;
    }
    static final CollationData getData() {
        CollationTailoring root = getRoot();
        return root.data;
    }
    static final CollationSettings getSettings() {
        CollationTailoring root = getRoot();
        return root.settings;
    }

    static {  // Corresponds to C++ load() function.
        CollationTailoring t = new CollationTailoring(null);
        InputStream inBytes = ICUData.getRequiredStream(
                ICUResourceBundle.ICU_BUNDLE + "/coll/ucadata.icu");
        CollationDataReader.read(null, inBytes, t);
        rootSingleton = t;
    }
}
