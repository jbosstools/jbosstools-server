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

import java.util.Date;

import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.ServerType;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.model.EventLogModel;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogRoot;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.runtime.server.polling.IServerStatePoller.PollingException;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.server.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.server.attributes.IServerPollingAttributes;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.SimpleTreeItem;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class PollThread extends Thread {
	public static final String SERVER_STATE_MAJOR_TYPE = "org.jboss.ide.eclipse.as.core.runtime.server.polling.MajorType";
	
	
	public static final String SERVER_STARTING = "org.jboss.ide.eclipse.as.core.runtime.server.PollThread.server.starting";
	public static final String SERVER_STOPPING = "org.jboss.ide.eclipse.as.core.runtime.server.PollThread.server.stopping";
	public static final String FAILURE = "org.jboss.ide.eclipse.as.core.runtime.server.PollThread.failure";
	public static final String SUCCESS = "org.jboss.ide.eclipse.as.core.runtime.server.PollThread.success";
	public static final String POLLER_NOT_FOUND = "org.jboss.ide.eclipse.as.core.runtime.server.PollThread.pollerNotFound";
	public static final String POLL_THREAD_ABORTED = "org.jboss.ide.eclipse.as.core.runtime.server.PollThread.aborted";
	public static final String POLL_THREAD_TIMEOUT = "org.jboss.ide.eclipse.as.core.runtime.server.PollThread.timeout";
	public static final String EXPECTED_STATE = "org.jboss.ide.eclipse.as.core.runtime.server.PollThreadEvent.expectedState";
	public static final String POLL_THREAD_EXCEPTION = "org.jboss.ide.eclipse.as.core.runtime.server.PollThread.exception";
	public static final String POLL_THREAD_EXCEPTION_MESSAGE = "org.jboss.ide.eclipse.as.core.runtime.server.PollThread.exception.message";

	
	private boolean expectedState;
	private IServerStatePoller poller;
	private boolean abort;
	private JBossServerBehavior behavior;
	private EventLogRoot eventRoot;
	
	private PollThreadEvent activeEvent;
	
	public PollThread(String name, boolean expectedState, JBossServerBehavior behavior) {
		super(name);
		this.expectedState = expectedState;
		this.abort = false;
		this.behavior = behavior;
		eventRoot = EventLogModel.getModel(behavior.getServer()).getRoot();
		
		poller = discoverPoller(behavior, expectedState);
	}
	
	protected IServerStatePoller discoverPoller(JBossServerBehavior behavior, boolean expectedState) {
		JBossServer s = ServerConverter.getJBossServer(behavior.getServer());
		ServerAttributeHelper helper = s.getAttributeHelper();
		String key = expectedState == IServerStatePoller.SERVER_UP ? 
				IServerPollingAttributes.STARTUP_POLLER_KEY : 
				IServerPollingAttributes.SHUTDOWN_POLLER_KEY;
		String defaultPoller = expectedState == IServerStatePoller.SERVER_UP ? 
				IServerPollingAttributes.DEFAULT_STARTUP_POLLER : 
				IServerPollingAttributes.DEFAULT_SHUTDOWN_POLLER;
		String pollerId = helper.getAttribute(key, defaultPoller);
		ServerStatePollerType type = ExtensionManager.getDefault().getPollerType(pollerId);
		return type == null ? null : type.createPoller();
	}
	
	public void cancel() {
		abort = true;
	}

	
	// Getting the timeouts. First from plugin.xml as default, or from user settings.
	public int getTimeout() {
		int timeout;
		JBossServer jbs = ((JBossServer)getServer().loadAdapter(JBossServer.class, null));
		ServerAttributeHelper helper = (ServerAttributeHelper)jbs.getAttributeHelper();
		if( expectedState == IServerStatePoller.SERVER_UP) {
			int def = ((ServerType)getServer().getServerType()).getStartTimeout();
			timeout = helper.getAttribute(IServerPollingAttributes.START_TIMEOUT, def);
		} else {
			int def = ((ServerType)getServer().getServerType()).getStopTimeout();
			timeout = helper.getAttribute(IServerPollingAttributes.STOP_TIMEOUT, def);
		}
		return timeout;
	}
	
	
	public void run() {
		// Poller not found
		if( poller == null ) {
			alertEventLogStarting();
			alertPollerNotFound();
			alertBehavior(getTimeoutBehavior(), false);
			return;
		}
		
		int maxWait = getTimeout();
		alertEventLogStarting();
		
		long startTime = new Date().getTime();
		boolean done = false;
		poller.beginPolling(getServer(), expectedState, this);
		
		// begin the loop; ask the poller every so often
		while( !abort && !done && new Date().getTime() < startTime + maxWait ) {
			try {
				Thread.sleep(100);
			} catch( InterruptedException ie ) { }
			
			try {
				done = poller.isComplete();
			} catch( PollingException e ) {
				// abort and put the message in event log
				poller.cancel(IServerStatePoller.CANCEL);
				poller.cleanup();
				alertEventLogPollerException(e);
				alertBehavior(IServerStatePoller.SERVER_DOWN, false);
				return;
			}
		}
		
		boolean currentState = !expectedState;
		// we stopped. Did we abort?
		if( abort ) {
			poller.cancel(IServerStatePoller.CANCEL);
			poller.cleanup();
			alertEventLogAbort();
		} else {
			boolean finalAlert = true;
			if( done ) {
				// the poller has an answer
				try {
					currentState = poller.getState();
					poller.cleanup();
				} catch( PollingException pe) {
					// abort and put the message in event log
					poller.cancel(IServerStatePoller.CANCEL);
					poller.cleanup();
					alertEventLogPollerException(pe);
					alertBehavior(IServerStatePoller.SERVER_DOWN, false);
					return;
				}
			} else {
				// we timed out.  get response from preferences
				poller.cancel(IServerStatePoller.TIMEOUT_REACHED);
				currentState = getTimeoutBehavior();
				poller.cleanup();
				alertEventLogTimeout();
				finalAlert = false;
			}
			alertBehavior(currentState, finalAlert);
		}
	}

	protected void alertBehavior(boolean currentState, boolean finalAlert) {
		if( currentState != expectedState ) {
			// it didnt work... cancel all processes! force stop
			behavior.forceStop(false);
			if( finalAlert ) alertEventLogFailure();
		} else {
			if( currentState == IServerStatePoller.SERVER_UP ) 
				behavior.setServerStarted();
			else {
				behavior.forceStop(false);
			}
			if( finalAlert ) alertEventLogSuccess(currentState);
		}
	}

	protected boolean getTimeoutBehavior() {
		// timeout has been reached, so let the user's preferences override
		JBossServer jbs = ((JBossServer)getServer().loadAdapter(JBossServer.class, null));
		ServerAttributeHelper helper = (ServerAttributeHelper)jbs.getAttributeHelper();
			
		boolean behavior = helper.getAttribute(IServerPollingAttributes.TIMEOUT_BEHAVIOR, IServerPollingAttributes.TIMEOUT_IGNORE);
		if( behavior == IServerPollingAttributes.TIMEOUT_ABORT ) 
			return !expectedState;

		return expectedState;
	}
		
	protected IServer getServer() {
		 return behavior.getServer();
	}
	

	/*
	 * Event Log Stuff here!
	 */
	protected void alertEventLogStarting() {
		if( expectedState == IServerStatePoller.SERVER_UP) {
			activeEvent = new PollThreadEvent(eventRoot, SERVER_STARTING, expectedState);
		} else {
			activeEvent = new PollThreadEvent(eventRoot, SERVER_STOPPING, expectedState);
		}
		EventLogModel.markChanged(eventRoot);
	}
	
	public PollThreadEvent getActiveEvent() {
		return activeEvent;
	}
	
	protected void alertEventLogPollerException(PollingException e) {
		PollThreadEvent event = new PollThreadEvent(activeEvent, POLL_THREAD_EXCEPTION, expectedState);
		event.setProperty(POLL_THREAD_EXCEPTION_MESSAGE, e.getMessage());
		EventLogModel.markChanged(activeEvent);
	}
	
	protected void alertEventLogAbort() {
		PollThreadEvent event = new PollThreadEvent(activeEvent, POLL_THREAD_ABORTED, expectedState);
		EventLogModel.markChanged(activeEvent);
	}
	protected void alertEventLogTimeout() {
		PollThreadEvent event = new PollThreadEvent(activeEvent, POLL_THREAD_TIMEOUT, expectedState);
		EventLogModel.markChanged(activeEvent);
	}
	protected void alertEventLogFailure() {
		PollThreadEvent event = new PollThreadEvent(eventRoot, FAILURE, expectedState);
		EventLogModel.markChanged(eventRoot);
	}
	protected void alertEventLogSuccess(boolean currentState) {
		PollThreadEvent event = new PollThreadEvent(activeEvent, SUCCESS, expectedState);
		EventLogModel.markChanged(eventRoot);
	}
	protected void alertPollerNotFound() {
		PollThreadEvent event = new PollThreadEvent(activeEvent, POLLER_NOT_FOUND, expectedState);
		EventLogModel.markChanged(activeEvent);
	}

	public class PollThreadEvent extends EventLogTreeItem {
		public PollThreadEvent(SimpleTreeItem parent, String type, boolean expectedState) {
			super(parent, SERVER_STATE_MAJOR_TYPE, type);
			setProperty(EXPECTED_STATE, new Boolean(expectedState));
		}
		
		public boolean getExpectedState() {
			return ((Boolean) getProperty(EXPECTED_STATE)).booleanValue();
		}
	}
}
