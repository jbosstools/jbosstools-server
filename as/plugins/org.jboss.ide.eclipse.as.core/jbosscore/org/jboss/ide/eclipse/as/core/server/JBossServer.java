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
package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.model.DescriptorModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel;
import org.jboss.ide.eclipse.as.core.model.DescriptorModel.ServerDescriptorModel;
import org.jboss.ide.eclipse.as.core.model.ServerProcessModel.ServerProcessModelEntity;
import org.jboss.ide.eclipse.as.core.server.runtime.JBossServerRuntime;
import org.jboss.ide.eclipse.as.core.util.ASDebug;

public class JBossServer extends ServerDelegate {

	
	private JBossServerRuntime runtime;
	
	
	public JBossServer() {
	}

	
	protected void initialize() {
		
	}
	
	public void debug( String s ) {
		ASDebug.p(s, this);
	}
	
	/*
	 * OVERRIDES
	 */

	public void setDefaults(IProgressMonitor monitor) {
		debug("setDefaults");
	}
	
	public void importRuntimeConfiguration(IRuntime runtime, IProgressMonitor monitor) throws CoreException {
		debug("import Runtime Configuration");
		//getJBossRuntime();
	}

	public void saveConfiguration(IProgressMonitor monitor) throws CoreException {
		debug("saveConfiguration");
		// Saving a change in server properties (via server editor)
		String newHost = getServer().getHost();
		
		ILaunchConfiguration launchConfig = 
			((Server)getServer()).getLaunchConfiguration(true, new NullProgressMonitor());

		JBossLaunchConfigurationDelegate.setHost(launchConfig, newHost, getDescriptorModel().getJNDIPort());
	}

	public void configurationChanged() {
		debug("configurationChanged");
	}


	/*
	 * Other
	 */
	public void setRuntime(JBossServerRuntime run) {
		runtime = run;
	}
	
	public JBossServerRuntime getJBossRuntime() {
		if( runtime == null ) {
			runtime = (JBossServerRuntime) getServer().getRuntime().loadAdapter(JBossServerRuntime.class, null);
		}
		return runtime;
		
	}

	
	public ServerAttributeHelper getAttributeHelper() {
//		IServer server = ServerCore.findServer(getServer().getId());
//		IServerWorkingCopy copy = server.createWorkingCopy();
		IServerWorkingCopy copy = getServerWorkingCopy();
		if( copy == null ) {
			copy = getServer().createWorkingCopy();
		}
		return new ServerAttributeHelper(this, copy);
	}
	
	
	/*
	 * Abstracts to implement
	 */
	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
		debug("canModifyModules");
		return new Status(IStatus.OK, JBossServerCorePlugin.PLUGIN_ID,0, "OK", null);
	}

	public IModule[] getChildModules(IModule[] module) {
		//debug("*****  getChildModules");
		return null;
	}

	// As of now none of my modules are implementing the parent / child nonesense
	public IModule[] getRootModules(IModule module) throws CoreException {
		//debug("***** getRootModules");
		return new IModule[] { module };
	}

	
	public void modifyModules(IModule[] add, IModule[] remove,
			IProgressMonitor monitor) throws CoreException {
		
		// Do nothing for now, just display to know I've been called. 
	}
	
	public ServerPort[] getServerPorts() {
		debug("****** getServerPorts");
		//return new ServerPort[] { new ServerPort("portid1", "portname1", 1099, "TCPIP") };
		return null;
	}

	public ServerProcessModelEntity getProcessModel() {
		return ServerProcessModel.getDefault().getModel(getServer().getId());
	}
	
	public ServerDescriptorModel getDescriptorModel() {
		return DescriptorModel.getDefault().getServerModel(getServer());
	}

}