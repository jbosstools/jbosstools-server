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
package org.jboss.ide.eclipse.as.ui.views.server.providers.events;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.provisional.UIDecoratorManager;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.runtime.server.polling.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.runtime.server.polling.PollThread;
import org.jboss.ide.eclipse.as.core.runtime.server.polling.JMXPoller;
import org.jboss.ide.eclipse.as.core.runtime.server.polling.PollThread.PollThreadEvent;
import org.jboss.ide.eclipse.as.core.server.JBossServerBehavior;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.IEventLogLabelProvider;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class PollingLabelProvider extends ComplexEventLogLabelProvider implements IEventLogLabelProvider {

	
	protected void addSupportedTypes() {
		supported = new ArrayList();
		supported.add(PollThread.SERVER_STARTING);
		supported.add(PollThread.SERVER_STOPPING);
		supported.add(PollThread.FAILURE);
		supported.add(PollThread.SUCCESS);
		supported.add(PollThread.POLL_THREAD_ABORTED);
		supported.add(PollThread.POLL_THREAD_TIMEOUT);
		supported.add(PollThread.POLL_THREAD_EXCEPTION);
		supported.add(PollThread.POLLER_NOT_FOUND);
		
		supported.add(JMXPoller.TYPE_TERMINATED);
		supported.add(JMXPoller.TYPE_RESULT);
		
		supported.add(JBossServerBehavior.FORCE_SHUTDOWN_EVENT_KEY);
	}

	public Image getImage(EventLogTreeItem element) {
		if( element.getSpecificType().equals(PollThread.SERVER_STARTING)) return getStartingImage();
		if( element.getSpecificType().equals(PollThread.SERVER_STOPPING)) return getStoppingImage();
		
		if( element instanceof PollThreadEvent ) {
			boolean expected = ((PollThreadEvent)element).getExpectedState();
			//String expectedString = expected == IServerStatePoller.SERVER_UP ? "startup" : "shutdown";
			if( element.getSpecificType().equals(PollThread.POLL_THREAD_ABORTED)) return getErrorImage();
			if( element.getSpecificType().equals(PollThread.POLL_THREAD_TIMEOUT)) return getErrorImage();
			if( element.getSpecificType().equals(PollThread.POLL_THREAD_EXCEPTION)) return getErrorImage();
			if( element.getSpecificType().equals(PollThread.POLLER_NOT_FOUND)) return getErrorImage();
			if( element.getSpecificType().equals(PollThread.SUCCESS)) {
				if( expected == IServerStatePoller.SERVER_UP)
					return getStartedImage();
				return getStoppedImage();
			}
			if( element.getSpecificType().equals(PollThread.FAILURE)) 
				return getErrorImage();
		}
		
		if( element.getSpecificType().equals(JMXPoller.TYPE_TERMINATED)) return getErrorImage();
		if( element.getSpecificType().equals(JMXPoller.TYPE_RESULT)) {
			int state = ((Integer)element.getProperty(JMXPoller.STATUS)).intValue();
			boolean expectedState = ((Boolean)element.getProperty(PollThread.EXPECTED_STATE)).booleanValue();
			if( state == JMXPoller.STATE_STOPPED) 
				return getStoppedImage();
			if( state == JMXPoller.STATE_STARTED)
				return getStartedImage();
			if( state == JMXPoller.STATE_TRANSITION) {
				if( expectedState == IServerStatePoller.SERVER_UP ) 
					return getStartingImage();
				return getStoppingImage();
			}
		}
		
		
		if( element.getSpecificType().equals(JBossServerBehavior.FORCE_SHUTDOWN_EVENT_KEY)) 
			return getErrorImage();
		return null;
	}

	public String getText(EventLogTreeItem element) {
		if( element.getSpecificType().equals(PollThread.SERVER_STARTING)) return "Starting the Server";
		if( element.getSpecificType().equals(PollThread.SERVER_STOPPING)) return "Stopping the Server";
		
		if( element instanceof PollThreadEvent ) {
			boolean expected = ((PollThreadEvent)element).getExpectedState();
			String expectedString = expected == IServerStatePoller.SERVER_UP ? "startup" : "shutdown";
			if( element.getSpecificType().equals(PollThread.POLL_THREAD_ABORTED)) return expectedString + " aborted";
			if( element.getSpecificType().equals(PollThread.POLL_THREAD_TIMEOUT)) return expectedString + " timed out";
			if( element.getSpecificType().equals(PollThread.POLL_THREAD_EXCEPTION)) return "Failure: " + element.getProperty(PollThread.POLL_THREAD_EXCEPTION_MESSAGE);
			if( element.getSpecificType().equals(PollThread.SUCCESS)) return expectedString + " succeeded";
			if( element.getSpecificType().equals(PollThread.FAILURE)) return expectedString + " failed";
			if( element.getSpecificType().equals(PollThread.POLLER_NOT_FOUND)) return expectedString + " failed. Poller not found";
		}
		
		if( element.getSpecificType().equals(JMXPoller.TYPE_TERMINATED)) return "All processes have been terminated";
		if( element.getSpecificType().equals(JMXPoller.TYPE_RESULT)) {
			int state = ((Integer)element.getProperty(JMXPoller.STATUS)).intValue();
			boolean expectedState = ((Boolean)element.getProperty(PollThread.EXPECTED_STATE)).booleanValue();
			if( state == JMXPoller.STATE_STOPPED) 
				return "The server is down.";
			if( state == JMXPoller.STATE_STARTED)
				return "The server is up.";
			if( state == JMXPoller.STATE_TRANSITION) {
				if( expectedState == IServerStatePoller.SERVER_UP ) 
					return "The server is still starting";
				return "The server is still stopping.";
			}
		}
		
		
		if( element.getSpecificType().equals(JBossServerBehavior.FORCE_SHUTDOWN_EVENT_KEY)) 
			return "The server was shutdown forcefully. All processes terminated.";
		return null;
	}
	
	protected Image getStateImage(int state) {
		return UIDecoratorManager.getUIDecorator(null).getStateImage(state, ILaunchManager.RUN_MODE, 0);
	}

	protected Image getErrorImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
	}
	public Image getStartedImage() {
		return getStateImage(IServer.STATE_STARTED);
	}
	public Image getStartingImage() {
		return getStateImage(IServer.STATE_STARTING);
	}
	public Image getStoppingImage() {
		return getStateImage(IServer.STATE_STOPPING);
	}
	public Image getStoppedImage() {
		return getStateImage(IServer.STATE_STOPPED);
	}


	
	/*
	 * Property Stuff
	 */
	protected void loadPropertyMap() {
		// property names and their readable forms
		propertyToMessageMap.put(EventLogTreeItem.DATE, "Time");
		propertyToMessageMap.put(JMXPoller.STATUS, "Status");
		propertyToMessageMap.put(PollThread.EXPECTED_STATE, "Expected State");
		
		// now values and their readable forms
		propertyToMessageMap.put(JMXPoller.STATUS + DELIMITER + 0, "Server is Down");
		propertyToMessageMap.put(JMXPoller.STATUS + DELIMITER + 1, "Server is Up");
		propertyToMessageMap.put(JMXPoller.STATUS + DELIMITER + -1, "Server is in transition");
		propertyToMessageMap.put(PollThread.EXPECTED_STATE + DELIMITER + "true", "Up");
		propertyToMessageMap.put(PollThread.EXPECTED_STATE + DELIMITER + "false", "Down");
	}
}
