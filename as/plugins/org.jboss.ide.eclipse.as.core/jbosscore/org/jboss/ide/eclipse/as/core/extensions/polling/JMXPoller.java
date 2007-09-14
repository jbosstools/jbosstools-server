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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Date;
import java.util.Properties;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.ReflectionException;
import javax.naming.CommunicationException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXClassLoaderRepository;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXUtil;
import org.jboss.ide.eclipse.as.core.extensions.jmx.JMXUtil.CredentialException;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.SimpleTreeItem;

public class JMXPoller implements IServerStatePoller {

	public static final String STATUS = "org.jboss.ide.eclipse.as.core.runtime.server.internal.TwiddlePoller.status";

	public static final String TYPE_TERMINATED = "org.jboss.ide.eclipse.as.core.runtime.server.internal.TwiddlePoller.TYPE_TERMINATED";
	public static final String TYPE_RESULT = "org.jboss.ide.eclipse.as.core.runtime.server.internal.TwiddlePoller.TYPE_RESULT";

	public static final int STATE_STARTED = 1;
	public static final int STATE_STOPPED = 0;
	public static final int STATE_TRANSITION = -1;

	private int started;
	private boolean canceled;
	private boolean done;
	private IServer server;
	private PollingException pollingException = null;

	private EventLogTreeItem event;

	public void beginPolling(IServer server, boolean expectedState,
			PollThread pt) {
		this.canceled = false;
		this.done = false;
		this.server = server;
		event = pt.getActiveEvent();
		launchJMXPoller();
	}

	private class PollerRunnable implements Runnable {
		public void run() {
			JMXClassLoaderRepository.getDefault().addConcerned(server, this);
			ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
			ClassLoader twiddleLoader = JMXClassLoaderRepository.getDefault().getClassLoader(server);
			if( pollingException != null ) {done = true; return;}
			if (twiddleLoader != null) {

				Thread.currentThread().setContextClassLoader(twiddleLoader);
				Properties props = JMXUtil.getDefaultProperties(server);
				setCredentials();
				if( pollingException != null ) {done = true; return;}

				Exception failingException = null;
				while (!done && !canceled) {
					InitialContext ic;
					try {
						ic = new InitialContext(props);
						Object obj = ic.lookup("jmx/invoker/RMIAdaptor");
						ic.close();
						if (obj instanceof MBeanServerConnection) {
							MBeanServerConnection connection = (MBeanServerConnection) obj;
							Object attInfo = connection.getAttribute(
									new ObjectName("jboss.system:type=Server"),
									"Started");
							boolean b = ((Boolean) attInfo).booleanValue();
							started = b ? STATE_STARTED : STATE_TRANSITION;
							done = b;
						}
					} catch (SecurityException se) {
						pollingException = new PollingSecurityException(
								"Security Exception: " + se.getMessage());
						done = true;
					} catch (CommunicationException ce) {
						started = STATE_STOPPED;
					} catch (NamingException nnfe) {
						started = STATE_STOPPED;
					} catch( OperationsException e ) {
						failingException = e;
					} catch (MBeanException e) {
						failingException = e;
					} catch (ReflectionException e) {
						failingException = e;
					} catch (NullPointerException e) {
						failingException = e;
					} catch (IOException e) {
						failingException = e;
					}
					
					if( failingException != null ) {
						pollingException = new PollingException(failingException.getMessage());
						done = true;
					}

					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
				} // end while
			}

			Thread.currentThread().setContextClassLoader(currentLoader);
			JMXClassLoaderRepository.getDefault().removeConcerned(server, this);
		}

		protected void setCredentials() {
			try {
				JMXUtil.setCredentials(server);
			} catch( CredentialException ce ) {
				pollingException = new PollingException(ce.getWrapped().getMessage());
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

	public class PollingSecurityException extends PollingException {
		public PollingSecurityException(String msg) {
			super(msg);
		}
	}

	public boolean getState() throws PollingException {
		if (pollingException != null)
			throw pollingException;
		if (started == 0)
			return SERVER_DOWN;
		if (started == 1)
			return SERVER_UP;

		if (!done && !canceled)
			return SERVER_DOWN; // Not there yet.

		return SERVER_UP; // done or canceled, doesnt matter
	}

	public boolean isComplete() throws PollingException {
		if (pollingException != null)
			throw pollingException;
		return done;
	}

	public class JMXPollerEvent extends EventLogTreeItem {
		public JMXPollerEvent(SimpleTreeItem parent, String type, int status,
				boolean expectedState) {
			super(parent, PollThread.SERVER_STATE_MAJOR_TYPE, type);
			setProperty(PollThread.EXPECTED_STATE, new Boolean(expectedState));
			setProperty(STATUS, new Integer(status));
			setProperty(DATE, new Long(new Date().getTime()));
		}
	}
}
