/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.util;

import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.extensions.polling.WebPortPoller;
import org.jboss.ide.eclipse.as.core.server.IDelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.INeedCredentials;
import org.jboss.ide.eclipse.as.core.server.IPollResultListener;
import org.jboss.ide.eclipse.as.core.server.IProvideCredentials;
import org.jboss.ide.eclipse.as.core.server.IServerProvider;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller2;
import org.jboss.ide.eclipse.as.core.server.IServerStatePollerType;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;

/**
 * @author Rob Stryker
 * @author André Dietisheim
 */
public class PollThreadUtils {

	/**
	 * Returns the poller id of the poller that is currently used in the given server and 
	 * that is waiting for the given state.
	 * 
	 * @param expectedState
	 * @param server
	 * @return
	 */
	public static String getPollerId(boolean expectedState, IServer server) {
		IJBossServer s = ServerConverter.getJBossServer(server);
		ServerAttributeHelper helper = s.getAttributeHelper();
		String key = expectedState == IServerStatePoller.SERVER_UP ?
				IJBossToolingConstants.STARTUP_POLLER_KEY
				: IJBossToolingConstants.SHUTDOWN_POLLER_KEY;
		String defaultPoller = expectedState == IServerStatePoller.SERVER_UP ?
				IJBossToolingConstants.DEFAULT_STARTUP_POLLER
				: IJBossToolingConstants.DEFAULT_SHUTDOWN_POLLER;
		String pollerId = helper.getAttribute(key, defaultPoller);
		return pollerId;
	}

	/**
	 * Returns the poller that is current used in the given server for the expected state.
	 *  
	 * @param expectedState the state that the poller waits for
	 * @param server the server that is using the poller
	 * @return
	 */
	public static IServerStatePoller getPoller(boolean expectedState, IServer server) {
		return getPoller(getPollerId(expectedState, server));
	}

	/**
	 * Returns a new poller instance for the given poller type id. 
	 * 
	 * @param pollerId the id of the poller to use
	 * @return the poller for the given id
	 */
	public static IServerStatePoller getPoller(String pollerId) {
		IServerStatePollerType type = ExtensionManager.getDefault().getPollerType(pollerId);
		if (type != null) {
			IServerStatePoller tempPoller = type.createPoller();
			tempPoller.setPollerType(type);
			return tempPoller;
		}
		return null;
	}

	/**
	 * Stops the given poll thread.
	 * 
	 * @param pollThread the poll thread to stop
	 */
	public static void stopPolling(PollThread pollThread) {
		cancelPolling(null, pollThread);
	}

	/**
	 * Cancels the given poll thread with the given message (that tells about the reason to cancel polling).
	 * 
	 * @param message the reason to cancel the poll thread
	 * @param pollThread the poll thread to cancel
	 */
	public static void cancelPolling(String message, PollThread pollThread) {
		if (pollThread != null) {
			if (message != null) {
				pollThread.cancel(message);
			} else {
				pollThread.cancel();
			}
		}
	}


	/**
	 * Stops the given poll thread and creates a new poll thread for the given
	 * expected state, poller, result listener and server.
	 * 
	 * @param expectedState the state to wait for 
	 * @param poller the poller to use to wait for the expected state
	 * @param pollThread the poll thread to stop
	 * @param listener the listener to inform about the polling result 
	 * @return the new poll thread
	 */
	public static PollThread pollServer(boolean expectedState, IServerStatePoller poller, PollThread currentPollThread,
			IPollResultListener listener, IServer server) {
		stopPolling(currentPollThread);
		PollThread newPollThread = new PollThread(expectedState, poller, listener, server);
		newPollThread.start();
		return newPollThread;
	}
	
	/**
	 * The credential provider is alerted that credentials are needed. 
	 * The response may come at any time. 
	 * 
	 * @param requester
	 * @param requiredProps
	 */
	public static void requestCredentialsAsynch(final INeedCredentials requester, final List<String> requiredProps) {
		new Thread() {
			public void run() {
				requestCredentialsSynchronous(requester, requiredProps);
			}
		}.start();
	}
	
	/**
	 * The credential provider is alerted that credentials are needed. 
	 * The calling thread will block until this method is finished. 
	 * The requester will be told of its credentials by the provider.
	 * 
	 * @param requester
	 * @param requiredProps
	 * @return
	 */

	public static void requestCredentialsSynchronous(final INeedCredentials requester, List<String> requiredProps) {
		IProvideCredentials provider = ExtensionManager.getDefault()
				.getFirstCredentialProvider(requester, requiredProps);
		provider.handle(requester, requiredProps);
	}


	/**
	 * The credential provider is alerted that credentials are needed. 
	 * The calling thread will block until this method is finished. 
	 * A dummy requester is created, which will receive the properties. 
	 * It will then return them to the caller directly. 
	 * 
	 * @param requester
	 * @param requiredProps
	 * @return Properties 
	 */

	public static Properties requestCredentialsSynchronous(final IServerProvider server, List<String> requiredProps) {
		NeedCredentials requester = new NeedCredentials(server.getServer(), requiredProps);
		IProvideCredentials provider = ExtensionManager.getDefault()
				.getFirstCredentialProvider(requester, requiredProps);
		provider.handle(requester, requiredProps);
		return requester.getReturnedCredentials();
	}
	
	public static class NeedCredentials implements INeedCredentials {
		private IServer server;
		private List<String> requiredProps;
		private Properties returnedCredentials;
		public NeedCredentials(IServer server, List<String> requiredProps) {
			this.server = server;
			this.requiredProps = requiredProps;
		}
		public IServer getServer() {
			return server;
		}
		public List<String> getRequiredProperties() {
			return requiredProps;
		}
		public void provideCredentials(Properties credentials) {
			returnedCredentials = credentials;
		}
		public Properties getReturnedCredentials() {
			return returnedCredentials;
		}
	}
	
	public static IStatus isServerStarted(IDelegatingServerBehavior jbsBehavior) {
		IServerStatePoller poller = PollThreadUtils.getPoller(IServerStatePoller.SERVER_UP, jbsBehavior.getServer());
		return isServerStarted(jbsBehavior.getServer(), poller);
	}
	
	/*
	 * A solution needs to be found here. 
	 * Should ideally use the poller that the server says is its poller,
	 * but some pollers such as timeout poller cannot actively check
	 */
	public static IStatus isServerStarted(IServer server,IServerStatePoller poller ) {
		
		// Need to be able to FORCE the poller to poll immediately
		if( poller == null || !(poller instanceof IServerStatePoller2)) 
			poller = new WebPortPoller();
		IStatus started = ((IServerStatePoller2)poller).getCurrentStateSynchronous(server);
		// Trace
		Trace.trace(Trace.STRING_FINER, "Checking if a server is already started: " + started.getMessage()); //$NON-NLS-1$
		
		return started;
	}

}
