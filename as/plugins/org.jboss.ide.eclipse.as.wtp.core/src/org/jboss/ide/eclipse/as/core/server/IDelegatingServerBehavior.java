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

import org.eclipse.core.runtime.IStatus;

@Deprecated
public interface IDelegatingServerBehavior extends IDeployableServerBehaviour {
	public IJBossBehaviourDelegate getDelegate();
	public IStatus canStart(String launchMode);
	public IStatus canStop(String launchMode);
	public IStatus canRestart(String launchMode);
	public void setServerStopping();
	public void setServerStopped();
	public void setServerStarting();
	public void setServerStarted();
}
