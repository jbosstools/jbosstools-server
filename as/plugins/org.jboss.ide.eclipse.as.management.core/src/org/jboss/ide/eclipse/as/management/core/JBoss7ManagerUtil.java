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
package org.jboss.ide.eclipse.as.management.core;

import org.eclipse.wst.server.core.IServer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;

public class JBoss7ManagerUtil {

	private static final String JBOSS7_RUNTIME = "org.jboss.ide.eclipse.as.runtime.70"; //$NON-NLS-1$
	private static final String JBOSS71_RUNTIME = "org.jboss.ide.eclipse.as.runtime.71"; //$NON-NLS-1$
	private static final String EAP6_RUNTIME = "org.jboss.ide.eclipse.as.runtime.eap.60"; //$NON-NLS-1$
	private static final String EAP61_RUNTIME = "org.jboss.ide.eclipse.as.runtime.eap.61"; //$NON-NLS-1$
	
	
	public static IJBoss7ManagerService getService(IServer server) throws JBoss7ManangerException  {
		return getService(server.getRuntime().getRuntimeType().getId());
	}

	public static IJBoss7ManagerService getService(String runtimeType) throws JBoss7ManangerException  {
		try {
			BundleContext context = AS7ManagementActivator.getContext();
			JBoss7ManagerServiceProxy proxy = new JBoss7ManagerServiceProxy(context, getRequiredServiceVersion(runtimeType));
			proxy.open();
			return proxy;
		} catch(InvalidSyntaxException ise) {
			throw new JBoss7ManangerException(ise);
		}
	}

	public static String getRequiredVersion(IServer server) {
		String id = server.getRuntime().getRuntimeType().getId();
		return getRequiredServiceVersion(id);
	}
	
	// This may need to be updated for as7-style servers NEW_SERVER_ADAPTER
	// TODO  move this to extended properties
	public static String getRequiredServiceVersion(String runtimeId) {
		if (JBOSS7_RUNTIME.equals(runtimeId)
				|| EAP6_RUNTIME.equals(runtimeId)) {
			return IJBoss7ManagerService.AS_VERSION_710_Beta; 
		}
		if( JBOSS71_RUNTIME.equals(runtimeId))
			return IJBoss7ManagerService.AS_VERSION_710_Beta;
		// This service fails for some tests, but it must be enabled here 
		// for querying server state. TODO change this if new service bundle is required
		if( EAP61_RUNTIME.equals(runtimeId)) 
			return IJBoss7ManagerService.AS_VERSION_710_Beta;
		return null;
	}

	public static void dispose(IJBoss7ManagerService service) {
		if (service != null) {
			service.dispose();
		}
	}
	
	public static <RESULT> RESULT executeWithService(IServiceAware<RESULT> serviceAware, IServer server) throws Exception {
		IJBoss7ManagerService service = null;
		try {
			service = JBoss7ManagerUtil.getService(server);
			return serviceAware.execute(service);
		} finally {
			if (service != null) {
				service.dispose();
			}
		}
	}
	
	public static interface IServiceAware<RESULT> {
		public RESULT execute(IJBoss7ManagerService service) throws Exception;
	}
	
}
