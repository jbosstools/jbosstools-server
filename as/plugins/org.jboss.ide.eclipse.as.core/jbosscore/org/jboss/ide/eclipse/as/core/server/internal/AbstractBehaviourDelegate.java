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
import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.as.core.modules.ResourceModuleResourceUtil;
import org.jboss.ide.eclipse.as.core.server.IDelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.IJBossBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.server.IModulePathFilter;
import org.jboss.ide.eclipse.as.core.server.IServerModeDetails;

/**
 * A complete stub implementation of the interface
 */
/**
 * 
 * @deprecated
 */
public abstract class AbstractBehaviourDelegate implements IJBossBehaviourDelegate {

	protected IDelegatingServerBehavior actualBehavior;
	
	@Override
	public void setActualBehaviour(IDelegatingServerBehavior actualBehaviour) {
		this.actualBehavior = actualBehaviour;
	}

	@Override
	public String getBehaviourTypeId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void stop(boolean force) {
		// TODO Auto-generated method stub

	}

	@Override
	public void publishStart(IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void publishFinish(IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onServerStarting() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onServerStopping() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onServerStarted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onServerStopped() {
		// TODO Auto-generated method stub

	}

	@Override
	public IStatus canChangeState(String launchMode) {
		// TODO Auto-generated method stub
		return Status.CANCEL_STATUS;
	}

	@Override
	public String getDefaultStopArguments() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @since 2.4
	 */
	public IModulePathFilter getPathFilter(IModule[] moduleTree) {
		return ResourceModuleResourceUtil.findDefaultModuleFilter(moduleTree[moduleTree.length-1]);
	}
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}
	
	/* 
	 * Not added to the interface, but is added to the abstract class. 
	 * Subclasses may override to return the proper information. 
	 */
	/**
	 * @since 2.5
	 */
	public IServerModeDetails getServerModeDetails() {
		return null;
	}

}
