/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.ui.mbeans.project;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.common.componentcore.internal.flat.IChildModuleReference;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.web.internal.deployables.FlatComponentDeployable;
import org.jboss.ide.eclipse.as.ui.mbeans.Activator;
import org.jboss.ide.eclipse.as.wtp.core.modules.JBTFlatProjectModuleFactory;

public class JBossSARModuleFactory extends JBTFlatProjectModuleFactory {
	public static final String FACTORY_ID = "org.jboss.ide.eclipse.as.core.modules.sar.moduleFactory"; //$NON-NLS-1$
	public static final String MODULE_TYPE = IJBossSARFacetDataModelProperties.JBOSS_SAR_FACET_ID;

	public String getFactoryId() {
		return FACTORY_ID;
	}
	
	public JBossSARModuleFactory() {
		super();
	}

	protected FlatComponentDeployable createModuleDelegate(IProject project, IVirtualComponent component) {
		return new JBossSARModuleDelegate(project, component, this);
	}

	@Override
	protected boolean canHandleProject(IProject project) {
		IProjectFacet facet = ProjectFacetsManager
				.getProjectFacet(MODULE_TYPE);
		IFacetedProject facetedProject = null;
		try {
			facetedProject = ProjectFacetsManager.create(project);
			if (facetedProject.hasProjectFacet(facet)) {
				return true;
			}
		} catch (CoreException e) {
			/*
			 *  Ignore. No matter what problem occurs here, 
			 *  if the project is closed, inaccessible, is not
			 *  a faceted project, etc, it is not an error. The 
			 *  project simply cannot be handled by this factory. 
			 *  
			 *  But I'll log it anyway :/
			 */
			Platform.getLog(Platform.getBundle(Activator.PLUGIN_ID)).log( 
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}

		return false;
	}

	@Override
	protected String getModuleType(IProject project) {
		// TODO Auto-generated method stub
		return MODULE_TYPE;
	}

	@Override
	protected String getModuleVersion(IProject project) {
		return "1.0"; //$NON-NLS-1$
	}

	@Override
	protected String getModuleType(File binaryFile) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getModuleVersion(File binaryFile) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override 
	public IModule createChildModule(FlatComponentDeployable parent, IChildModuleReference child) {
		return null;
	}
}
