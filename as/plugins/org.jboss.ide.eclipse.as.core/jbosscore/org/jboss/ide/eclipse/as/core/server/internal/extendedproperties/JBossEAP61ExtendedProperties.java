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
import org.jboss.ide.eclipse.as.core.server.IDefaultLaunchArguments;

public class JBossEAP61ExtendedProperties extends JBossEAP60ExtendedProperties {
	public JBossEAP61ExtendedProperties(IAdaptable obj) {
		super(obj);
	}
	
	public String getRuntimeTypeVersionString() {
		return "6.1+"; //$NON-NLS-1$
	}
	
	public IDefaultLaunchArguments getDefaultLaunchArguments() {
		if( server != null)
			return new JBoss72Eap61DefaultLaunchArguments(server);
		return new JBoss72Eap61DefaultLaunchArguments(runtime);
	}
	
	@Override
	public boolean allowExplodedModulesInWarLibs() {
		return true;
	}

}
