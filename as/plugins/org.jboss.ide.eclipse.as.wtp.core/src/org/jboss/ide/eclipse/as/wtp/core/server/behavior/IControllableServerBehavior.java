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
package org.jboss.ide.eclipse.as.wtp.core.server.behavior;

import org.eclipse.core.runtime.CoreException;

public interface IControllableServerBehavior {
	
	// A list of pre-defined subsystem categories
	public static final String SYSTEM_PUBLISH = IPublishController.SYSTEM_ID;
	public static final String SYSTEM_MODULES = IModuleStateController.SYSTEM_ID;
	public static final String SYSTEM_LAUNCH = ILaunchServerController.SYSTEM_ID;
	public static final String SYSTEM_SHUTDOWN = IServerShutdownController.SYSTEM_ID;
	
	
	/**
	 * get some data from the behavior's shared data map
	 * @param key  The key from to get
	 * @return
	 */
	public Object getSharedData(String key);
	
	/**
	 * Put some data in the behavior's shared data map
	 * @param key
	 * @param o
	 */
	public void putSharedData(String key, Object o);
	
	/**
	 * Get the controller for a specific system.
	 * @param system
	 * @return
	 * @throws CoreException
	 */
	public ISubsystemController getController(String system) throws CoreException;
	
	/**
	 * Allows a client to get a controller for a specific system with specific flags set. 
	 * 
	 * @param system
	 * @param env
	 * @return
	 * @throws CoreException
	 */
	public ISubsystemController getController(String system, ControllerEnvironment env) throws CoreException;
	
}
