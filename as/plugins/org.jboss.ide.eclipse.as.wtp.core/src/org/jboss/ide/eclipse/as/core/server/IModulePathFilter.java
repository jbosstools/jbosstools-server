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
import org.eclipse.wst.server.core.model.IModuleResourceDelta;

/**
 * This interface represents a module path filter. 
 * It is capable of determining whether a given IModuleResource should
 * be included, as well as returning "cleaned" module resource trees
 * and delta trees. 
 * 
 * Since implementations may choose to optimize results via caching, 
 * instances are not expected to return continuing or changing results. 
 * Changes to an underlying IModule or its members would require a 
 * new instance of the filter from which to create clean trees.
 * 
 * 
 * @since 2.4  Modified in 3.0
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
	
	/**
	 * Given a delta array, acquire a new delta array without any 
	 * files that would ordinarily be ignored by the filter
	 * 
	 * @param delta A module resource delta to be filtered
	 * @return delta A new delta cleaned of objects to be ignored
	 * @throws CoreException
	 * @since 3.0
	 */
	public IModuleResourceDelta[] getFilteredDelta(IModuleResourceDelta[] delta) throws CoreException;
}
