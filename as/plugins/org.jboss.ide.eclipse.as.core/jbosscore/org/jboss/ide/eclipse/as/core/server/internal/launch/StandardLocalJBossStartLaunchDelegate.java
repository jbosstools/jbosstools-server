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
package org.jboss.ide.eclipse.as.core.server.internal.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.IPollResultListener;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.tools.as.core.server.controllable.IDeployableServerBehaviorProperties;

/**
 * This is a launch configuration delegate for use with local jboss servers. 
 * It will launch the configuration, update server state, and 
 * register a process termination listener. 
 * 
 * We also kick off the polling mechanism from here as part of the launch
 */
public class StandardLocalJBossStartLaunchDelegate extends
		AbstractJBossStartLaunchConfiguration {

	public String[] getJavaLibraryPath(ILaunchConfiguration configuration) throws CoreException {
		return new String[] {};
	}
	
	protected void beforeVMRunner(ILaunchConfiguration configuration,
			String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(configuration);
		if( beh != null ) {
			((ControllableServerBehavior)beh).setRunMode(mode);
			((ControllableServerBehavior)beh).setServerStarting();
		}
	}
	
	protected void afterVMRunner(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IProcess[] processes = launch.getProcesses();
		IServer server = ServerUtil.getServer(configuration);
		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(configuration);
		if( processes != null && processes.length >= 1 && processes[0] != null ) {
			ProcessTerminatedDebugListener debug = new ProcessTerminatedDebugListener(ServerUtil.getServer(configuration), processes[0]);
			if( beh != null ) {
				beh.putSharedData(IDeployableServerBehaviorProperties.PROCESS, processes[0]);
				beh.putSharedData(IDeployableServerBehaviorProperties.DEBUG_LISTENER, debug);
			}
			DebugPlugin.getDefault().addDebugEventListener(debug);
		} 
		
		// Initiate Polling!
		PollThreadUtils.pollServer(server, IServerStatePoller.SERVER_UP);
	}
}
