/*
 *******************************************************************************
 * Copyright (C) 2013, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * BundleCollection objects are used for serialization and deserialization
 * in a way that is both backward and forward compatible.
 * 
 * A BundleCollection maps an id ranging from 1 to 255 to one or more
 * values. Different types of values can be stored at each Id, but the
 * type of value as well as whether or not there can be more than one value
 * stored is fixed for any particular Id never to change. This is how
 * forward and backward compatibility is achieved.
 * 
 * This class is designed to be subclassed. Each subclass defines the
 * fixed mapping between id and object type and quantity by overriding the 
 * {@link #getPayloadSpec} method. If data for an id is stored on the
 * wire that a subclass of BundleCollection does not know about, that data is
 * skipped during serialization.
 */
public class BundleCollection {
    
    private final Map<Integer,Bundle> bundleMap = new HashMap<Integer, Bundle>();

    /**
     * Get the single value for an id or null if there is none.
     * @param id between 1 and 255.
     * @return the value
     * @throws IllegalArgumentException if id does not store a single
     *  value.
     */
    public Object getById(int id) {
        checkId(id);
        PayloadSpec spec = getPayloadSpec(id);
        if (getPayloadClass(spec) == null) {
            throwIllegalArgumentException(id);
        }
        SingleBundle<?> bundle = (SingleBundle<?>) bundleMap.get(id);
        if (bundle == null) {
            return null;
        }
        return bundle.getPayload();
    }
    
    /**
     * Get multiple values for an id.
     * @param id between 1 and 255
     * @param coll the values stored here.
     * @param clazz the class that the collection stores.
     * @throws IllegalArgumentException if id does not store multiple
     *  values.
     * @throws ClassCastException if clazz is not assignable from the
     *  class of values this id stores.
     */
    public <T> void getCollectionById(
            int id, Collection<? super T> coll, Class<T> clazz) {
        checkId(id);
        PayloadSpec spec = getPayloadSpec(id);
        Class<?> payloadClass = getMultiPayloadClass(spec);
        if (payloadClass == null) {
            throwIllegalArgumentException(id);
        }
        if (!clazz.isAssignableFrom(payloadClass)) {
            throwClassCastException(id, payloadClass, clazz);
        }
        coll.clear();
        ListBundle<?> bundle = (ListBundle<?>) bundleMap.get(id);
        if (bundle == null) {
            return;
        }
        bundle.appendTo(coll, clazz);
    }
    
    /**
     * Sets a single value for an id
     * @param id between 1 and 255.
     * @param payload the value to store. If null, any value currently
     *  stored for id is removed.
     * @throws IllegalArgumentException if id does not store a single
     *   value.
     * @throws ClassCastException if payload is of the wrong class.
     */
    public void setById(int id, Object payload) {
        checkId(id);
        PayloadSpec spec = getPayloadSpec(id);
        Class<?> payloadClass = getPayloadClass(spec);
        if (payloadClass == null) {
            throwIllegalArgumentException(id);
        }
        if (payload == null) {
            remove(id);
            return;
        }
        if (!payloadClass.isAssignableFrom(payload.getClass())) {
            throwClassCastException(id, payloadClass, payload.getClass());
        }
        Integer key = id;
        SingleBundle<?> bundle = (SingleBundle<?>) bundleMap.get(key);
        if (bundle == null) {
            bundle = (SingleBundle<?>) createBundle(id, spec);
            bundleMap.put(key, bundle);
        }
        bundle.setPayload(payload);
    }
    
    /**
     * Sets multiple values for an id.
     * @param id between 1 and 255.
     * @param coll the values to store.
     * @param clazz the class of the values to store.
     * @throws IllegalArgumentException if id does not store multiple
     *   values.
     * @throws ClassCastException if clazz is the wrong class for id.
     */
    public <T> void setCollectionById(
            int id, Collection<? extends T> coll, Class<T> clazz) {
        checkId(id);
        PayloadSpec spec = getPayloadSpec(id);
        Class<?> payloadClass = getMultiPayloadClass(spec);
        if (payloadClass == null) {
            throwIllegalArgumentException(id);
        }
        if (!payloadClass.isAssignableFrom(clazz)) {
            throwClassCastException(id, payloadClass, clazz);
        }
        if (coll.isEmpty()) {
            remove(id);
            return;
        }
        Integer key = id;
        ListBundle<?> bundle = (ListBundle<?>) bundleMap.get(key);
        if (bundle == null) {
            bundle = (ListBundle<?>) createBundle(id, spec);
            bundleMap.put(key, bundle);
        }
        bundle.readFrom(coll);
    }
   
