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

import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.AbstractJBossBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;

public abstract class AbstractRSEBehaviourDelegate extends AbstractJBossBehaviourDelegate {
	private PollThread pollThread = null;
	
	@Override
	public String getBehaviourTypeId() {
		return RSEPublishMethod.RSE_ID;
	}
	
	@Override
	public void stop(boolean force) {
		if( force ) {
			forceStop();
		}

		if( LaunchCommandPreferences.isIgnoreLaunchCommand(getServer())) {
			getActualBehavior().setServerStopped();
			return;
		}

		getActualBehavior().setServerStopping();
		if (!gracefullStop().isOK()) {
			getActualBehavior().setServerStarted();
		} else {
			getActualBehavior().setServerStopped();
		}
	}
		
	public void setServerStarting() {
		pollServer(IServerStatePoller.SERVER_UP);
	}
	
	public void setServerStopping() {
		pollServer(IServerStatePoller.SERVER_DOWN);
	}
	
	protected void pollServer(final boolean expectedState) {
		IServerStatePoller poller = PollThreadUtils.getPoller(expectedState, getServer());
		this.pollThread = PollThreadUtils.pollServer(expectedState, poller, pollThread, getActualBehavior());	}
}
