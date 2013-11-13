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

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.rse.core.RSECorePlugin;
import org.jboss.ide.eclipse.as.rse.core.RSEHostShellModel;
import org.jboss.ide.eclipse.as.rse.core.RSEHostShellModel.ServerShellModel;
import org.jboss.ide.eclipse.as.rse.core.RSELaunchConfigProperties;
import org.jboss.ide.eclipse.as.rse.core.RSEUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerShutdownController;
import org.jboss.tools.as.core.server.controllable.IDeployableServerBehaviorProperties;

public class RSECommandLineShutdownController extends AbstractSubsystemController
		implements IServerShutdownController {

	@Override
	public IStatus canStop() {
		return Status.OK_STATUS;
	}

	@Override
	public void stop(boolean force) {
		if( LaunchCommandPreferences.isIgnoreLaunchCommand(getServer())) {
			((ControllableServerBehavior)getControllableBehavior()).setServerStopped();
			return;
		}
		// We remove the added scanners before shutting down
		removeScanners();
		stopImpl(force);
	}

	protected void removeScanners() {
		boolean removeScanners = getServer().getAttribute(IJBossToolingConstants.PROPERTY_REMOVE_DEPLOYMENT_SCANNERS, true);
		if( removeScanners ) {
			JBossExtendedProperties properties = (JBossExtendedProperties)getServer().loadAdapter(JBossExtendedProperties.class, null);
			if( properties != null ) {
				properties.getDeploymentScannerModifier().removeAddedDeploymentScanners(getServer());
			}		
		}
	}
	
	public void stopImpl(boolean force) {
		// If force, or if the server is already started (force a one-time synchronous poll)
		if( force || !PollThreadUtils.isServerStarted(getServer()).isOK()) {
			forceStop();
			return;
		}

		((ControllableServerBehavior)getControllableBehavior()).setServerStopping();
		IStatus shutdownStatus = gracefullStop();
		if (!shutdownStatus.isOK()) {
			// The shutdown failed. This indicates a bad command or nonfunctional shutdown command
			if(getServer().getServerState() == IServer.STATE_STOPPED)
				return; // The poller already changed state to stopped
			
			if( getPollThread() != null ) {
				getPollThread().cancel();
				clearPollThread();
			}
			((ControllableServerBehavior)getControllableBehavior()).setServerStarted();
		} // else wait for the poller to set the proper state
	}
	


	protected PollThread getPollThread() {
		return (PollThread)getControllableBehavior().getSharedData(IDeployableServerBehaviorProperties.POLL_THREAD);
	}
	
	protected void clearPollThread() {
		getControllableBehavior().putSharedData(IDeployableServerBehaviorProperties.POLL_THREAD, null);
	}
	
	protected String getProcessId() {
		return (String)getControllableBehavior().getSharedData(IDeployableServerBehaviorProperties.PROCESS_ID);
	}
	
	protected void clearProcessId() {
		getControllableBehavior().putSharedData(IDeployableServerBehaviorProperties.PROCESS_ID, null);
	}
	
	
	protected IStatus gracefullStop() {
		try {
			return executeShutdownCommand(getShutdownCommand(getServer()));
		} catch(CoreException ce) {
			ServerLogger.getDefault().log(getServer(), ce.getStatus());
			return new Status(
					IStatus.ERROR, RSECorePlugin.PLUGIN_ID,
					MessageFormat.format("Could not stop server {0}", getServer().getName()), 
					ce);
		}
	}
	

	protected String getShutdownCommand(IServer server) throws CoreException {
		JBossServer jbs = (JBossServer)ServerConverter.getJBossServer(getServer());
		String defaultArgs = jbs.getExtendedProperties().getDefaultLaunchArguments().getDefaultStopArgs();
		ILaunchConfiguration config = getServer().getLaunchConfiguration(false, new NullProgressMonitor());
		return RSELaunchConfigProperties.getShutdownCommand(config, defaultArgs);
	}
	

	protected IStatus executeShutdownCommand(String shutdownCommand) throws CoreException {
		ServerShellModel model = RSEHostShellModel.getInstance().getModel(getServer());
		int ret = model.executeRemoteCommandGetStatus("/", shutdownCommand, new String[]{}, new NullProgressMonitor(), 10000, true);
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
				RSECorePlugin.getLog().log(new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, "Unable to terminate remote process " + localPid, ce));
			}
		}
		clearProcessId();
	}

}
