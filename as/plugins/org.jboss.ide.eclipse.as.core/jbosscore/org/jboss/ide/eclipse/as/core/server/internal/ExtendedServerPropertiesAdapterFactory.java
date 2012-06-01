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
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerAttributes;
import org.eclipse.wst.server.core.IServerType;
import org.jboss.ide.eclipse.as.core.Trace;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossAS6ExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossAS710ExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossAS7ExtendedProperties;
import org.jboss.ide.eclipse.as.core.server.internal.extendedproperties.JBossEAP60ExtendedProperties;
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
		if( props != null && adapterType.isAssignableFrom(props.getClass()))
			return props;
		return null;
	}
	
	public ServerExtendedProperties getExtendedProperties(Object adaptableObject) {
		String typeId = null;
		IServer s = null;
		IRuntime r = null;
		if( adaptableObject instanceof IServerAttributes ) {
			IServerType type =  ((IServerAttributes)adaptableObject).getServerType();
			typeId = type == null ? null : type.getId();
			s = (IServer)adaptableObject;
		} else if( adaptableObject instanceof IRuntime ) {
			IRuntimeType type = ((IRuntime)adaptableObject).getRuntimeType();
			typeId = type == null ? null : type.getId();
			r = (IRuntime)adaptableObject;
		} else if( adaptableObject instanceof IRuntimeType ) {
			typeId = ((IRuntimeType)adaptableObject).getId();
		} else if( adaptableObject instanceof IServerType ) {
			typeId = ((IServerType)adaptableObject).getId();
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
				return new JBossAS6ExtendedProperties(adaptable);
			if( SERVER_EAP_43.equals(typeId) || EAP_43.equals(typeId))
				return new JBossExtendedProperties(adaptable);
			if( SERVER_EAP_50.equals(typeId) || EAP_50.equals(typeId))
				return new JBossAS6ExtendedProperties(adaptable);
			
			if( SERVER_AS_70.equals(typeId) || AS_70.equals(typeId))
				return new JBossAS7ExtendedProperties(adaptable);
			if( SERVER_AS_71.equals(typeId) || AS_71.equals(typeId))
				return new JBossAS710ExtendedProperties(adaptable);
			if( SERVER_EAP_60.equals(typeId) || EAP_60.equals(typeId))
				return new JBossEAP60ExtendedProperties(adaptable);

			// NEW_SERVER_ADAPTER
			
			// Last ditch, allows other server types to adapt also
			if( s != null ) {
				IExtendedPropertiesProvider propProvider = (IExtendedPropertiesProvider)
						s.loadAdapter(IExtendedPropertiesProvider.class, new NullProgressMonitor());
				if( propProvider != null ) {
					return propProvider.getExtendedProperties();
				}
			}

		} else {
			// typeId is null... why?
			Trace.trace(Trace.STRING_FINER, NLS.bind("ExtendedServerPropertiesAdapterFactory unable to adapt object {0} to ServerExtendedProperties", adaptable)); //$NON-NLS-1$
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
