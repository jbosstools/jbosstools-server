/**
 * JBoss, a Division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
* This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.core.server.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
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
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.modules.PackageModuleFactory;
import org.jboss.ide.eclipse.as.core.publishers.JstPublisher;
import org.jboss.ide.eclipse.as.core.publishers.NullPublisher;
import org.jboss.ide.eclipse.as.core.publishers.PackagesPublisher;
import org.jboss.ide.eclipse.as.core.publishers.PublisherEventLogger;
import org.jboss.ide.eclipse.as.core.publishers.SingleFilePublisher;
import org.jboss.ide.eclipse.as.core.publishers.PublisherEventLogger.PublishEvent;
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
	
	protected PublishEvent publishRootEvent;
	protected void publishStart(IProgressMonitor monitor) throws CoreException {
		publishRootEvent = new PublishEvent(null, PublisherEventLogger.ROOT_EVENT);
	}

	protected void publishFinish(IProgressMonitor monitor) throws CoreException {
        IModule[] modules = this.getServer().getModules();
        boolean allpublished= true;
        for (int i = 0; i < modules.length; i++) {
        	if(this.getServer().getModulePublishState(new IModule[]{modules[i]})!=IServer.PUBLISH_STATE_NONE)
                allpublished=false;
        }
        if(allpublished)
            setServerPublishState(IServer.PUBLISH_STATE_NONE);
        
        if( publishRootEvent.getChildren().length != 0 ) {
    		EventLogTreeItem root = EventLogModel.getModel(getServer()).getRoot();
    		root.addChild(publishRootEvent);
        }
		publishRootEvent = null;
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
		int modulePublishState = getServer().getModulePublishState(module);
		int publishType = getPublishType(kind, deltaKind, modulePublishState);
		IJBossServerPublisher publisher;
		

		if( module.length > 0 && publishType != IJBossServerPublisher.NO_PUBLISH) {
			Integer i,j;
			i = (Integer)publishRootEvent.getProperty(PublisherEventLogger.CHANGED_MODULE_COUNT);
			publishRootEvent.setProperty(PublisherEventLogger.CHANGED_MODULE_COUNT, new Integer(i == null ? 1 : i.intValue()+1));
			PublishEvent modulePublishEvent = PublisherEventLogger.createModuleRootEvent(publishRootEvent, module, kind, deltaKind, modulePublishState);
			
			IModule lastMod = module[module.length -1];
			if( lastMod.getProject() != null && 
					ModuleCoreNature.isFlexibleProject(lastMod.getProject())) {
				publisher = new JstPublisher(getServer(), modulePublishEvent);
			} else if( isPackagesTypeModule(lastMod) ) {
				publisher = new PackagesPublisher(getServer(), modulePublishEvent);
			} else if( lastMod.getModuleType().getId().equals("jboss.singlefile")){
				publisher = new SingleFilePublisher(getServer(), modulePublishEvent);
			} else {
				publisher = new NullPublisher();
			}
			publisher.setDelta(getPublishedResourceDelta(module));
			try {
				publisher.publishModule(module, publishType, monitor);
			} catch( CoreException ce ) {
				throw ce;
			} finally {
				setModulePublishState(module, publisher.getPublishState());
			}
			
			// add file changed count to top level element
			i = (Integer)publishRootEvent.getProperty(PublisherEventLogger.CHANGED_FILE_COUNT);
			j = (Integer)modulePublishEvent.getProperty(PublisherEventLogger.CHANGED_FILE_COUNT);
			j = j == null ? new Integer(0) : j;
			int count = (i == null ? 0 : i.intValue()) + j.intValue();
			publishRootEvent.setProperty(PublisherEventLogger.CHANGED_FILE_COUNT, count);
		}
	}
	
	
	protected int getPublishType(int kind, int deltaKind, int modulePublishState) {
		if (ServerBehaviourDelegate.REMOVED == deltaKind) {
			return IJBossServerPublisher.REMOVE_PUBLISH;
		} else if (kind == IServer.PUBLISH_FULL || modulePublishState == IServer.PUBLISH_STATE_FULL ||  kind == IServer.PUBLISH_CLEAN ) {
			return IJBossServerPublisher.FULL_PUBLISH;
		} else if (kind == IServer.PUBLISH_INCREMENTAL || modulePublishState == IServer.PUBLISH_STATE_INCREMENTAL || kind == IServer.PUBLISH_AUTO) {
			if( ServerBehaviourDelegate.CHANGED == deltaKind ) 
				return IJBossServerPublisher.INCREMENTAL_PUBLISH;
		} 
		return IJBossServerPublisher.NO_PUBLISH;
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
	@Deprecated
	public IStatus publishOneModule(int kind, IModule[] module, int deltaKind, IProgressMonitor monitor) {
		return publishOneModule(module, kind, deltaKind, true, monitor);
	}
	
	/*
	 * hack for eclipse bug 169570
	 */
	public IStatus publishOneModule(IModule[] module, int kind, int deltaKind, boolean recurse, IProgressMonitor monitor) {
		try {
	
			// add it to the server first
			if( module.length == 1 ) 
				addAndRemoveModules(module, deltaKind);

			ArrayList moduleList = new ArrayList(); 
			ArrayList deltaKindList = new ArrayList();
			fillPublishOneModuleLists(module, moduleList, deltaKindList, deltaKind, recurse);
				
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

		} catch( CoreException e ) {
			IStatus s = new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID,
					"Unexpected Exception publishing one module: ", e);
			JBossServerCorePlugin.getDefault().getLog().log(s);
			return s;
		}
	}
	
	protected void fillPublishOneModuleLists(IModule[] module, ArrayList moduleList, ArrayList deltaKindList, int deltaKind, boolean recurse) {
		moduleList.add(module);
		deltaKindList.add(new Integer(deltaKind));
		if( recurse ) {
			ArrayList tmp = new ArrayList();
			IModule[] children = getServer().getChildModules(module, new NullProgressMonitor());
			if( children != null ) {
				for( int i = 0; i < children.length; i++ ) {
					tmp = new ArrayList();
					tmp.addAll(Arrays.asList(module));
					tmp.add(children[i]);
					fillPublishOneModuleLists((IModule[]) tmp.toArray(new IModule[tmp.size()]), moduleList, deltaKindList, deltaKind, recurse);
				}
			}
		}
		
		// if removing, we must remove child first
		if( deltaKind == ServerBehaviourDelegate.REMOVED ) {
			Collections.reverse(moduleList);
			Collections.reverse(deltaKindList);
		}
	}
	
	protected void addAndRemoveModules(IModule[] module, int deltaKind) throws CoreException {
		if( getServer() == null ) return;
		boolean contains = ServerUtil.containsModule(getServer(), module[0], new NullProgressMonitor());

		if( !contains && (deltaKind == ServerBehaviourDelegate.ADDED) || (deltaKind == ServerBehaviourDelegate.CHANGED)) {
			IServerWorkingCopy wc = getServer().createWorkingCopy();
			ServerUtil.modifyModules(wc, module, new IModule[0], new NullProgressMonitor());			
			wc.save(false, new NullProgressMonitor());
		} else if( contains && deltaKind == ServerBehaviourDelegate.REMOVED) {
			IServerWorkingCopy wc = getServer().createWorkingCopy();
			ServerUtil.modifyModules(wc, new IModule[0], module, new NullProgressMonitor());
			wc.save(false, new NullProgressMonitor());
		}
	}
}
