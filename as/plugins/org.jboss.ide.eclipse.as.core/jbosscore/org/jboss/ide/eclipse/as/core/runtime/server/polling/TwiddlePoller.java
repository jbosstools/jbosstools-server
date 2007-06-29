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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.Properties;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.model.EventLogModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ServerProcessModelEntity;
import org.jboss.ide.eclipse.as.core.runtime.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.JBossServerLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.server.TwiddleLauncher;
import org.jboss.ide.eclipse.as.core.util.ArgsUtil;
import org.jboss.ide.eclipse.as.core.util.SimpleTreeItem;

public class TwiddlePoller implements IServerStatePoller {

	public static final String STATUS = "org.jboss.ide.eclipse.as.core.runtime.server.internal.TwiddlePoller.status";

	public static final String TYPE_TERMINATED = "org.jboss.ide.eclipse.as.core.runtime.server.internal.TwiddlePoller.TYPE_TERMINATED";
	public static final String TYPE_RESULT = "org.jboss.ide.eclipse.as.core.runtime.server.internal.TwiddlePoller.TYPE_RESULT";
	
	public static final int STATE_STARTED = 1;
	public static final int STATE_STOPPED = 0;
	public static final int STATE_TRANSITION = -1;
	
	private boolean expectedState;
	private int started; 
	private boolean canceled;
	private boolean done;
	private IServer server;
	private boolean securityException = false;
	
	private EventLogTreeItem event;
	public void beginPolling(IServer server, boolean expectedState, PollThread pt) {
		this.expectedState = expectedState;
		this.canceled = false;
		this.done = false;
		this.server = server;
		event = pt.getActiveEvent();
		launchTwiddlePoller();
	}

	private class PollerRunnable implements Runnable {
		private TwiddleLauncher launcher;
		public void run() {
			ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
			ClassLoader twiddleLoader = getClassLoader();
			if( twiddleLoader != null ) {
				Thread.currentThread().setContextClassLoader(twiddleLoader);
				System.out.println("here we go");
				
				Properties props = new Properties();
		        props.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
		        props.put("java.naming.provider.url","jnp://localhost:1099");
		        props.put("java.naming.factory.url.pkgs","org.jboss.naming:org.jnp.interfaces");
		        
		        while( !done && !canceled ) {
					InitialContext ic;
					try {
						ic = new InitialContext(props);
			            Object obj = ic.lookup("jmx/invoker/RMIAdaptor");
			            ic.close();
			            System.out.println(obj);
			            if( obj instanceof MBeanServerConnection ) {
			                    MBeanServerConnection connection = (MBeanServerConnection)obj;
			                    Object attInfo = connection.getAttribute(new ObjectName("jboss.system:type=Server"), "Started");
			                    boolean b = ((Boolean)attInfo).booleanValue();
			                    started = b ? STATE_STARTED : STATE_TRANSITION;
			                    if( b && expectedState )
			                    	done = true;
			            }
					} catch (NamingException e) {
						// should give up now
					} catch( SecurityException se ) {
						securityException = true;
					} catch( Exception e ) {
						System.out.println("exception: " + e.getMessage());
						e.printStackTrace();
						started = STATE_STOPPED;
						if( !expectedState )
							done = true;
					}

		        } // end while
			}
			
			Thread.currentThread().setContextClassLoader(currentLoader);

		}
		protected ClassLoader getClassLoader() {
			try {
				URL url = new URL("file:///C:/apps/jboss/4.2.ga.src/build/output/jboss-4.2.0.GA/client/jbossall-client.jar");
				URL url2 = new URL("file:///C:/apps/jboss/4.2.ga.src/build/output/jboss-4.2.0.GA/bin/twiddle.jar");
				URLClassLoader loader = new URLClassLoader(new URL[] {url, url2}, Thread.currentThread().getContextClassLoader());
				return loader;
			} catch( MalformedURLException murle) {
				murle.printStackTrace();
			}
			return null;
		}
		public void setCanceled() {
			if( launcher != null ) {
				launcher.setCanceled();
			}
		}
	}
	public void eventTwiddleExecuted() {
		TwiddlePollerEvent tpe = new TwiddlePollerEvent(event, TYPE_RESULT, started, expectedState);
		EventLogModel.markChanged(event);
	}
	public void eventAllProcessesTerminated() {
		TwiddlePollerEvent tpe = new TwiddlePollerEvent(event, TYPE_TERMINATED, started, expectedState);
		EventLogModel.markChanged(event);
	}
	
	private void launchTwiddlePoller() {
		PollerRunnable run = new PollerRunnable();
		Thread t = new Thread(run, "Twiddle Poller");
		t.start();
	}
	
	
	public void cancel(int type) {
		canceled = true;
	}

	public void cleanup() {
		ServerProcessModelEntity ent = ServerProcessModel.getDefault().getModel(server.getId());
		if( expectedState == SERVER_UP) {
			ent.terminateProcesses(JBossServerLaunchConfiguration.TWIDDLE);
		} else {
			ent.terminateProcesses(JBossServerLaunchConfiguration.TWIDDLE);
			ent.terminateProcesses(JBossServerLaunchConfiguration.STOP);
		}
	}

	public class PollingSecurityException extends PollingException {
		public PollingSecurityException(String msg) {super(msg);}
	}
	public boolean getState() throws PollingException  {
		if( securityException ) throw new PollingSecurityException("Security Exception");
		if( started == 0 ) return SERVER_DOWN;
		if( started == 1 ) return SERVER_UP;

		if( !done && !canceled ) 
			return !expectedState; // Not there yet.

		return expectedState; // done or canceled, doesnt matter
	}

	public boolean isComplete() throws PollingException {
		if( securityException ) 
			throw new PollingSecurityException("Security Exception");
		return done;
	}
	
	
	public class TwiddlePollerEvent extends EventLogTreeItem {
		public TwiddlePollerEvent(SimpleTreeItem parent, String type, int status, boolean expectedState) {
			super(parent, PollThread.SERVER_STATE_MAJOR_TYPE, type);
			setProperty(PollThread.EXPECTED_STATE, new Boolean(expectedState));
			setProperty(STATUS, new Integer(status));
			setProperty(DATE, new Long(new Date().getTime()));
		}
	}
	
	

}
