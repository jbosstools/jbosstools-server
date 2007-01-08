/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ide.eclipse.as.core.publishers;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.model.EventLogModel;
import org.jboss.ide.eclipse.as.core.model.ModuleModel;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogRoot;
import org.jboss.ide.eclipse.as.core.model.EventLogModel.EventLogTreeItem;
import org.jboss.ide.eclipse.as.core.module.JBossModuleDelegate;
import org.jboss.ide.eclipse.as.core.publishers.IJBossServerPublisher.PublishEvent;
import org.jboss.ide.eclipse.as.core.publishers.PackagesPublisher.PackagesPublisherRemoveEvent;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.server.attributes.IDeployableServer;
import org.jboss.ide.eclipse.as.core.server.stripped.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.util.FileUtil;
import org.jboss.ide.eclipse.as.core.util.SimpleTreeItem;

public class PathPublisher implements IJBossServerPublisher  {
	
	private IDeployableServer server;
	private DeployableServerBehavior behavior;
	private int publishState;
	private EventLogRoot eventRoot;
	
	public PathPublisher(IDeployableServer server, DeployableServerBehavior behavior) {
		this.server = server;
		this.behavior = behavior;
		publishState = IServer.PUBLISH_STATE_NONE;
		eventRoot = EventLogModel.getModel(server.getServer()).getRoot();
	}

    public void publishModule(int kind, int deltaKind, int modulePublishState, 
    		IModule[] module, IProgressMonitor monitor) throws CoreException {
		// delta = [no_change, added, changed, removed] = [0,1,2,3]
		// kind = [incremental, full, auto, clean] = [1,2,3,4]
		
		if( deltaKind == ServerBehaviourDelegate.NO_CHANGE ) {
			if( kind == IServer.PUBLISH_FULL || kind == IServer.PUBLISH_CLEAN ) {
				publishModule(module, monitor);
			}
			return;
		}
		
		if( deltaKind == ServerBehaviourDelegate.REMOVED ) {
			unPublishModule(module, monitor);
			return;
		}
		
		if( deltaKind == ServerBehaviourDelegate.ADDED ) {
			publishModule(module, monitor);
			return;
		}
		
		/*
		 * This part will require oversight later. There is no guarentee this 
		 * will remain working if full deltas are implemented. Right now
		 * it basically just says to either republish the entire module or not. 
		 */
		
		if( deltaKind == ServerBehaviourDelegate.CHANGED) {
			boolean republishRequired = false;
			IModuleResourceDelta[] deltas = behavior.getPublishedResourceDelta(module);
			
			if( deltas.length == 0 ) {
				// If it's changed but we don't know the changes...
				republishRequired = true;
			} else {
				for( int i = 0; i < deltas.length; i++ ) {
					int k = deltas[i].getKind();
					if( k == IModuleResourceDelta.ADDED || k == IModuleResourceDelta.CHANGED) {
						// if anything's been added or changed, republish.
						republishRequired = true;
					}
				}
			}
			
			// Now if we're not republishing, we're deleting.
			if( republishRequired ) {
				publishModule(module, monitor);
			} else {
				// The resource associated with this module has been deleted.
				// What to do???  For now ignore it. Only if the module
				// is removed from the server do you specifically delete it.
				
				//unPublishModule(module, monitor);
			}
			
		}
	}

	
	/**
	 * The module has been unambiguously removed from the server model.
	 * Proceed to remove it from the actual server directory.
	 */
	protected void unPublishModule(IModule[] module, IProgressMonitor monitor) {
//		PublishEvent event = new PublishEvent(eventRoot, UNPUBLISH_TOP_EVENT, module[0]);
//		EventLogModel.markChanged(eventRoot);
//
		Object o;
		JBossModuleDelegate delegate;
		
		for( int i = 0; i < module.length; i++ ) {
			// delete this module
			String deployDirectory = server.getDeployDirectory();
			o = module[i].getAdapter(JBossModuleDelegate.class);

			if( o == null ) {
				o = module[i].loadAdapter(JBossModuleDelegate.class, null);
				if( o == null ) 
					continue;
			}
			
			delegate = (JBossModuleDelegate)o;
			String dest = new Path(deployDirectory).append(delegate.getResourceName()).toOSString();
			try {
				File destFile = new File(dest);
				boolean result = destFile.delete();
				addUnPublishEvent(module, eventRoot, dest, result, null);
			} catch( Exception e ) {
				addUnPublishEvent(module, eventRoot, dest, false, e);
			}
		}
		publishState = IServer.PUBLISH_STATE_NONE;
	}

	
	protected void publishModule(IModule[] module, IProgressMonitor monitor) {
//		
//		PublishEvent event = new PublishEvent(eventRoot, PUBLISH_TOP_EVENT, module[0]);
//		EventLogModel.markChanged(eventRoot);
//
		JBossModuleDelegate delegate = null;
		Object o = null;
		String deployDirectory = server.getDeployDirectory();

		// Ignore anything that's not a jbossmodule
		for( int i = 0; i < module.length; i++ ) {
			o = module[i].getAdapter(JBossModuleDelegate.class);
			if( o == null ) {
				o = module[i].loadAdapter(JBossModuleDelegate.class, null);
				if( o == null ) 
					continue;
			}
			
			delegate = (JBossModuleDelegate)o;
			String src = delegate.getResourcePath();
			String dest = new Path(deployDirectory).append(delegate.getResourceName()).toOSString();
			
			Path srcName = new Path(src);
			
			IStatus status = FileUtil.copyFile(src, dest);
			addPublishEvent(module, eventRoot, src, dest, status);
		}
		publishState = IServer.PUBLISH_STATE_NONE;
	}
	
	
	
	
	public static final String PUBLISH_TOP_EVENT = "org.jboss.ide.eclipse.as.core.publishers.PathPublisher.PUBLISH_TOP_EVENT";
	public static final String UNPUBLISH_TOP_EVENT = "org.jboss.ide.eclipse.as.core.publishers.PathPublisher.UNPUBLISH_TOP_EVENT";
	
