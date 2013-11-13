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
package org.jboss.ide.eclipse.as.core.server.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.server.IDelegatingServerBehavior;
import org.jboss.ide.eclipse.as.core.server.IJBossBehaviourDelegate;
import org.jboss.ide.eclipse.as.core.server.IServerModeDetails;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IServerDetailsController;

/**
 * @since 3.0
 */
public class ServerModeDetailsAdapterFactory implements IAdapterFactory, IJBossToolingConstants {
	
	public ServerModeDetailsAdapterFactory() {
	}
	
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		IServerModeDetails props = getModeDetails(adaptableObject);
		if( props != null && adapterType.isAssignableFrom(props.getClass()))
			return props;
		return null;
	}
	
	public IServerModeDetails getModeDetails(Object adaptableObject) {
		if( adaptableObject instanceof IServer ) {
			IServer ao = (IServer)adaptableObject;
			
			// Following lines should be removed / deprecated
			ServerBehaviourDelegate del = (ServerBehaviourDelegate)ao.loadAdapter(ServerBehaviourDelegate.class, null);
			if( del instanceof IDelegatingServerBehavior) {
				IJBossBehaviourDelegate del2 = ((IDelegatingServerBehavior)del).getDelegate();
				if( del2 instanceof AbstractJBossBehaviourDelegate) {
					return ((AbstractJBossBehaviourDelegate)del2).getServerModeDetails();
				}
			}
			
			// New impl here
			if( del instanceof ControllableServerBehavior) {
				try {
					ControllableServerBehavior del2 = (ControllableServerBehavior)del;
					return (IServerModeDetails)del2.getController(IServerDetailsController.SYSTEM_ID);
				} catch(CoreException ce) {
					// TODO log this error
				}
			}
		}
		return null;
	}
	
	public Class[] getAdapterList() {
		return new Class[]{ IServerModeDetails.class};
	}

}
