/*******************************************************************************
 * Copyright (c) 2015 Red Hat Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    "Rob Stryker" <rob.stryker@redhat.com> - Initial implementation
 *******************************************************************************/
package org.jboss.tools.jmx.core;

public interface IDebuggableConnection {
	
	/**
	 * Get the main class for the given connection
	 * 
	 * @return
	 */
	public String getMainClass();
	
	/**
	 * Does this connection expose the proper debug flags
	 * 
	 * @return
	 */
	public boolean debugEnabled();
	
	/**
	 * Get the debug host for this connection
	 * @return
	 */
	public String getDebugHost();
	
	/**
	 * Get the exposed port to connect a debugger
	 * @return
	 */
	public int getDebugPort();
}
