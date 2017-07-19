/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime.modules.manifest;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.ide.eclipse.as.classpath.core.ClasspathCorePlugin;
import org.jboss.ide.eclipse.as.classpath.core.runtime.IRuntimePathProvider;
import org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal.ModuleSlot;
import org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal.ModuleSlotCache;
import org.jboss.ide.eclipse.as.classpath.core.runtime.path.internal.LayeredProductPathProvider;

/**
 * Read a given file and find all
 * module/slot combinations that are being requested
 * for classpath addition
 */
public abstract class AbstractModuleSlotUtil {
	public AbstractModuleSlotUtil() {
		
	}
	

	public static IRuntimePathProvider[] moduleSlotsAsProviders(ModuleSlot[] all) {
		ArrayList<IRuntimePathProvider> ret = new ArrayList<IRuntimePathProvider>();
		for( int i = 0; i < all.length; i++ ) {
			ret.add(new LayeredProductPathProvider(all[i]));
		}
		return (IRuntimePathProvider[]) ret.toArray(new IRuntimePathProvider[ret.size()]);
	}
	
	protected IFile[] locateFiles(IProject p, final String fileName) throws CoreException {
		final ArrayList<IFile> ret = new ArrayList<IFile>();
		if( p != null && p.isAccessible()) {
			p.accept(new IResourceVisitor(){
				public boolean visit(IResource resource) throws CoreException {
					if( resource.getName().toLowerCase().equals(fileName)) {
						if( resource instanceof IFile) {
							ret.add((IFile)resource);
						}
					}
					return true;
				}});
		}
		return (IFile[]) ret.toArray(new IFile[ret.size()]);
	}
	

	protected ModuleSlot getModuleSlot(String ms) {
		if( ms != null ) {
			ms = ms.trim();
			if( ms.contains(" ")) { //$NON-NLS-1$
				ms = ms.substring(0, ms.indexOf(" ")).trim();//$NON-NLS-1$
			}
			int colon = ms.indexOf(":");//$NON-NLS-1$
			String mod = null;
			String slot = null;
			if( colon != -1 ) {
				slot = ms.substring(colon+1);
				mod = ms.substring(0,colon);
			} else {
				mod = ms;
			}
			return new ModuleSlot(mod, slot);
		}
		return null;
	}
	
	protected boolean isCacheOutdated(IFile[] all) {
		for(int i = 0; i < all.length; i++ ) {
			if( ModuleSlotCache.getInstance().isOutdated(all[i])) {
				return true;
			}
		}
		return false;
	}


	public boolean isCacheOutdated(IProject p) {
		IFile[] all = getRelevantFiles(p);
		if( all != null ) {
			return isCacheOutdated(all);
		}
		return true;
	}
	
	protected IFile[] getRelevantFiles(IProject p) {
		if( !isInitialized(p) ) {
			try {
				cacheFiles(p, locateRelevantFiles(p));
			} catch(CoreException ce) {
				return new IFile[0];
			}
		}
		return getCachedFiles(p);
	}
	
	protected boolean isCacheOutdated(IFile f) {
		return ModuleSlotCache.getInstance().isOutdated(f);
	}
	
	public ModuleSlot[] getAllModuleSlots(IProject p, IFile[] files) {
		ArrayList<ModuleSlot> all = new ArrayList<ModuleSlot>();
		if( files == null )
			return new ModuleSlot[0];
		
		for( int i = 0; i < files.length; i++ ) {
			IFile f = files[i];
			if( !isCacheOutdated(f)) {
				ModuleSlot[] prevCached = fetchCachedModuleSlots(f);
				all.addAll(Arrays.asList(prevCached));
			} else {
				// don't use the cache
				// read the file to get a list of modules
				ModuleSlot[] forFile = calculateModuleSlots(files[i]);
				cacheModuleSlots(f, forFile);
				all.addAll(Arrays.asList(forFile));
			}
		}
		return (ModuleSlot[]) all.toArray(new ModuleSlot[all.size()]);
	}
	
	protected void cacheModuleSlots(IFile f, ModuleSlot[] all) {
		ModuleSlotCache.getInstance().cache(f, all);
	}
	
	protected abstract boolean isInitialized(IProject p);
	
	protected ModuleSlot[] fetchCachedModuleSlots(IFile f) {
		return ModuleSlotCache.getInstance().getEntries(f);
	}
	
	protected abstract ModuleSlot[] calculateModuleSlots(IFile f);
	

	protected abstract IFile[] locateRelevantFiles(IProject p) throws CoreException;
	
	protected abstract boolean cacheInitializedProject(IProject p);
	
	protected abstract IFile[] getCachedFiles(IProject p);
	
	protected abstract void cacheFiles(IProject p, IFile[] files);
	
	protected void cacheRelevantFiles(IProject p) throws CoreException {
		cacheFiles(p, locateRelevantFiles(p));
	}
	
	public ModuleSlot[] getAllModuleSlots(IProject p) {
		return getAllModuleSlots(p, getRelevantFiles(p));
	}
	
	@Deprecated  /* Misspelled method */
	public void esureInCache(IFile f) {
		ensureInCache(f);
	}
	
	public void ensureInCache(IFile f) {
		IProject p = f.getProject();
		boolean initialized = cacheInitializedProject(p);
		if( !initialized ) {
			try {
				cacheRelevantFiles(p);
			} catch(CoreException ce) {
				IStatus o = ce.getStatus();
				IStatus stat2 = new Status(o.getSeverity(), o.getPlugin(), o.getMessage(), ce);
				ClasspathCorePlugin.getDefault().getLog().log(stat2);
			}
		}
		IFile[] all = getCachedFiles(p);
		ArrayList<IFile> tmp = all == null ? new ArrayList<IFile>() : new ArrayList<IFile>(Arrays.asList(all));
		if( !tmp.contains(f)) {
			tmp.add(f);
			cacheFiles(p, (IFile[]) tmp.toArray(new IFile[tmp.size()]));
		}
	}
	
}
