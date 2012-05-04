/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.modules;

import org.eclipse.core.resources.IProject;
import org.eclipse.jst.j2ee.internal.web.deployables.WebDeployableArtifactUtil;
import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.model.ModuleArtifactAdapterDelegate;
import org.eclipse.wst.server.core.model.ModuleDelegate;
import org.jboss.ide.eclipse.as.core.server.IModuleArtifact2;

public class EarArtifactAdapter extends ModuleArtifactAdapterDelegate {

	public EarArtifactAdapter() {
	}

	@Override
	public IModuleArtifact getModuleArtifact(Object obj) {
		if( obj instanceof IProject) {
			IProject p = (IProject)obj;
			IModule[] mods = ServerUtil.getModules(p);
			for( int i = 0; i < mods.length; i++ ) {
				if( mods[i].getModuleType().getId().equals(IModuleConstants.JST_EAR_MODULE)) {
					return getArtifactFor(mods[i]);
				}
			}
		}
		return null;
	}

	protected IModuleArtifact getArtifactFor(IModule ear) {
		ModuleDelegate del = (ModuleDelegate)ear.loadAdapter(ModuleDelegate.class, null);
		IModule[] children = del.getChildModules();
		IModuleArtifact tmp = null;
		for( int i = 0; i < children.length; i++ ) {
			String typeId = children[i].getModuleType().getId();
			if( typeId.equals(IModuleConstants.JST_WEB_MODULE) || typeId.equals(IModuleConstants.WST_WEB_MODULE))
				tmp = WebDeployableArtifactUtil.getModuleObject(children[i].getProject());
			if( tmp != null )  {
				return new EarModuleArtifact(ear, tmp);
			}
		}
		return null;
	}
	
	public static class EarModuleArtifact implements IModuleArtifact2 {
		private IModule earModule;
		private IModuleArtifact childArtifact;
		public EarModuleArtifact(IModule ear, IModuleArtifact child) {
			this.earModule = ear;
			this.childArtifact = child;
		}
		public IModule getModule() {
			return earModule;
		}
		public IModuleArtifact getChildArtifact() {
			return childArtifact;
		}
		public IModule[] getModuleTree(IServer server) {
			return new IModule[]{earModule, childArtifact.getModule()};
		}
	}
}
