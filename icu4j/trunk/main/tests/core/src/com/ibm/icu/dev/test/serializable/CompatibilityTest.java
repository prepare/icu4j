/*
 *******************************************************************************
 * Copyright (C) 1996-2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 *
 */

package com.ibm.icu.dev.test.serializable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.ibm.icu.dev.test.TestFmwk;

/**
 * @author emader
 */
public class CompatibilityTest extends TestFmwk
{
    public class FolderTarget extends Target
    {
        private Target head = new Target(null);
        private Target tail = head;
        
        public FolderTarget(String name)
        {
            super(name);
        }
        
        public void add(String className, InputStream is)
        {
            HandlerTarget newTarget = new HandlerTarget(className, is);
            
            tail.setNext(newTarget);
            tail = newTarget;
        }
        
        protected boolean validate()
        {
            return true;
        }
        
        protected void execute() throws Exception
        {
            params.indentLevel += 1;
            
            for (Target target = head.getNext(); target != null; target = target.getNext())
            {
                target.run();
            }
            
            params.indentLevel -= 1;
        }
    }

    public class HandlerTarget extends Target
    {
        protected SerializableTest.Handler handler = null;
        protected InputStream inputStream = null;
        
        public HandlerTarget(String name, InputStream is)
        {
            super(name);
            inputStream = is;
        }
        
        protected boolean validate()
        {
            handler = SerializableTest.getHandler(name);
            
            return handler != null;
        }
        
        protected void execute() throws Exception
        {
            try {
                if (params.inDocMode()) {
                    // nothing to execute
                } else if (!params.stack.included) {
                    ++params.invalidCount;
                } else {
                    params.testCount += 1;

                    try {
                        ObjectInputStream in = new ObjectInputStream(inputStream);
                        Object inputObjects[] = (Object[]) in.readObject();
                        Object testObjects[] = handler.getTestObjects();

                        in.close();
                        inputStream.close();

                        // TODO: add equality test...
                        // The commented out code below does that,
                        // but some test objects don't define an equals() method,
                        // and the default method is the same as the "==" operator...
                        for (int i = 0; i < testObjects.length; i += 1) {
                            // if (! inputObjects[i].equals(testObjects[i])) {
                            // errln("Input object " + i + " failed equality test.");
                            // }

                            if (!handler.hasSameBehavior(inputObjects[i], testObjects[i])) {
                                warnln("Input object " + i + " failed behavior test.");
                            }
                        }
                    } catch (MissingResourceException e) {
                        warnln("Could not load the data. " + e.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                        errln("Exception: " + e.toString());
                    }
                }
            } finally {
                inputStream.close();
                inputStream = null;
            }
        }
    }

    private static final String[][] SKIP_CASES = {
        // ICU 52+ PluralRules/PluralFormat/CurrencyPluralInfo are not
        // serialization-compatible with previous versions. 
        {"ICU_50.1", "com.ibm.icu.text.CurrencyPluralInfo.dat"},
        {"ICU_51.1", "com.ibm.icu.text.CurrencyPluralInfo.dat"},

        {"ICU_50.1", "com.ibm.icu.text.PluralFormat.dat"},
        {"ICU_51.1", "com.ibm.icu.text.PluralFormat.dat"},

        {"ICU_50.1", "com.ibm.icu.text.PluralRules.dat"},
        {"ICU_51.1", "com.ibm.icu.text.PluralRules.dat"},
        
        // GeneralMeasureFormat was in technical preview, but is going away after ICU 52.1.
        {"ICU_52.1", "com.ibm.icu.text.GeneralMeasureFormat.dat"},

        // RuleBasedNumberFormat
        {"ICU_3.6",     "com.ibm.icu.text.RuleBasedNumberFormat.dat"},

        // ICU 4.8+ MessageFormat is not serialization-compatible with previous versions.
        {"ICU_3.6",     "com.ibm.icu.text.MessageFormat.dat"},
    };

