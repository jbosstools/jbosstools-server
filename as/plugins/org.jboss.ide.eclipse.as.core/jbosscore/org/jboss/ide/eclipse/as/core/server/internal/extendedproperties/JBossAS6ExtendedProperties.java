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
package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.internal.launching.environments.EnvironmentsManager;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.wst.server.core.IRuntimeType;
import org.jboss.ide.eclipse.as.core.server.IDefaultLaunchArguments;
import org.jboss.ide.eclipse.as.core.server.IServerModuleStateVerifier;
import org.jboss.ide.eclipse.as.core.server.internal.JBoss6ModuleStateVerifier;

public class JBossAS6ExtendedProperties extends JBossExtendedProperties {

	public JBossAS6ExtendedProperties(IAdaptable adaptable) {
		super(adaptable);
	}
	
	public String getRuntimeTypeVersionString() {
		return "6.x"; //$NON-NLS-1$
	}

	public IServerModuleStateVerifier getModuleStateVerifier() {
		return new JBoss6ModuleStateVerifier();
	}
	
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		if( server != null)
			return new JBoss6xDefaultLaunchArguments(server);
		return new JBoss6xDefaultLaunchArguments(runtime);
	}
	@Override
	public IExecutionEnvironment getDefaultExecutionEnvironment() {
		// NEW_SERVER_ADAPTER  Subclasses override this
		return EnvironmentsManager.getDefault().getEnvironment("JavaSE-1.6"); //$NON-NLS-1$
	}
	
	@Override
	public IExecutionEnvironment getMaximumExecutionEnvironment() {
		// NEW_SERVER_ADAPTER  Subclasses override this
		return null;
	}
}
