/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.core.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ServerProcessModelEntity;
import org.jboss.ide.eclipse.as.core.util.ASDebug;

public class ServerProcessLog {
	
	public static class ProcessLogEvent {
		public static final int SERVER_ROOT = -1;

		public static final int SERVER_CONSOLE = 0;
		
		public static final int SERVER_STARTING = 1;
		public static final int SERVER_STOPPING = 3;
		public static final int SERVER_UP = 9;
		public static final int SERVER_DOWN = 10;

		public static final int SERVER_PUBLISH = 2;
		public static final int SERVER_UNPUBLISH = 14;
		public static final int SERVER_VERIFY = 15;
		
		public static final int TWIDDLE = 4;
		public static final int STDOUT = 5;
		public static final int STDERR = 6;
		public static final int PROCESS = 7;
		public static final int ERROR = 8;
		
		public static final int WAR = 11;
		public static final int EJB_JAR = 12;
		public static final int EAR = 13;

		public static final int UNKNOWN = 16;
		
		
		public static final int ADD_BEGINNING = 0;
		public static final int ADD_END = 1;
		
		
		
		
		private HashMap properties = new HashMap();
		private ArrayList children = new ArrayList();
		private ProcessLogEvent parent;

		private String text;
		private int eventType;
		private long date;
		private boolean complete = false;
		
		public ProcessLogEvent(String text, int eventType) {
			this.text = text;
			this.eventType = eventType;
			this.date = new Date().getTime();
		}
		
		public ProcessLogEvent[] getChildren() {
			ProcessLogEvent[] events = new ProcessLogEvent[children.size()];
			children.toArray(events);
			return events;
		}
		
		public void addChild(ProcessLogEvent event, int location) {
			if( location == ADD_BEGINNING ) {
				children.add(0, event);
			} else {
				children.add(event);
			}
			event.setParent(this);
		}
		
		public void addChild(ProcessLogEvent event) {
			addChild(event, ADD_END);
		}
		
		
		public ProcessLogEvent addChild(String text, int eventType) {
			return addChild(text, eventType, ADD_END);
		}
		
		public ProcessLogEvent addChild(String text, int eventType, int location) {
			ProcessLogEvent e = new ProcessLogEvent(text, eventType);
			addChild(e, location);
			return e;
		}


		public void setParent(ProcessLogEvent parent) {
			this.parent = parent;
		}
		
		public void addChildren(ProcessLogEvent[] kids) {
			for( int i = 0; i < kids.length; i++ ) {
				addChild(kids[i]);
			}
		}
		
		public ProcessLogEventRoot getRoot() {
			if( this instanceof ProcessLogEventRoot ) return (ProcessLogEventRoot)this;
			
			if( getParent() == null ) {
				// disconnected
				return null;
			}
			if( getParent() instanceof ProcessLogEventRoot ) 
				return (ProcessLogEventRoot)getParent();
			
			return getParent().getRoot();
		}
		
		public void deleteChildren() {
			children.clear();
		}
		
		public void deleteChild(ProcessLogEvent o) {
			children.remove(o);
		}
		
		public void accept(IProcessLogVisitor visitor) {
			boolean ret = visitor.visit(this);
			if( ret ) {
				// visit the children
				ProcessLogEvent[] children = getChildren();
				for( int i = 0; i < children.length; i++ ) {
					children[i].accept(visitor);
				}
			}
		}
		
		public int getEventType() {
			return eventType;
		}
		
		public void setEventType(int eventType) {
			this.eventType = eventType;
		}

		public String getText() {
			return text;
		}

		public ProcessLogEvent getParent() {
			return parent;
		}
		
		public long getDate() {
			return this.date;
		}
		
		public void setProperty( Object key, Object val ) {
			properties.put(key, val); 
		}
		
		public Object getProperty(Object key) {
			return properties.get(key);
		}
		
		public HashMap getProperties() {
			return properties;
		}
		
		public boolean isComplete() {
			return complete;
		}

		public void setComplete() {
			this.complete = true;
		}
		
		public void setText(String text) {
			this.text = text;
		}

	}
	
	public static class ProcessLogEventRoot extends ProcessLogEvent {
		private IServer server;
		private String serverID;
//		private ProcessLogEvent consoleLog;
		
		public ProcessLogEventRoot(String serverID) {
			super(serverID, SERVER_ROOT);
			this.server = ServerCore.findServer(serverID);
			this.serverID = serverID;
		}
		/**
		 * One of my children has changed and I should alert someone 
		 * who might care.
		 */
		public void branchChanged() {
			ServerProcessModel.getDefault().processModelChanged(this);
		}
		
		public ServerProcessModelEntity getProcessModel() {
			return ServerProcessModel.getDefault().getModel(serverID);
		}
		
		public IServer getServer() {
			return server;
		}
		
		public ProcessLogEvent newMajorEvent(String text, int type) {
			ProcessLogEvent newEvent = new ProcessLogEvent(text, type);
			addChild(newEvent, ADD_BEGINNING);
			return newEvent;
		}
		
		/**
		 * Get the latest major event.
		 * @param create  Create a new event if the last is finished?
		 * @return
		 */
		public ProcessLogEvent getLatestMajorEvent(boolean create) {
			if( getChildren().length > 0 ) 
				if( !getChildren()[0].isComplete()) 
					return getChildren()[0];
			
			return addChild("Unknown Event", ProcessLogEvent.UNKNOWN, ProcessLogEvent.ADD_BEGINNING);
		}
		
	}
	
	
	public static interface IProcessLogEventListener {
		public void modulUpdated(ProcessLogEvent event);
	}
	
	public static interface IProcessLogVisitor {
		public boolean visit(ProcessLogEvent event);
		public Object getResult();
	}
	
}
