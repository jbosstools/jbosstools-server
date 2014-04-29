/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.tools.as.core.server.controllable;

import org.eclipse.debug.core.model.IProcess;
import org.jboss.ide.eclipse.as.wtp.core.server.launch.AbstractStartJavaServerLaunchDelegate;

/**
 * This interface is a collection of shared keys that a given deployable server
 * may have stored values for in its shared data map.
 */
public interface IDeployableServerBehaviorProperties {
	/**
	 * A key for shared data, the value should be an {@link IProcess}
	 */
	public static final String PROCESS = AbstractStartJavaServerLaunchDelegate.PROCESS;
	/**
	 * A key for shared data, the value should be a PollThread
	 */
	public static final String DEBUG_LISTENER = AbstractStartJavaServerLaunchDelegate.DEBUG_LISTENER;
	

	/**
	 * A key for shared data, the value should be an integer for a process id
	 */
	public static final String PROCESS_ID = "DeployableServerBehavior.Process_ID"; //$NON-NLS-1$

	
	/**
	 * A key for shared data, the value should be a PollThread
	 */
	public static final String POLL_THREAD = "DeployableServerBehavior.PollThread"; //$NON-NLS-1$
	
	/**
	 * A key for shared data, the value should be a Boolean
	 */
	public static final String NEXT_STOP_REQUIRES_FORCE = AbstractStartJavaServerLaunchDelegate.NEXT_STOP_REQUIRES_FORCE;
	
	
}
