/*******************************************************************************
 * Copyright (c) 2011-2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.ide.eclipse.as.classpath.core.runtime.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jst.server.core.RuntimeClasspathProviderDelegate;
import org.eclipse.wst.server.core.IRuntime;
import org.jboss.ide.eclipse.as.classpath.core.runtime.CustomRuntimeClasspathModel;
import org.jboss.ide.eclipse.as.classpath.core.runtime.IRuntimePathProvider;
import org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal.ProjectRuntimeClasspathCache;
import org.jboss.ide.eclipse.as.classpath.core.runtime.cache.internal.RuntimeClasspathCache;
import org.jboss.ide.eclipse.as.classpath.core.runtime.modules.manifest.DeploymentStructureEntryContainerInitializer.DeploymentStructureEntryContainer;
import org.jboss.ide.eclipse.as.classpath.core.runtime.modules.manifest.DeploymentStructureUtil;
import org.jboss.ide.eclipse.as.classpath.core.runtime.modules.manifest.ModuleSlotManifestUtil;
import org.jboss.ide.eclipse.as.classpath.core.runtime.modules.manifest.ModulesManifestEntryContainerInitializer.ModulesManifestEntryContainer;
import org.jboss.ide.eclipse.as.classpath.core.runtime.path.internal.LayeredProductPathProvider;
import org.jboss.ide.eclipse.as.core.server.internal.ExtendedServerPropertiesAdapterFactory;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;

/**
 * This class is in use for all server types, because legacy projects
 * may have this classpath container ID still enabled. It cannot be changed. 
 * 
 * This class delegates to the "throw everything you can find" utility class,
 * for as6 and below. For as7/wf, it will read both from the 
 * client-all cache, as well as read the project's manifest.mf file
 * for jboss-modules style dependencies that can or should be added. 
 * 
 * This class expects the container path to have 1 
 * additional argument:  the name of the runtime,
 * though that is resolved by the superclass into an
 * actual IRuntime.
 * 
 * This class does not receive any information on facets
 * or facet versions enabled on the project. It is most often
 * used to acquire a classpath for projects that are 
 * NOT facet-based, typically POJP. 
 * 
 * The delegate utility handles caching and manipulating
 * the list of jars into a proper returnable set. The 
 * logic in *discovering* the set of jars is found in 
 * RuntimeJarUtility.
 */
