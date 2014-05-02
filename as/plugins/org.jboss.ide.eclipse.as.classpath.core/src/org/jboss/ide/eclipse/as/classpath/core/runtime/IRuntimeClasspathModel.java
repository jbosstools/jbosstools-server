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
package org.jboss.ide.eclipse.as.classpath.core.runtime;

/**
 * A class representing the classpath entries for one runtime type.
 * You may query this for a list of {@link IRuntimePathProvider} 
 * objects for each of various criteria, including 
 * specific facet id's, or facet id + version combinations.
 * 
 * It can also be queried for a general-case 
 * list of {@link IRuntimePathProvider} objects
 * without the context of a facet id or version, 
 * however this list will probably be more extensive
 * then is needed by most users. 
 */
public interface IRuntimeClasspathModel {
	
	/**
	 * Get a list of {@link IRuntimePathProvider} objects
	 * without any facet for context. 
	 * @return
	 */
	public IRuntimePathProvider[] getStandardProviders();
	
	/**
	 * Returns a list for a given facet. 
	 * @param id
	 * @return
	 */
	public IRuntimePathProvider[] getProvidersForFacet(String id);
	
	/**
	 * Get the providers for a given facet and version.
	 * 
	 * A request with a null facet and version is equivilent
	 * to getRuntimeTypeDefaultProviders();
	 * 
	 * A request with a facet id, but a null version, is equivilent to 
	 * getProvidersForFacet(String id);
	 * 
	 * @param id
	 * @param version
	 * @return
	 */
	public IRuntimePathProvider[] getProvidersForFacet(String id, String version);
	
	
	/**
	 * Get a list of all facets that have customized sets
	 * @return
	 */
	public String[] getCustomizedFacets();
}
