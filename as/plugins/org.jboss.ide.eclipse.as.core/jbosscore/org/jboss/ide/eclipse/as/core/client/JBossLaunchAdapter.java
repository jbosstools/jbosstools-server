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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jst.server.core.EJBBean;
import org.eclipse.jst.server.core.JndiObject;
import org.eclipse.jst.server.core.Servlet;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.LaunchableAdapterDelegate;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.eclipse.wst.server.core.util.WebResource;
import org.jboss.ide.eclipse.as.core.server.attributes.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public class JBossLaunchAdapter extends LaunchableAdapterDelegate {
	public JBossLaunchAdapter() {
		super();
	}

	// Can I launch onto this server? Let's find out
	public Object getLaunchable(IServer server, IModuleArtifact moduleArtifact)
			throws CoreException {
		
		// Only play to jboss servers
		IDeployableServer dserver = ServerConverter.getDeployableServer(server);
		if( dserver == null ) 
			return null;
		
//		if( isJstArtifact(moduleArtifact))
//			return prepareJstArtifact(moduleArtifact, dserver);
        
        return new NullLaunchable();
	}
	
	private boolean isJstArtifact(IModuleArtifact moduleArtifact ) {
		// If we have JST projects being launched......
		if ((moduleArtifact instanceof Servlet))		return true;
		if ((moduleArtifact instanceof WebResource))	return true;
        if ((moduleArtifact instanceof EJBBean))		return true;
        if ((moduleArtifact instanceof JndiObject)) 	return true;

        return false;
	}
	
	private Object prepareJstArtifact(IModuleArtifact moduleObject, ServerDelegate delegate) {
		if ((moduleObject instanceof Servlet) ||(moduleObject instanceof WebResource))
            return prepareHttpLaunchable(moduleObject, delegate);
		
        if((moduleObject instanceof EJBBean) || (moduleObject instanceof JndiObject))
            return prepareJndiLaunchable(moduleObject,delegate);

        return null;
	}
	
	private Object prepareHttpLaunchable(IModuleArtifact moduleObject, ServerDelegate delegate) {
//		try {
//			return new HttpLaunchable(new URL("http://www.google.com"));
//		} catch( Exception e ) {
//		}
//		return null;
		return new NullLaunchable();
	}
	private Object prepareJndiLaunchable(IModuleArtifact moduleObject, ServerDelegate delegate) {
//        Properties props = new Properties();
//        props.put("java.naming.factory.initial","org.jnp.interfaces.NamingContextFactory");
//        props.put("java.naming.provider.url","");
//        props.put("java.naming.factory.url.pkgs","org.jboss.naming:org.jnp.interfaces");
//        return new JndiLaunchable(props, "Hello");
		return new NullLaunchable();
	}
}
