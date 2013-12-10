/*
 *******************************************************************************
 * Copyright (C) 2013, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * Represents a collection of Bundles.
 */
public class BundleCollection {
    
    private final Map<Integer,Bundle<?>> bundleMap = new HashMap<Integer, Bundle<?>>();
   
    /**
     * Get the payload of bundle with given id or null if id does not
     * exist.
     */
    public Object getById(int id) {
        Bundle<?> bundle = bundleMap.get(id);
        if (bundle == null) {
            return null;
        }
        return bundle.getPayload();
    }
    
    /**
     * Set payload of bundle with given id. A payload of null means
     * to remove bundle with given id.
     * @throws ClassCastException if payload does not
     *  have expected type.
     * @throws IllegalArgumentException if payload is non-null and
     *  getPayloadSpec() returns null. 
     */
    public void setById(int id, Object payload) {
        if (payload == null) {
            remove(id);
            return;
        }
        Integer key = id;
        Bundle<?> bundle = bundleMap.get(key);
        if (bundle == null) {
            PayloadSpec spec = getPayloadSpec(id);
            if (spec == null) {
                throwUnsupportedId(id);
            }
            bundle = createBundle(id, spec);
            bundleMap.put(key, bundle);
        }
        bundle.setPayload(payload);
    }
   
    /**
     * Clear all bundles from this object.
     */
    public void clear() {
        bundleMap.clear();
    }
   
    /**
     * Removes bundle with a particular key.
     */
    public void remove(int id) {
        bundleMap.remove(id);
    }
   
    /**
     * Replaces contents of this object with what is read from stream.
     */
    public void read(ObjectInput in) throws IOException {
        clear();
        int tagId = in.readByte() & 0xFF;
        while (tagId != 0) {
            Bundle<?> bundle = createBundle(tagId);
           
            // Read the size of the data in the bundle.
            int size = in.readShort() & 0xFFFF;
            bundle.read(in, size);
           
            bundleMap.put(bundle.getId(), bundle);
           
            // Read tagId of next bundle
            tagId = in.readByte() & 0xFF;
        }
    }
   
    /**
     * Writes this object to the stream.
     */
    public void write(ObjectOutput out) throws IOException {
        for (Bundle<?> bundle : bundleMap.values()) {
            out.writeByte(bundle.getId());
            byte[] b = bundle.write();
            out.writeShort(b.length);
            out.write(b);
        }
        out.writeByte(0);
    }
    
    /**
     * Represents the specification for a Bundle payload.
     */
    public static class PayloadSpec {
        
        private final Class<?> clazz;
        
        private PayloadSpec(Class<?> clazz) {
            this.clazz = clazz;
        }
        
        public Class<?> getPayloadClass() {
            return clazz;
        }
        
        public static PayloadSpec forString() {
            return new PayloadSpec(String.class);
        }
        
        public static PayloadSpec forBundleCollection(
                Class<? extends BundleCollection> clazz) {
            return new PayloadSpec(clazz);
        }
        
        public static PayloadSpec forExternalizable(
                Class<? extends Externalizable> clazz) {
            return new PayloadSpec(clazz);
        }
        
        Bundle<?> createBundle(int id) {
            if (clazz.equals(String.class)) {
                return new StringBundle(id);
            } else if (BundleCollection.class.isAssignableFrom(clazz)) {
                return new BundleCollectionBundle(
                        id, clazz.asSubclass(BundleCollection.class));
            } else if (Externalizable.class.isAssignableFrom(clazz)) {
                return new ExternalizableBundle(
                        id, clazz.asSubclass(Externalizable.class));
            } else {
                throw new IllegalStateException();
            }
        }
    }
    
    /**
     * Returns the payload specification for bundle with given id.
     * Returns null if id is unknown.
     */
    protected PayloadSpec getPayloadSpec(int id) {
        return null;       
    }
    
    private Bundle<?> createBundle(int id) {
        return createBundle(id, getPayloadSpec(id));
    }
    
    private Bundle<?> createBundle(int id, PayloadSpec spec) {
        if (spec == null) {
            return new RawBundle(id);
        }
        return spec.createBundle(id);
    }
   
   
    private abstract static class Bundle<T> {
        private final int id;
        private Class<? extends T> payloadClass;
        T payload;
       
        public Bundle(int id, Class<? extends T> clazz) {
            this.id = id;
            this.payloadClass = clazz;
        }
       
        public int getId() {
            return id;
        }
        
        public T getPayload() {
            return payload;
        }
        
        public void setPayload(Object payload) {
            if (payloadClass == null) {
                throwUnsupportedId(id);
            }
            this.payload = payloadClass.cast(payload);
        }
       
        public abstract void read(ObjectInput in, int size) throws IOException;
       
        public abstract byte[] write() throws IOException;

        /**
         * Only good for mutable payloads with default constructors.
         */
        protected void resetPayload() {
            try {
                payload = payloadClass.newInstance();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
    }
   
    private static class RawBundle extends Bundle<byte[]> {
       
        public RawBundle(int id) {
            super(id, null);
        }
       
        @Override
        public void read(ObjectInput in, int size) throws IOException {
            payload = new byte[size];
            in.readFully(payload);
        }
   
        @Override
        public byte[] write() throws IOException {
            return payload;
        }
    }
   
    private static class BundleCollectionBundle extends Bundle<BundleCollection> {
       
        public BundleCollectionBundle(
                int id, Class<? extends BundleCollection> clazz) {
            super(id, clazz);
        }

        @Override
        public void read(ObjectInput in, int size) throws IOException {
            resetPayload();
            payload.read(in);
        }

        @Override
        public byte[] write() throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            payload.write(oos);
            oos.close();
            return bos.toByteArray();
        }
    }
    
    private static class ExternalizableBundle extends Bundle<Externalizable> {
        
        public ExternalizableBundle(
                int id, Class<? extends Externalizable> clazz) {
            super(id, clazz);
        }

        @Override
        public void read(ObjectInput in, int size) throws IOException {
            resetPayload();
            try {
                payload.readExternal(in);
            } catch (ClassNotFoundException e) {
                throw new IOException(e);
            }
        }

        @Override
        public byte[] write() throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            payload.writeExternal(oos);
            oos.close();
            return bos.toByteArray();
        }
    }
   
    private static class StringBundle extends Bundle<String> {
       
        public StringBundle(int id) {
            super(id, String.class);
        }

        @Override
        public void read(ObjectInput in, int size) throws IOException {
            payload = in.readUTF();
        }

        @Override
        public byte[] write() throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeUTF(payload);
            oos.close();
            return bos.toByteArray();
        }
    }
    
    private static void throwUnsupportedId(int id) {
        throw new IllegalArgumentException("Unsupported Id: " + id);
    }

}
