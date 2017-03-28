/******************************************************************************* 
 * Copyright (c) 2014 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.wtp.core.server.behavior.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllerEnvironment;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPublishControllerDelegate;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.SubsystemModel;
import org.jboss.ide.eclipse.as.wtp.core.util.ServerModelUtilities;

public class PublishControllerUtil {
	/**
	 * A constant representing that no publish is required. 
	 * This constant is different from the wtp constants in that
	 * this constant is used after taking into account 
	 * the server flags of kind and deltaKind, as well as the module restart state,
	 * to come to a conclusion of what a publisher needs to do.
	 */
	public static final int NO_PUBLISH = 0;
	/**
	 * A constant representing that an incremental publish is required. 
	 * This constant is different from the wtp constants in that
	 * this constant is used after taking into account 
	 * the server flags of kind and deltaKind, as well as the module restart state,
	 * to come to a conclusion of what a publisher needs to do.
	 */
	public static final int INCREMENTAL_PUBLISH = 1;
	/**
	 * A constant representing that a full publish is required. 
	 * This constant is different from the wtp constants in that
	 * this constant is used after taking into account 
	 * the server flags of kind and deltaKind, as well as the module restart state,
	 * to come to a conclusion of what a publisher needs to do.
	 */
	public static final int FULL_PUBLISH = 2;
	
	/**
	 * A constant representing that a removal-type publish is required. 
	 * This constant is different from the wtp constants in that
	 * this constant is used after taking into account 
	 * the server flags of kind and deltaKind, as well as the module restart state,
	 * to come to a conclusion of what a publisher needs to do.
	 */
	public static final int REMOVE_PUBLISH = 3;

	
	
	/**
	 * Given the various flags, return which of the following options 
	 * our publishers should perform:
	 *    1) A full publish
	 *    2) A removed publish (remove the module)
	 *    3) An incremental publish, or
	 *    4) No publish at all. 
	 * @param module
	 * @param kind
	 * @param deltaKind
	 * @return
	 */
	public static int getPublishType(IServer server, IModule[] module, int kind, int deltaKind) {
		int modulePublishState = server.getModulePublishState(module);
		if( deltaKind == ServerBehaviourDelegate.ADDED ) 
			return FULL_PUBLISH;
		else if (deltaKind == ServerBehaviourDelegate.REMOVED) {
			return REMOVE_PUBLISH;
		} else if (kind == IServer.PUBLISH_FULL 
				|| modulePublishState == IServer.PUBLISH_STATE_FULL 
				|| kind == IServer.PUBLISH_CLEAN ) {
			return FULL_PUBLISH;
		} else if (kind == IServer.PUBLISH_INCREMENTAL 
				|| modulePublishState == IServer.PUBLISH_STATE_INCREMENTAL 
				|| kind == IServer.PUBLISH_AUTO) {
			if( ServerBehaviourDelegate.CHANGED == deltaKind ) 
				return INCREMENTAL_PUBLISH;
		} 
		return NO_PUBLISH;
	}
	
	/**
	 * If this is a bpel or osgi or some other strange publisher, 
	 * allow the delegate to handle the publish. 
	 * When resolving the delegate, it will first checked mapped subsystems.
	 * It will then also check global subsystems as well. 
	 * 
	 * @param server
	 * @param module
	 * @param global
	 * @return
	 */
	
	public static IPublishControllerDelegate findDelegatePublishController(IServer server, IModule[] module, boolean global) {
		// First try to find a new delegate for this module type
		String systemName = IPublishControllerDelegate.SYSTEM_ID;
		String typeId = module[module.length-1].getModuleType().getId();
		ControllerEnvironment env = new ControllerEnvironment();
		env.addRequiredProperty(systemName, "moduleType:" + typeId, "true"); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			ISubsystemController c = 
					SubsystemModel.getInstance().createSubsystemController(server, systemName, env.getMap());
			return (IPublishControllerDelegate)c;
		} catch(CoreException ce) {
			// Ignore, no delegate found for this module type...  probably uses standard publishing, no override
		}
		return null; 
	}

	
	public static boolean structureChanged(IServer server, IModule root) {
		ArrayList<IModule[]> deepModules = ServerModelUtilities.getDeepChildren(server, new IModule[] { root } );
		deepModules.add(new IModule[] {root}); 
		List<Integer> allDelta = computeDelta(server, deepModules);
		Iterator<Integer> it = allDelta.iterator();
		while(it.hasNext()) {
			int i = it.next();
			if( i == ServerBehaviourDelegate.ADDED || i == ServerBehaviourDelegate.REMOVED) {
				return true;
			}
		}
		return false;
	}
	
	

	/*
	 * Following methods are taken from ServerBehaviourDelegate
	 */

	private static List<Integer> computeDelta(IServer server, final List<IModule[]> moduleList) {

		final List<Integer> deltaKindList = new ArrayList<Integer>();
		final Iterator<IModule[]> iterator = moduleList.iterator();
		while (iterator.hasNext()) {
			IModule[] module = iterator.next();
			if (hasBeenPublished(server, module)) {
				IModule m = module[module.length - 1];
				if ((m.getProject() != null && !m.getProject().isAccessible())
						|| getPublishedResourceDelta(server, module).length == 0) {
					deltaKindList.add(new Integer(ServerBehaviourDelegate.NO_CHANGE));
				}
				else {
					deltaKindList.add(new Integer(ServerBehaviourDelegate.CHANGED));
				}
			}
			else {
				deltaKindList.add(new Integer(ServerBehaviourDelegate.ADDED));
			}
		}
		addRemovedModules(server, moduleList, null);
		while (deltaKindList.size() < moduleList.size()) {
			deltaKindList.add(new Integer(ServerBehaviourDelegate.REMOVED));
		}
		return deltaKindList;
	}
	
	protected static boolean hasBeenPublished(IServer server, IModule[] module) {
		return ((Server)server).getServerPublishInfo().hasModulePublishInfo(module);
	}
	
	protected static IModuleResourceDelta[] getPublishedResourceDelta(IServer server, IModule[] module) {
		return ((Server)server).getPublishedResourceDelta(module);
	}
	
	protected static void addRemovedModules(IServer server, List<IModule[]> moduleList, List<Integer> kindList) {
		((Server)server).getServerPublishInfo().addRemovedModules(moduleList);
	}
	

}
