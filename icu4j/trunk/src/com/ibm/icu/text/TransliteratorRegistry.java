/*
**********************************************************************
*   Copyright (c) 2001, International Business Machines
*   Corporation and others.  All Rights Reserved.
**********************************************************************
*   Date        Name        Description
*   08/19/2001  aliu        Creation.
**********************************************************************
*/

package com.ibm.text;
import java.util.*;
import java.io.UnsupportedEncodingException;
import com.ibm.text.resources.ResourceReader;
import com.ibm.util.CaseInsensitiveString;

class TransliteratorRegistry {

    // char constants
    private static final char LOCALE_SEP  = '_';
    private static final char ID_SEP      = '-';
    private static final char VARIANT_SEP = '/';

    // String constants
    private static final String NO_VARIANT = ""; // empty string
    private static final String ANY = "Any";

    /**
     * Resource bundle key for the RuleBasedTransliterator rule.
     */
    private static final String RB_RULE = "Rule";

    /**
     * Resource bundle containing locale-specific transliterator data.
     */
    private static final String RB_LOCALE_ELEMENTS =
        "com.ibm.text.resources.LocaleElements";

    /**
     * Locale indicating the root.
     */
    private static final Locale ROOT_LOCALE = new Locale("", "", "");

    /**
     * Dynamic registry mapping full IDs to Entry objects.  This
     * contains both public and internal entities.  The visibility is
     * controlled by whether an entry is listed in availableIDs and
     * specDAG or not.
     *
     * Keys are CaseInsensitiveString objects.
     * Values are objects of class Class (subclass of Transliterator),
     * RuleBasedTransliterator.Data, Transliterator.Factory, or one
     * of the entry classes defined here (AliasEntry or ResourceEntry).
     */
    private Hashtable registry;

    /**
     * DAG of visible IDs by spec.  Hashtable: source => (Hashtable:
     * target => (UVector: variant)) The UVector of variants is never
     * empty.  For a source-target with no variant, the special
     * variant NO_VARIANT (the empty string) is stored in slot zero of
     * the UVector.  This NO_VARIANT variant is invisible to
     * countAvailableVariants() and getAvailableVariant().
     *
     * Keys are CaseInsensitiveString objects.
     * Values are Hashtable of (CaseInsensitiveString -> Vector of
     * CaseInsensitiveString)
     */
    private Hashtable specDAG;

    /**
     * Vector of public full IDs (CaseInsensitiveString objects).
     */
    private Vector availableIDs;

    //----------------------------------------------------------------------
    // class Spec
    //----------------------------------------------------------------------

    /**
     * A Spec is a string specifying either a source or a target.  In more
     * general terms, it may also specify a variant, but we only use the
     * Spec class for sources and targets.
     *
     * A Spec may be a locale or a script.  If it is a locale, it has a
     * fallback chain that goes xx_YY_ZZZ -> xx_YY -> xx -> ssss, where
     * ssss is the script mapping of xx_YY_ZZZ.  The Spec API methods
     * hasFallback(), next(), and reset() iterate over this fallback
     * sequence.
     *
     * The Spec class canonicalizes itself, so the locale is put into
     * canonical form, or the script is transformed from an abbreviation
     * to a full name.
     */
    static class Spec {

        private String top;
        private String spec;
        private String nextSpec;
        private String scriptName;
        private boolean isSpecLocale; // TRUE if spec is a locale
        private boolean isNextLocale; // TRUE if nextSpec is a locale
        private ResourceBundle res;

        public Spec(String theSpec) {
            top = theSpec;
            spec = null;
            scriptName = null;

            Locale toploc = getLocale(top);
            res = ResourceBundle.getBundle(RB_LOCALE_ELEMENTS, toploc);
            if (res.getLocale() != ROOT_LOCALE) {
                isSpecLocale = false;
                res = null;
            } else {
                isSpecLocale = true;
            }

            // Canonicalize script name -or- do locale->script mapping
            int[] s = UScript.getCode(top);
            if (s[0] != UScript.INVALID_CODE) {
                scriptName = UScript.getName(s[0]);
            }

            // assert(spec != top);
            reset();
        }

        public boolean hasFallback() {
            return nextSpec != null;
        }

        public void reset() {
            if (spec != top) { // [sic] pointer comparison
                spec = top;
                isSpecLocale = (res != null);
                setupNext();
            }
        }

