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
package org.jboss.ide.eclipse.as.core.publishers.patterns;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.internal.ModuleResourceDelta;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.util.ModuleFolder;
import org.jboss.ide.eclipse.as.core.modules.ResourceModuleResourceUtil;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;

/**
 * This class is a default implementation for two of the three IModulePathFilter
 * methods. They are standard implementations of getFilteredMembers(etc) and 
 * getFilteredDelta(etc).  
 * 
 * This class promises to use only {@link IModulePathFilter#shouldInclude(IModuleResource)}
 * to evaluate whether an item should be included or not. 
 * 
 * IModulePathFilter implementors are not required to use this utility class
 * if they have more efficient ways of acquiring the same information.
 * 
 * This class performs no caching at all, so any client using it
 * should be sure to cache any results that may be requested often.
 * 
 * @since 3.0
 */
public class ModulePathFilterUtility {
	private IModulePathFilter filter;
	public ModulePathFilterUtility(IModulePathFilter filter) {
		this.filter = filter;
	}
	
    public IModuleResource[] getCleanedMembers(IModule module) throws CoreException {
    	return getCleanedChildren(ResourceModuleResourceUtil.getMembers(module));
    }

	
    public IModuleResource[] getCleanedMembers(IModuleResource[] resources) {
    	return getCleanedChildren(resources);
    }
    
    public IModuleResourceDelta[] getCleanedDelta(IModuleResourceDelta[] deltas) {
    	if( deltas == null )
    		return new IModuleResourceDelta[0];
    	
    	ArrayList<IModuleResourceDelta> collector = new ArrayList<IModuleResourceDelta>();
    	for( int i = 0; i < deltas.length; i++ ) {
    		IModuleResourceDelta delta = cleanCloneDelta(deltas[i]);
    		if( delta != null ) {
    			collector.add(delta);
    		}
    	}
    	return (IModuleResourceDelta[]) collector.toArray(new IModuleResourceDelta[collector.size()]);
    }
    
    private IModuleResourceDelta cleanCloneDelta(IModuleResourceDelta delta) {
    	IModuleResource r = delta.getModuleResource();
    	if( filter.shouldInclude(r)) {
    		IModuleResourceDelta[] children = delta.getAffectedChildren();
    		IModuleResourceDelta[] cleanedChildren = getCleanedDelta(children);
    		ModuleResourceDelta d = new ModuleResourceDelta(r, delta.getKind());
    		d.setChildren(cleanedChildren);
    		return d;
    	}
    	return null;
    }
    

    private IModuleResource[] getCleanedChildren(IModuleFolder parent) {
    	return getCleanedChildren(parent.members());
    }
    
    private IModuleResource[] getCleanedChildren(IModuleResource[] children) {
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
    	if( r instanceof IModuleFile && filter.shouldInclude(r)) {
    		return r; // No need to clone or clean since there are no setters
    	}
    	// IF the folder is included, OR, some file below it is included, this folder must be created
    	if( r instanceof IModuleFolder && filter.shouldInclude(r)) {
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
