/*
******************************************************************************
* Copyright (C) 2004-2005, International Business Machines Corporation and        *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

package com.ibm.icu.impl;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Vector;

import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.UResourceBundle;

/**
 * just a wrapper for Java ListResourceBundles and 
 * @author ram
 *
 */
public class ResourceBundleWrapper extends UResourceBundle {
    private ResourceBundle bundle = null;
    private String localeID = null;
    private String baseName = null;
    private Vector keys=null;
    private int loadingStatus = -1;    
    
    private ResourceBundleWrapper(ResourceBundle bundle){
        this.bundle=bundle;
    }

    protected void setLoadingStatus(int newStatus){
        loadingStatus = newStatus;
    }
    
    protected Object handleGetObject(String key){
        ResourceBundleWrapper current = this;
        Object obj = null;
        while(current!=null){
            try{
                obj = current.bundle.getObject(key);
                break;
            }catch(MissingResourceException ex){
                current = (ResourceBundleWrapper)current.getParent();
            }
        }
        if (obj == null){
            throw new MissingResourceException("Can't find resource for bundle "
                                               +baseName
                                               +", key "+key,
                                               this.getClass().getName(),
                                               key);
        }
        return obj;
    }
    
    public Enumeration getKeys(){
        return keys.elements();
    }
    
    private void initKeysVector(){
        ResourceBundleWrapper current = this;
        keys = new Vector();
        while(current!=null){
            Enumeration e = current.bundle.getKeys();
            while(e.hasMoreElements()){
                String elem = (String)e.nextElement();
                if(!keys.contains(elem)){
                    keys.add(elem);
                }
            }
            current = (ResourceBundleWrapper)current.getParent();
        }
    }
    protected String getLocaleID(){
        return localeID;   
    }
 
    protected String getBaseName(){
        return bundle.getClass().getName().replace('.','/');   
    }
    
    public ULocale getULocale(){
        return new ULocale(localeID);   
    }
    
    public UResourceBundle getParent(){
        return (UResourceBundle)parent;   
    }

    // Flag for enabling/disabling debugging code
    private static final boolean DEBUG = ICUDebug.enabled("resourceBundleWrapper");
    
    // This method is for super class's instantiateBundle method
    public static UResourceBundle getBundleInstance(String baseName, String localeID, 
                                                    ClassLoader root, boolean disableFallback){
        UResourceBundle b = instantiateBundle(baseName, localeID, root, disableFallback);
        if(b==null){
            throw new MissingResourceException("Could not find the bundle "+ baseName+"_"+ localeID,"","");
        }
        return b;
    }
    // recursively build bundle and override the super-class method
     protected static synchronized UResourceBundle instantiateBundle(String baseName, String localeID,
                                                                    ClassLoader root, boolean disableFallback) {
        if (root == null) {
            // we're on the bootstrap
            root = ClassLoader.getSystemClassLoader();
        }
        final ClassLoader cl = root;
        String name = baseName;
        ULocale defaultLocale = ULocale.getDefault();
        if (localeID.length() != 0) {
            name = name + "_" + localeID;
        }

        ResourceBundleWrapper b = (ResourceBundleWrapper)loadFromCache(cl, name, defaultLocale);
        if(b==null){
            ResourceBundleWrapper parent = null;
            int i = localeID.lastIndexOf('_');
    
            if (i != -1) {
                String locName = localeID.substring(0, i);
                parent = (ResourceBundleWrapper)loadFromCache(cl, baseName+"_"+locName,defaultLocale);
                if(parent == null){
                    parent = (ResourceBundleWrapper)instantiateBundle(baseName, locName , cl, disableFallback);
                }
            }else if(localeID.length()>0){
                parent = (ResourceBundleWrapper)loadFromCache(cl, baseName,defaultLocale);
                if(parent==null){
                    parent = (ResourceBundleWrapper)instantiateBundle(baseName, "", cl, disableFallback);
                }
            }
            try {
                Class cls = cl.loadClass(name);
                ResourceBundle bx = (ResourceBundle) cls.newInstance();
                b = new ResourceBundleWrapper(bx);
                if (parent != null) {
                    b.setParent(parent);
                }
                b.baseName=baseName;
                b.localeID = localeID;            
    
            } catch (ClassNotFoundException e) {
                
                final String resName = name.replace('.', '/') + ".properties";
                InputStream stream = (InputStream)java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction() {
                        public Object run() {
                            if (cl != null) {
                                return cl.getResourceAsStream(resName);
                            } else {
                                return ClassLoader.getSystemResourceAsStream(resName);
                            }
                        }
                    }
                );
                if (stream != null) {
                    // make sure it is buffered
                    stream = new java.io.BufferedInputStream(stream);
                    try {
                        b = new ResourceBundleWrapper(new PropertyResourceBundle(stream));
                        if (parent != null) {
                            b.setParent(parent);
                        }
                        b.baseName=baseName;
                        b.localeID=localeID;
                    } catch (Exception ex) {
                        // throw away exception
                    } finally {
                        try {
                            stream.close();
                        } catch (Exception ex) {
                            // throw away exception
                        }
                    }
                }
    
                // if a bogus locale is passed then the parent should be
                // the default locale not the root locale!
                if (b==null) {
                    String defaultName = defaultLocale.toString();
                    if (localeID.length()>0 && defaultName.indexOf(localeID) == -1) {
                        b = (ResourceBundleWrapper)loadFromCache(cl,baseName+"_"+defaultName, defaultLocale);
                        if(b==null){
                            b = (ResourceBundleWrapper)instantiateBundle(baseName , defaultName, cl, disableFallback);
                        }
                    }
                }
                // if still could not find the bundle then return the parent
                if(b==null){
                    b=parent;
                }
            } catch (Exception e) {
                if (DEBUG)
                    System.out.println("failure");
                if (DEBUG)
                    System.out.println(e);
            }

            addToCache(cl, name, defaultLocale, b);
        }
        if(b!=null){
            b.initKeysVector();
        }
        return b;
    }
}
