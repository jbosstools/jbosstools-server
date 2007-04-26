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
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.ServerCore;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.JBossServerLaunchConfiguration;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public class ServerProcessModel implements IServerLifecycleListener {

	private static ServerProcessModel instance;
	
	/**
	 * There is a singleton instance of the ServerProcessModel.
	 * @return
	 */
	public static ServerProcessModel getDefault() {
		if( instance == null ) {
			instance = new ServerProcessModel();
		}
		return instance;
	}
	
	/**
	 * Static method to check if all of some process array are terminated.
	 * @param processes
	 * @return
	 */
	public static boolean allProcessesTerminated(IProcess[] processes) {
		for( int i = 0; i < processes.length; i++ ) {
			if( !(processes[i].isTerminated())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Will map server ID's to a server process entity.
	 * Each server has it's own entity instance.
	 */
	private HashMap map;
	
	public ServerProcessModel() {
		map = new HashMap();
		initialize();
	}
	
	private void initialize() {
		ServerCore.addServerLifecycleListener(this);
		JBossServer[] servers = ServerConverter.getAllJBossServers();
		for( int i = 0; i < servers.length; i++ ) {
			getModel(servers[i].getServer().getId(), true);
		}
	}
	
	
	/**
	 * Each JBoss Server instance has it's own sub-model
	 * @param key
	 * @return
	 */
	public ServerProcessModelEntity getModel(String key) {
		return getModel(key, false);
	}
	
	
	private ServerProcessModelEntity getModel(String key, boolean create) {
		Object o = map.get(key);
		if( o == null ) {
			if( !create ) return null;
			o = new ServerProcessModelEntity(key);
			map.put(key, (ServerProcessModelEntity)o);
		}
		return ((ServerProcessModelEntity)o);
	}

	public ServerProcessModelEntity[] getModels() {
		ArrayList list = new ArrayList();
		Iterator i = map.keySet().iterator();
		while(i.hasNext()) {
			list.add(map.get(i.next()));
		}
		ServerProcessModelEntity[] models = new ServerProcessModelEntity[list.size()];
		list.toArray(models);
		return models;
	}

	/**
	 * Completely shut down all JBoss Servers, as well as all 
	 * start, stop, and twiddle launches associated with them.
	 */
	public void terminateAllProcesses() {
		Iterator i = map.values().iterator();
		ServerProcessModelEntity entity;
		while(i.hasNext()) {
			entity = (ServerProcessModelEntity)i.next();
			entity.clearAll();
		}
	}

	public class ServerProcessModelEntity { 
		
		private String serverId;
		private HashMap processMap; // <String type, ArrayList processes>
		public ServerProcessModelEntity(String key) {
			this.serverId = key;
			processMap = new HashMap(3);
			processMap.put(JBossServerLaunchConfiguration.START, new ArrayList());
			processMap.put(JBossServerLaunchConfiguration.STOP, new ArrayList());
			processMap.put(JBossServerLaunchConfiguration.TWIDDLE, new ArrayList());
		}
		public String getServerId() {
			return serverId;
		}
		
		public void add( IProcess[] processes, String processType, ILaunchConfiguration config ) {
			ArrayList pds = (ArrayList)processMap.get(processType);
			for( int i = 0; i < processes.length; i++ ) {
				pds.add(processes[i]);
			}
		}

		public void terminateProcesses(String type) {
			IProcess[] proc = getProcesses(type);
			for( int i = 0; i < proc.length; i++ ) {
				try {
					proc[i].terminate();
				} catch( Exception e ) {
				}
			}
		}
		public IProcess[] getProcesses(String type) {
			ArrayList list = (ArrayList)processMap.get(type);
			if( list != null )
				return (IProcess[]) list.toArray(new IProcess[list.size()]);
			return new IProcess[0];
		}
		
		public void clearAll() {
			ArrayList list;
			IProcess proc;
			for( Iterator i = processMap.values().iterator(); i.hasNext();) {
				list = (ArrayList)i.next();
				for( Iterator j = list.iterator(); j.hasNext();) {
					try {
						proc = (IProcess)j.next();
						proc.terminate();
					} catch( DebugException de ) {}
					j.remove();
				}
			}
		}
	}

	
	
	public void serverAdded(IServer server) {
		if( ServerConverter.getJBossServer(server) != null ) {
			getModel(server.getId(), true);
		}
	}

	public void serverChanged(IServer server) {
	}

	public void serverRemoved(IServer server) {
		if( ServerConverter.getJBossServer(server) != null ) {
			map.remove(server.getId());
		}
	}
}