    private Target getFileTargets(URL fileURL)
    {
        File topDir = new File(fileURL.getPath());
        File dataDirs[] = topDir.listFiles();
        FolderTarget target = null;
        
        for (int d = 0; d < dataDirs.length; d += 1) {
            File dataDir = dataDirs[d];
            
            if (dataDir.isDirectory()) {
                FolderTarget newTarget = new FolderTarget(dataDir.getName());
                File files[] = dataDir.listFiles();

                newTarget.setNext(target);
                target = newTarget;

                String dataDirName = dataDir.getName();

                element_loop:
                for (int i = 0; i < files.length; i += 1) {
                    File file = files[i];
                    String filename = file.getName();
                    int ix = filename.indexOf(".dat");

                    if (ix > 0) {
                        String className = filename.substring(0, ix);

                        // Skip some cases which do not work well
                        for (int j = 0; j < SKIP_CASES.length; j++) {
                            if (dataDirName.equals(SKIP_CASES[j][0]) && filename.equals(SKIP_CASES[j][1])) {
                                logln("Skipping test case - " + dataDirName + "/" + className);
                                continue element_loop;
                            }
                        }

                        try {
                            @SuppressWarnings("resource")  // Closed by HandlerTarget.execute().
                            InputStream is = new FileInputStream(file);
                            target.add(className, is);
                        } catch (FileNotFoundException e) {
                            errln("Exception: " + e.toString());
                        }
                        
                    }
                }
            }
        }
            
        return target;
    }

    private static InputStream copyInputStream(InputStream in) throws IOException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            while (true) {
                int r = in.read(buf);
                if (r == -1) {
                    break;
                }
                out.write(buf, 0, r);
            }
            return new ByteArrayInputStream(out.toByteArray());
        } finally {
            in.close();
        }
    }

    private Target getJarTargets(URL jarURL)
    {
        String prefix = jarURL.getPath();
        String currentDir = null;
        int ix = prefix.indexOf("!/");
        FolderTarget target = null;

        if (ix >= 0) {
            prefix = prefix.substring(ix + 2);
        }

        try {
            JarURLConnection conn = (JarURLConnection) jarURL.openConnection();
            JarFile jarFile = conn.getJarFile();
            try {
                Enumeration entries = jarFile.entries();
element_loop:
                while (entries.hasMoreElements()) {
                    JarEntry entry = (JarEntry) entries.nextElement();
                    String name = entry.getName();

                    if (name.startsWith(prefix)) {
                        name = name.substring(prefix.length());

                        if (!entry.isDirectory()) {
                            int dx = name.lastIndexOf("/");
                            String dirName = name.substring(1, dx);
                            String filename = name.substring(dx + 1);

                            if (!dirName.equals(currentDir)) {
                                currentDir = dirName;

                                FolderTarget newTarget = new FolderTarget(currentDir);

                                newTarget.setNext(target);
                                target = newTarget;
                            }

                            int xx = filename.indexOf(".dat");

                            if (xx > 0) {
                                String className = filename.substring(0, xx);

                                // Skip some cases which do not work well
                                for (int i = 0; i < SKIP_CASES.length; i++) {
                                    if (dirName.equals(SKIP_CASES[i][0]) && filename.equals(SKIP_CASES[i][1])) {
                                        logln("Skipping test case - " + dirName + "/" + className);
                                        continue element_loop;
                                    }
                                }

                                // The InputStream object returned by JarFile.getInputStream() will
                                // no longer be useable after JarFile.close() has been called. It's
                                // therefore necessary to make a copy of it here.
                                target.add(className, copyInputStream(jarFile.getInputStream(entry)));
                            }
                        }
                    }
                }
            } finally {
                jarFile.close();
            }
        } catch (Exception e) {
            errln("jar error: " + e.getMessage());
        }

        return target;
    }
    
    protected Target getTargets(String targetName)
    {
        URL dataURL = getClass().getResource("data");
        String protocol = dataURL.getProtocol();
        
        if (protocol.equals("jar")) {
            return getJarTargets(dataURL);
        } else if (protocol.equals("file")) {
            return getFileTargets(dataURL);
        } else {
            errln("Don't know how to test " + dataURL);
            return null;
        }
    }

    public static void main(String[] args)
    {
        CompatibilityTest test = new CompatibilityTest();
        
        test.run(args);
    }
}
