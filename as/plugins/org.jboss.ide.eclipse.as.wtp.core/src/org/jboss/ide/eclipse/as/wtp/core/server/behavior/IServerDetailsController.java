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

/**
 * This is a controller for managing different types of server details.
 * Depending on the implementation, it will respond to different keys
 * and return the expected values from either custom settings or defaults.  
 * 
 * 
 * @since 3.0
 */
public interface IServerDetailsController extends ISubsystemController {

	/**
	 * Retrieve the property's value as understood by this subsystem
	 * 
	 * @param prop
	 * @return
	 */
	public String getProperty(String prop);
}
