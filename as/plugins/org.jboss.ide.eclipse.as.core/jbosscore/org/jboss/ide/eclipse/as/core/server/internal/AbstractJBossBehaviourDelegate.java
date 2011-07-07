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
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IJBoss6Server;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public abstract class AbstractJBossBehaviourDelegate implements IJBossBehaviourDelegate {

	private DelegatingServerBehavior actualBehavior;
	
	@Override
	public void setActualBehaviour(DelegatingServerBehavior actualBehaviour) {
		this.actualBehavior = actualBehaviour;
	}
	
	public DelegatingServerBehavior getActualBehavior() {
		return actualBehavior;
	}

	public IServer getServer() {
		return actualBehavior.getServer();
	}

	@Override
	public void publishStart(IProgressMonitor monitor) throws CoreException {
	}

	@Override
	public void publishFinish(IProgressMonitor monitor) throws CoreException {		
	}

	protected abstract void forceStop();

	protected abstract IStatus gracefullStop();
	
	public void serverIsStarting() {
	}
	
	public void serverIsStopping() {
	}

	@Override
	public IStatus canChangeState(String launchMode) {
		return Status.OK_STATUS;
	}

	@Override
	public String getDefaultStopArguments() throws CoreException {
		
		String runtimeTypeId = getServer().getRuntime().getRuntimeType().getId();
		JBossServer jbs = ServerConverter.checkedGetJBossServer(getServer(), JBossServer.class);
		String serverUrl;
		if (runtimeTypeId.equals(IJBossToolingConstants.AS_60)){
			IJBoss6Server server6 = ServerConverter.checkedGetJBossServer(getServer(), IJBoss6Server.class);
			serverUrl = "service:jmx:rmi:///jndi/rmi://" + jbs.getHost() + ":" + server6.getJMXRMIPort() + "/jmxrmi"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else {
			serverUrl = jbs.getHost() + ":" + jbs.getJNDIPort(); //$NON-NLS-1$
		}
		String args = IJBossRuntimeConstants.SHUTDOWN_STOP_ARG 
				+ IJBossRuntimeConstants.SPACE;
		args += IJBossRuntimeConstants.SHUTDOWN_SERVER_ARG 
				+ IJBossRuntimeConstants.SPACE 
				+ serverUrl 
				+ IJBossRuntimeConstants.SPACE;
		if( jbs.getUsername() != null && !jbs.getUsername().equals(""))  //$NON-NLS-1$
			args += IJBossRuntimeConstants.SHUTDOWN_USER_ARG 
			+ IJBossRuntimeConstants.SPACE + jbs.getUsername() + IJBossRuntimeConstants.SPACE;
		if( jbs.getPassword() != null && !jbs.getPassword().equals(""))  //$NON-NLS-1$
			args += IJBossRuntimeConstants.SHUTDOWN_PASS_ARG 
			+ IJBossRuntimeConstants.SPACE + jbs.getPassword() + IJBossRuntimeConstants.SPACE;
		return args;
	}

}
