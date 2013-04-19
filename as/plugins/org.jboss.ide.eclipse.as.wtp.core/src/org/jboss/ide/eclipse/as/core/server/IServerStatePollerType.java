/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.server;

/**
 * @noimplement This interface is not intended to be implemented by clients.
 * 				Methods may be added in an api-compatible fashion.
 */
public interface IServerStatePollerType {
	/**
	 * Is this poller valid for startup?
	 * @return true if valid for polling startup
	 */
	public boolean supportsStartup();
	/**
	 * Is this poller valid for shutdown
	 * @return true if valid for polling shutdown
	 */
	public boolean supportsShutdown();
	
	/**
	 * Get the poller's name
	 * @return
	 */
	public String getName();
	
	/**
	 * Get the poller's ID
	 * @return
	 */
	public String getId(); 
	
	/**
	 * Get a list of comma-separated server type id's
	 * that this poller is valid for. 
	 * 
	 * Null or empty string indicates it is valid for all types.
	 * @return
	 */
	public String getServerTypes();
	
	/**
	 * Get a list of comma-separated server modes (local, rse, etc)
	 * that this poller is valid for. 
	 * 
	 * A null or empty string indicates it is valid for all modes
	 * @return
	 */
	public String getServerModes();
	
	/**
	 * Create an instance of this poller
	 * @return
	 */
	public IServerStatePoller createPoller(); 
}