        private void setupNext() {
            isNextLocale = false;
            if (isSpecLocale) {
                nextSpec = spec;
                int i = nextSpec.lastIndexOf(LOCALE_SEP);
                // If i == 0 then we have _FOO, so we fall through
                // to the scriptName.
                if (i > 0) {
                    nextSpec = spec.substring(0, i);
                    isNextLocale = true;
                } else {
                    nextSpec = scriptName; // scriptName may be null
                }
            } else {
                // spec is a script, so we are at the end
                nextSpec = null;
            }
        }

        // Protocol:
        // for(String& s(spec.get());
        //     spec.hasFallback(); s(spec.next())) { ...

        public String next() {
            spec = nextSpec;
            isSpecLocale = isNextLocale;
            setupNext();
            return spec;
        }

        public String get() {
            return spec;
        }

        public boolean isLocale() {
            return isSpecLocale;
        }

        /**
         * Return the ResourceBundle for this spec, at the current
         * level of iteration.  The level of iteration goes from
         * aa_BB_CCC to aa_BB to aa.  If the bundle does not
         * correspond to the current level of iteration, return null.
         * If isLocale() is false, always return null.
         */
        public ResourceBundle getBundle() {
            if (res != null &&
                res.getLocale().toString().equals(spec)) {
                return res;
            }
            return null;
        }

        public String getTop() {
            return top;
        }

        // Internal helper function
        private static Locale getLocale(String name) {
            String language = "";
            String country = "";
            String variant = "";

            int i1 = name.indexOf('_');
            if (i1 < 0) {
                language = name;
            } else {
                language = name.substring(0, i1);
                ++i1;
                int i2 = name.indexOf('_', i1);
                if (i2 < 0) {
                    country = name.substring(i1);
                } else {
                    country = name.substring(i1, i2);
                    variant = name.substring(i2+1);
                }
            }
            return new Locale(language, country, variant);
        }
    }

    //----------------------------------------------------------------------
    // Entry classes
    //----------------------------------------------------------------------

    static class ResourceEntry {
        public String resourceName;
        public String encoding;
        public int direction;
        public ResourceEntry(String n, String enc, int d) {
            resourceName = n;
            encoding = enc;
            direction = d;
        }
    }

    static class AliasEntry {
        public String alias;
        public AliasEntry(String a) {
            alias = a;
        }
    }

    static class CompoundRBTEntry {
        private String ID;
        private String idBlock;
        private int idSplitPoint;
        private RuleBasedTransliterator.Data data;
        private UnicodeSet compoundFilter;

        public CompoundRBTEntry(String theID, String theIDBlock,
                                int theIDSplitPoint,
                                RuleBasedTransliterator.Data theData,
                                UnicodeSet theCompoundFilter) {
            ID = theID;
            idBlock = theIDBlock;
            idSplitPoint = theIDSplitPoint;
            data = theData;
            compoundFilter = theCompoundFilter;
        }

        public Transliterator getInstance() {
            Transliterator t = new RuleBasedTransliterator("_", data, null);
            t = new CompoundTransliterator(ID, idBlock, idSplitPoint, t);
            t.setFilter(compoundFilter);
            return t;
        }
    }

    //----------------------------------------------------------------------
    // class TransliteratorRegistry: Basic public API
    //----------------------------------------------------------------------

    public TransliteratorRegistry() {
        registry = new Hashtable();
        specDAG = new Hashtable();
        availableIDs = new Vector();
    }

    /**
     * Given a simple ID (forward direction, no inline filter, not
     * compound) attempt to instantiate it from the registry.  Return
     * 0 on failure.
     *
     * Return a non-empty aliasReturn value if the ID points to an alias.
     * We cannot instantiate it ourselves because the alias may contain
     * filters or compounds, which we do not understand.  Caller should
     * make aliasReturn empty before calling.
     */
    public Transliterator get(String ID,
                              StringBuffer aliasReturn) {
        Object[] entry = find(ID);
        return (entry == null) ? null
            : instantiateEntry(ID, entry, aliasReturn);
    }

    /**
     * Register a class.  This adds an entry to the
     * dynamic store, or replaces an existing entry.  Any entry in the
     * underlying static locale resource store is masked.
     */
    public void put(String ID,
                    Class transliteratorSubclass,
                    boolean visible) {
        registerEntry(ID, transliteratorSubclass, visible);
    }

