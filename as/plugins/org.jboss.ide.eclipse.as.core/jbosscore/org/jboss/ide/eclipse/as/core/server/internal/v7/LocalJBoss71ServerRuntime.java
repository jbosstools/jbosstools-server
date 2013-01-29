/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.server.internal.v7;

import org.eclipse.core.runtime.IPath;
import org.jboss.ide.eclipse.as.core.util.IJBossRuntimeConstants;

/* This class no longer seems necessary, as all overridden methods match those of the superclass */
public class LocalJBoss71ServerRuntime extends LocalJBoss7ServerRuntime implements IJBossRuntimeConstants {

	@Override
	public String getDefaultRunArgs(IPath serverHome) {
		return getExtendedProperties().getDefaultLaunchArguments().getStartDefaultProgramArgs();
	}
		
	@Override
	public String getDefaultRunVMArgs(IPath serverHome) {
		return getExtendedProperties().getDefaultLaunchArguments().getStartDefaultVMArgs();
	}
}
