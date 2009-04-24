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

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.server.core.EJBBean;
import org.eclipse.jst.server.core.JndiLaunchable;
import org.eclipse.jst.server.core.JndiObject;
import org.eclipse.jst.server.core.Servlet;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IURLProvider;
import org.eclipse.wst.server.core.model.LaunchableAdapterDelegate;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.eclipse.wst.server.core.util.HttpLaunchable;
import org.eclipse.wst.server.core.util.WebResource;
import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;

public class JBossLaunchAdapter extends LaunchableAdapterDelegate {

	private static final String JAVA_NAMING_PROVIDER_URL_PROPKEY = IJBossServerConstants.NAMING_FACTORY_PROVIDER_URL;
	private static final String JAVA_NAMING_FACTORY_INITIAL_PROPKEY = IJBossServerConstants.NAMING_FACTORY_KEY;
	public JBossLaunchAdapter() {
		// TODO Auto-generated constructor stub
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
	            return prepareHttpLaunchable(moduleObject, delegate);
			
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
     * @return object
     */
    private Object prepareHttpLaunchable(IModuleArtifact moduleObject, ServerDelegate delegate) {
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
				if (path != null && path.startsWith("/") && path.length() > 0) //$NON-NLS-1$
					path = path.substring(1);
				if (path != null && path.length() > 0)
					url = new URL(url, path);
			} 
			return new HttpLaunchable(url);
		} catch (MalformedURLException e) {
			return null; // no launchable available.
		}
    }

}
