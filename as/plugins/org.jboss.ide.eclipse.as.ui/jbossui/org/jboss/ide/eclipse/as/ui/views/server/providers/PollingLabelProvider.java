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
	private HashMap propertyToMessageMap;
	public PollingLabelProvider() {
		addSupportedTypes();
		loadPropertyMap();
	}
	
	protected void addSupportedTypes() {
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
		if( element.getType().equals(PollThread.SERVER_STARTING)) return getStartingImage();
		if( element.getType().equals(PollThread.SERVER_STOPPING)) return getStoppingImage();
		
		if( element instanceof PollThreadEvent ) {
			boolean expected = ((PollThreadEvent)element).getExpectedState();
			//String expectedString = expected == IServerStatePoller.SERVER_UP ? "startup" : "shutdown";
			if( element.getType().equals(PollThread.POLL_THREAD_ABORTED)) return getErrorImage();
			if( element.getType().equals(PollThread.POLL_THREAD_TIMEOUT)) return getErrorImage();
			if( element.getType().equals(PollThread.SUCCESS)) {
				if( expected == IServerStatePoller.SERVER_UP)
					return getStartedImage();
				return getStoppedImage();
			}
			if( element.getType().equals(PollThread.FAILURE)) 
				return getErrorImage();
		}
		
		if( element.getType().equals(TwiddlePoller.TYPE_TERMINATED)) return getErrorImage();
		if( element.getType().equals(TwiddlePoller.TYPE_RESULT)) {
			int state = ((Integer)element.getProperty(TwiddlePoller.STATUS)).intValue();
			boolean expectedState = ((Boolean)element.getProperty(PollThread.EXPECTED_STATE)).booleanValue();
			if( state == TwiddlePoller.STATE_STOPPED) 
				return getStoppedImage();
			if( state == TwiddlePoller.STATE_STARTED)
				return getStartedImage();
			if( state == TwiddlePoller.STATE_TRANSITION) {
				if( expectedState == IServerStatePoller.SERVER_UP ) 
					return getStartingImage();
				return getStoppingImage();
			}
		}
		
		
		if( element.getType().equals(JBossServerBehavior.FORCE_SHUTDOWN_EVENT_KEY)) 
			return getErrorImage();
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
			boolean expectedState = ((Boolean)element.getProperty(PollThread.EXPECTED_STATE)).booleanValue();
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
	
	protected Image getStateImage(int state) {
		return UIDecoratorManager.getUIDecorator(null).getStateImage(state, ILaunchManager.RUN_MODE, 0);
	}

	protected Image getErrorImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
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
		propertyToMessageMap = new HashMap();
		propertyToMessageMap.put(EventLogTreeItem.DATE, "Time");
		propertyToMessageMap.put(TwiddlePoller.STATUS, "Status");
		propertyToMessageMap.put(PollThread.EXPECTED_STATE, "Expected State");
		
		// now values and their readable forms
		propertyToMessageMap.put(TwiddlePoller.STATUS + "::" + 0, "Server is Down");
		propertyToMessageMap.put(TwiddlePoller.STATUS + "::" + 1, "Server is Up");
		propertyToMessageMap.put(TwiddlePoller.STATUS + "::" + -1, "Server is in transition");
		propertyToMessageMap.put(PollThread.EXPECTED_STATE + "::" + "true", "Up");
		propertyToMessageMap.put(PollThread.EXPECTED_STATE + "::" + "false", "Down");
	}
	
	public Properties getProperties(EventLogTreeItem item) {
		loadPropertyMap(); // temporary to fascilitate debugging
		
		
		Properties p = new Properties();
		HashMap map = item.getProperties();
		Object key = null;
		String keyString, valueStringKey, valueString;
		for( Iterator i = map.keySet().iterator(); i.hasNext();) {
			key = i.next();
			if( key.equals(EventLogTreeItem.DATE)) {
				keyString = propertyToMessageMap.get(key) == null ? (String)key : propertyToMessageMap.get(key).toString();
				valueString = getDateAsString(((Long)map.get(key)).longValue());
				p.put(keyString, valueString);
			} else if( key instanceof String ) {
				keyString = propertyToMessageMap.get(key) == null ? (String)key : propertyToMessageMap.get(key).toString();
				valueStringKey = key + "::" + map.get(key).toString();
				valueString = propertyToMessageMap.get(valueStringKey) == null ? map.get(key).toString() : propertyToMessageMap.get(valueStringKey).toString();
				p.put(keyString, valueString);
			}
		}
		return p;
	}
	
	protected String getDateAsString(long date) {
		long now = new Date().getTime();
		long seconds = (now - date) / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		minutes -= (hours * 60);
		String minString = minutes + "m ago";
		if( hours == 0 )
			return minString;
		return hours + "h " + minString; 
	}
}
