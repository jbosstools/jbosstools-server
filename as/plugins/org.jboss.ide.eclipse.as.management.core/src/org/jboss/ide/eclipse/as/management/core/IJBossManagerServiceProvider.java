/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.management.core;


public interface IJBossManagerServiceProvider {
	/**
	 * Get a management service
	 * @param runtime
	 * @return
	 */
	public IJBoss7ManagerService getManagerService();
	
	/**
	 * Get the service id
	 * @return
	 */
	public String getManagerServiceId();
}
