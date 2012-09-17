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
package org.jboss.ide.eclipse.as.core.extensions.polling;

import java.util.List;
import java.util.Properties;

import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IProcessProvider;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.IServerStatePollerType;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;

/**
 * Essentially does nothing because the process already has a listener
 * on it that sets the server state to stopped once the process dies. 
 * 
 * It's here to make the shutdown include no polling though, thus
 * more efficient.
 * @author rob
 *
 */
public class ProcessTerminatedPoller implements IServerStatePoller {

	public static final String POLLER_ID = "org.jboss.ide.eclipse.as.core.runtime.server.processTerminatedPoller"; //$NON-NLS-1$
	
	private IServerStatePollerType type;
	private IServer server;
	
	public void beginPolling(IServer server, boolean expectedState) {
		this.server = server;
	}

	public IServer getServer() {
		return server;
	}

	public void cancel(int type) {
	}

	public void cleanup() {
	}

	public boolean getState() throws PollingException {
		return !isComplete();
	}

	public boolean isComplete() throws PollingException {
		if( server.getServerState() == IServer.STATE_STOPPED)
			return true;
		DelegatingServerBehavior beh = (DelegatingServerBehavior) server.getAdapter(DelegatingServerBehavior.class);
		if(beh != null && beh.getDelegate() != null && beh.getDelegate() instanceof IProcessProvider) {
			IProcess p = ((IProcessProvider)beh.getDelegate()).getProcess();
			if( p == null || p.isTerminated())
				return true;
		}
		return false;
	}

	public void provideCredentials(Properties properties) {
	}

	public List<String> getRequiredProperties() {
		return null;
	}
	
	public IServerStatePollerType getPollerType() {
		return type;
	}

	public void setPollerType(IServerStatePollerType type) {
		this.type = type;
	}

	public int getTimeoutBehavior() {
		return TIMEOUT_BEHAVIOR_IGNORE;
	}


}
