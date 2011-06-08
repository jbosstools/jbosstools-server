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
package org.jboss.ide.eclipse.as.core.server.internal.v7;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jboss.ide.eclipse.as.core.extensions.polling.WebPortPoller;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.LocalJBossBehaviorDelegate;
import org.jboss.ide.eclipse.as.core.server.internal.PollThread;
import org.jboss.ide.eclipse.as.core.util.PollThreadUtils;

public class LocalJBoss7BehaviorDelegate extends LocalJBossBehaviorDelegate {
	public IStatus canChangeState(String launchMode) {
		return Status.OK_STATUS;
	}
	protected void pollServer(final boolean expectedState) {
		if( pollThread != null )
			pollThread.cancel();
		
		IServerStatePoller poller = PollThreadUtils.getPoller(expectedState, getServer());
		// IF shutting down a process started OUTSIDE of eclipse, force use the web poller, 
		// since there's no process watch for shutdowns
		if( !expectedState && process == null ) 
			poller = PollThreadUtils.getPoller(WebPortPoller.WEB_POLLER_ID, false, getServer());
		
		this.pollThread = new PollThread(expectedState, poller, getActualBehavior());
		pollThread.start();
	}
}
