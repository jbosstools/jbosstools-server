/******************************************************************************* 
 * Copyright (c) 2016 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.wtp.core.server.behavior;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class CachedPublisherProfileBehavior extends ProfileServerBehavior {
	@Override
	public ISubsystemController getController(String system) throws CoreException {
		// publish caches, so we check that first
		if( SYSTEM_PUBLISH.equals(system)) {
			return getPublishController();
		}
		// pull from our profile method
		return getController(system, null);
		
	}
	
	@Override
	protected IPublishController getPublishController() throws CoreException {
		// If there's a current cache, use that one
		Object o = getSharedData(SYSTEM_PUBLISH);
		if( o != null ) {
			return (IPublishController)o;
		}
		return (IPublishController)getController(IPublishController.SYSTEM_ID, null);
	}
	
	/*
	 * JBoss servers require caching their publish controller to be more efficient
	 */
	
	@Override
	protected void publishStart(IProgressMonitor monitor) throws CoreException {
		// Cache the controller during publish start
		IPublishController controller = getPublishController();
		putSharedData(SYSTEM_PUBLISH, controller);
		controller.publishStart(monitor);
	}
	
	@Override
	protected void publishFinish(IProgressMonitor monitor) throws CoreException {
		IPublishController controller = getPublishController();
		try {
			controller.publishFinish(monitor);
		} finally {
			// clear the controller after publish finish
			putSharedData(SYSTEM_PUBLISH, null);
		}
	}
}
