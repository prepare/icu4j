//##header 1132616027000 
/*
 * *****************************************************************************
 * Copyright (C) 2005, International Business Machines Corporation and * others.
 * All Rights Reserved. *
 * *****************************************************************************
 */

package com.ibm.icu.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.Vector;

//#ifndef FOUNDATION
import java.nio.ByteBuffer;
//#else
//##import com.ibm.icu.impl.ByteBuffer;
//#endif
import com.ibm.icu.impl.URLHandler.URLVisitor;
import com.ibm.icu.util.StringTokenizer;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.UResourceBundle;
import com.ibm.icu.util.UResourceTypeMismatchException;
import com.ibm.icu.util.VersionInfo;

public abstract class ICUResourceBundle extends UResourceBundle {
    /**
     * The data path to be used with getBundleInstance API
     * @draft ICU 3.0
     */
    protected static final String ICU_DATA_PATH = "com/ibm/icu/impl/";
    /**
     * The data path to be used with getBundleInstance API
     * @draft ICU 3.0
     */
    public static final String ICU_BUNDLE = "data/icudt" + VersionInfo.ICU_DATA_VERSION;

    /**
     * The base name of ICU data to be used with getBundleInstance API
     * @draft ICU 3.0
     */
    public static final String ICU_BASE_NAME = ICU_DATA_PATH + ICU_BUNDLE;

    /**
     * The base name of collation data to be used with getBundleInstance API
     * @draft ICU 3.0
     */
    public static final String ICU_COLLATION_BASE_NAME = ICU_BASE_NAME + "/coll";

    /**
     * The base name of rbnf data to be used with getBundleInstance API
     * @draft ICU 3.0
     */
    public static final String ICU_RBNF_BASE_NAME = ICU_BASE_NAME + "/rbnf";

    /**
     * The base name of transliterator data to be used with getBundleInstance API
     * @draft ICU 3.0
     */
    public static final String ICU_TRANSLIT_BASE_NAME = ICU_BASE_NAME + "/translit";

    /**
     * The class loader constant to be used with getBundleInstance API
     * @draft ICU 3.0
     */
    public static final ClassLoader ICU_DATA_CLASS_LOADER;
    static {
        ClassLoader loader = ICUData.class.getClassLoader();
        if (loader == null) { // boot class loader
            loader = ClassLoader.getSystemClassLoader();
        }
        ICU_DATA_CLASS_LOADER = loader;
    }

    /**
     * The name of the resource containing the installed locales
     * @draft ICU 3.0
     */
    protected static final String INSTALLED_LOCALES = "InstalledLocales";

    /**
     * Resource type constant for "no resource".
     * @draft ICU 3.0
     */
    public static final int NONE = -1;

    /**
     * Resource type constant for strings.
     * @draft ICU 3.0
     */
    public static final int STRING = 0;

    /**
     * Resource type constant for binary data.
     * @draft ICU 3.0
     */
    public static final int BINARY = 1;

    /**
     * Resource type constant for tables of key-value pairs.
     * @draft ICU 3.0
     */
    public static final int TABLE = 2;

    /**
     * Resource type constant for aliases;
     * internally stores a string which identifies the actual resource
     * storing the data (can be in a different resource bundle).
     * Resolved internally before delivering the actual resource through the API.
     * @draft ICU 3.0
     * @internal
     */
    protected static final int ALIAS = 3;

    /**
     * Internal use only.
     * Alternative resource type constant for tables of key-value pairs.
     * Never returned by getType().
     * @internal
     * @draft ICU 3.0
     */
    protected static final int TABLE32 = 4;

    /**
     * Resource type constant for a single 28-bit integer, interpreted as
     * signed or unsigned by the getInt() function.
     * @see #getInt
     * @draft ICU 3.0
     */
    public static final int INT = 7;

    /**
     * Resource type constant for arrays of resources.
     * @draft ICU 3.0
     */
    public static final int ARRAY = 8;

    /**
     * Resource type constant for vectors of 32-bit integers.
     * @see #getIntVector
     * @draft ICU 3.0
     */
    public static final int INT_VECTOR = 14;

    public static final int FROM_FALLBACK = 1, FROM_ROOT = 2, FROM_DEFAULT = 3, FROM_LOCALE = 4;

    private int loadingStatus = -1;

    public void setLoadingStatus(int newStatus) {
        loadingStatus = newStatus;
    }
    /**
     * Returns the loading status of a particular resource. 
     * 
     * @return FROM_FALLBACK if the resource is fetched from fallback bundle
     *         FROM_ROOT if the resource is fetched from root bundle.
     *         FROM_DEFAULT if the resource is fetched from the default locale.
     */
    public int getLoadingStatus() {
        return loadingStatus;
    }

