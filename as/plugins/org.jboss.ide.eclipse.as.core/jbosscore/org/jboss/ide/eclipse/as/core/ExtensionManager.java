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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.jboss.ide.eclipse.as.core.runtime.server.IServerPollerTimeoutListener;

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
	
	private HashMap pollerListeners = null;
	public IServerPollerTimeoutListener[] getTimeoutListeners(String pollerClass) {
		if( pollerListeners == null ) 
			loadTimeoutListeners();
		ArrayList list = (ArrayList)pollerListeners.get(pollerClass);
		if( list != null ) {
			return (IServerPollerTimeoutListener[]) list.toArray(new IServerPollerTimeoutListener[list.size()]);
		}
		return new IServerPollerTimeoutListener[0];
	}
	
	protected void loadTimeoutListeners() {
		pollerListeners = new HashMap();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(JBossServerCorePlugin.PLUGIN_ID, "pollerTimeoutListener");
		for( int i = 0; i < cf.length; i++ ) {
			try {
				String poller = cf[i].getAttribute("pollerType");
				Object listener = cf[i].createExecutableExtension("listener");
				
				ArrayList list = (ArrayList)pollerListeners.get(poller);
				if( list == null ) list = new ArrayList();
				list.add(listener);
				pollerListeners.put(poller, list);
			} catch( CoreException ce ) {
				ce.printStackTrace();
			}
		}
	}

}
