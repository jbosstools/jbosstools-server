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
package org.jboss.tools.as.core.server.controllable.systems;

import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;


/**
 * This class is for the purposes of getting and setting 
 * standard options for deployment for filesystem-based 
 * servers, including the location for modules to be deployed on the server.
 * This is *not* designed for per-module overrides.
 *  
 * The system interface in charge of per-module overrides is
 * {@link IModuleDeployPathController}
 * 
 * This system is designed to be used during publish actions
 * that require filesystem-based deployment, whether local or remote.
 * 
 * This class will have getters that work on either an IServer or IServerWorkingCopy.
 * The class will have setters that work only on an IServerWorkingCopy.
 * Attempts to use the setters may throw IllegalStateException, depending on 
 * the implementation
 * 
 * @since 3.0
 */
public interface IDeploymentOptionsController extends ISubsystemController {
	
	public static final String SYSTEM_ID = "deploymentOptions"; //$NON-NLS-1$
	
	
	/**
	 * This is an optional property for use when targeting a remote windows system 
	 * from a linux machine, or a remote linux machine from a windows machine. 
	 * The value of this property should be a java.lang.Character 
	 */
	public static final String ENV_TARGET_OS_SEPARATOR = IModuleDeployPathController.ENV_TARGET_OS_SEPARATOR;

	
	/**
	 * Get the path separator character that should be used to 
	 * understand the full paths returned by this system.
	 * @return
	 */
	public char getPathSeparatorCharacter();
	
	/**
	 * Get the path to the root deploy folder on this server.
	 * The path will be absolute or relative depending on the parameter
	 * 
	 * An absolute value must be a proper string for whatever 
	 * system it is intended to be used on.
	 * 
	 * @param boolean If the path must be absolute
	 * @return
	 */
	public String getDeploymentsRootFolder(boolean absolute);

	/**
	 * Get the path for the temporary folder used for deployments to this server.
	 * The path will be absolute or relative depending on the parameter
	 * 
	 * An absolute value must be a proper string for whatever 
	 * system it is intended to be used on.
	 * 
	 * @param boolean if the path must be absolute
	 * @return
	 */
	public String getDeploymentsTemporaryFolder(boolean absolute);
	

	/**
	 * Return the currently selected deployment location type, 
	 * from the working copy if it exists, and if not, from the server itself.
	 * @return
	 */
	public String getCurrentDeploymentLocationType();
	
	/**
	 * Set on the working copy the new deployment location type 
	 * @throws IllegalStateException
	 */
	public void setCurrentDeploymentLocationType(String type) throws IllegalStateException;
	
	/**
	 * Set on the working copy the new deployment location root folder
	 * @param folder
	 * @throws IllegalStateException
	 */
	public void setDeploymentsRootFolder(String folder) throws IllegalStateException;
	
	/**
	 * Set on the working copy the new deployment location's temporary folder
	 * @param folder
	 * @throws IllegalStateException
	 */
	public void setDeploymentsTemporaryFolder(String folder) throws IllegalStateException;
	
	/**
	 * Set whether this server will prefer to zip deployments 
	 * when possible. 
	 * 
	 * @param val
	 */
	public void setPrefersZippedDeployments(boolean val);
	
	
	/**
	 * Get whether or not this server prefers zipped deployments
	 * @return
	 */
	public boolean prefersZippedDeployments();

}
