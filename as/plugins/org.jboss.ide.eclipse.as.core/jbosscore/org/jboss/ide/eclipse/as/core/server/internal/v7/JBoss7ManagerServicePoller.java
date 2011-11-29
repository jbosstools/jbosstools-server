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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller2;
import org.jboss.ide.eclipse.as.core.server.internal.ServerStatePollerType;
import org.jboss.ide.eclipse.as.core.server.v7.management.AS7ManagementDetails;
import org.jboss.ide.eclipse.as.core.server.v7.management.IJBoss7ManagerService;
import org.jboss.ide.eclipse.as.core.server.v7.management.JBoss7ManagerUtil;
import org.jboss.ide.eclipse.as.core.server.v7.management.JBoss7ManagerUtil.IServiceAware;
import org.jboss.ide.eclipse.as.core.server.v7.management.JBoss7ManangerConnectException;
import org.jboss.ide.eclipse.as.core.server.v7.management.JBoss7ServerState;
import org.osgi.framework.InvalidSyntaxException;

/**
 * @author André Dietisheim
 */
public class JBoss7ManagerServicePoller implements IServerStatePoller2 {

	public static final String POLLER_ID = "org.jboss.ide.eclipse.as.core.server.JBoss7ManagerServicePoller"; //$NON-NLS-1$
	private IServer server;
	private AS7ManagementDetails managementDetails;
	private ServerStatePollerType type;
	private boolean expectedState;
	private IJBoss7ManagerService service;
	private boolean done = false;
	private boolean canceled = false;
	private PollingException pollingException = null;
	private RequiresInfoException requiresInfoException = null;
	private List<String> requiredProperties = null; 
	private Properties requiredPropertiesReturned = null;

	public void beginPolling(IServer server, boolean expectedState) throws PollingException {
		try {
			this.service = JBoss7ManagerUtil.getService(server);
			this.server = server;
			this.managementDetails = createManagementDetails();
			this.expectedState = expectedState;
			launchPollingThread();
		} catch(InvalidSyntaxException e) {
			throw new PollingException(NLS.bind(Messages.CouldNotBeginPolling,server.getName()), e);
		}
	}

	private AS7ManagementDetails createManagementDetails() {
		return new AS7ManagementDetails(server) {
			public String[] handleCallbacks(String[] prompts) {
				return handleAsynchCallbacks(prompts);
			}
		};
	}
	

	private String[] handleAsynchCallbacks(String[] prompts) {
		List<String> tmp = new ArrayList<String>();
		tmp.addAll(Arrays.asList(prompts));
		requiredProperties = tmp;
		requiresInfoException = new RequiresInfoException("Requires proper credentials"); //$NON-NLS-1$
		while( !done && !canceled && requiredPropertiesReturned == null ) {
			try {
				Thread.sleep(500);
			} catch(InterruptedException ie) {/* Do nothing */}
		}
		
		if( done || canceled )
			return new String[0];
		String[] retPrompts = new String[prompts.length];
		for( int i = 0; i < retPrompts.length; i++) {
			retPrompts[i] = (String)requiredPropertiesReturned.get(prompts[i]);
		}
		return retPrompts;
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
	
	public boolean isComplete() throws PollingException, RequiresInfoException {
		if (pollingException != null)
			throw pollingException;
		if( requiresInfoException != null )
			throw requiresInfoException;
		return done;
	}

	public boolean getState() throws PollingException, RequiresInfoException {
		if( done ) 
			return expectedState;
		return !expectedState;
	}
	
	public void launchPollingThread() {
		new Thread() {
			public void run() {
				runLoop();
			}
		}.start();
	}
	
	public void runLoop() {
		try {
			while( !done && !canceled )  {
				if (expectedState == SERVER_DOWN) {
					done = checkShutdown(service);
				} else {
					done = checkRunning(service);
				}
				if( !done ) {
					try {
						Thread.sleep(300);
					} catch(InterruptedException ie) {
						// Ignore
					}
				}
			}
		} catch (Exception e) {
			pollingException = new PollingException(e.getMessage());
		}
	}

	private boolean checkRunning(IJBoss7ManagerService service) {
		try {
			JBoss7ServerState serverState = null;
			serverState = service.getServerState(managementDetails);
			return serverState == JBoss7ServerState.RUNNING;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean checkShutdown(IJBoss7ManagerService service) {
		try {
			service.getServerState(managementDetails);
			return false;
		} catch (JBoss7ManangerConnectException e) {
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void cleanup() {
		JBoss7ManagerUtil.dispose(service);
	}

	public List<String> getRequiredProperties() {
		return requiredProperties == null ? new ArrayList<String>() : requiredProperties;
	}

	public void provideCredentials(Properties properties) {
		requiredPropertiesReturned = properties;
	}

	public void cancel(int type) {
		canceled = true;
	}

	public int getTimeoutBehavior() {
		return TIMEOUT_BEHAVIOR_FAIL;
	}

	
	/* Code related to synchronous state checking */
	private boolean callbacksCalled = false;
	public IStatus getCurrentStateSynchronous(final IServer server) {
		try {
			Boolean result = JBoss7ManagerUtil.executeWithService(new IServiceAware<Boolean>() {
				@Override
				public Boolean execute(IJBoss7ManagerService service) throws Exception {
					try {
						JBoss7ServerState state = service.getServerState(createSynchronousManagementDetails(server));
						return state == JBoss7ServerState.RUNNING ? IServerStatePoller.SERVER_UP : IServerStatePoller.SERVER_DOWN;
					} catch(Exception e) {
						/* Should be JBoss7ManagerException, but cannot compile against since it is in jboss-as jars */
						return callbacksCalled ? IServerStatePoller.SERVER_UP : IServerStatePoller.SERVER_DOWN;
					}
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
					+ " and port " + getManagementPort(server) + " has resulted in an exception", e); //$NON-NLS-1$ //$NON-NLS-2$
			return s;
		}
	}
	
	private AS7ManagementDetails createSynchronousManagementDetails(IServer server) {
		return new AS7ManagementDetails(server) {
			public String[] handleCallbacks(String[] prompts) throws UnsupportedOperationException {
				// No need to do verification here... simply know that a server responded requesting callbacks
				// This means a server is up already
				callbacksCalled = true;
				throw new UnsupportedOperationException();
			}
		};
	}
	
	private int getManagementPort(IServer server) {
		if( server != null ) {
			JBoss7Server jbossServer = (JBoss7Server) server.loadAdapter(JBoss7Server.class, new NullProgressMonitor());
			return jbossServer.getManagementPort();
		}
		return IJBoss7ManagerService.MGMT_PORT;
	}

}
