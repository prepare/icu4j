/*
 *******************************************************************************
 * Copyright (C) 1996-2003, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;

/**
 * TestFmwk is a base class for tests that can be run conveniently from
 * the command line as well as under the Java test harness.
 * <p>
 * Sub-classes implement a set of methods named Test<something>. Each
 * of these methods performs some test. Test methods should indicate
 * errors by calling either err or errln.  This will increment the
 * errorCount field and may optionally print a message to the log.
 * Debugging information may also be added to the log via the log
 * and logln methods.  These methods will add their arguments to the
 * log only if the test is being run in verbose mode.
 */
public class TestFmwk extends AbstractTestLog {
    /**
     * Puts a copyright in the .class file
    */
    private static final String copyrightNotice
        = "Copyright \u00a91997-2003 IBM Corp.  All rights reserved.";

    /**
     * The default time zone for all of our tests.  Used in Target.run();
     */
    private final static TimeZone defaultTimeZone = TimeZone.getTimeZone("PST");

    /**
     * The default locale used for all of our tests.  Used in Target.run();
     */
    private final static Locale defaultLocale = Locale.US;

    public static final class TestFmwkException extends Exception {
        TestFmwkException(String msg) {
            super(msg);
        }
    }

    // use this instead of new random so we get a consistent seed 
    // for our tests
    protected Random createRandom() {
	return new Random(params.seed);
    }

    /**
     * A test that has no test methods itself, but instead runs other tests.
     *
     * This overrides methods are getTargets and getSubtest from TestFmwk.
     *
     * If you want the default behavior, pass an array of class names
     * and an optional description to the constructor.  The named
     * classes must extend TestFmwk. If a provided name doesn't
     * include a ".", package name is prefixed to it (the package of
     * the current test is used if none was provided in the
     * constructor).  The resulting full name is used to instantiate
     * an instance of the class using the default constructor.
     *
     * Class names are resolved to classes when getTargets or
     * getSubtest is called.  This allows instances of TestGroup to be
     * compiled and run without all the targets they would normally
     * invoke being available.
     */
    public static abstract class TestGroup extends TestFmwk {
        private String defaultPackage;
        private String[] names;
        private String description;

        private Class[] tests;   // deferred init

        /**
         * Constructor that takes a default package name and a list of class names.
         * Adopts and modifies the classname list
         */
        protected TestGroup(String defaultPackage, String[] classnames, String description) {
            if (classnames == null) {
                throw new InternalError("classnames must not be null");
            }

            if (defaultPackage == null) {
                defaultPackage = getClass().getPackage().getName();
            }
            defaultPackage = defaultPackage + ".";

            this.defaultPackage = defaultPackage;
            this.names = classnames;
            this.description = description;
        }

        /**
         * Constructor that takes a list of class names and a description,
         * and uses the package for this class as the default package.  
         */
        protected TestGroup(String[] classnames, String description) {
            this(null, classnames, description);
        }

        /**
         * Constructor that takes a list of class names, and uses the package
         * for this class as the default package.
         */
        protected TestGroup(String[] classnames) {
            this(null, classnames, null);
        }

        protected String getDescription() {
            return description;
        }

        protected Target getTargets(String targetName)  {
            Target target = null;
            if (targetName != null) {
                finishInit(); // hmmm, want to get subtest without initializing all tests
        
                try {
                    TestFmwk test = getSubtest(targetName);
                    if (test != null) {
                        target = test.new ClassTarget();
                    } else {
                        target = this.new Target(targetName);
                    }
                } catch (TestFmwkException e) {
                    target = this.new Target(targetName);
                }
            } else if (params.doRecurse()) {
                finishInit();
                boolean groupOnly = params.doRecurseGroupsOnly();
                for (int i = names.length; --i >= 0;) {
                    Target newTarget = null;
                    Class cls = tests[i];
                    if (cls == null) { // hack no warning for missing tests
                        if (params.warnings) {
                            continue;
                        } else {
                            newTarget = this.new Target(names[i]);
                        }
                    } else {
                        TestFmwk test = getSubtest(i, groupOnly);
                        if (test != null) {
                            newTarget = test.new ClassTarget();
                        } else {
                            if (groupOnly) {
                                newTarget = this.new EmptyTarget(names[i]);
                            } else {
                                newTarget = this.new Target(names[i]);
                            }
                        }
                    }
                    if (newTarget != null) {
                        newTarget.setNext(target);
                        target = newTarget;
                    }
                }
            }

            return target;
        }

        protected TestFmwk getSubtest(String testName) throws TestFmwkException {
            finishInit();

            for (int i = 0; i < names.length; ++i) {
                if (names[i].equalsIgnoreCase(testName)) { // allow case-insensitive matching
                    return getSubtest(i, false);
                }
            }
            throw new TestFmwkException(testName);
        }

