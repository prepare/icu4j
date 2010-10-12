package com.ibm.icu.impl.nrb;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Set;

import com.ibm.icu.impl.nrb.ResourceConstants.Type;

public abstract class ResourceTable extends ResourceArray {
    public abstract Set<String> keySet();

    public Type getType(String key) {
        Object res = handleGetObject(key);
        return (res == null) ? Type.NONE : ResourceAccessor.getType(res);
    }

    public Object getObject(String key) {
        return handleGetObject(key);
    }

    public String getString(String key) {
        Object res = handleGetObject(key);
        return (res == null) ? null : ResourceAccessor.getString(res);
    }

    public String[] getStringArray(String key) {
        Object res = handleGetObject(key);
        return (res == null) ? null : ResourceAccessor.getStringArray(res);
    }

    public int getInt(String key) {
        Object res = handleGetObject(key);
        return (res == null) ? null : ResourceAccessor.getInt(res);
    }

    public int getUInt(String key) {
        Object res = handleGetObject(key);
        return (res == null) ? null : ResourceAccessor.getUInt(res);
    }

    public byte[] getByteArray(String key) {
        Object res = handleGetObject(key);
        return (res == null) ? null : ResourceAccessor.getByteArray(res);
    }

    public ByteBuffer getByteBuffer(String key) {
        Object res = handleGetObject(key);
        return (res == null) ? null : ResourceAccessor.getByteBuffer(res);
    }

    public int[] getIntArray(String key) {
        Object res = handleGetObject(key);
        return (res == null) ? null : ResourceAccessor.getIntArray(res);
    }

    public IntBuffer getIntBuffer(String key) {
        Object res = handleGetObject(key);
        return (res == null) ? null : ResourceAccessor.getIntBuffer(res);
    }

    public ResourceArray getResourceArray(String key) {
        Object res = handleGetObject(key);
        return (res == null) ? null : ResourceAccessor.getResourceArray(res);
    }

    public ResourceTable getResourceTable(String key) {
        Object res = handleGetObject(key);
        return (res == null) ? null : ResourceAccessor.getResourceTable(res);
    }

    public ResourceAlias getResourceAlias(String key) {
        Object res = handleGetObject(key);
        return (res == null) ? null : ResourceAccessor.getResourceAlias(res);
    }

    protected abstract Object handleGetObject(String key);
}