    /**
     * Clear all values
     */
    public void clear() {
        bundleMap.clear();
    }
   
    /**
     * Remove value(s) for a particular id.
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
            PayloadSpec spec = getPayloadSpec(tagId);
            Bundle bundle = createBundle(tagId, spec);
           
            // Read the size of the data in the bundle.
            int size = in.readInt() & 0x7FFFFFFF;
            bundle.read(in, size);
            // TODO: Store these raw bundles once they can serialize themselves.
            if (spec != null) {
                bundleMap.put(bundle.getId(), bundle);
            }
           
            // Read tagId of next bundle
            tagId = in.readByte() & 0xFF;
        }
    }
   
    /**
     * Writes this object to the stream.
     */
    public void write(ObjectOutput out) throws IOException {
        for (Bundle bundle : bundleMap.values()) {
            out.writeByte(bundle.getId());
            byte[] b = bundle.write();
            out.writeInt(b.length);
            out.write(b);
        }
        out.writeByte(0);
    }
    
    /**
     * Indicates the type and quantity of values to store.
     */
    public static class PayloadSpec {
        
        private final Class<?> clazz;
        private final boolean bList;
        
        private PayloadSpec(Class<?> clazz, boolean bList) {
            this.clazz = clazz;
            this.bList = bList;
        }
        
        /**
         * Indicates one String.
         */
        public static PayloadSpec forString() {
            return new PayloadSpec(String.class, false);
        }
        
        /**
         * Indicates multiple Strings.
         */
        public static PayloadSpec forStringList() {
            return new PayloadSpec(String.class, true);
        }
        
        /**
         * Indicates one BundleCollection.
         */
        public static PayloadSpec forBundleCollection(
                Class<? extends BundleCollection> clazz) {
            return new PayloadSpec(clazz, false);
        }
        
        /**
         * Indicates one Externalizable.
         */
        public static PayloadSpec forExternalizable(
                Class<? extends Externalizable> clazz) {
            return new PayloadSpec(clazz, false);
        }
        
        /**
         * Indicates multiple Externalizables.
         */
        public static PayloadSpec forExternalizableList(
                Class<? extends Externalizable> clazz) {
            return new PayloadSpec(clazz, true);
        }
        
        Class<?> getPayloadClass() {
            return clazz;
        }
        
        boolean isList() {
            return bList;
        }
        
        Bundle createBundle(int id) {
            if (!bList) {
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
            } else {
                if (clazz.equals(String.class)) {
                    return new StringListBundle(id);
                } else if (Externalizable.class.isAssignableFrom(clazz)) {
                    return new ExternalizableListBundle(
                            id, clazz.asSubclass(Externalizable.class));
                } else {
                    throw new IllegalStateException();
                }
            }
        }
    }
    
    /**
     * Indicates the type and quantity of values to store at each Id.
     * This is what subclasses need to override. Subclasses should return
     * null for any Ids not yet used.
     */
    protected PayloadSpec getPayloadSpec(int id) {
        return null;       
    }
    
    private Bundle createBundle(int id, PayloadSpec spec) {
        if (spec == null) {
            return new RawBundle(id);
        }
        return spec.createBundle(id);
    }
    
    
    private abstract static class Bundle {
        private final int id;
       
        public Bundle(int id) {
            this.id = id;
        }
       
        public int getId() {
            return id;
        }
       
        public abstract void read(ObjectInput in, int size) throws IOException;
       
        public abstract byte[] write() throws IOException;
    }
   
    private abstract static class SingleBundle<T> extends Bundle {
        private Class<? extends T> payloadClass;
        T payload;
       
        public SingleBundle(int id, Class<? extends T> clazz) {
            super(id);
            this.payloadClass = clazz;
        }
        
        public T getPayload() {
            return payload;
        }
        
        public void setPayload(Object payload) {
            this.payload = payloadClass.cast(payload);
        }

        protected T newInstance() {
            return newInstanceFromClass(payloadClass);
        }

    }
    
    private abstract static class ListBundle<T> extends Bundle {
        private Class<? extends T> payloadClass;
        ArrayList<T> payload;
       
        public ListBundle(int id, Class<? extends T> clazz) {
            super(id);
            this.payloadClass = clazz;
        }
        
