/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.internal.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.jboss.tools.as.test.core.ASMatrixTests;
import org.osgi.framework.Bundle;

public class BundleUtils {
	/**
	 * Find a file inside this test bundle
	 * @param path
	 * @return
	 * @throws CoreException
	 */
	public static File getFileLocation(String path) throws CoreException {
		return getFileLocation(ASMatrixTests.PLUGIN_ID, path);
	}
	
	/**
	 * Find a file inside any bundle
	 * @param bundleId
	 * @param path
	 * @return
	 */
	public static File getFileLocation(String bundleId, String path) throws CoreException {
		
		Bundle bundle = Platform.getBundle(bundleId);
		URL url1 = bundle.getEntry(path);
		URL url = null;
		try {
			url = FileLocator.toFileURL(url1);
		} catch (IOException e) {
			String msg = "Cannot find file " + path + " in " + ASMatrixTests.PLUGIN_ID;
			IStatus status = new Status(IStatus.ERROR, ASMatrixTests.PLUGIN_ID, msg, e);
			throw new CoreException(status);
		}
		String location = url.getFile();
		return new File(location);
	}

}
