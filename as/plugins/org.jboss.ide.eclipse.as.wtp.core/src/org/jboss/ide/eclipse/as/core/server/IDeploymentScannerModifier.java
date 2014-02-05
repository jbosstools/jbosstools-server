/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.wst.server.core.IServer;

public interface IDeploymentScannerModifier {
	/**
	 * Update the deployment scanners for this server. 
	 * This will be done synchronously in the current thread. 
	 * 
	 * @param server the server we wish to update scanners for
	 */
	public void updateDeploymentScanners(IServer server);
	
	/**
	 * Get the job which can Update the deployment scanners for this server,
	 * or null if unsupported. 
	 * 
	 * @param server
	 * @return
	 */
	public Job getUpdateDeploymentScannerJob(IServer server);	
	
	/**
	 * Remove scanners which have been added
	 * This will be done synchronously in the current thread. 
	 * @param server
	 */
	public void removeAddedDeploymentScanners(IServer server);
	
	/**
	 * Get a job which may remove scanners that have been added, 
	 * or null if unsupported. 
	 * 
	 * @param server
	 * @return
	 */
	public Job getRemoveDeploymentScannerJob(IServer server);
	
	/**
	 * Suspend all deployment scanners from running
	 * @param server
	 */
	public void suspendScanners(IServer server) throws UnsupportedOperationException;
	
	/**
	 * Set all deployment scanners to enabled
	 * @param server
	 */
	public void resumeScanners(IServer server)throws UnsupportedOperationException;
	
}
