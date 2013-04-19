/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.archives.webtools.modules;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.ModuleArtifactAdapterDelegate;
import org.jboss.ide.eclipse.archives.core.model.IArchive;
import org.jboss.ide.eclipse.archives.core.model.IArchiveNode;
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

		PackageModuleFactory factory = PackageModuleFactory.getFactory();
		if( factory != null ) {
			if( obj instanceof IProject ) {
				IModule[] mods = factory.getModules((IProject)obj);
				if( mods != null && mods.length != 0) {
					return getArtifact(mods);
				}
			}
			if( obj instanceof IArchiveNode ) {
				obj = ((IArchiveNode)obj).getRootArchive();
			}
			if( obj != null && obj instanceof IArchive ) {
				return getArtifact(getModule(((IArchive)obj)));
			}
		}
		return null;
	}
	

	protected IModule[] getModule(IArchive node) {
		ModuleFactory factory = ServerPlugin.findModuleFactory(PackageModuleFactory.FACTORY_TYPE_ID);
		IModule mod = factory.findModule(PackageModuleFactory.getId(node), new NullProgressMonitor());
		return mod == null ? null : new IModule[] { mod };
	}
	
	protected IModuleArtifact getArtifact(IModule[] mod) {
		if( mod != null && mod.length == 1 && mod[0] != null)
			return new PackagedArtifact(mod[0]);
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
