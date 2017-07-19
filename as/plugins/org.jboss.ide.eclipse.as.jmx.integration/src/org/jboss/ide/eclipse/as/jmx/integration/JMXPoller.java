/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 

package org.jboss.ide.eclipse.as.jmx.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.CommunicationException;
import javax.naming.NamingException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller2;
import org.jboss.ide.eclipse.as.core.server.IServerStatePollerType;
import org.jboss.ide.eclipse.as.core.util.IEventCodes;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.tools.jmx.core.IJMXRunnable;

/**
 * A poller dedicated to server startup, checks via JMX
 * @author Rob rob.stryker@redhat.com
 *
 */
public class JMXPoller implements IServerStatePoller2 {

	public static final String POLLER_ID = "org.jboss.ide.eclipse.as.core.runtime.server.JMXPoller"; //$NON-NLS-1$
	public static final int JMXPOLLER_CODE = IEventCodes.JMXPOLLER_CODE;
	public static final Properties IGNORED_PROPERTIES = new Properties();
	
	public static final String REQUIRED_USER = "org.jboss.ide.eclipse.as.core.extensions.polling.jmx.REQUIRED_USER"; //$NON-NLS-1$
	public static final String REQUIRED_PASS = "org.jboss.ide.eclipse.as.core.extensions.polling.jmx.REQUIRED_PASS"; //$NON-NLS-1$
	
	public static final int STATE_STARTED = IEventCodes.STATE_STARTED;
	public static final int STATE_STOPPED = IEventCodes.STATE_STOPPED;
	public static final int STATE_TRANSITION = IEventCodes.STATE_TRANSITION;

	private int started;
	private boolean canceled, done;
	private boolean waitingForCredentials = false;
	private boolean ceFound,nnfeFound,startingFound;
					
	
	private IServer server;
	private IServerStatePollerType type;
	private PollingException pollingException = null;
	private RequiresInfoException requiresInfoException = null;
	private Properties requiredPropertiesReturned = null;

	private boolean expectedState;
	public void beginPolling(IServer server, boolean expectedState) throws PollingException {
		ceFound = nnfeFound = startingFound = canceled = done = false;
		this.server = server;
		this.expectedState = expectedState;
		launchJMXPoller();
	}


	protected JMXPollerModel createModel() {
		return new JMXPollerModel();
	}
	protected IJMXRunnable createJMXRunnable() {
		return new JMXPollerRunnable();
	}
	protected int getStateFromRunnable(IJMXRunnable runnable) {
		return ((JMXPollerRunnable)runnable).result ? STATE_STARTED : STATE_TRANSITION;
	}
	
	private static class JMXPollerRunnable implements IJMXRunnable {
		private boolean result;
		public void run(MBeanServerConnection connection) throws Exception {
			Object attInfo = connection.getAttribute(
					new ObjectName(IJBossRuntimeConstants.SYSTEM_MBEAN),
					IJBossRuntimeConstants.STARTED_METHOD);
			result = ((Boolean) attInfo).booleanValue();
		}
	}
	
	protected class PollerRunnable implements Runnable {
		public void run() {
			JMXPollerModel model = createModel();
			model.beginTransaction(server, this);

			IJMXRunnable runnable = createJMXRunnable();
			while( !done && !canceled) {
				CoreException coreCe = null;
				try {
					model.run(server, runnable);
					started = getStateFromRunnable(runnable);
				} catch(CoreException ce) {
					coreCe = ce;
				} 
				if( expectedState == IServerStatePoller.SERVER_UP)
					handleStartupLogging(coreCe, model);
				else
					handleShutdownLogging(coreCe, model);

				done = (started == STATE_STARTED && expectedState == IServerStatePoller.SERVER_UP)
						|| (started == STATE_STOPPED && expectedState == IServerStatePoller.SERVER_DOWN);
				try { 
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// Intentionally ignore
				}
			}
			model.endTransaction(server, this);
		}
		
		
		protected void handleCredentialRequest(Throwable t, JMXPollerModel runner) {
			synchronized(this) {
				if( !waitingForCredentials ) {
					waitingForCredentials = true;
					requiresInfoException = new PollingSecurityException(
							NLS.bind(Messages.securityException, t.getMessage()));
				} else {
					// we're waiting. are they back yet?
					if( requiredPropertiesReturned != null ) {
						if( requiredPropertiesReturned == IGNORED_PROPERTIES) {
							requiresInfoException = null;
							done = true;
							started = STATE_STARTED;
						} else {
							requiresInfoException = null;
							String user, pass;
							user = (String)requiredPropertiesReturned.get(REQUIRED_USER);
							pass = (String)requiredPropertiesReturned.get(REQUIRED_PASS);
							requiredPropertiesReturned = null;
							runner.setUser(user);
							runner.setPass(pass);
							waitingForCredentials = false;
						}
					}
				}
			}
		}
		protected void handleShutdownLogging(Throwable t, final JMXPollerModel runner) {
			if( t == null || t.getCause() == null )
				return;
			
			Throwable cause = t.getCause();
			if( cause instanceof SecurityException ) {
				handleCredentialRequest(cause, runner);
				return;
			}
			
			if( cause instanceof CommunicationException ) {
				// a CE exception indicates the server is down
				started = STATE_STOPPED;
				return;
			}
			
			if( cause instanceof NamingException ) {
				// a naming exception indicates the server is partially up
				started = STATE_TRANSITION;
				return;
			}
		
			if( cause instanceof IllegalArgumentException) {
				// the mbean has been removed, so server is still transitioning
				started = STATE_TRANSITION;
				return;
			}
			
			// All other exceptions are indication of a failure of some type
			if( cause != null ) {
				pollingException = new PollingException(cause.getMessage());
				done = true;
			}
		}
		
