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
package org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.ide.eclipse.as.classpath.core.runtime.IRuntimeClasspathModel;
import org.jboss.ide.eclipse.as.classpath.core.runtime.IRuntimePathProvider;

public class InternalRuntimeClasspathModel implements IRuntimeClasspathModel {
	// HOlds the default response 
	private ArrayList<IRuntimePathProvider> noFacetList = new ArrayList<IRuntimePathProvider>();
	
	// Holds the per-facet 
	private HashMap<String, IRuntimePathProvider[]> perFacet = new HashMap<String, IRuntimePathProvider[]>();
	
	// Holds the per-facet / version results. 
	private HashMap<String, HashMap<String, IRuntimePathProvider[]>> perFacetVersion = new HashMap<String, HashMap<String, IRuntimePathProvider[]>>();
	
	public InternalRuntimeClasspathModel() {
		
	}
	
	/**
	 * Add the given providers to the generic list
	 * to be returned for a request with no facet provider
	 * 
	 * @param providers
	 */
	public void addProviders(IRuntimePathProvider[] providers) {
		noFacetList.addAll(Arrays.asList(providers));
	}
	
	public void setProviders(IRuntimePathProvider[] providers) {
		noFacetList.clear();
		addProviders(providers);
	}
	
	public void addProviders(String facet, IRuntimePathProvider[] providers) {
		addProviders(facet, providers, false);
	}

	public void setProviders(String facet, IRuntimePathProvider[] providers) {
		addProviders(facet, providers, true);
	}
	
	public void addProviders(String facet, IRuntimePathProvider[] providers, boolean clearFirst) {
		IRuntimePathProvider[] existing = perFacet.get(facet);
		ArrayList<IRuntimePathProvider> toAdd = new ArrayList<IRuntimePathProvider>();
		if( !clearFirst && existing != null ) {
			toAdd.addAll(Arrays.asList(existing));
		}
		if( providers != null ) {
			toAdd.addAll(Arrays.asList(providers));
		}
		perFacet.put(facet, (IRuntimePathProvider[]) toAdd.toArray(new IRuntimePathProvider[toAdd.size()]));
	}
	
	public void addProviders(String facet, String version, IRuntimePathProvider[] providers) {
		addProviders(facet, version, providers, false);
	}

	public void setProviders(String facet, String version, IRuntimePathProvider[] providers) {
		addProviders(facet, version, providers, true);
	}

	public void addProviders(String facet, String version, IRuntimePathProvider[] providers, boolean clearFirst) {
		if( facet == null ) {
			addProviders(providers);
		}
		if( version == null ) {
			addProviders(facet, providers);
		}
		HashMap<String, IRuntimePathProvider[]> existingFacet = perFacetVersion.get(facet);
		if( existingFacet == null ) {
			existingFacet = new HashMap<String, IRuntimePathProvider[]>();
			perFacetVersion.put(facet, existingFacet);
		}
		
		IRuntimePathProvider[] existing = existingFacet.get(facet);
		ArrayList<IRuntimePathProvider> toAdd = new ArrayList<IRuntimePathProvider>();
		if( !clearFirst && existing != null ) {
			toAdd.addAll(Arrays.asList(existing));
		}
		if( providers != null ) {
			toAdd.addAll(Arrays.asList(providers));
		}
		perFacet.put(facet, (IRuntimePathProvider[]) toAdd.toArray(new IRuntimePathProvider[toAdd.size()]));
	}
	
	@Override
	public IRuntimePathProvider[] getStandardProviders() {
		return (IRuntimePathProvider[]) noFacetList.toArray(new IRuntimePathProvider[noFacetList.size()]);
	}

	@Override
	public IRuntimePathProvider[] getProvidersForFacet(String id) {
		IRuntimePathProvider[] ret = perFacet.get(id);
		return ret == null ? getStandardProviders() : ret;
	}
	
	@Override
	public IRuntimePathProvider[] getProvidersForFacet(String id, String version) {
		if( id != null ) {
			HashMap<String, IRuntimePathProvider[]> versionToProviders = perFacetVersion.get(id);
			if( version != null && versionToProviders != null ) {
				IRuntimePathProvider[] ret = versionToProviders.get(version);
				if( ret != null )
					return ret;
			}
			return getProvidersForFacet(id);
		}
		return getStandardProviders();
	}
	
	public String[] getCustomizedFacets() {
		Set<String> ts = new TreeSet<String>();
		ts.addAll(perFacet.keySet());
		return (String[]) ts.toArray(new String[ts.size()]);
	}

	public String[] getCustomizedFacetVersions() {
		Set<String> ts = new TreeSet<String>();
		ts.addAll(perFacetVersion.keySet());
		return (String[]) ts.toArray(new String[ts.size()]);
	}

	public String[] getCustomizedFacetVersions(String facet) {
		HashMap<String, IRuntimePathProvider[]> k = perFacetVersion.get(facet);
		if( k != null ) {
			Set<String> ts = new TreeSet<String>();
			ts.addAll(k.keySet());
			return (String[]) ts.toArray(new String[ts.size()]);
		}
		return new String[0];
	}
}
