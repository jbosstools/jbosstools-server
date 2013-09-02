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

/**
 * This interface is used to access any details that differ based on 
 * server version AND server mode. The extended properties framework
 * is for details which are agnostic to server mode (local vs rse), 
 * but this interface is used for accessing data which changes in
 * its storage and retrieval details based on server mode, or simply
 * have different defaults depending on the mode.  
 * 
 * This provides a clear interface to access fields that may 
 * change depending on the server mode, such as server home, 
 * configuration name, configuration file, etc. 
 * 
 * This interface reserves the right to add constants
 * with the prefix PROP_  
 * 
 * Not all properties are valid for all server versions. 
 * 
 * This interface is provisional API and is not intendd to be implemented
 * by clients outside of ASTools at this time. 
 */
public interface IServerModeDetails {
	/**
	 * Get the server home 
	 */
	public static final String PROP_SERVER_HOME = "PROP_SERVER_HOME";
	
	/**
	 * Get the configuration name, such as 'default' or 'all'
	 */
	public static final String PROP_CONFIG_NAME = "PROP_CONFIG_NAME";
	
	/**
	 * The full path of the folder this config is located in, for example:
	 *   /home/rob/tmp/default_copy1  would return 
	 *   /home/rob/tmp/
	 *       whereas 
	 *   /home/rob/apps/jboss/server/default_copy3 would return 
	 *   /home/rob/apps/jboss/server
	 *   
	 *   This property behaves different for AS7-style servers, 
	 *   which should return 
	 *   /home/rob/apps/jboss/standalone/configuration
	 *   
	 * @return
	 */	
	public static final String PROP_CONFIG_LOCATION = "PROP_CONFIG_LOCATION";
	
	/**
	 * Get the AS7-style configuration file being used, 
	 * such as standalone.xml
	 */
	public static final String PROP_AS7_CONFIG_FILE = "PROP_AS7_CONFIG_FILE";

	/**
	 * Get the default folder the server provides as a deployment folder given the
	 * current server version and mode information
	 */
	public static final String PROP_SERVER_DEPLOYMENTS_FOLDER = "PROP_SERVER_DEPLOYMENTS_FOLDER";

	
	/**
	 * Retrieve the server-mode-relative value for the given property
	 * @param prop
	 * @return
	 */
	public String getProperty(String prop);
	
}
