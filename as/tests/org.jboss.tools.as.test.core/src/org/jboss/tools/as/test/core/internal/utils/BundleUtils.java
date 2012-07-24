package org.jboss.tools.as.test.core.internal.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
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
		URL url = null;
		try {
			url = FileLocator.resolve(bundle.getEntry(path));
		} catch (IOException e) {
			String msg = "Cannot find file " + path + " in " + ASMatrixTests.PLUGIN_ID;
			IStatus status = new Status(IStatus.ERROR, ASMatrixTests.PLUGIN_ID, msg, e);
			throw new CoreException(status);
		}
		String location = url.getFile();
		return new File(location);
	}

}
