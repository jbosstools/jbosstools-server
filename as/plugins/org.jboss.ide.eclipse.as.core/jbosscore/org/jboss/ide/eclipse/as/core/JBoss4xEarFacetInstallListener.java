/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jst.jee.project.facet.EarCreateDeploymentFilesDataModelProvider;
import org.eclipse.jst.jee.project.facet.ICreateDeploymentFilesDataModelProperties;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.events.IFacetedProjectEvent;
import org.eclipse.wst.common.project.facet.core.events.IFacetedProjectListener;
import org.eclipse.wst.common.project.facet.core.events.IProjectFacetActionEvent;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.util.IWTPConstants;

import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.AS_42;
import static org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants.EAP_43;

/**
 * This class is here to deal with the case when a new Ear project is
 * created but targeted to a JBoss 4.x server. JBoss 4.x servers require
 * application.xml files in their structure to be deployable. 
 * 
 * @author rob.stryker@jboss.com
 */
public class JBoss4xEarFacetInstallListener implements IFacetedProjectListener {

	private static JBoss4xEarFacetInstallListener instance;
	public static JBoss4xEarFacetInstallListener getDefault() {
		if( instance == null )
			instance = new JBoss4xEarFacetInstallListener();
		return instance;
	}
	
	JBoss4xEarFacetInstallListener() {
		// Do nothing
	}
	
	public void handleEvent(IFacetedProjectEvent event) {
		IProjectFacetActionEvent e = (IProjectFacetActionEvent)event;
		IProjectFacet pf = e.getProjectFacet();
		if( pf.getId().equals(IWTPConstants.FACET_EAR)) {
			String rtName = e.getProject().getPrimaryRuntime().getName();
			IRuntime rt = ServerCore.findRuntime(rtName);
			if( rt != null ) {
				String type = rt.getRuntimeType().getId();
				if( type.equals(AS_42) || type.equals(EAP_43)) {
					// Launch the op to create the ear application.xml file
					IVirtualComponent vc = ComponentCore.createComponent(e.getProject().getProject());
					IDataModel model = DataModelFactory.createDataModel(new EarCreateDeploymentFilesDataModelProvider());
					model.setProperty(ICreateDeploymentFilesDataModelProperties.GENERATE_DD, vc);
					model.setProperty(ICreateDeploymentFilesDataModelProperties.TARGET_PROJECT, e.getProject().getProject());
					IDataModelOperation op = model.getDefaultOperation();
					try {
						op.execute(new NullProgressMonitor(), null);
					} catch (ExecutionException e1) {
						// Ignore
					}
				}
			}
		}
	}
}
