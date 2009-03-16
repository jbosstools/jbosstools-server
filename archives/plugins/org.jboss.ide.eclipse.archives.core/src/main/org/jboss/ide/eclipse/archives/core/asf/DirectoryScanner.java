/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */


/*
 * This file has been CHANGED, MODIFIED, EDITED.
 * It has been coppied because list(File file) is
 * a private method and is not able to be overridden.
 *
 * For archives, we need to be able to delegate to
 * the eclipse VFS / resource model.
 * rob.stryker@redhat.com
 */
package org.jboss.ide.eclipse.archives.core.asf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.selectors.FileSelector;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.apache.tools.ant.util.FileUtils;

/**
 * Class for scanning a directory for files/directories which match certain
 * criteria.
 * <p>
 * These criteria consist of selectors and patterns which have been specified.
 * With the selectors you can select which files you want to have included.
 * Files which are not selected are excluded. With patterns you can include
 * or exclude files based on their filename.
 * <p>
 * The idea is simple. A given directory is recursively scanned for all files
 * and directories. Each file/directory is matched against a set of selectors,
 * including special support for matching against filenames with include and
 * and exclude patterns. Only files/directories which match at least one
 * pattern of the include pattern list or other file selector, and don't match
 * any pattern of the exclude pattern list or fail to match against a required
 * selector will be placed in the list of files/directories found.
 * <p>
 * When no list of include patterns is supplied, "**" will be used, which
 * means that everything will be matched. When no list of exclude patterns is
 * supplied, an empty list is used, such that nothing will be excluded. When
 * no selectors are supplied, none are applied.
 * <p>
 * The filename pattern matching is done as follows:
 * The name to be matched is split up in path segments. A path segment is the
 * name of a directory or file, which is bounded by
 * <code>File.separator</code> ('/' under UNIX, '\' under Windows).
 * For example, "abc/def/ghi/xyz.java" is split up in the segments "abc",
 * "def","ghi" and "xyz.java".
 * The same is done for the pattern against which should be matched.
 * <p>
 * The segments of the name and the pattern are then matched against each
 * other. When '**' is used for a path segment in the pattern, it matches
 * zero or more path segments of the name.
 * <p>
 * There is a special case regarding the use of <code>File.separator</code>s
 * at the beginning of the pattern and the string to match:<br>
 * When a pattern starts with a <code>File.separator</code>, the string
 * to match must also start with a <code>File.separator</code>.
 * When a pattern does not start with a <code>File.separator</code>, the
 * string to match may not start with a <code>File.separator</code>.
 * When one of these rules is not obeyed, the string will not
 * match.
 * <p>
 * When a name path segment is matched against a pattern path segment, the
 * following special characters can be used:<br>
 * '*' matches zero or more characters<br>
 * '?' matches one character.
 * <p>
 * Examples:
 * <p>
 * "**\*.class" matches all .class files/dirs in a directory tree.
 * <p>
 * "test\a??.java" matches all files/dirs which start with an 'a', then two
 * more characters and then ".java", in a directory called test.
 * <p>
 * "**" matches everything in a directory tree.
 * <p>
 * "**\test\**\XYZ*" matches all files/dirs which start with "XYZ" and where
 * there is a parent directory called test (e.g. "abc\test\def\ghi\XYZ123").
 * <p>
 * Case sensitivity may be turned off if necessary. By default, it is
 * turned on.
 * <p>
 * Example of usage:
 * <pre>
 *   String[] includes = {"**\\*.class"};
 *   String[] excludes = {"modules\\*\\**"};
 *   ds.setIncludes(includes);
 *   ds.setExcludes(excludes);
 *   ds.setBasedir(new File("test"));
 *   ds.setCaseSensitive(true);
 *   ds.scan();
 *
 *   System.out.println("FILES:");
 *   String[] files = ds.getIncludedFiles();
 *   for (int i = 0; i < files.length; i++) {
 *     System.out.println(files[i]);
 *   }
 * </pre>
 * This will scan a directory called test for .class files, but excludes all
 * files in all proper subdirectories of a directory called "modules"
 *
 */

/*
 * Note: This class ideally should be translated, but since
 * it was stolen from ant, I'd rather leave it as close to the ant
 * version as possible.
 * 
 * Started to diverge quite a bit in preparation for 
 */
public class DirectoryScanner {

    /** Is OpenVMS the operating system we're running on? */
    private static final boolean ON_VMS = Os.isFamily("openvms");//$NON-NLS-1$

    protected static final String[] DEFAULTEXCLUDES = {
        // Miscellaneous typical temporary files
        "**/*~", //$NON-NLS-1$
        "**/#*#",//$NON-NLS-1$
        "**/.#*",//$NON-NLS-1$
        "**/%*%",//$NON-NLS-1$
        "**/._*",//$NON-NLS-1$

        // CVS
        "**/CVS",//$NON-NLS-1$
        "**/CVS/**",//$NON-NLS-1$
        "**/.cvsignore",//$NON-NLS-1$

        // SCCS
        "**/SCCS",//$NON-NLS-1$
        "**/SCCS/**",//$NON-NLS-1$

        // Visual SourceSafe
        "**/vssver.scc",//$NON-NLS-1$

        // Subversion
        "**/.svn",//$NON-NLS-1$
        "**/.svn/**",//$NON-NLS-1$

        // Mac
        "**/.DS_Store"//$NON-NLS-1$
    };

