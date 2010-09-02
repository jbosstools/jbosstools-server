/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IServerStatePoller;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServerBehavior.JBossBehaviourDelegate;

public abstract class AbstractJBossBehaviourDelegate implements JBossBehaviourDelegate {

	private JBossServerBehavior actualBehavior;
	public void setActualBehaviour(JBossServerBehavior actualBehaviour) {
		this.actualBehavior = actualBehaviour;
	}
	
	public JBossServerBehavior getActualBehavior() {
		return actualBehavior;
	}

	public IServer getServer() {
		return actualBehavior.getServer();
	}

	public abstract void stop(boolean force);
	public void publishStart(IProgressMonitor monitor) throws CoreException {
	}

	public void publishFinish(IProgressMonitor monitor) throws CoreException {
	}

	public void serverStarting() {
		actualBehavior.setServerStarting();
	}
	
	public void serverStopping() {
		actualBehavior.setServerStopping();
	}

	public IStatus canChangeState(String launchMode) {
		return Status.OK_STATUS;
	}

}
