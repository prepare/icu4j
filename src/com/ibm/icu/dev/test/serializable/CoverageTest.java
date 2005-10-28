/*
 *******************************************************************************
 * Copyright (C) 1996-2005, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 */

package com.ibm.icu.dev.test.serializable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.Policy;

import com.ibm.icu.dev.test.TestFmwk.Target;
import com.ibm.icu.dev.test.serializable.CompatibilityTest.HandlerTarget;
import com.ibm.icu.impl.URLHandler;

/**
 * @author emader
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CoverageTest extends CompatibilityTest implements URLHandler.URLVisitor
{

    private static Class serializable;

    static {
        try {    
            serializable = Class.forName("java.io.Serializable");
        } catch (Exception e) {
            // we're in deep trouble...
            System.out.println("Woops! Can't get class info for Serializable.");
        }
    }

    private Target head = new Target(null);
    private Target tail = head;
    
    private String path;
    
    public CoverageTest()
    {
        this(null);
    }
    
    public CoverageTest(String path)
    {
        this.path = path;
        
        if (path != null) {
            File dir = new File(path);
            
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
    }
    
    private void writeFile(String className, byte bytes[])
    {
        File file = new File(path + File.separator + className + ".dat");
        FileOutputStream stream;
        
        try {
            stream = new FileOutputStream(file);
            
            stream.write(bytes);
            stream.close();
        } catch (Exception e) {
            System.out.print(" - can't write file!");
        }
    }
    
    private void add(String className, int classModifiers, byte bytes[])
    {
        CoverageTarget newTarget = new CoverageTarget(className, classModifiers, bytes);
        
        tail.setNext(newTarget);
        tail = newTarget;
    }
    
    public class CoverageTarget extends HandlerTarget
    {
        private byte bytes[];
        private int modifiers;
        
        public CoverageTarget(String className, int classModifiers, byte bytes[])
        {
            super(className, bytes == null? null : new ByteArrayInputStream(bytes));
            
            this.bytes = bytes;
            modifiers = classModifiers;
        }
        
        public boolean validate()
        {
            return super.validate() || Modifier.isAbstract(modifiers);
        }
        
        public void execute() throws Exception
        {
            if (inputStream == null) {
                params.testCount += 1;
            } else {
                Class c = Class.forName(name);
                try {
                    Field uid = c.getDeclaredField("serialVersionUID");
                } catch (Exception e) {
                    errln("No serialVersionUID");
                }
                
                if (path != null) {
                    writeFile(name, bytes);
                }
                
                super.execute();
            }
        }
    }
    
    public void visit(String str)
    {
        int ix = str.lastIndexOf(".class");
        
        if (ix >= 0) {
            String className = "com.ibm.icu" + str.substring(0, ix).replace('/', '.');
            
            // Skip things in com.ibm.icu.dev; they're not relevant.
            if (className.startsWith("com.ibm.icu.dev.")) {
                return;
            }
            
            try {
                Class c = Class.forName(className);
                int   m = c.getModifiers();
                
                if (serializable.isAssignableFrom(c)) {
                    if (Modifier.isPublic(m)) {
                        SerializableTest.Handler handler = SerializableTest.getHandler(className);
                        
                        if (handler != null) {
                            Object objectsOut[] = handler.getTestObjects();
                            Object objectsIn[];
                            boolean passed = true;
                            
                            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                            ObjectOutputStream out = new ObjectOutputStream(byteOut);
                            
                            try {
                                out.writeObject(objectsOut);
                                out.close();
                                byteOut.close();
                            } catch (IOException e) {
                                System.out.println("Eror writing test objects: " + e.toString());
                                return;
                            }
                            
                            add(className, m, byteOut.toByteArray());
                        } else {
                            add(className, m, null);
                        }
                    }
                }
           } catch (Exception e) {
                System.out.println("Error processing " + className + ": " + e.toString());
            }
        }
    }
    
    protected Target getTargets(String targetName)
    {
        if (System.getSecurityManager() != null) {
            // This test won't run under a security manager
            return new CoverageTarget("Skipped Due To Security Manager", Modifier.ABSTRACT, null);
        }
        
        URL url = getClass().getResource("/com/ibm/icu");
        URLHandler handler  = URLHandler.get(url);
        
        handler.guide(this, true, false);
        
        return head.getNext();
    }
    
    public static void main(String[] args)
    {
        CoverageTest test = new CoverageTest();
        
        test.run(args);
    }
}
