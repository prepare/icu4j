package com.ibm.icu.impl.nrb;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.ibm.icu.impl.nrb.ResourceConstants.Type;

public abstract class ResourceArray {
    public abstract int size();

    public Type getType(int idx) {
        Object res = handleGetObject(idx);
        return (res == null) ? Type.NONE : ResourceAccessor.getType(res);
    }

    public Object getObject(int idx) {
        return handleGetObject(idx);
    }

    public String getString(int idx) {
        Object res = handleGetObject(idx);
        return (res == null) ? null : ResourceAccessor.getString(res);
    }

    public String[] getStringArray(int idx) {
        Object res = handleGetObject(idx);
        return (res == null) ? null : ResourceAccessor.getStringArray(res);
    }

    public int getInt(int idx) {
        Object res = handleGetObject(idx);
        return (res == null) ? null : ResourceAccessor.getInt(res);
    }

    public int getUInt(int idx) {
        Object res = handleGetObject(idx);
        return (res == null) ? null : ResourceAccessor.getUInt(res);
    }

    public byte[] getByteArray(int idx) {
        Object res = handleGetObject(idx);
        return (res == null) ? null : ResourceAccessor.getByteArray(res);
    }

    public ByteBuffer getByteBuffer(int idx) {
        Object res = handleGetObject(idx);
        return (res == null) ? null : ResourceAccessor.getByteBuffer(res);
    }

    public int[] getIntArray(int idx) {
        Object res = handleGetObject(idx);
        return (res == null) ? null : ResourceAccessor.getIntArray(res);
    }

    public IntBuffer getIntBuffer(int idx) {
        Object res = handleGetObject(idx);
        return (res == null) ? null : ResourceAccessor.getIntBuffer(res);
    }

    public ResourceArray getResourceArray(int idx) {
        Object res = handleGetObject(idx);
        return (res == null) ? null : ResourceAccessor.getResourceArray(res);
    }

    public ResourceTable getResourceTable(int idx) {
        Object res = handleGetObject(idx);
        return (res == null) ? null : ResourceAccessor.getResourceTable(res);
    }

    public ResourceAlias getResourceAlias(int idx) {
        Object res = handleGetObject(idx);
        return (res == null) ? null : ResourceAccessor.getResourceAlias(res);
    }

    protected abstract Object handleGetObject(int idx);
}