public class ProjectRuntimeClasspathProvider 
		extends RuntimeClasspathProviderDelegate {
	
	// This initialization is here instead of in activator, because loading it in
	// the activator may be premature and cause overhead when no users are using this
	// classpath container.
	static {
		ManifestChangeListener.register();
		DeploymentStructureChangeListener.register();
	}
	
	
	// The path this container can be found under
	static final IPath CONTAINER_PATH = 
			new Path("org.eclipse.jst.server.core.container") //$NON-NLS-1$
			.append("org.jboss.ide.eclipse.as.core.server.runtime.runtimeTarget"); //$NON-NLS-1$
	
	
	public ProjectRuntimeClasspathProvider() {
		// Do Nothing
	}

	@Override
	public IClasspathEntry[] resolveClasspathContainer(IProject project, IRuntime runtime) {
		System.out.println("Inside resolveClasspathContainer");
		if( !isJBossModulesStyle(runtime)) {
			return legacyClientAllImplementation(project, runtime);
		}
		
		return jbossModulesImplementation(project, runtime);
	}
	
	// Impl for as6 and below
	private IClasspathEntry[] legacyClientAllImplementation(IProject project, IRuntime runtime) {
		ArrayList<IClasspathEntry> all = new ArrayList<IClasspathEntry>();
		// Add entries from default model, which are cached
		IClasspathEntry[] standardList = resolveClasspathContainerFromRuntime(runtime);
		all.addAll(Arrays.asList(standardList));
		
		return (IClasspathEntry[]) all.toArray(new IClasspathEntry[all.size()]);
	}
	
	/**
	 * impl for jboss-modules type servers.
	 * Because there may be collissions in what a requires in manifest.mf, vs
	 * what the default for a runtime type is, we must 
	 * hand-merge these two sets together. 
	 * This means we cannot benefit from caching here. 
	 * 
	 * @param project
	 * @param runtime
	 * @return
	 */
	private IClasspathEntry[] jbossModulesImplementation(IProject project, IRuntime runtime) {
		System.out.println("Inside jbossModulesImplementation");

		// check outdated slots
		boolean manifestsChanged = new ModuleSlotManifestUtil().isCacheOutdated(project); 
		boolean deploymentStructureChanged = new DeploymentStructureUtil().isCacheOutdated(project);
		boolean defaultsPerRuntimeChanged = (RuntimeClasspathCache.getInstance().getEntries(runtime) == null ? true : false);

		System.out.println("manifestsChanged: " + manifestsChanged);
		System.out.println("deploymentStructureChanged: " + deploymentStructureChanged);
		System.out.println("defaultsPerRuntimeChanged: " + defaultsPerRuntimeChanged);
		
		IClasspathEntry[] entries = ProjectRuntimeClasspathCache.getInstance().getEntries(project, runtime);
		if( manifestsChanged || defaultsPerRuntimeChanged || deploymentStructureChanged ||  entries == null) {
			System.out.println("Inside if statement");
			// check the changed manifests
			ModulesManifestEntryContainer cpc = new ModulesManifestEntryContainer(runtime, project);
			IRuntimePathProvider[] fromManifest = cpc.getRuntimePathProviders();
			System.out.println("fromManifest length: " + fromManifest.length);
			
			// check deployment-structure xml files
			DeploymentStructureEntryContainer depStructureContainer = new DeploymentStructureEntryContainer(runtime, project);
			IRuntimePathProvider[] fromStructure = depStructureContainer.getRuntimePathProviders();
			System.out.println("fromStructure length: " + fromStructure.length);
			
			// check default modules for the project's runtime
			IRuntimePathProvider[] fromRuntimeDefaults = CustomRuntimeClasspathModel.getInstance().getEntries(runtime.getRuntimeType());
			System.out.println("fromRuntimeDefaults length: " + fromRuntimeDefaults.length);

			
			
			IRuntimePathProvider[] merged = jbossModulesMerge(jbossModulesMerge(fromManifest, fromRuntimeDefaults), fromStructure);
			System.out.println("merged length: " + merged.length);

			// Merge it all together
			IPath[] allPaths = PathProviderResolutionUtil.getAllPaths(runtime, merged);
			System.out.println("allPaths length: " + allPaths.length);
			IClasspathEntry[] runtimeClasspath = PathProviderResolutionUtil.getClasspathEntriesForResolvedPaths(allPaths);
			System.out.println("runtimeClasspath length: " + runtimeClasspath.length);
			
			// store Cache in the various locales
			IPath[] fromRuntimeDefaultsPaths = PathProviderResolutionUtil.getAllPaths(runtime, fromRuntimeDefaults);
			IClasspathEntry[] fromRuntimeDefaultsEntries = PathProviderResolutionUtil.getClasspathEntriesForResolvedPaths(fromRuntimeDefaultsPaths);
			RuntimeClasspathCache.getInstance().cacheEntries(runtime, fromRuntimeDefaultsEntries);
			ProjectRuntimeClasspathCache.getInstance().cacheEntries(project, runtime, runtimeClasspath);
			return runtimeClasspath;
		} else {
			// take from cache
			return entries;
		}
	}
	
	// We must merge the required sets to ensure that duplicate jboss-modules are not added to the classpath
	private IRuntimePathProvider[] jbossModulesMerge(IRuntimePathProvider[] manifest, IRuntimePathProvider[] other) {
		ArrayList<IRuntimePathProvider> result = new ArrayList<IRuntimePathProvider>();
		result.addAll(Arrays.asList(manifest));
		
		for( int i = 0; i < other.length; i++ ) {
			if( other[i] instanceof LayeredProductPathProvider ) {
				if( !jbossModuleConflicts((LayeredProductPathProvider)other[i], result)) {
					// no conflict, can add
					result.add(other[i]);
				}
			} else {
				result.add(other[i]);
			}
		}
		
		return result.toArray(new IRuntimePathProvider[result.size()]);
	}
	
	/*
	 * Does any existing path provider represent a ModuleSlot with the same module id
	 * as the requested. 
	 */
	private boolean jbossModuleConflicts(LayeredProductPathProvider requested, List<IRuntimePathProvider> existing) {
		String proposedModule = requested.getModule();
		for( int i = 0; i < existing.size(); i++ ) {
			if( existing.get(i) instanceof LayeredProductPathProvider) {
				String mod = ((LayeredProductPathProvider)existing.get(i)).getModule();
				if( proposedModule.equals(mod))
					return true;
			}
		}
		return false;
	}
	
	/*
	 * Is this runtime a jboss-modules type server
	 */
	protected boolean isJBossModulesStyle(IRuntime rt) {
		ServerExtendedProperties props = new ExtendedServerPropertiesAdapterFactory().getExtendedProperties(rt);
		if( props.getFileStructure() == ServerExtendedProperties.FILE_STRUCTURE_CONFIG_DEPLOYMENTS) {
			return true;
		}
		return false;
	}
	
	/*
	 * For as6 and below, pull from the runtime-type model, which is cached once
	 * per runtime-type and is only recached if the list of default path providers is changed. 
	 */
	public IClasspathEntry[] resolveClasspathContainerFromRuntime(IRuntime runtime) {
		if( runtime == null ) 
			return new IClasspathEntry[0];

		// if cache is available, use cache
		IClasspathEntry[] runtimeClasspath = RuntimeClasspathCache.getInstance().getEntries(runtime);
		if (runtimeClasspath != null) {
			return runtimeClasspath;
		}
		
		// resolve
		IRuntimePathProvider[] sets = CustomRuntimeClasspathModel.getInstance().getEntries(runtime.getRuntimeType());
		IPath[] allPaths = PathProviderResolutionUtil.getAllPaths(runtime, sets);
		runtimeClasspath = PathProviderResolutionUtil.getClasspathEntriesForResolvedPaths(allPaths);
	
		RuntimeClasspathCache.getInstance().cacheEntries(runtime, runtimeClasspath);
		return runtimeClasspath;
	}
}
