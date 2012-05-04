/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.wst.server.core.model.IURLProvider2;
import org.eclipse.wst.server.core.model.LaunchableAdapterDelegate;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.eclipse.wst.server.core.util.WebResource;
import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
import org.jboss.ide.eclipse.as.core.server.IModuleArtifact2;
import org.jboss.ide.eclipse.as.core.server.IMultiModuleURLProvider;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.PortalUtil;

/**
 * 
 * @author Rob Stryker
 *
 */
public class JBossLaunchAdapter extends LaunchableAdapterDelegate {

	private static final String JAVA_NAMING_PROVIDER_URL_PROPKEY = IJBossRuntimeConstants.NAMING_FACTORY_PROVIDER_URL;
	private static final String JAVA_NAMING_FACTORY_INITIAL_PROPKEY = IJBossRuntimeConstants.NAMING_FACTORY_KEY;
	private static final String JBOSS_PORTLET = "jboss.portlet"; //$NON-NLS-1$
	
	public JBossLaunchAdapter() {
		
	}

	/*
	 * @see ILaunchableAdapterDelegate#getLaunchable(IServer, IModuleObject)
	 */
	public Object getLaunchable(IServer server, IModuleArtifact moduleObject) {
		if (server != null) {
			ServerDelegate delegate = (ServerDelegate)server.loadAdapter(ServerDelegate.class,null);
	        if( moduleObject instanceof IModuleArtifact2 ) {
	        	IModuleArtifact2 rootArt = ((IModuleArtifact2)moduleObject);
	        	IModuleArtifact childArt = rootArt.getChildArtifact();
	        	IModule[] tree = rootArt.getModuleTree(server);
	        	if( delegate instanceof IMultiModuleURLProvider) {
	        		URL root = ((IMultiModuleURLProvider)delegate).getModuleRootURL(tree);
					return prepareHttpLaunchable(rootArt, delegate, server, root);
	        	} else {
	        		// Cannot calculate root url from application.xml for this server type.
	        		moduleObject = childArt;
	        	}
	        }
			if ((moduleObject instanceof Servlet) ||(moduleObject instanceof WebResource)) {
				URL root = ((IURLProvider) delegate).getModuleRootURL(moduleObject.getModule());
				return prepareHttpLaunchable(moduleObject, delegate, server, root);
			}
	        if((moduleObject instanceof EJBBean) || (moduleObject instanceof JndiObject))
	            return prepareJndiLaunchable(moduleObject,delegate);
		}
		return null;
	}

    private Object prepareJndiLaunchable(IModuleArtifact moduleObject, ServerDelegate delegate) {
        JndiLaunchable launchable = null;
        JBossServer server = (JBossServer)delegate;
        IPath p = new Path(server.getConfigDirectory())
        	.append(IJBossRuntimeResourceConstants.JNDI_PROPERTIES);
        Properties props = new Properties();
        try  {
	        props.load(new FileInputStream(p.toFile()));
        } catch( IOException ioe ) {
            props.put(JAVA_NAMING_FACTORY_INITIAL_PROPKEY, 
            		IJBossRuntimeConstants.NAMING_FACTORY_VALUE);
            props.put(JAVA_NAMING_PROVIDER_URL_PROPKEY,
            		IJBossRuntimeConstants.NAMING_FACTORY_INTERFACES);
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
    private Object prepareHttpLaunchable(IModuleArtifact moduleObject, ServerDelegate delegate, IServer server, URL rootUrl) {
        try {
			URL url = rootUrl;

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
				// If this is a java file, just stick with the root url
				if( !path.endsWith(".java")) { //$NON-NLS-1$
					if (path != null && path.startsWith("/")) //$NON-NLS-1$
						path = path.substring(1);
					if (path != null && path.length() > 0) {
						//path = getServlet30Mapping(resource, path);
						url = new URL(url, path);
					}
				}
			}
			URL portletURL = getPortletURL(moduleObject, delegate, server);
			if (portletURL != null) {
				url = portletURL;
			}
			return new JBTCustomHttpLaunchable(moduleObject, url);
		} catch (MalformedURLException e) {
			return null; // no launchable available.
		}
    }

	private URL getPortletURL(IModuleArtifact moduleObject, ServerDelegate delegate, IServer server) {
		// null checks all over the place
		IRuntime runtime = server.getRuntime();
		IModule module = moduleObject.getModule();
		if (runtime == null || runtime.getLocation() == null || 
				module == null || server == null || module.getProject() == null) 
			return null;
		
		IJBossServerRuntime jbossRuntime = (IJBossServerRuntime)runtime.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
		if( jbossRuntime== null || !(delegate instanceof JBossServer))
			return null;
		
		try {
			if (FacetedProjectFramework.hasProjectFacet(module.getProject(), JBOSS_PORTLET)) {
				// We are a portal project. Is the runtime also a portal runtime?
				String suffix = PortalUtil.getPortalSuffix(jbossRuntime);
				if( suffix != null ) {
					String urlString = "http://" + server.getHost(); //$NON-NLS-1$
					urlString = urlString + ":" + ((JBossServer)delegate).getJBossWebPort() + "/"; //$NON-NLS-1$ //$NON-NLS-2$
					URL url = new URL(urlString);
					return new URL(url, suffix);
				}
			}
		} catch (MalformedURLException e) {
			// It's not malformed. I know it. I would let it fall through, but hudson doesn't like that
			return null;
		} catch (CoreException e) {
			// The project is not faceted. Even if I check, I cannot prevent this catch. 
			// I'd let it fall through to the return null 2 lines down, but, hudson doesn't like it
			// I wonder if I'll get cited for my coding style having multiple returns next.
			return null;
		}
		// No launchable found
		return null;
	}

	public static class JBTCustomHttpLaunchable {
		private IURLProvider2 urlProvider;
		private IModuleArtifact artifact;
		public JBTCustomHttpLaunchable(IModuleArtifact artifact, final URL url) {
			this.artifact = artifact;
			this.urlProvider = new IURLProvider2() {
				public URL getModuleRootURL(IModule module){
					return url;
				}
				public URL getLaunchableURL() {
					return getModuleRootURL(null);
				}
			};
		}

		public JBTCustomHttpLaunchable(IURLProvider2 urlProvider){
			this.urlProvider = urlProvider;
		}
		public URL getURL() {
			return urlProvider.getLaunchableURL();
		}
		public IModule getModule() {
			return artifact.getModule();
		}
		public IModuleArtifact getArtifact() {
			return artifact;
		}
		public IModule[] getModuleTree(IServer server) {
			return artifact instanceof IModuleArtifact2 ? 
					((IModuleArtifact2)artifact).getModuleTree(server) : 
						new IModule[]{getModule()};
		}
	}
}
