/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
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

public class Wildfly100ExtendedProperties extends Wildfly90ExtendedProperties {
	public Wildfly100ExtendedProperties(IAdaptable obj) {
		super(obj);
	}
	@Override
	public String getRuntimeTypeVersionString() {
		return "10.0"; //$NON-NLS-1$
	}
	
	@Override
	public IExecutionEnvironment getDefaultExecutionEnvironment() {
		return EnvironmentsManager.getDefault().getEnvironment("JavaSE-1.8"); //$NON-NLS-1$
	}
	

	@Override
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		if( server != null)
			return new Wildfly100DefaultLaunchArguments(server);
		return new Wildfly100DefaultLaunchArguments(runtime);
	}
}
