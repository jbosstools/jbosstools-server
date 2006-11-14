/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.core.server;

import java.io.File;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.model.DescriptorModel;
import org.jboss.ide.eclipse.as.core.model.DescriptorModel.ServerDescriptorModel;
import org.jboss.ide.eclipse.as.core.runtime.IJBossServerLaunchDefaults;
import org.jboss.ide.eclipse.as.core.runtime.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.runtime.server.ServerLaunchDefaults;
import org.jboss.ide.eclipse.as.core.server.attributes.IServerStartupParameters;
import org.jboss.ide.eclipse.as.core.util.ArgsUtil;

public class JBossServer extends ServerDelegate implements IServerStartupParameters {

	
	public JBossServer() {
	}

	
	protected void initialize() {
		
	}
	
	
	public void setDefaults(IProgressMonitor monitor) {
	}
	
	public void importRuntimeConfiguration(IRuntime runtime, IProgressMonitor monitor) throws CoreException {
	}

	public void saveConfiguration(IProgressMonitor monitor) throws CoreException {
	}

	public void configurationChanged() {
	}


	public IJBossServerLaunchDefaults getLaunchDefaults() {
		return new ServerLaunchDefaults(getServer());
	}

	
	
	/*
	 * Abstracts to implement
	 */
	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
		return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID,0, "OK", null);
	}

	public IModule[] getChildModules(IModule[] module) {
		return null;
	}

	// As of now none of my modules are implementing the parent / child nonesense
	public IModule[] getRootModules(IModule module) throws CoreException {
		return new IModule[] { module };
	}

	
	public void modifyModules(IModule[] add, IModule[] remove,
			IProgressMonitor monitor) throws CoreException {
	}
	
	public boolean equals(Object o2) {
		if( !(o2 instanceof JBossServer)) 
			return false;
		JBossServer o2Server = (JBossServer)o2;
		return o2Server.getServer().getId().equals(getServer().getId());
	}
	
	

	public ServerAttributeHelper getAttributeHelper() {
		IServerWorkingCopy copy = getServerWorkingCopy();
		if( copy == null ) {
			copy = getServer().createWorkingCopy();
		}
		return new ServerAttributeHelper(getServer(), copy);
	}

	
	
	
	public String getConfigDirectory() {
		return getConfigDirectory(true);
	}
	public String getDeployDirectory() {
		return getDeployDirectory(true);
	}

	public ServerDescriptorModel getDescriptorModel() {
		String configPath = getConfigDirectory();
		return DescriptorModel.getDefault().getServerModel(new Path(configPath));
	}
	
	public String getConfigDirectory(boolean checkLaunchConfig) {
		if( !checkLaunchConfig ) 
			return getRuntimeConfigDirectory();
		
		String configDir = getLaunchConfigConfigurationDirectory();
		if( configDir == null )  
			return getRuntimeConfigDirectory();

		File f = new File(configDir);
		if( !f.exists() || !f.canRead() || !f.isDirectory())
			return getRuntimeConfigDirectory();

		return configDir;
	}
	
	public String getDeployDirectory(boolean checkLaunchConfig) {
		return getConfigDirectory(checkLaunchConfig) + Path.SEPARATOR + DEPLOY;
	}

	protected String getLaunchConfigConfigurationDirectory() {
		try {
			Server s = (Server)getServer();
			ILaunchConfiguration lc = s.getLaunchConfiguration(true, new NullProgressMonitor());
			String startArgs = lc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS 
					+ JBossServerLaunchConfiguration.PRGM_ARGS_START_SUFFIX, (String)null);
			Map map = ArgsUtil.getSystemProperties(startArgs);
			
			if( map.get(JBOSS_SERVER_HOME_DIR) != null ) 
				return (String)map.get(JBOSS_SERVER_HOME_DIR);

			if( map.get(JBOSS_SERVER_BASE_DIR) != null ) {
				String name = map.get(JBOSS_SERVER_NAME) != null ? 
						(String)map.get(JBOSS_SERVER_NAME) : DEFAULT_SERVER_NAME;
				return (String)map.get(JBOSS_SERVER_BASE_DIR) + Path.SEPARATOR + name;
			}
			
			if( map.get(JBOSS_HOME_DIR) != null ) {
				return (String)map.get(JBOSS_HOME_DIR) + Path.SEPARATOR + SERVER 
					+ Path.SEPARATOR + DEFAULT_SERVER_NAME;
			}
		} catch( CoreException ce ) {
		}
		return null;
	}

	protected String getRuntimeConfigDirectory() {
		IJBossServerRuntime runtime = (IJBossServerRuntime)
			getServer().getRuntime().loadAdapter(IJBossServerRuntime.class, null);
		return getServer().getRuntime().getLocation().toOSString() + Path.SEPARATOR + "server" + 
			Path.SEPARATOR + runtime.getJBossConfiguration();
	}
	
}