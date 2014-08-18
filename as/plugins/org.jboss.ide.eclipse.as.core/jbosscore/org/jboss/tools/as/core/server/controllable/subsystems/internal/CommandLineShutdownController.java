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

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.server.launch.CommandLineLaunchConfigProperties;
import org.jboss.ide.eclipse.as.core.server.launch.LocalCommandLineRunner;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.ThreadUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.tools.as.core.server.controllable.IDeployableServerBehaviorProperties;
import org.jboss.tools.as.core.server.controllable.systems.ICommandLineShutdownController;

public class CommandLineShutdownController extends AbstractSubsystemController implements ICommandLineShutdownController {

	@Override
	public IStatus canStop() {
		// Can always try to stop. 
		return Status.OK_STATUS;
	}

	@Override
	public void stop(boolean force) {
		
		boolean ignoreLaunch = LaunchCommandPreferences.isIgnoreLaunchCommand(getServer());
		
		if( ignoreLaunch ) {
			((ControllableServerBehavior)getControllableBehavior()).setServerStopping();
			((ControllableServerBehavior)getControllableBehavior()).setServerStopped();
			return;
		}
		stopImpl(force);
	}
	
	protected boolean nextStopRequiresForce() {
		Object o = ((ControllableServerBehavior)getControllableBehavior()).getSharedData(IDeployableServerBehaviorProperties.NEXT_STOP_REQUIRES_FORCE);
		return Boolean.TRUE.equals(o);
	}
	
	public void stopImpl(boolean force) {
		// If force, or if the server is already started (force a one-time synchronous poll)
		if( force || nextStopRequiresForce() || !isServerStarted()) {
			forceStop();
			return;
		}

		((ControllableServerBehavior)getControllableBehavior()).setServerStopping();
		
		beforeCommandExecuted();

		IStatus shutdownStatus = gracefullStop();
		if (!shutdownStatus.isOK()) {
			handleShutdownFailed();
			return;
		}
		
		afterCommandExecuted();
	}
	
	protected void afterCommandExecuted() {
		// Dumb server takes this as a success
		((ControllableServerBehavior)getControllableBehavior()).setServerStopped();
	}


	protected IStatus gracefullStop() {
		try {
			String command = getShutdownCommand(getServer());
			if( command.trim().length() == 0 ) {
				throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Unable to stop server: command to run is empty", null)); //$NON-NLS-1$
			}
			return executeShutdownCommand(command);
		} catch(CoreException ce) {
			Status error = new Status(
					IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
					MessageFormat.format("Could not stop server {0}", getServer().getName()),  //$NON-NLS-1$
					ce); 
			ServerLogger.getDefault().log(getServer(), error);
			return  error;
		}
	}

	
	protected IStatus executeShutdownCommand(String shutdownCommand) throws CoreException {
		IProcess stopProcess = new LocalCommandLineRunner().launchCommand(shutdownCommand, new NullProgressMonitor());
		ThreadUtils.sleepWhileRunning(stopProcess);
		if (stopProcess.getExitValue() == 0) {
			((ControllableServerBehavior)getControllableBehavior()).setServerStopped();
		} else {
			// Stop process exit value was NOT zero, so the stop process failed
			((ControllableServerBehavior)getControllableBehavior()).setServerStarted();
		}
		return Status.OK_STATUS;
	}

	protected synchronized void forceStop() {
		ControllableServerBehavior beh = ((ControllableServerBehavior)getControllableBehavior());
		beh.putSharedData(IDeployableServerBehaviorProperties.NEXT_STOP_REQUIRES_FORCE, false);
		if (beh != null ) {
			Object o = beh.getSharedData(IDeployableServerBehaviorProperties.PROCESS);
			if( o instanceof IProcess ) {
				IProcess p = ((IProcess)o);
				if( p.canTerminate()) {
					try {
						p.terminate();
					} catch(DebugException de) {
						JBossServerCorePlugin.log(de);
					}
				}
			}
			beh.setServerStopped();
		}
	}
	
	protected String getShutdownCommand(IServer server) throws CoreException {
		ILaunchConfiguration config = getServer().getLaunchConfiguration(false, new NullProgressMonitor());
		return new CommandLineLaunchConfigProperties().getShutdownCommand(config, getDefaultShutdownCommand(server));
	}

	protected String getDefaultShutdownCommand(IServer server) {
		return ""; // dumb impl cannot know //$NON-NLS-1$
	}
	
	protected boolean isServerStarted() {
		return getServer().getServerState() == IServer.STATE_STARTED; // Dumb impl cannot know, so we just check the server state
	}
	
	protected void beforeCommandExecuted() {
		// Nothing to do here for deploy-only server
	}
	
	protected void handleShutdownFailed() {
		// The shutdown failed. This indicates a bad command or nonfunctional shutdown command
		if(getServer().getServerState() == IServer.STATE_STOPPED)
			// Something already set server to stopped, and we should trust them
			return; 
		// Otherwise, we agree that it failed. 
		((ControllableServerBehavior)getControllableBehavior()).setServerStarted();
		((ControllableServerBehavior)getControllableBehavior()).putSharedData(IDeployableServerBehaviorProperties.NEXT_STOP_REQUIRES_FORCE, true);
	}
}
