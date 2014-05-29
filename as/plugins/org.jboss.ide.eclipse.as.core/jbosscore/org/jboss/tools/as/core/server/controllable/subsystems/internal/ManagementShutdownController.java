/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.as.core.server.controllable.subsystems.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.v7.management.AS7ManagementDetails;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerUtil;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManangerException;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ILaunchServerController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerShutdownController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IShutdownControllerDelegate;
import org.jboss.tools.as.core.server.controllable.IDeployableServerBehaviorProperties;

public class ManagementShutdownController extends LocalLegacyShutdownController {

	private IJBoss7ManagerService service;
	protected IJBoss7ManagerService getService() throws JBoss7ManangerException {
		if (service == null) {
			this.service = JBoss7ManagerUtil.getService(getServer());
		}
		return service;
	}
	

	protected boolean getRequiresForce() {
		Object o = getControllableBehavior().getSharedData(IDeployableServerBehaviorProperties.NEXT_STOP_REQUIRES_FORCE);
		return o == null ? false : ((Boolean)o).booleanValue();
	}
	
	protected boolean shouldUseForce() {
		return getRequiresForce();
	}

	protected void setNextStopRequiresForce(boolean val) {
		getControllableBehavior().putSharedData(IDeployableServerBehaviorProperties.NEXT_STOP_REQUIRES_FORCE, val);
	}
	
	protected void forceStop() {
		try {
			ILaunchServerController c = (ILaunchServerController)getControllableBehavior().getController(ILaunchServerController.SYSTEM_ID);
			if( c instanceof IShutdownControllerDelegate) {
				IServerShutdownController sc = ((IShutdownControllerDelegate) c).getShutdownController();
				sc.stop(true);
			}
		} catch(CoreException ce) {
			IStatus status = new Status(IStatus.ERROR,
					JBossServerCorePlugin.PLUGIN_ID, IEventCodes.BEHAVIOR_FORCE_STOP_FAILED, 
					Messages.FORCE_TERMINATE_FAILED, ce);
			ServerLogger.getDefault().log(getServer(), status);
		}
		getControllableBehavior().putSharedData(IDeployableServerBehaviorProperties.NEXT_STOP_REQUIRES_FORCE, new Boolean(false));
		// Management shutdown has no clear way to force terminate
		((ControllableServerBehavior)getControllableBehavior()).setServerStopped();
	}
	
	@Override
	protected IStatus gracefullStop() {
		IServer server = getServer();
		try {
			AS7ManagementDetails det = new AS7ManagementDetails(getServer());
			getService().stop(det);
			PollThreadUtils.pollServer(getServer(), IServerStatePoller.SERVER_DOWN);
			setNextStopRequiresForce(false);
			return Status.OK_STATUS;
		} catch (Exception e) {
			setNextStopRequiresForce(true);
			return new Status(
					IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
					NLS.bind(Messages.JBoss7ServerBehavior_could_not_stop, server.getName()), e);
		}
	}
}
