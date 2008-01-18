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

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.provisional.UIDecoratorManager;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.extensions.polling.JMXPoller;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread.PollThreadEvent;
import org.jboss.ide.eclipse.as.ui.views.server.extensions.IEventLogLabelProvider;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class PollingLabelProvider extends ComplexEventLogLabelProvider implements IEventLogLabelProvider {

	
	protected void addSupportedTypes() {
		supported.add(PollThread.SERVER_STARTING);
		supported.add(PollThread.SERVER_STOPPING);
		supported.add(PollThread.FAILURE);
		supported.add(PollThread.SUCCESS);
		supported.add(PollThread.POLL_THREAD_ABORTED);
		supported.add(PollThread.POLL_THREAD_TIMEOUT);
		supported.add(PollThread.POLL_THREAD_EXCEPTION);
		supported.add(PollThread.POLLER_NOT_FOUND);
		
		supported.add(EventLogModel.EVENT_TYPE_EXCEPTION);
		supported.add(JMXPoller.EVENT_TYPE_STARTING);
		
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
		
		if( element.getSpecificType().equals(EventLogModel.EVENT_TYPE_EXCEPTION)) 
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
		if( element.getSpecificType().equals(JMXPoller.EVENT_TYPE_STARTING)) {
			boolean started = ((Boolean)element.getProperty(JMXPoller.STARTED_PROPERTY)).booleanValue();
			if( !started ) 
				return getStartingImage();
			return getStartedImage();
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
			String expectedString = expected == IServerStatePoller.SERVER_UP ? "Startup" : "Shutdown";
			if( element.getSpecificType().equals(PollThread.POLL_THREAD_ABORTED)) {
				Object cause = element.getProperty(PollThread.POLL_THREAD_ABORTED_CAUSE); 
				return expectedString + " aborted" + (cause != null ? ": " + cause.toString() : "");
			}
			if( element.getSpecificType().equals(PollThread.POLL_THREAD_TIMEOUT)) return expectedString + " timed out";
			if( element.getSpecificType().equals(PollThread.POLL_THREAD_EXCEPTION)) return "Failure: " + element.getProperty(PollThread.POLL_THREAD_EXCEPTION_MESSAGE);
			if( element.getSpecificType().equals(PollThread.SUCCESS)) return expectedString + " succeeded";
			if( element.getSpecificType().equals(PollThread.FAILURE)) return expectedString + " failed";
			if( element.getSpecificType().equals(PollThread.POLLER_NOT_FOUND)) return expectedString + " failed. Poller not found";
		}
		
		if( element.getSpecificType().equals(EventLogModel.EVENT_TYPE_EXCEPTION)) {
			Object o = element.getProperty(EventLogModel.EXCEPTION_PROPERTY);
			return "JMXException: " + ( o == null ? "null" : ((Exception)o).getMessage());
		}
		
		if( element.getSpecificType().equals(JMXPoller.EVENT_TYPE_STARTING)) {
			boolean started = ((Boolean)element.getProperty(JMXPoller.STARTED_PROPERTY)).booleanValue();
			if( !started ) 
				return "Server is still starting";
			return "Server has completed startup";
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
		propertyToMessageMap.put(PollThread.EXPECTED_STATE, "Expected State");
		propertyToMessageMap.put(EventLogModel.EXCEPTION_PROPERTY, "Exception");
		propertyToMessageMap.put(JMXPoller.STARTED_PROPERTY, "Server Started");
		propertyToMessageMap.put(PollThread.POLL_THREAD_ABORTED_CAUSE, "Abort Cause");
		// now values and their readable forms
		propertyToMessageMap.put(PollThread.EXPECTED_STATE + DELIMITER + "true", "Up");
		propertyToMessageMap.put(PollThread.EXPECTED_STATE + DELIMITER + "false", "Down");
	}
}
