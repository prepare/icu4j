/**
*******************************************************************************
* Copyright (C) 2002, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
* $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/dev/tool/docs/ICUTaglet.java,v $ 
* $Date: 2002/12/17 07:31:26 $ 
* $Revision: 1.2 $
*
*******************************************************************************
*/
package com.ibm.icu.dev.tool.docs;

import com.sun.javadoc.*;
import com.sun.tools.doclets.*;

import java.text.BreakIterator;
import java.util.Locale;
import java.util.Map;

public abstract class ICUTaglet implements Taglet {
    protected final String name;
    protected final int mask;

    protected static final int MASK_FIELD = 1;
    protected static final int MASK_CONSTRUCTOR = 2;
    protected static final int MASK_METHOD = 4;
    protected static final int MASK_OVERVIEW = 8;
    protected static final int MASK_PACKAGE = 16;
    protected static final int MASK_TYPE = 32;
    protected static final int MASK_INLINE = 64;

    protected static final int MASK_DEFAULT = 0x003f; // no inline
    protected static final int MASK_VALID = 0x007f; // includes inline

    public static void register(Map taglets) {
        ICUInternalTaglet.register(taglets);
        ICUDraftTaglet.register(taglets);
        ICUStableTaglet.register(taglets);
        ICUDeprecatedTaglet.register(taglets);
        ICUObsoleteTaglet.register(taglets);
    }

    protected ICUTaglet(String name, int mask) {
        this.name = name;
        this.mask = mask & MASK_VALID;
    }

    public boolean inField() {
        return (mask & MASK_FIELD) != 0;
    }

    public boolean inConstructor() {
        return (mask & MASK_FIELD) != 0;
    }

    public boolean inMethod() {
        return (mask & MASK_FIELD) != 0;
    }

    public boolean inOverview() {
        return (mask & MASK_FIELD) != 0;
    }

    public boolean inPackage() {
        return (mask & MASK_FIELD) != 0;
    }

    public boolean inType() {
        return (mask & MASK_FIELD) != 0;
    }

    public boolean isInlineTag() {
        return (mask & MASK_FIELD) != 0;
    }

    public String getName() {
        return name;
    }

    public String toString(Tag tag) {
        return tag.text();
    }

    public String toString(Tag[] tags) {
        if (tags != null) {
            if (tags.length > 1) {
                String msg = "Should not have more than one ICU tag per element:\n";
                for (int i = 0; i < tags.length; ++i) {
                    msg += "  [" + i + "] " + tags[i] + "\n";
                }
                throw new InternalError(msg);
            } else if (tags.length > 0) {
                return toString(tags[0]);
            }
        }
        return null;
    }

    protected static final String STATUS = "<dt><b>Status:</b></dt>";

    public static class ICUInternalTaglet extends ICUTaglet {
        private static final String NAME = "internal";

        public static void register(Map taglets) {
            taglets.put(NAME, new ICUInternalTaglet());
        }

        private ICUInternalTaglet() {
            super(NAME, MASK_DEFAULT);
        }

        public String toString(Tag tag) {
            return STATUS + "<dd><em>Internal</em>. <font color='red'>This API is <em>Internal Only</em> and can change at any time.</font></dd>";
        }
    }

    public static class ICUDraftTaglet extends ICUTaglet {
        private static final String NAME = "draft";

        public static void register(Map taglets) {
            taglets.put(NAME, new ICUDraftTaglet());
        }

        private ICUDraftTaglet() {
            super(NAME, MASK_DEFAULT);
        }

        public String toString(Tag tag) {
            String text = tag.text();
            if (text.length() == 0) {
                System.err.println("Warning: empty draft tag");
            }
            return STATUS + "<dd>Draft " + tag.text() + ".</dd>";
        }
    }

    public static class ICUStableTaglet extends ICUTaglet {
        private static final String NAME = "stable";

        public static void register(Map taglets) {
            taglets.put(NAME, new ICUStableTaglet());
        }

        private ICUStableTaglet() {
            super(NAME, MASK_DEFAULT);
        }

        public String toString(Tag tag) {
            String text = tag.text();
            if (text.length() > 0) {
                return STATUS + "<dd>Stable " + text + ".</dd>";
            } else {
                return STATUS + "<dd>Stable.</dd>";
            }
        }
    }

    public static class ICUDeprecatedTaglet extends ICUTaglet {
        private static final String NAME = "deprecated";

        public static void register(Map taglets) {
            taglets.put(NAME, new ICUDeprecatedTaglet());
        }

        private ICUDeprecatedTaglet() {
            super(NAME, MASK_DEFAULT);
        }

        public String toString(Tag tag) {
            BreakIterator bi = BreakIterator.getSentenceInstance(Locale.US);
            String text = tag.text();
            bi.setText(text);
            int first = bi.first();
            int next = bi.next();
            if (first == -1 || next == -1) {
                System.err.println("Warning: bad deprecated tag '" + text + "'");
                return STATUS + "<dd><em>Deprecated</em>. " + text + "</dd>";
            } else {
                return STATUS + "<dd><em>Deprecated in " + text.substring(first, next) + "</em>. " + text.substring(next) + "</dd>";
            }
        }
    }

    public static class ICUObsoleteTaglet extends ICUTaglet {
        private static final String NAME = "obsolete";

        public static void register(Map taglets) {
            taglets.put(NAME, new ICUObsoleteTaglet());
        }

        private ICUObsoleteTaglet() {
            super(NAME, MASK_DEFAULT);
        }

        public String toString(Tag tag) {
            BreakIterator bi = BreakIterator.getSentenceInstance(Locale.US);
            String text = tag.text();
            bi.setText(text);
            int first = bi.first();
            int next = bi.next();
            return STATUS + "<dd><em>Obsolete.</em> <font color='red'>Will be removed in " + text.substring(first, next) + "</font>. " + text.substring(next) + "</dd>";

        }
    }
}
