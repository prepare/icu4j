package com.ibm.icu.impl.nrb.sres.tools;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.ibm.icu.impl.nrb.ResourceAccessor;
import com.ibm.icu.impl.nrb.ResourceAlias;
import com.ibm.icu.impl.nrb.ResourceArray;
import com.ibm.icu.impl.nrb.ResourceConstants;
import com.ibm.icu.impl.nrb.ResourceConstants.Type;
import com.ibm.icu.impl.nrb.ResourceTable;
import com.ibm.icu.impl.nrb.sres.ICUSResConstants;

public class SerializedResWriter {

    private Object _resRoot;
    private String _resRootPath;
    private List<Filter> _auxFilters;
    private SortedSet<String> _sharedKeys;

    private SortedSet<String> _localKeys;
    private Map<String, Integer> _keyIndex;

    private SortedMap<String, byte[]> _stringPoolUTF8;
    private SortedMap<String, byte[]> _stringPoolUTF16BE;
    private Map<String, Integer> _stringPoolIndex;

    public SerializedResWriter(Object resRoot, SortedSet<String> sharedKeys, List<Filter> auxFilters) {
        this(resRoot, "", sharedKeys, auxFilters);
    }

    private SerializedResWriter(Object resRoot, String resRootPath, SortedSet<String> sharedKeys, List<Filter> auxFilters) {
        _resRoot = resRoot;
        _sharedKeys = (sharedKeys == null) ? new TreeSet<String>() : sharedKeys;
        _auxFilters = auxFilters;
        _resRootPath = resRootPath;

        initKeys();
        initStringPools();
    }

    private void initKeys() {
        _localKeys = new TreeSet<String>();
        gatherLocalKeys(_resRootPath, _resRoot);

        // build a key index map
        _keyIndex = new HashMap<String, Integer>(_sharedKeys.size() + _localKeys.size());

        // local keys first
        int idx =  0;
        for (String key : _localKeys) {
            _keyIndex.put(key, Integer.valueOf(idx++));
        }
        for (String key : _sharedKeys) {
            _keyIndex.put(key, Integer.valueOf(idx++));
        }
        
    }

    private void gatherLocalKeys(String resPath, Object res) {
        if (res instanceof ResourceTable) {
            ResourceTable table = (ResourceTable)res;
            for (String key : table.keySet()) {
                if (!_sharedKeys.contains(key)) {
                    _localKeys.add(key);
                }
                // check for sub containers
                Type type = table.getType(key);
                if (type == Type.TABLE || type == Type.OBJECT_ARRAY) {
                    Object childRes = table.getObject(key);
                    String childPath = resPath + ResourceConstants.RESSEP + key;
                    if (!isAuxiliaryResource(childPath, childRes)) {
                        gatherLocalKeys(childPath, childRes);
                    }
                }
            }
        } else if (res instanceof ResourceArray) {
            ResourceArray array = (ResourceArray)res;
            for (int i = 0; i < array.size(); i++) {
                Type type = array.getType(i);
                if (type == Type.TABLE || type == Type.OBJECT_ARRAY) {
                    Object childRes = array.getObject(i);
                    String childPath = resPath + ResourceConstants.RESSEP + i;
                    if (!isAuxiliaryResource(childPath, childRes)) {
                        gatherLocalKeys(childPath, childRes);
                    }
                }
            }
        }
    }

    private void initStringPools() {
        _stringPoolUTF8 = new TreeMap<String, byte[]>();
        _stringPoolUTF16BE = new TreeMap<String, byte[]>();
        gatherStrings(_resRootPath, _resRoot);

        // make a unified index map
        _stringPoolIndex = new HashMap<String, Integer>(_stringPoolUTF8.size() + _stringPoolUTF16BE.size());
        int idx = 0;
        for (String s : _stringPoolUTF8.keySet()) {
            _stringPoolIndex.put(s, Integer.valueOf(idx++));
        }
        for (String s : _stringPoolUTF16BE.keySet()) {
            _stringPoolIndex.put(s, Integer.valueOf(idx++));
        }
    }

