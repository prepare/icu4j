package com.ibm.icu.impl.nrb.sres;

import java.lang.ref.SoftReference;

import com.ibm.icu.impl.nrb.sres.ICUSResReader.BundleInfo;

public class AuxiliaryResource {
    private BundleInfo _bundleInfo;
    private int _serial;

    private SoftReference<Object> _ref;

    public AuxiliaryResource(BundleInfo bundleInfo, int serial) {
        _bundleInfo = bundleInfo;
        _serial = serial;
    }

    public synchronized Object getResource() {
        Object res = null;
        if (_ref != null) {
            res = _ref.get();
        }
        if (res == null) {
            res = ICUSResReader.loadResource(_bundleInfo.getBase(), _bundleInfo.getName(),
                    Integer.valueOf(_serial), _bundleInfo.getLoader());
            _ref = new SoftReference<Object>(res);
        }
        return res;
    }
}
