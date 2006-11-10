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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.ModuleArtifactAdapterDelegate;

public class ArchiveModuleArtifactAdapter extends ModuleArtifactAdapterDelegate {
	public IModuleArtifact getModuleArtifact(Object obj) {
		if( obj instanceof IResource ) {
			try {
				IResource res = (IResource)obj;
				if( ArchiveModuleFactory.getDefault() == null ) {
					ModuleFactory[] factories = ServerPlugin.getModuleFactories(); // just make sure they're loaded
					for( int i = 0; i < factories.length; i++ ) {
						if( factories[i].getId().equals(ArchiveModuleFactory.FACTORY_ID)) {
							factories[i].getDelegate(new NullProgressMonitor());
						}
					}
				}
				if( !ArchiveModuleFactory.getDefault().supports(res)) return null;
				
				final IModule mod = ArchiveModuleFactory.getDefault().getModule(res);
				return new IModuleArtifact() {
					public IModule getModule() {
						return mod;
					} 
				};
			} catch( Throwable t ) {
				t.printStackTrace();
			}
		}
		return null;
	}
}