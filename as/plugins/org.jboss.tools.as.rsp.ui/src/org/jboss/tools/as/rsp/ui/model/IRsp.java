/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.rsp.ui.model;

/**
 * Represents an RSP instance.
 */
public interface IRsp {
	/**
	 * Get the core model for the UI
	 * 
	 * @return
	 */
	IRspCore getModel();

	/**
	 * Get the type of this RSP
	 * 
	 * @return
	 */
	public IRspType getRspType();

	/**
	 * Get the latest version of this RSP that can be discovered
	 * 
	 * @return
	 */
	public String getLatestVersion();

	/**
	 * Get the currently-installed version of this rsp, or null if not found
	 * 
	 * @return
	 */
	public String getInstalledVersion();

	/**
	 * Start this rsp.
	 * 
	 * @return The connection information (host/port) for the running instance or
	 *         null
	 */
	public ServerConnectionInfo start();

	/**
	 * Stop the RSP
	 */
	public void stop();

	/**
	 * Forcefully terminate the RSP process
	 */
	public void terminate();

	/**
	 * Get the current state of the rsp
	 * 
	 * @return
	 */
	public IRspCore.IJServerState getState();

	/**
	 * Return whether the rsp process was launched from this extension
	 * 
	 * @return
	 */
	public boolean wasLaunched();

	/**
	 * Return whether the RSP exists at the expected location or not
	 * 
	 * @return
	 */
	boolean exists();

	/**
	 * Download the latest version of this RSP
	 */
	void download();
}
