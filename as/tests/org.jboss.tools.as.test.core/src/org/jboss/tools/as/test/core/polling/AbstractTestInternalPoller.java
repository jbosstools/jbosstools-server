/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.polling;

import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller2;
import org.jboss.ide.eclipse.as.core.server.IServerStatePollerType;

public class AbstractTestInternalPoller implements IServerStatePoller2 {
	private IServerStatePollerType type;
	private IServer server;
	public AbstractTestInternalPoller() {
	}
	
	public IServerStatePollerType getPollerType() {
		return type;
	}
	public void setPollerType(IServerStatePollerType type) {
		this.type = type;
	}
	public void beginPolling(IServer server, boolean expectedState)
			throws PollingException {
		this.server = server;
	}
	public IServer getServer() {
		return server;
	}
	public boolean isComplete() throws PollingException, RequiresInfoException {
		return false;
	}
	public boolean getState() throws PollingException, RequiresInfoException {
		return IServerStatePoller.SERVER_DOWN;
	}
	public void cleanup() {
	}
	public void cancel(int type) {
	}
	public int getTimeoutBehavior() {
		return 0;
	}
	public List<String> getRequiredProperties() {
		return null;
	}
	public void provideCredentials(Properties credentials) {
	}
	public IStatus getCurrentStateSynchronous(IServer server) {
		return null;
	}

}
