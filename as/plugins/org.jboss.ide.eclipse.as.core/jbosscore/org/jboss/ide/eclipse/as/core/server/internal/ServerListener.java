/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal;

import java.io.File;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

public class ServerListener extends UnitedServerListener {
	private static ServerListener instance;
	public static ServerListener getDefault() {
		if( instance == null )
			instance = new ServerListener();
		return instance;
	}
	
	public boolean canHandleServer(IServer server) {
		return true;
	}

	public void init(IServer server) {
		initUnmanagedServerState(server);
	}
	
	protected void initUnmanagedServerState(IServer server) {
		DelegatingServerBehavior beh = ServerConverter.getJBossServerBehavior(server);
		if( beh != null ) {
			String ignoreLaunch = server.getAttribute(IJBossToolingConstants.IGNORE_LAUNCH_COMMANDS, Boolean.toString(false));
			if( new Boolean(ignoreLaunch).booleanValue()) {
				// Assume started already
				beh.setServerStarted();
			}
		}
	}
	public void serverChanged(IServer server) {
		// double check if the user toggled the 'assume started' flag to true
		initUnmanagedServerState(server);
	}

	public void serverAdded(IServer server) {
		ServerUtil.createStandardFolders(server);
		initUnmanagedServerState(server);
	}

	public void serverRemoved(IServer server) {
		// delete metadata area
		File f = JBossServerCorePlugin.getServerStateLocation(server).toFile();
		FileUtil.safeDelete(f);
	}
}
