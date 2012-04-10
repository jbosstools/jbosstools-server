/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal.v7;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.polling.WebPortPoller;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.LocalJBossBehaviorDelegate;
import org.jboss.ide.eclipse.as.core.server.v7.management.AS7ManagementDetails;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.management.core.JBoss7ManagerUtil;
import org.osgi.framework.InvalidSyntaxException;

public class LocalJBoss7BehaviorDelegate extends LocalJBossBehaviorDelegate {

	private IJBoss7ManagerService service;
	private boolean previousStopFailed = false;
	
	public IStatus canChangeState(String launchMode) {
		return Status.OK_STATUS;
	}

	@Override
	public void stopImpl(boolean force) {
		if (force || previousStopFailed) {
			forceStop();
		} else {
			setServerStopping();
			IStatus result = gracefullStop();
			if (!result.isOK()) {
				previousStopFailed = true;
				setServerStarted();
			} else {
				forceStop();
			}
		}
	}
	
	@Override
	protected synchronized void forceStop() {
		super.forceStop();
		previousStopFailed = false;
	}

	@Override
	protected IStatus gracefullStop() {
		IServer server = getServer();
		try {
			getService().stop(new AS7ManagementDetails(getServer()));
			return Status.OK_STATUS;
		} catch (Exception e) {
			return new Status(
					IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
					NLS.bind(Messages.JBoss7ServerBehavior_could_not_stop, server.getName()), e);
		}
	}

	@Override
	protected void pollServer(final boolean expectedState) {
		// IF shutting down a process started OUTSIDE of eclipse, force use the web poller, 
		// since there's no process watch for shutdowns
		if( !expectedState 
				&& process == null ) {
			IServerStatePoller poller = PollThreadUtils.getPoller(expectedState, getServer());
			poller = PollThreadUtils.getPoller(WebPortPoller.WEB_POLLER_ID);
			pollServer(expectedState, poller);
		} else {
			super.pollServer(expectedState);
		}
	}
	
	@Override
	protected boolean shouldSuspendScanner() {
		return false;
	}


	@Override
	public void dispose() {
		JBoss7ManagerUtil.dispose(service);
		this.service = null;
	}

	protected IJBoss7ManagerService getService() throws InvalidSyntaxException {
		if (service == null) {
			this.service = JBoss7ManagerUtil.getService(getServer());
		}
		return service;
	}
}
