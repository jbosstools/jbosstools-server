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
package org.jboss.ide.eclipse.as.core.publishers.patterns;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.apache.tools.ant.util.FileUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.util.ModuleFolder;

/**
 * Class for scanning a directory for files/directories which match certain
 * criteria.
 * <p>
 * These criteria consist of patterns which have been specified.
 * With patterns you can include or exclude files based on their filename.
 * <p>
 * The idea is simple. A given directory is recursively scanned for all files
 * and directories. Each file/directory is matched against a set of selectors,
 * including special support for matching against filenames with include and
 * and exclude patterns. Only files/directories which match at least one
 * pattern of the include pattern list , and don't match
 * any pattern of the exclude pattern list will be placed in the list of files/directories found.
 * <p>
 * When no list of include patterns is supplied, "**" will be used, which
 * means that everything will be matched. When no list of exclude patterns is
 * supplied, an empty list is used, such that nothing will be excluded. 
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
 * @since 2.3
 *
 */

/*
 * Note: This class ideally should be translated, but since
 * it was stolen from ant, I'd rather leave it as close to the ant
 * version as possible.
 * 
 * Started to diverge quite a bit in preparation for 
 */
/**
 * @since 2.3
 */
public class PublishFilterDirectoryScanner {

    /** The base directory to be scanned relative to module root. */
    protected IPath basedir;

    /** The patterns for the files to be included. */
    protected String[] includes;

    /** The patterns for the files to be excluded. */
    protected String[] excludes;

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

    /** The directories which were found and did not match any includes, but must be created because files under it are required */
    protected Vector<String> dirsNotIncludedButRequired;

    /**
     * The directories which matched at least one include and at least one
     * exclude.
     */
    protected Vector<String> dirsExcluded;

    /** Whether or not everything tested so far has been included. */
    protected boolean everythingIncluded = true;

    // CheckStyle:VisibilityModifier ON

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
        
    private IModuleResource[] resources;
    
    /**
     * Sole constructor.
     */
    public PublishFilterDirectoryScanner(IModuleResource[] resources) {
    	this.resources = resources;
    	setBasedir(new Path("/"));  //$NON-NLS-1$
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
    
    public synchronized void setBasedir(IPath basedir) {
        this.basedir = basedir;
    }

    public synchronized IPath getBasedir() {
        return basedir;
    }
    /* end Base dir */


    public synchronized void setIncludes(String includes) {
    	boolean nullInc = includes == null || "".equals(includes); //$NON-NLS-1$
    	setIncludes(nullInc ? null : includes.split(",")); //$NON-NLS-1$
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

    public synchronized void setExcludes(String excludes) {
    	setExcludes(excludes == null ? new String[0] : excludes.split(",")); //$NON-NLS-1$
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
    
    
    protected void scanPrepare() {
        clearResults();
        boolean nullIncludes = includes == null;
        includes = includes == null ? new String[] {"**"} : includes;//$NON-NLS-1$
        excludes = excludes == null ? new String[0] : excludes;

        if (basedir == null ) {
            // if no basedir and no includes, nothing to do:
            if (nullIncludes) {
                return;
            }
        } 
        if (isIncluded("")) {//$NON-NLS-1$
            if (!isExcluded("")) {//$NON-NLS-1$
                dirsIncluded.addElement("");//$NON-NLS-1$
            } else {
                dirsExcluded.addElement("");//$NON-NLS-1$
            }
        } else {
            dirsNotIncluded.addElement("");//$NON-NLS-1$
        }
    }
    
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
        clearCaches();
    }

    private IModuleFolder findBaseDirResource(IPath path) {
    	IModuleResource root = findResource(null, path);
    	if( root != null && !(root instanceof IModuleFolder))
    		throw new UnsupportedOperationException("Base Directory Cannot Be A File"); //$NON-NLS-1$
    	return (IModuleFolder)root;
    }
    
    private IModuleResource findResource(IModuleFolder parent, IPath path) {
    	return findResource(resources, parent, path);
    }
    
    public static IModuleResource findResource(IModuleResource[] allMembers, 
    		IModuleFolder parent, IPath path) {
    	if( path == null || path.segmentCount() == 0 )
    		return null;
    	
    	IModuleResource[] children = parent == null ? allMembers : parent.members();
    	for( int i = 0; i < children.length; i++ ) {
    		if( children[i].getName().equals(path.segment(0))) {
    			// we found our leaf
    			if( path.segmentCount() == 1 )
    				return children[i];
    			// keep digging
    			if( children[i] instanceof IModuleFolder ) 
    				return findResource(allMembers, (IModuleFolder)children[i], path.removeFirstSegments(1));
    			else 
    				throw new IllegalStateException("Requested Path Not Found"); //$NON-NLS-1$
    		}
    	}
    	throw new IllegalStateException("Requested Path Not Found"); //$NON-NLS-1$
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
        	IModuleFolder root = basedir.segmentCount() == 0 ? null : findBaseDirResource(basedir);
            scandirWrap(root, "");//$NON-NLS-1$
        } else {
            // only scan directories that can include matched files or
            // directories
            Set<String> s = newroots.keySet();
            Iterator<String> it = s.iterator();
            String tmp;
            while (it.hasNext()) {
            	tmp = it.next();
                handleOneEntry(tmp, newroots.get(tmp));
            }
        }
    }

    protected void handleOneEntry(String includeRoot, String originalPattern) {
        if (basedir == null && !FileUtils.isAbsolutePath(includeRoot)) {
            return; // a relative includeroot, with no basedir
        }
        
        IModuleResource myfile = findResource(null, new Path(includeRoot));
        String currentelement = includeRoot;

        if (myfile != null) {
            if (myfile instanceof IModuleFolder ) {
                if (isIncluded(currentelement)
                    && currentelement.length() > 0) {
                    accountForIncludedDir(currentelement, (IModuleFolder)myfile);
                }  else {
                    if (currentelement.length() > 0) {
                        if (currentelement.charAt(currentelement
                                                  .length() - 1)
                            != File.separatorChar) {
                            currentelement =
                                currentelement + File.separatorChar;
                        }
                    }
                    scandirWrap((IModuleFolder)myfile, currentelement);
                }
            } else {
                boolean included = originalPattern.equals(currentelement);
                if (included) {
                    accountForIncludedFile(currentelement, (IModuleFile)myfile);
                }
            }
        }
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
                    basedir.makeAbsolute().toString(), true)) {
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
        dirsIncluded     = new Vector<String>();
        dirsNotIncluded  = new Vector<String>();
        dirsNotIncludedButRequired  = new Vector<String>();
        dirsExcluded     = new Vector<String>();
        everythingIncluded = (basedir != null);
        scannedDirs.clear();
    }

