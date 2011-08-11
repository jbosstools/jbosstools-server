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
package org.jboss.ide.eclipse.as.core.server.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.server.IPollerFailureHandler;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller.PollingException;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller.RequiresInfoException;

/**
 * 
 * @author rob.stryker@jboss.com
 */
public class PollThread extends Thread {
	// PollThread status objects look like this:
	// 00000001xxxxxxxxaaaaaaaaaaaaaaaa  
	// 00000001000000010000000000xxx00x
	public static final int POLLING_ROOT_CODE = IEventCodes.POLLING_ROOT_CODE;
	public static final int SUCCESS = 0x1;
	public static final int FAIL = 0;
	public static final int POLLING_FAIL_CODE = POLLING_ROOT_CODE | FAIL;
	public static final int STATE_MASK = 0x38;   // 0b111000;
	public static final int STATE_UNKNOWN = IServer.STATE_UNKNOWN << 3;
	public static final int STATE_STARTING = IServer.STATE_STARTING << 3;
	public static final int STATE_STARTED = IServer.STATE_STARTED << 3;
	public static final int STATE_STOPPING = IServer.STATE_STOPPING << 3;
	public static final int STATE_STOPPED = IServer.STATE_STOPPED << 3;
	
	public static final String SERVER_STARTING = "org.jboss.ide.eclipse.as.core.runtime.server.PollThread.server.starting"; //$NON-NLS-1$
	public static final String SERVER_STOPPING = "org.jboss.ide.eclipse.as.core.runtime.server.PollThread.server.stopping"; //$NON-NLS-1$

	private boolean expectedState, abort, stateStartedOrStopped;
	private IServerStatePoller poller;
	private String abortMessage;
	@Deprecated
	private DelegatingServerBehavior behavior;
	private String pollerId;
	private IPollResultListener listener;
	private IServer server;

	@Deprecated
	public PollThread(boolean expectedState, IServerStatePoller poller, DelegatingServerBehavior behavior) {
		super(getThreadName(behavior.getServer()));
		this.expectedState = expectedState;
		this.behavior = behavior;
		this.server = behavior.getServer();
		this.poller = poller;
		this.abort = false;
	}

	public PollThread(boolean expectedState, IServerStatePoller poller, IPollResultListener listener, IServer server) {
		super(getThreadName(server));
		this.expectedState = expectedState;
		this.poller = poller;
		this.server = server;
		this.listener = listener;
		this.abort = false;
	}

	private static String getThreadName(IServer server) {
		return NLS.bind(Messages.ServerPollerThreadName, server.getName());
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
//			alertBehavior(!expectedState);
			alertListener(!expectedState);
			return;
		}

		int maxWait = getTimeout();
		alertEventLogStarting();

