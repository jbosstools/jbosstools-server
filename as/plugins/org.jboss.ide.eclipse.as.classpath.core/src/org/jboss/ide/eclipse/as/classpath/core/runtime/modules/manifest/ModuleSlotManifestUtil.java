/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime.modules.manifest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.jboss.ide.eclipse.as.classpath.core.runtime.IRuntimePathProvider;
import org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal.ModuleSlot;
import org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal.ModuleSlotCache;
import org.jboss.ide.eclipse.as.classpath.core.runtime.path.internal.LayeredProductPathProvider;
import org.jboss.ide.eclipse.as.core.util.FileUtil;

/**
 * Read a given manifest.mf file and find all
 * module/slot combinations that are being requested
 * for classpath addition
 */
public class ModuleSlotManifestUtil {
	
	public boolean isCacheOutdated(IProject p) {
		IFile[] all = getManifests(p);
		if( all != null ) {
			return isCacheOutdated(all);
		}
		return true;
	}
	
	public IFile[] getManifests(IProject p) {
		IFile[] manifests = null;
		try {
			manifests = getManifestFiles(p);
			return manifests;
		} catch(CoreException ce) {
			return new IFile[0];
		}
	}
	
	public boolean isCacheOutdated(IFile[] all) {
		for(int i = 0; i < all.length; i++ ) {
			if( ModuleSlotCache.getInstance().isOutdated(all[i])) {
				return true;
			}
		}
		return false;
	}
	
	public ModuleSlot[] getAllModuleSlots(IProject p) {
		ArrayList<ModuleSlot> all = new ArrayList<ModuleSlot>();
		IFile[] manifests = getManifests(p);
		if( manifests == null )
			return new ModuleSlot[0];
		
		for( int i = 0; i < manifests.length; i++ ) {
			IFile f = manifests[i];
			if( !ModuleSlotCache.getInstance().isOutdated(f)) {
				all.addAll(Arrays.asList(ModuleSlotCache.getInstance().getEntries(f)));
			}

			// don't use the cache
			// read the manifests to get a list of modules
			ModuleSlot[] forFile = getSlotsForManifest(manifests[i]);
			all.addAll(Arrays.asList(forFile));
			ModuleSlotCache.getInstance().cache(f, forFile);
		}
		return (ModuleSlot[]) all.toArray(new ModuleSlot[all.size()]);
	}

	private ModuleSlot[] getSlotsForManifest(IFile f) {
		ArrayList<ModuleSlot> list = new ArrayList<ModuleSlot>();
		try {
			if( f.exists()) {
				String contents = FileUtil.getContents(f);
				if( contents != null ) {
					Manifest mf = new Manifest(new ByteArrayInputStream(contents.getBytes()));
					Attributes a = mf.getMainAttributes();
					String val = a.getValue("Dependencies");
					if( val != null ) {
						String[] byComma = val.split(",");
						for( int j = 0; j < byComma.length; j++ ) {
							ModuleSlot ms = getModuleSlot(byComma[j]);
							if( ms != null ) {
								list.add(ms);
							}
						}
					}
				}
			}
		} catch(IOException ioe) {
			// Ignore
		} catch(CoreException ce) {
			// Ignore
		}
		return (ModuleSlot[]) list.toArray(new ModuleSlot[list.size()]);
	}
	
	private ModuleSlot getModuleSlot(String ms) {
		if( ms != null ) {
			ms = ms.trim();
			if( ms.contains(" ")) {
				ms = ms.substring(0, ms.indexOf(" ")).trim();
			}
			int colon = ms.indexOf(":");
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
	
	
	private IFile[] getManifestFiles(IProject p) throws CoreException {
		final ArrayList<IFile> ret = new ArrayList<IFile>();
		p.accept(new IResourceVisitor(){
			public boolean visit(IResource resource) throws CoreException {
				if( resource.getName().toLowerCase().equals("manifest.mf")) {
					if( resource instanceof IFile) {
						ret.add((IFile)resource);
					}
				}
				return true;
			}});
		return (IFile[]) ret.toArray(new IFile[ret.size()]);
	}

	public static IRuntimePathProvider[] moduleSlotsAsProviders(ModuleSlot[] all) {
		ArrayList<IRuntimePathProvider> ret = new ArrayList<IRuntimePathProvider>();
		for( int i = 0; i < all.length; i++ ) {
			ret.add(new LayeredProductPathProvider(all[i]));
		}
		return (IRuntimePathProvider[]) ret.toArray(new IRuntimePathProvider[ret.size()]);
	}
	
}
