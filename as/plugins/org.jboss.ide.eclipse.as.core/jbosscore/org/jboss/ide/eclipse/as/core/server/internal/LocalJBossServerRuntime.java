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

import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.DEFAULT_MEM_ARGS;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.DEFAULT_MEM_ARGS_AS50;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.ENDORSED_DIRS;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.EQ;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.FILE_COLON;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.JAVA_LIB_PATH;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.JAVA_PREFER_IP4_ARG;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.LOGGING_CONFIG_PROP;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.PROGRAM_NAME_ARG;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.QUOTE;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.SERVER_ARG;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.SPACE;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.STARTUP_ARG_CONFIG_LONG;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.SUN_CLIENT_GC_ARG;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.SUN_SERVER_GC_ARG;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.SYSPROP;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.BIN;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.ENDORSED;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.LIB;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.LOGGING_PROPERTIES;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.NATIVE;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.SERVER;

import java.util.HashMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.core.util.JavaUtils;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;

public class LocalJBossServerRuntime extends AbstractLocalJBossServerRuntime implements IJBossServerRuntime {

	@Override
	public void setDefaults(IProgressMonitor monitor) {
		super.setDefaults(monitor);
		setAttribute(IJBossServerRuntime.PROPERTY_CONFIGURATION_NAME, IJBossRuntimeResourceConstants.DEFAULT_CONFIGURATION);
	}

	@Override
	protected String getNextRuntimeName() {
		JBossExtendedProperties props = (JBossExtendedProperties)getRuntime().getAdapter(JBossExtendedProperties.class);
		String prefix = RuntimeUtils.isEAP(getRuntimeType()) ? Messages.jboss + " EAP" : Messages.jboss; //$NON-NLS-1$
		String rtVersion = props.getRuntimeTypeVersionString();
		String base = prefix + SPACE + rtVersion + SPACE + Messages.runtime;
		return getNextRuntimeName(base);
	}
	
	@Deprecated
	public boolean isEAP() {
		return RuntimeUtils.isEAP(getRuntimeType());
	}
	
	protected IRuntimeType getRuntimeType() {
	    IRuntime rt = getRuntime();
	    return rt == null ? getRuntimeWorkingCopy().getRuntimeType() : rt.getRuntimeType();
	}
	
	@Override
	public IStatus validate() {
		IStatus s = super.validate();
		if( !s.isOK()) return s;
		
		if( getJBossConfiguration().equals("")) //$NON-NLS-1$
			return new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 0, 
					NLS.bind(Messages.ServerRuntimeConfigNotFound, getRuntime().getName()), null);
		
		return Status.OK_STATUS;
	}
		
	@Override
	public String getJBossConfiguration() {
		return getAttribute(PROPERTY_CONFIGURATION_NAME, (String)""); //$NON-NLS-1$
	}
	
	@Override
	public void setJBossConfiguration(String config) {
		setAttribute(IJBossServerRuntime.PROPERTY_CONFIGURATION_NAME, config);
	}

	@Override
	public String getDefaultRunArgs() {
		return STARTUP_ARG_CONFIG_LONG + "=" + getJBossConfiguration() + SPACE;  //$NON-NLS-1$
	}

	@Override
	public String getDefaultRunArgs(IPath serverHome) {
		return getDefaultRunArgs();
	}

	@Override
	public String getDefaultRunVMArgs() {
		return getDefaultRunVMArgs(getRuntime().getLocation());
	}
	
	@Override
	public String getDefaultRunVMArgs(IPath serverHome) {
		String version = new ServerBeanLoader(serverHome.toFile()).getFullServerVersion();

		String name = getRuntime().getName();
		String ret = QUOTE + SYSPROP + PROGRAM_NAME_ARG + EQ +  
			"JBossTools: " + name + QUOTE + SPACE; //$NON-NLS-1$
		if( JavaUtils.supportsServerMode(getVM())) 
			ret += SERVER_ARG + SPACE;
		IRuntimeType type = getRuntime().getRuntimeType();
		if (type != null && 
				(IJBossToolingConstants.AS_50.equals(type.getId()) ||
				 IJBossToolingConstants.AS_51.equals(type.getId()) ||
				 IJBossToolingConstants.AS_60.equals(type.getId()) ||
				 IJBossToolingConstants.EAP_50.equals(type.getId())) ) {
			ret += DEFAULT_MEM_ARGS_AS50;
		} else {
			ret += DEFAULT_MEM_ARGS;
		}
		if( Platform.getOS().equals(Platform.OS_LINUX))
			ret += SYSPROP + JAVA_PREFER_IP4_ARG + EQ + true + SPACE; 
		ret += SYSPROP + SUN_CLIENT_GC_ARG + EQ + 3600000 + SPACE;
		ret += SYSPROP + SUN_SERVER_GC_ARG + EQ + 3600000 + SPACE;
		ret += QUOTE + SYSPROP + ENDORSED_DIRS + EQ + 
			(serverHome.append(LIB).append(ENDORSED)) + QUOTE + SPACE;
		if( serverHome.append(BIN).append(NATIVE).toFile().exists() ) 
			ret += SYSPROP + JAVA_LIB_PATH + EQ + QUOTE + 
				serverHome.append(BIN).append(NATIVE) + QUOTE + SPACE;
		
		if( version.startsWith(IJBossToolingConstants.V6_1)) {
			ret += SYSPROP + LOGGING_CONFIG_PROP + EQ + QUOTE + FILE_COLON + 
					serverHome.append(BIN).append(LOGGING_PROPERTIES) + QUOTE + SPACE;
		}
		return ret;
	}

	@Override
	public HashMap<String, String> getDefaultRunEnvVars(){
		HashMap<String, String> envVars = new HashMap<String, String>(1);
		envVars.put("PATH", NATIVE + System.getProperty("path.separator") + "${env_var:PATH}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return envVars;
	}

	@Override
	public String getConfigLocation() {
		return getAttribute(PROPERTY_CONFIG_LOCATION, SERVER);
	}

	@Override
	public void setConfigLocation(String configLocation) {
		setAttribute(PROPERTY_CONFIG_LOCATION, configLocation);
	}

	@Override
	public IPath getConfigurationFullPath() {
		return getConfigLocationFullPath().append(getJBossConfiguration());
	}

	@Override
	public IPath getConfigLocationFullPath() {
		String cl = getConfigLocation();
		if( new Path(cl).isAbsolute())
			return new Path(cl);
		return getRuntime().getLocation().append(cl);
	}
}
