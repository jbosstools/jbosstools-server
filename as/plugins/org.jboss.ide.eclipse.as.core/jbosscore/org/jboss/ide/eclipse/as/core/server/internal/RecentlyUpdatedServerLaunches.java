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
package org.jboss.ide.eclipse.as.core.server.internal;

import org.eclipse.wst.server.core.IServer;

/**
 * This is a complete hack of a class designed to get around 
 * the restrictions in UI on launch tab groups. 
 * 
 * A tab group provider cannot adjust which tabs to show based on the 
 * launch configuration, because it does not yet know the launch configuration. 
 * 
 * This class is a gigantic hack to have the server behaviours cache themselves here 
 * after setting up their launch config. In the use case of someone clicking the 
 * "show launch configuration" hyperlink, this cache can be accessed.
 * 
 */
public class RecentlyUpdatedServerLaunches {
	private static RecentlyUpdatedServerLaunches instance = null;
	public static RecentlyUpdatedServerLaunches getDefault() {
		if( instance == null )
			instance = new RecentlyUpdatedServerLaunches();
		return instance;
	}
	
	private IServer recentServer;
	public void setRecentServer(IServer server) {
		this.recentServer = server;
	}
	public IServer getRecentServer() {
		return recentServer;
	}
}