        private TestFmwk getSubtest(int i, boolean groupOnly) {
            Class cls = tests[i];
            if (cls != null) {
                if (groupOnly && !TestGroup.class.isAssignableFrom(cls)) {
                    return null;
                }

                try {
                    TestFmwk subtest = (TestFmwk)cls.newInstance();
                    subtest.params = params;
                    return subtest;
                }
                catch (InstantiationException e) {
                    throw new InternalError(e.getMessage());
                }
                catch (IllegalAccessException e) {
                    throw new InternalError(e.getMessage());
                }
            }
            return null;
        }

        private void finishInit() {
            if (tests == null) {
                tests = new Class[names.length];

                for (int i = 0; i < names.length; ++i) {
                    String name = names[i];
                    if (name.indexOf('.') == -1) {
                        name = defaultPackage + name;
                    }
                    try {
                        Class cls = Class.forName(name);
                        if (!TestFmwk.class.isAssignableFrom(cls)) {
                            throw new InternalError("class " + name + " does not extend TestFmwk");
                        }

                        tests[i] = cls;
                        names[i] = getClassTargetName(cls);
                    } catch (ClassNotFoundException e) {
                        // leave tests[i] null and name as classname
                    }
                }
            }
        }
    }

    /**
     * The default target is invalid.
     */
    public class Target {
        private Target next;
        public final String name;

        public Target(String name) {
            this.name = name;
        }

        public Target setNext(Target next) {
            this.next = next;
            return this;
        }

        public Target getNext() {
            return next;
        }
        
        public void run() {
            int f = filter();
            if (f == -1) {
                ++params.invalidCount;
            } else {
                Locale.setDefault(defaultLocale);
                TimeZone.setDefault(defaultTimeZone);

                if (!validate()) {
                    params.writeTestInvalid(name);
                } else {
                    params.push(name, getDescription(), f == 1);
                    execute();
                    params.pop();
                }
            }
        }
        
        protected int filter() {
            return params.filter(name);
        }

        protected boolean validate() {
            return false;
        }

        protected String getDescription() {
            return null;
        }

        protected void execute() {
        }
    }

    public class EmptyTarget extends Target {
        public EmptyTarget(String name) {
            super(name);
        }

        protected boolean validate() {
            return true;
        }
    }

    public class MethodTarget extends Target {
        private Method testMethod;

        public MethodTarget(String name, Method method) {
            super(name);
            testMethod = method;
        }

        protected boolean validate() {
            return testMethod != null && validateMethod(name);
        }

        protected String getDescription() {
            return getMethodDescription(name);
        }

        protected void execute() {
            if (params.inDocMode()) {
                // nothing to execute
            } else if (!params.stack.included) {
                ++params.invalidCount;
            } else {
                final Object[] NO_ARGS = new Object[0];
                try {
                    testMethod.invoke(TestFmwk.this, NO_ARGS);
                    ++params.testCount;
                } catch( IllegalAccessException e ) {
                    errln("Can't access test method " + testMethod.getName());
                } catch( InvocationTargetException e ) {
                    
                    String msg = "Uncaught exception " + getStackTrace(e);
                    
		    if (params.nothrow) {
                        errln(msg);
                    } else {
                        e.getTargetException().printStackTrace(System.out);
                        throw new RuntimeException(msg);
                    }
                }
            }
        }
        private String getStackTrace(InvocationTargetException e){
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(bs);
            e.getTargetException().printStackTrace(ps);
            return bs.toString();
        }
    }

    public class ClassTarget extends Target {
        String targetName;
        
        public ClassTarget() {
            this(null);
        }

        public ClassTarget(String targetName) {
            super(getClassTargetName(TestFmwk.this.getClass()));
            this.targetName = targetName;
        }

        protected boolean validate() {
            return TestFmwk.this.validate();
        }

        protected String getDescription() {
            return TestFmwk.this.getDescription();
        }

        protected void execute() {
            params.indentLevel++;
            Target target = randomize(getTargets(targetName));
            while (target != null) {
                target.run();
                target = target.next;
            }
            params.indentLevel--;
        }

