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
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal.v7;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.server.internal.ServerStatePollerType;

/**
 * @author André Dietisheim
 */
public class JBoss7ManagerServicePoller implements IServerStatePoller {

	public static final String POLLER_ID = "org.jboss.ide.eclipse.as.core.server.JBoss7ManagerServicePoller"; //$NON-NLS-1$
	private IServer server;
	private ServerStatePollerType type;
	private boolean expectedState;
	private IJBoss7ManagerService service;

	public void beginPolling(IServer server, boolean expectedState, PollThread pollTread) throws Exception {
		this.service = JBoss7ManagerUtil.getService(server);
		this.server = server;
		this.expectedState = expectedState;
	}

	public ServerStatePollerType getPollerType() {
		return type;
	}

	public void setPollerType(ServerStatePollerType type) {
		this.type = type;
	}

	public IServer getServer() {
		return server;
	}

	private int getManagementPort() {
		JBoss7Server server = (JBoss7Server)getServer().loadAdapter(JBoss7Server.class, new NullProgressMonitor());
		if( server != null )
			return server.getManagementPort();
		// TODO: provide this default in a single place (currently it is spread across the 
		// behavior and this poller). This port is already offered as constant in AS7Manager#MGMT_PORT
		return 9999;
	}
	
	public boolean isComplete() throws PollingException, RequiresInfoException {
		try {
			if (expectedState == SERVER_DOWN) {
				return awaitShutdown(service);
			} else {
				return awaitRunning(service);
			}
		} catch (Exception e) {
			throw new PollingException(e.getMessage());
		}
	}

	private Boolean awaitRunning(IJBoss7ManagerService service) {
		try {
			JBoss7ServerState serverState = null;
			do {
				serverState = service.getServerState(getServer().getHost(), getManagementPort());
			} while (serverState == JBoss7ServerState.STARTING);
			return serverState == JBoss7ServerState.RUNNING;
		} catch (Exception e) {
			return false;
		}
	}

	private Boolean awaitShutdown(IJBoss7ManagerService service) {
		try {
			JBoss7ServerState serverState = null;
			do {
				serverState = service.getServerState(getServer().getHost(), getManagementPort());
			} while (serverState == JBoss7ServerState.RUNNING);
			return false;
		} catch (JBoss7ManangerConnectException e) {
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean getState() throws PollingException, RequiresInfoException {
		try {
			JBoss7ServerState serverState = service.getServerState(getServer().getHost(), getManagementPort());
			return serverState == JBoss7ServerState.RUNNING
					|| serverState == JBoss7ServerState.RESTART_REQUIRED;
		} catch (Exception e) {
			throw new PollingException(e.getMessage());
		}
	}

	public void cleanup() {
		JBoss7ManagerUtil.dispose(service);
	}

	public List<String> getRequiredProperties() {
		return Collections.emptyList();
	}

	public void failureHandled(Properties properties) {
	}

	public void cancel(int type) {
	}

	public int getTimeoutBehavior() {
		return TIMEOUT_BEHAVIOR_FAIL;
	}
}
