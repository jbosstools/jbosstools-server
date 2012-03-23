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

import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;

public class AS71JMXClassLoaderRepository extends JMXClassLoaderRepository {
	protected URLClassLoader createClassLoader(IServer s) throws MalformedURLException {
		IRuntime rt = s.getRuntime();
		IPath loc = rt.getLocation();
		IPath clientJar = findClientJar(loc);
		if( clientJar != null ) {
			URL url = clientJar.toFile().toURI().toURL();
			URLClassLoader loader = new URLClassLoader(new URL[] { url, }, 
					Thread.currentThread().getContextClassLoader());
			return loader;
		}
		return null;
	}
	
	public IPath findClientJar(IPath root) {
		IPath p2 = root.append(IJBossRuntimeResourceConstants.BIN)
				.append(IJBossRuntimeResourceConstants.CLIENT);
		String[] children = p2.toFile().list();
		for( int i = 0; i < children.length; i++ ) {
			if( children[i].endsWith(".jar") && children[i].startsWith("jboss-client-"))
				return p2.append(children[i]);
		}
		return null;
	}
}
