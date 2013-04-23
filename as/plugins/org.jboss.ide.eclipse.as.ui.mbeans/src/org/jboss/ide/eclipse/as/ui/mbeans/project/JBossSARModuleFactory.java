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
	public static final String MODULE_TYPE = IJBossSARFacetDataModelProperties.SAR_PROJECT_FACET;
	public static final String V1_0 = "1.0"; //$NON-NLS-1$
	
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
		return hasProjectFacet(project, MODULE_TYPE);
	}

	@Override
	protected String getModuleType(IProject project) {
		return MODULE_TYPE;
	}

	@Override
	protected String getModuleVersion(IProject project) {
		return V1_0;
	}

	@Override
	protected String getModuleType(File binaryFile) {
		// sar allows no child modules
		return null;
	}

	@Override
	protected String getModuleVersion(File binaryFile) {
		// sar allows no child modules
		return null;
	}

	@Override 
	public IModule createChildModule(FlatComponentDeployable parent, IChildModuleReference child) {
		// sar allows no child modules
		return null;
	}
}
