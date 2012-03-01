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
package org.jboss.ide.eclipse.as.core.server.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossAS710ExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossAS7ExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class ExtendedServerPropertiesAdapterFactory implements IAdapterFactory {
	
	public static JBossExtendedProperties getExtendedProperties(IServer server) {
		return (JBossExtendedProperties) new ExtendedServerPropertiesAdapterFactory().getAdapter(server, JBossExtendedProperties.class);
	}
	
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if( adaptableObject instanceof IServer && adapterType == JBossExtendedProperties.class) {
			String typeId = ((IServer)adaptableObject).getServerType().getId();
			if( IJBossToolingConstants.SERVER_AS_32.equals(typeId))
				return new JBossExtendedProperties((IServer)adaptableObject);
			if( IJBossToolingConstants.SERVER_AS_40.equals(typeId)) 
				return new JBossExtendedProperties((IServer)adaptableObject);
			if( IJBossToolingConstants.SERVER_AS_42.equals(typeId))
				return new JBossExtendedProperties((IServer)adaptableObject);
			if( IJBossToolingConstants.SERVER_AS_50.equals(typeId))
				return new JBossExtendedProperties((IServer)adaptableObject);
			if( IJBossToolingConstants.SERVER_AS_51.equals(typeId))
				return new JBossExtendedProperties((IServer)adaptableObject);
			if( IJBossToolingConstants.SERVER_AS_60.equals(typeId))
				return new JBossExtendedProperties((IServer)adaptableObject);
			if( IJBossToolingConstants.SERVER_EAP_43.equals(typeId))
				return new JBossExtendedProperties((IServer)adaptableObject);
			if( IJBossToolingConstants.SERVER_EAP_50.equals(typeId))
				return new JBossExtendedProperties((IServer)adaptableObject);
			
			if( IJBossToolingConstants.SERVER_AS_70.equals(typeId))
				return new JBossAS7ExtendedProperties((IServer)adaptableObject);
			if( IJBossToolingConstants.SERVER_AS_71.equals(typeId))
				return new JBossAS710ExtendedProperties((IServer)adaptableObject);
			if( IJBossToolingConstants.SERVER_EAP_60.equals(typeId))
				return new JBossAS710ExtendedProperties((IServer)adaptableObject);
			
			// NEW_SERVER_ADAPTER
		}
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return new Class[]{ JBossExtendedProperties.class};
	}

}
