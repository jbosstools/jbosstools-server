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
package org.jboss.ide.eclipse.as.core.server.internal.v7;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class JBoss7ManagementUtil {
	public static final String SERVER_VERSION_PROPERTY = "as.version"; //$NON-NLS-1$
	
	public static final String SERVICE_VERSION_70 = "org.jboss.ide.eclipse.as.management.as7.service"; //$NON-NLS-1$

	public static IJBoss7ManagementService findManagementService(IServer server) {
		BundleContext context = JBossServerCorePlugin.getContext();

		//		String tmp = "org.jboss.ide.eclipse.as.management.as7.service"; //$NON-NLS-1$
		//		String clazz = "org.jboss.ide.eclipse.as.management.as7.deployment.JBossManagementService"; //$NON-NLS-1$
		//		String iface = "IJBoss7ManagementService"; //$NON-NLS-1$
		//		String clazz3 = "org.jboss.ide.eclipse.as.core.server.internal.v7." + iface; //$NON-NLS-1$
		String requiredService = getRequiredServiceName(server);
		if (requiredService == null)
			return null;

		try {
			Collection<ServiceReference<IJBoss7ManagementService>> references = context.getServiceReferences(
					IJBoss7ManagementService.class, null);
			for (Iterator<ServiceReference<IJBoss7ManagementService>> iterator = references.iterator(); 
					iterator.hasNext();) {
				ServiceReference<IJBoss7ManagementService> reference = iterator.next();
				Object compName = reference.getProperty("component.name"); //$NON-NLS-1$
				if (requiredService.equals(compName)) {
					return context.getService(reference);
				}
			}
		} catch (InvalidSyntaxException ise) {
		}
		return null;
	}

	private static String getRequiredServiceName(IServer s) {
		// TODO if required, make sure to add new versions here
		return SERVICE_VERSION_70;
	}
}
