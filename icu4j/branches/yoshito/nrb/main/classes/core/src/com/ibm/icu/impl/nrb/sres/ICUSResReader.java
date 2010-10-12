package com.ibm.icu.impl.nrb.sres;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ibm.icu.impl.ICUCache;
import com.ibm.icu.impl.ICUData;
import com.ibm.icu.impl.SimpleCache;
import com.ibm.icu.impl.nrb.ResourceAlias;
import com.ibm.icu.impl.nrb.ResourceTable;

public class ICUSResReader {

    private static ICUCache<String, List<String>> KEY_POOL_CACHE = new SimpleCache<String, List<String>>();

    public static ResourceTable loadRootTable(String base, String name, ClassLoader loader) throws IOException {
        String fullName = getFullName(base, name);
        InputStream is = ICUData.getStream(loader, fullName);
        if (is == null) {
            return null;
        }

        List<String> sharedKeys, localKeys, stringPool1, stringPool2;
        sharedKeys = localKeys = stringPool1 = stringPool2 = Collections.emptyList();

        BufferedInputStream resStream = new BufferedInputStream(is);

        // TODO
        int flag = readHeader(resStream);

        // Check if this resource uses shared keys
        if ((flag & ICUSResConstants.USE_SHARED_KEY_RESOURCE) != 0) {
            sharedKeys = loadSharedKeys(base, loader);
        }

        ResourceTable rootTable = null;

        try {
            byte signature = (byte) resStream.read();
            if (signature == ICUSResConstants.KEY_POOL) {
                localKeys = readStringsUTF8(resStream);
                signature = (byte) resStream.read();
            }
            if (signature == ICUSResConstants.STRING_POOL_UTF8) {
                stringPool1 = readStringsUTF8(resStream);
                signature = (byte) resStream.read();
            }
            if (signature == ICUSResConstants.STRING_POOL_UTF16BE) {
                stringPool2 = readStringsUTF16BE(resStream);
                signature = (byte) resStream.read();
            }

            if (signature == ICUSResConstants.RESOURCE_DATA) {
                Object res = readResource(resStream, localKeys, sharedKeys, stringPool1, stringPool2);
                if (!(res instanceof ResourceTable)) {
                    throw new RuntimeException("Root resource is not a table");
                }
                rootTable = (ResourceTable)res;
            }

            resStream.close();
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }

        return rootTable;
    }

    private static List<String> loadSharedKeys(String base, ClassLoader loader) {
        List<String> sharedKeys = null;
        String fullName = getFullName(base, "pool");

        sharedKeys = KEY_POOL_CACHE.get(fullName);
        if (sharedKeys != null) {
            return sharedKeys;
        }

        InputStream is = ICUData.getStream(loader, fullName);
        if (is == null) {
            return null;
        }

        BufferedInputStream resStream = new BufferedInputStream(is);


        List<String> keys = null;
        try {
            byte signature = (byte)resStream.read();
            if (signature == ICUSResConstants.KEY_POOL) {
                keys = readStringsUTF8(resStream);
            }

            resStream.close();
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }

        if (keys == null) {
            // TODO
            throw new RuntimeException("Missing pool.sres");
        }

        sharedKeys = Collections.unmodifiableList(keys);
        KEY_POOL_CACHE.put(fullName, sharedKeys);

        return sharedKeys;
    }

    private static int readHeader(InputStream resStream) throws IOException {
        // TODO format header
        return readInt32(resStream);
    }

