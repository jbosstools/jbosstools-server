/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.rsp.ui.util;

import java.io.File;
import java.util.Locale;

/**
 * Find the java executable on the user's hard drive at expected locations
 */
public class JavaUtils {
	private static final String JRE = "jre"; //$NON-NLS-1$
	private static final String JAVA_HOME = "java.home";
	private static final String[] fgCandidateJavaFiles = { "javaw", "javaw.exe", "java", "java.exe", "j9w", "j9w.exe", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"j9", "j9.exe" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] fgCandidateJavaLocations = { "bin" + File.separatorChar, //$NON-NLS-1$
			JRE + File.separatorChar + "bin" + File.separatorChar }; //$NON-NLS-1$

	public static File findJavaExecutable() {
		String home = System.getProperty(JAVA_HOME);
		File f = new File(home);
		return findJavaExecutable(f);
	}

	/**
	 * Starting in the specified VM install location, attempt to find the 'java'
	 * executable file. If found, return the corresponding <code>File</code> object,
	 * otherwise return <code>null</code>.
	 * 
	 * @param vmInstallLocation the {@link File} location to look in
	 * @return the {@link File} for the Java executable or <code>null</code>
	 */
	public static File findJavaExecutable(File vmInstallLocation) {
		// Try each candidate in order. The first one found wins. Thus, the order
		// of fgCandidateJavaLocations and fgCandidateJavaFiles is significant.
		for (int i = 0; i < fgCandidateJavaFiles.length; i++) {
			for (int j = 0; j < fgCandidateJavaLocations.length; j++) {
				File javaFile = new File(vmInstallLocation, fgCandidateJavaLocations[j] + fgCandidateJavaFiles[i]);
				if (javaFile.isFile()) {
					return javaFile;
				}
			}
		}
		return null;
	}

	private static String OS = System.getProperty("os.name", "unknown").toLowerCase(Locale.ROOT);

	public static boolean isWindows() {
		return OS.contains("win");
	}

	public static boolean isMac() {
		return OS.contains("mac");
	}

	public static boolean isUnix() {
		return OS.contains("nux");
	}

	public static String getUserHome() {
		if (isWindows()) {
			String userProfile = System.getenv("USERPROFILE");
			if (userProfile != null && new File(userProfile).exists())
				return userProfile;
		}
		return System.getProperty("user.home");
	}
}
