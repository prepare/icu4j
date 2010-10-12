package com.ibm.icu.impl.nrb.sres;

import com.ibm.icu.impl.nrb.ResourceArray;

public class ICUSResArray extends ResourceArray {
    private Object[] _resources;

    public ICUSResArray(Object[] resources) {
        _resources = resources;
    }

    @Override
    public int size() {
        return _resources.length;
    }

    @Override
    protected Object handleGetObject(int idx) {
        if (idx < 0 || idx >= _resources.length) {
            return null;
        }
        Object res = _resources[idx];
        if (res instanceof AuxiliaryResource) {
            // TODO
        }
        return res;
    }

    
}
