/*
 *******************************************************************************
 * Copyright (C) 2009, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.impl;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * @author Michael Ow
 *
 * Extends the Java Logger class adding a method to turn off/on logging.
 * Classes that have logging enabled contains a static ICULogger object
 * with logging turned off by default. 
 */
public class ICULogger extends Logger {
    private final static boolean LOGGER_OFF = false;
    private final static boolean LOGGER_ON = true;
    
    private boolean status;
    
    private ICULogger(String name, String resourceBundleName) {
        super(name, resourceBundleName);
    }
    
    private void SetStatus(boolean newStatus) {
        status = newStatus;
        
        /* Default to level INFO. */
        if (status == LOGGER_ON) {
            this.setLevel(Level.INFO);
        } else {
            this.setLevel(Level.OFF);
        }
    }
    
    public static ICULogger getICULogger(String name) {
        return getICULogger(name, null);
    }
    
    public static ICULogger getICULogger(String name, String resourceBundleName) {
        ICULogger logger = new ICULogger(name, resourceBundleName);
        /* Turn off logging by default. */
        logger.TurnOffLogging();
        return logger;
    }
    
    public void TurnOnLogging() {
        SetStatus(LOGGER_ON);
    }
    
    public void TurnOffLogging() {
        SetStatus(LOGGER_OFF);
    }
    
}
