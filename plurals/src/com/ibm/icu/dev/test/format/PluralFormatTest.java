/**
 * 
 */
package com.ibm.icu.dev.test.format;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.text.PluralFormat;
import com.ibm.icu.util.ULocale;


import java.util.HashMap;
import java.util.Map;

/**
 * @author tschumann (Tim Schumann)
 *
 */
public class PluralFormatTest extends TestFmwk {
  
  public static void main(String[] args) throws Exception {
    new PluralFormatTest().run(args);
  }
  
  private void helperTestRules(String localeIDs, String testPattern, Map changes) {
    String[] locales = localeIDs.split(",");
    
    // Create example outputs for all supported locales.
    /*
    System.out.println("\n" + localeIDs);
    String lastValue = (String) changes.get(new Integer(0));
    int  lastNumber = 0; 
    
    for (int i = 1; i < 199; ++i) {
        if (changes.get(new Integer(i)) != null) {
            if (lastNumber == i-1) {
                System.out.println(lastNumber + ": " + lastValue);
            } else {
                System.out.println(lastNumber + "... " + (i-1) + ": " + lastValue);
            }
            lastNumber = i;
            lastValue = (String) changes.get(new Integer(i));
        }
    }
    System.out.println(lastNumber + "..." + 199 + ": " + lastValue);
    */
    log("test pattern: '" + testPattern + "'");
    for (int i = 0; i < locales.length; ++i) {
      PluralFormat plf = new PluralFormat(new ULocale(locales[i]), testPattern);
      log("plf: " + plf);
      String expected = (String) changes.get(new Integer(0));
      for (int n = 0; n < 200; ++n) {
        if (changes.get(new Integer(n)) != null) {
          expected = (String) changes.get(new Integer(n));
        }
        assertEquals("Locale: " + locales[i] + ", number: " + n,
                     expected, plf.format(n));
      }
    }
  }
  
  public void TestOneFormLocales() {
    String localeIDs = "ja,ko,tr,vi";
    String testPattern = "other{other}";
    Map changes = new HashMap();
    changes.put(new Integer(0), "other");
    helperTestRules(localeIDs, testPattern, changes);
  }
  
  public void TestSingular1Locales() {
    String localeIDs = "da,de,el,en,eo,es,et,fi,fo,he,hu,it,nb,nl,nn,no,pt,sv";
    String testPattern = "one{one} other{other}";
    Map changes = new HashMap();
    changes.put(new Integer(0), "other");
    changes.put(new Integer(1), "one");
    changes.put(new Integer(2), "other");
    helperTestRules(localeIDs, testPattern, changes);
  }
  
  public void TestSingular01Locales() {
    String localeIDs = "fr,pt_BR";
    String testPattern = "one{one} other{other}";
    Map changes = new HashMap();
    changes.put(new Integer(0), "one");
    changes.put(new Integer(2), "other");
    helperTestRules(localeIDs, testPattern, changes);
  }
  
  public void TestZeroSingularLocales() {
    String localeIDs = "lv";
    String testPattern = "zero{zero} one{one} other{other}";
    Map changes = new HashMap();
    changes.put(new Integer(0), "zero");
    changes.put(new Integer(1), "one");
    changes.put(new Integer(2), "other");
    for (int i = 2; i < 20; ++i) {
      if (i == 11) {
        continue;
      }
      changes.put(new Integer(i*10 + 1), "one");
      changes.put(new Integer(i*10 + 2), "other");
    }
    helperTestRules(localeIDs, testPattern, changes);
  }
  
  public void TestSingularDual() {
      String localeIDs = "ga";
      String testPattern = "one{one} two{two} other{other}";
      Map changes = new HashMap();
      changes.put(new Integer(0), "other");
      changes.put(new Integer(1), "one");
      changes.put(new Integer(2), "two");
      changes.put(new Integer(3), "other");
      helperTestRules(localeIDs, testPattern, changes);
  }
  
  public void TestSingularZeroSome() {
      String localeIDs = "ro";
      String testPattern = "zero{zero} one{one} other{other}";
      Map changes = new HashMap();
      changes.put(new Integer(0), "zero");
      changes.put(new Integer(1), "one");
      changes.put(new Integer(2), "zero");
      changes.put(new Integer(20), "other");
      changes.put(new Integer(101), "zero");
      changes.put(new Integer(120), "other");
      helperTestRules(localeIDs, testPattern, changes);
  }
  
  public void TestSpecial12_19() {
      String localeIDs = "lt";
      String testPattern = "one{one} few{few} other{other}";
      Map changes = new HashMap();
      changes.put(new Integer(0), "other");
      changes.put(new Integer(1), "one");
      changes.put(new Integer(2), "few");
      changes.put(new Integer(10), "other");
      for (int i = 2; i < 20; ++i) {
        if (i == 11) {
          continue;
        }
        changes.put(new Integer(i*10 + 1), "one");
        changes.put(new Integer(i*10 + 2), "few");
        changes.put(new Integer((i+1)*10), "other");
      }
      helperTestRules(localeIDs, testPattern, changes);
  }
  
  public void TestPaucalExcept11_14() {
      String localeIDs = "hr,ru,sr,uk";
      String testPattern = "one{one} few{few} other{other}";
      Map changes = new HashMap();
      changes.put(new Integer(0), "other");
      changes.put(new Integer(1), "one");
      changes.put(new Integer(2), "few");
      changes.put(new Integer(5), "other");
      for (int i = 2; i < 20; ++i) {
        if (i == 11) {
          continue;
        }
        changes.put(new Integer(i*10 + 1), "one");
        changes.put(new Integer(i*10 + 2), "few");
        changes.put(new Integer(i*10 + 5), "other");
      }
      helperTestRules(localeIDs, testPattern, changes);
  }
  
  public void TestSingularPaucal() {
      String localeIDs = "cs,sk";
      String testPattern = "one{one} few{few} other{other}";
      Map changes = new HashMap();
      changes.put(new Integer(0), "other");
      changes.put(new Integer(1), "one");
      changes.put(new Integer(2), "few");
      changes.put(new Integer(5), "other");
      helperTestRules(localeIDs, testPattern, changes);
  }
  
  public void TestPaucal1_234() {
      String localeIDs = "pl";
      String testPattern = "one{one} few{few} other{other}";
      Map changes = new HashMap();
      changes.put(new Integer(0), "other");
      changes.put(new Integer(1), "one");
      changes.put(new Integer(2), "few");
      changes.put(new Integer(5), "other");
      for (int i = 2; i < 20; ++i) {
        if (i == 11) {
          continue;
        }
        changes.put(new Integer(i*10 + 2), "few");
        changes.put(new Integer(i*10 + 5), "other");
      }
      helperTestRules(localeIDs, testPattern, changes);
  }
  
  public void TestPaucal1_2_34() {
      String localeIDs = "sl";
      String testPattern = "one{one} two{two} few{few} other{other}";
      Map changes = new HashMap();
      changes.put(new Integer(0), "other");
      changes.put(new Integer(1), "one");
      changes.put(new Integer(2), "two");
      changes.put(new Integer(3), "few");
      changes.put(new Integer(5), "other");
      changes.put(new Integer(101), "one");
      changes.put(new Integer(102), "two");
      changes.put(new Integer(103), "few");
      changes.put(new Integer(105), "other");
      helperTestRules(localeIDs, testPattern, changes);
  }
}
