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
package org.jboss.ide.eclipse.as.management.core;

import org.eclipse.wst.server.core.IServer;

public interface IAS7ManagementDetails {
	
	/**
	 * A property key. The desired value should be an Integer object.
	 * The property should be in milliseconds. 
	 */
	public static final String PROPERTY_TIMEOUT = "PROPERTY_TIMEOUT";
	
	/**
	 * Get the desired host for this connection
	 * @return
	 */
	public String getHost();
	
	/**
	 * Get the desired port for this connection
	 * @return
	 */
	public int getManagementPort();	
	
	/**
	 * Get the username to be used
	 * @return
	 */
	public String getManagementUsername();
	/**
	 * Get the password to be used
	 * @return
	 */
	public String getManagementPassword();	
	
	/**
	 * Get a callback handler to provide further credentials if
	 * the first ones fail.
	 * 
	 * @param prompts
	 * @return
	 * @throws UnsupportedOperationException
	 */
	public String[] handleCallbacks(String[] prompts) throws UnsupportedOperationException;	
	
	/**
	 * Get the IServer for which this connection is being made
	 * @return
	 */
	public IServer getServer();
	
	/**
	 * Get a property value
	 * @return
	 */
	public Object getProperty(String key);
}
