/*
 * @(#)$RCSfile: Validation.java,v $ $Revision: 1.1 $ $Date: 2000/04/20 17:45:10 $
 *
 * (C) Copyright IBM Corp. 1998-1999.  All Rights Reserved.
 *
 * The program is provided "as is" without any warranty express or
 * implied, including the warranty of non-infringement and the implied
 * warranties of merchantibility and fitness for a particular purpose.
 * IBM will not be liable for any damages suffered by you as a result
 * of using the Program. In no event will IBM be liable for any
 * special, indirect or consequential damages or lost profits even if
 * IBM has been advised of the possibility of their occurrence. IBM
 * will not be liable for any third party claims against you.
 */
package com.ibm.richtext.styledtext;

/**
 * Iterators use this class to keep from getting out of sync with
 * their underlying data.  When created, the iterator gets a
 * Validation instance.  If the underlying data changes, the Validation
 * becomes invalid.  Usually iterators will throw exceptions if accessed
 * after becoming invalid.
 */
final class Validation {

    static final String COPYRIGHT =
                "(C) Copyright IBM Corp. 1998-1999 - All Rights Reserved";
    private boolean fIsValid = true;

    boolean isValid() {

        return fIsValid;
    }

    void invalidate() {

        fIsValid = false;
    }
}
