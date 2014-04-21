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

import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

/**
 * A shutdown controller provider is something that knows 
 * how to access a proper shutdown controller that can 
 * terminate it. 
 * @since 3.0
 */
public interface IShutdownControllerDelegate extends ISubsystemController, ILaunchConfigurationDelegate {
	public static final String SYSTEM_ID = "launch";

	/**
	 * Get the shutdown controller
	 * @return
	 */
	public IServerShutdownController getShutdownController();
	
}
