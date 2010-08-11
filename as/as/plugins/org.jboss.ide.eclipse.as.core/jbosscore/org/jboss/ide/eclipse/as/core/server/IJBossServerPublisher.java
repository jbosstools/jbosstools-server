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
package org.jboss.ide.eclipse.as.core.server;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;

/**
 * 
 * @author Rob Stryker
 *
 */
public interface IJBossServerPublisher {
	public static final int NO_PUBLISH = 0;
	public static final int INCREMENTAL_PUBLISH = 1;
	public static final int FULL_PUBLISH = 2;
	public static final int REMOVE_PUBLISH = 3;
	

	// pre-publish
	public boolean accepts(String method, IServer server, IModule[] module);
	// post publish
	public int getPublishState();
	public IStatus publishModule(
			IJBossServerPublishMethod method,
			IServer server, IModule[] module, 
			int publishType, IModuleResourceDelta[] delta, 
			IProgressMonitor monitor) throws CoreException;
}
