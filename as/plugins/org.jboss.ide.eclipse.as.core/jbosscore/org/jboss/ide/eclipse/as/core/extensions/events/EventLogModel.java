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
package org.jboss.ide.eclipse.as.core.extensions.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.util.SimpleTreeItem;
import org.osgi.service.prefs.BackingStoreException;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class EventLogModel {
	public static final String ENABLE_LOGGING_PREFERENCE = "org.jboss.ide.eclipse.as.core.extensions.events.enableLogging";
	public static final String EVENT_TYPE_EXCEPTION = "org.jboss.ide.eclipse.as.core.extensions.events.EXCEPTION";
	public static final String EXCEPTION_PROPERTY = "org.jboss.ide.eclipse.as.core.extensions.events.EXCEPTION_PROPERTY";

	public static void enableLogging(boolean enabled) {
		if( enabled )
			enableLogging();
		else
			disableLogging();
	}
	
	public static void enableLogging() {
		IEclipsePreferences prefs = new DefaultScope().getNode(JBossServerCorePlugin.PLUGIN_ID);
		prefs.putBoolean(ENABLE_LOGGING_PREFERENCE, true);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {	
			JBossServerCorePlugin.getDefault().getLog().log(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Saving preferences failed", e));
		}
	}
	
	public static void disableLogging() {
		IEclipsePreferences prefs = new DefaultScope().getNode(JBossServerCorePlugin.PLUGIN_ID);
		prefs.putBoolean(ENABLE_LOGGING_PREFERENCE, false);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {	
			JBossServerCorePlugin.getDefault().getLog().log(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Saving preferences failed", e));
		}
		ServerEventModel[] models = getModels();
		for( int i = 0; i < models.length; i++ ) {
			models[i].clearEvents();
		}
	}
	
	public static boolean isLoggingEnabled() {
		IEclipsePreferences prefs = new DefaultScope().getNode(JBossServerCorePlugin.PLUGIN_ID);
		return prefs.getBoolean(ENABLE_LOGGING_PREFERENCE, false);
	}
	
	public static final String JBOSS_EVENT_ROOT_TYPE = "jboss.event.root";
	private static EventLogModel instance;
	public static EventLogModel getDefault() {
		if( instance == null )
			instance = new EventLogModel();
		return instance;
	}
	
	public static ServerEventModel getModel(IServer server) {
		return getModel(server.getId());
	}
	
	public static ServerEventModel getModel(String serverId) {
		return getDefault().getEventModel(serverId);
	}
	
	public static ServerEventModel[] getModels() {
		return getDefault().getEventModels();
	}
	
	public static void markChanged(EventLogTreeItem item) {
		if( item == null ) return;
		SimpleTreeItem root = item.getRoot();
		if( root != null && root instanceof EventLogRoot ) {
			String serverId = ((EventLogRoot)root).getServerId();
			getDefault().fireItemChanged(serverId, item);
		}
	}

	public static interface IEventLogListener {
		public void eventModelChanged(String serverId, EventLogTreeItem changed);
	}
	
	private HashMap serverToModel;
	private ArrayList listeners;
	public EventLogModel() {
		serverToModel = new HashMap();
		listeners = new ArrayList();
	}
	
	public void addListener(IEventLogListener listener) {
		if( !listeners.contains(listener)) listeners.add(listener);
	}
	public void removeListener(IEventLogListener listener) {
		listeners.remove(listener);
	}
	public void fireItemChanged(String serverId, EventLogTreeItem changed) {
		IEventLogListener[] listenerArray = (IEventLogListener[]) listeners.toArray(new IEventLogListener[listeners.size()]);
		for( int i = 0; i < listenerArray.length; i++ ) {
			listenerArray[i].eventModelChanged(serverId, changed);
		}
	}
	
	public ServerEventModel getEventModel(String serverId) {
		if( serverToModel.get(serverId) != null ) 
			return (ServerEventModel)serverToModel.get(serverId);
		ServerEventModel m = new ServerEventModel(serverId);
		serverToModel.put(serverId, m);
		return m;
	}
	
	public ServerEventModel[] getEventModels() {
		Collection c = serverToModel.values();
		return (ServerEventModel[]) c.toArray(new ServerEventModel[c.size()]);
	}
	
	public static class ServerEventModel {
		private String id;
		private EventLogRoot root;
		protected ServerEventModel(String serverId) {
			this.id = serverId;
			root = new EventLogRoot(this);
		}
		public String getId() {
			return id;
		}
		public EventLogRoot getRoot() {
			return root;
		}
		public void clearEvents() {
			root.deleteChildren();
		}
	}
	
	public static class EventLogTreeItem extends SimpleTreeItem {
		public static final String DATE = "org.jboss.ide.eclipse.as.core.model.EventLogTreeItem.Date";
		protected String specificType, majorType;
		public EventLogTreeItem(SimpleTreeItem parent, String majorType, String specificType) {
			super(parent, null);
			this.specificType = specificType;
			this.majorType = majorType;
			setProperty(DATE, new Long(new Date().getTime()));
		}
		public String getEventClass() {
			return majorType;
		}
		public String getSpecificType() {
			return specificType;
		}
		public EventLogTreeItem getEventRoot() {
			SimpleTreeItem item = this;
			while(item.getParent() != null && item.getParent() instanceof EventLogTreeItem)
				item = item.getParent();
			return (EventLogTreeItem)item;
		}

	}

	public static class EventLogRoot extends EventLogTreeItem {
		protected ServerEventModel model;
		public EventLogRoot(ServerEventModel model) {
			super(null, null, JBOSS_EVENT_ROOT_TYPE);
			this.model = model;
		}
		public ServerEventModel getModel() {
			return model;
		}
		public String getServerId() {
			return model.getId();
		}
		
		public void addChild(SimpleTreeItem item) {
			if( isLoggingEnabled() ) 
				super.addChild(item);
		}
			
		public void addChildren(SimpleTreeItem[] kids) {
			if( isLoggingEnabled() ) 
				super.addChildren(kids);
		}

	}
}
