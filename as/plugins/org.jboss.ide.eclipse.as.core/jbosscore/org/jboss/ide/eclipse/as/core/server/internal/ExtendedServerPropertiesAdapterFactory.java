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
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossAS710ExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossAS7ExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class ExtendedServerPropertiesAdapterFactory implements IAdapterFactory, IJBossToolingConstants {
	
	public static JBossExtendedProperties getExtendedProperties(IServer server) {
		return (JBossExtendedProperties) new ExtendedServerPropertiesAdapterFactory().getAdapter(server, JBossExtendedProperties.class);
	}
	
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if( adapterType != JBossExtendedProperties.class )
			return null;
		String typeId = null;
		IServer s = null;
		IRuntime r = null;
		if( adaptableObject instanceof IServer ) {
			typeId = ((IServer)adaptableObject).getServerType().getId();
			s = (IServer)adaptableObject;
		} else if( adaptableObject instanceof IRuntime ) {
			typeId = ((IRuntime)adaptableObject).getRuntimeType().getId();
			r = (IRuntime)adaptableObject;
		}
		if( typeId != null ) {
			if( SERVER_AS_32.equals(typeId) || AS_32.equals(typeId)) 
				return new JBossExtendedProperties(s == null ? r : s);
			if( SERVER_AS_40.equals(typeId) || AS_40.equals(typeId)) 
				return new JBossExtendedProperties(s == null ? r : s);
			if( SERVER_AS_42.equals(typeId) || AS_42.equals(typeId))
				return new JBossExtendedProperties(s == null ? r : s);
			if( SERVER_AS_50.equals(typeId) || AS_50.equals(typeId))
				return new JBossExtendedProperties(s == null ? r : s);
			if( SERVER_AS_51.equals(typeId) || AS_51.equals(typeId))
				return new JBossExtendedProperties(s == null ? r : s);
			if( SERVER_AS_60.equals(typeId) || AS_60.equals(typeId))
				return new JBossExtendedProperties(s == null ? r : s);
			if( SERVER_EAP_43.equals(typeId) || EAP_43.equals(typeId))
				return new JBossExtendedProperties(s == null ? r : s);
			if( SERVER_EAP_50.equals(typeId) || EAP_50.equals(typeId))
				return new JBossExtendedProperties(s == null ? r : s);
			
			if( SERVER_AS_70.equals(typeId) || AS_70.equals(typeId))
				return new JBossAS7ExtendedProperties(s == null ? r : s);
			if( SERVER_AS_71.equals(typeId) || AS_71.equals(typeId))
				return new JBossAS710ExtendedProperties(s == null ? r : s);
			if( SERVER_EAP_60.equals(typeId) || EAP_60.equals(typeId))
				return new JBossAS710ExtendedProperties(s == null ? r : s);
			
			// NEW_SERVER_ADAPTER
		}
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return new Class[]{ JBossExtendedProperties.class};
	}

}
