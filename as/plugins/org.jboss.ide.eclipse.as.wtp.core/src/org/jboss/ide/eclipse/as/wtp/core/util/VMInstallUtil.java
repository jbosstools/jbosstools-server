/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;

public class VMInstallUtil {

	private static final Pattern MAJOR_MINOR_PATTERN = Pattern
			.compile("^(\\d+)\\.(\\d+)\\..*");
	private static final boolean PREFER_HIGHEST = true;
	private static final boolean PREFER_LOWEST = false;

	public static IVMInstall findVMInstall(String type, String id) {
		if( type != null && id != null) {
			IVMInstallType vmInstallType = JavaRuntime.getVMInstallType(type);
			IVMInstall[] vmInstalls = vmInstallType.getVMInstalls();
			for (int i = 0; i < vmInstalls.length; i++) {
				if (id.equals(vmInstalls[i].getId()))
					return vmInstalls[i];
			}
		}
		return null;
	}

	public static IVMInstall[] getAllVMInstalls() {
		ArrayList<IVMInstall> allRet = new ArrayList<IVMInstall>();
		IVMInstallType[] allTypes = JavaRuntime.getVMInstallTypes();
		for( int i = 0; i < allTypes.length; i++ ) {
			IVMInstallType vmInstallType = allTypes[i];
			IVMInstall[] vmInstalls = vmInstallType.getVMInstalls();
			for (int j = 0; j < vmInstalls.length; j++) {
				allRet.add(vmInstalls[j]);
			}
		}
		return (IVMInstall[]) allRet.toArray(new IVMInstall[allRet.size()]);
	}

	
	
	public static IVMInstall findVMInstall(IExecutionEnvironment environment) {
		return findVMInstall(environment, PREFER_LOWEST);
	}

	private static IVMInstall findVMInstall(IExecutionEnvironment environment,
			boolean preference) {
		if( environment != null ) {
			IVMInstall[] installs = environment.getCompatibleVMs();
			if( environment.getDefaultVM() != null )
				return environment.getDefaultVM();

			// Find an install that is strictly compatible first
			for (int i = 0; i < installs.length; i++) {
				IVMInstall install = installs[i];
				if (environment.isStrictlyCompatible(install)) {
					return install;
				}
			}

			// If there aren't any, check if the workspace default vm is in the
			// list of compatible vms
			IVMInstall workspaceDefault = JavaRuntime.getDefaultVMInstall();
			if( installs != null && workspaceDefault != null ) {
				for( int i = 0; i < installs.length; i++) {
					if( workspaceDefault.equals(installs[i]))
						return workspaceDefault;
				}
			}

			List<IVMInstall2> sorted = sortVMs(installs);
			if( sorted.size() > 0 ) {
				if( preference == PREFER_LOWEST ) {
					return (IVMInstall)sorted.get(0);
				}
				if( preference == PREFER_HIGHEST ) {
					return (IVMInstall)sorted.get(sorted.size()-1);
				}
			}

			// Otherwise, return the first vm of any compatability
			if( installs != null && installs.length > 0 && installs[0] != null )
				return installs[0];
		}
		// not found, return default vm
		IVMInstall i = environment == null ? null : environment.getDefaultVM();
		return i == null ? JavaRuntime.getDefaultVMInstall() : i;
	}

	/**
	 * Return an array of vms that implement IVMInstall2 and are sorted based on
	 * java version.
	 * 
	 * @param installs
	 * @return
	 */
	private static List<IVMInstall2> sortVMs(IVMInstall[] installs) {
		List<IVMInstall2> all = new ArrayList<IVMInstall2>();
		for (int i = 0; i < installs.length; i++) {
			if( installs[i] instanceof IVMInstall2 ) {
				// It's possible the version is null, or its version string doesn't match our expectations
				// so we can't sort those.  Dont add those to the list to be sorted
				String version = ((IVMInstall2)installs[i]).getJavaVersion(); 
				if( version != null ) {
					try {
						getMajorMinor(version);
						all.add((IVMInstall2) installs[i]);
					} catch( IllegalArgumentException iae) {
						// Ignore, do not log. User does not need to see this
					}
				}
			}
		}
		Collections.sort(all, new Comparator<IVMInstall2>() {
			public int compare(IVMInstall2 o1, IVMInstall2 o2) {
				String arg0vers = o1.getJavaVersion();
				String arg1vers = o2.getJavaVersion();
				return compareJavaVersions(arg0vers, arg1vers);
			}
		});
		return all;
	}

	private static int compareJavaVersions(String version0, String version1) {
		int[] arg0majorMinor = getMajorMinor(version0);
		int[] arg1majorMinor = getMajorMinor(version1);
		if( arg0majorMinor[0] < arg1majorMinor[0] )
			return -1;
		if( arg0majorMinor[0] > arg1majorMinor[0] )
			return 1;
		if( arg0majorMinor[1] < arg1majorMinor[1] )
			return -1;
		if( arg0majorMinor[1] > arg1majorMinor[1] )
			return 1;
		return 0;
	}

	private static int[] getMajorMinor(String version) {
		Matcher m = MAJOR_MINOR_PATTERN.matcher(version);
		if( !m.matches() ) {
			throw new IllegalArgumentException("Malformed version string");
		}
		return new int[] { Integer.parseInt(m.group(1)), // major
				Integer.parseInt(m.group(2)) };
	}
}