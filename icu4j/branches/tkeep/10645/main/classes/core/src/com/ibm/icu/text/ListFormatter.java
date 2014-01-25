/*
 *******************************************************************************
 * Copyright (C) 2012-2014, Google, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package com.ibm.icu.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import com.ibm.icu.impl.ICUCache;
import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.SimpleCache;
import com.ibm.icu.impl.Template;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.UResourceBundle;

/**
 * Immutable class for formatting a list, using data from CLDR (or supplied
 * separately). The class is not subclassable.
 *
 * @author Mark Davis
 * @draft ICU 50
 * @provisional This API might change or be removed in a future release.
 */
final public class ListFormatter {
    private final Template two;
    private final Template start;
    private final Template middle;
    private final Template end;
    private final ULocale locale;
    
    /**
     * Indicates the style of Listformatter
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public enum Style {
        /**
         * Standard style.
         * @internal
         * @deprecated This API is ICU internal only.
         */
        STANDARD("standard"),
        /**
         * Style for full durations
         * @internal
         * @deprecated This API is ICU internal only.
         */
        DURATION("unit"),
        /**
         * Style for durations in abbrevated form
         * @internal
         * @deprecated This API is ICU internal only.
         */
        DURATION_SHORT("unit-short"),
        /**
         * Style for durations in narrow form
         * @internal
         * @deprecated This API is ICU internal only.
         */
        DURATION_NARROW("unit-narrow");
        
        private final String name;
        
