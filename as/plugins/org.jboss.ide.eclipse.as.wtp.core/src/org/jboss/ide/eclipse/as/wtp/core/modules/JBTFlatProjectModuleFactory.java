/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core.modules;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.j2ee.internal.deployables.BinaryFileModuleDelegate;
import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.internal.StructureEdit;
import org.eclipse.wst.common.componentcore.internal.flat.IChildModuleReference;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.internal.Module;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.util.ProjectModuleFactoryDelegate;
import org.eclipse.wst.web.internal.deployables.FlatComponentDeployable;

public abstract class JBTFlatProjectModuleFactory extends ProjectModuleFactoryDelegate implements IResourceChangeListener {
	public static final String BINARY_PREFIX = "/binary:"; //$NON-NLS-1$
	public static void ensureFactoryLoaded(String factoryId) {
        ModuleFactory[] factories = ServerPlugin.getModuleFactories();
        for( int i = 0; i < factories.length; i++ ) {
                if( factories[i].getId().equals(factoryId)) {
                        factories[i].getDelegate(new NullProgressMonitor());
                }
        }
	}
	
	protected Map <IModule, FlatComponentDeployable> moduleDelegates = new HashMap<IModule, FlatComponentDeployable>(5);
	public JBTFlatProjectModuleFactory() {
		super();
	}
	
	@Override
	public void initialize() {
		super.initialize();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	
	@Override
	protected IModule[] createModules(IProject project) {
		IVirtualComponent component = ComponentCore.createComponent(project);
		if(component != null)
			return createModuleDelegates(component);
		return null;
	}


	@Override
	public ModuleDelegate getModuleDelegate(IModule module) {
		if (module == null)
			return null;
		ModuleDelegate md = moduleDelegates.get(module);
		if( md == null && ((Module)module).getInternalId().startsWith(BINARY_PREFIX))
			return createBinaryDelegate(module);
		if (md == null) {
			createModules(module.getProject());
			md = moduleDelegates.get(module);
		}
		return md;
	}

	protected abstract boolean canHandleProject(IProject p);
	protected abstract String getModuleType(IProject project);
	protected abstract String getModuleVersion(IProject project);
	protected abstract String getModuleType(File binaryFile);
	protected abstract String getModuleVersion(File binaryFile);
	protected FlatComponentDeployable createModuleDelegate(IProject project, IVirtualComponent component) {
		return new JBTFlatModuleDelegate(project, component, this);
	}

	protected FlatComponentDeployable getNestedDelegate(IVirtualComponent component) {
		return new JBTFlatModuleDelegate(component.getProject(), component, this);
	}

	protected IModule[] createModuleDelegates(IVirtualComponent component) {
		if(component != null && canHandleProject(component.getProject())) {
			String type = getModuleType(component.getProject());
			String version = getModuleVersion(component.getProject());
			IModule module = createModule(component.getName(), component.getName(), type, version, component.getProject());
			FlatComponentDeployable moduleDelegate = createModuleDelegate(component.getProject(), component);
			moduleDelegates.put(module, moduleDelegate);
			return new IModule[]{module};
		}
		return null; 
	}

	
	/**
	 * From this point on, when queried, projects will generate their binary 
	 * child modules on the fly and they will be small and dumb
	 * 
	 * @param parent
	 * @param child
	 * @return
	 */
	public IModule createChildModule(FlatComponentDeployable parent, IChildModuleReference child) {
		File file = child.getFile();
		if( file != null ) {
			IPath p = new Path(file.getAbsolutePath());
			String id = BINARY_PREFIX + file.getAbsolutePath();
			IModule nestedModule = createModule(id, file.getName(), 
					getModuleType(file), getModuleVersion(file), 
					parent.getProject());
			FlatComponentDeployable moduleDelegate = getNestedDelegate(child.getComponent());
			moduleDelegates.put(nestedModule, moduleDelegate);
			return nestedModule;
		}
		return null;
	}
	
	/**
	 * Create a module delegate on the fly for this binary file
	 * @param module
	 * @return
	 */
	public ModuleDelegate createBinaryDelegate(IModule module) {
		String internalId = ((Module)module).getInternalId();
		String path = internalId.substring(BINARY_PREFIX.length());
		File f = new File(path);
		return new BinaryFileModuleDelegate(f);
	}

	public void resourceChanged(IResourceChangeEvent event) {
		cleanAllDelegates();
	}
	
	protected void cleanAllDelegates() {
		Iterator<FlatComponentDeployable> i = moduleDelegates.values().iterator();
		while(i.hasNext()) {
			i.next().clearCache();
		}
		modulesChanged();
	}
	
	
	/**
	 * Returns the list of resources that the module should listen to for state
	 * changes. The paths should be project relative paths. Subclasses can
	 * override this method to provide the paths.
	 * 
	 * @return a possibly empty array of paths
	 */
	@Override
	protected IPath[] getListenerPaths() {
		return new IPath[] { new Path(".project"), // nature //$NON-NLS-1$
				new Path(StructureEdit.MODULE_META_FILE_NAME), // component
				new Path(".settings/org.eclipse.wst.common.project.facet.core.xml") // facets //$NON-NLS-1$
		};
	}

	@Override
	protected void clearCache(IProject project) {
		super.clearCache(project);
		List<IModule> modulesToRemove = null;
		for (Iterator<IModule> iterator = moduleDelegates.keySet().iterator(); iterator.hasNext();) {
			IModule module = iterator.next();
			if (module.getProject() != null && module.getProject().equals(project)) {
				if (modulesToRemove == null) {
					modulesToRemove = new ArrayList<IModule>();
				}
				modulesToRemove.add(module);
			}
		}
		if (modulesToRemove != null) {
			for (IModule module : modulesToRemove) {
				moduleDelegates.remove(module);
			}
		}
	}

	
}