package com.ibm.icu.impl.nrb.res;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.ibm.icu.impl.nrb.ResourceTable;
import com.ibm.icu.impl.nrb.res.ICUResReader.Container;
import com.ibm.icu.impl.nrb.res.ICUResReader.Table;

public class ICUResTable extends ResourceTable {

    private static final Map<String, ICUResTable> CACHE = new HashMap<String, ICUResTable>();
    private static final Map<String, ICUResReader> POOLCACHE = new HashMap<String, ICUResReader>();

    private volatile Map<Object, Object> _lookup;

    private Table _table;

    ICUResTable(Table table) {
        _table = table;
    }

    public static ICUResTable load(String base, String name, ClassLoader root) {
        String resKey = Integer.toHexString(root.hashCode()) + base + name;

        ICUResTable resTable = CACHE.get(resKey);
        if (resTable == null) {
            String resolvedName = getFullName(base, name);
            ICUResReader reader = ICUResReader.getReader(resolvedName, root);
            if (reader == null) {
                return null;
            }

            int rootRes = reader.getRootResource();
            int rootType = ICUResReader.RES_GET_TYPE(rootRes);
            if (!(rootType == ICUResConstants.TABLE || rootType == ICUResConstants.TABLE16 || rootType == ICUResConstants.TABLE32)) {
                return null;
            }

            if (reader.getUsesPoolBundle()) {
                String poolName = getFullName(base, "pool");
                ICUResReader poolReader = POOLCACHE.get(poolName);
                if (poolReader == null) {
                    poolReader = ICUResReader.getReader(poolName, root);
                    if (poolReader == null) {
                        return null;
                    }
                    POOLCACHE.put(poolName, poolReader);
                }
                reader.setPoolBundleKeys(poolReader);
            }

            Table t = reader.getTable(rootRes);
            resTable = new ICUResTable(t);

            String topAlias = resTable.getString("%%ALIAS");
            if (topAlias != null) {
                resTable = load(base, topAlias, root);
            }

            if (resTable != null) {
                CACHE.put(resKey, resTable);
            }
        }

        return resTable;
    }

    private static String getFullName(String base, String name) {
        if (base == null || base.length() == 0) {
            if (name.length() == 0) {
                return "root.res";
            } else {
                return name + ".res";
            }
        } else {
            if (base.indexOf('.') == -1) {
                if (base.charAt(base.length() - 1) != '/') {
                    return base + "/" + name + ".res";
                } else {
                    return base + name + ".res";
                }
            } else {
                base = base.replace('.', '/');
                if (name.length() == 0) {
                    return base + ".res";
                } else {
                    return base + "_" + name + ".res";
                }
            }
        }
    }

    @Override
    public Set<String> keySet() {
        // TODO - cache?
        TreeSet<String> keySet = new TreeSet<String>();
        for (int i = 0; i < _table.getSize(); i++) {
            keySet.add(_table.getKey(i));
        }
        return keySet;
    }

    @Override
    protected Object handleGetObject(String key) {
        Object resobj = findInCache(key);
        if (resobj != null) {
            return resobj;
        }

        int idx = _table.findTableItem(key);
        if (idx < 0) {
            return null;
        }

        resobj = readResource(idx);

        if (resobj != null) {
            addToCache(key, Integer.valueOf(idx), resobj);
        }

        return resobj;
    }

    @Override
    public int size() {
        return _table.getSize();
    }

    @Override
    protected Object handleGetObject(int idx) {
        Integer intKey = Integer.valueOf(idx);
        Object resobj = findInCache(intKey);
        if (resobj != null) {
            return resobj;
        }

        resobj = readResource(idx);

        if (resobj != null) {
            addToCache(_table.getKey(idx), intKey, resobj);
        }

        return resobj;
    }

    private Object readResource(int idx) {
        int item = _table.getContainerResource(idx);
        if (item == ICUResConstants.RES_BOGUS) {
            return null;
        }

        Object resobj = null;
        int type = ICUResReader.RES_GET_TYPE(item);

        switch (type) {
        case ICUResConstants.STRING:
        case ICUResConstants.STRING_V2:
            resobj = _table.getReader().getString(item);
            break;
        case ICUResConstants.BINARY:
            resobj = _table.getReader().getBinary(item, null);
            break;
        case ICUResConstants.ALIAS:
            resobj = _table.getReader().getAlias(item);
            break;
        case ICUResConstants.INT:
            resobj = Integer.valueOf(ICUResReader.RES_GET_INT(item));
            break;
        case ICUResConstants.INT_VECTOR:
            resobj = _table.getReader().getIntVector(item);
            break;
        case ICUResConstants.ARRAY:
        case ICUResConstants.ARRAY16:
        {
            Container array = _table.getReader().getArray(item);
            resobj = new ICUResArray(array);
            break;
        }
        case ICUResConstants.TABLE:
        case ICUResConstants.TABLE16:
        case ICUResConstants.TABLE32:
        {
            Table table = _table.getReader().getTable(item);
            resobj = new ICUResTable(table);
            break;
        }
        default:
            // TODO - error handling?
            break;
        }

        return resobj;
    }

    private Object findInCache(String key) {
        if (_lookup == null) {
            return null;
        }
        return _lookup.get(key);
    }

    private Object findInCache(Integer idx) {
        if (_lookup == null) {
            return null;
        }
        return _lookup.get(idx);
    }

    private void addToCache(String key, Integer idx, Object res) {
        if (_lookup == null) {
            synchronized(this) {
                if (_lookup == null) {
                    _lookup = new HashMap<Object, Object>();
                }
            }
        }
        synchronized(_lookup) {
            if (!_lookup.containsKey(key)) {
                _lookup.put(key, res);
                _lookup.put(idx, res);
            }
        }
    }
}
