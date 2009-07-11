/*
 *******************************************************************************
 * Copyright (C) 2004-2009, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.nio.ByteBuffer;
import java.util.HashMap;

import com.ibm.icu.util.UResourceBundle;
import com.ibm.icu.util.UResourceBundleIterator;
import com.ibm.icu.util.UResourceTypeMismatchException;

class ICUResourceBundleImpl extends ICUResourceBundle {
    /**
     * Access to the bits and bytes of the resource bundle.
     * Hides low-level details.
     */
    protected ICUResourceBundleReader reader;
    /** Data member where the subclasses store the offset within resource data. */
    protected int resource;

    protected ICUResourceBundleImpl(ICUResourceBundleReader reader, String key, String resPath, int resource) {
        super(key, resPath);
        this.reader = reader;
        this.resource = resource;
    }
    protected boolean getNoFallback() {
        return reader.getNoFallback();
    }
    public boolean getUsesPoolBundle() {
        return reader.getUsesPoolBundle();
    }
    public void setThisAsPoolBundleFor(ICUResourceBundleImpl target) {
        target.reader.setPoolBundleKeys(reader);
    }
    protected final ICUResourceBundle createBundleObject(String _key,
                                                         int _resource,
                                                         HashMap<String, String> table,
                                                         UResourceBundle requested,
                                                         ICUResourceBundle bundle,
                                                         boolean[] isAlias) {
        if (isAlias != null) {
            isAlias[0] = false;
        }
        String _resPath = resPath + "/" + _key;
        switch(ICUResourceBundleReader.RES_GET_TYPE(_resource)) {
        case STRING :
        case ICUResourceBundleReader.STRING_V2:
            return new ICUResourceBundleImpl.ResourceString(reader, _key, _resPath, _resource, this);
        case BINARY:
            return new ICUResourceBundleImpl.ResourceBinary(reader, _key, _resPath, _resource, this);
        case ICUResourceBundleReader.ALIAS:
            if (isAlias != null) {
                isAlias[0] = true;
            }
            return findResource(_key, _resource, table, requested);
        case INT:
            return new ICUResourceBundleImpl.ResourceInt(reader, _key, _resPath, _resource, this);
        case INT_VECTOR:
            return new ICUResourceBundleImpl.ResourceIntVector(reader, _key, _resPath, _resource, this);
        case ARRAY:
            return new ICUResourceBundleImpl.ResourceArray(reader, _key, _resPath, _resource, this);
        case ICUResourceBundleReader.ARRAY16:
            return new ICUResourceBundleImpl.ResourceArray16(reader, _key, _resPath, _resource, this);
        case ICUResourceBundleReader.TABLE32:
            return new ICUResourceBundleImpl.ResourceTable32(reader, _key, _resPath, _resource, this);
        case TABLE:
            return new ICUResourceBundleImpl.ResourceTable(reader, _key, _resPath, _resource, this);
        case ICUResourceBundleReader.TABLE16:
            return new ICUResourceBundleImpl.ResourceTable16(reader, _key, _resPath, _resource, this);
        default :
            throw new IllegalStateException("The resource type is unknown");
        }
    }

    static class ResourceArrayBase extends ICUResourceBundleImpl {
        protected int size;
        protected int itemsOffset;

        public int getSize() {
            return size;
        }
        protected String[] handleGetStringArray() {
            String[] strings = new String[size];
            UResourceBundleIterator iter = getIterator();
            int i = 0;
            while (iter.hasNext()) {
                strings[i++] = iter.next().getString();
            }
            return strings;
        }
        public String[] getStringArray() {
            return handleGetStringArray();
        }
        protected UResourceBundle handleGetImpl(String indexStr, HashMap<String, String> table,
                                                UResourceBundle requested,
                                                int[] index, boolean[] isAlias) {
            int i = indexStr.length() > 0 ? Integer.valueOf(indexStr).intValue() : -1;
            if(index != null) {
                index[0] = i;
            }
            if (i >= 0) {
                return handleGetImpl(i, table, requested, isAlias);
            }
            throw new UResourceTypeMismatchException("Could not get the correct value for index: "+ index);
        }
        ResourceArrayBase(ICUResourceBundleReader reader, String key, String resPath, int resource,
                          ICUResourceBundle bundle) {
            super(reader, key, resPath, resource);
            if(bundle!=null){
                assign(this, bundle);
            }
            createLookupCache(); // Use bundle cache to access array entries
        }
    }
    static final class ResourceArray extends ResourceArrayBase {
        protected UResourceBundle handleGetImpl(int index, HashMap<String, String> table,
                                                UResourceBundle requested, boolean[] isAlias) {
            if (index < 0 || size <= index) {
                throw new IndexOutOfBoundsException();
            }
            int item = reader.getInt(itemsOffset + 4 * index);
            return createBundleObject(Integer.toString(index), item, table, requested, this, isAlias);
        }
        ResourceArray(ICUResourceBundleReader reader, String key, String resPath, int resource,
                      ICUResourceBundle bundle) {
            super(reader, key, resPath, resource, bundle);
            if(bundle!=null){
                assign(this, bundle);
            }
            int offset = reader.getResourceByteOffset(resource);
            size = reader.getInt(offset);
            itemsOffset = offset + 4;
        }
    }
    static final class ResourceArray16 extends ResourceArrayBase {
        protected UResourceBundle handleGetImpl(int index, HashMap<String, String> table,
                                                UResourceBundle requested, boolean[] isAlias) {
            if (index < 0 || size <= index) {
                throw new IndexOutOfBoundsException();
            }
            int item = reader.get16BitResource(itemsOffset + index);
            return createBundleObject(Integer.toString(index), item, table, requested, this, isAlias);
        }
        ResourceArray16(ICUResourceBundleReader reader, String key, String resPath, int resource,
                        ICUResourceBundle bundle) {
            super(reader, key, resPath, resource, bundle);
            int offset = ICUResourceBundleReader.RES_GET_OFFSET(resource);
            size = reader.get16BitUnit(offset);
            itemsOffset = offset + 1;
        }
    }
    static final class ResourceBinary extends ICUResourceBundleImpl {
        public ByteBuffer getBinary() {
            return reader.getBinary(resource);
        }
        public byte [] getBinary(byte []ba) {
            return reader.getBinary(resource, ba);
        }
        ResourceBinary(ICUResourceBundleReader reader, String key, String resPath, int resource,
                       ICUResourceBundle bundle) {
            super(reader, key, resPath, resource);
            assign(this, bundle);

        }
    }
    static final class ResourceInt extends ICUResourceBundleImpl {
        public int getInt() {
            return ICUResourceBundleReader.RES_GET_INT(resource);
        }
        public int getUInt() {
            return ICUResourceBundleReader.RES_GET_UINT(resource);
        }
        ResourceInt(ICUResourceBundleReader reader, String key, String resPath, int resource,
                    ICUResourceBundle bundle) {
            super(reader, key, resPath, resource);
            assign(this, bundle);
        }
    }
    static final class ResourceString extends ICUResourceBundleImpl {
        private String value;
        public String getString() {
            return value;
        }
        ResourceString(ICUResourceBundleReader reader, String key, String resPath, int resource,
                       ICUResourceBundle bundle) {
            super(reader, key, resPath, resource);
            assign(this, bundle);
            value = reader.getString(resource);
        }
    }
    static final class ResourceIntVector extends ICUResourceBundleImpl {
        private int[] value;
        public int[] getIntVector() {
            return value;
        }
        ResourceIntVector(ICUResourceBundleReader reader, String key, String resPath, int resource,
                          ICUResourceBundle bundle) {
            super(reader, key, resPath, resource);
            assign(this, bundle);
            value = reader.getIntVector(resource);
        }
    }
    static final class ResourceTable extends ICUResourceBundleImpl {
        private char[] keyOffsets;
        private int itemsOffset;

        public int getSize() {
            return keyOffsets.length;
        }
        protected UResourceBundle handleGetImpl(String resKey, HashMap<String, String> table,
                                                UResourceBundle requested,
                                                int[] index, boolean[] isAlias) {
            int i = reader.findTableItem(keyOffsets, resKey);
            if(index != null) {
                index[0] = i;
            }
            if (i < 0) {
                return null;
            }
            int item = reader.getInt(itemsOffset + 4 * i);
            return createBundleObject(resKey, item, table, requested, this, isAlias);
        }
        protected UResourceBundle handleGetImpl(int index, HashMap<String, String> table,
                                                UResourceBundle requested, boolean[] isAlias) {
            int item = reader.getInt(itemsOffset + 4 * index);
            String itemKey = reader.getKey16String(keyOffsets[index]);
            return createBundleObject(itemKey, item, table, requested, this, isAlias);
        }
        ResourceTable(ICUResourceBundleReader reader, String key, String resPath, int resource,
                      ICUResourceBundle bundle) {
            super(reader, key, resPath, resource);
            if(bundle!=null){
                assign(this, bundle);
            }
            int offset = reader.getResourceByteOffset(resource);
            keyOffsets = reader.getTableKeyOffsets(offset);
            itemsOffset = offset + 2 * ((keyOffsets.length + 2) & ~1);  // Skip padding for 4-alignment.
            createLookupCache(); // Use bundle cache to access nested resources
        }
    }
    static final class ResourceTable16 extends ICUResourceBundleImpl {
        private char[] keyOffsets;
        private int itemsOffset;

        public int getSize() {
            return keyOffsets.length;
        }
        protected UResourceBundle handleGetImpl(String resKey, HashMap<String, String> table,
                                                UResourceBundle requested,
                                                int[] index, boolean[] isAlias) {
            int i = reader.findTableItem(keyOffsets, resKey);
            if(index != null) {
                index[0] = i;
            }
            if (i < 0) {
                return null;
            }
            int item = reader.get16BitResource(itemsOffset + i);
            return createBundleObject(resKey, item, table, requested, this, isAlias);
        }
        protected UResourceBundle handleGetImpl(int index, HashMap<String, String> table,
                                                UResourceBundle requested, boolean[] isAlias) {
            int item = reader.get16BitResource(itemsOffset + index);
            String itemKey = reader.getKey16String(keyOffsets[index]);
            return createBundleObject(itemKey, item, table, requested, this, isAlias);
        }
        ResourceTable16(ICUResourceBundleReader reader, String key, String resPath, int resource,
                        ICUResourceBundle bundle) {
            super(reader, key, resPath, resource);
            if(bundle!=null){
                assign(this, bundle);
            }
            int offset = ICUResourceBundleReader.RES_GET_OFFSET(resource);
            keyOffsets = reader.getTable16KeyOffsets(offset);
            itemsOffset = offset + 1 + keyOffsets.length;
            createLookupCache(); // Use bundle cache to access nested resources
        }
    }
    static final class ResourceTable32 extends ICUResourceBundleImpl {
        private int[] keyOffsets;
        private int itemsOffset;

        public int getSize() {
            return keyOffsets.length;
        }
        protected UResourceBundle handleGetImpl(String resKey, HashMap<String, String> table,
                                                UResourceBundle requested,
                                                int[] index, boolean[] isAlias) {
            int i = reader.findTable32Item(keyOffsets, resKey);
            if(index != null) {
                index[0] = i;
            }
            if (i < 0) {
                return null;
            }
            int item = reader.getInt(itemsOffset + 4 * i);
            return createBundleObject(resKey, item, table, requested, this, isAlias);
        }
        protected UResourceBundle handleGetImpl(int index, HashMap<String, String> table,
                                                UResourceBundle requested, boolean[] isAlias) {
            int item = reader.getInt(itemsOffset + 4 * index);
            String itemKey = reader.getKey32String(keyOffsets[index]);
            return createBundleObject(itemKey, item, table, requested, this, isAlias);
        }
        ResourceTable32(ICUResourceBundleReader reader, String key, String resPath, int resource, ICUResourceBundle bundle) {
            super(reader, key, resPath, resource);
            if(bundle!=null){
                assign(this, bundle);
            }
            int offset = reader.getResourceByteOffset(resource);
            keyOffsets = reader.getTable32KeyOffsets(offset);
            itemsOffset = offset + 4 * (1 + keyOffsets.length);
            createLookupCache(); // Use bundle cache to access nested resources
        }
    }
}
