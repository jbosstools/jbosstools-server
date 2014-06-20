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

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;

public class VMInstallUtil {
	
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
	
	public static IVMInstall findVMInstall(IExecutionEnvironment environment) {
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
			
			// If there aren't any, check if the workspace default vm is in the list of compatible vms
			IVMInstall workspaceDefault = JavaRuntime.getDefaultVMInstall();
			if( installs != null && workspaceDefault != null ) {
				for( int i = 0; i < installs.length; i++) {
					if( workspaceDefault.equals(installs[i]))
						return workspaceDefault;
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
}