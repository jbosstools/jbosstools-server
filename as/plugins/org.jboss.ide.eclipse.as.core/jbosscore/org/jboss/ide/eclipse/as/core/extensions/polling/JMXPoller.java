/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXClassLoaderRepository;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXSafeRunner;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.server.internal.ServerStatePollerType;
import org.jboss.tools.jmx.core.IJMXRunnable;

/**
 * A poller dedicated to server startup, checks via JMX
 * @author Rob rob.stryker@redhat.com
 *
 */
public class JMXPoller implements IServerStatePoller {

	public static final String POLLER_ID = "org.jboss.ide.eclipse.as.core.runtime.server.JMXPoller";

	public static final int CODE = 2 << 16;
	public static final int JMXPOLLER_CODE = POLLING_CODE | CODE; 
	
	public static final String REQUIRED_USER = "org.jboss.ide.eclipse.as.core.extensions.polling.jmx.REQUIRED_USER";
	public static final String REQUIRED_PASS = "org.jboss.ide.eclipse.as.core.extensions.polling.jmx.REQUIRED_PASS";
	
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
					new ObjectName("jboss.system:type=Server"),
					"Started");
			boolean b = ((Boolean) attInfo).booleanValue();
			started = b ? STATE_STARTED : STATE_TRANSITION;
			done = b;
			if( !startingFound ) {
				startingFound = true;
				IStatus s = new Status(IStatus.INFO, JBossServerCorePlugin.PLUGIN_ID, CODE|started, "Server is starting", null);
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
								"Security Exception: " + t.getMessage());
					} else {
						// we're waiting. are they back yet?
						if( requiredPropertiesReturned != null ) {
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
				return;
			}
			
			if( t instanceof CommunicationException ) {
				started = STATE_STOPPED;
				if( !ceFound ) {
					ceFound = true;
					IStatus s = new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, CODE|started, t.getMessage(), t);
					log(s);
				}
				return;
			}
			
			if( t instanceof NamingException ) {
				started = STATE_STOPPED;
				if( !nnfeFound ) {
					nnfeFound = true;
					IStatus s = new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID, CODE|started, t.getMessage(), t);
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
		Thread t = new Thread(run, "JMX Poller");
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
			done = true;
			pollingException = new PollingException("Request for more information ignored");
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
		return TIMEOUT_BEHAVIOR_IGNORE;
	}
	
	private void log(IStatus s) {
		if( !canceled )
			ServerLogger.getDefault().log(server,s);		
	}
}
