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
package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IServer;

/**
 * This interface is an addition to the IServerStatePoller
 * for pollers which can be forced immediately to poll the server.
 * Some pollers, such as timeout poller or processTerminated poller,
 * have no such ability. 
 */
public interface IServerStatePoller2 extends IServerStatePoller {
	/**
	 * Get the current state of the server via a forced 
	 * poll request. 
	 * 
	 * This API is required because the structure of the poller API
	 * allows some pollers to launch their own threads, and respond to 
	 * getState() as the answer comes in. 
	 * 
	 * This method, in contrast, initiates an immediate and synchronous 
	 * poll attempt to determine the current state. 
	 * 
	 * @return IServerStatePoller.SERVER_UP or SERVER_DOWN
	 */
	public IStatus getCurrentStateSynchronous(IServer server);
}
