package com.ibm.icu.impl.nrb.sres.tools;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;

import com.ibm.icu.impl.nrb.ResourceArray;
import com.ibm.icu.impl.nrb.ResourceTable;
import com.ibm.icu.impl.nrb.res.ICUResTable;
import com.ibm.icu.impl.nrb.sres.tools.SerializedResWriter.Filter;

public class ICUResToSResConverter {

    static final List<Filter> AUXFILTERS = new LinkedList<Filter>();
    static {
        AUXFILTERS.add(new AuxResourceFilter());
    }

    public static void main(String[] args) {
        File outRoot = new File("C:\\tmp\\sres\\com\\ibm\\icu\\impl\\data\\icudt45");
        String dataRoot = "com/ibm/icu/impl/data/icudt45b";

        String[] dataPath = {"", "coll", "curr", "lang", "region", "zone", "brkitr", "translit", "rbnf"};

        for (String path : dataPath) {
            File outDir = outRoot;
            String baseName = dataRoot;
            if (path.length() > 0) {
                baseName = baseName + "/" + path;
                outDir = new File(outRoot, path);
            }
            outDir.mkdir();
            convert(baseName, outDir);
        }
    }

    private static void convert(String baseName, File outputDir) {
        ICUResInfo resldr = new ICUResInfo(baseName);

        // locale resources
        List<String> localeNames = resldr.getLocaleResourceNames();

        // collect shared keys
        SortedSet<String> sharedKeys = resldr.getCommonKeys(localeNames, 2);

        // write out all locale data
        boolean keyPoolDone = false;
        for (String locale : localeNames) {
            ResourceTable root = ICUResTable.load(baseName, locale, ICUResToSResConverter.class.getClassLoader());
            SerializedResWriter sresw = new SerializedResWriter(root, sharedKeys, AUXFILTERS);

            File file = new File(outputDir, locale + ".sres");
            try {
                sresw.write(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!keyPoolDone) {
                File poolFile = new File(outputDir, "pool.sres");
                try {
                    sresw.writeShareKeyResource(poolFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                keyPoolDone = true;
            }
        }

        // non-locale resources
        List<String> names = resldr.getNonLocaleResourceNames();
        for (String name : names) {
            ResourceTable root = ICUResTable.load(baseName, name, ICUResToSResConverter.class.getClassLoader());
            SerializedResWriter sresw = new SerializedResWriter(root, null, AUXFILTERS);

            File file = new File(outputDir, name + ".sres");
            try {
                sresw.write(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class AuxResourceFilter extends Filter {

        @Override
        public boolean externalize(String resPath, Object resource) {
            String[] keys = resPath.split("/");

            // externalize table and array in the root table
            if (keys.length == 2 && resource instanceof ResourceArray) {
                return true;
            }
            return false;
        }
        
    }
}