        public <U> void appendTo(
                Collection<? super U> appendTo, Class<U> clazz) {
            for (T element : payload) {
                appendTo.add(clazz.cast(element));
            }
        }
        
        public <U> void readFrom(Collection<?> coll) {
            payload = new ArrayList<T>(coll.size());
            for (Object element : coll) {
                payload.add(payloadClass.cast(element));
            }
        }
                
        protected T newInstance() {
            return newInstanceFromClass(payloadClass);
        }
        
        protected ArrayList<T> newList(int desiredSize) {
            // Prevent DoS attacks.
            return new ArrayList<T>(Math.min(desiredSize, 1000));
        }
    }
   
    private static class RawBundle extends Bundle {
       
        public RawBundle(int id) {
            super(id);
        }
       
        @Override
        public void read(ObjectInput in, int size) throws IOException {
            // We do it this way to prevent DoS attacks.
            byte[] buffer = new byte[8192];
            int bytesLeft = size;
            while (bytesLeft > 0) {
                int numRead = in.read(buffer, 0, Math.min(buffer.length, bytesLeft));
                if (numRead == -1) {
                    throw new EOFException();
                }
                bytesLeft -= numRead;
            }
        }
   
        @Override
        public byte[] write() throws IOException {
            // TODO: Fix to support this.
            throw new UnsupportedOperationException();
        }
    }
   
    private static class BundleCollectionBundle extends SingleBundle<BundleCollection> {
       
        public BundleCollectionBundle(
                int id, Class<? extends BundleCollection> clazz) {
            super(id, clazz);
        }

        @Override
        public void read(ObjectInput in, int size) throws IOException {
            payload = newInstance();
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
    
    private static class ExternalizableBundle extends SingleBundle<Externalizable> {
        
        public ExternalizableBundle(
                int id, Class<? extends Externalizable> clazz) {
            super(id, clazz);
        }

        @Override
        public void read(ObjectInput in, int size) throws IOException {
            payload = newInstance();
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
    
    private static class ExternalizableListBundle extends ListBundle<Externalizable> {
        
        public ExternalizableListBundle(
                int id, Class<? extends Externalizable> clazz) {
            super(id, clazz);
        }

        @Override
        public void read(ObjectInput in, int size) throws IOException {
            int objCount = in.readInt();
            payload = newList(objCount);
            for (int i = 0; i < objCount; i++) {
                Externalizable externalizable = newInstance();
                try {
                    externalizable.readExternal(in);
                } catch (ClassNotFoundException e) {
                    throw new IOException(e);
                }
                payload.add(externalizable);
            }
        }

        @Override
        public byte[] write() throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            int objCount = payload.size();
            oos.writeInt(objCount);
            for (int i = 0; i < objCount; i++) {
                payload.get(i).writeExternal(oos);
            }
            oos.close();
            return bos.toByteArray();
        }
    }
   
    private static class StringBundle extends SingleBundle<String> {
       
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
    
    private static class StringListBundle extends ListBundle<String> {
        
        public StringListBundle(int id) {
            super(id, String.class);
        }

        @Override
        public void read(ObjectInput in, int size) throws IOException {
            int stringCount = in.readInt();
            payload = newList(stringCount);
            for (int i = 0; i < stringCount; i++) {
                payload.add(in.readUTF());
            }
        }

        @Override
        public byte[] write() throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            int stringCount = payload.size();
            oos.writeInt(stringCount);
            for (int i = 0; i < stringCount; i++) {
                oos.writeUTF(payload.get(i));
            }
            oos.close();
            return bos.toByteArray();
        }
    }
    
    private static Class<?> getMultiPayloadClass(PayloadSpec spec) {
        if (spec == null || !spec.isList()) {
            return null;
        }
        return spec.getPayloadClass();
    }
    
    private static Class<?> getPayloadClass(PayloadSpec spec) {
        if (spec == null || spec.isList()) {
            return null;
        }
        return spec.getPayloadClass();
    }
    
    private static void throwIllegalArgumentException(int id) {
        throw new IllegalArgumentException("Unsupported Id: " + id);
    }
    
    private static void throwClassCastException(
            int id, Class<?> need, Class<?> got) {
        throw new ClassCastException(
                "Id: " + id + " Need: " + need + " Got: " + got);
    }
    
    private static <T> T newInstanceFromClass(Class<? extends T> clazz) {
        try {
            return clazz.newInstance();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void checkId(int id) {
        if (id < 1 || id > 255) {
            throw new IllegalArgumentException("Out of range: " + id);
        }
    }
}
