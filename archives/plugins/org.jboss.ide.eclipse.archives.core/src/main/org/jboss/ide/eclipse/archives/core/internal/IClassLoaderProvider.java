/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.core.internal;

/**
 * A workaround interface for maintenance 
 * due to unit test failures regarding our
 * method of handling requests to truezip, and
 * truezip's use of context classloader.  
 */
public interface IClassLoaderProvider {
	/**
	 * Get the classloader
	 */
	public ClassLoader getClassLoader();
}
