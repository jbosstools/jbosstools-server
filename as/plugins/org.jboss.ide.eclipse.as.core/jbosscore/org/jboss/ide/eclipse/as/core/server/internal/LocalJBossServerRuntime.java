/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.model.RuntimeDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.IConstants;

public class LocalJBossServerRuntime extends RuntimeDelegate implements IJBossServerRuntime {

	public void setDefaults(IProgressMonitor monitor) {
		getRuntimeWorkingCopy().setName(getNextRuntimeName());
		setAttribute(IJBossServerRuntime.PROPERTY_CONFIGURATION_NAME, IJBossServerConstants.DEFAULT_CONFIGURATION);
		setVM(null);
	}

	private String getNextRuntimeName() {
		String version = getRuntime().getRuntimeType().getVersion(); 
		String base = null;
		if( getRuntime().getRuntimeType().getId().startsWith("org.jboss.ide.eclipse.as.runtime.eap.")) { //$NON-NLS-1$
			base = Messages.jboss + " EAP " + version + " " + Messages.runtime; //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			base = Messages.jboss + " " + version + " " + Messages.runtime;  //$NON-NLS-1$//$NON-NLS-2$
		}
		return getNextRuntimeName(base);
	}
	
	public static String getNextRuntimeName(String base) {
		IRuntime rt = ServerCore.findRuntime(base);
		if (rt == null)
			return base;

		int i = 0;
		while (rt != null) {
			rt = ServerCore.findRuntime(base + " " + ++i); //$NON-NLS-1$
		}
		return base + " " + i; //$NON-NLS-1$
	}

	public IStatus validate() {
		IStatus s = super.validate();
		if( !s.isOK()) return s;
		
		if( getJBossConfiguration().equals("")) //$NON-NLS-1$
			return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 0, 
					NLS.bind(Messages.ServerRuntimeConfigNotFound, getRuntime().getName()), null);
		
