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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.IURLProvider;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathModel;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathQuery;
import org.jboss.ide.eclipse.as.core.runtime.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.attributes.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.attributes.IServerStartupParameters;
import org.jboss.ide.eclipse.as.core.util.ArgsUtil;

public class JBossServer extends ServerDelegate 
		implements IServerStartupParameters, IDeployableServer, IURLProvider {

	public static final String SERVER_USERNAME = "org.jboss.ide.eclipse.as.core.server.userName";
	public static final String SERVER_PASSWORD = "org.jboss.ide.eclipse.as.core.server.password";
	
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

	public ServerPort[] getServerPorts() {
		return new ServerPort[0];
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
	
	public String getConfigDirectory(boolean checkLaunchConfig) {
		if( !checkLaunchConfig ) 
			return getRuntimeConfigDirectory();
		
		String configDir = getLaunchConfigConfigurationDirectory();
		if( configDir == null )  
			return getRuntimeConfigDirectory();

		File f = new File(configDir);
		if( !f.exists() || !f.canRead() || !f.isDirectory())
			return getRuntimeConfigDirectory();

		return new Path(configDir).toOSString();
	}
	
	public String getDeployDirectory(boolean checkLaunchConfig) {
		return new Path(getConfigDirectory(checkLaunchConfig) + Path.SEPARATOR + DEPLOY).toOSString();
	}

	protected String getLaunchConfigConfigurationDirectory() {
		try {
			Server s = (Server)getServer();
			ILaunchConfiguration lc = s.getLaunchConfiguration(true, new NullProgressMonitor());
			String startArgs = lc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
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
		String p = getServer().getRuntime().getLocation().toOSString() + Path.SEPARATOR + "server" + 
				Path.SEPARATOR + runtime.getJBossConfiguration();
		return new Path(p).toOSString();
	}
	
	private static final IPath JNDI_KEY = new Path("Ports").append("JNDI"); 
	private static final int JNDI_DEFAULT_PORT = 1099;
	public int getJNDIPort() {
		int port = findPort(JNDI_KEY);
		return port == -1 ? JNDI_DEFAULT_PORT : port;
	}
	
	private static final IPath JBOSS_WEB_KEY = new Path("Ports").append("JBoss Web");
	public static final int JBOSS_WEB_DEFAULT_PORT = 8080;
	public int getJBossWebPort() {
		int port = findPort(JBOSS_WEB_KEY);
		return port == -1 ? JBOSS_WEB_DEFAULT_PORT : port;
	}

	protected int findPort(IPath path) {
		
			XPathQuery query = XPathModel.getDefault().getQuery(getServer(), path);
			if(query!=null) {
				String result = query.getFirstResult();
				if( result != null ) {
					try {
						return Integer.parseInt(result);
					} catch(NumberFormatException nfe) {
						return -1;
					}
				}
			}		
		return -1;
	}
	
	
	
	public URL getModuleRootURL(IModule module) {

        if (module == null || module.loadAdapter(IWebModule.class,null)==null )
			return null;
        
        IWebModule webModule =(IWebModule)module.loadAdapter(IWebModule.class,null);
        String host = getServer().getHost();
		String url = "http://"+host; //$NON-NLS-1$
		int port = getJBossWebPort();
		if (port != 80)
			url += ":" + port; //$NON-NLS-1$

		url += "/"+webModule.getContextRoot(); //$NON-NLS-1$

		if (!url.endsWith("/")) //$NON-NLS-1$
			url += "/"; //$NON-NLS-1$

		try {
			return new URL(url);
		} catch( MalformedURLException murle) { return null; }
	}
	
	
	public String getUsername() {
		return getAttributeHelper().getAttribute(SERVER_USERNAME, "");
	}
	
	public String getPassword() {
		return getAttributeHelper().getAttribute(SERVER_PASSWORD, "");
	}
}