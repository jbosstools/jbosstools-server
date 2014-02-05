/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.wst.server.core.model.ServerDelegate;
import org.jboss.ide.eclipse.as.core.server.IDefaultLaunchArguments;
import org.jboss.ide.eclipse.as.core.server.IJMXURLProvider;
import org.jboss.ide.eclipse.as.core.server.IManagementPortProvider;
import org.jboss.ide.eclipse.as.core.server.bean.JBossServerType;
import org.jboss.ide.eclipse.as.core.server.bean.ServerBeanLoader;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeResourceConstants;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class JBossAS710ExtendedProperties extends JBossAS7ExtendedProperties implements IJMXURLProvider {

	public JBossAS710ExtendedProperties(IAdaptable obj) {
		super(obj);
	}
	
	public String getRuntimeTypeVersionString() {
		return "7.1"; //$NON-NLS-1$
	}

	public int getJMXProviderType() {
		return JMX_OVER_AS_MANAGEMENT_PORT_PROVIDER;
	}
	public boolean runtimeSupportsBindingToAllInterfaces() {
		return true;
	}
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		if( server != null) {
			ServerBeanLoader l = new ServerBeanLoader(server.getRuntime().getLocation().toFile());
			if( l.getServerType().getName().equals(JBossServerType.AS7GateIn.getName())) {
				String version = l.getServerBean().getVersion();
				if( JBossServerType.V3_3.equals(version) 
						|| JBossServerType.V3_4.equals(version) ) {
					return new GateIn33AS71DefaultLaunchArguments(server);
				}
			}
			return new JBoss71DefaultLaunchArguments(server);
		}
		return new JBoss71DefaultLaunchArguments(runtime);
	}
	
	@Override
	public String getJBossAdminScript() {
		return IJBossRuntimeResourceConstants.AS_71_MANAGEMENT_SCRIPT;
	}
	
	/**
	 * @since 3.0
	 */
	public String getJMXUrl() {
		return getJMXUrl(9999, "service:jmx:remoting-jmx"); //$NON-NLS-1$
	}
	
	/**
	 * @since 3.0
	 */
	protected String getJMXUrl(int defaultPort, String jmxScheme) {
		ServerDelegate sd = (ServerDelegate)server.loadAdapter(ServerDelegate.class, null);
		int port = (sd instanceof IManagementPortProvider) ? 
					((IManagementPortProvider)sd).getManagementPort() : defaultPort;
		String url = jmxScheme + "://" + server.getHost() + ":" + port;  //$NON-NLS-1$ //$NON-NLS-2$
		return url;
	}


}