    /**
     * Get the noFallback flag specified in the loaded bundle.
     * @return The noFallback flag.
     */
    protected boolean getNoFallback() {
        return false;
    }

    /**
     * Return the version number associated with this UResourceBundle as an
     * VersionInfo object.
     * @return VersionInfo object containing the version of the bundle
     * @draft ICU 3.0
     */
    public VersionInfo getVersion() {
        return null;
    }

    /**
     * Returns a string from a string resource type
     *
     * @return a string
     * @see #getBinary
     * @see #getIntVector
     * @see #getInt
     * @throws MissingResourceException
     * @throws UResourceTypeMismatchException
     * @draft ICU 3.0
     */
    public String getString() {
        throw new UResourceTypeMismatchException("");
    }

    /**
     * @internal ICU 3.0
     */
    public String[] getStringArray() {
        throw new UResourceTypeMismatchException("");
    }

    /**
     * Returns a string from a string resource type
     * @param key The key whose values needs to be fetched
     * @return a string
     * @see #getBinary
     * @see #getIntVector
     * @see #getInt
     * @throws MissingResourceException
     * @throws UResourceTypeMismatchException
     * @draft ICU 3.0
     */
    //  public String getString(String key) {
    //      throw new UResourceTypeMismatchException("");
    //  }

    /**
     * Returns a binary data from a binary resource.
     *
     * @return a pointer to a chuck of unsigned bytes which live in a memory mapped/DLL file.
     * @see #getIntVector
     * @see #getInt
     * @throws MissingResourceException
     * @throws UResourceTypeMismatchException
     * @draft ICU 3.0
     */
    public ByteBuffer getBinary() {
        throw new UResourceTypeMismatchException("");
    }

    /**
     * Returns a 32 bit integer array from a resource.
     *
     * @return a pointer to a chunk of unsigned bytes which live in a memory mapped/DLL file.
     * @see #getBinary
     * @see #getInt
     * @throws MissingResourceException
     * @throws UResourceTypeMismatchException
     * @draft ICU 3.0
     */
    public int[] getIntVector() {
        throw new UResourceTypeMismatchException("");
    }

    /**
     * Returns a signed integer from a resource.
     *
     * @return an integer value
     * @see #getIntVector
     * @see #getBinary
     * @throws MissingResourceException
     * @throws UResourceTypeMismatchException
     * @stable ICU 2.0
     */
    public int getInt() {
        throw new UResourceTypeMismatchException("");
    }

    /**
     * Returns a unsigned integer from a resource.
     * This integer is originally 28 bit and the sign gets propagated.
     *
     * @return an integer value
     * @see #getIntVector
     * @see #getBinary
     * @throws MissingResourceException
     * @throws UResourceTypeMismatchException
     * @stable ICU 2.0
     */
    public int getUInt() {
        throw new UResourceTypeMismatchException("");
    }
    /**
     * Returns the size of a resource. Size for scalar types is always 1,
     * and for vector/table types is the number of child resources.
     * <br><b><font color='red'>Warning: </font></b> Integer array is treated as a scalar type. There are no
     *          APIs to access individual members of an integer array. It
     *          is always returned as a whole.
     * @return number of resources in a given resource.
     * @draft ICU 3.0
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the type of a resource.
     * Available types are {@link #INT INT}, {@link #ARRAY ARRAY},
     * {@link #BINARY BINARY}, {@link #INT_VECTOR INT_VECTOR},
     * {@link #STRING STRING}, {@link #TABLE TABLE}.
     *
     * @return type of the given resource.
     * @draft ICU 3.0
     */
    public int getType() {
        int type = ICUResourceBundleImpl.RES_GET_TYPE(resource);
        if(type==TABLE32){
            return TABLE; //Mask the table32's real type
        }
        return type;
    }

    /**
     * Returns the key associated with a given resource. Not all the resources have a key - only
     * those that are members of a table.
     * @return a key associated to this resource, or NULL if it doesn't have a key
     * @draft ICU 3.0
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the iterator which iterates over this
     * resource bundle
     * @draft ICU 3.0
     */
    public ICUResourceBundleIterator getIterator() {
        return new ICUResourceBundleIterator(this);
    }

    /**
     * Returns the resource in a given resource at the specified index.
     *
     * @param index             an index to the wanted resource.
     * @return                  the sub resource UResourceBundle object
     * @throws IndexOutOfBoundsException
     * @draft ICU 3.0
     */
    public ICUResourceBundle get(int index) {
        return getImpl(index, null, this);
    }
    protected ICUResourceBundle getImpl(int index, HashMap table,
            ICUResourceBundle requested) {
        ICUResourceBundle obj = handleGet(index, table, requested);
        if (obj == null) {
            obj = (ICUResourceBundle) getParent();
            if (obj != null) {
                obj = obj.getImpl(index, table, requested);
            }
            if (obj == null)
                throw new MissingResourceException(
                        "Can't find resource for bundle "
                                + this.getClass().getName() + ", key "
                                + getKey(), this.getClass().getName(), getKey());
        }
        setLoadingStatus(obj, requested.getLocaleID());
        return obj;
    }
    // abstract UResourceBundle handleGetInt(int index);