    protected void scandirWrap(IModuleFolder dir, String vpath) {
   		scandirImpl(dir,vpath);
    }
    
    /**
     * Scan the given directory for files and directories. Found files and
     * directories are placed in their respective collections, based on the
     * matching of includes, excludes, and the selectors.  When a directory
     * is found, it is scanned recursively.
     *
     * @param dir   The IModuleFolder to scan, or null if root
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
    protected void scandirImpl(IModuleFolder dir, String vpath) {
        // avoid double scanning of directories, can only happen in fast mode
        if (hasBeenScanned(vpath)) {
            return;
        }

        IModuleResource[] newfiles = dir == null ? resources : dir.members();
        
        for (int i = 0; i < newfiles.length; i++) {
            String name = vpath + newfiles[i].getName();
            IModuleResource file = newfiles[i];
            if (file instanceof IModuleFolder ) {
                if (isIncluded(name)) {
                    accountForIncludedDir(name, (IModuleFolder)file);
                } else {
                    everythingIncluded = false;
                    dirsNotIncluded.addElement(name);
                    if (couldHoldIncluded(name)) {
                        scandirWrap((IModuleFolder)file, name + File.separator);
                    }
                }
            } else if (file instanceof IModuleFile ) {
                if (isIncluded(name)) {
                    accountForIncludedFile(name, (IModuleFile)file);
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
    private void accountForIncludedFile(String name, IModuleFile file) {
        processIncluded(name, file, filesIncluded, filesExcluded);
    }

    /**
     * Process included directory.
     * @param name path of the directory relative to the directory of
     *             the FileSet.
     * @param file directory as File.
     * @param fast whether to perform fast scans.
     */
    private void accountForIncludedDir(String name, IModuleFolder file) {
        processIncluded(name, file, dirsIncluded, dirsExcluded);
        if (couldHoldIncluded(name) && !contentsExcluded(name)) {
            scandirWrap(file, name + File.separator);
        }
    }

