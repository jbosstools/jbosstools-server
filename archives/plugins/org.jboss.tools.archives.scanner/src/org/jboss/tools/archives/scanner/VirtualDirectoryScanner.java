/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.codehaus.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Ant" and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact codehaus@codehaus.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.codehaus.org/>.
 */

package org.jboss.tools.archives.scanner;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import org.jboss.tools.archives.scanner.internal.MatchPattern;

/**
 * Class for scanning a directory for any virtual / heirarchical 
 * tree path model which match certain criteria.
 * <p/>
 * These criteria consist of patterns which have been specified.
 * With patterns you can include or exclude files based on their filename and path.
 * <p/>
 * The idea is simple. A given directory is recursively scanned for all node,
 * which may or may not be leaf nodes. Each node is matched against a set of 
 * include and exclude patterns. Only nodes which match at least one
 * pattern of the include pattern list, and don't match
 * any pattern of the exclude pattern list will be placed in the list of files/directories found.
 * <p/>
 * When no list of include patterns is supplied, "**" will be used, which
 * means that everything will be matched. When no list of exclude patterns is
 * supplied, an empty list is used, such that nothing will be excluded. When
 * no selectors are supplied, none are applied.
 * <p/>
 * The filename pattern matching is done as follows:
 * The name to be matched is split up in path segments. A path segment is the
 * name of a directory or file, which is bounded by
 * <code>File.separator</code> ('/' under UNIX, '\' under Windows).
 * For example, "abc/def/ghi/xyz.java" is split up in the segments "abc",
 * "def","ghi" and "xyz.java".
 * The same is done for the pattern against which should be matched.
 * <p/>
 * The segments of the name and the pattern are then matched against each
 * other. When '**' is used for a path segment in the pattern, it matches
 * zero or more path segments of the name.
 * <p/>
 * There is a special case regarding the use of <code>File.separator</code>s
 * at the beginning of the pattern and the string to match:<br>
 * When a pattern starts with a <code>File.separator</code>, the string
 * to match must also start with a <code>File.separator</code>.
 * When a pattern does not start with a <code>File.separator</code>, the
 * string to match may not start with a <code>File.separator</code>.
 * When one of these rules is not obeyed, the string will not
 * match.
 * <p/>
 * When a name path segment is matched against a pattern path segment, the
 * following special characters can be used:<br>
 * '*' matches zero or more characters<br>
 * '?' matches one character.
 * <p/>
 * Examples:
 * <p/>
 * "**\*.class" matches all .class files/dirs in a directory tree.
 * <p/>
 * "test\a??.java" matches all files/dirs which start with an 'a', then two
 * more characters and then ".java", in a directory called test.
 * <p/>
 * "**" matches everything in a directory tree.
 * <p/>
 * "**\test\**\XYZ*" matches all files/dirs which start with "XYZ" and where
 * there is a parent directory called test (e.g. "abc\test\def\ghi\XYZ123").
 * <p/>
 * Case sensitivity may be turned off if necessary. By default, it is
 * turned on.
 * <p/>
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
 *
 * Updates and enhancements:
 *    This class has been updated in two major ways. The first is that 
 *    it is now possible to traverse a virtual file system, or in fact
 *    any tree-structured content with names and paths. The class
 *    has been updated with generics to support this usecase. The second is that
 *    it is also possible to iterate through the results, to maximize
 *    efficiency when only a subset (for example, first 100) of the results 
 *    need to be acquired. 
 *    
 *    Includes and Excludes patterns may also respond to regular expressions, so 
 *    long as the includes statement begins with %regex[ and ends with ]. 
 *    
 *    For example, the includes pattern below would include all files inside
 *    folder a:
 *    		scanner.setIncludes("%regex[a/.*]");
 *
 *     Clients looking to extend this class with their own custom tree model
 *     will need to use a generic that extends ITreeNode. While the default 
 *     implementations for almost all methods should work, clients with uncommon
 *     usecases may wish to override some methods in the scanner, such as
 *         exists(T), isDirectory(ITreeNode), getName(ITreeNode), 
 *         getChild(T, String), listChildren(ITreeNode)
 *         
 *     A major assumption of this code is that any method taking an ITreeNode
 *     reserves the right to cast into T. Similar to how all java.io.File children
 *     are also instances of java.io.File, it is expected your model will follow 
 *     the same way. 
 *     
 *     For example, you may instantiate:
 *        new VirtualDirectoryScanner<MyModelNode> 
 *        
 *     where MyModelNode implements ITreeNode.
 *        
 *     It is expected that the following is also valid:
 *        ITreeNode[] children = listChildren(myModelNode);
 *        T child1 = (T)children[0];
 *        
 *        
 *
 * @author Arnout J. Kuiper
 *         <a href="mailto:ajkuiper@wxs.nl">ajkuiper@wxs.nl</a>
 * @author Magesh Umasankar
 * @author <a href="mailto:bruce@callenish.com">Bruce Atherton</a>
 * @author <a href="mailto:levylambert@tiscali-dsl.de">Antoine Levy-Lambert</a>
 * @author <a href="mailto:rstryker@redhat.com">Robert Stryker</a>
 */

