/*
**********************************************************************
* Copyright (c) 2002-2004, International Business Machines
* Corporation and others.  All Rights Reserved.
**********************************************************************
* Author: Mark Davis
**********************************************************************
*/
package com.ibm.icu.dev.tool.cldr;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import com.ibm.icu.dev.test.util.BagFormatter;
import com.ibm.icu.dev.tool.UOption;
import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.util.Currency;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.UResourceBundle;

//import javax.xml.parsers.*;

/**
 * This is a simple class that walks through the CLDR hierarchy and does 2 things.
 * First, it determines all the places where the CLDR is not minimal: where there
 * are redundancies with inheritance. It generates new files in the target directory.
 * Second, it gathers together all the items from all the locales that share the
 * same element chain, and thus presents a "sideways" view of the data, in files called
 * sideways_X.html, where X is a type.
 * @author medavis
 */public class GenerateSidewaysView {

    private static final int 
        HELP1 = 0,
		HELP2 = 1,
        SOURCEDIR = 2,
        DESTDIR = 3,
        MATCH = 4,
        SKIP = 5;

    private static final UOption[] options = {
            UOption.HELP_H(),
            UOption.HELP_QUESTION_MARK(),
            UOption.SOURCEDIR().setDefault("C:\\ICU4C\\locale\\common\\main\\"),
            UOption.DESTDIR().setDefault("C:\\DATA\\GEN\\cldr\\main\\"),
            UOption.create("match", 'm', UOption.REQUIRES_ARG).setDefault(".*"),
            UOption.create("skip", 's', UOption.REQUIRES_ARG).setDefault("zh_(C|S|HK|M).*"),
    };
    
    public static void main(String[] args) throws SAXException, IOException {
        UOption.parseArgs(args, options);

        Matcher skipper = Pattern.compile(options[SKIP].value).matcher("");
        Matcher matcher = Pattern.compile(options[MATCH].value).matcher("");
        //matcher = Pattern.compile("(root|b).*").matcher("");
        log = BagFormatter.openUTF8Writer(options[DESTDIR].value, "log.txt");
        try {
            File sourceDir = new File(options[SOURCEDIR].value);
            String[] contents = sourceDir.list();
            for (int i = 0; i < contents.length; ++i) {
                if (!contents[i].endsWith(".xml")) continue;
                if (contents[i].startsWith("supplementalData")) continue;
                if (!matcher.reset(contents[i]).matches()) continue; // debug shutoff
                if (skipper.reset(contents[i]).matches()) continue; // debug shutoff
                //System.out.println("Processing " + contents[i]);
                log.println();
                log.println("Processing " + contents[i]);
                String baseName = contents[i].substring(0,contents[i].length()-4);
                GenerateSidewaysView temp = getCLDR(baseName);
                if (baseName.equals("zh_TW")) baseName = "zh_Hant_TW";
                if (baseName.equals("root")) temp.addMissing();
                temp.writeTo(options[DESTDIR].value, baseName);
                sidewaysView.putData(temp.data, baseName);
                log.flush();          
            }
            sidewaysView.showCacheData();
        } finally {
            log.close();
       }
    }

    static MapComparator elementOrdering = new MapComparator();
    static MapComparator attributeOrdering = new MapComparator();
    static MapComparator valueOrdering = new MapComparator();
        
    OrderedMap data = new OrderedMap();
    SAXParser SAX;
    {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            SAX = factory.newSAXParser();
        } catch (Exception e) {
            throw new IllegalArgumentException("can't start");
        }
    }
    
    static PrintWriter log;
    
    /**
     * 
     */
    private void addMissing() {
        String[] currencies = getCodes(new ULocale("en","",""), "Currencies");
        //<ldml><numbers><currencies><currency type="AUD"><displayName>
        addCurrencies(currencies, "displayName");
        addCurrencies(currencies, "symbol");
    }

    private void addCurrencies(String[] currencies, String lastElement) {
        ElementChain temp = new ElementChain();
        temp.push("ldml",null).push("numbers",null).push("currencies",null)
            .push("currency",null).push(lastElement,null);
        for (int i = 0; i < currencies.length; ++i) {
            temp.setAttribute("currency","type",currencies[i]);
            String value = (String) data.get(temp);
            if (value != null) continue;
            putData(temp, currencies[i]);
        }
    }
    
    // UGLY hack
    private static String[] getCodes(ULocale locale, String tableName) {
        // TODO remove Ugly Hack
        // get stuff
        ICUResourceBundle bundle = (ICUResourceBundle)UResourceBundle.getBundleInstance(locale);
        ICUResourceBundle table = bundle.getWithFallback(tableName);
        // copy into array
        ArrayList stuff = new ArrayList();
        for (Enumeration keys = table.getKeys(); keys.hasMoreElements();) {
            stuff.add(keys.nextElement());
        }
        String[] result = new String[stuff.size()];
        return (String[]) stuff.toArray(result);
        //return new String[] {"Latn", "Cyrl"};
    }


    static Map cache = new HashMap();
    
    static GenerateSidewaysView getCLDR(String s) throws SAXException, IOException {
        GenerateSidewaysView temp = (GenerateSidewaysView)cache.get(s);
        if (temp == null) {
            temp = new GenerateSidewaysView(s);
            cache.put(s,temp);
        }
        return temp;
    }
    
    String filename;
    
    private GenerateSidewaysView(String filename) throws SAXException, IOException {
        //System.out.println("Creating " + filename);
        this.filename = filename;
        readFrom(options[SOURCEDIR].value, filename);
        String current = filename;
        while (true) {
            current = getParent(current);
            if (current == null) break;
            GenerateSidewaysView temp = getCLDR(current);
            this.removeAll(temp);
       }
    }
    
    private void removeAll(GenerateSidewaysView temp) {
        data.removeAll(temp.data);
    }

    private static String getParent(String filename) {
        if (filename.equals("zh_TW")) return "zh_Hant";
        int pos = filename.lastIndexOf('_');
        if (pos >= 0) {
            return filename.substring(0,pos);
        }
        if (filename.equals("root")) return null;
        return "root";
    }

    private void writeTo(String dir, String filename) throws IOException {
        PrintWriter out = BagFormatter.openUTF8Writer(dir, filename + ".xml");
        out.print(this);
        out.close();
    }

    public void readFrom(String dir, String filename) throws SAXException, IOException {
        File f = new File(dir + filename + ".xml");
        SAX.parse(f, DEFAULT_HANDLER);
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n"
        + "<!DOCTYPE ldml SYSTEM \"http://www.unicode.org/cldr/dtd/1.1/ldml.dtd\">\r\n");
        ElementChain empty = new ElementChain();
        ElementChain old = empty;
        for (Iterator it = data.iterator(); it.hasNext();) {
            ElementChain key = (ElementChain) it.next();
            String value = (String) data.get(key);
            key.getDifference(old, value, buffer);
            old = key;           
        }
        empty.getDifference(old, "", buffer);
        return buffer.toString();
    }
    
    
    static class SimpleAttribute implements Comparable {
        String name;
        String value;
        SimpleAttribute(String name, String value) {
            attributeOrdering.add(name);
            valueOrdering.add(value);
            this.name = name;
            this.value = value;
        }
        public boolean equals(Object other) {
            return compareTo(other) == 0;
        }
        public int hashCode() {
            return name.hashCode() ^ value.hashCode();
        }
        public String toString(boolean path) {
            if (path) {
            	return "@" + name + "=\"" + BagFormatter.toHTML.transliterate(value) + "\"";
            } else {
                return " " + name + "=\"" + BagFormatter.toHTML.transliterate(value) + "\"";                
            }
        }
        public int compareTo(Object o) {
            SimpleAttribute that = (SimpleAttribute) o;
            int result;
            if ((result = attributeOrdering.compare(name, that.name)) != 0) return result;
            return valueOrdering.compare(value, that.value);
        }
    }
    
    static class SimpleAttributes implements Comparable {
        Set contents = new TreeSet();
        
        SimpleAttributes(SimpleAttributes other, String elementName) {
            contents.clear();
        }
        
        SimpleAttributes(Attributes attributes, String elementName) {
            if (attributes != null) {
                for (int i = 0; i < attributes.getLength(); ++i) {
                    String name = attributes.getQName(i);
                    String value = attributes.getValue(i);
                    
                    // hack to removed #IMPLIED
                    if (elementName.equals("ldml")
                        && name.equals("version") 
                        && value.equals("1.1")) continue;
                    if (name.equals("type") 
                        && value.equals("standard")) continue;

                    contents.add(new SimpleAttribute(name, value));
                }
            }
        }
        
        public String toString(boolean path) {
            StringBuffer buffer = new StringBuffer();
            for (Iterator it = contents.iterator(); it.hasNext();) {
                SimpleAttribute a = (SimpleAttribute)it.next();
                if (path && a.name.equals("draft")) continue;
                buffer.append(a.toString(path));
            }
            return buffer.toString();
        }
        public int compareTo(Object o) {
            // IGNORE draft, source, reference
            int result;
            SimpleAttributes that = (SimpleAttributes) o;
            // compare one at a time. Stop if one element is less than another.
            Iterator it = contents.iterator();
            Iterator it2 = that.contents.iterator();
            while (true) {
            	SimpleAttribute a = getSkipping(it);
                SimpleAttribute a2 = getSkipping(it2);
                if (a == null) {
                	if (a2 == null) return 0;
                    return -1;
                }
                if (a2 == null) {
                	return 1;
                }
                if ((result = a.compareTo(a2)) != 0) return result;
            }
        }
        private SimpleAttribute getSkipping(Iterator it) {
            while (it.hasNext()) {
            	SimpleAttribute a = (SimpleAttribute)it.next();
                if (!a.name.equals("draft")) return a;
            }
            return null;
        }

        /**
         * @param attribute
         * @param value
         */
        public SimpleAttributes set(String attribute, String value) {
            for (Iterator it = contents.iterator(); it.hasNext();) {
                SimpleAttribute sa = (SimpleAttribute) it.next();
                if (sa.name.equals(attribute)) {
                    contents.remove(sa);
                    break;
                }
            }
            contents.add(new SimpleAttribute(attribute, value));
            return this;
        }
    }
    
    static class Element implements Comparable {
        String elementName;
        SimpleAttributes attributes;
        Element(String elementName, Attributes attributes) {
            //elementOrdering.add(elementName);
            this.elementName = elementName;
            this.attributes = new SimpleAttributes(attributes, elementName);
        }
        Element(Element other) {
            //elementOrdering.add(elementName);
            this.elementName = other.elementName;
            this.attributes = new SimpleAttributes(other.attributes, elementName);
        }
        public String toString(boolean path) {
            return toString(START_VALUE, path);
        }
        static final int NO_VALUE = 0, START_VALUE = 1, END_VALUE = 2;
        public String toString(int type, boolean path) {
            String a = attributes.toString(path);
            if (path) {
                if (type == NO_VALUE) return elementName + a + "-NOVALUE";
                if (type == END_VALUE) return "END-" + elementName + ">";
                return elementName + a;
            } else {
                if (type == NO_VALUE) return "<" + elementName + a + "/>";
                if (type == END_VALUE) return "</" + elementName + ">";
                return "<" + elementName + a + ">";
            }
        }
        public int compareTo(Object o) {
            if (o == null) return 1;
            int result;
            Element that = (Element) o;
            if ((result = elementOrdering.compare(elementName, that.elementName)) != 0) return result;
            return attributes.compareTo(that.attributes);
        }
        public boolean equals(Object o) {
        	return compareTo(o) == 0;
        }
    }
    
    static class ElementChain implements Comparable {
        List contexts;
        
        ElementChain() {
            contexts = new ArrayList();
        }
        
        /**
         * @param string
         * @param string2
         * @param string3
         */
        public void setAttribute(String element, String attribute, String value) {
            for (int i = 0; i < contexts.size(); ++i) {
                Element context = (Element)contexts.get(i);
                if (context.elementName.equals(element)) {
                    context = new Element(context); // clone for safety
                    context.attributes.set(attribute, value);
                    contexts.set(i, context);
                    break;
                }
            }
        }

        ElementChain(ElementChain other) {
            contexts = new ArrayList(other.contexts);
        }
        
        public ElementChain push(String elementName, Attributes attributes) {
            elementOrdering.add(elementName);
            contexts.add(new Element(elementName, attributes));
            return this;
        }
        
        public void pop(String elementName) {
            int last = contexts.size()-1;
            Element c = (Element) contexts.get(last);
            if (!c.elementName.equals(elementName)) throw new IllegalArgumentException("mismatch");
            contexts.remove(last);
        }
        
        public String toString(boolean path) {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < contexts.size(); ++i) {
                //if (i != 0) buffer.append(' ');
                Element e = (Element) contexts.get(i);
                if (path) buffer.append("/" + e.toString(path));
                else buffer.append(e.toString(path));
            }
            return buffer.toString();
        }
        
        public boolean equals(Object other) {
            return compareTo(other) == 0;
        }
        public int hashCode() {
            return contexts.hashCode();
        }

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Object o) {
            int result;
            ElementChain that = (ElementChain) o;
            int minLen = Math.min(contexts.size(), that.contexts.size());
            for (int i = 0; i < minLen; ++i) {
                if ((result = ((Element)contexts.get(i)).compareTo(that.contexts.get(i))) != 0) return result;
            }
            return compareInt(contexts.size(), that.contexts.size());
        }
        public void getDifference(ElementChain former, String value, StringBuffer out) {
            // find the identical stuff first!
            int csize = contexts.size();
            int fsize = former.contexts.size();
            int minLen = Math.min(csize, fsize);
            int result;
            
            // skip stuff that is in common
            int common;
            for (common = 0; common < minLen; ++common) {
                if ((result = ((Element)contexts.get(common)).compareTo(former.contexts.get(common))) != 0) break;
            }
            // finish up old elements, by writing out termination elements.
            // We don't do the very last one, however, since that was done with the value
            for (int j = fsize - 2; j >= common; --j) {
                indent(j, out);
                out.append(((Element)former.contexts.get(j)).toString(Element.END_VALUE, false));
                out.append("\r\n");
            }
            if (csize == 0) return; // we must be at the very end, bail.
            
            // write new elements if needed.
            for (; common < csize-1; ++common) {
                indent(common, out);
                out.append(((Element)contexts.get(common)).toString(Element.START_VALUE, false));
                out.append("\r\n");
            }
            // now write the very current element
            indent(common, out);
            if (value.length() == 0) {
                out.append(((Element)contexts.get(csize-1)).toString(Element.NO_VALUE, false));
            } else {
                out.append(((Element)contexts.get(csize-1)).toString(Element.START_VALUE, false));
                out.append(BagFormatter.toHTML.transliterate(value));
                out.append(((Element)contexts.get(csize-1)).toString(Element.END_VALUE, false));
            }
            out.append("\r\n");
        }

        /**
         * @param string
         * @return
         */
        public boolean containsElement(String string) {
            for (int i = 0; i < contexts.size(); ++i) {
                Element x = (Element)contexts.get(i);
                if (string.equals(x.elementName)) return true;
            }
            return false;
        }
    }
    
    static int compareInt(int a, int b) {
        return a < b ? -1 : a > b ? 1 : 0;
    }
    
    static void indent(int count, StringBuffer out) {
        for (int i = 0; i < count; ++i) {
            out.append("\t");
        }
    }
    
    /*
    static {
        Object[][] temp = {
            {"keys", new Integer(13)},
            {"scripts", new Integer(7)},
            {"script", new Integer(8)},
            {"era", new Integer(38)},
            {"ldml", new Integer(0)},
            {"calendar", new Integer(22)},
            {"numbers", new Integer(57)},
            {"timeFormats", new Integer(44)},
            {"infinity", new Integer(69)},
            {"localizedPatternChars", new Integer(20)},
            {"dateTimeFormats", new Integer(47)},
            {"eraAbbr", new Integer(37)},
            {"exemplarCharacters", new Integer(18)},
            {"month", new Integer(26)},
            {"variants", new Integer(11)},
            {"group", new Integer(60)},
            {"dateTimeFormat", new Integer(49)},
            {"day", new Integer(30)},
            {"zone", new Integer(51)},
            {"types", new Integer(15)},
            {"timeFormat", new Integer(46)},
            {"default", new Integer(40)},
            {"dates", new Integer(19)},
            {"language", new Integer(4)},
            {"long", new Integer(52)},
            {"version", new Integer(2)},
            {"dayWidth", new Integer(29)},
            {"characters", new Integer(17)},
            {"variant", new Integer(12)},
            {"short", new Integer(55)},
            {"generation", new Integer(3)},
            {"am", new Integer(34)},
            {"pattern", new Integer(43)},
            {"minDays", new Integer(32)},
            {"displayName", new Integer(73)},
            {"perMille", new Integer(68)},
            {"monthContext", new Integer(24)},
            {"days", new Integer(27)},
            {"months", new Integer(23)},
            {"territories", new Integer(9)},
            {"identity", new Integer(1)},
            {"currency", new Integer(72)},
            {"exponential", new Integer(67)},
            {"territory", new Integer(10)},
            {"firstDay", new Integer(33)},
            {"languages", new Integer(6)},
            {"nan", new Integer(70)},
            {"week", new Integer(31)},
            {"nativeZeroDigit", new Integer(63)},
            {"decimal", new Integer(59)},
            {"symbols", new Integer(58)},
            {"daylight", new Integer(54)},
            {"calendars", new Integer(21)},
            {"eras", new Integer(36)},
            {"localeDisplayNames", new Integer(5)},
            {"dateTimeFormatLength", new Integer(48)},
            {"dateFormats", new Integer(39)},
            {"exemplarCity", new Integer(56)},
            {"currencies", new Integer(71)},
            {"minusSign", new Integer(66)},
            {"list", new Integer(61)},
            {"dateFormatLength", new Integer(41)},
            {"type", new Integer(16)},
            {"plusSign", new Integer(65)},
            {"dayContext", new Integer(28)},
            {"dateFormat", new Integer(42)},
            {"symbol", new Integer(74)},
            {"timeZoneNames", new Integer(50)},
            {"key", new Integer(14)},
            {"patternDigit", new Integer(64)},
            {"percentSign", new Integer(62)},
            {"standard", new Integer(53)},
            {"monthWidth", new Integer(25)},
            {"pm", new Integer(35)},
            {"timeFormatLength", new Integer(45)},
        };
        elementOrdering = new MapComparator(temp);
    }
    */
    
    public static class MapComparator {
        Map ordering = new TreeMap(); // maps from name to rank
        List rankToName = new ArrayList();
        
        MapComparator(){}
        MapComparator(Comparable[] data) {
            for (int i = 0; i < data.length; ++i) {
                add(data[i]);
            }
        }
        public void add(Comparable newObject) {
            Object already = ordering.get(newObject);
            if (already == null) {
                ordering.put(newObject, new Integer(rankToName.size()));
                rankToName.add(newObject);
            }
        }
        public int compare(Comparable a, Comparable b) {
            Comparable aa = (Comparable) ordering.get(a);
            Object bb = ordering.get(b);
            if (aa == null || bb == null) return a.compareTo(b);
            return aa.compareTo(bb);
        }
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            for (Iterator it = rankToName.iterator(); it.hasNext();) {
                Object key = it.next();
                buffer.append("\"").append(key).append("\",\r\n");
            }
            return buffer.toString();
        }
    }
    
    public static class OrderedMap {
        private Map map = new TreeMap();
        private List list = new ArrayList();
        public void put(Object a, Object b) {
            map.put(a,b);
            list.add(a);
        }
        /**
         * @param map
         */
        public void removeAll(OrderedMap other) {
            for (Iterator it = other.iterator(); it.hasNext();) {
                Object key = it.next();
                Object otherValue = other.map.get(key);
                Object value = map.get(key);
                if (value == null || !value.equals(otherValue)) continue;
                if (((ElementChain)key).containsElement("identity")) continue;
                log.println("Removing " + key + "\t" + value);
                map.remove(key);
                while(list.remove(key)) {}
            }
        }
        public Object get(Object a) {
            return map.get(a);
        }
        public Iterator iterator() {
            return list.iterator();
        }
    }
    
    void putData(ElementChain stack, String associatedData) {
        data.put(new ElementChain(stack), associatedData);
    }
    
    static SidewaysView sidewaysView = new SidewaysView();
    
    static class SidewaysView {
        Map contextCache = new TreeMap();
        Set fileNames = new TreeSet();

        void putData(OrderedMap data, String filename) {
            for (Iterator it = data.iterator(); it.hasNext();) {
                ElementChain copy = (ElementChain) it.next();
                String associatedData = (String) data.get(copy);
                Map dataToFile = (Map)contextCache.get(copy);
                if (dataToFile == null) {
                    dataToFile = new TreeMap();
                    contextCache.put(copy, dataToFile);
                 }
                Set files = (Set) dataToFile.get(associatedData);
                if (files == null) {
                    files = new TreeSet();
                    dataToFile.put(associatedData, files);
                }
                files.add(filename);
            }
            if (filename.indexOf('_') < 0
                || filename.equals("zh_Hant")) fileNames.add(filename); // add all language-only locales
        }
        
        String getChainName(ElementChain ec) {
        	Element e = (Element)ec.contexts.get(1);
            String result = e.elementName;
            if (result.equals("numbers") || result.equals("localeDisplayNames") || result.equals("dates")) {
            	e = (Element)ec.contexts.get(2);
                result += "_" + e.elementName;
            }
            return result;
        }
        void showCacheData() throws IOException {
            PrintWriter out = null;
            String lastChainName = "";
            for (Iterator it = contextCache.keySet().iterator(); it.hasNext();) {
                ElementChain stack = (ElementChain) it.next();
                String chainName = getChainName(stack);
                if (!chainName.equals(lastChainName)) {
                    if (out != null) {
                        out.println("</table></body></html>");
                        out.close();
                    }
                	out = openAndDoHeader(chainName);
                    lastChainName = chainName;
                }
                out.println("<tr><td colspan='2' class='head'>" + BagFormatter.toHTML.transliterate(stack.toString(true)) + "</td></tr>");
                Map dataToFile = (Map) contextCache.get(stack);
                // walk through once, and gather all the filenames
                Set remainingFiles = new TreeSet(fileNames);
                for (Iterator it2 = dataToFile.keySet().iterator(); it2.hasNext();) {
                    String data = (String) it2.next();
                    remainingFiles.removeAll((Set) dataToFile.get(data));
                }
                // hack for zh_Hant
                if (!remainingFiles.contains("zh")) remainingFiles.remove("zh_Hant");
                // now display
                for (Iterator it2 = dataToFile.keySet().iterator(); it2.hasNext();) {
                    String data = (String) it2.next();
                    out.print("<tr><th>\"" + BagFormatter.toHTML.transliterate(data) + "\"</th><td>");
                    Set files = (Set) dataToFile.get(data);
                    if (files.contains("root")) files.addAll(remainingFiles);
                    boolean first = true;
                    for (Iterator it3 = files.iterator(); it3.hasNext();) {
                        if (first) first = false;
                        else out.print(" ");
                        out.print("�" + it3.next() + "�");
                    }
                    out.println("</td></tr>");
                }
            }
            out.println("</table></body></html>");
            out.close();
        }

		/**
		 * @param type
		 * @return
		 * @throws IOException
		 */
		private PrintWriter openAndDoHeader(String type) throws IOException {
			PrintWriter out = BagFormatter.openUTF8Writer(options[DESTDIR].value, "sideways_" + type + ".html");
            out.println("<html><head>");
            out.println("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>");
			out.println("<title>Sideways Locale Data: " + type + "</title>");
			out.println("<style>");
			out.println("<!--");
            out.println(".head { font-weight:bold; background-color:#DDDDFF }");
            out.println("td, th { border: 1px solid #0000FF; text-align }");
            out.println("th { width:10% }");
			out.println("table {margin-top: 1em}");
			out.println("-->");
			out.println("</style>");
			out.println("<link rel='stylesheet' type='text/css' href='http://oss.software.ibm.com/cvs/icu/~checkout~/icuhtml/common.css'>");
			out.println("</head>");
            out.println("<body><table>");
			return out;
		}
    }
    

    DefaultHandler DEFAULT_HANDLER = new DefaultHandler() {
        static final boolean DEBUG = false;
        
        ElementChain contextStack = new ElementChain();
        String lastChars = "";
        boolean justPopped = false;
        
        public void startElement(
            String uri,
            String localName,
            String qName,
            Attributes attributes)
            throws SAXException {
                //data.put(new ContextStack(contextStack), lastChars);
                //lastChars = "";
                try {
                    contextStack.push(qName, attributes);               
                    if (DEBUG) System.out.println("startElement:\t" + contextStack);
                    justPopped = false;
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    throw e;
                }
        }
        public void endElement(String uri, String localName, String qName)
            throws SAXException {
                try {
                    if (DEBUG) System.out.println("endElement:\t" + contextStack);
                    if (lastChars.length() != 0 || justPopped == false) {
                        putData(contextStack, lastChars);
                        lastChars = "";
                    }
                    contextStack.pop(qName);
                    justPopped = true;
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        public void characters(char[] ch, int start, int length)
            throws SAXException {
                try {
                    String value = new String(ch,start,length);
                    if (DEBUG) System.out.println("characters:\t" + value);
                    lastChars += value;
                    justPopped = false;
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    throw e;
                }
            }

        // just for debugging
        
        public void notationDecl (String name, String publicId, String systemId)
        throws SAXException {
            System.out.println("notationDecl: " + name
            + ", " + publicId
            + ", " + systemId
            );
        }

        public void processingInstruction (String target, String data)
        throws SAXException {
            System.out.println("processingInstruction: " + target + ", " + data);
        }
        
        public void skippedEntity (String name)
        throws SAXException
        {
            System.out.println("skippedEntity: " + name
            );
        }

        public void unparsedEntityDecl (String name, String publicId,
                        String systemId, String notationName)
        throws SAXException {
            System.out.println("unparsedEntityDecl: " + name
            + ", " + publicId
            + ", " + systemId
            + ", " + notationName
            );
        }

    };
}