/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.wtp.core.server.publish;

import java.util.ArrayList;

import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.PublishServerJob;
import org.eclipse.wst.server.core.internal.Server;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;

/**
 * Initiate a full publish on a server's module
 * @author rob
 *
 */
public class FullPublishJobScheduler {
	private IServer server;
	private IModule[] rootModules;
	public FullPublishJobScheduler(IServer server, IModule[] rootModules) {
		this.server = server;
		this.rootModules = rootModules;
	}
	
	public void schedule() {
		// Make sure all modules are marked as requiring a full publish
		for( int i = 0; i < rootModules.length; i++ ) {
			setModuleRequiresFullPublish(rootModules[i]);
		}
		// Launch an incremental publish
		new PublishServerJob(server, IServer.PUBLISH_INCREMENTAL, true).schedule();
	}
	
	private void setModuleRequiresFullPublish(IModule module) {
		IModule[] module2 = new IModule[]{module};
		((Server)server).setModulePublishState(module2, IServer.PUBLISH_STATE_FULL);
		ArrayList<IModule[]> allChildren = ServerModelUtilities.getDeepChildren(server, module2);
		for( int j = 0; j < allChildren.size(); j++ ) {
			((Server)server).setModulePublishState((IModule[])allChildren.get(j), IServer.PUBLISH_STATE_FULL);
		}
	}
}
