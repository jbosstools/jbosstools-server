/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 * TODO: Logging and Progress Monitors
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.rse.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.AbstractJBossBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.rse.core.RSEHostShellModel.ServerShellModel;

public abstract class AbstractRSEBehaviourDelegate extends AbstractJBossBehaviourDelegate {
	
	@Override
	public String getBehaviourTypeId() {
		return RSEPublishMethod.RSE_ID;
	}
	
	@Override
	public void stopImpl(boolean force) {
		// If force, or if the server is already started (force a one-time synchronous poll)
		if( force || !PollThreadUtils.isServerStarted(actualBehavior).isOK()) {
			forceStop();
			return;
		}

		setServerStopping();
		IStatus shutdownStatus = gracefullStop();
		if (!shutdownStatus.isOK()) {
			// The shutdown failed. This indicates a bad command or nonfunctional shutdown command
			if(getServer().getServerState() == IServer.STATE_STOPPED)
				return; // The poller already changed state to stopped
			
			if( getPollThread() != null )
				getPollThread().cancel();
			setServerStarted();
		} // else wait for the poller to set the proper state
	}

	private String pid;
	public void setPid(String pid) {
		this.pid = pid;
	}
	
	@Override
	protected synchronized void forceStop() {
		if( getServer().getServerState() == IServer.STATE_STOPPED)
			return;
		String localPid = pid;
		pid = null;
		setServerStopped();
		if( localPid != null ) {
			try {
				ServerShellModel model = RSEHostShellModel.getInstance().getModel(getServer());
				String cmd = "kill -9 " + localPid;
				model.executeRemoteCommand("/", cmd, new String[]{}, new NullProgressMonitor(), 2000, true);
			} catch(CoreException ce ) {
				RSECorePlugin.getLog().log(new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, "Unable to terminate remote process " + pid, ce));
			}
		}
	}

	protected abstract String getShutdownCommand(IServer server) throws CoreException;
	
	@Override
	public void onServerStarting() {
		pollServer(IServerStatePoller.SERVER_UP);
	}
	
	@Override
	public void onServerStopping() {
		pollServer(IServerStatePoller.SERVER_DOWN);
	}
	@Override
	protected void setServerStopped() {
		super.setServerStopped();
		pid = null;
	}
	
}
