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

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.server.internal.ServerStatePollerType;

/**
 * @author Rob Stryker
 * @author André Dietisheim
 */
public class PollThreadUtils {

	public static String getPollerId(boolean expectedState, IServer server) {
		JBossServer s = ServerConverter.getJBossServer(server);
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

	public static IServerStatePoller getPoller(boolean expectedState, IServer server) {
		return getPoller(getPollerId(expectedState, server), expectedState, server);
	}

	public static IServerStatePoller getPoller(String pollerId, boolean expectedState, IServer server) {
		ServerStatePollerType type = ExtensionManager.getDefault().getPollerType(pollerId);
		if (type != null) {
			IServerStatePoller tempPoller = type.createPoller();
			tempPoller.setPollerType(type);
			return tempPoller;
		}
		return null;
	}

	public static void stopPolling(PollThread pollThread) {
		cancelPolling(null, pollThread);
	}

	public static void cancelPolling(String message, PollThread pollThread) {
		if (pollThread != null) {
			if (message != null) {
				pollThread.cancel(message);
			} else {
				pollThread.cancel();
			}
		}
	}

	protected void pollServer(final boolean expectedState, PollThread pollThread, JBossServerBehavior behaviour, IServer server) {
		IServerStatePoller poller = PollThreadUtils.getPoller(expectedState, server);
		pollServer(expectedState, poller, pollThread, behaviour);
	}

	
	public static PollThread pollServer(boolean expectedState, IServerStatePoller poller, PollThread pollThread, JBossServerBehavior behaviour) {
		stopPolling(pollThread);
		PollThread newPollThread = new PollThread(expectedState, poller, behaviour);
		newPollThread.start();
		return newPollThread;
	}

}