    private void gatherStrings(String resPath, Object res) {
        if (isAuxiliaryResource(resPath, res)) {
            return;
        }
        if (res instanceof String) {
            addStringToPool((String) res);
        } else if (res instanceof ResourceAlias) {
            addStringToPool(((ResourceAlias) res).getPath());
        } else if (res instanceof ResourceTable) {
            ResourceTable table = (ResourceTable)res;
            for (String key : table.keySet()) {
                Object childRes = table.getObject(key);
                String childPath = resPath + ResourceConstants.RESSEP + key;
                if (!isAuxiliaryResource(childPath, childRes)) {
                    gatherStrings(childPath, childRes);
                }
            }
        } else if (res instanceof ResourceArray) {
            ResourceArray array = (ResourceArray)res;
            for (int i = 0; i < array.size(); i++) {
                Object childRes = array.getObject(i);
                String childPath = resPath + ResourceConstants.RESSEP + i;
                if (!isAuxiliaryResource(childPath, childRes)) {
                    gatherStrings(childPath, childRes);
                }
            }
        }
    }

    private void addStringToPool(String s) {
        if (!_stringPoolUTF8.containsKey(s) && !_stringPoolUTF16BE.containsKey(s)) {
            byte[] utf8;
            byte[] utf16be;
            try {
                utf8 = s.getBytes("UTF-8");
                utf16be = s.getBytes("UTF-16");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            if (utf8.length < utf16be.length) {
                _stringPoolUTF8.put(s, utf8);
            } else {
                _stringPoolUTF16BE.put(s, utf16be);
            }
        }
    }

    private boolean isAuxiliaryResource(String resPath, Object resource) {
        boolean auxRes = false;
        if (_auxFilters != null && !resPath.equals(_resRootPath)) {
            for (Filter f : _auxFilters) {
                if (f.externalize(resPath, resource)) {
                    auxRes = true;
                    break;
                }
            }
        }
        return auxRes;
    }

    public void write(File outFile) throws IOException {
        int[] auxNum = new int[] { 0 };
        write(outFile, auxNum);
    }

    private void write(File outFile, int[] auxNum) throws IOException {
        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outFile));

        // TODO - formal header
        int flag = 0;
        if (!_sharedKeys.isEmpty()) {
            flag |= ICUSResConstants.USE_SHARED_KEY_RESOURCE;
        }
        writeHeader(os, flag);

        writeLocalKeys(os);
        writeStringPools(os);

        // start of the actual resource data
        os.write(ICUSResConstants.RESOURCE_DATA);

        writeResource(os, _resRoot, _resRootPath, false, outFile, auxNum);

