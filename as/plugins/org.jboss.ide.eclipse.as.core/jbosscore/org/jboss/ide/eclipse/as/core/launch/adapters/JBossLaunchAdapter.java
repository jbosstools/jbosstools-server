package org.jboss.ide.eclipse.as.core.launch.adapters;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.LaunchableAdapterDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.module.factory.JBossModuleDelegate;
import org.jboss.ide.eclipse.as.core.module.factory.JBossModuleFactory;
import org.jboss.ide.eclipse.as.core.server.JBossServer;


/**
 * Delegates the creation of a launchable object to the 
 * factory that created the module. 
 * 
 * First checks that the server is a JBoss server and our
 * module delegate is a JBoss delegate.
 * 
 * @author rstryker
 *
 */
public class JBossLaunchAdapter extends LaunchableAdapterDelegate {

	public JBossLaunchAdapter() {
		super();
	}

	// Can I launch onto this server? Let's find out
	public Object getLaunchable(IServer server, IModuleArtifact moduleArtifact)
			throws CoreException {
		
		JBossServer jbServer = JBossServerCore.getServer(server);
		if( jbServer == null ) 
			return null;
		
		
		IModule module = moduleArtifact.getModule();
		Object o = module.loadAdapter(JBossModuleDelegate.class, null);
		
		// If this is not a module that I understand (in my framework), return null
		if( o == null ) 
			return null;
		
		JBossModuleDelegate delegate = (JBossModuleDelegate)o;
		JBossModuleFactory factory = delegate.getFactory();
		return factory.getLaunchable(delegate);
	}

}
