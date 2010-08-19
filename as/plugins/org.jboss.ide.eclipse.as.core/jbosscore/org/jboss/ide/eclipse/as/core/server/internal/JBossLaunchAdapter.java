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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jst.server.core.EJBBean;
import org.eclipse.jst.server.core.JndiLaunchable;
import org.eclipse.jst.server.core.JndiObject;
import org.eclipse.jst.server.core.Servlet;
import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IURLProvider;
import org.eclipse.wst.server.core.model.LaunchableAdapterDelegate;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.eclipse.wst.server.core.util.HttpLaunchable;
import org.eclipse.wst.server.core.util.WebResource;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;

public class JBossLaunchAdapter extends LaunchableAdapterDelegate {

	private static final String SIMPLE_PORTAL_PATH = "simple-portal"; //$NON-NLS-1$
	private static final String JBOSS_PORTLET = "jboss.portlet"; //$NON-NLS-1$
	private static final String PORTAL_PATH = "portal"; //$NON-NLS-1$
	
	private static final String JAVA_NAMING_PROVIDER_URL_PROPKEY = IJBossServerConstants.NAMING_FACTORY_PROVIDER_URL;
	private static final String JAVA_NAMING_FACTORY_INITIAL_PROPKEY = IJBossServerConstants.NAMING_FACTORY_KEY;
	
	private static final String SERVER_DEFAULT_DEPLOY_JBOSS_PORTAL_SAR = "deploy/jboss-portal.sar"; //$NON-NLS-1$
	
	private static final String SERVER_DEFAULT_DEPLOY_JBOSS_PORTAL_HA_SAR = "deploy/jboss-portal-ha.sar"; //$NON-NLS-1$

	private static final String SERVER_DEFAULT_DEPLOY_SIMPLE_PORTAL = "deploy/simple-portal"; //$NON-NLS-1$
	
	private static final String SERVER_DEFAULT_DEPLOY_SIMPLE_PORTAL_SAR = "deploy/simple-portal.sar"; //$NON-NLS-1$
	
	private static final String SERVER_DEFAULT_DEPLOY_GATEIN = "deploy/gatein.ear"; //$NON-NLS-1$

	
	public JBossLaunchAdapter() {
		
	}

	/*
	 * @see ILaunchableAdapterDelegate#getLaunchable(IServer, IModuleObject)
	 */
	public Object getLaunchable(IServer server, IModuleArtifact moduleObject) {
		if (server != null) {
			ServerDelegate delegate = (ServerDelegate)server.loadAdapter(ServerDelegate.class,null);
			if (!(delegate instanceof JBossServer ))
				return null;
			if ((moduleObject instanceof Servlet) ||(moduleObject instanceof WebResource))
	            return prepareHttpLaunchable(moduleObject, delegate, server);
			
	        if((moduleObject instanceof EJBBean) || (moduleObject instanceof JndiObject))
	            return prepareJndiLaunchable(moduleObject,delegate);
		}
		return null;
	}

    private Object prepareJndiLaunchable(IModuleArtifact moduleObject, ServerDelegate delegate) {
        JndiLaunchable launchable = null;
        JBossServer server = (JBossServer)delegate;
        IPath p = new Path(server.getConfigDirectory())
        	.append(IJBossServerConstants.JNDI_PROPERTIES);
        Properties props = new Properties();
        try  {
	        props.load(new FileInputStream(p.toFile()));
        } catch( IOException ioe ) {
            props.put(JAVA_NAMING_FACTORY_INITIAL_PROPKEY, 
            		IJBossServerConstants.NAMING_FACTORY_VALUE);
            props.put(JAVA_NAMING_PROVIDER_URL_PROPKEY,
            		IJBossServerConstants.NAMING_FACTORY_INTERFACES);
        }
	
        if(moduleObject instanceof EJBBean) {
            EJBBean bean = (EJBBean)moduleObject;
            launchable = new JndiLaunchable(props,bean.getJndiName());
        }
        if(moduleObject instanceof JndiObject)  {
            JndiObject jndi = (JndiObject)moduleObject;
            launchable = new JndiLaunchable(props,jndi.getJndiName());
        }
        return launchable;
    }

