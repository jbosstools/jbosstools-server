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

import java.util.Date;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.runtime.server.IServerStatePoller;

// Wait 15 seconds, then say it's at it's expected state
public class TimeoutPoller implements IServerStatePoller {

	private boolean expectedState;
	private long endTime;
	int timeout = -1;
	
	
	public void beginPolling(IServer server, boolean expectedState, PollThread pt) {
		this.expectedState = expectedState;
		this.endTime = new Date().getTime() + getTimeout();
	}

	protected int getTimeout() {
		return 15000;
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
}
