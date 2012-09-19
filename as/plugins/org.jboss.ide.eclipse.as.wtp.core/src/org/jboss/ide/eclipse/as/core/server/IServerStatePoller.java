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

import org.eclipse.wst.server.core.IServer;

/**
 * A server state poller will 
 * @author Rob Stryker
 *
 */
public interface IServerStatePoller extends INeedCredentials {
	public static final int POLLING_CODE = 1 << 24;
	public static final int POLLER_MASK = 0xFF << 16;
	
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
	
	public IServerStatePollerType getPollerType();
	public void setPollerType(IServerStatePollerType type);
	/*
	 * expected to launch own thread
	 */
	public void beginPolling(IServer server, boolean expectedState) throws PollingException; 
	public boolean isComplete() throws PollingException, RequiresInfoException;
	
	/**
	 * Called only after poller is "done".  
	 * Should return cached final state rather than poll again. 
	 *  
	 * @return
	 * @throws PollingException
	 * @throws RequiresInfoException
	 */
	public boolean getState() throws PollingException, RequiresInfoException; 
	/*
	 * clean up any resources / processes. Will ALWAYS be called
	 */
	public void cleanup();

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

		public PollingException(String message) {
			super(message);
		}

		public PollingException(String message, Throwable t) {
			super(message, t);
		}
	}
	
	public class RequiresInfoException extends Exception {

		private static final long serialVersionUID = 5050044329807740335L;
		private boolean checked = false;

		public RequiresInfoException(String msg) {
			super(msg);
		}

		public void setChecked() { 
			this.checked = true; 
		}

		public boolean getChecked() { 
			return this.checked; 
		}
	}
}
