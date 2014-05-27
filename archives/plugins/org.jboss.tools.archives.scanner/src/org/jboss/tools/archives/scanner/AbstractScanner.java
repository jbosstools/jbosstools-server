package org.jboss.tools.archives.scanner;

/*
 * Copyright The Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.File;

import org.jboss.tools.archives.scanner.internal.MatchPatterns;
import org.jboss.tools.archives.scanner.internal.SelectorUtils;

/**
 * Scan a directory tree for files, with specified inclusions and exclusions.
 */
public abstract class AbstractScanner<T> implements Scanner <T> {
    /**
     * Patterns which should be excluded by default, like SCM files
     * <ul>
     * <li>Misc: &#42;&#42;/&#42;~, &#42;&#42;/#&#42;#, &#42;&#42;/.#&#42;, &#42;&#42;/%&#42;%, &#42;&#42;/._&#42; </li>
     * <li>CVS: &#42;&#42;/CVS, &#42;&#42;/CVS/&#42;&#42;, &#42;&#42;/.cvsignore</li>
     * <li>RCS: &#42;&#42;/RCS, &#42;&#42;/RCS/&#42;&#42;</li>
     * <li>SCCS: &#42;&#42;/SCCS, &#42;&#42;/SCCS/&#42;&#42;</li>
     * <li>VSSercer: &#42;&#42;/vssver.scc</li>
     * <li>MKS: &#42;&#42;/project.pj</li>
     * <li>SVN: &#42;&#42;/.svn, &#42;&#42;/.svn/&#42;&#42;</li>
     * <li>GNU: &#42;&#42;/.arch-ids, &#42;&#42;/.arch-ids/&#42;&#42;</li>
     * <li>Bazaar: &#42;&#42;/.bzr, &#42;&#42;/.bzr/&#42;&#42;</li>
     * <li>SurroundSCM: &#42;&#42;/.MySCMServerInfo</li>
     * <li>Mac: &#42;&#42;/.DS_Store</li>
     * <li>Serena Dimension: &#42;&#42;/.metadata, &#42;&#42;/.metadata/&#42;&#42;</li>
     * <li>Mercurial: &#42;&#42;/.hg, &#42;&#42;/.hg/&#42;&#42;</li>
     * <li>GIT: &#42;&#42;/.git, &#42;&#42;/.gitignore, &#42;&#42;/.gitattributes, &#42;&#42;/.git/&#42;&#42;</li>
     * <li>Bitkeeper: &#42;&#42;/BitKeeper, &#42;&#42;/BitKeeper/&#42;&#42;, &#42;&#42;/ChangeSet, &#42;&#42;/ChangeSet/&#42;&#42;</li>
     * <li>Darcs: &#42;&#42;/_darcs, &#42;&#42;/_darcs/&#42;&#42;, &#42;&#42;/.darcsrepo, &#42;&#42;/.darcsrepo/&#42;&#42;&#42;&#42;/-darcs-backup&#42;, &#42;&#42;/.darcs-temp-mail
     * </ul>
     *
     * @see #addDefaultExcludes()
     */
    public static final String[] DEFAULTEXCLUDES = {
        // Miscellaneous typical temporary files
        "**/*~", "**/#*#", "**/.#*", "**/%*%", "**/._*", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

        // CVS
        "**/CVS", "**/CVS/**", "**/.cvsignore",//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 

        // RCS
        "**/RCS", "**/RCS/**",//$NON-NLS-1$ //$NON-NLS-2$ 

        // SCCS
        "**/SCCS", "**/SCCS/**",//$NON-NLS-1$ //$NON-NLS-2$ 

        // Visual SourceSafe
        "**/vssver.scc",//$NON-NLS-1$ 

        // MKS
        "**/project.pj",//$NON-NLS-1$ 

        // Subversion
        "**/.svn", "**/.svn/**",//$NON-NLS-1$ //$NON-NLS-2$ 

        // Arch
        "**/.arch-ids", "**/.arch-ids/**",//$NON-NLS-1$ //$NON-NLS-2$ 

        //Bazaar
        "**/.bzr", "**/.bzr/**",//$NON-NLS-1$ //$NON-NLS-2$ 

        //SurroundSCM
        "**/.MySCMServerInfo",//$NON-NLS-1$ 

        // Mac
        "**/.DS_Store",//$NON-NLS-1$ 

        // Serena Dimensions Version 10
        "**/.metadata", "**/.metadata/**",//$NON-NLS-1$ //$NON-NLS-2$ 

        // Mercurial
        "**/.hg", "**/.hg/**",//$NON-NLS-1$ //$NON-NLS-2$ 

        // git
        "**/.git", "**/.gitignore", "**/.gitattributes", "**/.git/**",//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 

        // BitKeeper
        "**/BitKeeper", "**/BitKeeper/**", "**/ChangeSet", "**/ChangeSet/**",//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 

        // darcs
        "**/_darcs", "**/_darcs/**", "**/.darcsrepo", "**/.darcsrepo/**", "**/-darcs-backup*", "**/.darcs-temp-mail" };//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

