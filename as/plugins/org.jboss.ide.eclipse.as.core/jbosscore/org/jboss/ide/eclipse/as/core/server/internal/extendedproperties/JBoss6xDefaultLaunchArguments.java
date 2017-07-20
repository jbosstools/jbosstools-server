/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.BIN;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.LOGGING_PROPERTIES;

import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBoss6Server;
import org.jboss.ide.eclipse.as.core.server.IJBossServer;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public class JBoss6xDefaultLaunchArguments extends JBoss5xDefaultLaunchArguments {
	public JBoss6xDefaultLaunchArguments(IRuntime rt) {
		super(rt);
	}
	public JBoss6xDefaultLaunchArguments(IServer server) {
		super(server);
	}

	protected String getShutdownServerUrl() {
		IJBossServer jbs = ServerConverter.getJBossServer(server);
		IJBoss6Server server6 = (IJBoss6Server) server.loadAdapter(IJBoss6Server.class, null);
		return "service:jmx:rmi:///jndi/rmi://" + jbs.getHost() + ":" + server6.getJMXRMIPort() + "/jmxrmi"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	protected String getJBossJavaFlags() {
		String ret = super.getJBossJavaFlags();
		
		// It's possible the remote server is of a different version, but we 
		// EXPECT the local dev copy is the same distribution. 
		IPath home = getServerHome();
		if( runtime != null ) {
			/// use the local version to know what version, since we can't actually look at the remote
			String version = new ServerBeanLoader(runtime.getLocation().toFile()).getFullServerVersion();
			if( version.startsWith(IJBossToolingConstants.V6_1)) {
				// Only relevent for as6.1
				ret += SYSPROP + LOGGING_CONFIG_PROP + EQ + QUOTE + FILE_COLON + 
						home.append(BIN).append(LOGGING_PROPERTIES).toOSString() + QUOTE + SPACE;
			}
		}
		return ret;
	}

}
