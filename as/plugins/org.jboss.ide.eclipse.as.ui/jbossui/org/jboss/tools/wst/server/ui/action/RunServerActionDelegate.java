/*******************************************************************************
 * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.tools.wst.server.ui.action;

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.internal.view.servers.StartAction;

public class RunServerActionDelegate extends AbstractServerActionDelegate {

	private StartAction delegate;
	public void init(IWorkbenchWindow window) {
		super.init(window);
		update();
	}
	
	protected void doRun() {
		IServer server = ServerManager.getInstance().getSelectedServer();
		if(server == null)
			return;
		delegate = new StartAction(window.getShell(), getSelectionProvider(), getLaunchMode());
		if( delegate.accept(server))
			delegate.perform(server);
		updateAll();
	}
	
	protected String getLaunchMode() {
		return ILaunchManager.RUN_MODE;
	}

	protected boolean isActionEnabled() {
		IServer selected = ServerManager.getInstance().getSelectedServer();
		return (selected != null 
				&& selected.getServerState() != IServer.STATE_STARTING);
	}

	protected String computeToolTip() {
		IServer selected = ServerManager.getInstance().getSelectedServer();
		String name = selected == null ? "" : selected.getName(); //$NON-NLS-1$
		if( selected.getServerState() == IServer.STATE_STARTED)
			return NLS.bind(ServerActionMessages.RESTART_IN_RUN_MODE, name);
		return NLS.bind(ServerActionMessages.START_IN_RUN_MODE, name);
	}	
}
