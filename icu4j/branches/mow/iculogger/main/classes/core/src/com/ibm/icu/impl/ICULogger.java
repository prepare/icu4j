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
 *
 * Extends the Java Logger class adding a method to turn off/on logging.
 * Classes where logging is wanted contains a static ICULogger object
 * with logging turned off by default.
 * <p>
 * To use logging, the system property "debug.logging" must be set to "on",
 * otherwise the static ICULogger object will be null. This will help lower
 * any unneccessary resource usage when logging is not desired.
 * 
 * @author       Michael Ow
 * @draft ICU 4.4
 */
public class ICULogger extends Logger {
    private final static boolean LOGGER_OFF = false;
    private final static boolean LOGGER_ON = true;
    
    private boolean status;
    
    /**
     * ICULogger constructor that calls the parent constructor with the desired parameters.
     */
    private ICULogger(String name, String resourceBundleName) {
        super(name, resourceBundleName);
    }
    
    /**
     * Set the status to either on or off. If status is switching from off to on
     * set the level of the logger to INFO.
     */
    private void setStatus(boolean newStatus) {
        if (status != newStatus) {
            status = newStatus;
            
            /* Default to level INFO. */
            if (status == LOGGER_ON) {
                this.setLevel(Level.INFO);
            } else {
                this.setLevel(Level.OFF);
            }
        }
    }
    
    /**
     * Check the system property "debug.logging" to see if it is set to on.
     * return true if it is otherwise return false.
     */
    private static boolean checkGlobalLoggingFlag() {
        String loggingProp = System.getProperty("debug.logging");
        if (loggingProp != null && loggingProp.equals("on")) {
            return true;
        }
        return false;
    }
    
    /**
     * Instantiates a new ICULogger object with logging turned off by default.
     *
     * @param name to be use by the logger (usually is the class name)
     * @return a new ICULogger object
     * @draft ICU 4.4
     */
    public static ICULogger getICULogger(String name) {
        return getICULogger(name, null);
    }
    
    /**
     * Instantiates a new ICULogger object with logging turned off by default
     *
     * @param name to be use by the logger (usually is the class name)
     * @param ResourceBundle name to localize messages (can be null)
     * @return a new ICULogger object
     * @draft ICU 4.4
     */
    public static ICULogger getICULogger(String name, String resourceBundleName) {
        if (checkGlobalLoggingFlag()) {
            ICULogger logger = new ICULogger(name, resourceBundleName);
            /* Turn off logging by default. */
            logger.turnOffLogging();
            return logger;
        }
        return null;
    }
    
    /**
     * Determined if logging is turned on or off. The return value is true if logging is on.
     *
     * @return whether logging is turned on or off.
     * @draft ICU 4.4
     */
    public boolean isLoggingOn() {
        return status;
    }
    
    /**
     * Turn loggin on.
     *
     * @draft ICU 4.4
     */
    public void turnOnLogging() {
        setStatus(LOGGER_ON);
    }
    
    /**
     * Turn logging off.
     *
     * @draft ICU 4.4
     */
    public void turnOffLogging() {
        setStatus(LOGGER_OFF);
    }
    
}
