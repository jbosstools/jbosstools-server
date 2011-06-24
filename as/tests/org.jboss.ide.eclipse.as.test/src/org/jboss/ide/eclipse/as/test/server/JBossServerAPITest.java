/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.test.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.test.util.ServerRuntimeUtils;

public class JBossServerAPITest extends ServerRuntimeUtils {
	protected IServer currentServer;
	public void setUp() {
	}
	
	public void tearDown() {
		try {
			if( currentServer != null )
				currentServer.delete();
		} catch( CoreException ce ) {
			// report
		}
	}
	public void testJBossServerGetConfigDirectory() {
		try {
			currentServer = create42Server();
			JBossServer jbs = (JBossServer)currentServer.getAdapter(JBossServer.class);
			IRuntime rt = currentServer.getRuntime();
			IJBossServerRuntime jbsrt = (IJBossServerRuntime)rt.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
			String configName = jbsrt.getJBossConfiguration();
			assertTrue(jbs.getConfigDirectory().endsWith(configName));
		} catch( CoreException ce ) {
			fail("Failed during setUp for " + getName() + ": " + ce.getMessage());
		}
	}

}
