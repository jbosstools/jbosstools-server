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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller2;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.server.internal.ServerStatePollerType;
import org.jboss.ide.eclipse.as.core.server.internal.v7.JBoss7ManagerUtil.IServiceAware;

/**
 * @author André Dietisheim
 */
public class JBoss7ManagerServicePoller implements IServerStatePoller2 {

	public static final String POLLER_ID = "org.jboss.ide.eclipse.as.core.server.JBoss7ManagerServicePoller"; //$NON-NLS-1$
	private IServer server;
	private ServerStatePollerType type;
	private boolean expectedState;
	private IJBoss7ManagerService service;

	@Deprecated
	public void beginPolling(IServer server, boolean expectedState, PollThread pollTread) throws Exception {
	}
	
	public void beginPolling(IServer server, boolean expectedState) throws Exception {
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

	private int getManagementPort(IServer server) {
		JBoss7Server jbossServer = (JBoss7Server) server.loadAdapter(JBoss7Server.class, new NullProgressMonitor());
		if( server != null )
			return jbossServer.getManagementPort();
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
				serverState = service.getServerState(getServer().getHost(), getManagementPort(getServer()));
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
				serverState = service.getServerState(getServer().getHost(), getManagementPort(getServer()));
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
			JBoss7ServerState serverState = service.getServerState(getServer().getHost(), getManagementPort(getServer()));
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

	public IStatus getCurrentStateSynchronous(final IServer server) {
		try {
			Boolean result = JBoss7ManagerUtil.executeWithService(new IServiceAware<Boolean>() {
	
				@Override
				public Boolean execute(IJBoss7ManagerService service) throws Exception {
					JBoss7ServerState state = service.getServerState(server.getHost(), getManagementPort(server));
					return state == JBoss7ServerState.RUNNING ? IServerStatePoller.SERVER_UP : IServerStatePoller.SERVER_DOWN;
				}
			}, server);
			if( result.booleanValue()) {
				Status s = new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, 
						"A JBoss 7 Management Service on " + server.getHost() //$NON-NLS-1$
						+ ", port " + getManagementPort(server) + " has responded that the server is completely started."); //$NON-NLS-1$ //$NON-NLS-2$
				return s;
			}
			Status s = new Status(IStatus.INFO, JBossServerCorePlugin.PLUGIN_ID, 
					"A JBoss 7 Management Service on " + server.getHost() //$NON-NLS-1$
					+ ", port " + getManagementPort(server) + " has responded that the server is not completely started."); //$NON-NLS-1$ //$NON-NLS-2$
			return s;
		} catch(Exception e) {
			Status s = new Status(IStatus.INFO, JBossServerCorePlugin.PLUGIN_ID, 
					"An attempt to reach the JBoss 7 Management Service on host " + server.getHost() //$NON-NLS-1$
					+ " and port " + getManagementPort(server) + " has resulted in an exception"); //$NON-NLS-1$ //$NON-NLS-2$
			return s;
		}
	}
}