    /**
     * Returns a resource in a given resource that has a given key.
     *
     * @param key               a key associated with the wanted resource
     * @return                  a resource bundle object representing rhe resource
     * @throws MissingResourceException
     * @draft ICU 3.0
     */
    public ICUResourceBundle get(String key) {
        return getImpl(key, null, this);
    }
    protected ICUResourceBundle getImpl(String key, HashMap table,
            ICUResourceBundle requested) {
        ICUResourceBundle obj = handleGet(key, table, requested);
        if (obj == null) {
            obj = (ICUResourceBundle) getParent();
            if (obj != null) {
                //call the get method to recursively fetch the resource
                obj = obj.getImpl(key, table, requested);
            }
            if (obj == null) {
                String fullName = ICUResourceBundleReader.getFullName(
                        getBaseName(), getLocaleID());
                throw new MissingResourceException(
                        "Can't find resource for bundle " + fullName + ", key "
                                + key, this.getClass().getName(), key);
            }
        }
        setLoadingStatus(obj, requested.getLocaleID());
        return obj;
    }

    private void setLoadingStatus(ICUResourceBundle bundle, String requestedLocale){
       String locale = bundle.getLocaleID(); 
       if(locale.equals("root")){
           bundle.setLoadingStatus(FROM_ROOT);
           return;
       }
       if(locale.equals(requestedLocale)){
           bundle.setLoadingStatus(FROM_LOCALE);
       }else{
           bundle.setLoadingStatus(FROM_FALLBACK);
       }
    } 
    
    /**
     * Returns the string in a given resource at the specified index.
     *
     * @param index            an index to the wanted string.
     * @return                  a string which lives in the resource.
     * @throws IndexOutOfBoundsException
     * @throws UResourceTypeMismatchException
     * @draft ICU 3.0
     */
    public String getString(int index) {
        ICUResourceBundle temp = get(index);
        if (temp.getType() == STRING) {
            return temp.getString();
        }
        throw new UResourceTypeMismatchException("");
    }

    /**
     * Returns the parent bundle of this bundle
     * @return UResourceBundle the parent of this bundle. Returns null if none
     * @draft ICU 3.0
     */
    public abstract UResourceBundle getParent();

    /**
     * Returns the locale id of this bundle as String
     * @return String locale id 
     * @draft ICU 3.4
     */
    protected abstract String getLocaleID();