        os.close();
    }

    public void writeShareKeyResource(File outFile) throws IOException {
        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outFile));

        writeKeys(os, _sharedKeys);

        os.close();
    }

    private void writeHeader(OutputStream os, int flag) throws IOException {
        // TODO - formal header
        os.write(flag >> 24);
        os.write(flag >> 16);
        os.write(flag >> 8);
        os.write(flag);
    }

    private void writeLocalKeys(OutputStream os) throws IOException {
        if (_localKeys.isEmpty()) {
            return;
        }

        writeKeys(os, _localKeys);
    }

    private void writeKeys(OutputStream os, SortedSet<String> keys) throws IOException {
        // signature
        os.write(ICUSResConstants.KEY_POOL);

        // number of elements represented by the variable width integer
        writeVarWidthInt(os, keys.size());

        // keys
        for (String key : keys) {
            // We use UTF-8 always
            byte[] utf8 = key.getBytes("UTF-8");

            // length of bytes represented by the variable width integer
            writeVarWidthInt(os, utf8.length);

            // UTF-8 bytes
            os.write(utf8);
        }
    }

    private void writeStringPools(OutputStream os) throws IOException {
        if (!_stringPoolUTF8.isEmpty()) {
            // signature
            os.write(ICUSResConstants.STRING_POOL_UTF8);

            // number of elements represented by the variable width integer
            writeVarWidthInt(os, _stringPoolUTF8.size());

            // strings
            for (byte[] utf8 : _stringPoolUTF8.values()) {
                // length of bytes represented by the variable width integer
                writeVarWidthInt(os, utf8.length);

                // UTF-8 bytes
                os.write(utf8);
            }
        }

        if (!_stringPoolUTF16BE.isEmpty()) {
            // signature
            os.write(ICUSResConstants.STRING_POOL_UTF16BE);

            // number of elements represented by the variable width integer
            writeVarWidthInt(os, _stringPoolUTF16BE.size());

            // strings
            for (byte[] utf16 : _stringPoolUTF16BE.values()) {
                // length of bytes represented by the variable width integer
                writeVarWidthInt(os, utf16.length);

                // UTF-16BE bytes
                os.write(utf16);
            }
        }
    }

    private void writeResource(OutputStream os, Object res, String resPath, boolean useFilter, File baseFile, int[] auxNum) throws IOException {
        if (useFilter && isAuxiliaryResource(resPath, res)) {
            writeAuxResource(os, res, resPath, baseFile, auxNum);
            return;
        }

        Type t = ResourceAccessor.getType(res);
        switch (t) {
        case STRING:
            writeStringResource(os, (String) res);
            break;
        case INTEGER:
            writeIntegerResource(os, (Integer) res);
            break;
        case INTEGER_ARRAY:
            writeIntArrayResource(os, (int[]) res);
            break;
        case BYTE_ARRAY:
            writeByteArrayResource(os, (byte[]) res);
            break;
        case OBJECT_ARRAY:
        {
            // check if this is a string array
            ResourceArray ra = (ResourceArray) res;
            String[] strarray = new String[ra.size()];
            for (int i = 0; i < ra.size(); i++) {
                if (ra.getType(i) == Type.STRING) {
                    strarray[i] = ra.getString(i);
                } else {
                    strarray = null;
                    break;
                }
            }
            if (strarray != null) {
                writeStringArrayResource(os, strarray);
            } else {
                writeResourceArray(os, ra, resPath, baseFile, auxNum);
            }
            break;
        }
        case TABLE:
            writeResourceTable(os, (ResourceTable) res, resPath, baseFile, auxNum);
            break;
        case ALIAS:
            writeResourceAlias(os, (ResourceAlias) res);
            break;
        default:
            // TODO
            break;
        }
        
    }

    private void writeStringResource(OutputStream os, String s) throws IOException {
        // signature
        os.write(ICUSResConstants.STRING);

        // index in the string pool represented by the variable width integer
        Integer idx = _stringPoolIndex.get(s);
        if (idx == null) {
            // all string resources must be pooled
            throw new RuntimeException("String '" + s + "' is not available in the string pool");
        }
        writeVarWidthInt(os, idx.intValue());
    }

    private void writeStringArrayResource(OutputStream os, String[] sa) throws IOException {
        // signature
        os.write(ICUSResConstants.STRING_ARRAY);

        // number of elements represented by the variable width integer
        writeVarWidthInt(os, sa.length);

        // index in the string pool represented by the variable width integer
        for (String s : sa) {
            Integer idx = _stringPoolIndex.get(s);
            if (idx == null) {
                // all string resources must be pooled
                throw new RuntimeException("String '" + s + "' is not available in the string pool");
            }
            writeVarWidthInt(os, idx.intValue());
        }
    }

    private static final int MAX_INT16 = 0x7fff;
    private static final int MIN_INT16 = -0x8000;

    private void writeIntegerResource(OutputStream os, Integer i) throws IOException {
        int val = i.intValue();
        if (i >= MIN_INT16 || i <= MAX_INT16) {
            // use 16bit representation

            // signature
            os.write(ICUSResConstants.INT16);

            // value in big endian order
            os.write(val >> 8);
            os.write(val);
        } else {
            // use 32bit representation

            // signature
            os.write(ICUSResConstants.INT32);

            // value in big endian order
            os.write(val >> 24);
            os.write(val >> 16);
            os.write(val >> 8);
            os.write(val);
        }
    }

    private void writeIntArrayResource(OutputStream os, int[] ia) throws IOException {
        boolean useInt16 = true;
        for (int i : ia) {
            if (i < MIN_INT16 || i > MAX_INT16) {
                useInt16 = false;
                break;
            }
        }

        if (useInt16) {
            // signature
            os.write(ICUSResConstants.INT16_ARRAY);

            // number of elements represented by the variable width integer
            writeVarWidthInt(os, ia.length);

            // values
            for (int i : ia) {
                os.write(i >> 8);
                os.write(i);
            }
        } else {
            // signature
            os.write(ICUSResConstants.INT32_ARRAY);

            // number of elements represented by the variable width integer
            writeVarWidthInt(os, ia.length);

            // values
            for (int i : ia) {
                os.write(i >> 24);
                os.write(i >> 16);
                os.write(i >> 8);
                os.write(i);
            }
        }
    }

    private void writeByteArrayResource(OutputStream os, byte[] ba) throws IOException {
        // signature
        os.write(ICUSResConstants.BYTE_ARRAY);

        // number of elements represented by the variable width integer
        writeVarWidthInt(os, ba.length);

        // byte array data
        os.write(ba);
    }

    private void writeResourceArray(OutputStream os, ResourceArray ra, String resPath, File baseFile, int[] auxNum) throws IOException {
        // signature
        os.write(ICUSResConstants.OBJECT_ARRAY);

        // number of elements represented by the variable width integer
        writeVarWidthInt(os, ra.size());

        // resource elements
        for (int i = 0; i < ra.size(); i++) {
            writeResource(os, ra.getObject(i), resPath + ResourceConstants.RESSEP + i, true, baseFile, auxNum);
        }
    }

    private void writeResourceTable(OutputStream os, ResourceTable rt, String resPath, File baseFile, int[] auxNum) throws IOException {
        // signature
        os.write(ICUSResConstants.TABLE);

        // number of elements represented by the variable width integer
        writeVarWidthInt(os, rt.size());

        // resource elements
        for (String key : rt.keySet()) {
            // index of the key in the key pool by the variable width integer
            Integer idx = _keyIndex.get(key);
            if (idx == null) {
                // all keys must be pooled
                throw new RuntimeException("Key '" + key + "' is not available in the key pool");
            }
            writeVarWidthInt(os, idx.intValue());

            // value
            writeResource(os, rt.getObject(key), resPath + ResourceConstants.RESSEP + key, true, baseFile, auxNum);
        }
    }

    private void writeResourceAlias(OutputStream os, ResourceAlias ralias) throws IOException {
        // signature
        os.write(ICUSResConstants.ALIAS);

        // index in the string pool represented by the variable width integer
        String path = ralias.getPath();
        Integer idx = _stringPoolIndex.get(path);
        if (idx == null) {
            // all string resources must be pooled
            throw new RuntimeException("Alias path '" + path + "' is not available in the string pool");
        }
        writeVarWidthInt(os, idx.intValue());
    }

    private void writeAuxResource(OutputStream os, Object res, String resPath, File baseFile, int[] auxNum) throws IOException {
        // signature
        os.write(ICUSResConstants.AUX);

        // write auxiliary resource serial number
        writeVarWidthInt(os, auxNum[0]);

        // auxiliary resource file name
        String baseName = baseFile.getName();
        int idx = baseName.lastIndexOf('.');
        String auxFileName;
        if (idx < 0) {
            auxFileName = baseName + "$" + auxNum[0];
        } else {
            auxFileName = baseName.substring(0, idx) + "$" + auxNum[0] + baseName.substring(idx);
        }

        File auxFile = new File(baseFile.getParentFile(), auxFileName);

        SerializedResWriter auxWriter = new SerializedResWriter(res, resPath, _sharedKeys, _auxFilters);
        auxNum[0]++;
        auxWriter.write(auxFile, auxNum);
    }

    /*
     * Writes integer value with 7-bit byte units.
     * The 8th (highest) bit is used for continuation marker.
     * For example,
     * 
     *   0: 00000000
     *   1: 00000001
     * 127: 01111111
     * 128: 10000001 00000000
     * 129: 10000001 00000001
     */
    private static void writeVarWidthInt(OutputStream os, int val) throws IOException {
        int[] units = new int[5];
        int lastNoneZeroIdx = 0;
        for (int i = 0; i < units.length; i++) {
            units[i] = val & 0x7f;
            if (units[i] != 0) {
                lastNoneZeroIdx = i;
            }
            val = val >>> 7;
        }

        for (int i = lastNoneZeroIdx; i >= 0; i--) {
            if (i != 0) {
                os.write(units[i] | 0x80);
            } else {
                os.write(units[i]);
            }
        }
    }

    public abstract static class Filter {
        public abstract boolean externalize(String resPath, Object resource);
    }


    // --------------------------------------------------------------------------
    // Dubug methods
    // --------------------------------------------------------------------------
    public void writeKeys(Object rootRes) {
        Integer idx;
        System.out.println("#### Local Keys (" + _localKeys.size() + " keys)");
        for (String key : _localKeys) {
            idx = _keyIndex.get(key);
            System.out.println(idx + ": " + key);
        }
        System.out.println("#### Shared Keys (" + _sharedKeys.size() + " keys)");
        for (String key : _sharedKeys) {
            idx = _keyIndex.get(key);
            System.out.println(idx + ": " + key);
        }
    }

    public void writePoolStrings(Object rootRes) {
        Integer idx;
        System.out.println("#### UTF8 Strings (" + _stringPoolUTF8.size() + " strings)");
        for (String s : _stringPoolUTF8.keySet()) {
            idx = _stringPoolIndex.get(s);
            System.out.println(idx + ": " + s);
        }

        System.out.println("#### UTF16 Strings (" + _stringPoolUTF16BE.size() + " strings)");
        for (String s : _stringPoolUTF16BE.keySet()) {
            idx = _stringPoolIndex.get(s);
            System.out.println(idx + ": " + s);
        }
    }

    public static byte[] getVarWidthIntBytes(int val) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            writeVarWidthInt(bos, val);
        } catch (IOException e) {
            return null;
        }
        return bos.toByteArray();
    }
}
