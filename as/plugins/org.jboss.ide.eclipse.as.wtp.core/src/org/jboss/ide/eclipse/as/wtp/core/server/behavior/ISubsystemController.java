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

import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IServerAttributes;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.SubsystemModel.Subsystem;

/**
 * 
 * This class represents a subsystem controller. 
 * 
 * @since 3.0
 */
public interface ISubsystemController {
	
	/**
	 * This method is not intended to be called by clients, and is a framework
	 * method to initialize the subsystem controller.   
	 * 
	 * @param server
	 * @param type
	 */
	public void initialize(IServerAttributes server, Subsystem type, Map<String, Object> environment);
	
	/**
	 * Get the subsystem id
	 * @return
	 */
	public String getSubsystemMappedId();

	/**
	 * Get the subsystem id
	 * @return
	 */
	public String getSystemId();

	/**
	 * This method validates whether the pre-requirements for this subsystem 
	 * have been met, such as other required subsystems being present and also valid.
	 * 
	 * @return
	 */
	public IStatus validate();
	
}
