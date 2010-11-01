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
package org.jboss.ide.eclipse.as.ssh.server;

import org.jboss.ide.eclipse.as.core.server.IDeployableServer;

public interface ISSHDeploymentConstants {
	public static final String HOSTS_FILE = "org.jboss.ide.eclipse.as.ssh.server.hostsFile"; //$NON-NLS-1$
	public static final String DEPLOY_DIRECTORY = "org.jboss.ide.eclipse.as.ssh.server.deployDirectory"; //$NON-NLS-1$
	public static final String USERNAME = "org.jboss.ide.eclipse.as.ssh.server.username"; //$NON-NLS-1$
	public static final String PASSWORD = "org.jboss.ide.eclipse.as.ssh.server.password"; //$NON-NLS-1$
	public static final String SSH_ZIP_DEPLOYMENTS_PREF_LEGACY = "org.jboss.ide.eclipse.as.ssh.server.zipDeploymentsPreference"; //$NON-NLS-1$
	public static final String ZIP_DEPLOYMENTS_PREF = IDeployableServer.ZIP_DEPLOYMENTS_PREF;
}
