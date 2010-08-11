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
package org.jboss.ide.eclipse.as.core.publishers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.jboss.ide.eclipse.as.core.JBossServerCorePlugin;
import org.jboss.ide.eclipse.as.core.Messages;
import org.jboss.ide.eclipse.as.core.extensions.events.IEventCodes;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublishMethod;
import org.jboss.ide.eclipse.as.core.server.IJBossServerPublisher;

/**
 *
 * @author rob.stryker@jboss.com
 */
public class NullPublisher implements IJBossServerPublisher {

	public int getPublishState() {
		return IServer.PUBLISH_STATE_NONE;
	}
	public boolean accepts(String method, IServer server, IModule[] module) {
		return true;
	}

	public IStatus publishModule(
			IJBossServerPublishMethod method,
			IServer server, IModule[] module, 
			int publishType, IModuleResourceDelta[] delta, 
			IProgressMonitor monitor) throws CoreException {
		return new Status(IStatus.WARNING, JBossServerCorePlugin.PLUGIN_ID,  
				IEventCodes.NO_PUBLISHER_ROOT_CODE, 
				NLS.bind(Messages.NoPublisherFound, module[module.length-1]), null);
	}
}
