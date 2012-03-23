/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.jmx.integration;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;

/**
 * A repository for classloaders that relate to servers, 
 * specifically jmx. It basically loads teh jboss-clientall.jar. 
 * 
 * 
 * @author Rob Stryker
 *
 */
public class JMXClassLoaderRepository {
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
			URLClassLoader loader = createClassLoader(s);
			idToLoader.put(s.getId(), loader);
		} catch (MalformedURLException murle) {
			JBossServerCorePlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
							NLS.bind(Messages.loadJMXClassesFailed, s.getName()), murle));
		}
	}

	protected URLClassLoader createClassLoader(IServer s) throws MalformedURLException {
		IRuntime rt = s.getRuntime();
		IPath loc = rt.getLocation();
		URL url = loc.append(IJBossRuntimeResourceConstants.CLIENT)
				.append(IJBossRuntimeResourceConstants.JBOSSALL_CLIENT_JAR)
				.toFile().toURI().toURL();
		URLClassLoader loader = new URLClassLoader(new URL[] { url, }, 
				Thread.currentThread().getContextClassLoader());
		return loader;
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
