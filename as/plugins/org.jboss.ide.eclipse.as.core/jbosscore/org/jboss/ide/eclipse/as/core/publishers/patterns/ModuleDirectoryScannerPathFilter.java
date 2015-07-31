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
import org.jboss.ide.eclipse.as.core.publishers.patterns.internal.PublishFilterDirectoryScanner;

/**
 * @since 2.3
 * @deprecated - please use superclass
 */
@Deprecated
public class ModuleDirectoryScannerPathFilter extends org.jboss.ide.eclipse.as.wtp.core.modules.filter.patterns.ModuleDirectoryScannerPathFilter {
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
		super(module, includes, excludes);
	}

	public ModuleDirectoryScannerPathFilter(IModule module, 
			String[] includes, String[] excludes) throws CoreException {
		super(module, includes, excludes);
	}
	
	public ModuleDirectoryScannerPathFilter(IModule module, 
			PublishFilterDirectoryScanner scanner) throws CoreException {
		super(module, scanner);
	}
}