    private void processIncluded(String name, IModuleResource file, Vector<String> inc, Vector<String> exc) {

        if (inc.contains(name) || exc.contains(name)) 
        	return;

        boolean included = false;
        if (isExcluded(name)) {
            exc.add(name);
            postExclude(file, name);
        } else {
            inc.add(name);
            // Ensure that all parents which are "notIncluded" are added here
            IPath p = new Path(name).removeLastSegments(1);
            while(p.segmentCount() > 0 ) {
            	if( !dirsIncluded.contains(p.toString()) && 
            			!dirsNotIncludedButRequired.contains(p.toString())) {
            		dirsNotIncludedButRequired.add(p.toString());
            	}
            	p = p.removeLastSegments(1);
            }
        }
        everythingIncluded &= included;
    }

    protected void postInclude(IModuleResource f, String name) {
    }

    protected void postExclude(IModuleResource f, String name) {
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

        if (includeNonPatterns.contains(name)){
            return true;
        }
        for (int i = 0; i < includePatterns.length; i++) {
            if (matchPath(includePatterns[i], name, true)) {
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
            if (matchPatternStart(includes[i], name, true)
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
                e.substring(0, e.length() - 2), name, true)) {
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

        if (excludeNonPatterns.contains(name)){
            return true;
        }
        for (int i = 0; i < excludePatterns.length; i++) {
            if (matchPath(excludePatterns[i], name, true)) {
                return true;
            }
        }
        return false;
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
                set.add(patterns[i]);
            } else {
                al.add(patterns[i]);
            }
        }
        return set.size() == 0 ? patterns
            : al.toArray(new String[al.size()]);
    }
    
    /**
     * Returns the platform-dependent resource path.
     */
    private String getResourcePath(IModuleResource resource) {
    	return resource.getModuleRelativePath().append(resource.getName()).makeRelative().toOSString();
    }

    /*
     * Public accessors
     */    
    public boolean isIncludedFile(IModuleFile resource) {
    	return isIncludedFile(getResourcePath(resource));
    }
    public boolean isIncludedDir(IModuleFolder resource) {
    	return isIncludedDir(getResourcePath(resource));
    }
    public boolean isNotIncludedButRequiredMember(IModuleResource resource) {
    	return isNotIncludedButRequired(getResourcePath(resource));
    }
    public boolean isIncludedMember(IModuleResource resource) {
      String path = getResourcePath(resource);
    	return isIncludedFile(path) 
    			|| isIncludedDir(path);
    }
    public boolean isRequiredMember(IModuleResource resource) {
      String path = getResourcePath(resource);
    	return isIncludedFile(path) 
    			|| isIncludedDir(path)
    			|| isNotIncludedButRequired(path);
    }
    public boolean isIncludedFile(String vpath) {
    	return filesIncluded.contains(vpath);
    }
    public boolean isIncludedDir(String vpath) {
    	return dirsIncluded.contains(vpath);
    }
    public boolean isNotIncludedButRequired(String vpath) {
    	return dirsNotIncludedButRequired.contains(vpath);
    }
    public boolean isIncludedMember(String vpath) {
    	return isIncludedFile(vpath) || isIncludedDir(vpath);
    }
    public boolean isRequiredMember(String vpath) {
    	return isIncludedFile(vpath) || isIncludedDir(vpath) || isNotIncludedButRequired(vpath);
    }
    
    private IModuleResource[] cleaned = null;
    public IModuleResource[] getCleanedMembers() {
    	if( cleaned == null )
    		cleaned = getCleanedChildren(null);
    	return cleaned;
    }

    public IModuleResource[] getCleanedChildren(IModuleFolder parent) {
    	IModuleResource[] children = (parent == null ? resources : parent.members());
    	// Depth-first cleaning
    	ArrayList<IModuleResource> cleaned = new ArrayList<IModuleResource>();
    	IModuleResource tmp = null;
    	for( int i = 0; i < children.length; i++ ) {
    		tmp = getCleanedResource(children[i]);
    		if( tmp != null )
    			cleaned.add(tmp);
    	}
    	
    	return cleaned.toArray(new IModuleResource[cleaned.size()]);
    }
    
    private IModuleResource getCleanedResource(IModuleResource r) {
    	if( r instanceof IModuleFile && isIncludedFile((IModuleFile)r)) {
    		return r; // No need to clone or clean since there are no setters
    	}
    	// IF the folder is included, OR, some file below it is included, this folder must be created
    	if( r instanceof IModuleFolder && isRequiredMember(r)) {
    		// Cloning folders
    		IModuleFolder o = (IModuleFolder)r;
    		IContainer c = (IContainer)r.getAdapter(IContainer.class);
    		ModuleFolder mf = new ModuleFolder(c, o.getName(), o.getModuleRelativePath());
    		mf.setMembers(getCleanedChildren(o));
    		return mf;
    	}
    	return null;
    }
}
