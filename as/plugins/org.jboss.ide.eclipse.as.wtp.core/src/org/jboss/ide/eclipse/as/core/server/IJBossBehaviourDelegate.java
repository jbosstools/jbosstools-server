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

package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IModule;

/**
 * This class represents jbt-specific delegates to the 
 * ServerBehaviorDelegate, so that how our servers behave can be
 * changed based on what 'mode' the server is currently in. 
 *   ex:  local, rse, management, etc... 
 */
public interface IJBossBehaviourDelegate {

	public String getBehaviourTypeId();

	public void setActualBehaviour(IDelegatingServerBehavior actualBehaviour);

	public void stop(boolean force);

	public void publishStart(final IProgressMonitor monitor) throws CoreException;

	public void publishFinish(final IProgressMonitor monitor) throws CoreException;

	public void onServerStarting();

	public void onServerStopping();

	public void onServerStarted();

	public void onServerStopped();

	
	/**
	 * It is possible this method is not sufficient.
	 * Some server modes may be able to stop a server, or restart a server, 
	 * but not start a server. 
	 * 
	 *  We may need an additional interface to expand this without breaking API
	 * @param launchMode
	 * @return
	 */
	public IStatus canChangeState(String launchMode);
		
	public void dispose();
}
