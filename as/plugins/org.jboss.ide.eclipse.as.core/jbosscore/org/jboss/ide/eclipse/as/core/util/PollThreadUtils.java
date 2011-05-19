package org.jboss.ide.eclipse.as.core.util;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.server.internal.ServerAttributeHelper;
import org.jboss.ide.eclipse.as.core.server.internal.ServerStatePollerType;

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
}
