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
package org.jboss.ide.eclipse.as.core.server;

import java.util.List;
import java.util.Properties;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.server.internal.ServerStatePollerType;

/**
 * A server state poller will 
 * @author Rob Stryker
 *
 */
public interface IServerStatePoller {
	public static final int POLLING_CODE = 1 << 24;

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
	public void beginPolling(IServer server, boolean expectedState, PollThread pt); // expected to launch own thread
	public IServer getServer();
	public boolean isComplete() throws PollingException, RequiresInfoException;
	public boolean getState() throws PollingException, RequiresInfoException; 
	public void cleanup();   // clean up any resources / processes. Will ALWAYS be called
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
