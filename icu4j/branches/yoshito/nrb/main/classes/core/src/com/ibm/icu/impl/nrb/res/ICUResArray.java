package com.ibm.icu.impl.nrb.res;

import com.ibm.icu.impl.nrb.ResourceArray;
import com.ibm.icu.impl.nrb.res.ICUResReader.Container;
import com.ibm.icu.impl.nrb.res.ICUResReader.Table;

public class ICUResArray extends ResourceArray {
    private Container _array;
    private volatile Object[] _resources;

    ICUResArray(Container array) {
        _array = array;
    }

    @Override
    public int size() {
        return _array.getSize();
    }

    @Override
    protected Object handleGetObject(int idx) {
        if (idx < 0 || idx >= _array.getSize()) {
            return null;
        }

        if (_resources == null) {
            synchronized(this) {
                if (_resources == null) {
                    _resources = new Object[_array.getSize()];
                }
            }
        }

        Object resobj = _resources[idx];
        if (resobj == null) {
            resobj = readResource(idx);
            if (resobj != null) {
                synchronized (_resources) {
                    if (_resources[idx] != null) {
                        resobj = _resources[idx];
                    } else {
                        _resources[idx] = resobj;
                    }
                }
            }
        }

        return resobj;
    }

    // TODO - share this code with ICUResTable
    private Object readResource(int idx) {
        int item = _array.getContainerResource(idx);
        if (item == ICUResConstants.RES_BOGUS) {
            return null;
        }

        Object resobj = null;
        int type = ICUResReader.RES_GET_TYPE(item);

        switch (type) {
        case ICUResConstants.STRING:
        case ICUResConstants.STRING_V2:
            resobj = _array.getReader().getString(item);
            break;
        case ICUResConstants.BINARY:
            resobj = _array.getReader().getBinary(item, null);
            break;
        case ICUResConstants.ALIAS:
            resobj = _array.getReader().getAlias(item);
            break;
        case ICUResConstants.INT:
            resobj = Integer.valueOf(ICUResReader.RES_GET_INT(item));
            break;
        case ICUResConstants.INT_VECTOR:
            resobj = _array.getReader().getIntVector(item);
            break;
        case ICUResConstants.ARRAY:
        case ICUResConstants.ARRAY16:
        {
            Container array = _array.getReader().getArray(item);
            resobj = new ICUResArray(array);
            break;
        }
        case ICUResConstants.TABLE:
        case ICUResConstants.TABLE16:
        case ICUResConstants.TABLE32:
        {
            Table table = _array.getReader().getTable(item);
            resobj = new ICUResTable(table);
            break;
        }
        default:
            // TODO - error handling?
            break;
        }

        return resobj;
    }

}
