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

import org.eclipse.core.runtime.IStatus;

/**
 * @since 3.0
 */
public interface IServerShutdownController extends ISubsystemController {

	/**
	 * Is the server in a position to be shutdown?
	 * @return
	 */
	public IStatus canStop();
	
	
	/**
	 * Stop the server
	 * @param force
	 */
	public void stop(boolean force);
}
