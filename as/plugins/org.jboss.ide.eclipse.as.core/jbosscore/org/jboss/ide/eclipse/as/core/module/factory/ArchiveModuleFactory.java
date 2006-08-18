package org.jboss.ide.eclipse.as.core.module.factory;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.client.verifiers.ArchiveVerifier;

public class ArchiveModuleFactory extends JBossModuleFactory {
	
	private static String GENERIC_JAR = "jboss.archive";
	private static String VERSION = "1.0";
	
	private static final String FACTORY_ID = "org.jboss.ide.eclipse.as.core.ArchiveDeployer";

	public ArchiveModuleFactory() {
	}

	public void initialize() {
	}

	protected IModule acceptAddition(String path) {
		if( !supports(path)) 
			return null;

		// otherwise create the module
		//String path = getPath(resource);
		String name = new Path(path).lastSegment();
		IModule module = createModule(path, name, 
				GENERIC_JAR, VERSION, null);
		
		
		ArchiveModuleDelegate delegate = new ArchiveModuleDelegate();
		delegate.initialize(module);
		delegate.setResourcePath(path);
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

	public boolean supports(String path) {
		try {
			//File f = resource.getLocation().toFile();
			File f = new File(path);
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
