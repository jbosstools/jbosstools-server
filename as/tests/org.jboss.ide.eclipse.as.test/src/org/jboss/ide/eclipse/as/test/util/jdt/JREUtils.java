/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.jboss.ide.eclipse.as.test.util.jdt;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.launching.VMDefinitionsContainer;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.jdt.launching.VMStandin;
import org.jboss.ide.eclipse.as.test.ASTest;

/**
 * Processes add/removed/changed VMs.
 */
public class JREUtils {
		
	/**
	 * Contstructs a new VM updater to update VM install settings.
	 */
	public JREUtils() {
	}
	
	public static IVMInstall createJRE() {
		return createJRE(new Path(ASTest.JRE_5_HOME));
	}

	/*
	 * This method does *NOT* work and I can't figure out why ;) 
	 */
	public static void removeJRE(IVMInstall install) {
		ArrayList<IVMInstall> vms = new ArrayList<IVMInstall>();
		IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
		for( int i = 0; i < types.length; i++ ) {
			vms.addAll(Arrays.asList(types[i].getVMInstalls()));
		}
		vms.remove(install);
		IVMInstall[] allInstalls = vms.toArray(new IVMInstall[vms.size()]);
		updateJRESettings(allInstalls, JavaRuntime.getDefaultVMInstall());
	}
		
	public static int countJREs() {
		int count = 0;
		IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
		for( int i = 0; i < types.length; i++ ) {
			IVMInstall[] installs = types[i].getVMInstalls();
			count+=installs.length;
		}
		return count;
	}

	public static IVMInstall createJRE(IPath jreBaseDir) {
		IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
		IVMInstallType t = null;
		System.out.println(types.length);
		for( int i = 0; i < types.length; i++ ) {
			if( types[i].getId().equals("org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType")) {
				t = types[i];
			}
		}
		
		String id = null;
		do {
			id = String.valueOf(System.currentTimeMillis());
		} while (t.findVMInstall(id) != null);

		VMStandin standin = new VMStandin(t, id);
		standin.setInstallLocation(jreBaseDir.toFile());
		standin.setName(jreBaseDir.lastSegment());
		LibraryLocation[] libraryLocations = JavaRuntime.getLibraryLocations(standin);
		standin.setLibraryLocations(libraryLocations);

		return standin.convertToRealVM();
	}
	

	
	/**
	 * Updates VM settings and returns whether the update was successful.
	 * 
	 * @param jres new installed JREs
	 * @param defaultJRE new default VM
	 * @return whether the update was successful
	 */
	public static boolean updateJRESettings(IVMInstall[] jres, IVMInstall defaultJRE) {
		
		// Create a VM definition container
		VMDefinitionsContainer vmContainer = new VMDefinitionsContainer();
		
		// Set the default VM Id on the container
		String defaultVMId = JavaRuntime.getCompositeIdFromVM(defaultJRE);
		vmContainer.setDefaultVMInstallCompositeID(defaultVMId);
		
		// Set the VMs on the container
		for (int i = 0; i < jres.length; i++) {
			vmContainer.addVM(jres[i]);
		}
		
		
		// Generate XML for the VM defs and save it as the new value of the VM preference
		saveVMDefinitions(vmContainer);
		
		return true;
	}
	
	private static void saveVMDefinitions(final VMDefinitionsContainer container) {
		try {
			String vmDefXML = container.getAsXML();
			JavaRuntime.getPreferences().setValue(JavaRuntime.PREF_VM_XML, vmDefXML);
			JavaRuntime.savePreferences();
		} catch( CoreException ce) {
		}
	}
}
