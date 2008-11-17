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
package org.jboss.ide.eclipse.as.core.server.internal;

import java.util.Date;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IPollerFailureHandler;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller.PollingException;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller.RequiresInfoException;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

/**
 * 
 * @author rob.stryker@jboss.com
 */
public class PollThread extends Thread {
	// PollThread status objects look like this:
	// 00000001xxxxxxxxaaaaaaaaaaaaaaaa  
	// 00000001000000010000000000xxx00x
	public static final int POLLING_ROOT_CODE = IServerStatePoller.POLLING_CODE | 1 << 16;
	public static final int SUCCESS = 0x1;
	public static final int FAIL = 0;
	public static final int POLLING_FAIL_CODE = POLLING_ROOT_CODE | FAIL;
	public static final int STATE_MASK = 0x111000;
	public static final int STATE_UNKNOWN = IServer.STATE_UNKNOWN << 3;
	public static final int STATE_STARTING = IServer.STATE_STARTING << 3;
	public static final int STATE_STARTED = IServer.STATE_STARTED << 3;
	public static final int STATE_STOPPING = IServer.STATE_STOPPING << 3;
	public static final int STATE_STOPPED = IServer.STATE_STOPPED << 3;
	
	public static final String SERVER_STARTING = "org.jboss.ide.eclipse.as.core.runtime.server.PollThread.server.starting";
	public static final String SERVER_STOPPING = "org.jboss.ide.eclipse.as.core.runtime.server.PollThread.server.stopping";

	private boolean expectedState, abort, stateStartedOrStopped;
	private IServerStatePoller poller;
	private String abortMessage;
	private JBossServerBehavior behavior;
	private String pollerId;

	public PollThread(String name, boolean expectedState,
			JBossServerBehavior behavior) {
		super(name);
		this.expectedState = expectedState;
		this.abort = false;
		this.abortMessage = null;
		this.behavior = behavior;
		this.poller = discoverPoller(behavior, expectedState);
	}

	protected IServerStatePoller discoverPoller(JBossServerBehavior behavior,
			boolean expectedState) {
		JBossServer s = ServerConverter.getJBossServer(behavior.getServer());
		ServerAttributeHelper helper = s.getAttributeHelper();
		String key = expectedState == IServerStatePoller.SERVER_UP ? IJBossServerConstants.STARTUP_POLLER_KEY
				: IJBossServerConstants.SHUTDOWN_POLLER_KEY;
		String defaultPoller = expectedState == IServerStatePoller.SERVER_UP ? IJBossServerConstants.DEFAULT_STARTUP_POLLER
				: IJBossServerConstants.DEFAULT_SHUTDOWN_POLLER;
		pollerId = helper.getAttribute(key, defaultPoller);
		ServerStatePollerType type = ExtensionManager.getDefault()
				.getPollerType(pollerId);
		if (type != null) {
			IServerStatePoller tempPoller = type.createPoller();
			tempPoller.setPollerType(type);
			return tempPoller;
		}
		return null;
	}

	public void cancel() {
		cancel(null);
	}

	public void cancel(String message) {
		abort = true;
		abortMessage = message;
		poller.cancel(IServerStatePoller.CANCEL);
	}

	public int getTimeout() {
		if (expectedState == IServerStatePoller.SERVER_UP)
			return (getServer().getStartTimeout() - 2) * 1000;
		else
			return (getServer().getStopTimeout() - 2) * 1000;
	}

	public void run() {
		// Poller not found
		if (poller == null) {
			alertEventLogStarting();
			alertPollerNotFound();
			alertBehavior(!expectedState, false);
			return;
		}

		int maxWait = getTimeout();
		alertEventLogStarting();

		long startTime = new Date().getTime();
		boolean done = false;
		poller.beginPolling(getServer(), expectedState, this);

		// begin the loop; ask the poller every so often
		while (!stateStartedOrStopped && !abort && !done
				&& (new Date().getTime() < startTime + maxWait) || maxWait < 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ie) {
			}

			try {
				done = poller.isComplete();
			} catch (PollingException e) {
				// abort and put the message in event log
				poller.cancel(IServerStatePoller.CANCEL);
				poller.cleanup();
				alertEventLogPollerException(e);
				alertBehavior(!expectedState, false);
				return;
			} catch (RequiresInfoException rie) {
				// This way each request for new info is checked only once.
				if (!rie.getChecked()) {
					rie.setChecked();
					String action = expectedState == IServerStatePoller.SERVER_UP ? SERVER_STARTING
							: SERVER_STOPPING;
					IPollerFailureHandler handler = ExtensionManager
							.getDefault().getFirstPollFailureHandler(poller,
									action, poller.getRequiredProperties());
					handler.handle(poller, action, poller
							.getRequiredProperties());
				}
			}
			stateStartedOrStopped = checkServerState();
		}

