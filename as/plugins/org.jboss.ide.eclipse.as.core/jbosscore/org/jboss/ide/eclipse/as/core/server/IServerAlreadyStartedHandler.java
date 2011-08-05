/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IServer;

public interface IServerAlreadyStartedHandler {
	public static int CONTINUE_STARTUP = 1;
	public static int ONLY_CONNECT = 2;
	public static int CANCEL = 3;
	
	/**
	 * Is this handler capable of handling this server?
	 * @param server
	 * @return
	 */
	public boolean accepts(IServer server);
	
	/**
	 * Handle the prompt for this server
	 * 
	 * @param server
	 * @param startedStatus
	 * @return One of the constants of this interface
	 */
	public int promptForBehaviour(IServer server, IStatus startedStatus);
}
