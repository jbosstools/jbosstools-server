/*******************************************************************************
 * Copyright (c) 2014 Red Hat and others
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
 * API for an optional extension to a connection provider.
 */
public interface IConnectionCategory {
	/**
	 * A category dedicated to user-defined connections
	 */
	public static final String DEFINED_CATEGORY = "IConnectionCategory.defined";

	/**
	 * A category dedicated to connections representing a webtools server
	 */
	public static final String SERVER_CATEGORY = "IConnectionCategory.server";

	/**
	 * A category dedicated to jmx connections backed by a jvmmonitor process
	 */
	public static final String PROCESS_CATEGORY = "IConnectionCategory.process";

	
	/**
	 * Get a string category ID 
	 * @return
	 */
	public String getCategoryId();
}
