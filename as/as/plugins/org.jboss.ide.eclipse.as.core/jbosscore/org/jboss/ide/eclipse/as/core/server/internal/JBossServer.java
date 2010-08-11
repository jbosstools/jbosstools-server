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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IURLProvider;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathModel;
import org.jboss.ide.eclipse.as.core.extensions.descriptors.XPathQuery;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
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
	
	public String getHost() {
		return getServer().getHost();
	}
		
	public String getConfigDirectory() {
		IJBossServerRuntime runtime = (IJBossServerRuntime)getServer().getRuntime().loadAdapter(IJBossServerRuntime.class, null);
		return runtime.getConfigLocationFullPath().toOSString();
	}
	
	public String getDeployFolder() {
		return getDeployFolder(this, getDeployLocationType());
	}
	
	public static String getDeployFolder(JBossServer jbs, String type) {
		IServer server = jbs.getServer();
		IJBossServerRuntime jbsrt = getRuntime(server);
		if( type.equals(DEPLOY_CUSTOM)) {
			String val = jbs.getAttribute(DEPLOY_DIRECTORY, (String)null);
			if( val != null ) {
				IPath val2 = new Path(val);
				return ServerUtil.makeGlobal(jbsrt, val2).toString();
			}
			// if no value is set, default to metadata
			type = DEPLOY_METADATA;
		}
		if( type.equals(DEPLOY_METADATA)) {
			return JBossServerCorePlugin.getServerStateLocation(server).
				append(IJBossServerConstants.DEPLOY).makeAbsolute().toString();
		} else if( type.equals(DEPLOY_SERVER)) {
			String loc = jbsrt.getConfigLocation();
			String config = jbsrt.getJBossConfiguration();
			IPath p = new Path(loc).append(config)
				.append(IJBossServerConstants.DEPLOY);
			return ServerUtil.makeGlobal(jbsrt, p).toString();
		}
		return null;
	}
	
	public String getTempDeployFolder() {
		return getTempDeployFolder(this, getDeployLocationType());
	}
	
	public static String getTempDeployFolder(JBossServer jbs, String type) {
		IServer server = jbs.getServer();
		IJBossServerRuntime jbsrt = getRuntime(server);
		if( type.equals(DEPLOY_CUSTOM))
			return ServerUtil.makeGlobal(jbsrt, new Path(server.getAttribute(TEMP_DEPLOY_DIRECTORY, ""))).toString(); //$NON-NLS-1$
		if( type.equals(DEPLOY_METADATA)) {
			return JBossServerCorePlugin.getServerStateLocation(server).
				append(IJBossServerConstants.TEMP_DEPLOY).makeAbsolute().toString();
		} else if( type.equals(DEPLOY_SERVER)) {
			String loc = jbsrt.getConfigLocation();
			String config = jbsrt.getJBossConfiguration();
			IPath p = new Path(loc)
				.append(config).append(IJBossServerConstants.TMP)
				.append(IJBossServerConstants.JBOSSTOOLS_TMP);
			return ServerUtil.makeGlobal(jbsrt, p).toString();
		}
		return null;
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