    /**
     * Returns a functionally equivalent locale, considering keywords as well, for the specified keyword.
     * @param baseName resource specifier
     * @param resName top level resource to consider (such as "collations")
     * @param keyword a particular keyword to consider (such as "collation" )
     * @param locID The requested locale
     * @param fillinIsAvailable If non-null, 1-element array of fillin parameter that indicates whether the
     * requested locale was available. The locale is defined as 'available' if it physically
     * exists within the specified tree.
     * @return the locale
     * @internal ICU 3.0
     */
    public static final ULocale getFunctionalEquivalent(String baseName,
            String resName, String keyword, ULocale locID,
            boolean fillinIsAvailable[]) {
        String kwVal = locID.getKeywordValue(keyword);
        String baseLoc = locID.getBaseName();
        String defStr = null;
        ULocale parent = new ULocale(baseLoc);
        ULocale found = locID;
        ULocale defLoc = null; // locale where default (found) resource is
        boolean lookForDefault = false; // true if kwVal needs to be set
        ULocale fullBase = null; // base locale of found (target) resource
        int defDepth = 0; // depth of 'default' marker
        int resDepth = 0; // depth of found resource;
        if (fillinIsAvailable != null) {
            fillinIsAvailable[0] = true;
        }

        if ((kwVal == null) || (kwVal.length() == 0)
                || kwVal.equals(DEFAULT_TAG)) {
            kwVal = ""; // default tag is treated as no keyword
            lookForDefault = true;
        }

        // Check top level locale first
        ICUResourceBundle r = null;

        r = (ICUResourceBundle) UResourceBundle.getBundleInstance(baseName, parent);
        found = r.getULocale();
        if (fillinIsAvailable != null) {
            if (!found.equals(parent)) {
                fillinIsAvailable[0] = false;
            }
        }
        // determine in which locale (if any) the currently relevant 'default' is
        do {
            try {
                ICUResourceBundle irb = r.get(resName);
                defStr = irb.getString(DEFAULT_TAG);
                if (lookForDefault == true) {
                    kwVal = defStr;
                    lookForDefault = false;
                }
                defLoc = r.getULocale();
            } catch (MissingResourceException t) {
                // Ignore error and continue search.
            }
            if (defLoc == null) {
                r = (ICUResourceBundle) r.getParent();
                defDepth++;
            }
        } while ((r != null) && (defLoc == null));

        // Now, search for the named resource
        parent = new ULocale(baseLoc);
        r = (ICUResourceBundle) UResourceBundle.getBundleInstance(baseName, parent);
        // determine in which locale (if any) the named resource is located
        do {
            try {
                ICUResourceBundle irb = r.get(resName);
                /* UResourceBundle urb = */irb.get(kwVal);
                fullBase = irb.getULocale(); 
                // If the get() completed, we have the full base locale
                // If we fell back to an ancestor of the old 'default',
                // we need to re calculate the "default" keyword.
                if ((fullBase != null) && ((resDepth) > defDepth)) {
                    defStr = irb.getString(DEFAULT_TAG);
                    defLoc = r.getULocale();
                    defDepth = resDepth;
                }
            } catch (MissingResourceException t) {
                // Ignore error,
            }
            if (fullBase == null) {
                r = (ICUResourceBundle) r.getParent();
                resDepth++;
            }
        } while ((r != null) && (fullBase == null));

        if (fullBase == null && // Could not find resource 'kwVal'
                (defStr != null) && // default was defined
                !defStr.equals(kwVal)) { // kwVal is not default
            // couldn't find requested resource. Fall back to default.
            kwVal = defStr; // Fall back to default.
            parent = new ULocale(baseLoc);
            r = (ICUResourceBundle) UResourceBundle.getBundleInstance(baseName, parent);
            resDepth = 0;
            // determine in which locale (if any) the named resource is located
            do {
                try {
                    ICUResourceBundle irb = r.get(resName);
                    UResourceBundle urb = irb.get(kwVal);
                    
                    // if we didn't fail before this..
                    fullBase = r.getULocale();
                    
                    // If the fetched item (urb) is in a different locale than our outer locale (r/fullBase)
                    // then we are in a 'fallback' situation. treat as a missing resource situation.
                    if(!fullBase.toString().equals(urb.getLocale().toString())) {
                        fullBase = null; // fallback condition. Loop and try again.
                    }

                    // If we fell back to an ancestor of the old 'default',
                    // we need to re calculate the "default" keyword.
                    if ((fullBase != null) && ((resDepth) > defDepth)) {
                        defStr = irb.getString(DEFAULT_TAG);
                        defLoc = r.getULocale();
                        defDepth = resDepth;
                    }
                } catch (MissingResourceException t) {
                    // Ignore error, continue search.
                }
                if (fullBase == null) {
                    r = (ICUResourceBundle) r.getParent();
                    resDepth++;
                }
            } while ((r != null) && (fullBase == null));
        }

        if (fullBase == null) {
            throw new MissingResourceException(
                "Could not find locale containing requested or default keyword.",
                baseName, keyword + "=" + kwVal);
        }

        if (defStr.equals(kwVal) // if default was requested and
            && resDepth <= defDepth) { // default was set in same locale or child
            return fullBase; // Keyword value is default - no keyword needed in locale
        } else {
            return new ULocale(fullBase.toString() + "@" + keyword + "=" + kwVal);
        }
    }

    /**
     * Given a tree path and keyword, return a string enumeration of all possible values for that keyword.
     * @param baseName resource specifier
     * @param keyword a particular keyword to consider, must match a top level resource name
     * within the tree. (i.e. "collations")
     * @internal ICU 3.0
     */
    public static final String[] getKeywordValues(String baseName, String keyword) {
        Set keywords = new HashSet();
        ULocale locales[] = createULocaleList(baseName, ICU_DATA_CLASS_LOADER);
        int i;

        for (i = 0; i < locales.length; i++) {
            try {
                UResourceBundle b = UResourceBundle.getBundleInstance(baseName, locales[i]);
                // downcast to ICUResourceBundle?
                ICUResourceBundle irb = (ICUResourceBundle) (b.getObject(keyword));
                Enumeration e = irb.getKeys();
                Object s;
                while (e.hasMoreElements()) {
                    s = e.nextElement();
                    if ((s instanceof String) && !DEFAULT_TAG.equals(s)) {
                        // don't add 'default' items
                        keywords.add(s);
                    }
                }
            } catch (Throwable t) {
                //System.err.println("Error in - " + new Integer(i).toString()
                // + " - " + t.toString());
                // ignore the err - just skip that resource
            }
        }
        return (String[])keywords.toArray(new String[0]);
    }

