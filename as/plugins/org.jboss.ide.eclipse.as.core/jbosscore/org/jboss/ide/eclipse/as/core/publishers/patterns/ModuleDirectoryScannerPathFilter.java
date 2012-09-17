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

import org.eclipse.wst.server.core.model.IModuleResource;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;

/**
 * @since 2.3
 */
public class ModuleDirectoryScannerPathFilter implements IModulePathFilter {

	private PublishFilterDirectoryScanner scanner;
	private boolean scanned = false;
	public ModuleDirectoryScannerPathFilter(IModuleResource[] members, 
			String includes, String excludes) {
		scanner = new PublishFilterDirectoryScanner(members);
		scanner.setIncludes(includes);
		scanner.setExcludes(excludes);
	}
	
	private void ensureScanned() {
		// TODO add synchronized
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
	public IModuleResource[] getFilteredMembers() {
		ensureScanned();
		return scanner.getCleanedMembers();
	}
}
