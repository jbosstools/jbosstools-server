/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.ServerUtil;
import org.jboss.ide.eclipse.as.core.server.IDelegatingServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;

/**
 * TODO These methods should be put into ServerConverter
 */
public class JBossServerBehaviorUtils {

	/**
	 * Return a DelegatingServerBehavior or null
	 * @param configuration
	 * @return
	 */
	public static IDelegatingServerBehavior getServerBehavior(ILaunchConfiguration configuration) {
		try {
			IServer server = ServerUtil.getServer(configuration);
			return (IDelegatingServerBehavior) server.getAdapter(IDelegatingServerBehavior.class);
		} catch(CoreException ce ) {
			return null;
		}
	}
	

	public static IControllableServerBehavior getControllableBehavior(ILaunchConfiguration configuration) throws CoreException {
		return getControllableBehavior(ServerUtil.getServer(configuration));
	}
	
	public static IControllableServerBehavior getControllableBehavior(IServerAttributes server) {
		IControllableServerBehavior behavior = (IControllableServerBehavior) server.getAdapter(IControllableServerBehavior.class);
		if( behavior == null ) {
			behavior = (IControllableServerBehavior) server.loadAdapter(IControllableServerBehavior.class, new NullProgressMonitor());
		}
		return behavior;
	}

	/**
	 * Get a controller type for a given server and subsystem that conforms to a class, or null if not possible 
	 * @param server
	 * @param id
	 * @param clazz
	 * @return
	 * @throws CoreException
	 */
	public static < T > T getController(IServerAttributes server, String id, Class< T > clazz) throws CoreException {
		IControllableServerBehavior behavior = getControllableBehavior(server);
		if (behavior != null) {
			ISubsystemController controller = behavior.getController(id);
			if (controller != null && clazz.isAssignableFrom(controller.getClass())) {
				return (T)controller;
			}
		}
		return null;
	}
}
