/*
 *******************************************************************************
 * Copyright (C) 2006, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.tool.docs;

import java.io.*;

public class SwatDeprecated {
    private File srcFile;
    private File dstFile;
    private int maxLength = 85;
    private String srcPrefix;
    private String dstPrefix;
    private String srcTag;
    private String trgTag;
    private boolean overwrite;
    private int verbosity;
    private int cc; // changed file count
    private boolean inPlace;

    private PrintWriter pw = new PrintWriter(System.out);

    private static FilenameFilter ff = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (new File(dir, name).isDirectory() && !"CVS".equals(name)) ||
                    (!name.equals("SwatDeprecated.java") && name.endsWith(".java"));
            }
        };

    public static void main(String[] args) {
        String src = System.getProperty("user.dir");
        String dst = src;
        boolean dep = true;
        boolean ovr = false;
        int vrb = 1;

        for (int i = 0; i < args.length; ++i) {
            String arg = args[i].toLowerCase();
            if (arg.charAt(0) == '-') {
                if (arg.equals("-src")) {
                    src = args[++i];
                }
                else if (arg.equals("-dst")) {
                    dst = args[++i];
                }
                else if (arg.equals("-dep")) {
                    dep = true;
                } 
                else if (arg.equals("-prov")) {
                    dep = false;
                }
                else if (arg.equals("-overwrite")) {
                    ovr = true;
                }
                else if (arg.equals("-silent")) { // no output
                    vrb = 0;
                }
                else if (arg.equals("-quiet")) { // output parameters and count of changed files (default)
                    vrb = 1;
                } 
                else if (arg.equals("-verbose")) { // output names of modified files
                    vrb = 2;
                } 
                else if (arg.equals("-noisy")) { // output names of files not modified
                    vrb = 3;
                } 
                else if (arg.equals("-copydebug")) { // output copyright debugging
                    vrb = 4;
                }
                else if (arg.equals("-debug")) { // output all debugging
                    vrb = 5;
                }
            }
        }

        new SwatDeprecated(src, dst, dep, ovr, vrb).run();
    }

    public SwatDeprecated(String src, String dst, boolean dep, boolean overwrite, int verbosity) {
        this.srcFile = new File(src);
        this.dstFile = new File(dst);
        this.overwrite = overwrite;
        this.verbosity = verbosity;

        this.srcTag = "@deprecated This is a draft API and might change in a future release of ICU.";
        this.trgTag = "@provisional";
        if (!dep) {
            String temp = srcTag;
            srcTag = trgTag;
            trgTag = temp;
        }
        try {
            this.srcPrefix = srcFile.getCanonicalPath();
            this.dstPrefix = dstFile.getCanonicalPath();
        }
        catch (IOException e) {
            RuntimeException re = new RuntimeException(e.getMessage());
            re.initCause(e);
            throw re;
        }

        this.inPlace = srcPrefix.equals(dstPrefix);
        this.cc = 0;

        if (verbosity >= 1) {
            pw.println("replacing '" + srcTag + "'");
            pw.println("     with '" + trgTag + "'");
            pw.println();
            pw.println("     source: '" + srcPrefix + "'");
            pw.println("destination: '" + dstPrefix + "'");
            pw.println("  overwrite: " + overwrite);
            pw.println("  verbosity: " + verbosity);
            pw.flush();
        }
    }

    public void run() {
        if (!srcFile.exists()) {
            throw new RuntimeException("file " + srcFile.getPath() + " does not exist.");
        }
        doList(srcFile);
        if (verbosity >= 1) {
            pw.println("changed " + cc + " file(s)");
            pw.flush();
        }
    }

    public void doList(File file) {
        String[] filenames = file.list(ff);
        if (verbosity >= 5) {
            pw.println(file.getPath());
            dumpList(filenames);
            pw.flush();
        }
        for (int i = 0; i < filenames.length; ++i) {
            File f = new File(file, filenames[i]);
            if (f.isDirectory()) {
                doList(f);
            } else {
                processFile(f);
            }
        }
    }

    /*
      if infile != outfile
      - if not overwrite and outfile exists, exit with error
      - if outfile.tmp exists, delete it
      --  exit with error if fail to delete
      - create new outfile.tmp
      - if don't need it, 
      --  delete outfile.tmp
      --  exit with nothing done
      - if outfile.old exists, delete it
      --  if fail to delete
      ---   delete outfile.tmp
      ---   exit with error (extra error if outfile doesn't exist)
<     - if outfile exists, 
<     --  rename it to outfile.old
      - rename outfile.tmp to outfile
      - return success

      if infile == outfile
      - if not overwrite (we know outfile exists) exit with error
      - if outfile.tmp exists, delete it
      --  exit with error if fail to delete
>     - get infile out of the way for creation
>     --  if infile.bak exists, delete it
>     ---   exit with error if fail to delete
>     --  rename infile to infile.bak
      - create new outfile.tmp
      - if don't need it,
      --  delete outfile.tmp
>     --  rename infile.bak to infile
      --  exit with nothing done
      - if outfile.old exists, delete it
      --  if fail to delete
      ---   delete outfile.tmp
>     ---   rename infile.bak to infile
      ---   exit with error
>     - rename infile.bak to outfile.old
      - rename outfile.tmp to outfile
      - exit with success
     */
    public void processFile(File inFile) {
        File bakFile = null;
        File oldFile = null;
        try {
            String inPath = inFile.getCanonicalPath();
            if (verbosity >= 5) {
                pw.println("processFile: " + inPath);
            }

            String outPath = dstPrefix + inPath.substring(srcPrefix.length());
            File outFile = new File(outPath);

            File tmpFile = null;
            if (outFile.exists()) {
                if (!overwrite) {
                    throw new RuntimeException("no permission to overwrite file: " + outPath);
                } else {
                    bakFile = outFile;
                    tmpFile = File.createTempFile(inFile.getName(), null, inFile.getParentFile());
                }
            } else {
                tmpFile = outFile;
                File parent = tmpFile.getParentFile();
                parent.mkdirs();
                tmpFile.createNewFile();
            }

            String tmpPath = tmpFile.getPath();
            if (verbosity >= 5) {
                pw.println("tmpFile: " + tmpPath);
            }

            InputStream is = new FileInputStream(inFile);
            OutputStream os = new FileOutputStream(tmpFile);

            PrintStream ps = new PrintStream(os);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line;
            int n = 0;
            int tc = 0;
            boolean debug = false;
            while (null != (line = br.readLine())) {
                // int temp = line.indexOf("@deprecated");
                int ix = line.indexOf(srcTag);
//                 if (temp != -1 && ix == -1) {
//                     if (debug == false) {
//                         debug = true;
//                         pw.println("file: " + name);
//                     }
//                     pw.println("[" + n + "] " + line);
//                     pw.flush();
//                 }
                if (ix != -1) {
                    if (verbosity >= 5) {
                        pw.println("[" + n + "] " + line);
                    }

                    line = line.substring(0,ix) + trgTag;
                    
                    ++tc;
                } else if (n < 20) {
                    // swat copyrights while we're at it
                    ix = line.indexOf("opyright");
                    if (ix != -1) {
                        String nline = null;
                        do {
                            if (verbosity == 4) {
                                pw.println("[" + n + "] " + line);
                            }
                            ix = line.indexOf("-200");
                            if (ix != -1) {
                                nline = line.substring(0, ix) + "-2006" + line.substring(ix+5);
                                break;
                            }
                            ix = line.indexOf("- 200");
                            if (ix != -1) {
                                nline = line.substring(0, ix) + "-2006" + line.substring(ix+6);
                                break;
                            }
                            ix = line.indexOf("-199");
                            if (ix != -1) {
                                nline = line.substring(0, ix) + "-2006" + line.substring(ix+5);
                                break;
                            }
                            ix = line.indexOf("200");
                            if (ix != -1) {
                                nline = line.substring(0, ix) + "2006" + line.substring(ix+4);
                                break;
                            }
                            ix = line.indexOf("199");
                            if (ix != -1) {
                                nline = line.substring(0, ix) + "2006" + line.substring(ix+4);
                                break;
                            }
                        } while (false);

                        if (nline != null) {
                            if (verbosity >= 4) {
                                pw.println("  --> " + nline);
                            }
                            line = nline;
                        }
                    }
                }
                ps.println(line);
                ++n;
            }
            ps.flush();
            is.close();
            os.close();

            if (tc == 0) { // nothing changed, forget this file
                if (verbosity >= 3) {
                    pw.println("no changes in file: " + inPath);
                }
                if (!tmpFile.delete()) {
                    throw new RuntimeException("unable to delete unneeded temporary file: " + tmpPath);
                }

                return;
            }

            if (bakFile != null) {
                if (bakFile.exists()) {
                    bakFile.delete();
                }
                if (!tmpFile.renameTo(bakFile)) {
                    pw.println("warning: couldn't rename temp file to: " + outPath);
                }
            }

            outFile.setLastModified(inFile.lastModified());

            if (verbosity >= 2) {
                pw.println(inPath);
                pw.flush();
            }
        }
        catch (IOException e) {
            RuntimeException re = new RuntimeException(e.getMessage());
            re.initCause(e);
            throw re;
        }
        finally {
            pw.flush();
        }

        ++cc;
    }

    public void dumpList(String[] names) {
        if (names == null) {
            pw.print("null");
        } else {
            pw.print("{");
            int lc = 0;
            if (names.length > 0) {
                pw.println();
                pw.print("    ");
                lc = 4;
            }
            for (int i = 0; i < names.length; ++i) {
                String name = names[i];
                int nl = name.length();
                if (lc + nl > maxLength) {
                    pw.println();
                    pw.print("    ");
                    lc = 4;
                }
                pw.print(name);
                pw.print(", ");
                lc += nl + 2;
            }
            if (names.length > 0) {
                pw.println();
            }
            pw.print("} ");
        }
    }
}
