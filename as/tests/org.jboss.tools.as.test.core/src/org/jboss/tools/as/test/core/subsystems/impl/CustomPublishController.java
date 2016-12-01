/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.subsystems.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IFilesystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.publish.LocalZippedModulePublishRunner;
import org.jboss.tools.as.core.server.controllable.subsystems.internal.LocalDeploymentOptionsController;
import org.jboss.tools.as.core.server.controllable.subsystems.internal.ModuleDeployPathController;
import org.jboss.tools.as.core.server.controllable.subsystems.internal.StandardFileSystemPublishController;
import org.jboss.tools.as.core.server.controllable.subsystems.internal.StandardModuleRestartBehaviorController;
import org.jboss.tools.as.core.server.controllable.systems.IDeploymentOptionsController;
import org.jboss.tools.as.core.server.controllable.systems.IModuleDeployPathController;
import org.jboss.tools.as.core.server.controllable.systems.IModuleRestartBehaviorController;
import org.jboss.tools.as.test.core.internal.MockPublishMethodFilesystemController;

/**
 * This is a custom publish controller that subclasses the official one.
 * It exposes more details so the test cases that use it can access
 * exactly what was done for the purposes of verification 
 */
public class CustomPublishController extends StandardFileSystemPublishController {
	public IModuleRestartBehaviorController getModuleRestartBehaviorController() throws CoreException {
		StandardModuleRestartBehaviorController c = new StandardModuleRestartBehaviorController();
		c.initialize(getServer(), null, getEnvironment());
		return c;
	}
	public IDeploymentOptionsController getDeploymentOptions() throws CoreException {
		LocalDeploymentOptionsController c = new LocalDeploymentOptionsController();
		c.initialize(getServer(), null, getEnvironment());
		return c;
	}
	
	public IModuleDeployPathController getDeployPathController() throws CoreException {
		ModuleDeployPathController c = new ModuleDeployPathController();
		Map<String, Object> env2 = new HashMap<String, Object>(getEnvironment());
		env2.put(IModuleDeployPathController.ENV_DEPLOYMENT_OPTIONS_CONTROLLER, getDeploymentOptions());
		c.initialize(getServer(), null, env2);
		return c;
	}
	public IFilesystemController getFilesystemController() throws CoreException {
		IFilesystemController c = new MockPublishMethodFilesystemController();
		c.initialize(getServer(), null, getEnvironment());
		return c;
	}
	
	public HashMap<IModule[], IModuleResourceDelta[]> deltaMap = new HashMap<IModule[], IModuleResourceDelta[]>();
	public ArrayList<IModule[]> removedList = new ArrayList<IModule[]>();
	public HashMap<IModule[], Boolean> beenPublished = new HashMap<IModule[], Boolean>();
	
	public void setDeltaMap(IModule[] m, IModuleResourceDelta[] d) {
		deltaMap.put(m, d);
	}
	public void setRemovedList(ArrayList<IModule[]> list) {
		this.removedList = list;
	}
	public void setBeenPublished(IModule[] m, Boolean b) {
		beenPublished.put(m,b);
	}
	
	// Exposed only for unit tests to override
	// The methods here typically ask a server for its actual publish information, 
	//and since I'm not testing server.publish() but rather on a controller instance directly,
	//the server has no knowledge of the module, and I must mock this a bit. 
	@Override
	public LocalZippedModulePublishRunner createZippedRunner(IModule m, IPath p) {
		return createZippedRunner(new IModule[]{m}, p);
	}
	
	@Override
	protected LocalZippedModulePublishRunner createZippedRunner(IModule[] m, IPath p) {
		return new LocalZippedModulePublishRunner(getServer(), m,p, 
				getModulePathFilterProvider()) {
			@Override
			public IModuleResourceDelta[] getDeltaForModule(IModule[] module) {
				return deltaMap.get(module);
			}
			
			@Override
			public List<IModule[]> getRemovedModules() {
				return removedList;
			}
			
			@Override
			public boolean hasBeenPublished(IModule[] mod) {
				Boolean b = beenPublished.get(mod);
				if( b == null )
					return false;
				return b; 
			}
			@Override
			public IModule[] getChildModules(IModule[] parent) {
				ServerDelegate sd = ((ServerDelegate)getServer().loadAdapter(ServerDelegate.class, null));
				return sd.getChildModules(parent);
			}
		};
	}
	
	@Override
	public IModuleResourceDelta[] getDeltaForModule(IModule[] module) {
		return deltaMap.get(module);
	}
}