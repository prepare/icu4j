/*
 *******************************************************************************
 * Copyright (C) 2004-2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import com.ibm.icu.util.UResourceBundle;
import com.ibm.icu.util.UResourceTypeMismatchException;

class ICUResourceBundleImpl extends ICUResourceBundle {
    protected ICUResourceBundleImpl(ICUResourceBundleImpl container, String key) {
        super(container, key);
    }
    ICUResourceBundleImpl(WholeBundle wholeBundle) {
        super(wholeBundle);
    }
    protected final ICUResourceBundle createBundleObject(String _key,
                                                         int _resource,
                                                         HashMap<String, String> aliasesVisited,
                                                         UResourceBundle requested,
                                                         boolean[] isAlias) {
        if (isAlias != null) {
            isAlias[0] = false;
        }
        switch(ICUResourceBundleReader.RES_GET_TYPE(_resource)) {
        case STRING :
        case STRING_V2:
            return new ICUResourceBundleImpl.ResourceString(this, _key, _resource);
        case BINARY:
            return new ICUResourceBundleImpl.ResourceBinary(this, _key, _resource);
        case ALIAS:
            if (isAlias != null) {
                isAlias[0] = true;
            }
            return getAliasedResource(_key, _resource, aliasesVisited, requested);
        case INT:
            return new ICUResourceBundleImpl.ResourceInt(this, _key, _resource);
        case INT_VECTOR:
            return new ICUResourceBundleImpl.ResourceIntVector(this, _key, _resource);
        case ARRAY:
        case ARRAY16:
            return new ICUResourceBundleImpl.ResourceArray(this, _key, _resource);
        case TABLE:
        case TABLE16:
        case TABLE32:
            return new ICUResourceBundleImpl.ResourceTable(this, _key, _resource);
        default :
            throw new IllegalStateException("The resource type is unknown");
        }
    }

    // Scalar values ------------------------------------------------------- ***

    private static final class ResourceBinary extends ICUResourceBundleImpl {
        private int resource;
        public int getType() {
            return BINARY;
        }
        public ByteBuffer getBinary() {
            return wholeBundle.reader.getBinary(resource);
        }
        public byte [] getBinary(byte []ba) {
            return wholeBundle.reader.getBinary(resource, ba);
        }
        ResourceBinary(ICUResourceBundleImpl container, String key, int resource) {
            super(container, key);
            this.resource = resource;
        }
    }
    private static final class ResourceInt extends ICUResourceBundleImpl {
        private int resource;
        public int getType() {
            return INT;
        }
        public int getInt() {
            return ICUResourceBundleReader.RES_GET_INT(resource);
        }
        public int getUInt() {
            return ICUResourceBundleReader.RES_GET_UINT(resource);
        }
        ResourceInt(ICUResourceBundleImpl container, String key, int resource) {
            super(container, key);
            this.resource = resource;
        }
    }
    private static final class ResourceString extends ICUResourceBundleImpl {
        private int resource;
        private String value;
        public int getType() {
            return STRING;
        }
        public String getString() {
            if (value != null) {
                return value;
            }
            return wholeBundle.reader.getString(resource);
        }
        ResourceString(ICUResourceBundleImpl container, String key, int resource) {
            super(container, key);
            this.resource = resource;
            String s = wholeBundle.reader.getString(resource);
            // Allow the reader cache's SoftReference to do its job.
            if (s.length() < ICUResourceBundleReader.LARGE_SIZE / 2) {
                value = s;
            }
        }
    }
    private static final class ResourceIntVector extends ICUResourceBundleImpl {
        private int resource;
        public int getType() {
            return INT_VECTOR;
        }
        public int[] getIntVector() {
            return wholeBundle.reader.getIntVector(resource);
        }
        ResourceIntVector(ICUResourceBundleImpl container, String key, int resource) {
            super(container, key);
            this.resource = resource;
        }
    }

    // Container values ---------------------------------------------------- ***

    static abstract class ResourceContainer extends ICUResourceBundleImpl {
        protected ICUResourceBundleReader.Container value;

        public int getSize() {
            return value.getSize();
        }
        @Override
        public String getString(int index) {
            int res = value.getContainerResource(wholeBundle.reader, index);
            if (res == RES_BOGUS) {
                throw new IndexOutOfBoundsException();
            }
            String s = wholeBundle.reader.getString(res);
            if (s != null) {
                return s;
            }
            return super.getString(index);
        }
        protected int getContainerResource(int index) {
            return value.getContainerResource(wholeBundle.reader, index);
        }
        protected UResourceBundle createBundleObject(int index, String resKey, HashMap<String, String> aliasesVisited,
                                                     UResourceBundle requested, boolean[] isAlias) {
            int item = getContainerResource(index);
            if (item == RES_BOGUS) {
                throw new IndexOutOfBoundsException();
            }
            return createBundleObject(resKey, item, aliasesVisited, requested, isAlias);
        }

        // Resource bundle lookup cache, which may be used by subclasses
        // which have nested resources
        protected ICUCache<Object, UResourceBundle> lookup;
        private static final int MAX_INITIAL_LOOKUP_SIZE = 64;

        protected void createLookupCache() {
            lookup = new SimpleCache<Object, UResourceBundle>(ICUCache.WEAK, Math.max(getSize()*2, MAX_INITIAL_LOOKUP_SIZE));
        }

        protected UResourceBundle handleGet(String resKey, HashMap<String, String> aliasesVisited, UResourceBundle requested) {
            UResourceBundle res = null;
            if (lookup != null) {
                res = lookup.get(resKey);
            }
            if (res == null) {
                int[] index = new int[1];
                boolean[] alias = new boolean[1];
                res = handleGetImpl(resKey, aliasesVisited, requested, index, alias);
                if (res != null && lookup != null && !alias[0]) {
                    // We do not want to cache a result from alias entry
                    lookup.put(resKey, res);
                    lookup.put(Integer.valueOf(index[0]), res);
                }
            }
            return res;
        }

        protected UResourceBundle handleGet(int index, HashMap<String, String> aliasesVisited, UResourceBundle requested) {
            UResourceBundle res = null;
            Integer indexKey = null;
            if (lookup != null) {
                indexKey = Integer.valueOf(index);
                res = lookup.get(indexKey);
            } 
            if (res == null) {
                boolean[] alias = new boolean[1];
                res = handleGetImpl(index, aliasesVisited, requested, alias);
                if (res != null && lookup != null && !alias[0]) {
                    // We do not want to cache a result from alias entry
                    lookup.put(res.getKey(), res);
                    lookup.put(indexKey, res);
                }
            }
            return res;
        }

        // Subclass which supports key based resource access to implement this method
        protected abstract UResourceBundle handleGetImpl(
                String resKey, HashMap<String, String> aliasesVisited, UResourceBundle requested,
                int[] index, boolean[] isAlias);

        // Subclass which supports index based resource access to implement this method
        protected abstract UResourceBundle handleGetImpl(
                int index, HashMap<String, String> aliasesVisited, UResourceBundle requested,
                boolean[] isAlias);

        ResourceContainer(ICUResourceBundleImpl container, String key) {
            super(container, key);
        }
        ResourceContainer(WholeBundle wholeBundle) {
            super(wholeBundle);
        }
    }
    private static class ResourceArray extends ResourceContainer {
        public int getType() {
            return ARRAY;
        }
        protected String[] handleGetStringArray() {
            ICUResourceBundleReader reader = wholeBundle.reader;
            int length = value.getSize();
            String[] strings = new String[length];
            for (int i = 0; i < length; ++i) {
                String s = reader.getString(value.getContainerResource(reader, i));
                if (s == null) {
                    throw new UResourceTypeMismatchException("");
                }
                strings[i] = s;
            }
            return strings;
        }
        public String[] getStringArray() {
            return handleGetStringArray();
        }
        protected UResourceBundle handleGetImpl(String indexStr, HashMap<String, String> aliasesVisited,
                                                UResourceBundle requested,
                                                int[] index, boolean[] isAlias) {
            int i = indexStr.length() > 0 ? Integer.valueOf(indexStr).intValue() : -1;
            if(index != null) {
                index[0] = i;
            }
            if (i < 0) {
                throw new UResourceTypeMismatchException("Could not get the correct value for index: "+ indexStr);
            }
            return createBundleObject(i, indexStr, aliasesVisited, requested, isAlias);
        }
        protected UResourceBundle handleGetImpl(int index, HashMap<String, String> aliasesVisited,
                                                UResourceBundle requested, boolean[] isAlias) {
            return createBundleObject(index, Integer.toString(index), aliasesVisited, requested, isAlias);
        }
        ResourceArray(ICUResourceBundleImpl container, String key, int resource) {
            super(container, key);
            value = wholeBundle.reader.getArray(resource);
            createLookupCache(); // Use bundle cache to access array entries
        }
    }
    static class ResourceTable extends ResourceContainer {
        public int getType() {
            return TABLE;
        }
        protected String getKey(int index) {
            return ((ICUResourceBundleReader.Table)value).getKey(wholeBundle.reader, index);
        }
        protected Set<String> handleKeySet() {
            ICUResourceBundleReader reader = wholeBundle.reader;
            TreeSet<String> keySet = new TreeSet<String>();
            ICUResourceBundleReader.Table table = (ICUResourceBundleReader.Table)value;
            for (int i = 0; i < table.getSize(); ++i) {
                keySet.add(table.getKey(reader, i));
            }
            return keySet;
        }
        protected UResourceBundle handleGetImpl(String resKey, HashMap<String, String> aliasesVisited,
                                                UResourceBundle requested,
                                                int[] index, boolean[] isAlias) {
            int i = ((ICUResourceBundleReader.Table)value).findTableItem(wholeBundle.reader, resKey);
            if(index != null) {
                index[0] = i;
            }
            if (i < 0) {
                return null;
            }
            return createBundleObject(i, resKey, aliasesVisited, requested, isAlias);
        }
        protected UResourceBundle handleGetImpl(int index, HashMap<String, String> aliasesVisited,
                                                UResourceBundle requested, boolean[] isAlias) {
            String itemKey = ((ICUResourceBundleReader.Table)value).getKey(wholeBundle.reader, index);
            if (itemKey == null) {
                throw new IndexOutOfBoundsException();
            }
            return createBundleObject(index, itemKey, aliasesVisited, requested, isAlias);
        }
        @Override
        protected Object handleGetObject(String key) {
            // Fast path for common cases: Avoid creating UResourceBundles if possible.
            // It would be even better if we could override getString(key)/getStringArray(key),
            // so that we know the expected object type,
            // but those are final in java.util.ResourceBundle.
            ICUResourceBundleReader reader = wholeBundle.reader;
            int index = ((ICUResourceBundleReader.Table)value).findTableItem(reader, key);
            if (index >= 0) {
                int res = value.getContainerResource(reader, index);
                // getString(key)
                String s = reader.getString(res);
                if (s != null) {
                    return s;
                }
                // getStringArray(key)
                ICUResourceBundleReader.Container array = reader.getArray(res);
                if (array != null) {
                    int length = array.getSize();
                    String[] strings = new String[length];
                    for (int j = 0;; ++j) {
                        if (j == length) {
                            return strings;
                        }
                        s = reader.getString(array.getContainerResource(reader, j));
                        if (s == null) {
                            // Equivalent to resolveObject(key, requested):
                            // If this is not a string array,
                            // then build and return a UResourceBundle.
                            break;
                        }
                        strings[j] = s;
                    }
                }
            }
            return super.handleGetObject(key);
        }
        /**
         * Returns a String if found, or null if not found or if the key item is not a string.
         */
        String findString(String key) {
            ICUResourceBundleReader reader = wholeBundle.reader;
            int index = ((ICUResourceBundleReader.Table)value).findTableItem(reader, key);
            if (index < 0) {
                return null;
            }
            return reader.getString(value.getContainerResource(reader, index));
        }
        ResourceTable(ICUResourceBundleImpl container, String key, int resource) {
            super(container, key);
            value = wholeBundle.reader.getTable(resource);
            createLookupCache(); // Use bundle cache to access table entries
        }
        /**
         * Constructor for the root table of a bundle.
         */
        ResourceTable(WholeBundle wholeBundle, int rootRes) {
            super(wholeBundle);
            value = wholeBundle.reader.getTable(rootRes);
            createLookupCache(); // Use bundle cache to access table entries
        }
    }
}
