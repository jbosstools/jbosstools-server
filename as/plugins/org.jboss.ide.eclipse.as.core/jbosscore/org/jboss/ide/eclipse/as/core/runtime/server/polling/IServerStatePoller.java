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
package org.jboss.ide.eclipse.as.core.runtime.server.polling;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.attributes.IServerPollingAttributes;

public interface IServerStatePoller extends IServerPollingAttributes {
	
	public static final boolean SERVER_UP = true;
	public static final boolean SERVER_DOWN = false;
	
	public static final int CANCEL = 0;
	public static final int TIMEOUT_REACHED = 1;
	
	public void beginPolling(IServer server, boolean expectedState, PollThread pt); // expected to launch own thread
	public boolean isComplete() throws PollingException;
	public boolean getState() throws PollingException; 
	public void cancel(int type);    // cancel the polling
	public void cleanup();   // clean up any resources / processes. Will ALWAYS be called
	
	public class PollingException extends Exception {
		public PollingException(String message) {super(message);}
	}
}