        private Target randomize(Target t) {
            if (t != null && t.getNext() != null) {
                ArrayList list = new ArrayList();
                while (t != null) {
                    list.add(t);
                    t = t.getNext();
                }

                Target[] arr = (Target[])list.toArray(new Target[list.size()]);

                if (true) { // todo - add to params?
                    // different jvms return class methods in different orders,
                    // so we sort them (always, and then randomize them, so that 
                    // forcing a seed will also work across jvms).
                    Arrays.sort(arr, new Comparator() {
                            public int compare(Object lhs, Object rhs) {
                                // sort in reverse order, later we link up in forward order
                                return ((Target)rhs).name.compareTo(((Target)lhs).name);
                            }
                        });

                    // t is null to start, ends up as first element (arr[arr.length-1])
                    for (int i = 0; i < arr.length; ++i) {
                        t = arr[i].setNext(t); // relink in forward order
                    }
                }

                if (params.random != null) {
                    t = null; // reset t to null
                    Random r = params.random;
                    for (int i = arr.length; --i >= 1;) {
                        int x = r.nextInt(i+1);
                        t = arr[x].setNext(t);
                        arr[x] = arr[i];
                    }

                    t = arr[0].setNext(t); // new first element
                }
            }

            return t;
        }
    }

    //------------------------------------------------------------------------
    // Everything below here is boilerplate code that makes it possible
    // to add a new test by simply adding a function to an existing class
    //------------------------------------------------------------------------

    protected TestFmwk() {
    }

