/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
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

public class Wildfly130ExtendedProperties extends JBossAS710ExtendedProperties {
	public Wildfly130ExtendedProperties(IAdaptable obj) {
		super(obj);
	}
	@Override
	public String getRuntimeTypeVersionString() {
		return "13.0"; //$NON-NLS-1$
	}
	
	@Override
	public String getManagerServiceId() {
		return IJBoss7ManagerService.WILDFLY_VERSION_110;
	}

	@Override
	public IExecutionEnvironment getDefaultExecutionEnvironment() {
		return JavaRuntime.getExecutionEnvironmentsManager().getEnvironment("JavaSE-1.8"); //$NON-NLS-1$
	}
	
	@Override
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		if( server != null)
			return new Wildfly100DefaultLaunchArguments(server);
		return new Wildfly100DefaultLaunchArguments(runtime);
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
	 * Wildfly 11 appears to work through java 9 to varying degrees
	 */
	@Override
	public IExecutionEnvironment getMaximumExecutionEnvironment() {
		return null;
	}
}
