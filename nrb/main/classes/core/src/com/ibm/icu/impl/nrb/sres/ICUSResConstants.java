package com.ibm.icu.impl.nrb.sres;

public class ICUSResConstants {
    public static final byte STRING = (byte)0x01;

    public static final byte INT16 = (byte)0x02;
    public static final byte INT32 = (byte)0x03;

    public static final byte BYTE_ARRAY = (byte)0x10;
    public static final byte INT16_ARRAY = (byte)0x11;
    public static final byte INT32_ARRAY = (byte)0x12;

    public static final byte STRING_ARRAY = (byte)0x20;
    public static final byte OBJECT_ARRAY = (byte)0x21;

    public static final byte TABLE = (byte)0x30;

    public static final byte ALIAS = (byte)0x40;

    public static final byte AUX = (byte)0x50;

    public static final byte KEY_POOL = (byte)0xff;
    public static final byte STRING_POOL_UTF8 = (byte)0xfe;
    public static final byte STRING_POOL_UTF16BE = (byte)0xfd;
    public static final byte RESOURCE_DATA = (byte)0xfc;


    // resource type
    public static final int IS_SHARED_KEY_RESOURCE = 0x00000001;
    public static final int USE_SHARED_KEY_RESOURCE = 0x00000002;
}
