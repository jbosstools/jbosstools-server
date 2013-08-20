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

import java.util.Iterator;

/**
 * This class represents an iterator to be used by a deployment scanner.
 * The scanner is able to scan only until the next set of results are found,
 * at which point it adds them to the iterator as a match. 
 * 
 * A directory scanner should add newly discovered folders 
 * to the scan list by calling addElementToScanList, 
 * and add matches by calling addMatch. 
 */
public interface IDirectoryScannerIterator<T> extends Iterator<T> {
	/**
	 * The iterator should add this file / folder to the list
	 * to be scanned for matches. 
	 * 
	 * @param file   the file or folder to scan
	 * @param vpath  the path
	 */
	public void addElementToScanList(T file, String vpath);
	
	/**
	 * The scanner has located a match, and the iterator should
	 * make it available for access to clients. 
	 * 
	 * @param file  The file that matches
	 * @param vpath The path of the matching file
	 */
	public void addMatch(T file, String vpath);
}
