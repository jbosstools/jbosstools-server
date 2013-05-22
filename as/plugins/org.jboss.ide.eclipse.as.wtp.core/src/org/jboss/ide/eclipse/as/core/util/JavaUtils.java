/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.util;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;

public class JavaUtils {
	public static boolean supportsServerMode(IVMInstall install) {
		File f = install.getInstallLocation();
		String version = (install instanceof IVMInstall2 ? ((IVMInstall2)install).getJavaVersion() : null);

		// Maintain legacy behaviour for all older server adapters
		if( version == null || Platform.getOS().equals(Platform.OS_MACOSX))
			return true;
		
		File jdkLibFolder = null;
		File jreLibFolder = null;
		if( Platform.getOS().equals(Platform.OS_WIN32)) {
			jdkLibFolder = getWindowsServerLibFolder(version, install, true);
			jreLibFolder = getWindowsServerLibFolder(version, install, false);
		} else {
			jdkLibFolder = getLinuxServerLibFolder(version, install, true);
			jreLibFolder = getLinuxServerLibFolder(version, install, false);
		}
		if( jdkLibFolder != null && jdkLibFolder.exists() && 
				jdkLibFolder.isDirectory() && jdkLibFolder.list().length > 0)
			return true;
		if( jreLibFolder != null && jreLibFolder.exists() && 
				jreLibFolder.isDirectory() && jreLibFolder.list().length > 0)
			return true;
		return false;
	}

	private static File getLinuxServerLibFolder(String version, IVMInstall install, boolean jdk) {
		File serverFolder = null;
		IPath locPath = new Path(install.getInstallLocation().getAbsolutePath());
		if( jdk )
			locPath = locPath.append("jre");//$NON-NLS-1$
		serverFolder = findServerFolder(locPath.append("lib")); //$NON-NLS-1$
		return serverFolder;
	}
	
	private static File findServerFolder(IPath parent) {
		File f = parent.toFile();
		if( !f.exists())
			return null;
		File[] children = f.listFiles();
		for( int i = 0; i < children.length; i++ ) {
			if( children[i].isDirectory() ) {
				String[] second = children[i].list();
				for( int j = 0; j < second.length; j++ ) {
					if( second[j].toLowerCase().equals("server")) //$NON-NLS-1$
							return new File(children[i], second[j]);
				}
			}
		}
		return null;
	}
	
	/**
	 * This method performs a simple jdk check based on file structure ONLY
	 * @return
	 */
	public static boolean isJDK(IVMInstall install) {
		IPath locPath = new Path(install.getInstallLocation().getAbsolutePath());
		// Find a javac at pre-determined locations
		if( locPath.append("bin").append("javac").toFile().exists())
			return true;
		if( locPath.append("bin").append("javac.exe").toFile().exists())
			return true;
		
		// Oracle-style folder structure
		if( locPath.append("bin").toFile().exists() && locPath.append("jre").append("bin").toFile().exists())
			return true;
		
		return false;
	}

	private static File getWindowsServerLibFolder(String version, IVMInstall install, boolean jdk) {
		IPath locPath = new Path(install.getInstallLocation().getAbsolutePath());
		if( jdk )
			locPath = locPath.append("jre");//$NON-NLS-1$
		return locPath.append("bin").append("server").toFile(); //$NON-NLS-1$ //$NON-NLS-2$ 
	}

}