    /**
     * The patterns for the files to be included.
     */
    protected String[] includes;

    private MatchPatterns includesPatterns;

    /**
     * The patterns for the files to be excluded.
     */
    protected String[] excludes;

    private MatchPatterns excludesPatterns;

    /**
     * Whether or not the file system should be treated as a case sensitive
     * one.
     */
    protected boolean isCaseSensitive = true;

    /**
     * Sets whether or not the file system should be regarded as case sensitive.
     *
     * @param isCaseSensitive whether or not the file system should be
     *                        regarded as a case sensitive one
     */
    public void setCaseSensitive( boolean isCaseSensitive )
    {
        this.isCaseSensitive = isCaseSensitive;
    }

    /**
     * Sets the list of include patterns to use. All '/' and '\' characters
     * are replaced by <code>File.separatorChar</code>, so the separator used
     * need not match <code>File.separatorChar</code>.
     * <p/>
     * When a pattern ends with a '/' or '\', "**" is appended.
     *
     * @param includes A list of include patterns.
     *                 May be <code>null</code>, indicating that all files
     *                 should be included. If a non-<code>null</code>
     *                 list is given, all elements must be
     *                 non-<code>null</code>.
     */
    public void setIncludes( String[] includes )
    {
        if ( includes == null || includes.length == 0)
        {
            this.includes = null;
        }
        else
        {
            this.includes = new String[includes.length];
            for ( int i = 0; i < includes.length; i++ )
            {
                this.includes[i] = normalizePattern( includes[i] );
            }
        }
    }

    /**
     * Sets the list of exclude patterns to use. All '/' and '\' characters
     * are replaced by <code>File.separatorChar</code>, so the separator used
     * need not match <code>File.separatorChar</code>.
     * <p/>
     * When a pattern ends with a '/' or '\', "**" is appended.
     *
     * @param excludes A list of exclude patterns.
     *                 May be <code>null</code>, indicating that no files
     *                 should be excluded. If a non-<code>null</code> list is
     *                 given, all elements must be non-<code>null</code>.
     */
    public void setExcludes( String[] excludes )
    {
        if ( excludes == null )
        {
            this.excludes = null;
        }
        else
        {
            this.excludes = new String[excludes.length];
            for ( int i = 0; i < excludes.length; i++ )
            {
                this.excludes[i] = normalizePattern( excludes[i] );
            }
        }
    }

