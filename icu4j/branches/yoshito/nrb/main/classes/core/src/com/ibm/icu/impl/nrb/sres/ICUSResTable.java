package com.ibm.icu.impl.nrb.sres;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.ibm.icu.impl.nrb.ResourceTable;

public class ICUSResTable extends ResourceTable {
    private Object[][] _resources;
    private Map<String, Object> _keyMap;

    public ICUSResTable(Object[][] resources) {
        _resources = resources;
    }

    @Override
    public Set<String> keySet() {
        return Collections.unmodifiableSet(getKeyMap().keySet());
    }

    @Override
    protected Object handleGetObject(String key) {
        Object res = getKeyMap().get(key);
        if (res != null && res instanceof AuxiliaryResource) {
            // TODO
        }
        return res;
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
        Object res = _resources[idx][1];
        if (res instanceof AuxiliaryResource) {
            // TODO
        }
        return res;
    }

    private synchronized Map<String, Object> getKeyMap() {
        if (_keyMap == null) {
            _keyMap = new HashMap<String, Object>(_resources.length);
            for (Object[] entry : _resources) {
                _keyMap.put((String) entry[0], entry[1]);
            }
        }
        return _keyMap;
    }
}
