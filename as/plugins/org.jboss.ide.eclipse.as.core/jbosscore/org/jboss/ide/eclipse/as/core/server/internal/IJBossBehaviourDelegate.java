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

package org.jboss.ide.eclipse.as.core.server.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public interface IJBossBehaviourDelegate {

	public String getBehaviourTypeId();

	public void setActualBehaviour(DelegatingServerBehavior actualBehaviour);

	public void stop(boolean force);

	public void publishStart(final IProgressMonitor monitor) throws CoreException;

	public void publishFinish(final IProgressMonitor monitor) throws CoreException;

	public void serverIsStarting();

	public void serverIsStopping();

	public IStatus canChangeState(String launchMode);

	public String getDefaultStopArguments() throws CoreException; 
}
