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
package org.jboss.ide.eclipse.as.rse.core.subsystems;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.rse.core.RSECorePlugin;
import org.jboss.ide.eclipse.as.rse.core.RSEHostShellModel;
import org.jboss.ide.eclipse.as.rse.core.RSEHostShellModel.ServerShellModel;
import org.jboss.ide.eclipse.as.rse.core.RSEUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerShutdownController;
import org.jboss.tools.as.core.server.controllable.IDeployableServerBehaviorProperties;
import org.jboss.tools.as.core.server.controllable.subsystems.internal.CommandLineShutdownController;

public class RSECommandLineShutdownController extends CommandLineShutdownController
		implements IServerShutdownController {
	
	@Override
	protected IStatus executeShutdownCommand(String shutdownCommand) throws CoreException {
		ServerShellModel model = RSEHostShellModel.getInstance().getModel(getServer());
		int ret = model.executeRemoteCommandGetStatus("/", shutdownCommand, null, new NullProgressMonitor(), 10000, true);
		if( ret == -1 || ret == 0 ) {
			// either a shutdown success or a failure on the part of the tools to accurately discover the exit code
			// proceed as normal
			IHostShell shell = model.getStartupShell();
			if( RSEUtils.isActive(shell)) {
				shell.writeToShell("exit");
			}
			return Status.OK_STATUS;
		}
		return new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, 
				NLS.bind("Remote shutdown command failed with status {0}", ret));
	}
	
	protected synchronized void forceStop() {
		if( getServer().getServerState() == IServer.STATE_STOPPED)
			return;
		String localPid = getProcessId();
		((ControllableServerBehavior)getControllableBehavior()).setServerStopped();
		if( localPid != null ) {
			try {
				ServerShellModel model = RSEHostShellModel.getInstance().getModel(getServer());
				String cmd = "kill -9 " + localPid;
				model.executeRemoteCommand("/", cmd, new String[]{}, new NullProgressMonitor(), 2000, true);
			} catch(CoreException ce ) {
				RSECorePlugin.pluginLog().logStatus(new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, "Unable to terminate remote process " + localPid, ce));
			}
		}
		clearProcessId();
		((ControllableServerBehavior)getControllableBehavior()).putSharedData(IDeployableServerBehaviorProperties.NEXT_STOP_REQUIRES_FORCE, false);
	}


	protected String getProcessId() {
		return (String)getControllableBehavior().getSharedData(IDeployableServerBehaviorProperties.PROCESS_ID);
	}
	
	protected void clearProcessId() {
		getControllableBehavior().putSharedData(IDeployableServerBehaviorProperties.PROCESS_ID, null);
	}
}
