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
package org.jboss.ide.eclipse.as.core.module;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;

public class ArchiveModuleFactory extends JBossModuleFactory {
	
	private static String GENERIC_JAR = "jboss.archive";
	private static String VERSION = "1.0";
	
	public static final String FACTORY_ID = "org.jboss.ide.eclipse.as.core.ArchiveFactory";

	private static ArchiveModuleFactory factory;
	public static ArchiveModuleFactory getDefault() {
		return factory;
	}
	
	public ArchiveModuleFactory() {
		factory = this;
	}

	public void initialize() {
	}

	protected IModule acceptAddition(String path) {
		if( !supports(path)) 
			return null;

		// otherwise create the module
		//String path = getPath(resource);
		String name = new Path(path).lastSegment();
		IModule module = createModule(path, name, 
				GENERIC_JAR, VERSION, null);
		
		
		ArchiveModuleDelegate delegate = new ArchiveModuleDelegate();
		delegate.initialize(module);
		delegate.setResourcePath(path);
		delegate.setFactory(this);
		
		// and insert it
		pathToModule.put(path, module);
		moduleToDelegate.put(module, delegate);
		
		// ensure the factory clears its cache
		clearModuleCache();
		
		return module;	
		
	}

	public boolean supports(String path) {
		try {
			File f = new File(path);
			JarFile jf = new JarFile(f);
			return true;
		} catch( IOException e ) {
		}
		return false;
	}
	
	
	public class ArchiveModuleDelegate extends JBossModuleDelegate {
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
