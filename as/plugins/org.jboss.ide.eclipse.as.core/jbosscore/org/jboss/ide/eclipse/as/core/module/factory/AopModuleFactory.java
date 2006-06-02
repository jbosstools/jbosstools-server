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
import java.util.HashMap;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.client.verifiers.AopDeploymentVerifier;
import org.jboss.ide.eclipse.as.core.util.ASDebug;


/**
 * This class should have no state whatseoever. Merely factory methods.
 * @author rstryker
 *
 */
public class AopModuleFactory extends JBossModuleFactory {
	
	private final static String AOP_MODULE_TYPE = "jboss.aop";
	private final static String AOP_MODULE_VERSION = "2.2222";
	private final static String AOP_DESCRIPTOR = "META-INF/jboss-aop.xml";
	

	public Object getLaunchable(JBossModuleDelegate delegate) {
		if( !(delegate instanceof AopModuleDelegate) ) {
			// If I didn't make this guy... get rid of him!
			return null;
		}
		return new AopDeploymentVerifier(delegate);
	}
	
	protected IModule acceptAddition(IResource resource) {
		if( !supports(resource))
			return null;
		
		return null;
		
			
	}

	public void initialize() {
	}

	public boolean supports(IResource resource) {
		if( resource.getFileExtension() == null ) return false;
		//if( !"jar".equals(resource.getFileExtension())) return false;
		
		if( !resource.exists()) return false;

		try {
			File f = resource.getLocation().toFile();
			JarFile jf = new JarFile(f);
			
			JarEntry entry = jf.getJarEntry(AOP_DESCRIPTOR);
			if( entry == null ) return false;
		} catch( IOException ioe ) {
			return false;
		}
		return true;
	}

	
	
	public class AopModuleDelegate extends JBossModuleDelegate {

		
		public IModule[] getChildModules() {
			return new IModule[] { };
		}

		public void initialize() {
		}

		public IModuleResource[] members() throws CoreException {
			return new IModuleResource[] { };
		}

		public IStatus validate() {
			return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, 
					0, "Deployment is valid", null);
		}

	}

}