    /**
     * This method performs multilevel fallback for fetching items from the
     * bundle e.g: If resource is in the form de__PHONEBOOK{ collations{
     * default{ "phonebook"} } } If the value of "default" key needs to be
     * accessed, then do: <code>
     *  UResourceBundle bundle = UResourceBundle.getBundleInstance("de__PHONEBOOK");
     *  ICUResourceBundle result = null;
     *  if(bundle instanceof ICUListResourceBundle){
     *      result = ((ICUListResourceBundle) bundle).getWithFallback("collations/default");
     *  }
     * </code>
     * 
     * @param path
     *            The path to the required resource key
     * @return resource represented by the key
     * @exception MissingResourceException
     */
    public ICUResourceBundle getWithFallback(String path)
            throws MissingResourceException {
        ICUResourceBundle result = null;
        ICUResourceBundle actualBundle = this;

        // now recuse to pick up sub levels of the items
        result = findResourceWithFallback(path, actualBundle, null);

        if (result == null) {
            throw new MissingResourceException(
                "Can't find resource for bundle "
                + this.getClass().getName() + ", key " + getType(),
                path, getKey());
        }
        return result;
    }

    // will throw type mismatch exception if the resource is not a string
    public String getStringWithFallback(String path) throws MissingResourceException {
        return getWithFallback(path).getString();
    }

    /**
     * Return a set of the locale names supported by a collection of resource
     * bundles.
     * 
     * @param bundlePrefix the prefix of the resource bundles to use.
     */
    public static Set getAvailableLocaleNameSet(String bundlePrefix) {
        return getAvailEntry(bundlePrefix).getLocaleNameSet();
    }

    /**
     * Return a set of all the locale names supported by a collection of
     * resource bundles.
     */
    public static Set getFullLocaleNameSet() {
        return getFullLocaleNameSet(ICU_BASE_NAME);
    }

    /**
     * Return a set of all the locale names supported by a collection of
     * resource bundles.
     * 
     * @param bundlePrefix the prefix of the resource bundles to use.
     */
    public static Set getFullLocaleNameSet(String bundlePrefix) {
        return getAvailEntry(bundlePrefix).getFullLocaleNameSet();
    }

    /**
     * Return a set of the locale names supported by a collection of resource
     * bundles.
     */
    public static Set getAvailableLocaleNameSet() {
        return getAvailableLocaleNameSet(ICU_BASE_NAME);
    }

    /**
     * Get the set of Locales installed in the specified bundles.
     * @return the list of available locales
     * @draft ICU 3.0
     */
    public static final ULocale[] getAvailableULocales(String baseName) {
        return getAvailEntry(baseName).getULocaleList();
    }

    /**
     * Get the set of ULocales installed the base bundle.
     * @return the list of available locales
     * @draft ICU 3.0
     */
    public static final ULocale[] getAvailableULocales() {
        return getAvailableULocales(ICU_BASE_NAME);
    }

    /**
     * Get the set of Locales installed in the specified bundles.
     * @return the list of available locales
     * @draft ICU 3.0
     */
    public static final Locale[] getAvailableLocales(String baseName) {
        return getAvailEntry(baseName).getLocaleList();
    }

   /**
     * Get the set of Locales installed the base bundle.
     * @return the list of available locales
     * @draft ICU 3.0
     */
    public static final Locale[] getAvailableLocales() {
        return getAvailEntry(ICU_BASE_NAME).getLocaleList();
    }

    /**
     * Convert a list of ULocales to a list of Locales.  ULocales with a script code will not be converted
     * since they cannot be represented as a Locale.  This means that the two lists will <b>not</b> match
     * one-to-one, and that the returned list might be shorter than the input list.
     * @param ulocales a list of ULocales to convert to a list of Locales.
     * @return the list of converted ULocales
     * @draft ICU 3.0
     */
    public static final Locale[] getLocaleList(ULocale[] ulocales) {
        ArrayList list = new ArrayList();
        for (int i = 0; i < ulocales.length; i++) {
            // if the ULocale does not contain a script code
            // only then convert it to a Locale object
            if (ulocales[i].getScript().length() == 0) {
                list.add(ulocales[i].toLocale());
            }
        }
        return (Locale[]) list.toArray(new Locale[list.size()]);
    }
 
    public Enumeration getKeys() {
        initKeysVector();
        return keys.elements();
    }
    private Vector keys = null;
    private synchronized void initKeysVector(){
        if(keys!=null){
            return;
        }
        ICUResourceBundle current = this;
        keys = new Vector();
        while(current!=null){
            Enumeration e = current.handleGetKeys();
            while(e.hasMoreElements()){
                String elem = (String)e.nextElement();
                if(!keys.contains(elem)){
                    keys.add(elem);
                }
            }
            current = (ICUResourceBundle)current.getParent();
        }
    }
    protected Enumeration handleGetKeys(){
        Vector keys = new Vector();
        ICUResourceBundle item = null;
        for (int i = 0; i < size; i++) {
            item = get(i);
            keys.add(item.getKey());
        }
        return keys.elements();
    }

