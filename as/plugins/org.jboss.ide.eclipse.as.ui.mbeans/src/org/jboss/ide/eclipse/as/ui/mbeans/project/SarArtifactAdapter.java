package org.jboss.ide.eclipse.as.ui.mbeans.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.model.ModuleArtifactAdapterDelegate;

public class SarArtifactAdapter extends ModuleArtifactAdapterDelegate {
	public IModuleArtifact getModuleArtifact(Object obj) {
		if( obj instanceof IJavaElement ) {
			obj = ((IJavaElement)obj).getJavaProject().getProject();
		}
		if( obj instanceof IResource ) {
			IProject p = ((IResource)obj).getProject();
			if( p != null ) {
				IModule[] mods = ServerUtil.getModules(p);
				if( mods.length == 1 ) {
					return new MBeanNullArtifact(mods[0]);
				}
			}
		}
		return null;
	}
	
	public static class MBeanNullArtifact implements IModuleArtifact {
		private IModule module;
		public MBeanNullArtifact(IModule mod) {
			this.module = mod;
		}
		
		public IModule getModule() {
			return module;
		}
	}

}
