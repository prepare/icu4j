/*
*   Copyright (C) 2008, International Business Machines
*   Corporation and others.  All Rights Reserved.
*/

package com.ibm.icu.util;

import java.util.Date;
import java.io.Serializable;


/**
 * This class represents date interval.
 * It is a pair of long representing from long 1 to long 2.
 * @draft ICU 4.0
**/
public final class DateInterval implements Serializable {

    private static final long serialVersionUID = 1;

    private final long fromDate;
    private final long toDate;

    /** 
     * Constructor given from date and to date.
     * @param from      The from date in date interval.
     * @param to        The to date in date interval.
     * @draft ICU 4.0
     */
    public DateInterval(long from, long to)
    {
        fromDate = from;
        toDate = to;
    }

    /** 
     * Get the from date.
     * @return  the from date in dateInterval.
     * @draft ICU 4.0
     */
    public long getFromDate()
    {
        return fromDate;
    }

    /** 
     * Get the to date.
     * @return  the to date in dateInterval.
     * @draft ICU 4.0
     */
    public long getToDate()
    {
        return toDate;
    }

} ;// end class DateInterval