    /**
     * @param moduleObject
     * @param delegate
     * @param server 
     * @return object
     */
    private Object prepareHttpLaunchable(IModuleArtifact moduleObject, ServerDelegate delegate, IServer server) {
        try {
			URL url = ((IURLProvider) delegate).getModuleRootURL(moduleObject.getModule());

			if (moduleObject instanceof Servlet) {
				Servlet servlet = (Servlet) moduleObject;
				if (servlet.getAlias() != null) {
					String path = servlet.getAlias();
					if (path.startsWith("/")) //$NON-NLS-1$
						path = path.substring(1);
					url = new URL(url, path);
				} else
					url = new URL(url, servlet.getName()); 
			} else if (moduleObject instanceof WebResource) {
				WebResource resource = (WebResource) moduleObject;
				String path = resource.getPath().toString();
				if (path != null && path.startsWith("/")) //$NON-NLS-1$
					path = path.substring(1);
				if (path != null && path.length() > 0) {
					//path = getServlet30Mapping(resource, path);
					url = new URL(url, path);
				}
			}
			URL portletURL = getPortletURL(moduleObject, delegate, server);
			if (portletURL != null) {
				url = portletURL;
			}
			return new HttpLaunchable(url);
		} catch (MalformedURLException e) {
			return null; // no launchable available.
		}
    }

	private URL getPortletURL(IModuleArtifact moduleObject, ServerDelegate delegate, IServer server) {
		IModule module = moduleObject.getModule();
		if (module != null && server != null) { 
			IProject project = module.getProject();
			if (project != null) {
				try {
					if (FacetedProjectFramework.hasProjectFacet(project, JBOSS_PORTLET)) {
						IRuntime runtime = server.getRuntime();
						if (runtime == null || runtime.getLocation() == null) {
							return null;
						}
						IJBossServerRuntime jbossRuntime = (IJBossServerRuntime)runtime.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
						if (jbossRuntime != null) {
							String urlString = "http://" + server.getHost(); //$NON-NLS-1$
							if (delegate instanceof JBossServer) {
								JBossServer jBossServer = (JBossServer) delegate;
								urlString = urlString + ":" + jBossServer.getJBossWebPort() + "/"; //$NON-NLS-1$ //$NON-NLS-2$
							} else {
								return null;
							}
							URL url = new URL(urlString);
							IPath jbossLocation = runtime.getLocation();
							IPath configPath = jbossLocation.append(IJBossServerConstants.SERVER).append(jbossRuntime.getJBossConfiguration());
							File configFile = configPath.toFile();
							// JBoss Portal server
							if (exists(configFile, SERVER_DEFAULT_DEPLOY_JBOSS_PORTAL_SAR)) {
								return new URL(url,PORTAL_PATH);
							}
							// JBoss Portal clustering server
							if (exists(configFile,
									SERVER_DEFAULT_DEPLOY_JBOSS_PORTAL_HA_SAR)) {
								return new URL(url,PORTAL_PATH);
							}
							// JBoss portletcontainer
							if (exists(configFile,SERVER_DEFAULT_DEPLOY_SIMPLE_PORTAL) ||
									exists(configFile,SERVER_DEFAULT_DEPLOY_SIMPLE_PORTAL_SAR)) {
								return new URL(url,SIMPLE_PORTAL_PATH);
							}
							// GateIn Portal Server
							if (exists(configFile, SERVER_DEFAULT_DEPLOY_GATEIN)) {
								return new URL(url,PORTAL_PATH);
							}
						}
					}
				} catch (MalformedURLException e) {
					// ignore
				} catch (CoreException e) {
					// ignore
				}
			}
		}
		return null;
	}

	private static boolean exists(final File location,String portalDir) {
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			portalDir = portalDir.replace("/", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		File file = new File(location,portalDir);
		return file.exists();
	}
}
