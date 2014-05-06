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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.wst.server.core.IRuntime;

/**
 * This class caches individual manifest files and what
 * ModuleSlot combinations they require. 
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
	
	
	ModuleSlotCache() {
		moduleSlotEntries = new HashMap<IFile, ModuleSlot[]>();
		lastUpdated = new HashMap<IFile, Long>();
		defaultModuleSlots = new HashMap<RuntimeKey, ModuleSlot[]>();
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
