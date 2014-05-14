/*******************************************************************************
 * Copyright (c) 2006 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    "Rob Stryker" <rob.stryker@redhat.com> - Initial implementation
 *******************************************************************************/
package org.jboss.tools.jmx.core;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.jmx.core.tree.Root;


/**
 * API for a connection wrapper
 */
public interface IConnectionWrapper {
	public IConnectionProvider getProvider();
	public boolean isConnected();
	public boolean canControl();
	public void connect() throws IOException;
	public void disconnect() throws IOException;
	
	/**
	 * Loads the root object in the current thread if it is not loaded.
	 * If it is loaded, does nothing.
	 */
	public void loadRoot(IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Gets the current root object, or null if its not yet loaded.
	 * @return
	 */
	public Root getRoot();
	
	/**
	 * Run this runnable 
	 * @param runnable
	 * @throws JMXException
	 */
	public void run(IJMXRunnable runnable) throws Exception;
	
	/**
	 * Run this runnable, but pass in a map full of preferences 
	 * that may contribute to the setup 
	 * 
	 * @param runnable
	 * @param prefs
	 * @throws JMXException
	 */
	 public void run(IJMXRunnable runnable, HashMap<String, String> prefs) throws JMXException;
	
}
