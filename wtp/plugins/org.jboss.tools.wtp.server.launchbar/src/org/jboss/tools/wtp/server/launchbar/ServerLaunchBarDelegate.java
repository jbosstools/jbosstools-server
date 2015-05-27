/******************************************************************************* 
 * Copyright (c) 2015 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.wtp.server.launchbar;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.server.core.IModuleArtifact;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.model.ModuleArtifactDelegate;
import org.eclipse.wst.server.ui.internal.Trace;
import org.eclipse.wst.server.ui.internal.actions.RunOnServerActionDelegate;
import org.eclipse.wst.server.ui.internal.actions.RunOnServerProcess;
import org.jboss.tools.wtp.server.launchbar.launch.DeclaredArtifactRunOnServerActionDelegate;
import org.jboss.tools.wtp.server.launchbar.launch.DeclaredServerRunOnServerActionDelegate;
import org.jboss.tools.wtp.server.launchbar.objects.LaunchedArtifacts;
import org.jboss.tools.wtp.server.launchbar.objects.ModuleArtifactDetailsWrapper;


/**
 * The launch configuration in charge of launching our 
 * RunOnServer actions. 
 */
public class ServerLaunchBarDelegate implements ILaunchConfigurationDelegate {

	public static final String ATTR_LAUNCH_TYPE = "wst.launchbar.launchtype";
	public static final String ATTR_LAUNCH_TYPE_MODULE = "module";
	public static final String ATTR_LAUNCH_TYPE_ARTIFACT = "artifact";
	public static final String ATTR_ARTIFACT_STRING = "artifactString";
	public static final String ATTR_ARTIFACT_CLASS = "artifactClass";
	public static final String ATTR_PROJECT = "my.projectname";
	public static final String ATTR_TARGET_NAME = "target.name";

	
	
	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		IProcess dummyProcess = new RunOnServerProcess(launch);
		launch.addProcess(dummyProcess);
		

		IServer s = findServer(configuration);
		String launchType = configuration.getAttribute(ServerLaunchBarDelegate.ATTR_LAUNCH_TYPE, (String)null);
		if( ServerLaunchBarDelegate.ATTR_LAUNCH_TYPE_MODULE.equals(launchType)) {
			launchModule(s, configuration);
		} else if( ServerLaunchBarDelegate.ATTR_LAUNCH_TYPE_ARTIFACT.equals(launchType)) {
			launchArtifact(s, configuration);
		}
		
		dummyProcess.terminate();
		launch.terminate();
	}
	
	private IServer findServer(ILaunchConfiguration config) throws CoreException {
		String name = config.getAttribute(ATTR_TARGET_NAME, (String)null);
		if( name != null ) {
			for( IServer s : ServerCore.getServers()) {
				if( s.getId().equals(name) || s.getName().equals(name)) {
					return s;
				}
			}
		}
		return null;
	}
	
	private void launchModule(IServer server, ILaunchConfiguration configuration) throws CoreException {
		
		final IServer s2 = server;
		if( s2 != null ) {
			final RunOnServerActionDelegate action = new DeclaredServerRunOnServerActionDelegate(s2);
			final IAction dummyAction = new Action(){
			};
			
			String projectName = configuration.getAttribute(ATTR_PROJECT, (String)null);
			if( projectName != null ) {
				IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				final IStructuredSelection sel = new StructuredSelection(p);
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						action.selectionChanged(dummyAction, sel);
						action.run(dummyAction);
					}
				});
			}
		}		
	}

	
	private void launchArtifact(IServer server, ILaunchConfiguration configuration) throws CoreException {
		final IServer s2 = server;
		if( s2 != null ) {
			String clazz = configuration.getAttribute(ATTR_ARTIFACT_CLASS, (String)null);
			String serial = configuration.getAttribute(ATTR_ARTIFACT_STRING, (String)null);
			IModuleArtifact artifact = getArtifact(clazz, serial);
			markLaunched(configuration.getName(), clazz, serial);
			IModuleArtifact[] art = new IModuleArtifact[]{artifact};
			final DeclaredArtifactRunOnServerActionDelegate action = new DeclaredArtifactRunOnServerActionDelegate(s2, art);
			final IAction dummyAction = new Action(){
			};
			
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					action.selectionChanged(dummyAction, new StructuredSelection());
					action.run(dummyAction);
				}
			});
		}		
	}

	private void markLaunched(String name, String clazz, String serial) {
		ModuleArtifactDetailsWrapper details = new ModuleArtifactDetailsWrapper(name, serial, clazz);
		LaunchedArtifacts.getDefault().markLaunched(details);
	}
	
	public static ModuleArtifactDelegate getArtifact(String clazz, String str) {
		ModuleArtifactDelegate moduleArtifact = null;
		try {
			Class c = Class.forName(clazz);
			moduleArtifact = (ModuleArtifactDelegate) c.newInstance();
			moduleArtifact.deserialize(str);
			return moduleArtifact;
		} catch (Throwable t) {
			if (Trace.WARNING) {
				Trace.trace(Trace.STRING_WARNING, "Could not load module artifact delegate class");
			}
		}
		return null;
	}

}
