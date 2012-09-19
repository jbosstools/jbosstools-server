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
package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.model.IModuleResource;

/**
 * @since 2.3
 */
public interface IModulePathFilter {
	/**
	 * This method should be used for incremental checks on a 
	 * particular resource, such as during incremental publishing
	 * through deltas. 
	 * 
	 * It *can* be used throughout the entire tree without a substantial
	 * performance hit, however, the suggested API to use for that is 
	 * getFilteredResources(IModuleResource[] original)
	 * 
	 * @param moduleResource  The resource to check
	 * @return
	 */
	public boolean shouldInclude(IModuleResource moduleResource);
	
	/**
	 * The easiest way to get a new clean pre-filtered list of members
	 * that actually need to be published 
	 * 
	 * @return  the filtered list of members
	 */
	public IModuleResource[] getFilteredMembers() throws CoreException;
}