    /**
     * Register an ID and a factory function pointer.  This adds an
     * entry to the dynamic store, or replaces an existing entry.  Any
     * entry in the underlying static locale resource store is masked.
     */
    public void put(String ID,
                    Transliterator.Factory factory,
                    boolean visible) {
        registerEntry(ID, factory, visible);
    }

    /**
     * Register an ID and a resource name.  This adds an entry to the
     * dynamic store, or replaces an existing entry.  Any entry in the
     * underlying static locale resource store is masked.
     */
    public void put(String ID,
                    String resourceName,
                    String encoding,
                    int dir,
                    boolean visible) {
        registerEntry(ID, new ResourceEntry(resourceName, encoding, dir), visible);
    }

    /**
     * Register an ID and an alias ID.  This adds an entry to the
     * dynamic store, or replaces an existing entry.  Any entry in the
     * underlying static locale resource store is masked.
     */
    public void put(String ID,
                    String alias,
                    boolean visible) {
        registerEntry(ID, new AliasEntry(alias), visible);
    }

    /**
     * Unregister an ID.  This removes an entry from the dynamic store
     * if there is one.  The static locale resource store is
     * unaffected.
     */
    public void remove(String ID) {
        String[] stv = IDtoSTV(ID);
        // Only need to do this if ID.indexOf('-') < 0
        String id = STVtoID(stv[0], stv[1], stv[2]);
        registry.remove(new CaseInsensitiveString(id));
        removeSTV(stv[0], stv[1], stv[2]);
        availableIDs.removeElement(new CaseInsensitiveString(id));
    }
    
    //----------------------------------------------------------------------
    // class TransliteratorRegistry: Public ID and spec management
    //----------------------------------------------------------------------

    /**
     * An internal class that adapts a n enumeration over
     * CaseInsensitiveStrings to an enumeration over Strings.
     */
    private static class IDEnumeration implements Enumeration {
        Enumeration enum;

        public IDEnumeration(Enumeration e) {
            enum = e;
        }

        public boolean hasMoreElements() {
            return enum != null && enum.hasMoreElements();
        }

        public Object nextElement() {
            return ((CaseInsensitiveString) enum.nextElement()).getString();
        }
    }

    /**
     * Returns an enumeration over the programmatic names of visible
     * registered transliterators.
     *
     * @return An <code>Enumeration</code> over <code>String</code> objects
     */
    public Enumeration getAvailableIDs() {
        // Since the cache contains CaseInsensitiveString objects, but
        // the caller expects Strings, we have to use an intermediary.
        return new IDEnumeration(availableIDs.elements());
    }

    /**
     * Returns an enumeration over all visible source names.
     *
     * @return An <code>Enumeration</code> over <code>String</code> objects
     */
    public Enumeration getAvailableSources() {
        return new IDEnumeration(specDAG.keys());
    }

    /**
     * Returns an enumeration over visible target names for the given
     * source.
     *
     * @return An <code>Enumeration</code> over <code>String</code> objects
     */
    public Enumeration getAvailableTargets(String source) {
        CaseInsensitiveString cisrc = new CaseInsensitiveString(source);
        Hashtable targets = (Hashtable) specDAG.get(cisrc);
        if (targets == null) {
            return new IDEnumeration(null);
        }
        return new IDEnumeration(targets.keys());
    }

    /**
     * Returns an enumeration over visible variant names for the given
     * source and target.
     *
     * @return An <code>Enumeration</code> over <code>String</code> objects
     */
    public Enumeration getAvailableVariants(String source, String target) {
        CaseInsensitiveString cisrc = new CaseInsensitiveString(source);
        CaseInsensitiveString citrg = new CaseInsensitiveString(target);
        Hashtable targets = (Hashtable) specDAG.get(cisrc);
        if (targets == null) {
            return new IDEnumeration(null);
        }
        Vector variants = (Vector) targets.get(citrg);
        if (variants == null) {
            return new IDEnumeration(null);
        }
        return new IDEnumeration(variants.elements());
    }

    //----------------------------------------------------------------------
    // class TransliteratorRegistry: internal
    //----------------------------------------------------------------------

    /**
     * Given an ID, parse it into source, target, and variant strings.
     * The variant may be empty.  If the source is empty it will be set to
     * "Any".  If the variant is empty it will be set to the empty string.
     */
    private String[] IDtoSTV(String id) {
        String source = null;
        String target = null;
        String variant = null;

        int dash = id.indexOf(ID_SEP);
        int stroke = id.indexOf(VARIANT_SEP);
        int start = 0;
        int limit = id.length();
        if (dash < 0) {
            source = ANY;
        } else {
            source = id.substring(0, dash);
            start = dash + 1;
        }
        if (stroke >= 0) {
            variant = id.substring(stroke + 1, id.length());
            limit = stroke;
        } else {
            variant = "";
        }
        target = id.substring(start, limit);
        return new String[] { source, target, variant };
    }

