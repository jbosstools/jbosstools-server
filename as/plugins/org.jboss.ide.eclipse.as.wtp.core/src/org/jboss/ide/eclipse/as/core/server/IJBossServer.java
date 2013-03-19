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
package org.jboss.ide.eclipse.as.core.server;

public interface IJBossServer extends IDeployableServer {
	public String getHost();
	public String getUsername();
	public String getPassword();
	public int getJNDIPort();
	public int getJBossWebPort();
	public IJBossServerRuntime getRuntime();
}
