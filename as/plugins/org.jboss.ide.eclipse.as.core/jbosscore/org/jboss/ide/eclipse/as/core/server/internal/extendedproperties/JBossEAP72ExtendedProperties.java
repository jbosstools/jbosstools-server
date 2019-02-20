/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.jboss.ide.eclipse.as.core.server.IDefaultLaunchArguments;
import org.jboss.ide.eclipse.as.management.core.IJBoss7ManagerService;

public class JBossEAP72ExtendedProperties extends JBossAS710ExtendedProperties {
	public JBossEAP72ExtendedProperties(IAdaptable obj) {
		super(obj);
	}
	@Override
	public String getRuntimeTypeVersionString() {
		return "7.2"; //$NON-NLS-1$
	}
	
	@Override
	public IExecutionEnvironment getDefaultExecutionEnvironment() {
		return JavaRuntime.getExecutionEnvironmentsManager().getEnvironment("JavaSE-1.8"); //$NON-NLS-1$
	}
	
	@Override
	public String getManagerServiceId() {
		return IJBoss7ManagerService.WILDFLY_VERSION_110;
	}
	
	@Override
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		if( server != null)
			return new JBossEAP72DefaultLaunchArguments(server);
		return new JBossEAP72DefaultLaunchArguments(runtime);
	}
	@Override
	public String getJMXUrl() {
			return getJMXUrl(9990, "service:jmx:remote+http"); //$NON-NLS-1$
	}
	
	@Override
	public boolean requiresJDK() {
		return true;
	}
	
	@Override
	public boolean allowExplodedModulesInWarLibs() {
		return true;
	}
	@Override
	public boolean allowExplodedModulesInEars() {
		return true;
	}
	/**
	 * EAP 7.1 appears to work through java 9 to varying degrees
	 */
	@Override
	public IExecutionEnvironment getMaximumExecutionEnvironment() {
		return null;
	}

}
