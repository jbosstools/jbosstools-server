/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal.v7;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;
import org.jboss.ide.eclipse.as.core.server.v7.management.AS7ManagementDetails;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7DeploymentState;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerUtil;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManangerException;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IModuleStateController;
import org.jboss.tools.as.core.server.controllable.systems.IModuleDeployPathController;

public class JBoss7ModuleStateVerifier extends AbstractJBoss7ModuleStateVerifier implements IModuleStateController, IServerModuleStateVerifier {


	protected int getRootModuleState(IServer server, IModule root,
			String deploymentName, IProgressMonitor monitor) throws Exception {
		IJBoss7ManagerService service = JBoss7ManagerUtil.getService(server);
		AS7ManagementDetails details = new AS7ManagementDetails(server);
		int ret = IServer.STATE_UNKNOWN;
		if (service.isRunning(details)) { // to avoid asking while server is starting up.
			JBoss7DeploymentState state = service.getDeploymentState(
					details, deploymentName);
			if( state == JBoss7DeploymentState.STARTED)
				return IServer.STATE_STARTED;
			if( state == JBoss7DeploymentState.STOPPED)
				return IServer.STATE_STOPPED;
		}
		return ret;
	}
	
	public int changeModuleStateTo(IModule[] module, int state, IProgressMonitor monitor) throws CoreException {
		AS7ManagementDetails details = new AS7ManagementDetails(getServer());
		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(getServer());
		IModuleDeployPathController controller = (IModuleDeployPathController)beh.getController(IModuleDeployPathController.SYSTEM_ID);
		String deploymentName =  controller.getOutputName(module);

		try {
			IJBoss7ManagerService service = JBoss7ManagerUtil.getService(getServer());
			if( service == null ) {
				throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Management service for server not found.")); //$NON-NLS-1$
			} else if(state == IServer.STATE_STARTED ) {
				service.deploySync(details, deploymentName, null, false, monitor);
			} else if (state == IServer.STATE_STOPPED) {
				service.undeploySync(details, deploymentName, false, monitor);
			} else {
				throw new IllegalArgumentException("Only states IServer.STATE_STARTED and IServer.STATE_STOPPED are supported"); //$NON-NLS-1$
			}
			return state;
		} catch(JBoss7ManangerException j7me) {
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, j7me.getMessage(),j7me));
		}
	}
}
