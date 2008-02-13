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
public class DateInterval implements Serializable {

    private long fromDate;
    private long toDate;

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
     * Set the from date.
     * @param date  The from date to be set in date interval.
     * @draft ICU 4.0
     */
    public void setFromDate(long date)
    {
        fromDate = date;
    }
 

    /**
     * Set the to date.
     * @param date  The to date to be set in date interval.
     * @draft ICU 4.0
     */
    public void setToDate(long date)
    {
        toDate = date;
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
