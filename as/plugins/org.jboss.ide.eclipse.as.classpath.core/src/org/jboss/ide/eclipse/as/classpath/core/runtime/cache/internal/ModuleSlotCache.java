/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.wst.server.core.IRuntime;

/**
 * This class caches individual manifest or deployment-structure 
 * files and what ModuleSlot combinations they require. 
 */
public class ModuleSlotCache {
	private static ModuleSlotCache instance = null;
	public static ModuleSlotCache getInstance() {
		if( instance == null )
			instance = new ModuleSlotCache();
		return instance;
	}
	
	// Cache a given file to the required module-slot combos
	private Map<IFile, ModuleSlot[]> moduleSlotEntries;
	// Cache last time a module file was updated
	private Map<IFile, Long> lastUpdated;
	
	// Cache the default module/slots for a given runtime type
	private Map<RuntimeKey, ModuleSlot[]> defaultModuleSlots;
	
	// Keep a list of manifest files for each project
	private Map<IProject, ArrayList<IFile>> manifests;

	// Keep a list of manifest files for each project
	private Map<IProject, ArrayList<IFile>> deploymentStructures;

	
	ModuleSlotCache() {
		moduleSlotEntries = new HashMap<IFile, ModuleSlot[]>();
		lastUpdated = new HashMap<IFile, Long>();
		defaultModuleSlots = new HashMap<RuntimeKey, ModuleSlot[]>();
		manifests = new HashMap<IProject, ArrayList<IFile>>();
		deploymentStructures = new HashMap<IProject, ArrayList<IFile>>();
	}
	
	public boolean hasInitializedManifests(IProject p) {
		return manifests.containsKey(p) && manifests.get(p) != null;
	}

	public boolean hasInitializedDeploymentStructures(IProject p) {
		return deploymentStructures.containsKey(p) && deploymentStructures.get(p) != null;
	}

	public void setManifests(IProject p, IFile[] manFiles) {
		manifests.put(p, new ArrayList<IFile>(Arrays.asList(manFiles)));
	}
	
	public void setDeploymentStructures(IProject p, IFile[] manFiles) {
		deploymentStructures.put(p, new ArrayList<IFile>(Arrays.asList(manFiles)));
	}
	
	public IFile[] getManifests(IProject p) {
		ArrayList<IFile> ret = manifests.get(p);
		if( ret != null ) {
			return (IFile[]) ret.toArray(new IFile[ret.size()]);
		}
		return null;
	}
	
	public IFile[] getDeploymentStructures(IProject p) {
		ArrayList<IFile> ret = deploymentStructures.get(p);
		if( ret != null ) {
			return (IFile[]) ret.toArray(new IFile[ret.size()]);
		}
		return null;
	}
	
	public void cache(IFile file, ModuleSlot[] ms) {
		lastUpdated.put(file, file.getLocation().toFile().lastModified());
		moduleSlotEntries.put(file, ms);
		
	}
	
	public ModuleSlot[] getEntries(IFile file) {
		return moduleSlotEntries.get(file);
	}
	
	public boolean isOutdated(IFile file) {
		Object o = lastUpdated.get(file);
		if( o == null || !(o instanceof Long) || ((Long)o).longValue() < file.getLocation().toFile().lastModified()) {
			return true;
		}
		return false;
	}	
	
	public void cacheRuntimeModuleSlots(IRuntime runtime, ModuleSlot[] all) {
		RuntimeKey key = RuntimeClasspathCache.getRuntimeKey(runtime);
		defaultModuleSlots.put(key, all);
	}
	
	public ModuleSlot[] getRuntimeModuleSlots(IRuntime runtime) {
		RuntimeKey key = RuntimeClasspathCache.getRuntimeKey(runtime);
		return defaultModuleSlots.get(key);
	}
	
	public void clearRuntimeModuleSlots(IRuntime runtime) {
		RuntimeKey key = RuntimeClasspathCache.getRuntimeKey(runtime);
		defaultModuleSlots.remove(key);		
	}
}
