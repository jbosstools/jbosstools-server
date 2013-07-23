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

import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;

public class JBoss7ManagerUtil {

	public static IJBoss7ManagerService getService(IServer server) throws JBoss7ManangerException  {
		return getService(server.getRuntime().getRuntimeType().getId());
	}
	
	public static IJBoss7ManagerService getManagerService(String serviceId) throws JBoss7ManangerException {
		try {
			BundleContext context = AS7ManagementActivator.getContext();
			JBoss7ManagerServiceProxy proxy = new JBoss7ManagerServiceProxy(context, 
					serviceId);
			proxy.open();
			return proxy;
		} catch(InvalidSyntaxException ise) {
			throw new JBoss7ManangerException(ise);
		}
	}

	public static IJBoss7ManagerService getService(String runtimeType) throws JBoss7ManangerException  {
		IJBossManagerServiceProvider serviceProvider = getServiceProvider(runtimeType);
		return serviceProvider == null ? null : serviceProvider.getManagerService();
	}

	public static String getRequiredVersion(IServer server) {
		String id = server.getRuntime().getRuntimeType().getId();
		return getRequiredServiceVersion(id);
	}
	
	public static String getRequiredServiceVersion(String runtimeType) {
		IJBossManagerServiceProvider serviceProvider = getServiceProvider(runtimeType);
		return serviceProvider == null ? null : serviceProvider.getManagerServiceId();
	}

	protected static IJBossManagerServiceProvider getServiceProvider(String runtimeType) {
		if( runtimeType != null ) {
			IRuntimeType rtType = ServerCore.findRuntimeType(runtimeType);
			if( rtType != null ) {
				IJBossManagerServiceProvider serviceProvider = (IJBossManagerServiceProvider)Platform.getAdapterManager()
							.getAdapter(rtType, IJBossManagerServiceProvider.class);
				return serviceProvider;
			}
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
