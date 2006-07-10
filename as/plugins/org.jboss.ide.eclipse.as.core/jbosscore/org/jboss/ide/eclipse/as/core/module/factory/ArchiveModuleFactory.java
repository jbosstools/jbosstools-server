package org.jboss.ide.eclipse.as.core.module.factory;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.internal.ModuleFactory;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.client.verifiers.ArchiveVerifier;
import org.jboss.ide.eclipse.as.core.util.ASDebug;

public class ArchiveModuleFactory extends JBossModuleFactory {
	
	private static String GENERIC_JAR = "jboss.archive";
	private static String VERSION = "1.0";
	
	private static final String FACTORY_ID = "org.jboss.ide.eclipse.as.core.ArchiveDeployer";

	public ArchiveModuleFactory() {
	}

	public void initialize() {
	}

	protected IModule acceptAddition(IResource resource) {
		if( !supports(resource)) 
			return null;

		// otherwise create the module
		String path = getPath(resource);
		IModule module = createModule(path, resource.getName(), 
				GENERIC_JAR, VERSION, resource.getProject());
		
		
		ArchiveModuleDelegate delegate = new ArchiveModuleDelegate();
		delegate.initialize(module);
		delegate.setResource(resource);
		delegate.setFactory(this);
		
		// and insert it
		pathToModule.put(path, module);
		moduleToDelegate.put(module, delegate);
		
		// ensure the factory clears its cache
		ServerPlugin.findModuleFactory(FACTORY_ID).clearModuleCache();		
		
		return module;	
		
	}

	public Object getLaunchable(JBossModuleDelegate delegate) {
		return new ArchiveVerifier(delegate);
	}

	public boolean supports(IResource resource) {
		try {
			File f = resource.getLocation().toFile();
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
