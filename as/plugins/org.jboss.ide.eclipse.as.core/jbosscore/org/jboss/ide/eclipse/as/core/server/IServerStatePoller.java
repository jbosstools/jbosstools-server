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

import java.util.List;
import java.util.Properties;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.server.internal.ServerStatePollerType;

/**
 * A server state poller will 
 * @author Rob Stryker
 *
 */
public interface IServerStatePoller {
	public static final int POLLING_CODE = IEventCodes.POLLING_CODE;
	public static final int POLLER_MASK = IEventCodes.POLLER_MASK;
	
	public static final boolean SERVER_UP = true;
	public static final boolean SERVER_DOWN = false;
	
	public static final int CANCEL = 0;
	public static final int TIMEOUT_REACHED = 1;
	public static final int SUCCESS = 2;
	public static final int FAILED = 3;
	
	/**
	 * Force a successful state change if we timeout
	 */
	public static final int TIMEOUT_BEHAVIOR_SUCCEED = 0;
	
	/**
	 * Ignore the timeout and let the wst tools handle the timeout
	 */
	public static final int TIMEOUT_BEHAVIOR_IGNORE = 1;
	
	/**
	 * Force a failure upon timeout
	 */
	public static final int TIMEOUT_BEHAVIOR_FAIL = 2;
	
	public ServerStatePollerType getPollerType();
	public void setPollerType(ServerStatePollerType type);
	/*
	 * expected to launch own thread
	 */
	@Deprecated
	public void beginPolling(IServer server, boolean expectedState, PollThread pt) throws Exception; 
	public void beginPolling(IServer server, boolean expectedState) throws Exception; 
	public IServer getServer();
	public boolean isComplete() throws PollingException, RequiresInfoException;
	public boolean getState() throws PollingException, RequiresInfoException; 
	/*
	 * clean up any resources / processes. Will ALWAYS be called
	 */
	public void cleanup();
	public List<String> getRequiredProperties();
	public void failureHandled(Properties properties);
	
	/**
	 * Cancel the polling. 
	 * @param type CANCEL or TIMEOUT_REACHED
	 */
	public void cancel(int type);    
	
	/**
	 * Returns a TIMEOUT_BEHAVIOR_XXX constant
	 * @return
	 */
	public int getTimeoutBehavior();

	public class PollingException extends Exception {
		private static final long serialVersionUID = -7830978018908940551L;
		public PollingException(String message) {super(message);}
	}
	
	public class RequiresInfoException extends Exception {
		private static final long serialVersionUID = 5050044329807740335L;
		private boolean checked = false;
		public RequiresInfoException(String msg) {super(msg);}
		public void setChecked() { this.checked = true; }
		public boolean getChecked() { return this.checked; }
	}
}
