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
import java.util.Date;
import java.util.HashMap;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.util.SimpleTreeItem;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class EventLogModel {
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
	
	public static void markChanged(EventLogTreeItem item) {
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
	}
}
