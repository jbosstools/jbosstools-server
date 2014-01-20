/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.publishers.patterns;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.as.core.publishers.patterns.internal.PublishFilterDirectoryScanner;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.util.ModuleResourceUtil;

/**
 * @since 2.3
 */
public class ModuleDirectoryScannerPathFilter implements IModulePathFilter {

	private PublishFilterDirectoryScanner scanner;
	private boolean scanned = false;
	private IModuleResource[] raw;
	private IModuleResource[] cleanedResources;
	
	/**
	 * Convenience method for the constructor signature with arrays.
	 * This signature will auto-split a pattern on the comma character 
	 * into an array of patterns. 
	 * 
	 * For example, if includesPattern = "a/**,b/**"  it will be split 
	 * into new String[]{"a/**", "b/**"};
	 * 
	 * @param module
	 * @param includes
	 * @param excludes
	 * @throws CoreException
	 */
	public ModuleDirectoryScannerPathFilter(IModule module, 
			String includes, String excludes) throws CoreException {
		this(module, explode(includes), explode(excludes));
	}
		
	// Explode a given string by comma
	private static String[] explode(String pattern) {
		return pattern.split(","); //$NON-NLS-1$
	}
	
	/**
	 * 
	 * @param module
	 * @param includes
	 * @param excludes
	 * @throws CoreException
	 */
	public ModuleDirectoryScannerPathFilter(IModule module, 
			String[] includes, String[] excludes) throws CoreException {
		raw = ModuleResourceUtil.getMembers(module);
		scanner = new PublishFilterDirectoryScanner(raw);
		scanner.setIncludes(includes);
		scanner.setExcludes(excludes);
	}
	
	private synchronized void ensureScanned() {
		if( !scanned ) {
			scanner.scan();
			scanned = true;
		}
	}
	
	@Override
	public boolean shouldInclude(IModuleResource moduleResource) {
		ensureScanned();
		return scanner.isRequiredMember(moduleResource);
	}

	@Override
	public IModuleResource[] getFilteredMembers() throws CoreException {
		ensureScanned();
		if( cleanedResources == null ) {
			cleanedResources = new ModulePathFilterUtility(this).getCleanedMembers(raw);
		}
		return cleanedResources;
	}

	
	/*
	 * This class will not cache the results of a cleaned delta. 
	 * It will use the cleaned resource tree and already cached scanner 
	 * to filter the delta and return a cleaned delta.
	 * 
	 * It is required that this delta is a delta for the module used
	 * in this object's constructor.
	 * 
	 * (non-Javadoc)
	 * @see org.jboss.ide.eclipse.as.core.server.IModulePathFilter#getFilteredDelta(org.eclipse.wst.server.core.model.IModuleResourceDelta)
	 */
	@Override
	public IModuleResourceDelta[] getFilteredDelta(IModuleResourceDelta[] delta) throws CoreException {
		ensureScanned();
		return new ModulePathFilterUtility(this).getCleanedDelta(delta);
	}
}
