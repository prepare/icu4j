package com.ibm.icu.impl.nrb.sres;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public static Object loadResource(String base, String name, Integer serial, ClassLoader loader) {
        String fullName = getFullName(base, name, serial);
        InputStream is = ICUData.getStream(loader, fullName);
        if (is == null) {
            return null;
        }

        List<String> sharedKeys, localKeys, stringPool1, stringPool2;
        sharedKeys = localKeys = stringPool1 = stringPool2 = Collections.emptyList();

        BufferedInputStream resStream = new BufferedInputStream(is);
        Object res = null;
        try {
            // TODO
            int flag = readHeader(resStream);

            // Check if this resource uses shared keys
            if ((flag & ICUSResConstants.USE_SHARED_KEY_RESOURCE) != 0) {
                sharedKeys = loadSharedKeys(base, loader);
            }

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
                BundleInfo bundleInfo = new BundleInfo(base, name, loader);
                res = readResource(resStream, localKeys, sharedKeys, stringPool1, stringPool2, bundleInfo);
            }

            resStream.close();
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }

        return res;
    }

    public static ResourceTable loadTopTable(String base, String name, ClassLoader loader) {
        Object res = loadResource(base, name, null, loader);
        if (!(res instanceof ResourceTable)) {
            throw new RuntimeException("Top resource is not a table");
        }
        return (ResourceTable) res;
    }

    private static List<String> loadSharedKeys(String base, ClassLoader loader) {
        List<String> sharedKeys = null;
        String fullName = getFullName(base, "pool", null);

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
        return readStrings(resStream, true);
    }

    private static List<String> readStringsUTF16BE (InputStream resStream) throws IOException {
        return readStrings(resStream, false);
    }

    private static List<String> readStrings (InputStream resStream, boolean isUTF8) throws IOException {
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

            String str;
            if (isUTF8) {
                str = UTF8ToString(buf, 0, len);
            } else {
                str = UTF16BEToString(buf, 0, len);
            }
            strings.add(str);
        }

        return strings;
    }

    private static Object readResource(InputStream resStream, List<String> localKeys, List<String> sharedKeys,
            List<String> strings1, List<String> strings2, BundleInfo bundleInfo) throws IOException {
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
                objs[i] = readResource(resStream, localKeys, sharedKeys, strings1, strings2, bundleInfo);
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
                objs[i][1] = readResource(resStream, localKeys, sharedKeys, strings1, strings2, bundleInfo);
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
            resObj = new AuxiliaryResource(bundleInfo, idx);
            break;
        }
        default:
            break;
        }

        return resObj;
    }

    private static String getFullName(String base, String name, Integer serial) {
        StringBuilder buf = new StringBuilder();

        if (base == null || base.length() == 0) {
            if (name.length() == 0) {
                buf.append("root");
            } else {
                buf.append(name);
            }
        } else {
            if (base.indexOf('.') == -1) {
                if (base.charAt(base.length() - 1) != '/') {
                    buf.append(base);
                    buf.append("/");
                    buf.append(name);
                } else {
                    buf.append(base);
                    buf.append(name);
                }
            } else {
                base = base.replace('.', '/');
                if (name.length() == 0) {
                    buf.append(base);
                } else {
                    buf.append(base);
                    buf.append("_");
                    buf.append(name);
                }
            }
        }

        if (serial != null) {
            buf.append("$");
            buf.append(serial);
        }

        buf.append(".sres");

        return buf.toString();
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

    // Local UTF converters
    // 4 to 5 times faster than the String constructor
    private static final int ER = -1;
    private static final int B1 = 0;
    private static final int D2 = 1;
    private static final int T2 = 2;
    private static final int T3 = 3;
    private static final int Q2 = 4;
    private static final int Q3 = 5;
    private static final int Q4 = 6;

    public static String UTF8ToString(byte[] bytes, int start, int len) {
        StringBuilder longBuf = null;
        char[] cbuf = new char[32];
        int charIdx = 0;
        int state = B1;
        int scalar;

        for (int i = 0; i < len ; i++) {
            byte b = bytes[start + i];
            switch (state) {
            case B1:
                if ((b & 0x80) == 0) {
                    cbuf[charIdx++] = (char) b;
                } else if ((b & 0x20) == 0) {
                    state = D2;
                } else if ((b & 0x10) == 0) {
                    state = T2;
                } else if ((b & 0x08) == 0) {
                    state = Q2;
                } else {
                    state = ER;
                }
                break;

            case D2:
                if ((b & 0x80) == 0) {
                    state = ER;
                } else {
                    scalar = (bytes[start + i -1] & 0x1f) << 6
                           | (bytes[start + i] & 0x3f);
                    if (scalar >= 0x80) {
                        cbuf[charIdx++] = (char) scalar;
                        state = B1;
                    } else {
                        state = ER;
                    }
                }
                break;

            case T2:
                if ((b & 0x80) == 0) {
                    state = ER;
                } else {
                    state = T3;
                }
                break;

            case T3:
                if ((b & 0x80) == 0) {
                    state = ER;
                } else {
                    scalar = (bytes[start + i - 2] & 0x0f) << 12
                           | (bytes[start + i - 1] & 0x3f) << 6
                           | (bytes[start + i] & 0x3f);
                    if (scalar >= 0x0400) {
                        cbuf[charIdx++] = (char) scalar;
                        state = B1;
                    } else {
                        state = ER;
                    }
                }
                break;

            case Q2:
                if ((b & 0x80) == 0) {
                    state = ER;
                } else {
                    state = Q3;
                }
                break;

            case Q3:
                if ((b & 0x80) == 0) {
                    state = ER;
                } else {
                    state = Q4;
                }
                break;

            case Q4:
                if ((b & 0x80) == 0) {
                    state = ER;
                } else {
                    scalar = (bytes[start + i - 3] & 0x07) << 18
                           | (bytes[start + i - 2] & 0x3f) << 12
                           | (bytes[start + i - 1] & 0x3f) << 6
                           | (bytes[start + i] & 0x3f);
                    if (scalar >= 0x10000) {
                        int utf16h = ((scalar >> 16) - 1) << 6 | (scalar & 0xffff) >> 10 | 0xd800;
                        int utf16l = (scalar & 0x3ff) | 0xdc00;
                        cbuf[charIdx++] = (char) utf16h;
                        cbuf[charIdx++] = (char) utf16l;
                        state = B1;
                    } else {
                        state = ER;
                    }
                }
                break;

            default:
                state = ER;
                break;
            }

            if (state == ER) {
                break;
            }

            if (charIdx >= cbuf.length - 1) {
                // write out characters
                if (longBuf == null) {
                    longBuf = new StringBuilder(charIdx + 10);
                }
                longBuf.append(cbuf, 0, charIdx);
                charIdx = 0;
            }
        }

        if (state != B1) {
            return null;
        }

        if (longBuf == null) {
            return new String(cbuf, 0, charIdx);
        }

        if (charIdx != 0) {
            longBuf.append(cbuf, 0, charIdx);
        }
        return longBuf.toString();
    }

    private static String UTF16BEToString(byte[] bytes, int start, int len) {
        StringBuilder buf = new StringBuilder(len / 2);
        for (int i = 0; i < len / 2; i++) {
            char c = (char) (bytes[start + i * 2] << 8 | bytes[start + i * 2 + 1]);
            buf.append(c);
        }
        return buf.toString();
    }

    static class BundleInfo {
        private String _base;
        private String _name;
        private ClassLoader _loader;

        BundleInfo(String base, String name, ClassLoader loader) {
            _base = base;
            _name = name;
            _loader = loader;
        }

        String getBase() {
            return _base;
        }

        String getName() {
            return _name;
        }

        ClassLoader getLoader() {
            return _loader;
        }
    }
}