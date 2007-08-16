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
package org.jboss.ide.eclipse.as.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.jboss.ide.eclipse.as.core.runtime.server.polling.ServerStatePollerType;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class ExtensionManager {
	private static ExtensionManager instance;
	public static ExtensionManager getDefault() {
		if( instance == null ) 
			instance = new ExtensionManager();
		return instance;
	}
	
	private HashMap pollers;
	public void loadPollers() {
		pollers = new HashMap();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(JBossServerCorePlugin.PLUGIN_ID, "pollers");
		for( int i = 0; i < cf.length; i++ ) {
			pollers.put(cf[i].getAttribute("id"), new ServerStatePollerType(cf[i]));
		}
	}
	public ServerStatePollerType getPollerType(String id) {
		if( pollers == null ) 
			loadPollers();
		return (ServerStatePollerType)pollers.get(id);
	}
	public ServerStatePollerType[] getStartupPollers() {
		if( pollers == null ) 
			loadPollers();
		ArrayList list = new ArrayList();
		Iterator i = pollers.values().iterator();
		ServerStatePollerType type;
		while(i.hasNext()) {
			type = (ServerStatePollerType)i.next();
			if( type.supportsStartup())
				list.add(type);
		}
		return (ServerStatePollerType[]) list.toArray(new ServerStatePollerType[list.size()]);
	}
	public ServerStatePollerType[] getShutdownPollers() {
		if( pollers == null ) 
			loadPollers();
		ArrayList list = new ArrayList();
		Iterator i = pollers.values().iterator();
		ServerStatePollerType type;
		while(i.hasNext()) {
			type = (ServerStatePollerType)i.next();
			if( type.supportsShutdown() )
				list.add(type);
		}
		return (ServerStatePollerType[]) list.toArray(new ServerStatePollerType[list.size()]);
	}

}
