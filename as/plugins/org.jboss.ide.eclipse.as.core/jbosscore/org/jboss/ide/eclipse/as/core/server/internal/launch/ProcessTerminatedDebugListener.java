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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.tools.as.core.server.controllable.IDeployableServerBehaviorProperties;

/**
 * A debug listener for use with servers who's behavior is of type {@link IControllableServerBehavior}
 * This listener will set the server adapter to 'stopped' if the process is suddenly terminated. 
 */
public class ProcessTerminatedDebugListener implements IDebugEventSetListener {
	private IProcess myProcess;
	private IServer server;
	
	/**
	 * Create a process-terminated debug listener for the given server and process combination
	 * @param server  a non-null server
	 * @param process a non-null process
	 */
	public ProcessTerminatedDebugListener(IServer server, IProcess process) {
		this.myProcess = process;
		this.server = server;
	}
	
	public void handleDebugEvents(DebugEvent[] events) {
		if (events != null) {
			int size = events.length;
			for (int i = 0; i < size; i++) {
				if (myProcess != null && myProcess.equals(events[i].getSource()) && events[i].getKind() == DebugEvent.TERMINATE) {
					handleProcessTerminatedEvent(this);
				}
			}
		}
	}
	
	private void handleProcessTerminatedEvent(ProcessTerminatedDebugListener listener) {
		synchronized(this) {
			// If there's a new process that's not equal to myProcess, 
			// then the server is already starting again
			if( listener.myProcess != null && !listener.myProcess.equals(myProcess)) 
				return;
			if( server.getServerState() != IServer.STATE_STOPPED) {
				Object pt = ((ControllableServerBehavior)getServerBehavior(server)).getSharedData(IDeployableServerBehaviorProperties.POLL_THREAD);
				if( pt != null ) {
					PollThreadUtils.cancelPolling(null, (PollThread)pt);
				}
				addProcessTerminatedEvent();
			}
			DebugPlugin.getDefault().removeDebugEventListener(listener);
			// TODO handle next stop requires force stuff
		}
		
		if( server.getServerState() != IServer.STATE_STOPPED) {
			((ControllableServerBehavior)getServerBehavior(server)).setServerStopped();
		}
	}
	
	protected void addProcessTerminatedEvent() {
		IStatus status = new Status(IStatus.INFO,
				JBossServerCorePlugin.PLUGIN_ID, IEventCodes.BEHAVIOR_PROCESS_TERMINATED, 
				Messages.TERMINATED, null);
		ServerLogger.getDefault().log(server, status);
	}
	

	protected static IControllableServerBehavior getServerBehavior(ILaunchConfiguration configuration) throws CoreException {
		return getServerBehavior(ServerUtil.getServer(configuration));
	}
	
	protected static IControllableServerBehavior getServerBehavior(IServer server) {
		IControllableServerBehavior behavior = (IControllableServerBehavior) server.getAdapter(IControllableServerBehavior.class);
		return behavior;
	}
}
