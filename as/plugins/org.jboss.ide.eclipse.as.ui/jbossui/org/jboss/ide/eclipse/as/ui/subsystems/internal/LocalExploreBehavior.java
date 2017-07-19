/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.subsystems.internal;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.core.util.RemotePath;
import org.jboss.ide.eclipse.as.ui.actions.ExploreUtils;
import org.jboss.ide.eclipse.as.ui.subsystems.IExploreBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.AbstractSubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.tools.as.core.server.controllable.systems.IDeploymentOptionsController;
import org.jboss.tools.as.core.server.controllable.systems.IModuleDeployPathController;

public class LocalExploreBehavior extends AbstractSubsystemController implements IExploreBehavior {
	public boolean canExplore(IServer server, IModule[] module) {
		if( module != null )
			return canExploreModule(server, module);
		return canExploreServer(server);
	}
	
	public void openExplorer(IServer server, IModule[] module) {
		if( module != null ) 
			runExploreModuleServer(server, module);
		else
			runExploreServer(server);
	}
	
	private void runExploreServer(IServer server) {
		String deployDirectory = getDeployDirectory(server);
		if (deployDirectory != null && deployDirectory.length() > 0) {
			ExploreUtils.explore(deployDirectory);
		} 
	}
	
	private void runExploreModuleServer(IServer server, IModule[] module) {
		IPath path = getPath(server, module);
		if (path != null) {
			File file = path.toFile();
			if (file.exists()) {
				ExploreUtils.explore(file.getAbsolutePath());
			}
		}
	}

	private String getDeployDirectory(IServer server) {
		IPath serverPath = getPath(server, null);
		if( serverPath != null )
			return serverPath.toOSString();

		// In case openshift or others support this key (?)
		String ret = server.getAttribute(IDeployableServer.DEPLOY_DIRECTORY,(String) null);
		if( ret != null )
			return ret.trim();
		
		// Other runtimes like tomcat / default behavior (?)
		IRuntime rt = server.getRuntime();
		if( rt != null ) {
			return rt.getLocation().toString();
		}
		
		return null; // No idea
	}
	
	private boolean canExploreServer(IServer server) {
		String deployDirectory = getDeployDirectory(server);
		if (deployDirectory == null || deployDirectory.length() <= 0 && new File(deployDirectory).exists()) {
			return false;
		}
		if (ExploreUtils.getExploreCommand() == null) {
			return false;
		}
		return true;
	}
	
	public boolean canExploreModule(IServer server, IModule[] modules) {
		IPath p = getPath(server, modules);
		if (p == null || !p.toFile().exists() || ExploreUtils.getExploreCommand() == null)
			return false;
		return true;
	}
	
	// Get the path to explore
	private IPath getPath(IServer server, IModule[] module) {
		try {
			IPath p = null;
			IControllableServerBehavior cs = JBossServerBehaviorUtils.getControllableBehavior(server);
			if( module == null ) {
				IDeploymentOptionsController controller = (IDeploymentOptionsController)cs.getController(IDeploymentOptionsController.SYSTEM_ID);
				String depRoot = controller.getDeploymentsRootFolder(true);
				if( depRoot != null )
					p = new RemotePath(depRoot, controller.getPathSeparatorCharacter());
			} else {
				IModuleDeployPathController controller = (IModuleDeployPathController)cs.getController(IModuleDeployPathController.SYSTEM_ID);
				p = controller.getDeployDirectory(module);
			}
			return p;
		} catch(CoreException ce) {
		}
		return null;
	}
	
	
}