    /** Helper. */
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();

    /** iterations for case-sensitive scanning. */
    private static final boolean[] CS_SCAN_ONLY = new boolean[] {true};

    /** iterations for non-case-sensitive scanning. */
    private static final boolean[] CS_THEN_NON_CS = new boolean[] {true, false};

    /**
     * Patterns which should be excluded by default.
     *
     * @see #addDefaultExcludes()
     */
    private static Vector<String> defaultExcludes = new Vector<String>();
    static {
        resetDefaultExcludes();
    }

    // CheckStyle:VisibilityModifier OFF - bc

    /** The base directory to be scanned. */
    protected File basedir;

    /** The patterns for the files to be included. */
    protected String[] includes;

    /** The patterns for the files to be excluded. */
    protected String[] excludes;

    /** Selectors that will filter which files are in our candidate list. */
    protected FileSelector[] selectors = null;

    /**
     * The files which matched at least one include and no excludes
     * and were selected.
     */
    protected Vector<String> filesIncluded;

    /** The files which did not match any includes or selectors. */
    protected Vector<String> filesNotIncluded;

    /**
     * The files which matched at least one include and at least
     * one exclude.
     */
    protected Vector<String> filesExcluded;

    /**
     * The directories which matched at least one include and no excludes
     * and were selected.
     */
    protected Vector<String> dirsIncluded;

    /** The directories which were found and did not match any includes. */
    protected Vector<String> dirsNotIncluded;

    /**
     * The directories which matched at least one include and at least one
     * exclude.
     */
    protected Vector<String> dirsExcluded;

    /**
     * The files which matched at least one include and no excludes and
     * which a selector discarded.
     */
    protected Vector<String> filesDeselected;

    /**
     * The directories which matched at least one include and no excludes
     * but which a selector discarded.
     */
    protected Vector<String> dirsDeselected;

    /**
     * Whether or not the file system should be treated as a case sensitive
     * one.
     */
    protected boolean isCaseSensitive = true;


    /** Whether or not everything tested so far has been included. */
    protected boolean everythingIncluded = true;

    // CheckStyle:VisibilityModifier ON

    /**
     * Temporary table to speed up the various scanning methods.
     *
     * @since Ant 1.6
     */
    private Map<File, File[]> fileListMap = new HashMap<File, File[]>();

    /**
     * List of all scanned directories.
     *
     * @since Ant 1.6
     */
    private Set<String> scannedDirs = new HashSet<String>();

    /**
     * Set of all include patterns that are full file names and don't
     * contain any wildcards.
     *
     * <p>If this instance is not case sensitive, the file names get
     * turned to lower case.</p>
     *
     * <p>Gets lazily initialized on the first invocation of
     * isIncluded or isExcluded and cleared at the end of the scan
     * method (cleared in clearCaches, actually).</p>
     *
     * @since Ant 1.6.3
     */
    private Set<String> includeNonPatterns = new HashSet<String>();

    /**
     * Set of all include patterns that are full file names and don't
     * contain any wildcards.
     *
     * <p>If this instance is not case sensitive, the file names get
     * turned to lower case.</p>
     *
     * <p>Gets lazily initialized on the first invocation of
     * isIncluded or isExcluded and cleared at the end of the scan
     * method (cleared in clearCaches, actually).</p>
     *
     * @since Ant 1.6.3
     */
    private Set<String> excludeNonPatterns = new HashSet<String>();

    /**
     * Array of all include patterns that contain wildcards.
     *
     * <p>Gets lazily initialized on the first invocation of
     * isIncluded or isExcluded and cleared at the end of the scan
     * method (cleared in clearCaches, actually).</p>
     *
     * @since Ant 1.6.3
     */
    private String[] includePatterns;

    /**
     * Array of all exclude patterns that contain wildcards.
     *
     * <p>Gets lazily initialized on the first invocation of
     * isIncluded or isExcluded and cleared at the end of the scan
     * method (cleared in clearCaches, actually).</p>
     *
     * @since Ant 1.6.3
     */
    private String[] excludePatterns;

    /**
     * Have the non-pattern sets and pattern arrays for in- and
     * excludes been initialized?
     *
     * @since Ant 1.6.3
     */
    private boolean areNonPatternSetsReady = false;

    /**
     * Scanning flag.
     *
     * @since Ant 1.6.3
     */
    private boolean scanning = false;

    /**
     * Has this scan finished?
     */
    private boolean complete = false;

    /**
     * Scanning lock.
     *
     * @since Ant 1.6.3
     */
    private Object scanLock = new Object();

    /**
     * Exception thrown during scan.
     *
     * @since Ant 1.6.3
     */
    private IllegalStateException illegal = null;
    
    private DirectoryScannerIterator iterator = null;
    