    /**
     * Given source, target, and variant strings, concatenate them into a
     * full ID.  If the source is empty, then "Any" will be used for the
     * source, so the ID will always be of the form s-t/v or s-t.
     */
    private String STVtoID(String source,
                           String target,
                           String variant) {
        StringBuffer id = new StringBuffer(source);
        if (id.length() == 0) {
            id.append(ANY);
        }
        id.append(ID_SEP).append(target);
        if (variant.length() != 0) {
            id.append(VARIANT_SEP).append(variant);
        }
        return id.toString();
    }

    /**
     * Convenience method.  Calls 6-arg registerEntry().
     */
    private void registerEntry(String source,
                               String target,
                               String variant,
                               Object entry,
                               boolean visible) {
        String s = source;
        if (s.length() == 0) {
            s = ANY;
        }
        String ID = STVtoID(source, target, variant);
        registerEntry(ID, s, target, variant, entry, visible);
    }

    /**
     * Convenience method.  Calls 6-arg registerEntry().
     */
    private void registerEntry(String ID,
                               Object entry,
                               boolean visible) {
        String[] stv = IDtoSTV(ID);
        // Only need to do this if ID.indexOf('-') < 0
        String id = STVtoID(stv[0], stv[1], stv[2]);
        registerEntry(id, stv[0], stv[1], stv[2], entry, visible);
    }

    /**
     * Register an entry object (adopted) with the given ID, source,
     * target, and variant strings.
     */
    private void registerEntry(String ID,
                               String source,
                               String target,
                               String variant,
                               Object entry,
                               boolean visible) {
        CaseInsensitiveString ciID = new CaseInsensitiveString(ID);

        // Store the entry within an array so it can be modified later
        if (!(entry instanceof Object[])) {
            entry = new Object[] { entry };
        }

        registry.put(ciID, entry);
        if (visible) {
            registerSTV(source, target, variant);
            if (!availableIDs.contains(ciID)) {
                availableIDs.addElement(ciID);
            }
        } else {
            removeSTV(source, target, variant);
            availableIDs.removeElement(ciID);
        }
    }

    /**
     * Register a source-target/variant in the specDAG.  Variant may be
     * empty, but source and target must not be.  If variant is empty then
     * the special variant NO_VARIANT is stored in slot zero of the
     * UVector of variants.
     */
    private void registerSTV(String source,
                             String target,
                             String variant) {
        // assert(source.length() > 0);
        // assert(target.length() > 0);
        CaseInsensitiveString cisrc = new CaseInsensitiveString(source);
        CaseInsensitiveString citrg = new CaseInsensitiveString(target);
        CaseInsensitiveString civar = new CaseInsensitiveString(variant);
        Hashtable targets = (Hashtable) specDAG.get(cisrc);
        if (targets == null) {
            targets = new Hashtable();
            specDAG.put(cisrc, targets);
        }
        Vector variants = (Vector) targets.get(citrg);
        if (variants == null) {
            variants = new Vector();
            targets.put(citrg, variants);
        }
        // assert(NO_VARIANT == "");
        // We add the variant string.  If it is the special "no variant"
        // string, that is, the empty string, we add it at position zero.
        if (!variants.contains(civar)) {
            if (variant.length() > 0) {
                variants.addElement(civar);
            } else {
                variants.insertElementAt(civar, 0);
            }
        }
    }

    /**
     * Remove a source-target/variant from the specDAG.
     */
    private void removeSTV(String source,
                           String target,
                           String variant) {
        // assert(source.length() > 0);
        // assert(target.length() > 0);
        CaseInsensitiveString cisrc = new CaseInsensitiveString(source);
        CaseInsensitiveString citrg = new CaseInsensitiveString(target);
        CaseInsensitiveString civar = new CaseInsensitiveString(variant);
        Hashtable targets = (Hashtable) specDAG.get(cisrc);
        if (targets == null) {
            return; // should never happen for valid s-t/v
        }
        Vector variants = (Vector) targets.get(citrg);
        if (variants == null) {
            return; // should never happen for valid s-t/v
        }
        variants.removeElement(variant);
        if (variants.size() == 0) {
            targets.remove(citrg); // should delete variants
            if (targets.size() == 0) {
                specDAG.remove(cisrc); // should delete targets
            }
        }
    }

