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
package org.jboss.ide.eclipse.as.core.extensions.jmx;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;

/**
 * A repository for classloaders that relate to servers, 
 * specifically jmx. It basically loads teh jboss-clientall.jar. 
 * 
 * 
 * @author Rob Stryker
 *
 */
public class JMXClassLoaderRepository {
	protected static JMXClassLoaderRepository instance;
	public static JMXClassLoaderRepository getDefault() {
		if( instance == null ) {
			instance = new JMXClassLoaderRepository();
		}
		return instance;
	}
	
	protected HashMap<String, ClassLoader> idToLoader;
	protected HashMap<String, ArrayList<Object>> idToConcerned;
	protected JMXClassLoaderRepository() {
		idToLoader = new HashMap<String, ClassLoader>();
		idToConcerned = new HashMap<String, ArrayList<Object>>();
	}
	
	/**
	 * Add a concerned citizen to the list of said citizens.
	 * These are people who may at any time ask to access
	 * the classloader. 
	 * 
	 * @param server
	 * @param concerned
	 */
	public void addConcerned(IServer server, Object concerned) {
		ArrayList<Object> list = idToConcerned.get(server.getId());
		if( list == null ) {
			list = new ArrayList<Object>();
			idToConcerned.put(server.getId(), list);
		}
		if( !list.contains(concerned))
			list.add(concerned);
	}
	
	/**
	 * Removes an element from the list of concerned citizens.
	 * If no one cares about this server any longer, remove
	 * its classloader cache and forget about it.
	 * 
	 * @param server
	 * @param concerned
	 */
	public void removeConcerned(IServer server, Object concerned) {
		ArrayList<Object> list = idToConcerned.get(server.getId());
		if( list != null ) {
			list.remove(concerned);
			if( list.size() == 0 ) {
				idToConcerned.remove(server.getId());
				idToLoader.remove(server.getId());
			}
		}
	}
	
	/**
	 * Get the classloader for some server.
	 * If there are 0 concerned citizens, null should be returned.
	 * @param server
	 * @return
	 */
	public ClassLoader getClassLoader(IServer server) {
		ClassLoader loader = idToLoader.get(server.getId());
		if( loader == null && anyoneCares(server)) {
			loadClassLoader(server);
		}
		return idToLoader.get(server.getId());
	}
	
	/**
	 * Actually create and store the classloader
	 * @param s
	 */
	protected void loadClassLoader(IServer s) {
		try {
			IRuntime rt = s.getRuntime();
			IPath loc = rt.getLocation();
			URL url = loc.append("client").append("jbossall-client.jar")
					.toFile().toURI().toURL();
			URLClassLoader loader = new URLClassLoader(new URL[] { url, }, 
					Thread.currentThread().getContextClassLoader());
			idToLoader.put(s.getId(), loader);
		} catch (MalformedURLException murle) {
			JBossServerCorePlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
							"Cannot load JMX classes for server: " + s.getName(), murle));
		}
	}

	/**
	 * Are there any concerned citizens for this server?
	 * @param server
	 * @return
	 */
	protected boolean anyoneCares(IServer server) {
		ArrayList<Object> list = idToConcerned.get(server.getId());
		if( list != null && list.size() > 0 ) return true;
		return false;
	}
}
