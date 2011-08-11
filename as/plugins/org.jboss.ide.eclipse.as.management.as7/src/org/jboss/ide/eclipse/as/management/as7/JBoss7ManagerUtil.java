/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.management.as7;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.internal.management.as7.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;

public class JBoss7ManagerUtil {

	private static final String JBOSS7_RUNTIME = "org.jboss.ide.eclipse.as.runtime.70"; //$NON-NLS-1$

	public static IJBoss7ManagerService getService(IServer server) throws InvalidSyntaxException  {
		BundleContext context = Activator.getContext();
		JBoss7ManagerServiceProxy proxy = new JBoss7ManagerServiceProxy(context, getRequiredVersion(server));
		proxy.open();
		return proxy;
	}

	private static String getRequiredVersion(IServer server) {
		String id = server.getRuntime().getRuntimeType().getId();
		if (JBOSS7_RUNTIME.equals(id)) {
			return IJBoss7ManagerService.AS_VERSION_700;
		}
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
