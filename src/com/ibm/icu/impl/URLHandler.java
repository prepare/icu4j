/*
******************************************************************************
* Copyright (C) 2005, International Business Machines Corporation and        *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

package com.ibm.icu.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class URLHandler {
    public static final String PROPNAME = "urlhandler.props";

    private static final Map handlers;

    private static final boolean DEBUG = ICUDebug.enabled("URLHandler");

    static {
	Map h = null;
	try {
	    InputStream is = URLHandler.class.getResourceAsStream(PROPNAME);
	    if (is == null) {
		is = ClassLoader.getSystemClassLoader().getResourceAsStream(PROPNAME);
	    }
	    if (is != null) {
		Class[] params = { URL.class };
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
		    line = line.trim();
		    if (line.length() == 0 || line.charAt(0) == '#') {
			continue;
		    }
		    int ix = line.indexOf('=');
		    if (ix == -1) {
			if (DEBUG) System.err.println("bad urlhandler line: '" + line + "'");
			break;
		    }
		    String key = line.substring(0, ix).trim();
		    String value = line.substring(ix+1).trim();

		    try {
			Class cl = Class.forName(value);
			Method m = cl.getDeclaredMethod("get", params);
			if (h == null) {
			    h = new HashMap();
			}
			h.put(key, m);
		    }
		    catch (ClassNotFoundException e) {
			if (DEBUG) System.err.println(e);
		    }
		    catch(NoSuchMethodException e) {
			if (DEBUG) System.err.println(e);
		    }
		    catch(SecurityException e) {
			if (DEBUG) System.err.println(e);
		    }
		}
	    }
	} catch (Throwable t) {
	    if (DEBUG) System.err.println(t);
	}
	handlers = h;
    }

    public static URLHandler get(URL url) {
	if (url == null) {
	    return null;
	}
	String protocol = url.getProtocol();
	if (handlers != null) {
	    Method m = (Method)handlers.get(protocol);
	    if (m != null) {
		try {
		    URLHandler handler = (URLHandler)m.invoke(null, new Object[] { url });
		    if (handler != null) {
			return handler;
		    }
		}
		catch(IllegalAccessException e) {
		    if (DEBUG) System.err.println(e);
		}
		catch(IllegalArgumentException e) {
		    if (DEBUG) System.err.println(e);
		}
		catch(InvocationTargetException e) {
		    if (DEBUG) System.err.println(e);
		}
	    }
	}

	return getDefault(url);
    }

    protected static URLHandler getDefault(URL url) {
	String protocol = url.getProtocol();
	if (protocol.equals("file")) {
	    return new FileURLHandler(url);
	} else if (protocol.equals("jar") || protocol.equals("wsjar")) {
	    return new JarURLHandler(url);
	} else {
	    return null;
	}
    }

    private static class FileURLHandler extends URLHandler {
	File file;

	FileURLHandler(URL url) {
	    file = new File(url.getPath());
	    if (!file.exists()) {
		if (DEBUG) System.err.println("file does not exist");
		throw new IllegalArgumentException();
	    }
	}

	public void guide(URLVisitor v, boolean recurse) {
	    if (file.isDirectory()) {
		process(v, recurse, file.listFiles());
	    } else {
		v.visit(file.getName());
	    }
	}

	private void process(URLVisitor v, boolean recurse, File[] files) {
	    for (int i = 0; i < files.length; i++) {
		File f = files[i];
		if (f.isDirectory()) {
		    if (recurse) {
			process(v, recurse, f.listFiles());
		    }
		} else {
		    v.visit(f.getName());
		}
	    }
	}
    }

    private static class JarURLHandler extends URLHandler {
	JarFile jarFile;
	String prefix;

	JarURLHandler(URL url) {
	    try {
		prefix = url.getPath();
		int ix = prefix.indexOf("!/");
		if (ix >= 0) {
		    prefix = prefix.substring(ix + 2); // truncate after "!/"
		}
		JarURLConnection conn = (JarURLConnection)url.openConnection();
		jarFile = conn.getJarFile();
	    }
	    catch (Exception e) {
		if (DEBUG) System.err.println("icurb jar error: " + e);
		throw new IllegalArgumentException("jar error: " + e.getMessage());
	    }
	}

	public void guide(URLVisitor v, boolean recurse) {
	    try {
		Enumeration entries = jarFile.entries();
		while (entries.hasMoreElements()) {
		    JarEntry entry = (JarEntry)entries.nextElement();
		    if (!entry.isDirectory()) { // skip just directory paths
			String name = entry.getName();
			if (name.startsWith(prefix)) {
			    name = name.substring(prefix.length());
			    int ix = name.lastIndexOf('/');
			    if (ix != -1) {
				if (!recurse) {
				    continue;
				}
				name = name.substring(ix+1);
			    }
			    v.visit(name);
			}
		    }
		}
	    }
	    catch (Exception e) {
		if (DEBUG) System.err.println("icurb jar error: " + e);
	    }
	}
    }

    public abstract void guide(URLVisitor visitor, boolean recurse);

    public interface URLVisitor {
	void visit(String str);
    }
}

