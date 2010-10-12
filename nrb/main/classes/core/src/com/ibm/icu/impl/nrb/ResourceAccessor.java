package com.ibm.icu.impl.nrb;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.ibm.icu.impl.nrb.ResourceConstants.Type;

public final class ResourceAccessor {

    public static Type getType(Object res) {
        if (res instanceof String) {
            return Type.STRING;
        } else if (res instanceof Integer) {
            return Type.INTEGER;
        } else if (res instanceof int[]) {
            return Type.INTEGER_ARRAY;
        } else if (res instanceof byte[]) {
            return Type.BYTE_ARRAY;
        } else if (res instanceof ResourceTable) {
            return Type.TABLE;
        } else if (res instanceof ResourceArray) {
            return Type.OBJECT_ARRAY;
        } else if (res instanceof ResourceAlias) {
            return Type.ALIAS;
        }
        return Type.UNKNOWN;
    }

    public static String getString(Object res) {
        if (!(res instanceof String)) {
            throw new ClassCastException("Not a string resource");
        }
        return (String)res;
    }

    public static String[] getStringArray(Object res) {
        String[] strarray = null;
        if (res instanceof ResourceArray) {
            ResourceArray array = (ResourceArray)res;
            boolean bStrings = true;
            for (int i = 0; i < array.size(); i++) {
                if (!(array.getObject(i) instanceof String)) {
                    bStrings = false;
                    break;
                }
            }
            if (bStrings) {
                // Create String[]
                strarray = new String[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    strarray[i] = (String)array.getObject(i);
                }
            }
        }
        if (strarray == null) {
            throw new ClassCastException("Not a string array resource");
        }

        return strarray;
    }

    public static int getInt(Object res) {
        if (!(res instanceof Integer)) {
            throw new ClassCastException("Not an int resource");
        }
        // TODO
        return ((Integer)res).intValue();
    }

    public static int getUInt(Object res) {
        if (!(res instanceof Integer)) {
            throw new ClassCastException("Not an integer resource");
        }
        // TODO
        return ((Integer)res).intValue();
    }

    public static IntBuffer getIntBuffer(Object res) {
        if (!(res instanceof int[])) {
            throw new ClassCastException("Not an integer vector resource");
        }
        return IntBuffer.wrap((int[])res).asReadOnlyBuffer();
    }

    public static int[] getIntArray(Object res) {
        if (!(res instanceof int[])) {
            throw new ClassCastException("Not an integer vector resource");
        }
        return (int[])((int[])res).clone();
    }

    public static ByteBuffer getByteBuffer(Object res) {
        if (!(res instanceof byte[])) {
            throw new ClassCastException("Not a binary resource");
        }
        return ByteBuffer.wrap((byte[])res).asReadOnlyBuffer();
    }

    public static byte[] getByteArray(Object res) {
        if (!(res instanceof byte[])) {
            throw new ClassCastException("Not a binary resource");
        }
        return (byte[])((byte[])res).clone();
    }

    public static ResourceTable getResourceTable(Object res) {
        if (!(res instanceof ResourceTable)) {
            throw new ClassCastException("Not a table resource");
        }
        return (ResourceTable)res;
    }

    public static ResourceArray getResourceArray(Object res) {
        if (!(res instanceof ResourceArray)) {
            throw new ClassCastException("Not a table resource");
        }
        return (ResourceArray)res;
    }

    public static ResourceAlias getResourceAlias(Object res) {
        if (!(res instanceof ResourceAlias)) {
            throw new ClassCastException("Not a resource alias");
        }
        return (ResourceAlias)res;
    }
}
