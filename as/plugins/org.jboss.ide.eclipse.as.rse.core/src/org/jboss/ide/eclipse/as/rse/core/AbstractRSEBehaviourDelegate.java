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
import org.jboss.ide.eclipse.as.core.util.LaunchCommandPreferences;

public abstract class AbstractRSEBehaviourDelegate extends AbstractJBossBehaviourDelegate {
	
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
			setServerStopped();
			return;
		}

		setServerStopping();
		if (!gracefullStop().isOK()) {
			setServerStarted();
		} else {
			setServerStopped();
		}
	}
		
	/**
	 * ATTENTION: don't call this directly, use {@link #getActualBehavior().getServerStarting()} instead. 
	 * if we would call the delegating server behavior here to set it's state, we would cause an infinite loop.
	 */
	public void serverIsStarting() {
		pollServer(IServerStatePoller.SERVER_UP);
	}
	
	/**
	 * ATTENTION: don't call this directly, use {@link #getActualBehavior().getServerStopping()} instead. 
	 * if we would call the delegating server behavior here to set it's state, we would cause an infinite loop.
	 */
	public void serverIsStopping() {
		pollServer(IServerStatePoller.SERVER_DOWN);
	}
	
}
