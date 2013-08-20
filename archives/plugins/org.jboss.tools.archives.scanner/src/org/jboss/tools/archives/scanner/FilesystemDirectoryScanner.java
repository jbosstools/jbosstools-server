/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.archives.scanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.jboss.tools.archives.scanner.internal.TreeNodeFile;

/**
 * A directory scanner optimized for the filesystem
 */
public class FilesystemDirectoryScanner extends VirtualDirectoryScanner<TreeNodeFile> {
    private boolean followSymlinks = true;
    
    public void setBasedir( File basedir ) {
    	super.setBasedir(new TreeNodeFile(basedir));
    }
	
    /**
     * Sets whether or not symbolic links should be followed.
     *
     * @param followSymlinks whether or not symbolic links should be followed
     */
    public void setFollowSymlinks( boolean followSymlinks ) {
        this.followSymlinks = followSymlinks;
    }
    
    @Override
    public ITreeNode[] trimInapplicableEntries(ITreeNode[] newfiles, ITreeNode dir, String vpath) {
        if ( !followSymlinks ) {
        	return trimSymLinkFiles(newfiles, dir, vpath);
        }
        return newfiles;
    }
    private ITreeNode[] trimSymLinkFiles(ITreeNode[] newfiles, ITreeNode dir, String vpath) {
        ArrayList<ITreeNode> noLinks = new ArrayList<ITreeNode>();
        for ( ITreeNode newfile : newfiles ) {
            try {
            	// Mark which entries are ignored bc they are symlinks and we aren't following them
                if ( isSymbolicLink( dir, newfile ) ) {
                    String name = vpath + newfile;
                    ITreeNode file = newfile;
                    if ( isDirectory(file)) {
                        dirsExcluded.addElement( name );
                    } else {
                        filesExcluded.addElement( name );
                    }
                } else {
                	// This is a legitimate file we should track
                    noLinks.add( newfile );
                }
            }
            catch ( IOException ioe )
            {
                String msg = "IOException caught while checking " + "for links, couldn't get cannonical path!"; //$NON-NLS-1$ //$NON-NLS-2$
                // will be caught and redirected to Ant's logging system
                System.err.println( msg );
                noLinks.add( newfile );
            }
        }
        newfiles = noLinks.toArray(new ITreeNode[noLinks.size()]);
        return newfiles;
    }
    /**
     * Checks whether a given file is a symbolic link.
     * <p/>
     * <p>It doesn't really test for symbolic links but whether the
     * canonical and absolute paths of the file are identical - this
     * may lead to false positives on some platforms.</p>
     *
     * @param parent the parent directory of the file to test
     * @param name   the name of the file to test.
     * @return true if it's a symbolic link
     * @throws java.io.IOException .
     * @since Ant 1.5
     */
    protected boolean isSymbolicLink( ITreeNode parent, ITreeNode name )
        throws IOException {
    	// We won't use this feature to avoid copying more classes from
    	// plexus-utils into jbosstools. 
    	
//        if ( Java7Detector.isJava7() )
//        {
//            return Java7FileUtil.isSymLink( new File( parent, name ) );
//        }
        File resolvedParent = new File( ((File)parent).getCanonicalPath() );
        File toTest = new File( resolvedParent, name.getName() );
        return !toTest.getAbsolutePath().equals( toTest.getCanonicalPath() );
    }
    
}
