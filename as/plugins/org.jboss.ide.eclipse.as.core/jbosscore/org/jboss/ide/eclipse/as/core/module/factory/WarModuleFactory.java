package org.jboss.ide.eclipse.as.core.module.factory;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.client.verifiers.WarDeploymentVerifier;
import org.jboss.ide.eclipse.as.core.module.factory.EjbModuleFactory.EjbModuleDelegate;

public class WarModuleFactory extends JBossModuleFactory {

	private final static String WAR_MODULE_TYPE = "jboss.web";
	private final static String WAR_MODULE_VERSION_3_2 = "3.2";
	private final static String WAR_DESCRIPTOR = "WEB-INF/web.xml";
	private final static String JBOSS_WAR_DESCRIPTOR = "WEB-INF/jboss-web.xml";

	
	public void initialize() {
	}

	public boolean supports(IResource resource) {
		if( resource.getFileExtension() == null ) return false;
		if( !"war".equals(resource.getFileExtension())) return false;
		if( !resource.exists()) return false;
		
		try {
			File f = resource.getLocation().toFile();
			JarFile jf = new JarFile(f);
			
			JarEntry entry = jf.getJarEntry(WAR_DESCRIPTOR);
			if( entry == null ) return false;			
		} catch( IOException ioe ) {
			return false;
		}
		
		
		return true;
	}

	protected IModule acceptAddition(IResource resource) {
		if( !supports(resource)) return null;
		
		// otherwise create the module
		IModule module = createModule(getPath(resource), resource.getName(), 
				WAR_MODULE_TYPE, WAR_MODULE_VERSION_3_2, resource.getProject());
		
		
		WarModuleDelegate delegate = new WarModuleDelegate();
		delegate.initialize(module);
		delegate.setResource(resource);
		delegate.setFactory(this);
		
		// and insert it
		pathToModule.put(getPath(resource), module);
		moduleToDelegate.put(module, delegate);
		
		return module;

	}

	public Object getLaunchable(JBossModuleDelegate delegate) {
		if( !(delegate instanceof WarModuleDelegate) ) {
			// If I didn't make this guy... get rid of him!
			return null;
		}
		return new WarDeploymentVerifier(delegate);
	}



	public class WarModuleDelegate extends JBossModuleDelegate {

		public IModule[] getChildModules() {
			return null;
		}

		public void initialize() {
			
		}

		public IModuleResource[] members() throws CoreException {
			return new IModuleResource[] { };
		}

		public IStatus validate() {
			return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID, 
					0, "Deployment is valid", null);
		}
		
	}
	
	
}