		protected void handleStartupLogging(Throwable t, final JMXPollerModel runner) {
			if( t == null && expectedState == IServerStatePoller.SERVER_UP && !startingFound ) {
				// Log that the server is still starting up (once)
				startingFound = true;
				IStatus s = new Status(IStatus.INFO, JBossServerCorePlugin.PLUGIN_ID, 
						JMXPOLLER_CODE|started, Messages.ServerStarting, null);
				log(s);
			}
			if( t == null || t.getCause() == null )
				return;
			
			Throwable cause = t.getCause();
			
			if( cause instanceof SecurityException ) {
				handleCredentialRequest(cause, runner);
				return;
			}
			
			if( cause instanceof CommunicationException ) {
				// a CE exception indicates the server is down
				started = STATE_STOPPED;
				if( !ceFound ) {
					ceFound = true;
					IStatus s = new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, JMXPOLLER_CODE|started, cause.getMessage(), cause);
					log(s);
				}
				return;
			}
			
			if( cause instanceof NamingException ) {
				// a naming exception indicates the server is partially up
				started = STATE_TRANSITION;
				if( !nnfeFound ) {
					nnfeFound = true;
					IStatus s = new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, JMXPOLLER_CODE|started, cause.getMessage(), cause);
					log(s);
				}
				return;
			}
		
			// All other exceptions are indication of a failure of some type
			if( cause != null ) {
				pollingException = new PollingException(cause.getMessage());
				done = true;
			}
		}
	}
	
	private void launchJMXPoller() {
		PollerRunnable run = new PollerRunnable();
		Thread t = new Thread(run, Messages.JMXPoller);
		t.start();
	}

	public void cancel(int type) {
		canceled = true;
	}

	public void cleanup() {
	}

	public class PollingSecurityException extends RequiresInfoException {
		private static final long serialVersionUID = 1L;
		public PollingSecurityException(String msg) {
			super(msg);
		}
	}

	public boolean getState() throws PollingException, RequiresInfoException {
		if (pollingException != null)
			throw pollingException;
		if( requiresInfoException != null )
			throw requiresInfoException;
		
		if (started == 0)
			return SERVER_DOWN;
		if (started == 1)
			return SERVER_UP;

		if (!done && !canceled)
			return SERVER_DOWN; // Not there yet.

		return SERVER_UP; // done or canceled, doesnt matter
	}

	public boolean isComplete() throws PollingException, RequiresInfoException {
		if (pollingException != null)
			throw pollingException;
		if( requiresInfoException != null )
			throw requiresInfoException;
		return done;
	}
	
	public void provideCredentials(Properties properties) {
		if( properties == null ) {
			requiredPropertiesReturned = IGNORED_PROPERTIES;
		} else
			requiredPropertiesReturned = properties;
	}

	public List<String> getRequiredProperties() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(REQUIRED_USER);
		list.add(REQUIRED_PASS);
		return list;
	}

	public IServerStatePollerType getPollerType() {
		return type;
	}

	public void setPollerType(IServerStatePollerType type) {
		this.type = type;
	}
	
	public IServer getServer() {
		return server;
	}
	
	public int getTimeoutBehavior() {
		return TIMEOUT_BEHAVIOR_SUCCEED;
	}
	
	private void log(IStatus s) {
		if( !canceled )
			JBossServerCorePlugin.log(s);		
	}

	public IStatus getCurrentStateSynchronous(IServer server) {
		JMXPollerModel model = createModel();
		model.beginTransaction(server, this);
		IJMXRunnable runnable2 = createJMXRunnable();

		try {
			model.run(server, runnable2);
			int started2 = getStateFromRunnable(runnable2);
			if( started2 == STATE_STARTED ) {
				Status s = new Status(IStatus.OK, Activator.PLUGIN_ID, 
						"JMX Poller found a running server on " + server.getHost());
				return s;
			}
		} catch(CoreException ce) {
			// No need to return the specifics of the exception. Just note we could not connect. 
		} finally {
			model.endTransaction(server, this);
		}
		Status s = new Status(IStatus.INFO, Activator.PLUGIN_ID, 
				"JMX Poller did not find a running server on " + server.getHost());
		return s;
	}
}