    /**
     * Attempt to find a source-target/variant in the dynamic registry
     * store.  Return 0 on failure.
     */
    private Object[] findInDynamicStore(Spec src,
                                      Spec trg,
                                      String variant) {
        String ID = STVtoID(src.get(), trg.get(), variant);
        return (Object[]) registry.get(new CaseInsensitiveString(ID));
    }

    /**
     * Attempt to find a source-target/variant in the static locale
     * resource store.  Do not perform fallback.  Return 0 on failure.
     *
     * On success, create a new entry object, register it in the dynamic
     * store, and return a pointer to it, but do not make it public --
     * just because someone requested something, we do not expand the
     * available ID list (or spec DAG).
     */
    private Object[] findInStaticStore(Spec src,
                                     Spec trg,
                                     String variant) {
        Object[] entry = null;
        if (src.isLocale()) {
            entry = findInBundle(src, trg, variant,
                                 "TransliterateTo");
        } else if (trg.isLocale()) {
            entry = findInBundle(trg, src, variant,
                                 "TransliterateFrom");
        }

        // If we found an entry, store it in the Hashtable for next
        // time.
        if (entry != null) {
            registerEntry(src.getTop(), trg.getTop(), variant, entry, false);
        }

        return entry;
    }

    /**
     * Attempt to find an entry in a single resource bundle.  This is
     * a one-sided lookup.  findInStaticStore() performs up to two such
     * lookups, one for the source, and one for the target.
     *
     * Do not perform fallback.  Return 0 on failure.
     *
     * On success, create a new Entry object, populate it, and return it.
     * The caller owns the returned object.
     */
    private Object[] findInBundle(Spec specToOpen,
                                  Spec specToFind,
                                  String variant,
                                  String tagPrefix) {
        // assert(specToOpen.isLocale());
        ResourceBundle res = specToOpen.getBundle();
        if (res == null) {
            // This means that the bundle's locale does not match
            // the current level of iteration for the spec.
            return null;
        }

        StringBuffer tag = new StringBuffer(tagPrefix);
        tag.append(LOCALE_SEP).append(specToFind.get());

        try {
            // The TranslateTo_xxx or TranslateFrom_xxx resource is
            // an array of strings of the format { <v0>, <r0>, ... }.
            // Each <vi> is a variant name, and each <ri> is a rule.
            String[] subres = res.getStringArray(tag.toString());

            // assert(subres != null);
            // assert(subres.length % 2 == 0);
            int i = 0;
            if (variant.length() != 0) {
                for (i=0; i<subres.length; i+= 2) {
                    if (subres[i].equalsIgnoreCase(variant)) {
                        break;
                    }
                }
            }

            if (i < subres.length) {
                // We have a match, or there is no variant and i == 0.
                // We have succeeded in loading a string from the
                // locale resources.  Return the rule string which
                // will itself become the registry entry.
                return new Object[] { subres[i+1] };
            }

        } catch (MissingResourceException e) {}

        // If we get here we had a missing resource exception or we
        // failed to find a desired variant.
        return null;
    }

    /**
     * Convenience method.  Calls 3-arg find().
     */
    private Object[] find(String ID) {
        String[] stv = IDtoSTV(ID);
        return find(stv[0], stv[1], stv[2]);
    }

    /**
     * Top-level find method.  Attempt to find a source-target/variant in
     * either the dynamic or the static (locale resource) store.  Perform
     * fallback.
     *
     * Lookup sequence for ss_SS_SSS-tt_TT_TTT/v:
     *
     *   ss_SS_SSS-tt_TT_TTT/v -- in hashtable
     *   ss_SS_SSS-tt_TT_TTT/v -- in ss_SS_SSS (no fallback)
     *
     *     repeat with t = tt_TT_TTT, tt_TT, tt, and tscript
     *
     *     ss_SS_SSS-t/*
     *     ss_SS-t/*
     *     ss-t/*
     *     sscript-t/*
     *
     * Here * matches the first variant listed.
     *
     * Caller does NOT own returned object.  Return 0 on failure.
     */
    private Object[] find(String source,
                          String target,
                          String variant) {

        Spec src = new Spec(source);
        Spec trg = new Spec(target);
        Object[] entry = null;

        if (variant.length() != 0) {

            // Seek exact match in hashtable
            entry = findInDynamicStore(src, trg, variant);
            if (entry != null) {
                return entry;
            }

            // Seek exact match in locale resources
            entry = findInStaticStore(src, trg, variant);
            if (entry != null) {
                return entry;
            }
        }

        for (;;) {
            src.reset();
            for (;;) {
                // Seek match in hashtable
                entry = findInDynamicStore(src, trg, NO_VARIANT);
                if (entry != null) {
                    return entry;
                }

                // Seek match in locale resources
                entry = findInStaticStore(src, trg, NO_VARIANT);
                if (entry != null) {
                    return entry;
                }
                if (!src.hasFallback()) {
                    break;
                }
                src.next();
            }
            if (!trg.hasFallback()) {
                break;
            }
            trg.next();
        }

        return null;
    }

