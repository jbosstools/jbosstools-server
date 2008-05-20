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

import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.server.internal.ServerStatePollerType;

/**
 * Essentially does nothing because the process already has a listener
 * on it that sets the server state to stopped once the process dies. 
 * 
 * It's here to make the shutdown include no polling though, thus
 * more efficient.
 * @author rob
 *
 */
public class ProcessTerminatedPoller implements IServerStatePoller {

	public static final String POLLER_ID = "org.jboss.ide.eclipse.as.core.runtime.server.processTerminatedPoller";
	
	private ServerStatePollerType type;
	private JBossServerBehavior server;
	public void beginPolling(IServer server, boolean expectedState,
			PollThread pt) {
		this.server = (JBossServerBehavior)server.loadAdapter(JBossServerBehavior.class, new NullProgressMonitor());
	}

	public IServer getServer() {
		return server.getServer();
	}

	public void cancel(int type) {
	}

	public void cleanup() {
	}

	public boolean getState() throws PollingException {
		return !isComplete();
	}

	public boolean isComplete() throws PollingException {
		return server.getProcess() == null || server.getProcess().isTerminated();
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
		return TIMEOUT_BEHAVIOR_IGNORE;
	}


}
