/*******************************************************************************
 * Copyright (c) 2014 Red Hat Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    "Rob Stryker" <rob.stryker@redhat.com> - Initial implementation
 *******************************************************************************/
package org.jboss.tools.jmx.core;


/**
 * Interface for a class which can provide a jmx connection
 * that is associated with it. 
 */
public interface IConnectionFacade {
		
	/**
	 * Get the connection 
	 * @return
	 */
	public IConnectionWrapper getJMXConnection();
}
