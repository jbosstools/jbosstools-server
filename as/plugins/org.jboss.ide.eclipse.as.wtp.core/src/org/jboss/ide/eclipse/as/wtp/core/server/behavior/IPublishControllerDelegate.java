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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IModule;

/**
 * A subsystem controller to handle publishing for specific module types only
 * 
 * @since 3.0
 */
public interface IPublishControllerDelegate extends ISubsystemController {
	
	public static final String SYSTEM_ID = "publishDelegate";

	/**
	 * Publish this module
	 * @param kind The kind of publish, as a constant from IServer
	 * @param deltaKind The kind of delta, as a constant from ServerBehaviourDelegate
	 * @param module  The module to be published
	 * @param monitor The progress monitor
	 * @return An IServer.STATE_XXX constant, or -1 if the behaviour should not change server state
	 * @throws CoreException
	 */
	public int publishModule(int kind, int deltaKind, IModule[] module, IProgressMonitor monitor) throws CoreException;
}
