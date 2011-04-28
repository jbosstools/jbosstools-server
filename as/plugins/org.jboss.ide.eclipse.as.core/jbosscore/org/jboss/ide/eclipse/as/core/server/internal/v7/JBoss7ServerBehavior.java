/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal.v7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IModule;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.publishers.LocalPublishMethod;
import org.jboss.ide.eclipse.as.core.server.internal.DeployableServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServerBehavior;
import org.jboss.ide.eclipse.as.core.server.internal.LocalJBossBehaviorDelegate;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public class JBoss7ServerBehavior extends JBossServerBehavior {
	private static HashMap<String, Class> delegateClassMap;
	static {
		delegateClassMap = new HashMap<String, Class>();
		delegateClassMap.put(LocalPublishMethod.LOCAL_PUBLISH_METHOD, LocalJBoss7BehaviorDelegate.class);
	}
	public static void addDelegateMapping(String s, Class c) {
		delegateClassMap.put(s, c);
	}
	protected HashMap<String, Class> getDelegateMap() {
		return delegateClassMap;
	}

	
	public void stop(boolean force) {
		setServerStopped();
	}
	
	public boolean shouldSuspendScanner() {
		return false;
	}
	
	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {
		//super.setupLaunchConfiguration(workingCopy, monitor);
	}

	@Override
	protected void publishModule(int kind, int deltaKind, IModule[] module, IProgressMonitor monitor) throws CoreException {
		if( method == null )
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Not publishing")); //$NON-NLS-1$
		int result = method.publishModule(this, kind, deltaKind, module, monitor);
		setModulePublishState(module, result);
	}
	
	@Override
	protected void publishFinish(IProgressMonitor monitor) throws CoreException {
		if( method == null )
			throw new CoreException(new Status(IStatus.ERROR, JBossServerCorePlugin.PLUGIN_ID, "Not publishing")); //$NON-NLS-1$
		// Handle the dodeploy 
		DeployableServerBehavior beh = ServerConverter.getDeployableServerBehavior(getServer());
		Object o = beh.getPublishData(JBoss7JSTPublisher.MARK_DO_DEPLOY);
		if( o != null && (o instanceof ArrayList<?>)) {
			ArrayList<IPath> l = (ArrayList<IPath>)o;
			int size = l.size();
			monitor.beginTask("Completing Publishes", size+1); //$NON-NLS-1$
			Iterator<IPath> i = l.iterator();
			IPath p;
			while(i.hasNext()) {
				JBoss7JSTPublisher.addDoDeployMarkerFile(method, getServer(), i.next(), new SubProgressMonitor(monitor, 1));
			}
			super.publishFinish(new SubProgressMonitor(monitor, 1));
		} else 
			super.publishFinish(monitor);
	}
}
