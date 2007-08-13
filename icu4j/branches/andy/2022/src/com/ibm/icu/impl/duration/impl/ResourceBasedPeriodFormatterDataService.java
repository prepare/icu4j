/*
******************************************************************************
* Copyright (C) 2007, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

package com.ibm.icu.impl.duration.impl;

import java.util.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.io.OutputStreamWriter;

/**
 * A PeriodFormatterDataService that serves PeriodFormatterData
 * objects based on data files stored as resources in this directory.
 * These are text files named after the locale, for example,
 * 'pfd_he_IL.txt' specifies an period formatter data file for Hebrew
 * as spoken in Israel.  Data is in a JSON-like format.
 */
public class ResourceBasedPeriodFormatterDataService 
    extends PeriodFormatterDataService {
  private Collection  availableLocales; // of String

  private PeriodFormatterData lastData = null;
  private String lastLocale = null;
  private Map cache = new HashMap(); // String -> PeriodFormatterData
  private PeriodFormatterData fallbackFormatterData;

  private static final String PATH = "data/";

  private static final ResourceBasedPeriodFormatterDataService singleton = 
    new ResourceBasedPeriodFormatterDataService();

  /**
   * Returns the singleton instance of this class.
   */
  public static ResourceBasedPeriodFormatterDataService getInstance() {
    return singleton;
  }

  /**
   * Constructs the service.
   */
  private ResourceBasedPeriodFormatterDataService() {
    List localeNames = new ArrayList(); // of String
    try {
      InputStream is = getClass().getResourceAsStream(PATH + "index.txt");
      if (is == null) {
        System.err.println("could not load index.txt");
      } else {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String string = null;
        while (null != (string = br.readLine())) {
          string = string.trim();
          if(string.startsWith("#") || string.length()==0) {
              continue;
          }
          localeNames.add(string);
        }
      }
    }
    catch (Exception e) {
      System.err.println(e.getMessage());
    }
    availableLocales = Collections.unmodifiableList(localeNames);
  }

  public PeriodFormatterData get(String localeName) {
    synchronized(this) {
      if (lastLocale != null && lastLocale.equals(localeName)) {
        return lastData;
      }

      PeriodFormatterData ld = (PeriodFormatterData)cache.get(localeName);
      if (ld == null) {
        String ln = localeName;
        while (!availableLocales.contains(ln)) {
          int ix = ln.lastIndexOf("_");
          if (ix > -1) {
            ln = ln.substring(0, ix);
          } else if (!"test".equals(ln)) {
            ln = "test";
          } else {
            ln = null;
            break;
          }
        }
        if (ln != null) {
          try {
            String name = PATH + "pfd_" + ln + ".xml";
            InputStream is = getClass().getResourceAsStream(name);
            if (is == null) {
              System.err.println("no resource named " + name);
            } else {
              DataRecord dr = DataRecord.read(ln,
                  new XMLRecordReader(
                      new InputStreamReader(is, "UTF-8")));
              if (dr != null) {
		  // debug
                if (false && ln.equals("ar_EG")) {
                  OutputStreamWriter osw = new OutputStreamWriter(System.out, "UTF-8");
                  XMLRecordWriter xrw = new XMLRecordWriter(osw);
                  dr.write(xrw);
                  osw.flush();
                }
                ld = new PeriodFormatterData(localeName,dr);
              }
            }
          } 
          catch (Exception e) {
            System.err.println(e);
          }
        }

        if (ld == null) {
          ld = getFallbackFormatterData();
        }
        cache.put(localeName, ld);
      }
      lastData = ld;
      lastLocale = localeName;

      return ld;
    }
  }
  
  public Collection  getAvailableLocales() {
    return availableLocales;
  }

  PeriodFormatterData getFallbackFormatterData() {
    synchronized (this) {
      if (fallbackFormatterData == null) {
        DataRecord dr = new DataRecord(); // hack, no default, will die if used
        fallbackFormatterData = new PeriodFormatterData(null, dr);
      }
      return fallbackFormatterData;
    }
  }
}
