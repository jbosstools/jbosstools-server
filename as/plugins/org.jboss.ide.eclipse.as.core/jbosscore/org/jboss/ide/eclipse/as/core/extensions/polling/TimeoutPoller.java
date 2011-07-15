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
package org.jboss.ide.eclipse.as.core.extensions.polling;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.server.internal.ServerStatePollerType;

/**
 * Wait 15 seconds, then say it's at it's expected state
 * @author Rob
 *
 */
public class TimeoutPoller implements IServerStatePoller {
	public static final String POLLER_ID = "org.jboss.ide.eclipse.as.core.runtime.server.timeoutpoller"; //$NON-NLS-1$
	
	private boolean expectedState;
	private long endTime;
	private IServer server;
	private ServerStatePollerType type;

	@Deprecated
	public void beginPolling(IServer server, boolean expectedState, PollThread pollThread) {
	}
	
	public void beginPolling(IServer server, boolean expectedState) {
		this.expectedState = expectedState;
		this.server = server;
		this.endTime = new Date().getTime() + getTimeout();
	}
	
	public IServer getServer() {
		return server;
	}

	public void cancel(int type) {
	}

	public boolean getState() {
		if( new Date().getTime() > endTime ) return expectedState;
		return !expectedState;
	}

	public boolean isComplete() {
		if( new Date().getTime() > endTime ) return true;
		return false;
	}

	public void cleanup() {
	}
	
	public int getTimeout() {
		if( expectedState == IServerStatePoller.SERVER_UP)
			return (getServer().getStartTimeout()-3) * 1000;
		else 
			return (getServer().getStopTimeout()-3) * 1000;
	}

	public boolean supportsShutdown() {
		return true;
	}

	public boolean supportsStartup() {
		return true;
	}

	public void failureHandled(Properties properties) {
	}

	public List getRequiredProperties() {
		return null;
	}
	
	public ServerStatePollerType getPollerType() {
		return type;
	}

	public void setPollerType(ServerStatePollerType type) {
		this.type = type;
	}

	public int getTimeoutBehavior() {
		if( expectedState == IServerStatePoller.SERVER_UP)
			return TIMEOUT_BEHAVIOR_SUCCEED;
		else 
			return TIMEOUT_BEHAVIOR_IGNORE;
	}


}
