/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
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
import org.eclipse.jdt.internal.launching.environments.EnvironmentsManager;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.jboss.ide.eclipse.as.core.server.IDefaultLaunchArguments;
import org.jboss.ide.eclipse.as.core.util.IJBossToolingConstants;

public class Wildfly80ExtendedProperties extends JBossAS710ExtendedProperties {
	public Wildfly80ExtendedProperties(IAdaptable obj) {
		super(obj);
	}
	public String getRuntimeTypeVersionString() {
		return "8.0"; //$NON-NLS-1$
	}
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		if( server != null)
			return new Wildfly80DefaultLaunchArguments(server);
		return new Wildfly80DefaultLaunchArguments(runtime);
	}
	public boolean requiresJDK() {
		return true;
	}
	public IExecutionEnvironment getDefaultExecutionEnvironment() {
		return EnvironmentsManager.getDefault().getEnvironment("JavaSE-1.7"); //$NON-NLS-1$
	}
	
	public String getJMXUrl() {
			return getJMXUrl(IJBossToolingConstants.WILDFLY8_MANAGEMENT_PORT_DEFAULT_PORT, "service:jmx:http-remoting-jmx"); //$NON-NLS-1$
	}
}