    /**
     * Given an Entry object, instantiate it.  Caller owns result.  Return
     * 0 on failure.
     *
     * Return a non-empty aliasReturn value if the ID points to an alias.
     * We cannot instantiate it ourselves because the alias may contain
     * filters or compounds, which we do not understand.  Caller should
     * make aliasReturn empty before calling.
     *
     * The entry object is assumed to reside in the dynamic store.  It may be
     * modified.
     */
    private Transliterator instantiateEntry(String ID,
                                            Object[] entryWrapper,
                                            StringBuffer aliasReturn) {
        // We actually modify the entry object in some cases.  If it
        // is a string, we may partially parse it and turn it into a
        // more processed precursor.  This makes the next
        // instantiation faster and allows sharing of immutable
        // components like the RuleBasedTransliterator.Data objects.
        // For this reason, the entry object is an Object[] of length
        // 1.

        for (;;) {
            Object entry = entryWrapper[0];

            if (entry instanceof RuleBasedTransliterator.Data) {
                RuleBasedTransliterator.Data data = (RuleBasedTransliterator.Data) entry;
                return new RuleBasedTransliterator(ID, data, null);
            } else if (entry instanceof Class) {
                try {
                    return (Transliterator) ((Class) entry).newInstance();
                } catch (InstantiationException e) {
                } catch (IllegalAccessException e2) {}
                return null;
            } else if (entry instanceof AliasEntry) {
                aliasReturn.append(((AliasEntry) entry).alias);
                return null;
            } else if (entry instanceof Transliterator.Factory) {
                return ((Transliterator.Factory) entry).getInstance(ID);
            } else if (entry instanceof CompoundRBTEntry) {
                return ((CompoundRBTEntry) entry).getInstance();
            }

            // At this point entry type must be either RULES_FORWARD or
            // RULES_REVERSE.  We process the rule data into a
            // TransliteratorRuleData object, and possibly also into an
            // .id header and/or footer.  Then we modify the registry with
            // the parsed data and retry.
            ResourceEntry re = (ResourceEntry) entry;
            ResourceReader r = null;
            try {
                r = new ResourceReader(re.resourceName, re.encoding);
            } catch (UnsupportedEncodingException e) {
                // This should never happen; UTF8 is always supported
                throw new RuntimeException(e.getMessage());
            }

            TransliteratorParser parser = new TransliteratorParser();
            parser.parse(r, re.direction);

            // Reset entry to something that we process at the
            // top of the loop, then loop back to the top.  As long as we
            // do this, we only loop through twice at most.
            // NOTE: The logic here matches that in
            // Transliterator.createFromRules().
            if (parser.idBlock.length() == 0) {
                if (parser.data == null) {
                    // No idBlock, no data -- this is just an
                    // alias for Null
                    entryWrapper[0] = new AliasEntry(NullTransliterator._ID);
                } else {
                    // No idBlock, data != 0 -- this is an
                    // ordinary RBT_DATA
                    entryWrapper[0] = parser.data;
                }
            } else {
                if (parser.data == null) {
                    // idBlock, no data -- this is an alias.  The ID has
                    // been munged from reverse into forward mode, if
                    // necessary, so instantiate the ID in the forward
                    // direction.
                    entryWrapper[0] = new AliasEntry(parser.idBlock);
                } else {
                    // idBlock and data -- this is a compound
                    // RBT
                    entryWrapper[0] = new CompoundRBTEntry(
                        ID, parser.idBlock, parser.idSplitPoint, parser.data, parser.compoundFilter);
                }
            }
        }
    }
}

//eof
