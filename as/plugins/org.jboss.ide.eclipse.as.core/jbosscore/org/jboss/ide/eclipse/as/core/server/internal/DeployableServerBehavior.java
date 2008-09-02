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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.ExtensionManager;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel;
import org.jboss.ide.eclipse.as.core.extensions.events.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.publishers.PublisherEventLogger;
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
			
			publisher = ExtensionManager.getDefault().getPublisher(getServer(), module);
			if( publisher != null ) {
				try {
					publisher.publishModule(getServer(), module, publishType,  
							getPublishedResourceDelta(module), 
							modulePublishEvent, monitor);
				} catch( CoreException ce ) {
					throw ce;
				} finally {
					setModulePublishState(module, publisher.getPublishState());
				}
				// add file changed count to top level element
				i = (Integer)publishRootEvent.getProperty(PublisherEventLogger.CHANGED_RESOURCE_COUNT);
				j = (Integer)modulePublishEvent.getProperty(PublisherEventLogger.CHANGED_RESOURCE_COUNT);
				j = j == null ? new Integer(0) : j;
				int count = (i == null ? 0 : i.intValue()) + j.intValue();
				publishRootEvent.setProperty(PublisherEventLogger.CHANGED_RESOURCE_COUNT, count);
			}
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
	
	/*
	 * Change the state of the server
	 * Also, cache the state we think we're setting it to.
	 * 
	 * Much of this can be changed once eclipse bug 231956 is fixed
	 */
	protected int serverStateVal;
	protected int getServerStateVal() {
		return serverStateVal;
	}
	public void setServerStarted() {
		serverStateVal = IServer.STATE_STARTED;
		setServerState(IServer.STATE_STARTED);
	}
	
	public void setServerStarting() {
		serverStateVal = IServer.STATE_STARTING;
		setServerState(IServer.STATE_STARTING);
	}
	
	public void setServerStopped() {
		serverStateVal = IServer.STATE_STOPPED;
		setServerState(IServer.STATE_STOPPED);
	}
	
	public void setServerStopping() {
		serverStateVal = IServer.STATE_STOPPING;
		setServerState(IServer.STATE_STOPPING);
	}
	protected void initialize(IProgressMonitor monitor) {
		serverStateVal =  getServer().getServerState();
		getServer().addServerListener(new IServerListener() {
			public void serverChanged(ServerEvent event) {
				if( event.getState() != serverStateVal ) {
					// something's been changed by the framework and NOT by us. 
					if( serverStateVal == IServer.STATE_STARTING && event.getState() == IServer.STATE_STOPPED) {
						stop(true);
					}
				}
			} 
		} );
	}
}
