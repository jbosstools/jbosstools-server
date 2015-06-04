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
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal.ModuleSlot;
import org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal.ModuleSlotCache;
import org.jboss.ide.eclipse.as.core.util.FileUtil;

/**
 * Read a given manifest.mf file and find all
 * module/slot combinations that are being requested
 * for classpath addition
 */
public class ModuleSlotManifestUtil extends AbstractModuleSlotUtil {
	public ModuleSlotManifestUtil() {
		
	}
	
	@Override
	protected boolean cacheInitializedProject(IProject p) {
		return ModuleSlotCache.getInstance().hasInitializedManifests(p);
	}

	@Override
	protected IFile[] getCachedFiles(IProject p) {
		return ModuleSlotCache.getInstance().getManifests(p);
	}

	@Override
	protected void cacheFiles(IProject p, IFile[] files) {
		ModuleSlotCache.getInstance().setManifests(p, files);
	}
	
	protected boolean isInitialized(IProject p) {
		return ModuleSlotCache.getInstance().hasInitializedManifests(p);
	}
	
	protected ModuleSlot[] calculateModuleSlots(IFile f) {
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
	
	protected IFile[] locateRelevantFiles(IProject p) throws CoreException {
		return super.locateFiles(p, "manifest.mf");
	}
}