        Style(String name) {
            this.name = name;
        }
        /**
         * @internal
         * @deprecated This API is ICU internal only.
         */
        public String getName() {
            return name;
        }
        
    }

    /**
     * <b>Internal:</b> Create a ListFormatter from component strings,
     * with definitions as in LDML.
     *
     * @param two
     *            string for two items, containing {0} for the first, and {1}
     *            for the second.
     * @param start
     *            string for the start of a list items, containing {0} for the
     *            first, and {1} for the rest.
     * @param middle
     *            string for the start of a list items, containing {0} for the
     *            first part of the list, and {1} for the rest of the list.
     * @param end
     *            string for the end of a list items, containing {0} for the
     *            first part of the list, and {1} for the last item.
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public ListFormatter(String two, String start, String middle, String end) {
        this(
                Template.compile(two),
                Template.compile(start),
                Template.compile(middle),
                Template.compile(end),
                null);
        
    }
    
    private ListFormatter(Template two, Template start, Template middle, Template end, ULocale locale) {
        this.two = two;
        this.start = start;
        this.middle = middle;
        this.end = end;
        this.locale = locale;
    }

    /**
     * Create a list formatter that is appropriate for a locale.
     *
     * @param locale
     *            the locale in question.
     * @return ListFormatter
     * @draft ICU 50
     * @provisional This API might change or be removed in a future release.
     */
    public static ListFormatter getInstance(ULocale locale) {
      return getInstance(locale, Style.STANDARD);
    }

    /**
     * Create a list formatter that is appropriate for a locale.
     *
     * @param locale
     *            the locale in question.
     * @return ListFormatter
     * @draft ICU 50
     * @provisional This API might change or be removed in a future release.
     */
    public static ListFormatter getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale), Style.STANDARD);
    }
    
    /**
     * Create a list formatter that is appropriate for a locale and style.
     *
     * @param locale the locale in question.
     * @param style the style
     * @return ListFormatter
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public static ListFormatter getInstance(ULocale locale, Style style) {
        return cache.get(locale, style.getName());
    }

    /**
     * Create a list formatter that is appropriate for the default FORMAT locale.
     *
     * @return ListFormatter
     * @draft ICU 50
     * @provisional This API might change or be removed in a future release.
     */
    public static ListFormatter getInstance() {
        return getInstance(ULocale.getDefault(ULocale.Category.FORMAT));
    }

    /**
     * Format a list of objects.
     *
     * @param items
     *            items to format. The toString() method is called on each.
     * @return items formatted into a string
     * @draft ICU 50
     * @provisional This API might change or be removed in a future release.
     */
    public String format(Object... items) {
        return format(Arrays.asList(items));
    }

    /**
     * Format a collection of objects. The toString() method is called on each.
     *
     * @param items
     *            items to format. The toString() method is called on each.
     * @return items formatted into a string
     * @draft ICU 50
     * @provisional This API might change or be removed in a future release.
     */
    public String format(Collection<?> items) {
        // TODO optimize this for the common case that the patterns are all of the
        // form {0}<sometext>{1}.
        // We avoid MessageFormat, because there is no "sub" formatting.
        return format(items, -1).toString();
    }
    
    // Formats a collection of objects and returns the formatted string plus the offset
    // in the string where the index th element appears. index is zero based. If index is
    // negative or greater than or equal to the size of items then this function returns -1 for
    // the offset.
    FormattedListBuilder format(Collection<?> items, int index) {
        Iterator<?> it = items.iterator();
        int count = items.size();
        switch (count) {
        case 0:
            return new FormattedListBuilder("", false);
        case 1:
            return new FormattedListBuilder(it.next(), index == 0);
        case 2:
            return new FormattedListBuilder(it.next(), index == 0).append(two, it.next(), index == 1);
        }
        FormattedListBuilder builder = new FormattedListBuilder(it.next(), index == 0);
        builder.append(start, it.next(), index == 1);
        for (int idx = 2; idx < count - 1; ++idx) {
            builder.append(middle, it.next(), index == idx);
        }
        return builder.append(end, it.next(), index == count - 1);
    }
    
    /**
     * Returns the pattern to use for a particular item count.
     * @param count the item count.
     * @return the pattern with {0}, {1}, {2}, etc. For English,
     * getPatternForNumItems(3) == "{0}, {1}, and {2}"
     * @throws IllegalArgumentException when count is 0 or negative.
     * @draft ICU 52
     * @provisional This API might change or be removed in a future release.
     */
    public String getPatternForNumItems(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be > 0");
        }
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            list.add(String.format("{%d}", i));
        }
        return format(list);
    }
    
    /**
     * Returns the locale of this object.
     * @internal
     * @deprecated This API is ICU internal only.
     */
    public ULocale getLocale() {
        return locale;
    }
    
    // Builds a formatted list
    static class FormattedListBuilder {
        private String current;
        private int offset;
        
        // Start is the first object in the list; If recordOffset is true, records the offset of
        // this first object.
        public FormattedListBuilder(Object start, boolean recordOffset) {
            this.current = start.toString();
            this.offset = recordOffset ? 0 : -1;
        }
        
        // Appends additional object. pattern is a template indicating where the new object gets
        // added in relation to the rest of the list. {0} represents the rest of the list; {1}
        // represents the new object in pattern. next is the object to be added. If recordOffset
        // is true, records the offset of next in the formatted string.
        public FormattedListBuilder append(Template pattern, Object next, boolean recordOffset) {
            if (!pattern.has(0) || !pattern.has(1)) {
                throw new IllegalArgumentException("Missing {0} or {1} in pattern " + pattern);
            }
            if (recordOffset || offsetRecorded()) {
                Template.Evaluation evaluation = pattern.evaluateFull(current, next);
                if (recordOffset) {
                    offset = evaluation.getOffset(1);
                } else {
                    offset += evaluation.getOffset(0);
                }
                current = evaluation.toString();
            } else {
                current = pattern.evaluate(current, next);
            }
            return this;
        }

        @Override
        public String toString() {
            return current;
        }
        
        // Gets the last recorded offset or -1 if no offset recorded.
        public int getOffset() {
            return offset;
        }
        
        private boolean offsetRecorded() {
            return offset >= 0;
        }
    }

    /** JUST FOR DEVELOPMENT */
    // For use with the hard-coded data
    // TODO Replace by use of RB
    // Verify in building that all of the patterns contain {0}, {1}.

    static Map<ULocale, ListFormatter> localeToData = new HashMap<ULocale, ListFormatter>();
    static void add(String locale, String...data) {
        localeToData.put(new ULocale(locale), new ListFormatter(data[0], data[1], data[2], data[3]));
    }

    private static class Cache {
        private final ICUCache<String, ListFormatter> cache =
            new SimpleCache<String, ListFormatter>();

        public ListFormatter get(ULocale locale, String style) {
            String key = String.format("%s:%s", locale.toString(), style);
            ListFormatter result = cache.get(key);
            if (result == null) {
                result = load(locale, style);
                cache.put(key, result);
            }
            return result;
        }

        private static ListFormatter load(ULocale ulocale, String style) {
            ICUResourceBundle r = (ICUResourceBundle)UResourceBundle.
                    getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, ulocale);
            // TODO(Travis Keep): This try-catch is a hack to cover missing aliases
            // for listPattern/duration and listPattern/duration-narrow in root.txt.
            try {
                return new ListFormatter(
                    Template.compile(r.getWithFallback("listPattern/" + style + "/2").getString()),
                    Template.compile(r.getWithFallback("listPattern/" + style + "/start").getString()),
                    Template.compile(r.getWithFallback("listPattern/" + style + "/middle").getString()),
                    Template.compile(r.getWithFallback("listPattern/" + style + "/end").getString()),
                    ulocale);
            } catch (MissingResourceException e) {
                return new ListFormatter(
                        Template.compile(r.getWithFallback("listPattern/standard/2").getString()),
                        Template.compile(r.getWithFallback("listPattern/standard/start").getString()),
                        Template.compile(r.getWithFallback("listPattern/standard/middle").getString()),
                        Template.compile(r.getWithFallback("listPattern/standard/end").getString()),
                        ulocale);
            }
        }
    }

    static Cache cache = new Cache();
}
