/*******************************************************************************
 * Copyright (c) 2015 Red Hat Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    "Rob Stryker" <rob.stryker@redhat.com> - Initial implementation
 *******************************************************************************/
package org.jboss.tools.jmx.ui.internal.actions;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.jboss.tools.common.jdt.debug.RemoteDebugActivator;
import org.jboss.tools.jmx.core.IDebuggableConnection;
import org.jboss.tools.jmx.ui.JMXUIActivator;

public class ConnectDebuggerAction extends Action {
	private IDebuggableConnection con;
	public ConnectDebuggerAction(IDebuggableConnection wrapper) {
		this.con = wrapper;
		setImageDescriptor(JMXUIActivator.getDefault().getSharedImages().descriptor(JMXUIActivator.CONNECT_DEBUGGER_SHARED_IMAGE));
		setEnabled(!isDebuggerConnected(wrapper));
		setText("Connect Debugger"); //$NON-NLS-1$
	}
	
	/**
	 * Is there already a remote java launch connected to this host/port
	 * @return
	 */
	private boolean isDebuggerConnected(IDebuggableConnection wrapper) {
		return RemoteDebugActivator.isRemoteDebuggerConnected(wrapper.getDebugHost(), wrapper.getDebugPort());
	}


	public void run() {
		String mainClass = con.getMainClass();
		if( mainClass != null ) {
			IJavaProject jp = findProjectForMain(mainClass);
			try {
				ArrayList<IJavaProject> javaProjects = new ArrayList<IJavaProject>();
				IProject[] all = ResourcesPlugin.getWorkspace().getRoot().getProjects();
				for( int i = 0; i < all.length; i++ ) {
					if ((jp == null || !all[i].equals(jp.getProject())) && all[i].hasNature(JavaCore.NATURE_ID)) {
						javaProjects.add(JavaCore.create(all[i]));
					}
				}
				
				ILaunchConfiguration config = RemoteDebugActivator.createOrGetDefaultLaunchConfiguration(
						new Integer(con.getDebugPort()).toString(), 
						con.getDebugHost(), jp, (IJavaProject[]) javaProjects.toArray(new IJavaProject[javaProjects.size()]));
				DebugUITools.launch(config, ILaunchManager.DEBUG_MODE);
			} catch(CoreException ce) {
				JMXUIActivator.log(IStatus.ERROR, ce.getMessage(), ce);
			}
		}
	}
	
	private IJavaProject findProjectForMain(String mainClass) {
		IProject[] all = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for( int i = 0; i < all.length; i++ ) {
			IJavaProject jp = JavaCore.create(all[i]);
			try {
				IType t = jp.findType(mainClass);
				if( t != null ) {
					return jp;
				}
			} catch(JavaModelException jme) {
				// Ignore
			}
		}
		return null;
	}
}
