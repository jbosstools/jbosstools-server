/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 

package org.jboss.ide.eclipse.as.core.extensions.polling;

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
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXClassLoaderRepository;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXSafeRunner;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.server.internal.ServerStatePollerType;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.tools.jmx.core.IJMXRunnable;

/**
 * A poller dedicated to server startup, checks via JMX
 * @author Rob rob.stryker@redhat.com
 *
 */
public class JMXPoller implements IServerStatePoller {

	public static final String POLLER_ID = "org.jboss.ide.eclipse.as.core.runtime.server.JMXPoller"; //$NON-NLS-1$
	public static final int JMXPOLLER_CODE = IEventCodes.JMXPOLLER_CODE;
	public static final Properties IGNORED_PROPERTIES = new Properties();
	
	public static final String REQUIRED_USER = "org.jboss.ide.eclipse.as.core.extensions.polling.jmx.REQUIRED_USER"; //$NON-NLS-1$
	public static final String REQUIRED_PASS = "org.jboss.ide.eclipse.as.core.extensions.polling.jmx.REQUIRED_PASS"; //$NON-NLS-1$
	
	public static final int STATE_STARTED = 1;
	public static final int STATE_STOPPED = 0;
	public static final int STATE_TRANSITION = 2;

	private int started;
	private boolean canceled, done;
	private boolean waitingForCredentials = false;
	private boolean ceFound,nnfeFound,startingFound;
					
	
	private IServer server;
	private ServerStatePollerType type;
	private PollingException pollingException = null;
	private RequiresInfoException requiresInfoException = null;
	private Properties requiredPropertiesReturned = null;

	private JMXPollerRunnable runnable;
	private JMXSafeRunner runner;
	
	public void beginPolling(IServer server, boolean expectedState,
			PollThread pt) {
		ceFound = nnfeFound = startingFound = canceled = done = false;
		this.server = server;
		launchJMXPoller();
	}

	private class JMXPollerRunnable implements IJMXRunnable {
		public void run(MBeanServerConnection connection) throws Exception {
			Object attInfo = connection.getAttribute(
					new ObjectName(IJBossRuntimeConstants.SYSTEM_MBEAN),
					IJBossRuntimeConstants.STARTED_METHOD);
			boolean b = ((Boolean) attInfo).booleanValue();
			started = b ? STATE_STARTED : STATE_TRANSITION;
			done = b;
			if( !startingFound ) {
				startingFound = true;
				IStatus s = new Status(IStatus.INFO, JBossServerCorePlugin.PLUGIN_ID, 
						JMXPOLLER_CODE|started, Messages.ServerStarting, null);
				log(s);
			}
		}
	}
	
	private class PollerRunnable implements Runnable {
		public void run() {
			JMXClassLoaderRepository.getDefault().addConcerned(server, this);
			runnable = new JMXPollerRunnable();
			runner = new JMXSafeRunner(server);
			while( !done && !canceled) {
				try {
					runner.run(runnable);
				} catch(CoreException ce) {
					handleException(ce.getCause());
				} 

				try { Thread.sleep(500);} 
				catch (InterruptedException e) {}
			}
			JMXClassLoaderRepository.getDefault().removeConcerned(server, this);
		}
		
		protected void handleException(Throwable t) {
			if( t instanceof SecurityException ) {
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
				return;
			}
			
			if( t instanceof CommunicationException ) {
				started = STATE_STOPPED;
				if( !ceFound ) {
					ceFound = true;
					IStatus s = new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, JMXPOLLER_CODE|started, t.getMessage(), t);
					log(s);
				}
				return;
			}
			
			if( t instanceof NamingException ) {
				started = STATE_STOPPED;
				if( !nnfeFound ) {
					nnfeFound = true;
					IStatus s = new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, JMXPOLLER_CODE|started, t.getMessage(), t);
					log(s);
				}
				return;
			}
		
			if( t != null ) {
				pollingException = new PollingException(t.getMessage());
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
	
	public void failureHandled(Properties properties) {
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

	public ServerStatePollerType getPollerType() {
		return type;
	}

	public void setPollerType(ServerStatePollerType type) {
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
			ServerLogger.getDefault().log(server,s);		
	}
}
