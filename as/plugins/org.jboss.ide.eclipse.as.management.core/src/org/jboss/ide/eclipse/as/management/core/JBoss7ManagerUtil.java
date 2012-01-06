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
	
	
	public static IJBoss7ManagerService getService(IServer server) throws InvalidSyntaxException  {
		BundleContext context = AS7ManagementActivator.getContext();
		JBoss7ManagerServiceProxy proxy = new JBoss7ManagerServiceProxy(context, getRequiredVersion(server));
		proxy.open();
		return proxy;
	}
	
	private static String getRequiredVersion(IServer server) {
		String id = server.getRuntime().getRuntimeType().getId();
		if (JBOSS7_RUNTIME.equals(id)
				|| EAP6_RUNTIME.equals(id)) {
			// This is the proper fix for now. See JBIDE-10293
			return IJBoss7ManagerService.AS_VERSION_710_Beta; 
		}
		if( JBOSS71_RUNTIME.equals(id))
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