    /**
     * Sole constructor.
     */
    public DirectoryScanner() {
    }

    /* Protected static class delegators to SelectorUtils */
    
    protected static boolean matchPatternStart(String pattern, String str) {
        return SelectorUtils.matchPatternStart(pattern, str);
    }

    protected static boolean matchPatternStart(String pattern, String str,
                                               boolean isCaseSensitive) {
        return SelectorUtils.matchPatternStart(pattern, str, isCaseSensitive);
    }

    protected static boolean matchPath(String pattern, String str) {
        return SelectorUtils.matchPath(pattern, str);
    }

    protected static boolean matchPath(String pattern, String str,
                                       boolean isCaseSensitive) {
        return SelectorUtils.matchPath(pattern, str, isCaseSensitive);
    }

    public static boolean match(String pattern, String str) {
        return SelectorUtils.match(pattern, str);
    }

    protected static boolean match(String pattern, String str,
                                   boolean isCaseSensitive) {
        return SelectorUtils.match(pattern, str, isCaseSensitive);
    }


    /* Working with default excludes */
    public static String[] getDefaultExcludes() {
        return defaultExcludes.toArray(
        		new String[defaultExcludes.size()]);
    }

	public static String implodeStrings(String[] strings) {
		StringBuffer buffer = new StringBuffer();
		for( int i = 0; i < strings.length; i++ )
			buffer.append(strings[i]).append(',');
		return buffer.toString();
	}
    
    public static boolean addDefaultExclude(String s) {
        if (defaultExcludes.indexOf(s) == -1) {
            defaultExcludes.add(s);
            return true;
        }
        return false;
    }

    public static boolean removeDefaultExclude(String s) {
        return defaultExcludes.remove(s);
    }

    public static void resetDefaultExcludes() {
        defaultExcludes = new Vector<String>();
        for (int i = 0; i < DEFAULTEXCLUDES.length; i++) {
            defaultExcludes.add(DEFAULTEXCLUDES[i]);
        }
    }
    /* End default excludes */

    
    /* Base dir */
    public void setBasedir(String basedir) {
        setBasedir(basedir == null ? (File) null
            : new File(basedir.replace('/', File.separatorChar).replace(
            '\\', File.separatorChar)));
    }

    public synchronized void setBasedir(File basedir) {
        this.basedir = basedir;
    }

    public synchronized File getBasedir() {
        return basedir;
    }
    /* end Base dir */


    public synchronized boolean isCaseSensitive() {
        return isCaseSensitive;
    }

    public synchronized void setCaseSensitive(boolean isCaseSensitive) {
        this.isCaseSensitive = isCaseSensitive;
    }

    public synchronized void setIncludes(String[] includes) {
        if (includes == null) {
            this.includes = null;
        } else {
            this.includes = new String[includes.length];
            for (int i = 0; i < includes.length; i++) {
                this.includes[i] = normalizePattern(includes[i]);
            }
        }
    }

    public synchronized void setExcludes(String[] excludes) {
        if (excludes == null) {
            this.excludes = null;
        } else {
            this.excludes = new String[excludes.length];
            for (int i = 0; i < excludes.length; i++) {
                this.excludes[i] = normalizePattern(excludes[i]);
            }
        }
    }

    public synchronized void addExcludes(String[] excludes) {
        if (excludes != null && excludes.length > 0) {
            if (this.excludes != null && this.excludes.length > 0) {
                String[] tmp = new String[excludes.length
                                          + this.excludes.length];
                System.arraycopy(this.excludes, 0, tmp, 0,
                                 this.excludes.length);
                for (int i = 0; i < excludes.length; i++) {
                    tmp[this.excludes.length + i] =
                        normalizePattern(excludes[i]);
                }
                this.excludes = tmp;
            } else {
                setExcludes(excludes);
            }
        }
    }

    private static String normalizePattern(String p) {
        String pattern = p.replace('/', File.separatorChar)
            .replace('\\', File.separatorChar);
        if (pattern.endsWith(File.separator)) {
            pattern += "**";//$NON-NLS-1$
        }
        return pattern;
    }

    public synchronized boolean isEverythingIncluded() {
        return everythingIncluded;
    }
    
