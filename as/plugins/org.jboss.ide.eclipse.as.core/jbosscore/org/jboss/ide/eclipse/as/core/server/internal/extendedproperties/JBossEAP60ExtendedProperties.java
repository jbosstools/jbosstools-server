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
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.server.IDefaultLaunchArguments;

/**
 *
 */
public class JBossEAP60ExtendedProperties extends JBossAS710ExtendedProperties {

	public JBossEAP60ExtendedProperties(IAdaptable obj) {
		super(obj);
	}
	
	public String getRuntimeTypeVersionString() {
		return "6.0"; //$NON-NLS-1$
	}
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		if( server != null) {
			return new JBossEAP60LaunchArgs(server);
		}
		return new JBossEAP60LaunchArgs(runtime);
	}

	private class JBossEAP60LaunchArgs extends JBoss71DefaultLaunchArguments {
		public JBossEAP60LaunchArgs(IRuntime rt) {
			super(rt);
		}
		public JBossEAP60LaunchArgs(IServer rt) {
			super(rt);
		}
		protected String getMemoryArgs() {
			return "-Xms1303m -Xmx1303m -XX:MaxPermSize=256m "; //$NON-NLS-1$
		}
	}
	
	public boolean requiresJDK() {
		return true;
	}
	
	public boolean allowExplodedModulesInEars() {
		return allowExplodedModulesInWarLibs();
	}

	@Override
	public boolean allowExplodedModulesInWarLibs() {
		String version = getServerBeanLoader().getFullServerVersion();
		if (version == null)
			return false;
		else if (version.startsWith("6.0.0")) //$NON-NLS-1$
			return false; // 6.0.0 contains AS 7.1.2 which is bugged, 6.0.1 contains AS 7.1.3 which is fixed
		else
			return true;
	}
}
