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
import org.jboss.ide.eclipse.as.core.JBossServerCore;

public class JBossModuleArtifactAdapter extends ModuleArtifactAdapterDelegate {
	public IModuleArtifact getModuleArtifact(Object obj) {
		if( obj instanceof IResource ) {
			IResource res = (IResource)obj;
			
			ModuleFactory[] mfs = JBossServerCore.getJBossModuleFactories();
			IModule mod = null;
			for( int i = 0; i < mfs.length && mod == null; i++ ) {
				if( getDelegate(mfs[i]) != null && getDelegate(mfs[i]).supports(res)) {
					mod = getDelegate(mfs[i]).getModule(res);
				}
			}
			if( mod != null )
				return new JBossModuleArtifact(mod);
		}
		return null;
	}
	
	protected JBossModuleFactory getDelegate(ModuleFactory mf) {
		return (JBossModuleFactory) mf.getDelegate(new NullProgressMonitor());
	}
	
	public static class JBossModuleArtifact implements IModuleArtifact {
		private IModule module;
		public JBossModuleArtifact(IModule module) {
			this.module = module;
		}
		public IModule getModule() {
			return this.module;
		}
	}
	
}