		long startTime = System.currentTimeMillis();
		boolean done = false;
		try {
			poller.beginPolling(getServer(), expectedState);
	
			// begin the loop; ask the poller every so often
			while (!stateStartedOrStopped 
					&& !abort 
					&& !done
					&& !timeoutReached(startTime, maxWait)) {
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
//					alertBehavior(!expectedState);
					alertListener(!expectedState);
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
		} catch(PollingException e) {
			abort = true;
		}

		// we stopped. Did we abort?
		if (stateStartedOrStopped) {
			int state = server.getServerState();
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
//					alertBehavior(currentState);
					alertListener(currentState);
					if (finalAlert) {
						alertEventLog(currentState);
					}
				} catch (PollingException pe) {
					// abort and put the message in event log
					poller.cancel(IServerStatePoller.CANCEL);
					poller.cleanup();
					alertEventLogPollerException(pe);
//					alertBehavior(!expectedState);
					alertListener(!expectedState);
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
//					alertBehavior(currentState);					
					alertListener(currentState);
				}
			}
		}
	}

	private boolean timeoutReached(long startTime, int maxWait) {
		return System.currentTimeMillis() >= (startTime + maxWait);
	}

	protected boolean checkServerState() {
		int state = server.getServerState();
		if (state == IServer.STATE_STARTED)
			return true;
		if (state == IServer.STATE_STOPPED)
			return true;
		return false;
	}

	protected void alertEventLog(boolean currentState) {
		if (currentState != expectedState) {
			alertEventLogFailure();			
		} else {
			alertEventLogSuccess(currentState);
		}
	}
	
	@Deprecated
	protected void alertBehavior(boolean currentState) {
		if (currentState != expectedState) {
			// it didnt work... cancel all processes! force stop
			behavior.stop(true);
		} else {
			if (currentState == IServerStatePoller.SERVER_UP)
				behavior.setServerStarted();
			else 
				behavior.stop(true);
		}
	}

	protected void alertListener(boolean currentState) {
		if (currentState != expectedState) {
			listener.stateNotAsserted(currentState, expectedState);
		} else {
			listener.stateAsserted(currentState, expectedState);
		}
	}
	
	protected IServer getServer() {
		return server;
	}

	/*
	 * Event Log Stuff here!
	 */
	protected void alertEventLogStarting() {
		String message = expectedState ?
				Messages.PollingStarting : Messages.PollingShuttingDown;
		int state = expectedState ? STATE_STARTING : STATE_STOPPING;
		
		IStatus status = new Status(IStatus.INFO,
				JBossServerCorePlugin.PLUGIN_ID, POLLING_ROOT_CODE | state, message, null);
		ServerLogger.getDefault().log(server, status);
	}

	protected void alertEventLogPollerException(PollingException e) {
		IStatus status = new Status(IStatus.ERROR,
				JBossServerCorePlugin.PLUGIN_ID, POLLING_FAIL_CODE, Messages.PollerFailure, e);
		ServerLogger.getDefault().log(server, status);
	}

	protected void alertEventLogAbort() {
		IStatus status = new Status(IStatus.WARNING,
				JBossServerCorePlugin.PLUGIN_ID, POLLING_FAIL_CODE | getStateMask(expectedState, false), 
				NLS.bind(Messages.PollerAborted, abortMessage), null);
		ServerLogger.getDefault().log(server, status);
	}

	protected void alertEventLogTimeout() {
		IStatus status = new Status(IStatus.ERROR,
				JBossServerCorePlugin.PLUGIN_ID, POLLING_FAIL_CODE | getStateMask(expectedState, false), "", null); //$NON-NLS-1$
		ServerLogger.getDefault().log(server, status);
	}

	protected void alertEventLogFailure() {
		String startupFailed = Messages.PollingStartupFailed;
		String shutdownFailed = Messages.PollingShutdownFailed;
		IStatus status = new Status(IStatus.ERROR,
				JBossServerCorePlugin.PLUGIN_ID, POLLING_FAIL_CODE | getStateMask(expectedState, false),
				expectedState ? startupFailed : shutdownFailed, null);
		ServerLogger.getDefault().log(server, status);
	}

	protected void alertEventLogSuccess(boolean currentState) {
		String startupSuccess = Messages.PollingStartupSuccess;
		String shutdownSuccess = Messages.PollingShutdownSuccess;
		int state = getStateMask(expectedState, true);
		IStatus status = new Status(IStatus.INFO,
				JBossServerCorePlugin.PLUGIN_ID, POLLING_ROOT_CODE | state |  SUCCESS,
				expectedState ? startupSuccess : shutdownSuccess, null);
		ServerLogger.getDefault().log(server, status);
	}

	protected void alertPollerNotFound() {
		String startupPollerNotFound = NLS.bind(Messages.StartupPollerNotFound, pollerId); 
		String shutdownPollerNotFound = NLS.bind(Messages.ShutdownPollerNotFound, pollerId);
		IStatus status = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
				POLLING_FAIL_CODE | getStateMask(expectedState, false), 
				expectedState ? startupPollerNotFound : shutdownPollerNotFound, null);
		ServerLogger.getDefault().log(server, status);
	}
	
	protected int getStateMask(boolean expected, boolean success) {
		if( expected && success )
			return STATE_STARTED;
		if( !expected && !success)
			return STATE_STARTED;
		return STATE_STOPPED;
	}
}
