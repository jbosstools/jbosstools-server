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

import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;


/**
 * This interface represents a service capable of retrieving 
 * or setting a module's deploy path for this server. It is primarily
 * used for servers that deploy using the filesystem, whether local or remote,
 * and need access to customized paths per-module
 * 
 * If instantiated with a IServerWorkingCopy in the environment,
 * it will get and set the values from the working copy. 
 * 
 * If instantiated with only an IServer,  
 * setters may fail with an IllegalStateException, depending on the implementation.
 */
public interface IModuleDeployPathController extends ISubsystemController {
	
	/**
	 * In the event that any returned paths are relative paths, this 
	 * path will be pre-pended to the result. All paths are deemed to be
	 * relative to the value at this environment setting
	 */
	public static final String ENV_DEFAULT_DEPLOY_FOLDER = "IModuleDeployPathController.DeployFolder";  //$NON-NLS-1$

	/**
	 * In the event that any returned paths are relative paths, this 
	 * path will be pre-pended to the result. All paths are deemed to be
	 * relative to the value at this environment setting
	 */
	public static final String ENV_DEFAULT_TMP_DEPLOY_FOLDER = "IModuleDeployPathController.TmpDeployFolder";  //$NON-NLS-1$

	
	/**
	 * This is an optional property for use when targeting a remote windows system 
	 * from a linux machine, or a remote linux machine from a windows machine. 
	 * The value of this property should be a java.lang.Character 
	 */
	public static final String ENV_TARGET_OS_SEPARATOR = "PathHandling.TargetSystemSeparator"; //$NON-NLS-1$
	
	/**
	 * Get the output name for this module from the preference store.
	 * 
	 * @param module
	 * @return
	 */
	public String getOutputName(IModule[] module);
	
	/**
	 * Get the ABSOLUTE path of the deploy folder for this module.
	 * For example, given a war nested in an ear, it will return something like:
	 * 		/home/user/server/deploy/App.ear/Web.war
	 * 
	 * @param module
	 * @return
	 */
	public String getDeployDirectory(IModule[] module);
	
	/**
	 * Get a absolute path of the temporary deploy folder for this module
	 * @param module
	 * @return
	 */
	public String getTemporaryDeployDirectory(IModule[] module);
	
	/**
	 * Set the output name for this module in the preference store
	 * @param module
	 * @param name
	 */
	public void setOutputName(IModule module, String name) throws IllegalStateException;

	/**
	 * Set the output name for this module in the preference store
	 * This value may be absolute, or it may be relative to a current default
	 * 
	 * @param module
	 * @param directory
	 */
	public void setDeployDirectory(IModule module, String directory) throws IllegalStateException;

	/**
	 * Set the output name for this module in the preference store
	 * This value may be absolute, or it may be relative to a current default
	 * 
	 * @param module
	 * @param directory
	 */
	public void setTemporaryDeployDirectory(IModule module, String directory) throws IllegalStateException;
	
	/**
	 * Get the default suffix for the given module based on the module type
	 */
	public String getDefaultSuffix(IModule module);
}
