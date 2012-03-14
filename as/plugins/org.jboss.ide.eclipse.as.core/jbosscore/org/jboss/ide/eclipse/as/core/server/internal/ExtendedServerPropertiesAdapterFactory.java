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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossAS710ExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossAS7ExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.ServerExtendedProperties;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class ExtendedServerPropertiesAdapterFactory implements IAdapterFactory, IJBossToolingConstants {
	
	public static JBossExtendedProperties getJBossExtendedProperties(IServerAttributes server) {
		Object ret = new ExtendedServerPropertiesAdapterFactory().getAdapter(server, ServerExtendedProperties.class);
		return ret instanceof JBossExtendedProperties ? (JBossExtendedProperties)ret : null;
	}

	public static ServerExtendedProperties getServerExtendedProperties(IServerAttributes server) {
		Object ret = new ExtendedServerPropertiesAdapterFactory().getAdapter(server, ServerExtendedProperties.class);
		return (ServerExtendedProperties)ret;
	}

	public Object getAdapter(Object adaptableObject, Class adapterType) {
		ServerExtendedProperties props = getExtendedProperties(adaptableObject);
		if( adapterType.isAssignableFrom(props.getClass()))
			return props;
		return null;
	}
	
	public ServerExtendedProperties getExtendedProperties(Object adaptableObject) {
		String typeId = null;
		IServer s = null;
		IRuntime r = null;
		if( adaptableObject instanceof IServerAttributes ) {
			typeId = ((IServerAttributes)adaptableObject).getServerType().getId();
			s = (IServer)adaptableObject;
		} else if( adaptableObject instanceof IRuntime ) {
			typeId = ((IRuntime)adaptableObject).getRuntimeType().getId();
			r = (IRuntime)adaptableObject;
		}
		IAdaptable adaptable = s == null ? r : s;
		if( typeId != null ) {
			if( SERVER_AS_32.equals(typeId) || AS_32.equals(typeId)) 
				return new JBossExtendedProperties(adaptable);
			if( SERVER_AS_40.equals(typeId) || AS_40.equals(typeId)) 
				return new JBossExtendedProperties(adaptable);
			if( SERVER_AS_42.equals(typeId) || AS_42.equals(typeId))
				return new JBossExtendedProperties(adaptable);
			if( SERVER_AS_50.equals(typeId) || AS_50.equals(typeId))
				return new JBossExtendedProperties(adaptable);
			if( SERVER_AS_51.equals(typeId) || AS_51.equals(typeId))
				return new JBossExtendedProperties(adaptable);
			if( SERVER_AS_60.equals(typeId) || AS_60.equals(typeId))
				return new JBossExtendedProperties(adaptable);
			if( SERVER_EAP_43.equals(typeId) || EAP_43.equals(typeId))
				return new JBossExtendedProperties(adaptable);
			if( SERVER_EAP_50.equals(typeId) || EAP_50.equals(typeId))
				return new JBossExtendedProperties(adaptable);
			
			if( SERVER_AS_70.equals(typeId) || AS_70.equals(typeId))
				return new JBossAS7ExtendedProperties(adaptable);
			if( SERVER_AS_71.equals(typeId) || AS_71.equals(typeId))
				return new JBossAS710ExtendedProperties(adaptable);
			if( SERVER_EAP_60.equals(typeId) || EAP_60.equals(typeId))
				return new JBossAS710ExtendedProperties(adaptable);
			
			// NEW_SERVER_ADAPTER
		}
		
		// Last ditch
		if( s != null ) {
			IExtendedPropertiesProvider propProvider = (IExtendedPropertiesProvider)
					s.loadAdapter(IExtendedPropertiesProvider.class, new NullProgressMonitor());
			if( propProvider != null ) {
				return propProvider.getExtendedProperties();
			}
		}
		return null;
	}
	
	public Class[] getAdapterList() {
		return new Class[]{ ServerExtendedProperties.class, 
				JBossExtendedProperties.class,
				JBossAS7ExtendedProperties.class,
				JBossAS710ExtendedProperties.class};
	}

}
