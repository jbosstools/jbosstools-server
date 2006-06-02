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
package org.jboss.ide.eclipse.as.core.launch.adapters;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.LaunchableAdapterDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.module.factory.JBossModuleDelegate;
import org.jboss.ide.eclipse.as.core.module.factory.JBossModuleFactory;
import org.jboss.ide.eclipse.as.core.server.JBossServer;


/**
 * Delegates the creation of a launchable object to the 
 * factory that created the module. 
 * 
 * First checks that the server is a JBoss server and our
 * module delegate is a JBoss delegate.
 * 
 * @author rstryker
 *
 */
public class JBossLaunchAdapter extends LaunchableAdapterDelegate {

	public JBossLaunchAdapter() {
		super();
	}

	// Can I launch onto this server? Let's find out
	public Object getLaunchable(IServer server, IModuleArtifact moduleArtifact)
			throws CoreException {
		
		JBossServer jbServer = JBossServerCore.getServer(server);
		if( jbServer == null ) 
			return null;
		
		
		IModule module = moduleArtifact.getModule();
		Object o = module.loadAdapter(JBossModuleDelegate.class, null);
		
		// If this is not a module that I understand (in my framework), return null
		if( o == null ) 
			return null;
		
		JBossModuleDelegate delegate = (JBossModuleDelegate)o;
		JBossModuleFactory factory = delegate.getFactory();
		return factory.getLaunchable(delegate);
	}

}
