package org.jboss.ide.eclipse.as.core.server.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerUtil;
import org.eclipse.wst.server.core.internal.IModuleVisitor;
import org.eclipse.wst.server.core.internal.ProgressUtil;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.ServerPlugin;
import org.eclipse.wst.server.core.model.PublishOperation;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.modules.PackageModuleFactory;
import org.jboss.ide.eclipse.as.core.publishers.JstPublisher;
import org.jboss.ide.eclipse.as.core.publishers.NullPublisher;
import org.jboss.ide.eclipse.as.core.publishers.PackagesPublisher;
import org.jboss.ide.eclipse.as.core.publishers.PublisherEventLogger;
import org.jboss.ide.eclipse.as.core.publishers.SingleFilePublisher;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;
import org.jboss.ide.eclipse.as.core.server.internal.launch.DeployableLaunchConfiguration;

public class DeployableServerBehavior extends ServerBehaviourDelegate {

	public DeployableServerBehavior() {
	}

	public void stop(boolean force) {
		setServerStopped(); // simple enough
	}
	
	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {
		workingCopy.setAttribute(DeployableLaunchConfiguration.ACTION_KEY, DeployableLaunchConfiguration.START);
	}
	
	private void print(int kind, int deltaKind, IModule[] module) {
		String name = "";
		for( int i = 0; i < module.length; i++ ) 
			name += module[i].getName();
		
		System.out.print("publishing module (" + name + "): ");
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
		System.out.println(" to server " + getServer().getName() + "(" + getServer().getId() + ")");
	}
	
	/*
	 * The module is a list of module trail points, from parent to child
	 * Thus: 
	 *    {ear, war} for the war portion,  {ear, ejb} for the ejb portion
	 * 
	 * (non-Javadoc)
	 * @see org.eclipse.wst.server.core.model.ServerBehaviourDelegate#publishModule(int, int, org.eclipse.wst.server.core.IModule[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void publishModule(int kind, int deltaKind, IModule[] module, IProgressMonitor monitor) throws CoreException {
		// kind = [incremental, full, auto, clean] = [1,2,3,4]
		// delta = [no_change, added, changed, removed] = [0,1,2,3]
		if( module.length == 0 ) return;
		IJBossServerPublisher publisher;
		print(kind, deltaKind, module);
		int modulePublishState = getServer().getModulePublishState(module) + 0;
		
		EventLogTreeItem root = EventLogModel.getModel(getServer()).getRoot();
		if( module.length > 1 ) {
			root = PublisherEventLogger.createMultipleModuleTopLevelEvent(root, module.length);
		}
		
		if( module.length > 0 ) {
			IModule lastMod = module[module.length -1];
			try {
				if( isJstModule(lastMod) ) {
					publisher = new JstPublisher(getServer(), root);
				} else if( isPackagesTypeModule(lastMod) ) {
					publisher = new PackagesPublisher(getServer(), root);
				} else if( lastMod.getModuleType().getId().equals("jboss.singlefile")){
					publisher = new SingleFilePublisher(getServer());
				} else {
					publisher = new NullPublisher();
				}
				publisher.setDelta(getPublishedResourceDelta(module));
				publisher.publishModule(kind, deltaKind, modulePublishState, module, monitor);
				setModulePublishState(module, publisher.getPublishState());
			} catch( Throwable e ) {
				e.printStackTrace();
			}
		}
	}

	
	
	/* Temporary and will need to be fixed */
	// TODO: Change to if it is a flex project. Don't know how to do that yet. 
	protected boolean isJstModule(IModule mod) {
		String type = mod.getModuleType().getId();
		if( type.equals("jst.ejb") || type.equals("jst.web") || 
				type.equals("jst.ear") || type.equals("jst.utility"))
			return true;
		return false;
	}
	
	protected boolean isPackagesTypeModule(IModule module) {
		return module.getModuleType().getId().equals(PackageModuleFactory.MODULE_TYPE);
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
	
	
	// 	Basically stolen from RunOnServerActionDelegate
	public IStatus publishOneModule(int kind, IModule[] module, int deltaKind, IProgressMonitor monitor) {
		return publishOneModule(module, kind, deltaKind, true, monitor);
	}
	
	/*
	 * hack for eclipse bug 169570
	 */
	public IStatus publishOneModule(IModule[] module, int kind, int deltaKind, boolean recurse, IProgressMonitor monitor) {
	
		// add it to the server first
		if( module.length == 1 ) 
			addAndRemoveModules(module, deltaKind);

		ArrayList moduleList = new ArrayList(); 
		ArrayList deltaKindList = new ArrayList();
		fillPublishOneModuleLists(module, moduleList, deltaKindList, deltaKind, recurse);

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
			
			return Status.OK_STATUS;

		} catch( Exception e ) {
			
		}
		return Status.CANCEL_STATUS;
	}
	
	protected void fillPublishOneModuleLists(IModule[] module, ArrayList moduleList, ArrayList deltaKindList, int deltaKind, boolean recurse) {
		if( recurse ) {
			ArrayList tmp = new ArrayList();
			IModule[] children = getServer().getChildModules(module, new NullProgressMonitor());
			for( int i = 0; i < children.length; i++ ) {
				tmp.addAll(Arrays.asList(module));
				tmp.add(children[i]);
				fillPublishOneModuleLists((IModule[]) tmp.toArray(new IModule[tmp.size()]), moduleList, deltaKindList, deltaKind, recurse);
			}
		}
		moduleList.add(module);
		deltaKindList.add(new Integer(deltaKind));
	}
	
	protected void addAndRemoveModules(IModule[] module, int deltaKind) {
		if( getServer() == null ) return;
		boolean contains = ServerUtil.containsModule(getServer(), module[0], new NullProgressMonitor());
		try {
			if( !contains && (deltaKind == ServerBehaviourDelegate.ADDED) || (deltaKind == ServerBehaviourDelegate.CHANGED)) {
				IServerWorkingCopy wc = getServer().createWorkingCopy();
				ServerUtil.modifyModules(wc, module, new IModule[0], new NullProgressMonitor());			
				wc.save(false, new NullProgressMonitor());
			} else if( contains && deltaKind == ServerBehaviourDelegate.REMOVED) {
				IServerWorkingCopy wc = getServer().createWorkingCopy();
				ServerUtil.modifyModules(wc, new IModule[0], module, new NullProgressMonitor());
				wc.save(false, new NullProgressMonitor());
			}
		} catch( Exception e ) {} // swallowed
	}
}
