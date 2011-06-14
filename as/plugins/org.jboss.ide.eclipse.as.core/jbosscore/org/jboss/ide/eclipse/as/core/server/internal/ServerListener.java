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
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

public class ServerListener extends UnitedServerListener {
	private static ServerListener instance;
	public static ServerListener getDefault() {
		if( instance == null )
			instance = new ServerListener();
		return instance;
	}
	
	public void serverAdded(IServer server) {
		ServerUtil.createStandardFolders(server);
	}

	public void serverRemoved(IServer server) {
		// delete metadata area
		File f = JBossServerCorePlugin.getServerStateLocation(server).toFile();
		FileUtil.safeDelete(f);
	}
}
