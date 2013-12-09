/*
 *******************************************************************************
 * Copyright (C) 2013, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutputStream;

/**
 * Represents a Bundle collection as a bundle.
 */
public final class BundleCollectionBundle extends Bundle {

    private final BundleCollection coll;
    
    public BundleCollectionBundle(int id, BundleCollection coll) {
        super(id);
        this.coll = coll;
    }

    @Override
    public void read(ObjectInput in, int size) throws IOException {
        coll.read(in);
    }

    @Override
    public byte[] write() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        coll.write(oos);
        oos.close();
        return bos.toByteArray();
    }
    
    public BundleCollection getCollection() {
        return coll; 
    }
}