		return Status.OK_STATUS;
	}
	
	public IVMInstall getVM() {
		if (getVMInstallTypeId() != null) {
			String id = getAttribute(PROPERTY_VM_ID, (String)null);
			String type = getAttribute(PROPERTY_VM_TYPE_ID, (String)null);

			IVMInstallType vmInstallType = JavaRuntime.getVMInstallType(type);
			IVMInstall[] vmInstalls = vmInstallType.getVMInstalls();

			for (int i = 0; i < vmInstalls.length; i++) {
				if (id.equals(vmInstalls[i].getId()))
					return vmInstalls[i];
			}
		}
		// not found, return default vm
		return getDefaultVMInstall();
	}
	
	public void setVM(IVMInstall selectedVM) {
		if (selectedVM == null) {
			setAttribute(IJBossServerRuntime.PROPERTY_VM_ID, (String) null);
			setAttribute(IJBossServerRuntime.PROPERTY_VM_TYPE_ID, (String) null);
		} else {
			setAttribute(IJBossServerRuntime.PROPERTY_VM_ID, selectedVM.getId());
			setAttribute(IJBossServerRuntime.PROPERTY_VM_TYPE_ID, selectedVM
					.getVMInstallType().getId());
		}
	}
	
	public String getJBossConfiguration() {
		return getAttribute(PROPERTY_CONFIGURATION_NAME, (String)""); //$NON-NLS-1$
	}
	
	public void setJBossConfiguration(String config) {
		setAttribute(IJBossServerRuntime.PROPERTY_CONFIGURATION_NAME, config);
	}

	public String getDefaultRunArgs() {
		return IConstants.STARTUP_ARG_CONFIG_LONG + "=" + getJBossConfiguration() + IConstants.SPACE;  //$NON-NLS-1$
	}

	public String getDefaultRunVMArgs() {
		IConstants c = new IConstants(){};
		String name = getRuntime().getName();
		String ret = c.SYSPROP + c.PROGRAM_NAME_ARG + c.EQ + c.QUOTE + 
			"JBossTools: " + name + c.QUOTE + c.SPACE; //$NON-NLS-1$
		if( Platform.getOS().equals(Platform.OS_MACOSX))
			ret += c.SERVER_ARG + c.SPACE;
		ret += c.DEFAULT_MEM_ARGS;
		if( Platform.getOS().equals(Platform.OS_LINUX))
			ret += c.SYSPROP + c.JAVA_PREFER_IP4_ARG + c.EQ + true + c.SPACE; 
		ret += c.SYSPROP + c.SUN_CLIENT_GC_ARG + c.EQ + 3600000 + c.SPACE;
		ret += c.SYSPROP + c.SUN_SERVER_GC_ARG + c.EQ + 3600000 + c.SPACE;
		ret += c.SYSPROP + c.ENDORSED_DIRS + c.EQ + c.QUOTE +
			(getRuntime().getLocation().append(c.LIB).append(c.ENDORSED)) + c.QUOTE + c.SPACE;
		if( getRuntime().getLocation().append(c.BIN).append(c.NATIVE).toFile().exists() ) 
			ret += c.SYSPROP + c.JAVA_LIB_PATH + c.EQ + c.QUOTE + 
				getRuntime().getLocation().append(c.BIN).append(c.NATIVE) + c.QUOTE + c.SPACE;
		
		return ret;
	}
	
	public HashMap<String, String> getDefaultRunEnvVars(){
		HashMap<String, String> envVars = new HashMap<String, String>(1);
		envVars.put("Path", IConstants.NATIVE); //$NON-NLS-1$
		return envVars;
	}

	public boolean isUsingDefaultJRE() {
		return getVMInstallTypeId() == null;
	}
	
	protected String getVMInstallTypeId() {
		return getAttribute(PROPERTY_VM_TYPE_ID, (String)null);
	}

	public String getConfigLocation() {
		return getAttribute(PROPERTY_CONFIG_LOCATION, IConstants.SERVER);
	}

	public void setConfigLocation(String configLocation) {
		setAttribute(PROPERTY_CONFIG_LOCATION, configLocation);
	}

	public IPath getConfigurationFullPath() {
		return getConfigLocationFullPath().append(getJBossConfiguration());
	}

	public IPath getConfigLocationFullPath() {
		String cl = getConfigLocation();
		if( new Path(cl).isAbsolute())
			return new Path(cl);
		return getRuntime().getLocation().append(cl);
	}
	
	protected IVMInstall getDefaultVMInstall() {
		IVMInstall install = JavaRuntime.getDefaultVMInstall();
		if( install instanceof IVMInstall2 ) {
			String version = ((IVMInstall2)install).getJavaVersion();
			if( isValidJREVersion(version, getRuntime().getRuntimeType()))
				return install;
		}
		ArrayList<IVMInstall> installs = getValidJREs(getRuntime().getRuntimeType());
		Iterator<IVMInstall> i = installs.iterator();
		while(i.hasNext()) {
			IVMInstall next = i.next();
			if( next instanceof IVMInstall2 ) {
				String version = ((IVMInstall2)next).getJavaVersion();
				if( isValidJREVersion(version, getRuntime().getRuntimeType()))
					return next;
			}
		}
		return null;
	}
	
	public static ArrayList<IVMInstall> getValidJREs(IRuntimeType type) {
		ArrayList<IVMInstall> valid = new ArrayList<IVMInstall>();
		IVMInstallType[] vmInstallTypes = JavaRuntime.getVMInstallTypes();
		int size = vmInstallTypes.length;
		for (int i = 0; i < size; i++) {
			IVMInstall[] vmInstalls = vmInstallTypes[i].getVMInstalls();
			int size2 = vmInstalls.length;
			for (int j = 0; j < size2; j++) {
				if( vmInstalls[j] instanceof IVMInstall2 ) {
					String version = ((IVMInstall2)vmInstalls[j]).getJavaVersion();
					if( isValidJREVersion(version, type))
						valid.add(vmInstalls[j]);
				}
			}
		}
		return valid;
	}
	
	public static boolean isValidJREVersion(String jreVersion, IRuntimeType rtType) {
		String id = rtType.getId();
		String version = rtType.getVersion();
		if( id.equals(IConstants.EAP_50) && version.equals(IConstants.V5_0)) { 
			return !jreVersion.startsWith(JavaCore.VERSION_1_1) &&
				!jreVersion.startsWith(JavaCore.VERSION_1_2) &&
				!jreVersion.startsWith(JavaCore.VERSION_1_3) &&
				!jreVersion.startsWith(JavaCore.VERSION_1_4) &&
				!jreVersion.startsWith(JavaCore.VERSION_1_5);
		}
		return true;
	}
}
