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

import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants.SPACE;
import static org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants.SERVER;

import java.util.HashMap;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.RuntimeUtils;

public class LocalJBossServerRuntime extends AbstractLocalJBossServerRuntime implements IJBossServerRuntime {

	public JBossExtendedProperties getExtendedProperties() {
		return (JBossExtendedProperties)getRuntime().getAdapter(JBossExtendedProperties.class);
	}
	
	@Override
	public void setDefaults(IProgressMonitor monitor) {
		super.setDefaults(monitor);
		setAttribute(IJBossServerRuntime.PROPERTY_CONFIGURATION_NAME, IJBossRuntimeResourceConstants.DEFAULT_CONFIGURATION);
	}
	
	@Override
	protected String getRuntimeNameBase() {
		JBossExtendedProperties props = getExtendedProperties();
		String prefix = RuntimeUtils.isEAP(getRuntimeType()) ? Messages.jboss + " EAP" : Messages.jboss; //$NON-NLS-1$
		String rtVersion = props.getRuntimeTypeVersionString();
		String base = prefix + SPACE + rtVersion + SPACE + Messages.runtime;
		return base;
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

	@Override @Deprecated
	public String getDefaultRunArgs() {
		return getExtendedProperties().getDefaultLaunchArguments().getStartDefaultProgramArgs();
	}

	@Override @Deprecated
	public String getDefaultRunArgs(IPath serverHome) {
		return getDefaultRunArgs();
	}

	@Override @Deprecated
	public String getDefaultRunVMArgs() {
		return getDefaultRunVMArgs(getRuntime().getLocation());
	}
	
	@Override @Deprecated
	public String getDefaultRunVMArgs(IPath serverHome) {
		JBossExtendedProperties props = getExtendedProperties();
		return props.getDefaultLaunchArguments().getStartDefaultVMArgs();
	}

	@Override @Deprecated
	public HashMap<String, String> getDefaultRunEnvVars(){
		JBossExtendedProperties props = getExtendedProperties();
		return props.getDefaultLaunchArguments().getDefaultRunEnvVars();
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