		// we stopped. Did we abort?
		if (stateStartedOrStopped) {
			int state = behavior.getServer().getServerState();
			boolean success = false;
			if (expectedState == IServerStatePoller.SERVER_UP)
				success = state == IServer.STATE_STARTED;
			else
				success = state == IServer.STATE_STOPPED;

			poller.cancel(success ? IServerStatePoller.SUCCESS
					: IServerStatePoller.FAILED);
			poller.cleanup();
		} else if (abort) {
			poller.cleanup();
			alertEventLogAbort();
		} else {
			boolean currentState = !expectedState;
			boolean finalAlert = true;
			if (done) {
				// the poller has an answer
				try {
					currentState = poller.getState();
					poller.cleanup();
					alertBehavior(currentState, finalAlert);
				} catch (PollingException pe) {
					// abort and put the message in event log
					poller.cancel(IServerStatePoller.CANCEL);
					poller.cleanup();
					alertEventLogPollerException(pe);
					alertBehavior(!expectedState, false);
					return;
				} catch (RequiresInfoException rie) {
					// You don't have an answer... liar!
				}
			} else {
				// we timed out. get response from preferences
				poller.cancel(IServerStatePoller.TIMEOUT_REACHED);
				int behavior = poller.getTimeoutBehavior();
				poller.cleanup();
				alertEventLogTimeout();
				if (behavior != IServerStatePoller.TIMEOUT_BEHAVIOR_IGNORE) {
					// xnor;
					// if behavior is to succeed and we're expected to go up,
					// we're up
					// if behavior is to fail and we're expecting to be down,
					// we're up (failed to shutdown)
					// all other cases, we're down.
					currentState = (expectedState == (behavior == IServerStatePoller.TIMEOUT_BEHAVIOR_SUCCEED));
					finalAlert = false;
					alertBehavior(currentState, finalAlert);
				}
			}
		}
	}

	protected boolean checkServerState() {
		int state = behavior.getServer().getServerState();
		if (state == IServer.STATE_STARTED)
			return true;
		if (state == IServer.STATE_STOPPED)
			return true;
		return false;
	}

	protected void alertBehavior(boolean currentState, boolean finalAlert) {
		if (currentState != expectedState) {
			// it didnt work... cancel all processes! force stop
			behavior.forceStop();
			if (finalAlert)
				alertEventLogFailure();
		} else {
			if (currentState == IServerStatePoller.SERVER_UP)
				behavior.setServerStarted();
			else 
				behavior.forceStop();

			if (finalAlert)
				alertEventLogSuccess(currentState);
		}
	}

	protected IServer getServer() {
		return behavior.getServer();
	}

	/*
	 * Event Log Stuff here!
	 */
	protected void alertEventLogStarting() {
		String message = expectedState ? "Server Starting" : "Server shutting down";
		int state = expectedState ? STATE_STARTING : STATE_STOPPING;
		
		IStatus status = new Status(IStatus.INFO,
				JBossServerCorePlugin.PLUGIN_ID, POLLING_ROOT_CODE | state, message, null);
		ServerLogger.getDefault().log(behavior.getServer(), status);
	}

	protected void alertEventLogPollerException(PollingException e) {
		IStatus status = new Status(IStatus.ERROR,
				JBossServerCorePlugin.PLUGIN_ID, POLLING_FAIL_CODE, "Failure in Poll Thread", e);
		ServerLogger.getDefault().log(behavior.getServer(), status);
	}

	protected void alertEventLogAbort() {
		IStatus status = new Status(IStatus.WARNING,
				JBossServerCorePlugin.PLUGIN_ID, POLLING_FAIL_CODE | getStateMask(expectedState, false), "Poll Thread Aborted: "
						+ abortMessage, null);
		ServerLogger.getDefault().log(behavior.getServer(), status);
	}

	protected void alertEventLogTimeout() {
		IStatus status = new Status(IStatus.ERROR,
				JBossServerCorePlugin.PLUGIN_ID, POLLING_FAIL_CODE | getStateMask(expectedState, false), "", null);
		ServerLogger.getDefault().log(behavior.getServer(), status);
	}

	protected void alertEventLogFailure() {
		String startupFailed = "Server Startup Failed";
		String shutdownFailed = "Server Shutdown Failed";
		IStatus status = new Status(IStatus.ERROR,
				JBossServerCorePlugin.PLUGIN_ID, POLLING_FAIL_CODE | getStateMask(expectedState, false),
				expectedState ? startupFailed : shutdownFailed, null);
		ServerLogger.getDefault().log(behavior.getServer(), status);
	}

	protected void alertEventLogSuccess(boolean currentState) {
		String startupSuccess = "Server Startup Succeeded";
		String shutdownSuccess = "Server Shutdown Succeeded";
		int state = getStateMask(expectedState, true);
		IStatus status = new Status(IStatus.INFO,
				JBossServerCorePlugin.PLUGIN_ID, POLLING_ROOT_CODE | state |  SUCCESS,
				expectedState ? startupSuccess : shutdownSuccess, null);
		ServerLogger.getDefault().log(behavior.getServer(), status);
	}

	protected void alertPollerNotFound() {
		String startupPollerNotFound = "Startup Poller not found: " + pollerId;
		String shutdownPollerNotFound = "Shutdown Poller not found: "+ pollerId;
		IStatus status = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
				POLLING_FAIL_CODE | getStateMask(expectedState, false), 
				expectedState ? startupPollerNotFound : shutdownPollerNotFound, null);
		ServerLogger.getDefault().log(behavior.getServer(), status);
	}
	
	protected int getStateMask(boolean expected, boolean success) {
		if( expected && success )
			return STATE_STARTED;
		if( !expected && !success)
			return STATE_STARTED;
		return STATE_STOPPED;
	}
}