    protected void scanWait() {
        synchronized (scanLock) {
            if (scanning) {
                while (scanning) {
                    try {
                        scanLock.wait();
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
                if (illegal != null) {
                    throw illegal;
                }
                return;
            }
            scanning = true;
        	complete = false;
        }
    }

    protected void scanPrepare() {
        tmpNullIncludes = (includes == null);
        tmpNullExcludes = (excludes == null);
        illegal = null;
        clearResults();
        boolean nullIncludes = includes == null;
        includes = includes == null ? new String[] {"**"} : includes;//$NON-NLS-1$
        excludes = excludes == null ? new String[0] : excludes;

        if (basedir == null ) {
            // if no basedir and no includes, nothing to do:
            if (nullIncludes) {
                return;
            }
        } else {
            if (!basedir.exists()) {
                illegal = new IllegalStateException("basedir " + basedir//$NON-NLS-1$
                                                    + " does not exist");//$NON-NLS-1$
            }
            if (!basedir.isDirectory()) {
                illegal = new IllegalStateException("basedir " + basedir//$NON-NLS-1$
                                                    + " is not a directory");//$NON-NLS-1$
            }
            if (illegal != null) {
                throw illegal;
            }
        }
        if (isIncluded("")) {//$NON-NLS-1$
            if (!isExcluded("")) {//$NON-NLS-1$
                if (isSelected("", basedir)) {//$NON-NLS-1$
                    dirsIncluded.addElement("");//$NON-NLS-1$
                } else {
                    dirsDeselected.addElement("");//$NON-NLS-1$
                }
            } else {
                dirsExcluded.addElement("");//$NON-NLS-1$
            }
        } else {
            dirsNotIncluded.addElement("");//$NON-NLS-1$
        }
    }
    
    private boolean tmpNullIncludes, tmpNullExcludes;
    /**
     * Scan for files which match at least one include pattern and don't match
     * any exclude patterns. If there are selectors then the files must pass
     * muster there, as well.  Scans under basedir, if set; otherwise the
     * include patterns without leading wildcards specify the absolute paths of
     * the files that may be included.
     *
     * @exception IllegalStateException if the base directory was set
     *            incorrectly (i.e. if it doesn't exist or isn't a directory).
     */
    public void scan() throws IllegalStateException {
    	scanWait();
        try {
            synchronized (this) {
                scanPrepare();
                runScan();
            }
        } finally {
           	release();
        }
    }
    
    protected void release() {
        synchronized (scanLock) {
	        clearCaches();
	        iterator = null;
	        includes = tmpNullIncludes ? null : includes;
	        excludes = tmpNullExcludes ? null : excludes;
	    	complete = true;
	        scanning = false;
	        scanLock.notifyAll();
        }
    }
    
    /**
     * This routine is actually checking all the include patterns in
     * order to avoid scanning everything under base dir.
     * 
     * if a pattern says scan it all, though, we scan it all
     */
    private void runScan() {
        Map<String, String> newroots = getNewRoots();
        if (newroots.containsKey("") && basedir != null) {//$NON-NLS-1$
            // we are going to scan everything anyway
            scandirWrap(basedir, "");//$NON-NLS-1$
        } else {
            // only scan directories that can include matched files or
            // directories
            File canonBase = null;
            if (basedir != null) {
                try {
                    canonBase = basedir.getCanonicalFile();
                } catch (IOException ex) {
                    throw new BuildException(ex);
                }
            }

            Iterator it = newroots.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                handleOneEntry(entry, canonBase);
            }
        }
    }

    protected void handleOneEntry(Map.Entry entry, File canonBase) {
        String[] currentelement_ = new String[] { (String) entry.getKey() };
        if (basedir == null && !FileUtils.isAbsolutePath(currentelement_[0])) {
            return;
        }
        String originalpattern = (String) entry.getValue();
        File[] myfile_ = new File[] { getChild(basedir, currentelement_[0])};
        perfectOneEntry(canonBase, myfile_, currentelement_);
        File myfile = myfile_[0];
        String currentelement = currentelement_[0];

        if (myfile != null && myfile.exists()) {
            if (myfile.isDirectory()) {
                if (isIncluded(currentelement)
                    && currentelement.length() > 0) {
                    accountForIncludedDir(currentelement, myfile);
                }  else {
                    if (currentelement.length() > 0) {
                        if (currentelement.charAt(currentelement
                                                  .length() - 1)
                            != File.separatorChar) {
                            currentelement =
                                currentelement + File.separatorChar;
                        }
                    }
                    scandirWrap(myfile, currentelement);
                }
            } else {
                boolean included = isCaseSensitive()
                    ? originalpattern.equals(currentelement)
                    : originalpattern.equalsIgnoreCase(currentelement);
                if (included) {
                    accountForIncludedFile(currentelement, myfile);
                }
            }
        }
    }
    
    protected void perfectOneEntry(File canonBase, File[] myfile_, String[] currentelement_) {
    	File myfile = myfile_[0];
    	String currentelement = currentelement_[0];
    	
        if (myfile.exists()) {
            // may be on a case insensitive file system.  We want
            // the results to show what's really on the disk, so
            // we need to double check.
            try {
                String path = (basedir == null)
                    ? myfile.getCanonicalPath()
                    : FILE_UTILS.removeLeadingPath(canonBase,
                    myfile.getCanonicalFile());
                if (!path.equals(currentelement) || ON_VMS) {
                    myfile = findFile(basedir, currentelement, true);
                    if (myfile != null && basedir != null) {
                        currentelement = FILE_UTILS.removeLeadingPath(
                            basedir, myfile);
                    }
                }
            } catch (IOException ex) {
                throw new BuildException(ex);
            }
        }
        if ((myfile == null || !myfile.exists()) && !isCaseSensitive()) {
            File f = findFile(basedir, currentelement, false);
            if (f != null && f.exists()) {
                // adapt currentelement to the case we've
                // actually found
                currentelement = (basedir == null)
                    ? f.getAbsolutePath()
                    : FILE_UTILS.removeLeadingPath(basedir, f);
                myfile = f;
            }
        }
        
        myfile_[0] = myfile;
        currentelement_[0] = currentelement;
    }
    
    protected Map<String, String> getNewRoots() {
        Map<String, String> newroots = new HashMap<String, String>();
        // put in the newroots map the include patterns without
        // wildcard tokens
        for (int i = 0; i < includes.length; i++) {
            if (FileUtils.isAbsolutePath(includes[i])) {
                //skip abs. paths not under basedir, if set:
                if (basedir != null
                    && !SelectorUtils.matchPatternStart(includes[i],
                    basedir.getAbsolutePath(), isCaseSensitive())) {
                    continue;
                }
            } else if (basedir == null) {
                //skip non-abs. paths if basedir == null:
                continue;
            }
            newroots.put(SelectorUtils.rtrimWildcardTokens(
                includes[i]), includes[i]);
        }
        return newroots;
    }
    
    /**
     * Clear the result caches for a scan.
     */
    protected synchronized void clearResults() {
        filesIncluded    = new Vector<String>();
        filesNotIncluded = new Vector<String>();
        filesExcluded    = new Vector<String>();
        filesDeselected  = new Vector<String>();
        dirsIncluded     = new Vector<String>();
        dirsNotIncluded  = new Vector<String>();
        dirsExcluded     = new Vector<String>();
        dirsDeselected   = new Vector<String>();
        everythingIncluded = (basedir != null);
        scannedDirs.clear();
    }

    
    protected void scandirWrap(File dir, String vpath) {
    	if( iterator == null )
    		scandirImpl(dir,vpath);
    	else
    		iterator.addFileToScanList(dir, vpath);
    }
    
    /**
     * Scan the given directory for files and directories. Found files and
     * directories are placed in their respective collections, based on the
     * matching of includes, excludes, and the selectors.  When a directory
     * is found, it is scanned recursively.
     *
     * @param dir   The directory to scan. Must not be <code>null</code>.
     * @param vpath The path relative to the base directory (needed to
     *              prevent problems with an absolute path when using
     *              dir). Must not be <code>null</code>.
     * @param fast  Whether or not this call is part of a fast scan.
     *
     * @see #filesIncluded
     * @see #filesNotIncluded
     * @see #filesExcluded
     * @see #dirsIncluded
     * @see #dirsNotIncluded
     * @see #dirsExcluded
     * @see #slowScan
     */
    protected void scandirImpl(File dir, String vpath) {
        if (dir == null) {
            throw new BuildException("dir must not be null.");//$NON-NLS-1$
        } else if (!dir.exists()) {
            throw new BuildException(dir + " doesn't exist.");//$NON-NLS-1$
        } else if (!dir.isDirectory()) {
            throw new BuildException(dir + " is not a directory.");//$NON-NLS-1$
        }
        // avoid double scanning of directories, can only happen in fast mode
        if (hasBeenScanned(vpath)) {
            return;
        }

        // LINE MODIFIED FOR JBOSS TOOLS;  was  dir.list();
        File[] newfiles = list(dir);

        if (newfiles == null) {
            /*
             * two reasons are mentioned in the API docs for File.list
             * (1) dir is not a directory. This is impossible as
             *     we wouldn't get here in this case.
             * (2) an IO error occurred (why doesn't it throw an exception
             *     then???)
             */
            throw new BuildException("IO error scanning directory '"//$NON-NLS-1$
                                     + dir.getAbsolutePath() + "'");//$NON-NLS-1$
        }
        for (int i = 0; i < newfiles.length; i++) {
            String name = vpath + getName(newfiles[i]);
            File file = newfiles[i];
            if (file.isDirectory()) {
                if (isIncluded(name)) {
                    accountForIncludedDir(name, file);
                } else {
                    everythingIncluded = false;
                    dirsNotIncluded.addElement(name);
                    if (couldHoldIncluded(name)) {
                        scandirWrap(file, name + File.separator);
                    }
                }
            } else if (file.isFile()) {
                if (isIncluded(name)) {
                    accountForIncludedFile(name, file);
                } else {
                    everythingIncluded = false;
                    filesNotIncluded.addElement(name);
                }
            }
        }
    }

    protected String getName(File file) {
    	return file.getName();
    }

    /**
     * Process included file.
     * @param name  path of the file relative to the directory of the FileSet.
     * @param file  included File.
     */
    private void accountForIncludedFile(String name, File file) {
        processIncluded(name, file, filesIncluded, filesExcluded, filesDeselected);
    }

    /**
     * Process included directory.
     * @param name path of the directory relative to the directory of
     *             the FileSet.
     * @param file directory as File.
     * @param fast whether to perform fast scans.
     */
    private void accountForIncludedDir(String name, File file) {
        processIncluded(name, file, dirsIncluded, dirsExcluded, dirsDeselected);
        if (couldHoldIncluded(name) && !contentsExcluded(name)) {
            scandirWrap(file, name + File.separator);
        }
    }

    private void processIncluded(String name, File file, Vector<String> inc, Vector<String> exc, Vector<String> des) {

        if (inc.contains(name) || exc.contains(name) || des.contains(name)) { return; }

        boolean included = false;
        if (isExcluded(name)) {
            exc.add(name);
            postExclude(file, name);
        } else if (isSelected(name, file)) {
            included = true;
            inc.add(name);
            postInclude(file, name);
        } else {
            des.add(name);
        }
        everythingIncluded &= included;
    }

    protected void postInclude(File f, String name) {
    	if( iterator != null ) {
    		iterator.addMatch(f, name);
    	}
    }

    protected void postExclude(File f, String name) {
    	// do nothing
    }
    /**
     * Test whether or not a name matches against at least one include
     * pattern.
     *
     * @param name The name to match. Must not be <code>null</code>.
     * @return <code>true</code> when the name matches against at least one
     *         include pattern, or <code>false</code> otherwise.
     */
    protected boolean isIncluded(String name) {
        ensureNonPatternSetsReady();

        if (isCaseSensitive()
            ? includeNonPatterns.contains(name)
            : includeNonPatterns.contains(name.toUpperCase())) {
            return true;
        }
        for (int i = 0; i < includePatterns.length; i++) {
            if (matchPath(includePatterns[i], name, isCaseSensitive())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test whether or not a name matches the start of at least one include
     * pattern.
     *
     * @param name The name to match. Must not be <code>null</code>.
     * @return <code>true</code> when the name matches against the start of at
     *         least one include pattern, or <code>false</code> otherwise.
     */
    protected boolean couldHoldIncluded(String name) {
        for (int i = 0; i < includes.length; i++) {
            if (matchPatternStart(includes[i], name, isCaseSensitive())
                && isMorePowerfulThanExcludes(name, includes[i])
                && isDeeper(includes[i], name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verify that a pattern specifies files deeper
     * than the level of the specified file.
     * @param pattern the pattern to check.
     * @param name the name to check.
     * @return whether the pattern is deeper than the name.
     * @since Ant 1.6.3
     */
    private boolean isDeeper(String pattern, String name) {
        Vector p = SelectorUtils.tokenizePath(pattern);
        Vector n = SelectorUtils.tokenizePath(name);
        return p.contains("**") || p.size() > n.size();//$NON-NLS-1$
    }

    /**
     *  Find out whether one particular include pattern is more powerful
     *  than all the excludes.
     *  Note:  the power comparison is based on the length of the include pattern
     *  and of the exclude patterns without the wildcards.
     *  Ideally the comparison should be done based on the depth
     *  of the match; that is to say how many file separators have been matched
     *  before the first ** or the end of the pattern.
     *
     *  IMPORTANT : this function should return false "with care".
     *
     *  @param name the relative path to test.
     *  @param includepattern one include pattern.
     *  @return true if there is no exclude pattern more powerful than this include pattern.
     *  @since Ant 1.6
     */
    private boolean isMorePowerfulThanExcludes(String name, String includepattern) {
        String soughtexclude = name + File.separator + "**";//$NON-NLS-1$
        for (int counter = 0; counter < excludes.length; counter++) {
            if (excludes[counter].equals(soughtexclude))  {
                return false;
            }
        }
        return true;
    }

    /**
     * Test whether all contents of the specified directory must be excluded.
     * @param name the directory name to check.
     * @return whether all the specified directory's contents are excluded.
     */
    private boolean contentsExcluded(String name) {
        name = (name.endsWith(File.separator)) ? name : name + File.separator;
        for (int i = 0; i < excludes.length; i++) {
            String e = excludes[i];
            if (e.endsWith("**") && SelectorUtils.matchPath(//$NON-NLS-1$
                e.substring(0, e.length() - 2), name, isCaseSensitive())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test whether or not a name matches against at least one exclude
     * pattern.
     *
     * @param name The name to match. Must not be <code>null</code>.
     * @return <code>true</code> when the name matches against at least one
     *         exclude pattern, or <code>false</code> otherwise.
     */
    protected boolean isExcluded(String name) {
        ensureNonPatternSetsReady();

        if (isCaseSensitive()
            ? excludeNonPatterns.contains(name)
            : excludeNonPatterns.contains(name.toUpperCase())) {
            return true;
        }
        for (int i = 0; i < excludePatterns.length; i++) {
            if (matchPath(excludePatterns[i], name, isCaseSensitive())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test whether a file should be selected.
     *
     * @param name the filename to check for selecting.
     * @param file the java.io.File object for this filename.
     * @return <code>false</code> when the selectors says that the file
     *         should not be selected, <code>true</code> otherwise.
     */
    protected boolean isSelected(String name, File file) {
        if (selectors != null) {
            for (int i = 0; i < selectors.length; i++) {
                if (!selectors[i].isSelected(basedir, name, file)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Return the names of the files which matched at least one of the
     * include patterns and none of the exclude patterns.
     * The names are relative to the base directory.
     *
     * @return the names of the files which matched at least one of the
     *         include patterns and none of the exclude patterns.
     */
    public synchronized String[] getIncludedFiles() {
        if (filesIncluded == null) {
            throw new IllegalStateException("Must call scan() first");//$NON-NLS-1$
        }
        String[] files = new String[filesIncluded.size()];
        filesIncluded.copyInto(files);
        Arrays.sort(files);
        return files;
    }

    /**
     * Return the count of included files.
     * @return <code>int</code>.
     * @since Ant 1.6.3
     */
    public synchronized int getIncludedFilesCount() {
        if (filesIncluded == null) {
            throw new IllegalStateException("Must call scan() first");//$NON-NLS-1$
        }
        return filesIncluded.size();
    }

    /**
     * Return the names of the directories which matched at least one of the
     * include patterns and none of the exclude patterns.
     * The names are relative to the base directory.
     *
     * @return the names of the directories which matched at least one of the
     * include patterns and none of the exclude patterns.
     */
    public synchronized String[] getIncludedDirectories() {
        if (dirsIncluded == null) {
            throw new IllegalStateException("Must call scan() first");//$NON-NLS-1$
        }
        String[] directories = new String[dirsIncluded.size()];
        dirsIncluded.copyInto(directories);
        Arrays.sort(directories);
        return directories;
    }

    /**
     * Return the count of included directories.
     * @return <code>int</code>.
     * @since Ant 1.6.3
     */
    public synchronized int getIncludedDirsCount() {
        if (dirsIncluded == null) {
            throw new IllegalStateException("Must call scan() first");//$NON-NLS-1$
        }
        return dirsIncluded.size();
    }

    /**
     * Add default exclusions to the current exclusions set.
     */
    public synchronized void addDefaultExcludes() {
        int excludesLength = excludes == null ? 0 : excludes.length;
        String[] newExcludes;
        newExcludes = new String[excludesLength + defaultExcludes.size()];
        if (excludesLength > 0) {
            System.arraycopy(excludes, 0, newExcludes, 0, excludesLength);
        }
        String[] defaultExcludesTemp = getDefaultExcludes();
        for (int i = 0; i < defaultExcludesTemp.length; i++) {
            newExcludes[i + excludesLength] =
                defaultExcludesTemp[i].replace('/', File.separatorChar)
                .replace('\\', File.separatorChar);
        }
        excludes = newExcludes;
    }

    /**
     * Get the named resource.
     * @param name path name of the file relative to the dir attribute.
     *
     * @return the resource with the given name.
     * @since Ant 1.5.2
     */
    public synchronized Resource getResource(String name) {
        return new FileResource(basedir, name);
    }

    /**
     * Return a cached result of list performed on file, if
     * available.  Invokes the method and caches the result otherwise.
     *
     * @param file File (dir) to list.
     * @since Ant 1.6
     */
    private File[] list(File file) {
        File[] files = fileListMap.get(file);
        if (files == null) {
            files = list2(file);
            if (files != null) {
                fileListMap.put(file, files);
            }
        }
        return files;
    }

    // Made protected to be over-ridden
    protected File[] list2(File file) {
    	return file.listFiles();
    }

    /**
     * From <code>base</code> traverse the filesystem in order to find
     * a file that matches the given name.
     *
     * @param base base File (dir).
     * @param path file path.
     * @param cs whether to scan case-sensitively.
     * @return File object that points to the file in question or null.
     *
     * @since Ant 1.6.3
     */
    private File findFile(File base, String path, boolean cs) {
        if (FileUtils.isAbsolutePath(path)) {
            if (base == null) {
                String[] s = FILE_UTILS.dissect(path);
                base = new File(s[0]);
                path = s[1];
            } else {
                File f = FILE_UTILS.normalize(path);
                String s = FILE_UTILS.removeLeadingPath(base, f);
                if (s.equals(f.getAbsolutePath())) {
                    //removing base from path yields no change; path not child of base
                    return null;
                }
                path = s;
            }
        }
        return findFile(base, SelectorUtils.tokenizePath(path), cs);
    }

    /**
     * From <code>base</code> traverse the filesystem in order to find
     * a file that matches the given stack of names.
     *
     * @param base base File (dir).
     * @param pathElements Vector of path elements (dirs...file).
     * @param cs whether to scan case-sensitively.
     * @return File object that points to the file in question or null.
     *
     * @since Ant 1.6.3
     */
    private File findFile(File base, Vector pathElements, boolean cs) {
        if (pathElements.size() == 0) {
            return base;
        }
        String current = (String) pathElements.remove(0);
        if (base == null) {
            return findFile(new File(current), pathElements, cs);
        }
        if (!base.isDirectory()) {
            return null;
        }
        File[] files = list(base);
        if (files == null) {
            throw new BuildException("IO error scanning directory "//$NON-NLS-1$
                                     + base.getAbsolutePath());
        }
        boolean[] matchCase = cs ? CS_SCAN_ONLY : CS_THEN_NON_CS;
        for (int i = 0; i < matchCase.length; i++) {
            for (int j = 0; j < files.length; j++) {
                if (matchCase[i] ? files[j].getName().equals(current)
                                 : files[j].getName().equalsIgnoreCase(current)) {
                    return findFile(files[j], pathElements, cs);
                }
            }
        }
        return null;
    }

    /**
     * Do we have to traverse a symlink when trying to reach path from
     * basedir?
     * @param base base File (dir).
     * @param path file path.
     * @since Ant 1.6
     */
    private boolean isSymlink(File base, String path) {
        return isSymlink(base, SelectorUtils.tokenizePath(path));
    }

    /**
     * Do we have to traverse a symlink when trying to reach path from
     * basedir?
     * @param base base File (dir).
     * @param pathElements Vector of path elements (dirs...file).
     * @since Ant 1.6
     */
    private boolean isSymlink(File base, Vector pathElements) {
        if (pathElements.size() > 0) {
            String current = (String) pathElements.remove(0);
            try {
                return FILE_UTILS.isSymbolicLink(base, current)
                    || isSymlink(new File(base, current), pathElements);
            } catch (IOException ioe) {
                String msg = "IOException caught while checking "//$NON-NLS-1$
                    + "for links, couldn't get canonical path!";//$NON-NLS-1$
                // will be caught and redirected to Ant's logging system
                System.err.println(msg);
            }
        }
        return false;
    }

    /**
     * Has the directory with the given path relative to the base
     * directory already been scanned?
     *
     * <p>Registers the given directory as scanned as a side effect.</p>
     *
     * @since Ant 1.6
     */
    private boolean hasBeenScanned(String vpath) {
        return !scannedDirs.add(vpath);
    }

    /**
     * This method is of interest for testing purposes.  The returned
     * Set is live and should not be modified.
     * @return the Set of relative directory names that have been scanned.
     */
    /* package-private */ Set<String> getScannedDirs() {
        return scannedDirs;
    }

    /**
     * Clear internal caches.
     *
     * @since Ant 1.6
     */
    private synchronized void clearCaches() {
        fileListMap.clear();
        includeNonPatterns.clear();
        excludeNonPatterns.clear();
        includePatterns = null;
        excludePatterns = null;
        areNonPatternSetsReady = false;
    }

    /**
     * Ensure that the in|exclude &quot;patterns&quot;
     * have been properly divided up.
     *
     * @since Ant 1.6.3
     */
    private synchronized void ensureNonPatternSetsReady() {
        if (!areNonPatternSetsReady) {
            includePatterns = fillNonPatternSet(includeNonPatterns, includes);
            excludePatterns = fillNonPatternSet(excludeNonPatterns, excludes);
            areNonPatternSetsReady = true;
        }
    }

    /**
     * Add all patterns that are not real patterns (do not contain
     * wildcards) to the set and returns the real patterns.
     *
     * @param set Set to populate.
     * @param patterns String[] of patterns.
     * @since Ant 1.6.3
     */
    private String[] fillNonPatternSet(Set<String> set, String[] patterns) {
        ArrayList<String> al = new ArrayList<String>(patterns.length);
        for (int i = 0; i < patterns.length; i++) {
            if (!SelectorUtils.hasWildcards(patterns[i])) {
                set.add(isCaseSensitive() ? patterns[i]
                    : patterns[i].toUpperCase());
            } else {
                al.add(patterns[i]);
            }
        }
        return set.size() == 0 ? patterns
            : al.toArray(new String[al.size()]);
    }
    
    protected File getChild(File file, String element) {
    	return new File(file, element);
    }
    
    public Iterator<File> iterator() {
    	iterator = new DirectoryScannerIterator();
    	return iterator;
    }
    
    public class DirectoryScannerIterator implements Iterator<File> {
    	protected ArrayList<DSPair> matches;
    	protected ArrayList<DSPair> toScan;
    	protected DirectoryScanner scanner;
    	protected int pointer;
    	public DirectoryScannerIterator() {
    		this.matches = new ArrayList<DSPair>();
    		this.toScan = new ArrayList<DSPair>();
    		pointer = 0;
    		iterator = this;
            scanPrepare();
            runScan();
    	}
    	
    	protected void addFileToScanList(File file, String vpath) {
    		toScan.add(0, new DSPair(file, vpath));
    	}
    	
    	protected void addMatch(File file, String vpath) {
    		matches.add(pointer, new DSPair(file, vpath));
    	}
    	
		public boolean hasNext() {
			if( pointer <= matches.size() -1 ) return true;
			if( toScan.isEmpty()) return false;
			
			while( !toScan.isEmpty() && pointer == matches.size()) {
				DSPair pair = toScan.remove(0);
				scandirImpl(pair.file, pair.vpath);
			}
			
			boolean hasNext =  pointer <= matches.size() -1 ||
				(pointer == matches.size() && !toScan.isEmpty());
			if( !hasNext )
				release();
			return hasNext;
		}

		public File next() {
			if( pointer <= (matches.size()-1))
				return matches.get(pointer++).file;
			return null;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
    	
    }
    
    protected class DSPair {
    	private File file;
    	private String vpath;
    	public DSPair(File file, String vpath) {
    		this.file = file;
    		this.vpath = vpath;
    	}
    }
}
