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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.IURLProvider;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathModel;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathQuery;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.ArgsUtil;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.ServerUtil;

/**
 * 
 * @author Rob Stryker rob.stryker@jboss.com
 *
 */
public class JBossServer extends DeployableServer 
		implements IJBossServerConstants, IDeployableServer, IURLProvider {

	public JBossServer() {
	}

	public void setDefaults(IProgressMonitor monitor) {
		super.setDefaults(monitor);
		setAttribute("auto-publish-time", 1); //$NON-NLS-1$
		setAttribute("id", getAttribute("id", (String)"") + new Date().getTime()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public void saveConfiguration(IProgressMonitor monitor) throws CoreException {
		// here we update the launch configuration with any details that might have changed. 
		try {
			Server s = (Server)getServer();
			ILaunchConfiguration lc = s.getLaunchConfiguration(false, new NullProgressMonitor());
			if( lc != null ) {
				String startArgs = lc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
				String originalArgs = startArgs;
				if( !getServer().getHost().equals(getHost(true)))
					startArgs = ArgsUtil.setArg(startArgs, 
							IJBossRuntimeConstants.STARTUP_ARG_HOST_SHORT, 
							IJBossRuntimeConstants.STARTUP_ARG_HOST_LONG, 
							getServer().getHost());
				
				IJBossServerRuntime runtime = (IJBossServerRuntime)
					getServer().getRuntime().loadAdapter(IJBossServerRuntime.class, null);
				String config = runtime.getJBossConfiguration();
				startArgs = ArgsUtil.setArg(startArgs, 
						IJBossRuntimeConstants.STARTUP_ARG_CONFIG_SHORT, 
						IJBossRuntimeConstants.STARTUP_ARG_CONFIG_LONG, config);
				
				if( startArgs != null && !startArgs.trim().equals(originalArgs)) {
					ILaunchConfigurationWorkingCopy wc = lc.getWorkingCopy();
					wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, startArgs);
					wc.doSave();
				}
			}
		} catch( CoreException ce )  {
			IStatus s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, 
					NLS.bind(Messages.CannotSaveServersStartArgs, getServer().getName()), ce);
			JBossServerCorePlugin.getDefault().getLog().log(s);
		}
	}
	
	public String getHost() {
		String host = getHost(true);
		return host == null ? getServer().getHost() : host;
	}
	
	public String getHost(boolean checkLaunchConfig) {
		String host = null;
		if( checkLaunchConfig ) {
			try {
				Server s = (Server)getServer();
				ILaunchConfiguration lc = s.getLaunchConfiguration(true, new NullProgressMonitor());
				if(lc!=null) {
					String startArgs = lc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
					String val = ArgsUtil.getValue(startArgs, 
							IJBossRuntimeConstants.STARTUP_ARG_HOST_SHORT, 
							IJBossRuntimeConstants.STARTUP_ARG_HOST_LONG); 
					if( val != null ) {
						host = val;
					}
				}
			} catch( CoreException ce )  {}
		}
		return host;
	}
	
	public String getConfigDirectory() {
		return getConfigDirectory(true);
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
	
	public String getDeployFolder() {
		return getDeployFolder(this, getDeployLocationType());
	}
	
	public static String getDeployFolder(JBossServer jbs, String type) {
		IServer server = jbs.getServer();
		IJBossServerRuntime jbsrt = getRuntime(server);
		if( type.equals(DEPLOY_CUSTOM))
			return ServerUtil.makeGlobal(jbsrt, new Path(jbs.getAttribute(DEPLOY_DIRECTORY, ""))).toString(); //$NON-NLS-1$
		if( type.equals(DEPLOY_METADATA)) {
			return JBossServerCorePlugin.getServerStateLocation(server).
				append(IJBossServerConstants.DEPLOY).makeAbsolute().toString();
		} else if( type.equals(DEPLOY_SERVER)) {
			String loc = jbsrt.getConfigLocation();
			String config = jbsrt.getJBossConfiguration();
			IPath p = new Path(loc).append(config)
				.append(IJBossServerConstants.DEPLOY).makeRelative();
			return p.toString();
		}
		return null;
	}
	
	protected String getDeployFolder(boolean checkLaunchConfig) {
		return new Path(getConfigDirectory(checkLaunchConfig) + Path.SEPARATOR + DEPLOY).toOSString();
	}

	
	public String getTempDeployFolder() {
		IJBossServerRuntime jbsrt = getRuntime();
		String type = getDeployLocationType();
		if( type.equals(DEPLOY_CUSTOM))
			return ServerUtil.makeGlobal(jbsrt, new Path(getAttribute(TEMP_DEPLOY_DIRECTORY, ""))).toString(); //$NON-NLS-1$
		if( type.equals(DEPLOY_METADATA)) {
			return JBossServerCorePlugin.getServerStateLocation(getServer()).
				append(IJBossServerConstants.TEMP_DEPLOY).makeAbsolute().toString();
		} else if( type.equals(DEPLOY_SERVER)) {
			String loc = jbsrt.getConfigLocation();
			String config = jbsrt.getJBossConfiguration();
			IPath p = new Path(loc)
				.append(config).append(IJBossServerConstants.TMP)
				.append(IJBossServerConstants.JBOSSTOOLS_TMP).makeRelative();
			return p.toString();
		}
		return null;
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
						(String)map.get(JBOSS_SERVER_NAME) : DEFAULT_CONFIGURATION;
				return (String)map.get(JBOSS_SERVER_BASE_DIR) + Path.SEPARATOR + name;
			}
			
			if( map.get(JBOSS_HOME_DIR) != null ) {
				return (String)map.get(JBOSS_HOME_DIR) + Path.SEPARATOR + SERVER 
					+ Path.SEPARATOR + DEFAULT_CONFIGURATION;
			}
		} catch( CoreException ce ) {
		}
		return null;
	}
	
	protected String getRuntimeConfigDirectory() {
		IJBossServerRuntime runtime = (IJBossServerRuntime)
			getServer().getRuntime().loadAdapter(IJBossServerRuntime.class, null);
		String p = getServer().getRuntime().getLocation().toOSString() + Path.SEPARATOR + SERVER + 
				Path.SEPARATOR + runtime.getJBossConfiguration();
		return new Path(p).toOSString();
	}
	
	public int getJNDIPort() {
		return findPort(JNDI_PORT, JNDI_PORT_DETECT, JNDI_PORT_DETECT_XPATH, 
				JNDI_PORT_DEFAULT_XPATH, JNDI_DEFAULT_PORT);
	}
	
	public int getJBossWebPort() {
		return findPort(WEB_PORT, WEB_PORT_DETECT, WEB_PORT_DETECT_XPATH, 
				WEB_PORT_DEFAULT_XPATH, JBOSS_WEB_DEFAULT_PORT);
	}

	protected int findPort(String attributeKey, String detectKey, String xpathKey, String defaultXPath, int defaultValue) {
		boolean detect = getAttribute(detectKey, true);
		String result = null;
		if( !detect ) {
			result = getAttribute(attributeKey, (String)null);
		} else {
			String xpath = getAttribute(xpathKey, defaultXPath);
			XPathQuery query = XPathModel.getDefault().getQuery(getServer(), new Path(xpath));
			if(query!=null) {
				result = query.getFirstResult();
			}
		}
		
		if( result != null ) {
			try {
				return Integer.parseInt(result);
			} catch(NumberFormatException nfe) {
				return defaultValue;
			}
		}
		return defaultValue;
	}
	
	
	
	public URL getModuleRootURL(IModule module) {

        if (module == null || module.loadAdapter(IWebModule.class,null)==null )
			return null;
        
        IWebModule webModule =(IWebModule)module.loadAdapter(IWebModule.class,null);
        String host = getHost();
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
	
	
	// first class parameters
	public String getUsername() {
		return getAttribute(SERVER_USERNAME, ""); //$NON-NLS-1$
	}
	public void setUsername(String name) {
		setAttribute(SERVER_USERNAME, name);
	}

	public String getPassword() {
		return getAttribute(SERVER_PASSWORD, ""); //$NON-NLS-1$
	}
	public void setPassword(String pass) {
		setAttribute(SERVER_PASSWORD, pass);
	}

}