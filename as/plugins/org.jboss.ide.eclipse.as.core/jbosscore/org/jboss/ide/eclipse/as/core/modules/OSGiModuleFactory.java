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
package org.jboss.ide.eclipse.as.core.modules;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.pde.internal.core.WorkspaceModelManager;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.eclipse.wst.server.core.util.ProjectModuleFactoryDelegate;
import org.jboss.ide.eclipse.as.wtp.core.modules.IJBTModule;

public class OSGiModuleFactory extends ProjectModuleFactoryDelegate {

	public static final String MODULE_TYPE = "jboss.osgi"; //$NON-NLS-1$
	public static final String VERSION = "1.0"; //$NON-NLS-1$

	public class OSGiModuleDelegate extends ModuleDelegate implements IJBTModule {
		public IStatus validate() {
			return null;
		}
		public IModule[] getChildModules() {
			return new IModule[]{};
		}
		public IModuleResource[] members() throws CoreException {
			return new IModuleResource[]{};
		}
		public IModule[] getModules() {
			return getChildModules();
		}
		public String getURI(IModule module) {
			return null;
		}
		public boolean isBinary() {
			return true;
		}
	}
	
	@Override
	public ModuleDelegate getModuleDelegate(IModule module) {
		return new OSGiModuleDelegate();
	}
	
	/**
	 * Creates the modules that are contained within a given project.
	 * 
	 * @param project a project to create modules for
	 * @return a possibly-empty array of modules
	 */
	protected IModule[] createModules(IProject project) {
		if (!WorkspaceModelManager.isBinaryProject(project) && WorkspaceModelManager.isPluginProject(project)) {
			IModel model = PluginRegistry.findModel(project);
			if (model != null && isValidModel(model) && hasBuildProperties((IPluginModelBase) model)) {
				IModule module = createModule(project.getName(), project.getName(), 
						MODULE_TYPE, VERSION, project);
				return new IModule[] { module };
			}
		}
		return null;
	}
	
	private boolean hasBuildProperties(IPluginModelBase model) {
		File file = new File(model.getInstallLocation(), ICoreConstants.BUILD_FILENAME_DESCRIPTOR);
		return file.exists();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.wizards.exports.BaseExportWizardPage#isValidModel(org.eclipse.pde.core.IModel)
	 */
	protected boolean isValidModel(IModel model) {
		return model != null && model instanceof IPluginModelBase;
	}

	@Override
	protected IPath[] getListenerPaths() {
		return new IPath[] { new Path(".project"), // nature //$NON-NLS-1$
				new Path("META-INF/MANIFEST.MF"), // manifest //$NON-NLS-1$
				new Path(".settings/org.eclipse.pde.core.prefs") // pde prefs //$NON-NLS-1$
		};
	}

}
