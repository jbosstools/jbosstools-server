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
package org.jboss.ide.eclipse.as.core.server.publishers;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.server.tomcat.core.internal.FileUtil;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.model.ModuleModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog;
import org.jboss.ide.eclipse.as.core.model.ServerProcessLog.ProcessLogEvent;
import org.jboss.ide.eclipse.as.core.module.factory.JBossModuleDelegate;
import org.jboss.ide.eclipse.as.core.server.JBossServer;
import org.jboss.ide.eclipse.as.core.server.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.util.ASDebug;

public class PackagedPublisher implements IJbossServerPublisher  {
	
	private JBossServer server;
	private ProcessLogEvent log;
	private JBossServerBehavior behavior;
	private int publishState;
	
	public PackagedPublisher(JBossServer server, JBossServerBehavior behavior) {
		this.server = server;
		this.behavior = behavior;
		this.log = new ServerProcessLog.ProcessLogEvent("Parent", -2);
		publishState = IServer.PUBLISH_STATE_UNKNOWN;
	}

	public void publishModule(int kind, int deltaKind, IModule[] module, IProgressMonitor monitor) throws CoreException {
		ASDebug.p("(kind, deltakind)=(" + kind + "," + deltaKind + ") : " + module, this);
		// delta = [no_change, added, changed, removed] = [0,1,2,3]
		// kind = [incremental, full, auto, clean] = [1,2,3,4]
		
		if( deltaKind == ServerBehaviourDelegate.NO_CHANGE ) {
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

		//ASDebug.p("publish done", this);
	}

	
	/**
	 * The module has been unambiguously removed from the server model.
	 * Proceed to remove it from the actual server directory.
	 */
	protected void unPublishModule(IModule[] module, IProgressMonitor monitor) {
		ASDebug.p("UnPublishing " + module.length + " modules", this);
		Object o;
		JBossModuleDelegate delegate;
		
		for( int i = 0; i < module.length; i++ ) {
			// delete this module
			String deployDirectory = server.getRuntimeConfiguration().getDeployDirectory();
			o = module[i].getAdapter(JBossModuleDelegate.class);
			if( o == null ) {
				o = module[i].loadAdapter(JBossModuleDelegate.class, null);
				if( o == null ) 
					continue;
			}
			
			delegate = (JBossModuleDelegate)o;
			String dest = new Path(deployDirectory).append(delegate.getResourceName()).toOSString();
			ASDebug.p("Deleting fine from server: " + dest, this);
			try {
				Path destName = new Path(dest);
				String config = server.getRuntimeConfiguration().getJbossConfiguration();
				
				log.addChild("Removing " + destName.lastSegment() + " from the " + 
						config + "/deploy directory", ProcessLogEvent.SERVER_UNPUBLISH );
				

				File destFile = new File(dest);
				destFile.delete();
			
				// tell the model you're aware of the change
				ModuleModel.getDefault().getDeltaModel().setDeltaSeen(module[i], server.getServer().getId());
			} catch( Exception e ) {
				ASDebug.p("ERROR in unpublish", this);
				e.printStackTrace();
			}
		}
		publishState = IServer.PUBLISH_STATE_NONE;
	}

	
	protected void publishModule(IModule[] module, IProgressMonitor monitor) {
		
		JBossModuleDelegate delegate = null;
		Object o = null;
		String deployDirectory = server.getRuntimeConfiguration().getDeployDirectory();
		
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
			String config = server.getRuntimeConfiguration().getJbossConfiguration();
			
			log.addChild("Copying " + srcName.lastSegment() + " to the " + 
					config + "/deploy directory", ProcessLogEvent.SERVER_PUBLISH );
			
			
			FileUtil.copyFile(src, dest);
			
			// tell everyone we need no more changes right now
			ModuleModel.getDefault().getDeltaModel().setDeltaSeen(module[i], server.getServer().getId());

			ASDebug.p("publishing module: " + module[i].getId(), this);
		}
		publishState = IServer.PUBLISH_STATE_NONE;
	}
	
	
	public ProcessLogEvent[] getLogEvents() {
		return log.getChildren();
	}
	
	public int getPublishState() {
		return this.publishState;
	}
}
