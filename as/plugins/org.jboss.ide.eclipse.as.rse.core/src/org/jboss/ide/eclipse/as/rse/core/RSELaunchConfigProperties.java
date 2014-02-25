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
package org.jboss.ide.eclipse.as.rse.core;

import org.jboss.ide.eclipse.as.core.server.launch.CommandLineLaunchConfigProperties;

/**
 * @author André Dietisheim
 */
public class RSELaunchConfigProperties extends CommandLineLaunchConfigProperties {
	// Existing keys for legacy rse usecase
	private static class RSEKeySet extends KeySet {
		public RSEKeySet() {
			DEFAULT_STARTUP_COMMAND = "org.jboss.ide.eclipse.as.rse.core.RSEJBossStartLaunchDelegate.DEFAULT_STARTUP_COMMAND"; //$NON-NLS-1$
			STARTUP_COMMAND = "org.jboss.ide.eclipse.as.rse.core.RSEJBossStartLaunchDelegate.STARTUP_COMMAND";//$NON-NLS-1$
			DEFAULT_SHUTDOWN_COMMAND = "org.jboss.ide.eclipse.as.rse.core.RSEJBossStartLaunchDelegate.DEFAULT_SHUTDOWN_COMMAND";//$NON-NLS-1$
			SHUTDOWN_COMMAND = "org.jboss.ide.eclipse.as.rse.core.RSEJBossStartLaunchDelegate.SHUTDOWN_COMMAND";//$NON-NLS-1$
			DETECT_STARTUP_COMMAND = "org.jboss.ide.eclipse.as.rse.core.RSEJBossStartLaunchDelegate.DETECT_STARTUP_COMMAND";//$NON-NLS-1$
			DETECT_SHUTDOWN_COMMAND = "org.jboss.ide.eclipse.as.rse.core.RSEJBossStartLaunchDelegate.DETECT_SHUTDOWN_COMMAND";//$NON-NLS-1$
		}
	}
	
	public static KeySet RSE_KEYSET = new RSEKeySet();

	public RSELaunchConfigProperties() {
		super(RSE_KEYSET);
	}
}
