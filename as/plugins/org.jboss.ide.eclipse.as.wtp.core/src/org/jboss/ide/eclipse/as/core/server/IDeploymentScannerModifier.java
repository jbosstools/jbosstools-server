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
	 * Update the deployment scanners for this server
	 * 
	 * @param server the server we wish to update scanners for
	 */
	public void updateDeploymentScanners(IServer server);
	
	/**
	 * Get the job which can Update the deployment scanners for this server
	 * @param server
	 * @return
	 */
	public Job getUpdateDeploymentScannerJob(IServer server);	
	
}
