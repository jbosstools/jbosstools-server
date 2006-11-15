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
package org.jboss.ide.eclipse.as.ui.views.server.providers;

import java.util.ArrayList;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.runtime.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.runtime.server.polling.PollThread;
import org.jboss.ide.eclipse.as.core.runtime.server.polling.TwiddlePoller;
import org.jboss.ide.eclipse.as.core.runtime.server.polling.PollThread.PollThreadEvent;
import org.jboss.ide.eclipse.as.core.server.JBossServerBehavior;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.IEventLogLabelProvider;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class PollingLabelProvider extends LabelProvider implements IEventLogLabelProvider {

	private ArrayList supported;
	public PollingLabelProvider() {
		supported = new ArrayList();
		supported.add(PollThread.SERVER_STARTING);
		supported.add(PollThread.SERVER_STOPPING);
		supported.add(PollThread.FAILURE);
		supported.add(PollThread.SUCCESS);
		supported.add(PollThread.POLL_THREAD_ABORTED);
		supported.add(PollThread.POLL_THREAD_TIMEOUT);
		
		supported.add(TwiddlePoller.TYPE_TERMINATED);
		supported.add(TwiddlePoller.TYPE_RESULT);
		
		supported.add(JBossServerBehavior.FORCE_SHUTDOWN_EVENT_KEY);
	}

	public boolean supports(String type) {
		return supported.contains(type);
	}

	public Image getImage(EventLogTreeItem element) {
		return null;
	}

	public String getText(EventLogTreeItem element) {
		if( element.getType().equals(PollThread.SERVER_STARTING)) return "Starting the Server";
		if( element.getType().equals(PollThread.SERVER_STOPPING)) return "Stopping the Server";
		
		if( element instanceof PollThreadEvent ) {
			boolean expected = ((PollThreadEvent)element).getExpectedState();
			String expectedString = expected == IServerStatePoller.SERVER_UP ? "startup" : "shutdown";
			if( element.getType().equals(PollThread.POLL_THREAD_ABORTED)) return expectedString + " aborted";
			if( element.getType().equals(PollThread.POLL_THREAD_TIMEOUT)) return expectedString + " timed out";
			if( element.getType().equals(PollThread.SUCCESS)) return expectedString + " succeeded";
			if( element.getType().equals(PollThread.FAILURE)) return expectedString + " failed";
		}
		
		if( element.getType().equals(TwiddlePoller.TYPE_TERMINATED)) return "All processes have been terminated";
		if( element.getType().equals(TwiddlePoller.TYPE_RESULT)) {
			int state = ((Integer)element.getProperty(TwiddlePoller.STATUS)).intValue();
			boolean expectedState = ((Boolean)element.getProperty(TwiddlePoller.EXPECTED_STATE)).booleanValue();
			if( state == TwiddlePoller.STATE_STOPPED) 
				return "The server is down.";
			if( state == TwiddlePoller.STATE_STARTED)
				return "The server is up.";
			if( state == TwiddlePoller.STATE_TRANSITION) {
				if( expectedState == IServerStatePoller.SERVER_UP ) 
					return "The server is still starting";
				return "The server is still stopping.";
			}
		}
		
		
		if( element.getType().equals(JBossServerBehavior.FORCE_SHUTDOWN_EVENT_KEY)) 
			return "The server was shutdown forcefully. All processes terminated.";
		return null;
	}
}
