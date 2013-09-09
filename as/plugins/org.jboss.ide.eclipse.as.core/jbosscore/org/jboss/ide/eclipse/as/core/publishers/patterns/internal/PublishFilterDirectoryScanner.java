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

package org.jboss.ide.eclipse.as.core.publishers.patterns.internal;

import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.util.ModuleFolder;
import org.jboss.tools.archives.scanner.VirtualDirectoryScanner;

/**
 * Class for scanning the servertools IModuleResource model
 * for IModuleResource elements which match certain
 * criteria.  Please see superclass for detailed information
 * about the syntax of inclusion and exclusion. 
 * <p>
 * Example of usage:
 * <pre>
 *   IModule mod = {some module};
 *   ModuleDelegate del = (ModuleDelegate)mod.loadAdapter(ModuleDelegate.class, null);
 *   PublishFilterDirectoryScanner scanner = new PublishFilterDirectoryScanner(del.members());
 *   String[] includes = {"**\\*.class"};
 *   String[] excludes = {"modules\\*\\**"};
 *   ds.setIncludes(includes);
 *   ds.setExcludes(excludes);
 *   scanner.scan();
 *   IModuleResource[] trimmed = scanner.getCleanedMembers();
 *   
 * </pre>
 * This will return an IModuleResource model which is a clone 
 * of the original minus any resources that should be excluded. 
 * @since 2.3
 *
 */

/**
 * @since 2.3
 */
public class PublishFilterDirectoryScanner extends VirtualDirectoryScanner<ModuleResourceTreeNode> {
    /** The directories which were found and did not match any includes, but must be created because files under it are required */
    protected Vector<String> dirsNotIncludedButRequired;
        
    private IModuleResource[] resources;
    
    /**
     * Sole constructor.
     */
    public PublishFilterDirectoryScanner(IModuleResource[] resources) {
    	this.resources = resources;
    	this.dirsNotIncludedButRequired  = new Vector<String>();
    	IModuleFolder mf = new ModuleFolder(null, "", new Path("/")); //$NON-NLS-1$ //$NON-NLS-2$
    	((ModuleFolder)mf).setMembers(resources);
    	ModuleResourceTreeNode n = new ModuleResourceTreeNode(mf);
    	super.setBasedir(n);
    }
    /* end Base dir */

    @Override
    protected void postInclude(ModuleResourceTreeNode f, String name) {
    	if( iterator != null ) {
    		iterator.addMatch(f, name);
    	}
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

    @Override
    public void scan() throws IllegalStateException {
    	scanPrepare();
        super.scandirWrap( super.basedir, "", true );//$NON-NLS-1$
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
    

    /*
     * Utility methods for traversing the servertools IModuleResource model. 
     */
    
    
    /**
     * Returns the platform-dependent resource path.
     */
    private String getResourcePath(IModuleResource resource) {
    	return resource.getModuleRelativePath().append(resource.getName()).makeRelative().toOSString();
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
