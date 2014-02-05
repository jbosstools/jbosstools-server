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
 * This interface represents a service capable of retrieving 
 * ports. 
 * 
 * @Since 3.0
 */
public interface IPortsController extends ISubsystemController {
	
	public static final String SYSTEM_ID = "server.ports"; //$NON-NLS-1$
	
	
	public static final int KEY_JNDI = 100;
	public static final int KEY_WEB = 101;
	public static final int KEY_PORT_OFFSET = 102;
	public static final int KEY_JMX_RMI = 103;
	public static final int KEY_MANAGEMENT_PORT = 104;
	
	/**
	 * Get the relevant port
	 * 
	 * @param id  the id of the port
	 * @param defaultValue  a default value
	 * @return
	 */
	public int findPort(int id, int defaultValue);
	
	
}
