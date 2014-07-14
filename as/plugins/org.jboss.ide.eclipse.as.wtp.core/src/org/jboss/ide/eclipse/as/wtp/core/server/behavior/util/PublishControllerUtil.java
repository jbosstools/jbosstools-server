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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllerEnvironment;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPublishControllerDelegate;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.SubsystemModel;

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


}
