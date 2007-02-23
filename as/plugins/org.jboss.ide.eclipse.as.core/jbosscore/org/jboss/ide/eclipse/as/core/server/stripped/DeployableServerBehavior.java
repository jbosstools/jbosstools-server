package org.jboss.ide.eclipse.as.core.server.stripped;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.IModuleVisitor;
import org.eclipse.wst.server.core.internal.Module;
import org.eclipse.wst.server.core.internal.ProgressUtil;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ModuleFactoryDelegate;
import org.eclipse.wst.server.core.model.PublishOperation;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCore;
import org.jboss.ide.eclipse.as.core.module.PackageModuleFactory;
import org.jboss.ide.eclipse.as.core.module.PathModuleFactory;
import org.jboss.ide.eclipse.as.core.publishers.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.publishers.JstPackagesPublisher;
import org.jboss.ide.eclipse.as.core.publishers.NullPublisher;
import org.jboss.ide.eclipse.as.core.publishers.PackagesPublisher;
import org.jboss.ide.eclipse.as.core.publishers.PathPublisher;
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

		System.out.print("publishing module (" + module[0].getName() + "): ");
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
		System.out.println(" to server " + getServer().getId());
		
		if( module.length == 0 ) return;
		IJBossServerPublisher publisher;

		int modulePublishState = getServer().getModulePublishState(module) + 0;
		
		/**
		 * If our modules are already packaged as ejb jars, wars, aop files, 
		 * then go ahead and publish
		 */
		if( arePathModules(module)) {
			publisher = new PathPublisher(JBossServerCore.getDeployableServer(getServer()), this);
		} else if( areJstStyleModules(module)){
			publisher = new JstPackagesPublisher(JBossServerCore.getDeployableServer(getServer()));
//			} else if( hasPackagingConfiguration(module) ) {
//			publisher = new PackagesPublisher(JBossServerCore.getDeployableServer(getServer()));
		} else if( module[0].getModuleType().getId().equals(PackageModuleFactory.MODULE_TYPE)) {
			publisher = new PackagesPublisher(JBossServerCore.getDeployableServer(getServer()));
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
	// TODO: Change to if it is a flex project. Don't know how to do that yet. 
	protected boolean areJstStyleModules(IModule[] module) {
		String type;
		for( int i = 0; i < module.length; i++ ) {
			type = module[i].getModuleType().getId();
			if( type.equals("jst.ejb") || type.equals("jst.client") 
					|| type.equals("jst.web") || type.equals("jst.ear")
					|| type.equals("jbide.ejb30"))
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
	
	
	public IStatus publishOneModule(int kind, IModule[] module, int deltaKind, IProgressMonitor monitor) {
		addAndRemoveModules( module, deltaKind);
		ArrayList moduleList = new ArrayList();
		ArrayList deltaKindList = new ArrayList();
		moduleList.add(module);
		deltaKindList.add(new Integer(deltaKind));
		

		try {
			((Server)getServer()).getServerPublishInfo().startCaching();
			
			
			PublishOperation[] tasks = getTasks(kind, moduleList, deltaKindList);
			MultiStatus tempMulti = new MultiStatus(ServerPlugin.PLUGIN_ID, 0, "", null);
			publishStart(ProgressUtil.getSubMonitorFor(monitor, 1000));
			performTasks(tasks, monitor);
			publishServer(kind, ProgressUtil.getSubMonitorFor(monitor, 1000));
			publishModules(kind, moduleList, deltaKindList, tempMulti, monitor);
			publishFinish(ProgressUtil.getSubMonitorFor(monitor, 500));
			
			final List modules2 = new ArrayList();
			((Server)getServer()).visit(new IModuleVisitor() {
				public boolean visit(IModule[] module) {
					if (((Server)getServer()).getModulePublishState(module) == IServer.PUBLISH_STATE_NONE)
						((Server)getServer()).getServerPublishInfo().fill(module);
					
					modules2.add(module);
					return true;
				}
			}, monitor);
			
			((Server)getServer()).getServerPublishInfo().removeDeletedModulePublishInfo(((Server)getServer()), modules2);
			((Server)getServer()).getServerPublishInfo().clearCache();
			((Server)getServer()).getServerPublishInfo().save();
			
			return null;

		} catch( Exception e ) {
			
		}
		return null;
	}
	
	protected void addAndRemoveModules(IModule[] module, int deltaKind) {
		if( deltaKind != ServerBehaviourDelegate.ADDED && deltaKind != ServerBehaviourDelegate.REMOVED) return;

		if (getServer() != null && !ServerUtil.containsModule(getServer(), module[0], new NullProgressMonitor())) {
			IServerWorkingCopy wc = getServer().createWorkingCopy();
			try {
				if( deltaKind == ServerBehaviourDelegate.ADDED )
					ServerUtil.modifyModules(wc, module, new IModule[0], new NullProgressMonitor());
				else if( deltaKind == ServerBehaviourDelegate.REMOVED) 
					ServerUtil.modifyModules(wc, new IModule[0], module, new NullProgressMonitor());
					
				wc.save(false, new NullProgressMonitor());
			} catch (CoreException ce) {
			}
		}
	}
}
