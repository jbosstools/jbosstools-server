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
package org.jboss.ide.eclipse.as.core.client;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

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
import org.jboss.ide.eclipse.as.core.server.JBossServer;

public class JBossLaunchAdapter extends LaunchableAdapterDelegate {
	public JBossLaunchAdapter() {
		super();
	}
	private static final String JAVA_NAMING_PROVIDER_URL_PROPKEY = "java.naming.provider.url"; //$NON-NLS-1$
	private static final String JAVA_NAMING_FACTORY_INITIAL_PROPKEY = "java.naming.factory.initial"; //$NON-NLS-1$

	/*
	 * @see ILaunchableAdapterDelegate#getLaunchable(IServer, IModuleObject)
	 */
	public Object getLaunchable(IServer server, IModuleArtifact moduleObject) {
		ServerDelegate delegate = (ServerDelegate)server.loadAdapter(ServerDelegate.class,null);
		if (!(delegate instanceof JBossServer))
			return null;
		if ((moduleObject instanceof Servlet) ||(moduleObject instanceof WebResource))
            return prepareHttpLaunchable(moduleObject, (JBossServer) delegate);
		
        if((moduleObject instanceof EJBBean) || (moduleObject instanceof JndiObject))
            return prepareJndiLaunchable(moduleObject, (JBossServer)delegate);
		return null;
	}

    private Object prepareJndiLaunchable(IModuleArtifact moduleObject, JBossServer delegate) {
        JndiLaunchable launchable = null;
        Properties props = new Properties();
//        props.put(JAVA_NAMING_FACTORY_INITIAL_PROPKEY,definition.getJndiConnection().getInitialContextFactory());
//        props.put(JAVA_NAMING_PROVIDER_URL_PROPKEY,definition.getJndiConnection().getProviderUrl());
//        List jps = definition.getJndiConnection().getJndiProperty();
//        Iterator propsIt =jps.iterator();
//        while(propsIt.hasNext()){
//            ArgumentPair prop = (ArgumentPair)propsIt.next();
//            props.put(prop.getName(),prop.getValue());
//        }
        
        if(moduleObject instanceof EJBBean)
        {
            EJBBean bean = (EJBBean)moduleObject;
            launchable = new JndiLaunchable(props,bean.getJndiName());
        }
        if(moduleObject instanceof JndiObject)
        {
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
    private Object prepareHttpLaunchable(IModuleArtifact moduleObject, JBossServer delegate) {
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
					url = new URL(url, "servlet/" + servlet.getServletClassName()); //$NON-NLS-1$
			} else if (moduleObject instanceof WebResource) {
				WebResource resource = (WebResource) moduleObject;
				String path = resource.getPath().toString();
				if (path != null && path.startsWith("/") && path.length() > 0) //$NON-NLS-1$
					path = path.substring(1);
				if (path != null && path.length() > 0)
					url = new URL(url, path);
			} 
			return new HttpLaunchable(url);
		} catch (Exception e) {
			return null;
		}
    }
}