    private static List<String> readStringsUTF8 (InputStream resStream) throws IOException {
        // number of strings
        int n = readVarWidthInt(resStream);
        ArrayList<String> strings = new ArrayList<String>(n);

        // read each string
        byte[] buf = new byte[256];
        char[] cbuf = new char[256];
        for (int i = 0; i < n; i++) {
            // length of a string
            int len = readVarWidthInt(resStream);

            if (len > buf.length) {
                buf = new byte[len];
            }

            int readLen = 0;
            while (readLen != len) {
                int tmpLen = resStream.read(buf, readLen, len - readLen);
                if (tmpLen == -1) {
                    throw new RuntimeException("Premature bytes");
                }
                readLen += tmpLen;
            }

            // TODO
            for (int j = 0; j < len; j++) {
                cbuf[j] = (char) buf[j];
            }
            String str = new String(cbuf, 0, len);
            strings.add(str);
        }

        return strings;
    }

    private static List<String> readStringsUTF16BE (InputStream resStream) throws IOException {
        // number of strings
        int n = readVarWidthInt(resStream);
        ArrayList<String> strings = new ArrayList<String>(n);

        // read each string
        byte[] buf = new byte[256];
        char[] cbuf = new char[128];
        for (int i = 0; i < n; i++) {
            // length of a string
            int len = readVarWidthInt(resStream);

            if (len > buf.length) {
                buf = new byte[len];
                cbuf = new char[len / 2];
            }

            int readLen = 0;
            while (readLen != len) {
                int tmpLen = resStream.read(buf, readLen, len - readLen);
                if (tmpLen == -1) {
                    throw new RuntimeException("Premature bytes");
                }
                readLen += tmpLen;
            }

            for (int j = 0; j < len / 2 ; j++) {
                cbuf[j] = (char) (buf[j * 2] << 8 | buf[j * 2 + 1]);
            }

            String str = new String(cbuf, 0, len / 2);
            strings.add(str);
        }

        return strings;
    }

    private static List<String> readStrings (InputStream resStream, String encoding) throws IOException {
        // number of strings
        int n = readVarWidthInt(resStream);
        ArrayList<String> strings = new ArrayList<String>(n);

        // read each string
        byte[] buf = new byte[256];
        for (int i = 0; i < n; i++) {
            // length of a string
            int len = readVarWidthInt(resStream);

            if (len > buf.length) {
                buf = new byte[len];
            }

            int readLen = 0;
            while (readLen != len) {
                int tmpLen = resStream.read(buf, readLen, len - readLen);
                if (tmpLen == -1) {
                    throw new RuntimeException("Premature bytes");
                }
                readLen += tmpLen;
            }

            String str = new String(buf, 0, len, encoding);
            strings.add(str);
        }

        return strings;
    }

    private static Object readResource(InputStream resStream, List<String> localKeys, List<String> sharedKeys,
            List<String> strings1, List<String> strings2) throws IOException {
        Object resObj = null;

        byte signature = (byte) resStream.read();

        switch (signature) {
        case ICUSResConstants.STRING:
        {
            resObj = readString(resStream, strings1, strings2);
            break;
        }
        case ICUSResConstants.INT16:
        {
            int val = readInt16(resStream);
            resObj = Integer.valueOf(val);
            break;
        }
        case ICUSResConstants.INT32:
        {
            int val = readInt32(resStream);
            resObj = Integer.valueOf(val);
            break;
        }
        case ICUSResConstants.BYTE_ARRAY:
        {
            // number of elements
            int n = readVarWidthInt(resStream);

            // byte array data
            resObj = readBytes(resStream, n);
            break;
        }
        case ICUSResConstants.INT16_ARRAY:
        {
            // number of elements
            int n = readVarWidthInt(resStream);

            // int array data
            int[] ia = new int[n];
            for (int i = 0; i < n; i++) {
                ia[i] = readInt16(resStream);
            }
            resObj = ia;
            break;
        }
        case ICUSResConstants.INT32_ARRAY:
        {
            // number of elements
            int n = readVarWidthInt(resStream);

            // int array data
            int[] ia = new int[n];
            for (int i = 0; i < n; i++) {
                ia[i] = readInt32(resStream);
            }
            resObj = ia;
            break;
        }
        case ICUSResConstants.STRING_ARRAY:
        {
            // number of elements
            int n = readVarWidthInt(resStream);

            // String array data
            String[] sa = new String[n];
            for (int i = 0; i < n; i++) {
                sa[i] = readString(resStream, strings1, strings2);
            }
            resObj = sa;
            break;
        }
        case ICUSResConstants.OBJECT_ARRAY:
        {
            // number of elements
            int n = readVarWidthInt(resStream);

            // resource array
            Object[] objs = new Object[n];
            for (int i = 0; i < n; i++) {
                objs[i] = readResource(resStream, localKeys, sharedKeys, strings1, strings2);
            }
            resObj = new ICUSResArray(objs);
            break;
        }
        case ICUSResConstants.TABLE:
        {
            // number of elements
            int n = readVarWidthInt(resStream);

            // resource table
            Object[][] objs = new Object[n][2];
            for (int i = 0; i < n; i++) {
                // key
                objs[i][0] = readString(resStream, localKeys, sharedKeys);
                // resource
                objs[i][1] = readResource(resStream, localKeys, sharedKeys, strings1, strings2);
            }

            resObj = new ICUSResTable(objs);
            break;
        }
        case ICUSResConstants.ALIAS:
        {
            String path = readString(resStream, strings1, strings2);
            resObj = new ResourceAlias(path);
            break;
        }
        case ICUSResConstants.AUX:
        {
            // serial# of auxiliary resource
            int idx = readVarWidthInt(resStream);
            resObj = new AuxiliaryResource(idx);
            break;
        }
        default:
            break;
        }

        return resObj;
    }

