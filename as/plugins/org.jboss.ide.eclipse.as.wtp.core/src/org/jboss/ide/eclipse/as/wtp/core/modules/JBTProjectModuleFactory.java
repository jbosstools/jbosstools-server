/******************************************************************************* 
 * Copyright (c) 2009 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.internal.StructureEdit;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.model.ModuleFactoryDelegate;
import org.eclipse.wst.server.core.util.ProjectModuleFactoryDelegate;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;

public abstract class JBTProjectModuleFactory extends ProjectModuleFactoryDelegate {
	public static JBTProjectModuleFactory getFactory(String id) {
		ModuleFactory[] factories = ServerPlugin.getModuleFactories();
		for (int i = 0; i < factories.length; i++) {
			if (factories[i].getId().equals(id)) {
				ModuleFactoryDelegate o = factories[i]
						.getDelegate(new NullProgressMonitor());
				return (JBTProjectModuleFactory)o;
			}
		}
		return null;
	}

	
	protected HashMap<IModule, JBTProjectModuleDelegate> moduleToDelegate;
	protected String moduleType;
	protected String facetType;
	public JBTProjectModuleFactory(String moduleType, String facetType) {
		moduleToDelegate = new HashMap<IModule, JBTProjectModuleDelegate>();
		this.moduleType = moduleType;
		this.facetType = facetType;
	}

	@Override
	protected void clearCache(IProject project) {
		super.clearCache(project);
		moduleToDelegate.remove(project);
	}
	
	@Override
	public ModuleDelegate getModuleDelegate(IModule module) {
		return moduleToDelegate.get(module);
	}

	protected IModule[] createModules(IProject project) {
		IFacetedProject facetProject;
		try {
			facetProject = ProjectFacetsManager.create(project);
			if (facetProject == null) {
				return null;
			}
			IProjectFacet facet = ProjectFacetsManager
					.getProjectFacet(facetType);

			if (facetProject.hasProjectFacet(facet)) {
				IProjectFacetVersion version = facetProject.getProjectFacetVersion(facet);
				IModule module = createModule(
						moduleType + "." + project.getName(), 
						project.getName(), 
						moduleType, 
						version.getVersionString(), 
						project);
				JBTProjectModuleDelegate delegate = createDelegate(project);
				moduleToDelegate.put(module, delegate);
				
				createBinaryModules(ComponentCore.createComponent(project), delegate);
				// TODO - create children!!! see JEEDeployableFactory
				return new IModule[] { module };
			}
		} catch (CoreException e) {
			ASWTPToolsPlugin.getDefault().getLog().log(e.getStatus());
		}
		return null;
	}
	
	protected void createBinaryModules(IVirtualComponent component, JBTProjectModuleDelegate delegate) {
		List projectModules = new ArrayList();
		IVirtualReference[] references = component.getReferences();
		for (int i = 0; i < references.length; i++) {
			IVirtualComponent moduleComponent = references[i].getReferencedComponent();
			if (moduleComponent.isBinary()) {		
				if( !delegate.shouldIncludeUtilityComponent(moduleComponent, references, null)) {
					// if we shouldn't include it there, we should do it here
					// TODO do this
				}
			}
		}
	}

	protected IPath[] getListenerPaths() {
		return new IPath[] { new Path(".project"), // nature
				new Path(StructureEdit.MODULE_META_FILE_NAME), // component
				new Path(".settings/org.eclipse.wst.common.project.facet.core.xml") // facets
		};
	}

	
	protected abstract JBTProjectModuleDelegate createDelegate(IProject project);
	
}