    public static ICUResourceBundle createBundle(String baseName,
            String localeID, ClassLoader root) {
        ICUResourceBundle b = ICUResourceBundleImpl.createBundle(baseName, localeID, root);
        if(b==null){
            throw new MissingResourceException("Could not find the bundle "+ baseName+"/"+ localeID+".res","","");
        }
        return b;
    }

    //====== protected members ==============
    protected String key;
    protected int size = 1;
    protected String resPath;
    protected long resource = RES_BOGUS;
    protected boolean isTopLevel = false;

    protected static final long UNSIGNED_INT_MASK = 0xffffffffL;

    protected static final long RES_BOGUS = 0xffffffff;

    protected ICUResourceBundle handleGet(String key, HashMap table,
            ICUResourceBundle requested) {
        throw new UResourceTypeMismatchException("");
    }
    protected ICUResourceBundle handleGet(int index, HashMap table,
            ICUResourceBundle requested) {
        throw new UResourceTypeMismatchException("");
    }

    /**
     * Returns the locale of this resource bundle. This method can be used after
     * a call to getBundle() to determine whether the resource bundle returned
     * really corresponds to the requested locale or is a fallback.
     * 
     * @return the locale of this resource bundle
     */
    public Locale getLocale() {
        return getULocale().toLocale();
    }

    // this method is declared in ResourceBundle class
    // so cannot change the signature
    protected Object handleGetObject(String key) {
        return handleGetObjectImpl(key, this);
    }

    // To facilitate XPath style aliases we need a way to pass the reference
    // to requested locale. The only way I could figure out is to implement
    // the look up logic here. This has a disadvantage that if the client
    // loads an ICUResourceBundle, calls ResourceBundle.getObject method
    // with a key that does not exist in the bundle then the lookup is
    // done twice before throwing a MissingResourceExpection.
    private Object handleGetObjectImpl(String key, ICUResourceBundle requested) {
        Object obj = resolveObject(key, requested);
        if (obj == null) {
            ICUResourceBundle parent = (ICUResourceBundle) getParent();
            if (parent != null) {
                obj = parent.handleGetObjectImpl(key, requested);
            }
            if (obj == null)
                throw new MissingResourceException(
                    "Can't find resource for bundle "
                    + this.getClass().getName() + ", key " + key,
                    this.getClass().getName(), key);
        }
        return obj;
    }

    private Object resolveObject(String key, ICUResourceBundle requested) {
        if (getType() == STRING) {
            return getString();
        }
        ICUResourceBundle obj = handleGet(key, requested);
        if (obj != null) {
            if (obj.getType() == STRING) {
                return obj.getString();
            }
            try {
                if (obj.getType() == ARRAY) {
                    return obj.handleGetStringArray();
                }
            } catch (UResourceTypeMismatchException ex) {
                return obj;
            }
        }
        return obj;
    }

    protected ICUResourceBundle handleGet(int index, ICUResourceBundle requested) {
        return null;
    }

    protected ICUResourceBundle handleGet(String key,
            ICUResourceBundle requested) {
        return null;
    }

    protected String[] handleGetStringArray() {
        return null;
    }

    // ========== privates ==========
    private static final String ICU_RESOURCE_INDEX = "res_index";

    private static final String DEFAULT_TAG = "default";

    // Flag for enabling/disabling debugging code
    private static final boolean DEBUG = ICUDebug.enabled("localedata");

    // Cache for getAvailableLocales
    private static SoftReference GET_AVAILABLE_CACHE;
    private static final ULocale[] createULocaleList(String baseName,
            ClassLoader root) {
        // the canned list is a subset of all the available .res files, the idea
        // is we don't export them
        // all. gotta be a better way to do this, since to add a locale you have
        // to update this list,
        // and it's embedded in our binary resources.
        ICUResourceBundle bundle = (ICUResourceBundle) createBundle(baseName, ICU_RESOURCE_INDEX, root);
        
        bundle = bundle.get(INSTALLED_LOCALES);
        int length = bundle.getSize();
        int i = 0;
        ULocale[] locales = new ULocale[length];
        ICUResourceBundleIterator iter = bundle.getIterator();
        iter.reset();
        while (iter.hasNext()) {
            locales[i++] = new ULocale(iter.next().getKey());
        }
        bundle = null;
        return locales;
    }

    private static final Locale[] createLocaleList(String baseName) {
        ULocale[] ulocales = getAvailEntry(baseName).getULocaleList();
        return getLocaleList(ulocales);
    }

