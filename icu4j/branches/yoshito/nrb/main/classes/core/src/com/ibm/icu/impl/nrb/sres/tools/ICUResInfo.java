package com.ibm.icu.impl.nrb.sres.tools;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import com.ibm.icu.impl.URLHandler;
import com.ibm.icu.impl.URLHandler.URLVisitor;
import com.ibm.icu.impl.nrb.ResourceArray;
import com.ibm.icu.impl.nrb.ResourceConstants.Type;
import com.ibm.icu.impl.nrb.ResourceTable;
import com.ibm.icu.impl.nrb.res.ICUResTable;

public class ICUResInfo {
    private String _baseName;
    private ClassLoader _loader = this.getClass().getClassLoader();

    public ICUResInfo(String baseName) {
        _baseName = baseName;
    }

    public List<String> getLocaleResourceNames() {
        return getResourceNames(true);
    }

    public List<String> getNonLocaleResourceNames() {
        return getResourceNames(false);
    }

    public SortedSet<String> getCommonKeys(List<String> names, int minCount) {
        Map<String, Integer> keyCounts = new HashMap<String, Integer>();
        for (String name : names) {
            ResourceTable table = ICUResTable.load(_baseName, name, _loader);
            if (table == null) {
                // TODO - logging
                continue;
            }
            addKeys(keyCounts, table);
        }
        SortedSet<String> keys = new TreeSet<String>();
        for (Entry<String, Integer> entry : keyCounts.entrySet()) {
            Integer cnt = entry.getValue();
            if (cnt >= minCount) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    private void addKeys(Map<String, Integer> keyCounts, ResourceTable table) {
        for (String key : table.keySet()) {
            Integer cnt = keyCounts.get(key);
            if (cnt == null) {
                keyCounts.put(key, Integer.valueOf(1));
            } else {
                keyCounts.put(key, Integer.valueOf(cnt + 1));
            }
            Type t = table.getType(key);
            if (t == Type.TABLE) {
                addKeys(keyCounts, table.getResourceTable(key));
            } else if (t == Type.OBJECT_ARRAY) {
                addKeys(keyCounts, table.getResourceArray(key));
            }
        }
    }

    private void addKeys(Map<String, Integer> keyCounts, ResourceArray array) {
        for (int i = 0; i < array.size(); i++) {
            Type t = array.getType(i);
            if (t == Type.TABLE) {
                addKeys(keyCounts, array.getResourceTable(i));
            } else if (t == Type.OBJECT_ARRAY) {
                addKeys(keyCounts, array.getResourceArray(i));
            }
        }
    }

    private List<String> getResourceNames(boolean localeResources) {
        TreeSet<String> names = null;
        try {
            Enumeration<URL> urls = _loader.getResources(_baseName);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                URLHandler handler = URLHandler.get(url);
                if (handler != null) {
                    final List<String> list = new ArrayList<String>();
                    final boolean localeRes = localeResources;
                    URLVisitor v = new URLVisitor() {
                        public void visit(String s) {
                            String name = localeRes ? getLocaleResourceName(s) : getNonLocaleResourceName(s);
                            if (name != null) {
                                list.add(name);
                            }
                        }
                    };
                    handler.guide(v, false);

                    if (names == null) {
                        names = new TreeSet<String>(list);
                    } else {
                        names.addAll(list);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<String>(names);
    }

    private static String getLocaleResourceName(String fileName) {
        if (!fileName.endsWith(".res")) {
            return null;
        }
        String name = fileName.substring(0, fileName.length() - 4);
        if (isLocaleString(name)) {
            return name;
        }
        return null;
    }

    private static String getNonLocaleResourceName(String fileName) {
        if (!fileName.endsWith(".res")) {
            return null;
        }
        String name = fileName.substring(0, fileName.length() - 4);
        if (!isLocaleString(name) && !name.equals("pool")) {
            return name;
        }
        return null;
    }

    private static boolean isLocaleString(String str) {
        if (str.equals("root")) {
            return true;
        }
        if (str.length() == 2 || str.length() == 3) {
            // two/three letter name is always locale resource
            return true;
        }
        int index = str.indexOf("_");
        if (index == 2 || index == 3) {
            // locale resource may have separator "_" at the index 2 or 3
            return true;
        }
        return false;
    }
}