    /**
     * Parse arguments into a TestParams object and a collection of
     * target paths.  If there was an error parsing the TestParams,
     * print usage and exit with -1.  Otherwise, call
     * resolveTarget(TestParams, String) for each path, and run the
     * returned target.  After the last test returns, if prompt is
     * set, prompt and wait for input from stdin.  Finally, exit with
     * number of errors.
     *
     * This method never returns, since it always exits with System.exit(); 
     */
    public void run(String[] args) {
        TestParams params = new TestParams();

        ArrayList targets = null;
        boolean prompt = false;
        int exitCode = 0;
        boolean usageError = false;
        String filter = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.charAt(0) == '-') {
                arg = arg.toLowerCase();
                if (arg.equals("-verbose") || arg.equals("-v")) {
                    params.verbose = true;
                    params.quiet = false;
                } else if (arg.equals("-quiet") || arg.equals("-q")) {
                    params.quiet = true;
                    params.verbose = false;
                } else if (arg.equals("-help") || arg.equals("-h")) {
                    usageError = true;
                } else if (arg.equals("-warning") || arg.equals("-w")) {
                    params.warnings = true;
                } else if (arg.equals("-nodata") || arg.equals("-nd")) {
                    params.nodata = true;
                } else if (arg.equals("-prompt")) {
                    prompt = true;
                } else if (arg.equals("-list") || arg.equals("-l")) {
                    params.listlevel = 1;
                } else if (arg.equals("-listall") || arg.equals("-la")) {
                    params.listlevel = 2;
                } else if (arg.equals("-listexaustive") || arg.equals("-le")) {
                    params.listlevel = 3;
                } else if (arg.equals("-nothrow") || arg.equals("-n")) {
                    params.nothrow = true;
                    params.errorSummary = new StringBuffer();
                } else if (arg.equals("-describe") || arg.equals("-d")) {
                    params.describe = true;
                } else if (arg.startsWith("-r")) {
                    String s = null;
                    int n = arg.indexOf(':');
                    if (n != -1) {
                        s = arg.substring(n+1);
                        arg = arg.substring(0, n);
                    }

                    if (arg.equals("-r") || arg.equals("-random")) {
                        if (s == null) {
                            params.seed = System.currentTimeMillis();
                        } else {
                            params.seed = Long.parseLong(s);
                        }
                    } else {
                        System.out.println("*** Error: unrecognized argument: " + arg);
                        exitCode = 1;
                        usageError = true;
                    }
                } else if (arg.startsWith("-e")) {
                    // see above
                    params.inclusion = (arg.length() == 2) ? 5 : Integer.parseInt(arg.substring(2));
                    if (params.inclusion < 0 || params.inclusion > 10) {
                        usageError = true;
                    }
                } else if (arg.startsWith("-tfilter:")) {
                    params.tfilter = arg.substring(8);
                } else if (arg.startsWith("-filter:")) {
                    String temp = arg.substring(8).toLowerCase();
                    filter = filter == null ? temp : filter + "," + temp;
                } else if (arg.startsWith("-f:")) {
                    String temp = arg.substring(3).toLowerCase();
                    filter = filter == null ? temp : filter + "," + temp;
                } else if (arg.startsWith("-s")) {
                    params.log = new NullWriter();
                } else {
                    System.out.println("*** Error: unrecognized argument: " + args[i]);
                    exitCode = 1;
                    usageError = true;
                }
            } else {
                if (targets == null) {
                    targets = new ArrayList();
                }
                targets.add(arg);
            }
        }

        if (usageError) {
            usage();
            System.exit(exitCode);
        } 

        if (filter != null) {
            params.filter = filter.toLowerCase();
        }

        try {
            if (targets == null) {
                params.init();
                resolveTarget(params).run();
            } else {
                for (int i = 0; i < targets.size(); ++i) {
                    if (i > 0) {
                        System.out.println();
                    }

                    //String target = (String)targets.get(i);
                    params.init();
                    resolveTarget(params, (String)targets.get(i)).run();
                }
            }
        }
        catch (Exception e) {
	    e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("encountered exception, exiting");
        }

        if (params.seed != 0) {
            System.out.println("-random:" + params.seed);
            System.out.flush();
        }

        if (params.errorSummary != null && params.errorSummary.length() > 0) {
            System.out.println("\nError summary:");
            System.out.println(params.errorSummary.toString());
        }

        if (prompt) {
            System.out.println("Hit RETURN to exit...");
            try {
                System.in.read();
            } catch (IOException e) {
                System.out.println("Exception: " + e.toString() + e.getMessage());
            }
        }

        System.exit(params.errorCount);
    }

    /**
     * Return a ClassTarget for this test.  Params is set on this test.
     */
    public Target resolveTarget(TestParams params) {
        this.params = params;
        return new ClassTarget();
    }

    /**
     * Resolve a path from this test to a target.  If this test has subtests, and the
     * path contains '/', the portion before the '/' is resolved to a
     * subtest, until the path is consumed or the test has no
     * subtests.  Returns a ClassTarget created using the 
     * resolved test and remaining path (which ought to be null or a
     * method name).  Params is set on the target's test.
     */
    public Target resolveTarget(TestParams params, String targetPath) {
        TestFmwk test = this;
        test.params = params;

        if (targetPath != null) {
            if (targetPath.length() == 0) {
                targetPath = null;
            } else {
                int p = 0;
                int e = targetPath.length();

                // trim all leading and trailing '/'
                while (targetPath.charAt(p) == '/') {
                    ++p;
                }
                while (e > p && targetPath.charAt(e-1) == '/') {
                    --e;
                }
                if (p > 0 || e < targetPath.length()) {
                    targetPath = targetPath.substring(p, e-p);
                    p = 0;
                    e = targetPath.length();
                }

                try {
                    for (;;) {
                        int n = targetPath.indexOf('/');
                        String prefix = n == -1 ? targetPath : targetPath.substring(0, n);
                        TestFmwk subtest = test.getSubtest(prefix);

                        if (subtest == null) {
                            break;
                        }

                        test = subtest;

                        if (n == -1) {
                            targetPath = null;
                            break;
                        }

                        targetPath = targetPath.substring(n+1);
                    }
                } catch (TestFmwkException ex) {
                    return test.new Target(targetPath);
                }
            }
        }

        return test.new ClassTarget(targetPath);
    }

    /**
     * Return true if we can run this test (allows test to inspect jvm, environment, params before running)
     */
    protected boolean validate() {
        return true;
    }

    /**
     * Return the targets for this test.  If targetName is null, return all
     * targets, otherwise return a target for just that name.  The returned
     * target can be null.
     *
     * The default implementation returns a MethodTarget for each public
     * method of the object's class whose name starts with "Test" or "test".
     */
    protected Target getTargets(String targetName) {
        Class cls = getClass();
        if (targetName != null) {
            try {
                Method method = cls.getMethod(targetName, null);
                return new MethodTarget(targetName, method);
            } catch (NoSuchMethodException e) {
                return new Target(targetName); // invalid target
            } catch (SecurityException e) {
                return null;
            }
        } else {
            Target target = null;
            if (params.doMethods()) {
                Method[] methods = cls.getDeclaredMethods();
                for (int i = methods.length; --i >= 0;) {
                    String name = methods[i].getName();
                    if (name.startsWith("Test") || name.startsWith("test")) {
                        target = new MethodTarget(name, methods[i]).setNext(target);
                    }
                }
            }
            return target;
        }
    }

    protected String getDescription() {
        return null;
    }

    protected boolean validateMethod(String name) {
        return true;
    }

    protected String getMethodDescription(String name) {
        return null;
    }

    // method tests have no subtests, group tests override
    protected TestFmwk getSubtest(String prefix) throws TestFmwkException {
        return null;
    }

    public boolean isVerbose() {
        return params.verbose;
    }

    public boolean noData() {
        return params.nodata;
    }

    /**
     * 0 = fewest tests, 5 is normal build, 10 is most tests
     */
    public int getInclusion() {
        return params.inclusion;
    }

    public boolean isModularBuild() {
        return params.warnings;
    }

    public boolean isQuick() {
        return params.inclusion == 0;
    }

    public void msg(String message, int level, boolean incCount, boolean newln) {
        params.msg(message, level, incCount, newln);
    }
    
    protected int getErrorCount() {
        return params.errorCount;
    }

    protected TimeZone safeGetTimeZone(String id) {
        TimeZone tz = TimeZone.getTimeZone(id);
        if (tz == null) {
            // should never happen
            errln("FAIL: TimeZone.getTimeZone(" + id + ") => null");
        }
        if (!tz.getID().equals(id)) {
            errln("FAIL: TimeZone.getTimeZone(" + id + ") => " + tz.getID());
        }
        return tz;
    }

    /**
     * Print a usage message for this test class.
     */
    void usage() {
        System.out.println("Usage: " + getClass().getName() + " option* target*");
        System.out.println();
        System.out.println("Options:");
        System.out.println(" -describe Print a short descriptive string for this test and all");
        System.out.println("       listed targets.");
        System.out.println(" -e<n> Set exhaustiveness from 0..10.  Default is 0, fewest tests.\n" +
                           "       To run all tests, specify -e10.  Giving -e with no <n> is\n" +
                           "       the same as -e5.");
        System.out.println(" -filter:<str> Only tests matching filter will be run or listed.\n"
                         + "       <str> is of the form ['^']text[','['^']text].\n"
                         + "       Each string delimited by ',' is a separate filter argument.\n"
                         + "       If '^' is prepended to an argument, its matches are excluded.\n"
                         + "       Filtering operates on test groups as well as tests, if a test\n"
                         + "       group is included, all its subtests that are not excluded will\n"
                         + "       be run.  Examples:\n"
                         + "    -filter:A -- only tests matching A are run.  If A matches a group,\n"
                         + "       all subtests of this group are run.\n"
                         + "    -filter:^A -- all tests except those matching A are run.  If A matches\n"
                         +"        a group, no subtest of that group will be run.\n"
                         + "    -filter:A,B,^C,^D -- tests matching A or B and not C and not D are run\n"
                         + "       Note: Filters are case insensitive.");
        System.out.println(" -h[elp] Print this help text and exit.");
        System.out.println(" -l[ist] List immediate targets of this test");
        System.out.println("   -la, -listAll List immediate targets of this test, and all subtests");
        System.out.println("   -le, -listExaustive List all subtests and targets");
        System.out.println(" -n[othrow] Message on test failure rather than exception");
        System.out.println(" -prompt Prompt before exiting");
        System.out.println(" -q[uiet] Do not show warnings");
        System.out.println(" -r[andom][:<n>] If present, randomize targets.  If n is present,\n" +
                           "       use it as the seed.  If random is not set, targets will\n" +
                           "       be in alphabetical order to ensure cross-platform consistency.");
        System.out.println(" -s[ilent] No output except error summary or exceptions.\n");
        System.out.println(" -tfilter:<str> Transliterator Test filter of ids.");
        System.out.println(" -v[erbose] Show log messages");
        System.out.println(" -w[arning] Continue in presence of warnings, and disable missing test warnings.");
        System.out.println(" -nodata | -nd Do not warn if resource data is not present.");
        System.out.println();
        System.out.println(" If a list or describe option is provided, no tests are run.");
        System.out.println();
        System.out.println("Targets:");
        System.out.println(" If no target is specified, all targets for this test are run.");
        System.out.println(" If a target contains no '/' characters, and matches a target");
        System.out.println(" of this test, the target is run.  Otherwise, the part before the");
        System.out.println(" '/' is used to match a subtest, which then evaluates the");
        System.out.println(" remainder of the target as above.  Target matching is case-insensitive.");
        System.out.println();
        System.out.println(" If multiple targets are provided, each is executed in order.");
    }

    public static String hex(char ch) {
        StringBuffer result = new StringBuffer();
        String foo = Integer.toString(ch,16).toUpperCase();
        for (int i = foo.length(); i < 4; ++i) {
            result.append('0');
        }
        return result + foo;
    }

    public static String hex(int ch) {
        StringBuffer result = new StringBuffer();
        String foo = Integer.toString(ch,16).toUpperCase();
        for (int i = foo.length(); i < 4; ++i) {
            result.append('0');
        }
        return result + foo;
    }

    public static String hex(String s) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < s.length(); ++i) {
            if (i != 0) result.append(',');
            result.append(hex(s.charAt(i)));
        }
        return result.toString();
    }

    public static String hex(StringBuffer s) {
        return hex(s.toString());
    }
    public static String prettify(String s) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < s.length(); ++i) {
            char ch =s.charAt(i);
            if(ch > 0x7f){
                result.append("\\u");
                result.append(hex(ch));
            }else{
                result.append(ch);
            }
            
        }
        return result.toString();
    }
    public static String prettify(StringBuffer s) {
        return prettify(s.toString());
    }

    private static java.util.GregorianCalendar cal;

    /**
     * Return a Date given a year, month, and day of month.  This is
     * similar to new Date(y-1900, m, d).  It uses the default time
     * zone at the time this method is first called.
     * @param year use 2000 for 2000, unlike new Date()
     * @param month use Calendar.JANUARY etc.
     * @param dom day of month, 1-based
     * @return a Date object for the given y/m/d
     */
    protected static synchronized java.util.Date getDate(int year, int month, int dom) {
        if (cal == null) {
            cal = new java.util.GregorianCalendar();
        }
        cal.clear();
        cal.set(year, month, dom);
        return cal.getTime();
    }

    private static class NullWriter extends PrintWriter {
        public NullWriter() {
            super(System.out, false);
        }
        public void write(int c) {
        }
        public void write(char[] buf, int off, int len) {
        }
        public void write(String s, int off, int len) {
        }
        public void println() {
        }
    }

    private static class ASCIIWriter extends PrintWriter {
        private Writer w;
        private StringBuffer buffer = new StringBuffer();

        // Characters that we think are printable but that escapeUnprintable
        // doesn't
        private static final String PRINTABLES = "\t\n\r";

        public ASCIIWriter(Writer w, boolean autoFlush) {
            super(w, autoFlush);
        }

        public ASCIIWriter(OutputStream os, boolean autoFlush) {
            super(os, autoFlush);
        }

        public void write(int c) {
            synchronized(lock) {
                buffer.setLength(0);
                if (PRINTABLES.indexOf(c)<0 && TestUtil.escapeUnprintable(buffer, c)) {
                    super.write(buffer.toString());
                } else {
                    super.write(c);
                }
            }
        }

        public void write(char[] buf, int off, int len) {
            synchronized (lock) {
                buffer.setLength(0);
                int limit = off + len;
                while (off < limit) {
                    int c = UTF16Util.charAt(buf, 0, buf.length, off);
                    off += UTF16Util.getCharCount(c);
                    if (PRINTABLES.indexOf(c)<0 && TestUtil.escapeUnprintable(buffer, c)) {
                        super.write(buffer.toString());
                        buffer.setLength(0);
                    } else {
                        super.write(c);
                    }
                }
            }
        }

        public void write(String s, int off, int len) {
            write(s.substring(off, off + len).toCharArray(), 0, len);
        }
    }

    // filters
    // match against the entire hierarchy
    // A;B;!C;!D --> (A ||B) && (!C && !D)
    // positive, negative, unknown matches
    // positive -- known to be included, negative- known to be excluded
    // positive only if no excludes, and matches at least one include, if any
    // negative only if matches at least one exclude
    // otherwise, we wait

    public static class TestParams {
        public boolean   prompt;
        public boolean   nothrow;
        public boolean   verbose;
        public boolean   quiet;
        public int       listlevel;
        public boolean   describe;
        public boolean   warnings;
        public boolean   nodata;
        public int       inclusion;
        public String    filter;
        public long      seed;
        public String    tfilter; // for transliterator tests
        
        private State    stack;

        private StringBuffer errorSummary;

        private PrintWriter log = new ASCIIWriter(System.out, true);
        private int         indentLevel;
        private boolean     needLineFeed;
        private boolean     suppressIndent;
        private int         errorCount;
        private int         warnCount;
        private int         invalidCount;
        private int         testCount;
        public Random       random;

        public void init() {
            indentLevel = 0;
            needLineFeed = false;
            suppressIndent = false;
            errorCount = 0;
            warnCount = 0;
            invalidCount = 0;
            testCount = 0;
            random = seed == 0 ? null : new Random(seed);
        }
        
        private class State {
            State link;
            String name;
            StringBuffer buffer;
            int level;
            int ec;
            int wc;
            int ic;
            int tc;
            boolean flushed;
            boolean included;

            public State(State link, String name, boolean included) {
                this.link = link;
                this.name = name;
                if (link == null) {
                    this.level = 0;
                    this.included = included;
                } else {
                    this.level = link.level + 1;
                    this.included = included || link.included;
                }
                this.ec = errorCount;
                this.wc = warnCount;
                this.ic = invalidCount;
                this.tc = testCount;

                if (link == null || this.included) {
                    flush();
                }
            }

            void flush() {
                if (!flushed) {
                    if (link != null) {
                        link.flush();
                    }

                    indent(level);
                    log.print(name);
                    log.flush();

                    flushed = true;

                    needLineFeed = true;
                }
            }

            void appendPath(StringBuffer buf) {
                if (this.link != null) {
                    this.link.appendPath(buf);
                    buf.append('/');
                }
                buf.append(name);
            }
        }

        public void push(String name, String description, boolean included) {
            if (inDocMode() && describe && description != null) {
                name += ": " + description;
            }
            stack = new State(stack, name, included);
        }

        public void pop() {
            if (stack != null) {
                writeTestResult(stack.ec, stack.ic, stack.tc);
                stack = stack.link;
            }
        }
            
        public boolean inDocMode() {
            return describe || listlevel != 0;
        }

        public boolean doMethods() {
            return !inDocMode() || listlevel == 3 || (indentLevel == 1 && listlevel > 0);
        }

        public boolean doRecurse() {
            return !inDocMode() || listlevel > 1 || (indentLevel == 1 && listlevel > 0);
        }

        public boolean doRecurseGroupsOnly() {
            return inDocMode() && (listlevel == 2 || (indentLevel == 1 && listlevel > 0));
        }

	// return 0, -1, or 1
	// 1: run this test
	// 0: might run this test, no positive include or exclude on this group
	// -1: exclude this test
        public int filter(String testName) {
            int result = 0;
            if (filter == null) {
                result = 1;
            } else {
                boolean noIncludes = true;
                boolean noExcludes = filter.indexOf('^') == -1;
                testName = testName.toLowerCase();
                int ix = 0;
                while (ix < filter.length()) {
                    int nix = filter.indexOf(',', ix);
                    if (nix == -1) {
                        nix = filter.length();
                    }
                    if (filter.charAt(ix) == '^') {
                        if (testName.indexOf(filter.substring(ix + 1, nix)) != -1) {
                            result = -1;
                            break;
                        }
                    } else {
                        noIncludes = false;
                        if (testName.indexOf(filter.substring(ix, nix)) != -1) {
                            result = 1;
                            if (noExcludes) {
                                break;
                            }
                        }
                    }

                    ix = nix + 1;
                }
                if (result == 0 && noIncludes) {
                    result = 1;
                }
            }
//              System.out.println("filter: " + testName + " returns: " + result);
            return result;
        }

        private void msg(String message, int level, boolean incCount, boolean newln) {
            if (level == WARN && !warnings) {
                level = ERR;
            }

            if (incCount) {
                if (level == WARN) {
                    warnCount++;
                    invalidCount++;
                } else if (level == ERR) {
                    errorCount++;
                }
            }

            // should roll indentation stuff into log ???
            if (verbose || level > (quiet ? WARN : LOG)) {
                if (!suppressIndent) {
                    indent(indentLevel + 1);
                    final String[] MSGNAMES = { "", "Warning: ", "Error: " };
                    log.print(MSGNAMES[level]);
                }

                log.print(message);
                if (newln) {
                    log.println();
                }
                log.flush();
            }

            if (level == ERR) {
                if (!nothrow) {
                    throw new RuntimeException(message);
                }
                if (!suppressIndent && errorSummary != null && (errorCount == stack.ec + 1)) {
                    stack.appendPath(errorSummary);
                    errorSummary.append("\n");
                }
            }

            suppressIndent = !newln;
        }

        private void writeTestInvalid(String name) {
//              msg("***" + name + "*** not found or not valid.", WARN, true, true);
            if (inDocMode()) {
                if (!warnings) {
                    if (stack != null) {
			stack.flush();
		    }
                    log.println(" *** Target not found or not valid.");
                    log.flush();
                    needLineFeed = false;
                }
            } else {
                msg("Test " + name + " not found or not valid.", WARN, true, true);
            }
        }

        private void writeTestResult(int oldFail, int oldInvalid, int oldTestCount) {
            if (inDocMode()) {
                if (needLineFeed) {
                    log.println();
                    log.flush();
                }
                needLineFeed = false;
                return;
            }

            int errorDelta = errorCount - oldFail;
            int invalidDelta = invalidCount - oldInvalid;
            int testDelta = testCount - oldTestCount;
            if (testDelta == 0) {
                return;
            }

            stack.flush();

            if (!needLineFeed) {
                indent(indentLevel);
                log.print("}");
            }
            needLineFeed = false;

            if (errorDelta != 0) {
                log.println(" FAILED (" + errorDelta + " failures" +
                            ((invalidDelta != 0) ?
                             ", " + invalidDelta + " tests skipped)" :
                             ")"));
            } else if (invalidDelta != 0) {
                log.println(" Qualified (" + invalidDelta + " tests skipped)");
            } else {
                log.println(" Passed");
            }
        }

        private final void indent(int distance) {
            boolean idm = inDocMode();
            if (needLineFeed) {
                if (idm) {
                    log.println();
                } else {
                    log.println(" {");
                }
                needLineFeed = false;
            }
                
            log.print(spaces.substring(0, distance * (idm ? 3 : 2)));

            if (idm) {
                log.print("-- ");
            }
        }
    }

    public String getTranslitTestFilter() {
        return params.tfilter;
    }

    /**
     * Return the target name for a test class.  This is either the
     * end of the class name, or if the class declares a public static
     * field CLASS_TARGET_NAME, the value of that field.
     */
    private static String getClassTargetName(Class testClass) {
        String name = testClass.getName();
        try {
            Field f = testClass.getField("CLASS_TARGET_NAME");
            name = (String)f.get(null);
        } 
        catch (IllegalAccessException e) {
            throw new InternalError("static field CLASS_TARGET_NAME must be accessible");
        }
        catch (NoSuchFieldException e) {
            int n = Math.max(name.lastIndexOf('.'), name.lastIndexOf('$'));
            if (n != -1) {
                name = name.substring(n+1);
            }
        }
        return name;
    }

    /**
     * Check the given array to see that all the strings
     * in the expected array are present.
     * @param msg string message, for log output
     * @param array array of strings to check
     * @param expected array of strings we expect to see, or null
     * @return the length of 'array', or -1 on error
     */
    protected int checkArray(String msg,
                             String array[],
                             String expected[]) {
        int explen = (expected != null) ? expected.length : 0;
        if (!(explen >= 0 && explen < 31)) { // [sic] 31 not 32
            errln("FAIL: Internal error");
            return -1;
        }
        int i = 0;
        StringBuffer buf = new StringBuffer();
        int seenMask = 0;
        for (; i<array.length; ++i) {
            String s = array[i];
            if (i != 0) buf.append(", ");
            buf.append(s);
            // check expected list
            for (int j=0, bit=1; j<explen; ++j, bit<<=1) {
                if ((seenMask&bit)==0) {
                    if (s.equals(expected[j])) {
                        seenMask |= bit;
                        logln("Ok: \"" + s + "\" seen");
                    }
                }
            }
        }
        logln(msg + " = [" + buf + "] (" + i + ")");
        // did we see all expected strings?
        if (((1<<explen)-1) != seenMask) {
            for (int j=0, bit=1; j<expected.length; ++j, bit<<=1) {
                if ((seenMask&bit)==0) {
                    errln("\"" + expected[j] + "\" not seen");
                }
            }
        }
        return array.length;
    }

    /**
     * Check the given array to see that all the locales
     * in the expected array are present.
     * @param msg string message, for log output
     * @param array array of locales to check
     * @param expected array of locales names we expect to see, or null
     * @return the length of 'array'
     */
    protected int checkArray(String msg,
                             Locale array[],
                             String expected[]) {
        String strs[] = new String[array.length];
        for (int i=0; i<array.length; ++i) strs[i] = array[i].toString(); 
        return checkArray(msg, strs, expected);
    }
    
    /**
     * Check the given array to see that all the locales
     * in the expected array are present.
     * @param msg string message, for log output
     * @param array array of locales to check
     * @param expected array of locales names we expect to see, or null
     * @return the length of 'array'
     */
    protected int checkArray(String msg,
                             ULocale array[],
                             String expected[]) {
        String strs[] = new String[array.length];
        for (int i=0; i<array.length; ++i) strs[i] = array[i].toString(); 
        return checkArray(msg, strs, expected);
    }
    
    /**
     * JUnit-like assertions.
     */
    protected boolean assertTrue(String message, boolean condition) {
        if (!condition) {
            errln("assertTrue() failed: " + message);
        } else if (VERBOSE_ASSERTIONS) {
            logln("Ok: " + message);
        }
        return condition;
    }
    protected boolean assertFalse(String message, boolean condition) {
        if (condition) {
            errln("assertFalse() failed: " + message);
        } else if (VERBOSE_ASSERTIONS) {
            logln("Ok: " + message);
        }
        return !condition;
    }
    protected boolean assertEquals(String message,
                                   Object expected, Object actual) {
        if (!expected.equals(actual)) {
            if (actual instanceof String) {
                // display quotes around string objects, for clarity
                actual = "\"" + actual + '"';
                expected = "\"" + expected + '"';
            }
            errln(message + "; got " + actual +
                  "; expected " + expected);
            return false;
        } else if (VERBOSE_ASSERTIONS) {
            if (actual instanceof String) {
                // display quotes around string objects, for clarity
                actual = "\"" + actual + '"';
            }
            logln("Ok: " + message + "; got " + actual);
        }
        return true;
    }
    protected void fail(String message) {
        errln(message);
    }

    private static final boolean VERBOSE_ASSERTIONS = true;

    protected TestParams params = null;

    private final static String spaces = "                                          ";
}
