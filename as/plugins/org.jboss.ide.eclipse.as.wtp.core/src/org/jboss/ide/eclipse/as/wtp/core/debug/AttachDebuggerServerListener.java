/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.wtp.core.debug;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.jboss.ide.eclipse.as.core.server.UnitedServerListener;
import org.jboss.ide.eclipse.as.wtp.core.ASWTPToolsPlugin;

public class AttachDebuggerServerListener implements IServerListener {
	private ILaunch debuggerLaunch = null;
	private boolean registerDebuggerLaunch;
	
	public AttachDebuggerServerListener(boolean registerDebuggerLaunch) {
		this.registerDebuggerLaunch = registerDebuggerLaunch;
	}
	
	public void serverChanged(ServerEvent event) {
		if( UnitedServerListener.serverSwitchesToState(event, IServer.STATE_STARTED)) {
			serverStarted(event);
		} else if( UnitedServerListener.serverSwitchesToState(event, IServer.STATE_STOPPED)) {
			event.getServer().removeServerListener(this);
		}
	}
	
	protected void serverStarted(ServerEvent event) {
		event.getServer().removeServerListener(this);
		IServer s = event.getServer();
		int debugPort = getDebugPort(s);
		try {
			debuggerLaunch = attachRemoteDebugger(event.getServer(), debugPort, new NullProgressMonitor());
			
		} catch(CoreException ce) {
			ASWTPToolsPlugin.pluginLog().logError(ce);
		}
	}
	
	protected int getDebugPort(IServer s) {
		return RemoteDebugUtils.get().getDebugPort(s);
	}
	
	protected ILaunch attachRemoteDebugger(IServer server, int debugPort, IProgressMonitor mon) throws CoreException {
		return RemoteDebugUtils.get().attachRemoteDebugger(server,  debugPort, registerDebuggerLaunch, mon);
	}
	
	protected boolean shouldRegisterDebuggerLaunch() {
		return registerDebuggerLaunch;
	}
	public ILaunch getLaunch() {
		return debuggerLaunch;
	}
}