public class VirtualDirectoryScanner<T extends ITreeNode> extends AbstractScanner<T> implements IterableDirectoryScanner<T> {

    /**
     * The base directory to be scanned.
     */
    protected T basedir;

    /**
     * The files which matched at least one include and no excludes
     * and were selected.
     */
    protected Vector<String> filesIncluded;

    /**
     * The files which did not match any includes or selectors.
     */
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

    /**
     * The directories which were found and did not match any includes.
     */
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
     * Whether or not our results were built by a slow scan.
     */
    protected boolean haveSlowResults = false;

    /**
     * Whether or not everything tested so far has been included.
     */
    protected boolean everythingIncluded = true;

    private final String[] tokenizedEmpty = MatchPattern.tokenizePathToString( "", File.separator ); //$NON-NLS-1$
    
    protected IDirectoryScannerIterator<T> iterator = null;

    /**
     * Patterns which should be excluded by default.
     *
     * @see #addDefaultExcludes()
     */
    private static Vector<String> defaultExcludes = new Vector<String>();
    static {
        resetDefaultExcludes();
    }

    /**
     * Sole constructor.
     */
    public VirtualDirectoryScanner()
    {
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

    /**
     * Convenience method for setExcludes(String[] arr);
     * @param s
     */
    public void setExcludes(String s) {
    	super.setExcludes( s == null ? null : new String[]{s});
    }
    
    /**
     * Convenience method for setIncludes(String[] arr);
     * @param s
     */
    public void setIncludes(String s) {
    	super.setIncludes( s == null ? null : new String[]{s});
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

    /**
     * Sets the base directory to be scanned. This is the directory which is
     * scanned recursively.
     *
     * @param basedir The base directory for scanning.
     *                Should not be <code>null</code>.
     */
    public void setBasedir( T basedir )
    {
        this.basedir = basedir;
    }

    /**
     * Returns the base directory to be scanned.
     * This is the directory which is scanned recursively.
     *
     * @return the base directory to be scanned
     */
    public T getBasedir() {
        return basedir;
    }

    /**
     * Returns whether or not the scanner has included all the files or
     * directories it has come across so far.
     *
     * @return <code>true</code> if all files and directories which have
     *         been found so far have been included.
     */
    public boolean isEverythingIncluded() {
        return everythingIncluded;
    }

    /**
     * Scans the base directory for files which match at least one include
     * pattern and don't match any exclude patterns. If there are selectors
     * then the files must pass muster there, as well.
     *
     * @throws IllegalStateException if the base directory was set
     *                               incorrectly (i.e. if it is <code>null</code>, doesn't exist,
     *                               or isn't a directory).
     */
    public void scan() throws IllegalStateException {
    	scanPrepare();
        scandirWrap( basedir, "", true );//$NON-NLS-1$
    }

    /*
     * A JBossTools construct to allow us to separate out the preparation 
     * from the beginning of the actual scanning. Mostly just readability. 
     */
    protected void scanPrepare() throws IllegalStateException {
        if ( basedir == null ) {
            throw new IllegalStateException( "No basedir set" ); //$NON-NLS-1$
        }
        if ( basedirMustExist() && !exists(basedir)) {
            throw new IllegalStateException( "basedir " + basedir + " does not exist" ); //$NON-NLS-1$ //$NON-NLS-2$
        } 
        if ( !isDirectory(basedir) ) {
            throw new IllegalStateException( "basedir " + basedir + " is not a directory" ); //$NON-NLS-1$ //$NON-NLS-2$
        }

        setupDefaultFilters();
        setupMatchPatterns();

        filesIncluded = new Vector<String>();
        filesNotIncluded = new Vector<String>();
        filesExcluded = new Vector<String>();
        filesDeselected = new Vector<String>();
        dirsIncluded = new Vector<String>();
        dirsNotIncluded = new Vector<String>();
        dirsExcluded = new Vector<String>();
        dirsDeselected = new Vector<String>();

        if ( isIncluded( "", tokenizedEmpty ) ) { //$NON-NLS-1$ 
            if ( !isExcluded( "", tokenizedEmpty ) ) { //$NON-NLS-1$ 
                if ( isSelected( "", basedir ) ) { //$NON-NLS-1$
                    dirsIncluded.addElement( "" );//$NON-NLS-1$
                } else {
                    dirsDeselected.addElement( "" );//$NON-NLS-1$
                }
            } else {
                dirsExcluded.addElement( "" );//$NON-NLS-1$
            }
        } else {
            dirsNotIncluded.addElement( "" );//$NON-NLS-1$
        }
    }
    
    /**
     * Top level invocation for a slow scan. A slow scan builds up a full
     * list of excluded/included files/directories, whereas a fast scan
     * will only have full results for included files, as it ignores
     * directories which can't possibly hold any included files/directories.
     * <p/>
     * Returns immediately if a slow scan has already been completed.
     */
    protected void slowScan() {
        if ( haveSlowResults ) {
            return;
        }

        String[] excl = new String[dirsExcluded.size()];
        dirsExcluded.copyInto( excl );

        String[] notIncl = new String[dirsNotIncluded.size()];
        dirsNotIncluded.copyInto( notIncl );

        for ( String anExcl : excl ) {
            if ( !couldHoldIncluded( anExcl ) ) {
                scandir( getChild(basedir, anExcl ), anExcl + File.separator, false );
            }
        }

        for ( String aNotIncl : notIncl ) {
            if ( !couldHoldIncluded( aNotIncl ) ) {
                scandir( getChild( basedir, aNotIncl ), aNotIncl + File.separator, false );
            }
        }

        haveSlowResults = true;
    }
        
    /**
     * Scans the given directory for files and directories. Found files and
     * directories are placed in their respective collections, based on the
     * matching of includes, excludes, and the selectors.  When a directory
     * is found, it is scanned recursively.
     *
     * @param dir   The directory to scan. Must not be <code>null</code>.
     * @param vpath The path relative to the base directory (needed to
     *              prevent problems with an absolute path when using
     *              dir). Must not be <code>null</code>.
     * @param fast  Whether or not this call is part of a fast scan.
     * @see #filesIncluded
     * @see #filesNotIncluded
     * @see #filesExcluded
     * @see #dirsIncluded
     * @see #dirsNotIncluded
     * @see #dirsExcluded
     * @see #slowScan
     */
    protected void scandir( ITreeNode dir, String vpath, boolean fast ) {
    	 // LINE MODIFIED FOR JBOSS TOOLS;  was  dir.list();
    	ITreeNode[] newfiles = listChildren(dir);
        
        if ( newfiles == null )
        {
            /*
             * two reasons are mentioned in the API docs for File.list
             * (1) dir is not a directory. This is impossible as
             *     we wouldn't get here in this case.
             * (2) an IO error occurred (why doesn't it throw an exception
             *     then???)
             */

            /*
            * [jdcasey] (2) is apparently happening to me, as this is killing one of my tests...
            * this is affecting the assembly plugin, fwiw. I will initialize the newfiles array as
            * zero-length for now.
            *
            * NOTE: I can't find the problematic code, as it appears to come from a native method
            * in UnixFileSystem...
            */
            /*
             * [bentmann] A null array will also be returned from list() on NTFS when dir refers to a soft link or
             * junction point whose target is not existent.
             */
            newfiles = new ITreeNode[0];

            // throw new IOException( "IO error scanning directory " + dir.getAbsolutePath() );
        }

        trimInapplicableEntries(newfiles, dir, vpath);

        // Run scanner on files to track (excluding symlinks if we are ignoring symlinks)
        for ( ITreeNode newfile : newfiles ) {
            String name = vpath + getName(newfile);
            String[] tokenizedName =  MatchPattern.tokenizePathToString( name, File.separator );
            ITreeNode file = newfile;
            if ( isDirectory(file) ) {
                if ( isIncluded( name, tokenizedName ) ) {
                	accountForIncludedDir(name, (T)file, fast);
                } else {
                    everythingIncluded = false;
                    dirsNotIncluded.addElement( name );
                    if ( fast && couldHoldIncluded( name ) ) {
                        scandirWrap( (T)file, name + File.separator, fast );
                    }
                }
                
                if ( !fast ) {
                    scandirWrap( (T)file, name + File.separator, fast );
                }
                
            } else { // assumed to be a file   
                if ( isIncluded( name, tokenizedName ) ) {
                    accountForIncludedFile(name, (T)file);
                } else {
                    everythingIncluded = false;
                    filesNotIncluded.addElement( name );
                }
            }
        }
    }

    /**
     * "Selectors are not used in this api, but this code is unmodified
     * from the plexus-utils version. Theoretically, subclasses
     * can override this method to add selectors if they wish."  - jbt
     *
     * @param name the filename to check for selecting
     * @param file the java.io.File object for this filename
     * @return <code>false</code> when the selectors says that the file
     *         should not be selected, <code>true</code> otherwise.
     */
    protected boolean isSelected( String name, ITreeNode file ) {
        return true;
    }

    /**
     * Returns the names of the files which matched at least one of the
     * include patterns and none of the exclude patterns.
     * The names are relative to the base directory.
     *
     * @return the names of the files which matched at least one of the
     *         include patterns and none of the exclude patterns.
     */
    public String[] getIncludedFiles() {
        if (filesIncluded == null) {
            throw new IllegalStateException("Must call scan() first");//$NON-NLS-1$
        }
        String[] files = new String[filesIncluded.size()];
        filesIncluded.copyInto(files);
        Arrays.sort(files);
        return files;
    }

    /**
     * Returns the names of the files which matched none of the include
     * patterns. The names are relative to the base directory. This involves
     * performing a slow scan if one has not already been completed.
     *
     * @return the names of the files which matched none of the include
     *         patterns.
     * @see #slowScan
     */
    public String[] getNotIncludedFiles() {
        slowScan();
        String[] files = new String[filesNotIncluded.size()];
        filesNotIncluded.copyInto( files );
        return files;
    }

    /**
     * Returns the names of the files which matched at least one of the
     * include patterns and at least one of the exclude patterns.
     * The names are relative to the base directory. This involves
     * performing a slow scan if one has not already been completed.
     *
     * @return the names of the files which matched at least one of the
     *         include patterns and at at least one of the exclude patterns.
     * @see #slowScan
     */
    public String[] getExcludedFiles()
    {
        slowScan();
        String[] files = new String[filesExcluded.size()];
        filesExcluded.copyInto( files );
        return files;
    }

    /**
     * <p>Returns the names of the files which were selected out and
     * therefore not ultimately included.</p>
     * <p/>
     * <p>The names are relative to the base directory. This involves
     * performing a slow scan if one has not already been completed.</p>
     *
     * @return the names of the files which were deselected.
     * @see #slowScan
     */
    public String[] getDeselectedFiles()
    {
        slowScan();
        String[] files = new String[filesDeselected.size()];
        filesDeselected.copyInto( files );
        return files;
    }

    /**
     * Returns the names of the directories which matched at least one of the
     * include patterns and none of the exclude patterns.
     * The names are relative to the base directory.
     *
     * @return the names of the directories which matched at least one of the
     *         include patterns and none of the exclude patterns.
     */
    public String[] getIncludedDirectories() {
        if (dirsIncluded == null) {
            throw new IllegalStateException("Must call scan() first");//$NON-NLS-1$
        }
        String[] directories = new String[dirsIncluded.size()];
        dirsIncluded.copyInto(directories);
        Arrays.sort(directories);
        return directories;
    }

    /**
     * Returns the names of the directories which matched none of the include
     * patterns. The names are relative to the base directory. This involves
     * performing a slow scan if one has not already been completed.
     *
     * @return the names of the directories which matched none of the include
     *         patterns.
     * @see #slowScan
     */
    public String[] getNotIncludedDirectories()
    {
        slowScan();
        String[] directories = new String[dirsNotIncluded.size()];
        dirsNotIncluded.copyInto( directories );
        return directories;
    }

    /**
     * Returns the names of the directories which matched at least one of the
     * include patterns and at least one of the exclude patterns.
     * The names are relative to the base directory. This involves
     * performing a slow scan if one has not already been completed.
     *
     * @return the names of the directories which matched at least one of the
     *         include patterns and at least one of the exclude patterns.
     * @see #slowScan
     */
    public String[] getExcludedDirectories() {
        slowScan();
        String[] directories = new String[dirsExcluded.size()];
        dirsExcluded.copyInto( directories );
        return directories;
    }

    /**
     * <p>Returns the names of the directories which were selected out and
     * therefore not ultimately included.</p>
     * <p/>
     * <p>The names are relative to the base directory. This involves
     * performing a slow scan if one has not already been completed.</p>
     *
     * @return the names of the directories which were deselected.
     * @see #slowScan
     */
    public String[] getDeselectedDirectories() {
        slowScan();
        String[] directories = new String[dirsDeselected.size()];
        dirsDeselected.copyInto( directories );
        return directories;
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
     * Return the count of included files.
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
     * Subclasses should override this if they know the model
     * will include entries which should be ignored in some fashion. 
     * One example would be symbolic links, if this was geared for files
     * specifically. 
     * 
     * @param newfiles
     * @param dir
     * @param vpath
     * @return
     */
    public ITreeNode[] trimInapplicableEntries(ITreeNode[] newfiles, ITreeNode dir, String vpath) {
    	return newfiles;
    }
    

    /**
     * This method is a JBT method. 
     * This method is intended to wrap the call to scan a directory, 
     * providing different implementations depending on whether
     * an iterator is present or not. 
     * 
     * @param dir
     * @param vpath
     * @param fast
     */
    protected void scandirWrap(T dir, String vpath, boolean fast) {
    	if( iterator == null )
    		scandir(dir,vpath, fast);
    	else
    		iterator.addElementToScanList(dir, vpath);
    }
    
    
    /**
     * Process included directory.
     * @param name path of the directory relative to the directory of
     *             the FileSet.
     * @param file directory as File.
     * @param fast whether to perform fast scans.
     */
    private void accountForIncludedDir(String name, T file, boolean fast) {
        processIncluded(name, file, dirsIncluded, dirsExcluded, dirsDeselected);
        if (couldHoldIncluded(name)) {
            scandirWrap(file, name + File.separator, fast);
        }
    }
    
    /**
     * Process included file.
     * @param name  path of the file relative to the directory of the FileSet.
     * @param file  included File.
     */
    private void accountForIncludedFile(String name, T file) {
        processIncluded(name, file, filesIncluded, filesExcluded, filesDeselected);
    }
    
    /**
     * Check the inclusion state for the given file, 
     * and add it to the relevent vector. 
     * 
     * Follow up via the postExclude and postInclude methods
     * to allow subclasses a way to handle each situation. 
     * 
     * @param name
     * @param file
     * @param inc
     * @param exc
     * @param des
     */
    protected void processIncluded(String name, T file, Vector<String> inc, Vector<String> exc, Vector<String> des) {
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
    
    /**
     * Alert subclasses as to the ultimate inclusion of the file
     * 
     * @param f
     * @param name
     */
    protected void postInclude(T f, String name) {
    	if( iterator != null ) {
    		iterator.addMatch(f, name);
    	}
    }

    /**
     * Alert subclasses as to the ultimate exclusion of the file
     * 
     * @param f
     * @param name
     */
    protected void postExclude(ITreeNode f, String name) {
    	// do nothing
    }
    

    public Iterator<T> iterator() {
    	iterator = new DirectoryScannerIterator<T>(this);
        scanPrepare();
        scandir( basedir, "", true );//$NON-NLS-1$
    	return iterator;
    }


    /**
     * Get a list of child files from this file. 
     * For default implementations, the response is to simply use the file API, 
     * but if your implementation depends on a different model, 
     * you can override this to provide custom File subclasses with
     * added information
     * 
     * @param file
     * @return
     */
    protected ITreeNode[] listChildren(ITreeNode node) {
    	return node.listChildren();
    }	
    
    /**
     * Get a child file of this file.  
     * For default implementations, the response is to simply use the file API, 
     * but if your implementation depends on a different model, 
     * you can override this to provide custom File subclasses with
     * added information
     * 
     * @param file
     * @return
     */
    protected ITreeNode getChild(T file, String child) {
    	return file.getChild(child);
    }	
    
    /**
     * Get the name of the File  
     * For default implementations, the response is to simply use the file API, 
     * but if your implementation depends on a different model, 
     * you can override this to provide custom File subclasses with
     * added information
     * 
     * @param file
     * @return
     */ 
    protected String getName(ITreeNode node) {
    	return node.getName();
    }

    /**
     * Return whether the given file is a directory or not. 
     * For default implementations, the response is to simply use the file API, 
     * but if your implementation depends on a different model, 
     * you can override this to provide custom File subclasses with
     * added information
     * 
     * 
     * @param file
     * @return
     */ 
    protected boolean isDirectory(ITreeNode file) {
    	return !file.isLeaf();
    }

    /**
     * If we must check if the basedir exists
     * @return
     */
    protected boolean basedirMustExist() {
    	return false;
    }

    /**
     * Check if the base directory exists
     * @return
     */
    protected boolean exists(T node) {
    	return false;
    }

    /*
     * (non-Javadoc)ls
     * @see org.jboss.tools.archives.core.scanner.IterableDirectoryScanner#scanDirectory(java.io.File, java.lang.String)
     */
    public void scanDirectory(T file, String vpath) {
        scandir( file, vpath, true );
	}

    /*
     * (non-Javadoc)
     * @see org.jboss.tools.archives.core.scanner.IterableDirectoryScanner#cleanup()
     */
	public void cleanup() {
	}
}
