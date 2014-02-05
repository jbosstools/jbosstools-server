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
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;


/**
 * This interface represents a service capable of retrieving 
 * how to determine whether a module requires
 * a restart for a given publish request. 
 * @Since 3.0
 */
public interface IModuleRestartBehaviorController extends ISubsystemController {
	
	public static final String SYSTEM_ID = "module.restart.behavior"; //$NON-NLS-1$
	
	/**
	 * Does the given module require a restart based on the resources that are being published.
	 * 
	 * @param module
	 * @param resourcesToTest
	 * @return
	 */
	public boolean moduleRequiresRestart(IModule[] module, IModuleResource[] resourcesToTest);
	
	
	/**
	 * Does the given module require a restart based on the resource delta passed in
	 * @param module
	 * @param deltaToTest
	 * @return
	 */
	public boolean moduleRequiresRestart(IModule[] module, IModuleResourceDelta[] deltaToTest);
	
	
}