	public static final String PUBLISH_EVENT = "org.jboss.ide.eclipse.as.core.publishers.PathPublisher.PUBLISH_EVENT";
	public static final String UNPUBLISH_EVENT = "org.jboss.ide.eclipse.as.core.publishers.PathPublisher.UNPUBLISH_EVENT";
	
	public static final String SOURCE_FILE = "org.jboss.ide.eclipse.as.core.publishers.PathPublisher.SOURCE_FILE";
	public static final String DEST_FILE = "org.jboss.ide.eclipse.as.core.publishers.PathPublisher.DEST_FILE";
	public static final String TARGET_FILE = "org.jboss.ide.eclipse.as.core.publishers.PathPublisher.TARGET_FILE";
	public static final String SUCCESS = "org.jboss.ide.eclipse.as.core.publishers.PathPublisher.SUCCESS";
	public static final String EXCEPTION = "org.jboss.ide.eclipse.as.core.publishers.PathPublisher.EXCEPTION";
	
	
	protected void addPublishEvent(IModule[] module, EventLogTreeItem parent, String src, String dest, IStatus result) {
		PathPublisherEvent event = new PathPublisherEvent(parent, PUBLISH_EVENT);
		event.setProperty(IJBossServerPublisher.MODULE_NAME, module[0].getName());
		event.setProperty(SOURCE_FILE, src);
		event.setProperty(DEST_FILE, dest);
		event.setProperty(SUCCESS, new Boolean(result.isOK()).toString());
		if( !result.isOK() && result.getException() != null) {
			event.setProperty(EXCEPTION, result.getException().getLocalizedMessage());
		}
		EventLogModel.markChanged(parent);
	}

	protected void addUnPublishEvent(IModule[] module, EventLogTreeItem parent, String dest, boolean success, Exception e) {
		PathPublisherEvent event = new PathPublisherEvent(parent, UNPUBLISH_EVENT);
		event.setProperty(IJBossServerPublisher.MODULE_NAME, module[0].getName());
		event.setProperty(DEST_FILE, dest);
		event.setProperty(SUCCESS, new Boolean(success).toString());
		if( !success && e != null ) {
			event.setProperty(EXCEPTION, e.getLocalizedMessage());
		}
		EventLogModel.markChanged(parent);
	}
	
	public static class PathPublisherEvent extends EventLogTreeItem {
		public PathPublisherEvent(SimpleTreeItem parent, String specificType) {
			super(parent, PUBLISH_MAJOR_TYPE, specificType);
		}
	}

	
	public int getPublishState() {
		return this.publishState;
	}
}
