package org.jboss.ide.eclipse.as.core.module.artifact;

import org.eclipse.core.resources.IResource;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.model.ModuleArtifactAdapterDelegate;
import org.jboss.ide.eclipse.as.core.model.ModuleModel;
import org.jboss.ide.eclipse.as.core.util.ASDebug;

/**
 * This is the only artifact adapter in the bunch, essentially
 * delegating the conversion to the module factory, and wrapping it in 
 * a simple artifact adapter inner class.
 * 
 * @author rstryker
 *
 */

public class JBossModuleArtifactAdapter extends
		ModuleArtifactAdapterDelegate {

	public IModuleArtifact getModuleArtifact(Object obj) {
		//ASDebug.p("object is " + obj + ", class is " + obj.getClass().getName(), this);
		if( obj instanceof IResource ) {
			try {
				final IModule mod = ModuleModel.getDefault().getModule(((IResource)obj));
				//ASDebug.p("getModuleArtifact, module found: " + mod + ":" + (mod==null?"":mod.getId()), this);
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
