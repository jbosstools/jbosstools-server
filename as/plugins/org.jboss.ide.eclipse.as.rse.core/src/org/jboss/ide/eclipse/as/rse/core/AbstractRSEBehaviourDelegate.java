/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 * 
 * TODO: Logging and Progress Monitors
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.rse.core;

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.jboss.ide.eclipse.as.core.extensions.events.ServerLogger;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.AbstractJBossBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;
import org.jboss.ide.eclipse.as.rse.core.RSEHostShellModel.ServerShellModel;

public abstract class AbstractRSEBehaviourDelegate extends AbstractJBossBehaviourDelegate {
	private PollThread pollThread = null;
	public String getBehaviourTypeId() {
		return RSEPublishMethod.RSE_ID;
	}
	
	@Override
	public void stop(boolean force) {
		if( force ) {
			forceStop();
		}

		if( LaunchCommandPreferences.isIgnoreLaunchCommand(getServer())) {
			setServerStopping();
			serverStopped();
			return;
		}

		setServerStopping();
		if (!gracefullStop().isOK()) {
			serverStarted();
		} else {
			serverStopped();
		}
	}
	
	@Override
	protected abstract void forceStop();

	@Override
	protected abstract IStatus gracefullStop();
	
	public void setServerStarting() {
		pollServer(IServerStatePoller.SERVER_UP);
	}
	
	public void serverStarted() {
		getActualBehavior().setServerStarted();
	}

	public void setServerStopping() {
		getActualBehavior().setServerStopping();
		pollServer(IServerStatePoller.SERVER_DOWN);
	}
	
	public void serverStopped() {
		getActualBehavior().setServerStopped();
	}

	protected void pollServer(final boolean expectedState) {
		IServerStatePoller poller = PollThreadUtils.getPoller(expectedState, getServer());
		this.pollThread = PollThreadUtils.pollServer(expectedState, poller, pollThread, getActualBehavior());	}
}
