/*
 *******************************************************************************
 * Copyright (C) 2013, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.io.IOException;
import java.io.ObjectInput;

/**
 * Represents a Serialization bundle
 */
public abstract class Bundle {
    private final int id;
    
    public Bundle(int id) {
        this.id = id;
    }
    
    /**
     * Identifies the type of bundle within a collection.
     */
    public int getId() {
        return id;
    }
    
    /**
     * Reads the payload of the bundle.
     * @param in the input stream.
     * @param size the total number of bytes to be read.
     */
    public abstract void read(ObjectInput in, int size) throws IOException;
    
    /**
     * Converts the payload to wired format. The size of the payload in bytes is the
     * length of the returned array.
     */
    public abstract byte[] write() throws IOException;   

    /**
     * Creates a raw bundle. Raw bundles represents bundles that aren't understood by current
     * versions but may be understood by future versions. They are good for passing uninterpreted
     * data along in its raw format. Raw bundles must be read before they are written.
     * 
     * @param id the bundle Id.
     * @return the new Bundle.
     */
    public static Bundle getRawBundle(int id) {
        return new RawBundle(id);
    }

    private static class RawBundle extends Bundle {
        private byte[] rawData;
        
        public RawBundle(int id) {
            super(id);
        }
        
        @Override
        public void read(ObjectInput in, int size) throws IOException {
            rawData = new byte[size];
            in.readFully(rawData);
        }
    
        @Override
        public byte[] write() throws IOException {
            if (rawData == null) {
                throw new IllegalStateException("Raw bundle must be read first.");
            }
            return rawData;
        }
    }
}






