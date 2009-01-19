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
			IModule m = SingleDeployableFactory.getFactory().findModule(f);
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
