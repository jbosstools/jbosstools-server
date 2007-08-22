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
package org.jboss.ide.eclipse.as.core.runtime.server.polling;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Principal;
import java.util.Date;
import java.util.Properties;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.CommunicationException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.server.JBossServerLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.util.ArgsUtil;
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
			ClassLoader currentLoader = Thread.currentThread()
					.getContextClassLoader();
			ClassLoader twiddleLoader = getClassLoader();
			if( pollingException != null ) {done = true; return;}
			if (twiddleLoader != null) {
				int port = ServerConverter.getJBossServer(server).getJNDIPort();

				Thread.currentThread().setContextClassLoader(twiddleLoader);
				Properties props = new Properties();
				props.put("java.naming.factory.initial",
						"org.jnp.interfaces.NamingContextFactory");
				props.put("java.naming.factory.url.pkgs",
						"org.jboss.naming:org.jnp.interfaces");
				props.put("java.naming.provider.url", "jnp://"
						+ server.getHost() + ":" + port);

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
					} catch (AttributeNotFoundException e) {
						failingException = e;
					} catch (InstanceNotFoundException e) {
						failingException = e;
					} catch (MalformedObjectNameException e) {
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
		}

		protected void setCredentials() {
			Exception temp = null;
			try {
				ILaunchConfiguration lc = server.getLaunchConfiguration(true,
						new NullProgressMonitor());
				String twiddleArgs = lc
						.getAttribute(
								IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS
										+ JBossServerLaunchConfiguration.PRGM_ARGS_TWIDDLE_SUFFIX,
								(String) null);
				
				// get user from the IServer, but override with launch configuration
				String user = ServerConverter.getJBossServer(server).getUsername();
				String userLaunch = ArgsUtil.getValue(twiddleArgs, "-u", "--user");
				user = userLaunch == null ? user : userLaunch;
				
				// get password from the IServer, but override with launch configuration
				String pass = ServerConverter.getJBossServer(server).getPassword();
				String passwordLaunch = ArgsUtil.getValue(twiddleArgs, "-p", "--password");
				pass = passwordLaunch == null ? pass : passwordLaunch;
				
				
				// get our methods
				Class simplePrincipal = Thread.currentThread()
						.getContextClassLoader().loadClass(
								"org.jboss.security.SimplePrincipal");
				Class securityAssoc = Thread.currentThread()
						.getContextClassLoader().loadClass(
								"org.jboss.security.SecurityAssociation");
				securityAssoc.getMethods(); // force-init the methods since the
				// class hasn't been initialized yet.

				Constructor newSimplePrincipal = simplePrincipal
						.getConstructor(new Class[] { String.class });
				Object newPrincipalInstance = newSimplePrincipal
						.newInstance(new Object[] { user });

				// set the principal
				Method setPrincipalMethod = securityAssoc.getMethod(
						"setPrincipal", new Class[] { Principal.class });
				setPrincipalMethod.invoke(null,
						new Object[] { newPrincipalInstance });

				// set the credential
				Method setCredentialMethod = securityAssoc.getMethod(
						"setCredential", new Class[] { Object.class });
				setCredentialMethod.invoke(null, new Object[] { pass });
			} catch (CoreException e) {
				temp = e;
			} catch (ClassNotFoundException e) {
				temp = e;
			} catch (SecurityException e) {
				temp = e;
			} catch (NoSuchMethodException e) {
				temp = e;
			} catch (IllegalArgumentException e) {
				temp = e;
			} catch (InstantiationException e) {
				temp = e;
			} catch (IllegalAccessException e) {
				temp = e;
			} catch (InvocationTargetException e) {
				temp = e;
			}
			if( temp != null ) {
				pollingException = new PollingException(temp.getMessage());
			}
		}

		protected ClassLoader getClassLoader() {
			try {
				IRuntime rt = server.getRuntime();
				IPath loc = rt.getLocation();
				URL url = loc.append("client").append("jbossall-client.jar")
						.toFile().toURI().toURL();
				URL url2 = loc.append("bin").append("twiddle.jar").toFile()
						.toURI().toURL();
				URLClassLoader loader = new URLClassLoader(new URL[] { url,
						url2 }, Thread.currentThread().getContextClassLoader());
				return loader;
			} catch (MalformedURLException murle) {
				pollingException = new PollingException(murle.getMessage());
			}
			return null;
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
