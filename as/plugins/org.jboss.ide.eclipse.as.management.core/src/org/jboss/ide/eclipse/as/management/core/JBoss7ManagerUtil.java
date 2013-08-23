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

/**
 * A utility class for retrieving a IJBoss7ManagerService or 
 * the id of a IJBoss7ManagerService for a given IServer or IRuntimeType id 
 */
public class JBoss7ManagerUtil {

	/**
	 * Retrieve the appropriate IJBoss7ManagerService for a given IServer
	 * 
	 * @param server
	 * @return
	 * @throws JBoss7ManangerException
	 */
	public static IJBoss7ManagerService getService(IServer server) throws JBoss7ManangerException  {
		return getService(server.getRuntime().getRuntimeType().getId());
	}
	
	/**
	 * Retrieve the IJBoss7ManagerService with the given service id. 
	 * 
	 * @param serviceId
	 * @return
	 * @throws JBoss7ManangerException
	 */
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

	/**
	 * Retrieve the IJBoss7ManagerService for the given runtime type id
	 * 
	 * @param runtimeType   the runtimeType id of a org.eclipse.wst.server.core.IRuntimeType
	 * @return
	 * @throws JBoss7ManangerException
	 */
	public static IJBoss7ManagerService getService(String runtimeType) throws JBoss7ManangerException  {
		IJBossManagerServiceProvider serviceProvider = getServiceProvider(runtimeType);
		return serviceProvider == null ? null : serviceProvider.getManagerService();
	}

	/**
	 * Get the required management service id for a given IServer
	 * 
	 * @param server
	 * @return
	 */
	public static String getRequiredVersion(IServer server) {
		String id = server.getRuntime().getRuntimeType().getId();
		return getRequiredServiceVersion(id);
	}
	
	/**
	 * Get the required management service id for a given IServer
	 * 
	 * @param server
	 * @return
	 */
	public static String getRequiredServiceVersion(String runtimeType) {
		IJBossManagerServiceProvider serviceProvider = getServiceProvider(runtimeType);
		return serviceProvider == null ? null : serviceProvider.getManagerServiceId();
	}

	/**
	 * Get an IJBossManagerServiceProvider for a given runtime type id
	 * 
	 * @param runtimeType the runtimeType id of a org.eclipse.wst.server.core.IRuntimeType
	 * @return
	 */
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
	
	/**
	 * Dispose of a management service
	 * @param service
	 */
	public static void dispose(IJBoss7ManagerService service) {
		if (service != null) {
			service.dispose();
		}
	}
	
	/**
	 * Execute some command, request, or action on the appropriate 
	 * IJBoss7ManagerService for the given IServer. 
	 * 
	 * @param serviceAware	The action to be executed
	 * @param server   
	 * @return				
	 * @throws Exception
	 */
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
	
	/**
	 * An interface for an object capable of executing a command,
	 * request, or action on a IJBoss7ManagerService. 
	 * 
	 * @param <RESULT> The type of the result you are expecting from the service
	 */
	public static interface IServiceAware<RESULT> {
		public RESULT execute(IJBoss7ManagerService service) throws Exception;
	}
	
}
