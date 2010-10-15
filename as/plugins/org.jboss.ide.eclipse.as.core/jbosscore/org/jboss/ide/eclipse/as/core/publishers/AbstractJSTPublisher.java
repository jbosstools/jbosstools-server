/******************************************************************************* 
 * Copyright (c) 2010 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.core.publishers;

import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.publishers.PublishUtil;
import org.jboss.ide.eclipse.as.core.server.IDeployableServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;

public abstract class AbstractJSTPublisher extends AbstractServerToolsPublisher {
	public AbstractJSTPublisher() {}
	
	/**
	 * This abstract publisher is only suitable for non force-zipped deployments
	 */
	public boolean accepts(String method, IServer server, IModule[] module) {
		if( module == null || (publishMethodSpecific() && !method.equals(getTargetedPublishMethodId())))
			return false;
		IDeployableServer ds = ServerConverter.getDeployableServer(server);
		return ds != null 
			&& ModuleCoreNature.isFlexibleProject(module[0].getProject())
			&& !ds.zipsWTPDeployments();
	}
	
	/**
	 * Return true if this publisher requires a specific 
	 * publish method / publish method id. 
	 * Clients are expected to override
	 */
	protected boolean publishMethodSpecific() {
		return false;
	}
	
	/**
	 * Get the publish method this publisher is associated with
	 * @return
	 */
	protected abstract String getTargetedPublishMethodId();
	
	/**
	 * JST projects require certain children (utility, etc) to be zipped up
	 */
	@Override
	protected boolean forceZipModule(IModule[] moduleTree) {
		return PublishUtil.deployPackaged(moduleTree);
	}
}
