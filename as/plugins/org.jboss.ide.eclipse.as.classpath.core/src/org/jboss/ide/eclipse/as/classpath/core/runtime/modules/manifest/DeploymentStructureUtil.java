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

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal.ModuleSlot;
import org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal.ModuleSlotCache;
import org.jboss.tools.foundation.core.xml.IMemento;
import org.jboss.tools.foundation.core.xml.XMLMemento;

/**
 * Read a given jboss-deployment-structure.xml file and find all
 * module/slot combinations that are being requested
 * for classpath addition
 * 
 * This class may consider merging with ModuleSlotManifestUtil or gaining a common superclass
 */
public class DeploymentStructureUtil extends AbstractModuleSlotUtil{
	public DeploymentStructureUtil() {
	}
	
	protected void cacheFiles(IProject p, IFile[] files) {
		ModuleSlotCache.getInstance().setDeploymentStructures(p, files);
	}
	
	protected boolean cacheInitializedProject(IProject p) {
		return ModuleSlotCache.getInstance().hasInitializedDeploymentStructures(p);
	}
	
	protected IFile[] getCachedFiles(IProject p) {
		return ModuleSlotCache.getInstance().getDeploymentStructures(p);
	}
	
	protected boolean isInitialized(IProject p) {
		return ModuleSlotCache.getInstance().hasInitializedDeploymentStructures(p);
	}

	protected ModuleSlot[] calculateModuleSlots(IFile f) {
		ArrayList<ModuleSlot> collector = new ArrayList<ModuleSlot>();
		try {
			XMLMemento mem = XMLMemento.createReadRoot(f.getContents());
			// contains a list of  deployment   sub-deployment   module
			IMemento[] all = mem.getChildren();
			for( int i = 0; i < all.length; i++ ) {
				IMemento[] deps = all[i].getChildren("dependencies"); //$NON-NLS-1$
				for( int j = 0; j < deps.length; j++ ) {
					IMemento[] modules = deps[j].getChildren("module"); //$NON-NLS-1$
					for( int k = 0; k < modules.length; k++ ) {
						String name = modules[k].getString("name"); //$NON-NLS-1$
						String slot = modules[k].getString("slot"); //$NON-NLS-1$
						if( name != null )
							collector.add(new ModuleSlot(name, slot));
					}
				}
			}
			return (ModuleSlot[]) collector.toArray(new ModuleSlot[collector.size()]);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO
		return new ModuleSlot[0];
	}
	
	protected IFile[] locateRelevantFiles(IProject p) throws CoreException {
		return super.locateFiles(p,  "jboss-deployment-structure.xml"); //$NON-NLS-1$
	}
}