    private static String getFullName(String base, String name) {
        if (base == null || base.length() == 0) {
            if (name.length() == 0) {
                return "root.sres";
            } else {
                return name + ".sres";
            }
        } else {
            if (base.indexOf('.') == -1) {
                if (base.charAt(base.length() - 1) != '/') {
                    return base + "/" + name + ".sres";
                } else {
                    return base + name + ".sres";
                }
            } else {
                base = base.replace('.', '/');
                if (name.length() == 0) {
                    return base + ".sres";
                } else {
                    return base + "_" + name + ".sres";
                }
            }
        }
    }

    private static int readVarWidthInt(InputStream resStream) throws IOException {
        int val = 0;
        for (int i = 0; i < 5; i++) {
            int tmp = readByte(resStream);
            val = (val << 7) | (tmp & 0x7f);
            if ((tmp & 0x80) == 0) {
                break;
            }
        }
        return val;
    }

    private static int readByte(InputStream resStream) throws IOException {
        int b = resStream.read();
        if (b == -1) {
            throw new RuntimeException("Premature data");
        }
        return b;
    }

    private static byte[] readBytes(InputStream resStream, int len) throws IOException {
        byte[] bytes = new byte[len];
        int readLen = 0;
        while (readLen == len) {
            int tmp = resStream.read(bytes, readLen, len - readLen);
            if (tmp == -1) {
                throw new RuntimeException("Premature data");
            }
            readLen += tmp;
        }
        return bytes;
    }

    private static int readInt16(InputStream resStream) throws IOException {
        int val = (readByte(resStream) << 8) | readByte(resStream);
        if ((val & 0x8000) != 0) {
            // negative number
            val |= 0xffff0000;
        }
        return val;
    }

    private static int readInt32(InputStream resStream) throws IOException {
        int val = (readByte(resStream) << 24) | (readByte(resStream) << 16)
                | (readByte(resStream) << 8) | readByte(resStream);
        return val;
    }

    private static String readString(InputStream resStream, List<String> list1, List<String> list2) throws IOException {
        String str = null;
        int idx = readVarWidthInt(resStream);
        if (idx >=0) {
            if (idx < list1.size()) {
                str = list1.get(idx);
            } else if (idx < list1.size() + list2.size()) {
                str = list2.get(idx - list1.size());
            }
        }
        if (str == null) {
            throw new RuntimeException("String index out of range - " + idx);
        }
        return str;
    }
}