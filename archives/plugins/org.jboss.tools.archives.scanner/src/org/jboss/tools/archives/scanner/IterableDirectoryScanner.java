/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.archives.scanner;

/**
 * This represents an extension to the Scanner interface, 
 * allowing one folder to be scanned at a time as needed, 
 * rather than requiring a full scan before results become available. 
 */
public interface IterableDirectoryScanner<T> extends Iterable<T>, Scanner<T> {
	/**
	 * Scan the given directory. 
	 * 
	 * @param file
	 * @param vpath
	 */
	public void scanDirectory(T file, String vpath);
	
	/**
	 * The scanner should clean up everything associated with it.
	 */
	public void cleanup();
}
