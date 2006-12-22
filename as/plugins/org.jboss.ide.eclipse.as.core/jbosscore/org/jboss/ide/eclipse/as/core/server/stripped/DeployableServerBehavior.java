package org.jboss.ide.eclipse.as.core.server.stripped;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Module;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ModuleFactoryDelegate;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.module.PathModuleFactory;
import org.jboss.ide.eclipse.as.core.publishers.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.publishers.JstPackagesPublisher;
import org.jboss.ide.eclipse.as.core.publishers.NullPublisher;
import org.jboss.ide.eclipse.as.core.publishers.PackagesPublisher;
import org.jboss.ide.eclipse.as.core.publishers.PathPublisher;
import org.jboss.ide.eclipse.as.core.server.JBossServerLaunchConfiguration;
import org.jboss.ide.eclipse.packages.core.model.PackagesCore;

public class DeployableServerBehavior extends ServerBehaviourDelegate {

	public DeployableServerBehavior() {
	}

	public void stop(boolean force) {
		setServerStopped(); // simple enough
	}
	
	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {
		workingCopy.setAttribute(DeployableLaunchConfiguration.ACTION_KEY, DeployableLaunchConfiguration.START);
	}


	
	// By default, goes to check if the members are all the same or any changes
	public IModuleResourceDelta[] getPublishedResourceDelta(IModule[] module) {
		return ((Server)getServer()).getPublishedResourceDelta(module);
	}
	
	protected void publishModule(int kind, int deltaKind, IModule[] module, IProgressMonitor monitor) throws CoreException {
		// kind = [incremental, full, auto, clean] = [1,2,3,4]
		// delta = [no_change, added, changed, removed] = [0,1,2,3]

		System.out.print("publishing module: ");
		switch( kind ) {
			case IServer.PUBLISH_INCREMENTAL: System.out.print("incremental, "); break;
			case IServer.PUBLISH_FULL: System.out.print("full, "); break;
			case IServer.PUBLISH_AUTO: System.out.print("auto, "); break;
			case IServer.PUBLISH_CLEAN: System.out.print("clean, "); break;
		}
		switch( deltaKind ) {
			case ServerBehaviourDelegate.NO_CHANGE: System.out.print("no change"); break;
			case ServerBehaviourDelegate.ADDED: System.out.print("added"); break;
			case ServerBehaviourDelegate.CHANGED: System.out.print("changed"); break;
			case ServerBehaviourDelegate.REMOVED: System.out.print("removed"); break;
		}
		System.out.println("");
		
		if( module.length == 0 ) return;
		IJBossServerPublisher publisher;

		int modulePublishState = getServer().getModulePublishState(module) + 0;
		
		/**
		 * If our modules are already packaged as ejb jars, wars, aop files, 
		 * then go ahead and publish
		 */
		if( arePathModules(module)) {
			publisher = new PathPublisher(JBossServerCore.getDeployableServer(getServer()), this);
		} else if( hasPackagingConfiguration(module) ) {
			publisher = new PackagesPublisher(JBossServerCore.getDeployableServer(getServer()));
		} else if( areJstModules(module)){
			publisher = new JstPackagesPublisher(JBossServerCore.getDeployableServer(getServer()));
		} else {
			publisher = new NullPublisher();
		}
		
		publisher.publishModule(kind, deltaKind, modulePublishState, module, monitor);
		setModulePublishState(module, publisher.getPublishState());
	}
	
	// Is it just a file being deployed? 
	// .xml, or .jar specifically
	public boolean arePathModules(IModule[] module) {
		if( module.length == 1 && module[0] instanceof Module ) {
			ModuleFactoryDelegate delegate = 
				((Module)module[0]).getModuleFactory().getDelegate(new NullProgressMonitor());
			if( delegate instanceof PathModuleFactory ) return true;
		}
		return false;
	}
	
	/* Temporary and will need to be fixed */
	protected boolean areJstModules(IModule[] module) {
		String type;
		for( int i = 0; i < module.length; i++ ) {
			type = module[i].getModuleType().getId();
			if( type.equals("jst.ejb") || type.equals("jst.client") 
					|| type.equals("jst.web") || type.equals("jst.ear")) 
				continue;
			return false;
		}
		return true;
	}
	/* Temporary and will need to be fixed */
	protected boolean hasPackagingConfiguration(IModule[] module) {
		try {
			String projectName = module[0].getName();
			IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			return PackagesCore.getProjectPackages(proj, new NullProgressMonitor()).length == 0 ? false : true;
		} catch( Exception e ) {} 
		return false;
	}
	
	
	
	
	
	/*
	 * Change the state of the server
	 */
	public void setServerStarted() {
		setServerState(IServer.STATE_STARTED);
	}
	
	public void setServerStarting() {
		setServerState(IServer.STATE_STARTING);
	}
	
	public void setServerStopped() {
		setServerState(IServer.STATE_STOPPED);
	}
	
	public void setServerStopping() {
		setServerState(IServer.STATE_STOPPING);
	}
}
