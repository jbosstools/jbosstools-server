/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.core.modules;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.model.ModuleArtifactAdapterDelegate;
/**
 * 
 * @author Rob Stryker rob.stryker@redhat.com
 *
 */
public class PackagedArtifactAdapter extends ModuleArtifactAdapterDelegate {

	public PackagedArtifactAdapter() {
	}

	public IModuleArtifact getModuleArtifact(Object obj) {
		if( obj instanceof IJavaProject ) {
			IProject[] projects2 = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			boolean done = false;
			String jpName = ((IJavaProject)obj).getElementName();
			for( int i = 0; i < projects2.length && !done; i++ ) {
				if( projects2[i].getName().equals(jpName)) {
					done = true;
					obj = projects2[i];
					break;
				}
			}
		} 
		
		if( obj instanceof IProject ) {
			PackageModuleFactory factory = PackageModuleFactory.getFactory();
			if( factory != null ) {
//				IModule[] mods = factory.getModulesFromProject((IProject)obj);
//				if( mods != null && mods.length != 0) {
//					return getArtifact(mods);
//				}
			}
		}
		return null;
	}
		
	protected IModuleArtifact getArtifact(IModule[] mod) {
		//return new PackagedArtifact(mod);
		// TODO Blocking on eclipse bug 174372 
		return null;
	}
	
	public class PackagedArtifact implements IModuleArtifact{
		protected IModule mod;
		public PackagedArtifact(IModule module) {
			this.mod = module;
		}
		public IModule getModule() {
			return mod;
		}
	}
}