    private static final String[] createLocaleNameArray(String baseName,
            ClassLoader root) {
        ICUResourceBundle bundle = (ICUResourceBundle) createBundle( baseName, ICU_RESOURCE_INDEX, root);
        bundle = bundle.get(INSTALLED_LOCALES);
        int length = bundle.getSize();
        int i = 0;
        String[] locales = new String[length];
        ICUResourceBundleIterator iter = bundle.getIterator();
        iter.reset();
        while (iter.hasNext()) {
            locales[i++] = iter.next().getKey();
        }
        bundle = null;
        return locales;
    }

    private static final ArrayList createFullLocaleNameArray(
            final String baseName, final ClassLoader root) {

        ArrayList list = (ArrayList) java.security.AccessController
            .doPrivileged(new java.security.PrivilegedAction() {
                public Object run() {
                    // WebSphere class loader will return null for a raw
                    // directory name without trailing slash
                    String bn = baseName.endsWith("/")
                        ? baseName
                        : baseName + "/";

                    // look for prebuilt indices first
                    try {
                        InputStream s = root.getResourceAsStream(bn + ICU_RESOURCE_INDEX + ".txt");
                        if (s != null) {
                            ArrayList list = new ArrayList();
                            BufferedReader br = new BufferedReader(new InputStreamReader(s, "ASCII"));
                            String line;
                            while ((line = br.readLine()) != null) {
                                if (line.length() != 0 && !line.startsWith("#")) {
                                    list.add(line);
                                }
                            }
                            return list;
                        }
                    } catch (IOException e) {
                        // swallow it
                    }

                    URL url = root.getResource(bn);
                    URLHandler handler = URLHandler.get(url);
                    if (handler != null) {
                        final ArrayList list = new ArrayList();
                        URLVisitor v = new URLVisitor() {
                            public void visit(String s) {
                                if (s.endsWith(".res") && !"res_index.res".equals(s)) {
                                    list.add(s.substring(0, s.length() - 4)); // strip '.res'
                                }
                            }
                        };
                        handler.guide(v, false);
                        return list;
                    }

                    return null;
                }
            });

        return list;
    }

    private static Set createFullLocaleNameSet(String baseName) {
        ArrayList list = createFullLocaleNameArray(baseName,ICU_DATA_CLASS_LOADER);
        HashSet set = new HashSet();
        if(list==null){
            throw new MissingResourceException("Could not find "+  ICU_RESOURCE_INDEX, "", "");
        }
        set.addAll(list);
        return Collections.unmodifiableSet(set);
    }

    private static Set createLocaleNameSet(String baseName) {
        try {
            String[] locales = createLocaleNameArray(baseName, ICU_DATA_CLASS_LOADER);

            HashSet set = new HashSet();
            set.addAll(Arrays.asList(locales));
            return Collections.unmodifiableSet(set);
        } catch (MissingResourceException e) {
            if (DEBUG) {
                System.out.println("couldn't find index for bundleName: " + baseName);
                Thread.dumpStack();
            }
        }
        return Collections.EMPTY_SET;
    }

    /**
     * Holds the prefix, and lazily creates the Locale[] list or the locale name
     * Set as needed.
     */
    private static final class AvailEntry {
        private String prefix;
        private ULocale[] ulocales;
        private Locale[] locales;
        private Set nameSet;
        private Set fullNameSet;

        AvailEntry(String prefix) {
            this.prefix = prefix;
        }

        ULocale[] getULocaleList() {
            if (ulocales == null) {
                ulocales = createULocaleList(prefix, ICU_DATA_CLASS_LOADER);
            }
            return ulocales;
        }
        Locale[] getLocaleList() {
            if (locales == null) {
                locales = createLocaleList(prefix);
            }
            return locales;
        }
        Set getLocaleNameSet() {
            if (nameSet == null) {
                nameSet = createLocaleNameSet(prefix);
            }
            return nameSet;
        }
        Set getFullLocaleNameSet() {
            if (fullNameSet == null) {
                fullNameSet = createFullLocaleNameSet(prefix);
            }
            return fullNameSet;
        }
    }

    /**
     * Stores the locale information in a cache accessed by key (bundle prefix).
     * The cached objects are AvailEntries. The cache is held by a SoftReference
     * so it can be GC'd.
     */
    private static AvailEntry getAvailEntry(String key) {
        AvailEntry ae = null;
        Map lcache = null;
        if (GET_AVAILABLE_CACHE != null) {
            lcache = (Map) GET_AVAILABLE_CACHE.get();
            if (lcache != null) {
                ae = (AvailEntry) lcache.get(key);
            }
        }

        if (ae == null) {
            ae = new AvailEntry(key);
            if (lcache == null) {
                lcache = new HashMap();
                lcache.put(key, ae);
                GET_AVAILABLE_CACHE = new SoftReference(lcache);
            } else {
                lcache.put(key, ae);
            }
        }

        return ae;
    }