    /**
     * Normalizes the pattern, e.g. converts forward and backward slashes to the platform-specific file separator.
     *
     * @param pattern The pattern to normalize, must not be <code>null</code>.
     * @return The normalized pattern, never <code>null</code>.
     */
    private String normalizePattern( String pattern )
    {
        pattern = pattern.trim();

        if ( pattern.startsWith( SelectorUtils.REGEX_HANDLER_PREFIX ) )
        {
            if ( File.separatorChar == '\\' )
            {
                pattern = replace( pattern, "/", "\\\\" ); //$NON-NLS-1$ //$NON-NLS-2$
            }
            else
            {
                pattern = replace( pattern, "\\\\", "/" ); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        else
        {
            pattern = pattern.replace( File.separatorChar == '/' ? '\\' : '/', File.separatorChar );

            if ( pattern.endsWith( File.separator ) )
            {
                pattern += "**"; //$NON-NLS-1$
            }
        }

        return pattern;
    }

    /**
     * <p>Replace a String with another String inside a larger String,
     * for the first <code>max</code> values of the search String.</p>
     *
     * <p>A <code>null</code> reference passed to this method is a no-op.</p>
     *
     * @param text text to search and replace in
     * @param repl String to search for
     * @param with String to replace with
     * @param max maximum number of values to replace, or <code>-1</code> if no maximum
     * @return the text with any replacements processed
     */
    private static String replace( String text, String repl, String with) {
        if ( ( text == null ) || ( repl == null ) || ( with == null ) || ( repl.length() == 0 ) ) {
            return text;
        }

        StringBuilder buf = new StringBuilder( text.length() );
        int start = 0, end;
        while ( ( end = text.indexOf( repl, start ) ) != -1 ) {
            buf.append( text, start, end ).append( with );
            start = end + repl.length();
        }
        buf.append( text, start, text.length());
        return buf.toString();
    }
    
    /**
     * Tests whether or not a name matches against at least one include
     * pattern.
     *
     * @param name The name to match. Must not be <code>null</code>.
     * @return <code>true</code> when the name matches against at least one
     *         include pattern, or <code>false</code> otherwise.
     */
    protected boolean isIncluded( String name )
    {
        return includesPatterns.matches( name, isCaseSensitive );
    }

    protected boolean isIncluded( String name, String[] tokenizedName )
    {
        return includesPatterns.matches( name, tokenizedName, isCaseSensitive );
    }

    /**
     * Tests whether or not a name matches the start of at least one include
     * pattern.
     *
     * @param name The name to match. Must not be <code>null</code>.
     * @return <code>true</code> when the name matches against the start of at
     *         least one include pattern, or <code>false</code> otherwise.
     */
    protected boolean couldHoldIncluded( String name )
    {
        return includesPatterns.matchesPatternStart(name, isCaseSensitive);
    }

    /**
     * Tests whether or not a name matches against at least one exclude
     * pattern.
     *
     * @param name The name to match. Must not be <code>null</code>.
     * @return <code>true</code> when the name matches against at least one
     *         exclude pattern, or <code>false</code> otherwise.
     */
    protected boolean isExcluded( String name )
    {
        return excludesPatterns.matches( name, isCaseSensitive );
    }

    protected boolean isExcluded( String name, String[] tokenizedName )
    {
        return excludesPatterns.matches( name, tokenizedName, isCaseSensitive );
    }

    /**
     * Adds default exclusions to the current exclusions set.
     */
    public void addDefaultExcludes()
    {
        int excludesLength = excludes == null ? 0 : excludes.length;
        String[] newExcludes;
        newExcludes = new String[excludesLength + DEFAULTEXCLUDES.length];
        if ( excludesLength > 0 )
        {
            System.arraycopy( excludes, 0, newExcludes, 0, excludesLength );
        }
        for ( int i = 0; i < DEFAULTEXCLUDES.length; i++ )
        {
            newExcludes[i + excludesLength] = DEFAULTEXCLUDES[i].replace( '/', File.separatorChar );
        }
        excludes = newExcludes;
    }

    protected void setupDefaultFilters()
    {
        if ( includes == null )
        {
            // No includes supplied, so set it to 'matches all'
            includes = new String[1];
            includes[0] = "**"; //$NON-NLS-1$
        }
        if ( excludes == null )
        {
            excludes = new String[0];
        }
    }

    protected void setupMatchPatterns()
    {
        includesPatterns = MatchPatterns.from( includes );
        excludesPatterns = MatchPatterns.from( excludes );
    }
}
