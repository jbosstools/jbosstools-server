/******************************************************************************* 
 * Copyright (c) 2010 - 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.test.util;

import org.eclipse.core.runtime.IPath;

/**
 * @author André Dietisheim
 */
public class PathUtils {

	/**
	 * Returns <code>true</code> if the given path (string) is contained in the
	 * given array of paths
	 * 
	 * @param pathString
	 *            the path (string) to check
	 * @param pathArray
	 *            the array of paths to check
	 * @return true if the path was found in the array
	 */
	public static boolean containsPath(String pathString, IPath[] pathArray) {
		if (pathArray == null
				|| pathString == null) {
			return false;
		}
		for (IPath path : pathArray) {
			if (pathString.equals(path.toString())) {
				return true;
			}
		}
		return false;
	}

}