    private ICUResourceBundle findResourceWithFallback(String path,
            ICUResourceBundle actualBundle, ICUResourceBundle requested) {
        ICUResourceBundle sub = null;
        if (requested == null) {
            requested = actualBundle;
        }
        while (actualBundle != null) {
            StringTokenizer st = new StringTokenizer(path, "/");
            ICUResourceBundle current = actualBundle;
            while (st.hasMoreTokens()) {
                String subKey = st.nextToken();
                sub = current.handleGet(subKey, requested);
                if (sub == null) {
                    break;
                }
                current = sub;
            }
            if (sub != null) {
                //we found it
                break;
            }
            if (actualBundle.resPath.length() != 0) {
                path = resPath + "/" + path;
            }
            // if not try the parent bundle
            actualBundle = (ICUResourceBundle) actualBundle.getParent();

        }
        if(sub != null){
            setLoadingStatus(sub, requested.getLocaleID());
        }
        return sub;
    }
    public boolean equals(Object other) {
        if (other instanceof ICUResourceBundle) {
            ICUResourceBundle o = (ICUResourceBundle) other;
            if (getBaseName().equals(o.getBaseName())
                    && getULocale().equals(o.getULocale())) {
                return true;
            }
        }
        return false;
    }
    // This method is for super class's instantiateBundle method
    public static UResourceBundle getBundleInstance(String baseName, String localeID, 
                                                    ClassLoader root, boolean disableFallback){
        UResourceBundle b = instantiateBundle(baseName, localeID, root, disableFallback);
        if(b==null){
            throw new MissingResourceException("Could not find the bundle "+ baseName+"/"+ localeID+".res","","");
        }
        return b;
    }
    //  recursively build bundle .. over-ride super class method.
    protected synchronized static UResourceBundle instantiateBundle(String baseName, String localeID, 
                                                                    ClassLoader root, boolean disableFallback){
        ULocale defaultLocale = ULocale.getDefault();
        String localeName = localeID;
        if(localeName.indexOf('@')>0){
            localeName = ULocale.getBaseName(localeID);
        }
        String fullName = ICUResourceBundleReader.getFullName(baseName, localeName);
        ICUResourceBundle b = (ICUResourceBundle)loadFromCache(root, fullName, defaultLocale);
        
        // here we assume that java type resource bundle organization
        // is required then the base name contains '.' else 
        // the resource organization is of ICU type
        // so clients can instantiate resources of the type
        // com.mycompany.data.MyLocaleElements_en.res and 
        // com.mycompany.data.MyLocaleElements.res
        //
        final String rootLocale = (baseName.indexOf('.')==-1) ? "root" : "";
        final String defaultID = ULocale.getDefault().toString();
        
        if(localeName.equals("")){
            localeName = rootLocale;   
        }
        if(DEBUG) System.out.println("Creating "+fullName+ " currently b is "+b);
        if (b == null) {
            b = ICUResourceBundleImpl.createBundle(baseName, localeName, root);
            
            if(DEBUG)System.out.println("The bundle created is: "+b+" and disableFallback="+disableFallback+" and bundle.getNoFallback="+(b!=null && b.getNoFallback()));
            if(disableFallback || (b!=null && b.getNoFallback())){
                // no fallback because the caller said so or because the bundle says so
                return b;
            }

            // fallback to locale ID parent
            if(b == null){
                int i = localeName.lastIndexOf('_');
                if (i != -1) {
                    String temp = localeName.substring(0, i);
                    b = (ICUResourceBundle)instantiateBundle(baseName, temp, root, disableFallback);
                    if(b!=null && b.getULocale().equals(temp)){
                        b.setLoadingStatus(ICUResourceBundle.FROM_FALLBACK);
                    }
                }else{
                    if(defaultID.indexOf(localeName)==-1){
                        b = (ICUResourceBundle)instantiateBundle(baseName, defaultID, root, disableFallback);
                        if(b!=null){
                            b.setLoadingStatus(ICUResourceBundle.FROM_DEFAULT);
                        }
                    }else if(rootLocale.length()!=0){
                        b = ICUResourceBundleImpl.createBundle(baseName, rootLocale, root); 
                        if(b!=null){
                            b.setLoadingStatus(ICUResourceBundle.FROM_ROOT);
                        }
                    }
                }
            }else{
                UResourceBundle parent = null;
                localeName = b.getLocaleID();
                int i = localeName.lastIndexOf('_');
                
                addToCache(root, fullName, defaultLocale, b);
                
                if (i != -1) {
                    parent = instantiateBundle(baseName, localeName.substring(0, i), root, disableFallback);
                }else if(!localeName.equals(rootLocale)){
                    parent = ICUResourceBundleImpl.createBundle(baseName, rootLocale, root);   
                }
                
                if(!b.equals(parent)){
                    b.setParent(parent);
                }
            }      
        }
        return b;
    }
}
