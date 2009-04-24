/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.modules;

import org.eclipse.core.resources.IFile;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.model.ModuleArtifactAdapterDelegate;

public class SingleDeployableAdapter extends ModuleArtifactAdapterDelegate {

	public SingleDeployableAdapter() {
	}

	public IModuleArtifact getModuleArtifact(Object obj) {
		if( obj instanceof IFile ) {
			IFile f = ((IFile)obj);
			IModule m = SingleDeployableFactory.findModule(f);
			if( m != null )
				return new SingleDeployableModuleArtifact(m);
		}
		return null;
	}
	
	public class SingleDeployableModuleArtifact implements IModuleArtifact {
		private IModule module;
		public SingleDeployableModuleArtifact(IModule m) {
			module = m;
		}
		public IModule getModule() {
			return module;
		}
	}

}
