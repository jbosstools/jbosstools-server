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

import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.DelegatingServerBehavior;
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

	public static final String POLLER_ID = "org.jboss.ide.eclipse.as.core.runtime.server.processTerminatedPoller"; //$NON-NLS-1$
	
	private ServerStatePollerType type;
	private DelegatingServerBehavior server;
	public void beginPolling(IServer server, boolean expectedState,
			PollThread pt) {
		this.server = (DelegatingServerBehavior)server.loadAdapter(DelegatingServerBehavior.class, new NullProgressMonitor());
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
//		IJBossBehaviourDelegate del = server.getDelegate();
//		if( del instanceof IProcessProvider ) {
//			IProcess p = ((IProcessProvider)del).getProcess();
//			boolean b = p == null || p.isTerminated();
//			System.out.println(p + " " + b); //$NON-NLS-1$
//		} 
		return false;
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
