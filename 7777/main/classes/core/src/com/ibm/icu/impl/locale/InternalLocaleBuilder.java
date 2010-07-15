/*
 *******************************************************************************
 * Copyright (C) 2009-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl.locale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class InternalLocaleBuilder {

    private static final boolean JDKIMPL = false;

    private String _language = "";
    private String _script = "";
    private String _region = "";
    private String _variant = "";

    private static final String LOCALESEP = "_";
    private static final String PRIVUSE_VARIANT_PREFIX = "jvariant";
    private static final CaseInsensitiveChar PRIVUSE_KEY = new CaseInsensitiveChar(LanguageTag.PRIVATEUSE.charAt(0));

    private HashMap<CaseInsensitiveChar, String> _extensions;
    private HashSet<CaseInsensitiveString> _uattributes;
    private HashMap<CaseInsensitiveString, String> _ukeywords;


    public InternalLocaleBuilder() {
    }

    public InternalLocaleBuilder setLanguage(String language) throws LocaleSyntaxException {
        if (language == null || language.length() == 0) {
            _language = "";
        } else {
            if (!LanguageTag.isLanguage(language)) {
                throw new LocaleSyntaxException("Ill-formed language: " + language, 0);
            }
            _language = language;
        }
        return this;
    }

    public InternalLocaleBuilder setScript(String script) throws LocaleSyntaxException {
        if (script == null || script.length() == 0) {
            _script = "";
        } else {
            if (!LanguageTag.isScript(script)) {
                throw new LocaleSyntaxException("Ill-formed script: " + script, 0);
            }
            _script = script;
        }
        return this;
    }

    public InternalLocaleBuilder setRegion(String region) throws LocaleSyntaxException {
        if (region == null || region.length() == 0) {
            _region = "";
        } else {
            if (!LanguageTag.isRegion(region)) {
                throw new LocaleSyntaxException("Ill-formed region: " + region, 0);
            }
            _region = region;
        }
        return this;
    }

    public InternalLocaleBuilder setVariant(String variant) throws LocaleSyntaxException {
        if (variant == null || variant.length() == 0) {
            _variant = "";
        } else {
            // normalize separators to "_"
            String var = variant.replaceAll(LanguageTag.SEP, LOCALESEP);
            int errIdx = checkVariants(var, LOCALESEP);
            if (errIdx != -1) {
                throw new LocaleSyntaxException("Ill-formed variant: " + variant, errIdx);
            }
        }
        return this;
    }

    public InternalLocaleBuilder addUnicodeLocaleAttribute(String attribute) throws LocaleSyntaxException {
        if (attribute == null || !UnicodeLocaleExtension.isAttribute(attribute)) {
            throw new LocaleSyntaxException("Ill-formed Unicode locale attribute: " + attribute);
        }
        // to lower case to prevent duplication
        if (_uattributes == null) {
            _uattributes = new HashSet<CaseInsensitiveString>(4);
        }
        _uattributes.add(new CaseInsensitiveString(attribute));
        return this;
    }

    public InternalLocaleBuilder removeUnicodeLocaleAttribute(String attribute) throws LocaleSyntaxException {
        if (attribute == null || !UnicodeLocaleExtension.isAttribute(attribute)) {
            throw new LocaleSyntaxException("Ill-formed Unicode locale attribute: " + attribute);
        }
        if (_uattributes != null) {
            _uattributes.remove(new CaseInsensitiveString(attribute));
        }
        return this;
    }

    public InternalLocaleBuilder setUnicodeLocaleKeyword(String key, String type) throws LocaleSyntaxException {
        if (key == null || !UnicodeLocaleExtension.isKey(key)) {
            throw new LocaleSyntaxException("Ill-formed Unicode locale keyword key: " + key);
        }

        CaseInsensitiveString cikey = new CaseInsensitiveString(key);
        if (type == null) {
            if (_ukeywords != null) {
                // null type is used for remove the key
                _ukeywords.remove(cikey);
            }
        } else {
            if (type.length() != 0) {
                // normalize separator to "-"
                String tp = type.replaceAll(LOCALESEP, LanguageTag.SEP);
                // validate
                StringTokenIterator itr = new StringTokenIterator(tp, LanguageTag.SEP);
                while (itr.hasNext()) {
                    String s = itr.next();
                    if (!UnicodeLocaleExtension.isTypeSubtag(s)) {
                        throw new LocaleSyntaxException("Ill-formed Unicode locale keyword type: " + type, itr.currentStart());
                    }
                }
            }
            if (_ukeywords == null) {
                _ukeywords = new HashMap<CaseInsensitiveString, String>(4);
            }
            _ukeywords.put(cikey, type);
        }
        return this;
    }

    public InternalLocaleBuilder setExtension(char singleton, String value) throws LocaleSyntaxException {
        // validate key
        boolean isBcpPrivateuse = LanguageTag.isPrivateusePrefixChar(singleton);
        if (!isBcpPrivateuse && !LanguageTag.isExtensionSingletonChar(singleton)) {
            throw new LocaleSyntaxException("Ill-formed extension key: " + singleton);
        }

        boolean remove = (value == null || value.length() == 0);
        CaseInsensitiveChar key = new CaseInsensitiveChar(singleton);

        if (remove) {
            if (UnicodeLocaleExtension.isSingletonChar(key.value())) {
                // clear entire Unicode locale extension
                if (_uattributes != null) {
                    _uattributes.clear();
                }
                if (_ukeywords != null) {
                    _ukeywords.clear();
                }
            } else {
                if (_extensions != null && _extensions.containsKey(key)) {
                    _extensions.remove(key);
                }
            }
        } else {
            // validate value
            String val = value.replaceAll(LOCALESEP, LanguageTag.SEP);
            StringTokenIterator itr = new StringTokenIterator(val, LanguageTag.SEP);
            while (itr.hasNext()) {
                String s = itr.next();
                boolean validSubtag;
                if (isBcpPrivateuse) {
                    validSubtag = LanguageTag.isPrivateuseSubtag(s);
                } else {
                    validSubtag = LanguageTag.isExtensionSubtag(s);
                }
                if (!validSubtag) {
                    throw new LocaleSyntaxException("Ill-formed extension value: " + s, itr.currentStart());
                }
            }

            if (UnicodeLocaleExtension.isSingletonChar(key.value())) {
                setUnicodeLocaleExtension(val);
            } else {
                if (_extensions == null) {
                    _extensions = new HashMap<CaseInsensitiveChar, String>(4);
                }
                _extensions.put(key, val);
            }
        }
        return this;
    }

    /*
     * Set extension/private subtags in a single string representation
     */
    public InternalLocaleBuilder setExtensions(String subtags) throws LocaleSyntaxException {
        subtags = subtags.replaceAll(LOCALESEP, LanguageTag.SEP);
        StringTokenIterator itr = new StringTokenIterator(subtags, LanguageTag.SEP);

        List<String> extensions = null;
        String privateuse = null;

        int parsed = 0;
        int start;

        // Make a list of extension subtags
        while (!itr.isDone()) {
            String s = itr.current();
            if (LanguageTag.isExtensionSingleton(s)) {
                start = itr.currentStart();
                String singleton = s;
                StringBuilder sb = new StringBuilder(singleton);

                itr.next();
                while (!itr.isDone()) {
                    s = itr.current();
                    if (LanguageTag.isExtensionSubtag(s)) {
                        sb.append(LanguageTag.SEP).append(s);
                        parsed = itr.currentEnd();
                    } else {
                        break;
                    }
                    itr.next();
                }

                if (parsed < start) {
                    throw new LocaleSyntaxException("Incomplete extension '" + singleton + "'", start);
                }

                if (extensions == null) {
                    extensions = new ArrayList<String>(4);
                }
                extensions.add(sb.toString());
            } else {
                break;
            }
        }
        if (!itr.isDone()) {
            String s = itr.current();
            if (LanguageTag.isPrivateusePrefix(s)) {
                start = itr.currentStart();
                StringBuilder sb = new StringBuilder(s);

                itr.next();
                while (!itr.isDone()) {
                    s = itr.current();
                    if (!LanguageTag.isPrivateuseSubtag(s)) {
                        break;
                    }
                    sb.append(LanguageTag.SEP).append(s);
                    parsed = itr.currentEnd();

                    itr.next();
                }
                if (parsed <= start) {
                    throw new LocaleSyntaxException("Incomplete privateuse:" + subtags.substring(start), start);
                } else {
                    privateuse = sb.toString();
                }
            }
        }

        if (!itr.isDone()) {
            throw new LocaleSyntaxException("Ill-formed extension subtags:" + subtags.substring(itr.currentStart()), itr.currentStart());
        }

        return setExtensions(extensions, privateuse);
    }

    /*
     * Set a list of BCP47 extensions and private use subtags
     * BCP47 extensions are already validated and well-formed, but may contain duplicates
     */
    private InternalLocaleBuilder setExtensions(List<String> bcpExtensions, String privateuse) {
        try {
            if (bcpExtensions.size() > 0) {
                HashSet<CaseInsensitiveChar> processedExntensions = new HashSet<CaseInsensitiveChar>(bcpExtensions.size());
                for (String bcpExt : bcpExtensions) {
                    CaseInsensitiveChar key = new CaseInsensitiveChar(bcpExt.charAt(0));
                    // ignore duplicates
                    if (!processedExntensions.contains(key)) {
                        // each extension string contains singleton, e.g. "a-abc-def"
                        setExtension(key.value(), bcpExt.substring(2));
                    }
                }
            }
            if (privateuse != null && privateuse.length() > 0) {
                // privateuse string contains prefix, e.g. "x-abc-def"
                setExtension(privateuse.charAt(0), privateuse.substring(2));
            }
        } catch (LocaleSyntaxException lse) {
            // should never happen...
            throw new RuntimeException(lse);
        }

        return this;
    }

    /*
     * Reset Builder's internal state with the given language tag
     */
    public InternalLocaleBuilder setLanguageTag(LanguageTag langtag) {
        if (langtag.getExtlangs().size() > 0) {
            _language = langtag.getExtlangs().get(0);
        } else {
            String language = langtag.getLanguage();
            if (!language.equals(LanguageTag.UNDETERMINED)) {
                _language = language;
            }
        }
        _script = langtag.getScript();
        _region = langtag.getRegion();

        List<String> bcpVariants = langtag.getVariants();
        if (bcpVariants.size() > 0) {
            StringBuilder var = new StringBuilder(bcpVariants.get(0));
            for (int i = 1; i < bcpVariants.size(); i++) {
                var.append(LOCALESEP).append(bcpVariants.get(i));
            }
            _variant = var.toString();
        }

        setExtensions(langtag.getExtensions(), langtag.getPrivateuse());

        return this;
    }

    public InternalLocaleBuilder setLocale(BaseLocale base, LocaleExtensions extensions) throws LocaleSyntaxException {
        String language = base.getLanguage();
        String script = base.getScript();
        String region = base.getRegion();
        String variant = base.getVariant();

        boolean skipVariantCheck = false;

        if (JDKIMPL) {
            // Special backward compatibility support

            // Exception 1 - ja_JP_JP
            if (language.equals("ja") && region.equals("JP") && (variant.equals("JP") || variant.startsWith("JP_"))) {
                // When locale ja_JP_JP[_*] is created, ca-japanese is always there.
                // In builder, ignore variant "JP". It will be added when an instance of BaseLocale
                // is created by getBaseLocale().
                assert("japanese".equals(extensions.getUnicodeLocaleType("ca")));
                if (variant.startsWith("JP_")) {
                    variant = variant.substring(3);
                } else {
                    variant = "";
                }
            }
            // Exception 2 - th_TH_TH
            else if (language.equals("th") && region.equals("TH") && (variant.equals("TH") || variant.startsWith("TH_"))) {
                // When locale th_TH_TH[_*] is created, nu-thai is always there.
                // In builder, ignore variant "TH". It will be added when an instance of BaseLocale
                // is created by getBaseLocale().
                assert("thai".equals(extensions.getUnicodeLocaleType("nu")));
                if (variant.startsWith("TH_")) {
                    variant = variant.substring(3);
                } else {
                    variant = "";
                }
                
            }
            // Exception 3 - no_NO_NY
            else if (language.equals("no") && region.equals("NO") && variant.equals("NY")) {
                // no_NO_NY is a valid locale and used by Java 6 or older versions.
                // In builder, we preserve Illformed variant "NY", but change language to "nn".
                // If user does not change base locale field, getBaseLocale() maps back "nn"
                // to "no".
                language = "nn";
                skipVariantCheck = true;
            }
        }

        // Validate base locale fields before updating internal state.
        // LocaleExtensions always store validated/canonicalized values,
        // so no checks are necessary.
        if (language.length() > 0 && !LanguageTag.isLanguage(language)) {
            throw new LocaleSyntaxException("Ill-formed language: " + language);
        }

        if (script.length() > 0 && !LanguageTag.isScript(script)) {
            throw new LocaleSyntaxException("Ill-formed script: " + script);
        }

        if (region.length() > 0 && !LanguageTag.isRegion(region)) {
            throw new LocaleSyntaxException("Ill-formed region: " + region);
        }

        if (!skipVariantCheck && variant.length() > 0) {
            int errIdx = checkVariants(variant, LOCALESEP);
            if (errIdx != -1) {
                throw new LocaleSyntaxException("Ill-formed variant: " + variant, errIdx);
            }
        }

        // update builder's internal fields
        _language = language;
        _script = script;
        _region = region;
        _variant = variant;

        Set<Character> extKeys = (extensions == null) ? null : extensions.getKeys();
        if (extKeys != null) {
            // map extensions back to builder's internal format
            for (Character key : extKeys) {
                Extension e = extensions.getExtension(key);
                if (e instanceof UnicodeLocaleExtension) {
                    UnicodeLocaleExtension ue = (UnicodeLocaleExtension)e;
                    for (String uatr : ue.getUnicodeLocaleAttributes()) {
                        if (_uattributes == null) {
                            _uattributes = new HashSet<CaseInsensitiveString>(4);
                        }
                        _uattributes.add(new CaseInsensitiveString(uatr));
                    }
                    for (String ukey : ue.getUnicodeLocaleKeys()) {
                        if (_ukeywords == null) {
                            _ukeywords = new HashMap<CaseInsensitiveString, String>(4);
                        }
                        _ukeywords.put(new CaseInsensitiveString(ukey), ue.getUnicodeLocaleType(ukey));
                    }
                } else {
                    if (_extensions == null) {
                        _extensions = new HashMap<CaseInsensitiveChar, String>(4);
                    }
                    _extensions.put(new CaseInsensitiveChar(key.charValue()), e.getValue());
                }
            }
        }
        return this;
    }

    public InternalLocaleBuilder clear() {
        _language = "";
        _script = "";
        _region = "";
        _variant = "";
        clearExtensions();
        return this;
    }

    public InternalLocaleBuilder clearExtensions() {
        if (_extensions != null) {
            _extensions.clear();
        }
        if (_uattributes != null) {
            _uattributes.clear();
        }
        if (_ukeywords != null) {
            _ukeywords.clear();
        }
        return this;
    }

    public BaseLocale getBaseLocale() {
        String language = _language;
        String script = _script;
        String region = _region;
        String variant = _variant;

        // Special private use subtag sequence identified by "jvariant" will be
        // interpreted as Java variant.
        if (_extensions != null) {
            String privuse = _extensions.get(PRIVUSE_KEY);
            if (privuse != null) {
                StringTokenIterator itr = new StringTokenIterator(privuse, LanguageTag.SEP);
                boolean sawPrefix = false;
                int privVarStart = -1;
                while (!itr.isDone()) {
                    if (sawPrefix) {
                        privVarStart = itr.currentStart();
                        break;
                    }
                    if (AsciiUtil.caseIgnoreMatch(itr.current(), PRIVUSE_VARIANT_PREFIX)) {
                        sawPrefix = true;
                    }
                    itr.next();
                }
                if (privVarStart != -1) {
                    StringBuilder sb = new StringBuilder(variant);
                    if (sb.length() != 0) {
                        sb.append(LOCALESEP);
                    }
                    sb.append(privuse.substring(privVarStart).replaceAll(LanguageTag.SEP, LOCALESEP));
                    variant = sb.toString();
                }
            }
        }

        if (JDKIMPL) {
            // Special backward compatibility support

            // Exception 1 - ja_JP_JP
            if (language.equals("ja") && region.equals("JP") && isUnicodeLocaleType("ca", "japanese")) {
                // When Unicode keyword "ca-japanese" is set and language is "ja" and
                // region is "JP", insert ill-formed variant "JP" at the beginning of variant.
                if (variant.length() == 0) {
                    variant = "JP";
                } else if (!variant.equals("JP") && !variant.startsWith("JP_")) {
                    variant = "JP_" + variant;
                }
            }
            // Exception 2 - th_TH_TH
            else if (language.equals("th") && region.equals("TH") && isUnicodeLocaleType("th", "thai")) {
                // When Unicode keyword "nu-thai" is set and language is "th" and
                // region is "TH", insert ill-formed variant "TH" at the beginning of variant.
                if (variant.length() == 0) {
                    variant = "TH";
                } else if (!variant.equals("TH") && !variant.startsWith("TH_")) {
                    variant = "TH_" + variant;
                }
            }
            // Exception 3 - no_NO_NY
            else if (variant.equals("NY")) {
                // Variant "NY" is only there when this builder is initialized by setLocale with no_NO_NY.
                // setLocale changes language "no" to "nn" for the case. If no base locale field had change,
                // this implementation map the language back to "no", so
                //   new InternalLocaleBuilder().setLocale(new Locale("no","NO","NY").getBaseLocale()
                // can restore the original locale.
                if (language.equals("nn") && script.equals("") && region.equals("NO")) {
                    language = "no";
                } else {
                    // all other cases, drop the variant "NY"
                    variant = "";
                }
            }
        }

        return BaseLocale.getInstance(language, script, region, variant);
    }

    public LocaleExtensions getLocaleExtensions() {
        if ((_extensions == null || _extensions.size() == 0)
                && (_uattributes == null || _uattributes.size() == 0)
                && (_ukeywords == null || _ukeywords.size() == 0)) {
            return LocaleExtensions.EMPTY_EXTENSIONS;
        }

        return new LocaleExtensions(_extensions, _uattributes, _ukeywords);
    }

    /*
     * Remove special private use subtag sequence identified by "jvariant"
     * and return the rest. Only used by LocaleExtensions
     */
    static String removePrivateuseVariant(String privuseVal) {
        StringTokenIterator itr = new StringTokenIterator(privuseVal, LanguageTag.SEP);

        // Note: privateuse value "abc-jvariant" is unchanged
        // because no subtags after "jvariant".

        int prefixStart = -1;
        boolean sawPrivuseVar = false;;
        while (!itr.isDone()) {
            if (prefixStart != -1) {
                // Note: privateuse value "abc-jvariant" is unchanged
                // because no subtags after "jvariant".
                sawPrivuseVar = true;
                break;
            }
            if (AsciiUtil.caseIgnoreMatch(itr.current(), PRIVUSE_VARIANT_PREFIX)) {
                prefixStart = itr.currentStart();
            }
            itr.next();
        }
        if (!sawPrivuseVar) {
            return privuseVal;
        }

        assert(prefixStart == 0 || prefixStart > 1);
        return (prefixStart == 0) ? null : privuseVal.substring(0, prefixStart -1);
    }

    /*
     * Check if the given variant subtags separated by the given
     * separator(s) are valid
     */
    private int checkVariants(String variants, String sep) {
        StringTokenIterator itr = new StringTokenIterator(variants, sep);
        while (itr.hasNext()) {
            String s = itr.next();
            if (!LanguageTag.isVariant(s)) {
                return itr.currentStart();
            }
        }
        return -1;
    }

    /*
     * Private methods parsing Unicode Locale Extension subtags.
     * Duplicated attributes/keywords will be ignored.
     * The input must be a valid extension subtags (excluding singleton).
     */
    private void setUnicodeLocaleExtension(String subtags) {
        StringTokenIterator itr = new StringTokenIterator(subtags, LanguageTag.SEP);

        // parse attributes
        while (!itr.isDone()) {
            if (!UnicodeLocaleExtension.isAttribute(itr.current())) {
                break;
            }
            if (_uattributes == null) {
                _uattributes = new HashSet<CaseInsensitiveString>(4);
            }
            _uattributes.add(new CaseInsensitiveString(itr.current()));
            itr.next();
        }

        // parse keywords
        CaseInsensitiveString key = null;
        String type;
        int typeStart = -1;
        int typeEnd = -1;
        while (!itr.isDone()) {
            if (key != null) {
                if (UnicodeLocaleExtension.isKey(itr.current())) {
                    // next keyword - emit previous one
                    assert(typeStart == -1 || typeEnd != -1);
                    type = (typeStart == -1) ? "" : subtags.substring(typeStart, typeEnd);
                    if (_ukeywords == null) {
                        _ukeywords = new HashMap<CaseInsensitiveString, String>(4);
                    }
                    _ukeywords.put(key, type);

                    // reset keyword info
                    String tmpKey = itr.current();
                    key = _ukeywords.containsKey(tmpKey) ? null : new CaseInsensitiveString(tmpKey);
                    typeStart = typeEnd = -1;
                } else {
                    if (typeStart == -1) {
                        typeStart = itr.currentStart();
                    }
                    typeEnd = itr.currentEnd();
                }
            } else if (UnicodeLocaleExtension.isKey(itr.current())) {
                // 1. first keyword or
                // 2. next keyword, but previous one was duplicate
                key = new CaseInsensitiveString(itr.current());
                if (_ukeywords != null && _ukeywords.containsKey(key)) {
                    // duplicate
                    key = null;
                }
            }

            if (!itr.hasNext()) {
                if (key != null) {
                    // last keyword
                    assert(typeStart == -1 || typeEnd != -1);
                    type = (typeStart == -1) ? "" : subtags.substring(typeStart, typeEnd);
                    if (_ukeywords == null) {
                        _ukeywords = new HashMap<CaseInsensitiveString, String>(4);
                    }
                    _ukeywords.put(key, type);
                }
                break;
            }

            itr.next();
        }
    }

    /*
     * Private helper function for checking Unicode locale keyword type
     */
    private boolean isUnicodeLocaleType(String key, String type) {
        if (_ukeywords != null) {
            String t = _ukeywords.get(new CaseInsensitiveString(key));
            if (t != null) {
                return AsciiUtil.caseIgnoreMatch(t, type);
            }
        }
        return false;
    }

    static class CaseInsensitiveString {
        private String _s;

        CaseInsensitiveString(String s) {
            _s = s;
        }

        public String value() {
            return _s;
        }

        public int hashCode() {
            return AsciiUtil.toLowerString(_s).hashCode();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CaseInsensitiveString)) {
                return false;
            }
            return AsciiUtil.caseIgnoreMatch(_s, ((CaseInsensitiveString)obj).value());
        }
    }

    static class CaseInsensitiveChar {
        private char _c;

        CaseInsensitiveChar(char c) {
            _c = c;
        }

        public char value() {
            return _c;
        }

        public int hashCode() {
            return AsciiUtil.toLower(_c);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CaseInsensitiveChar)) {
                return false;
            }
            return _c ==  AsciiUtil.toLower(((CaseInsensitiveChar)obj).value());
        }

    }
}
