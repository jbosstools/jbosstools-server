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
package org.jboss.ide.eclipse.as.core.module.factory;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.client.verifiers.EarDeploymentVerifier;
import org.jboss.ide.eclipse.as.core.util.ASDebug;

public class EarModuleFactory extends JBossModuleFactory {

	private final static String EAR_MODULE_TYPE = "jboss.ear";
	private final static String EAR_MODULE_VERSION_3_2 = "3.2";
	private final static String EAR_DESCRIPTOR = "META-INF/application.xml";
	private final static String JBOSS_EAR_DESCRIPTOR = "META-INF/jboss.xml";

	
	public void initialize() {
	}

	public boolean supports(IResource resource) {
		if( resource.getFileExtension() == null ) return false;
		if( !"ear".equals(resource.getFileExtension())) return false;
		
		if( !resource.exists()) return false;

		try {
			File f = resource.getLocation().toFile();
			JarFile jf = new JarFile(f);
			
			JarEntry entry = jf.getJarEntry(EAR_DESCRIPTOR);
			if( entry == null ) return false;			
		} catch( IOException ioe ) {
			return false;
		}
		
		return true;
	}

	protected IModule acceptAddition(IResource resource) {
		 //return null if I don't support it
		if( !supports(resource)) 
			return null;

		// otherwise create the module
		String path = getPath(resource);
		IModule module = createModule(path, resource.getName(), 
				EAR_MODULE_TYPE, EAR_MODULE_VERSION_3_2, resource.getProject());
		
		
		EarModuleDelegate delegate = new EarModuleDelegate();
		delegate.initialize(module);
		delegate.setResource(resource);
		delegate.setFactory(this);
		
		// and insert it
		pathToModule.put(path, module);
		moduleToDelegate.put(module, delegate);
		
		return module;
	}

	public Object getLaunchable(JBossModuleDelegate delegate) {
		if( !(delegate instanceof EarModuleDelegate) ) {
			// If I didn't make this guy... get rid of him!
			return null;
		}
		return new EarDeploymentVerifier(delegate);
	}

	
	public class EarModuleDelegate extends JBossModuleDelegate {

		public IModule[] getChildModules() {
			return null;
		}

		public void initialize() {
		}

		public IStatus validate() {
			return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, 
					0, "Deployment is valid", null);
		}

		public IModuleResource[] members() throws CoreException {
			return new IModuleResource[0];
		}
	}
}
